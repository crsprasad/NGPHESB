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
 * Created on March 31, 2007 
 */
package com.logica.ngph.finance.messageparser.swift.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base class for SWIFT <b>Body Block (block 4)</b>.<br>
 * This block is where the actual message content is specified 
 * and is what most users see. Generally the other blocks are 
 * stripped off before presentation. It mainly contains a list of
 * tags and its format representation, which is variable 
 * length and requires use of CRLF as a field delimiter.<br>
 * 
 * @author zubri, mikkey
 * @since 4.0
 * @version $Revision: 1.6 $
 */
public class SwiftBlock4 extends SwiftTagListBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public SwiftBlock4() {

	}

	/**
	 * Constructor with tag initialization
	 * @param tags the list of tags to initialize
	 * @throws IllegalArgumentException if parameter tags is <code>null</code>
	 * @throws IllegalArgumentException if parameter tags is not composed of Strings
	 * @since 5.0
	 */
	public SwiftBlock4(List tags) {

		// sanity check
		Validate.notNull(tags, "parameter 'tags' cannot be null");
		Validate.allElementsOfType(tags, Tag.class, "parameter 'tags' may only have Tag elements");

		this.addTags(tags);
	}

	/**
	 * Sets the block number. Will cause an exception unless setting block number to 4.
	 * @param blockNumber the block number to set
	 * @throws IllegalArgumentException if parameter blockName is not the integer 4
	 * @since 5.0
	 */
	protected void setBlockNumber(Integer blockNumber) {

		// sanity check
		Validate.notNull(blockNumber, "parameter 'blockNumber' cannot be null");
		Validate.isTrue(blockNumber.intValue() == 4, "blockNumber must be 4");
	}

	/**
	 * Sets the block name. Will cause an exception unless setting block number to "4".
	 * @param blockName the block name to set
	 * @throws IllegalArgumentException if parameter blockName is not the string "4"
	 * @since 5.0
	 */
	protected void setBlockName(String blockName) {

		// sanity check
		Validate.notNull(blockName, "parameter 'blockName' cannot be null");
		Validate.isTrue(blockName.compareTo("4") == 0, "blockName must be string '4'");
	}

	/**
	 * Returns the block number (the value 4 as an integer)
	 * @return Integer containing the block's number
	 */
	public Integer getNumber() {
		return new Integer(4);
	}

	/**
	 * Returns the block name (the value 4 as a string)
	 * @return block name
	 * 
	 * @since 5.0
	 */
	public String getName() {
		return("4");
	}

	/**
	 * convert this to string
	 */
	public String toString() {
		return(ToStringBuilder.reflectionToString(this));
	}

	/**
	 * Iterates the internal list of tags and returns true if there is at least one tag with the given number <code>i</code>
	 * This method is useful to search any variant of a tag
	 * <code>containsTag(58)</code> will return <code>true</code> if there is any variant of 58A, 58D, or so.
	 * 
	 * @param i the tag number to search in tags
	 * @return <code>true</code> if there is a tag with the given number, no matter if the tag is or not a letter option (letter option may be any or empty)
	 */
	public boolean containsTag(int i) {
		if (super.tags == null || super.tags.isEmpty())
			return false;
		for (Iterator it = tags.iterator() ; it.hasNext() ; ) {
			Tag t = (Tag) it.next();
			if (t.isNumber(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Search and retrieve the first tag with the given number.
	 * 
	 * @param i the tagname to search
	 * @return the first tag with the given number or <code>null</code> if no tag is found.
	 * @see #containsTag(int)
	 */
	public Tag getTagByNumber(int i) {
		if (super.tags != null && !super.tags.isEmpty()) {
			for (Iterator it = tags.iterator() ; it.hasNext() ; ) {
				Tag t = (Tag) it.next();
				if (t.isNumber(i)) {
					return t;
				}
			}
		}
		return null;
	}
}
