package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;

/**
 * Base class for SFMS blocks that contain its fields concatenated as a single fixed length value; blocks 1 and 2.
 * This is an abstract class so specific block classes for each block should be instantiated.
 *
 * @author Logica
 * @version 
 */
public abstract class SfmsValueBlock extends SfmsBlock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2508657915389068738L;

	/**
	 * Default constructor, shouldn't be used normally.
	 * present only for subclasses
	 */
	protected SfmsValueBlock() {

	}

	/**
	 * This method should be overwritten by subclasses, calling 
	 * this method will throw a java.lang.UnsupportedOperationException
	 */
	public String getBlockValue() {
		throw new UnsupportedOperationException("cannot call getBlockValue on SfmsValueBlock, must be on specific subclasses");
	}

	/**
	 * This method should be overwritten by subclasses, calling 
	 * this method will throw a java.lang.UnsupportedOperationException
	 * 
	 * @return the blocks value as a single string
	 */
	public String getValue() {
		throw new UnsupportedOperationException("cannot call getValue on SfmsValueBlock, must be on specific subclasses");
	}

	/**
	 * This method should be overwritten by subclasses and proper 
	 * parsing be done to set specific values of the block, calling 
	 * this method will throw a java.lang.UnsupportedOperationException
	 * 
	 * @param value the blocks value as a single string
	 */
	public void setValue(String value) {
		throw new UnsupportedOperationException("cannot call setValue on SfmsValueBlock, must be on specific subclasses");
	}

	/**
	 * This method should be overwritten by subclasses, calling 
	 * this method will throw a java.lang.UnsupportedOperationException
	 */
	public void setBlockValue(String value) {
		throw new UnsupportedOperationException("cannot call setBlockValue on SfmsValueBlock, must be on specific subclasses");
	}
	
	/**
	 * Tells if the block contains at least one field.
	 * This method must be called on specific subclasses, calling it for SfmsValueBlock will throw 
	 * a java.lang.UnsupportedOperationException
	 */
	public boolean isEmpty() {
		// this.getValue will throw if not overwritten
		return(this.getValue() == null);
	}
	
	/**
	 * Tells the block's string value size (in chars).
	 * NOTICE this does not return the actual number of fields set
	 * because value blocks are mostly fixed length.
	 * This method must be called on specific subclasses, calling it for SfmsValueBlock will throw 
	 * a java.lang.UnsupportedOperationException
	 */
	public int size() {
		// this.getValue will throw if not overwritten
		return(this.getValue() != null ? this.getValue().length() : 0);
	}

	/**
	 * returns a fragment of the block value received (or null if value is not large enough).<br />
	 * This method is used in derived classes to get value fragments. 
	 * @param value the full block value
	 * @param start the starting point of the fragment in the big block value
	 * @param size the fragment size
	 * @return the value fragment or null if value is not large enough
	 */
	protected String getValuePart(String value, int start, int size) {

		// prepare the result
		String s = null;

		// check start is within bounds
		if (start < value.length()) {

			// check start+size is within bounds
			if ( (start + size) >= value.length())
				size = value.length() - start;
			
			// get the fragment
			try {
				s = value.substring(start, start + size);
			} catch (IndexOutOfBoundsException ob) {
			};
		};
		
		return(s);
	}
}
