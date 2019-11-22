package com.logica.ngph.finance.messageparser.sfms.model;

/**
 * Interface to be implemented by classes that will 'visit' a SFMS message.
 *
 * There method call sequence is as follows:
 *
 * <ol>
 * <li><code>startMessage</code>
 * <li><code>startBlockA -> value -> endBlock1</code> (if block 1 exists)
 * <li><code>startBlock4 -> tag (for every tag) -> endBlock4</code> (if block 4 exists)
 * <li><code>startBlock5 -> tag (for every tag) -> endBlock5</code> (if block 5 exists)
 * <li><code>startBlockUser -> tag (for every tag) -> endBlockUser</code> (for every user defined block and every tag of that block)
 * <li><code>endMessage</code>
 * </ol>
 *
 * <p>Notice that the <code>tag</code> and <code>value</code> methods are overloaded for every type of SfmsBlock
 * derived class.</p>
 * 
 * 
 * @author Logica
 * @version 
 */
public interface ISfmsMessageVisitor {

	/**
	 * @param b block to visit
	 */
	void startBlock1(SfmsBlock1 b);
	
	/**
	 * @param b block to visit
	 */
	void startBlock4(SfmsBlock4 b);
	
	/**
	 * @param b block to visit
	 */
	void startBlock5(SfmsBlock5 b);
	
	/**
	 * @param b block to visit
	 */
	void endBlock1(SfmsBlock1 b);
	

	/**
	 * @param b block to visit
	 */
	void endBlock4(SfmsBlock4 b);
	
	/**
	 * @param b block to visit
	 */
	void endBlock5(SfmsBlock5 b);
	
	/**
	 * This method checks the kind of SfmsBlock passed in and calls the appropriate method.
	 * 
	 * @param b block to check
	 * @param t 
	 * 
	 */
	void tag(SfmsBlock b, Tag t);
	
	/**
	 * @param b
	 * @param t
	 */
	void tag(SfmsBlock4    b, Tag t);
	
	/**
	 * @param b
	 * @param t
	 */
	void tag(SfmsBlock5    b, Tag t);
	
	
	/**
	 * @param b
	 * @param v
	 */
	void value(SfmsBlock1  b, String v);
	
	
	/**
	 * 
	 * @param m
	 */
	void startMessage(WifeSFMSMessage m);
	
	/**
	 * 
	 * @param m
	 */
	void endMessage(WifeSFMSMessage m);
}
