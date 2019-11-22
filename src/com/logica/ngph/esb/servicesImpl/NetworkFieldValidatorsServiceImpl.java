package com.logica.ngph.esb.servicesImpl;

import org.apache.log4j.Logger;

import com.logica.ngph.common.enums.EnumDateFormat;
import com.logica.ngph.common.utils.DateHelper;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.enums.ValidateServiceRuleCodeEnum;
import com.logica.ngph.esb.services.NetworkFieldValidatorsService;
import com.logica.ngph.utils.ApplicationContextProvider;

/**
 * @author guptarb
 * A Generic Service Class for common field Validation functions.
 */
public class NetworkFieldValidatorsServiceImpl implements NetworkFieldValidatorsService {

	static Logger logger = Logger.getLogger(NetworkFieldValidatorsServiceImpl.class);
	
	private boolean IS_VALIDATION_FAIL = false;
	private String ERROR_CODE = null;
	EsbServiceDao esbServiceDao;
	
	public String validatefieldRules(String fldVal, String ruleCode) throws Exception
	{
		logger.info("validatefieldRules Starts");
		
		String fldCodeArr[] = null;
		//Check if the codes Value is comma Separated Value
		if(ruleCode!=null && ruleCode.contains(";"))
		{
			fldCodeArr = ruleCode.split(";");
		}
		else
		{
			fldCodeArr = new String[]{ruleCode};
		}
		
		//Iterate over the array
		for (String config : fldCodeArr) 
		{
			ValidateServiceRuleCodeEnum vsr = ValidateServiceRuleCodeEnum.findValidateServiceRuleCodeEnumByCode(config);

			switch (vsr) 
			{
				case VSR0064:
					call64(fldVal);
					break;
				case VSR0065:
					call65(fldVal);
					break;
				case VSR0066:
					call66(fldVal);
					break;
				case VSR0068:
					call68(fldVal);
					break;
				case VSR0069:
					call69(fldVal);
					break;
				default:
					break;
			}
			//if validation error occurs then break the loop
			if (IS_VALIDATION_FAIL) 
			{
				logger.error("Network Field Validation fails for validatefieldRules : "+config+",Error/Warning Code: "+ERROR_CODE);
				break;
			}
		}
		logger.info("validatefieldRules Ends");
		return ERROR_CODE;
	}
	
	private void call69(String fldVal) throws Exception
	{
		logger.info("call69 START");
		boolean isValid = true;
		
		esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
		int result = esbServiceDao.getCntCurrCode(fldVal);
		
		if(result>0)
		{}
		else
		{
			isValid = false;
		}

		if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NFV0005;//Currency must be a valid ISO 4217 currency code
				IS_VALIDATION_FAIL = true;
				logger.info("call69 Validation Failed");
			}
		logger.info("call69 END");
	}
	
	private void call68(String fldVal) throws Exception
	{
		logger.info("call68 START");
		boolean isValid = true;
		
		esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
		int result = esbServiceDao.validateBIC(fldVal);
		
		if(result>0)
		{}
		else
		{
			isValid = false;
		}

		if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NFV0004;//The BIC must be a SWIFT registered BIC
				IS_VALIDATION_FAIL = true;
				logger.info("call68 Validation Failed");
			}
		logger.info("call68 END");
	}
	
	private void call66(String fldVal) throws Exception
	{
		logger.info("call66 START");
		boolean isValid = true;
		
		if((fldVal.length() == 6 && DateHelper.isValidDate(fldVal, EnumDateFormat.SWIFT_FORMAT)))
		{}
		else
		{
			isValid = false;
		}

		if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NFV0003;//Date must contain a valid date expressed as YYMMDD
				IS_VALIDATION_FAIL = true;
				logger.info("call66 Validation Failed");
			}
		logger.info("call66 END");
	}
	
	private void call65(String fldVal)
	{
		logger.info("call65 START");
		boolean isValid = true;
		if(fldVal.contains("PREADV"))
		{
				int searchIndex = fldVal.indexOf("PREADV") + 6;
				if((fldVal.charAt(searchIndex)+"").equalsIgnoreCase("/"))
				{}
				else
				{
					isValid = false;
				}
		}
		else
		{
			isValid = false;
		}
		
		if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NFV0002;//This field must contain the code PREADV followed by a slash '/' and a reference to the pre-advice
				IS_VALIDATION_FAIL = true;
				logger.info("call65 Validation Failed");
			}
		logger.info("call65 END");
	}
	
	private void call64(String fldVal)
	{
		logger.info("call64 START");
		boolean isValid = true;
		
		//Start and end SLash
		if(fldVal.startsWith("/") || fldVal.endsWith("/"))
		{
			isValid = false;
		}

		//If not Start and End Slash, check in between consecutive slashes
		if(isValid == true)
		{
			char arr[] = fldVal.toCharArray();
			for(int i=0;i<arr.length;i++)
			{
				if((arr[i]+"").equalsIgnoreCase("/"))
				{
					int nextVal = i+1;
					if(nextVal<arr.length)
					{
						if((arr[nextVal]+"").equalsIgnoreCase("/"))
						{
							isValid = false;
						}
					}
				}
			}
		}
			if(isValid = false)
			{
				ERROR_CODE = NgphEsbConstants.NGPH_NFV0001;//This field must not start or end with a slash '/' and must not contain two consecutive slashes '//'
				IS_VALIDATION_FAIL = true;
				logger.info("call64 Validation Failed");
			}
		logger.info("call64 END");
	}
}
