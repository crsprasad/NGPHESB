package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base class for SFMS Trailer Block (block 5). Each SFMS message has one or
 * more trailers as required by the message exchange and security requirements.
 * System trailers, if applicable, follow user trailers.
 * 
 * @author Logica
 * @version
 */
public class SfmsBlock5 extends SfmsTagListBlock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1449213733480227996L;

	/**
	 * Default constructor
	 */
	public SfmsBlock5() {

	}

	/**
	 * Constructor with tag initialization
	 * 
	 * @param tags
	 *            the list of tags to initialize
	 * @throws IllegalArgumentException
	 *             if parameter tags is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if parameter tags is not composed of Strings
	 */
	public SfmsBlock5(List tags) {

		this.addTags(tags);
	}

	/**
	 * Sets the block number. Will cause an exception unless setting block
	 * number to 5.
	 * 
	 * @param blockNumber
	 *            the block number to set
	 * @throws IllegalArgumentException
	 *             if parameter blockName is not the integer 5
	 */
	@Override
	protected void setBlockNumber(Integer blockNumber) {

	}

	/**
	 * Sets the block name. Will cause an exception unless setting block number
	 * to "5".
	 * 
	 * @param blockName
	 *            the block name to set
	 * @throws IllegalArgumentException
	 *             if parameter blockName is not the string "5"
	 */
	@Override
	protected void setBlockName(String blockName) {

	}

	/**
	 * Returns the block number (the value 5 as an integer)
	 * 
	 * @return Integer containing the block's number
	 */
	@Override
	public Integer getNumber() {
		return new Integer(5);
	}

	/**
	 * Returns the block name (the value 5 as a string)
	 * 
	 * @return block name
	 */
	@Override
	public String getName() {
		return ("5");
	}

	/**
	 * convert this to string
	 */
	@Override
	public String toString() {
		return (ToStringBuilder.reflectionToString(this));
	}
}
