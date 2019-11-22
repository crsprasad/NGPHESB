package com.logica.ngph.esb.servicesImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.enums.ValidateServiceRuleCodeEnum;
import com.logica.ngph.esb.services.NetworkValidationService;
import com.logica.ngph.utils.ApplicationContextProvider;

public class NetworkValidationServiceImpl implements NetworkValidationService{

	static Logger logger = Logger.getLogger(NetworkValidationServiceImpl.class);
	
	private EsbServiceDao esbServiceDao;
	
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}
	
	private boolean IS_VALIDATION_FAIL = false;
	private String ERROR_CODE = null;
	
	public String validateNetworkRules(Map<String, String> fieldMap, String msgType, String subMsgType, String hostId) throws Exception
	{
			//esbServiceDao = (EsbServiceDao) ApplicationContextProvider.getBean("esbServiceDao");
			List<String> validationsOrder = esbServiceDao.getValidateServiceRulesOrder(msgType, subMsgType,hostId);
			logger.info("Network Rules Fetched for MsgType : " + msgType +" and subMsgType : " + subMsgType + " and Host Id : " + hostId + " are : " + validationsOrder);
		
			if(validationsOrder!=null && !(validationsOrder.isEmpty()))
			{
				for (String config : validationsOrder) 
				{
					ValidateServiceRuleCodeEnum vsr = ValidateServiceRuleCodeEnum.findValidateServiceRuleCodeEnumByCode(config);

					switch (vsr) 
					{
					case VSR0039:
						 call39(fieldMap);
						break;
					case VSR0040:
						 call40(fieldMap);
						break;
					case VSR0041:
						 call41(fieldMap);
						break;
					case VSR0042:
						 call42(fieldMap);
						break;
					case VSR0043:
						 call43(fieldMap);
						break;
					case VSR0044:
						 call44(fieldMap);
						break;
					case VSR0045:
						 call45(fieldMap);
						break;
					case VSR0046:
						 call46(fieldMap);
						break;
					case VSR0047:
						 call47(fieldMap);
						break;
					case VSR0048:
						 call48(fieldMap);
						break;
					case VSR0049:
						 call49(fieldMap);
						break;
					case VSR0050:
						 call50(fieldMap);
						break;
					case VSR0051:
						 call51(fieldMap);
						break;
					case VSR0052:
						 call52(fieldMap);
						break;
					case VSR0053:
						 call53(fieldMap);
						break;
					case VSR0054:
						 call54(fieldMap);
						break;
					case VSR0055:
						 call55(fieldMap);
						break;
					case VSR0056:
						 call56(fieldMap);
						break;
					case VSR0057:
						 call57(fieldMap);
						break;
					case VSR0058:
						 call58(fieldMap);
						break;
					case VSR0059:
						 call59(fieldMap);
						break;
					case VSR0060:
						 call60(fieldMap);
						break;
					case VSR0061:
						 call61(fieldMap);
						break;
					case VSR0062:
						 call62(fieldMap);
						break;
					case VSR0063:
						 call63(fieldMap);
						break;
					case VSR0067:
						call67(fieldMap);
						break;
					case VSR0070:
						call70(fieldMap);
						break;
					case VSR0071:
						call71(fieldMap);
						break;
					//Start :: Added for BG COV messages
					case VSR0072:
						call72(fieldMap);
						break;
					case VSR0073:
						call73(fieldMap);
						break;
					//End :: Added for BG COV messages	
					default:
						break;
					}
					//if validation error occurs then break the loop
					if (IS_VALIDATION_FAIL) 
					{
						logger.error("Network Validation fails for..."+"NetworkValidationRule : "+config+",Error/Warning Code: "+ERROR_CODE);
						break;
					}
				}
		   }else
		   {
			   logger.warn("No Network Rules configured for MsgType : " + msgType +" and subMsgType : " + subMsgType + " and Host Id : " + hostId);
		   }
		return ERROR_CODE;
	}
	private void call70(Map<String, String> fieldMap) throws Exception
	{
		logger.info("call70 START");
		boolean isValid = true;
		
		String fldval = fieldMap.get("32B");
		String curr = fldval.substring(0, 3);
		String IntegerPart = fldval.substring(curr.length(), fldval.indexOf(","));
		String decVal = fldval.substring(fldval.indexOf(",")+1, fldval.length());
		
		BigDecimal result = esbServiceDao.getCurrDecimal(curr);
		
		 // The integer part of Amount must contain at least one digit.
		 if(IntegerPart.length()==0)
		 {
			isValid = false;
		 }
		 // The decimal comma ',' is mandatory.
		 if(!fldval.contains(","))
		 {
			isValid = false;
		 }
		 //The number of digits following the comma must not exceed the maximum number allowed for the specified currency
		 if(decVal.length() <= result.intValue())
		 {}
		 else
		 {
			isValid = false;
		 }
			 
		if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0028;//The integer part of Amount must contain at least one digit. The decimal comma ',' is mandatory.The number of digits following the comma must not exceed the maximum number allowed for the specified currency
				IS_VALIDATION_FAIL = true;
				logger.info("call70 Validation Failed");
			}
		logger.info("call70 END");
	}
	private void call67(Map<String, String> fieldMap) throws Exception
	{
		logger.info("call67 START");
		boolean isValid = true;
		String fldVal = fieldMap.get("40E");
		
		if(StringUtils.isNotBlank(fldVal) && StringUtils.isNotEmpty(fldVal)&& fldVal.contains("OTHR") && fldVal.length()<=30)
		{}
		else
		{
			isValid = false;
		}

		if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0027;//Subfield 2 of field 40E, i.e, /35x, is only allowed when subfield 1 of this field consists of OTHR
				IS_VALIDATION_FAIL = true;
				logger.info("call67 Validation Failed");
			}
		logger.info("call67 END");
	}
	private void call39(Map<String, String> fieldMap)
	{
		logger.info("call39 START");
	
			if(fieldMap.containsKey("39A") && fieldMap.containsKey("39B"))
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0001;//Either field 39A or 39B, but not both, may be present
				IS_VALIDATION_FAIL = true;
				logger.info("call39 Validation Failed");
			}
		logger.info("call39 END");
	}
	
	private void call40(Map<String, String> fieldMap)
	{
		logger.info("call40 START");
	
			if(fieldMap.containsKey("42C") && (fieldMap.containsKey("42A") || fieldMap.containsKey("42D")))
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0002;//When used, fields 42C and 42a must both be present
				IS_VALIDATION_FAIL = true;
				logger.info("call40 Validation Failed");
			}
		logger.info("call40 END");
	}
	
	private void call41(Map<String, String> fieldMap)
	{
		logger.info("call41 START");
		boolean is42Cprsnt = false;
		boolean is42aprsnt = false;
		boolean is42Mprsnt = false;
		boolean is42Pprsnt = false;
		
		if(fieldMap.containsKey("42C"))
		{
			is42Cprsnt = true;
		}
		if(fieldMap.containsKey("42A") || fieldMap.containsKey("42D"))
		{
			is42aprsnt = true;
		}
		if(fieldMap.containsKey("42M"))
		{
			is42Mprsnt = true;
		}
		if(fieldMap.containsKey("42P"))
		{
			is42Pprsnt = true;
		}
		
		if(is42Cprsnt == true && is42aprsnt == true && is42Mprsnt == false && is42Pprsnt == false)
		{}
		else if(is42Cprsnt == false && is42aprsnt == false && is42Mprsnt == true && is42Pprsnt == false)
		{}
		else if(is42Cprsnt == false && is42aprsnt == false && is42Mprsnt == false && is42Pprsnt == true)
		{}
		else
		{
			ERROR_CODE = NgphEsbConstants.NGPH_NVS0003;//Either fields 42C and 42a together, or field 42M alone, or field 42P alone may be present. No other combination of these fields is allowed
			IS_VALIDATION_FAIL = true;
			logger.info("call41 Validation Failed");
		}
		logger.info("call41 END");
	}

	private void call42(Map<String, String> fieldMap)
	{
		logger.info("call42 START");
	
			if(fieldMap.containsKey("44C") && fieldMap.containsKey("44D"))
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0004;//Either field 44C or 44D, but not both, may be present
				IS_VALIDATION_FAIL = true;
				logger.info("call42 Validation Failed");
			}
		logger.info("call42 END");
	}
	private void call43(Map<String, String> fieldMap)
	{
		logger.info("call43 START");
	
			if(fieldMap.containsKey("32B") || fieldMap.containsKey("33B"))
			{
				if(fieldMap.containsKey("34B"))
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0005;//If either field 32B or 33B is present, field 34B must also be present
					IS_VALIDATION_FAIL = true;
					logger.info("call43 Validation Failed");
				}

			}
		logger.info("call43 END");
	}
	private void call44(Map<String, String> fieldMap)
	{
		logger.info("call44 START");
	
			if(fieldMap.containsKey("34B")) 
			{
				if(fieldMap.containsKey("32B") || fieldMap.containsKey("33B"))
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0006;//If field 34B is present, either field 32B or 33B must also be present
					IS_VALIDATION_FAIL = true;
					logger.info("call44 Validation Failed");
				}

			}
		logger.info("call44 END");
	}
	private void call45(Map<String, String> fieldMap)
	{
		logger.info("call45 START");
	
			if(fieldMap.containsKey("23"))
			{
				if(fieldMap.containsKey("52A") || fieldMap.containsKey("52D"))
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0007;//If field 23 is present, field 52a must also be present
					IS_VALIDATION_FAIL = true;
					logger.info("call45 Validation Failed");
				}

			}
		logger.info("call45 END");
	}
	private void call46(Map<String, String> fieldMap)
	{
		logger.info("call46 START");
	
			if(fieldMap.containsKey("31E") || fieldMap.containsKey("32B") || fieldMap.containsKey("33B") || fieldMap.containsKey("34B") || fieldMap.containsKey("39A") || fieldMap.containsKey("39B") || fieldMap.containsKey("39C") || fieldMap.containsKey("44A") || fieldMap.containsKey("44E") || fieldMap.containsKey("44F") || fieldMap.containsKey("44B") || fieldMap.containsKey("44C") || fieldMap.containsKey("44D") || fieldMap.containsKey("79") || fieldMap.containsKey("72"))
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0008;//At least one of the fields 31E, 32B, 33B, 34B, 39A, 39B, 39C, 44A, 44E, 44F, 44B, 44C, 44D, 79 or 72 must be present
				IS_VALIDATION_FAIL = true;
				logger.info("call46 Validation Failed");
			}
		logger.info("call46 END");
	}
	
	private void call47(Map<String, String> fieldMap)
	{
		logger.info("call47 START");
	
		String curr32B = null;
		String curr33B = null;
		String curr34B = null;
		
			if(fieldMap.containsKey("32B") && fieldMap.containsKey("33B") && fieldMap.containsKey("34B"))
			{
				curr32B = fieldMap.get("32B").substring(0, 3);
				curr33B = fieldMap.get("33B").substring(0, 3);
				curr34B = fieldMap.get("34B").substring(0, 3);
				
				if(curr32B.equalsIgnoreCase(curr33B) && curr32B.equalsIgnoreCase(curr34B) && curr33B.equalsIgnoreCase(curr34B))
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0009;//The currency code in the amount fields 32B, 33B, and 34B must be the same
					IS_VALIDATION_FAIL = true;
					logger.info("call47 Validation Failed");
				}
			}
			else if(fieldMap.containsKey("32B") && fieldMap.containsKey("33B"))
			{
				curr32B = fieldMap.get("32B").substring(0, 3);
				curr33B = fieldMap.get("33B").substring(0, 3);
				
				if(curr32B.equalsIgnoreCase(curr33B) )
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0009;//The currency code in the amount fields 32B, 33B, and 34B must be the same
					IS_VALIDATION_FAIL = true;
					logger.info("call47 Validation Failed");
				}
			}
			else if(fieldMap.containsKey("32B") && fieldMap.containsKey("34B"))
			{
				curr32B = fieldMap.get("32B").substring(0, 3);
				curr34B = fieldMap.get("34B").substring(0, 3);
				
				if(curr32B.equalsIgnoreCase(curr34B) )
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0009;//The currency code in the amount fields 32B, 33B, and 34B must be the same
					IS_VALIDATION_FAIL = true;
					logger.info("call47 Validation Failed");
				}
			}
			else if(fieldMap.containsKey("33B") && fieldMap.containsKey("34B"))
			{
				curr33B = fieldMap.get("33B").substring(0, 3);
				curr34B = fieldMap.get("34B").substring(0, 3);
				
				if(curr33B.equalsIgnoreCase(curr34B) )
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0009;//The currency code in the amount fields 33B and 34B must be the same
					IS_VALIDATION_FAIL = true;
					logger.info("call47 Validation Failed");
				}
			}

			logger.info("call47 END");
	}
	private void call48(Map<String, String> fieldMap)
	{
		logger.info("call48 START");
	
			if(fieldMap.containsKey("50B") && (fieldMap.containsKey("52A") ||fieldMap.containsKey("52D"))) 
			{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0010;//Either field 52a "Issuing Bank" or field 50B "Non-Bank Issuer", but not both, must be present
					IS_VALIDATION_FAIL = true;
					logger.info("call48 Validation Failed");
			}

		logger.info("call48 END");
	}
	private void call49(Map<String, String> fieldMap)
	{
		logger.info("call49 START");
	
			if(fieldMap.containsKey("25") && (fieldMap.containsKey("57A") ||fieldMap.containsKey("57D"))) 
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0011;//Either field 25 or 57a, but not both, may be present
				IS_VALIDATION_FAIL = true;
				logger.info("call49 Validation Failed");
			}
		logger.info("call49 END");
	}
	private void call50(Map<String, String> fieldMap)
	{
		logger.info("call50 START");
	
			if(fieldMap.containsKey("32D") && (fieldMap.containsKey("57A") ||fieldMap.containsKey("57D"))) 
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0012;//If field 32D is present, field 57a must not be present
				IS_VALIDATION_FAIL = true;
				logger.info("call50 Validation Failed");
			}
		logger.info("call50 END");
	}
	private void call51(Map<String, String> fieldMap)
	{
		logger.info("call51 START");
	
			if(fieldMap.containsKey("73")) 
			{
				if(fieldMap.containsKey("33A") || fieldMap.containsKey("33B"))
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0013;//If field 73 is present, field 33a must also be present
					IS_VALIDATION_FAIL = true;
					logger.info("call51 Validation Failed");
				}
			}
		logger.info("call51 END");
	}
	private void call52(Map<String, String> fieldMap)
	{
		logger.info("call52 START");
	
		String curr32A = null;
		String curr33a = null;
		
			if(fieldMap.containsKey("32A") && (fieldMap.containsKey("33B") || fieldMap.containsKey("33A")))
			{
				curr32A = fieldMap.get("curr32A").substring(6, 9);

				if(fieldMap.containsKey("33A"))
				{
					curr33a = fieldMap.get("33A").substring(6, 9);
					
					if(curr32A.equalsIgnoreCase(curr33a))
					{}
					else
					{
						ERROR_CODE = NgphEsbConstants.NGPH_NVS0014;//The currency code in the amount fields 32A and 33a must be the same
						IS_VALIDATION_FAIL = true;
						logger.info("call52 Validation Failed");
					}
				}
				if(fieldMap.containsKey("33B"))
				{
					curr33a = fieldMap.get("33B").substring(0, 3);
					
					if(curr32A.equalsIgnoreCase(curr33a))
					{}
					else
					{
						ERROR_CODE = NgphEsbConstants.NGPH_NVS0014;//The currency code in the amount fields 32A and 33a must be the same
						IS_VALIDATION_FAIL = true;
						logger.info("call52 Validation Failed");
					}

				}
			}
				
		logger.info("call52 END");
	}
	private void call53(Map<String, String> fieldMap)
	{
		logger.info("call53 START");
	
			if(fieldMap.containsKey("59") && (fieldMap.containsKey("58A") || fieldMap.containsKey("58D"))) 
			{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0015;//Either field 58a or 59, but not both, may be present
					IS_VALIDATION_FAIL = true;
					logger.info("call53 Validation Failed");
			}
		logger.info("call53 END");
	}
	private void call54(Map<String, String> fieldMap)
	{
		logger.info("call54 START");
	
			if(fieldMap.containsKey("31E") || fieldMap.containsKey("32B") || fieldMap.containsKey("33B") || fieldMap.containsKey("34B") || fieldMap.containsKey("39A") || fieldMap.containsKey("39B") || fieldMap.containsKey("39C") || fieldMap.containsKey("72") || fieldMap.containsKey("77A"))
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0016;//At least one of the fields 31E, 32B, 33B, 34B, 39A, 39B, 39C, 72 or 77A must be present 
				IS_VALIDATION_FAIL = true;
				logger.info("call54 Validation Failed");
			}

		logger.info("call54 END");
	}
	private void call55(Map<String, String> fieldMap)
	{
		logger.info("call55 START");
	
			if(fieldMap.containsKey("33B") || fieldMap.containsKey("71B") || fieldMap.containsKey("73")) 
			{
				if(fieldMap.containsKey("34B") )
				{}
				else
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0017;//If field 33B and/or field 71B and/or field 73 is/are present, field 34B must also be present
					IS_VALIDATION_FAIL = true;
					logger.info("call55 Validation Failed");
				}

			}
		logger.info("call55 END");
	}
	private void call56(Map<String, String> fieldMap)
	{
		logger.info("call56 START");
	
		if(fieldMap.containsKey("32B") && fieldMap.containsKey("34B"))
		{
			String curr32B = fieldMap.get("32B").substring(0, 3);
			String curr34B = fieldMap.get("34B").substring(0, 3);
			
			if(curr32B.equalsIgnoreCase(curr34B) )
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0018;//The currency code in the amount fields 32B and 34B must be the same
				IS_VALIDATION_FAIL = true;
				logger.info("call47 Validation Failed");
			}
		}
		logger.info("call56 END");
	}
	private void call57(Map<String, String> fieldMap)
	{
		logger.info("call57 START");
	
		if(fieldMap.containsKey("32B")&& fieldMap.containsKey("71B"))
		{
			if(fieldMap.containsKey("33A") || fieldMap.containsKey("33B"))
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0019;//If fields 32B and 71B are both present, then field 33a must also be present
				IS_VALIDATION_FAIL = true;
				logger.info("call57 Validation Failed");
			}
		}
		logger.info("call57 END");
	}
	private void call58(Map<String, String> fieldMap)
	{
		logger.info("call58 START");
	
		String curr32B = null;
		String curr33a = null;
		
			if(fieldMap.containsKey("32B") && (fieldMap.containsKey("33B") || fieldMap.containsKey("33A")))
			{
				curr32B = fieldMap.get("32B").substring(0, 3);

				if(fieldMap.containsKey("33A"))
				{
					curr33a = fieldMap.get("33A").substring(8, 11);
					
					if(curr32B.equalsIgnoreCase(curr33a))
					{}
					else
					{
						ERROR_CODE = NgphEsbConstants.NGPH_NVS0020;//The currency code in the amount fields 32B and 33a must be the same
						IS_VALIDATION_FAIL = true;
						logger.info("call58 Validation Failed");
					}
				}
				else if(fieldMap.containsKey("33B"))
				{
					curr33a = fieldMap.get("33B").substring(0, 3);
					
					if(curr32B.equalsIgnoreCase(curr33a))
					{}
					else
					{
						ERROR_CODE = NgphEsbConstants.NGPH_NVS0020;//The currency code in the amount fields 32B and 33a must be the same
						IS_VALIDATION_FAIL = true;
						logger.info("call58 Validation Failed");
					}

				}
			}
		logger.info("call58 END");
	}
	private void call59(Map<String, String> fieldMap)
	{
		logger.info("call59 START");
		boolean is72Prsnt =false;
		boolean is77aPrsnt =false;
		
		if(fieldMap.containsKey("72"))
		{
			is72Prsnt = true;
		}
		if(fieldMap.containsKey("77A"))
		{
			is77aPrsnt = true;
		}
		if(is72Prsnt == true && is77aPrsnt == true)
		{
			ERROR_CODE = NgphEsbConstants.NGPH_NVS0021;//Either field 72 or 77A may be present, but not both
			IS_VALIDATION_FAIL = true;
			logger.info("call59 Validation Failed");
		}
		logger.info("call59 END");
	}
	
	private void call60(Map<String, String> fieldMap)
	{
		logger.info("call60 START");
		boolean is53aPrsnt =false;
		boolean is57aPrsnt =false;
		
		if(fieldMap.containsKey("53A") || fieldMap.containsKey("53B") || fieldMap.containsKey("53D"))
		{
			is53aPrsnt =true;
		}
		if(fieldMap.containsKey("57A") || fieldMap.containsKey("57B") || fieldMap.containsKey("57D"))
		{
			is57aPrsnt =true;
		}

		if(is53aPrsnt ==true && is57aPrsnt ==true)
		{
			ERROR_CODE = NgphEsbConstants.NGPH_NVS0022;//Either field 53a or 57a may be present, but not both
			IS_VALIDATION_FAIL = true;
			logger.info("call60 Validation Failed");
		}
		logger.info("call60 END");
	}

	private void call61(Map<String, String> fieldMap)
	{
		logger.info("call61 START");
		String curr32a = null;
		String curr34a = null;
			
		if(fieldMap.containsKey("32A"))
		{
			curr32a =fieldMap.get("32A").substring(8, 11);
		}
		if(fieldMap.containsKey("32B"))
		{
			curr32a =fieldMap.get("32B").substring(0, 3);
		}
		if(fieldMap.containsKey("34A"))
		{
			curr34a =fieldMap.get("34A").substring(8, 11);
		}
		if(fieldMap.containsKey("34B"))
		{
			curr34a =fieldMap.get("32B").substring(0, 3);
		}
		if(curr32a!= null && curr34a!= null)
		{
			if(curr32a.equalsIgnoreCase(curr34a))
				{}
			else 
				{
					ERROR_CODE = NgphEsbConstants.NGPH_NVS0023;//The currency code in the amount fields 32a and 34a must be the same
					IS_VALIDATION_FAIL = true;
					logger.info("call61 Validation Failed");
				}	
		}
		else if(curr32a!= null && curr34a == null)
			{}
		else
			{
				logger.info("Message field values are empty");
			}
			
		
		
		logger.info("call61 END");
	}
	
	private void call62(Map<String, String> fieldMap)
	{
		logger.info("call62 START");

		if(fieldMap.containsKey("71B"))
		{
			if(fieldMap.containsKey("32B") || fieldMap.containsKey("32D"))
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0024;//If field 71B is present, field 32a must also be present
				IS_VALIDATION_FAIL = true;
				logger.info("call62 Validation Failed");
			}
		}
		logger.info("call62 END");
	}
	private void call63(Map<String, String> fieldMap)
	{
		logger.info("call63 START");

			if(fieldMap.containsKey("33B") && fieldMap.containsKey("39C"))
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0025;//Either field 33B or field 39C, but not both, must be present
				IS_VALIDATION_FAIL = true;
				logger.info("call63 Validation Failed");
			}
			else
				{}
		logger.info("call63 END");
	}
	
	private void call71(Map<String, String> fieldMap) throws Exception
	{
		logger.info("call71 START");
		boolean isValid = true;
		String fldVal = fieldMap.get("40C");
		
		if(StringUtils.isNotBlank(fldVal) && StringUtils.isNotEmpty(fldVal)&& fldVal.contains("OTHR") && fldVal.length()<=35)
		{}
		else
		{
			isValid = false;
		}

		if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0029;//Subfield 2 of field 40C, ie, /35x, is only allowed when subfield 1 of this field consists of OTHR
				IS_VALIDATION_FAIL = true;
				logger.info("call71 Validation Failed");
			}
		logger.info("call71 END");
	}
	
	private void call72(Map<String, String> fieldMap) throws Exception
	{
		logger.info("call72 START");
		String fidValue = fieldMap.get("7040");	
		if((fieldMap.get("7040")!=null) && fidValue.equalsIgnoreCase("Y"))
		{
			if(fieldMap.containsKey("7043") && fieldMap.containsKey("7044") && fieldMap.containsKey("7045") && fieldMap.containsKey("7046"))
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0030;//If field 7040 is Y, fields 7043, 7044, 7045 and 7046 must also be present
				IS_VALIDATION_FAIL = true;
				logger.info("call72 Validation Failed");
			}
		}
		logger.info("call72 END");
	}
	
	private void call73(Map<String, String> fieldMap) throws Exception
	{
		logger.info("call73 START");
		String fidValue = fieldMap.get("7048");	
		if((fieldMap.get("7048")!=null) && fidValue.equalsIgnoreCase("Y"))
		{
			if(fieldMap.containsKey("7049"))
			{}
			else
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NVS0031;//If field 7048 is Y, field 7049 must also be present
				IS_VALIDATION_FAIL = true;
				logger.info("call73 Validation Failed");
			}
		}
		logger.info("call73 END");
	}
}
	

