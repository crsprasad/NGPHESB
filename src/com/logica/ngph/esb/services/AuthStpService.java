package com.logica.ngph.esb.services;

import org.jboss.soa.esb.message.Message;

public interface AuthStpService {
	
	public void execute(Message message) throws Exception;

}
