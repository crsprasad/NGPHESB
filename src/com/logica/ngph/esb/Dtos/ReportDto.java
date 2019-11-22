/**
 * 
 */
package com.logica.ngph.esb.Dtos;

import java.math.BigDecimal;

/**
 * @author chakkar
 *
 */
public class ReportDto {

	private String msgType;
	private String msgChannel;
	private String msgRef;
	private String paymentStatus;
	private String msgDirection;
	private String senderBank;
	private String receiverBank;
	private String msgCurrency;
	private BigDecimal msgAmount;
	private String msgValueDate;
	private String orderingCustomer;
	private String beneficiaryCustomer;
	private String MUR;
	private String sendertoreciverInfo;
	private String hostID;
	private String relatedRefrence;
	private String department;
	private String branch;
	private String txnType;
	private String beneficiaryAccount;
	private String orderingAccount;
	private String txnReference;
	private String lastModifiedDate;
	/**
	 * @return the msgType
	 */
	public String getMsgType() {
		return msgType;
	}
	/**
	 * @param msgType the msgType to set
	 */
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	/**
	 * @return the msgChannel
	 */
	public String getMsgChannel() {
		return msgChannel;
	}
	/**
	 * @param msgChannel the msgChannel to set
	 */
	public void setMsgChannel(String msgChannel) {
		this.msgChannel = msgChannel;
	}
	/**
	 * @return the msgRef
	 */
	public String getMsgRef() {
		return msgRef;
	}
	/**
	 * @param msgRef the msgRef to set
	 */
	public void setMsgRef(String msgRef) {
		this.msgRef = msgRef;
	}
	/**
	 * @return the paymentStatus
	 */
	public String getPaymentStatus() {
		return paymentStatus;
	}
	/**
	 * @param paymentStatus the paymentStatus to set
	 */
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	/**
	 * @return the msgDirection
	 */
	public String getMsgDirection() {
		return msgDirection;
	}
	/**
	 * @param msgDirection the msgDirection to set
	 */
	public void setMsgDirection(String msgDirection) {
		this.msgDirection = msgDirection;
	}
	/**
	 * @return the senderBank
	 */
	public String getSenderBank() {
		return senderBank;
	}
	/**
	 * @param senderBank the senderBank to set
	 */
	public void setSenderBank(String senderBank) {
		this.senderBank = senderBank;
	}
	/**
	 * @return the receiverBank
	 */
	public String getReceiverBank() {
		return receiverBank;
	}
	/**
	 * @param receiverBank the receiverBank to set
	 */
	public void setReceiverBank(String receiverBank) {
		this.receiverBank = receiverBank;
	}
	/**
	 * @return the msgCurrency
	 */
	public String getMsgCurrency() {
		return msgCurrency;
	}
	/**
	 * @param msgCurrency the msgCurrency to set
	 */
	public void setMsgCurrency(String msgCurrency) {
		this.msgCurrency = msgCurrency;
	}
	/**
	 * @return the msgAmount
	 */
	public BigDecimal getMsgAmount() {
		return msgAmount;
	}
	/**
	 * @param msgAmount the msgAmount to set
	 */
	public void setMsgAmount(BigDecimal msgAmount) {
		this.msgAmount = msgAmount;
	}
	/**
	 * @return the msgValueDate
	 */
	public String getMsgValueDate() {
		return msgValueDate;
	}
	/**
	 * @param msgValueDate the msgValueDate to set
	 */
	public void setMsgValueDate(String msgValueDate) {
		this.msgValueDate = msgValueDate;
	}
	/**
	 * @return the orderingCustomer
	 */
	public String getOrderingCustomer() {
		return orderingCustomer;
	}
	/**
	 * @param orderingCustomer the orderingCustomer to set
	 */
	public void setOrderingCustomer(String orderingCustomer) {
		this.orderingCustomer = orderingCustomer;
	}
	/**
	 * @return the beneficiaryCustomer
	 */
	public String getBeneficiaryCustomer() {
		return beneficiaryCustomer;
	}
	/**
	 * @param beneficiaryCustomer the beneficiaryCustomer to set
	 */
	public void setBeneficiaryCustomer(String beneficiaryCustomer) {
		this.beneficiaryCustomer = beneficiaryCustomer;
	}
	/**
	 * @return the mUR
	 */
	public String getMUR() {
		return MUR;
	}
	/**
	 * @param mUR the mUR to set
	 */
	public void setMUR(String mUR) {
		MUR = mUR;
	}
	/**
	 * @return the sendertoreciverInfo
	 */
	public String getSendertoreciverInfo() {
		return sendertoreciverInfo;
	}
	/**
	 * @param sendertoreciverInfo the sendertoreciverInfo to set
	 */
	public void setSendertoreciverInfo(String sendertoreciverInfo) {
		this.sendertoreciverInfo = sendertoreciverInfo;
	}
	/**
	 * @return the hostID
	 */
	public String getHostID() {
		return hostID;
	}
	/**
	 * @param hostID the hostID to set
	 */
	public void setHostID(String hostID) {
		this.hostID = hostID;
	}
	/**
	 * @return the relatedRefrence
	 */
	public String getRelatedRefrence() {
		return relatedRefrence;
	}
	/**
	 * @param relatedRefrence the relatedRefrence to set
	 */
	public void setRelatedRefrence(String relatedRefrence) {
		this.relatedRefrence = relatedRefrence;
	}
	/**
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}
	/**
	 * @param department the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}
	/**
	 * @return the branch
	 */
	public String getBranch() {
		return branch;
	}
	/**
	 * @param branch the branch to set
	 */
	public void setBranch(String branch) {
		this.branch = branch;
	}
	/**
	 * @return the txnType
	 */
	public String getTxnType() {
		return txnType;
	}
	/**
	 * @param txnType the txnType to set
	 */
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	/**
	 * @return the beneficiaryAccount
	 */
	public String getBeneficiaryAccount() {
		return beneficiaryAccount;
	}
	/**
	 * @param beneficiaryAccount the beneficiaryAccount to set
	 */
	public void setBeneficiaryAccount(String beneficiaryAccount) {
		this.beneficiaryAccount = beneficiaryAccount;
	}
	/**
	 * @return the orderingAccount
	 */
	public String getOrderingAccount() {
		return orderingAccount;
	}
	/**
	 * @param orderingAccount the orderingAccount to set
	 */
	public void setOrderingAccount(String orderingAccount) {
		this.orderingAccount = orderingAccount;
	}
	/**
	 * @return the txnReference
	 */
	public String getTxnReference() {
		return txnReference;
	}
	/**
	 * @param txnReference the txnReference to set
	 */
	public void setTxnReference(String txnReference) {
		this.txnReference = txnReference;
	}
	/**
	 * @return the lastModifiedDate
	 */
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	/**
	 * @param lastModifiedDate the lastModifiedDate to set
	 */
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	
	@Override
	public String toString() {
		
		
		return "ReportDto [msgType=" + msgType + ", msgChannel=" + msgChannel
				+ ", msgRef=" + msgRef + ", paymentStatus=" + paymentStatus
				+ ", msgDirection=" + msgDirection + ", senderBank="
				+ senderBank + ", receiverBank=" + receiverBank
				+ ", msgCurrency=" + msgCurrency + ", msgAmount=" + msgAmount
				+ ", msgValueDate=" + msgValueDate + ", orderingCustomer="
				+ orderingCustomer + ", beneficiaryCustomer="
				+ beneficiaryCustomer + ", MUR=" + MUR
				+ ", sendertoreciverInfo=" + sendertoreciverInfo + ", hostID="
				+ hostID + ", relatedRefrence=" + relatedRefrence + ",beneficiaryAccount="
				+ beneficiaryAccount + ",orderingAccount="+orderingAccount + ",txnReference="+ txnReference+"]";
	}
}
