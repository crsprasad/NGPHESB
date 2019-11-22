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
package com.logica.ngph.finance.messageparser.swift.parser;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock1;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock2;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock2Input;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock2Output;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock3;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock4;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlock5;
import com.logica.ngph.finance.messageparser.swift.model.SwiftBlockUser;
import com.logica.ngph.finance.messageparser.swift.model.SwiftTagListBlock;
import com.logica.ngph.finance.messageparser.swift.model.Tag;
import com.logica.ngph.finance.messageparser.swift.model.UnparsedTextList;
import com.logica.ngph.finance.messageparser.swift.model.WifeSwiftMessage;

import org.apache.commons.lang.Validate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the main parser for WIFE's XML internal representation.<br>
 * The supported XML format is consider <i>internal</i> because it is an ad-hoc
 * defined XML structure for Swift messages, so it's not the SWIFT XML
 * Standard for FIN Messages.<br>
 * <br>
 * 
 * This implementation should be used by calling some of the the conversion
 * services.
 * 
 * @see net.sourceforge.wife.services.IConversionService
 * @since 5.0
 * @author zubri
 * @version $Id: XMLParser.java,v 1.9 2007/07/25 17:56:08 mikkey Exp $
 */
public class XMLParser {
	private static transient final java.util.logging.Logger log = java.util.logging.Logger.getLogger(XMLParser.class.getName());
	static {
		log.info("$Id: XMLParser.java,v 1.9 2007/07/25 17:56:08 mikkey Exp $");
	}

	/**
	 * Given a String containing a message in its WIFE internal XML
	 * representation, returns a WifeSwiftMessage object.
	 * If there is any error during conversion this method returns <code>null</code>
	 * @param xml the string containing the XML to parse
	 * @return the XML parsed into a WifeSwiftMessage object
	 * 
	 * @see net.sourceforge.wife.services.IConversionService#getMessageFromXML(java.lang.String)
	 */
	public WifeSwiftMessage parse(String xml) {
		Validate.notNull(xml);
		try {
			final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
			return createMessage(doc);
		} catch (Exception e) {
			log.log(Level.WARNING, "Error parsing XML", e);
			return null;
		}
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * 
	 * @param doc
	 *            Document object containing a message in XML format
	 * @return WifeSwiftMessage object populated with the given XML message data
	 */
	private WifeSwiftMessage createMessage(Document doc) {
		final NodeList messageNL = doc.getElementsByTagName("message");

		if (messageNL.getLength() == 1) {
			Node message = messageNL.item(0);
			WifeSwiftMessage m = new WifeSwiftMessage(false);

			NodeList blocksNL = message.getChildNodes();
			if (log.isLoggable(Level.FINE))
				log.fine("blocks in message: " + blocksNL.getLength());

			for (int i = 0; i < blocksNL.getLength(); i++) {
				Node blockNode = blocksNL.item(i);
				if (log.isLoggable(Level.FINE))
					log.fine("evaluating node " + blockNode.getNodeName());
				if (blockNode.getNodeType()==Node.ELEMENT_NODE) {
					String blockName = blockNode.getNodeName();

					if (blockName.equalsIgnoreCase("block1")) {
						m.setBlock1(getBlock1FromNode(blockNode));
					} else if (blockName.equalsIgnoreCase("block2")) {
						m.setBlock2(getBlock2FromNode(blockNode));
					} else if (blockName.equalsIgnoreCase("unparsedtexts")) {
						// unparsed texts at <message> level
						m.setUnparsedTexts(getUnparsedTextsFromNode(blockNode));
					} else {
						// blocks 3, 4, 5 or user blocks
						m.addBlock(getTagListBlockFromNode(blockNode));
					}
				}
			} // end block list iteration
			return m;
		} else {
			throw new IllegalArgumentException("<message> tag not found");
		}
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Given the <block1> node in the XML tree, returns the SwiftBlock1 object.
	 * 
	 * @param blockNode Node object of the <block1> tag in the XML message
	 * @return SwiftBlock1 object populated with the given portion of the XML message
	 */
	private SwiftBlock1 getBlock1FromNode(Node blockNode) {
		NodeList fields = blockNode.getChildNodes();
		if (log.isLoggable(Level.FINE))
			log.fine(fields.getLength() + " children in <block1>");

		SwiftBlock1 b1 = new SwiftBlock1();

		for (int i = 0; i < fields.getLength(); i++) {
			Node n = fields.item(i);
			if ("APPLICATIONID".equalsIgnoreCase(n.getNodeName())) {
				b1.setApplicationId(getText(n));
			} else if ("SERVICEID".equalsIgnoreCase(n.getNodeName())) {
				b1.setServiceId(getText(n));
			} else if ("LOGICALTERMINAL".equalsIgnoreCase(n.getNodeName())) {
				b1.setLogicalTerminal(getText(n));
			} else if ("SESSIONNUMBER".equalsIgnoreCase(n.getNodeName())) {
				b1.setSessionNumber(getText(n));
			} else if ("SEQUENCENUMBER".equalsIgnoreCase(n.getNodeName())) {
				b1.setSequenceNumber(getText(n));
			} else if ("unparsedTexts".equalsIgnoreCase(n.getNodeName())) {
				b1.setUnparsedTexts(getUnparsedTextsFromNode(n));
			}
		}

		return b1;
	}

	private String getText(Node n) {
		String text = null;
		Node c = n.getFirstChild();
		if (c.getNodeType() == Node.TEXT_NODE ) {
			text = c.getNodeValue().trim();
		} else {
			log.warning("Node is not TEXT_NODE: "+c);
		}
		log.fine("text: "+text);
		return text;
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Given the <block2> node in the XML tree, returns the SwiftBlock1 object.
	 * The method checks for the "type" attribute in the <block2> tag and
	 * returns a SwiftBlock2Input or SwiftBlock2Output.
	 * 
	 * @param blockNode Node object of the <block2> tag in the XML message
	 * @return SwiftBlock2 object populated with the given portion of the XML message
	 * @see #getBlock2InputFromNode(Node)
	 * @see #getBlock2OutputFromNode(Node)
	 */
	private SwiftBlock2 getBlock2FromNode(Node blockNode) {
		String type = getNodeAttribute(blockNode, "type");

		if (type == null) {
			log.severe("atrribute 'type' was expected but not found at <block2> xml tag");
			return null;
		} else if (type.equals("input")) {
			return getBlock2InputFromNode(blockNode);
		} else if (type.equals("output")) {
			return getBlock2OutputFromNode(blockNode);
		} else {
			log.severe("expected 'input' or 'output' value for 'type' atribute at <block2> xml tag, and found: " + type);
			return null;
		}
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Given the <block2 type="input"> node in the XML tree, returns the
	 * SwiftBlock2Input object.
	 * 
	 * @param blockNode Node object of the <block2> tag in the XML message
	 * @return SwiftBlock2Input object populated with the given portion of the XML message
	 */
	private SwiftBlock2Input getBlock2InputFromNode(Node blockNode) {
		NodeList fields = blockNode.getChildNodes();
		if (log.isLoggable(Level.FINE))
			log.fine(fields.getLength() + " childrens in <block2 type=\"input\">");

		SwiftBlock2Input b2 = new SwiftBlock2Input();

		for (int i = 0; i < fields.getLength(); i++) {
			Node n = fields.item(i);
			if ("MESSAGETYPE".equalsIgnoreCase(n.getNodeName()))
				b2.setMessageType(getText(n));
			else if ("RECEIVERADDRESS".equalsIgnoreCase(n.getNodeName()))
				b2.setReceiverAddress(getText(n));
			else if ("MESSAGEPRIORITY".equalsIgnoreCase(n.getNodeName()))
				b2.setMessagePriority(getText(n));
			else if ("DELIVERYMONITORING".equalsIgnoreCase(n.getNodeName()))
				b2.setDeliveryMonitoring(getText(n));
			else if ("OBSOLESCENCEPERIOD".equalsIgnoreCase(n.getNodeName()))
				b2.setObsolescencePeriod(getText(n));
			else if ("unparsedTexts".equalsIgnoreCase(n.getNodeName()))
				b2.setUnparsedTexts(getUnparsedTextsFromNode(n));
		}

		return b2;
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Given the <block2 type="output"> node in the XML tree, returns the
	 * SwiftBlock2Output object.
	 * 
	 * @param blockNode Node object of the <block2> tag in the XML message
	 * @return SwiftBlock2Output object populated with the given portion of the XML message
	 */
	private SwiftBlock2Output getBlock2OutputFromNode(Node blockNode) {
		NodeList fields = blockNode.getChildNodes();
		if (log.isLoggable(Level.FINE))
			log.fine(fields.getLength() + " childrens in <block2 type=\"output\">");

		SwiftBlock2Output b2 = new SwiftBlock2Output();

		for (int i = 0; i < fields.getLength(); i++) {
			Node n = fields.item(i);
			if ("MESSAGETYPE".equalsIgnoreCase(n.getNodeName()))
				b2.setMessageType(getText(n));
			else if ("SENDERINPUTTIME".equalsIgnoreCase(n.getNodeName()))
				b2.setSenderInputTime(getText(n));
			else if ("MIRDATE".equalsIgnoreCase(n.getNodeName()))
				b2.setMIRDate(getText(n));
			else if ("MIRLOGICALTERMINAL".equalsIgnoreCase(n.getNodeName()))
				b2.setMIRLogicalTerminal(getText(n));
			else if ("MIRSESSIONNUMBER".equalsIgnoreCase(n.getNodeName()))
				b2.setMIRSessionNumber(getText(n));
			else if ("MIRSEQUENCENUMBER".equalsIgnoreCase(n.getNodeName()))
				b2.setMIRSequenceNumber(getText(n));
			else if ("RECEIVEROUTPUTDATE".equalsIgnoreCase(n.getNodeName()))
				b2.setReceiverOutputDate(getText(n));
			else if ("RECEIVEROUTPUTTIME".equalsIgnoreCase(n.getNodeName()))
				b2.setReceiverOutputTime(getText(n));
			else if ("MESSAGEPRIORITY".equalsIgnoreCase(n.getNodeName()))
				b2.setMessagePriority(getText(n));
			else if ("unparsedTexts".equalsIgnoreCase(n.getNodeName()))
				b2.setUnparsedTexts(getUnparsedTextsFromNode(n));
		}

		return b2;
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Given the <block3>, <block4>, <block5> or <block> (user block) node in
	 * the XML tree, returns the corresponding SwiftTagListBlock object
	 * populated with the given portion of the XML message.
	 * 
	 * @param blockNode Node object of the <block3>, <block4>, <block5> or <block> tag in the XML message
	 * @return SwiftTagListBlock object populated with the given portion of the XML message
	 */
	private SwiftTagListBlock getTagListBlockFromNode(Node blockNode) {
		String blockName = blockNode.getNodeName();
		SwiftTagListBlock b = null;
		if (blockName.equalsIgnoreCase("block3")) {
			b = new SwiftBlock3();
		} else if (blockName.equalsIgnoreCase("block4")) {
			b = new SwiftBlock4();
		} else if (blockName.equalsIgnoreCase("block5")) {
			b = new SwiftBlock5();
		} else if (blockName.equalsIgnoreCase("block")) {
			String name = getNodeAttribute(blockNode, "name");
			if (name != null)
				b = new SwiftBlockUser(name);
			else
				b = new SwiftBlockUser();
		} else {
			return null;
		}
		
		NodeList fields = blockNode.getChildNodes();
		if (log.isLoggable(Level.FINE))
			log.fine(fields.getLength() + " children in tag list " + blockName);

		for (int j = 0; j < fields.getLength(); j++) {
			Node t = fields.item(j);
			if ("tag".equalsIgnoreCase(t.getNodeName())) {
				Tag tag = getTag(t);
				b.addTag(tag);
			} else if ("unparsedtexts".equalsIgnoreCase(t.getNodeName())) {
				b.setUnparsedTexts(getUnparsedTextsFromNode(t));
			}
		}

		return b;
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Parses the given <tag> Node and returns a Tag object containing data from
	 * the expected <name> and <value> tags. If name or value are not found as
	 * childs of the given node, the Tag object is returned with empty values.
	 * 
	 * @param t the XML node to parse for name-value pair
	 * @return a Tag object containing the name and value of the given XML node.
	 */
	private Tag getTag(Node t) {
		Tag tag = new Tag();
		NodeList children = t.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if ("name".equalsIgnoreCase(n.getNodeName())) {
				tag.setName(getText(n));
			}
			if ("value".equalsIgnoreCase(n.getNodeName())) {
				tag.setValue(getText(n));
			} else if ("unparsedtexts".equalsIgnoreCase(n.getNodeName())) {
				tag.setUnparsedTexts(getUnparsedTextsFromNode(n));
			}
		}
		return tag;
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Given the <unparsedtexts> node in the XML tree, returns an
	 * UnparsedTextList object populated with the contents of the <text> child
	 * tags of <unparsedtexts>.
	 * 
	 * @param blockNode Node object of the <unparsedtexts> tag in the XML message
	 * @return UnparsedTextList object populated with the given <text> tags content of the <unparsedtexts>
	 */
	private UnparsedTextList getUnparsedTextsFromNode(Node blockNode) {
		UnparsedTextList unparsedTexts = new UnparsedTextList();

		NodeList texts = blockNode.getChildNodes();
		if (log.isLoggable(Level.FINE))
			log.fine(texts.getLength() + " children in <unparsedtexts>");
		for (int j = 0; j < texts.getLength(); j++) {
			Node t = texts.item(j);
			if ("text".equalsIgnoreCase(t.getNodeName())) {
				unparsedTexts.addText(getText(t));
			}
		}
		return unparsedTexts;
	}

	/**
	 * Helper method for XML representation parsing.<br>
	 * Gets the value of an expected attribute in a Node.
	 * 
	 * @param n Node to analyze to find the attribute
	 * @param attributeName the attribute name expected in the analyzed Node n
	 * @return the value of the attribute expected, or null if the attribute was not found
	 */
	private String getNodeAttribute(Node n, String attributeName) {
		Node attr = n.getAttributes().getNamedItem(attributeName);
		if (attr == null || !attr.getNodeName().equals(attributeName)) {
			return null;
		} else {
			return attr.getNodeValue();
		}
	}
}
