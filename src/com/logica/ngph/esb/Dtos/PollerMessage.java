package com.logica.ngph.esb.Dtos;

import java.io.Serializable;


public class PollerMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6858428507029515391L;
	private String msgValueDate;
	private String BranchName;
	private String msgStatus;
	private String prevMsgStatus ;
	
	public String getPrevMsgStatus() {
		return prevMsgStatus;
	}
	public void setPrevMsgStatus(String prevMsgStatus) {
		this.prevMsgStatus = prevMsgStatus;
	}
	public String getMsgStatus() {
		return msgStatus;
	}
	public void setMsgStatus(String msgStatus) {
		this.msgStatus = msgStatus;
	}
	public String getMsgValueDate() {
		return msgValueDate;
	}
	public void setMsgValueDate(String msgValueDate) {
		this.msgValueDate = msgValueDate;
	}
	public String getBranchName() {
		return BranchName;
	}
	public void setBranchName(String branchName) {
		BranchName = branchName;
	}
	
	

}
