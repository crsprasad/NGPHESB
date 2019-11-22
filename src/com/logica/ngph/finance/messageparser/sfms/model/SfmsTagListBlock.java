package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base class for SFMS blocks that contain and arbitrary <b>set of fields</b> (3, 4, 5 and user blocks).<br>
 * This is an <b>abstract</b> class so specific block classes for each block should be instantiated.
 *
 * @author Logica
 * @version 
 */
public abstract class SfmsTagListBlock extends SfmsBlock implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6257232577401303904L;
	/**
	 * Contains instances of Tag in this block, used to store the block's fields.
	 */
	protected List<Tag> tags = new ArrayList<Tag>();

	/**
	 * Default constructor, shouldn't be used normally.
	 * present only for subclasses
	 */
	protected SfmsTagListBlock() {

	}

	/**
	 * Empty iterator to be used when an API that returns an Iterator does not return <code>null</code>.
	 */
	private static final class EmptyItr implements Iterator {
		public boolean hasNext() {
	            return false;
		}
		public Object next() {
			throw new NoSuchElementException();
		}
		public void remove() {
			throw new UnsupportedOperationException("Can't remove on an empty iterator");
		}
	}
	
	/**
	 * Tells if this block contains a tag with the given name.
	 * This method iterates throw tags and checks the name to be matched against the given tagname.
	 * Thus, it will not distinguish between tags that appear only one time and tags that appear 
	 * in multiple instances.
	 * 
	 * @param tagName fieldname to search, like "32A" or "58"
	 * @return <code>true</code> if the given field has been set on this block
	 *         or <code>false</code> in other case
	 * @throws IllegalArgumentException if parameter tagName is <code>null</code>
	 */
	public boolean containsTag(String tagName) {

		// sanity check
		Validate.notNull(tagName, "parameter 'tagName' cannot not be null");

		for (Iterator<Tag> it = tags.iterator(); it.hasNext();) {
			Tag t = it.next();
			if (t.getName() != null && t.getName().equals(tagName))
				return true;
		};
		return false;
	}
	
	/**
	 * Gets the value of the given tag or <code>null</code> if that tagname is not found.<br>
	 * NOTE: if the tag is present more than once, then this method retrieves the value of the first occurence
	 * 
	 * @param key name of the tag, ex: 13C (case sensitive)
	 * @return a String containing the value <code>null</code> if the tag is not set
	 * @throws IllegalArgumentException if parameter key is <code>null</code>
	 */
	public String getTagValue(String key) {

		// sanity check
		Validate.notNull(key, "parameter 'key' cannot not be null");

		// just try to get the tag (containsTag => getTagByName runs the list twice)
		Tag tag = this.getTagByName(key);
		return(tag != null ? tag.getValue() : null);
	}
	
	/**
	 * Iterate through tags in this block and return the first tag whose name matches key, 
	 * or <code>null</code> if none is found.
	 * 
	 * @param key name of the tag to search
	 * @return the tag containing the given key or <code>null</code> if it is not found
	 * @throws IllegalArgumentException if parameter key is <code>null</code>
	 */
	public Tag getTagByName(String key) {

		// sanity check
		Validate.notNull(key, "parameter 'key' cannot not be null");

		// scan the list
		for (Iterator<Tag> it = tags.iterator(); it.hasNext();) {
			Tag f = it.next();
			if (f.getName().equals(key))
				return f;
		}
		return null;
	}
	
	/**
	 * Gets the internal List of tags in block.
	 * @return a List of Tag
	 * @see Tag
	 */
	public List<Tag> getTags() {
		return(this.tags);
	}

	/**
	 * Adds a tag to this block.
	 * 
	 * @param t the tag to add
	 * @throws IllegalArgumentException if parameter t is <code>null</code>
	 */
	public void addTag(Tag t) {

		// sanity check
		Validate.notNull(t, "parameter 't' cannot not be null");

		if (this.tags == null)
			this.tags = new ArrayList<Tag>();
		this.tags.add(t);
	}
	
	/**
	 * Counts how many tags with the given name are present in the block.
	 * 
	 * @param key the name of the tag
	 * @return the amount of tags with the given name in the block
	 * @throws IllegalArgumentException if parameter key is <code>null</code>
	 */
	public int getTagCount(String key) {

		// sanity check
		Validate.notNull(key, "parameter 'key' cannot not be null");

		// count the matches
		int count = 0;
		if (this.tags != null) {
			for (Iterator<Tag> it = tags.iterator(); it.hasNext(); ) {
				Tag f = it.next();
				if (f.getName().equals(key))
					count++;
			}			
		}
		return count;
	}
	
	/**
	 * Gets all values for a given tagname. The tag list is searched in order, all tag 
	 * values matching the name of the given key are added to the resulting array.
	 * <em>NOTE:</em> the resulting array may be empty if no tagname is matched. 
	 * 
	 * @param key name of the tag to be searched, case sensitive
	 * @return and array containing the values of all the instances of the tag
	 * @throws IllegalArgumentException if parameter key is <code>null</code>
	 */
	public String[] getTagValues(String key) {

		// sanity check
		Validate.notNull(key, "parameter 'key' cannot not be null");

		ArrayList<String> ret = new ArrayList<String>();
		if (this.tags != null) {
			for (Iterator<Tag> it = tags.iterator(); it.hasNext(); ) {
				Tag f = it.next();
				if (f.getName().equals(key))
					ret.add(f.getValue());
			}			
		}

		return ret.toArray(new String[ret.size()]);
	}

	/**
	 * convert this to string
	 */
	@Override
	public String toString() {
		return(ToStringBuilder.reflectionToString(this));
	}

	/**
	 * Gets a Map that contains the the fields names as keys and the values as map value.
	 * If a field is present more than once, then the first instance is processed and the rest is ignored.
	 * 
	 * @return a Map with tagname as key and values or <code>null</code> if there are not tags in the block
	 */
	public Map<String, String> getTagMap() {

		if (this.tags != null) {
			Map<String, String> m = new HashMap<String, String>(tags.size());
			for (Iterator<Tag> it = tags.iterator(); it.hasNext();) {
				Tag f = it.next();
				if ( ! m.containsKey(f.getName()))
					m.put(f.getName(), f.getValue());
			}			
			return m;
		}
		return null;		
	}
	
	/**
     * Remove all tags with the given name in the block.
     * If more than one instance of the given name is 
     * found the first instance is removed and then the 
     * rest remains untouched.
     * 
     * @param tag name of the tag to remove must not be null 
     * @return the value of the removed tag
	 * @throws IllegalArgumentException if parameter tag is <code>null</code>
     * @see #removeAll(String)
     */
	public String removeTag(String tag) {

		// sanity check
		Validate.notNull(tag, "parameter 'tag' cannot not be null");

		 if (this.tags!=null) {
			int i = 0;
			for (Iterator<Tag> it = tags.iterator() ; it.hasNext() ; ) {
				Tag t = it.next();
				if (t.getName()!=null && t.getName().equals(tag)) {
					tags.remove(i);
					return t.getValue();
				}
				i++;
			}
		}
		return null;
	}
	
	/**
	 * Remove all tags in the current block that match the given name.
	 * If name is an invalid tag no error is thrown. There is no difference by using this method
	 * to tell if a tag was present or not. for quering the block for existing tags 
	 * {@link #containsTag(String)} must be used. 
	 * 
	 * @param name the name of the tag to remove. may be <code>null</code> in which case the tags containing 'block data' will be removed
	 * @return the amount of tags removed
	 * @throws IllegalArgumentException if parameter name is <code>null</code>
	 */
	public int removeAll(String name) {

		// sanity check
		Validate.notNull(name, "parameter 'name' cannot not be null");

		int removed = 0;
		Tag[] matching = getTagsByName(name);
		for (int i=0;i<matching.length;i++) {
			this.tags.remove(matching[i]);
			removed++;
		}
		return removed;
	}
	
	/**
	 * Get a reference to tags in the block that match the given tag name.
	 * If name is <code>null</code> all tags that contain block data will be returned.
	 * If no tag is found an empty array is returned.
	 * 
	 * @param name the name of the tag to search in this block
	 * @return an array of tags or an empty array if no tags are found
	 * @throws IllegalArgumentException if parameter name is <code>null</code>
	 */
	public Tag[] getTagsByName(String name) {

		// sanity check
		Validate.notNull(name, "parameter 'name' cannot not be null");

		final List<Tag> l = new ArrayList<Tag>();
		for (Iterator<Tag> it = this.tags.iterator() ; it.hasNext() ; ) {
			Tag t = it.next();
			if (t.getName()==null && name==null) {
				l.add(t);
			}
			if (t.getName()!=null && name!=null && t.getName().equals(name)) {
				l.add(t);
			}
		}
		return l.toArray(new Tag[l.size()]);
	}
	
	/**
	 * Gets a Iterator for the tags in this block or <code>null</code> if no tags are present on the block an empty iterator is returned.
	 * 
	 * @return an Iterator that may or may not contain objects of type Tag
	 * @see Tag
	 */
	public Iterator<Tag> tagIterator() {
		if (this.tags == null || this.tags.isEmpty() ) {
			return new EmptyItr();
		}
		return this.tags.iterator();
	}
	
	/**
	 * Gets the Tag of the given index in this block, the position is zero-based.
	 * 
	 * @param i the index position of the tag to retrieve
	 * @return the Tag at the given index
	 * @throws IndexOutOfBoundsException if the index is invalid
	 * @see List#get(int)
	 */
	public Tag getTag(int i) {
		return this.tags.get(i);
	}
	
	/**
	 * Add all tags in the List argument to the current blocks. Current tags will not be removed.
	 * @param tags the list of tags to add
	 * @throws IllegalArgumentException if parameter name is <code>null</code>
	 */
	public void addTags(List<Tag> tags) {

		// sanity check
		Validate.notNull(tags, "parameter 'tags' cannot not be null");
		Validate.allElementsOfType(tags, Tag.class, "parameter 'tags' may only have Tag elements");

		this.tags.addAll(tags);
	}
	
	/**
	 * Gets the amount of tags added to the block.
	 * @return zero or the amount of tags contained in the block
	 */
	public int getTagCount() {
		return(this.tags == null ? 0 : tags.size());
	}
	
	/**
	 * Set the list of tags of this block.
	 * NOTE that the order of the tags in the list is the order that really matters.
	 * 
	 * @param tags the tags of the block, may be <code>null</code> to remove all the tags of the block
	 * @throws IllegalArgumentException if parameter tags is not <code>null</code> and contains elements of class other than Tag
	 */
	public void setTags(List<Tag> tags) {

		// sanity check
		if (tags != null)
			Validate.allElementsOfType(tags, Tag.class, "parameter 'tags' may only have Tag elements");

		this.tags = tags;
	}
	
	/**
	 * Tells if the block contains at least one Tag.
	 * 
	 * @return <code>true</code> if the block contains at least one Tag and <code>false</code> in other case
	 */
	public boolean isEmpty() {
		return(this.tags==null || this.tags.isEmpty());
	}
	
	/**
	 * Tells the amount of fields contained in the block, may be zero.
	 */
	public int size() {
		return(this.tags == null ? 0 : this.tags.size());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SfmsTagListBlock other = (SfmsTagListBlock) obj;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		return true;
	}
}
