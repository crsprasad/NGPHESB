package com.logica.ngph.esb.Dtos;

import java.io.Serializable;

public class FieldCanonicalAttribute implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String fieldNo;
	private String fieldCanonicalAtt;
	private String fieldSeq;
	private String fieldCompSeq;
	private String fieldCompMandOpt;
	private String fieldEocInd;
	private String fieldcnsdr;
	private String field_comp_fmt; 
	private String msg_field_id;



	/**
	 * @return the msg_field_id
	 */
	public String getMsg_field_id() {
		return msg_field_id;
	}
	/**
	 * @param msg_field_id the msg_field_id to set
	 */
	public void setMsg_field_id(String msg_field_id) {
		this.msg_field_id = msg_field_id;
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
	 * @return the fieldcnsdr
	 */
	public String getFieldcnsdr() {
		return fieldcnsdr;
	}
	/**
	 * @param fieldcnsdr the fieldcnsdr to set
	 */
	public void setFieldcnsdr(String fieldcnsdr) {
		this.fieldcnsdr = fieldcnsdr;
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
	 * @return the fieldSeq
	 */
	public String getFieldSeq() {
		return fieldSeq;
	}
	/**
	 * @param fieldSeq the fieldSeq to set
	 */
	public void setFieldSeq(String fieldSeq) {
		this.fieldSeq = fieldSeq;
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
