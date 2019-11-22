package com.logica.ngph.esb.Dtos;

import java.io.Serializable;
import java.sql.Timestamp;

import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * @author guptarb
 *
 */
public class ResponseBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String resCode;
	private Timestamp reqTmStmp;
	private Timestamp resTmStmp;
	private int verSendCt =0;
	private NgphCanonical canonicalObj;
	private String msgType;
	private String msgSubType;
	private String msgDirection;
	
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
	 * @return the msgType
	 */
	public String getMsgType() {
		return msgType;
	}
	/**
	 * @return the msgSubType
	 */
	public String getMsgSubType() {
		return msgSubType;
	}
	/**
	 * @param msgType the msgType to set
	 */
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	/**
	 * @param msgSubType the msgSubType to set
	 */
	public void setMsgSubType(String msgSubType) {
		this.msgSubType = msgSubType;
	}
	/**
	 * @return the resCode
	 */
	public String getResCode() {
		return resCode;
	}
	/**
	 * @return the reqTmStmp
	 */
	public Timestamp getReqTmStmp() {
		return reqTmStmp;
	}
	/**
	 * @return the resTmStmp
	 */
	public Timestamp getResTmStmp() {
		return resTmStmp;
	}
	/**
	 * @return the verSendCt
	 */
	public int getVerSendCt() {
		return verSendCt;
	}
	/**
	 * @return the canonicalObj
	 */
	public NgphCanonical getCanonicalObj() {
		return canonicalObj;
	}
	/**
	 * @param resCode the resCode to set
	 */
	public void setResCode(String resCode) {
		this.resCode = resCode;
	}
	/**
	 * @param reqTmStmp the reqTmStmp to set
	 */
	public void setReqTmStmp(Timestamp reqTmStmp) {
		this.reqTmStmp = reqTmStmp;
	}
	/**
	 * @param resTmStmp the resTmStmp to set
	 */
	public void setResTmStmp(Timestamp resTmStmp) {
		this.resTmStmp = resTmStmp;
	}
	/**
	 * @param verSendCt the verSendCt to set
	 */
	public void setVerSendCt(int verSendCt) {
		this.verSendCt = verSendCt;
	}
	/**
	 * @param canonicalObj the canonicalObj to set
	 */
	public void setCanonicalObj(NgphCanonical canonicalObj) {
		this.canonicalObj = canonicalObj;
	}
}
