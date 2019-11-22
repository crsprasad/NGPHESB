package com.logica.ngph.esb.servicesImpl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.EnumDateFormat;
import com.logica.ngph.esb.Dtos.FieldCanonicalAttribute;
import com.logica.ngph.esb.Dtos.Raw_Msgs;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.enums.SFMSFieldsEnum;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;

/**
 * 
 * @author guptarb
 *
 */
public class SFMSChannelServiceImpl implements SFMSChannelService
{
	static Logger logger = Logger.getLogger(SFMSChannelServiceImpl.class);	
	private EsbServiceDao esbServiceDao;
	private SwiftParserDao swiftParserDao;
	/**
	 * @param esbServiceDao
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	
	public static HashMap<String, ArrayList<FieldCanonicalAttribute>> dataInitializer_CanonicalAttributes = new HashMap<String, ArrayList<FieldCanonicalAttribute>>();


	/**
	 * This is a Loader method that will load field values and Canonical Variable in HashMap(dataInitializer).
	 * The Purpose of same is to cache the data instead of hitting the DB. 
	 */
	public void populatefieldsForCanonical()throws Exception
	{
		esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
		dataInitializer_CanonicalAttributes = esbServiceDao.loadFieldsData();
		swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
		//logger.info(dataInitializer_CanonicalAttributes);
	}
	/*
	 * Test Purpose
	 */
	public static void main(String[] args) {/*
		
		NgphCanonical obj =  new NgphCanonical();
		obj.setMsgChnlType("RTGS");
		obj.setSrcMsgType("298");
		obj.setSrcMsgSubType("R41");
		
		ApplicationContextProvider.initializeContextProvider();
		new RtgsChannelServiceImpl().populatefieldsForCanonical();
		try {
			new RtgsChannelServiceImpl().constructMessageTextBlock(obj);
		} catch (NGPHException e) {
			e.printStackTrace();
		}
	*/}
	
	//For Ack
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.services.SFMSChannelService#buildRtgsMessageforAck(com.logica.ngph.common.dtos.AcknowledgementDto)
	 */
	public String buildRtgsMessageforAck(AcknowledgementCanonical ackObj)throws Exception
	{
		String result = null;
		try
		{
			StringBuilder ackMes = new StringBuilder();
			ackMes.append(NgphEsbConstants.BLOCK_BEGINING);
			ackMes.append(NgphEsbConstants.NGPH_CONSTANT_A);
			ackMes.append(NgphEsbConstants.NGPH_COLON);
			ackMes.append(esbServiceDao.getAppId(ackObj.getSrcEiId()));
			if((ackObj.getDstChnlType().equalsIgnoreCase("NEFT") || ackObj.getDstChnlType().equalsIgnoreCase("SFMS")) && ackObj.getDstMsgType().startsWith("F"))
			{
				ackMes.append(ackObj.getDstMsgType());
				ackMes.append(ackObj.getMsgDirection());
				ackMes.append(ackObj.getSeqNo());
				ackMes.append(ackObj.getSenderBank());
				ackMes.append(timeStampFormatterWithTime(ackObj.getAckReceivedTmStmp()));
				if (ackObj.getAckReasonCode()!=null)
				{
					ackMes.append(StringUtils.rightPad(ackObj.getAckReasonCode(), 13, "X"));
				}
			}
			else
			{
				//construct block header 1 for Acknowledgment
				String bankApplicationHeader = constructBankApplicationHeaderForAck(ackObj);
				if(StringUtils.isNotEmpty(bankApplicationHeader))
				{
					//append to the Acknowledgment message
					ackMes.append(bankApplicationHeader);
				}
				else
				{
					return null;
				}
				//construct block 4 for Acknowledgment
				ackMes.append(NgphEsbConstants.BLOCK_BEGINING);
				ackMes.append(NgphEsbConstants.NGPH_STRING_FOUR);
				ackMes.append(NgphEsbConstants.NGPH_COLON);
				ackMes.append(NgphEsbConstants.NGPH_SFMS_CRLF);
				//appending the block fields and values
				String messageTextBlock = constructMessageTextBlockforAck(ackObj);
				if(StringUtils.isNotEmpty(messageTextBlock))
				{
					ackMes.append(messageTextBlock);
				}
				else
				{
					result= null;
				}
				//closing the block-4
				String lastChars = ackMes.toString().substring(ackMes.length()-2, ackMes.length());
				if(lastChars.equalsIgnoreCase("\r") || lastChars.equalsIgnoreCase("\n"))
				{
					ackMes.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
				}
				else
				{
					ackMes.append(NgphEsbConstants.NGPH_SFMS_CRLF);
					ackMes.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
				}	
			}
			ackMes.append(NgphEsbConstants.BLOCK_CLOSING);
			swiftParserDao.insertRawMessage("QNGSYS", ackObj.getMsgId(), ackMes.toString(), ackObj.getDstChnlType(),ackObj.getMsgDirection());
			ackMes.append("{999:" + ackObj.getDstEiId() + "," + ackObj.getMsgId() + "}");
			result= ackMes.toString();
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in building SFMS message for acknowledgment");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSFCSVC0001", null, SFMSChannelServiceImpl.class, ackObj.getMsgId());//Exception occured while creating SFMS acknowledgment message. Refer error log for details. 
			result= null;
			throw new Exception(e);
		}
		
		return result;
	}

	//For Ack
	private String constructMessageTextBlockforAck(AcknowledgementCanonical ackCanonicalData)throws Exception
	{
		logger.info("constructMessageTextBlockforAck(...)  Start....");
		StringBuilder sb = new StringBuilder();
		// Check for Null for channelType/SrcMsgType/SrcSubMsgType
		if(StringUtils.isNotBlank(ackCanonicalData.getDstChnlType()) && StringUtils.isNotEmpty(ackCanonicalData.getDstChnlType()) && StringUtils.isNotBlank(ackCanonicalData.getDstMsgType()) && StringUtils.isNotEmpty(ackCanonicalData.getDstMsgType()) && StringUtils.isNotBlank(ackCanonicalData.getDstSubMsgType()) && StringUtils.isNotEmpty(ackCanonicalData.getDstSubMsgType()))
		{
			String key = ackCanonicalData.getDstChnlType() + ackCanonicalData.getDstMsgType() + ackCanonicalData.getDstSubMsgType();
			logger.info("The key Value is : " + key);
			//Fetch the ArrayList Holding the Bean Objects where Canonical Attributes are mapped to corresponding field.
			ArrayList<FieldCanonicalAttribute> list = dataInitializer_CanonicalAttributes.get(key);
			// Fetching All Methods of NGPH Canonical class using Reflection API
			try 
			{
				@SuppressWarnings("rawtypes")
				Class c = Class.forName(ackCanonicalData.getClass().getName());
	           //Fetch all the Public methods of AckCanonical Class
				Method allMethods[] = c.getDeclaredMethods();
				ArrayList<Method> functionHolder = new ArrayList<Method>();
				//Fetch all getter Methods and store in a local ArrayList
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
            		FieldCanonicalAttribute object = list.get(i);
            		String canVal = "get" + object.getFieldCanonicalAtt();
            		String fldNo = object.getFieldNo();
            		String fldSeq = object.getFieldSeq();
            		String fldManOpt = object.getFieldCompMandOpt();
            		int fldCompSeq = Integer.parseInt(object.getFieldCompSeq());
            		String fldEocInd = object.getFieldEocInd();
            		//logger.info(canVal + "\t" + fldNo + "\t" + fldSeq + "\t" + fldManOpt +"\t" + fldCompSeq + "\t" + fldEocInd);           		
           			for (int j=0;j<functionHolder.size();j++) 
		            {
    					Method m = functionHolder.get(j);
    					String mname = m.getName();
	    				if(mname.equalsIgnoreCase(canVal))
	    				{
	    					try
							{
								Object obj = m.invoke(ackCanonicalData,new Object[]{});
								if(obj!=null) 
								{
									// Either the object is a TimeStamp/BigDecimal/String
									// check if the Object value is BigDecimal, then do formatting
									if(obj instanceof BigDecimal)
									{
										obj = bigDecimalFormatter(obj);	
									}
									// check if the Object value is TimeStamp, then do formatting and convert to normal String
									else if(obj instanceof Timestamp)
									{
										obj = timeStampFormatter(obj);	
									}
									else if(obj!=null && obj instanceof String[])
									{
										obj = constructObj(obj);
									}
									//append the AckCanonical Value
									sb.append(NgphEsbConstants.NGPH_COLON);
									sb.append(fldNo);
									sb.append(NgphEsbConstants.NGPH_COLON);
									sb.append(obj);
									sb.append(NgphEsbConstants.NGPH_SFMS_CRLF);
									//Once field is matched then break from inner loop, to avoid Performance issues
									break;
								}										
							}
	    					catch (Exception e) 
	    					{
	    						logger.error("Exception occurred in SFMS text block construction of acknowledgment during value fetching from payment data");
	    						logger.error(e, e);
	    						EventLogger.logEvent("NGPHSFCSVC0002", null, SFMSChannelServiceImpl.class,ackCanonicalData.getMsgId());//Exception occured in constructing SFMS acknowledgement message. Refer Error log for details.
	    						throw new Exception(e);
							}
	    				}
		            }
    			}
			}	
			catch (Exception e) 
			{
				logger.error("Exception occurred in SFMS text block construction of acknowledgment");
				logger.error(e, e);
				EventLogger.logEvent("NGPHSFCSVC0002", null, SFMSChannelServiceImpl.class,ackCanonicalData.getMsgId());//Exception occured in constructing SFMS acknowledgement message. Refer Error log for details.
				throw new Exception(e);
			}
	}//parent If
		logger.info("constructMessageTextBlockforAck(...)  End....");
		return sb.toString();
	}//Method Closed
	
	/**
	 * Builds the RTGS message from the Info Canonical object.
	 * This reason we took it from Canonical because we might have 
	 * to construct a RTGS message when the Input message is Swift or Vice-Versa.
	 */
	public String buildRtgsMessageForInfoCan(InfoCanonical canonicalData) throws Exception
	{
		String result = null;
		try
		{
			logger.info("buildRtgsMessageForInfoCan(...)  Start....");
			StringBuilder rtgsMessage = new StringBuilder();
			//Block-A construction
			String bankApplicationHeader = constructBankApplicationHeaderforInfo(canonicalData);
			logger.info("Block 1 constructed for Info Canonical is : " + bankApplicationHeader);
			if(StringUtils.isNotEmpty(bankApplicationHeader))
			{
				rtgsMessage.append(bankApplicationHeader);
			}
			else
			{
				return null;
			}
			//Block-B construction also called as 4-block.
			//appending the block-name
			rtgsMessage.append(NgphEsbConstants.BLOCK_BEGINING);
			rtgsMessage.append(NgphEsbConstants.NGPH_STRING_FOUR);
			rtgsMessage.append(NgphEsbConstants.NGPH_COLON);
			//appending the block fields and values
			String messageTextBlock = constructMessageTextBlockForInfoCan(canonicalData);
			logger.info("Block 4 constructed for Info Canonical is : " + messageTextBlock );
			
			if(messageTextBlock.startsWith("\r\n"))
			{
				//do nothing
			}
			else
			{
				rtgsMessage.append(NgphEsbConstants.NGPH_SFMS_CRLF);
			}
			if(StringUtils.isNotEmpty(messageTextBlock))
			{
				rtgsMessage.append(messageTextBlock);
				logger.info("buildRtgsMessage(): RTGS Message:"+canonicalData.getMsgRef()+" Constructed Successfully....");
			}
			else
			{
				logger.info("buildRtgsMessage(): Failed To Construct RTGS message for msg:"+canonicalData.getMsgRef());
				result= null;
			}
			//closing the block-B (OR) block-4
			String lastChars = rtgsMessage.toString().substring(rtgsMessage.length()-2, rtgsMessage.length());
			if(lastChars.equalsIgnoreCase("\r") || lastChars.equalsIgnoreCase("\n"))
			{
				rtgsMessage.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
				rtgsMessage.append(NgphEsbConstants.BLOCK_CLOSING);
			}
			else
			{
				rtgsMessage.append(NgphEsbConstants.NGPH_SFMS_CRLF);
				rtgsMessage.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
				rtgsMessage.append(NgphEsbConstants.BLOCK_CLOSING);
	
			}
			logger.info("buildRtgsMessageForInfoCan(...)  End....");
			swiftParserDao.insertRawMessage("QNGSYS", canonicalData.getMsgRef(), rtgsMessage.toString(), canonicalData.getDstChnl(),canonicalData.getDirection());
			//Appending the destination EI Id for Response handler
			rtgsMessage.append("{999:" + canonicalData.getEi_ID() + "," + canonicalData.getMsgRef() + "}");
			result= rtgsMessage.toString();
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in building buildRtgsMessageForInfoCan message.");
			logger.error(e,e);
			result= null;
			throw new Exception(e);
		}
		return result;
	}
	
	/**
	 * Builds the RTGS message from the canonical object.
	 * This reason we took it from Canonical because we might have 
	 * to construct a RTGS message when the Input message is Swift or Vice-Versa.
	 */
	public String buildRtgsMessage(NgphCanonical canonicalData) throws Exception
	{
		String result = null;
		try
		{
			
			logger.info("buildRtgsMessage(...)  Start....");
			StringBuilder rtgsMessage = new StringBuilder();
			//Block-A construction
			String bankApplicationHeader = constructBankApplicationHeader(canonicalData);
			if(StringUtils.isNotEmpty(bankApplicationHeader))
			{
				rtgsMessage.append(bankApplicationHeader);
			}
			else
			{
				return null;
			}
			//Block-B construction also called as 4-block.
			//appending the block-name
			rtgsMessage.append(NgphEsbConstants.BLOCK_BEGINING);
			rtgsMessage.append(NgphEsbConstants.NGPH_STRING_FOUR);
			rtgsMessage.append(NgphEsbConstants.NGPH_COLON);
			//appending the block fields and values
			String messageTextBlock = constructMessageTextBlock(canonicalData);
			
			if(messageTextBlock.startsWith("\r\n"))
			{
				//do nothing
			}
			else
			{
				rtgsMessage.append(NgphEsbConstants.NGPH_SFMS_CRLF);
			}
			if(StringUtils.isNotEmpty(messageTextBlock))
			{
				rtgsMessage.append(messageTextBlock);
				//AuditEventLogging
				EventLogger.logEvent("NGPHSFCSVC0003", canonicalData, SFMSChannelServiceImpl.class, canonicalData.getMsgRef());//Payment message text block constructed successfully.
				logger.info("buildRtgsMessage(): RTGS Message:"+canonicalData.getMsgRef()+" Constructed Successfully....");
			}
			else
			{
				//AuditEventLogging
				EventLogger.logEvent("NGPHSFCSVC0004", canonicalData, SFMSChannelServiceImpl.class, canonicalData.getMsgRef());//Payment message text block constuction failed.
				logger.info("buildRtgsMessage(): Failed To Construct RTGS message for msg:"+canonicalData.getMsgRef());
				result= null;
			}
			//closing the block-B (OR) block-4
			String lastChars = rtgsMessage.toString().substring(rtgsMessage.length()-2, rtgsMessage.length());
			if(lastChars.equalsIgnoreCase("\r") || lastChars.equalsIgnoreCase("\n"))
			{
				rtgsMessage.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
				rtgsMessage.append(NgphEsbConstants.BLOCK_CLOSING);
			}
			else
			{
				rtgsMessage.append(NgphEsbConstants.NGPH_SFMS_CRLF);
				rtgsMessage.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
				rtgsMessage.append(NgphEsbConstants.BLOCK_CLOSING);
	
			}
			logger.info("buildRtgsMessage(...)  End....");
			swiftParserDao.insertRawMessage("QNGSYS", canonicalData.getMsgRef(), rtgsMessage.toString(), canonicalData.getDstMsgChnlType(),canonicalData.getMsgDirection());
			//Appending the destination EI Id for Response handler
			rtgsMessage.append("{999:" + canonicalData.getDstEiId() + "," + canonicalData.getMsgRef() + "}");
			result= rtgsMessage.toString();
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in building SFMS message.");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSFCSVC0005", canonicalData, SFMSChannelServiceImpl.class, canonicalData.getMsgRef());//Exception occured in creating SFMS Payment message. Refer error log for details.
			result= null;
			throw new Exception(e);
		}
		return result;
	}

	/**
	 * This Method Constructs RTGS Message fourth Block for InfoCanonical
	 * @param InfoCanonical
	 * @return String
	 */
	private String constructMessageTextBlockForInfoCan(InfoCanonical canonicalData)throws Exception
	{
		logger.info("constructMessageTextBlockForInfoCan(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		// Check for Null for channelType/SrcMsgType/SrcSubMsgType
		if(StringUtils.isNotBlank(canonicalData.getDstChnl()) && StringUtils.isNotEmpty(canonicalData.getDstChnl()) && StringUtils.isNotBlank(canonicalData.getDstMsgType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgType()) && StringUtils.isNotBlank(canonicalData.getDstMsgSubType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()))
		{
			String key = canonicalData.getDstChnl() + canonicalData.getDstMsgType() + canonicalData.getDstMsgSubType();
			logger.info("The key Value is : " + key);
			// Declaring temp Variables
			String curFldNo="";
			String tempFldNo="";
			String prevField_Id = "";
			String tempMsgStr="";
			String prevTempMsgStr="";
			String prevEocInd="";
			String canValue="";
			String EolInd = "";
			int prevComSeq = 0;
			
			int maxCompSeq = 0;
			//Fetch the ArrayList Holding the Bean Objects where Canonical Attributes are mapped to corresponding field.
			ArrayList<FieldCanonicalAttribute> list = dataInitializer_CanonicalAttributes.get(key);

			// Fetching All Methods of NGPH Canonical class using Reflection API
			try 
			{
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
				// Storing first FldNo to Temp Field No
				tempFldNo = list.get(0).getFieldNo();
				//prevField_Id = list.get(0).getMsg_field_id();
				prevComSeq = Integer.parseInt(list.get(0).getFieldCompSeq());
        		//Iterating over the ArrayList Containing Pojo Objects.
				//Outer Loop
				OuterLoop:
				for(int i=0;i<list.size();i++)
    			{
            		FieldCanonicalAttribute tempobject=null;
            		if (i+1 < list.size())
            		{
            			tempobject = list.get(i+1);
            		}
            		FieldCanonicalAttribute object = list.get(i);
            		String canVal = object.getFieldCanonicalAtt();
            		String fldNo = object.getFieldNo();
            		String fldSeq = object.getFieldSeq();
            		String fldManOpt = object.getFieldCompMandOpt();
            		String field_Id = object.getMsg_field_id();
            		int fldCompSeq = Integer.parseInt(object.getFieldCompSeq());
            		String fldEocInd = object.getFieldEocInd();
            		String fldCnsdr = object.getFieldcnsdr();
            		String compFmt = object.getField_comp_fmt();
            		
            		
            		
            		//To identify max sequence no of the field no
    				if((!tempFldNo.equalsIgnoreCase(fldNo)) || i==0)
    				{
    					String currFNo = fldNo;
    					String preFNo=fldNo;
    					FieldCanonicalAttribute seqObject=null;
    					int m=i;
    					seqObject = list.get(m);
    					while(currFNo.equalsIgnoreCase(preFNo))
    					{
    						preFNo = currFNo;
    						maxCompSeq = Integer.parseInt(seqObject.getFieldCompSeq());
    						m++;
    						if(m<list.size()-1)
    						{
    							seqObject = list.get(m);
    							currFNo = seqObject.getFieldNo();
    						}
    						else
    						{
    							currFNo ="";
    						}
    					}
    				}

            		//logger.info(canVal + "\t" + fldNo + "\t" + fldSeq + "\t" + fldManOpt +"\t" + fldCompSeq + "\t" + fldEocInd);
            		//Repeating group to be implemented and till then just equate sum amount to message amount and transaction count to 1 
            		if(canVal.contains("CAN_GRP"))
            		{
            			if (canVal.equalsIgnoreCase("CAN_GRPGrpNoOfTxns"))
            			{
            				sb.append(NgphEsbConstants.NGPH_SFMS_CRLF + ":" + fldNo + ":1");
            			}
/*            			else if (canVal.equalsIgnoreCase("CAN_GRPGrpControlSum"))
            			{
            				sb.append(NgphEsbConstants.NGPH_SFMS_CRLF + ":" + fldNo + ":" + bigDecimalFormatter(canonicalData.getMsgAmount().toString()));
            			}
*/            			//FIXME - Arriving at the number of messages and its current part to be implemented - This also need to be done in SWIFT channel service
            			else if (canVal.equalsIgnoreCase("CAN_GRPGrpNoOfMsgsPart"))
            			{
            				sb.append(":" + fldNo + ":1");
            			}
            			else if (canVal.equalsIgnoreCase("CAN_GRPGrpNoOfMsgsTotal"))
            			{
            				sb.append("/1");
            			}
            		}
            		else
            		{
            			List<String> canVarList = new ArrayList<String>();
            			//check if Canonical Variable in DB contains SemiColon (;)
            			if(canVal.contains(";"))
            			{
            				String colon[] = canVal.split(";");
            				canVarList = Arrays.asList(colon);
            			}
            			else
            			{
            				canVarList.add(canVal);
            			}
            			if(fldEocInd==null)
    					{
    						EolInd = "";
    					}
    					else if(fldEocInd.equalsIgnoreCase("CRLF"))
    					{
    						EolInd = NgphEsbConstants.NGPH_SFMS_CRLF;
    					}
    					else if(fldEocInd.equalsIgnoreCase("SLSH"))
    					{
    						EolInd = NgphEsbConstants.NGPH_CHAR_EOL_SLSH;
    					}
    					else if(fldEocInd.equalsIgnoreCase("LENT"))
    					{
    						EolInd = "";
    					}
    					curFldNo = fldNo;
            			//Inner Loop
            			innerLoop:
            			for(int k=0;k<canVarList.size();k++)
            			{
            				canValue = null;
            				canVal = "get" + canVarList.get(k);
            				if(field_Id.equalsIgnoreCase(prevField_Id) && fldCompSeq != prevComSeq+1 && !tempMsgStr.isEmpty())
            				{
            					//break this loop and start will the parent loop
            					break innerLoop;
            				}
            				if(canVal.contains("CAN_SPL"))
    	    				{
    							if(canVal.equalsIgnoreCase("getCAN_SPLSTATIC"))
    	            			{
        							canValue = fldCnsdr.substring(fldCnsdr.indexOf("~")+1, fldCnsdr.lastIndexOf("~"));
    	            			}
    	    				}
	    					else
	    					{
	    						for (int j=0;j<functionHolder.size();j++) 
					            {
			    					Method m = functionHolder.get(j);
			    					String mname = m.getName();
			    					if(mname.equalsIgnoreCase(canVal))
				    				{
				    					try
										{
											Object obj = m.invoke(canonicalData,new Object[]{});
											if(obj!=null) 
											{
												// Either the object is a TimeStamp/BigDecimal/String
												// check if the Object value is BigDecimal, then do formatting
												if(obj instanceof BigDecimal)
												{
													obj = bigDecimalFormatter(obj);	
												}
												// check if the Object value is TimeStamp, then do formatting and convert to normal String
												else if(obj instanceof Timestamp)
												{
													obj = timeStampFormatter(obj);
													
													/*if (canVal.equalsIgnoreCase("getMsgBatchTime"))
													{
														String tempStr = canonicalData.getMsgBatchTime().toString();
														obj = tempStr.substring(11, 13) + tempStr.substring(14, 16);
													}
													else
													{
														obj = timeStampFormatter(obj);
													}*/
												}
												else if(obj instanceof String[])
												{
													obj = constructObj(obj);
												}
												canValue = obj.toString();
												break;
											}
										}
				    					catch (Exception e) 
										{
				    						logger.error("Exception occured while fetching canonical value for field " + curFldNo + " / " + tempFldNo);
											logger.error(e, e);
											sb.delete(0, sb.length());
										}
				    				}
					            }
	    					}
	    					try
	    					{
								if (!StringUtils.isEmpty(canValue))
								{
			                		if(StringUtils.isEmpty(tempMsgStr))
			                		{
			                			if(prevTempMsgStr.contains(":" + curFldNo +":"))
			                			{
			                				if(compFmt.contains("\\") && fldCompSeq ==1) 
			                				{
			                					canValue = "\\" + canValue; 
			                				}
				                			tempMsgStr = tempMsgStr + prevEocInd + canValue;
			                			}
			                			else
			                			{
			                				if(i==0)
			                				{
				                				if(compFmt.contains("\\") && fldCompSeq ==1) 
				                				{
				                					canValue = "\\" + canValue; 
				                				}
			                					tempMsgStr = tempMsgStr + ":" + curFldNo + ":" + canValue;
			                				}
			                				else
			                				{
				                				if(compFmt.contains("\\") && fldCompSeq ==1) 
				                				{
				                					canValue = "\\" + canValue; 
				                				}
			                					tempMsgStr = NgphEsbConstants.NGPH_SFMS_CRLF + tempMsgStr + ":" + curFldNo + ":" + canValue;
			                				}
			                			}
			                		}
			                		else
			                		{
			                			if(compFmt.contains("\\") && fldCompSeq ==1) 
			                			{
			                				canValue = "\\" + canValue; 
			                			}
			                			tempMsgStr = tempMsgStr + prevEocInd + canValue;
			                		}
								}
								else
								{
									prevTempMsgStr = tempMsgStr;
	                				//tempMsgStr="";
								}
		                		if(fldManOpt.equalsIgnoreCase("M"))
		                		{
		                			if(StringUtils.isEmpty(canValue) && canVarList.size() == 1 && k==0)
	                				{
	                					prevTempMsgStr = tempMsgStr;
		                				tempMsgStr="";
		                				//if a mandatory component is missing then move to next field itself. 
		                				do
		                				{
		                					tempFldNo = curFldNo;
		                    				prevEocInd = EolInd;
		                    				prevComSeq = fldCompSeq;
		                    				prevField_Id = field_Id;
		                					i++;
		                					if (i<list.size())
		                					{
			                					object = list.get(i);
			                					canVal = object.getFieldCanonicalAtt();
			                            		fldNo = object.getFieldNo();
			                            		fldSeq = object.getFieldSeq();
			                            		fldManOpt = object.getFieldCompMandOpt();
			                            		field_Id = object.getMsg_field_id();
			                            		fldCompSeq = Integer.parseInt(object.getFieldCompSeq());
			                            		fldEocInd = object.getFieldEocInd();
			                            		fldCnsdr = object.getFieldcnsdr();
		                					}
		                					else
		                					{
		                						i--;
		                						break;
		                					}
		                				}while (prevField_Id == field_Id);
		                				if (!field_Id.equalsIgnoreCase(prevField_Id))
		                				{
		                					i--;
		                				}
		                				break innerLoop;
	                				}
		                			else
	                				{
		                				if(maxCompSeq == fldCompSeq)
		                				{
		                					sb.append(tempMsgStr);
		                					prevTempMsgStr = tempMsgStr;
		                					tempMsgStr="";
		                				}
	                				}
		                		}
		                		else
		                		{
		                			if(tempFldNo.equalsIgnoreCase(curFldNo) && !(sb.toString().contains(":" + curFldNo +":")))
		                			{
		                				if(maxCompSeq == fldCompSeq)
		                				{
		                					sb.append(tempMsgStr);
		                				}
		                				prevTempMsgStr = tempMsgStr;
	                					tempMsgStr="";
		                			}
		                			else if(tempobject!=null && !(tempobject.getFieldNo().equalsIgnoreCase(curFldNo)))
		                			{
                						sb.append(tempMsgStr);
                						prevTempMsgStr = tempMsgStr;
		                				tempMsgStr="";
		                			}
		                		}
                				tempFldNo = curFldNo;
                				prevEocInd = EolInd;
                				prevComSeq = fldCompSeq;
                				prevField_Id = field_Id;
		    				}
	    					catch (Exception e) 
							{
	    						logger.error("Exception occured while fetching canonical value for field " + curFldNo + " / " + tempFldNo);
								logger.error(e, e);
								sb.delete(0, sb.length());
								sb=null;
								throw new Exception(e);
							}
            			}
            		}
    			}
			}
			catch (Exception e) 
			{
				logger.error("Exception occured while processing field " + curFldNo + " / " + tempFldNo);
				logger.error(e, e);
				sb.delete(0, sb.length());
				sb=null;
				throw new Exception(e);
			}
		}
		logger.info("constructMessageTextBlockForInfoCan(...)  End....");
		return sb.toString();
	}
	
	/**
	 * This Method Constructs RTGS Message fourth Block
	 * @param canonicalData
	 * @return String
	 */
	private String constructMessageTextBlock(NgphCanonical canonicalData)throws Exception
	{
		logger.info("constructMessageTextBlock(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		// Check for Null for channelType/SrcMsgType/SrcSubMsgType
		if(StringUtils.isNotBlank(canonicalData.getDstMsgChnlType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgChnlType()) && StringUtils.isNotBlank(canonicalData.getDstMsgType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgType()) && StringUtils.isNotBlank(canonicalData.getDstMsgSubType()) && StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()))
		{
			String key = canonicalData.getDstMsgChnlType() + canonicalData.getDstMsgType() + canonicalData.getDstMsgSubType();
			logger.info("The key Value is : " + key);
			// Declaring temp Variables
			String curFldNo="";
			String tempFldNo="";
			String prevField_Id = "";
			String tempMsgStr="";
			String prevTempMsgStr="";
			String prevEocInd="";
			String canValue="";
			String EolInd = "";
			int prevComSeq = 0;
			String raw_Message ="";
			List<Raw_Msgs> rawMsgsData = null;
			
	
			int maxCompSeq = 0;
			//Fetch the ArrayList Holding the Bean Objects where Canonical Attributes are mapped to corresponding field.
			ArrayList<FieldCanonicalAttribute> list = dataInitializer_CanonicalAttributes.get(key);
			
			
			rawMsgsData = swiftParserDao.getRaw_msgs(canonicalData.getMsgRef());
			if(rawMsgsData!=null && !rawMsgsData.isEmpty())
			{
				for(int j=0;j<rawMsgsData.size();j++)
				{
					Raw_Msgs obj = rawMsgsData.get(0);
					Clob objrawMsgs = obj.getRawMsgs();
					 raw_Message = objrawMsgs.getSubString(1, (int) objrawMsgs.length());
				}
			}
			
			
			// Fetching All Methods of NGPH Canonical class using Reflection API
			try 
			{
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
				// Storing first FldNo to Temp Field No
				tempFldNo = list.get(0).getFieldNo();
				//prevField_Id = list.get(0).getMsg_field_id();
				prevComSeq = Integer.parseInt(list.get(0).getFieldCompSeq());
        		//Iterating over the ArrayList Containing Pojo Objects.
				//Outer Loop for every record
				OuterLoop:
				for(int i=0;i<list.size();i++)
    			{					
            		FieldCanonicalAttribute tempobject=null;
            		if (i+1 < list.size())
            		{
            			tempobject = list.get(i+1);
            		}
            		FieldCanonicalAttribute object = list.get(i);
            		String canVal = object.getFieldCanonicalAtt();
            		String fldNo = object.getFieldNo();
            		String fldSeq = object.getFieldSeq();
            		String fldManOpt = object.getFieldCompMandOpt();
            		String field_Id = object.getMsg_field_id();
            		int fldCompSeq = Integer.parseInt(object.getFieldCompSeq());
            		String fldEocInd = object.getFieldEocInd();
            		String fldCnsdr = object.getFieldcnsdr();
            		String compFmt = object.getField_comp_fmt();
            		
            		logger.info("FieldCanonicalAttribute List size is "+list.size());
            		
            		FieldCanonicalAttribute tempObj = list.get(4);
            		
            		logger.info("tempObj FieldNo"+tempObj.getFieldNo() +"tempObj Field "+tempObj.getFieldCompSeq());
            		
            		if(i==4)
            		{
            			logger.info("i=4");
            		}
            		if(i==5)
            		{
            			logger.info("i=5");
            		}
            		if(tempObj.getFieldCompSeq().equalsIgnoreCase("1"))
            		{
            			logger.info("32B getFieldCompSeq =1 ");
            		}
            			
  		
               		//To identify max sequence no of the field no
    				if((!tempFldNo.equalsIgnoreCase(fldNo)) || i==0)
    				{
    					String currFNo = fldNo;
    					String preFNo=fldNo;
    					FieldCanonicalAttribute seqObject=null;
    					int m=i;
    					seqObject = list.get(m);
    					while(currFNo.equalsIgnoreCase(preFNo))
    					{
    						preFNo = currFNo;
    						maxCompSeq = Integer.parseInt(seqObject.getFieldCompSeq());
    						m++;
    						if(m<list.size()-1)
    						{
    							seqObject = list.get(m);
    							currFNo = seqObject.getFieldNo();
    						}
    						else
    						{
    							currFNo ="";
    						}
    					}
    				}
            		//logger.info(canVal + "\t" + fldNo + "\t" + fldSeq + "\t" + fldManOpt +"\t" + fldCompSeq + "\t" + fldEocInd);
            		//Repeating group to be implemented and till then just equate sum amount to message amount and transaction count to 1 
            		if(canVal.contains("CAN_GRP"))
            		{
            			if (canVal.equalsIgnoreCase("CAN_GRPGrpNoOfTxns"))
            			{
            				sb.append(NgphEsbConstants.NGPH_SFMS_CRLF + ":" + fldNo + ":1");
            			}
            			else if (canVal.equalsIgnoreCase("CAN_GRPGrpControlSum"))
            			{
            				sb.append(NgphEsbConstants.NGPH_SFMS_CRLF + ":" + fldNo + ":" + bigDecimalFormatter(canonicalData.getMsgAmount().toString()));
            			}
            			else if (canVal.equalsIgnoreCase("CAN_GRPGrpNoOfMsgsPart"))
            			{
            				logger.info("Msg Host is "+canonicalData.getMsgHost());
            				// It is added for guarantee messages and to be checked for other messages types
            				if (!(canonicalData.getMsgHost().equalsIgnoreCase("9999")) && canonicalData.getSrcMsgType().equalsIgnoreCase("760") || canonicalData.getSrcMsgType().equalsIgnoreCase("767"))            					
            				{
            					//sb.append(":" + fldNo + ":" + canonicalData.getSequenceNo().toString());        
            					sb.append(":" + fldNo + ":1");
            				}
            				else
            				{
            					sb.append(":" + fldNo + ":1");
            				}
            			}
            			else if (canVal.equalsIgnoreCase("CAN_GRPGrpNoOfMsgsTotal"))
            			{
            				// It is added for guarantee messages and to be checked for other messages types
            				if (!(canonicalData.getMsgHost().equalsIgnoreCase("9999")) && canonicalData.getSrcMsgType().equalsIgnoreCase("760") || canonicalData.getSrcMsgType().equalsIgnoreCase("767"))            					
            				{            					
            					//sb.append("/" + canonicalData.getNoofMessages().toString());
            					sb.append("/1");
            				}
            				else
            				{
            					sb.append("/1");
            				}
            			}
            		}
            		else
            		{
            			List<String> canVarList = new ArrayList<String>();
            			//check if Canonical Variable in DB contains SemiColon (;)
            			if(canVal.contains(";"))
            			{
            				String colon[] = canVal.split(";");
            				canVarList = Arrays.asList(colon);
            			}
            			else
            			{
            				canVarList.add(canVal);
            			}
            			if(fldEocInd==null)
    					{
    						EolInd = "";
    					}
    					else if(fldEocInd.equalsIgnoreCase("CRLF"))
    					{
    						EolInd = NgphEsbConstants.NGPH_SFMS_CRLF;
    					}
    					else if(fldEocInd.equalsIgnoreCase("SLSH"))
    					{
    						EolInd = NgphEsbConstants.NGPH_CHAR_EOL_SLSH;
    					}
    					else if(fldEocInd.equalsIgnoreCase("LENT"))
    					{
    						EolInd = "";
    					}
    					curFldNo = fldNo;
            			//Inner Loop
            			innerLoop:
            			for(int k=0;k<canVarList.size();k++)
            			{
            				canValue = null;
            				canVal = "get" + canVarList.get(k);
            				if(field_Id.equalsIgnoreCase(prevField_Id) && fldCompSeq != prevComSeq+1 && !tempMsgStr.isEmpty())
            				{
            					//break this loop and start will the parent loop
            					break innerLoop;
            				}          			
            				
            				if(canVal.contains("CAN_SPL"))
    	    				{
    							if(canVal.equalsIgnoreCase("getCAN_SPLSTATIC"))
    	            			{
        							canValue = fldCnsdr.substring(fldCnsdr.indexOf("~")+1, fldCnsdr.lastIndexOf("~"));
    	            			}
    	    				}
	    					else
	    					{
	    						
	    						for (int j=0;j<functionHolder.size();j++) 
					            {
			    					Method m = functionHolder.get(j);
			    					String mname = m.getName();
			    					if(mname.equalsIgnoreCase(canVal))
				    				{
				    					try
										{
											Object obj = m.invoke(canonicalData,new Object[]{});
											if(obj!=null) 
											{
												// Either the object is a TimeStamp/BigDecimal/String
												// check if the Object value is BigDecimal, then do formatting
												if(obj instanceof BigDecimal)
												{
													obj = bigDecimalFormatter(obj);	
												}
												// check if the Object value is TimeStamp, then do formatting and convert to normal String
												else if(obj instanceof Timestamp)
												{
													if (canVal.equalsIgnoreCase("getMsgBatchTime"))
													{
														String tempStr = canonicalData.getMsgBatchTime().toString();
														obj = tempStr.substring(11, 13) + tempStr.substring(14, 16);
													}
													else
													{
														obj = timeStampFormatter(obj);
													}
												}
												else if(obj instanceof String[])
												{
													obj = constructObj(obj);
												}
												canValue = obj.toString();
												break;
											}
											
										}
				    					catch (Exception e) 
										{
				    						logger.error("Exception occured while fetching canonical value for field " + curFldNo + " / " + tempFldNo);
											logger.error(e, e);
											sb.delete(0, sb.length());
										}
				    				}
					            }
	    					}
	    					try
	    					{
								if (!StringUtils.isEmpty(canValue))
								{
			                		if(StringUtils.isEmpty(tempMsgStr))
			                		{
			                			if(prevTempMsgStr.contains(":" + curFldNo +":"))
			                			{
			                				if(compFmt.contains("\\") && fldCompSeq ==1) 
			                				{
			                					canValue = "\\" + canValue; 
			                				}
			                			
			                					tempMsgStr = prevTempMsgStr + prevEocInd + canValue;
			                			}
			                			else
			                			{
			                				if(i==0)
			                				{
				                				if(compFmt.contains("\\") && fldCompSeq ==1) 
				                				{
				                					canValue = "\\" + canValue; 
				                				}				                				
				                				tempMsgStr = tempMsgStr + ":" + curFldNo + ":" + canValue;				                							                				
			                				}
			                				else
			                				{
				                				if(compFmt.contains("\\") && fldCompSeq ==1) 
				                				{
				                					canValue = "\\" + canValue; 
				                				}
				                				if(canVal.equalsIgnoreCase("getCAN_SPLSTATIC")) //Added a condition to check the Field is CAN_SPLSTATIC Field or not
				                				{
				                					tempMsgStr = NgphEsbConstants.NGPH_SFMS_CRLF + tempMsgStr + ":" + curFldNo + ":" + canValue;
				                				}
				                				else
				                				{
				                					if(!canonicalData.getMsgHost().equalsIgnoreCase("9999"))
				                					{
						                				if(canonicalData.getSrcMsgType().equalsIgnoreCase("754") || canonicalData.getSrcMsgType().equalsIgnoreCase("752") || canonicalData.getSrcMsgType().equalsIgnoreCase("750") || canonicalData.getSrcMsgType().equalsIgnoreCase("769") || canonicalData.getSrcMsgType().equalsIgnoreCase("730")) // added a condition to check whether QNG is sending only the fields that are received. and added for message 754 32 field and 34 field 
						                				{	
						                					if(raw_Message.contains(curFldNo))  // added a condition to check whether QNG is sending only the fields that are received. and added for message 754 32 field and 34 field 
						                					{
						                						tempMsgStr = NgphEsbConstants.NGPH_SFMS_CRLF + tempMsgStr + ":" + curFldNo + ":" + canValue;
						                					}
						                				}
						                				else 
						                				{
						                					tempMsgStr = NgphEsbConstants.NGPH_SFMS_CRLF + tempMsgStr + ":" + curFldNo + ":" + canValue;
						                				}
				                					}
					                				else 
					                				{	
					                					tempMsgStr = NgphEsbConstants.NGPH_SFMS_CRLF + tempMsgStr + ":" + curFldNo + ":" + canValue;
					                				}
				                				}
			                				}
			                			}
			                		}
			                		else
			                		{
			                			if(compFmt.contains("\\") && fldCompSeq ==1) 
			                			{
			                				canValue = "\\" + canValue; 
			                			}
			                			
			                				tempMsgStr = tempMsgStr + prevEocInd + canValue;
			                		}
								}
								else
								{
									prevTempMsgStr = tempMsgStr;
	                				//tempMsgStr="";
								}
		                		if(fldManOpt.equalsIgnoreCase("M"))
		                		{
		                			if(StringUtils.isEmpty(canValue) && canVarList.size() == 1 && k==0)
	                				{
	                					prevTempMsgStr = tempMsgStr;
		                				tempMsgStr="";
		                				//if a mandatory component is missing then move to next field itself. 
		                				do
		                				{
		                					tempFldNo = curFldNo;
		                    				prevEocInd = EolInd;
		                    				prevComSeq = fldCompSeq;
		                    				prevField_Id = field_Id;
		                					i++;
		                					if (i<list.size())
		                					{
			                					object = list.get(i);
			                					canVal = object.getFieldCanonicalAtt();
			                            		fldNo = object.getFieldNo();
			                            		fldSeq = object.getFieldSeq();
			                            		fldManOpt = object.getFieldCompMandOpt();
			                            		field_Id = object.getMsg_field_id();
			                            		fldCompSeq = Integer.parseInt(object.getFieldCompSeq());
			                            		fldEocInd = object.getFieldEocInd();
			                            		fldCnsdr = object.getFieldcnsdr();
		                					}
		                					else
		                					{
		                						i--;
		                						break;
		                					}
		                				}while (prevField_Id == field_Id);
		                				if (!field_Id.equalsIgnoreCase(prevField_Id))
		                				{
		                					i--;
		                				}
		                				break innerLoop;
	                				}
		                			else
	                				{
		                				if(maxCompSeq == fldCompSeq)
		                				{
		                					sb.append(tempMsgStr);	
		                					logger.info("tempMsgStr mandatory:: "+tempMsgStr);
			                				prevTempMsgStr = tempMsgStr;		                					
		                					tempMsgStr="";
		                				}
	                				}
		                		}
		                		else
		                		{
		                			if(tempFldNo.equalsIgnoreCase(curFldNo) && !(sb.toString().contains(":" + curFldNo +":")))
		                			{
		                				if(maxCompSeq == fldCompSeq)
		                				{
		                					sb.append(tempMsgStr);
		                					logger.info("tempMsgStr in temp and curr fid are same:: "+tempMsgStr);
		                				}		                							                					
		                				prevTempMsgStr = tempMsgStr;		                				
	                					tempMsgStr="";
		                			}
		                			else if(tempobject!=null && !(tempobject.getFieldNo().equalsIgnoreCase(curFldNo)))
		                			{
                						sb.append(tempMsgStr);  
                						logger.info("tempMsgStr in temp and curr fid are not same:: "+tempMsgStr);
		                				prevTempMsgStr = tempMsgStr;               						
		                				tempMsgStr="";
		                			}
		                		}
                				tempFldNo = curFldNo;
                				prevEocInd = EolInd;
                				prevComSeq = fldCompSeq;
                				prevField_Id = field_Id;
		    				}
	    					catch (Exception e) 
							{
	    						logger.error("Exception occured while fetching canonical value for field " + curFldNo + " / " + tempFldNo);
								logger.error(e, e);
								sb.delete(0, sb.length());
								sb=null;
								throw new Exception(e);
							}
            			}
            		}
    			}
			}
			catch (Exception e) 
			{
				logger.error("Exception occured while processing field " + curFldNo + " / " + tempFldNo);
				logger.error(e, e);
				sb.delete(0, sb.length());
				sb=null;
				throw new Exception(e);
			}
		}
		logger.info("constructMessageTextBlock(...)  End....");
		return sb.toString();
	}
	private String bigDecimalFormatter(Object o)throws Exception
	{
		String ret = null;
		if (o.toString().contains("."))
		{
			ret = o.toString().replace(".", ",");
		}
		else
		{
			ret = o.toString() + ",00";
		}
		return ret;
	}
	private String timeStampFormatter(Object o)throws Exception
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
			//SFMS date only should be returned
			return colon.substring(0, 8);    
		}
	}
	private String timeStampFormatterWithTime(Object o)throws Exception
	{
		String dataVal = o.toString().substring(0, 16);
		String hiphen  = dataVal.replace("-", "");
		String space = hiphen.replace(" ", "");
		String colon = space.replace(":", "");
		return colon;
	}
	
	/**
	 * 
	 * @param Info Canonical
	 * @return String
	 */
	private String constructBankApplicationHeaderforInfo(InfoCanonical canonicalData)throws Exception 
	{
		logger.info("constructBankApplicationHeaderforInfo(...)  Start....");
		StringBuilder sb = new StringBuilder();	
		//adding the block name
		sb.append(NgphEsbConstants.BLOCK_BEGINING);
		sb.append(NgphEsbConstants.NGPH_CONSTANT_A);
		sb.append(NgphEsbConstants.NGPH_COLON);
		//ADDING THE BLOCK VALUE
		//appending BankApplicationIdentifier
		String bankApplicationIdentifier = null;
		if(StringUtils.isNotEmpty(canonicalData.getEi_ID()))
		{
			//bankApplicationIdentifier = canonicalData.getMsgHost().substring(NgphEsbConstants.NGPH_INT_ZERO, NgphEsbConstants.NGPH_INT_THREE);
			bankApplicationIdentifier = esbServiceDao.getAppId(canonicalData.getEi_ID());
		}
		if(bankApplicationIdentifier != null)
		{
			sb.append(bankApplicationIdentifier);
		}
		else
		{
			return null;
		}
		//appending message identifier
		sb.append(NgphEsbConstants.NGPH_RTGS_MSG_IDENTIFIER);
		//appending i/o identifier
		if(canonicalData.getDirection().equalsIgnoreCase("o"))
		{
			sb.append(NgphEsbConstants.OUTWARD_PAYMENT); 
		}
		else
		{
			sb.append(NgphEsbConstants.INWARD_PAYMENT); 
		}
		
		//appending message type
		if(StringUtils.isNotEmpty(canonicalData.getDstMsgType()))
		{
			sb.append(canonicalData.getDstMsgType().trim()); 
		}
		else
		{
			logger.error("Destination message type is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//appending  message sub type
		if(StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()))
		{
			sb.append(canonicalData.getDstMsgSubType().trim()); 
		}
		else
		{
			logger.error("Destination SUB message type is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//appending sender address
		if(StringUtils.isNotEmpty(canonicalData.getInstdagt_bkcd()))
		{
			sb.append(canonicalData.getInstdagt_bkcd().trim()); 
		}
		else
		{
			logger.error("Sender bank is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//receiver address
		if(StringUtils.isNotEmpty(canonicalData.getInstgagt_bkcd()))
		{
			sb.append(canonicalData.getInstgagt_bkcd().trim()); 
		}
		else
		{
			logger.error("Receiver bank is empty while constructing bank app header for SFMS payment");
			return null;
		}
		//delivery monitoring flag 1-yes, 2-no
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//open notification flag 1-yes, 2-no
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//Non-delivery warning flag
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//Obsolescence Period
		sb.append(NgphEsbConstants.NGPH_TRIPLE_ZERO);
		
		//MUR(message user reference)
		if(StringUtils.isNotBlank(canonicalData.getMsgMur()))
		{
			sb.append(canonicalData.getMsgMur());
		}
		else
		{
			logger.error("Message Mur is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//Possible duplicate flag 1-yes, 2-no.
		sb.append(NgphEsbConstants.NGPH_INT_TWO);
		
		//Service Identifier
		sb.append(bankApplicationIdentifier.substring(0, NgphEsbConstants.NGPH_INT_THREE));
		
		//originating date and time of our System
		SimpleDateFormat formatter = new SimpleDateFormat(EnumDateFormat.DATETIME_FORAMT.getFormat());
		java.util.Date today = new java.util.Date();
		sb.append(formatter.format(new java.sql.Timestamp(today.getTime())));
	
		try
		{
			if(esbServiceDao.getInitialisedValBranch("SFMSTESTFLAG",canonicalData.getBranch()).equalsIgnoreCase("Y"))
			{
				//testing and training flag 1-yes, 2-no.
				sb.append(NgphEsbConstants.NGPH_INT_TWO);
			}
			else
			{
				//testing and training flag 1-yes, 2-no.
				sb.append(NgphEsbConstants.NGPH_INT_ONE);
			}
		}
		catch (Exception e) {
			logger.error(e, e);
			throw new Exception(e);
		}
		
		//Sequence number
		String seq = "000000000" + canonicalData.getSeqNo(); 
		sb.append(StringUtils.right(seq, 9));
		
		//filler
		sb.append(NgphEsbConstants.NGPH_RTGS_CONSTANT_FILLER);
		
		//Unique transaction reference i.e SndrTxnId
		if(StringUtils.isNotEmpty(canonicalData.getPmtId_instrId()))
		{
			if (canonicalData.getPmtId_instrId().trim().length() < 16)
			{
				sb.append(StringUtils.rightPad(canonicalData.getPmtId_instrId().trim(),16,"X"));
			}
			else if (canonicalData.getPmtId_instrId().trim().length() == 16)
			{
				sb.append(canonicalData.getPmtId_instrId().trim());
			}
			else
			{
				sb.append(canonicalData.getPmtId_instrId().trim().substring(0,16));
			}
		}
		else
		{
			logger.error("Sender transaction id or transaction reference is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//RTGS priority
		if(StringUtils.isNotEmpty(canonicalData.getSndrPymtPriority()))
		{
			sb.append(canonicalData.getSndrPymtPriority().trim()); 
		}
		else
		{
			sb.append("99");
		}
		
		
		//closing the block-A
		sb.append(NgphEsbConstants.BLOCK_CLOSING);
		logger .info("constructBankApplicationHeaderforInfo(...)  End....");
		return sb.toString();
	}
	
	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	private String constructBankApplicationHeader(NgphCanonical canonicalData)throws Exception 
	{
		logger.info("constructBankApplicationHeader(...)  Start....");
		StringBuilder sb = new StringBuilder();	
		//adding the block name
		sb.append(NgphEsbConstants.BLOCK_BEGINING);
		sb.append(NgphEsbConstants.NGPH_CONSTANT_A);
		sb.append(NgphEsbConstants.NGPH_COLON);
		//ADDING THE BLOCK VALUE
		//appending BankApplicationIdentifier
		String bankApplicationIdentifier = null;
		if(StringUtils.isNotEmpty(canonicalData.getDstEiId()))
		{
			//bankApplicationIdentifier = canonicalData.getMsgHost().substring(NgphEsbConstants.NGPH_INT_ZERO, NgphEsbConstants.NGPH_INT_THREE);
			bankApplicationIdentifier = esbServiceDao.getAppId(canonicalData.getDstEiId());
		}
		if(bankApplicationIdentifier != null)
		{
			sb.append(bankApplicationIdentifier);
		}
		else
		{
			return null;
		}
		//appending message identifier
		sb.append(NgphEsbConstants.NGPH_RTGS_MSG_IDENTIFIER);
		//appending i/o identifier
		if(canonicalData.getMsgDirection().equalsIgnoreCase("o"))
		{
			sb.append(NgphEsbConstants.OUTWARD_PAYMENT); 
		}
		else
		{
			sb.append(NgphEsbConstants.INWARD_PAYMENT); 
		}
		
		//appending message type
		if(StringUtils.isNotEmpty(canonicalData.getDstMsgType()))
		{
			sb.append(canonicalData.getDstMsgType().trim()); 
		}
		else
		{
			logger.error("Destination message type is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//appending  message sub type
		if(StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()))
		{
			sb.append(canonicalData.getDstMsgSubType().trim()); 
		}
		else
		{
			logger.error("Destination SUB message type is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//appending sender address
		if(StringUtils.isNotEmpty(canonicalData.getSenderBank()))
		{
			sb.append(canonicalData.getSenderBank().trim()); 
		}
		else
		{
			logger.error("Sender bank is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//receiver address
		logger.info("ReceiverBank() is :: "+canonicalData.getReceiverBank());
		if(StringUtils.isNotEmpty(canonicalData.getReceiverBank()))
		{
			sb.append(canonicalData.getReceiverBank().trim()); 
		}
		else
		{
			logger.error("Receiver bank is empty while constructing bank app header for SFMS payment");
			return null;
		}
		//delivery monitoring flag 1-yes, 2-no
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//open notification flag 1-yes, 2-no
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//Non-delivery warning flag
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//Obsolescence Period
		sb.append(NgphEsbConstants.NGPH_TRIPLE_ZERO);
		//MUR(message user reference)
		if(StringUtils.isNotEmpty(canonicalData.getCustTxnReference()))
		{
			//logger.info("Cust Txn Ref Val : " + canonicalData.getCustTxnReference());
			if (canonicalData.getCustTxnReference().trim().length() < 16)
			{
				sb.append(StringUtils.rightPad(canonicalData.getCustTxnReference().trim(),16,"X"));
			}
			else if (canonicalData.getCustTxnReference().trim().length() == 16)
			{
				sb.append(canonicalData.getCustTxnReference().trim());
			}
			else
			{
				sb.append(canonicalData.getCustTxnReference().trim().substring(0,16));
			}
		}
		else if(StringUtils.isNotEmpty(canonicalData.getTxnReference()))
		{

			//logger.info("Txn Ref Val : " + canonicalData.getTxnReference());
			if (canonicalData.getTxnReference().trim().length() < 16)
			{
				sb.append(StringUtils.rightPad(canonicalData.getTxnReference().trim(),16,"X"));
			}
			else if (canonicalData.getTxnReference().trim().length() == 16)
			{
				sb.append(canonicalData.getTxnReference().trim());
			}
			else
			{
				sb.append(canonicalData.getTxnReference().trim().substring(0,16));
			}
		}
		else
		{
			logger.error("transaction reference or transaction reference is empty while constructing bank app header for SFMS payment");
			return null;
		}
		//Possible duplicate flag 1-yes, 2-no.
		sb.append(NgphEsbConstants.NGPH_INT_TWO);
		//Service Identifier
		//sb.append(NgphEsbConstants.TRIPLE_X);
		sb.append(bankApplicationIdentifier.substring(0, NgphEsbConstants.NGPH_INT_THREE));
		//originating date and time of our System
		SimpleDateFormat formatter = new SimpleDateFormat(EnumDateFormat.DATETIME_FORAMT.getFormat());
		java.util.Date today = new java.util.Date();
		//logger.info(formatter.format(new java.sql.Timestamp(today.getTime())));
		sb.append(formatter.format(new java.sql.Timestamp(today.getTime())));
		
		try
		{
			if(esbServiceDao.getInitialisedValBranch("SFMSTESTFLAG",canonicalData.getMsgBranch()).equalsIgnoreCase("Y"))
			{
				//testing and training flag 1-yes, 2-no.
				sb.append(NgphEsbConstants.NGPH_INT_TWO);
			}
			else
			{
				//testing and training flag 1-yes, 2-no.
				sb.append(NgphEsbConstants.NGPH_INT_ONE);
			}
		}
		catch (Exception e) {
			logger.error(e, e);
			throw new Exception(e);
		}
		//Sequence number
		
	
			String seq = "000000000" + canonicalData.getSeqNo(); 
			sb.append(StringUtils.right(seq, 9));

		
		//filler
		sb.append(NgphEsbConstants.NGPH_RTGS_CONSTANT_FILLER);
		//Unique transaction reference i.e SndrTxnId
		if(StringUtils.isNotEmpty(canonicalData.getSndrTxnId()))
		{
			if (canonicalData.getSndrTxnId().trim().length() < 16)
			{
				sb.append(StringUtils.rightPad(canonicalData.getSndrTxnId().trim(),16,"X"));
			}
			else if (canonicalData.getSndrTxnId().trim().length() == 16)
			{
				sb.append(canonicalData.getSndrTxnId().trim());
			}
			else
			{
				sb.append(canonicalData.getSndrTxnId().trim().substring(0,16));
			}
		}
		else if(StringUtils.isNotEmpty(canonicalData.getTxnReference()))
		{
			if (canonicalData.getTxnReference().trim().length() < 16)
			{
				sb.append(StringUtils.rightPad(canonicalData.getTxnReference().trim(),16,"X"));
			}
			else if (canonicalData.getTxnReference().trim().length() == 16)
			{
				sb.append(canonicalData.getTxnReference().trim());
			}
			else
			{
				sb.append(canonicalData.getTxnReference().trim().substring(0,16));
			}
		}
		else
		{
			logger.error("Sender transaction id or transaction reference is empty while constructing bank app header for SFMS payment");
			return null;
		}
		
		//RTGS priority
		if(StringUtils.isNotEmpty(canonicalData.getSndrPymtPriority()))
		{
			sb.append(canonicalData.getSndrPymtPriority().trim()); 
		}
		else
		{
			sb.append("99");
		}
		
		//closing the block-A
		sb.append(NgphEsbConstants.BLOCK_CLOSING);
		logger .info("constructBankApplicationHeader(...)  End....");
		return sb.toString();
	}
	
	//For Ack
	private String constructBankApplicationHeaderForAck(AcknowledgementCanonical ackCanonicalData)throws Exception 
	{
		logger.info("constructBankApplicationHeaderForAck(...)  Start....");
		StringBuilder sb = new StringBuilder();		
		//appending message identifier
		//sb.append(NgphEsbConstants.NGPH_RTGS_MSG_IDENTIFIER);
		String bankApplicationIdentifier=null;
		if(StringUtils.isNotEmpty(ackCanonicalData.getDstEiId()))
		{
			//bankApplicationIdentifier = canonicalData.getMsgHost().substring(NgphEsbConstants.NGPH_INT_ZERO, NgphEsbConstants.NGPH_INT_THREE);
			bankApplicationIdentifier = esbServiceDao.getAppId(ackCanonicalData.getDstEiId());
		}
		if(bankApplicationIdentifier != null)
		{
			sb.append(bankApplicationIdentifier);
		}
		else
		{
			return null;
		}
		//appending i/o identifier
		if(ackCanonicalData.getMsgDirection().equalsIgnoreCase("o"))
		{
			sb.append(NgphEsbConstants.OUTWARD_PAYMENT); 
		}
		else
		{
			sb.append(NgphEsbConstants.INWARD_PAYMENT); 
		}
		//appending message type
		if(StringUtils.isNotEmpty(ackCanonicalData.getDstMsgType()))
		{
			sb.append(ackCanonicalData.getDstMsgType().trim()); 
		}
		else
		{
			logger.error("Destination message type is empty while constructing bank app header for SFMS acknowledgment");
			return null;
		}
		
		//appending  message sub type
		if(StringUtils.isNotEmpty(ackCanonicalData.getDstSubMsgType()))
		{
			sb.append(ackCanonicalData.getDstSubMsgType().trim()); 
		}
		else
		{
			logger.error("Destination SUB message type is empty while constructing bank app header for SFMS acknowledgment");
			return null;
		}
		
		//appending sender address
		if(StringUtils.isNotEmpty(ackCanonicalData.getSenderBank()))
		{
			sb.append(ackCanonicalData.getSenderBank().trim()); 
		}
		else
		{
			logger.error("Sender Bank is empty while constructing bank app header for SFMS acknowledgment");
			return null;
		}
		
		//receiver address
		if(StringUtils.isNotEmpty(ackCanonicalData.getReceiverBank()))
		{
			sb.append(ackCanonicalData.getReceiverBank().trim()); 
		}
		else
		{
			logger.error("Receiver bank is empty while constructing bank app header for SFMS acknowledgment");
			return null;
		}
		//delivery monitoring flag 1-yes, 2-no
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//open notification flag 1-yes, 2-no
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//Non-delivery warning flag
		sb.append(NgphEsbConstants.NGPH_INT_TWO); 
		//Obsolescence Period
		sb.append(NgphEsbConstants.NGPH_TRIPLE_ZERO);
		//MUR(message user reference)
		if(StringUtils.isNotEmpty(ackCanonicalData.getMsgMur()))
		{
			sb.append(ackCanonicalData.getMsgMur().trim()); 
		}
		else
		{
			logger.error("MUR is empty while constructing bank app header for SFMS acknowledgment");
			return null;
		}
		//Possible duplicate flag 1-yes, 2-no.
		sb.append(NgphEsbConstants.NGPH_INT_TWO);
		//Service Identifier
		//sb.append(NgphEsbConstants.TRIPLE_X);
		sb.append(bankApplicationIdentifier.substring(0, NgphEsbConstants.NGPH_INT_THREE));
		//originating date and time of our System
		SimpleDateFormat formatter = new SimpleDateFormat(EnumDateFormat.DATETIME_FORAMT.getFormat());
		java.util.Date today = new java.util.Date();
		//logger.info(formatter.format(new java.sql.Timestamp(today.getTime())));
		sb.append(formatter.format(new java.sql.Timestamp(today.getTime())));
		//testing and training flag 1-yes, 2-no.
		sb.append(NgphEsbConstants.NGPH_INT_TWO);
		//Sequence number
		//sb.append(NgphEsbConstants.NGPH_RTGS_CONSTANT_SEQ_NUM);
		sb.append(ackCanonicalData.getSeqNo());
		//filler
		sb.append(NgphEsbConstants.NGPH_RTGS_CONSTANT_FILLER);
		//Unique transaction reference i.e SndrTxnId
		if(StringUtils.isNotEmpty(ackCanonicalData.getSndrTxnId()))
		{
			sb.append(ackCanonicalData.getSndrTxnId().trim());
		}
		else
		{
			logger.error("Sender transaction id is empty while constructing bank app header for SFMS acknowledgment");
			return null;
		}
		//RTGS priority
		if(StringUtils.isNotEmpty(ackCanonicalData.getSndrPymtPriority()))
		{
			sb.append(ackCanonicalData.getSndrPymtPriority().trim()); 
		}	
		//closing the block-A
		sb.append(NgphEsbConstants.BLOCK_CLOSING);
		logger.info("constructBankApplicationHeaderForAck(...)  End....");
		return sb.toString();
	}
	
	private String constructObj(Object o)throws Exception
	{
		StringBuilder constructedVal = new StringBuilder();
		String dataVal[] = (String[])o;
		for(int i=0;i<dataVal.length;i++)
		{
			if(dataVal[i]!=null)
			{
				if (i+1 == dataVal.length)
				{
					constructedVal.append(dataVal[i]);
				}
				else
				{
					constructedVal.append(dataVal[i] + NgphEsbConstants.NGPH_SFMS_CRLF); //System.getProperty("line.separator"));
				}
			}
		}
		return constructedVal.toString();
	}
	private String getCurrentDate()throws Exception
    {
          logger.info("getCurrentDate(...) Inside...");
          Date dt = new Date();
          SimpleDateFormat sdf = new SimpleDateFormat(NgphEsbConstants.NGPH_MUR_SEQ_DATE_FORMAT);
          return sdf.format(dt);
    }
	/*
	 * Generate TxnID
	 */
	private String getTxnId()throws Exception
	{
        //constructing the 16-chars MUR value
        //MUR value Format= First 4-chars of LocalBic+current date as YYYYMMDD+4-chars sequence value
        StringBuilder murValue = new StringBuilder();
        //getting from local BIC
        String localBic = null;
        try 
        {
			localBic = esbServiceDao.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);
		} 
        catch (Exception e) 
        {
			logger.error(e, e);
			throw new Exception(e);
		}
        if(StringUtils.isNotEmpty(localBic) && localBic.length()>4)
        {
              murValue.append(localBic.substring(0,4));
        }
        else
        {
              return null;
        }
        //getting the current date and appending
        murValue.append(getCurrentDate());
        //get the sequence number from TA_SEQUENCES
        String seqNum=null;
		try 
		{
			seqNum = esbServiceDao.getSequenceNumber(NgphEsbConstants.SEQUENCE_KEY_MUR,4);
		}
		catch (Exception e) 
		{	
			logger.error(e, e);
			throw new Exception(e);
		}
        if(StringUtils.isNotEmpty(seqNum))
        {
        	murValue.append(seqNum);
        }
        else
        {
        	return null;
        }
        return murValue.toString();
	}
	
	
}
