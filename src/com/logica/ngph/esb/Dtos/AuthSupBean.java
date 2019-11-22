package com.logica.ngph.esb.Dtos;

public class AuthSupBean {
	String groupID = null;
	String groupName = null;
	String supId= null;
	String supSeq = null;
	String isMan= null;

	public AuthSupBean(String groupID,String groupName,String supId,String supSeq,String isMan)
	{
		this.groupID=groupID;
		this.groupName=groupName;
		this.supId=supId;
		this.supSeq=supSeq;
		this.isMan=isMan;
	}

	/**
	 * @return the groupID
	 */
	public String getGroupID() {
		return groupID;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @return the supId
	 */
	public String getSupId() {
		return supId;
	}

	/**
	 * @return the supSeq
	 */
	public String getSupSeq() {
		return supSeq;
	}

	/**
	 * @return the isMan
	 */
	public String getIsMan() {
		return isMan;
	}

	/**
	 * @param groupID the groupID to set
	 */
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @param supId the supId to set
	 */
	public void setSupId(String supId) {
		this.supId = supId;
	}

	/**
	 * @param supSeq the supSeq to set
	 */
	public void setSupSeq(String supSeq) {
		this.supSeq = supSeq;
	}

	/**
	 * @param isMan the isMan to set
	 */
	public void setIsMan(String isMan) {
		this.isMan = isMan;
	}
}
