package com.logica.ngph.finance.messageparser.sfms.exceptions;

/**
 * Base class for exceptions in WIFE
 * 
 * @author Logica
 */
public class SfmsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * @see RuntimeException#RuntimeException()
	 */
	public SfmsException() {
		super();
	}

	/**
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public SfmsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see RuntimeException#RuntimeException(String)
	 */
	public SfmsException(String message) {
		super(message);
	}

	/**
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public SfmsException(Throwable cause) {
		super(cause);
	}
}
