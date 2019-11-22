package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base class for a generic SFMS block.
 * This is an abstract class so specific block classes for each block (block 1, 2, 3, etc...)
 * should be instantiated.
 * Instances of this class may have a list of unparsed texts (UnparsedTextList).
 * For easy access, methods have been created that first ensure the lists exists 
 * (the  real object is created and then call the base method).
 * However, not all the base list methods have been implemented. If you need to use not
 * exposed functionality, retrieve the underlying list with (see getUnparsedTexts method)
 *
 * @author Logica
 * @version 
 */
public abstract class SfmsBlock implements Serializable {

	private static final long serialVersionUID = 5251016175365021925L;

	/**
	 * Unique identifier of the SFMS block.
	 * Mainly used for persistence services.
	 */
	protected Long id;

	/**
	 * List of unparsed texts. For performance reasons, this will be null until really needed.
	 */
	protected UnparsedTextList unparsedTexts = null;

	/**
	 * Default constructor, shouldn't be used normally.
	 * DO NOT USE: present only for subclasses
	 */
	public SfmsBlock() {

	}

	/**
	 * Constructor for an unparsed text list
	 * @param unparsedText the list of unparsed texts
	 */
	public SfmsBlock(UnparsedTextList unparsedText) {

		// set the unparsed text list
		this.unparsedTexts = unparsedText;
	}

	/**
	 * Sets the block number (this method is to be overwrite for derived classes).
	 * @param blockNumber the block number to set
	 * 
	 */
	protected abstract void setBlockNumber(Integer blockNumber);

	/**
	 * Sets the block name (this method is to be overwrite for derived classes).
	 * @param blockName the block name to set
	 * 
	 */
	protected abstract void setBlockName(String blockName);

	/**
	 * Returns the block number (this method is to be overwritten for derived classes).
	 * @return Integer containing the block's number
	 */
	public abstract Integer getNumber();

	/**
	 * Returns the block name (this method is to be overwritten for derived classes).
	 * @return block name
	 * 
	 */
	public abstract String getName();

	/**
	 * Get the unique identifier of this block or <code>null</code> if it is not set
	 * @return the unique identifier 
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the unique identifier of this block
	 * @param id the unique identifier to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}
			
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
		
	/**
	 * Tell if this block is a block that contains a list of tags (3-5) or is a block with fixed length value (1-2)
	 * @return true if this object contains a list of tags (which may be empty or null
	 */
	public boolean isTagBlock() {
		return this instanceof SfmsTagListBlock;
	}

	/**
	 * verifies that the unparsed text list exists
	 */
	protected void unparsedTextVerify() {
		if (this.unparsedTexts == null)
			this.unparsedTexts = new UnparsedTextList();
	}

	/**
	 * Returns the unparsed text list attached to the Block.
	 * @return the unparsed texts attached to the block
	 */
	public UnparsedTextList getUnparsedTexts() {
		// create the list if needed
		unparsedTextVerify();
		return(this.unparsedTexts);
	}

	/**
	 * sets the list of unparsed texts
	 * @param texts the new list of unparsed texts (may be null)
	 */
	public void setUnparsedTexts(UnparsedTextList texts) {
		this.unparsedTexts = texts;
	}

	/**
	 * returns the size of the unparsed text list
	 * @return the count of unparsed test attached to the block
	 */
	public Integer getUnparsedTextsSize() {
		// no list => size is zero...
		if (this.unparsedTexts == null)
			return new Integer(0);
		return this.unparsedTexts.size();
	}

	/**
	 * decides if a specific text (by index) is likely a SFMS message. Exceptions are inherited from
	 * base implementation methods.
	 * @param index the unparsed text number
	 * @throws IllegalArgumentException if parameter index is null
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds
	 * @return true if the unparsed text at position index is a full SWIFT Message
	 */
	public Boolean unparsedTextIsMessage(Integer index) {
		// create the list if needed
		unparsedTextVerify();
		return(this.unparsedTexts.isMessage(index));
	}

	/**
	 * get an unparsed text
	 * @param index the unparsed text number
	 * @return the requested text
	 * @throws IllegalArgumentException if parameter index is <code>null</code>
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds
	 */
	public String unparsedTextGetText(Integer index) {
		// create the list if needed
		unparsedTextVerify();
		return(this.unparsedTexts.getText(index));
	}

	/**
	 * get an unparsed text as a parsed SFMS message
	 * @param index the unparsed text number
	 * @throws IllegalArgumentException if parameter index is <code>null</code> 
	 * @return the blocks unparsed text at position index, parsed into a WifeSFMSMessage object
	 */
	public WifeSFMSMessage unparsedTextGetAsMessage(Integer index) {
		// create the list if needed
		unparsedTextVerify();
		return(this.unparsedTexts.getTextAsMessage(index));
	}

	/**
	 * adds a new unparsed text
	 * @param text the unparsed text to append
	 * @throws IllegalArgumentException if parameter text is <code>null</code> 
	 */
	public void unparsedTextAddText(String text) {
		// create the list if needed
		unparsedTextVerify();
		this.unparsedTexts.addText(text);
	}

	/**
	 * adds a new unparsed text from a message
	 * @param message the message to be appended
	 * @throws IllegalArgumentException if parameter message is <code>null</code> 
	 */
	public void unparsedTextAddText(WifeSFMSMessage message) {
		// create the list if needed
		unparsedTextVerify();
		this.unparsedTexts.addText(message);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unparsedTexts == null) ? 0 : unparsedTexts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SfmsBlock other = (SfmsBlock) obj;
		if (unparsedTexts == null) {
			if (other.unparsedTexts != null)
				return false;
		} else if (!unparsedTexts.equals(other.unparsedTexts))
			return false;
		return true;
	}
}
