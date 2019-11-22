package com.logica.ngph.action;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.ChargesDetailsDto;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.common.enums.SwiftFieldsEnum;
import com.logica.ngph.common.utils.NGPHUtil;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.AcknowledgementsService;
import com.logica.ngph.esb.services.AutoRouterService;
import com.logica.ngph.esb.services.NetworkValidationService;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.esb.services.ServiceController;
import com.logica.ngph.esb.servicesImpl.AutoRouterServiceImpl;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock1;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock2;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock3;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock4;
import com.logica.ngph.finance.messageparser.swift.model.WifeSwiftMessage;
import com.logica.ngph.finance.messageparser.swift.parser.SwiftParser;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.utils.NGPHEsbUtils;
import com.logica.ngph.validators.services.IMsgFieldDataInitializer;
import com.logica.ngph.validators.services.IMsgFormatDataInitializer;
import com.logica.ngph.validators.services.IMsgFormatValidator;

/**
 * @author guptarb
 * 
 * A Generic Parser for Swift Messages.
 *
 */
public class SwiftMSgAction extends AbstractActionLifecycle{
	
	protected ConfigTree	_config;
	private NgphCanonical canonicalObj = null;
	private ChargesDetailsDto chds = new ChargesDetailsDto();
	private List<ChargesDetailsDto> chargeDetails = new ArrayList<ChargesDetailsDto>();
	
	private WifeSwiftMessage wifeSwiftObj = null;
	private SwiftBlock4 swiftblock4 = null;
	private SwiftBlock2 swiftblock2 = null;
	private SwiftBlock3 swiftblock3 = null;
	private SwiftBlock1 swiftblock1 = null;

	private String providerESB = null;
	
	private String msgRef = null;
	private String mesType = null;
	private String swiftDirection = null;
	private String payPriority = null;
	private String blockVal = null;
	private String receiverAdd = null;
	private String mes = null;
	private String msgDirection =null;
	private String subMsgType=null;
	private String sndrTxnId =null;
	private String sndrPymPr =null;
	private String custTxnRef =null;
	private String sndrBank =null;
	private String rcvBank =null;
	private String pymntAcceptedTime =null;
	private String txnReference =null;
	private String relReference =null;
	private String msgValueDate =null;
	private String msgCurrency =null;
	private String msgAmount =null;
	private String orderingInstitution =null;
	private String senderCorrespondent  =null;
	private String receiverCorrespondent =null;
	private String intermediary1Bank =null;
	private String accountWithInstitution =null;
	private String beneficiaryInstitution = null;
	private String beneficiaryInstitutionName=null;
	private String instructionsForCrdtrAgtCode=null;
	private String instructionsForCrdtrAgtText=null;
	private String chargeBearer=null;
	private String instructionsForNextAgtCode =null;
	private String instructionsForNextAgtText =null;
	private String hostID = null;
	private boolean bParserError = false;
	
	static Logger logger = Logger.getLogger(SwiftMSgAction.class);
	public SwiftMSgAction (ConfigTree config) { _config = config; } 
	public SwiftMSgAction () { } 
	
	// Creating Dependency Injection(IOC)
	private IMsgFormatValidator msgFormatValidator;
	private ServiceController serviceController;
	private SwiftParserDao swiftParserDao;
	private EsbServiceDao esbParserDao;
	private AcknowledgementsService acknowledgementsService;
	private AutoRouterService autoRouterService;
	private NetworkValidationService networkValdationService;

	
	public AutoRouterService getAutoRouterService() {
		return autoRouterService;
	}
	public void setAutoRouterService(AutoRouterService autoRouterService) {
		this.autoRouterService = autoRouterService;
	}
	/**
	 * @param acknowledgementsService the acknowledgementsService to set
	 */
	public void setAcknowledgementsService(
			AcknowledgementsService acknowledgementsService) {
		this.acknowledgementsService = acknowledgementsService;
	}

	private static IMsgFieldDataInitializer msgFieldDataInitializer;
	/**
	 * @param msgFieldDataInitializer the msgFieldDataInitializer to set
	 */
	public static void setMsgFieldDataInitializer(
			IMsgFieldDataInitializer msgFieldDataInitializer) {
		SwiftMSgAction.msgFieldDataInitializer = msgFieldDataInitializer;
	}
	/**
	 * @param msgFormatDataInitializer the msgFormatDataInitializer to set
	 */
	public static void setMsgFormatDataInitializer(
			IMsgFormatDataInitializer msgFormatDataInitializer) {
		SwiftMSgAction.msgFormatDataInitializer = msgFormatDataInitializer;
	}

	private static IMsgFormatDataInitializer msgFormatDataInitializer;	
	/**
	 * @param swiftParserDao the swiftParserDao to set
	 */
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) {
		this.swiftParserDao = swiftParserDao;
	}
	/**
	 * @param msgFormatValidator the msgFormatValidator to set
	 */
	public void setMsgFormatValidator(IMsgFormatValidator msgFormatValidator) {
		this.msgFormatValidator = msgFormatValidator;
	}
	
	//Provide the Setter method required for IOC
	public void setServiceController(ServiceController serviceController) {
		this.serviceController = serviceController;
	}

	//Dummy Main Method to Test the code
	/*public static void main(String[] args) 
	{
		System.out.println("debugging");
		try
		{
			//new RequestQueueHandlerAction();
			ApplicationContextProvider.initializeContextProvider();
			msgFieldDataInitializer = (IMsgFieldDataInitializer) ApplicationContextProvider.getBean("msgFieldDataInitializer");
			msgFieldDataInitializer.getFileds();
			msgFormatDataInitializer = (IMsgFormatDataInitializer) ApplicationContextProvider.getBean("msgFormatDataInitializer");
			msgFormatDataInitializer.getMsgFileds();
			SFMSChannelService sfmsChannelService = (SFMSChannelService)ApplicationContextProvider.getBean("sfmsChannelService");
			sfmsChannelService.populatefieldsForCanonical();
			NGPHEsbUtils.populateErrorCodes();
			SwiftParserDao dao = (SwiftParserDao) ApplicationContextProvider.getBean("swiftParserDao");
			//Open the file that is the first 
			FileInputStream fstream = new FileInputStream("C:/MessageFormats/Test.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String mes = "";
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)
			{
				//Print the content on the console
				mes = mes + strLine + NgphEsbConstants.NGPH_SFMS_CRLF; 
				System.out.println(strLine);
			}
			//Close the input stream
			in.close();
			System.out.println(mes);  
			new SFMSAction().doProcess(mes);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}*/
	
	//Main Processing Function
	/*
	 * This Function takes the message argument send on a Queue.
	 * It reads the message and breaks the message component based on business rules and creates the corresponding Canonical Object.
	 */
	//public void doProcess(String message) throws Exception
	public void doProcess(Message message) throws Exception
	{
		logger.info("Inside doProcess Method " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date()));
		if(message!=null)
		{
			//Getting the String Object from the Message Object of ESB Class
			mes = message.getBody().get().toString();
			//mes = message;
			
			swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
			esbParserDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			serviceController = (ServiceController)ApplicationContextProvider.getBean("serviceController");
			msgFormatValidator = (IMsgFormatValidator)ApplicationContextProvider.getBean("msgFormatValidator");
			networkValdationService = (NetworkValidationService)ApplicationContextProvider.getBean("networkValdationService");
			
			String qngBlock = mes.substring(mes.indexOf("{999:")+5,mes.length());
		  	String[] temp = qngBlock.substring(0, qngBlock.indexOf("}")).split(",");
		  	msgRef =temp[0].trim(); 
		  	hostID = temp[1].trim();
		  	logger.info("hostID : " + hostID);
		  	logger.info("msgRef : " + msgRef);
		  	
			if(esbParserDao.validateMsgRef(msgRef)>0)
		  	{
		  		logger.info("Duplicate Message with Same Message Reference : " + msgRef);
		  		return;
		  	}
			
		  	EventLogger.logEvent("NGPHSWFACT0001", null, SFMSAction.class, msgRef); //SWIFT message processing started.
		 
			String msgDirection = null;
			bParserError = false;
			boolean bNoRecovery = false; 
			canonicalObj = null;
			canonicalObj = new NgphCanonical();
			canonicalObj.setErrorCodeMap(NGPHEsbUtils.errorCodeMap);
			canonicalObj.setOrderingType("I");
			canonicalObj.setReceivedTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			
			
		 
			logger.info("Message Received by SWIFT Adapter -------> " + mes);
		 	wifeSwiftObj =  new SwiftParser(mes).message();
		 	swiftblock1 = wifeSwiftObj.getBlock1();
			swiftblock2 = wifeSwiftObj.getBlock2();
			swiftblock3 = wifeSwiftObj.getBlock3();
			swiftblock4 = wifeSwiftObj.getBlock4();
			
		
		  	String eiType = swiftParserDao.getEiType(hostID);
			if(eiType.equalsIgnoreCase("P"))
			{
				msgDirection ="I";
			}
			else if (eiType.equalsIgnoreCase("H"))
			{
				msgDirection ="O";
			}
			logger.info("The direction derived is : " + msgDirection);
			
	  		//Check whether Acknowledgement is Supported based on mes Type and Mes Sub Type
		 	int support_Ack = swiftParserDao.validateAckNowledgement("MT", swiftblock1.getBlockValue().substring(0,3),"I");
	  		if(support_Ack>=1)
            {
	  			logger.info("************Acknowledgement Processing *************************");
                if(swiftblock4 != null && swiftblock1!= null)
                {
                    //processing the acknowledgement.
                    String murValue = null;
                    AcknowledgementCanonical acknowledgementCanonical=null;
                    NgphCanonical canonical = null;
                        
                    @SuppressWarnings("unchecked")
					Map<String, String> map = swiftblock4.getTagMap();
                    murValue = map.get("108");
                        
                    //check whether 108 value is null or blank
                    if(murValue!=null && StringUtils.isNotBlank(murValue) && StringUtils.isNotEmpty(murValue))
                    {
                    	logger.info("MsgMur : " + murValue);
                        //Fetch the Canonical From Db Vals
                        canonical = swiftParserDao.getCanonicalFromMessagesTx(murValue);
                        if (canonical == null)
                        {
                        	logger.error("Original Message not found for the acknowledgement received with reference " + murValue + " -- " + msgRef);
	                    	EventLogger.logEvent("NGPHSWFACT0002", null,SwiftMSgAction.class, msgRef);//Original Message not available for the received acknowledgment.	
                        }
                        else
                        {
                        	//Getting Session No and Seq from block 1
	                   	  	String block1Val = swiftblock1.getBlockValue();
	                   	  	String seqNo = block1Val.substring(block1Val.length()-6, block1Val.length());
	                   	  	String sessionNo = block1Val.substring(block1Val.length()-10, block1Val.length()-6);
	                   	  	String mesType ="MT";
	                   	  	String subMesType = block1Val.substring(0, 3);
	                   	  	logger.info("SeqNo : " + seqNo);
	                   	  	logger.info("SessionNo : " + sessionNo);
	                   	  	logger.info("MesType : " + mesType);
	                   	  	logger.info("SubMestype : " + subMesType);
	
	                   	  	//create a new Instance of Ack Canonical and store the values in it present in message
	                   	  	acknowledgementCanonical = new AcknowledgementCanonical();
	                   	  	acknowledgementCanonical.setSeqNo(seqNo);
	                   	  	acknowledgementCanonical.setSessionNo(sessionNo);
	                   	  	acknowledgementCanonical.setSrcMsgType(mesType);
	                   	  	acknowledgementCanonical.setSrcSubMsgType(subMesType);
	                   	  	acknowledgementCanonical.setSrcMsgType("MT");
	                   	  	acknowledgementCanonical.setSrcSubMsgType(swiftblock1.getBlockValue().substring(0,3));
	                   	  	acknowledgementCanonical.setMsgId(msgRef);
	                   	  	
	                   	  	//Normal Functionality of Ack
	                        String dateTime = null;
	                        dateTime = map.get("177");
	              				
	                        if(dateTime!=null)
	                        {
	                        	DateFormat sdf = new SimpleDateFormat("yyMMddhhmm");
	                    		Date dt = sdf.parse(dateTime);
	                            acknowledgementCanonical.setMsgTmstmp(new Timestamp(dt.getTime()));
	                        }
	                        String ackType = null;
	                        ackType = map.get("451");
	                        if(StringUtils.isNotEmpty(ackType))
	                        {         
	                        	if(ackType.equalsIgnoreCase("1"))
	                            {
	                        		acknowledgementCanonical.setAckType("Y");
	                        		EventLogger.logEvent("NGPHSWFACT0003", canonical, SwiftMSgAction.class, msgRef);//Positive acknowledgment received for payment of QNG reference {msgRef} and payment reference {txnReference}
	                            }
	                            else
	                            {
	                            	acknowledgementCanonical.setAckType("N");
	                                acknowledgementCanonical.setAckReasonCode(map.get("405"));
	                                EventLogger.logEvent("NGPHSWFACT0004", canonical, SFMSAction.class, msgRef);//Negative acknowledgment received for payment of QNG reference {msgRef} and payment reference {txnReference}
	                            }
	                        }	                              
	                        //passing the AckCanonical to Acknowledgement Service
	                        acknowledgementsService = (AcknowledgementsService)ApplicationContextProvider.getBean("acknowledgementsService");
	                        acknowledgementsService.processAcknowledgement(acknowledgementCanonical, canonical);
                        }
                    }
                }
                else
                {
                	logger.error("Block 4 and block 1 is empty as it is required by Acknowledgement Service");
                }
            }
	  		else
	  		{
	  			logger.info("This is not an acknowledgment message");
	  		}
			
	  		//Check if the 1,2 and 4 block is present as these are the mandatory blocks for the message to be parsed.
			if((!swiftblock1.isEmpty()) && (!swiftblock2.isEmpty()) && (!swiftblock4.isEmpty()))
				{
				  	canonicalObj.setMsgHost(hostID);
				  	canonicalObj.setMsgRef(msgRef);
					canonicalObj.setMsgCurrency(esbParserDao.getInitialisedValue(NgphEsbConstants.BASE_CUR_INIT_ENTRY));
					canonicalObj.setLastModTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
				  	canonicalObj.setLastModTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
				  	canonicalObj.setSrcMsgType("MT");
					canonicalObj.setMsgChnlType("SWIFT");
				  	canonicalObj.setMsgDirection(msgDirection);

					if(mesType.matches("103"))
					{
						canonicalObj.setOrderingType("I");
						canonicalObj.setBeneficiaryType("I");
					}
					else if(mesType.matches("910") || mesType.matches("202"))
					{
						canonicalObj.setOrderingType("F");
						canonicalObj.setBeneficiaryType("F");
					}	
				  	//parseBlock1(swiftblock1);
				  	parseBlock2(swiftblock2);
				  	if (swiftblock3 != null) 
	  				{
	  					parseBlock3(swiftblock3);
		  			}	
				  	
				  //Check from DB Configuration whether Format Validation is required for particular host or not.
				  	String isValReq = esbParserDao.getValFmtReq(canonicalObj.getMsgHost());
				  	logger.info("Validation required for Host :" + canonicalObj.getMsgHost() + " is : " + isValReq);
				  	
				  	if(isValReq==null || isValReq.equalsIgnoreCase("Y"))
				  	{
					  	// Main body block of SFMS Message checked for Message Validation
						String errorCode = msgFormatValidator.validate_Field(swiftblock4.getTagMap(),canonicalObj.getMsgChnlType(),canonicalObj.getSrcMsgType(),canonicalObj.getSrcMsgSubType(), msgRef); 
					  	if(errorCode !=null && StringUtils.isNotBlank(errorCode) && StringUtils.isNotEmpty(errorCode))
						{
					  		if (errorCode.contains(NgphEsbConstants.NGPH_FLD0003) || errorCode.contains(NgphEsbConstants.NGPH_FLD0004) || errorCode.contains(NgphEsbConstants.NGPH_FLD0006) || errorCode.contains(NgphEsbConstants.NGPH_FLD0007) || errorCode.contains(NgphEsbConstants.NGPH_FLD0008) || errorCode.contains(NgphEsbConstants.NGPH_FLD0009))
					  		{
					  			logger.error("Swift message format Validation Failed, Irrecoverable Error");
					  			canonicalObj.setMsgErrorCode(errorCode);
					  			bNoRecovery = true;
					  		}
					  		else
					  		{
					  			logger.error("Swift message format Validation Failed, Setting the error Code in Canonical");				  			EventLogger.logEvent("NGPHSFMACT0017", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format validation failed with error {msgErrorDesc}. Message moved to repair.
					  			canonicalObj.setMsgErrorCode(errorCode);
					  			bParserError = true;
								EventLogger.logEvent("NGPHSWFACT0005", canonicalObj, SwiftMSgAction.class, canonicalObj.getMsgRef());//SWIFT message format validation failed for payment QNG reference {msgRef}.
					  		}
						}
					  	// Main body block of SFMS Message checked for Network Validation
					  	String networkValErrorCode =networkValdationService.validateNetworkRules(swiftblock4.getTagMap(), canonicalObj.getSrcMsgType(), canonicalObj.getSrcMsgSubType(), canonicalObj.getMsgHost());
					  	if(networkValErrorCode !=null && StringUtils.isNotBlank(networkValErrorCode) && StringUtils.isNotEmpty(networkValErrorCode))
						{
					  		logger.error("Swift Network Validation Failed, Setting the error Code in Canonical");
					  		//Setting the Error Code for Message Validation Failed
					  		canonicalObj.setMsgErrorCode(networkValErrorCode);
							EventLogger.logEvent("NGPHSWFACT0005", canonicalObj, SwiftMSgAction.class, canonicalObj.getMsgRef());//SWIFT message format validation failed for payment QNG reference {msgRef}.
					  		bParserError = true;
						}
				  	}// if loop for field Val Req
				  	parseBlock4(swiftblock4);
				  	
				  	if (bNoRecovery)
				  	{
	  					EventLogger.logEvent("NGPHSWFACT0013", canonicalObj, SwiftMSgAction.class, canonicalObj.getMsgRef());//SWIFT message format parser error for payment QNG reference {msgRef}. Please refer error log for details.
		  				swiftParserDao.updateMsgStatusForRaw_Msgs(1, msgRef,canonicalObj.getMsgErrorCode());
				  	}
				  	else
				  	{
			  			if (!bParserError)
			  			{	
							if(canonicalObj.getMsgStatus()==null || (canonicalObj.getMsgStatus()!=null && !canonicalObj.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_O))))
							{
								if(StringUtils.isNotEmpty(canonicalObj.getMsgDirection()))
								{
									if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalObj.getMsgDirection().trim()))
									{
										canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RECEIVED_I));
									}
									else
									{
										canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RECEIVED_O));
									}
								}
								swiftParserDao.insertParsedMessage(canonicalObj);
								new ReportQueue().QueueCall(canonicalObj);
								EventLogger.logEvent("NGPHSFMACT0006", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG
								if (canonicalObj.getMsgErrorCode()== null)
								{
									logger.info("Service Controller invoked");
									/* Heart of processing*/
									serviceController.performPaymentProcessing(canonicalObj);
									/* Heart of processing*/
							  		EventLogger.logEvent("NGPHSFMACT0007", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment processing completed by QNG.
								}
								
						  		//Performing Auto Response Acknowledgment
						  		if(StringUtils.isNotBlank(canonicalObj.getMsgErrorCode()) && StringUtils.isNotEmpty(canonicalObj.getMsgErrorCode()))
						  		{
					  				//Check  Ack_Req for Particular host id
					  				int ack_req = swiftParserDao.validateAutoAckNowledgement(canonicalObj.getMsgHost());
							 		if(ack_req==1)
							 		{
							 			//Construction and processing of Auto Acknowledgment Canonical object
							 			constructAndProcessAutoAck(canonicalObj);
								 	}
								 	else
								 	{
								 		  logger.info("No Auto Acknowledgment configured for host :" + canonicalObj.getMsgHost());
								 	}
						  		}
						  		else
						  		{
						  			logger.info("Error code was not null, so no Auto Acknowledgement");
						  		}
							}
							else
							{
								logger.info("Service Controller not invoked due to dulplicate TxnRefernce");							
						  		EventLogger.logEvent("NGPHSFMACT0016", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Duplicate SFMS message received
							}
			  			}
			  			else
			  			{
					  		if (msgDirection.equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
					  		{
					  			canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O));
					  		}
					  		else
					  		{
					  			canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I));
					  		}
					  		if (canonicalObj.getMsgErrorCode()==null)
					  		{
					  			canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_SFE0001);//General exception occurred while SFMS message format validation
					  		}
					  		EventLogger.logEvent("NGPHSFMACT0005", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format validation failed for payment QNG reference {msgRef}.
					  		swiftParserDao.insertParsedMessage(canonicalObj);
							new ReportQueue().QueueCall(canonicalObj);
							EventLogger.logEvent("NGPHSFMACT0006", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG
			  			}
				  	}
				}
				else
				{
					logger.error("******************************* Swift block1 or SFMS block 4 is empty****************");
					EventLogger.logEvent("NGPHSFMACT0008", null, SFMSAction.class, msgRef);//SFMS message format validation failed.
				}
			
		}
		else
		{
			logger.error("Empty message received in Swift Action");
		 	EventLogger.logEvent("NGPHSFMACT0009", null, SFMSAction.class, msgRef);//No message received.
		}
	}
	 
	/**
	 * @param blockValue2
	 * Fetching Useful Info from Block 2
	 */
	private void parseBlock2(SwiftBlock2 blockValue2) 
	{
		try
		{
			logger.info("********************* Block 2 ***************");
			//Fetching the complete block value
			blockVal = blockValue2.getValue();
			logger.info(blockVal);
			
			swiftDirection = blockVal.substring(0, 1);
			if(swiftDirection.equalsIgnoreCase("I"))
			{
				logger.info("Direction is inbound");
				//Fetching the message type
				mesType = blockValue2.getMessageType();
				
				//Fetching the Payment Priority
				payPriority = blockValue2.getMessagePriority();
				
				canonicalObj.setSrcMsgSubType(mesType);
				canonicalObj.setSndrPymtPriority(payPriority);
				
				int start = blockVal.indexOf(mesType);
				int end = blockVal.indexOf(payPriority);
				
				//Fetching the Receiver address from the string
				receiverAdd = blockVal.substring(start + mesType.length(), end);
				canonicalObj.setReceiverBank(receiverAdd);
		
				logger.info("Src Message Type-->" + mesType);
				logger.info("Message Priority-->" + payPriority);
				logger.info("Message Direction-->" + swiftDirection);
				logger.info("ReceiverAddress-->" + receiverAdd);
			}
			// direction is out bound
			else
			{
				logger.info("Direction is Out bound");
				//Fetching the message type
				mesType = blockValue2.getMessageType();
				
				//Fetching the Payment Priority
				payPriority = blockValue2.getMessagePriority();
				
				canonicalObj.setSrcMsgSubType(mesType);
				canonicalObj.setSndrPymtPriority(payPriority);
				
				int start = blockVal.indexOf(mesType)+ mesType.length() + 10 ;
				int end = blockVal.indexOf(mesType)+ mesType.length() + 10 + 12;
				
				logger.info(start);
				logger.info(end);
				
				//Fetching the Receiver address from the string
				receiverAdd = blockVal.substring(start , end);
				canonicalObj.setReceiverBank(receiverAdd);
		
				logger.info("Src Message Type-->" + mesType);
				logger.info("Message Priority-->" + payPriority);
				logger.info("Message Direction-->" + swiftDirection);
				logger.info("ReceiverAddress-->" + receiverAdd);
			}
			//Swift direction and Ngph Directions are opposite to each other
			if(StringUtils.isNotEmpty(swiftDirection))
			{
				if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(swiftDirection.trim()))
					swiftDirection = NgphEsbConstants.OUTWARD_PAYMENT;
				else
					swiftDirection = NgphEsbConstants.INWARD_PAYMENT;
			}
			canonicalObj.setMsgDirection(swiftDirection);
		}
		catch (Exception e) 
		{
			bParserError=true;
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWFACT0006", canonicalObj, SwiftMSgAction.class, canonicalObj.getMsgRef()); // Exception Occured while Parsing Block 2 of message with Id as {MsgRef}.
		}
	}

	/**
	 * If Block 3 is not null, Fetching Useful Info from Block 3
	 * @param blockValue3
	 */
	private void parseBlock3(SwiftBlock3 swiftblock3) 
	{
		try
		{
			logger.info("********************* Block 3 ***************");
			Map<String, String> block3Map = swiftblock3.getTagMap();
			Iterator block3Iteration = block3Map.entrySet().iterator();
			String block3key = "";
			while (block3Iteration.hasNext()) 
			{
				Map.Entry block3Entry = (Map.Entry) block3Iteration.next();
				block3key = block3Entry.getKey().toString();
				logger.info(block3key);
				if (block3key.equalsIgnoreCase("113")) 
				{
					canonicalObj.setSndrTxnId(block3Entry.getValue().toString());
				}
				// TODO add covers variable in Canonical and DB
				if(block3key.equalsIgnoreCase("COV"))
				{
					// Set the Canonical Boolean Var Cover=true
				}
			}
		}
		catch (Exception e) 
		{
			bParserError=true;
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWFACT0007", canonicalObj, SwiftMSgAction.class, canonicalObj.getMsgRef()); //Exception Occured while Parsing Third Block of message with Id as {MsgRef}.
		}
	}

	private void constructAndProcessAutoAck(NgphCanonical canonical) throws Exception
	{
		logger.info("************Auto Acknowledgement Processing *************************");
		AcknowledgementCanonical autoAcknowledgementCanonical = new AcknowledgementCanonical();

		//passing the AutoAckCanonical to Acknowledgment Service for further Enriching and Processing
        acknowledgementsService = (AcknowledgementsService)ApplicationContextProvider.getBean("acknowledgementsService");
        acknowledgementsService.processAcknowledgement(autoAcknowledgementCanonical, canonical);
	}
	 
 	 /*
 	 * Fetches a HaspMap for the fields present in Block 4 using WIFE API
 	 * Fetched the Values of each field present in Block 4 and creates a Canonical Object.
 	 */
	 private void parseBlock4(SwiftBlock4 blockValue4) 
	 {
		logger.info("******************* BLOCK 4 ***********************");	
		String key = null;
		Map<String, String> map = blockValue4.getTagMap();
		logger.info(map);
			
		Iterator iter = map.entrySet().iterator();
		try
		{
		while (iter.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) iter.next();
			logger.info("Key and value pair retreived from MAP : " + entry);
			key = entry.getKey().toString();
			SwiftFieldsEnum val = SwiftFieldsEnum.findEnumByTag(key);	
			// check if the enum gets the null value
			if(val!=null)
			{
				switch (val) 
				{
					case FiftyThree_A:
						call53A(entry.getValue().toString());
						break;
					case ThirtyThree_B:
						call33B(entry.getValue().toString());
						break;
					case SeventySeven_A:
						canonicalObj.setLcNarrative(entry.getValue().toString());
						break;
					case ThirtyTwo_B:
						String principalAmtClaimed = entry.getValue().toString();
						String cur = principalAmtClaimed.substring(0, 3);
						String amt = principalAmtClaimed.substring(cur.length(), principalAmtClaimed.length());
						canonicalObj.setMsgCurrency(cur);
						if(mesType.equalsIgnoreCase("730") || mesType.equalsIgnoreCase("768"))
						{
							canonicalObj.setLcToAmtClaimed(new BigDecimal(amt.replace(",", ".")));
							canonicalObj.setLcChgsClaimed(new BigDecimal(amt.replace(",", ".")));
						}
						else if(mesType.equalsIgnoreCase("707") || mesType.equalsIgnoreCase("747"))
						{
							canonicalObj.setLcAmndmntIncAmt(new BigDecimal(amt.replace(",", ".")));
						}
						canonicalObj.setMsgAmount(new BigDecimal(amt.replace(",", ".")));
						break;
					case ThirtyTwo_D:
						String data = entry.getValue().toString();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdf.parse(data.substring(0, 8));
							canonicalObj.setMsgValueDate(new Timestamp(dt.getTime()));
						} catch (ParseException e) 
						{
							logger.error(e, e);
						}
						String lcChgsclaimed = data.substring(11, data.length());
						if(mesType.equalsIgnoreCase("730") || mesType.equalsIgnoreCase("768"))
						{
							canonicalObj.setLcChgsClaimed(new BigDecimal(lcChgsclaimed.replace(",", ".")));
						}
						break;
					case ThirtyThree_A:
						String thirtyThreeA = entry.getValue().toString();
						if (mesType.equalsIgnoreCase("752"))
						{
							SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
							try 
							{
								Date dt = sdf1.parse(thirtyThreeA.substring(0, 8));
								canonicalObj.setMsgValueDate(new Timestamp(dt.getTime()));
							} 
							catch (ParseException e) 
							{
								logger.error(e, e);
							}
						}
						String localVal = thirtyThreeA.substring(11, thirtyThreeA.length());
						canonicalObj.setLcNetAmtClaimed(new BigDecimal(localVal.replace(",", ".")));
						if(mesType.equalsIgnoreCase("756"))
						{
							canonicalObj.setLcAmtPaid(new BigDecimal(localVal.replace(",", ".")));
						}
	
						break;
					case ThirtyFour_A:
						String thirtyFourA = entry.getValue().toString();
						String lcToAmtClaimed = thirtyFourA.substring(11, thirtyFourA.length());
						canonicalObj.setLcToAmtClaimed(new BigDecimal(lcToAmtClaimed.replace(",", ".")));
						break;
					case ThirtyFour_B:
						String thirtyFourB = entry.getValue().toString();
						String lcToAmtClaimed1 = thirtyFourB.substring(3, thirtyFourB.length());
						canonicalObj.setLcToAmtClaimed(new BigDecimal(lcToAmtClaimed1.replace(",", ".")));
						canonicalObj.setMsgAmount(new BigDecimal(lcToAmtClaimed1.replace(",", ".")));
						break;
					case SeventySeven_J:
						canonicalObj.setLcDiscrepancies(entry.getValue().toString());
						break;
					case FortyTwo_A:
						call42A(entry.getValue().toString());
						break;
					case FortyTwo_D:
						call42D(entry.getValue().toString());
						break;	
					case FortyTwo_C:
						canonicalObj.setLcDraftsAt(entry.getValue().toString());
						break;
					case ThirtyOne_E:
						String expDate = entry.getValue().toString();
						SimpleDateFormat sdffff = new SimpleDateFormat("yyMMdd");
						try 
						{
							Date dt = sdffff.parse(expDate);
							canonicalObj.setLcExpDt(new Timestamp(dt.getTime()));
						}
						catch (ParseException e) 
						{
							bParserError=true;
							logger.error("Exception occured while parsing 31E");
							logger.error(e, e);
						}
					break;
					case TwentySix_E:
						canonicalObj.setLcAmndmntNo(Integer.parseInt(entry.getValue().toString()));
					break;
					case Thirty:
						String amndDate = entry.getValue().toString();
						SimpleDateFormat sdfff = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdfff.parse(amndDate);
							if(mesType.equalsIgnoreCase("760"))
							{
								canonicalObj.setLcIssueDt(new Timestamp(dt.getTime()));
							}
							else if(mesType.equalsIgnoreCase("730") || mesType.equalsIgnoreCase("747"))
							{
								canonicalObj.setLcAckDt(new Timestamp(dt.getTime()));
							}
							else
							{
								canonicalObj.setLcAmndmntDt(new Timestamp(dt.getTime()));
							}
							
						}
						catch (ParseException e) 
						{
							bParserError=true;
							logger.error("Exception occured while parsing 30");
							logger.error(e, e);
						}
						break;
					case SeventyNine:
						canonicalObj.setLcNarrative(entry.getValue().toString());
						break;
					case SeventyEight:
						canonicalObj.setLcInstrnTopay(entry.getValue().toString());
						break;
					case FortyNine:
						canonicalObj.setLcConfrmInstrns(entry.getValue().toString());
						break;
					case FortyEight:
						canonicalObj.setLcPrsntnPrd(entry.getValue().toString());
						break;
					case SeventyOne_B:
						canonicalObj.setLcCharges(entry.getValue().toString());
						break;
					case FortySix_A:
						String docsReq = entry.getValue().toString();
						if (docsReq.length() >= 4000)
						{
							String docsReq1 = docsReq.substring(0, 4000);
							if (docsReq.length() > 4000)
							{
								String docsReq2 = docsReq.substring(docsReq1.length(), docsReq.length());;
								canonicalObj.setLcDocsReq2(docsReq2);
							}
							canonicalObj.setLcDocsReq1(docsReq1);
						}
						else
						{
							canonicalObj.setLcDocsReq1(docsReq);
						}
						break;
					case FortySeven_A:
						String addlCndts = entry.getValue().toString();
						if (addlCndts.length() >= 4000)
						{
							String addlCndts1 = addlCndts.substring(0, 4000);
							if (addlCndts.length() > 4000)
							{
								String addlCndts2 = addlCndts.substring(addlCndts1.length(), addlCndts.length());;
								canonicalObj.setLcAddnlCndt2(addlCndts2);
							}
							canonicalObj.setLcAddnlCndt1(addlCndts1);
						}
						else
						{
							canonicalObj.setLcAddnlCndt1(addlCndts);
						}
						break;
					case FortyFour_D:
						canonicalObj.setLcShipPeriod(entry.getValue().toString());
						break;
					case FortyFive_A:
						String lcArrCommodity[] = entry.getValue().toString().split(NgphEsbConstants.NGPH_SFMS_CRLF);
						canonicalObj.setLcShipTerms(lcArrCommodity[0]);
						canonicalObj.setLcArrCommodity(lcArrCommodity);
						break;
					case FortyFour_C:
						String lcShpDt = entry.getValue().toString();
						SimpleDateFormat sdff = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdff.parse(lcShpDt);
							canonicalObj.setLcLstShipDt(new Timestamp(dt.getTime()));
							
						} 
						catch (ParseException e) 
						{
							bParserError=true;
							logger.error("Exception occured while parsing 44C");
							logger.error(e, e);
						}
						break;
					case FortyFour_B:
						canonicalObj.setLcFinalDstn(entry.getValue().toString());
						break;
					case FortyFour_A:
						canonicalObj.setLcDispatchPlace(entry.getValue().toString());
						break;
					case FortyFour_E:
						canonicalObj.setLcDeparturePlace(entry.getValue().toString());
						break;
					case FortyFour_F:
						canonicalObj.setLcDstn(entry.getValue().toString());
						break;
					case FortyThree_T:
						canonicalObj.setLcTransShipment(entry.getValue().toString());
						break;
					case FortyThree_P:
						canonicalObj.setLcPartialShipment(entry.getValue().toString());
						break;
					case FortyTwo_P:
						canonicalObj.setLcDefPymtDet(entry.getValue().toString());
						break;
					case FortyTwo_M:
						canonicalObj.setLcMixedPymtDet(entry.getValue().toString());
						break;
					case FortyOne_A:
						String dataVal = entry.getValue().toString();
						call41A(dataVal);
						break;
					case FortyOne_D:
						String dataValue = entry.getValue().toString();
						call41D(dataValue);
						break;
					case ThirtyNine_C:
						canonicalObj.setLcAddlAmts(entry.getValue().toString());
						break;
					case ThirtyNine_B:
						canonicalObj.setLcMaxCrAmt(entry.getValue().toString());
						break;
					case ThirtyNine_A:
						String lcTolerance = entry.getValue().toString();
						canonicalObj.setLcPosTolerance(lcTolerance.substring(0,lcTolerance.indexOf("/")));
						canonicalObj.setLcTolerance(lcTolerance.substring(lcTolerance.indexOf("/")+1,lcTolerance.length()));
						break;
					case Forty_A:
						canonicalObj.setLcType(entry.getValue().toString());
						break;	
					case SeventySeven_C:
						String sevenC = entry.getValue().toString();
						if (sevenC.length()>8000)
						{
							canonicalObj.setLcDocsReq1(sevenC.substring(0, 4000));
							canonicalObj.setLcDocsReq2(sevenC.substring(4001, 8000));
							canonicalObj.setLcAddnlCndt1(sevenC.substring(8001, sevenC.length()));
						}
						else if(sevenC.length()>4000)
						{
							canonicalObj.setLcDocsReq1(sevenC.substring(0, 4000));
							canonicalObj.setLcDocsReq2(sevenC.substring(4001, sevenC.length()));
						}
						else
						{
							canonicalObj.setLcDocsReq1(sevenC);
						}
						break;	
					case TwentyThree:
						canonicalObj.setLcPrevAdvRef(entry.getValue().toString());
						break;
					case ThirtyOne_C:
						SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdf1.parse(entry.getValue().toString());
							canonicalObj.setLcIssueDt(new Timestamp(dt.getTime()));
							canonicalObj.setMsgValueDate(new Timestamp(dt.getTime()));
						} 
						catch (ParseException e) 
						{
							bParserError=true;
							logger.error("Exception occured while parsing 31C");
							logger.error(e, e);
						}
						break;	
					case ThirtyOne_D:
						call31D(entry.getValue().toString());
						break;	
					case TwoZeroTwoZero:
						txnReference = entry.getValue().toString();
						canonicalObj.setTxnReference(txnReference);
						break;		
					case TwoZeroZeroSix:
						relReference = entry.getValue().toString();
						canonicalObj.setRelReference(relReference);
						NgphCanonical relCan = swiftParserDao.getCanonicalFromMessagesTxforTxnRef(relReference);
						if (relCan == null)
						{
							logger.error("Related payment could not be found for the SFMS payment with related reference " + relReference + " -- " + canonicalObj.getMsgRef());
							EventLogger.logEvent("NGPHSFMACT0014", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Related payment could not be found for the quoted related reference {relReference}
						}
						else
						{
							int iRet = swiftParserDao.msgIsReturn(canonicalObj.getSrcMsgType(), canonicalObj.getSrcMsgSubType(),canonicalObj.getMsgDirection());
							if (iRet > -1)
							{
								canonicalObj.setMsgIsReturn(iRet);
							}
							else
							{
								canonicalObj.setMsgIsReturn(0);
							}
							canonicalObj.setRelCanonical(relCan);
						}
						break;		
					case FourFourEightEight:	
						call4488(entry.getValue().toString());
						break;			
					case FiveFiveOneSeven:
						orderingInstitution = entry.getValue().toString();
						canonicalObj.setOrderingInstitution(orderingInstitution);
						break;		
					case FiveFiveOneSix:
						call5516(entry.getValue().toString());
						break;			
					case FiveFiveOneEight:	
						senderCorrespondent = entry.getValue().toString();
						canonicalObj.setSenderCorrespondent(senderCorrespondent);
						break;
					case SixSevenOneSeven:	
						call6717(entry.getValue().toString());
						break;
					case FiveFiveTwoOne:
						call5521(entry.getValue().toString());
						break;
					case SixFiveZeroZero:
						receiverCorrespondent = entry.getValue().toString();
						canonicalObj.setReceiverCorrespondent(receiverCorrespondent);
						break;
					case SixSevenOneEight:
						call6718(entry.getValue().toString());
						break;
					case FiveFiveTwoSix:	
						call5526(entry.getValue().toString());
						break;
					case SixFiveOneOne:	
						intermediary1Bank=entry.getValue().toString();
						canonicalObj.setIntermediary1Bank(intermediary1Bank);
						break;
					case FiveFiveFourSix:	
						call5546(entry.getValue().toString());
						break;
					case SixFiveOneSix:	
						accountWithInstitution=entry.getValue().toString();
						canonicalObj.setAccountWithInstitution(accountWithInstitution);
						break;
					case SixSevenOneNine:
						call6719(entry.getValue().toString());
						break;
					case FiveFiveFiveOne:
						call5551(entry.getValue().toString());
						break;
					case FiveFiveZeroZero:	
						call5500(entry.getValue().toString());
						break;
					case FiveFiveSixOne:
						call5561(entry.getValue().toString());
						break;
					case SixFiveTwoOne:	
						beneficiaryInstitution=entry.getValue().toString();
						canonicalObj.setBeneficiaryInstitution(beneficiaryInstitution);
						break;
					case FiveFiveFiveSix:
						beneficiaryInstitutionName=entry.getValue().toString();
						canonicalObj.setBeneficiaryInstitutionName(beneficiaryInstitutionName);
						break;
					case SevenZeroTwoThree:
						String data1 =entry.getValue().toString();
						if(data1.startsWith("/"))
						{
							instructionsForCrdtrAgtCode= data1.substring(data1.indexOf("/"), data1.lastIndexOf("/")); 
							instructionsForCrdtrAgtText=data1.substring(data1.lastIndexOf("/")+1, data1.length());
							canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode);
							canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
						}
						else
						{
							canonicalObj.setInstructionsForCrdtrAgtText(data1);
						}
						break;
					case SevenZeroTwoEight:
						chargeBearer=entry.getValue().toString();
						canonicalObj.setChargeBearer(chargeBearer);
						break;
					case SevenFourNineFive:
						String dataVall =entry.getValue().toString();
						if(dataVall.startsWith("/"))
						{
							instructionsForNextAgtCode = dataVall.substring(dataVall.indexOf("/")+1, dataVall.lastIndexOf("/")); 
							instructionsForNextAgtText=dataVall.substring(dataVall.lastIndexOf("/")+1, dataVall.length());
							canonicalObj.setInstructionsForNextAgtCode(instructionsForNextAgtCode );
							canonicalObj.setInstructionsForNextAgtText(instructionsForNextAgtText);
						}
						else
						{
							canonicalObj.setInstructionsForCrdtrAgtText(dataVall);
						}
						break;
					case SixOneOneSix:
						//For Inbound Payments
						if(msgDirection.equalsIgnoreCase("I"))
						{	
							canonicalObj.setBeneficiaryCustAcct(entry.getValue().toString());
						}
						//For OutBond Payments
						else
						{
							canonicalObj.setOrderingCustAccount(entry.getValue().toString());
						}
						break;
					case OneZeroSevenSix:
						break;
					case SixThreeFourSix:
						canonicalObj.setReturnReasonCode(entry.getValue().toString());
						break;
					case ThreeFiveTwoFive:
						break;
					case SixFourFiveZero:
						break;
					case ThreeFiveThreeFive:
						call3535(entry.getValue().toString());
						break;
					case FiveSevenFiveSix:
						canonicalObj.setSenderBank(entry.getValue().toString());
						break;
					case SixThreeZeroFive:
						break;
					case SixZeroTwoOne:
						canonicalObj.setOrderingCustAccount(entry.getValue().toString());
						break;
					case SixZeroNineOne:
						canonicalObj.setOrderingCustomerName(entry.getValue().toString());
						break;
					case SevenZeroZeroTwo:
						canonicalObj.setInitiatingPartyAddress(entry.getValue().toString());
						break;
					case FiveFiveSixNine:
						canonicalObj.setBeneficiaryInstitution(entry.getValue().toString());
						break;
					case SixThreeOneZero:
						break;
					case SixZeroSixOne:
						canonicalObj.setBeneficiaryCustAcct(entry.getValue().toString());
						break;
					case SixZeroEightOne:
						canonicalObj.setBeneficiaryCustomerName(entry.getValue().toString());
						break;
					case FiveFiveSixFive:
						canonicalObj.setBeneficiaryCustomerAddress(entry.getValue().toString());
						break;
					case SixThreeSixSix:
						canonicalObj.setReturnReasonDesc(entry.getValue().toString());
						break;
					case FourZeroThreeEight:
						canonicalObj.setMsgAmount(new BigDecimal(entry.getValue().toString().replace(",", ".")));
						break;
					case ThreeThreeEightZero:
						call3380(entry.getValue().toString());
						break;
					case ThreeThreeSevenFive:
						call3375(entry.getValue().toString());
						break;
					case ThreeThreeEightOne:
						break;
					case ThreeThreeEightFive:
						break;
					case FiveOneSevenFive:
						break;
					case FourOneZeroFive:
						break;
					case FiveOneEightZero:
						break;
					case FourOneOneZero:
						break;
					case FiveOneEightFive:
						break;
					case FourOneOneFive:
						break;
					case FiveTwoSixSeven:
						break;
					case FourOneoneFive:
						break;
					case FiveZeroFourSeven:
						break;
					case FourFourSixZero:
						break;
					case OneOneZeroSix:
						break;
					case FourZeroSixThree:
						break;
					case FiveSixTwoNine:
						break;
					case SixSevenOneTwo:
						break;
					case SixThreeOneTwo:
						break;
					case ThreeFiveZeroOne:
						call3501(entry.getValue().toString());
						break;
					case Forty_B:
						call40B(entry.getValue().toString());
					case Forty_F:
						canonicalObj.setLcAppRulesCode(entry.getValue().toString());
						break;
					case Twenty:
						logger.info("Field 20 val : " + entry.getValue().toString());
						canonicalObj.setLcNo(entry.getValue().toString());
						canonicalObj.setTxnReference(entry.getValue().toString());
						canonicalObj.setClrgSysReference(entry.getValue().toString());
						String isDup= esbParserDao.getInitialisedValue("DUPREQ");
						if(StringUtils.isNotBlank(isDup) && StringUtils.isNotEmpty(isDup) && isDup.equalsIgnoreCase("Y"))
						{
							if((canonicalObj.getSrcMsgType().equalsIgnoreCase("700") || canonicalObj.getSrcMsgType().equalsIgnoreCase("705") || canonicalObj.getSrcMsgType().equalsIgnoreCase("760")) && swiftParserDao.getmsgCount(canonicalObj.getTxnReference(), canonicalObj.getMsgDirection(), canonicalObj.getSenderBank()) > 0)
							{
								logger.info("Setting msgstatus for duplicate");
								if (canonicalObj.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
								{
									canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_O));
								}
								else
								{
									canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_I));
								}
							}
						}
						break;
					case TwentyOne:
						//canonicalObj.setLcNo(entry.getValue().toString());
						canonicalObj.setRelReference(entry.getValue().toString());
						break;
					case Fifty:
						canonicalObj.setOrderingCustomerName(entry.getValue().toString());
						break;
					case FiftyEight_A:
						call58A(entry.getValue().toString());
						break;
					case FiftyEight_D:
						call58D(entry.getValue().toString());
						break;
					case Fifty_B:
						canonicalObj.setLcNonBankIssuer(entry.getValue().toString());
						canonicalObj.setOrderingCustomerName(entry.getValue().toString());
						break;
					case TwentyFive:
						String val25 = entry.getValue().toString();
						canonicalObj.setLcAccId(val25);
						break;
					case Forty_E:
						call40E(entry.getValue().toString());
						break;
					case FiftyNine:
						call59(entry.getValue().toString());
						break;
					case SeventyTwo:
						call72(entry.getValue().toString());
						break;
					case FiftyTwo_A:
						call52A(entry.getValue().toString());
						break;
					case FiftyTwo_D:
						call52D(entry.getValue().toString());
						break;
					case FiftySeven_A:
						call57A(entry.getValue().toString());
						break;
					case FiftySeven_B:
						call57B(entry.getValue().toString());
						break;
					case FiftySeven_D:
						call57D(entry.getValue().toString());
						break;
					case FiftyOne_A:
						call51A(entry.getValue().toString());
						break;
					case FiftyOne_D:
						call51D(entry.getValue().toString());
						break;
					case FiftyThree_D:
						call53D(entry.getValue().toString());
						break;
					case SeventyOne_A:
						logger.info("Inside 71A : " + entry.getValue().toString());
						canonicalObj.setChargeBearer(entry.getValue().toString());
						break;
					case Forty_C:
						String fortyCVal =entry.getValue().toString();  
						if (fortyCVal.contains("/"))
						{
							canonicalObj.setLcAppRulesCode(fortyCVal.substring(0, fortyCVal.indexOf("/")));
							canonicalObj.setLcAppRulesDesc(fortyCVal.substring(fortyCVal.indexOf("/")+1,fortyCVal.length()));
						}
						else
						{
							canonicalObj.setLcAppRulesCode(fortyCVal);
							if (fortyCVal.equalsIgnoreCase("OTHR"))
							{
								bParserError = true;
								canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_SFE0002);//Applicable rule description not available for rule code OTHR 
								EventLogger.logEvent("NGPHSFMACT0015", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Error occurred in block 4 parsing of the payment. Applicable rules description not available.
							}
						}
						break;
					case SeventyThree:
						canonicalObj.setLcCharges(entry.getValue().toString());
					case SeventySeven_B:
						canonicalObj.setLcDispoDocs(entry.getValue().toString());
					default:
						 logger.info("Field value does not Match");
						 break;
					}// switch closed
				}// if closed
				else
				{
					 logger.info("There was no mapping found for field in Enum : " + key);
				}		
			}
		} 
		catch (Exception e) 
	 	{
			bParserError = true;
	 		logger.error(e, e);
	 		EventLogger.logEvent("NGPHSFMACT0012", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Error occurred in block 4 parsing of the payment.
	 	}
	 }
	
	private void call41D(String val)
	{ 
		try
		{
			StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
			String firstElement = null;
			String secondElement = null;
			String thirdElement = null;
			String fourthElement = null;
			String fifthElement = null;	
			String lcAuthBankAddr = null;
			String lcAuthMode = null;
				
			int tokenCount = token.countTokens();
			if(tokenCount==5)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
					thirdElement = (String) token.nextElement();
					fourthElement = (String) token.nextElement();
					fifthElement = (String) token.nextElement();
				}
				lcAuthBankAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF  + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
				lcAuthMode =fifthElement; 
			}
			else if(tokenCount==4)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
					thirdElement = (String) token.nextElement();
					fourthElement = (String) token.nextElement();
				}
				lcAuthBankAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
				lcAuthMode =fourthElement; 
			}
			else if(tokenCount==3)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
					thirdElement = (String) token.nextElement();
				}
				lcAuthBankAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement;
				lcAuthMode =thirdElement;
			}
			else if(tokenCount==2)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
				}
				lcAuthBankAddr = firstElement;
				lcAuthMode =secondElement;
			}
			else
			{
				lcAuthBankAddr =val;
			}		
			canonicalObj.setLcAuthBankAddr(lcAuthBankAddr);
			canonicalObj.setLcAuthMode(lcAuthMode);		
		}
		catch (Exception e) 
		{
			bParserError=true;
			logger.error("Error Occurred in parsing 41D");
			logger.error(e,e);
		}
	}
	private void call41A(String val)
	{
		try
		{
			StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
			String firstElement = null;
			String secondElement = null;
			int tokenCount = token.countTokens();
			if(tokenCount==2)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
				}
				canonicalObj.setLcAuthBankCode(firstElement);
				canonicalObj.setLcAuthMode(secondElement);	
			}
			else
			{
				canonicalObj.setLcAuthMode(val);	
			}		
		}
		catch (Exception e) 
		{
			bParserError=true;
			logger.error("Error Occurred in parsing 41A");
			logger.error(e,e);
		}
	}
	
	private void call58A(String val)
	{
		logger.info("Inside 58 A %%%%%%");
		try
		{
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());
				canonicalObj.setBeneficiaryInstitution(secondElement);		
				int count  = 0;
				char arr[] = firstElement.toCharArray();	
				for(int i=0;i<arr.length;i++)
				{
					if(arr[i]=='/')
					{
						++count;
					}
				}	
				//check whether there are only one slashes or not. If one slashes that means two lines will be present
				if(count==2)
				{
					canonicalObj.setBeneficiaryInstitutionPID(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
				}
			}
			else
			{
				logger.info("58A : " + val);
				canonicalObj.setBeneficiaryInstitution(val);
			}
		}
		catch (Exception e) 
		{
			bParserError=true;
			logger.error("Error occured in parsing 58A");
			logger.error(e,e);
		}
	}
	
	private void call58D(String val)
		{ 
		 try
			{
				StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
				String firstElement = null;
				String secondElement = null;
				String thirdElement = null;
				String fourthElement = null;
				String fifthElement = null;	
				String drAddr = null;
				
				int tokenCount = token.countTokens();
				if(tokenCount==5)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
						fifthElement = (String) token.nextElement();
					}
					
					int count  = 0;
					char arr[] = firstElement.toCharArray();
					
					for(int i=0;i<arr.length;i++)
					{
						if(arr[i]=='/')
						{
							++count;
						}
					}
					
					//check whether there are two slashes or not. If Two slashes that means two lines will be present
					if(count==2)
					{
						canonicalObj.setBeneficiaryInstitutionPID(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
						canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
					}
					
					//check whether there are only one slashes or not. If one slashes that means two lines will be present
					else if(count==1)
					{
						canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
					}
					drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement + NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
				}
				else if(tokenCount==4)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setBeneficiaryInstitutionPID(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;	
					}
				}
				else if(tokenCount==3)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setBeneficiaryInstitutionPID(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;	
					}
				}
				else if(tokenCount==2)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setBeneficiaryInstitutionPID(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement;	
					}			
				}
				else
				{
						drAddr = val;
				}	
				
				canonicalObj.setBeneficiaryInstitutionName(drAddr);
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 58D");
				logger.error(e,e);
			}
		}
	 private void call53D(String val)
		{

			try
			{
				StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
				String firstElement = null;
				String secondElement = null;
				String thirdElement = null;
				String fourthElement = null;
				String fifthElement = null;	
				String drAddr = null;
				
				int tokenCount = token.countTokens();
				if(tokenCount==5)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
						fifthElement = (String) token.nextElement();
					}
					
					int count  = 0;
					char arr[] = firstElement.toCharArray();
					
					for(int i=0;i<arr.length;i++)
					{
						if(arr[i]=='/')
						{
							++count;
						}
					}
					
					//check whether there are two slashes or not. If Two slashes that means two lines will be present
					if(count==2)
					{
						canonicalObj.setSenderCorrespondentId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
						canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
					}
					
					//check whether there are only one slashes or not. If one slashes that means two lines will be present
					else if(count==1)
					{
						canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
					}
					drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement + NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
				}
				else if(tokenCount==4)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setSenderCorrespondentId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;	
					}
				}
				else if(tokenCount==3)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setSenderCorrespondentId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;	
					}
				}
				else if(tokenCount==2)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setSenderCorrespondentId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setSenderCorrespondentAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement;	
					}			
				}
				else
				{
						drAddr = val;
				}	
				
				canonicalObj.setSenderCorrespondentName(drAddr);
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42D");
				logger.error(e,e);
			}
		}
	 
	 private void call51A(String value)
		{
			try
			{
				int count  = 0;
				char arr[] = value.toCharArray();
				
				for(int i=0;i<arr.length;i++)
				{
					if(arr[i]=='/')
					{
						++count;
					}
				}	
				//check whether there are two slashes or not. If Two slashes that means two lines will be present
				if(count==2)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setSendingInst(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setSendingInstId(val.substring(val.indexOf("/")+1, val.lastIndexOf("/")));
					canonicalObj.setSendingInstAcct(val.substring(val.lastIndexOf("/")+1, val.length()));
				}
				
				//check whether there are only one slashes or not. If one slashes that means two lines will be present
				else if(count==1)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setSendingInst(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setSendingInstAcct(val.substring(val.indexOf("/")+1, val.length()));
				}
				
				//Only single line is present
				else
				{
					canonicalObj.setSendingInst(value);
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42A");
				logger.error(e,e);
			}
		}
	 
	 private void call51D(String val)
		{

			try
			{
				StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
				String firstElement = null;
				String secondElement = null;
				String thirdElement = null;
				String fourthElement = null;
				String fifthElement = null;	
				String drAddr = null;
				
				int tokenCount = token.countTokens();
				if(tokenCount==5)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
						fifthElement = (String) token.nextElement();
					}
					
					int count  = 0;
					char arr[] = firstElement.toCharArray();
					
					for(int i=0;i<arr.length;i++)
					{
						if(arr[i]=='/')
						{
							++count;
						}
					}
					
					//check whether there are two slashes or not. If Two slashes that means two lines will be present
					if(count==2)
					{
						canonicalObj.setSendingInstId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
						canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
					}
					
					//check whether there are only one slashes or not. If one slashes that means two lines will be present
					else if(count==1)
					{
						canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
					}
					drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement + NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
				}
				else if(tokenCount==4)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setSendingInstId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;	
					}
				}
				else if(tokenCount==3)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setSendingInstId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;	
					}
				}
				else if(tokenCount==2)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setSendingInstId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setSendingInstAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement;	
					}			
				}
				else
				{
						drAddr = val;
				}	
				
				canonicalObj.setSendingInstNameAdd(drAddr);
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42D");
				logger.error(e,e);
			}
		}
	 
	 private void call57D(String val)
		{
			try
			{
				if(val.startsWith("/"))
				{
					String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
					String secondElement = val.substring(firstElement.length()+1, val.length());
					canonicalObj.setAccountWithInstitutionId(firstElement);
					canonicalObj.setAccountWithInstitutionName(secondElement);
				}
				else
				{
					canonicalObj.setAccountWithInstitutionName(val);
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error occured in parsing 57D");
				logger.error(e,e);
			}
		}
	 private void call57B(String val)
		{
			try
			{
				if(val.startsWith("/"))
				{
					String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
					String secondElement = val.substring(firstElement.length()+1, val.length());
					if(firstElement.startsWith("//"))
					{
						canonicalObj.setSvcLevelProperitary(firstElement.substring(firstElement.indexOf("//")+2, firstElement.length()));
					}
					else
					{
					canonicalObj.setAccountWithInstitutionId(firstElement);
					}
					canonicalObj.setAccountWithInstitutionLoc(secondElement);
				}
				else
				{
					canonicalObj.setAccountWithInstitutionLoc(val);
				}
				//canonicalObj.setAccountWithInstitution(accountWithInstitution);
				//canonicalObj.setAccountWithInstitutionAcct(accountWithInstitutionAcct);
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error occured in parsing 57B");
				logger.error(e,e);
			}
		}
	 private void call57A(String val)
		{
			try
			{
				if(val.startsWith("/"))
				{
					String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
					String secondElement = val.substring(firstElement.length()+1, val.length());
					if(val.startsWith("//"))
					{
						canonicalObj.setSvcLevelProperitary(val.substring(val.indexOf("//")+2, val.length()));
					}
					else
					{
						canonicalObj.setAccountWithInstitutionId(firstElement);
					}
					canonicalObj.setAccountWithInstitution(secondElement);
				}
				else
				{
					canonicalObj.setAccountWithInstitution(val);
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error occured in parsing 57A");
				logger.error(e,e);
			}
		}
	 private void call52A(String value)
		{
			try
			{
				int count  = 0;
				char arr[] = value.toCharArray();
				
				for(int i=0;i<arr.length;i++)
				{
					if(arr[i]=='/')
					{
						++count;
					}
				}	
				//check whether there are two slashes or not. If Two slashes that means two lines will be present
				if(count==2)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setOrderingInstitution(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setOrderingInstitutionId(val.substring(val.indexOf("/")+1, val.lastIndexOf("/")));
					canonicalObj.setOrderingInstitutionAcct(val.substring(val.lastIndexOf("/")+1, val.length()));
				}
				
				//check whether there are only one slashes or not. If one slashes that means two lines will be present
				else if(count==1)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setOrderingInstitution(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setOrderingInstitutionAcct(val.substring(val.indexOf("/")+1, val.length()));
				}
				
				//Only single line is present
				else
				{
					canonicalObj.setOrderingInstitution(value);
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42A");
				logger.error(e,e);
			}
		}
		private void call52D(String val)
		{
			try
			{
				StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
				String firstElement = null;
				String secondElement = null;
				String thirdElement = null;
				String fourthElement = null;
				String fifthElement = null;	
				String drAddr = null;
				
				int tokenCount = token.countTokens();
				if(tokenCount==5)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
						fifthElement = (String) token.nextElement();
					}
					
					int count  = 0;
					char arr[] = firstElement.toCharArray();
					
					for(int i=0;i<arr.length;i++)
					{
						if(arr[i]=='/')
						{
							++count;
						}
					}
					
					//check whether there are two slashes or not. If Two slashes that means two lines will be present
					if(count==2)
					{
						canonicalObj.setOrderingInstitutionId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
						canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
					}
					
					//check whether there are only one slashes or not. If one slashes that means two lines will be present
					else if(count==1)
					{
						canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
					}
					drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement + NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
				}
				else if(tokenCount==4)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setOrderingInstitutionId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;	
					}
				}
				else if(tokenCount==3)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setOrderingInstitutionId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;	
					}
				}
				else if(tokenCount==2)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setOrderingInstitutionId(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setOrderingInstitutionAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement;	
					}			
				}
				else
				{
						drAddr = val;
				}	
				
				canonicalObj.setLcDraweeBnkAddr(drAddr);
				canonicalObj.setOrderingCustomerName(drAddr);
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42D");
				logger.error(e,e);
			}
		}
		
	 private void call72(String aValue) 
		{
			try
			{
				if(aValue.startsWith("/"))// code is present (INS/ACC/REC/INT)
				{
					if(aValue.contains("/PHONBEN/"))
					{
						String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/PHONBEN/") + 9, aValue.length());
						String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length() + aValue.length());
						
						canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode); 
						canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
					}
					else if(aValue.contains("/TELEBEN//"))
					{
						String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/TELEBEN/") + 9, aValue.length());
						String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length() + aValue.length());
						
						canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode); 
						canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
					}
				}
				else // no Code
				{
					canonicalObj.setInstructionsForCrdtrAgtText(aValue); // 107 field of Data Model.xls
					//canonicalObj.setInstructionsForNextAgtText(aValue); // 109 Field of Data Model.xls
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error occured in parsing field 72");
				logger.error(e,e);
			}
		}
	 private void call59(String val)
		{
			try
			{
				if(val.startsWith("//"))
				{		  
				}
				else
				{
					StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
					String firstElement = null;
					String secondElement = null;
					String thirdElement = null;
					String fourthElement = null;
					String fifthElement = null;
					String beneficiaryCustAcct = null;
					String beneficiaryCustomerName = null;
					String beneficiaryCustomerAddress = null;
					
					int tokenCount = token.countTokens();
					if(tokenCount==5)
					{
						while (token.hasMoreElements()) 
						{
							firstElement = (String) token.nextElement();
							secondElement = (String) token.nextElement();
							thirdElement = (String) token.nextElement();
							fourthElement = (String) token.nextElement();
							fifthElement = (String) token.nextElement();
						}
						beneficiaryCustAcct= firstElement.substring(firstElement.indexOf("/")+1, firstElement.length());
						beneficiaryCustomerName = secondElement+ NgphEsbConstants.NGPH_SFMS_CRLF +thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement + NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
						//beneficiaryCustomerAddress = thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement + NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
					}
					else if(tokenCount==4)
					{
						while (token.hasMoreElements()) 
						{
							firstElement = (String) token.nextElement();
							secondElement = (String) token.nextElement();
							thirdElement = (String) token.nextElement();
							fourthElement = (String) token.nextElement();
						}
						if(firstElement.startsWith("/"))
						{
							beneficiaryCustAcct= firstElement.substring(firstElement.indexOf("/")+1, firstElement.length());
							beneficiaryCustomerName = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
							//beneficiaryCustomerAddress = thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
						}
						else
						{
							beneficiaryCustomerName = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement+ NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
							//beneficiaryCustomerAddress =secondElement+ NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
						}
					}
					else if(tokenCount==3)
					{
						while (token.hasMoreElements()) 
						{
							firstElement = (String) token.nextElement();
							secondElement = (String) token.nextElement();
							thirdElement = (String) token.nextElement();
						}
						if(firstElement.startsWith("/"))
						{
							beneficiaryCustAcct= firstElement.substring(firstElement.indexOf("/")+1, firstElement.length());
							beneficiaryCustomerName = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
							//beneficiaryCustomerAddress = thirdElement;
						}
						else
						{
							beneficiaryCustomerName = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement+ NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
							//beneficiaryCustomerAddress =secondElement+ NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
						}
					}
					else if(tokenCount==2)
					{
						while (token.hasMoreElements()) 
						{
							firstElement = (String) token.nextElement();
							secondElement = (String) token.nextElement();
						}
						if(firstElement.startsWith("/"))
						{
							beneficiaryCustAcct= firstElement.substring(firstElement.indexOf("/")+1, firstElement.length());
							beneficiaryCustomerName = secondElement;
						}
						else
						{
							beneficiaryCustomerName = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement; 
							//beneficiaryCustomerAddress = secondElement;
						}
					}
					else
					{
						beneficiaryCustomerName = firstElement;
					}
					canonicalObj.setBeneficiaryCustomerName(beneficiaryCustomerName);
					canonicalObj.setUltimateCreditorName(beneficiaryCustomerName);
					canonicalObj.setBeneficiaryCustAcct(beneficiaryCustAcct);
					canonicalObj.setBeneficiaryCustomerAddress(beneficiaryCustomerAddress);
					canonicalObj.setUltimateCreditorAddress(beneficiaryCustomerAddress);
					
					HashMap<String, String> countryList = NGPHUtil.getCountryName_Code();
					Iterator iter = countryList.entrySet().iterator();
					while (iter.hasNext()) 
					{
						Map.Entry entry = (Map.Entry) iter.next();
						String countryName = entry.getKey().toString();
						if(val.contains(countryName))
						{
							canonicalObj.setBeneficiaryCustomerCtry(entry.getValue().toString());
							canonicalObj.setUltimateDebtorCtry(entry.getValue().toString());
						}
					}
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error occured in parsing 59");
				logger.error(e,e);
			}
		}

	 private void call53A(String value)
		{
			try
			{
				int count  = 0;
				char arr[] = value.toCharArray();
				
				for(int i=0;i<arr.length;i++)
				{
					if(arr[i]=='/')
					{
						++count;
					}
				}	
				//check whether there are two slashes or not. If Two slashes that means two lines will be present
				if(count==2)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setSenderCorrespondent(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setSenderCorrespondentId(val.substring(val.indexOf("/")+1, val.lastIndexOf("/")));
					canonicalObj.setSenderCorrespondentAcct(val.substring(val.lastIndexOf("/")+1, val.length()));
				}
				
				//check whether there are only one slashes or not. If one slashes that means two lines will be present
				else if(count==1)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setSenderCorrespondent(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setSenderCorrespondentAcct(val.substring(val.indexOf("/")+1, val.length()));
				}
				
				//Only single line is present
				else
				{
					canonicalObj.setSenderCorrespondent(value);
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42A");
				logger.error(e,e);
			}
		}
		private void call40E(String aValue)
		{
			if(aValue.contains("/"))
			{
				String lcAppRulesCode = aValue.substring(0, aValue.indexOf("/"));
				String lcAppRulesDesc = aValue.substring(lcAppRulesCode.length()+1, aValue.length());
				
				if(lcAppRulesCode.length()>30)
				{
					canonicalObj.setLcAppRulesCode(lcAppRulesCode.substring(0, 30));
				}
				else
				{
					canonicalObj.setLcAppRulesCode(lcAppRulesCode);
				}
				
				canonicalObj.setLcAppRulesDesc(lcAppRulesDesc);
			}
			else
			{
				if(aValue.length()>30)
				{
					canonicalObj.setLcAppRulesCode(aValue.substring(0, 30));
				}
				else
				{
					canonicalObj.setLcAppRulesCode(aValue);
				}
			}
			
		}
	 private void call33B(String aValue) 
		{
			try
			{
				//first 3 characters are currency
				String currency = aValue.substring(0, 3);
				//remaining string is amount
				String oldamount = aValue.substring(3, aValue.length());
				String newamount = oldamount.replace(",", ".");
				if(mesType.matches("103"))
				{
					canonicalObj.setInstructedCurrency(currency);
					canonicalObj.setInstructedAmount(new BigDecimal(newamount));
				}
				else if(mesType.equalsIgnoreCase("202"))
				{
					canonicalObj.setInstructedCurrency(currency);
				}
				else if(mesType.equalsIgnoreCase("742") || mesType.equalsIgnoreCase("734"))
				{
					canonicalObj.setLcTotalAmtClaimed(new BigDecimal(newamount));
				}
				else
				{
					canonicalObj.setLcAmndmntDecAmt(new BigDecimal(newamount));
					canonicalObj.setLcNetAmtClaimed(new BigDecimal(newamount));
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error occured in parsing 33B");
				logger.error(e,e);
			}
		}
	 private void  call40B(String aValue)
		{
			try
			{
				String token[] = aValue.split(NgphEsbConstants.NGPH_SFMS_CRLF);
				if(token.length==2)
				{
					canonicalObj.setLcType(token[0]);
					canonicalObj.setLcTypeAuthCode(token[1]);
				}
				else
				{
					throw new NGPHException();
				}
			}
			catch (Exception e) {
				logger.error(e, e);
			}
				
		}
	 private void call42A(String value)
		{
			try
			{
				int count  = 0;
				char arr[] = value.toCharArray();
				
				for(int i=0;i<arr.length;i++)
				{
					if(arr[i]=='/')
					{
						++count;
					}
				}	
				//check whether there are two slashes or not. If Two slashes that means two lines will be present
				if(count==2)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setLcDraweeBnkCode(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setLcDraweeBnkPid(val.substring(val.indexOf("/")+1, val.lastIndexOf("/")));
					canonicalObj.setLcDraweeBnkAcct(val.substring(val.lastIndexOf("/")+1, val.length()));
				}
				
				//check whether there are only one slashes or not. If one slashes that means two lines will be present
				else if(count==1)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setLcDraweeBnkCode(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setLcDraweeBnkAcct(val.substring(val.indexOf("/")+1, val.length()));
				}
				
				//Only single line is present
				else
				{
					canonicalObj.setLcDraweeBnkCode(value);
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42A");
				logger.error(e,e);
			}
		}
	 
	 private void call42D(String val)
		{

			try
			{
				StringTokenizer token = new StringTokenizer(val,NgphEsbConstants.NGPH_SFMS_CRLF);
				String firstElement = null;
				String secondElement = null;
				String thirdElement = null;
				String fourthElement = null;
				String fifthElement = null;	
				String drAddr = null;
				
				int tokenCount = token.countTokens();
				if(tokenCount==5)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
						fifthElement = (String) token.nextElement();
					}
					
					int count  = 0;
					char arr[] = firstElement.toCharArray();
					
					for(int i=0;i<arr.length;i++)
					{
						if(arr[i]=='/')
						{
							++count;
						}
					}
					
					//check whether there are two slashes or not. If Two slashes that means two lines will be present
					if(count==2)
					{
						canonicalObj.setLcDraweeBnkPid(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
						canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
					}
					
					//check whether there are only one slashes or not. If one slashes that means two lines will be present
					else if(count==1)
					{
						canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
					}
					drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement + NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
				}
				else if(tokenCount==4)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
						fourthElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setLcDraweeBnkPid(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;	
					}
				}
				else if(tokenCount==3)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
						thirdElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setLcDraweeBnkPid(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;	
					}
				}
				else if(tokenCount==2)
				{
					while (token.hasMoreElements()) 
					{
						firstElement = (String) token.nextElement();
						secondElement = (String) token.nextElement();
					}
					if(firstElement.startsWith("/"))
					{
						int count  = 0;
						char arr[] = firstElement.toCharArray();
						
						for(int i=0;i<arr.length;i++)
						{
							if(arr[i]=='/')
							{
								++count;
							}
						}
						
						//check whether there are two slashes or not. If Two slashes that means two lines will be present
						if(count==2)
						{
							canonicalObj.setLcDraweeBnkPid(firstElement.substring(firstElement.indexOf("/")+1, firstElement.lastIndexOf("/")));
							canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.lastIndexOf("/")+1, firstElement.length()));
						}
						
						//check whether there are only one slashes or not. If one slashes that means two lines will be present
						else if(count==1)
						{
							canonicalObj.setLcDraweeBnkAcct(firstElement.substring(firstElement.indexOf("/")+1, firstElement.length()));
						}
						drAddr = secondElement;
					}
					else
					{
						drAddr = firstElement + NgphEsbConstants.NGPH_SFMS_CRLF + secondElement;	
					}			
				}
				else
				{
						drAddr = val;
				}	
				
				canonicalObj.setLcDraweeBnkAddr(drAddr);
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 42D");
				logger.error(e,e);
			}
		}
	 
	 private void call31D(String aValue)
		{
			String lcExpDt = aValue.substring(0, 8);
			String lcExpPlace = aValue.substring(lcExpDt.length(), aValue.length());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try 
			{
				Date dt = sdf.parse(lcExpDt);
				canonicalObj.setLcExpDt(new Timestamp(dt.getTime()));
				canonicalObj.setLcExpPlace(lcExpPlace);
			} catch (ParseException e) 
			{
				logger.error(e, e);
			}
		}
	 private void call5561(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());				
				canonicalObj.setBeneficiaryCustAcct(firstElement);
				canonicalObj.setBeneficiaryCustomerName(secondElement);
			}
			else
			{
				canonicalObj.setBeneficiaryCustomerName(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 5561");
		 	logger.error(e, e);
		 }
	 }
	 	
	 private void call5551(String val)
	 {
		 try
		 {
		 	if(val.startsWith("/"))
			{
		 		String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());
				canonicalObj.setAccountWithInstitutionId(firstElement);
				canonicalObj.setAccountWithInstitutionName(secondElement);
			}
			else
			{
				canonicalObj.setAccountWithInstitutionName(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 5551");
		 	logger.error(e, e);
		 }
	 }
	 	
	 private void call6719(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());
				canonicalObj.setAccountWithInstitutionId(firstElement);
				canonicalObj.setAccountWithInstitutionLoc(secondElement);
			}
			else
			{
				canonicalObj.setAccountWithInstitutionLoc(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 6719");
		 	logger.error(e, e);
		 }
	 }	
	 
	 private void call5546(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());
				canonicalObj.setIntermediary1BankId(firstElement);
				canonicalObj.setIntermediary1BankName(secondElement);
			}
			else
			{
				canonicalObj.setIntermediary1BankName(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 5546");
		 	logger.error(e, e);
		 }
	 }
	 	
	 private void call5526(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());
				canonicalObj.setReceiverCorrespondentId(firstElement);
				canonicalObj.setReceiverCorrespondentName(secondElement);
			}
			else
			{
				canonicalObj.setReceiverCorrespondentName(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 5526");
		 	logger.error(e, e);
		 }
	 }	
	 	
	 private void call6718(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());		
				canonicalObj.setReceiverCorrespondentId(firstElement);
				//receiverCorrespondent = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement +NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement +NgphEsbConstants.NGPH_SFMS_CRLF + fifthElement;
				canonicalObj.setReceiverCorrespondentLoc(secondElement);
			}
			else
			{
				canonicalObj.setReceiverCorrespondentLoc(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 6718");
		 	logger.error(e, e);
		 }
	 }
	 
	 private void call5521(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());		
				canonicalObj.setSenderCorrespondentId(firstElement);
				canonicalObj.setSenderCorrespondentName(secondElement);
			}
			else
			{
				canonicalObj.setSenderCorrespondentName(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 5521");
		 	logger.error(e, e);
		 }
	 }
	 
	 private void call6717(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());		
				canonicalObj.setSenderCorrespondentId(firstElement);
				canonicalObj.setSenderCorrespondentLoc(secondElement);
			}
			else
			{
				canonicalObj.setSenderCorrespondentLoc(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 6717");
		 	logger.error(e, e);
		 }
	 }
	 	
	 private void call5516(String val)
	 {
		 try
		 {
			if(val.startsWith("/"))
			{
				String firstElement = val.substring(0, val.indexOf(NgphEsbConstants.NGPH_SFMS_CRLF));
				String secondElement = val.substring(firstElement.length()+1, val.length());		
				canonicalObj.setOrderingInstitutionId(firstElement);
				canonicalObj.setOrderingInstitutionName(secondElement);
			}
			else
			{
				canonicalObj.setOrderingInstitutionName(val);
			}
		 }
		 catch (Exception e) 
		 {
			bParserError = true;
			logger.error("Error occurred in parsing 5516");
		 	logger.error(e, e);
		 }
	 }

	 private void call3501(String val)
	 {
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
			Date dt = sdf.parse(val);
			Timestamp timestamp = new Timestamp(dt.getTime());
			canonicalObj.setCrDateTime(timestamp);
		}
		catch (Exception e) 
		{
			bParserError = true;
			logger.error("Error occurred in parsing 3501");
			logger.error(e, e);
		}
	 }
	 
	 private void call3375(String val)
	 {
	 	try
	 	{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date dt = sdf.parse(val);
			Timestamp timestamp = new Timestamp(dt.getTime());
			canonicalObj.setPymntAcceptedTime(timestamp);
	 	}
		catch (Exception e) 
		{
			bParserError = true;
			logger.error("Error occurred in parsing 3375");
			logger.error(e, e);
		}
	 }
	 
	 private void call3380(String val)
	 {
	 	try
	 	{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date dt = sdf.parse(val);
			Timestamp timestamp = new Timestamp(dt.getTime());
			canonicalObj.setMsgValueDate(timestamp);
	 	}
		catch (Exception e) 
		{
			bParserError = true;
			logger.error("Error occurred in parsing 3380");
			logger.error(e, e);
		}
	 }
	 
	 private void call3535(String val)
	 {
	 	try
	 	{
			SimpleDateFormat sdf = new SimpleDateFormat("hhmm");
			Date dt = sdf.parse(val);
			Timestamp timestamp = new Timestamp(dt.getTime());
			canonicalObj.setMsgBatchTime(timestamp);
	 	}
		catch (Exception e) 
		{
			bParserError = true;
			logger.error("Error occurred in parsing 3535");
			logger.error(e, e);
		}
	 }
	 
	 private void call5500(String value)
	 {
		 try
		 {
		    StringTokenizer token = new StringTokenizer(value,NgphEsbConstants.NGPH_SFMS_CRLF);
			String firstElement = null;
			String secondElement = null;
			String thirdElement = null;
			String fourthElement = null;
				
			int tokenCount = token.countTokens();
			String orderingCustomerAddress=null;
			if(tokenCount==2)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
				}
				canonicalObj.setOrderingCustomerName(firstElement);
				canonicalObj.setOrderingCustomerAddress(secondElement);
			}	
			if(tokenCount==3)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
					thirdElement = (String) token.nextElement();
				}
				orderingCustomerAddress = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement;
				canonicalObj.setOrderingCustomerName(firstElement);
				canonicalObj.setOrderingCustomerAddress(orderingCustomerAddress);
			}
			if(tokenCount==4)
			{
				while (token.hasMoreElements()) 
				{
					firstElement = (String) token.nextElement();
					secondElement = (String) token.nextElement();
					thirdElement = (String) token.nextElement();
					fourthElement = (String) token.nextElement();
				}
				orderingCustomerAddress = secondElement + NgphEsbConstants.NGPH_SFMS_CRLF + thirdElement + NgphEsbConstants.NGPH_SFMS_CRLF + fourthElement;
				canonicalObj.setOrderingCustomerName(firstElement);
				canonicalObj.setOrderingCustomerAddress(orderingCustomerAddress);
			}
			//Only one Value
			else
			{
				canonicalObj.setOrderingCustomerName(value);
			}
		 }
		catch (Exception e) 
		{
			bParserError = true;
			logger.error("Error occurred in parsing 5500");
			logger.error(e, e);
		}
	 }
	 	
	 //Gets the Data Value and convert the Date and currency in a specified manner
	 //Date as YYYYMMDD and Amount as . instead of ,  
	 private void call4488(String value)
	 {
	 	try
	 	{
	 		msgValueDate = value.substring(0, 8);
	 		msgCurrency = value.substring(8,11);
	 		msgAmount = value.substring(11,value.length()).replace(",", ".");
	 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date dt = sdf.parse(msgValueDate);
			Timestamp timestamp = new Timestamp(dt.getTime());
			logger.info("Date - > " + timestamp);
		 	logger.info("Currency - > " + msgCurrency);
		 	logger.info("Amount - > " + msgAmount);
		 		
		 	canonicalObj.setMsgValueDate(timestamp);
		 	canonicalObj.setMsgCurrency(msgCurrency);
		 	canonicalObj.setMsgAmount(new BigDecimal(msgAmount));
	 	}
		catch (Exception e) 
		{
			bParserError = true;
			logger.error("Error occurred in parsing 4488");
			logger.error(e, e);
		}
	 }
	
	 /**
	  *  This is the Default Exception Handler provided by ESB, mapping needs to be done in JbossEsb.xml
	  *  Whenever any Exception will Occurs Automatically this method will be invoked.
	  *  We need to set this method name as the property value for this class in JbossEsb.xml
	 */
	 
	 public void exceptionHandler(Message message, Throwable exception)
	 {
		 logger.error("=============================== SFMS Parser ExceptionHandler Start==========================");
		 logger.error(message,exception);
		 logger.error("****************************** SFMS Parser ExceptionHandler End ***************************");
	 }
}

