package com.logica.ngph.esb.servicesImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.utils.NGPHUtil;
import com.logica.ngph.esb.AuditServiceClient;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.CoversService;
import com.logica.ngph.esb.services.ServiceController;
import com.logica.ngph.utils.ApplicationContextProvider;

/**
 * @author guptarb
 * This is Service that will be invoked for Received MT 910 for Covers.
 */
public class CoversServiceImpl implements CoversService{

	static Logger logger = Logger.getLogger(CoversServiceImpl.class);
	private SwiftParserDao swiftParserDao;
	private ServiceController serviceController;
	
	/**
	 * @param serviceController the serviceController to set
	 */
	public void setServiceController(ServiceController serviceController) {
		this.serviceController = serviceController;
	}
	
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) 
	{
		this.swiftParserDao = swiftParserDao;
	}
	
	public static void main(String[] args) 
	{
/*		NgphCanonical obj = new NgphCanonical();
		
		obj.setMsgRef("a09c7653-32e5-4ebe-b4c3-7a497d733625");
		obj.setBeneficiaryCustAcct("987654321");
		obj.setRelReference("MTO-793-001481"); 
		obj.setMsgDirection("I");
		obj.setMsgChnlType("SWIFT");
		obj.setSrcMsgSubType("910");
		obj.setSrcMsgType("MT");
		obj.setMsgCurrency("INR");
		obj.setMsgAmount(new BigDecimal("300011"));
		String date = "110215";// 150211
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		try
		{
		Date dt = sdf.parse(date);
		Timestamp timestamp = new Timestamp(dt.getTime());
		obj.setMsgValueDate(timestamp);
		
		new CoversServiceImpl().doProcess(obj);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
*/
	}
	
	private String generateTxnRef()throws Exception
	{
		// 4(Ngph bic first 4 digits) + yyyyMMdd+ 4 digit sequence no(from Ta_sequences).

		StringBuilder txnRef = new StringBuilder();
		String bic = swiftParserDao.retrieveBICDetails();
		logger.info(bic);
		txnRef.append(bic.substring(0,4));
		
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(NgphEsbConstants.NGPH_TXNREF_DATE_FORMAT);
		String date = sdf.format(dt);
		
		logger.info(date);
		txnRef.append(date);
		
		String seq_num = swiftParserDao.getSequenceNumber(NgphEsbConstants.SEQUENCE_KEY_TXN);
		
		String val = "0000" + seq_num;
		
		String final_Seq_Val = val.substring(val.length()-4, val.length());
		
		txnRef.append(final_Seq_Val);
		return txnRef.toString();
	}
	
	/**
	 * This method is called when isPartyCorrospondent is false  and currency is not supported.
	 * Hence we have to send the Out bound 202 COV Canonical Object.
	 * @param canOf103
	 * @return canOf202
	 */
	private NgphCanonical constructCanonical(NgphCanonical canonicalOf103)throws Exception
	{
		NgphCanonical canObj202OutBound = new NgphCanonical();
		
		// Generic members (Hard coded values)
		canObj202OutBound.setMsgRef(NGPHUtil.generateUUID());
		canObj202OutBound.setMsgHost(swiftParserDao.getHostID());// 9001
		canObj202OutBound.setMsgChnlType(canonicalOf103.getMsgChnlType());//SWIFT
		canObj202OutBound.setSrcMsgType(canonicalOf103.getSrcMsgType());//MT
		canObj202OutBound.setSrcMsgSubType("202");
		canObj202OutBound.setMsgDirection("O");
		canObj202OutBound.setOrderingType("F");
		canObj202OutBound.setRelUid(canonicalOf103.getMsgRef());// msg ref of 103
		
		// Bussiness rules
		//20
		canObj202OutBound.setTxnReference(generateTxnRef());
		
		//21
		canObj202OutBound.setRelReference(canonicalOf103.getTxnReference());
		
		//13
		canObj202OutBound.setDrDateTime(canonicalOf103.getDrDateTime());
		canObj202OutBound.setCrDateTime(canonicalOf103.getCrDateTime());
		canObj202OutBound.setClsDateTime(canonicalOf103.getClsDateTime());
		
		//32
		canObj202OutBound.setMsgCurrency(canonicalOf103.getMsgCurrency());
		canObj202OutBound.setMsgAmount(canonicalOf103.getMsgAmount());
		canObj202OutBound.setMsgValueDate(canonicalOf103.getMsgValueDate());
		
		//52
		canObj202OutBound.setOrderingInstitution(canonicalOf103.getOrderingInstitution());
		canObj202OutBound.setOrderingInstitutionAcct(canonicalOf103.getOrderingInstitutionAcct());
		
		// 53 If field 53 is present i.e its canonical value exists
		if(canonicalOf103.getSenderCorrespondent()!=null)
		{
			canObj202OutBound.setSenderCorrespondent(canonicalOf103.getSenderCorrespondent());
			canObj202OutBound.setSenderCorrespondentAcct(canonicalOf103.getSenderCorrespondentAcct());
		}
		// We will send NGPH BIC
		else
		{
			canObj202OutBound.setSenderCorrespondent(swiftParserDao.retrieveBICDetails());
		}
		
		//54
		canObj202OutBound.setReceiverCorrespondent(canonicalOf103.getReceiverCorrespondent());
		canObj202OutBound.setReceiverCorrespondentAcct(canonicalOf103.getReceiverCorrespondentAcct());
		
		//56
		canObj202OutBound.setIntermediary1BankName(canonicalOf103.getIntermediary1BankName());
		canObj202OutBound.setIntermediary1AgentAcct(canonicalOf103.getIntermediary1AgentAcct());
		
		//57 needs to be set as blank
		
		//58
		canObj202OutBound.setAccountWithInstitution(canonicalOf103.getAccountWithInstitution());
		canObj202OutBound.setAccountWithInstitutionAcct(canonicalOf103.getAccountWithInstitutionAcct());
		
		//72
		canObj202OutBound.setPrevInstructingBank(canonicalOf103.getPrevInstructingBank());
		canObj202OutBound.setInstructionsForCrdtrAgtText(canonicalOf103.getInstructionsForCrdtrAgtText());
		canObj202OutBound.setInstructionsForNextAgtCode(canonicalOf103.getInstructionsForNextAgtCode());

		// Sequence B set all variables of 103
		
		return canObj202OutBound;
	}
	// Generic Event Logger Method.
	private static void performAuditEventLogging(String eventId, String msgRef)throws Exception
	{
        AuditServiceClient auditServiceClient = new AuditServiceClient();
        EventAudit audit = new EventAudit();
        audit.setAuditEventId(eventId);
        audit.setAuditMessageRef(msgRef);
        audit.setAuditSource(CoversServiceImpl.class.toString());
        auditServiceClient.dbPollerQueueCall(null, "AUDIT", audit);
	}

	/**
	 * @param canonical
	 * This method is the main method that will 
	 * do all the processing as per business needs.
	 * 
	 * This is the Entry Point of the Service.
	 */
	public void doProcess(NgphCanonical canonical)throws Exception
	{
		if(canonical !=null)
		{
		 	//ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		 	//swiftParserDao = (SwiftParserDao)context.getBean("swiftParserDao");
		 	
			swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
		 	
			// Fetch Direction, SrcMsgType and SrcSubMSgType
		 	String direction = canonical.getMsgDirection();
		 	String srcMsgType = canonical.getSrcMsgType();
		 	String srcMsgSubType = canonical.getSrcMsgSubType();
		 	String msgChnlType = canonical.getMsgChnlType();
		 	// TODO add a covers variable in Canonical and its respective column value in TA_Messages_tx
		 	boolean covers =true;
		 	
		 	// First Story of Covers(REC Inbound 910)
		 	// if direction="I" and srcMsgType = "MT" and srcSubMsgType = "910" and Channel Type = "SWIFT"
		 	if(direction.equalsIgnoreCase("I") && srcMsgSubType.equalsIgnoreCase("910") && srcMsgType.equalsIgnoreCase("MT") && msgChnlType.equalsIgnoreCase("SWIFT"))
		 	{
		 		// Fetch MSgRef of 910
		 		String msgRef910 = canonical.getMsgRef();
		 		// Fetch 25 field of 910
		 		String beneficiaryCustAcct = canonical.getBeneficiaryCustAcct();
		 		boolean resultAcc = swiftParserDao.validateTa_Accounts(beneficiaryCustAcct);
		 		
		 		logger.info("Value returened from Accounts table : " + resultAcc);
		 		
		 		if(resultAcc==true)
		 		{
		 			//Fetch required field of 910
		 			String relReference = canonical.getRelReference();
		 			String msgCurrency = canonical.getMsgCurrency();
		 			BigDecimal msgAmount = canonical.getMsgAmount();
		 			Timestamp msgValueDate = canonical.getMsgValueDate();

		 			if(swiftParserDao.validateTa_Msg_Tx(relReference, msgChnlType, msgCurrency , msgAmount, msgValueDate)==null)
		 			{
		 				logger.info("No Action is Required by CoversRec910ServiceImpl as there was no data in MSg table for Corrosponding MT 103");
			 			swiftParserDao.updateTa_Msg_TxforExcp(msgRef910, "16");
			 			// 'NGPHCVR910Ev002','There was no data for Corrosponding 103 for MSgref {0}',0,'W'
			 			performAuditEventLogging("NGPHCVR910Ev002", msgRef910);
		 			}
		 			else // Value Exists in Msg_tx table for Corosponding MT 103
		 			{
		 				String resultMsg_Tx = swiftParserDao.validateTa_Msg_Tx(relReference, msgChnlType, msgCurrency , msgAmount, msgValueDate);
		 				swiftParserDao.updateTa_Msg_tx(msgRef910, resultMsg_Tx, "14");
			 			// 'NGPHCVR910Ev003','Msg status updated for 103 for MSgref {0}',0,'W'
		 				performAuditEventLogging("NGPHCVR910Ev003", resultMsg_Tx);
		 				
			 			// 'NGPHCVR910Ev004','Msg status updated for 910 for MSgref {0}',0,'W'
		 				performAuditEventLogging("NGPHCVR910Ev004", msgRef910);

		 			}
		 		}
		 		else
		 		{
		 			logger.info("No Action is Required by CoversRec910ServiceImpl as there was no account Info in Ta_Acc table");
		 			swiftParserDao.updateTa_Msg_TxforExcp(msgRef910, "16");
		 			// 'NGPHCVR910Ev001','There was no account Info in Ta_Acc table for MSgref {0}',0,'W'
		 			performAuditEventLogging("NGPHCVR910Ev001", msgRef910);
		 			
		 		}
		 	}
		 	// Another Story
		 	// Outbound 103 create Outbound 202COV 
		 	// if direction="O" and srcMsgType = "MT" and srcSubMsgType = "103" and Channel Type = "SWIFT"
		 	else if(direction.equalsIgnoreCase("O") && srcMsgSubType.equalsIgnoreCase("103") && srcMsgType.equalsIgnoreCase("MT") && msgChnlType.equalsIgnoreCase("SWIFT"))
		 	{
		 		// Fetch whether 202 Covers Generation is required
		 		String isCovGenReq = swiftParserDao.getCovGen();
		 		
		 		// If covers generation is required
		 		if(isCovGenReq.equalsIgnoreCase("1"))
		 		{
		 		// Check for null for AccountWithInstitutionAcct
		 		if(canonical.getAccountWithInstitutionAcct()!=null && StringUtils.isNotBlank(canonical.getAccountWithInstitutionAcct()) && StringUtils.isNotEmpty(canonical.getAccountWithInstitutionAcct()))
		 		{
		 			String accountWithInstitutionAcct = canonical.getAccountWithInstitutionAcct(); 
		 			
		 			if(swiftParserDao.isCurrSuptByAcct(accountWithInstitutionAcct)!=null)
		 			{
		 				// Currency is supported hence take no action and Exit
		 				logger.info("Currency is supported hence take no action");
		 			}
		 			else
		 			{
		 				// Check ispartyCorrospodent true or not
		 				logger.info("Currency is not supported Check for IsParty Corrospondent");
		 				int res = swiftParserDao.isPtyCorrspndtByAcct(accountWithInstitutionAcct);
		 				logger.info("The result from parties table is : " + res + " for account number : " + accountWithInstitutionAcct);
		 				
		 				if(res==0)
		 				{
		 					//IsPartyCOrrospondent is false hence create 202 Canonical
		 					logger.info("IsPartyCOrrospondent is false hence create 202 Canonical");
		 					
		 					// Will return 202 Outbound Canonical taking input as Inbound 103 canonical 
		 					NgphCanonical finalCanonicalOf202 = constructCanonical(canonical);
		 					
		 					// insert the canonical obj to DB
		 					swiftParserDao.insertParsedMessage(finalCanonicalOf202);

		 					// pass the control to Service Controller
		 					//serviceController.performPaymentProcessing(finalCanonicalOf202);
		 				}
		 				else
		 				{
		 					// IsPartyCOrrospondent is true hence take no action
		 					logger.info("IsPartyCOrrospondent is true hence take no action");
		 				}
		 			}
		 			
		 		}
		 		else
		 		{
		 			// check for null for AccountWithInstitution
			 		if(canonical.getAccountWithInstitution()!=null && StringUtils.isNotBlank(canonical.getAccountWithInstitution()) && StringUtils.isNotEmpty(canonical.getAccountWithInstitution()))
			 		{
			 			String accountWithInstitution = canonical.getAccountWithInstitution();

			 			// Identifying whether its a BIC or Name and Address
			 			if(accountWithInstitution.length()>=8 && accountWithInstitution.length()<=11)
			 			{
			 				// It is BIC of Field 57A
			 				int res = swiftParserDao.isPtyCorrspndtByBIC(accountWithInstitution);
			 				logger.info("The result from parties table is : " + res + " for account number : " + accountWithInstitution);

			 				if(res==0)
			 				{
			 					//IsPartyCOrrospondent is false hence create 202 Canonical
			 					logger.info("IsPartyCOrrospondent is false hence create 202 Canonical");
			 					
			 					// Check for Currency support
			 					if(swiftParserDao.isCurrSuptByBIC(accountWithInstitution)!=null)
					 			{
					 				// Currency is supported hence take no action and Exit
					 				logger.info("Currency is supported hence take no action");
					 			}
			 					else
			 					{
					 				// Currency is supported hence take no action and Exit
					 				logger.info("Currency is not supported hence create canonical");

				 					// Will return 202 Outbound Canonical taking input as Inbound 103 canonical 
				 					NgphCanonical finalCanonicalOf202 = constructCanonical(canonical);
				 					
				 					// insert the canonical obj to DB
				 					swiftParserDao.insertParsedMessage(finalCanonicalOf202);
	
				 					// pass the control to Service Controller
				 					//serviceController.performPaymentProcessing(finalCanonicalOf202);
			 					}
			 				}
			 				else
			 				{
			 					// IsPartyCOrrospondent is true hence take no action
			 					logger.info("IsPartyCOrrospondent is presnt hence take no action");
			 				}
			 			}
			 			else
			 			{
			 				// It is Name and Address of Field 57D
			 				int res = swiftParserDao.isPtyCorrspndtByNameAdd(accountWithInstitution);
			 				logger.info("The result from parties table is : " + res + " for account number : " + accountWithInstitution);

			 				if(res==0)
			 				{
			 					//IsPartyCOrrospondent is false hence create 202 Canonical
			 					logger.info("IsPartyCOrrospondent is false hence create 202 Canonical");
			 					
			 					// Check for Currency support
			 					if(swiftParserDao.isCurrSuptByNameAdd(accountWithInstitution)!=null)
					 			{
					 				// Currency is supported hence take no action and Exit
					 				logger.info("Currency is supported hence take no action");
					 			}
			 					else
			 					{
					 				// Currency is supported hence take no action and Exit
					 				logger.info("Currency is not supported hence create canonical");

				 					// Will return 202 Outbound Canonical taking input as Inbound 103 canonical 
				 					NgphCanonical finalCanonicalOf202 = constructCanonical(canonical);
				 					
				 					// insert the canonical obj to DB
				 					swiftParserDao.insertParsedMessage(finalCanonicalOf202);
	
				 					// pass the control to Service Controller
				 					//serviceController.performPaymentProcessing(finalCanonicalOf202);
			 					}
			 				}
			 				else
			 				{
			 					// IsPartyCOrrospondent is true hence take no action
			 					logger.info("IsPartyCOrrospondent is present hence take no action");
			 				}
			 			}
			 		}
		 		}
		 	}// if for initializationm
		 	else
		 	{
		 		logger.info("No Action is Required by CoversServiceImpl for 202 covers as initializationm was false");
		 	}
		 }

		 	// None of the Conditions met for Covers
		 	else
		 	{
		 		logger.info("No Action is Required by CoversServiceImpl as none of the Conditions met for Covers Requirement");
		 	}
		}
		// Null Canonical Object Received
		else
		{
			logger.warn("Null Canonical Obj Received by CoversServiceImpl");
		}

	}
}
