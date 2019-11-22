package com.logica.ngph.finance.messageparser.sfms.model;

/**
 * Base class for a IMessageVisitor. This class does nothing, implements all
 * methods empty. All methods may be overwritten.
 * 
 * @author Logica
 * @version 
 */

public class BaseSfmsMessageVisitor implements ISfmsMessageVisitor {

	public void startBlock1(SfmsBlock1 b) {
	}


	public void startBlock4(SfmsBlock4 b) {
	}

	public void startBlock5(SfmsBlock5 b) {
	}

	public void endBlock1(SfmsBlock1 b) {
	}

	public void endBlock4(SfmsBlock4 b) {
	}

	public void endBlock5(SfmsBlock5 b) {
	}

	/**
	 * This method checks the kind of SfmsBlock passed in and calls the appropriate method.
	 *
	 * 
	 */
	public void tag(SfmsBlock b, Tag t) {
		if (b == null)
			return;
		if (b instanceof SfmsBlock4) {
			tag( (SfmsBlock4) b, t);
		};
		if (b instanceof SfmsBlock5) {
			tag( (SfmsBlock5) b, t);
		};
	}

	public void tag(SfmsBlock4 b, Tag t) {
	}

	public void tag(SfmsBlock5 b, Tag t) {
	}

	public void value(SfmsBlock1 b, String v) {
	}

	public void endMessage(WifeSFMSMessage m) {
	}

	public void startMessage(WifeSFMSMessage m) {
	}

}
