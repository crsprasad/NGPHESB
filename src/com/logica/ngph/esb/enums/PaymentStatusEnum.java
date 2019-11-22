package com.logica.ngph.esb.enums;

public enum PaymentStatusEnum {

	//Statuses for Out bound Payments
	RECEIVED_O("Received", "1"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_REPAIR_O("AwaitingRepair", "2"), //POSSIBLE_ACTIONS: Modify, Delete, Release
	WAREHOUSED_O("Warehoused", "3"), //POSSIBLE_ACTIONS: Move to Repair
	AWAITING_ACCOUNTING_O("AwaitingAccounting", "4"), //POSSIBLE_ACTIONS: Mark as Accounting Completed
	ACCOUNTING_COMPLETED_O("AccountingCompleted", "5"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_LIQUIDITY_O("AwaitingLiquidity", "6"), //POSSIBLE_ACTIONS:Mark as LM Completed
	LIQUIDITY_COMPLETED_O("LiquidityCompleted", "7"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_AML_O("AwaitingAML", "8"), //POSSIBLE_ACTIONS: Mark as AML Completed
	AML_COMPLETED_O("AMLCompleted", "9"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_BILLING_O("AwaitingBilling", "10"), //POSSIBLE_ACTIONS: Marks as Billing Completed
	BILLING_COMPLETED_O("BillingCompleted", "11"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_ACK_O("AwaitingAcknowledgement", "12"), //POSSIBLE_ACTIONS: Generate Cancellation
	COMPLETED_O("Completed", "13"), //POSSIBLE_ACTIONS: Mark as finalized
	FINALISED_O("Finalised", "14"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_RELEASE_O("AwaitingRelease", "15"), //POSSIBLE_ACTIONS: Release
	EXCEPTION_O("Exceptions", "16"), //POSSIBLE_ACTIONS: Move to Repair, Re-process
	AWAITING_AUTHORISATION_O("AwaitingAuthorisation", "17"), //POSSIBLE_ACTIONS: Approve, Reject
	REJECTED_BY_CHANNEL_O("RejectedByChannel", "18"), //POSSIBLE_ACTIONS: Delete, Move to Repair
	INVALID_O("Invalids", "19"), //POSSIBLE_ACTIONS: Delete, Move to Repair, Re-process
	DELETED_O("Deleted", "20"), //POSSIBLE_ACTIONS: NotApplicable
	RETURNED_O("Returned","21"), //Return Status for OutBound Payments
	DUPLICATES_O("Duplicate","22"),
	
	//Statuses for In bound Payments
	RECEIVED_I("Received", "31"), //POSSIBLE_ACTIONS: Select Host
	INTERVENED_I("Intervened", "32"), //POSSIBLE_ACTIONS: Move to Repair, Return, Mark as processed
	AWAITING_REPAIR_I("AwaitingRepair", "33"), //POSSIBLE_ACTIONS: Modify
	AWAITING_ACCOUNTING_I("AwaitingAccounting", "34"), //POSSIBLE_ACTIONS: Mark as Accounting Completed
	ACCOUNTING_COMPLETED_I("AccountingCompleted", "35"), //POSSIBLE_ACTIONS: NotApplicable
	ACCOUNTING_MISMATCH_I("Account Mismatch", "36"), //POSSIBLE_ACTIONS: Move to Repair, Return, Mark as processed
	AWAITING_LIQUIDITY_I("AwaitingLiquidity", "37"), //POSSIBLE_ACTIONS: Mark as LM Completed
	LIQUIDITY_COMPLETED_I("LiquidityCompleted", "38"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_AML_I("AwaitingAML", "39"), //POSSIBLE_ACTIONS: Mark as AML completed
	AML_COMPLETED_I("AMLCompleted", "40"), //POSSIBLE_ACTIONS: NotApplicable
	AWAITING_BILLING_I("AwaitingBilling", "41"), //POSSIBLE_ACTIONS: Mark as Billing Completed
	BILLING_COMPLETED_I("BillingCompleted", "42"), //POSSIBLE_ACTIONS: NotApplicable
	SENT_TO_HOST_I("SentToHost", "43"), //POSSIBLE_ACTIONS: Generate Return
	PRODESSED_MANUALLY_I("ProcessedManually", "44"), //POSSIBLE_ACTIONS: Generate Return, Move to Repair
	RETURNED_I("Returned","46"); //Return Status for InBound Payments
	

	//Description of the payment status
	String paymentStatusDesc;
	
	//Code of the payment status
	String code;
	
	/*
	 * 
	 */
	private PaymentStatusEnum(String paymentStatusDesc, String paymentStatusCode){
		 this.paymentStatusDesc = paymentStatusDesc;
		 this.code = paymentStatusCode;
	 }
	
	/**
	 * 
	 * @param enumAttribute
	 * @return
	 */
	public static String findPaymentStatusCodeByEnum(PaymentStatusEnum enumAttribute)
	{
		return enumAttribute.code;
	}
	
	/**
	 * 
	 * @param statusCode
	 * @return
	 */
	public static PaymentStatusEnum findPaymentStatusEnumByCode(String statusCode)
	{
		System.out.println("Inside Esb Payment status");
		for(PaymentStatusEnum oneEnumSample :PaymentStatusEnum.values())
		{
			if(oneEnumSample.code.equalsIgnoreCase(statusCode))
				return oneEnumSample;
	
		}
		return null;
	}	

}
