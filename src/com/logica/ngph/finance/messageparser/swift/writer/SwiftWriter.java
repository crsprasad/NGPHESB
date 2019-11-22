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
/*
 * Created on Sep 6, 2005
 *
 */
package com.logica.ngph.finance.messageparser.swift.writer;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang.Validate;

import com.logica.ngph.finance.messageparser.swift.model.WifeSwiftMessage;

/**
 * This class handles writing swift messages exclusively, 
 * all validation and consistency checks must be done
 * previous to using the writer.
 * 
 * @author mgriffa
 * @version $Id: SwiftWriter.java,v 1.4 2007/07/25 17:56:08 mikkey Exp $ 
 */
//FIXME when validation component is done add link here
public class SwiftWriter 
{
    //private static transient final java.util.logging.Logger log = java.util.logging.Logger.getLogger( SwiftWriter.class );

    /**
     * Write the given message to writer.
     * 
     * @param msg the message to write
     * @param writer the writer that will actually receive all the write operations
     * @throws IllegalArgumentException if msg or writer are <code>null</code>
     */
    public void writeMessage(WifeSwiftMessage msg, Writer writer) {
    	Validate.notNull(msg , "msg cannot be null");
    	Validate.notNull(writer, "writer cannot be null");
    	FINWriterVisitor v = new FINWriterVisitor(writer);
    	msg.visit(v);
    }
    
    /**
     * Get a string with the internal xml representation of a message.
     * @param msg the message to write 
     * @return a string with an internal xml representation of the message
     * @throws IllegalArgumentException if msg is <code>null</code>
     */
    public String getInternalXml(WifeSwiftMessage msg) {
    	Validate.notNull(msg , "msg cannot be null");
    	StringWriter w = new StringWriter();
    	XMLWriterVisitor visitor = new XMLWriterVisitor(w);
    	msg.visit(visitor);
    	return w.toString();
    }
}
