package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.logica.ngph.finance.messageparser.sfms.services.SFMSConversionService;

/**
 * <p>List of unparsed texts for messages, blocks or tags.<br /> 
 * For performance reasons, the
 * unparsed texts are stored directly as strings inside this list object. The need then
 * for this object (as opposed to directly using a List) is for some functionality
 * aggregation, specially if you consider that the same is used in all levels of the
 * message structure.</p> 
 * 
 * <p>It is expected that classes that use this object do not create unnecessary instances of
 * this (also for performance reasons). The motive become obvious when you consider that
 * an average SFMS message will have 4 blocks (1, 2, 3 and 4) and that block 4 will have
 * at least 20 tags (so the count of instances of this will be: 1 for the message, 4 for the
 * blocks and 20 for the tags, giving 25). For more complex messages, the number is near
 * linear with the number of tags, while at the same time, most of those messages will have
 * no unparsed texts.</p>  
 *
 * <p>For this, it is expected that the message, block and tag objects will have some convenience
 * methods to access this class methods only if they have a valid object.</p>
 *
 * <p>This class will be used in four different scenarios:</p>
 * 
 * <p>1) SERVICE MESSAGES (for example: ACK)</p>
 * 
 * <p>It's been reported that Swift Alliance Access appends the original message to the ACK on
 * delivery. In this case, the appended original message will be attached to the ACK as an
 * unparsed text</p> 
 * 
 * <p>2) SOME SYSTEM MESSAGES (for example: MT 021, Retrieval Response)</p>
 * 
 * <p>In this case, as per documentation, the retrieved message is appended in block 4, after
 * the tags of the message. In this case, the original (retrieved) message is appended to
 * block 4 as an unparsed text.</p>
 * 
 * <p>3) SOME REPORT MESSAGES (for example: MT 056, LT History Report)</p>
 * 
 * <p>In this case, complete messages (one or more) are appended to a tag value.
 * An example of this is MT 056 (LT History Report) where the original login request and
 * the associated login response (optional) are appended to TAG 270 value. Here, two
 * unparsed texts are appended to tag 270 of the parsed message.</p> 
 * 
 * <p>4) USER DEFINED BLOCKS</p>
 * 
 * <p>As part of the user defined blocks support, we have decided to append the (complete) original
 * block text as an unparsed text to the User Block (class SfmsBlockUser) to allow for some
 * degree of liberty regarding data encoding in these blocks (however, these user defined blocks
 * where designed considering that they behave as standard block 3 or 5.</p> 
 * 
 * @author Logica
 * @version 
 */
public class UnparsedTextList implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Unique identifier of the unparsed texts list.
	 * Mainly used for persistence services.
	 */
	protected Long id;
	
	/**
	 * list of unparsed texts
	 *
	 */
	protected List texts = new ArrayList();

	/**
	 * Default Constructor 
	 */
	public UnparsedTextList() {
		super();
	}
	
	/**
	 * Constructor from a collection of texts
	 * @param texts the list of unparsed texts to set
	 * @throws IllegalArgumentException if parameter texts is <code>null</code>
	 * @throws IllegalArgumentException if parameter texts has elements of class other than String
	 */
	public UnparsedTextList(Collection texts) {

		this.texts = new ArrayList(texts);
	}
	
	/**
	 * Get the unique identifier of this unparsed text list or <code>null</code> if it is not set
	 * @return the unique identifier 
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the unique identifier of this unparsed text list
	 * @param id the unique identifier to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * gets as FIN string, conforming a String object with the concatenation of the unparsed texts.
	 * @return String concatenation of the unparsed texts
	 */
	public String getAsFINString() {

		// performance optimization
        if (this.texts.isEmpty())
        	return("");

        // visit every unparsed text
        StringBuffer s = new StringBuffer();
        for(Iterator itr = this.texts.iterator(); itr.hasNext(); ) {
        	s.append(itr.next());
        };

        return(s.toString());
	}

	/**
	 * convert this to string
	 */
	@Override
	public String toString() {
		return(ToStringBuilder.reflectionToString(this));
	}

	/**
	 * decides if it is likely that an unparsed text is a SWIFT FIN message.<br>
	 * It is considered that a text it is likely to be message if it contains
	 * the text "{1:".
	 * @param text the text to analyze
	 * @return true if the text is likely to be a SWIFT message
	 */
	static public Boolean isMessage(String text) {

		// sanity check and evaluation
		return Boolean.valueOf((text != null && (text.indexOf("{1:")>=0)));
	}

	/**
	 * returns the full list of unparsed texts
	 * @return the list of texts
	 */
	public List getTexts() {

		return(this.texts);
	}

	/**
	 * Set the list of texts, the list must be a list of Strings or an empty list.<br>
	 * This method is mainly needed for persistence services.
	 * @param texts the list of unparsed texts to set
	 * @throws IllegalArgumentException if parameter texts is <code>null</code>
	 * @throws IllegalArgumentException if parameter texts has elements of class other than String
	 */
	protected void setTexts(List texts) {

		// sanity check
		Validate.notNull(texts, "parameter 'texts' cannot be null");
		Validate.allElementsOfType(texts, String.class, "parameter 'texts' may only have String elements");

		// setup the new list
		this.texts = texts;
	}

	/**
	 * get the number of unparsed texts
	 * @return the number of unparsed texts
	 */
	public Integer size() {

		// sanity check and evaluation
		return new Integer(this.texts.size());
	}

	/**
	 * decides if a specific text (by index) is likely a SWIFT FIN message. Exceptions are inherited from
	 * base implementation methods.
	 * @param index the unparsed text number
	 * @return true if the text at position index is likely to be a SWIFT message
	 * @throws IllegalArgumentException if parameter index is <code>null</code>
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds
	 */
	public Boolean isMessage(Integer index) {

		return(UnparsedTextList.isMessage(this.getText(index)));
	}

	/**
	 * get an unparsed text
	 * @param index the unparsed text number
	 * @return the requested text
	 * @throws IllegalArgumentException if parameter index is <code>null</code>
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds
	 */
	public String getText(Integer index) {

		// sanity check
		Validate.notNull(index, "parameter 'index' cannot be null");

		return( (String) this.texts.get(index.intValue()));
	}

	/**
	 * get an unparsed text as a parsed swift message
	 * @param index the unparsed text number
	 * @return the text at position index parsed into a WifeSFMSMessage object
	 * @throws IllegalArgumentException if parameter index is <code>null</code>
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds 
	 */
	public WifeSFMSMessage getTextAsMessage(Integer index) {

		// sanity check
		Validate.notNull(index, "parameter 'index' cannot be null");

		// create a conversion class
		SFMSConversionService cService = new SFMSConversionService();
		return(cService.getMessageFromFIN((String) this.texts.get(index.intValue())));
	}

	/**
	 * adds a new unparsed text
	 * @param text the unparsed text to append
	 * @throws IllegalArgumentException if parameter text is <code>null</code> 
	 */
	public void addText(String text) {

		// sanity check
		Validate.notNull(text, "parameter 'text' cannot be null");

		// append the text
		this.texts.add(text);
	}

	/**
	 * adds a new unparsed text from a message
	 * @param message the message to be appended
	 * @throws IllegalArgumentException if parameter message is <code>null</code> 
	 */
	public void addText(WifeSFMSMessage message) {

		// sanity check
		Validate.notNull(message, "parameter 'message' cannot be null");

		// get the text version of the message
		SFMSConversionService cService = new SFMSConversionService();
		String msg = cService.getFIN(message); 

		// add the text
		this.addText(msg);
	}

	/**
	 * removes an unparsed text
	 * @param index the index of the text to remove
	 * @throws IllegalArgumentException if parameter index is <code>null</code>
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds
	 */
	public void removeText(Integer index) {

		// sanity check
		Validate.notNull(index, "parameter 'index' cannot be null");

		// remove the text
		this.texts.remove(index.intValue());
	}

	/**
	 * removes an unparsed text
	 * @param index the index of the text to remove
	 * @throws IndexOutOfBoundsException if parameter index is out of bounds
	 */
	public void removeText(int index) {

		// remove the text
		this.texts.remove(index);
	}

	/**
	 * removes an unparsed text
	 * @param text the text value to remove (uses equals)
	 * @throws IllegalArgumentException if parameter text is <code>null</code>
	 */
	public void removeText(String text) {

		// sanity check
		Validate.notNull(text, "parameter 'text' cannot be null");

		// remove the text (if it exists)
		int pos = this.texts.indexOf(text);
		if (pos != -1)
			this.texts.remove(pos);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((texts == null) ? 0 : texts.hashCode());
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
		final UnparsedTextList other = (UnparsedTextList) obj;
		if (texts == null) {
			if (other.texts != null)
				return false;
		} else if (!texts.equals(other.texts))
			return false;
		return true;
	}

}
