package com.logica.ngph.esb.Dtos;

import java.io.Serializable;
import java.sql.Timestamp;

public class DbPoller implements Serializable{

	private static final long serialVersionUID = 1L;

	Timestamp inTime;
	String lastOrchSvIdCalled;
	Timestamp markedOutTime;
	String msgRef;
	String pollReason;
	String pollStatus;

	/**
	 * @return the inTime
	 */
	public Timestamp getInTime() {
		return inTime;
	}
	/**
	 * @return the lastOrchSvIdCalled
	 */
	public String getLastOrchSvIdCalled() {
		return lastOrchSvIdCalled;
	}
	/**
	 * @return the markedOutTime
	 */
	public Timestamp getMarkedOutTime() {
		return markedOutTime;
	}
	/**
	 * @return the msgRef
	 */
	public String getMsgRef() {
		return msgRef;
	}
	/**
	 * @return the pollReason
	 */
	public String getPollReason() {
		return pollReason;
	}
	/**
	 * @return the pollStatus
	 */
	public String getPollStatus() {
		return pollStatus;
	}
	/**
	 * @param inTime the inTime to set
	 */
	public void setInTime(Timestamp inTime) {
		this.inTime = inTime;
	}
	/**
	 * @param lastOrchSvIdCalled the lastOrchSvIdCalled to set
	 */
	public void setLastOrchSvIdCalled(String lastOrchSvIdCalled) {
		this.lastOrchSvIdCalled = lastOrchSvIdCalled;
	}
	/**
	 * @param markedOutTime the markedOutTime to set
	 */
	public void setMarkedOutTime(Timestamp markedOutTime) {
		this.markedOutTime = markedOutTime;
	}
	/**
	 * @param msgRef the msgRef to set
	 */
	public void setMsgRef(String msgRef) {
		this.msgRef = msgRef;
	}
	/**
	 * @param pollReason the pollReason to set
	 */
	public void setPollReason(String pollReason) {
		this.pollReason = pollReason;
	}
	/**
	 * @param pollStatus the pollStatus to set
	 */
	public void setPollStatus(String pollStatus) {
		this.pollStatus = pollStatus;
	}
	
	
}
