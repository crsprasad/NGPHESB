package com.logica.ngph.esb.Dtos;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class BGMast  implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private String msgRef;
	private String bgNo;
	private BigDecimal bgNoOfMsgs;
	private String bgDirection;
	private Timestamp bgIssueDate;
	private String bgCreateType;
	private String bgRulesCode;
	private String bgRulesDesc;
	private String bgDetails;
	private String bgNarration;
	private String bgIssuingBank;
	private String bgAdvisingBank;
	private BigDecimal bgStatus;
	private BigDecimal bgNumOfAmndments;
	private BigDecimal bgSequenceNo;
	private Timestamp bgLastAmendmentDate;
	private String bgLastAmendmentRef;
	private String bgRelReferenceNo;
	private String bgAccIdentification;
	private Timestamp bgAckDate;
	private String bgChargeDetails;
	private Timestamp bgDebitDate;
	// Start :: Added for enabling 769 message for Mizuho Bank
	private Timestamp bgReductionDate;
	private Timestamp bgChargeDate;
	private String bgChargeCurrCode;
	private BigDecimal bgChargeAmt;
	private String bgReductionCurrCode;
	private BigDecimal bgReductionAmt;
	private String bgOctStdCurrCode;
	private BigDecimal bgOctStdAmt;
	private String BgAmtSpecification;
	// End :: Added for enabling 769 message for Mizuho Bank
	
	
	
	
	public BigDecimal getBgNoOfMsgs() {
		return bgNoOfMsgs;
	}
	public Timestamp getBgReductionDate() {
		return bgReductionDate;
	}
	public void setBgReductionDate(Timestamp bgReductionDate) {
		this.bgReductionDate = bgReductionDate;
	}
	public Timestamp getBgChargeDate() {
		return bgChargeDate;
	}
	public void setBgChargeDate(Timestamp bgChargeDate) {
		this.bgChargeDate = bgChargeDate;
	}
	public String getBgChargeCurrCode() {
		return bgChargeCurrCode;
	}
	public void setBgChargeCurrCode(String bgChargeCurrCode) {
		this.bgChargeCurrCode = bgChargeCurrCode;
	}
	public BigDecimal getBgChargeAmt() {
		return bgChargeAmt;
	}
	public void setBgChargeAmt(BigDecimal bgChargeAmt) {
		this.bgChargeAmt = bgChargeAmt;
	}
	public String getBgReductionCurrCode() {
		return bgReductionCurrCode;
	}
	public void setBgReductionCurrCode(String bgReductionCurrCode) {
		this.bgReductionCurrCode = bgReductionCurrCode;
	}
	public BigDecimal getBgReductionAmt() {
		return bgReductionAmt;
	}
	public void setBgReductionAmt(BigDecimal bgReductionAmt) {
		this.bgReductionAmt = bgReductionAmt;
	}
	public String getBgOctStdCurrCode() {
		return bgOctStdCurrCode;
	}
	public void setBgOctStdCurrCode(String bgOctStdCurrCode) {
		this.bgOctStdCurrCode = bgOctStdCurrCode;
	}
	public BigDecimal getBgOctStdAmt() {
		return bgOctStdAmt;
	}
	public void setBgOctStdAmt(BigDecimal bgOctStdAmt) {
		this.bgOctStdAmt = bgOctStdAmt;
	}
	public String getBgAmtSpecification() {
		return BgAmtSpecification;
	}
	public void setBgAmtSpecification(String bgAmtSpecification) {
		BgAmtSpecification = bgAmtSpecification;
	}
	public void setBgNoOfMsgs(BigDecimal bgNoOfMsgs) {
		this.bgNoOfMsgs = bgNoOfMsgs;
	}
	public BigDecimal getBgStatus() {
		return bgStatus;
	}
	public void setBgStatus(BigDecimal bgStatus) {
		this.bgStatus = bgStatus;
	}
	public BigDecimal getBgNumOfAmndments() {
		return bgNumOfAmndments;
	}
	public void setBgNumOfAmndments(BigDecimal bgNumOfAmndments) {
		this.bgNumOfAmndments = bgNumOfAmndments;
	}
	private BigDecimal bgAmount;
	
	public String getMsgRef() {
		return msgRef;
	}
	public void setMsgRef(String msgRef) {
		this.msgRef = msgRef;
	}
	public String getBgNo() {
		return bgNo;
	}
	public void setBgNo(String bgNo) {
		this.bgNo = bgNo;
	}
	
	public String getBgDirection() {
		return bgDirection;
	}
	public void setBgDirection(String bgDirection) {
		this.bgDirection = bgDirection;
	}
	public Timestamp getBgIssueDate() {
		return bgIssueDate;
	}
	public void setBgIssueDate(Timestamp bgIssueDate) {
		this.bgIssueDate = bgIssueDate;
	}
	public String getBgCreateType() {
		return bgCreateType;
	}
	public void setBgCreateType(String bgCreateType) {
		this.bgCreateType = bgCreateType;
	}
	public String getBgRulesCode() {
		return bgRulesCode;
	}
	public void setBgRulesCode(String bgRulesCode) {
		this.bgRulesCode = bgRulesCode;
	}
	public String getBgRulesDesc() {
		return bgRulesDesc;
	}
	public void setBgRulesDesc(String bgRulesDesc) {
		this.bgRulesDesc = bgRulesDesc;
	}
	public String getBgDetails() {
		return bgDetails;
	}
	public void setBgDetails(String bgDetails) {
		this.bgDetails = bgDetails;
	}
	public String getBgNarration() {
		return bgNarration;
	}
	public void setBgNarration(String bgNarration) {
		this.bgNarration = bgNarration;
	}
	public String getBgIssuingBank() {
		return bgIssuingBank;
	}
	public void setBgIssuingBank(String bgIssuingBank) {
		this.bgIssuingBank = bgIssuingBank;
	}
	public String getBgAdvisingBank() {
		return bgAdvisingBank;
	}
	public void setBgAdvisingBank(String bgAdvisingBank) {
		this.bgAdvisingBank = bgAdvisingBank;
	}
	
	public BigDecimal getBgAmount() {
		return bgAmount;
	}
	public void setBgAmount(BigDecimal bgAmount) {
		this.bgAmount = bgAmount;
	}
	public BigDecimal getBgSequenceNo() {
		return bgSequenceNo;
	}
	public void setBgSequenceNo(BigDecimal bgSequenceNo) {
		this.bgSequenceNo = bgSequenceNo;
	}
	public Timestamp getBgLastAmendmentDate() {
		return bgLastAmendmentDate;
	}
	public void setBgLastAmendmentDate(Timestamp bgLastAmendmentDate) {
		this.bgLastAmendmentDate = bgLastAmendmentDate;
	}
	public String getBgLastAmendmentRef() {
		return bgLastAmendmentRef;
	}
	public void setBgLastAmendmentRef(String bgLastAmendmentRef) {
		this.bgLastAmendmentRef = bgLastAmendmentRef;
	}
	/**
	 * @return the bgRelReferenceNo
	 */
	public String getBgRelReferenceNo() {
		return bgRelReferenceNo;
	}
	/**
	 * @param bgRelReferenceNo the bgRelReferenceNo to set
	 */
	public void setBgRelReferenceNo(String bgRelReferenceNo) {
		this.bgRelReferenceNo = bgRelReferenceNo;
	}
	/**
	 * @return the bgAccIdentification
	 */
	public String getBgAccIdentification() {
		return bgAccIdentification;
	}
	/**
	 * @param bgAccIdentification the bgAccIdentification to set
	 */
	public void setBgAccIdentification(String bgAccIdentification) {
		this.bgAccIdentification = bgAccIdentification;
	}
	/**
	 * @return the bgAckDate
	 */
	public Timestamp getBgAckDate() {
		return bgAckDate;
	}
	/**
	 * @param bgAckDate the bgAckDate to set
	 */
	public void setBgAckDate(Timestamp bgAckDate) {
		this.bgAckDate = bgAckDate;
	}
	/**
	 * @return the bgChargeDetails
	 */
	public String getBgChargeDetails() {
		return bgChargeDetails;
	}
	/**
	 * @param bgChargeDetails the bgChargeDetails to set
	 */
	public void setBgChargeDetails(String bgChargeDetails) {
		this.bgChargeDetails = bgChargeDetails;
	}
	/**
	 * @return the bgDebitDate
	 */
	public Timestamp getBgDebitDate() {
		return bgDebitDate;
	}
	/**
	 * @param bgDebitDate the bgDebitDate to set
	 */
	public void setBgDebitDate(Timestamp bgDebitDate) {
		this.bgDebitDate = bgDebitDate;
	}
	
}
