package com.logica.ngph.esb.Dtos;

/**
 * 
 * @author mohdabdulaa
 *
 */
public class MessageFormats
{
	//@FORMAT_CHANNEL
	private String msgChannel;
	
	//@FORMAT_MSGTYPE
	private String msgType;
	
	//@FORMAT_MSGSUBTYPE
	private String msgSubType;
	
	//@FORMAT_FIELD_SEQ
	private int fieldSeq;
	
	//@FORMAT_FIELD_NO
	private String fieldNo;
	
	//@FORMAT_FIELD_ISRECURSIVE
	private int isFieldRecursive;
	
	//@FORMAT_FIELD_MANDATORY
	private int isFieldMandatory;
	
	//@FORMAT_FIELDTAG
	private String fieldTag;
	
	//@FORMAT_TAG_SEQ
	private int tagSeq;
	
	//@FORMAT_TAG_COMPONENT
	private int tagComponent;
	
	//@FORMAT_COMPONENT_MANDATORY
	private int isComponentMandatory;
	
	//@FORMAT_COMP_MINLENGTH
	private int componentMinLength;
	
	//@FORMAT_COMP_MAXLENGTH
	private int componentMaxLength;
	
	//@FORMAT_COMP_OUTLF
	private int componentOutLef;
	
	//@FORMAT_COMP_PREFIX
	private String componentPrefix;
	
	//@FORMAT_COMP_SUFFIX
	private String componentSufix;
	
	//@FORMAT_COMP_TYPE
	private int componentType;
	
	//@FORMAT_COMP_DEFAULTVALUE
	private String componentDefaultVal;

	public String getMsgChannel() {
		return msgChannel;
	}

	public void setMsgChannel(String msgChannel) {
		this.msgChannel = msgChannel;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsgSubType() {
		return msgSubType;
	}

	public void setMsgSubType(String msgSubType) {
		this.msgSubType = msgSubType;
	}

	public int getFieldSeq() {
		return fieldSeq;
	}

	public void setFieldSeq(int fieldSeq) {
		this.fieldSeq = fieldSeq;
	}

	public String getFieldNo() {
		return fieldNo;
	}

	public void setFieldNo(String fieldNo) {
		this.fieldNo = fieldNo;
	}

	public int getIsFieldRecursive() {
		return isFieldRecursive;
	}

	public void setIsFieldRecursive(int isFieldRecursive) {
		this.isFieldRecursive = isFieldRecursive;
	}

	public int getIsFieldMandatory() {
		return isFieldMandatory;
	}

	public void setIsFieldMandatory(int isFieldMandatory) {
		this.isFieldMandatory = isFieldMandatory;
	}

	public String getFieldTag() {
		return fieldTag;
	}

	public void setFieldTag(String fieldTag) {
		this.fieldTag = fieldTag;
	}

	public int getTagSeq() {
		return tagSeq;
	}

	public void setTagSeq(int tagSeq) {
		this.tagSeq = tagSeq;
	}

	public int getTagComponent() {
		return tagComponent;
	}

	public void setTagComponent(int tagComponent) {
		this.tagComponent = tagComponent;
	}

	public int getIsComponentMandatory() {
		return isComponentMandatory;
	}

	public void setIsComponentMandatory(int isComponentMandatory) {
		this.isComponentMandatory = isComponentMandatory;
	}

	public int getComponentMinLength() {
		return componentMinLength;
	}

	public void setComponentMinLength(int componentMinLength) {
		this.componentMinLength = componentMinLength;
	}

	public int getComponentMaxLength() {
		return componentMaxLength;
	}

	public void setComponentMaxLength(int componentMaxLength) {
		this.componentMaxLength = componentMaxLength;
	}

	public int getComponentOutLef() {
		return componentOutLef;
	}

	public void setComponentOutLef(int componentOutLef) {
		this.componentOutLef = componentOutLef;
	}

	public String getComponentPrefix() {
		return componentPrefix;
	}

	public void setComponentPrefix(String componentPrefix) {
		this.componentPrefix = componentPrefix;
	}

	public String getComponentSufix() {
		return componentSufix;
	}

	public void setComponentSufix(String componentSufix) {
		this.componentSufix = componentSufix;
	}

	public int getComponentType() {
		return componentType;
	}

	public void setComponentType(int componentType) {
		this.componentType = componentType;
	}

	public String getComponentDefaultVal() {
		return componentDefaultVal;
	}

	public void setComponentDefaultVal(String componentDefaultVal) {
		this.componentDefaultVal = componentDefaultVal;
	}
}
