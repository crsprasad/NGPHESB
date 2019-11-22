package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>Base class for SFMS messages.<br />
 * This class is a generic data structure container for WifeSFMS messages.</p>
 * 
 * <p>Instances of this class may have a list of unparsed texts (UnparsedTextList).
 * For easy access, methods have been created that first ensure the lists exists (the
 * real object is created and then call the base method).<br />
 * However, not all the base list methods have been implemented. If you need to use not
 * exposed functionality, retrieve the underlying list with (see getUnparsedTexts method).</p>
 * 
 * @author Prasad. B.S.R.
 * @version 1.0
 */

public class WifeSFMSMessage implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 4913522232370154400L;

    /**
	 * Block 1
	 */
    private SfmsBlock1 block1;
    
    /**
	 * Block 4
	 */
    private SfmsBlock4 block4;

    /**
	 * Block 5
	 */
    private SfmsBlock5 block5;

	/**
	 * List of unparsed texts. For performance reasons, this will be null until really needed.
	 */
	protected UnparsedTextList unparsedTexts = null;

    /**
     * Identification of the message when persisted
     */
    protected Long id;
	private boolean initBlocks = false;

    /**
     * Default constructor.
     * Must be called since here is performed default handler registration
     */
    public WifeSFMSMessage() {
		super();
		initBlocks();
	}

    /**
     * Constructor that initializes blocks
     * @param initBlocks when <code>false</code> the message will not have any blocks when constructed, if <code>true</code> blocks are created, just like with default consturctor
     */
    public WifeSFMSMessage(boolean initBlocks) {
    	super();
    	this.initBlocks = initBlocks;
    	if (initBlocks)
    		initBlocks();
    }

	/**
	 * Constructor for an unparsed text list that initializes blocks
     * @param initBlocks when <code>false</code> the message will not have any blocks when constructed, if <code>true</code> blocks are created, just like with default consturctor
	 * @param unparsedText the list of unparsed texts
	 */
	public WifeSFMSMessage(boolean initBlocks, UnparsedTextList unparsedText) {

		// base constructor
		this(initBlocks);

		// set the unparsed text list
		this.unparsedTexts = unparsedText;
	}

	/**
	 * Constructor for an unparsed text list
	 * @param unparsedText the list of unparsed texts
	 * @see WifeSFMSMessage#SwiftMessage()
	 */
	public WifeSFMSMessage(UnparsedTextList unparsedText) {

		// base constructor
		this();

		// set the unparsed text list
		this.unparsedTexts = unparsedText;
	}

    /**
     * initializes blocks
     */
	private void initBlocks() {
		if (initBlocks) {
			addBlock(new SfmsBlock1());
			addBlock(new SfmsBlock4());
			addBlock(new SfmsBlock5());
		}
	}

	/**
	 * Get the block number specified by b.
	 * 
	 * @param b the block number to retrieve, must be greater or equal to 1 and smaller or equal to 5.
	 * @return the block number specified in this message
	 * @throws IllegalArgumentException if b &lt; 1 or b &gt; 5  
	 */
	public SfmsBlock getBlock(int b) {

		// sanity check
		Validate.isTrue(1 <= b && b <= 5, "block index must be 1-5 (was " + b + ")");

		switch(b) {
    	case 1:
    		return(this.block1);
    	case 4:
    		return(this.block4);
    	}
    	// should not be reached
    	return null;
    }
	
    @Override
	public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    /**
     * Add a block to this message
     * 
     * @param b the block to add, may be <code>null</code> in which case nothing happens
     * @throws IllegalArgumentException if the method getInt in the block returns a value out of range
     */
	public void addBlock(SfmsBlock b) {

		// do nothing if we have a null block
		if (b == null)
			return;
		
		
		Validate.notNull(b.getNumber(), "SfmsBlock.getNumber() is null");
		int index = b.getNumber();
		switch(index) {
    	case 1:
    		setblock1( (SfmsBlock1) b);
    		break;
    	case 4:
    		setBlock4( (SfmsBlock4) b);
    		break;
    	case 5:
    		setBlock5( (SfmsBlock5) b);
    		break;
    	};
	}
	

	
	/**
	 * Visit the current message with the given visitor.
	 * This is a simple implementation of the visitor pattern.
	 * 
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter visitor is <code>null</code>
	 */
	public void visit(ISfmsMessageVisitor visitor) {

	}

	/**
	 * Visit a Block 4 (SfmsBlock4), i.e: call the tag method for block 4
	 * @param block the block containing the tags to visit
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter block or visitor are null
	 */
	public void visit(SfmsBlock4 block, ISfmsMessageVisitor visitor) {
	}
	/**
	 * Get the unique identifier of this message
	 * @return the message id
	 */
	public Long getId() {
		return(this.id);
	}

	/**
	 * Set the unique identifier of this message
	 * @param id the id to be set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Get the number of blocks in this message, including the user blocks
	 * @return an int greater or equal to zero
	 */
	public int getBlockCount() {
		return(this.getBlockCount(Boolean.TRUE));
	}

	/**
	 * Get the number of blocks in this message, optionally including the user blocks.<br>
	 * A block is summed if it is not <code>null</code> and it is not empty.
	 * 
	 * @param includeUserBlocks indicates whether or not user defined blocks should be counted
	 * @return an int greater or equal to zero
	 */
	public int getBlockCount(Boolean includeUserBlocks) {

		// count the basic blocks
		int count = 0;
		if (this.block1 != null && !getBlock1().isEmpty()) count++;
		if (this.block4 != null && !getBlock4().isEmpty()) count++;
		if (this.block5 != null && !getBlock5().isEmpty()) count++;

		return(count);
	}

	/**
	 * Get block number 1 of this message, may be <code>null</code> if not set
	 * @return the block 1 of the message or <code>null</code>
	 */
	public SfmsBlock1 getBlock1() {
		return(this.block1);
	}

	/**
	 * Set the block 1 of the message
	 * @param block1 the content of the block 1
	 */
	public void setblock1(SfmsBlock1 block1) {
		this.block1 = block1;
	}
	
	/**
	 * Get block number B of this message, may be <code>null</code> if not set
	 * @return the block B of the message or <code>null</code>
	 */
	public SfmsBlock4 getBlock4() {
		return(this.block4);
	}


	/**
	 * Set the block B of the message
	 * @param blockB the content of the block 1
	 */
	public void setBlock4(SfmsBlock4 block4) {
		this.block4 = block4;
	}
	
	/**
	 * Get block number 5 of this message, may be <code>null</code> if not set
	 * @return the block B of the message or <code>null</code>
	 */
	public SfmsBlock5 getBlock5() {
		return(this.block5);
	}


	/**
	 * Set the block B of the message
	 * @param blockB the content of the block 1
	 */
	public void setBlock5(SfmsBlock5 block5) {
		this.block5 = block5;
	}
	/**
	 * remove all blocks from these message, including user blocks
	 */
	public void clear() {

		// release all blocks
		this.block1 = null;
		this.block4 = null;
		this.block5 = null;
	}	

	/**
	 * Checks if the message is a fragment
	 * @return true if the message is a fragment
	 */
	public Boolean isFragment() {

		// get the block 4 (if exists)
		SfmsBlock4 bb = this.getBlock4();
		if (bb != null) {
			String t202 = bb.getTagValue("202");
			String t203 = bb.getTagValue("203");

			// if both tag exists => this is a fragment
			return(t202 != null && t203 != null ? Boolean.TRUE : Boolean.FALSE); 
		};

		return(Boolean.FALSE);
	}

	/**
	 * Checks if the message is the last fragment
	 * @return true if the message is the last fragment of a fragmented message
	 */
	public Boolean isLastFragment() {

		if ( ! this.isFragment().booleanValue())
			return(Boolean.FALSE);

		try {
			Integer count  = this.fragmentCount();
			Integer number = this.fragmentNumber();
			return(count.intValue() == number.intValue() ? Boolean.TRUE : Boolean.FALSE);
		} catch (UnsupportedOperationException uoe) {
		};
		
		return(Boolean.FALSE);
	}
		
	/**
	 * Gets the total number of fragments of a fragmented message as informed in tag 203.
	 * 
	 * @return the total number of fragments or zero if the message is not fragmented
	 */
	public Integer fragmentCount() {

		// if this is not a fragment => 0
		if ( ! this.isFragment().booleanValue())
			return new Integer(0);

		// get the block 4 and tag 203 (they BOTH exists here)
		SfmsBlock4 bb = this.getBlock4();
		String t203 = bb.getTagValue("203");

		// process the number
		Integer _t203;
		try {
			_t203 = new Integer(Integer.parseInt(t203, 10));
		} catch (NumberFormatException nfe) {
			throw new UnsupportedOperationException("message is not a fragment");
		};

		return(_t203);
	}

	/**
	 * Gets the number of this fragment
	 * 
	 * @return the number of this fragment
	 * @throws UnsupportedOperationException if the message is not a part of a fragmented message
	 */
	public Integer fragmentNumber() {

		// if this is not a fragment => 0
		if ( ! this.isFragment().booleanValue())
			throw new UnsupportedOperationException("message is not a fragment");

		// get the block 4 and tag 203 (they BOTH exists here)
		SfmsBlock4 b4 = this.getBlock4();
		String t202 = b4.getTagValue("202");

		// process the number
		Integer _t202;
		try {
			_t202 = new Integer(Integer.parseInt(t202, 10));
		} catch (NumberFormatException nfe) {
			throw new UnsupportedOperationException("message is not a fragment");
		};

		return(_t202);
	}

	/**
	 * verifies that the unparsed text list exists
	 */
	protected void unparsedTextVerify() {
		if (this.unparsedTexts == null)
			this.unparsedTexts = new UnparsedTextList();
	}

	/**
	 * returns the unparsed text list
	 * @return the unparsed text attached to this message
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
	 * @return the count of unparsed texts attached to this message
	 */
	public Integer getUnparsedTextsSize() {

		// no list => size is zero...
		if (this.unparsedTexts == null)
			return new Integer(0);
		
		return this.unparsedTexts.size();
	}

	/**
	 * decides if a specific text (by index) is likely a SWIFT FIN message. Exceptions are inherited from
	 * base implementation methods.
	 * @param index the unparsed text number
	 * @return true if the unparsed text at position index is a full SWIFT message
	 * @throws IllegalArgumentException if parameter index is <code>null</code>
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds
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
	 * get an unparsed text as a parsed swift message
	 * @param index the unparsed text number
	 * @return the unparsed text at position index parsed into a WifeSFMSMessage object
	 * @throws IllegalArgumentException if parameter index is <code>null</code> 
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
}
