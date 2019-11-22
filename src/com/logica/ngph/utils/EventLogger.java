package com.logica.ngph.utils;

import org.apache.log4j.Logger;

import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.AuditServiceClient;
import com.logica.ngph.esb.Dtos.EventAudit;

/**
 * @author guptarb
 * 
 * A Generic Class to perform Event Logging.
 */

public class EventLogger {
	
	static Logger logger = Logger.getLogger(EventLogger.class);
	
	/**
	 private String auditEventId ;
	 private String auditEventDesc;
	 private String extraInformation[];
	 private String auditTransactionRef;
	 private String auditMessageRef;
	 private String auditSource;
	 private String auditBranch;
	 private String auditDept;
	*/

	/**
	 * This Method will be called from Various Services.
	 * It will populate the auditDTO and put this Object in JMS Queue.
	 * 
	 */
	public static void logEvent(String eventId, NgphCanonical canonical, Class<?> auditSource, String auditMessageRef) 
	{
		EventAudit audit = new EventAudit();
		
		audit.setAuditEventId(eventId);
		audit.setAuditSource(auditSource.getName());
		audit.setCanonical(canonical);
		audit.setAuditMessageRef(auditMessageRef);
		
		new AuditServiceClient().sendEventLog(audit);
	}
	public static void logEvent(String eventId, Class<?> auditSource, String auditMessageRef, String[] extraInfo) 
	{
		EventAudit audit = new EventAudit();
		
		audit.setAuditEventId(eventId);
		audit.setAuditSource(auditSource.getName());
		audit.setAuditMessageRef(auditMessageRef);
		audit.setExtraInformation(extraInfo);
		
		new AuditServiceClient().sendEventLog(audit);
		
	}
	public static void logEvent(String eventId, Class<?> auditSource, InfoCanonical infoCanonical,  String auditMessageRef) 
	{
		EventAudit audit = new EventAudit();
		
		audit.setAuditEventId(eventId);
		audit.setAuditSource(auditSource.getName());
		audit.setInfoCanonical(infoCanonical);
		audit.setAuditMessageRef(auditMessageRef);
		
		new AuditServiceClient().sendEventLog(audit);
	}
	

}
