package com.logica.ngph.validators.serviceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.enums.EnumDateFormat;
import com.logica.ngph.common.utils.DateHelper;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.validators.daoImpl.ValidatorDao;
import com.logica.ngph.validators.dto.MsgField;
import com.logica.ngph.validators.dto.MsgFormat;
import com.logica.ngph.validators.services.IMsgFieldValidator;

/**
 * @author guptarb
 *
 */
public class MsgFieldValidator implements IMsgFieldValidator
{	
	static Logger logger = Logger.getLogger(MsgFieldValidator.class);
	private List<String> fieldKey = new ArrayList<String>();
	private EsbServiceDao esbParserDao;
	
	private List<String> getFieldKeys(Map<String, String> fieldMap)
	{
		try
		{
	        Iterator iterator = fieldMap.keySet().iterator();
	       
	        while(iterator.hasNext())
	        {        
	            fieldKey.add(iterator.next().toString());
	        }
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
        
        return fieldKey;
	}
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.validators.services.IMsgFieldValidor#validate_Msg_Fields(java.util.Map)
	 * 
	 * FieldMap is the Map Object received by Adapter Service containing Fourth block contents.
	 * This Map is created using WIFE API where the fourth block or main Processing block is splitted into a HashMap.
	 * Where Field no is the Key and the field Value is the Value Object of HashMap.
	 * e.g("20","16 chars")
	 */
	public String validate_Msg_Fields(Map<String, String> fieldMap,String msgChnlType, String srcMsgType, String  srcMsgSubType,String msgRef)
	{
		String errorMessage = "";  
		// Final Status send from Validate Service, default taken to false.
		boolean validationStatus = true;
		/*
		 *  Gets the Data Intialized by MsgFieldDataInitializer in a local Map
		 *  The Map contains Key as a combination of  Field no and FieldCompSeq
		 *  and Map value is the MsgField Object where all the values are initialized at early stage by MsgField Data Initializer 
		 */
		// Populate the DB Initialized Map to a Local Map
		HashMap<String, MsgFormat> hm = ValidatorDao.hm;
		List<String> fieldKeyList = getFieldKeys(fieldMap);
		logger.info(fieldKeyList);
		MsgFormat msgFormat;
		HashMap<String, MsgField> fielddataMap;
		String mapKey = null;
		String mapValue = null;
		String tempFldNo=null;
		int idxOfSlsh=0;
		int idxOfCRLF=0;
		
		try
		{
			// Labeled Loop
			// If validation Fails then we will quit from parent loop as there is not need for further validation.
			
			ParentFor:// name of the Labeled Loop
			// Iterating the Map received for Validation containing original field values 
			for (Map.Entry<String, String> entry : fieldMap.entrySet()) 
			{
				if(StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotEmpty(entry.getKey())&& entry.getKey()!=null)
				{

					mapKey = entry.getKey();
					mapValue = entry.getValue();
					
					if (msgChnlType.equalsIgnoreCase("SWIFT"))
		        	{
			        	if (mapKey.startsWith("5") && (mapKey.contains("A") || mapKey.contains("B") || mapKey.contains("C") || mapKey.contains("D") || mapKey.contains("K")))
			        	{
			        		tempFldNo = mapKey.substring(0, mapKey.length()-1);
			        	}
			        	else
			        	{
			        		tempFldNo = mapKey;
			        	}
		        	}
		        	else
		        	{
		        		tempFldNo = mapKey;
		        	}
					
					msgFormat = hm.get(msgChnlType+srcMsgType+srcMsgSubType+tempFldNo);
					if (msgFormat == null)
					{
						tempFldNo = mapKey.substring(0, mapKey.length()-1);
						msgFormat = hm.get(msgChnlType+srcMsgType+srcMsgSubType+tempFldNo);
					}
					fielddataMap = msgFormat.getMsgFieldMapper();
					
					// Check if the Actual Value present in the message in not Empty or blank or null
					if(StringUtils.isNotBlank(mapValue) && StringUtils.isNotEmpty(mapValue)&& mapValue!=null)
					{
						 
						logger.info("Key = " +  mapKey+ ", Value = " + mapValue);
						
						//Ltrim the Value
						mapValue = StringUtils.stripStart(mapValue, " ");
						
						//Check for Occurrence of \r\n in the starting i.e It Contains Enter or not
						if(mapValue.startsWith("\r\n"))
						{
							mapValue = mapValue.substring(2, mapValue.length());
						}
						
						// Used For LENT CASE
						StringBuilder leftTrimMapVal= null;
						leftTrimMapVal = new StringBuilder(mapValue);
						
						
						ComponentFor:
						//Adding a running sequence to the Key so as to match the BD Initialized Map key
						for(int i=1;i<10;i++)
						{
							// Local variable for Storing the Length 
							String dataVal="";
							// Check the Sequence value has a corresponding key in DB Map
							if(fielddataMap.get(mapKey+i)!=null)
							{
								// The Corresponding Bean Object is Fetched
								MsgField obj = fielddataMap.get(mapKey+i);
								System.out.println(leftTrimMapVal+"^^^^^^^");
								logger.info(mapKey+i + "\t" + obj.getFldCompFmt());
								//If there is no more value to extract for components and there is further mandatory component then validation fails
								if(leftTrimMapVal.length()<=0)
								{
									if(obj.getFldCompManOpt().equals("M"))
									{
										logger.info("Component is Mandatory for field " + obj.getFldNo());
										errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0018 +";"; // Mandatory field component missing 
										EventLogger.logEvent("NGPHFLDSVC0018", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
										validationStatus = false;
									}
									//Move to next field for both mandatory and optional anyway because there is no further value to extract
									continue ParentFor;
								}
								// Check if IsSlash Variable is true or not in DB  Initialized Pojo Object
								if(obj.isSlash()==true)
								{
									logger.info("ISSlash is true");
									String fstChrOfMapVal = leftTrimMapVal.substring(0,1);
									//check if first character is slash or not
									if(fstChrOfMapVal.equals("/"))
									{
										logger.info("Slash is present");
										//Remove the first slash to identify the next slash as EOC later
										leftTrimMapVal.delete(0, 1);
									}
									// if first char is not slash check for it is Mandatory or not
									else
									{
										logger.info("Slash is Absent");
										// Check whether the component is Mandatory
										if(obj.getFldCompManOpt().equals("M"))
										{
											logger.info("Component is Mandatory for field " + obj.getFldNo());
											errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0002 + ";"; // Mandatory component missing for field
											EventLogger.logEvent("NGPHFLDSVC0002", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
											validationStatus = false;
											continue ParentFor; // Exiting from Parent Labeled Loop, no need to validate for other fields
										}
										// Component is not Mandatory
										else
										{
											logger.info("Component is Optional");
											//if slash should be available by message format but not available in the message value and
											//this component is optionally left out in the message. In this situation, just move on to
											//the next component and not continue with below code since the below code might do 
											//validation that is actually not required for this component and it might fail.
											continue ComponentFor;
										}
									}
								}// Is Slash check If End
								
								//Check the value in the End of Component variable and if not null
								if(StringUtils.isNotBlank(obj.getEoCompIndctr()) && StringUtils.isNotEmpty(obj.getEoCompIndctr())&& obj.getEoCompIndctr()!=null)
								{
									// Fetches the value of the End Of Component Indicator and derives the field value based on it
									String eocIndVal = obj.getEoCompIndctr().trim();
									logger.info("EOC Indicator Value : " + eocIndVal);
									
									if(eocIndVal.equalsIgnoreCase("LENT"))
									{
										// Storing the particular component is a String
										if (leftTrimMapVal.toString().length() > obj.getLengthOfField())
										{
											dataVal = leftTrimMapVal.substring(0, obj.getLengthOfField());
										}
										else
										{ 
											dataVal = leftTrimMapVal.toString();
										}
										// Updating the Map Value to new Value after eliminating the Left Trim Characters
										leftTrimMapVal.delete(0, dataVal.length());	
									}
									else if(eocIndVal.equalsIgnoreCase("SLSH"))
									{
										// Storing the particular component is a String
										idxOfSlsh = leftTrimMapVal.indexOf("/");
										if (idxOfSlsh >= 0)
										{
											dataVal = leftTrimMapVal.substring(0, idxOfSlsh);
										}
										else
										{
											dataVal = leftTrimMapVal.toString();
										}
										// Updating the Map Value to new Value after eliminating the Left Trim Characters
										leftTrimMapVal.delete(0, dataVal.length());
										
										//Check for number of lines where the End Character is Slash (Special Case for SFMS 700 XXX)
										if(msgChnlType.equalsIgnoreCase("SFMS") && srcMsgType.equalsIgnoreCase("700")&& srcMsgSubType.equalsIgnoreCase("XXX") && (obj.getFldNo().equalsIgnoreCase("40E")))
										{
												if (mapValue.toString().contains("\r") || mapValue.toString().contains("\n"))
												{
													if(!mapValue.toString().contains("OTHR"))
													{
														String lines[] = mapValue.toString().split("\r\n");
														if(lines.length<=obj.getNoOfLines())
														{
															logger.info("Component has no of lines less than equal");
														}
														else
														{
															logger.info("No Of Lines did not Match for field " + obj.getFldNo());
															
															errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0004 +";"; // Numbers of lines does not match
															EventLogger.logEvent("NGPHFLDSVC0004", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
															validationStatus = false;
															continue ParentFor;
														}
													}													
											}
										}
									}
									else if(eocIndVal.equalsIgnoreCase("CRLF"))
									{
										/*
										 * Breaking the lines by Line Separator, in our case its /r/n
										 * We can split on \n, but then we are fixed to the line separator of *nix systems.
										 * If it happens that we have to parse on windows, splitting on \n won't work 
										 * (unless of course your string is hard-coded, which defeats the whole purpose of splitting)
										 */
										if(obj.getNoOfLines()>1)
										{
											idxOfCRLF = leftTrimMapVal.toString().lastIndexOf(NgphEsbConstants.NGPH_SFMS_CRLF);
										}
										else
										{
											idxOfCRLF = leftTrimMapVal.toString().indexOf(NgphEsbConstants.NGPH_SFMS_CRLF);
										}
										if (idxOfCRLF >= 0)
										{
											dataVal = leftTrimMapVal.substring(0, idxOfCRLF);
											// Updating the Map Value to new Value after eliminating the Left Trim Characters
											leftTrimMapVal.delete(0, idxOfCRLF + 2);
										}
										else
										{
											dataVal = leftTrimMapVal.toString();
											// Updating the Map Value to new Value after eliminating the Left Trim Characters
											leftTrimMapVal.delete(0, dataVal.length());
										}
										
										logger.info("Component Value (LeftTrimMap) : " + leftTrimMapVal);
										logger.info("Remaining Component Value (DataVal) : " + dataVal);
									}
								}
								else
								{
									dataVal = leftTrimMapVal.toString();
									leftTrimMapVal.delete(0, dataVal.length());
								}// EOC If End	
								//Added for LCBG-171 :: Starts :: dataVal starts with Special Characters validation 
								if(dataVal.startsWith(":") || dataVal.startsWith(";") || dataVal.startsWith(":"))
								{
									logger.info("Field Value Should not begins with Special Characters " + obj.getFldNo());
									errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0021 +";"; // Field Value validation fails
									EventLogger.logEvent("NGPHFLDSVC0010", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
									validationStatus = false;
									continue ParentFor;
								}
								//Added for LCBG-171 :: End :: dataVal starts with Special Characters validation 
								//dataVal arrived at, is blank and if the component is mandatory then format validation error							
								if(StringUtils.isBlank(dataVal) || StringUtils.isEmpty(dataVal) || dataVal==null || dataVal.length()<=0)
								{
									if(obj.getFldCompManOpt().equals("M"))
									{
										logger.info("Component is Mandatory for field " + obj.getFldNo());
										errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0018 +";"; // Mandatory field component missing 
										EventLogger.logEvent("NGPHFLDSVC0018", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
										validationStatus = false;
										continue ParentFor;
									}
								}
								//Check if no of lines in the Pojo object is greater than 1 
								if(obj.getNoOfLines()>1)
								{
									logger.info("No of Lines are more than 1");
									String[] lines = new String[1];
									logger.info("Value so far ******** " + dataVal);
									
									
										// Getting the Lines Array using separator as /r/n
										if (dataVal.toString().contains("\r") || dataVal.toString().contains("\n"))
										{
											lines = dataVal.toString().split("\r\n");
											logger.info(lines + "*&*&*&*&*&");
											// Check if lines Array Length is = the no of Lines in Pojo Object
											if(lines.length<=obj.getNoOfLines())
											{
												logger.info("Component has no of lines less than equal");
												
												// Extracting each line from the Actual Message
												for(int j=0;j<lines.length;j++)
												{
													String eachLine = lines[j];
													
													//check if the length of each line is less than or equal to the length in Pojo Object
													if(eachLine.length()<=obj.getLengthOfField())
													{
														logger.info("Lines Length in Original Message Matches");
													}
													else
													{
														logger.info("Line Length Validation fails for field " + obj.getFldNo());
														
														errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0005 +";"; // Line Length Validation failed
														EventLogger.logEvent("NGPHFLDSVC0005", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
														validationStatus = false;
														continue ParentFor;
													}
	
												}// For loop for Extracting String Ends
											}// If lines Array Length Loop Ends
											else
											{
												logger.info("No Of Lines did not Match for field " + obj.getFldNo());
												
												errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0004 +";"; // Numbers of lines does not match
												EventLogger.logEvent("NGPHFLDSVC0004", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
												validationStatus = false;
												continue ParentFor;
											}
										}
										else
										{
											//if there are no multiple lines in the message then the single line should be of the right length 
											if (leftTrimMapVal.length() > obj.getLengthOfField())
											{
												logger.info("Length of the field value greater than format specifications for field " + obj.getFldNo());
												
												errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0005 +";"; // Length of the field is greater
												EventLogger.logEvent("NGPHFLDSVC0005", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
												validationStatus = false;
												continue ParentFor;
											}
										}
									
								}/// If Loop for no of lines >1 ends
								//if no of lines is not greater then Check if the component value length is less than or equal to the length in the Pojo object
								//IT IS ASSUMED THAT IF THERE ARE MORE THAN 1 LINE THEN PLING WOULD NOT BE THERE. THIS HAS BEEN OBSERVED IN ALL MESSAGE FORMATS AS OF NOW
								else
								{
									if(StringUtils.isNotBlank(obj.getConsideration()) && StringUtils.isNotEmpty(obj.getConsideration())&& obj.getConsideration()!=null)
									{
										String cnsdrtn = obj.getConsideration();
										if(cnsdrtn.equalsIgnoreCase("IFS"))
										{	
											if(dataVal.length()<11)
											{
												logger.info("IFSC format Length is less than 11 for field " + obj.getFldNo());
												
												errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0020 +";"; // IFSC Format not Correct, Length is less for Field No {0}
												EventLogger.logEvent("NGPHFLDSVC0020", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
												validationStatus = false;
												continue ParentFor;
											}
											else
											{
												if (isThisValueIFSC(dataVal))
												{
													logger.info("IFSC format is Correct");
												}
												else
												{
													logger.info("IFSC format is NOT Correct for field " + obj.getFldNo());
													
													errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0006 +";"; // IFSC Format not Correct
													EventLogger.logEvent("NGPHFLDSVC0006", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
													validationStatus = false;
													continue ParentFor;
												}
											}
										}
										if(cnsdrtn.equalsIgnoreCase("BIC"))
										{
											if(dataVal.length()<8)
											{
												logger.info("BIC format Length is less than 8 for field " + obj.getFldNo());

												errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0020 +";"; // IFSC Format not Correct, Length is less for Field No {0}
												EventLogger.logEvent("NGPHFLDSVC0020", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
												validationStatus = false;
												continue ParentFor;
											
											}
											else
											{
												if (isThisValueBic(dataVal))
												{
													logger.info("BIC format is Correct");
												}
												else
												{
													logger.info("BIC format is NOT Correct for field " + obj.getFldNo());
													
													errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0007 +";"; // BIC Format not Correct
													EventLogger.logEvent("NGPHFLDSVC0007", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
													validationStatus = false;
													continue ParentFor;
												}
											}
										}
										if(cnsdrtn.equalsIgnoreCase("DAT"))
										{
											if((dataVal.length() == 6 && DateHelper.isValidDate(dataVal.substring(0,obj.getLengthOfField()), EnumDateFormat.SWIFT_FORMAT)) || (dataVal.length() == 8 && DateHelper.isValidDate(dataVal.substring(0,obj.getLengthOfField()), EnumDateFormat.RTGS_FORMAT)))
											{
												logger.info("Date format is Correct");
											}
											else
											{
												errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0001 +";"; // Date Format not correct
												EventLogger.logEvent("NGPHFLDSVC0001", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
												validationStatus = false;
												continue ParentFor;
											}
										}
									}
									// Check if is Pling is true
									else if(obj.isPling()==true)
									{
										logger.info("IsPling is present");
										logger.info("Pling value is ::"+dataVal);
										//check if component value length is equal to the length in Pojo Object
										if(dataVal.length()==obj.getLengthOfField())
										{
											logger.info("Length is equal");
										}
										else
										{
											// Check whether the component is Mandatory
											if(obj.getFldCompManOpt().equals("M"))
											{
												logger.info("Component is Mandatory for field " + obj.getFldNo());
												errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0016 +";"; // Minimum length required for the field component is not available 
												EventLogger.logEvent("NGPHFLDSVC0016", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
												validationStatus = false;
												continue ParentFor;
											}
											// Component is not Mandatory
											else
											{
												logger.info("Component is Optional");
												continue ComponentFor;
											}
										}
									}// Is Pling If Loop
									else
									{
										if(StringUtils.isNotBlank(dataVal) && StringUtils.isNotEmpty(dataVal)&& dataVal!=null)
										{
											if (dataVal.length()<=obj.getLengthOfField())
											{
												logger.info("Length is Less than or Equal to the Pojo Object Length");
											}
											else
											{
												logger.info("Length of the Line greater than Pojo Object for field " + obj.getFldNo());
												errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0003 +";"; // Line Length Validation failed
												EventLogger.logEvent("NGPHFLDSVC0003", MsgFieldValidator.class, msgRef, new String[]{obj.getFldNo()});
												validationStatus = false;
												continue ParentFor;
											}
										}
									}
								}
								//Check for CharSet
								if(obj.getCharType().equalsIgnoreCase("x"))
								{
									esbParserDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
									String xCharSet = esbParserDao.getInitialisedValue("XCHARSET");
									char [] charArray = dataVal.toCharArray();
									
									for(int j=0;j<charArray.length;j++)
									{
										if(xCharSet.contains(charArray[j] +"") || (charArray[j] +"").equalsIgnoreCase("\n") || (charArray[j] +"").equalsIgnoreCase("\r"))
										{}
										else
										{
											logger.info("X CharSet Validation failed for :" + charArray[j] + "with Key = " +  mapKey+ ", Value = " + mapValue );
											errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0010+";"; //X Character Set Validation Failed
											EventLogger.logEvent("NGPHFLDSVC0010", MsgFieldValidator.class, msgRef, new String[]{mapKey,mapValue});
											validationStatus = false;
											continue ParentFor;
										}
									}
								}
								else if(obj.getCharType().equalsIgnoreCase("z"))
								{
									esbParserDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
									String xCharSet = esbParserDao.getInitialisedValue("ZCHARSET");
									char [] charArray = dataVal.toCharArray();
									
									for(int j=0;j<charArray.length;j++)
									{
										if(xCharSet.contains(charArray[j] +"") || (charArray[j] +"").equalsIgnoreCase("\n") || (charArray[j] +"").equalsIgnoreCase("\r"))
										{}
										else
										{
											logger.info("Z CharSet Validation failed for : Key = " +  mapKey+ ", Value = " + mapValue);
											errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0015 +";"; //Z Character Set Validation Failed
											EventLogger.logEvent("NGPHFLDSVC0015", MsgFieldValidator.class, msgRef, new String[]{mapKey,mapValue});
											validationStatus = false;
											continue ParentFor;
										}
									}
								}
								else if(obj.getCharType().equalsIgnoreCase("a"))
								{
									for (char c : dataVal.toCharArray()) 
									{
									    if (Character.isUpperCase(c)) 
									    {}
									    else
									    {
											logger.info("a CharSet Validation failed for : Key = " +  mapKey+ ", Value = " + mapValue);
											errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0011 +";"; //a Character Set Validation Failed
											EventLogger.logEvent("NGPHFLDSVC0011", MsgFieldValidator.class, msgRef, new String[]{mapKey,mapValue});
											validationStatus = false;
											continue ParentFor;
									    }
									}
								}
								else if(obj.getCharType().equalsIgnoreCase("c"))
								{
									for (char c : dataVal.toCharArray()) 
									{
									    if (Character.isUpperCase(c) || Character.isDigit(c)) 
									    {}
									    else
									    {
											logger.info("c CharSet Validation failed for : Key = " +  mapKey+ ", Value = " + mapValue);
											errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0012+";"; //c Character Set Validation Failed
											EventLogger.logEvent("NGPHFLDSVC0012", MsgFieldValidator.class, msgRef, new String[]{mapKey,mapValue});
											validationStatus = false;
											continue ParentFor;
									    }
									}
								}
								else if(obj.getCharType().equalsIgnoreCase("n"))
								{
									for (char c : dataVal.toCharArray()) 
									{
									    if (Character.isDigit(c)) 
									    {}
									    else
									    {
											logger.info("n CharSet Validation failed for : Key = " +  mapKey+ ", Value = " + mapValue);
											errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0013+";"; //n Character Set Validation Failed
											EventLogger.logEvent("NGPHFLDSVC0013", MsgFieldValidator.class, msgRef, new String[]{mapKey,mapValue});
											validationStatus = false;
											continue ParentFor;
									    }
									}
								}
								else if(obj.getCharType().equalsIgnoreCase("d"))
								{
									try
									{
										if(dataVal.contains(","))
										{
											new BigDecimal(dataVal.replace(",", "."));
										}
										
									}
									catch (NumberFormatException f) 
									{
										logger.info("d CharSet Validation failed for : Key = " +  mapKey+ ", Value = " + mapValue);
										errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0014 +";"; //d Character Set Validation Failed
										EventLogger.logEvent("NGPHFLDSVC0014", MsgFieldValidator.class, msgRef, new String[]{mapKey,mapValue});
										validationStatus = false;
										continue ParentFor;
									}
									catch (Exception e) {
										logger.error(e);
										errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0014 +";"; //d Character Set Validation Failed
									}
								}
								// Check for code Words
								if(obj.getFldCodeWrds()!=null && StringUtils.isNotBlank(obj.getFldCodeWrds()) && StringUtils.isNotEmpty(obj.getFldCodeWrds()))
								{
									String codeWords[];
									boolean isKeyWordPrsnt = false;
									if(obj.getFldCodeWrds().contains(";"))
									{
										codeWords = obj.getFldCodeWrds().split(";");
									}
									else
									{
										codeWords = new String[]{obj.getFldCodeWrds()};
									}
									for(int j=0;j<codeWords.length;j++)
									{
										if(msgChnlType.equalsIgnoreCase("SFMS") && srcMsgType.equalsIgnoreCase("700")&& srcMsgSubType.equalsIgnoreCase("XXX") && (obj.getFldNo().equalsIgnoreCase("40E")))
										{
											if(dataVal.trim().equals(codeWords[j]))
											{
												logger.info("Code Word Validation Passed for : Key = " +  mapKey+ ", Value = " + mapValue + " amd Code Word : " + codeWords[j]);
												isKeyWordPrsnt = true;
												break;
											}
										}
										else
										{
											if(dataVal.trim().contains(codeWords[j]))
											{
												logger.info("Code Word Validation Passed for : Key = " +  mapKey+ ", Value = " + mapValue + " amd Code Word : " + codeWords[j]);
												isKeyWordPrsnt = true;
												break;
											}
										}
									}
									if(isKeyWordPrsnt == false)
									{
										logger.info("Code Word Validation failed for : Key = " +  mapKey+ ", Value = " + mapValue + " for Code Words : " + obj.getFldCodeWrds());
										errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0017+";"; //Code Word Validation Failed
										EventLogger.logEvent("NGPHFLDSVC0017", MsgFieldValidator.class, msgRef, new String[]{mapKey,mapValue});
										validationStatus = false;
										continue ParentFor;
									}
								}

							}// If Loop for checking the key+seq is present as a key in DB Map
							// There is no more key found has break here and move to next field value
							else
							{
								logger.info("Break as there is no key in the DB Map, Hence move to next key");
								break;
							}
						}//For Loop for running Sequence
					    
					}// If Loop to Check if Key value is not null
					else
					{
						logger.info("Value for : " + mapKey + " is null");
						errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0008 +";"; // Null value received for a field value
						EventLogger.logEvent("NGPHFLDSVC0008", MsgFieldValidator.class, msgRef, new String[]{mapKey});
						validationStatus = false;
						break;
					}
				}// If loop for checking key is null or not
				else
				{
					logger.info("Key is null");
					errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0009+";"; //  {0} Key is Null
					EventLogger.logEvent("NGPHFLDSVC0009", MsgFieldValidator.class, msgRef, new String[]{entry.getKey().toString()});
					validationStatus = false;
					break;
				}
				
			}//Parent For Loop
		}//try
		catch (Exception e) 
		{
			logger.error(e, e);
			errorMessage = errorMessage + NgphEsbConstants.NGPH_FLD0019+";"; // Exception Occured while performing MsgFld Validation as {0}
			EventLogger.logEvent("NGPHFLDSVC0019", MsgFieldValidator.class, msgRef, new String[]{e.getMessage()});
		}
		
		logger.info("Error Message Returned from MsgFiledVal is : " + errorMessage);
		return errorMessage;
	}
    private boolean isThisValueBic(String value)
    {
          if(value.contains(NgphEsbConstants.NGPH_CHAR_EOL))
          {
                //it means not a BIC
                return false;
          }else if(value.trim().length() >= NgphEsbConstants.NGPH_INT_EIGHT && value.trim().length()<= NgphEsbConstants.NGPH_INT_ELEVEN){

                //evaluating regex for alpha
                Pattern patternAlpha = Pattern.compile(NgphEsbConstants.NGPH_REGEX_ALPHA,Pattern.CASE_INSENSITIVE);   
                Matcher matcherAlpha = patternAlpha.matcher(value.trim().substring(NgphEsbConstants.NGPH_INT_ZERO, NgphEsbConstants.NGPH_INT_SIX));  
                
                //evaluating regex for alphanumeric
                Pattern patternAlphaNumeric = Pattern.compile(NgphEsbConstants.NGPH_REGEX_ALPHA_NUMERIC);
                Matcher matcherAlphaNumeric = patternAlphaNumeric.matcher(value.trim().subSequence(NgphEsbConstants.NGPH_INT_SIX, value.trim().length())); 
                
                //checking for 4!a & 2!a
                if(!matcherAlpha.matches())
                {
                      //it means not a BIC
                      return false;
                }
                
                //checking for 2!c & 3!c
                if(!matcherAlphaNumeric.matches())
                {
                      //it means not a BIC
                      return false;
                }
                
                //if not exist this is an invalid BIC/ not a BIC
          }else{
                //it means not a BIC
                return false;
          }
          return true;
    }
    private boolean isThisValueIFSC (String value)
    {
          if(value.contains(NgphEsbConstants.NGPH_CHAR_EOL))
          {
                //it means not a IFSC
                return false;
          }
          else if(value.trim().length() >= NgphEsbConstants.NGPH_INT_EIGHT && value.trim().length()<= NgphEsbConstants.NGPH_INT_ELEVEN)
          {
                //evaluating regex for alpha
                Pattern patternAlpha = Pattern.compile(NgphEsbConstants.NGPH_REGEX_ALPHA,Pattern.CASE_INSENSITIVE);   
                Matcher matcherAlpha = patternAlpha.matcher(value.trim().substring(NgphEsbConstants.NGPH_INT_ZERO, 4));  
                
                //evaluating regex for alphanumeric
                Pattern patternAlphaNumeric = Pattern.compile(NgphEsbConstants.NGPH_REGEX_ALPHA_NUMERIC);
                Matcher matcherAlphaNumeric = patternAlphaNumeric.matcher(value.trim().subSequence(5, value.trim().length())); 
                
                //checking for 4!a 
                if(!matcherAlpha.matches())
                {
                      //it means not a IFSC
                      return false;
                }
                
                //checking for 2!c & 3!c
                if(!matcherAlphaNumeric.matches())
                {
                      //it means not a IFSC
                      return false;
                }
                if (value.trim().charAt(4) != '0')
                {
                	return false;
                }
                //if not exist this is an invalid IFSC/ not a IFSC
          }else{
                //it means not a IFSC
                return false;
          }
          return true;
    }

	
	/*
	 * Code testing purpose
	 */
	public static void main(String[] args) 
	{/*
		ApplicationContextProvider.initializeContextProvider();
		new MsgFieldDataInitializer().getFileds();
		MsgFieldValidor obj1 = new MsgFieldValidor();
		
		Map<String,String> objMap = new HashMap<String, String>();
		objMap.put("20", "MTO-793-001481");
		objMap.put("13C", "BlaBlaBlaBla");
		objMap.put("32A", "110215INR300011,00");
		objMap.put("23B", "wlfgler;gjker");
		objMap.put("23E", "jweljhlrwe");
		objMap.put("26T", "jweilfjpl;");
		objMap.put("32A", "a");
		objMap.put("33B", "a");
		objMap.put("36", "a");
		objMap.put("50A", "44442222");
		objMap.put("51A", "/a");
		objMap.put("52A", "C44442222");
		objMap.put("53A", "a");
		objMap.put("54A", "a");
		objMap.put("55A", "a");
		objMap.put("56A", "a");
		objMap.put("57A", "a");
		objMap.put("59", "a");// with A or no letter
		objMap.put("70", "a");
		objMap.put("71A", "a");
		objMap.put("71F", "a");
		objMap.put("71G", "a");
		objMap.put("72", "a");
		objMap.put("77B", "a");
		objMap.put("77T", "a");
		
		obj1.validate_Msg_Fields(objMap);
		Iterator i = obj1.fieldList.iterator();
		
		MsgField obj = null;
		 while (i.hasNext()) 
	        {
			 	obj = (MsgField)i.next();
	        	
			 	logger.info("Field Component Format--> " + obj.getFldCompFmt());
			 	logger.info("Field Component Mandatory/Optional -> "+ obj.getFldCompManOpt());
			 	logger.info("Field Comp Seq-> " + obj.getFldCompSeq());
			 	logger.info("Field No--> " + obj.getFldNo());
			 	logger.info("Length of Field--> " + obj.getLengthOfField());
			 	logger.info("No of Lines Present in field--> " + obj.getNoOfLines());
			 	logger.info("Is Slash Val--> " + obj.isSlash());
			 	logger.info("Is Pling Val-> " + obj.isPling());
			 	logger.info("Charater type -> " + obj.getCharType());
			 	
			 	logger.info("**********************************************************************");
	        	
	        }
		
	*/}


}
