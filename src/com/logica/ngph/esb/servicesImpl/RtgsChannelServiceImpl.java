package com.logica.ngph.esb.servicesImpl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.EnumDateFormat;
import com.logica.ngph.esb.AuditServiceClient;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.Dtos.FieldCanonicalAttribute;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.services.RtgsChannelService;
import com.logica.ngph.esb.services.SwiftChannelService;
import com.logica.ngph.utils.ApplicationContextProvider;

/**
 * 
 * @author guptarb
 *
 */
public class RtgsChannelServiceImpl implements RtgsChannelService{

	static Logger logger = Logger.getLogger(RtgsChannelServiceImpl.class);
	
	private EsbServiceDao esbServiceDao;
	
	/**
	 * @param esbServiceDao
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
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
	/**
	 * Builds the RTGS message from the canonical object.
	 * This reason we took it from Canonical bcoz we might have 
	 * to construct a RTGS message when the Input message is Swift or Vice-Versa.
	 */
	public String buildRtgsMessage(NgphCanonical canonicalData) throws Exception{
		logInfo("buildRtgsMessage(...)  Start....");
	
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
		rtgsMessage.append(NgphEsbConstants.NGPH_CHAR_EOL);
		
		//appending the block fields and values
		String messageTextBlock = constructMessageTextBlock(canonicalData);
		if(StringUtils.isNotEmpty(messageTextBlock))
		{
			rtgsMessage.append(messageTextBlock);

			//AuditEventLogging
			performAuditEventLogging("NGPHRTGSSER001", canonicalData.getMsgRef(), canonicalData.getTxnReference(), canonicalData.getMsgDept(), canonicalData.getMsgBranch(), canonicalData.getMsgRef());
			logDebuggers("buildRtgsMessage(): RTGS Message:"+canonicalData.getMsgRef()+" Constructed Successfully....");
		}
		else
		{
			//AuditEventLogging
			performAuditEventLogging("NGPHRTGSSER002", canonicalData.getMsgRef(), canonicalData.getTxnReference(), canonicalData.getMsgDept(), canonicalData.getMsgBranch(), canonicalData.getMsgRef());
			logDebuggers("buildRtgsMessage(): Failed To Construct RTGS message for msg:"+canonicalData.getMsgRef());
			return null;
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
			rtgsMessage.append(NgphEsbConstants.NGPH_CHAR_EOL_CTRF);
			rtgsMessage.append(NgphEsbConstants.NGPH_CHAR_HYPHEN);
			rtgsMessage.append(NgphEsbConstants.BLOCK_CLOSING);

		}
		logInfo("buildRtgsMessage(...)  End....");
		return rtgsMessage.toString();
	}
	
	/**
	 * This Method Constructs RTGS Message fourth Block
	 * @param canonicalData
	 * @return String
	 */
	private String constructMessageTextBlock(NgphCanonical canonicalData)throws Exception
	{
		//Venky code starts
		 
		logDebuggers("constructMessageTextBlock(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		// Check for Null for channelType/SrcMsgType/SrcSubMsgType
		if(StringUtils.isNotBlank(canonicalData.getMsgChnlType()) && StringUtils.isNotEmpty(canonicalData.getMsgChnlType()) && StringUtils.isNotBlank(canonicalData.getSrcMsgType()) && StringUtils.isNotEmpty(canonicalData.getSrcMsgType()) && StringUtils.isNotBlank(canonicalData.getSrcMsgSubType()) && StringUtils.isNotEmpty(canonicalData.getSrcMsgSubType()))
		{
			String key = canonicalData.getMsgChnlType() + canonicalData.getSrcMsgType() + canonicalData.getSrcMsgSubType();
			logger.info("The key Value is : " + key);
			
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
				// Declaring temp Variables
				String curFldNo="";
				String tempFldNo="";
				String tempMsgStr="";
				String prevEocInd="";
				String canValue="";
				String EolInd = "";
				
				// Storing first FldNo to Temp Field No
				tempFldNo = list.get(0).getFieldNo();
				
        		//Iterating over the ArrayList Containing Pojo Objects.
            	for(int i=0;i<list.size();i++)
    			{
            		FieldCanonicalAttribute object = list.get(i);
            		String canVal = "get" + object.getFieldCanonicalAtt();
            		String fldNo = object.getFieldNo();
            		String fldSeq = object.getFieldSeq();
            		String fldManOpt = object.getFieldCompMandOpt();
            		String fldCompSeq = object.getFieldCompSeq();
            		String fldEocInd = object.getFieldEocInd();
            		
            		logger.info(canVal + "\t" + fldNo + "\t" + fldSeq + "\t" + fldManOpt +"\t" + fldCompSeq + "\t" + fldEocInd);
            		
    				for (int j=0;j<functionHolder.size();j++) 
		            {
    					Method m = functionHolder.get(j);
    					String mname = m.getName();
	    				if(mname.equalsIgnoreCase(canVal))
	    				{
	    					if(fldEocInd==null || fldEocInd.equalsIgnoreCase("CRLF"))
	    					{
	    						EolInd = NgphEsbConstants.NGPH_CHAR_EOL_CTRF;
	    					}
	    					else if(fldEocInd.equalsIgnoreCase("SLSH"))
	    					{
	    						EolInd = NgphEsbConstants.NGPH_CHAR_EOL_SLSH;
	    					}
	    					else if( fldEocInd.equalsIgnoreCase("LENT"))
	    					{
	    						EolInd = "";
	    					}

	    					try
							{
								Object obj = m.invoke(canonicalData,new Object[]{});
								if(obj!=null) 
								{
									canValue = obj.toString();
			    					curFldNo = fldNo;

			                		if(StringUtils.isEmpty(tempMsgStr))
			                		{
			                			tempMsgStr = tempMsgStr + ":" + curFldNo + ":" + canValue + EolInd;
			                		}
			                		else
			                		{
			                			tempMsgStr = tempMsgStr + prevEocInd + canValue;
			                		}
			                		
			                		if(fldManOpt.equalsIgnoreCase("M"))
			                		{
			                			if(StringUtils.isEmpty(canValue))
			                				{
			                					tempMsgStr="";
			                				}
			                			else
			                				{
			                					sb.append(tempMsgStr);
			                					tempMsgStr="";
			                				}
			                		}
			                		else
			                		{
			                			if(tempFldNo.equalsIgnoreCase(curFldNo))
			                			{
			                				tempMsgStr="";
			                			}
			                			else
			                			{
			                				tempFldNo = curFldNo;
			                				prevEocInd = fldEocInd;
			                			}
			                		}

								}
							}catch (Exception e) {
								e.printStackTrace();
							}
	                		break;
	    				}

		            }
    			}
			}catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e);
			}
		}
				
		logDebuggers("constructMessageTextBlock(...)  End....");
		return sb.toString();

		 //Venky code Ends 
		
		/* Rajat Code Starts
		logDebuggers("constructMessageTextBlock(...)  Start....");
		
		StringBuilder sb = new StringBuilder();
		
		// Check for Null for channelType/SrcMsgType/SrcSubMsgType
		if(StringUtils.isNotBlank(canonicalData.getMsgChnlType()) && StringUtils.isNotEmpty(canonicalData.getMsgChnlType()) && StringUtils.isNotBlank(canonicalData.getSrcMsgType()) && StringUtils.isNotEmpty(canonicalData.getSrcMsgType()) && StringUtils.isNotBlank(canonicalData.getSrcMsgSubType()) && StringUtils.isNotEmpty(canonicalData.getSrcMsgSubType()))
		{
			String key = canonicalData.getMsgChnlType() + canonicalData.getSrcMsgType() + canonicalData.getSrcMsgSubType();
			logger.info("The key Value is : " + key);
			
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
				
				
				 * Variable used to Store first time Value of FldVal, because each field may have multiple occurrences.
				 * So Our Requirement is that it should enter into the condition only once and not multiple times
				 
				String tempFldNo = "";
				String tempStr="";
				String EolInd = null;
	           
	        		//Iterating over the ArrayList Containing Pojo Objects.
	            	for(int i=0;i<list.size();i++)
	    			{
	            		FieldCanonicalAttribute object = list.get(i);
	            		String canVal = "get" + object.getFieldCanonicalAtt();
	            		String fldNo = object.getFieldNo();
	            		String fldSeq = object.getFieldSeq();
	            		String fldManOpt = object.getFieldCompMandOpt();
	            		String fldCompSeq = object.getFieldCompSeq();
	            		String fldEocInd = object.getFieldEocInd();
	            		
	            		logger.info(canVal + "\t" + fldNo + "\t" + fldSeq + "\t" + fldManOpt +"\t" + fldCompSeq + "\t" + fldEocInd);
	            		
	    				for (int j=0;j<functionHolder.size();j++) 
    		            {
	    					Method m = functionHolder.get(j);
	    					String mname = m.getName();
	    					//logger.info("Canonical MAethod name : " + mname);
    		        		
		    				//If Canonical Method name matches POJO Canonical Attribute
		    				if(mname.equalsIgnoreCase(canVal))
		    				{
		    					if(fldEocInd==null || fldEocInd.equalsIgnoreCase("CRLF"))
		    					{
		    						EolInd = NgphEsbConstants.NGPH_CHAR_EOL_CTRF;
		    					}
		    					else if(fldEocInd.equalsIgnoreCase("SLSH"))
		    					{
		    						EolInd = NgphEsbConstants.NGPH_CHAR_EOL_SLSH;
		    					}
		    					else if( fldEocInd.equalsIgnoreCase("LENT"))
		    					{
		    						EolInd = "";
		    					}
		    						
		    					//Special Cases handled for Filed ending with Id
		    					if(canVal.endsWith("Id"))
		    					{
	    							//Fetch the next Attribute value
			    					FieldCanonicalAttribute nextobject = list.get(i+1);
				    				String nextcanVal = "get" + nextobject.getFieldCanonicalAtt();
				    				String nextfldNo = nextobject.getFieldNo();
				    				
				    				if(fldNo.equalsIgnoreCase(nextfldNo))
			    					{
							    		logger.info("First");
			    						logger.info(canVal + "\t" + fldNo);
			    						logger.info("NExt");
			    						logger.info(nextcanVal + "\t" + nextfldNo);
			    						
	    								try
	    								{
				    						//Fetching the Next Method Name
											Method mm = canonicalData.getClass().getMethod(nextcanVal) ;
											//Invoking the next Method using Reflection 
				    						Object obj = mm.invoke(canonicalData,new Object[]{});
				    						
				    						// If Method does not return Null 
				    						if(obj!=null) 
				    						{
					    						//Invoke particular method (Id Method)using Reflection 
					    						Object objId = m.invoke(canonicalData,new Object[]{});
					    						// If Method with Id does not return Null 
					    						if(objId!=null)
					    						{
					    							sb.append(NgphEsbConstants.NGPH_COLON);
					    							sb.append(fldNo.trim());
					    							sb.append(NgphEsbConstants.NGPH_COLON);
					    							sb.append(objId);
					    							sb.append(obj);
					    							sb.append(EolInd);
					    						}
					    						else
					    						{
					    							sb.append(NgphEsbConstants.NGPH_COLON);
					    							sb.append(nextfldNo.trim());
					    							sb.append(NgphEsbConstants.NGPH_COLON);
					    							sb.append(obj);
					    							sb.append(EolInd);
					    						}
				    						}

	    								}
	    								catch (Exception e) {
											e.printStackTrace();
										}
	    								i=i+1;
			    					}

		    					}
		    					//Normal Field Value execution as they do not have and pair members, so their uniqueness is maintained
		    					else
		    					{
			    					Object obj=null;
									try {
										obj = m.invoke(canonicalData,new Object[]{});
										if(obj!=null) 
										{
											// Either the object is a TimeStamp/Bigdecimal/String
											
											// check if the Object value is BigDecimal, then do formatting
											if(obj instanceof BigDecimal)
											{
												obj = bigDecimalFormatter(obj);	
											}

											// check if the Object value is Timestamp, then do formatting and convert to normal String
											else if(obj instanceof Timestamp)
											{
												obj = timeStampFormatter(obj);	
											}
	
											// Check whether the SB Object already holds field No
										if(sb.toString().contains(fldNo))
										{
											sb.append(obj);
			    							sb.append(EolInd);

										}
										else
										{
											sb.append(NgphEsbConstants.NGPH_COLON);
			    							sb.append(fldNo.trim());
			    							sb.append(NgphEsbConstants.NGPH_COLON);
			    							sb.append(obj);
			    							sb.append(EolInd);
										}
									}
									} 
									catch (Exception e) {
										e.printStackTrace();
									}
		    					}
		    						break;
		    				}
    		            }

	    			}
			}
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		logDebuggers("constructMessageTextBlock(...)  End....");
		return sb.toString();
		
		Rajat Code Ends here */
		}
	
	private String bigDecimalFormatter(Object o)
	{
		return o.toString().replace(".", ",");
	}
	

	private String timeStampFormatter(Object o)
	{
		String dataVal = o.toString().substring(0, 10);
		return dataVal.replace("-", "");
	}

		/* Old Aaqil Code
		logDebuggers("constructMessageTextBlock(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		//getting fields of the message for block-4
		List<String> fieldsList = esbServiceDao.getApplicableTextBlockFormatFields(canonicalData.getDstMsgType(), canonicalData.getDstMsgSubType());
		
		if(fieldsList != null && !fieldsList.isEmpty())
		{
			int fieldSeqNo = 0;
			boolean isMandatoryField = false;
			for(String fieldNo: fieldsList)
			{
				StringBuilder fieldBlock = null;
				int tempFieldSeqNo = 0;
				if(NgphEsbConstants.NGPH_RTGS_FIELD_TEN.equals(fieldNo))
				{
					String valueOfField = null;
					// FIXME remove comments later.String valueOfField = getTenthFieldValue(canonicalData.getMsgRef());
					if(StringUtils.isNotEmpty(valueOfField))
					{
						fieldBlock = new StringBuilder();
						fieldBlock.append(NgphEsbConstants.NGPH_COLON);
						fieldBlock.append(fieldNo.trim());
						fieldBlock.append(NgphEsbConstants.NGPH_COLON);
						fieldBlock.append(valueOfField);
						tempFieldSeqNo = NgphEsbConstants.NGPH_INT_TEN;
					}
				}else{
					List<MessageFormats> msgFormats = esbServiceDao.getFieldsOfMessage(canonicalData.getDstMsgType(), canonicalData.getDstMsgSubType(), fieldNo, NgphEsbConstants.NGPH_CONSTANT_HASH);
					if(msgFormats != null && !msgFormats.isEmpty())
					{
						Map<String, String> tagAttributes = esbServiceDao.getTagValue(fieldNo, NgphEsbConstants.NGPH_CONSTANT_HASH, null);
						int i =1;
						StringBuilder fieldValue = new StringBuilder();
						for(MessageFormats msgFmt: msgFormats)
						{
							if(fieldSeqNo != msgFmt.getFieldSeq())
							{
								tempFieldSeqNo = msgFmt.getFieldSeq();
								if(NgphEsbConstants.CONSTANT_VALUE_INT_ONE == msgFmt.getIsFieldMandatory())
								{
									isMandatoryField = true;
								}else{
									isMandatoryField = false;
								}
								//build the field
								if(fieldBlock == null)
								{
									fieldBlock = new StringBuilder();
									fieldBlock.append(NgphEsbConstants.NGPH_COLON);
									fieldBlock.append(fieldNo.trim());
									fieldBlock.append(NgphEsbConstants.NGPH_COLON);
								}
								
								//BUILDING TAG VALUE
								//appending prefix if required
								if(StringUtils.isNotEmpty(msgFmt.getComponentPrefix()))
								{
									fieldValue.append(msgFmt.getComponentPrefix().trim());
								}
								//some time keys begins with zero also
								String canonicalAttribute = null;
								if(i == NgphEsbConstants.CONSTANT_VALUE_INT_ONE)
								{
									if(tagAttributes.containsKey(NgphEsbConstants.CONSTANT_VALUE_ZERO))
									{
										canonicalAttribute = tagAttributes.get(NgphEsbConstants.CONSTANT_VALUE_ZERO);
									}else{
										canonicalAttribute = tagAttributes.get(String.valueOf(i));
									}
								}else{
									canonicalAttribute = tagAttributes.get(String.valueOf(i));
								}
								String attributeVal = fetchAttributeValue(canonicalAttribute, canonicalData);
								i++;
								
								if(StringUtils.isNotEmpty(attributeVal))
								{
									boolean isValidValue = false;
									
									//check whether attribute value is in proper format to add this field in message
									//1-BIC/IFSC, 2-SingleLine(35x), 3-MultipleLine(4*35x)
									if(msgFmt.getFieldTag() != null)
									{
										if(NgphEsbConstants.NGPH_STRING_ONE.equals(msgFmt.getFieldTag().trim()))
										{
											if(isThisValueBic(attributeVal))
											{
												//getting the IFSC code for this BIC from TA_PARTIES table
											String ifscCode	 = getIfscForThisBic(attributeVal);
												
												if(StringUtils.isNotEmpty(attributeVal))
												{
													attributeVal = ifscCode;
													isValidValue = true;
												}
													
											}else if(isThisAnIfscCode(attributeVal)){
												isValidValue = true;
											}
										}else if(NgphEsbConstants.NGPH_STRING_TWO.equals(msgFmt.getFieldTag().trim()))
										{
											if(!attributeVal.contains(NgphEsbConstants.NGPH_CHAR_EOL))
											{
												isValidValue = true;
											}
										}else if(NgphEsbConstants.NGPH_STRING_THREE.equals(msgFmt.getFieldTag().trim()))
										{
											if(attributeVal.contains(NgphEsbConstants.NGPH_CHAR_EOL))
											{
												isValidValue = true;
											}
										}else{

											if(attributeVal.trim().length() == 11 && msgFmt.getIsFieldMandatory()== 0)
											{
												if(isThisValueBic(attributeVal))
												{
													isValidValue = true;
												}
											}else if(attributeVal.trim().length() <= 3 && msgFmt.getIsFieldMandatory()== 0)
											{
												isValidValue = true;
											}
										
											isValidValue = true;
										}
									}else{
										//TODO log event as improper data configuration
										return null;
									}
									
									if(isValidValue)
									{
										//BUILDING TAG VALUE
										//appending prefix if required
										if(StringUtils.isNotEmpty(msgFmt.getComponentPrefix()))
										{
											fieldValue.append(msgFmt.getComponentPrefix().trim());
										}
										//appending component value if present
										fieldValue.append(attributeVal.trim());
										
										//appending suffix if available
										if(StringUtils.isNotEmpty(msgFmt.getComponentSufix()))
										{
											fieldValue.append(msgFmt.getComponentSufix().trim());
										}
										
										//appending EOL if required
										if(NgphEsbConstants.CONSTANT_VALUE_INT_ONE == msgFmt.getComponentOutLef())
										{
											fieldValue.append(NgphEsbConstants.NGPH_CHAR_EOL);
										}
									}
									
								}else if(NgphEsbConstants.CONSTANT_VALUE_INT_ONE == msgFmt.getIsComponentMandatory() && isMandatoryField){
									return null;
								}
								
							}else{
								//break the loop to avoid the tag & value construction
								break;
							}
						}
						
						//appending field and its value to fieldBlock
						if(StringUtils.isNotEmpty(fieldValue.toString()))
						{
							fieldBlock.append(fieldValue);
						}else{
							if(isMandatoryField)
							{
								return null;
							}else{
								fieldBlock = null;
							}
						}
					}
				}
				
				if(fieldBlock != null && StringUtils.isNotEmpty(fieldBlock.toString()))
				{
					sb.append(fieldBlock);
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
					fieldSeqNo = tempFieldSeqNo;
				}
			}
		}
		logDebuggers("constructMessageTextBlock(...)  End....");
		return sb.toString();
	*/
		
	/*
	*//**
	 * 
	 * @param attributeVal
	 * @return
	 *//*
	private String getIfscForThisBic(String attributeVal) throws NGPHException{
		logDebuggers("getIfscForThisBic(...)  Start....");
		String ifscCode = null;
		
		if(NgphEsbConstants.NGPH_INT_ELEVEN == attributeVal.trim().length())
		{
			ifscCode = esbServiceDao.getIfscCodeByBic(attributeVal.trim());
		}
		logDebuggers("getIfscForThisBic(...)  End....");
		return ifscCode;
	}
	
	*//**
	 * 
	 * @param msgRef
	 * @return
	 *//*
	private String getTenthFieldValue(String msgRef)throws NGPHException
	{
		logDebuggers("getTenthFieldValue(...)  Start....");
		String ustrd = null;
		esbServiceDao.getUstrdValue(msgRef);
		logDebuggers("getTenthFieldValue(...)  End....");
		return ustrd;
	}
	*//**
	 * 
	 * @param fieldNo
	 * @param tagName
	 * @param tagSeq
	 * @return
	 *//*
	private String fetchAttributeValue(String attributeName, NgphCanonical canonicalData)throws NGPHException
	{
		logDebuggers("fetchAttributeValue(...)  Start....");
		//Class cls = canonicalData.getClass();
		String val = null;
		if(StringUtils.isNotEmpty(attributeName))
		{
			String firstLetter = String.valueOf(attributeName.charAt(NgphEsbConstants.NGPH_INT_ZERO));
			String methodName = NgphEsbConstants.NGPH_GET_METHOD_PREFIX.concat(attributeName.trim().replaceFirst(firstLetter, firstLetter.toUpperCase()));

			@SuppressWarnings("unchecked")
			Class<NgphCanonical>[] types = new Class[]{};
			
			Method method = null;
			try {
				
				method = canonicalData.getClass().getMethod(methodName, types);
				Object obj = method.invoke(canonicalData, new Object[0]);
				if(obj != null)
				{
					if("MT".equalsIgnoreCase(canonicalData.getSrcMsgType()))
					   val = formatTheCanonicalValue(obj, EnumDateFormat.SWIFT_FORMAT);
					else
						 val = formatTheCanonicalValue(obj, EnumDateFormat.RTGS_FORMAT);
				}
			} catch (SecurityException e) {
				logError("fetchAttributeValue(...) Error....", e.getMessage());
				throw new NGPHException();
			} catch (NoSuchMethodException e) {
				logError("fetchAttributeValue(...) Error....", e.getMessage());
				throw new NGPHException();
			} catch (IllegalArgumentException e) {
				logError("fetchAttributeValue(...) Error....", e.getMessage());
				throw new NGPHException();
			} catch (IllegalAccessException e) {
				logError("fetchAttributeValue(...) Error....", e.getMessage());
				throw new NGPHException();
			} catch (InvocationTargetException e) {
				logError("fetchAttributeValue(...) Error....", e.getMessage());
				throw new NGPHException();
			}
		}
		logDebuggers("fetchAttributeValue(...)  End....");
		return val;
	}
	
	*//**
	 * 
	 * @param obj
	 * @return
	 *//*
	private String formatTheCanonicalValue(Object obj, EnumDateFormat dateFmt)
	{
		logDebuggers("formatTheCanonicalValue(...) Start...");
		String formatedValue = null;
		if(obj instanceof BigDecimal)
		{
			//FIXME later make this separator as configurable collect detailed business.
			formatedValue = String.valueOf(obj).replace(".", ",");
		}else if(obj instanceof Timestamp)
		{
				formatedValue = DateHelper.parseDate(new java.util.Date(((Timestamp)obj).getTime()), dateFmt);
		}else{
			formatedValue = String.valueOf(obj);
		}
		logDebuggers("formatTheCanonicalValue(...) End...");
		return formatedValue;
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 *//*
	private String buildMessageTextBlock(NgphCanonical canonicalData) {
		logDebuggers("buildMessageTextBlock(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		//appending the first field, this is mandatory
		if(StringUtils.isNotEmpty(canonicalData.getTxnReference()))
		{
			//appending field name
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_ONE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			//appending field value
			sb.append(canonicalData.getTxnReference().trim()); 
			sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		}else{
			return null;
		}
		
		//appending the second field, this is mandatory
		if(canonicalData.getMsgValueDate() != null && StringUtils.isNotEmpty(canonicalData.getMsgCurrency()) && canonicalData.getMsgAmount() != null)
		{
			//appending field name
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_TWO);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			//appending field value
			sb.append(canonicalData.getMsgValueDate()); 
			sb.append(canonicalData.getMsgCurrency().trim()); 
			sb.append(canonicalData.getMsgAmount()); 
			sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
			
		}else{
			return null;
		}
		
		//appending the third field, this is mandatory
		if(StringUtils.isNotEmpty(canonicalData.getOrderingCustomerName()) && StringUtils.isNotEmpty(canonicalData.getOrderingCustomerAddress()))
		{
			//appending field name
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_THREE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			//appending field value
			sb.append(canonicalData.getOrderingCustomerName().trim()); 
			sb.append(canonicalData.getOrderingCustomerAddress().trim());
			sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		}else{
			return null;
		}
		
		//appending the fourth field, this is optional
		String fourthField = buildFourthField(canonicalData);
		if(StringUtils.isNotEmpty(fourthField))
		{
			sb.append(fourthField);
		}
		
		//appending the fifth field, this is optional
		String fifthField = buildFifthField(canonicalData);
		if(StringUtils.isNotEmpty(fifthField))
		{
			sb.append(fifthField);
		}
		
		//appending the sixth field, this is optional
		String sixthField = buildSixthField(canonicalData);
		if(StringUtils.isNotEmpty(sixthField))
		{
			sb.append(sixthField);
		}
		
		//appending the seventh field, this is optional
		String sevenhField = buildSeventhField(canonicalData);
		if(StringUtils.isNotEmpty(sevenhField))
		{
			sb.append(sevenhField);
		}
		
		//appending the eighth field, this is optional
		String eighthField = buildEighthField(canonicalData);
		if(StringUtils.isNotEmpty(eighthField))
		{
			sb.append(eighthField);
		}
		
		//appending the ninth field, this is mandatory
		String ninthField = buildNinthField(canonicalData);
		if(StringUtils.isNotEmpty(ninthField))
		{
			sb.append(ninthField);
		}else{
			return null;
		}
		
		//appending the tenth field, this is optional 
		String tenthField = buildTenthField(canonicalData.getMsgRef());
		if(StringUtils.isNotEmpty(tenthField))
		{
			sb.append(tenthField);
		}
		
		//appending the eleventh field, this is optional
		if(StringUtils.isNotEmpty(canonicalData.getChargeBearer()))
		{
			//build the field 7028
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_ELEVEN);
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(canonicalData.getChargeBearer().trim());
			sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		}
		
		//appending the twelve field, this is optional
		if(StringUtils.isNotEmpty(canonicalData.getInstructionsForCrdtrAgtCode()) || StringUtils.isNotEmpty(canonicalData.getInstructionsForNextAgtText()))
		{
			//build the field 7495
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_TWELVE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			if(StringUtils.isNotEmpty(canonicalData.getInstructionsForCrdtrAgtCode()))
			{
				sb.append(canonicalData.getInstructionsForCrdtrAgtCode().trim());
				sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
			}
			if(StringUtils.isNotEmpty(canonicalData.getInstructionsForNextAgtText()))
			{
				sb.append(canonicalData.getInstructionsForNextAgtText().trim());
				//sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
			}
		}
		logDebuggers("buildMessageTextBlock(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param referenceNum
	 * @return
	 *//*
	private String buildTenthField(String referenceNum)
	{
		logDebuggers("buildTenthField(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		//TODO find in TA_RMT_INFO table for the value with message reference id
		if(StringUtils.isNotEmpty(referenceNum))
		{
			//call DAO method
		}
		logDebuggers("buildTenthField(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 *//*
	private String buildNinthField(NgphCanonical canonicalData)
	{
		logDebuggers("buildNinthField(...)  Start....");
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotEmpty(canonicalData.getBeneficiaryCustomerName()) || StringUtils.isNotEmpty(canonicalData.getBeneficiaryCustomerAddress()))
		{
			//build the field 5561
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_NINE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			if(StringUtils.isNotEmpty(canonicalData.getBeneficiaryCustAcct()))
			{
				sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
				sb.append(canonicalData.getBeneficiaryCustAcct().trim());
				sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
			}
			if(StringUtils.isNotEmpty(canonicalData.getBeneficiaryCustomerName()))
			{
				sb.append(canonicalData.getBeneficiaryCustomerName().trim());
				sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
			}
			if(StringUtils.isNotEmpty(canonicalData.getBeneficiaryCustomerAddress()))
			{
				sb.append(canonicalData.getBeneficiaryCustomerAddress().trim());
				sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
			}
			
		}else{
			return null;
		}
		
		sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		logDebuggers("buildNinthField(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 *//*
	private String buildEighthField(NgphCanonical canonicalData)
	{
		logDebuggers("buildEighthField(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		if(StringUtils.isNotEmpty(canonicalData.getAccountWithInstitution()))
		{
			//build the field 6516
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_EIGHTH_ONE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			if(isThisValueBic(canonicalData.getAccountWithInstitution().trim()))
			{
				//get the IFSC code of this BIC from TA_PARTIES
				
			}else if(isThisAnIfscCode(canonicalData.getAccountWithInstitution().trim())){
				sb.append(canonicalData.getAccountWithInstitution().trim());
			}else if(StringUtils.isNotEmpty(canonicalData.getAccountWithInstitutionAcct()) && !canonicalData.getAccountWithInstitutionAcct().contains(NgphEsbConstants.NGPH_CHAR_EOL)){
				//build the field 6719
				sb = new StringBuilder();
				sb.append(NgphEsbConstants.NGPH_COLON);
				sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_EIGHTH_TWO);
				sb.append(NgphEsbConstants.NGPH_COLON);
				
				if(StringUtils.isNotEmpty(canonicalData.getAccountWithInstitutionAcct()))
				{
					sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
					sb.append(canonicalData.getAccountWithInstitutionAcct().trim());
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
				}
				sb.append(canonicalData.getAccountWithInstitution().trim());
			}else{
				//build the field 5551
				sb = new StringBuilder();
				sb.append(NgphEsbConstants.NGPH_COLON);
				sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_EIGHTH_THREE);
				sb.append(NgphEsbConstants.NGPH_COLON);
				
				if(StringUtils.isNotEmpty(canonicalData.getAccountWithInstitutionAcct()))
				{
					sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
					sb.append(canonicalData.getAccountWithInstitutionAcct().trim());
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
				}
				sb.append(canonicalData.getAccountWithInstitution().trim());
			}
			
		}else{
			return null;
		}
		
		sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		logDebuggers("buildEighthField(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 *//*
	private String buildSeventhField(NgphCanonical canonicalData){
		logDebuggers("buildSeventhField(...)  Start....");
		StringBuilder sb = new StringBuilder();

		if(StringUtils.isNotEmpty(canonicalData.getIntermediary1Bank()))
		{
			//build the field 6511
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_SEVENTH_ONE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			if(isThisValueBic(canonicalData.getIntermediary1Bank().trim()))
			{
				//get the IFSC code of this BIC from TA_PARTIES
				
			}else if(isThisAnIfscCode(canonicalData.getIntermediary1Bank().trim())){
				sb.append(canonicalData.getIntermediary1Bank().trim());
			}else{
				return null;
			}
		}else if(StringUtils.isNotEmpty(canonicalData.getIntermediary1BankName())){
			
			//build the field 5546
			sb = new StringBuilder();
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_SEVENTH_TWO);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			if(StringUtils.isNotEmpty(canonicalData.getIntermediary1AgentAcct()))
			{
				sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
				sb.append(canonicalData.getIntermediary1AgentAcct().trim());
				sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
			}
			sb.append(canonicalData.getIntermediary1BankName().trim());
			
		}else{
			return null;
		}
		
		sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		logDebuggers("buildSeventhField(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 *//*
	private String buildSixthField(NgphCanonical canonicalData)
	{
		logDebuggers("buildSixthField(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		if(StringUtils.isNotEmpty(canonicalData.getReceiverCorrespondent()))
		{
			//build the field 6500
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_SIXTH_ONE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			if(isThisValueBic(canonicalData.getReceiverCorrespondent().trim()))
			{
				//get the IFSC code of this BIC from TA_PARTIES
				
			}else if(isThisAnIfscCode(canonicalData.getReceiverCorrespondent().trim())){
				sb.append(canonicalData.getReceiverCorrespondent().trim());
			}else if(StringUtils.isNotEmpty(canonicalData.getReceiverCorrespondentAcct()) && !canonicalData.getReceiverCorrespondentAcct().contains(NgphEsbConstants.NGPH_CHAR_EOL)){
				//build the field 6718
				sb = new StringBuilder();
				sb.append(NgphEsbConstants.NGPH_COLON);
				sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_SIXTH_TWO);
				sb.append(NgphEsbConstants.NGPH_COLON);
				
				if(StringUtils.isNotEmpty(canonicalData.getReceiverCorrespondentAcct()))
				{
					sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
					sb.append(canonicalData.getReceiverCorrespondentAcct().trim());
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
				}
				sb.append(canonicalData.getReceiverCorrespondent().trim());
			}else{
				//build the field 5526
				sb = new StringBuilder();
				sb.append(NgphEsbConstants.NGPH_COLON);
				sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_SIXTH_THREE);
				sb.append(NgphEsbConstants.NGPH_COLON);
				
				if(StringUtils.isNotEmpty(canonicalData.getReceiverCorrespondentAcct()))
				{
					sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
					sb.append(canonicalData.getReceiverCorrespondentAcct().trim());
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
				}
				sb.append(canonicalData.getReceiverCorrespondent().trim());
			}
			
		}else{
			return null;
		}
		
		sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		logDebuggers("buildSixthField(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 *//*
	private String buildFifthField(NgphCanonical canonicalData)
	{
		logDebuggers("buildFifthField(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		if(StringUtils.isNotEmpty(canonicalData.getSenderCorrespondent()))
		{
			//build the field 5518
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_FIVE_ONE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			if(isThisValueBic(canonicalData.getSenderCorrespondent().trim()))
			{
				//get the IFSC code of this BIC from TA_PARTIES
				
			}else if(isThisAnIfscCode(canonicalData.getSenderCorrespondent().trim())){
				sb.append(canonicalData.getSenderCorrespondent().trim());
			}else if(StringUtils.isNotEmpty(canonicalData.getSenderCorrespondentAcct()) && !canonicalData.getSenderCorrespondentAcct().contains(NgphEsbConstants.NGPH_CHAR_EOL)){
				//build the field 6717
				sb = new StringBuilder();
				sb.append(NgphEsbConstants.NGPH_COLON);
				sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_FIVE_TWO);
				sb.append(NgphEsbConstants.NGPH_COLON);
				
				if(StringUtils.isNotEmpty(canonicalData.getSenderCorrespondentAcct()))
				{
					sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
					sb.append(canonicalData.getSenderCorrespondentAcct().trim());
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
				}
				sb.append(canonicalData.getSenderCorrespondent().trim());
			}else{
				//build the field 5521
				sb = new StringBuilder();
				sb.append(NgphEsbConstants.NGPH_COLON);
				sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_FIVE_THREE);
				sb.append(NgphEsbConstants.NGPH_COLON);
				
				if(StringUtils.isNotEmpty(canonicalData.getSenderCorrespondentAcct()))
				{
					sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
					sb.append(canonicalData.getSenderCorrespondentAcct().trim());
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
				}
				sb.append(canonicalData.getSenderCorrespondent().trim());
			}
			
		}else{
			return null;
		}
		
		sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		logDebuggers("buildFifthField(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 *//*
	private String buildFourthField(NgphCanonical canonicalData)
	{
		logDebuggers("buildFourthField(...)  Start....");
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotEmpty(canonicalData.getOrderingInstitution()))
		{
			//build the field 5517
			sb.append(NgphEsbConstants.NGPH_COLON);
			sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_FOUR_ONE);
			sb.append(NgphEsbConstants.NGPH_COLON);
			
			if(isThisValueBic(canonicalData.getOrderingInstitution().trim()))
			{
				//get the IFSC code of this BIC from TA_PARTIES
				
			}else if(isThisAnIfscCode(canonicalData.getOrderingInstitution().trim())){
				sb.append(canonicalData.getOrderingInstitution().trim());
			}else{
				//build the field 5516
				sb = new StringBuilder();
				sb.append(NgphEsbConstants.NGPH_COLON);
				sb.append(NgphEsbConstants.NGPH_RTGS_FIELD_FOUR_TWO);
				sb.append(NgphEsbConstants.NGPH_COLON);
				
				if(StringUtils.isNotEmpty(canonicalData.getOrderingInstitutionAcct()))
				{
					sb.append(NgphEsbConstants.NGPH_CONSTANT_SLASH);
					sb.append(canonicalData.getOrderingInstitutionAcct().trim());
					sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
				}
				sb.append(canonicalData.getOrderingInstitution().trim());
			}
		}else{
			return null;
		}
		
		sb.append(NgphEsbConstants.NGPH_CHAR_EOL);
		logDebuggers("buildFourthField(...)  End....");
		return sb.toString();
	}
	
	*//**
	 * 
	 * @param value
	 * @return
	 *//*
	private boolean isThisAnIfscCode(String value)
	{
		return false;
	}
	
	*//**
	 * 
	 * @param value
	 * @return
	 *//*
	private boolean isThisValueBic(String value)
	{
		return false;
	}
	
	*//**
	 * 
	 * @param canonicalData
	 * @return
	 */
	private String constructBankApplicationHeader(NgphCanonical canonicalData) {
		logDebuggers("constructBankApplicationHeader(...)  Start....");
		StringBuilder sb = new StringBuilder();
		
		//adding the block name
		sb.append(NgphEsbConstants.BLOCK_BEGINING);
		sb.append(NgphEsbConstants.NGPH_CONSTANT_A);
		sb.append(NgphEsbConstants.NGPH_COLON);
		
		//ADDING THE BLOCK VALUE
		//appending BankApplicationIdentifier
		String bankApplicationIdentifier = null;
		if(StringUtils.isNotEmpty(canonicalData.getMsgHost()) && canonicalData.getMsgHost().trim().length() > NgphEsbConstants.NGPH_INT_THREE)
		{
			bankApplicationIdentifier = canonicalData.getMsgHost().substring(NgphEsbConstants.NGPH_INT_ZERO, NgphEsbConstants.NGPH_INT_THREE);
		}
		if(bankApplicationIdentifier != null)
		{
			sb.append(bankApplicationIdentifier);
		}else{
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
		}else{
			return null;
		}
		
		//appending  message sub type
		if(StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()))
		{
			sb.append(canonicalData.getDstMsgSubType().trim()); 
		}else{
			return null;
		}
		
		//appending sender address
		if(StringUtils.isNotEmpty(canonicalData.getSenderBank()))
		{
			sb.append(canonicalData.getSenderBank().trim()); 
		}else{
			return null;
		}
		
		//receiver address
		if(StringUtils.isNotEmpty(canonicalData.getReceiverBank()))
		{
			sb.append(canonicalData.getReceiverBank().trim()); 
		}else{
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
			sb.append(canonicalData.getCustTxnReference().trim()); 
		}else if(StringUtils.isNotEmpty(canonicalData.getTxnReference())){
			sb.append(canonicalData.getTxnReference().trim());
		}else{
			return null;
		}
		
		//Possible duplicate flag 1-yes, 2-no.
		//FIXME-later we need to add logic to assign the value.
		sb.append(NgphEsbConstants.NGPH_INT_TWO);
		
		//Service Identifier
		sb.append(NgphEsbConstants.TRIPLE_X);
		
		//originating date and time of our System
		SimpleDateFormat formatter = new SimpleDateFormat(EnumDateFormat.DATETIME_FORAMT.getFormat());
		java.util.Date today = new java.util.Date();
		//logger.info(formatter.format(new java.sql.Timestamp(today.getTime())));
		sb.append(formatter.format(new java.sql.Timestamp(today.getTime())));
		
		//testing and training flag 1-yes, 2-no.
		sb.append(NgphEsbConstants.NGPH_INT_TWO);
		
		//Sequence number
		sb.append(NgphEsbConstants.NGPH_RTGS_CONSTANT_SEQ_NUM);
		
		//filler
		sb.append(NgphEsbConstants.NGPH_RTGS_CONSTANT_FILLER);
		
		//Unique transaction reference i.e SndrTxnId
		if(StringUtils.isNotEmpty(canonicalData.getSndrTxnId()))
		{
			sb.append(canonicalData.getSndrTxnId().trim()); 
		}else{
			return null;
		}
		
		//RTGS priority
		if(StringUtils.isNotEmpty(canonicalData.getSndrPymtPriority()))
		{
			sb.append(canonicalData.getSndrPymtPriority().trim()); 
		}
		
		//closing the block-A
		sb.append(NgphEsbConstants.BLOCK_CLOSING);
		logDebuggers("constructBankApplicationHeader(...)  End....");
		return sb.toString();
	}
	
	/**
	 * 
	 * @param eventId
	 * @param msgRef
	 * @param msgTxnRef
	 * @param msgBranch
	 * @param msgDept
	 */
	private void performAuditEventLogging(String eventId, String msgRef, String msgTxnRef, String msgDept, String msgBranch, String extras)
	{
		AuditServiceClient auditServiceClient = new AuditServiceClient();
		EventAudit audit = new EventAudit();
		audit.setAuditEventId(eventId);
		audit.setAuditMessageRef(msgRef);
		audit.setAuditTransactionRef(msgTxnRef);
		audit.setAuditSource(SwiftChannelService.class.toString().replace("com.logica.ngph.", "").trim());
		audit.setAuditBranch(msgBranch);
		audit.setAuditDept(msgDept);
		if(StringUtils.isNotEmpty(extras))
		{
			String[] extraInfo = extras.split(",");
			audit.setExtraInformation(extraInfo);
		}
		auditServiceClient.dbPollerQueueCall(null, "AUDIT", audit);
	}
	
	/**
	 * 
	 * @param message
	 * @param code
	 */
	private void logError(String message, String code)
	{
		/**
         * log the information when logger is in debug mode 
         * 
         */
        if(logger.isDebugEnabled()){
              logger.debug(message+code);
        }
        /**
         * log the information when logger is in info mode 
         * 
         */
        if(logger.isInfoEnabled()){
              logger.info(message+code);   
        }
        /**
         * log the information when logger is in error mode 
         * 
         */
        if(logger.isEnabledFor(Level.ERROR)){
              logger.error(message+code);  
        }
	}
	
	 /**
     * log the information when logger is in info mode 
     * 
     */
	private void logInfo(String info)
	{
		
        if(logger.isInfoEnabled()){
              logger.info(info);   
        }
	}
	
	 /**
     * log the information when logger is in debug mode 
     * 
     */
	private void logDebuggers(String debugInfo)
	{
		
        if(logger.isDebugEnabled()){
              logger.debug(debugInfo);
        }
	}
}
