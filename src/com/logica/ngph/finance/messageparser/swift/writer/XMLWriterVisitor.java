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
package com.logica.ngph.finance.messageparser.swift.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;

import com.logica.ngph.finance.messageparser.swift.common.exceptions.SwiftException;
import com.logica.ngph.finance.messageparser.swift.model.ISwiftMessageVisitor;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock1;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock2;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock2Input;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock2Output;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock3;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock4;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock5;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlockUser;
import com.logica.ngph.finance.messageparser.swift.model.Tag;
import com.logica.ngph.finance.messageparser.swift.model.UnparsedTextList;
import com.logica.ngph.finance.messageparser.swift.model.WifeSwiftMessage;

/**
 * Main class for XML generation, that is called from {@link WifeSwiftMessage#visit(ISwiftMessageVisitor)}.
 * Presence of blocks is checked by the calling class so the methods below asume that blocks are not null.
 * 
 * @version $Id: XMLWriterVisitor.java,v 1.6 2007/07/25 17:56:09 mikkey Exp $
 */
public class XMLWriterVisitor implements ISwiftMessageVisitor {
	private static transient final java.util.logging.Logger log = java.util.logging.Logger
		.getLogger(XMLWriterVisitor.class.getName());
	
	private static final String EOL = System.getProperties().getProperty("line.separator", "\n");

	private Writer writer;

	/**
	 * Constructor for XMLWriteVisitor from a Writer object
	 * @param writer
	 */
	public XMLWriterVisitor(Writer writer) {
		this.writer = writer;
	}

	////////////////////////////////////////////////////////////
	//
	// MESSAGE HANDLING
	//
	////////////////////////////////////////////////////////////
	public void startMessage(WifeSwiftMessage m) {
		write("<message>");
	}

	public void endMessage(WifeSwiftMessage m) {

		// if message has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (m.getUnparsedTextsSize().intValue() > 0)
			write(m.getUnparsedTexts(), 0);

		write(EOL+"</message>");
	}

	////////////////////////////////////////////////////////////
	//
	// BLOCK 1
	//
	////////////////////////////////////////////////////////////
	public void startBlock1(SwiftBlock1 b) {
		write(EOL+"<block1>");
	}

	public void value(SwiftBlock1  b, String v) {
		
		// generate the attributes for this block
		final StringBuffer sb = new StringBuffer();
		if ( ! b.isEmpty()) {
			appendElement(sb, "applicationId",		b.getApplicationId());
			appendElement(sb, "serviceId",			b.getServiceId());
			appendElement(sb, "logicalTerminal",	b.getLogicalTerminal());
			if (b.getSessionNumber() != null)		// optional for service messages
			appendElement(sb, "sessionNumber",		b.getSessionNumber());
			if (b.getSequenceNumber() != null)		// optional for service messages
			appendElement(sb, "sequenceNumber",		b.getSequenceNumber());
			write(sb.toString());
		};
	}
	
	public void endBlock1(SwiftBlock1 b) {

		// if block has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (b.getUnparsedTextsSize().intValue() > 0)
			write(b.getUnparsedTexts(), 1);

		// write block termination
		write(EOL+"</block1>");
	}

	////////////////////////////////////////////////////////////
	//
	// BLOCK 2
	//
	////////////////////////////////////////////////////////////
	public void startBlock2(SwiftBlock2 b) {

		// decide on the tag to use
		String xmlTag = "<block2>";
		if ( ! b.isEmpty()) {
			if (b instanceof SwiftBlock2Input)
				xmlTag = "<block2 type=\"input\">";
			if (b instanceof SwiftBlock2Output)
				xmlTag = "<block2 type=\"output\">";
		};
		write(EOL + xmlTag);
	}

	public void value(SwiftBlock2  b, String v) {

		// if there is no value (null or empty) => add no nodes
		if (v == null || v.equals(""))
			return;
		
		// generate the attributes for this block
		final StringBuffer sb = new StringBuffer();
		if (b instanceof SwiftBlock2Input) {
			SwiftBlock2Input b2 = (SwiftBlock2Input) b;
			appendElement(sb, "messageType",		b2.getMessageType());
			appendElement(sb, "receiverAddress",	b2.getReceiverAddress());
			if (b2.getMessagePriority() != null)		// optional for service messages
			appendElement(sb, "messagePriority",	b2.getMessagePriority());
			if (b2.getDeliveryMonitoring() != null)		// optional for service messages
			appendElement(sb, "deliveryMonitoring",	b2.getDeliveryMonitoring()); 
			if (b2.getObsolescencePeriod() != null)		// optional for service messages
			appendElement(sb, "obsolescencePeriod",	b2.getObsolescencePeriod());
		};
		if (b instanceof SwiftBlock2Output) {
			SwiftBlock2Output b2 = (SwiftBlock2Output) b;
			appendElement(sb, "messageType",		b2.getMessageType());
			appendElement(sb, "senderInputTime",	b2.getSenderInputTime());
			appendElement(sb, "MIRDate",			b2.getMIRDate());
			appendElement(sb, "MIRLogicalTerminal",	b2.getMIRLogicalTerminal());
			appendElement(sb, "MIRSessionNumber",	b2.getMIRSessionNumber());
			appendElement(sb, "MIRSequenceNumber",	b2.getMIRSequenceNumber());
			appendElement(sb, "receiverOutputDate",	b2.getReceiverOutputDate());
			appendElement(sb, "receiverOutputTime",	b2.getReceiverOutputTime());
			if (b2.getMessagePriority() != null)		// optional for service messages
			appendElement(sb, "messagePriority",	b2.getMessagePriority());
		};
		
		write(sb.toString());
	}

	public void endBlock2(SwiftBlock2 b) {

		// if block has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (b.getUnparsedTextsSize().intValue() > 0)
			write(b.getUnparsedTexts(), 1);

		// write block termination
		write(EOL+"</block2>");
	}

	////////////////////////////////////////////////////////////
	//
	// BLOCK 3
	//
	////////////////////////////////////////////////////////////
	public void startBlock3(SwiftBlock3 b) {
		write(EOL+"<block3>");
	}

	public void tag(SwiftBlock3 b, Tag t) {
		appendTag(t);
	}

	public void endBlock3(SwiftBlock3 b) {

		// if block has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (b.getUnparsedTextsSize().intValue() > 0)
			write(b.getUnparsedTexts(), 1);

		// write block termination
		write(EOL+"</block3>");
	}

	////////////////////////////////////////////////////////////
	//
	// BLOCK 4
	//
	////////////////////////////////////////////////////////////
	public void startBlock4(SwiftBlock4 b) {
		write(EOL+"<block4>");
	}

	public void tag(SwiftBlock4 b, Tag t) {
		appendTag(t);
	}

	public void endBlock4(SwiftBlock4 b) {

		// if block has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (b.getUnparsedTextsSize().intValue() > 0)
			write(b.getUnparsedTexts(), 1);

		// write block termination
		write(EOL+"</block4>");
	}

	////////////////////////////////////////////////////////////
	//
	// BLOCK 5
	//
	////////////////////////////////////////////////////////////
	public void startBlock5(SwiftBlock5 b) {
		write(EOL+"<block5>");
	}

	public void tag(SwiftBlock5 b, Tag t) {
		appendTag(t);
	}

	public void endBlock5(SwiftBlock5 b) {

		// if block has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (b.getUnparsedTextsSize().intValue() > 0)
			write(b.getUnparsedTexts(), 1);

		// write block termination
		write(EOL+"</block5>");
	}

	////////////////////////////////////////////////////////////
	//
	// USER DEFINED BLOCK
	//
	////////////////////////////////////////////////////////////
	public void startBlockUser(SwiftBlockUser b) {
		write(EOL+"<block name=\"" + b.getName() + "\">");
	}

	public void tag(SwiftBlockUser b, Tag t) {
		appendTag(t);
	}

	public void endBlockUser(SwiftBlockUser b) {

		// if block has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (b.getUnparsedTextsSize().intValue() > 0)
			write(b.getUnparsedTexts(), 1);

		// write block termination
		write(EOL+"</block>");
	}

	////////////////////////////////////////////////////////////
	//
	// DEPRECATED
	//
	////////////////////////////////////////////////////////////
	public void tag(SwiftBlock b, Tag t) {
		if (b == null)
			return;
		if (b instanceof SwiftBlock3) {
			tag( (SwiftBlock3) b, t);
		};
		if (b instanceof SwiftBlock4) {
			tag( (SwiftBlock4) b, t);
		};
		if (b instanceof SwiftBlock5) {
			tag( (SwiftBlock5) b, t);
		};
		if (b instanceof SwiftBlockUser) {
			tag( (SwiftBlockUser) b, t);
		};
	}

	////////////////////////////////////////////////////////////
	//
	// INTERNAL METHODS
	//
	////////////////////////////////////////////////////////////
	private final void appendTag(Tag t) {

		// generate the xml tag
		write(EOL+"\t<tag>");
		write(EOL+"\t\t<name>");
		if (t.getName() != null)			// otherwise, null name writes name "null"
			write(t.getName());
		write("</name>");
		write(EOL+"\t\t<value>");
		if (t.getValue() != null)			// otherwise, null value writes value "null" 
			write(t.getValue());
		write("</value>");

		// if tag has unparsed texts, write them down
		//
		// IMPORTANT: do not just "write(m.getUnparsedTexts())" because this latest method actually
		//            creates the object if not already there. Guard this with the size "if" that is
		//            safe (returns 0 if there is no list or real size otherwise).
		if (t.getUnparsedTextsSize().intValue() > 0)
			write(t.getUnparsedTexts(), 2);

		// write tag termination
		write(EOL+"\t</tag>");
	}

	private final void appendElement(StringBuffer sb, String element, String value) {
		sb.append(EOL+"\t<").append(element).append('>')
		.append(value)
		.append("</").append(element).append('>');
	}

	private void write(UnparsedTextList texts, int level) {

		// write prefix
		String prefix = "\t";
		switch (level) {
			case 0:  prefix = "";     break;
			case 1:  prefix = "\t";   break;
			case 2:  prefix = "\t\t"; break;
			default: prefix = "\t";   break;
		}
		
		// write the unparsed texts (if any)
		if (texts.size().intValue() > 0) {
			write(EOL + prefix + "<unparsedTexts>");
			for(int i = 0; i < texts.size().intValue(); i++) {
				write(EOL + prefix + "\t<text>");
				write(texts.getText(new Integer(i)));
				write("</text>");
			};
			write(EOL + prefix + "</unparsedTexts>");
		};
	}

	private void write(String s) {
		try {
			writer.write(s);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Caught exception in XMLWriterVisitor, method write", e);
			throw new SwiftException(e);
		}
	}
}
