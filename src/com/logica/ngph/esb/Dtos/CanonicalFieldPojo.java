package com.logica.ngph.esb.Dtos;

import java.io.Serializable;
/**
 * 
 * @author guptarb
 *
 *	This Pojo class is used for Attributes from TA_FORMAT_FIELDS and TA_MSG_FORMAT.
 *	 * CHANNELTYPE
	 * MSGTYPE
	 * MSGSUBTYPE
	 * FIELD_SEQ
	 * from TA_MSG_FORMAT
	 * and
	 * FIELD_NO
	 * FIELD_CANONICAL
	 * FIELD_CONSIDERATION
	 * FIELD_COMP_FORMAT
	 * field_eoc_ind
	 * from TA_FORMAT_FIELDS
 */
public class CanonicalFieldPojo implements Serializable{

	private static final long serialVersionUID = 1L;

	/**	
	 * CHANNELTYPE
	 * MSGTYPE
	 * MSGSUBTYPE
	 * FIELD_SEQ
	 * FIELD_NO
	 * FIELD_CANONICAL
	 * FIELD_CONSIDERATION
	 * FIELD_COMP_FORMAT
	 */
	private String channelType;
	private String msgType;
	private String msgSubType;
	private String fieldNo;
	private String fieldCanonicalAtt;
	private String field_Seq;
	private String fieldCompSeq;
	private String fieldCompMandOpt;
	private String fieldEocInd;
	private String field_cnsdr;
	private String field_comp_fmt; 
	private String field_Id;
	/**
	 * @return the field_Id
	 */
	public String getField_Id() {
		return field_Id;
	}
	/**
	 * @param field_Id the field_Id to set
	 */
	public void setField_Id(String field_Id) {
		this.field_Id = field_Id;
	}
	/**
	 * @return the field_comp_fmt
	 */
	public String getField_comp_fmt() {
		return field_comp_fmt;
	}
	/**
	 * @param field_comp_fmt the field_comp_fmt to set
	 */
	public void setField_comp_fmt(String field_comp_fmt) {
		this.field_comp_fmt = field_comp_fmt;
	}
	/**
	 * @return the field_cnsdr
	 */
	public String getField_cnsdr() {
		return field_cnsdr;
	}
	/**
	 * @param field_cnsdr the field_cnsdr to set
	 */
	public void setField_cnsdr(String field_cnsdr) {
		this.field_cnsdr = field_cnsdr;
	}
	/**
	 * @return the fieldEocInd
	 */
	public String getFieldEocInd() {
		return fieldEocInd;
	}
	/**
	 * @param fieldEocInd the fieldEocInd to set
	 */
	public void setFieldEocInd(String fieldEocInd) {
		this.fieldEocInd = fieldEocInd;
	}
	/**
	 * @return the fieldCompSeq
	 */
	public String getFieldCompSeq() {
		return fieldCompSeq;
	}
	/**
	 * @return the fieldCompMandOpt
	 */
	public String getFieldCompMandOpt() {
		return fieldCompMandOpt;
	}
	/**
	 * @param fieldCompSeq the fieldCompSeq to set
	 */
	public void setFieldCompSeq(String fieldCompSeq) {
		this.fieldCompSeq = fieldCompSeq;
	}
	/**
	 * @param fieldCompMandOpt the fieldCompMandOpt to set
	 */
	public void setFieldCompMandOpt(String fieldCompMandOpt) {
		this.fieldCompMandOpt = fieldCompMandOpt;
	}
	/**
	 * @return the field_Seq
	 */
	public String getField_Seq() {
		return field_Seq;
	}
	/**
	 * @param field_Seq the field_Seq to set
	 */
	public void setField_Seq(String field_Seq) {
		this.field_Seq = field_Seq;
	}
	/**
	 * @return the channelType
	 */
	public String getChannelType() {
		return channelType;
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
	 * @return the fieldNo
	 */
	public String getFieldNo() {
		return fieldNo;
	}
	/**
	 * @return the fieldCanonicalAtt
	 */
	public String getFieldCanonicalAtt() {
		return fieldCanonicalAtt;
	}
	/**
	 * @param channelType the channelType to set
	 */
	public void setChannelType(String channelType) {
		this.channelType = channelType;
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
	 * @param fieldNo the fieldNo to set
	 */
	public void setFieldNo(String fieldNo) {
		this.fieldNo = fieldNo;
	}
	/**
	 * @param fieldCanonicalAtt the fieldCanonicalAtt to set
	 */
	public void setFieldCanonicalAtt(String fieldCanonicalAtt) {
		this.fieldCanonicalAtt = fieldCanonicalAtt;
	}
}
