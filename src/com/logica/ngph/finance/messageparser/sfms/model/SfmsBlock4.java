package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base class for SFMS Body Block (block 4)>
 * This block is where the actual message content is specified 
 * and is what most users see. Generally the other blocks are 
 * stripped off before presentation. It mainly contains a list of
 * tags and its format representation, which is variable 
 * length and requires use of CRLF as a field delimiter.
 * 
 * @author Logica
 * @version 
 */
public class SfmsBlock4 extends SfmsTagListBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public SfmsBlock4() {

	}

	/**
	 * Constructor with tag initialization
	 * @param tags the list of tags to initialize
	 * @throws IllegalArgumentException if parameter tags is <code>null</code>
	 * @throws IllegalArgumentException if parameter tags is not composed of Strings
	 */
	public SfmsBlock4(List tags) {

		this.addTags(tags);
	}


	/**
	 * Returns the block number (the value 4 as an integer)
	 * @return Integer containing the block's number
	 */
	@Override
	public Integer getNumber() {
		return new Integer(4);
	}

	/**
	 * Returns the block name (the value 4 as a string)
	 * @return block name
	 * 
	 */
	@Override
	public String getName() {
		return("4");
	}

	/**
	 * convert this to string
	 */
	@Override
	public String toString() {
		return(ToStringBuilder.reflectionToString(this));
	}

	/**
	 * Iterates the internal list of tags and returns true if there is at least one tag with the given number i</code>
	 * This method is useful to search any variant of a tag
	 * 
	 * @param i the tag number to search in tags
	 * @return true if there is a tag with the given number, no matter if the tag is or not a letter option (letter option may be any or empty)
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

	@Override
	protected void setBlockName(String blockName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setBlockNumber(Integer blockNumber) {
		// TODO Auto-generated method stub
		
	}
}
