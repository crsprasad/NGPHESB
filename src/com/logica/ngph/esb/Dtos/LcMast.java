package com.logica.ngph.esb.Dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class LcMast implements Serializable {

	private static final long serialVersionUID = 1L;

	private String msgRef;
	private String lcNo;
	private String lcType;
	private String lcDirection;
	private Timestamp lcIssueDate;
	private Timestamp lcExpireDate;
	private String lcAppicant;
	private String lcBenificiary;
	private String lcCurrency;
	private BigDecimal lcNumOfMsgs;
	private BigDecimal lcStatus;
	private BigDecimal lcNumOfAmndments;
	private String lcNarrative;
	private String lcAdvisingBank;
	private String lcIssuingBank;
	private BigDecimal lcAmount;
	private Timestamp lcAckDate;
	
	
	/**
	 * @return the lcAckDate
	 */
	public Timestamp getLcAckDate() {
		return lcAckDate;
	}
	/**
	 * @param lcAckDate the lcAckDate to set
	 */
	public void setLcAckDate(Timestamp lcAckDate) {
		this.lcAckDate = lcAckDate;
	}
	/**
	 * @return the msgRef
	 */
	public String getMsgRef() {
		return msgRef;
	}
	/**
	 * @return the lcNo
	 */
	public String getLcNo() {
		return lcNo;
	}
	/**
	 * @return the lcType
	 */
	public String getLcType() {
		return lcType;
	}
	/**
	 * @return the lcDirection
	 */
	public String getLcDirection() {
		return lcDirection;
	}
	/**
	 * @return the lcIssueDate
	 */
	public Timestamp getLcIssueDate() {
		return lcIssueDate;
	}
	/**
	 * @return the lcExpireDate
	 */
	public Timestamp getLcExpireDate() {
		return lcExpireDate;
	}
	/**
	 * @return the lcAppicant
	 */
	public String getLcAppicant() {
		return lcAppicant;
	}
	/**
	 * @return the lcBenificiary
	 */
	public String getLcBenificiary() {
		return lcBenificiary;
	}
	/**
	 * @return the lcCurrency
	 */
	public String getLcCurrency() {
		return lcCurrency;
	}
	
	/**
	 * @return the lcNarrative
	 */
	public String getLcNarrative() {
		return lcNarrative;
	}
	/**
	 * @return the lcAdvisingBank
	 */
	public String getLcAdvisingBank() {
		return lcAdvisingBank;
	}
	
	public String getLcIssuingBank() {
		return lcIssuingBank;
	}
	/**
	 * @return the lcAmount
	 */
	public BigDecimal getLcAmount() {
		return lcAmount;
	}
	/**
	 * @param msgRef the msgRef to set
	 */
	public void setMsgRef(String msgRef) {
		this.msgRef = msgRef;
	}
	/**
	 * @param lcNo the lcNo to set
	 */
	public void setLcNo(String lcNo) {
		this.lcNo = lcNo;
	}
	/**
	 * @param lcType the lcType to set
	 */
	public void setLcType(String lcType) {
		this.lcType = lcType;
	}
	/**
	 * @param lcDirection the lcDirection to set
	 */
	public void setLcDirection(String lcDirection) {
		this.lcDirection = lcDirection;
	}
	/**
	 * @param lcIssueDate the lcIssueDate to set
	 */
	public void setLcIssueDate(Timestamp lcIssueDate) {
		this.lcIssueDate = lcIssueDate;
	}
	/**
	 * @param lcExpireDate the lcExpireDate to set
	 */
	public void setLcExpireDate(Timestamp lcExpireDate) {
		this.lcExpireDate = lcExpireDate;
	}
	/**
	 * @param lcAppicant the lcAppicant to set
	 */
	public void setLcAppicant(String lcAppicant) {
		this.lcAppicant = lcAppicant;
	}
	/**
	 * @param lcBenificiary the lcBenificiary to set
	 */
	public void setLcBenificiary(String lcBenificiary) {
		this.lcBenificiary = lcBenificiary;
	}
	/**
	 * @param lcCurrency the lcCurrency to set
	 */
	public void setLcCurrency(String lcCurrency) {
		this.lcCurrency = lcCurrency;
	}
	
	/**
	 * @param lcNarrative the lcNarrative to set
	 */
	public void setLcNarrative(String lcNarrative) {
		this.lcNarrative = lcNarrative;
	}
	/**
	 * @param lcAdvisingBank the lcAdvisingBank to set
	 */
	public void setLcAdvisingBank(String lcAdvisingBank) {
		this.lcAdvisingBank = lcAdvisingBank;
	}
	
	public void setLcIssuingBank(String lcIssuingBank) {
		this.lcIssuingBank = lcIssuingBank;
	}
	/**
	 * @param lcAmount the lcAmount to set
	 */
	public void setLcAmount(BigDecimal lcAmount) {
		this.lcAmount = lcAmount;
	}
	public BigDecimal getLcNumOfMsgs() {
		return lcNumOfMsgs;
	}
	public void setLcNumOfMsgs(BigDecimal lcNumOfMsgs) {
		this.lcNumOfMsgs = lcNumOfMsgs;
	}
	public BigDecimal getLcStatus() {
		return lcStatus;
	}
	public void setLcStatus(BigDecimal lcStatus) {
		this.lcStatus = lcStatus;
	}
	public BigDecimal getLcNumOfAmndments() {
		return lcNumOfAmndments;
	}
	public void setLcNumOfAmndments(BigDecimal lcNumOfAmndments) {
		this.lcNumOfAmndments = lcNumOfAmndments;
	}
}
