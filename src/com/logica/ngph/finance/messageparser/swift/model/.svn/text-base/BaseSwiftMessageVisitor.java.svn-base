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
package com.logica.ngph.finance.messageparser.swift.model;

/**
 * Base class for a IMessageVisitor. This class does nothing, implements all
 * methods empty. All methods may be overwritten.
 * 
 * @author mgriffa
 * @version $Id: BaseMessageVisitor.java,v 1.3 2007/07/06 23:42:57 zubri Exp $
 */
//TODO: complete javadocs 
public class BaseSwiftMessageVisitor implements ISwiftMessageVisitor {

	public void startBlock1(SwiftBlock1 b) {
	}

	public void startBlock2(SwiftBlock2 b) {
	}

	public void startBlock3(SwiftBlock3 b) {
	}

	public void startBlock4(SwiftBlock4 b) {
	}

	public void startBlock5(SwiftBlock5 b) {
	}

	public void startBlockUser(SwiftBlockUser b) {
	}

	public void endBlock1(SwiftBlock1 b) {
	}

	public void endBlock2(SwiftBlock2 b) {
	}

	public void endBlock3(SwiftBlock3 b) {
	}

	public void endBlock4(SwiftBlock4 b) {
	}

	public void endBlock5(SwiftBlock5 b) {
	}

	public void endBlockUser(SwiftBlockUser b) {
	}

	/**
	 * This method checks the kind of SwiftBlock passed in and calls the appropriate method.
	 *
	 * @deprecated Override the specific value method for every block type
	 */
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

	public void tag(SwiftBlock3 b, Tag t) {
	}

	public void tag(SwiftBlock4 b, Tag t) {
	}

	public void tag(SwiftBlock5 b, Tag t) {
	}

	public void tag(SwiftBlockUser b, Tag t) {
	}

	public void value(SwiftBlock1 b, String v) {
	}

	public void value(SwiftBlock2 b, String v) {
	}

	public void endMessage(WifeSwiftMessage m) {
	}

	public void startMessage(WifeSwiftMessage m) {
	}

}
