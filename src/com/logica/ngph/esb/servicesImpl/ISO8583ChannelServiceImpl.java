package com.logica.ngph.esb.servicesImpl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.BASE24Packager;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.IsoFieldEnum;
import com.logica.ngph.common.utils.NGPHUtil;
import com.logica.ngph.esb.Dtos.FieldCanonicalAttribute;
import com.logica.ngph.esb.Dtos.ResponseBean;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.ISO8583ChannelService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.utils.NGPHEsbUtils;

public class ISO8583ChannelServiceImpl implements ISO8583ChannelService 
{	
	static Logger logger = Logger.getLogger(ISO8583ChannelServiceImpl.class);	
	private EsbServiceDao esbServiceDao;
	private SwiftParserDao swiftParserDao;
	/**
	 * @param esbServiceDao the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	/**
	 * @param swiftParserDao the swiftParserDao to set
	 */
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) 
	{
		this.swiftParserDao = swiftParserDao;
	}
	/**
	 * 
	 * @param String
	 * @return String
	 * 
	 * Compares business date and date in TA_Seq table and initializes stan value to 1
	*/
	private final static String propName = "System.properties";
	private static String logonNMCI = null;
	private static String echoNMCI = null;
	public static String impsServerEi = null;
	
	static 
	{
		//loading property file in memory
		Properties props = new Properties();
		try 
		{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			logonNMCI=props.getProperty("logonNMCI");
			echoNMCI=props.getProperty("echoNMCI");
			impsServerEi=props.getProperty("impsServerEi");
			logger.info("logonNMCI is : " + logonNMCI);
			logger.info("EchiNMCI is : " + echoNMCI);
			logger.info("impsServerEi is : " + impsServerEi);
		} 
		catch (IOException e) 
		{
			logger.error(e,e);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.services.ISO8583ChannelService#createLogOnOrEcho(int, int)
	 */
	public String createLogOnOrEcho(int req_res_Flag, int log_echo_Flag,NgphCanonical canonicalData) throws Exception
	{
		String mes=null;
		try
		{
			ResponseBean resObj = null;
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			//check if the EIStatus is not 1
			if(canonicalData!=null && esbServiceDao.getEIStatus(canonicalData.getDstEiId())!=1 && log_echo_Flag==0 && req_res_Flag == 0)
			{
				logger.info("Connection status not right to send log on message");
				return null;
			}
			if(req_res_Flag==0)
			{
				NgphCanonical canReq = new NgphCanonical();
				if(log_echo_Flag==0)
				{
					// read property file for variable logonNMIC and set in field 70 of Canonical
					canReq.setInitiatorRemitAdviceMethod(logonNMCI);
				}
				else if(log_echo_Flag==1)
				{
					// read property file for variable EchoNMIC and set in field 70 of Canonical
					canReq.setInitiatorRemitAdviceMethod(echoNMCI);
				}
				swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
				resObj = new ResponseBean();
				resObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
				resObj.setMsgType("08");
				resObj.setMsgSubType("00");
				resObj.setMsgDirection(NgphEsbConstants.OUTWARD_PAYMENT);
				canReq.setMsgBranch(esbServiceDao.getInitialisedValue("DEFBRANCH"));
				String acqInstIdCode = esbServiceDao.getInitialisedValBranch("IMPSLOCOFFCD", canReq.getMsgBranch());
	
				//Setting Canonical Variables
				String msgRef = NGPHUtil.generateUUID();
				canReq.setMsgRef(msgRef);
				canReq.setDstEiId(impsServerEi);
				canReq.setDstMsgType("08");
				canReq.setDstMsgSubType("00");
				canReq.setDstMsgChnlType(esbServiceDao.getDstChnlType(impsServerEi));
				canReq.setLastModTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
				canReq.setSenderBank(acqInstIdCode);
				//canReq.setClrgSysReference( getRetRefNo(getStan(canReq.getMsgBranch())));
				//The log on or echo request does no use field 37 and hence setting the STAN itself as the reference number - anyway this is not a payment
				canReq.setClrgSysReference( getStan(canReq.getMsgBranch()));
				canReq.setPrevInstructingBank(acqInstIdCode);
				canReq.setMsgDirection(NgphEsbConstants.OUTWARD_PAYMENT);
				
				//Storing the final message construction in a local Variable
				mes = constructMessage(canReq);
				if (mes != null)
				{
					// Updating the Global Cache Maps
					resObj.setCanonicalObj(canReq);
					updateGlobalCacheMap(canReq,resObj);
					int idx = mes.indexOf("{999:");
					String mesWO999 = mes;
					if (idx > 0)
					{
						mesWO999 = mes.substring(0, idx);
					}
					swiftParserDao.insertRawMessage("QNGSYS", msgRef, mesWO999, canReq.getDstMsgChnlType(),canReq.getMsgDirection());
				}
				else
				{
					logger.error("The constructed log on / echo request ISO8583 message is blank");
					EventLogger.logEvent("NGPHISCSVC0001", canReq, ISO8583ChannelServiceImpl.class, canReq.getMsgRef());//The log on / echo request could not be constructed.
				}
			}
			else
			{
				if(StringUtils.isNotBlank(canonicalData.getClrgSysReference()) && StringUtils.isNotEmpty(canonicalData.getClrgSysReference()))
				{
					String msgId = null;
					//String stan =canonicalData.getClrgSysReference().substring(canonicalData.getClrgSysReference().length()-6, canonicalData.getClrgSysReference().length());
					String stan =canonicalData.getClrgSysReference();
					if(stan != null && StringUtils.isNotBlank(stan)&& StringUtils.isNotEmpty(stan))
					{
						msgId = NGPHEsbUtils.stanMsgMap.get(stan);
						if(msgId!=null && StringUtils.isNotBlank(msgId)&& StringUtils.isNotEmpty(msgId))
						{
							resObj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
						}
						else
						{
							logger.error("Mapping message reference not found for the STAN " + stan);
							EventLogger.logEvent("NGPHISCSVC0002", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//The mapping request with IMPS reference {ClrgSysReference} for the response being created could not be found.
						}
					}
					else
					{
						logger.error("Empty Stan is Received for QNG Ref " + canonicalData.getMsgRef());
					}
					resObj.setResTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
					resObj.setResCode(canonicalData.getMsgErrorCode());
					canonicalData.setMsgDirection(NgphEsbConstants.OUTWARD_PAYMENT);
					//Storing the final message construction in a local Variable
					mes = constructMessage(canonicalData);
					if (mes != null)
					{
						// Updating the Global Cache Maps
						resObj.setCanonicalObj(canonicalData);
						updateGlobalCacheMap(canonicalData,resObj);
						int idx = mes.indexOf("{999:");
						String mesWO999 = mes;
						if (idx > 0)
						{
							mesWO999 = mes.substring(0, idx);
						}
						swiftParserDao.insertRawMessage("QNGSYS", canonicalData.getMsgRef(), mesWO999, canonicalData.getDstMsgChnlType(),canonicalData.getMsgDirection());
					}
					else
					{
						logger.error("The constructed log on / echo response ISO8583 message is blank");
						EventLogger.logEvent("NGPHISCSVC0003", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//The log on / echo response could not be constructed.
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("Exception occured while creating log on / echo request / response");
			logger.error(e,e);
			EventLogger.logEvent("NGPHISCSVC0004", null, ISO8583ChannelServiceImpl.class, null);//Exception occurred while creating log on / echo request / response. Refer error log for details.
			throw new Exception(e);
		}
		return mes;
	}

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.services.IMPSChannelService#createAndSendPaymentRequestOrResponse(com.logica.ngph.common.dtos.NgphCanonical)
	 */
	public String createAndSendPaymentRequestOrResponse(NgphCanonical canonicalData, int req_res_Flag) throws Exception 
	{	
		String mes = null;
		try
		{
			//check whether the Destination channel Type is IMPS.
			if(canonicalData.getDstMsgChnlType().equalsIgnoreCase("IMPS"))
			{
				ResponseBean resObj = null;	
				// Check if flag value is 0(Numeric Zero).This is a request Message. Add Additional Info in Canonical Object.
				if(req_res_Flag==0)
				{
					esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
					//Processing Code.
					if (canonicalData.getMsgPurposeCode() == null)
					{
						canonicalData.setMsgPurposeCode("900000");
					}
					// MERCHANT CATEGORY CODE
					String hostCat = esbServiceDao.getHostCategory(canonicalData.getMsgHost());
					if(hostCat.equalsIgnoreCase("MOB") || hostCat == null)
					{
						canonicalData.setSvcLevelCode("4814");
						canonicalData.setLclInstCode("019");
						canonicalData.setCatgPurposeCode("05");
					}
					else if(hostCat.equalsIgnoreCase("ATM"))
					{
						canonicalData.setSvcLevelCode("6011");
						canonicalData.setLclInstCode("901");
						canonicalData.setCatgPurposeCode("00");
					}
					else if(hostCat.equalsIgnoreCase("INET"))
					{
						canonicalData.setSvcLevelCode("4829");
						canonicalData.setLclInstCode("012");
						canonicalData.setCatgPurposeCode("05");
					}	
					// Acquiring institution ID code
					String acqInstIdCode = esbServiceDao.getInitialisedValBranch("IMPSLOCOFFCD", canonicalData.getMsgBranch());
					if (acqInstIdCode == null)
					{
						logger.error("IMPS Local office code is not configured");
					}
					//canonicalData.setSenderBank(acqInstIdCode);			
					//Retrieval Reference No
					String retRefNo = null;
					try
					{
					  retRefNo = getRetRefNo(getStan(canonicalData.getMsgBranch()));
					}
					catch (Exception e) 
					{
						logger.error("Exception occured while generating retreival reference number for IMPS payment request / response message creation");
						logger.error(e, e);
						EventLogger.logEvent("NGPHISCSVC0005", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//Exception occured while generating retreival reference number for the IMPS payment request / response. Refer error log for details.
						throw new Exception(e);
					}
					logger.info("retRefNo : " + retRefNo);
					canonicalData.setClrgSysReference(retRefNo);
					//canonicalData.setRelReference(retRefNo);
					  
					//Card Acceptor terminal id
					if(hostCat.equalsIgnoreCase("MOB") || hostCat.equalsIgnoreCase("INET"))
					{
						String terminalId = esbServiceDao.getInitialisedValBranch("LOCALBIC", canonicalData.getMsgBranch()).substring(0, 3); 
						logger.info("terminalId-->" + terminalId);
						//get Mob number
						String mobNo = esbServiceDao.getMobNo(canonicalData.getOrderingCustAccount());
						String mobVal = mobNo.substring(mobNo.length()-5, mobNo.length());
						canonicalData.setInitiatingPartyID(terminalId + mobNo);
						canonicalData.setInitiatorRemitReference(terminalId + mobVal);
					}
					//This is ATM Terminal
					else
					{
						canonicalData.setInitiatorRemitReference(canonicalData.getInitiatorRemitReference());
						canonicalData.setInitiatingPartyID(canonicalData.getInitiatingPartyID());
					}
					//Card Acceptor Name / Location
					if(hostCat.equalsIgnoreCase("MOB") || hostCat.equalsIgnoreCase("INET"))
					{
						String terminalName = esbServiceDao.getInitialisedValBranch("LOCALBNKNAME", canonicalData.getMsgBranch());
						if (terminalName == null)
						{
							logger.error("Local bank name is not configured");
						}
						String cardAccptr = null;
						//get Mob number
						String mobLoc = esbServiceDao.getMobNo(canonicalData.getOrderingCustAccount());	
						if(terminalName.length()>25)
						{
							cardAccptr = terminalName.substring(0, 25); 
						}
						else
						{
							cardAccptr = StringUtils.rightPad(terminalName,25);
						}
						canonicalData.setInitiatingPartyName(cardAccptr+"MOB" + mobLoc+"IN");
					}
					//This is ATM Terminal
					else
					{
						canonicalData.setInitiatingPartyName(canonicalData.getInitiatingPartyName());
					}  
					//FIXME Below is specific for banks that have only corporate customers, for retail customers the person to merchant or 
					//merchant to person to be determined by an appropriate logic
					if (canonicalData.getDstMsgType().equalsIgnoreCase("02"))
					{
						canonicalData.setMsgTxnType("47");
					}
					else if (canonicalData.getDstMsgType().equalsIgnoreCase("04"))
					{
						canonicalData.setMsgTxnType("46");
					}
					//  
				  	// Updating the Response Bean Object for Flag value=0
					resObj = new ResponseBean();
					//Set the Response bean members
					resObj.setCanonicalObj(canonicalData);
					resObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
					resObj.setMsgType(canonicalData.getDstMsgType());
					resObj.setMsgSubType(canonicalData.getDstMsgSubType());
					resObj.setMsgDirection(canonicalData.getMsgDirection());
					
					// Updating the Global Cache Maps
					updateGlobalCacheMap(canonicalData,resObj);
				}//Parent if loop closed, checking for flag value
				// Check if flag value is 1(Numeric one).This is a response Message.
				else if (req_res_Flag==1 || req_res_Flag==3)
				{
					if(StringUtils.isNotBlank(canonicalData.getClrgSysReference()) && StringUtils.isNotEmpty(canonicalData.getClrgSysReference()))
					{
						String msgId=null;
						//String stan =canonicalData.getClrgSysReference().substring(canonicalData.getClrgSysReference().length()-6, canonicalData.getClrgSysReference().length());
						String stan =canonicalData.getClrgSysReference();
						if(stan != null && StringUtils.isNotBlank(stan)&& StringUtils.isNotEmpty(stan))
						{
							msgId = NGPHEsbUtils.stanMsgMap.get(stan);
							if(msgId!=null && StringUtils.isNotBlank(msgId)&& StringUtils.isNotEmpty(msgId))
							{
								resObj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
								logger.info("The error code set while the response 0210 is being sent " + canonicalData.getMsgErrorCode());
								if (canonicalData.getMsgErrorCode() == null)
								{
									resObj.setResCode("00");
								}
								else
								{
									resObj.setResCode(canonicalData.getMsgErrorCode());
								}
								resObj.setResTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
								resObj.setCanonicalObj(canonicalData);
							}
							else
							{
								logger.error("Mapping message reference not found for the STAN " + stan);
								EventLogger.logEvent("NGPHISCSVC0006", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//The mapping payment request with IMPS reference {ClrgSysReference} for the payment response being created could not be found.
							}
						}
						else
						{
							logger.error("Empty Stan is Received for payment response creation");
						}
						// Updating the Global Cache Maps
						updateGlobalCacheMap(canonicalData,resObj);
					}
				}
				// This is Verification Request with flag value as 2
				else if(req_res_Flag==2)
				{
					//FIXME add field47 in fields format table and handle here accordingly to set appropriate variables in canonical
					canonicalData.setMsgTxnType("33");
				}
				//Storing the final message construction in a local Variable
				mes = constructMessage(canonicalData);
				
				//check if mes is null or empty, if so Log Error
				if(mes==null || StringUtils.isEmpty(mes) || StringUtils.isBlank(mes))
				{
					logger.error("Null message received after construction logic by ISO8583ChannelServiceImpl");
					EventLogger.logEvent("NGPHISCSVC0007", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//Message construction failed for IMPS payment request / response.
					return null;
				}		
				swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
				int idx = mes.indexOf("{999:");
				String mesWO999 = mes;
				if (idx > 0)
				{
					mesWO999 = mes.substring(0, idx);
				}
				swiftParserDao.insertRawMessage(canonicalData.getMsgHost(), canonicalData.getMsgRef(), mesWO999, canonicalData.getDstMsgChnlType(),canonicalData.getMsgDirection());
				//check if the EIStatus is not 2
				if(!(esbServiceDao.getEIStatus(canonicalData.getDstEiId())==2))
				{
					EventLogger.logEvent("NGPHISCSVC0008", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//The connection to IMPS server is not active and hence the payment request is queued.
					//Store in TA_TCPQUEUE Table for status = 1
					esbServiceDao.populateTCPStatus(mes, canonicalData.getMsgRef(), 1);
				}
			}
			else
			{
				//FIXME If its not IMPS then we need to write certain functionality
			}
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in creating ISO8583 payment request / response");
			logger.error(e,e);
			EventLogger.logEvent("NGPHISCSVC0013", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//Exception occurred in IMPS Payment request / response creation. Refer error log for details.
			throw new Exception(e);
		}
		return mes;  
	}//method body closed
	
	//A Generic method for Message construction for ISO8583
	private String constructMessage(NgphCanonical canonicalData) throws Exception
	{
		logger.info("Inside Construct Method of ISO Channel Service");
		
		// Performing packed BCD on data
		ISOPackager packager = new BASE24Packager();
		// Create ISO Message
		ISOMsg isoMsg = new ISOMsg();
		isoMsg.setPackager(packager);
		
		//Store values where fldNo is present
		String prsntFldNo = null;
		//Will hold the complete message finally constructed
		StringBuilder isoMes = new StringBuilder();
		//Will hold the bit values. 
		StringBuilder bitMap = new StringBuilder();
		//Will hold the canonical values
		StringBuilder canValues = new StringBuilder();
		logger.info(canonicalData.getDstMsgChnlType() + "\t" + canonicalData.getDstMsgType() + "\t" + canonicalData.getDstMsgSubType());
		// Check for Null for channelType/SrcMsgType/SrcSubMsgType
		if(StringUtils.isNotBlank(canonicalData.getDstMsgChnlType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgChnlType()) && StringUtils.isNotBlank(canonicalData.getDstMsgType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgType()) && StringUtils.isNotBlank(canonicalData.getDstMsgSubType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()))
		{
			String key = canonicalData.getDstMsgChnlType() + canonicalData.getDstMsgType() + canonicalData.getDstMsgSubType();
			logger.info("The key Value is : " + key);
			//Fetch the ArrayList Holding the Bean Objects where Canonical Attributes are mapped to corresponding field.
			ArrayList<FieldCanonicalAttribute> list = SFMSChannelServiceImpl.dataInitializer_CanonicalAttributes.get(key);
			logger.info(list.size()+"************");
			// Store the message type to isoMessage
			isoMes.append(canonicalData.getDstMsgType());
			isoMes.append(canonicalData.getDstMsgSubType());
			//Storing the Previous field value
			String prevFldNo = "";
			String canSplValue="";
			String canFldValue="";
			String prevFldEocInd ="";
			String nextFldEocInd="";
			String nextFldNo="";
			String canSubFldVal="";
			String canFldVal="";
			String canVal="";
			boolean flag=false;
			boolean secBitMap=false;
			
			// Fetching All Methods of NGPH Canonical class using Reflection API
			try 
			{
				//Setting the message type and sub type (MTI)
				isoMsg.setMTI(canonicalData.getDstMsgType() + canonicalData.getDstMsgSubType());
				
				@SuppressWarnings("rawtypes")
				Class c = Class.forName(canonicalData.getClass().getName());
	           //Fetch all the Public methods of NGPHCanonical Class
				Method allMethods[] = c.getDeclaredMethods();
				ArrayList<Method> functionHolder = new ArrayList<Method>();
				for(int i=0;i<allMethods.length;i++)
				{
					if(allMethods[i].getName().startsWith("get"))
					{
						functionHolder.add(allMethods[i]);
					}
				}
        		//Iterating over the ArrayList Containing Pojo Objects.
            	for(int i=0;i<list.size();i++)
    			{
            		flag=false;
            		canFldValue = "";
            		FieldCanonicalAttribute object = list.get(i);
            		FieldCanonicalAttribute nextobject;
            		if(i<list.size()-1)
            		{
            			nextobject = list.get(i+1);
            			nextFldEocInd = nextobject.getFieldEocInd();
            			nextFldNo = nextobject.getFieldNo();
            		}
            		canVal = "get" + object.getFieldCanonicalAtt();
            		String fldNo = object.getFieldNo();
            		String fldSeq = object.getFieldSeq();
            		String fldManOpt = object.getFieldCompMandOpt();
            		int fldCompSeq = Integer.parseInt(object.getFieldCompSeq());
            		String fldEocInd = object.getFieldEocInd();
            		String fldCnsdr = object.getFieldcnsdr();
            		String fldCompFmt = object.getField_comp_fmt();
            		//logger.info(canVal + "\t" + fldNo + "\t" + fldSeq + "\t" + fldManOpt +"\t" + fldCompSeq + "\t" + fldEocInd + "\t" + fldCnsdr +"\t" + fldCompFmt);
            		if(object.getFieldCanonicalAtt()==null && !prevFldNo.equalsIgnoreCase(fldNo))
            		{
            			bitMap.append(0);
            		}
            		else
	            	{
	            		//Handling special cases for CAN_SPL as these will not be present in Canonical Method
	            		if(canVal.contains("CAN_SPL"))
	            		{
	                		//Handling special cases for ~MOB~ and ~001~
	            			if(canVal.equalsIgnoreCase("getCAN_SPLSTATIC"))
	            			{
	            				canSplValue = fldCnsdr.substring(fldCnsdr.indexOf("~")+1, fldCnsdr.lastIndexOf("~"));
	            				if (fldCnsdr.length() > fldCnsdr.lastIndexOf("~")+1)
	            				{
	            					String tmpCndstr = null;
	            					tmpCndstr = fldCnsdr.substring (fldCnsdr.lastIndexOf("~")+1);
	            					fldCnsdr = tmpCndstr; 
	            				}
	            			}
	            			else if(canVal.equalsIgnoreCase("getCAN_SPLINIT"))
	            			{
	            				canSplValue = esbServiceDao.getInitialisedValBranch(fldCnsdr, canonicalData.getMsgBranch());
	               			 
	            			}
	            			else if(canVal.equalsIgnoreCase("getCAN_SPLSRCEICATG"))
	            			{
	            				canSplValue = esbServiceDao.getHostCategory(canonicalData.getMsgHost());
	            			}
	            			//FIXME Sign Data or Security Data forming to be done
	            			else if(canVal.equalsIgnoreCase("getCAN_SPLKEYDATA"))
	            			{
	            				canSplValue = "122333444455555666666777777788888888999999999";
	            			}
	
	            			String formattedVal=canSplValue;
							if(fldCnsdr!=null && StringUtils.isNotBlank(fldCnsdr)&& StringUtils.isNotEmpty(fldCnsdr) && !fldCnsdr.contains("APPLENF"))
							{
								formattedVal = performComputation(canSplValue,fldCnsdr,fldCompFmt);
							}
							canSplValue = formattedVal;	
	            			if(fldEocInd!=null && StringUtils.isNotBlank(fldEocInd)&& StringUtils.isNotEmpty(fldEocInd) && prevFldEocInd!=fldEocInd)
	            			{
								if(nextFldEocInd !=null)
								{
									canSubFldVal = canSubFldVal + canSplValue;
			            			if(!nextFldEocInd.equalsIgnoreCase(fldEocInd))
			        				{
			        					String temp = "000" + canSubFldVal.length();
			        					canSubFldVal = temp.substring(temp.length()-3, temp.length()) + canSubFldVal;
			        					//if the txn is not P2A type then don't include subfields 059 and 062
			        					if (fldEocInd.equalsIgnoreCase("059") || fldEocInd.equalsIgnoreCase("062"))
										{
											if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER)))
											{
												canFldVal = canFldVal + fldEocInd + canSubFldVal;
											}
										}
			        					else if (fldEocInd.equalsIgnoreCase("057"))
			        					{
			        						if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER)))
											{
												canFldVal = canFldVal + fldEocInd + canSubFldVal;
											}
			        					}
										else
										{
											canFldVal = canFldVal + fldEocInd + canSubFldVal;
										}
			        					if (!nextFldNo.equalsIgnoreCase(fldNo))
			        					{
			        						if (fldCnsdr!=null && fldCnsdr.contains("APPLENF"))
			        						{
			        							temp = "000" + canFldVal.length();
			        							canFldVal = temp.substring(temp.length()-3, temp.length()) + canFldVal;  
			        						}
			        						canValues.append(canFldVal);
			        					}
			        					canSubFldVal = "";
			        				}
								}
								else
								{
									canSubFldVal = canSubFldVal + canSplValue;
		        					String temp = "000" + canSubFldVal.length();
		        					canSubFldVal = temp.substring(temp.length()-3, temp.length()) + canSubFldVal;
		        					//if the txn is not P2A type then don't include subfields 059 and 062
		        					if (fldEocInd.equalsIgnoreCase("059") || fldEocInd.equalsIgnoreCase("062"))
									{
										if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER)))
										{
											canFldVal = canFldVal + fldEocInd + canSubFldVal;
										}
									}
		        					else if (fldEocInd.equalsIgnoreCase("057"))
		        					{
		        						if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER)))
										{
											canFldVal = canFldVal + fldEocInd + canSubFldVal;
										}
		        					}
									else
									{
										canFldVal = canFldVal + fldEocInd + canSubFldVal;
									}
		        					if (!nextFldNo.equalsIgnoreCase(fldNo))
		        					{
		        						if (fldCnsdr!=null && fldCnsdr.contains("APPLENF"))
		        						{
		        							temp = "000" + canFldVal.length();
		        							canFldVal = temp.substring(temp.length()-3, temp.length()) + canFldVal;  
		        						}
		        						canValues.append(canFldVal);
		        						canFldVal = "";
		        					}
		        					canSubFldVal = "";
		        				}
	            			}
	            			else
	            			{
	            				canFldVal = canFldVal + canSplValue;
	            				if (!nextFldNo.equalsIgnoreCase(fldNo))
	        					{
	        						if (fldCnsdr!=null && fldCnsdr.contains("APPLENF"))
	        						{
	        							String temp = "000" + canFldVal.length();
	        							canFldVal = temp.substring(temp.length()-3, temp.length()) + canFldVal;  
	        						}
	        						canValues.append(canFldVal);
	        						canFldVal = "";
	        					}
	            			} 
	            			if(!prevFldNo.equalsIgnoreCase(fldNo))
							{
	            				if(canSplValue!=null && StringUtils.isNotBlank(canSplValue) && StringUtils.isNotEmpty(canSplValue))
	            				{
	            					flag=true;
	            					bitMap.append(1);
	            					prsntFldNo = object.getFieldNo();
	            				}
	            				else
	            				{
	            					flag=true;
	            					bitMap.append(0);
	            				}
							}
						}
	            		else
	            		{
	    					/*
	    					 * Special case taken for CAN_ORG, if CAN_ORG is present replace it with empty space
	    					 * After doing so canVal matched with the canonical method and normal execution happens.
	    					 * Set a flag status and check this flag while invoking on canonical methods 
	    					 */
	    					boolean isCan_OrgSpecial = false;
	    					if(canVal.contains("CAN_ORG"))
	            			{
	    						canVal= canVal.replace("CAN_ORG", "");
	    						isCan_OrgSpecial=true;
	            			}
		            		//Processing for Normal Cases
		    				for (int j=0;j<functionHolder.size();j++) 
				            {
		    					Method m = functionHolder.get(j);
		    					String mname = m.getName();
			    				if(mname.equalsIgnoreCase(canVal))
			    				{
			    					try
									{
			    						//Check the flag status of CAN_ORG
			    						if(isCan_OrgSpecial==true)
			    						{
			    							//get the related Canonical and invoke on the methods
			    							NgphCanonical relCanonical = canonicalData.getRelCanonical();
			    							if(relCanonical!=null)
			    							{
			    								//invoke value on this Related Canonical
			    								Object relObj = m.invoke(relCanonical,new Object[]{});
			    								String relCanFldVal = null;
			    								if(relObj!=null) 
		    									{
		    										//if(relObj instanceof Timestamp)
													//{
													//	relObj = timeStampFormatter(relObj);	
													//}
		    										relCanFldVal = relObj.toString();
		    										//checking for special cases present in field consideration
		    										if(fldCnsdr!=null && StringUtils.isNotBlank(fldCnsdr)&& StringUtils.isNotEmpty(fldCnsdr))
		    										{
		    											//perform special cases logic
		    											String formattedVal = performComputation(relObj,fldCnsdr,fldCompFmt);
		    											//check if after computation value is not null
		    											if(formattedVal!=null && StringUtils.isNotBlank(formattedVal) && StringUtils.isNotEmpty(formattedVal))
		    											{
		    												relCanFldVal = formattedVal;
		    											}
		    										}
		    										if(canVal.equalsIgnoreCase("getSenderBank"))
		    										{
		    											//get its code Value
		    											if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(relCanonical.getMsgDirection().trim()))
														{
		    												relCanFldVal = esbServiceDao.getIsoPartyCode(relObj.toString());
														}
														else
														{
															relCanFldVal = esbServiceDao.getIsoCodeByBranch(relCanonical.getMsgBranch());
														}
		    											String formattedVal=relCanFldVal;
		    											if(fldCnsdr!=null && StringUtils.isNotBlank(fldCnsdr)&& StringUtils.isNotEmpty(fldCnsdr))
		    											{
		    												formattedVal = performComputation(relCanFldVal,fldCnsdr,fldCompFmt);
		    											}
		    											relCanFldVal = formattedVal;
		    										}
		    										canValues.append(relCanFldVal);
		    										if(!prevFldNo.equalsIgnoreCase(fldNo))
		    										{
		    											flag=true;
		    											bitMap.append(1);
		    											prsntFldNo = object.getFieldNo();
		    										}
			    								}
											}
			    							else
			    							{
			    								if(!prevFldNo.equalsIgnoreCase(fldNo))
			    								{
			    									bitMap.append(0);
			    								}
			    								logger.info("Related Canonical is null");
			    							}
				    						//break the inner Function Loop from here itself as there is no need to traverse down
			    							break;
			    						}
			    						Object obj = m.invoke(canonicalData,new Object[]{});
										if(obj!=null) 
										{
											//append one for to bitmap SB as its canonical value is present
											if(!prevFldNo.equalsIgnoreCase(fldNo))
											{
												flag=true;
												bitMap.append(1);
												prsntFldNo = object.getFieldNo();
											}
											//revert the MsgCurrency and Sender Bank to their codes
											if(canVal.equalsIgnoreCase("getSenderBank"))
											{
												//get its code Value
												if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection().trim()))
												{
													canFldValue = esbServiceDao.getIsoPartyCode(obj.toString());
												}
												else
												{
													canFldValue = esbServiceDao.getIsoCodeByBranch(canonicalData.getMsgBranch());
												}
												//if this is null then either it is not available in database for that BIC or IFSC or we got the iso code itself
												if (canFldValue == null)
												{
													canFldValue = obj.toString();
												}
												String formattedVal=canFldValue;
												if(fldCnsdr!=null && StringUtils.isNotBlank(fldCnsdr)&& StringUtils.isNotEmpty(fldCnsdr))
												{
													formattedVal = performComputation(canFldValue,fldCnsdr,fldCompFmt);
												}
												canValues.append(formattedVal);
												break;
											}
											else if(canVal.equalsIgnoreCase("getMsgCurrency"))
											{
												//get its code Value
												canFldValue = esbServiceDao.getIsoCurrCode(obj.toString());
												canValues.append(canFldValue);
												break;
											}
											//checking for special cases present in field consideration
											if(fldCnsdr!=null && StringUtils.isNotBlank(fldCnsdr)&& StringUtils.isNotEmpty(fldCnsdr))
											{
												//perform special cases logic
												String formattedVal = performComputation(obj,fldCnsdr,fldCompFmt);
												//check if after computation value is not null
												if(formattedVal!=null && StringUtils.isNotBlank(formattedVal) && StringUtils.isNotEmpty(formattedVal))
												{
													canFldValue = canFldValue + formattedVal;
													//append the canonical values to canValues SB.
												}
											}
											//If the field Consideration value is null, no need to perform any special computation on the values
											else
											{
												// Either the object is a TimeStamp/BigDecimal/String
												// check if the Object value is BigDecimal, then do formatting
												if(obj instanceof BigDecimal)
												{
													//obj = bigDecimalFormatter(obj);	
													double d = Double.parseDouble(obj.toString())*100;
													String tt = d+"";
													String replacer = tt.substring(0, tt.indexOf("."));
													int fldLen = 0;
													//check if ! is present get the length upto last 2 chars
													if(fldCompFmt.contains("!"))
													{
														fldLen = Integer.parseInt(fldCompFmt.substring(0, fldCompFmt.length()-2));
													}
													//check if ! is not present get the length upto last 1 chars
													else
													{
														fldLen = Integer.parseInt(fldCompFmt.substring(0, fldCompFmt.length()-1));
													}	
													obj = StringUtils.leftPad(replacer, fldLen, "0");;
												}
												// check if the Object value is TimeStamp, then do formatting and convert to normal String
												else if(obj instanceof Timestamp)
												{
													obj = timeStampFormatter(obj);	
												}
												//append the canonical values to canValues SB.
												canFldValue = obj.toString();
											}
											
											if(fldEocInd!=null && StringUtils.isNotBlank(fldEocInd)&& StringUtils.isNotEmpty(fldEocInd) && prevFldEocInd!=fldEocInd)
					            			{
												if(nextFldEocInd !=null)
												{
													canSubFldVal = canSubFldVal + canFldValue;
						        					String temp = "000" + canSubFldVal.length();
						        					canSubFldVal = temp.substring(temp.length()-3, temp.length()) + canSubFldVal;
						        					//if the txn is not P2A type then don't include subfields 059 and 062
						        					if (fldEocInd.equalsIgnoreCase("059") || fldEocInd.equalsIgnoreCase("062"))
													{
														if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER)))
														{
															canFldVal = canFldVal + fldEocInd + canSubFldVal;
														}
													}
						        					if (fldEocInd.equalsIgnoreCase("057"))
													{
						        						if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER)))
														{
															canFldVal = canFldVal + fldEocInd + canSubFldVal;
														}
													}
													else
													{
														canFldVal = canFldVal + fldEocInd + canSubFldVal;
													}
						        					if (!nextFldNo.equalsIgnoreCase(fldNo))
						        					{
						        						if (fldCnsdr!=null && fldCnsdr.contains("APPLENF"))
						        						{
						        							temp = "000" + canFldVal.length();
						        							canFldVal = temp.substring(temp.length()-3, temp.length()) + canFldVal;  
						        						}
						        						canValues.append(canFldVal);
						        						canFldVal = "";
						        					}
						        					canSubFldVal = "";
												}
												else
												{
													canSubFldVal = canSubFldVal + canFldValue;
						        					String temp = "000" + canSubFldVal.length();
						        					canSubFldVal = temp.substring(temp.length()-3, temp.length()) + canSubFldVal;
						        					//if the txn is not P2A type then don't include subfields 059 and 062
						        					if (fldEocInd.equalsIgnoreCase("059") || fldEocInd.equalsIgnoreCase("062"))
													{
														if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER)))
														{
															canFldVal = canFldVal + fldEocInd + canSubFldVal;
														}
													}
						        					if (fldEocInd.equalsIgnoreCase("057"))
													{
														if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER)))
														{
															canFldVal = canFldVal + fldEocInd + canSubFldVal;
														}
													}
													else
													{
														canFldVal = canFldVal + fldEocInd + canSubFldVal;
													}
						        					if (!nextFldNo.equalsIgnoreCase(fldNo))
						        					{
						        						if (fldCnsdr!=null && fldCnsdr.contains("APPLENF"))
						        						{
						        							temp = "000" + canFldVal.length();
						        							canFldVal = temp.substring(temp.length()-3, temp.length()) + canFldVal;  
						        						}
						        						canValues.append(canFldVal);
						        						canFldVal = "";
						        					}
						        					canSubFldVal = "";
												}
					            			}
											else
											{
												canFldVal = canFldVal + canFldValue;
					            				if (!nextFldNo.equalsIgnoreCase(fldNo))
					        					{
					        						if (fldCnsdr!=null && fldCnsdr.contains("APPLENF"))
					        						{
					        							String temp = "000" + canFldVal.length();
					        							canFldVal = temp.substring(temp.length()-3, temp.length()) + canFldVal;  
					        						}
					        						canValues.append(canFldVal);
					        						canFldVal = "";
					        					}
											}
										}
										//append zero(0) to bitmap StringBuilder
										else
										{
											if(!prevFldNo.equalsIgnoreCase(fldNo))
											{
											//if Canonical value is null that means that bit value is not present, hence append 0 to bitmap StringBuilder
											flag=true;	
											bitMap.append(0);
											}
										}
									}
			    					catch (Exception e) 
			    					{
			    						logger.error("Exception occured in fetching canonical values when constructing IMPS message for field " + fldNo);
										logger.error(e, e);
										throw new Exception(e);
									}
			                		break;
			    				}
				            }
	            		}
		        		prevFldNo = fldNo;
		        		prevFldEocInd = fldEocInd;
		        		if(flag==false && !prevFldNo.equalsIgnoreCase(fldNo))
		        		{
		        			bitMap.append(0);
		        		}
		        		if (Integer.parseInt(fldNo)  > 65)
		        		{
		        			secBitMap=true;
		        		}
	            	}	
	    		}
	        	//Add  the last value into the message
	        	if(canVal.contains("CAN_SPL"))
	        	{
	        		canValues.append(canSplValue);
	        	}
	        	else
	        	{
	        		canValues.append(canFldVal);
	        	}
	        	if (secBitMap==true)
	        	{
	        		bitMap.insert(0, "1");
	        	}
	        	else
	        	{
	        		bitMap.insert(0, "0");
	        	}
	        	//logger.info(bitMap+"###########");
	        	//converting the binary values to HexDecimal Format
	        	String hexaVal = binaryToHexa(bitMap.toString());
	        	//appending the Bitmap and Canonical Values SB to final isoMessage.
	        	isoMes.append(hexaVal);
	        	isoMes.append(canValues);
	        	
	        	//Store only the present field No's.
	        	if(StringUtils.isNotBlank(prsntFldNo) && StringUtils.isNotEmpty(prsntFldNo) && StringUtils.isNotBlank(canValues.toString()) && StringUtils.isNotEmpty(canValues.toString()))
	        	{
	        		isoMsg.set(prsntFldNo, canValues.toString());
	        	}
			}
			catch (Exception e) 
			{
				logger.error("Exception occured in IMPS message construction for previous field and next field " + prevFldNo + " -- " + nextFldNo);
				logger.error(e, e);
				EventLogger.logEvent("NGPHISCSVC0009", canonicalData, ISO8583ChannelServiceImpl.class, canonicalData.getMsgRef());//Exception occurred in constructing IMPS message. Refer error log for details.
				isoMes=null;
				throw new Exception(e);
			}
			String mesLeninHexa = Integer.toHexString(isoMes.length()).toUpperCase();
			String tempLen = "0000" + mesLeninHexa;
			String finalLen = tempLen.substring(tempLen.length()-4, tempLen.length());
			isoMes.insert(0, finalLen);
			isoMes.append("{999:" + canonicalData.getDstEiId() + "," + canonicalData.getMsgRef() + "}");
		}
		else
		{
			logger.error("Dst Channel Type , Dst Mes Type, Dst Sub Mes Type is null while constructing IMPS message");
		}	
		
		byte[] data = isoMsg.pack();

		String mesLength = Integer.toHexString(ISOUtil.hexString(data).length());
		if(mesLength.length()==1)
		{
			mesLength = "000" +mesLength; 
		}
		else if(mesLength.length()==2)
		{
			mesLength = "00" +mesLength; 
		}
		else if(mesLength.length()==3)
		{
			mesLength = "0" +mesLength; 
		}
		
		//Convert decimal Value to Hexa val and hexa to Packed BCD..
		String packedMes = mesLength + ISOUtil.hexString(data);
		
		System.out.println("Final Message in Packed BCD Hexa format : " + packedMes);
		//return isoMes.toString();

		//Variable that holds the final String Return Message based on Test/Production enviroment.
		String returnMes = null;
		
		//Fetch the flag value from DB
		String isTestFlag = esbServiceDao.getInitialisedValue("ISSIMULATOR");
		
		if(isTestFlag!=null && StringUtils.isNotBlank(isTestFlag) && StringUtils.isNotEmpty(isTestFlag)&& isTestFlag.equalsIgnoreCase("Y"))
		{
			returnMes = isoMes.toString();
		}
		else
		{
			returnMes = packedMes;
		}
		return returnMes;
	}
	
	//Special cases handled by this method
	private String performComputation(Object canVal, String fldCnsdr, String fldCompFmt)throws Exception
	{
		//This is the final value that will be returned by this method
		String finalVal = null;
		//Store length of each field component
		int fldLen = 0;
		//check if ! is presnt get the length upto last 2 chars
		if(fldCompFmt.contains("!"))
		{
			fldLen = Integer.parseInt(fldCompFmt.substring(0, fldCompFmt.length()-2));
		}
		//check if ! is not presnt get the length upto last 1 chars
		else
		{
			fldLen = Integer.parseInt(fldCompFmt.substring(0, fldCompFmt.length()-1));
		}
		//Fetch the Enum val for the corresponding value
		IsoFieldEnum val = IsoFieldEnum.findEnumByTag(fldCnsdr);
		String tempLen;
		int idxPling = 0;
		// check if the enum gets the null value
		if(val!=null)
		{
			switch (val) 
			{
				case YYMMHHMMSS:
						SimpleDateFormat sdf = new SimpleDateFormat("yyMMhhmmss");
						finalVal = sdf.format(canVal);
						break;
				case LAST:
						if(canVal.toString().length()>fldLen)
						{
							finalVal=canVal.toString().substring(canVal.toString().length()-fldLen, canVal.toString().length());
						}
						else
						{
							finalVal = canVal.toString();
						}
						break;
				case HHMMSS:
						SimpleDateFormat sdff = new SimpleDateFormat("hhmmss");
						finalVal = sdff.format(canVal);
						break;
				case YYMM:
						SimpleDateFormat sdfff = new SimpleDateFormat("yyMM");
						finalVal = sdfff.format(canVal);
						break;
				case MMDD:
						SimpleDateFormat sdMMDD = new SimpleDateFormat("MMdd");
						finalVal = sdMMDD.format(canVal);
						break;
				case FIRST:
						if(canVal.toString().length()>fldLen)
						{
							finalVal=canVal.toString().substring(0, canVal.toString().length()-fldLen);
						}
						else
						{
							finalVal = canVal.toString();
						}
	
						break;
				case APPLEN:
						finalVal=canVal.toString().length() + canVal.toString();
					break;
				case APPLEN2:
						tempLen = "00" + canVal.toString().length();
						finalVal=tempLen.substring(tempLen.length()-2, tempLen.length()) + canVal.toString(); 
					break;
				case APPLEN3:
						tempLen = "000" + canVal.toString().length();
						finalVal=tempLen.substring(tempLen.length()-3, tempLen.length()) + canVal.toString(); 
					break;
				case PADSPACE:
						idxPling = fldCompFmt.indexOf("!");
						if (idxPling > -1)
						{
							finalVal = StringUtils.rightPad(canVal.toString(),Integer.parseInt(fldCompFmt.substring(0,idxPling)));
						}
					break;
				default:
						finalVal = canVal.toString();
						break;
			}// switch closed
		}// if loop closed
		else
		{
			//just return the string that was received so that even if special consideration codes are not available in enum this function would not empty the received string
			finalVal = canVal.toString(); 
			logger.warn("There was no mapping found in IsoFieldEnum");
		}
		//logger.info("Final value : " + finalVal);
		return finalVal;
	}
	
    //convert binary values to HexDecimal Format
	private String binaryToHexa(String bitval)throws Exception
	{
		//populates the hashMap
		HashMap<String, String> binaryValues = populateBinToHexa();
		
		//final value that will returned after computation
		StringBuilder bnryVal = new StringBuilder();

		//split the input String for every 4 chars
		String[] data = bitval.split("(?<=\\G....)");
		
		for(int i=0;i<data.length;i++)
		{
			//logger.info(data[i]);
			bnryVal.append(binaryValues.get(data[i]).toString());
		}

		return bnryVal.toString();
	}

    //Private method containing a hashMap holding HexDecimal values for Binary val
	private HashMap<String, String> populateBinToHexa()throws Exception
	{
		HashMap<String, String> binToHex = new HashMap<String, String>();

		binToHex.put("0000","0");
		binToHex.put("0001", "1");
		binToHex.put("0010", "2");
		binToHex.put("0011", "3");
		binToHex.put("0100", "4");
		binToHex.put("0101", "5");
		binToHex.put("0110", "6");
		binToHex.put("0111", "7");
		binToHex.put("1000", "8");
		binToHex.put("1001", "9");
		binToHex.put("1010", "A");
		binToHex.put("1011", "B");
		binToHex.put("1100", "C");
		binToHex.put("1101", "D");
		binToHex.put("1110", "E");
		binToHex.put("1111", "F");

		return binToHex;
	}

	//check stan for business dates
	public String getStan(String branch)throws Exception
	{
		String stan = null;
		try
		{
			int cycle = Integer.parseInt(esbServiceDao.getStan("ImpsStan"));
			String bussDate = esbServiceDao.getbusday_Date(branch);
			Date currdate  = esbServiceDao.getcurrbusday_Date("ImpsStan");
			//Curr busy date is present and is not null
			if(currdate !=null)
			{
				//fetch the date and convert into same format
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
				String date = sdf.format(currdate);
				
				Date busdate = null;
				Date curdate = null;
				//compare dates in String format to Date format
				 try 
				 { 
					 DateFormat formatter ; 
					 formatter = new SimpleDateFormat("dd-MMM-yy");
					 busdate = (Date)formatter.parse(bussDate);
					 curdate = (Date)formatter.parse(date);
					 
					 logger.info("Bussiness date -> " + busdate);
					 logger.info("Current date in Seq table-> " + curdate);
					 
				  } catch (ParseException e)
				  {
					  logger.error(e, e);  
					  throw new Exception(e);
				  } 
				  catch (Exception e) {
					  logger.error(e, e);
					  throw new Exception(e);
				}
				  
				  //compare two Dates for equality
				  //If dates are not equal set cycle =0
				  if(curdate.compareTo(busdate) > 0 || curdate.compareTo(busdate) < 0)
				  {
					  logger.info("Dates are not equal");
					  //update the cycle to 0
					  cycle=0;
					  ++cycle;
					  String temp = "00000" + cycle;
					  stan =temp.substring(temp.length()-6, temp.length());
					  
					  esbServiceDao.updateStan(bussDate, stan, "ImpsStan");
				  }
				  //update the next cycle
				  else
				  {
					  logger.info("First Date and Second Date are equal");
					  ++cycle;
					  String temp = "00000" + cycle;
					  stan =temp.substring(temp.length()-6, temp.length());
					  
					  esbServiceDao.updateStan(bussDate, stan, "ImpsStan");
				  }
			}
			//curr busy date is null as first time there will be no data inserted
			else
			{
				// update Ta_Seq table for date and stan value
				++cycle;
				stan ="00000" + cycle;
				esbServiceDao.updateStan(bussDate, stan, "ImpsStan");
			}
	
			logger.info("Value returned for Stan" + "\t" + stan);
		}
		catch (Exception e) 
		{
			logger.error("Exception occurred in generating STAN");
			logger.error(e,e);
			EventLogger.logEvent("NGPHISCSVC0010", null, ISO8583ChannelServiceImpl.class, null);//Exception occured in generating STAN for the IMPS payment. Refer error log for details.
			throw new Exception(e);
		}
		return stan;
	}
	
	//YDDDHHSSSSSS
	//Last digit of the year + Julian date + hour + STAN
	private String getRetRefNo(String stan)throws Exception
	{
		String result =null;
		try
		{
			StringBuilder sb = new StringBuilder();
			Calendar cal=Calendar.getInstance();
			String year=cal.get(Calendar.YEAR) +"";
			String hour = "0" + cal.get(Calendar.HOUR_OF_DAY);
			String dayofyear = "000" + cal.get(Calendar.DAY_OF_YEAR)+"";
			//Fetch last digit of year
			String lastDgtOfyr = year.substring(year.length()-1, year.length());
			sb.append(lastDgtOfyr);
			//fetch Julian date
			String julianDate  = dayofyear.substring(dayofyear.length()-3, dayofyear.length());
			sb.append(julianDate);
			//fetch hour
			String hr  = hour.substring(hour.length()-2, hour.length());
			sb.append(hr);
			//append stan 
			sb.append(stan);
			result = sb.toString();
		}
		catch (Exception e) 
		{
			logger.error("Exception occurred in generating Reterival Reference Number for IMPS payment");
			logger.error(e,e);
			EventLogger.logEvent("NGPHISCSVC0011", null, ISO8583ChannelServiceImpl.class, null);//Exception occured in generating RRN for the IMPS payment. Refer error log for details.
			result = null;
			throw new Exception(e);
		}
		return result;
	}
	
	//Method to update the Response Bean Object in the Global Map Cache.
	public void updateGlobalCacheMap(NgphCanonical canObj, ResponseBean resObj)throws Exception
	{
		try
		{
			//update the response bean object in Global Cache with a unique msgId.
			if(resObj!=null && canObj.getMsgRef()!=null && StringUtils.isNotBlank(canObj.getMsgRef()) &&  StringUtils.isNotEmpty(canObj.getMsgRef()))
			{
				NGPHEsbUtils.populateResponseObject(canObj.getMsgRef(), resObj);
			}
			else
			{
				logger.info("Fail to update Global Map Either Response Object or canObj.getMsgRef() is null");
			}
			
			//Fetch the STAN Value from Canonical Object
			String stan = null;
			if(canObj.getClrgSysReference()!=null && StringUtils.isNotBlank(canObj.getClrgSysReference()) && StringUtils.isNotEmpty(canObj.getClrgSysReference()))
			{
				//stan = canObj.getClrgSysReference().substring(canObj.getClrgSysReference().length()-6, canObj.getClrgSysReference().length());
				stan = canObj.getClrgSysReference();;
				
				//check if STAN Key is already present or not
				if(NGPHEsbUtils.stanMsgMap.containsKey(stan))
				{
					//remove the STAN key
					NGPHEsbUtils.stanMsgMap.keySet().remove(stan);
				}
				//updating STAN and MSgId Map
				if(canObj.getMsgRef()!=null && StringUtils.isNotBlank(canObj.getMsgRef()) &&  StringUtils.isNotEmpty(canObj.getMsgRef()) && stan!=null && StringUtils.isNotBlank(stan) && StringUtils.isNotEmpty(stan))
				{
					NGPHEsbUtils.populateStanMsgId(stan, canObj.getMsgRef());
				}
				else
				{
					logger.warn("Fail to update Global Map Either Stan or canObj.getMsgRef() is null");
	
				}
			}
			else
			{
				logger.warn("Stan value not found bcoz CustTxn Refrence is null");
			}
		}
		catch (Exception e) 
		{
			logger.error("Exception occurred in updating global cache with the imps transaction details");
			logger.error(e,e);
			EventLogger.logEvent("NGPHISCSVC0012", canObj, ISO8583ChannelServiceImpl.class, canObj.getMsgRef());//Exception occured in updating global cache for IMPS payment. Refer error log for details.
			throw new Exception(e);
		}
	}
	
	private String timeStampFormatter(Object o)
	{
		if(o.toString().contains("00:00:00.0"))
		{
			String dataVal = o.toString().substring(0, 10);
			return dataVal.replace("-", "");
		}
		else
		{
			String dataVal = o.toString().substring(0, o.toString().indexOf("."));
			String hiphen  = dataVal.replace("-", "");
			String space = hiphen.replace(" ", "");
			String colon = space.replace(":", "");
			return colon;    
		}
	}
}
