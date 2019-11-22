package com.logica.ngph.finance.messageparser.sfms.services;

import com.logica.ngph.finance.messageparser.sfms.model.WifeSFMSMessage;

/**
 * This interface provides a general conversion service between three different formats:
 * <ul>
 * 	<li><b>FIN</b>: SFMS message format as used by INFYNet (ISO 15022 compliance).</li>
 *  <li><b>WifeSFMSMessage</b>: SFMS's java object model of SFMS messages.</li>
 * </ul>
 *  
 * @author Logica
 * @version 
 */
public interface ISFMSConversionService {
	
	/**
	 * Gets a String containing the FIN message from the msg object 
	 * argument, using FIN writer.
	 * 
	 * @param msg an object containing the message to convert
	 * @return a string with the FIN format representation of the message
	 * 
	 * @throws IllegalArgumentException if msg is <code>null</code>
	 */
	public abstract String getFIN(WifeSFMSMessage msg);


	/**
	 * Gets a message object containing the message data 
	 * from the FIN string message passed as argument.
	 * 
	 * @param fin a string containing the FIN message to convert
	 * @return a swift object containing the message data
	 * 
	 * @throws IllegalArgumentException if fin is <code>null</code>
	 */
	public abstract WifeSFMSMessage getMessageFromFIN(String fin);


}