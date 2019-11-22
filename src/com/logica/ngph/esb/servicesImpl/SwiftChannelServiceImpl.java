package com.logica.ngph.esb.servicesImpl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.EnumDateFormat;
import com.logica.ngph.esb.Dtos.FieldCanonicalAttribute;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;

import com.logica.ngph.esb.daos.SwiftParserDao;


import com.logica.ngph.esb.services.SwiftChannelService;

import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;

/**
 * @author guptarb
 *
 */
public class SwiftChannelServiceImpl implements SwiftChannelService
{
	static Logger logger = Logger.getLogger(SwiftChannelServiceImpl.class);  
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
	
	//Build Message for Acknowledgement
	public String buildSwiftMessageForAck(AcknowledgementCanonical ackCanonical) throws Exception
	{
		String result = null;
		try
		{
			logger.info("buildSwiftMessageForAck() Start....");
			StringBuilder swiftMessage = new StringBuilder();
			//Block-1 construction.
			String basicHeaderBlock = constructBasicHeaderBlockforAck(ackCanonical);
			if(StringUtils.isNotEmpty(basicHeaderBlock))
			{
				swiftMessage.append(basicHeaderBlock);
				logger.info("buildSwiftMessageforAck() Block-1 constructed....");
			}
			else
			{
				logger.error("SWIFT Basic header block not constructed for ack");
				result = null;
			}
			//Block-4 construction.
			swiftMessage.append(NgphEsbConstants.BLOCK_BEGINING);
			swiftMessage.append(NgphEsbConstants.TEXTBLOCK_BLOCKIDENTIFIER);
			String textBlock = constructTextBlockforAck(ackCanonical);
			if(StringUtils.isNotEmpty(textBlock))
			{
				swiftMessage.append(textBlock);
				swiftMessage.append(NgphEsbConstants.BLOCK_CLOSING);
				logger.info("buildSwiftMessageforAck() Block-4 constructed....");
			}
			else
			{
				logger.error("SWIFT text block not constructed for ack");
				result= null;
			}
			logger.info("buildSwiftMessageForAck() End....");
			//Appending the destination EI Id for Response handler
			swiftMessage.append("{999:" + ackCanonical.getDstEiId() + "," + ackCanonical.getMsgId() + "}");
			result= swiftMessage.toString();
		}
		catch (Exception e) 
		{
			logger.error("Exception occured while building SWIFT message for ack");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWCSVC0006", null, SwiftChannelServiceImpl.class, ackCanonical.getMsgId());//Exception occurred when constructing  SWIFT ack message. Refer error log for details.
			result= null;
			throw new Exception(e);
		}
		return result;
	}

	private String constructBasicHeaderBlockforAck(AcknowledgementCanonical ackCanonical)throws Exception
	{
		String blockOne = null;
		try
		{
			logger.info("constructBasicHeaderBlockforAck(...) Start...");
			
			StringBuilder sb = new StringBuilder();
			sb.append(NgphEsbConstants.BLOCK_BEGINING);
			sb.append(NgphEsbConstants.BASICHEADERBLOCK_BLOCKIDENTIFIER);
			sb.append(ackCanonical.getSrcSubMsgType());
			String localBic = null;
			try 
			{
				localBic = esbServiceDao.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);
			} 
			catch (Exception e) 
			{
				logger.error("Exception occured when trying to fetch local BIC while constructing swift header block for ack");
				logger.error(e, e);
				EventLogger.logEvent("NGPHSWCSVC0002", null, SwiftChannelServiceImpl.class, ackCanonical.getMsgId());//Exception occurred when fetching Local BIC for SWIFT ack header. Refer error log for details.
				blockOne= null;
				throw new  Exception(e);
			}
			if(StringUtils.isNotEmpty(localBic))
			{
				sb.append(localBic.trim());
				sb.append(ackCanonical.getSeqNo());
				sb.append(ackCanonical.getSeqNo());
				sb.append(NgphEsbConstants.BLOCK_CLOSING);
				blockOne = sb.toString();
			}
			logger.info("constructBasicHeaderBlock(...) End...");
		}
		catch (Exception e) 
		{
			logger.error("Exception occured while constructing basic header block for SWIFT ack");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWCSVC0003", null, SwiftChannelServiceImpl.class, ackCanonical.getMsgId());//Exception occurred when constructing  SWIFT ack header. Refer error log for details.
			blockOne= null;
			throw new  Exception(e);
		}
		return blockOne;}
	
	private String constructTextBlockforAck(AcknowledgementCanonical ackCanonical)throws Exception
	{
		String blockOne = null;
		try
		{
			logger.info("constructTextBlockforAck(...) Start...");
			
			StringBuilder sb = new StringBuilder();
			sb.append(NgphEsbConstants.BLOCK_BEGINING);
			
			sb.append(ackCanonical.getSrcSubMsgType());
			
			String localBic = null;
			try 
			{
				localBic = esbServiceDao.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);
			} 
			catch (Exception e) 
			{
				logger.error("Exception occured when trying to fetch local BIC while constructing swift text block for ack");
				logger.error(e, e);
				EventLogger.logEvent("NGPHSWCSVC0004", null, SwiftChannelServiceImpl.class, ackCanonical.getMsgId());//Exception occurred when fetching Local BIC for SWIFT ack text block. Refer error log for details.
				throw new  Exception(e);
			}
			if(StringUtils.isNotEmpty(localBic))
			{
				sb.append(localBic.trim());
				sb.append(ackCanonical.getSeqNo());
				sb.append(ackCanonical.getSeqNo());
				sb.append(NgphEsbConstants.BLOCK_CLOSING);
				blockOne = sb.toString();
			}
			logger.info("constructTextBlockforAck(...) End...");
		}
		catch (Exception e) 
		{
			logger.error("Exception occured while constructing text block for SWIFT ack");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWCSVC0005", null, SwiftChannelServiceImpl.class, ackCanonical.getMsgId());//Exception occurred when constructing  SWIFT ack text block. Refer error log for details.
			blockOne= null;
			throw new Exception(e);
		}
		return blockOne;}//Method Closed
	
	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	public String buildSwiftMessage(NgphCanonical canonicalData)throws Exception
	{
		String result=null;
		try
		{
			logger.info("buildSwiftMessage() Start....");
			StringBuilder swiftMessage = new StringBuilder();
			//Block-1 construction.
			String basicHeaderBlock = constructBasicHeaderBlock();
			if(StringUtils.isNotEmpty(basicHeaderBlock))
			{
				swiftMessage.append(basicHeaderBlock);
				logger.info("buildSwiftMessage() Block-1 constructed....");
			}
			else
			{
				logger.error("SWIFT basic header block could not be constructed for payment");
				result= null;
			}
			//Block-2 construction.
			String applicationHeaderBlock = constructApplicationHeaderBlock(canonicalData);
			if(StringUtils.isNotEmpty(applicationHeaderBlock))
			{
				swiftMessage.append(applicationHeaderBlock);
				logger.info("buildSwiftMessage() Block-2 constructed....");
			}
			else
			{
				logger.error("SWIFT application header block could not be constructed for payment");
				result= null;
			}
			
			//Block-3 construction.
			String userHeaderBlock = constructUserHeaderBlock(canonicalData);
			if(StringUtils.isNotEmpty(userHeaderBlock))
			{
				swiftMessage.append(userHeaderBlock);
				logger.info("buildSwiftMessage() Block-3 constructed....");
			}
			else
			{
				logger.error("SWIFT user header block could not be constructed for payment");
				result= null;
			}
			
			//Block-4 construction.
			swiftMessage.append(NgphEsbConstants.BLOCK_BEGINING);
			swiftMessage.append(NgphEsbConstants.TEXTBLOCK_BLOCKIDENTIFIER);
			swiftMessage.append(NgphEsbConstants.NGPH_CHAR_EOL);
			String textBlock = constructTextBlock(canonicalData);
			if(StringUtils.isNotEmpty(textBlock))
			{
				swiftMessage.append(textBlock);
				swiftMessage.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
				swiftMessage.append(NgphEsbConstants.BLOCK_CLOSING);
				logger.info("buildSwiftMessage() Block-4 constructed....");
			}
			else
			{
				logger.error("SWIFT text block could not be constructed for payment");
				result= null;
			}
			
			//Block-5 construction.
			swiftMessage.append(constructTrailerBlock());
			logger.info("buildSwiftMessage() Block-5 constructed....");
			//AuditEventLogging
			EventLogger.logEvent("NGPHSWCSVC0001", canonicalData, SwiftChannelServiceImpl.class, canonicalData.getMsgRef());//SWIFT message successfully constructed for payment.
			logger.info("buildSwiftMessage() The message is \n...."+swiftMessage.toString());
			logger.info("buildSwiftMessage() End....");
			//Appending the destination EI Id for Response handler
			swiftMessage.append("{999:" + canonicalData.getDstEiId() + "," + canonicalData.getMsgRef() + "}");
			result= swiftMessage.toString();
		}
		catch (Exception e) 
		{
			logger.error("Exception occured while buliding SWIFT message for payment");
			logger.error(e,e);
			result= null;
			throw new Exception(e);	
		}
		return result;
	}
	
	/**
	 * Block-5 construction.
	 * @return
	 */
	private String constructTrailerBlock()
	{
		logger.info("constructTrailerBlock() Start....");
		StringBuilder sb = new StringBuilder();
		sb.append(NgphEsbConstants.BLOCK_BEGINING);
		sb.append(NgphEsbConstants.TRAILERBLOCK_BLOCKIDENTIFIER);
		sb.append(NgphEsbConstants.BLOCK_CLOSING);
		logger.info("constructTrailerBlock() End....");
		return sb.toString();
	}
	
	/**
	 * Block-4 construction.
	 * @return
	 */
	private String constructTextBlock(NgphCanonical canonicalData)throws Exception
	{
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
		logger.info("constructMessageTextBlock(...)  Start....");
		StringBuilder sb = new StringBuilder();
		// Check for Null for channelType/SrcMsgType/SrcSubMsgType
		if(StringUtils.isNotBlank(canonicalData.getMsgChnlType()) && StringUtils.isNotEmpty(canonicalData.getMsgChnlType()) && StringUtils.isNotBlank(canonicalData.getSrcMsgType()) && StringUtils.isNotEmpty(canonicalData.getSrcMsgType()) && StringUtils.isNotBlank(canonicalData.getSrcMsgSubType()) && StringUtils.isNotEmpty(canonicalData.getSrcMsgSubType()))
		{
			String key = canonicalData.getMsgChnlType() + canonicalData.getSrcMsgType() + canonicalData.getSrcMsgSubType();
			logger.info("The key Value is : " + key);
			int maxCompSeq = 0;
			//Fetch the ArrayList Holding the Bean Objects where Canonical Attributes are mapped to corresponding field.
			ArrayList<FieldCanonicalAttribute> list = SFMSChannelServiceImpl.dataInitializer_CanonicalAttributes.get(key);
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
            			else if (canVal.equalsIgnoreCase("CAN_GRPGrpControlSum"))
            			{
            				sb.append(NgphEsbConstants.NGPH_SFMS_CRLF + ":" + fldNo + ":" + bigDecimalFormatter(canonicalData.getMsgAmount().toString()));
            			}
            			//FIXME - Arriving at the number of messages and its current part to be implemented - This also need to be done in SWIFT channel service
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
				EventLogger.logEvent("NGPHSWCSVC0007", canonicalData, SwiftChannelServiceImpl.class, canonicalData.getMsgRef());//Exception occurred when constructing text block from canonical. Refer error log for details.
				sb= null;
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

	private String constructObj(Object o)
	{
		StringBuilder constructedVal = new StringBuilder();
		String dataVal[] = (String[])o;
		for(int i=0;i<dataVal.length;i++)
		{
			if(dataVal[i]!=null)
			{
				constructedVal.append(dataVal[i] +"\n");
			}
		}
		
		return constructedVal.toString();
	}
	
	private String timeStampFormatter(Object o) throws Exception
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
	private String constructUserHeaderBlock(NgphCanonical canonicalData)throws Exception
	{
		String result=null;
		try
		{
			logger.info("constructUserHeaderBlock(...) Start...");
			StringBuilder sb = new StringBuilder();
			sb.append(NgphEsbConstants.BLOCK_BEGINING);
			sb.append(NgphEsbConstants.USERHEADERBLOCK_BLOCKIDENTIFIER);
			
			if(StringUtils.isNotEmpty(canonicalData.getSndrTxnId()))
			{
				sb.append(NgphEsbConstants.BLOCK_BEGINING);
				sb.append(NgphEsbConstants.USERHEADERBLOCK_BANKINGPRIORITY_TAG);
				sb.append(canonicalData.getSndrTxnId());
				sb.append(NgphEsbConstants.BLOCK_CLOSING);
			}
			
			//opening the MUR tag block
			sb.append(NgphEsbConstants.BLOCK_BEGINING);
			sb.append(NgphEsbConstants.USERHEADERBLOCK_MUR_TAG);
			
			//if(StringUtils.isNotEmpty(canonicalData.getMsgMur()))
			//{
			//	sb.append(canonicalData.getMsgMur().trim());
			//}
			//else
			//{
				//constructing the 16-chars MUR value
				//MUR value Format= First 4-chars of LocalBic+current date as YYYYMMDD+4-chars sequence value
				StringBuilder murValue = new StringBuilder();
				//getting from local BIC
				String localBic = null;
				localBic = esbServiceDao.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);
				if(StringUtils.isNotEmpty(localBic) && localBic.length()>4)
				{
					murValue.append(localBic.substring(0,4));
				}
				else
				{
					logger.error("Local BIC not configured");
					result= null;
				}
				
				//getting the current date and appending
				murValue.append(getCurrentDate());
				//get the sequence number from TA_SEQUENCES
				String seqNum = esbServiceDao.getSequenceNumber(NgphEsbConstants.SEQUENCE_KEY_MUR,4);
				if(StringUtils.isNotEmpty(seqNum))
				{
					murValue.append(seqNum);
				}
				else
				{
					logger.error("MUR Sequence not configured in SEQUENCES");
					result= null;
				}
				//appending the MUR value to tag
				if(murValue != null)
				{
					sb.append(murValue);
					//setting generated MUR for updating into TA_MESSAGES_TX table
					canonicalData.setMsgMur(murValue.toString());
				}
			//}
			
			//closing the MUR tag block
			sb.append(NgphEsbConstants.BLOCK_CLOSING);
			sb.append(NgphEsbConstants.BLOCK_CLOSING);
			result=sb.toString();
			logger.info("constructUserHeaderBlock(...) End...");
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in constructing SWIFT user header block");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWCSVC0008", canonicalData, SwiftChannelServiceImpl.class, canonicalData.getMsgRef());//Exception occurred when constructing SWIFT user header block. Refer error log for details.
			result= null;
			throw new Exception(e);
		}
		return result;

	}
	
	/**
	 * Block-2 construction.
	 * @return
	 */
	private String constructApplicationHeaderBlock(NgphCanonical canonicalData)throws Exception
	{
		String result=null;
		try
		{
			logger.info("constructApplicationHeaderBlock(...) Start...");
			StringBuilder sb = new StringBuilder();
			sb.append(NgphEsbConstants.BLOCK_BEGINING);
			sb.append(NgphEsbConstants.APPLICATIONHEADERBLOCK_BLOCKIDENTIFIER);
			sb.append(NgphEsbConstants.INWARD_PAYMENT);
			if(StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()))
			{
				sb.append(canonicalData.getDstMsgSubType());
			}
			else
			{
				logger.error("Destination message sub type is found to be empty in application header block contruction for SWIFT message");
				result= null;
			}
			if(StringUtils.isNotEmpty(canonicalData.getReceiverBank()))
			{
				sb.append(canonicalData.getReceiverBank());
			}
			else
			{
				logger.error("Receiver bank is found to be empty in application header block contruction for SWIFT message");
				result= null;
			}
			sb.append(NgphEsbConstants.APPLICATIONHEADERBLOCK_MSGPRIORITY);
			sb.append(NgphEsbConstants.APPLICATIONHEADERBLOCK_DELIVERYMONITORING);
			sb.append(NgphEsbConstants.APPLICATIONHEADERBLOCK_OBSOLESCENCE);		
			sb.append(NgphEsbConstants.BLOCK_CLOSING);
			logger.info("constructApplicationHeaderBlock(...) End...");
			result= sb.toString();
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in constructing SWIFT application header block");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWCSVC0009", canonicalData, SwiftChannelServiceImpl.class, canonicalData.getMsgRef());//Exception occurred when constructing SWIFT application header block. Refer error log for details.
			result= null;
			throw new Exception(e);
		}
		return result;
	}
	/**
	 * Block-1 construction.
	 * @return
	 */
	private String constructBasicHeaderBlock() throws Exception
	{
		String blockOne = null;
		try
		{
			logger.info("constructBasicHeaderBlock(...) Start...");
			StringBuilder sb = new StringBuilder();
			sb.append(NgphEsbConstants.BLOCK_BEGINING);
			sb.append(NgphEsbConstants.BASICHEADERBLOCK_BLOCKIDENTIFIER);
			sb.append(NgphEsbConstants.BASICHEADERBLOCK_APPLICATIONIDENTIFIER);
			sb.append(NgphEsbConstants.BASICHEADERBLOCK_SERVICEIDENTIFIER);
			String localBic = null;
			localBic = esbServiceDao.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);
			if(StringUtils.isNotEmpty(localBic))
			{
				sb.append(localBic.trim());
				sb.append(NgphEsbConstants.BASICHEADERBLOCK_SESSION_NUMBER);
				sb.append(NgphEsbConstants.BASICHEADERBLOCK_SEQUENCE_NUMBER);
				sb.append(NgphEsbConstants.BLOCK_CLOSING);
				blockOne = sb.toString();
			}
			else
			{
				logger.error("Local BIC is not configured");
				blockOne= null;
			}
			logger.info("constructBasicHeaderBlock(...) End...");
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in constructing SWIFT basic header block");
			logger.error(e,e);
			EventLogger.logEvent("NGPHSWCSVC0010", null, SwiftChannelServiceImpl.class, null);//Exception occurred when constructing SWIFT basic header block. Refer error log for details.
			blockOne= null;
			throw new Exception(e);
		}
		return blockOne;
	}

	/**
	 * 
	 * @return
	 */
	private String getCurrentDate()
	{
		logger.info("getCurrentDate(...) Inside...");
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(NgphEsbConstants.NGPH_MUR_SEQ_DATE_FORMAT);
		return sdf.format(dt);
	}
}
