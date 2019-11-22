package com.logica.ngph.action;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.services.ServiceController;
import com.logica.ngph.esb.servicesImpl.ServiceControllerImpl;

public class ResumeMessageAction extends AbstractActionLifecycle{
	     
	static Logger logger = Logger.getLogger(ResumeMessageAction.class);
	protected ConfigTree	_config;
	public ResumeMessageAction(ConfigTree config) { _config = config; } 
	public Message performServiceController(Message message) throws Exception{
		
		NgphCanonical ngphCanonical = (NgphCanonical)message.getBody().get();
		ServiceController serviceController = new ServiceControllerImpl();
		serviceController.performPaymentProcessing(ngphCanonical);
		return message;
	}

}
