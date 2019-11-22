/**
 * 
 */
package com.logica.ngph.esb.enums;

/**
 * @author mohdabdulaa
 *
 */
public enum RuleActionsEnum {
	
	MOVETO("moveTo"),
	SENDTO("SendTo"),
	ASSIGN("assign"),
	PREFIX("prefix"),
	SUFFIX("suffix"),
	INSERTAT("insertAt"),
	REMOVEFIRST("removeFirst"),
	REMOVELAST("removeLast"),
	REMOVEMIDDLE("removeMiddle"),
	REPLACEIN("replaceIn"),
	USE("use");
	
	String actionName;
	
	RuleActionsEnum(String action)
	{
		this.actionName = action;
	}
	
	/**
	 * 
	 */
	public static RuleActionsEnum findRuleActionsEnumByName(String action)
	{
		for(RuleActionsEnum oneEnumSample :RuleActionsEnum.values())
		{
			if(oneEnumSample.actionName.equalsIgnoreCase(action))
				return oneEnumSample;
	
		}
		return null;
		
	}

}
