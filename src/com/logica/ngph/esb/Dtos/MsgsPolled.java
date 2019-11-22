package com.logica.ngph.esb.Dtos;

import java.io.Serializable;

public class MsgsPolled implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4242285157410618898L;
	/*MSGS_MSGREF,
	IN_TIME, 
	LASTORCHSERVICEIDCALLED, 
	MARKED_OUT_TIME*/
	
	private String msgsRef;
	//private Timestamp inTime;
	//private Timestamp markedOutTime;
	private String lastOrchServiceIdCalled;
	private String branchName;
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	private String settlementDate ;
	private String msgsStatus;
	private String msgsPrevStatus;
	public String getSettlementDate() {
		return settlementDate;
	}
	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}
	public String getMsgsStatus() {
		return msgsStatus;
	}
	public void setMsgsStatus(String msgsStatus) {
		this.msgsStatus = msgsStatus;
	}
	public String getMsgsPrevStatus() {
		return msgsPrevStatus;
	}
	public void setMsgsPrevStatus(String msgsPrevStatus) {
		this.msgsPrevStatus = msgsPrevStatus;
	}
	public String getMsgsRef() {
		return msgsRef;
	}
	public void setMsgsRef(String msgsRef) {
		this.msgsRef = msgsRef;
	}
	
	public String getLastOrchServiceIdCalled() {
		return lastOrchServiceIdCalled;
	}
	public void setLastOrchServiceIdCalled(String lastOrchServiceIdCalled) {
		this.lastOrchServiceIdCalled = lastOrchServiceIdCalled;
	}

}
