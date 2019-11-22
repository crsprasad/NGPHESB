/* 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
*/
package com.logica.ngph.finance.messageparser.swift.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;

import com.logica.ngph.finance.messageparser.swift.common.exceptions.SwiftException;
import com.logica.ngph.finance.messageparser.swift.model.WifeSwiftMessage;
import com.logica.ngph.finance.messageparser.swift.parser.SwiftParser;
import com.logica.ngph.finance.messageparser.swift.parser.XMLParser;
import com.logica.ngph.finance.messageparser.swift.writer.SwiftWriter;
import com.logica.ngph.finance.messageparser.swift.writer.XMLWriterVisitor;


import org.apache.commons.lang.Validate;


/**
 * This interface provides a general conversion service between three different formats:
 * <ul>
 * 	<li><b>FIN</b>: SWIFT message format as used by SWIFTNet (ISO 15022 compliance).</li>
 *  <li><b>XML</b>: WIFE's XML representation of SWIFT messages.</li>
 *  <li><b>WifeSwiftMessage</b>: WIFE's java object model of SWIFT messages.</li>
 * </ul>
 * <p>This class may be used as a serializer.</p>
 *  
 * @author mgriffa
 * @version $Id: ConversionService.java,v 1.5 2007/07/25 17:56:09 mikkey Exp $
 */
public class SwiftConversionService implements ISwiftConversionService {
	private static transient final java.util.logging.Logger log = java.util.logging.Logger.getLogger(SwiftConversionService.class.getName());
	static {
		log.info("$Id: ConversionService.java,v 1.5 2007/07/25 17:56:09 mikkey Exp $");
	}
	
	/** 
	 * Given a WifeSwiftMessage object returns a String containing its SWIFT message representation.
	 * 
	 * @see net.sourceforge.wife.services.IConversionService#getFIN(net.sourceforge.wife.swift.model.WifeSwiftMessage)
	 */
	public String getFIN(WifeSwiftMessage msg) {
		Validate.notNull(msg);
		final SwiftWriter w = new SwiftWriter();
		final StringWriter writer = new StringWriter();
		w.writeMessage(msg, writer);
		return writer.getBuffer().toString();
	}

	/** 
	 * Given a String containing a message in its Wife XML internal representation, returns a String
	 * containing its SWIFT message representation.
	 * 
	 * @see net.sourceforge.wife.services.IConversionService#getFIN(java.lang.String)
	 */
	public String getFIN(String xml) {
		Validate.notNull(xml);
		WifeSwiftMessage msg = getMessageFromXML(xml);
		if (msg == null)
			throw new RuntimeException("WifeSwiftMessage is null");
		return getFIN(msg);
	}

	/** 
	 * Given a WifeSwiftMessage objects returns a String containing WIFE internal XML representation of the message.
	 * 
	 * @see net.sourceforge.wife.services.IConversionService#getXml(net.sourceforge.wife.swift.model.WifeSwiftMessage)
	 */
	public String getXml(WifeSwiftMessage msg) {
		Validate.notNull(msg);
		StringWriter w = new StringWriter();
		msg.visit(new XMLWriterVisitor(w));
		String xml = w.getBuffer().toString();
		if (log.isLoggable(Level.FINE)) log.fine("xml: "+xml);
		return xml;
	}

	/** 
	 * Given a Swift message String returns a String containing WIFE internal XML representation of the message.
	 * 
	 * @see net.sourceforge.wife.services.IConversionService#getXml(java.lang.String)
	 */
	public String getXml(String fin) {
		Validate.notNull(fin);
		WifeSwiftMessage msg = this.getMessageFromFIN(fin);
		return getXml(msg);
	}

	/** 
	 * Given a Swift message String returns a WifeSwiftMessage object.
	 * 
	 * @see net.sourceforge.wife.services.IConversionService#getMessageFromFIN(java.lang.String)
	 */
	public WifeSwiftMessage getMessageFromFIN(String fin) {
		Validate.notNull(fin);
		SwiftParser p = new SwiftParser(new ByteArrayInputStream(fin.getBytes()));
		try {
			return p.message();
		} catch (IOException e) {
			throw new SwiftException(e+" during parse of message");
		}
	}
	
	/**
	 * Given a String containing a message in its WIFE internal XML representation, returns a WifeSwiftMessage object.
	 * 
	 * @see net.sourceforge.wife.services.IConversionService#getMessageFromXML(java.lang.String)
	 */
	public WifeSwiftMessage getMessageFromXML(String xml) {
		return (new XMLParser().parse(xml));
	}

}
