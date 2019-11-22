package com.logica.ngph.validators.dto;

import java.util.HashMap;

/*
 * Pojo Class for data Transportation.
 * Fetch the Data row Wise from TA_MSG_Field and map the column value to the corresponding variable.
 */
public class MsgFormat {
	
	private String chnlType;
	private String msgType;
	private String subMsgType;
	private String fieldNo;
	private String fieldMand;
	private String fieldSeq;
	private String fieldTagOpt;
	private String fieldId;
	private HashMap<String, MsgField> MsgFieldMapper;
	/**
	 * @return the fieldId
	 */
	public String getFieldId() {
		return fieldId;
	}
	/**
	 * @param fieldId the fieldId to set
	 */
	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}
	/**
	 * @return the msgFieldMapper
	 */
	public HashMap<String, MsgField> getMsgFieldMapper() {
		return MsgFieldMapper;
	}
	/**
	 * @param msgFieldMapper the msgFieldMapper to set
	 */
	public void setMsgFieldMapper(HashMap<String, MsgField> msgFieldMapper) {
		MsgFieldMapper = msgFieldMapper;
	}
	/**
	 * @return the chnlType
	 */
	public String getChnlType() {
		return chnlType;
	}
	/**
	 * @return the msgType
	 */
	public String getMsgType() {
		return msgType;
	}
	/**
	 * @return the subMsgType
	 */
	public String getSubMsgType() {
		return subMsgType;
	}
	/**
	 * @return the fieldNo
	 */
	public String getFieldNo() {
		return fieldNo;
	}
	/**
	 * @return the fieldMand
	 */
	public String getFieldMand() {
		return fieldMand;
	}
	/**
	 * @return the fieldSeq
	 */
	public String getFieldSeq() {
		return fieldSeq;
	}
	/**
	 * @return the fieldTagOpt
	 */
	public String getFieldTagOpt() {
		return fieldTagOpt;
	}
	/**
	 * @param chnlType the chnlType to set
	 */
	public void setChnlType(String chnlType) {
		this.chnlType = chnlType;
	}
	/**
	 * @param msgType the msgType to set
	 */
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	/**
	 * @param subMsgType the subMsgType to set
	 */
	public void setSubMsgType(String subMsgType) {
		this.subMsgType = subMsgType;
	}
	/**
	 * @param fieldNo the fieldNo to set
	 */
	public void setFieldNo(String fieldNo) {
		this.fieldNo = fieldNo;
	}
	/**
	 * @param fieldMand the fieldMand to set
	 */
	public void setFieldMand(String fieldMand) {
		this.fieldMand = fieldMand;
	}
	/**
	 * @param fieldSeq the fieldSeq to set
	 */
	public void setFieldSeq(String fieldSeq) {
		this.fieldSeq = fieldSeq;
	}
	/**
	 * @param fieldTagOpt the fieldTagOpt to set
	 */
	public void setFieldTagOpt(String fieldTagOpt) {
		this.fieldTagOpt = fieldTagOpt;
	}
	

}
