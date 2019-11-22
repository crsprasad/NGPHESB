/**
 * 
 */
package com.logica.ngph.esb.services;

/**
 * @author guptarb
 *
 */
public interface RequestQueueHandlerService {
	
	public void execute(String message, String providerName, String direction) throws Exception;

}
