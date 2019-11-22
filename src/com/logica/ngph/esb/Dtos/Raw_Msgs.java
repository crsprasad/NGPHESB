package com.logica.ngph.esb.Dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Timestamp;

public class Raw_Msgs implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Clob rawMsgs;
	private String rawHost;
	private String rawChnl;
	private String rawDrctn;
	private BigDecimal rawMsgValStatus;
	private Timestamp rawRcvdTm;
	
	/**
	 * @return the rawMsgs
	 */
	public Clob getRawMsgs() {
		return rawMsgs;
	}
	/**
	 * @return the rawHost
	 */
	public String getRawHost() {
		return rawHost;
	}
	/**
	 * @return the rawChnl
	 */
	public String getRawChnl() {
		return rawChnl;
	}
	/**
	 * @return the rawDrctn
	 */
	public String getRawDrctn() {
		return rawDrctn;
	}
	/**
	 * @return the rawMsgValStatus
	 */
	public BigDecimal getRawMsgValStatus() {
		return rawMsgValStatus;
	}
	/**
	 * @return the rawRcvdTm
	 */
	public Timestamp getRawRcvdTm() {
		return rawRcvdTm;
	}
	/**
	 * @param rawMsgs the rawMsgs to set
	 */
	public void setRawMsgs(Clob rawMsgs) {
		this.rawMsgs = rawMsgs;
	}
	/**
	 * @param rawHost the rawHost to set
	 */
	public void setRawHost(String rawHost) {
		this.rawHost = rawHost;
	}
	/**
	 * @param rawChnl the rawChnl to set
	 */
	public void setRawChnl(String rawChnl) {
		this.rawChnl = rawChnl;
	}
	/**
	 * @param rawDrctn the rawDrctn to set
	 */
	public void setRawDrctn(String rawDrctn) {
		this.rawDrctn = rawDrctn;
	}
	/**
	 * @param rawMsgValStatus the rawMsgValStatus to set
	 */
	public void setRawMsgValStatus(BigDecimal rawMsgValStatus) {
		this.rawMsgValStatus = rawMsgValStatus;
	}
	/**
	 * @param rawRcvdTm the rawRcvdTm to set
	 */
	public void setRawRcvdTm(Timestamp rawRcvdTm) {
		this.rawRcvdTm = rawRcvdTm;
	}
	
	

	
}
