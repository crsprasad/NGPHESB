package com.logica.ngph.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logica.ngph.esb.constants.NgphEsbConstants;

public class PaymentsHelper {
	
	/**
	 * This method checks whether the inputted value is BIC or not
	 * based on format 4!a2!a2!c[3!c]. If it is BIC then returns tre else false
	 * @param value
	 * @return
	 */
	public static boolean isBic(String value)
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
			
		}else{
			//it means not a BIC
			return false;
		}
		return true;
	}
	public static boolean isThisValueIFSC (String value)
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

}
