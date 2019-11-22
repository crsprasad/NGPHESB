package com.logica.ngph.action;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.common.utils.NGPHUtil;
import com.logica.ngph.common.utils.PropertyReader;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.enums.SFMSFieldsEnum;
import com.logica.ngph.esb.services.AcknowledgementsService;
import com.logica.ngph.esb.services.AutoRouterService;
import com.logica.ngph.esb.services.InfoCanonicalService;
import com.logica.ngph.esb.services.NetworkValidationService;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.esb.services.ServiceController;
import com.logica.ngph.esb.servicesImpl.AcknowledgementsServiceImpl;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsBlock1;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsBlock4;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsBlock5;
import com.logica.ngph.finance.messageparser.sfms.model.WifeSFMSMessage;
import com.logica.ngph.finance.messageparser.sfms.parser.SFMSParser;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.utils.NGPHEsbUtils;
import com.logica.ngph.validators.services.IMsgFieldDataInitializer;
import com.logica.ngph.validators.services.IMsgFormatDataInitializer;
import com.logica.ngph.validators.services.IMsgFormatValidator;

/**
 * 
 * @author guptarb
 * 
 *	This Class is the Parser/Adapter for RTGS and NEFT Messages.
 *	This Class will respond to RTGS/NEFT Messages.
 *	This is SFMS Parser, that will do all the Processing of the messages and converts to Canonical.
 */
public class SFMSAction extends AbstractActionLifecycle {
	protected ConfigTree	_config;
	private final String propName = "System.properties";
	private NgphCanonical canonicalObj = null;
	
	private WifeSFMSMessage wifeSFMSObj = null;
	private SfmsBlock1 sfmsBlock1 = null;
	private SfmsBlock4 sfmsBlock4 = null;
	private SfmsBlock5 sfmsBlock5 = null;
	
	private String msgRef = null;
	private String mesType = null;
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
	private boolean isParser = false;

	static Logger logger = Logger.getLogger(SFMSAction.class);
	public SFMSAction (ConfigTree config) { _config = config; } 
	public SFMSAction () { } 
	
	// Creating Dependency Injection(IOC)
	private IMsgFormatValidator msgFormatValidator;
	private ServiceController serviceController;
	private SwiftParserDao swiftParserDao;
	private EsbServiceDao esbParserDao;
	private AcknowledgementsService acknowledgementsService;
	private AutoRouterService autoRouterService;
	private NetworkValidationService networkValdationService;
	private SFMSChannelService sfmsChannelService;
	
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
		SFMSAction.msgFieldDataInitializer = msgFieldDataInitializer;
	}
	/**
	 * @param msgFormatDataInitializer the msgFormatDataInitializer to set
	 */
	public static void setMsgFormatDataInitializer(
			IMsgFormatDataInitializer msgFormatDataInitializer) {
		SFMSAction.msgFormatDataInitializer = msgFormatDataInitializer;
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

	/*//Dummy Main Method to Test the code
	public static void main(String[] args) 
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
			new PropertyReader();
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
	}
	*/
	
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
		  	
		  	EventLogger.logEvent("NGPHSFMACT0001", null, SFMSAction.class, msgRef); //SFMS message processing started.
		 
			String msgDirection = null;
			bParserError = false;
			boolean bNoRecovery = false; 
			canonicalObj = null;
			canonicalObj = new NgphCanonical();
			canonicalObj.setErrorCodeMap(NGPHEsbUtils.errorCodeMap);
			canonicalObj.setOrderingType("I");
			canonicalObj.setReceivedTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			
			
			sfmsBlock1 = null;
			sfmsBlock4 = null;
			sfmsBlock5 = null;
			
			// Splitting the message into chunks of blocks using WIFE API
			logger.info("Message Received by SFMS Adapter -------> " + mes);
			wifeSFMSObj =  new SFMSParser(mes).message();
			sfmsBlock1 = wifeSFMSObj.getBlock1();
			sfmsBlock4 = wifeSFMSObj.getBlock4();
			sfmsBlock5 = wifeSFMSObj.getBlock5();
			
			
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
			
		  	//Check whether this is an Acknowledgement message based on mes Type and Mes Sub Type
		  	int support_Ack = 0;
		  	logger.info("sfmsBlock1.getMessageIdentifier() is "+sfmsBlock1.getMessageIdentifier());
		  	logger.info("sfmsBlock1.getMessageType() is "+sfmsBlock1.getMessageType());
		  	logger.info("sfmsBlock1.getSubMessageType()"+sfmsBlock1.getSubMessageType());
			if (sfmsBlock1.getMessageType() == null || sfmsBlock1.getSubMessageType() == null)
		  	{
		  		support_Ack = swiftParserDao.validateAckNowledgement(sfmsBlock1.getMessageIdentifier(), "XXX", msgDirection);
		  	}
		  	else
		  	{
		  		support_Ack = swiftParserDao.validateAckNowledgement(sfmsBlock1.getMessageType(), sfmsBlock1.getSubMessageType(), msgDirection);
		  	}
		 	if(support_Ack==1)
            {
		 		logger.info("************Acknowledgement Processing*************************");
	            if(sfmsBlock1!=null)
	            {
				  	NgphCanonical canonical = null;
				  	InfoCanonical infoCanonical = null;
				  	AcknowledgementCanonical acknowledgementCanonical = null;
				  	acknowledgementCanonical = parseBlock1forAck(sfmsBlock1);
				  	acknowledgementCanonical.setMsgDirection(msgDirection);
                   	if(acknowledgementCanonical!=null)
                   	{
                   		acknowledgementCanonical.setMsgId(msgRef);
                   	  	acknowledgementCanonical.setSrcEiId(hostID);
                   	  	if(sfmsBlock4!=null)
                   	  	{
                   	  		String murValue = null;
	    					Map<String, String> map = sfmsBlock4.getTagMap();
	    	                murValue = map.get("2020");
	    	                if(murValue!=null && StringUtils.isNotBlank(murValue) && StringUtils.isNotEmpty(murValue))
	    	                {
	    	                	logger.info("MsgMur : " + murValue);
	    	                	
	    	                    canonical = swiftParserDao.getCanonicalFromMessagesTx(murValue);
	    	                    infoCanonical = swiftParserDao.getInfoCanonicalFromTaMsgInformation(murValue);
	    	                    
	    	                    if (canonical == null && infoCanonical == null)
	    	                    {
	    	                    	logger.error("Original Message not found for the acknowledgement received with reference " + murValue);
	    	                    	EventLogger.logEvent("NGPHSFMACT0002", null,SFMSAction.class, msgRef);//Original Message not available for the received acknowledgment.
	    	                    }
	    	                    else 
	    	                    {
			 	                    String ackType = null;
				                    ackType = map.get("1076");
				                    if(StringUtils.isNotEmpty(ackType))
				                    {
				                    	acknowledgementCanonical.setAckType(ackType);
				                    	if(canonical!=null && infoCanonical == null)
				                    	{
					                        if(ackType.equalsIgnoreCase("Y"))
					                        {
					                        	EventLogger.logEvent("NGPHSFMACT0003", canonical, SFMSAction.class, canonical.getMsgRef());//Positive acknowledgment received for payment of QNG reference {msgRef} and payment reference {txnReference}  
					                        }
				                            else
				                            {
				                            	EventLogger.logEvent("NGPHSFMACT0004", canonical, SFMSAction.class, canonical.getMsgRef());//Negative acknowledgment received for payment of QNG reference {msgRef} and payment reference {txnReference}
				                            	acknowledgementCanonical.setAckReasonCode(map.get("6346"));
				                            }      
				                    	}
				                    	else
				                    	{
				                    		if(ackType.equalsIgnoreCase("Y"))
					                        {
					                        	EventLogger.logEvent("NGPHSFMACT0003", SFMSAction.class, infoCanonical,  infoCanonical.getMsgRef());//Positive acknowledgment received for information message of QNG reference {msgRef} and payment reference {txnReference}  
					                        }
				                            else
				                            {
				                            	EventLogger.logEvent("NGPHSFMACT0004", SFMSAction.class,infoCanonical, infoCanonical.getMsgRef());//Negative acknowledgment received for information message of QNG reference {msgRef} and payment reference {txnReference}
				                            	acknowledgementCanonical.setAckReasonCode(map.get("6346"));
				                            }
				                    	}
				                    }
	    	                    }
	    	                }
                   	 	}//block 4 
                   	  	else
                   	  	{
                   	  		String SeqNo = acknowledgementCanonical.getSeqNo();
                   	  		if(SeqNo!=null && StringUtils.isNotBlank(SeqNo) && StringUtils.isNotEmpty(SeqNo))
                   	  		{
                   	  			logger.info("Sequence Number is ::"+SeqNo);
                   	  			canonical = swiftParserDao.getCanonicalFromMessagesTxForSeq(SeqNo);
                   	  			infoCanonical = swiftParserDao.getInfoCanonicalFromTaMsgInformationForSeq(SeqNo);
                   	  			//infoCanonical = swiftParserDao.
                   	  			if (canonical == null && infoCanonical == null)
                   	  			{
                   	  				logger.error("Original Message not found for the acknowledgement received with Sequence " + SeqNo);
                   	  				EventLogger.logEvent("NGPHSFMACT0002", null,SFMSAction.class, msgRef);//Original Message not available for the received acknowledgment.
                   	  			}
                   	  		}
                   	  	}
                   	  	
                   	  	acknowledgementsService = (AcknowledgementsService)ApplicationContextProvider.getBean("acknowledgementsService");
                   	  		//Processing of ack message for financial message types
                   	  	if (canonical != null && acknowledgementCanonical != null && infoCanonical==null)
	                    {
	                   	  	//passing the Canonical & acAckCanonical to Acknowledgement Service
                   	  		logger.info("Start processing Canonical and Acknowledgement Canonical");
	    	                acknowledgementsService.processAcknowledgement(acknowledgementCanonical, canonical);
	                    }
                   	  	//Processing of ack message for non financial message types
                   	  	else if(canonical== null && acknowledgementCanonical!= null && infoCanonical!=null)
                   	  	{
                   	  		//passing Information & AckCanonical to Acknowledgement Service
                   	  		logger.info("Start processing InfoCanonical and Acknowledgement Canonical");
                   	  		acknowledgementsService.processInfoAcknowledgement(acknowledgementCanonical, infoCanonical);
                   	  	}
                   	  	else{
                   	  		logger.info("No Object Found");
                   	  	}
                   	}
                }
	            else
	            {
	            	logger.error("Block 4 or block 1 is empty as it is required by Acknowledgement Service");
	            }
	        }
		 	else if (support_Ack==9)
		 	{
		
		 		logger.info("************Info Canonical Processing*************************");
		 		// Create an Instance of Info Canonical Object

		 		if(sfmsBlock1!=null && sfmsBlock4!=null)
		 		 {
			 		// Get the required info from block 1
		 			//setting SRC Message type and SRC Message Sub Type and Sequence No available from block1. 
			 		InfoCanonical infoCan = parseBlock1forInfoCan(sfmsBlock1);
    
                   	infoCan.setMsgRef(msgRef);
                   	infoCan.setDirection(msgDirection);
                   	infoCan.setLstModTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                   	
                   	//Fetch the Bean instance
                   	InfoCanonicalService infoCanonicalService = (InfoCanonicalService)ApplicationContextProvider.getBean("infoCanonicalService");
                   	infoCan = infoCanonicalService.enrichInfoCanonical(infoCan);
                   	
    			 	//Get required data from block 4
                   	String key, value = null;
                	Map<String, String> map = sfmsBlock4.getTagMap();
                	Iterator iter = map.entrySet().iterator();
                	while (iter.hasNext()) 
                	{
                		Map.Entry entry = (Map.Entry) iter.next();
                		logger.info("Key and value pair retreived from MAP : " + entry);
                		key = entry.getKey().toString();
                		value = entry.getValue().toString();
                		
                		SFMSFieldsEnum val = SFMSFieldsEnum.findEnumByTag(key);
                				
                			switch (val) 
                			{
                				case Twenty:
                					infoCan.setPmtId_instrId(value);
                					break;
                				case TwentyOne:
                					infoCan.setPmtId_relRef(value);
                					break;
                				case SeventyNine:
                					infoCan.setInfo(value);
                					break;
                				default:
               					 	logger.info( key + "mapping not found in SFMSFieldsEnum");
               					 break;
                			}
                    }
                	
                	// Main body block of SFMS Message checked for Message Validation
                	//Set Message status 
                	if(infoCan.getDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
						{
	                		logger.info("Inside If block");
	                		infoCan.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RECEIVED_O));// Outward Received
						}
	                	else{
	                		infoCan.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RECEIVED_I));// Inward Received
	                	}
	                	try{
	                		// Insert the infoCanonical in DB
	                    	esbParserDao.populateInfoCan(infoCan);
	                	}catch(Exception e){
	                		logger.error("Exception Occured while inserting data in Transaction table, Hence move to error messages");
	                	}
                	
                	//Calling channel Service based on Channel
                	String baseFormat = PropertyReader.getMapValue(infoCan.getDstChnl());
    				logger.info("Base Format received for Info Canonical : " + baseFormat);
    				if("SWIFT".equalsIgnoreCase(baseFormat))
    				{
    					logger.info("Construct Swift Message for InfoCanonical");
    				}
    				else if("SFMS".equalsIgnoreCase(baseFormat))
    				{

    					SFMSChannelService sfmsChannelService = (SFMSChannelService) ApplicationContextProvider.getBean("sfmsChannelService");
    					String infoCanMes = sfmsChannelService.buildRtgsMessageForInfoCan(infoCan);
    					logger.info("infoCanMes Message : " + infoCanMes);			
    					//populating MESSAGEMASTER_T Table for Information message
    					if(infoCan.getDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
    					{
    						String infoMesWithoutNineBlock = infoCanMes.substring(0, infoCanMes.indexOf("{999:"));
        					esbParserDao.populateMesMaster_T(infoCan.getSeqNo(), infoMesWithoutNineBlock, null);
    					}
    					else
    					{
    						infoCan.setMessage_info(infoCanMes);
    						new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, infoCan);
    					}
    					
    					if(infoCan.getDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
						{
	                		infoCan.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_ACK_O));// Awaiting Acknowledge
						}
	                	else{
	                		infoCan.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.SENT_TO_HOST_I));// Send to Host
	                	}
	                	try{
	                		// Insert the infoCanonical in DB
	                    	esbParserDao.updatedInfoMessagestatus(infoCan.getMsgRef(), infoCan.getMsgStatus());
	                	}catch(Exception e){
	                		logger.error("Exception Occured while inserting data in Transaction table, Hence move to error messages");
	                	}
    				}
    				else if("ISO8583".equalsIgnoreCase(baseFormat))
    				{
    					logger.info("Construct ISO8583 Message for InfoCanonical");
    				}
    				else
    				{
    					logger.error("Invalid Channel");
    				}
		 		 }
		 		 else
		 		 {
		 			 logger.warn("Either Block 1 or Block 4 is Null for Message : " + mes + " with Ack Satus as 9");
		 		 }
		 	}
			else
			{
				if(sfmsBlock1 != null && sfmsBlock4 != null && !sfmsBlock1.isEmpty() && !sfmsBlock4.isEmpty())
				{
				  	canonicalObj.setMsgHost(hostID);
				  	canonicalObj.setMsgRef(msgRef);
					canonicalObj.setMsgCurrency(esbParserDao.getInitialisedValue(NgphEsbConstants.BASE_CUR_INIT_ENTRY));
					canonicalObj.setLastModTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
				  	canonicalObj.setLastModTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));

				  	parseBlock1(sfmsBlock1);
				  	
				  	canonicalObj.setMsgDirection(msgDirection);
				  	
				  	//Check from DB Configuration whether Format Validation is required for particular host or not.
				  	String isValReq = esbParserDao.getValFmtReq(canonicalObj.getMsgHost());
				  	logger.info("Validation required for Host :" + canonicalObj.getMsgHost() + " is : " + isValReq);
				  	
				 	if(isValReq==null || isValReq.equalsIgnoreCase("Y"))
				  	{
					  	// Main body block of SFMS Message checked for Message Validation
				 		logger.info("SFMSAction :: doProcess():: else block ::Message channel Type is ::"+canonicalObj.getMsgChnlType()+"Message Type ::"+canonicalObj.getSrcMsgType()+" SubMessage Type :: "+canonicalObj.getSrcMsgSubType()+" Message Reference is ::"+msgRef);
					  	String errorCode = msgFormatValidator.validate_Field(sfmsBlock4.getTagMap(),canonicalObj.getMsgChnlType(),canonicalObj.getSrcMsgType(),canonicalObj.getSrcMsgSubType(),msgRef); 
					  	if(errorCode !=null && StringUtils.isNotBlank(errorCode) && StringUtils.isNotEmpty(errorCode))
						{
					  		if (errorCode.contains(NgphEsbConstants.NGPH_FLD0003) || errorCode.contains(NgphEsbConstants.NGPH_FLD0004) || errorCode.contains(NgphEsbConstants.NGPH_FLD0005) || errorCode.contains(NgphEsbConstants.NGPH_FLD0006) || errorCode.contains(NgphEsbConstants.NGPH_FLD0007) || errorCode.contains(NgphEsbConstants.NGPH_FLD0008) || errorCode.contains(NgphEsbConstants.NGPH_FLD0009))
					  		{
					  			logger.error("SFMS message format Validation Failed, Irrecoverable Error");
					  			canonicalObj.setMsgErrorCode(errorCode);
					  			bNoRecovery = true;

					  			EventLogger.logEvent("NGPHSFMACT0019", SFMSAction.class, canonicalObj.getMsgRef(), new String[]{errorCode});//SFMS message format validation failed with error {0}. Irrecoverable Error.
					  		}
					  		else
					  		{
					  			logger.error("SFMS message format Validation Failed, Setting the error Code in Canonical");
					  			EventLogger.logEvent("NGPHSFMACT0017", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format validation failed with error {msgErrorDesc}. Message moved to repair.
					  			canonicalObj.setMsgErrorCode(errorCode);
					  			bParserError = true;
					  		}
						}
					  	// Main body block of SFMS Message checked for Network Validation
					  	String networkValErrorCode =networkValdationService.validateNetworkRules(sfmsBlock4.getTagMap(), canonicalObj.getSrcMsgType(), canonicalObj.getSrcMsgSubType(), canonicalObj.getMsgHost());
					  	if(networkValErrorCode !=null && StringUtils.isNotBlank(networkValErrorCode) && StringUtils.isNotEmpty(networkValErrorCode))
						{
					  		logger.error("SFMS Network Validation Failed, Setting the error Code in Canonical");
					  		EventLogger.logEvent("NGPHSFMACT0018", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message network validation failed with error {msgErrorDesc}. Message moved to repair
					  		canonicalObj.setMsgErrorCode(networkValErrorCode);
					  		bParserError = true;
						}
				  	}// if loop for field Val Req
				  	
				  	parseBlock4(sfmsBlock4);
				  	
				  	if (sfmsBlock5 != null) 
		  			{
				  		parseBlock5(sfmsBlock5);
			  		}
					// If Irrecoverable Error is present
				  	logger.info("bParserError Value is :: "+bParserError +"isParser value is :: "+isParser);
				  	if (bNoRecovery)
				  	{
				  		EventLogger.logEvent("NGPHSFMACT0013", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format parser error for payment QNG reference {msgRef} with irrecoverable error {msgErrorDesc}. Please refer error log for details.
		  				swiftParserDao.updateMsgStatusForRaw_Msgs(1, msgRef, canonicalObj.getMsgErrorCode());
		  				//Performing Auto Response Acknowledgment for messages that cannot be repaired
				  		if(StringUtils.isNotBlank(canonicalObj.getMsgErrorCode()) && StringUtils.isNotEmpty(canonicalObj.getMsgErrorCode()))
				  		{
			  				//Check  Ack_Req for Particular host id
			  				int ack_req = swiftParserDao.validateAutoAckNowledgement(canonicalObj.getMsgHost());
					 		if(ack_req==1)
					 		{
					 			constructAndProcessAutoAck(canonicalObj);
						 	}
						 	else
						 	{
						 		  logger.info("No Auto Acknowledgment configured for host :" + canonicalObj.getMsgHost());
						 	}
				  		}
				  		else
				  		{
				  			logger.info("Error code was null, so no Auto Acknowledgement");
				  		}
				  	}
					
				  	else
				  	{
					  	
				  		
			  			if ((!bParserError && isParser) || (!bParserError && canonicalObj.getSrcMsgSubType().equalsIgnoreCase("COV"))) // If No Error is present, Normal Business Processing
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
								try
								{
									swiftParserDao.insertParsedMessage(canonicalObj);
									new ReportQueue().QueueCall(canonicalObj);
									EventLogger.logEvent("NGPHSFMACT0006", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG
									if (canonicalObj.getMsgErrorCode()== null)
									{
										logger.info("Service Controller invoked for Message type is :: "+canonicalObj.getSrcMsgType());
										/* Heart of processing*/
										serviceController.performPaymentProcessing(canonicalObj);
										/* Heart of processing*/
								  		EventLogger.logEvent("NGPHSFMACT0007", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment processing completed by QNG.
									}
								}catch (Exception e) {
									logger.error("Exception Occured while inserting data in Transaction table, Hence move to error messages");
							  		EventLogger.logEvent("NGPHSFMACT0013", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format parser error for payment QNG reference {msgRef} with irrecoverable error {msgErrorDesc}. Please refer error log for details.
									//throw new  Exception(e);
								}
							}
							else
							{
								logger.info("Service Controller not invoked due to dulplicate TxnRefernce");							
						  		EventLogger.logEvent("NGPHSFMACT0016", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Duplicate SFMS message received
							}
			  			}
			  			
					  	// If Recoverable Error is present and can be repaired, save in Transaction table and Later DB Poller will process it.
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
					  		try
							{
						  		EventLogger.logEvent("NGPHSFMACT0005", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format validation failed for payment QNG reference {msgRef}.
						  		swiftParserDao.insertParsedMessage(canonicalObj);
								new ReportQueue().QueueCall(canonicalObj);
								EventLogger.logEvent("NGPHSFMACT0006", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG
							}catch (Exception e) 
					  		{
								logger.error("Exception Occured while inserting data in Transaction table, Hence move to error messages");
						  		EventLogger.logEvent("NGPHSFMACT0013", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format parser error for payment QNG reference {msgRef} with irrecoverable error {msgErrorDesc}. Please refer error log for details.
								//throw new  Exception(e);   
							}
					  		
			  			}
				  	}
				}
				else
				{
					logger.error("******************************* SFMS block1 or SFMS block 4 is empty****************");
					EventLogger.logEvent("NGPHSFMACT0008", null, SFMSAction.class, msgRef);//SFMS message format validation failed.
				}
			}
		}
		else
		{
			logger.error("Empty message received in SFMS Action");
		 	EventLogger.logEvent("NGPHSFMACT0009", null, SFMSAction.class, msgRef);//No message received.
		}
	}
	 
	private void constructAndProcessAutoAck(NgphCanonical canonical) throws Exception
	{
		logger.info("************Auto Acknowledgement Processing *************************");
		AcknowledgementCanonical autoAcknowledgementCanonical = new AcknowledgementCanonical();
		try
		{
			sfmsChannelService = (SFMSChannelService)ApplicationContextProvider.getBean("sfmsChannelService");
			String ackMes = null;
			String codes[] = null;
			
			// If Canonical Contains Error Code
			if(StringUtils.isNotBlank(canonical.getMsgErrorCode()) && StringUtils.isNotEmpty(canonical.getMsgErrorCode()))
			{
				if(canonical.getMsgErrorCode().contains(";"))
				{
					codes = canonical.getMsgErrorCode().split(";");
				}
				else
				{
					codes = new String[]{canonical.getMsgErrorCode()};
				}
			}
			// But If there are no error codes in Canonical, then Null pointer Exception Should not be thrown
			else
			{
				codes = new String[1];
				codes[0] = null;
			}
			autoAcknowledgementCanonical.setDstChnlType(canonical.getMsgChnlType());
			canonical.setMsgErrorCode(codes[0], autoAcknowledgementCanonical.getDstChnlType());
			autoAcknowledgementCanonical.setMsgId(NGPHUtil.generateUUID());
			autoAcknowledgementCanonical.setAckReasonCode(canonical.getMsgErrorCode());
			autoAcknowledgementCanonical.setAckReceivedTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			autoAcknowledgementCanonical.setAckType("N");
			autoAcknowledgementCanonical.setDstEiId(canonical.getMsgHost());
			autoAcknowledgementCanonical.setSeqNo(canonical.getSeqNo());
			//FIXME - This is to be analysed more as to whether the source EI is to be set as the message source or ack source, which is Q-NG
			autoAcknowledgementCanonical.setSrcEiId(canonical.getMsgHost());
			autoAcknowledgementCanonical.setMsgBranch(canonical.getMsgBranch());
			autoAcknowledgementCanonical.setMsgDept(canonical.getMsgDept());
			//For acknowledgement from Q-NG, both sender and receiver institutions are same
			autoAcknowledgementCanonical.setSenderBank(canonical.getSenderBank());
			autoAcknowledgementCanonical.setReceiverBank(canonical.getSenderBank());
			if(canonical.getMsgDirection()!=null && canonical.getMsgDirection().equalsIgnoreCase("I"))
			{
				autoAcknowledgementCanonical.setMsgDirection("O");
			}
			else
			{
				autoAcknowledgementCanonical.setMsgDirection("I");
			}
			
			autoAcknowledgementCanonical.setMsgMur(canonical.getMsgMur());
			autoAcknowledgementCanonical.setMsgOriginalId(canonical.getMsgRef());
			//fetch the ackMsgtype and ackSubMestype from DB based on configuration
			List<String> ackMsgInfo = esbParserDao.getAckMsgtype(canonical.getSrcMsgType(), canonical.getSrcMsgSubType(),canonical.getMsgChnlType(),canonical.getMsgDirection());
			if(ackMsgInfo!=null && ackMsgInfo.size()>1)
			{
				autoAcknowledgementCanonical.setDstMsgType(ackMsgInfo.get(0));
				autoAcknowledgementCanonical.setDstSubMsgType(ackMsgInfo.get(1));		
				ackMes = sfmsChannelService.buildRtgsMessageforAck(autoAcknowledgementCanonical);
				logger.info("Q-NG auto Ack SFMS Message : " + ackMes);
				if (ackMes == null)
				{
					EventLogger.logEvent("NGPHACKSVC0006", canonical, AcknowledgementsServiceImpl.class, canonical.getMsgRef());//SFMS acknowledgement message could not be constructed.
				}
				if (ackMes!=null)
				{
					try 
					{
						autoAcknowledgementCanonical.setMsgSoFar(ackMes);
						new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, autoAcknowledgementCanonical);
					}
					catch (Exception e) 
					{
						logger.error("Error occured while sending the auto acknowledgment");
						logger.error(e, e);
						EventLogger.logEvent("NGPHACKSVC0004", null,  AcknowledgementsServiceImpl.class,autoAcknowledgementCanonical.getMsgId());//QNG could not send the acknowledgment to the destination. Refer error log for details.
						throw new Exception(e);
					}
					//populating table
					if (!esbParserDao.insertAckCanonicalDetails(autoAcknowledgementCanonical))
					{
						logger.error("Error occured during insert of the acknowledgment data");
						EventLogger.logEvent("NGPHACKSVC0003", null,  AcknowledgementsServiceImpl.class, autoAcknowledgementCanonical.getMsgId());//QNG could not persist the acknowledgment data. Refer error log for details.
					}
				}
			}
			else
			{
				logger.error("Acknowledgement message type and sub message type could not be found for the source details " + canonical.getSrcMsgType() + " -- " + canonical.getSrcMsgSubType() + " -- " + canonical.getMsgChnlType() + " -- " + canonical.getMsgDirection());
				EventLogger.logEvent("NGPHACKSVC0001", null, AcknowledgementsServiceImpl.class, autoAcknowledgementCanonical.getMsgId());//Destination message type mapping not found for acknowledgment. Refer error log for details.
			}
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in acknowledgment processing");
			logger.error(e, e);
			EventLogger.logEvent("NGPHACKSVC0002", null, AcknowledgementsServiceImpl.class, autoAcknowledgementCanonical.getMsgId());//Exception occured in acknowledgment processing. Refer error log for details.
			throw new Exception(e);
		}
	}
	
 	/*
 	 * For Info Canonical.
 	 */
	 private InfoCanonical parseBlock1forInfoCan(SfmsBlock1 blockValue1) 
	 {
		 InfoCanonical infoCanonical=null;
	 	try 
	 	{

	 		logger.info("********* BLOCK 1 Started for InfoCanonical***********************");

	 		//create a new instance of InfoCanonical 
	 		infoCanonical = new InfoCanonical();
				
			if (blockValue1.getMessageType() == null)
			{
				infoCanonical.setSrcMsgType(blockValue1.getMessageIdentifier());
			}
			else
			{
				infoCanonical.setSrcMsgType(blockValue1.getMessageType());
			}
			if (blockValue1.getSubMessageType() == null)
			{
				infoCanonical.setSrcMsgSubType("XXX");
			}
			else
			{
				infoCanonical.setSrcMsgSubType(blockValue1.getSubMessageType());
			}
			
			infoCanonical.setSeqNo(blockValue1.getSequenceNumber());
			
			infoCanonical.setInstgagt_bkcd(blockValue1.getSenderAddress());
					
			infoCanonical.setInstdagt_bkcd(blockValue1.getReceiverAddress());
			
			infoCanonical.setMsgMur(blockValue1.getMessageUserReference());
			
			infoCanonical.setSndrPymtPriority(blockValue1.getPriority());

			logger.info("********** BLOCK 1 Ended for InfoCanonical****************");
	 	} 
	 	catch (Exception e) 
	 	{
	 		bParserError = true;
	 		logger.error(e, e);
	 		EventLogger.logEvent("NGPHSFMACT0010", null, SFMSAction.class, null);//Error occurred in block 1 processing of the infoCanonical.
	 	}
	 	return infoCanonical;
	 }
	 
 	/*
 	 * For Acknowledgement.
 	 */
	 private AcknowledgementCanonical parseBlock1forAck(SfmsBlock1 blockValue1) 
	 {
		AcknowledgementCanonical ackCanonical=null;
	 	try 
	 	{

	 		logger.info("********* BLOCK 1 Started for Ack***********************");
			//create a new instance of AcknowledgementCanonical 
			ackCanonical = new AcknowledgementCanonical();
				
			ackCanonical.setSrcChnlType(swiftParserDao.getHostFormat(hostID));
			
			//ackCanonical.setMsgDirection(blockValue1.getInputOutputIdentifier());
			if (blockValue1.getMessageType() == null)
			{
				ackCanonical.setSrcMsgType(blockValue1.getMessageIdentifier());
			}
			else
			{
				ackCanonical.setSrcMsgType(blockValue1.getMessageType());
			}
			if (blockValue1.getSubMessageType() == null)
			{
				ackCanonical.setSrcSubMsgType("XXX");
			}
			else
			{
				ackCanonical.setSrcSubMsgType(blockValue1.getSubMessageType());
			}
			ackCanonical.setSndrTxnId(blockValue1.getUniqueTransactionReference());
			ackCanonical.setSndrPymtPriority(blockValue1.getPriority());
			ackCanonical.setMsgMur(blockValue1.getMessageUserReference());
			ackCanonical.setSeqNo(blockValue1.getSequenceNumber());
			ackCanonical.setAckReasonCode(blockValue1.getBankAPIResponseCode());
			logger.info("AckReasonCode is :: "+ackCanonical.getAckReasonCode());
			ackCanonical.setSenderBank(blockValue1.getSenderAddress());
			ackCanonical.setReceiverBank(blockValue1.getReceiverAddress());
					
			String ackRcvdTmStmp = blockValue1.getOriginatingDate()+blockValue1.getOriginatingTime();
			logger.info("ackRcvdTmStmp-->" + ackRcvdTmStmp);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");
			Date dt = sdf.parse(ackRcvdTmStmp);
			Timestamp timestamp = new Timestamp(dt.getTime());
			logger.info("ackRcvdTmStmp AckCanonical Value -->"+ timestamp);
				
			ackCanonical.setAckReceivedTmStmp(timestamp);
			logger.info("********** BLOCK 1 Ended for Ack ****************");
	 	} 
	 	catch (Exception e) 
	 	{
	 		bParserError = true;
	 		logger.error(e, e);
	 		EventLogger.logEvent("NGPHSFMACT0010", null, SFMSAction.class, null);//Error occurred in block 1 processing of the acknowledgment.
	 	}
	 	return ackCanonical;
	 }
	 
 	 /*
 	 * Getting the MeaningFul Information Present in Block 1 as per the business requirements.
 	 */
	 private void parseBlock1(SfmsBlock1 blockValue1) 
	 {
		 try 
		 {
			logger.info("********* BLOCK 1 Started***********************");
			 
			canonicalObj.setMsgChnlType(swiftParserDao.getHostFormat(canonicalObj.getMsgHost()));
			logger.info("Message MsgChnlType is "+canonicalObj.getMsgChnlType());		
			String block1Val = blockValue1.getBlockValue();
			logger.info(block1Val);
		
			//msgDirection = blockValue1.getInputOutputIdentifier();
			mesType = blockValue1.getMessageType();
			logger.info("Message Type -> " + mesType);
			canonicalObj.setSrcMsgType(mesType);
		
			subMsgType = blockValue1.getSubMessageType();
			logger.info("SubMsgType -->" + subMsgType);
			canonicalObj.setSrcMsgSubType(subMsgType);
		
			//todo
			sndrTxnId = blockValue1.getUniqueTransactionReference();
			logger.info("SndrTxnId -->" + sndrTxnId);
			canonicalObj.setSndrTxnId(sndrTxnId);
		
			//todo
			sndrPymPr = blockValue1.getPriority();
			logger.info("SndrPymPr -->" + sndrPymPr);
			canonicalObj.setSndrPymtPriority(sndrPymPr);
			//mur
			custTxnRef = blockValue1.getMessageUserReference();
			logger.info("CustTxnRef -->" + custTxnRef);
			canonicalObj.setClrgSysReference(custTxnRef);
			canonicalObj.setMsgMur(custTxnRef);
		
			//doa
			sndrBank = blockValue1.getSenderAddress();
			logger.info("Sender Bank -->" + sndrBank);
			canonicalObj.setSenderBank(sndrBank);
		
			//doa
			rcvBank = blockValue1.getReceiverAddress();
			logger.info("Receiver Bank -->" + rcvBank);
			canonicalObj.setReceiverBank(rcvBank);
		
			pymntAcceptedTime = blockValue1.getOriginatingDate()+blockValue1.getOriginatingTime();
			logger.info("PymntAcceptedTime-->" + pymntAcceptedTime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");
			Date dt = sdf.parse(pymntAcceptedTime);
			Timestamp timestamp = new Timestamp(dt.getTime());
			logger.info("PymntAcceptedTime Canonical Value -->"+ timestamp);
			canonicalObj.setPymntAcceptedTime(timestamp);
					
			String seqNo = blockValue1.getSequenceNumber();
			logger.info("Sequence No -->" + seqNo);
			canonicalObj.setSeqNo(seqNo);
	
			logger.info("********** BLOCK 1 Ended ****************");
	 	} 
		catch (Exception e) 
	 	{
			bParserError = true;
	 		logger.error(e, e);
	 		EventLogger.logEvent("NGPHSFMACT0011", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Error occurred in block 1 processing of the payment.
	 	}
	 }
 	 /*
 	 * Fetches a HaspMap for the fields present in Block 4 using WIFE API
 	 * Fetched the Values of each field present in Block 4 and creates a Canonical Object.
 	 */
	 private void parseBlock4(SfmsBlock4 blockValue4) 
	 {
		logger.info("******************* BLOCK 4 ***********************");	
		String key = null;
		Map<String, String> map = blockValue4.getTagMap();
		
		Iterator iter = map.entrySet().iterator();
		try
		{
		while (iter.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) iter.next();
			logger.info("Key and value pair retreived from MAP : " + entry);
			key = entry.getKey().toString();
			SFMSFieldsEnum val = SFMSFieldsEnum.findEnumByTag(key);
				
			// check if the enum gets the null value
			if(val!=null)
			{
				String mapValue = entry.getValue().toString();
				//Ltrim the Value
				mapValue = StringUtils.stripStart(mapValue, " ");
				
				//Check for Occurrence of \r\n in the starting i.e It Contains Enter or not
				if(mapValue.startsWith("\r\n"))
				{
					mapValue = mapValue.substring(2, mapValue.length());
				}
				
				switch (val) 
				{
					case FiftyThree_A:
						call53A(mapValue);
						break;
					case ThirtyThree_B:
						call33B(mapValue);
						break;
					case SeventySeven_A:
						canonicalObj.setLcNarrative(mapValue);
						break;
					case ThirtyTwo_B:
						String principalAmtClaimed = mapValue;
						String cur = principalAmtClaimed.substring(0, 3);
						String amt = principalAmtClaimed.substring(cur.length(), principalAmtClaimed.length());
						canonicalObj.setMsgCurrency(cur);
						if(mesType.equalsIgnoreCase("768") || mesType.equalsIgnoreCase("769"))
						{
							canonicalObj.setLcToAmtClaimed(new BigDecimal(amt.replace(",", ".")));
						}
						else if(mesType.equalsIgnoreCase("707") || mesType.equalsIgnoreCase("747"))
						{
							canonicalObj.setLcAmndmntIncAmt(new BigDecimal(amt.replace(",", ".")));
						}
						else if(mesType.equalsIgnoreCase("754") || mesType.equalsIgnoreCase("750"))
						{
							canonicalObj.setPrincipalAmt(new BigDecimal(amt.replace(",", ".")));
							canonicalObj.setPrincipalCurrency(cur);
						}
						else if(mesType.equalsIgnoreCase("730"))
						{
							canonicalObj.setMsgCurrency(cur);
							canonicalObj.setLcChgsClaimed(new BigDecimal(amt.replace(",", ".")));
						}
						canonicalObj.setMsgAmount(new BigDecimal(amt.replace(",", ".")));
						break;
					case ThirtyTwo_D:
						String data = mapValue;
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdf.parse(data.substring(0, 8));
							canonicalObj.setMsgValueDate(new Timestamp(dt.getTime()));
						} catch (ParseException e) 
						{
							logger.error(e, e);
						}
						String msgCur = data.substring(8,11);
						String lcChgsclaimed = data.substring(11, data.length());
						String lcToAmtClaimed = data.substring(11, data.length());
						if( mesType.equalsIgnoreCase("768"))
						{
							canonicalObj.setLcChgsClaimed(new BigDecimal(lcChgsclaimed.replace(",", ".")));
						}
						else if(mesType.equalsIgnoreCase("769"))
						{
							canonicalObj.setMsgCurrency(msgCur);
							canonicalObj.setLcToAmtClaimed(new BigDecimal(lcToAmtClaimed.replace(",", ".")));
						}
						
						break;
					case ThirtyThree_A:
						String thirtyThreeA = mapValue;
						String localVal = thirtyThreeA.substring(11, thirtyThreeA.length());
						String msgCurrency = thirtyThreeA.substring(8,11);
						if (mesType.equalsIgnoreCase("752"))
						{
							SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
							try 
							{
								Date dt = sdf1.parse(thirtyThreeA.substring(0, 8));
								canonicalObj.setMsgValueDate(new Timestamp(dt.getTime()));
								canonicalObj.setMsgCurrency(msgCurrency);
								canonicalObj.setLcTotalAmtClaimed(new BigDecimal(localVal.replace(",", ".")));
							} 
							catch (ParseException e) 
							{
								logger.error(e, e);
							}
						}
						
						canonicalObj.setLcNetAmtClaimed(new BigDecimal(localVal.replace(",", ".")));
						if(mesType.equalsIgnoreCase("756"))
						{
							canonicalObj.setLcAmtPaid(new BigDecimal(localVal.replace(",", ".")));
						}
	
						break;
					case ThirtyFour_A:
						call34A(mapValue);
						break;
					case ThirtyFour_B:
						String thirtyFourB = mapValue;
						String messageCurrency = thirtyFourB.substring(0,3);
						String lcToAmtClaimed1 = thirtyFourB.substring(3, thirtyFourB.length());
						if(mesType.equalsIgnoreCase("754"))
						{
							canonicalObj.setLcTotalAmtClaimed(new BigDecimal(lcToAmtClaimed1.replace(",", ".")));
							canonicalObj.setMsgCurrency(messageCurrency);
						}
						else if(mesType.equalsIgnoreCase("707"))
						{
							canonicalObj.setLcToAmtClaimed(new BigDecimal(lcToAmtClaimed1.replace(",", ".")));
							canonicalObj.setMsgCurrency(messageCurrency);
						}
						else if(mesType.equalsIgnoreCase("769"))
						{
							canonicalObj.setLcTotalAmtClaimed(new BigDecimal(lcToAmtClaimed1.replace(",", ".")));
							canonicalObj.setInstructedCurrency(messageCurrency);
						}
						else
						{
							canonicalObj.setMsgAmount(new BigDecimal(lcToAmtClaimed1.replace(",", ".")));	
						}
						
						
						break;
					case SeventySeven_J:
						canonicalObj.setLcDiscrepancies(mapValue);
						break;
					case FortyTwo_A:
						call42A(mapValue);
						break;
					case FortyTwo_D:
						call42D(mapValue);
						break;	
					case FortyTwo_C:
						canonicalObj.setLcDraftsAt(mapValue);
						break;
					case ThirtyOne_E:
						String expDate = mapValue;
						SimpleDateFormat sdffff = new SimpleDateFormat("yyyyMMdd");
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
						String amndDate = mapValue;
						SimpleDateFormat sdfff = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdfff.parse(amndDate);
							if(mesType.equalsIgnoreCase("760"))
							{
								canonicalObj.setLcIssueDt(new Timestamp(dt.getTime()));
							}
							else if(mesType.equalsIgnoreCase("730") || mesType.equalsIgnoreCase("747") || mesType.equalsIgnoreCase("768"))
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
						canonicalObj.setLcNarrative(mapValue);
						break;
					case SeventyEight:
						canonicalObj.setLcInstrnTopay(mapValue);
						break;
					case FortyNine:
						canonicalObj.setLcConfrmInstrns(mapValue);
						break;
					case FortyEight:
						canonicalObj.setLcPrsntnPrd(mapValue);
						break;
					case SeventyOne_B:
						canonicalObj.setLcCharges(mapValue);
						break;
					case FortySix_A:
						String docsReq = mapValue;
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
					case FortySix_B:
						String docsReqAddl = mapValue;
						if (docsReqAddl.length() >= 4000)
						{
							String docsReqAddl1 = docsReqAddl.substring(0, 4000);
							if (docsReqAddl.length() > 4000)
							{
								String docsReqAddl2 = docsReqAddl.substring(docsReqAddl1.length(), docsReqAddl.length());;
								canonicalObj.setLcDocsReq2(docsReqAddl2);
							}
							canonicalObj.setLcDocsReq1(docsReqAddl1);
						}
						else
						{
							canonicalObj.setLcDocsReq1(docsReqAddl);
						}
						break;
					case FortySeven_A:
						String addlCndts = mapValue;
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
					case FortySeven_B:
						String addlCndtsAddl = mapValue;
						if (addlCndtsAddl.length() >= 4000)
						{
							String addlCndtsAddl1 = addlCndtsAddl.substring(0, 4000);
							if (addlCndtsAddl.length() > 4000)
							{
								String addlCndtsAddl2 = addlCndtsAddl.substring(addlCndtsAddl1.length(), addlCndtsAddl.length());;
								canonicalObj.setLcAddnlCndt2(addlCndtsAddl2);
							}
							canonicalObj.setLcAddnlCndt1(addlCndtsAddl1);
						}
						else
						{
							canonicalObj.setLcAddnlCndt1(addlCndtsAddl);
						}
						break;
					case FortyFour_D:
						canonicalObj.setLcShipPeriod(mapValue);
						break;
					case FortyFive_A:
						String descriptionofGoods = mapValue;
						if (descriptionofGoods.length() >= 4000)
						{
							String descriptionofGoods1 = descriptionofGoods.substring(0, 4000);
							if (descriptionofGoods.length() > 4000)
							{
								String descriptionofGoods2 = descriptionofGoods.substring(descriptionofGoods1.length(), descriptionofGoods.length());;
								//canonicalObj.setDescriptionofGoods2(descriptionofGoods2);
							}
							//canonicalObj.setDescriptionofGoods1(descriptionofGoods1);
						}
						else
						{
							//canonicalObj.setDescriptionofGoods1(descriptionofGoods);
						}
						break;
					case FortyFive_B:
						String lcArrCommodityAddl [] = mapValue.split(NgphEsbConstants.NGPH_SFMS_CRLF);
						canonicalObj.setLcArrCommodity(lcArrCommodityAddl);
						break;
					case FortyFour_C:
						String lcShpDt = mapValue;
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
						canonicalObj.setLcFinalDstn(mapValue);
						break;
					case FortyFour_A:
						canonicalObj.setLcDispatchPlace(mapValue);
						break;
					case FortyFour_E:
						canonicalObj.setLcDeparturePlace(mapValue);
						break;
					case FortyFour_F:
						canonicalObj.setLcDstn(mapValue);
						break;
					case FortyThree_T:
						canonicalObj.setLcTransShipment(mapValue);
						break;
					case FortyThree_P:
						canonicalObj.setLcPartialShipment(mapValue);
						break;
					case FortyTwo_P:
						canonicalObj.setLcDefPymtDet(mapValue);
						break;
					case FortyTwo_M:
						canonicalObj.setLcMixedPymtDet(mapValue);
						break;
					case FortyOne_A:
						String dataVal = mapValue;
						call41A(dataVal);
						break;
					case FortyOne_D:
						String dataValue = mapValue;
						call41D(dataValue);
						break;
					case ThirtyNine_C:
						canonicalObj.setLcAddlAmts(mapValue);
						break;
					case ThirtyNine_B:
						canonicalObj.setLcMaxCrAmt(mapValue);
						break;
					case ThirtyNine_A:
						String lcTolerance = mapValue;
						canonicalObj.setLcPosTolerance(lcTolerance.substring(0,lcTolerance.indexOf("/")));
						canonicalObj.setLcTolerance(lcTolerance.substring(lcTolerance.indexOf("/")+1,lcTolerance.length()));
						break;
					case Forty_A:
						canonicalObj.setLcType(mapValue);
						break;	
					case SeventySeven_C:
						String sevenC = mapValue;
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
						canonicalObj.setLcPrevAdvRef(mapValue);
						break;
					case ThirtyOne_C:
						SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdf1.parse(mapValue);													
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
						call31D(mapValue);
						break;	
					case TwoZeroTwoZero:
						txnReference = mapValue;
						canonicalObj.setTxnReference(txnReference);
						break;		
					case TwoZeroZeroSix:
						relReference = mapValue;
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
						call4488(mapValue);
						break;			
					case FiveFiveOneSeven:
						orderingInstitution = mapValue;
						canonicalObj.setOrderingInstitution(orderingInstitution);
						break;		
					case FiveFiveOneSix:
						call5516(mapValue);
						break;			
					case FiveFiveOneEight:	
						senderCorrespondent = mapValue;
						canonicalObj.setSenderCorrespondent(senderCorrespondent);
						break;
					case SixSevenOneSeven:	
						call6717(mapValue);
						break;
					case FiveFiveTwoOne:
						call5521(mapValue);
						break;
					case SixFiveZeroZero:
						receiverCorrespondent = mapValue;
						canonicalObj.setReceiverCorrespondent(receiverCorrespondent);
						break;
					case SixSevenOneEight:
						call6718(mapValue);
						break;
					case FiveFiveTwoSix:	
						call5526(mapValue);
						break;
					case SixFiveOneOne:	
						intermediary1Bank=mapValue;
						canonicalObj.setIntermediary1Bank(intermediary1Bank);
						break;
					case FiveFiveFourSix:	
						call5546(mapValue);
						break;
					case SixFiveOneSix:	
						accountWithInstitution=mapValue;
						canonicalObj.setAccountWithInstitution(accountWithInstitution);
						break;
					case SixSevenOneNine:
						call6719(mapValue);
						break;
					case FiveFiveFiveOne:
						call5551(mapValue);
						break;
					case FiveFiveZeroZero:	
						call5500(mapValue);
						break;
					case FiveFiveSixOne:
						call5561(mapValue);
						break;
					case SixFiveTwoOne:	
						beneficiaryInstitution=mapValue;
						canonicalObj.setBeneficiaryInstitution(beneficiaryInstitution);
						break;
					case FiveFiveFiveSix:
						beneficiaryInstitutionName=mapValue;
						canonicalObj.setBeneficiaryInstitutionName(beneficiaryInstitutionName);
						break;
					case SevenZeroTwoThree:
						String data1 =mapValue;
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
						chargeBearer=mapValue;
						canonicalObj.setChargeBearer(chargeBearer);
						break;
					case SevenFourNineFive:
						String dataVall =mapValue;
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
							canonicalObj.setBeneficiaryCustAcct(mapValue);
						}
						//For OutBond Payments
						else
						{
							canonicalObj.setOrderingCustAccount(mapValue);
						}
						break;
					case OneZeroSevenSix:
						break;
					case SixThreeFourSix:
						canonicalObj.setReturnReasonCode(mapValue);
						break;
					case ThreeFiveTwoFive:
						break;
					case SixFourFiveZero:
						break;
					case ThreeFiveThreeFive:
						call3535(mapValue);
						break;
					case FiveSevenFiveSix:
						canonicalObj.setSenderBank(mapValue);
						break;
					case SixThreeZeroFive:
						break;
					case SixZeroTwoOne:
						canonicalObj.setOrderingCustAccount(mapValue);
						break;
					case SixZeroNineOne:
						canonicalObj.setOrderingCustomerName(mapValue);
						break;
					case SevenZeroZeroTwo:
						canonicalObj.setInitiatingPartyAddress(mapValue);
						break;
					case FiveFiveSixNine:
						canonicalObj.setBeneficiaryInstitution(mapValue);
						break;
					case SixThreeOneZero:
						break;
					case SixZeroSixOne:
						canonicalObj.setBeneficiaryCustAcct(mapValue);
						break;
					case SixZeroEightOne:
						canonicalObj.setBeneficiaryCustomerName(mapValue);
						break;
					case FiveFiveSixFive:
						canonicalObj.setBeneficiaryCustomerAddress(mapValue);
						break;
					case SixThreeSixSix:
						canonicalObj.setReturnReasonDesc(mapValue);
						break;
					case FourZeroThreeEight:
						canonicalObj.setMsgAmount(new BigDecimal(mapValue.replace(",", ".")));
						break;
					case ThreeThreeEightZero:
						call3380(mapValue);
						break;
					case ThreeThreeSevenFive:
						call3375(mapValue);
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
						call3501(mapValue);
						break;
					case Forty_B:
						call40B(mapValue);
					case Forty_F:
						canonicalObj.setLcAppRulesCode(mapValue);
						break;
					case Twenty:
						logger.info("Field 20 val : " + mapValue);
						canonicalObj.setLcNo(mapValue);
						canonicalObj.setTxnReference(mapValue);
						canonicalObj.setClrgSysReference(mapValue);
						String isDup= esbParserDao.getInitialisedValue("DUPREQ");
											
							if(StringUtils.isNotBlank(isDup) && StringUtils.isNotEmpty(isDup) && isDup.equalsIgnoreCase("Y"))
							{
								if(canonicalObj.getSrcMsgType().equalsIgnoreCase("707") && swiftParserDao.getmsgCount(canonicalObj.getTxnReference(), canonicalObj.getMsgDirection(),canonicalObj.getSenderBank() ) <= 0)
								{	
									isParser=true;
									
								}
								else if((canonicalObj.getSrcMsgType().equalsIgnoreCase("700") || canonicalObj.getSrcMsgType().equalsIgnoreCase("705") || canonicalObj.getSrcMsgType().equalsIgnoreCase("760")) && swiftParserDao.getmsgCount(canonicalObj.getTxnReference(), canonicalObj.getMsgDirection(),canonicalObj.getSenderBank()) > 0)
								{
									if(canonicalObj.getSrcMsgType().equalsIgnoreCase("700") && swiftParserDao.getmsgCount(canonicalObj.getTxnReference(), canonicalObj.getMsgDirection(),canonicalObj.getSenderBank()) <= 1 && swiftParserDao.getSRCMsgType(canonicalObj.getTxnReference(), canonicalObj.getMsgDirection()).equalsIgnoreCase("707"))
									{
										isParser=true;
									}
									else
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
								else
								{
									isParser=true;
								}
							}
						
						break;
					case TwentyOne:
						//canonicalObj.setLcNo(mapValue);
						canonicalObj.setRelReference(mapValue);
						break;
					case Fifty:
						canonicalObj.setOrderingCustomerName(mapValue);
						break;
					case FiftyEight_A:
						call58A(mapValue);
						break;
					case FiftyEight_D:
						call58D(mapValue);
						break;
					case Fifty_B:
						canonicalObj.setLcNonBankIssuer(mapValue);
						canonicalObj.setOrderingCustomerName(mapValue);
						break;
					case TwentyFive:
						String val25 = mapValue;
						canonicalObj.setLcAccId(val25);
						break;
					case Forty_E:
						call40E(mapValue);
						break;
					case FiftyNine:
						call59(mapValue);
						break;
					case SeventyTwo:
						call72(mapValue);
						break;
					case FiftyTwo_A:
						call52A(mapValue);
						break;
					case FiftyTwo_D:
						call52D(mapValue);
						break;
					case FiftySeven_A:
						call57A(mapValue);
						break;
					case FiftySeven_B:
						call57B(mapValue);
						break;
					case FiftySeven_D:
						call57D(mapValue);
						break;
					case FiftyOne_A:
						call51A(mapValue);
						break;
					case FiftyOne_D:
						call51D(mapValue);
						break;
					case FiftyThree_D:
						call53D(mapValue);
						break;
					case SeventyOne_A:
						logger.info("Inside 71A : " + mapValue);
						canonicalObj.setChargeBearer(mapValue);
						break;
					case Forty_C:
						String fortyCVal =mapValue;  
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
						canonicalObj.setLcCharges(mapValue);
						break;
					case SeventySeven_B:
						canonicalObj.setLcDispoDocs(mapValue);
						break;
					case TwentySeven:
						String sequenceTotal = mapValue;
						if (sequenceTotal.contains("/"))
						{
							canonicalObj.setSequenceNo(new BigDecimal(sequenceTotal.substring(0,sequenceTotal.indexOf("/"))));
							logger.info("canonicalObj.getSequenceNo "+canonicalObj.getSequenceNo());
							canonicalObj.setNoofMessages(new BigDecimal(sequenceTotal.substring(sequenceTotal.indexOf("/")+1,sequenceTotal.length())));
							logger.info("canonicalObj.getNoofMessages "+canonicalObj.getNoofMessages());
						}
						break;
					case ThirtyTwo_A:
						call32A(mapValue);
						break;
					case FiftyFour_A:
						call54A(mapValue);
						break;
					case SevenZeroTwoZero:
						canonicalObj.setTxnReference(mapValue);
						break;	
					case SevenZeroTwoOne:
						canonicalObj.setRelReference(mapValue);
						break;
					case SevenZeroTwoTwo:
						canonicalObj.setBgFormNumber(mapValue);
						break;
					case SevenZeroTwoFour:
						canonicalObj.setBgType(mapValue);
						break;
					case SevenZeroTwoFive:
						String bgAmount = mapValue;
						String messageCurr = bgAmount.substring(0,3);
						String bgAmt = bgAmount.substring(3, bgAmount.length());
						canonicalObj.setBgAmt(new BigDecimal(bgAmt.replace(",", ".")));
						canonicalObj.setBgCurrency(messageCurr);
						canonicalObj.setMsgAmount(new BigDecimal(bgAmt.replace(",", ".")));
						break;
					case SevenZeroTwoSix:
						call7026(mapValue);
						break;
					case SevenZeroTwoSeven:
						try
						{
							logger.info("Start parsing :: 7027");
							String effectiveDate = mapValue.substring(0, 8);
							SimpleDateFormat sdf11 = new SimpleDateFormat("yyyyMMdd");
							Date bgEffectiveDate = sdf11.parse(effectiveDate);
							canonicalObj.setBgEffectiveDate(new Timestamp(bgEffectiveDate.getTime()));
							logger.info("End parsing :: 7027");
						}
						catch(ParseException e)
						{
							logger.error("Parsing occuerd in 7027 in BG Cover message");
						}
						break;
					case SevenZeroTwoNine:		
						try
						{	
							logger.info("Start parsing :: 7029");
							String lodgementDate = mapValue.substring(0, 8);
							SimpleDateFormat sdf11 = new SimpleDateFormat("yyyyMMdd");
							Date bgEffectiveDate = sdf11.parse(lodgementDate);
							canonicalObj.setBgLodgementEndDate(new Timestamp(bgEffectiveDate.getTime()));
							logger.info("End parsing :: 7029");
						}
						catch(ParseException e)
						{
							logger.error("Parsing occuerd in 7029 in BG Cover message");
						}
						break;
					case SevenZeroThreeZero:
						canonicalObj.setBgLodgementPlace(mapValue);
						break;
					case SevenZeroThreeOne:
						canonicalObj.setIssuingBankCode(mapValue);
						break;
					case SevenZeroThreeTwo:
						canonicalObj.setIssueingBankAddr(mapValue);
						break;
					case SevenZeroThreeThree:
						canonicalObj.setBgApplicentName(mapValue);
						break;
					case SevenZeroThreeFour:
						canonicalObj.setBgBenificiaryName(mapValue);
						call59(mapValue);
						break;
					case SevenZeroThreeFive:
						canonicalObj.setBgBenificiaryBankCode(mapValue);
						break;
					case SevenZeroThreeSix:
						canonicalObj.setBgBenificiaryBankAddr(mapValue);
						break;
					case SevenZeroThreeSeven:
						canonicalObj.setInstructionsForCrdtrAgtText(mapValue);
						break;
					case SevenZeroThreeEight:
						canonicalObj.setBgPurpose(mapValue);
						break;
					case SevenZeroThreeNine:
						canonicalObj.setContractReference(mapValue);
						break;
					case SevenZeroFourZero:
						canonicalObj.setStampDutyPaid(mapValue);
						break;
					case SevenZeroFourOne:
						canonicalObj.setStampDutyNum(mapValue);
						break;
					case SevenZeroFourTwo:
						call7042(mapValue);
						break;
					case SevenZeroFourThree:
						String amountPaid = mapValue;
						canonicalObj.setBgPaidAmt(new BigDecimal(amountPaid.replace(",", ".")));
						break;
					case SevenZeroFourFour:
						canonicalObj.setBgStateCode(mapValue);
						break;
					case SevenZeroFourFive:
						canonicalObj.setBgArticleNum(mapValue);
						break;
					case SevenZeroFourSix:
						try
						{	
							logger.info("Start parsing :: 7046");
							String paymentDate = mapValue.substring(0, 8);
							SimpleDateFormat sdf11 = new SimpleDateFormat("yyyyMMdd");
							Date bgDateofPayment = sdf11.parse(paymentDate);
							canonicalObj.setBgPaymentDate(new Timestamp(bgDateofPayment.getTime()));
							logger.info("End parsing :: 7029");
						}
						catch(ParseException e)
						{
							logger.error("Parsing occuerd in 7029 in BG Cover message");
						}
						break;
					case SevenZeroFourSeven:
						canonicalObj.setBgPaymentPlace(mapValue);
						break;
					case SevenZeroFourEight:
						canonicalObj.setBgDematForm(mapValue);
						break;
					case SevenZeroFourNine:
						canonicalObj.setBgCostodianProvider(mapValue);
						break;
					case SevenZeroFiveZero:
						canonicalObj.setBgDematAccNum(mapValue);
						break;
					case SevenZeroFiveFive:
						canonicalObj.setLcPrevAdvRef(mapValue);
						break;
					case SevenZeroFiveSix:
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = simpleDateFormat.parse(mapValue);													
							canonicalObj.setLcAmndmntDt(new Timestamp(dt.getTime()));					
						} 
						catch (ParseException e) 
						{
							bParserError=true;
							logger.error("Exception occured while parsing 7056");
							logger.error(e, e);
						}
						break;
					case SevenZeroFiveSeven:
						canonicalObj.setLcAmndmntNo(Integer.parseInt(entry.getValue().toString()));
						break;
					case SevenZeroFiveEight:
						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
						try 
						{
							Date dt = sdFormat.parse(mapValue);													
							canonicalObj.setLcIssueDt(new Timestamp(dt.getTime()));							
						} 
						catch (ParseException e) 
						{
							bParserError=true;
							logger.error("Exception occured while parsing 7058");
							logger.error(e, e);
						}
						break;
					case SevenZeroFiveNine:
						String seventySevenC = mapValue;
						if (seventySevenC.length()>8000)
						{
							canonicalObj.setLcDocsReq1(seventySevenC.substring(0, 4000));
							canonicalObj.setLcDocsReq2(seventySevenC.substring(4001, 8000));
							canonicalObj.setLcAddnlCndt1(seventySevenC.substring(8001, seventySevenC.length()));
						}
						else if(seventySevenC.length()>4000)
						{
							canonicalObj.setLcDocsReq1(seventySevenC.substring(0, 4000));
							canonicalObj.setLcDocsReq2(seventySevenC.substring(4001, seventySevenC.length()));
						}
						else
						{
							canonicalObj.setLcDocsReq1(seventySevenC);
						}
						break;
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
					canonicalObj.setBeneficiaryInstitutionAcct(firstElement.substring(firstElement.lastIndexOf("/")+1,firstElement.length()));
				}
			}
			else
			{
				logger.info("58A : " + val);
				canonicalObj.setBeneficiaryInstitutionClrgCd(val);
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
				logger.error("Error Occurred in parsing 51A");
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
				logger.error("Error Occurred in parsing 51D");
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
					logger.info("firstElement of 57D is ::"+canonicalObj.getAccountWithInstitutionId());
					logger.info("secondElement of 57D is ::"+canonicalObj.getAccountWithInstitutionName());
				}
				else
				{
					canonicalObj.setAccountWithInstitutionName(val);
					logger.info("ThirdElement of 57D is ::"+canonicalObj.getAccountWithInstitutionName());
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
					canonicalObj.setAccountWithInstitutionClrgCd(secondElement);
				}
				else
				{
					canonicalObj.setAccountWithInstitutionClrgCd(val);
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
						//String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/PHONBEN/") + 9, aValue.length());
						//String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length() + aValue.length());
						String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/PHONBEN/"), 9);
						String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length(), aValue.length());
						
						//canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode);   
						canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
					}
					else if(aValue.contains("/TELEBEN//"))
					{
						//String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/TELEBEN/") + 9, aValue.length());
						//String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length() + aValue.length());
						String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/TELEBEN/"), 9);
						String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length(), aValue.length());
						
						//canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode); 
						canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
					}
					else if(aValue.contains("/BENCON/"))
					{
						String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/BENCON/"), 8);
						String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length(), aValue.length());
					
						//canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode); 
						canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
					}
					else if(aValue.contains("/BENACC/"))
					{
						String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/BENACC/"), 8);
						String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length(), aValue.length());
					
						canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode); 
						canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
					}
					else if(aValue.contains("/BENREJ/"))
					{
						String instructionsForCrdtrAgtCode = aValue.substring(aValue.indexOf("/BENREJ/"), 8);
						String instructionsForCrdtrAgtText = aValue.substring(instructionsForCrdtrAgtCode.length(), aValue.length());
					
						canonicalObj.setInstructionsForCrdtrAgtCode(instructionsForCrdtrAgtCode); 
						canonicalObj.setInstructionsForCrdtrAgtText(instructionsForCrdtrAgtText);
					}
					else // no Code
					{
						canonicalObj.setInstructionsForCrdtrAgtText(aValue); // 107 field of Data Model.xls
						//canonicalObj.setInstructionsForNextAgtText(aValue); // 109 Field of Data Model.xls
					}
				}				
				else // no code identified using char '/' then set the text as it is. 
				{
						canonicalObj.setInstructionsForCrdtrAgtText(aValue); // 107 field of Data Model.xls
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
						beneficiaryCustomerName = val;
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
				//For Narrative field 
				if (lcAppRulesDesc.length() > 35)
				{
					
					canonicalObj.setLcAppRulesDesc(lcAppRulesDesc.substring(0, 35));
				}
				else
				{
					canonicalObj.setLcAppRulesDesc(lcAppRulesDesc);
				}
				
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
				else if(mesType.equalsIgnoreCase("754") || mesType.equalsIgnoreCase("769"))
				{
					canonicalObj.setLcAdditionalAmt(new BigDecimal(newamount));
					canonicalObj.setLcAdditionalCurrency(currency);
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
	 
	 private void call32A(String aValue)
	 {
		 logger.info("Starting call32A");
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try 
			{
				if(canonicalObj.getSrcMsgType().equalsIgnoreCase("734"))
					{
					logger.info("734 message 32A ");
					String msgValueDate = aValue.substring(0, 8);
					String msgCurrency = aValue.substring(msgValueDate.length(), 11);
					String msgAmount = aValue.substring(11,aValue.length());
					Date dt = sdf.parse(msgValueDate);
					canonicalObj.setMsgValueDate(new Timestamp(dt.getTime()));
					canonicalObj.setMsgCurrency(msgCurrency);
					canonicalObj.setMsgAmount(new BigDecimal(msgAmount.replace(",", ".")));
					}
				else
				{
					
				 	String principalDate = aValue.substring(0, 8);
					String pricipalcurrencyCode = aValue.substring(principalDate.length(), 11);
					String principalAmount = aValue.substring(11,aValue.length());
					Date dt = sdf.parse(principalDate);
					canonicalObj.setPrincipalDate(new Timestamp(dt.getTime()));
					canonicalObj.setPrincipalCurrency(pricipalcurrencyCode);
					canonicalObj.setPrincipalAmt(new BigDecimal(principalAmount.replace(",", ".")));
				}
				
			}
			catch(NumberFormatException n)
			{
				logger.error(n,n);
			}
			catch (ParseException e) 
			{
				logger.error(e, e);
			}
			logger.info("Ending call32A");
	 }
	 
	 private void call34A(String aValue)
	 {
		 	logger.info("Starting call34A");
		 	String msgDate = aValue.substring(0, 8);
			String msgCurrency = aValue.substring(msgDate.length(), 11);
			String lcToAmtClaimed = aValue.substring(11, aValue.length());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try 
			{
				Date dt = sdf.parse(msgDate);
				
				canonicalObj.setMsgValueDate(new Timestamp(dt.getTime()));
				canonicalObj.setMsgCurrency(msgCurrency);
				canonicalObj.setLcTotalAmtClaimed(new BigDecimal(lcToAmtClaimed.replace(",", ".")));
			} 
			catch(NumberFormatException n)
			{
				logger.error(n,n);
			}
			catch (ParseException e) 
			{
				logger.error(e, e);
			}
			logger.info("Ending call34A");
	 }
	 
	 private void call54A(String value)
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
					canonicalObj.setReceiverCorrespondent(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setReceiverCorrespondentId(val.substring(val.indexOf("/")+1, val.lastIndexOf("/")));
					canonicalObj.setReceiverCorrespondentAcct(val.substring(val.lastIndexOf("/")+1, val.length()));
				}
				
				//check whether there are only one slashes or not. If one slashes that means two lines will be present
				else if(count==1)
				{
					String dataElements[] = value.split(NgphEsbConstants.NGPH_SFMS_CRLF);
					canonicalObj.setReceiverCorrespondent(dataElements[1]);
					
					String val = dataElements[0];
					canonicalObj.setReceiverCorrespondentAcct(val.substring(val.indexOf("/")+1, val.length()));
				}
				
				//Only single line is present
				else
				{
					canonicalObj.setReceiverCorrespondent(value);
				}
			}
			catch (Exception e) 
			{
				bParserError=true;
				logger.error("Error Occurred in parsing 54A");
				logger.error(e,e);
			}
		}
	 
	 /*
	  * 
	  */
	 private void call7026(String value)
	 {
		 logger.info("Starting call7026");
		String dateValue = value;
		String fromDate = dateValue.substring(0, 8);
		String toDate = dateValue.substring(fromDate.length(), 16);
											
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		
		try 
		{
			Date guaranteeFromdate = sdf.parse(fromDate);
			Date guaranteeToDate = sdf.parse(toDate);
			canonicalObj.setBgFromDate(new Timestamp(guaranteeFromdate.getTime()));
			canonicalObj.setBgToDate(new Timestamp(guaranteeToDate.getTime()));
		} 
		catch (ParseException e) 
		{
			bParserError=true;
			logger.error("Error occured while parsing 7026 BG Cover message");
		}
		logger.info("Ending call7026");
					
	}
			
		 	
	 
	 
	 /*
	  * 
	  */
	 private void call7042(String value)
	 {
		 logger.info("Starting call7042");	
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 /*try
		 {
			// Date eStampDate = sdf.parse(value);
			// canonicalObj.setStampDutyDateTime(new Timestamp(eStampDate.getTime()));
		 }
		 catch(ParseException e)
		 {
			 bParserError=true;
			 logger.error("Error occured while parsing 7042 BG Cover message");
		 }*/
		 logger.info("End call7042");
	 }
 	 /*
 	 * Processes the Block 5 and gets the meaningful info from Block 5
 	 */
	 private void parseBlock5(SfmsBlock5 blockValue5)
	 {
		 logger.info("******************* BLOCK 5 ***********************");
		 Map<String, String> map = blockValue5.getTagMap();
		 logger.info(map);
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

