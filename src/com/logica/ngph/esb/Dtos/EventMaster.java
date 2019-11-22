package com.logica.ngph.esb.Dtos;

import java.io.Serializable;

public class EventMaster implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5198656227870928982L;
	private String eventId;
	private String eventDesc;
	private int eventAlertable;
	private String eventSeverity;
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getEventDesc() {
		return eventDesc;
	}
	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}
	public int getEventAlertable() {
		return eventAlertable;
	}
	public void setEventAlertable(int eventAlertable) {
		this.eventAlertable = eventAlertable;
	}
	public String getEventSeverity() {
		return eventSeverity;
	}
	public void setEventSeverity(String eventSeverity) {
		this.eventSeverity = eventSeverity;
	}
	
	

}
