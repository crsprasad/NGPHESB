package com.logica.ngph.esb.Dtos;

/**
 * 
 * @author mohdabdulaa
 * THis class represents as entity for table SERVICEORCH
 * this table contains the details of service configuration.
 *
 */
public class ServiceConfigDetails
{
	/**
	 * We are using the following standard serviceIds for the services across the Product/Application
	 * 9001 ENTITY CONTROL
	 * 9002 AUTO ROUTER
	 * 9003 VALIDATE
	 * 9004 INTERVENE
	 * 9005 WAREHOUSE 
	 * 9006 FX
	 * 9007 ACCOUONTING
	 * 9008 ENRICH
	 * 9009 CUT-OFF CONTROL
	 * 9010 MANDATE CONTROL
	 * 9011 RETURNS/COVERS
	 * 9012 NOTIFICATIONS/CONFIRMATIONS
	 * 9013 LM LINK
	 * 9014 AML LINK
	 * 9015 BILLING LINK
	 * 9016 AUTHENTICATE
	 * 9017 BULK
	 * 9018 AUTHORISE
	 * 9019 REFERENCE GENERATE
	 * 9020 AUDIT
	 */
	//@Cloumn SRVC_MSG_TYPE
	//Message type of the message
	private String serviceMessageType;
	
	//@Column SRVC_MSG_SUBTYPE
	//The sub type of the message
	private String serviceMsgSubType;
	
	//@Column SRVC_SERVICEID
	//The class name or ID of the service
	private String serviceId;
	
	//@Column SRVC_CALLSEQ
	//The call sequence of the service in the message flow
	private int serviceCallSeq;

	public String getServiceMessageType() {
		return serviceMessageType;
	}

	public void setServiceMessageType(String serviceMessageType) {
		this.serviceMessageType = serviceMessageType;
	}

	public String getServiceMsgSubType() {
		return serviceMsgSubType;
	}

	public void setServiceMsgSubType(String serviceMsgSubType) {
		this.serviceMsgSubType = serviceMsgSubType;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public int getServiceCallSeq() {
		return serviceCallSeq;
	}

	public void setServiceCallSeq(int serviceCallSeq) {
		this.serviceCallSeq = serviceCallSeq;
	}

}
