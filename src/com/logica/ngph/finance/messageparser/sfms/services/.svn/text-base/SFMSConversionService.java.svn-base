package com.logica.ngph.finance.messageparser.sfms.services;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import org.apache.commons.lang.Validate;

import com.logica.ngph.finance.messageparser.sfms.exceptions.SfmsException;
import com.logica.ngph.finance.messageparser.sfms.model.WifeSFMSMessage;
import com.logica.ngph.finance.messageparser.sfms.parser.SFMSParser;


/**
 * This interface provides a general conversion service between three different formats:
 * <ul>
 * 	<li><b>FIN</b>: SWIFT message format as used by SWIFTNet (ISO 15022 compliance).</li>
 *  <li><b>SwiftMessage</b>: WIFE's java object model of SWIFT messages.</li>
 * </ul>
 * <p>This class may be used as a serializer.</p>
 *  
 * @author Logica
 * @version 
 */
public class SFMSConversionService implements ISFMSConversionService {
	
	/** 
	 * Given a SwiftMessage object returns a String containing its SWIFT message representation.
	 * 
	 * @see com.logica.ngph.finance.messageparser.sfms.services.ISFMSConversionService#getFIN(com.logica.sfms.swift.model.SwiftMessage)
	 */
	public String getFIN(WifeSFMSMessage msg) {
		Validate.notNull(msg);
		//final SFMSWriter w = new SFMSWriter();	
		final StringWriter writer = new StringWriter();
		//w.writeMessage(msg, writer);
		return writer.getBuffer().toString();
	}


	/** 
	 * Given a Sfms message String returns a WifeSFMSMessage object.
	 * 
	 * @see com.logica.ngph.finance.messageparser.sfms.services.ISFMSConversionService#getMessageFromFIN(java.lang.String)
	 */
	public WifeSFMSMessage getMessageFromFIN(String fin) {
		Validate.notNull(fin);
		SFMSParser p = new SFMSParser(new ByteArrayInputStream(fin.getBytes()));
		try {
			return p.message();
		} catch (SfmsException e) {
			throw new SfmsException(e+" during parse of message");
		}
	}
}
