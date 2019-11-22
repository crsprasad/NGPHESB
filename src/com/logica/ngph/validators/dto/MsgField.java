package com.logica.ngph.validators.dto;

import java.util.ArrayList;


/*
 * Pojo Class for data Transportation.
 * Fetch the Data row Wise from TA_Fields_Format and map the column value to the corresponding variable.
 */
public class MsgField {
	
	/*
	 * FIELD_NO
	 * FIELD_COMP_FORMAT
	 * FIELD_COMP_SEQ
	 * FIELD_COMP_MANDOPT
	 * 
	 */
	
	//Table Level Variables
	private String fldNo;
	private String fldCompFmt;
	private String fldCompSeq;
	private String fldCompManOpt;
	
	// Extra Variables Required
	private boolean isSlash;
	private boolean isPling;
	private String charType;
	private int lengthOfField;
	private int noOfLines;
	private String consideration;
	private String eoCompIndctr;
	private String fldCodeWrds;
	
	/**
	 * @return the fldCodeWrds
	 */
	public String getFldCodeWrds() {
		return fldCodeWrds;
	}
	/**
	 * @param fldCodeWrds the fldCodeWrds to set
	 */
	public void setFldCodeWrds(String fldCodeWrds) {
		this.fldCodeWrds = fldCodeWrds;
	}
	/**
	 * @return the fldNo
	 */
	public String getFldNo() {
		return fldNo;
	}
	/**
	 * @return the fldCompFmt
	 */
	public String getFldCompFmt() {
		return fldCompFmt;
	}
	/**
	 * @return the fldCompSeq
	 */
	public String getFldCompSeq() {
		return fldCompSeq;
	}
	/**
	 * @return the consideration
	 */
	public String getConsideration() {
		return consideration;
	}
	/**
	 * @return the eoCompIndctr
	 */
	public String getEoCompIndctr() {
		return eoCompIndctr;
	}
	/**
	 * @param consideration the consideration to set
	 */
	public void setConsideration(String consideration) {
		this.consideration = consideration;
	}
	/**
	 * @param eoCompIndctr the eoCompIndctr to set
	 */
	public void setEoCompIndctr(String eoCompIndctr) {
		this.eoCompIndctr = eoCompIndctr;
	}
	/**
	 * @return the fldCompManOpt
	 */
	public String getFldCompManOpt() {
		return fldCompManOpt;
	}
	/**
	 * @return the isSlash
	 */
	public boolean isSlash() {
		return isSlash;
	}
	/**
	 * @return the isPling
	 */
	public boolean isPling() {
		return isPling;
	}
	/**
	 * @return the charType
	 */
	public String getCharType() {
		return charType;
	}
	/**
	 * @return the lengthOfField
	 */
	public int getLengthOfField() {
		return lengthOfField;
	}
	/**
	 * @return the noOfLines
	 */
	public int getNoOfLines() {
		return noOfLines;
	}
	/**
	 * @param fldNo the fldNo to set
	 */
	public void setFldNo(String fldNo) {
		this.fldNo = fldNo;
	}
	/**
	 * @param fldCompFmt the fldCompFmt to set
	 */
	public void setFldCompFmt(String fldCompFmt) {
		this.fldCompFmt = fldCompFmt;
	}
	/**
	 * @param fldCompSeq the fldCompSeq to set
	 */
	public void setFldCompSeq(String fldCompSeq) {
		this.fldCompSeq = fldCompSeq;
	}
	/**
	 * @param fldCompManOpt the fldCompManOpt to set
	 */
	public void setFldCompManOpt(String fldCompManOpt) {
		this.fldCompManOpt = fldCompManOpt;
	}
	/**
	 * @param isSlash the isSlash to set
	 */
	public void setSlash(boolean isSlash) {
		this.isSlash = isSlash;
	}
	/**
	 * @param isPling the isPling to set
	 */
	public void setPling(boolean isPling) {
		this.isPling = isPling;
	}
	/**
	 * @param charType the charType to set
	 */
	public void setCharType(String charType) {
		this.charType = charType;
	}
	/**
	 * @param lengthOfField the lengthOfField to set
	 */
	public void setLengthOfField(int lengthOfField) {
		this.lengthOfField = lengthOfField;
	}
	/**
	 * @param noOfLines the noOfLines to set
	 */
	public void setNoOfLines(int noOfLines) {
		this.noOfLines = noOfLines;
	}
	

}
