package com.logica.ngph.esb.Dtos;

import java.io.Serializable;

import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;


public class EventAudit implements Serializable{

	private static final long serialVersionUID = 3517787653828192601L;
	private String auditEventId ;
	private String auditEventDesc;
	private String extraInformation[];
	private String auditTransactionRef;
	private String auditMessageRef;
	private String auditSource;
	private String auditBranch;
	private String auditDept;
	private NgphCanonical canonical;
	private InfoCanonical infoCanonical;
	
	
	
	
	/**
	 * @return the canonical
	 */
	public NgphCanonical getCanonical() {
		return canonical;
	}
	/**
	 * @param canonical the canonical to set
	 */
	public void setCanonical(NgphCanonical canonical) {
		this.canonical = canonical;
	}
	public String getAuditEventId() {
		return auditEventId;
	}
	public void setAuditEventId(String auditEventId) {
		this.auditEventId = auditEventId;
	}
	public String getAuditEventDesc() {
		return auditEventDesc;
	}
	public void setAuditEventDesc(String auditEventDesc) {
		this.auditEventDesc = auditEventDesc;
	}
	public String getAuditTransactionRef() {
		return auditTransactionRef;
	}
	public void setAuditTransactionRef(String auditTransactionRef) {
		this.auditTransactionRef = auditTransactionRef;
	}
	public String getAuditDept() {
		return auditDept;
	}
	public void setAuditDept(String auditDept) {
		this.auditDept = auditDept;
	}
	public String getAuditMessageRef() {
		return auditMessageRef;
	}
	public void setAuditMessageRef(String auditMessageRef) {
		this.auditMessageRef = auditMessageRef;
	}
	public String getAuditSource() {
		return auditSource;
	}
	public void setAuditSource(String auditSource) {
		this.auditSource = auditSource;
	}
	public String getAuditBranch() {
		return auditBranch;
	}
	public void setAuditBranch(String auditBranch) {
		this.auditBranch = auditBranch;
	}

	public String[] getExtraInformation() 
	{
		return extraInformation;
	}
	public void setExtraInformation(String[] extraInformation) {
		this.extraInformation = extraInformation;
	}
	/**
	 * @return the infoCanonical
	 */
	public InfoCanonical getInfoCanonical() {
		return infoCanonical;
	}
	/**
	 * @param infoCanonical the infoCanonical to set
	 */
	public void setInfoCanonical(InfoCanonical infoCanonical) {
		this.infoCanonical = infoCanonical;
	}
	
}
