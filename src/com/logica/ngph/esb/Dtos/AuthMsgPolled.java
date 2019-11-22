package com.logica.ngph.esb.Dtos;

import java.io.Serializable;
import java.math.BigDecimal;

public class AuthMsgPolled implements Serializable {

	/**
	 * MSGS_HOSTID, 
	 * MSGS_MSGCHNLTYPE,
	 * MSGS_SRC_MSGTYPE,
	 * MSGS_DIRECTION,  
	 * MSGS_INTRBKSTTLMCCY,
	 * MSGS_INTRBKSTTLMAMT
	 * MSGS_BRANCH
	 * MSGS_DEPT
	 * MSGS_SRC_MSGSUBTYPE
	 */
	private String hosiId;
	private String channelType;
	private String msgType;
	private String direction;
	private String currency;
	private String branch;
	private String dept;
	private String subMSgType;
	private BigDecimal amt;
	
	public String getHosiId() {
		return hosiId;
	}
	public String getChannelType() {
		return channelType;
	}
	public String getMsgType() {
		return msgType;
	}
	public String getDirection() {
		return direction;
	}
	public String getCurrency() {
		return currency;
	}
	public String getBranch() {
		return branch;
	}
	public String getDept() {
		return dept;
	}
	public String getSubMSgType() {
		return subMSgType;
	}
	public BigDecimal getAmt() {
		return amt;
	}
	public void setHosiId(String hosiId) {
		this.hosiId = hosiId;
	}
	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public void setDept(String dept) {
		this.dept = dept;
	}
	public void setSubMSgType(String subMSgType) {
		this.subMSgType = subMSgType;
	}
	public void setAmt(BigDecimal amt) {
		this.amt = amt;
	}
	


}
