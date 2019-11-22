package com.logica.ngph.action;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.esb.services.ISMSHandler;
import com.logica.ngph.utils.ApplicationContextProvider;

public class SMSHandlerAction extends AbstractActionLifecycle {
	
	static Logger logger = Logger.getLogger(SMSHandlerAction.class);
	protected ConfigTree	_config;
	public SMSHandlerAction(ConfigTree config) { _config = config; }

	private ISMSHandler ismsHandler;

	public void setIsmsHandler(ISMSHandler ismsHandler) {
		this.ismsHandler = ismsHandler;
	}

	public void doProcess(Message message) throws Exception
	{
		if(message!=null)
		{
			try
			{
				ismsHandler = (ISMSHandler)ApplicationContextProvider.getBean("ismsHandler");
				ismsHandler.generateMMID(message.getBody().get().toString());
			}
			catch (Exception e) {
				logger.error(e, e);
			}
		}
		else
		{
			logger.error("Null message received by SMSHandlerAction");
		}
		
	}
}
