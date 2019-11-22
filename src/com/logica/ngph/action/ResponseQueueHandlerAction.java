package com.logica.ngph.action;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.esb.services.RequestQueueHandlerService;
import com.logica.ngph.esb.services.ResponseQueueHandlerService;
import com.logica.ngph.utils.ApplicationContextProvider;

public class ResponseQueueHandlerAction extends AbstractActionLifecycle {
	
	protected ConfigTree _config;
	static Logger logger = Logger.getLogger(ResponseQueueHandlerAction.class);
	public ResponseQueueHandlerAction(ConfigTree config) {	_config = config; }
	public ResponseQueueHandlerAction() {}
	
	private ResponseQueueHandlerService responseQueueHandlerService;
	/**
	 * @return the responseQueueHandlerService
	 */
	public ResponseQueueHandlerService getResponseQueueHandlerService() {
		return responseQueueHandlerService;
	}
	/**
	 * @param responseQueueHandlerService the responseQueueHandlerService to set
	 */
	public void setResponseQueueHandlerService(
			ResponseQueueHandlerService responseQueueHandlerService) {
		this.responseQueueHandlerService = responseQueueHandlerService;
	}


	// Main Execution Point
	public void execute(Message message) throws Exception 
	{
		responseQueueHandlerService = (ResponseQueueHandlerService) ApplicationContextProvider.getBean("responseQueueHandlerService");
		responseQueueHandlerService.execute(message);
	}
	
	 public void exceptionHandler(Message message, Throwable exception)
	 {
		  logger.error("=============================== Response action ExceptionHandler Start==========================");
		  logger.error(message,exception);
		  logger.error("For Message: ");
		  logger.error(message.getBody().get());
		  logger.error("****************************** Response action ExceptionHandler End ***************************");
	 }


}
