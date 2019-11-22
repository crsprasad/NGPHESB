package com.logica.ngph.esb.enums;
public enum ValidateServiceRuleCodeEnum 
{
	VSR0001("VSR0001", "InstructedAgentRule"),
	VSR0002("VSR0002", "InstructingAgentRule"),
	VSR0003("VSR0003", "TotalInterbankSettlementAmountRule"),
	VSR0004("VSR0004", "TotalInterbankSettlementAmountAndSumRule"),
	VSR0005("VSR0005", "GroupHeaderInterbankSettlementDateRule"),
	VSR0006("VSR0006", "TransactionInterbankSettlementDateRule"),
	VSR0007("VSR0007", "PaymentTypeInformationRule"),
	VSR0008("VSR0008", "NumberOfTransactionsAndCreditTransfersRule"),
	VSR0009("VSR0009", "TotalInterbankSettlementAmountAndDateRule"),
	VSR0010("VSR0010", "ThirdReimbursementAgentRule"),
	VSR0011("VSR0011", "SettlementMethodAgentRule"),
	VSR0012("VSR0012", "SettlementMethodCoverRule"),
	VSR0013("VSR0013", "SettlementMethodCoverAgentRule"),
	VSR0014("VSR0014", "SettlementMethodClearingRule"),
	VSR0015("VSR0015", "InstructingReimbursementAgentAccountRule"),
	VSR0016("VSR0016", "InstructedReimbursementAgentAccountRule"),
	VSR0017("VSR0017", "ThirdReimbursementAgentAccountRule"),
	VSR0018("VSR0018", "InstructedAmountAndExchangeRate1Rule"),
	VSR0019("VSR0019", "InstructedAmountAndExchangeRate2Rule"),
	VSR0020("VSR0020", "ChargesInformationAndInstructedAmountRule"),
	VSR0021("VSR0021", "ChargesAmountRule"),
	VSR0022("VSR0022", "ChargeBearerAndChargesInformationRule"),
	VSR0023("VSR0023", "InstructionForCreditorAgentRule"),
	VSR0024("VSR0024", "IntermediaryAgent2Rule"),
	VSR0025("VSR0025", "IntermediaryAgent3Rule"),
	VSR0026("VSR0026", "IntermediaryAgent1AccountRule"),
	VSR0027("VSR0027", "IntermediaryAgent2AccountRule"),
	VSR0028("VSR0028", "IntermediaryAgent3AccountRule"),
	VSR0029("VSR0029", "PreviousInstructingAgentAccountRule"),
	VSR0030("VSR0030", "DebtorAgentAccountRule"),
	VSR0031("VSR0031", "CreditorAgentAccountRule"),
	VSR0032("VSR0032", "InstructedAmountAndExchangeRate3Rule"),
	VSR0033("VSR0033", "RemitterMobileRule"),
	VSR0034("VSR0034", "NRE Account rule"),
	VSR0035("VSR0035", "Beneficiary Account daily limit rule"),
	VSR0036("VSR0036", "Member credit limit rule"),
	VSR0037("VSR0037", "Sender and Receiver Bank is not same"),
	VSR0038("VSR0038", "Transaction type allowed for account"),
	VSR0039("VSR0039", "Either 39A or 39B, but not both, may be present"),
	VSR0040("VSR0040", "42C and 42a must both be present"),
	VSR0041("VSR0041", "42C and 42a together, or field 42M alone, or field 42P alone may be present"),
	VSR0042("VSR0042", "Either 44C or 44D, but not both, may be present"),
	VSR0043("VSR0043", "If either field 32B or 33B is present, field 34B must also be present"),
	VSR0044("VSR0044", "If field 34B is present, either field 32B or 33B must also be present"),
	VSR0045("VSR0045", "If field 23 is present, field 52a must also be present"),
	VSR0046("VSR0046", "At least one of the fields 31E, 32B, 33B, 34B, 39A, 39B, 39C, 44A, 44E, 44F, 44B, 44C, 44D, 79 or72 must be present"),
	VSR0047("VSR0047", "The currency code in the amount fields 32B, 33B, and 34B must be the same"),
	VSR0048("VSR0048", "Either field 52a Issuing Bank or field 50B Non-Bank Issuer, but not both, must be present"),
	VSR0049("VSR0049", "Either field 25 or 57a, but not both, may be present"),
	VSR0050("VSR0050", "If field 32D is present, field 57a must not be present"),
	VSR0051("VSR0051", "If field 73 is present, field 33a must also be present"),
	VSR0052("VSR0052", "The currency code in the amount fields 32A and 33a must be the same"),
	VSR0053("VSR0053", "Either field 58a or 59, but not both, may be present"),
	VSR0054("VSR0054", "At least one of the fields 31E, 32B, 33B, 34B, 39A, 39B, 39C, 72 or 77A must be present"),
	VSR0055("VSR0055", "If field 33B and/or field 71B and/or field 73 is/are present, field 34B must also be present"),
	VSR0056("VSR0056", "The currency code in the amount fields 32B and 34B must be the same"),
	VSR0057("VSR0057", "If fields 32B and 71B are both present, then field 33a must also be present"),
	VSR0058("VSR0058", "The currency code in the amount fields 32B and 33a must be the same"),
	VSR0059("VSR0059", "Either field 72 or 77A may be present, but not both"),
	VSR0060("VSR0060", "Either field 53a or 57a may be present, but not both"),
	VSR0061("VSR0061", "The currency code in the amount fields 32a and 34a must be the same"),
	VSR0062("VSR0062", "If field 71B is present, field 32a must also be present"),
	VSR0063("VSR0063", "Either field 33B or field 39C, but not both, must be present"),
	VSR0064("VSR0064", "This field must not start or end with a slash '/' and must not contain two consecutive slashes '//'"),
	VSR0065("VSR0065", "This field must contain the code PREADV followed by a slash '/' and a reference to the pre-advice"),
	VSR0066("VSR0066", "Date must contain a valid date expressed as YYMMDD"),
	VSR0067("VSR0067", "Subfield 2 of field 40E, ie, /35x, is only allowed when subfield 1 of this field consists of OTHR"),
	VSR0068("VSR0068", "The BIC must be a SWIFT registered BIC"),
	VSR0069("VSR0069", "Currency must be a valid ISO 4217 currency code"),
	VSR0070("VSR0070", "The integer part of Amount must contain at least one digit. The decimal comma ',' is mandatory.The number of digits following the comma must not exceed the maximum number allowed for the specified currency"),
	VSR0071("VSR0071", "Subfield 2 of field 40C, ie, /35x, is only allowed when subfield 1 of this field consists of OTHR"),
	//Start :: Added for BG COV messages
	VSR0072("VSR0072","If field 7040 is Y, fields 7043, 7044, 7045 and 7046 must also be present"),
	VSR0073("VSR0073","If field 7048 is Y,field 4049 must also present");
	//End :: Added for BG COV messages
	
	String vsRuleCode;
	String vsRuleName;
	
	/**
	 * 
	 */
	ValidateServiceRuleCodeEnum(String vsRuleCode)
	{
		this.vsRuleCode = vsRuleCode;
	}
	/**
	 * 
	 */
	ValidateServiceRuleCodeEnum(String vsRuleCode, String vsRuleName)
	{
		this.vsRuleCode = vsRuleCode;
		this.vsRuleName = vsRuleName;
	}
	
	/**
	 * 
	 */
	public static ValidateServiceRuleCodeEnum findValidateServiceRuleCodeEnumByCode(String code)
	{
		for(ValidateServiceRuleCodeEnum oneEnumSample :ValidateServiceRuleCodeEnum.values())
		{
			if(oneEnumSample.vsRuleCode.equalsIgnoreCase(code))
				return oneEnumSample;
	
		}
		return null;
	}

}
