package com.logica.ngph.esb.services;

import org.jboss.soa.esb.message.Message;
/**
 * @author guptarb
 *
 */
public interface ResponseQueueHandlerService {
	
	public void execute(Message mes) throws Exception;

}
