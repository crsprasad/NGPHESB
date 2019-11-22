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
package com.logica.ngph.finance.messageparser.swift.common.exceptions;

/**
 * Thrown if a message cannot be identified.
 * 
 * @author mgriffa
 * @version $Id: UnknownMTException.java,v 1.3 2007/06/25 17:35:41 zubri Exp $
 */
public class UnknownMTException extends SwiftException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public UnknownMTException() {
		super();
	}

	/**
	 * Constructor with given text message and cause
	 * @param message
	 * @param cause
	 */
	public UnknownMTException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with given text message, it takes a default cause.
	 * @param message
	 */
	public UnknownMTException(String message) {
		super(message);
	}

	/**
	 * Constructor with given cause, it takes a default message.
	 * @param cause
	 */
	public UnknownMTException(Throwable cause) {
		super(cause);
	}

}
