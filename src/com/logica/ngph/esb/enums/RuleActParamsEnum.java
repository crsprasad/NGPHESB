/**
 * 
 */
package com.logica.ngph.esb.enums;

/**
 * @author mohdabdulaa
 *
 */
public enum RuleActParamsEnum {
	
	SWIFT("Swift"),
	RTGS("in-rtgs"),
	NEFT("in-neft");
	
	String actParamName;
	
	RuleActParamsEnum(String action)
	{
		this.actParamName = action;
	}
	
	/**
	 * 
	 */
	public static RuleActParamsEnum findRuleActParamsEnumByName(String actParam)
	{
		for(RuleActParamsEnum oneEnumSample :RuleActParamsEnum.values())
		{
			if(oneEnumSample.actParamName.equalsIgnoreCase(actParam))
				return oneEnumSample;
	
		}
		return null;
		
	}

}
