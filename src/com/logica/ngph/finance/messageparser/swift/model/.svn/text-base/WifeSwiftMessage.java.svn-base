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
 * Created on Sep 23, 2005 
 */
package com.logica.ngph.finance.messageparser.swift.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;



/**
 * <p>Base class for swift messages.<br />
 * This class is a generic data structure container for SWIFT messages.</p>
 * 
 * <p>Instances of this class may have a list of unparsed texts (UnparsedTextList).
 * For easy access, methods have been created that first ensure the lists exists (the
 * real object is created and then call the base method).<br />
 * However, not all the base list methods have been implemented. If you need to use not
 * exposed functionality, retrieve the underlying list with (see getUnparsedTexts method).</p>
 * 
 * @author mgriffa
 * @version $Id: WifeSwiftMessage.java,v 1.9 2007/07/25 17:56:08 mikkey Exp $
 */
//TODO: add parameter checks (Validate.*) and complete javadocs 
public class WifeSwiftMessage implements Serializable {
    private static transient final java.util.logging.Logger log = java.util.logging.Logger.getLogger(WifeSwiftMessage.class.getName());
    /**
     * Handlers that know message-specific logic.
     * @deprecated
     */
    protected transient Map handlers = new HashMap();
   
    /**
	 * Block 1
	 */
    private SwiftBlock1 block1;
    
    /**
	 * Block 2
	 */
    private SwiftBlock2 block2;
    
    /**
	 * Block 3
	 */
    private SwiftBlock3 block3;
    
    /**
	 * Block 4
	 */
    private SwiftBlock4 block4;
    
    /**
	 * Block 5
	 */
    private SwiftBlock5 block5;
	
	/**
	 * User defined blocks
	 *
	 * @since 5.0
	 */
	protected List userBlocks;
	
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
     * @see #WifeSwiftMessage(boolean) 
     */
    public WifeSwiftMessage() {
		super();
		initBlocks();
	}

    /**
     * Constructor that initializes blocks
     * TODO document hibernate didn't work
     * @param initBlocks when <code>false</code> the message will not have any blocks when constructed, if <code>true</code> blocks are created, just like with default consturctor
     */
    public WifeSwiftMessage(boolean initBlocks) {
    	super();
    	this.initBlocks = initBlocks;
    	if (initBlocks)
    		initBlocks();
    }

	/**
	 * Constructor for an unparsed text list that initializes blocks
     * @param initBlocks when <code>false</code> the message will not have any blocks when constructed, if <code>true</code> blocks are created, just like with default consturctor
	 * @param unparsedText the list of unparsed texts
	 * @see WifeSwiftMessage#SwiftMessage()
	 */
	public WifeSwiftMessage(boolean initBlocks, UnparsedTextList unparsedText) {

		// base constructor
		this(initBlocks);

		// set the unparsed text list
		this.unparsedTexts = unparsedText;
	}

	/**
	 * Constructor for an unparsed text list
	 * @param unparsedText the list of unparsed texts
	 * @see WifeSwiftMessage#SwiftMessage()
	 */
	public WifeSwiftMessage(UnparsedTextList unparsedText) {

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
			addBlock(new SwiftBlock1());
			addBlock(new SwiftBlock2Input());
			addBlock(new SwiftBlock3());
			addBlock(new SwiftBlock4());
			addBlock(new SwiftBlock5());
			this.userBlocks = new ArrayList();
		}
	}

	/**
	 * Get the block number specified by b.
	 * 
	 * @param b the block number to retrieve, must be greater or equal to 1 and smaller or equal to 5.
	 * @return the block number specified in this message
	 * @throws IllegalArgumentException if b &lt; 1 or b &gt; 5  
	 */
	public SwiftBlock getBlock(int b) {

		// sanity check
		Validate.isTrue(1 <= b && b <= 5, "block index must be 1-5 (was " + b + ")");

		switch(b) {
    	case 1:
    		return(this.block1);
    	case 2:
    		return(this.block2);
    	case 3:
    		return(this.block3);
    	case 4:
    		return(this.block4);
    	case 5:
    		return(this.block5);
    	}
    	// should not be reached
    	return null;
    }
	
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
//            StringBuffer buffer = new StringBuffer();
//            buffer.append("[WifeSwiftMessage:");
//            buffer.append(" id: ");
//            buffer.append(id);
//            buffer.append(" block1: ");
//            buffer.append(block1);
//            buffer.append(" block2: ");
//            buffer.append(block2);
//            buffer.append(" block3: ");
//            buffer.append(block3);
//            buffer.append(" block4: ");
//            buffer.append(block4);
//            buffer.append(" block5: ");
//            buffer.append(block5);
//            buffer.append("]");
//            return buffer.toString();
    }
    
    /**
     * Add a block to this message
     * FIXME this is not a good implementation, must be reworked 
     * 
     * @param b the block to add, may be <code>null</code> in which case nothing happens
     * @throws IllegalArgumentException if the method getInt in the block returns a value out of range
     */
	public void addBlock(SwiftBlock b) {

		// do nothing if we have a null block
		// FIXME this should be an exception. Check usage on other sources
		if (b == null)
			return;
		if (log.isLoggable(Level.FINE)) log.fine("Add block " + b);
		
		// support for user blocks in this method is useful for XML parser and other code that
		// takes advantages of using SwiftTagListBlock
		if (b instanceof SwiftBlockUser) {
			addUserBlock((SwiftBlockUser) b);
			return;
		};
		
		Validate.notNull(b.getNumber(), "SwiftBlock.getNumber() is null");
		int index = b.getNumber().intValue();
		Validate.isTrue(index >= 1 && index <= 5, "SwiftBlock.getNumber int did not return an int between 1-5");
		switch(index) {
    	case 1:
    		setBlock1( (SwiftBlock1) b);
    		break;
    	case 2:
    		setBlock2( (SwiftBlock2) b);
    		break;
    	case 3:
    		setBlock3( (SwiftBlock3) b);
    		break;
    	case 4:
    		setBlock4( (SwiftBlock4) b);
    		break;
    	case 5:
    		setBlock5( (SwiftBlock5) b);
    		break;
    	};
	}
	
	/**
	 * Attempt to identify the current message type (MT).
	 * 
	 * @param type must be a valid registered handler id
	 * @return <code>true</code> if this message is successfully identified as the given MT and <code>false</code> in other case	 * 
	 * @throws IllegalArgumentException if parameter type is <code>null</code> or not a valid type (i.e: 3 chars len)
	 * @see SwiftBlock#getMessageType()
	 * @see #getType()
	 * 
	 */
	public boolean isMT(String type) {

		// sanity check
		Validate.notNull(type);
		Validate.isTrue(type.length() == 3, "The string must be exactly 3 chars size (type=" + type + ")");

		return(getType() != null && getType().equals(type));
	}
//	 * @see #registerHandlers()
//	 * @see #addHandler(IMessageHandler)
//		if (!this.handlers.containsKey(type)) {
//			throw new IllegalArgumentException("Handler with id '"+type+"' not registered");
//		}
//		// FIXME maybe handler registry should be separated and shared?  this is probably safer
//		IMessageHandler hnd = (IMessageHandler) this.handlers.get(type);
//		hnd.initialize(this);
//		return hnd.isType();

	
	/**
	 * Tell the message type associated with this object if a block 2 is present.
	 * 
	 * @return a String containing the SWIFT numeric code for the message types or <code>null</code> if the message does not have a block 2.
	 * @see SwiftBlock2Input#getMessageType()
	 * @see SwiftBlock2Output#getMessageType()
	 */
	public String getType() {
		final SwiftBlock2 b2 = getBlock2();
		if (b2==null)
			return null;
		else if (b2 instanceof SwiftBlock2Input)
			return ((SwiftBlock2Input)b2).getMessageType();
		else if (b2 instanceof SwiftBlock2Output)
			return ((SwiftBlock2Output)b2).getMessageType();
		else return null;
	}
	
	/**
	 * Visit the current message with the given visitor.
	 * This is a simple implementation of the visitor pattern.
	 * 
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter visitor is <code>null</code>
	 */
	public void visit(ISwiftMessageVisitor visitor) {

		Validate.notNull(visitor);

		// start visiting
		visitor.startMessage(this);

		// visit block 1 and value
		SwiftBlock1 b1 = getBlock1();
		if (b1 != null) {
			visitor.startBlock1(b1);
			visitor.value(b1, b1.getValue());
			visitor.endBlock1(b1);
		};
		
		// visit block 1 and value
		SwiftBlock2 b2 = getBlock2();
		if (b2 != null) {
			visitor.startBlock2(b2);		
			visitor.value(b2, b2.getValue());
			visitor.endBlock2(b2);
		};
		
		SwiftBlock3 b3 = getBlock3();
		if (b3 != null) {
			visitor.startBlock3(b3);		
			visit(b3, visitor);
			visitor.endBlock3(b3);
		};
		
		SwiftBlock4 b4 = getBlock4();
		if (b4 != null) {
			visitor.startBlock4(b4);		
			visit(b4, visitor);
			visitor.endBlock4(b4);
		};
		
		SwiftBlock5 b5 = getBlock5();
		if (b5 != null) {
			visitor.startBlock5(b5);		
			visit(b5, visitor);
			visitor.endBlock5(b5);
		};

		// visit user defined blocks
		if (this.userBlocks != null) {

			// visit every user defined block
			for(int i = 0; i < this.userBlocks.size(); i++) {

				SwiftBlockUser userBlock = (SwiftBlockUser) this.userBlocks.get(i);
				if (userBlock != null) {
					visitor.startBlockUser(userBlock);
					visit(userBlock, visitor);
					visitor.endBlockUser(userBlock);
				};
			};
		};

		// stop visiting
		visitor.endMessage(this);
	}

	/**
	 * Visit the fields on a given block.
	 * This method is called from {@link #visit(ISwiftMessageVisitor)} but may be 
	 * used independently, in such case, the startBlockX and endBlockX in the visitor
	 * will not be called
	 *  
	 * @param block the block containing the tags to visit
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter block or visitor are <code>null</code>
	 * 
	 * @deprecated Use the specific visit method if needed, as this method will do nothing for block 1 and 2
	 */
	public void visit(SwiftBlock block, ISwiftMessageVisitor visitor) {

		Validate.notNull(block);
		Validate.notNull(visitor);

		// call the correct method
		if (block instanceof SwiftBlock3) {
			visit( (SwiftBlock3) block, visitor);
		};
		if (block instanceof SwiftBlock4) {
			visit( (SwiftBlock4) block, visitor);
		};
		if (block instanceof SwiftBlock5) {
			visit( (SwiftBlock5) block, visitor);
		};
		if (block instanceof SwiftBlockUser) {
			visit( (SwiftBlockUser) block, visitor);
		};
	}

	/**
	 * Visit a Block 3 (SwiftBlock3), i.e: call the tag method for block 3
	 * This method is called from {@link #visit(ISwiftMessageVisitor)} or the deprecated
	 * {@link #visit(SwiftBlock, ISwiftMessageVisitor)} but may be used independently, in such case,
	 * the startBlockX and endBlockX in the visitor will not be called.
	 * 
	 * @param block the block containing the tags to visit
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter block or visitor are <code>null</code>
	 * 
	 * @since 5.0
	 */
	public void visit(SwiftBlock3 block, ISwiftMessageVisitor visitor) {

		// sanity check
		Validate.notNull(block);
		Validate.notNull(visitor);

		// iterate thru tags
		for(Iterator it = block.tagIterator() ; it.hasNext() ; ) {

			Tag t = (Tag) it.next();
			visitor.tag(block, t);
		};
	}

	/**
	 * Visit a Block 4 (SwiftBlock4), i.e: call the tag method for block 4
	 * This method is called from {@link #visit(ISwiftMessageVisitor)} or the deprecated
	 * {@link #visit(SwiftBlock, ISwiftMessageVisitor)} but may be used independently, in such case,
	 * the startBlockX and endBlockX in the visitor will not be called.
	 * 
	 * @param block the block containing the tags to visit
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter block or visitor are <code>null</code>
	 * 
	 * @since 5.0
	 */
	public void visit(SwiftBlock4 block, ISwiftMessageVisitor visitor) {

		// sanity check
		Validate.notNull(block);
		Validate.notNull(visitor);

		// iterate thru tags
		for(Iterator it = block.tagIterator() ; it.hasNext() ; ) {

			Tag t = (Tag) it.next();
			visitor.tag(block, t);
		};
	}

	/**
	 * Visit a Block 5 (SwiftBlock5), i.e: call the tag method for block 4
	 * This method is called from {@link #visit(ISwiftMessageVisitor)} or the deprecated
	 * {@link #visit(SwiftBlock, ISwiftMessageVisitor)} but may be used independently, in such case,
	 * the startBlockX and endBlockX in the visitor will not be called.
	 * 
	 * @param block the block containing the tags to visit
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter block or visitor are <code>null</code>
	 * 
	 * @since 5.0
	 */
	public void visit(SwiftBlock5 block, ISwiftMessageVisitor visitor) {

		// sanity check
		Validate.notNull(block);
		Validate.notNull(visitor);

		// iterate thru tags
		for(Iterator it = block.tagIterator() ; it.hasNext() ; ) {

			Tag t = (Tag) it.next();
			visitor.tag(block, t);
		};
	}

	/**
	 * Visit a User Defined Block (SwiftBlockUser), i.e: call the tag method for block 4
	 * This method is called from {@link #visit(ISwiftMessageVisitor)} or the deprecated
	 * {@link #visit(SwiftBlock, ISwiftMessageVisitor)} but may be used independently, in such case,
	 * the startBlockX and endBlockX in the visitor will not be called.
	 * 
	 * @param block the block containing the tags to visit
	 * @param visitor the visitor to use
	 * @throws IllegalArgumentException if parameter block or visitor are <code>null</code>
	 * 
	 * @since 5.0
	 */
	public void visit(SwiftBlockUser block, ISwiftMessageVisitor visitor) {

		// sanity check
		Validate.notNull(block);
		Validate.notNull(visitor);

		// iterate thru tags
		for(Iterator it = block.tagIterator() ; it.hasNext() ; ) {

			Tag t = (Tag) it.next();
			visitor.tag(block, t);
		};
	}

	/**
	 * Get the unique identifier of this message
	 * @return the message id
	 * @see #id
	 */
	public Long getId() {
		return(this.id);
	}

	/**
	 * Set the unique identifier of this message
	 * @param id the id to be set
	 * @see #id
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
	 * Get the number of blocks in this message, optionaly including the user blocks.<br>
	 * A block is summed if it is not <code>null</code> and it is not empty.
	 * 
	 * @param includeUserBlocks indicates whether or not user defined blocks should be counted
	 * @return an int greater or equal to zero
	 */
	public int getBlockCount(Boolean includeUserBlocks) {

		// count the basic blocks
		int count = 0;
		if (this.block1 != null && !getBlock1().isEmpty()) count++;
		if (this.block2 != null && !getBlock2().isEmpty()) count++;
		if (this.block3 != null && !getBlock3().isEmpty()) count++;
		if (this.block4 != null && !getBlock4().isEmpty()) count++;
		if (this.block5 != null && !getBlock5().isEmpty()) count++;

		// count user defined blocks (if requested to do so)
		if (includeUserBlocks.booleanValue() && this.userBlocks != null)
			count += this.userBlocks.size();

		return(count);
	}

	/**
	 * Get block number 1 of this message, may be <code>null</code> if not set
	 * @return the block 1 of the message or <code>null</code>
	 */
	public SwiftBlock1 getBlock1() {
		return(this.block1);
	}

	/**
	 * Set the block 1 of the message
	 * @param block1 the content of the block 1
	 * @deprecated use {@link #setBlock1(SwiftBlock1)} instead
	 * @throws ClassCastException if block1 is not a SwiftBlock1
	 */
	public void setBlock1(SwiftBlock block1) {
		this.block1 = (SwiftBlock1) block1;
	}

	/**
	 * Set the block 1 of the message
	 * @param block1 the content of the block 1
	 */
	public void setBlock1(SwiftBlock1 block1) {
		this.block1 = block1;
	}
	
	/**
	 * Get block number 2 of this message, may be <code>null</code> if not set
	 * @return the block 2 of the message or <code>null</code>
	 */
	public SwiftBlock2 getBlock2() {
		return(this.block2);
	}

	/**
	 * Set the block 2 of the message
	 * @param block2 the content of the block 1
	 * @deprecated use {@link #setBlock2(SwiftBlock2)} instead
	 * @throws ClassCastException if block1 is not a SwiftBlock2
	 */
	public void setBlock2(SwiftBlock block2) {
		this.block2 = (SwiftBlock2) block2;
	}
	
	/**
	 * Set the block 2 of the message
	 * @param block2 the content of the block 1
	 */
	public void setBlock2(SwiftBlock2 block2) {
		this.block2 = block2;
	}

	/**
	 * Get block number 3 of this message, may be <code>null</code> if not set
	 * @return the block 3 of the message or <code>null</code>
	 */
	public SwiftBlock3 getBlock3() {
		return(this.block3);
	}

	/**
	 * Set the block 3 of the message
	 * @param block3 the content of the block 1
	 * @deprecated use {@link #setBlock3(SwiftBlock3)} instead
	 * @throws ClassCastException if block1 is not a SwiftBlock3
	 */
	public void setBlock3(SwiftBlock block3) {
		this.block3 = (SwiftBlock3) block3;
	}

	/**
	 * Set the block 3 of the message
	 * @param block3 the content of the block 1
	 */
	public void setBlock3(SwiftBlock3 block3) {
		this.block3 = block3;
	}
	
	/**
	 * Get block number 4 of this message, may be <code>null</code> if not set
	 * @return the block 4 of the message or <code>null</code>
	 */
	public SwiftBlock4 getBlock4() {
		return(this.block4);
	}

	/**
	 * Set the block 4 of the message
	 * @param block4 the content of the block 1
	 * @deprecated use {@link #setBlock4(SwiftBlock4)} instead
	 * @throws ClassCastException if block1 is not a SwiftBlock4
	 */
	public void setBlock4(SwiftBlock block4) {
		this.block4 = (SwiftBlock4) block4;
	}

	/**
	 * Set the block 4 of the message
	 * @param block4 the content of the block 1
	 */
	public void setBlock4(SwiftBlock4 block4) {
		this.block4 = block4;
	}
	
	/**
	 * Get block number 5 of this message, may be <code>null</code> if not set
	 * @return the block 5 of the message or <code>null</code>
	 */
	public SwiftBlock5 getBlock5() {
		return(this.block5);
	}

	/**
	 * Set the block 5 of the message
	 * @param block5 the content of the block 5
	 * @deprecated use {@link #setBlock5(SwiftBlock5)} instead
	 * @throws ClassCastException if block5 is not a SwiftBlock5
	 */
	public void setBlock5(SwiftBlock block5) {
		this.block5 = (SwiftBlock5)block5;
	}

	/**
	 * Set the block 5 of the message
	 * @param block5 the content of the block 5
	 */
	public void setBlock5(SwiftBlock5 block5) {
		this.block5 = block5;
	}

	/**
	 * Finds the position of a given User Defined Block in the internal list
	 * @param blockName the block name to find
	 * @return the position or <code>-1</code> if not found
	 *
	 * @since 5.0
	 */
	public Integer getUserBlockPosition(String blockName) {

		// check parameters
		if (blockName == null || blockName.equals("")
				|| //check user blocks array
				(this.userBlocks == null))
			return new Integer(-1);

		// start scanning the list
		for(int i = 0; i < this.userBlocks.size(); i++) {

			SwiftBlockUser userBlock = (SwiftBlockUser) this.userBlocks.get(i);
			if (userBlock != null && userBlock.getName().equals(blockName))
				return new Integer(i);
		};

		return new Integer(-1);
	}

	/**
	 * Get the list of user defined blocks, a list is always returned.
	 * The requested object may be <code>null</code> if the message was clear or not initialized.
	 *
	 * @return the list or user blocks or null
	 * @since 5.0
	 */
	//FIXME supposedly we always return a list... this may be null
	public List getUserBlocks() {
		return(this.userBlocks);
	}

	/**
	 * Set the list of user defined blocks.<br>
	 * This method is mainly needed for persistence services.
	 * 
	 * @param userBlocks the new list of user defined blocks
	 * @throws IllegalArgumentException if parameter userBlocks is <code>null</code>
	 * @throws IllegalArgumentException if parameter userBlocks has elements of class other than SwiftBlockUser
	 * @since 5.0
	 */
	protected void setUserBlocks(List userBlocks) {

		// sanity check
		Validate.notNull(userBlocks, "parameter 'userBlocks' cannot be null");
		Validate.allElementsOfType(userBlocks, SwiftBlockUser.class, "parameter 'userBlocks' may only have SwiftBlockUser elements");

		// setup the new list
		this.userBlocks = userBlocks;
	}
	
	/**
	 * Get a user defined block by name, may be <code>null</code> if not set
	 * 
	 * @param blockName the name of the block to find 
	 * @return the requested user defined block or <code>null</code>
	 * @throws IllegalArgumentException if parameter blockName is <code>null</code>
	 * @throws IllegalArgumentException if parameter blockName has an invalid block name
	 * @since 5.0
	 */
	public SwiftBlockUser getUserBlock(String blockName) {

		// sanity check
		Validate.notNull(blockName, "parameter 'blockName' cannot be null");

		// find the block position
		Integer pos = getUserBlockPosition(blockName);
		if (pos.intValue() != -1)
			return( (SwiftBlockUser) this.userBlocks.get(pos.intValue()));

		return(null);
	}

	/**
	 * Get a user defined block by number, may be <code>null</code> if not set
	 * 
	 * @param blockNumber the number of the block to find 
	 * @return the requested user defined block or <code>null</code>
	 * @throws IllegalArgumentException if parameter userBlock is <code>null</code>
	 * @throws IllegalArgumentException if parameter userBlock has an invalid block name
	 *
	 * @since 5.0
	 */
	public SwiftBlockUser getUserBlock(Integer blockNumber) {

		// sanity check
		Validate.notNull(blockNumber, "parameter 'blockNumber' cannot be null");

		return(this.getUserBlock(blockNumber.toString()));
	}

	/**
	 * Add a user defined block to the message (if the block already exists, it is replaced)
	 * @param userBlock the user defined block
	 * @throws IllegalArgumentException if parameter userBlock is <code>null</code>
	 * @throws IllegalArgumentException if parameter userBlock has an invalid block name
	 * @since 5.0
	 */
	public void addUserBlock(SwiftBlockUser userBlock) {

		// sanity check
		Validate.notNull(userBlock);
		Validate.isTrue(userBlock.isValidName().booleanValue(), "Invalid name for User Defined Blocks (" + userBlock.getName() + ")");

		if (this.userBlocks == null)
			this.userBlocks = new ArrayList();
	
		// find the block position (if it's already there)
		Integer pos = getUserBlockPosition(userBlock.getName());
		if (pos.intValue() != -1) {
			this.userBlocks.add(pos.intValue(), userBlock);
		} else {
			this.userBlocks.add(userBlock);
		};
	}

	/**
	 * removes a user defined block to the message (if the block does not exists, nothing is done)
	 * @param blockNumber the block number to remove
	 * @throws IllegalArgumentException if parameter blockNumber is <code>null</code>
	 * @throws IllegalArgumentException if parameter blockNumber is invalid
	 * @since 5.0
	 */
	public void removeUserBlock(Integer blockNumber) {

		// sanity check
		Validate.notNull(blockNumber, "parameter 'blockNumber' cannot be null");
		Validate.isTrue(SwiftBlockUser.isValidName(blockNumber).booleanValue(), "Invalid name for User Defined Blocks (" + blockNumber.toString() + ")");

		this.removeUserBlock(blockNumber.toString());
	}

	/**
	 * removes a user defined block to the message (if the block does not exists, nothing is done)
	 * @param blockName the block name to remove
	 * @throws IllegalArgumentException if parameter blockName is <code>null</code>
	 * @throws IllegalArgumentException if parameter blockName is invalid
	 * @since 5.0
	 */
	public void removeUserBlock(String blockName) {

		// sanity check
		Validate.notNull(blockName, "parameter 'blockName' cannot be null");
		Validate.isTrue(SwiftBlockUser.isValidName(blockName).booleanValue(), "Invalid name for User Defined Blocks (" + blockName + ")");

		// find the block position (if it's there)
		Integer pos = getUserBlockPosition(blockName);
		if (pos.intValue() != -1)
			this.userBlocks.remove(pos);
	}

	/**
	 * remove all blocks from these message, including user blocks
	 */
	public void clear() {

		// release all blocks
		this.block1 = null;
		this.block2 = null;
		this.block3 = null;
		this.block4 = null;
		this.block5 = null;

		// release user blocks
		this.userBlocks = null;
	}	

	/**
	 * Checks if the message is a fragment
	 * @return true if the message is a fragment
	 * 
	 * @since 5.0
	 */
	public Boolean isFragment() {

		// get the block 4 (if exists)
		SwiftBlock4 b4 = this.getBlock4();
		if (b4 != null) {
			String t202 = b4.getTagValue("202");
			String t203 = b4.getTagValue("203");

			// if both tag exists => this is a fragment
			return(t202 != null && t203 != null ? Boolean.TRUE : Boolean.FALSE); 
		};

		return(Boolean.FALSE);
	}

	/**
	 * Checks if the message is the last fragment
	 * @return true if the message is the last fragment of a fragmented message
	 * 
	 * @since 5.0
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
	 * @since 5.0
	 */
	public Integer fragmentCount() {

		// if this is not a fragment => 0
		if ( ! this.isFragment().booleanValue())
			return new Integer(0);

		// get the block 4 and tag 203 (they BOTH exists here)
		SwiftBlock4 b4 = this.getBlock4();
		String t203 = b4.getTagValue("203");

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
	 * @since 5.0
	 */
	public Integer fragmentNumber() {

		// if this is not a fragment => 0
		if ( ! this.isFragment().booleanValue())
			throw new UnsupportedOperationException("message is not a fragment");

		// get the block 4 and tag 203 (they BOTH exists here)
		SwiftBlock4 b4 = this.getBlock4();
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
	 * @return the unparsed text at position index parsed into a WifeSwiftMessage object
	 * @throws IllegalArgumentException if parameter index is <code>null</code> 
	 */
	public WifeSwiftMessage unparsedTextGetAsMessage(Integer index) {

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
	public void unparsedTextAddText(WifeSwiftMessage message) {

		// create the list if needed
		unparsedTextVerify();
		this.unparsedTexts.addText(message);
	}
}
