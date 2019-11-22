package com.logica.ngph.finance.messageparser.sfms.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.logica.ngph.finance.messageparser.sfms.exceptions.SfmsException;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsBlock;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsBlock1;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsBlock4;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsBlock5;
import com.logica.ngph.finance.messageparser.sfms.model.SfmsTagListBlock;
import com.logica.ngph.finance.messageparser.sfms.model.Tag;
import com.logica.ngph.finance.messageparser.sfms.model.UnparsedTextList;
import com.logica.ngph.finance.messageparser.sfms.model.WifeSFMSMessage;

/**
 * @author Logica
 * @version 
 */
public class SFMSParser {
	
	public static final String EOL = "\n";

	private Reader reader;
	
	private StringBuffer buffer;

	/**
	 * Reference to the current message being parsed.
	 * This should be used when some parsing decision needs to be made based on a previous item parsed,
	 * like a value in a previous tag or block.
	 */
	private WifeSFMSMessage currentMessage;
	
	/**
	 * Errors found while parsing the message.
	 */
	private final List<String> errors = new ArrayList<String>();
	

	/**
	 * Constructor with an input stream for parsing a message 
	 * @param is stream to read
	 */
	public SFMSParser(InputStream is) {
		this(new InputStreamReader(is));
	}
	
	/**
	 * Constructor with a reader for parsing a message 
	 * @param r the Reader with the sfms message to read
	 */
	public SFMSParser(Reader r) {
		setReader(r);
	}
	

	/**
	 * Constructor with a reader for parsing a message 
	 * @param message the String with the sfms message to read
	 */
	public SFMSParser(String message) {
		this(new StringReader(message));
	}
	/**
	 * default constructor.
	 * NOTE: If this constructor is called, setReader must be called to use the parser
	 */
	public SFMSParser() {
	}
	
	/**
	 * sets the input reader.
	 * NOTE: this resets the internal buffer
	 * @param r the reader to use
	 */
	public void setReader(Reader r) {
		this.buffer = new StringBuffer();
		this.reader = r;
	}
	
	/**
	 * sets the input data to the received string.
	 * @param data the data to use as input
	 */
	public void setData(String data) {
		setReader(new StringReader(data));
	}

	/**
	 * Parse a SFMS message into a data structure
	 *  
	 * @return the parsed SFMS message object
	 * @throws IOException
	 */
	public WifeSFMSMessage message() throws SfmsException {
        
        // create a message and store for local reference
		WifeSFMSMessage message = new WifeSFMSMessage(false);
        this.currentMessage  = message;

        // Clear all errors before starting the parse process
        this.errors.clear();
        try {
        	boolean done = false;
        	SfmsBlock b = null;
        	do {
        		// try to consume a block
            	if ( (b = consumeBlock()) != null) {
            		this.currentMessage.addBlock(b);
            	} else {
            		done = true;
            	}
        	} while ( ! done);
        } catch(IOException e) {
        	throw new SfmsException(e.getLocalizedMessage());
        } finally {
        	// Clean the reference to the message being parsed
        	this.currentMessage = null;
        };
        
        return(message);
	}

	/**
	 * Parse a SFMS message into a data structure
	 *  
	 * @return the parsed SFMS message object
	 * @throws IOException
	 */

/*	public SFMSMessageSchema parseMessage() throws SfmsException {

		// create a message and store for local reference
		WifeSFMSMessage sfmsMessage = new WifeSFMSMessage(false);
        this.currentMessage  = sfmsMessage;

        // Clear all errors before starting the parse process

        try {
        	boolean done = false;
        	SfmsBlock b = null;
        	do {
        		// try to consume a block
            	if ( (b = consumeBlock()) != null) {
            		this.currentMessage.addBlock(b);
            	} else {
            		done = true;
            	}
        	} while ( ! done);
        } catch (IOException e) {
        	throw new SfmsException(e.getLocalizedMessage());
        } finally {
        	// Clean the reference to the message being parsed
        	this.currentMessage = null;
        };
        
        
    	SFMSMessageSchema xsdMsgObj = new SFMSMessageSchema();
    	xsdMsgObj.setSFMSBlockA(getBlockAXSDObj(sfmsMessage));
        xsdMsgObj.setSFMSBlock4(getBlock4XSDObj(sfmsMessage));
        
        return(xsdMsgObj);
	}
*/
	/**
	 * Consume the next block of the message on the reader.
	 * This methods seeks to a block start, then identifies the block
	 * and calls the proper method to consume the block type 
	 * that is coming, not all blocks are parsed in the same manner.
	 * 
	 * @return the next block in the reader or null if none was found (i.e: end of input)
	 * @throws IOException if an error occurred during read
	 */
	protected SfmsBlock consumeBlock() throws IOException, SfmsException {

		// search for block start
		findBlockStart();

		// read the block contents
		String s = readUntilBlockEnds();
		if (s.equals("")) {
			return(null);
		};

		// analyze if it is an unparsed text
		//
		// NOTE: This can happen when we have got a block 1 and there is already a block A
		//
		if (s.startsWith("A:")) {


			// check if the block 1 is already here
			if (this.currentMessage != null && this.currentMessage.getBlock1() != null) {

				// unparsed text => initialize value to append
				StringBuffer utBuffer = new StringBuffer();
				utBuffer.append("{");
				utBuffer.append(s);
				utBuffer.append("}");
				boolean done = false;
				while ( ! done) {

					// try to read a block of data
					char data[] = new char[128];
					int  size   = this.reader.read(data);
					if (size > 0) {

						// append the read buffer
						utBuffer.append(data);
					} else {
						// we are done
						done = true;
					};
				};
				String unparsedText = utBuffer.toString();

				// build an unparsed text list
				UnparsedTextList list = processUnparsedText(unparsedText);
				if (list != null)
					this.currentMessage.setUnparsedTexts(list);

				// no more reading
				return(null);
			};
		};

		// identify and create the proper block
		char blockId = identifyBlock(s);
		SfmsBlock b = null;
		if (blockId == ' ') {
			// block cannot be identified
			throw new SfmsException("The block " + s + " could not be identified");
		};
	
		// create the block object
		switch (blockId) {
		case 'A':											// block 1 (single valued)
			b = new SfmsBlock1(s);
			break;
		case '4':											// block 4
			b = block4Consume(new SfmsBlock4(), s);
			break;
		case '5':											// block 5 (tag list)
			b = tagListBlockConsume(new SfmsBlock5(), s);
			break;
		};


		return(b);
	}


	/**
	 * consumes a tag list block (i.e: block 3, block 5 or user defined block)
	 * 
	 * @param b the block to set up tags into
	 * @param s the block data to process
	 * @return the processed block (the parameter b)
	 * @throws IOException 
	 */
	protected SfmsTagListBlock tagListBlockConsume(SfmsTagListBlock b, String s) throws IOException {


		// start processing the block data
		int start = s.indexOf(':');
		if (start >= 0 && (start + 1) < s.length()) {
			String data = s.substring(start + 1);

			/*
			 * Enter a loop that will read any block or inner data
			 * The idea is to accept equally these strings:
			 * {block1}{block2}
			 * data1{block1}data2{block2}data3
			 */
			for(int i = 0; i < data.length(); i++) {
				char c = data.charAt(i);
				if (c == '{') {
					int end = data.indexOf('}', i);
					if (end >= 0 && data.length() > end) {

						String inner = data.substring(i + 1, end);
						// Seek the cursor to last 'processed' position
						i = end;
						Tag t = new Tag(inner);
						b.addTag(t);
					};
				} else {
					// read all the characters until data end or a new '{'
					int end;
					for(end = i; end < data.length() && data.charAt(end) != '{'; end++);
					String unparsedText = data.substring(i, end).trim();
					if ( ! "".equals(unparsedText)) {
						b.unparsedTextAddText(unparsedText);
					};
					i = end - 1;
				};
			};
		};


		return(b);
	}

	/**
	 * Parses a block 4 from an input string. This method supports the two possible formats of
	 * a sfms block 4:<br />
	 * <ul>
	 * <li><b>Text mode</b>: this is the common block 4 for categories 1 to 9.
	 * <li><b>Tag mode</b>: this is the same format as for blocks 3 and 5. This format is used by
	 * service messages (for example: ACK) and system messages (category 0).
	 * </ul><br />
	 * 
	 * @param b the block to set up tags into
	 * @param s the block data to process
	 * @return the processed block (the parameter b)
	 * @throws IOException 
	 */
	protected SfmsBlock4 block4Consume(SfmsBlock4 b, String s) throws IOException {

		// block 4 can end with "-}" (the closing brace not present now) => remove the dash if present
		s = block4EndOfBlockFix(s);

		// process by "tokenizing" the input, this meaning:
		// - skip the "4:" (block identifier)
		// - scan for a tag start character (maybe '{' or ':')
		//   - if start is '{' => find tag end by balancing braces => split tag (considering unparsed texts)
		//   - if start is ':' => find tag end as '<CR><LF>:[X]' => split tag (no unparsed texts)
		// - detect block end as '<CR><LF>-}' or '}'
		//
		int start = 0;
		if (s.charAt(start) == '4')
			start++;
		if (s.charAt(start) == ':')
			start++;

		// start processing tags
		while (start < s.length()) {

			// position ourselves at something meaningful
			int  begin = start;
			char c;
			do {
				c = s.charAt(start++);
			} while (start < s.length() && c != ':' && c != '{' && c != '}');

			// check if we must correct end of unparsed text by "-}" (we don't want "-" to be unparsed text)
			int ignore = 0;
			if (c == '}') {
				if (s.charAt(start - 1) == '-')
					ignore = 1;
			};
			
			// check if we skiped an block unparsed text
			String unparsedText = s.substring(begin, start - ignore - 1).trim();
			if ( ! "".equals(unparsedText)) {
				b.unparsedTextAddText(unparsedText);
			};

			// if no more buffer => terminate
			if (start == s.length()) {
				continue;
			};

			// decide what are we looking at (notice that "-}" is detected by "}")
			int    end 				= 0;
			String tag 				= null;
			String tagUnparsedText	= null;
			switch (c) {
				case '}':
					// sanity check
					if (start != s.length())

					// force termination
					start = s.length();
					break;
				case ':':
					// get the tag text
					end = textTagEnd(s, start);
					tag = s.substring(start, end);
					break;
				case '{':
					// two things are possible here:
					// A) this is an unparsed text (i.e: the tag id is 1)
					// B) this is a valid tag (i.e: the tag id is not one)
					if (s.startsWith("1:", start)) {
						//
						// CASE A (an unparsed text)
						//

						// keep our position
						begin = start > 0 ? start - 1 : 0;
						end = begin + 1;
						while (end < s.length() && ! s.startsWith("{1:", end) ) {
							end = blockTagEnd(s, end + 1);
						};

						// get the unparsed text
						unparsedText = s.substring(begin, end);


						// add the unparsed text
						b.unparsedTextAddText(unparsedText);
					} else {
						//
						// CASE B (a tag)
						//
					
						// get the tag text
						end = blockTagEnd(s, start);
						tag = s.substring(start, end - 1);
						int utPos = tag.indexOf("{1:");
						if (utPos != -1) {
							// separate unparsed texts from value 
							tagUnparsedText = tag.substring(utPos);
							tag 			= tag.substring(0, utPos);
						};
					};
					break;
			};

			// process the tag (only if we have a tag)
			if (tag != null) {

				// process the tag
				Tag t = consumeTag(tag, tagUnparsedText);
				if (t != null) {
					b.addTag(t);
				};
			};

			// continue processing from the end of this tag
			start = end;
		};


		return(b);
	}

	/**
	 * Remove extra trailing data in block4.<br />
	 * All blocks end with '}', block4, however, can end with "[CR][LF]-}".<br />
	 * This method will strip the trailing "[CR][LF]-" if present and thus, fix the data for simpler processing. 
	 * 
	 * @param data data of the block to fix
	 * @return the received data, possibly without the trailing "[CR][LF]-".
	 */
	private String block4EndOfBlockFix(String data) {

		// sanity checks
		if (data == null || data.length() == 0)
			return(data);

		// find the ending position
		int end = data.length();
		if (data.charAt(--end) == '-') {
			if (end > 0 && data.charAt(--end) == '\n') {
				if (end > 0 && data.charAt(--end) != '\r') {
					end++;
				};
			};
			data = end > 0 ? data.substring(0, end) : "";
		};

		return(data);
	}

	/**
	 * finds the end of a text tag (i.e: ":TAG:VALUE"). This is used to parse block 4.<br />
	 * The function search the string looking for the occurrence of any of the sequences:<br />
	 * <ul>
	 * <li>"[LBR]:[X]"</li>
	 * <li>"[LBR]}"</li>
	 * <li>"[LBR]{"</li>
	 * <li>"}"</li>
	 * </ul>
	 * where "[LBR]" stands for any of: "[CR]", "[LF]" or "[CR][LF]"
	 * and "[X]" is any character other than [CR] and [LF].<br />
	 * Then considers the end of the tag as <b>NOT</b> containing the found sequence.<br />
	 * <b>NOTE</b>: the condition "-}" cannot happen because the terminating dash is already removed.<br />
	 * 
	 * @param s the FIN input text
	 * @param start the position to start analysis at
	 * @return the position where the tag ends (excluding the <CR><LF>)
	 */
	private int textTagEnd(String s, int start) {


		// start scanning for tag end
		for( ; start < s.length(); start++) {

			// check if we found tag end
			char c = s.charAt(start);
			if (c == '\r' || c == '\n') {

				// keep this position
				int begin = start;

				// repeat cause "\r\n", accept "\n\r" also
				if ( (start + 1) == s.length())
					break;
				c = s.charAt(++start);
				if (c == '\r' || c == '\n') {
					if (start == s.length())
						break;
					c = s.charAt(++start);
				};

				// if open brace => it's a proper tag end (mixing BLOCK and TEXT tags, rare but...)
				// if closing brace => it's a proper tag end (because of block end)
				if (c == '{' || c == '}') {
					// found it
					start = begin;
					break;
				} 
				// if it's a colon followed by a character different than CR or LF (':x') => it's a proper tag end
				// because we have reached a new line with a beginning new tag.
				// Note: It is note sufficient to check for a starting colon because for some fields like
				// 77E for example, it is allowed the field content to have a ':<CR><LF>' as the second line
				// of its content.
				else if (c == ':' && !(start == s.length())) {
					char z = s.charAt(++start);
					if (z != '\r' && z != '\n') {
						// found it
						start = begin;
						break;
					}
				};
				
				// not matched => skip current char and continue
				start = begin;
				continue;
			};

			// check if we found block end (as "-}")
			if (c == '-') {

				// check for closing brace
				c = (start + 1) < s.length() ? s.charAt(start + 1) : ' ';
				if (c == '}')
					break;
			};

			// check if we found block end (as "}")
			if (c == '}') {
				break;
			};
		};


		return(start);
	}

	/**
	 * finds the end of a block tag (i.e: "{TAG:VALUE}"). This is used to parse block 4.<br />
	 * The function search the string looking for the occurrence of the sequence "}". It is important to
	 * note that curly braces are balanced along the search.
	 * @param s the FIN input text
	 * @param start the position to start analysis at
	 * @return the position where the tag ends (including the "}")
	 */
	private int blockTagEnd(String s, int start) {


		// scan until end or end of string
		int  balance = 0;
		char c;
		do {
			// analyze this position
			switch ( (c = s.charAt(start++)) ) {
				case '{': balance++; break;
				case '}': balance--; break;
			};
		} while (start < s.length() && (balance >= 0 || (balance == 0 && c != '}') ) );


		return(start);
	}

	/**
	 * process the input as a tag. That is: split name and value (and possibly unparsed texts).<br />
	 * The received buffer contains only the pertinent data for the tag (name and value). Trailing
	 * [CR][LF] on the text <b>MUST</b> not be present.
	 * 
	 * @param buffer the buffer containing the tag
	 * @param unparsedText the unparsed text to assign (use <code>null</code> if none is wanted).
	 * This single text is fragmented in multiple texts if there are more than one message. 
	 * @return a sfms Tag
	 * @throws IOException
	 */
	protected Tag consumeTag(String buffer, String unparsedText) throws IOException {


		// separate name and value
		int sep      = buffer.indexOf(':');
		String name  = null;
		String value = null;
		if (sep != -1) {
			name  = buffer.substring(0, sep);
			value = buffer.substring(sep + 1);
		} else {
			value = buffer;
		};

		if (name == null || name.equals("")) {
			Validate.notNull(name, "Invalid field format for the value: " + buffer);
		}
		
		// ignore empty tags (most likely, an "{}" in an unparsed text...)
		if ( (name == null || name.equals("")) && (value == null || value.equals("")) ) {


			return(null);			// no tag...
		};

		// remove terminating [CR][LF] (or any combination)
		int  size = value.length();
		if (size > 0) {
			char c = value.charAt(size - 1); 
			if (c == '\r' || c == '\n')
				size--;
		};
		if (size > 0) {
			char c = value.charAt(size - 1); 
			if (c == '\r' || c == '\n')
				size--;
		};
		if (size != value.length())
			value = value.substring(0, size);


		// build the tag
		//
		// NOTE: if we will use different Tag classes, here is the instantiation point
		//
		Tag t = new Tag();
		t.setName(name);
		t.setValue(value);

		// if there is unparsed text => process it
		if (unparsedText != null)
			t.setUnparsedTexts(processUnparsedText(unparsedText));

		return(t);
	}

	/**
	 * this method receives a string that is a sequence of unparsed text and splits it into
	 * different unparsed texts. The algorithm is to split on message begin (i.e: "{1:" and
	 * balance curly braces). This last thing ensures that a single message with unparsed text
	 * inner messages is treated as one single unparsed text.<br />
	 * That is:<br />
	 * 
	 * <pre>
	 * {1:...}                 -- message block 1
	 * {4:...                  -- message block 4
	 *    {1:...}              -- \
	 *    {4:...               -- | one single unparsed text
	 *        {1:...}          -- | for block 4
	 *        {4:...}          -- /
	 *    }
	 * }
	 * </pre>
	 *   
	 * @param unparsedText the unparsed text to split (this parameter cannot be <b>null</b>).
	 * @return the list of unparsed texts. This can be <b>null</b> if the input is the empty string.
	 */
	private UnparsedTextList processUnparsedText(String unparsedText) {


		// prepare to process
		UnparsedTextList list = null;

		// we start a new unparsed text at every "{1:"
		int start = 0;
		while (start < unparsedText.length()) {

			// find the block end (balancing braces)
			int end = start + 1;
			while ( (end + 1) < unparsedText.length() && ! unparsedText.startsWith("{A:", end) ) {
				end = blockTagEnd(unparsedText, end + 1);

				// include trailing white spaces
				while (end < unparsedText.length() && Character.isWhitespace(unparsedText.charAt(end))) {
					end++;
				};
			};

			// separate a text
			String text = unparsedText.substring(start, end).trim();
			if ( ! text.equals("")) {
		
				// add it to the list (create it if needed)
				if (list == null)
					list = new UnparsedTextList();
				list.addText(text);
			};

			// continue with next text
			start = end;
		};


		return(list);
	}

	/**
	 * Identify the block to be consumed.
	 * 
	 * @param s the block identifier
	 * @return the block identifier or a space if the block can not be identified
	 */
	protected char identifyBlock(String s) {
		if (s != null && s.length() > 1) {
			char c = s.charAt(0);
			if ('0' <= c && c <= '9')			// 0-9 are valid block ids (1..5 standard, 0/6..9 user defined)
				return(c);
			if ('a' <= c && c <= 'z')			// a-z are valid block ids (all user defined)
				return(c);
			if ('A' <= c && c <= 'Z')			// A-Z are valid block ids (all user defined)
				return(c);
		}
		return(' ');
	}

	/**
	 * read buffer until end of block is reached.
	 * The initial character of the block must be already consumed and the
	 * reader has to be ready to consume the first character inside the block
	 * 
	 * <p>This method assumes that the starting block character was consumed 
	 * because that is required in order to identify the start of a block, and
	 * call this method which reads until this block ends.</p>
	 *  
	 * @return a string with the block contents
	 * @throws IOException
	 */
	protected String readUntilBlockEnds() throws IOException {
		int start = buffer.length();
		int len	  = 0;
		int c;

		// starts holds the amount of blockstart chars encountered, when this method
		// is called, the initial block start was consumed, and therefore, this is initialized in 1
		// this is needed to be able to include inner {} inside blocks
		int starts = 1; 
		boolean done = false;
		while ( ! done) {
			c = getChar();

			if (c == -1) {
				done = true;
			} else {
				if (isBlockStart((char) c)) {
					starts++;
				};
				if (isBlockEnd((char) c)) {
					starts--;
					if (starts == 0) {
						done = true;
					} else {
						len++;
					};
				} else {
					len++;
				};
			};
		};

		// check for unbalanced { and }
		int end = start + len;
		if (starts != 0 && (end - start) > 0) {
			//if (log.isLoggable(Level.FINE)) log.fine("unbalanced '{' and '}' inside block (starts=" + starts + ")");
		};


		return(buffer.substring(start, end));
	}

	private boolean isBlockEnd(char c) {
		return c == '}';
	}

	/**
	 * read on the reader until a block start character or EOF is reached.
	 * @throws IOException if thrown during read
	 */
	protected void findBlockStart() throws IOException {
		int c ;
		do {
			c = getChar();
		} while (c != -1 && ! isBlockStart((char) c)) ;
	}

	private boolean isBlockStart(char c) {
		return c == '{';
	}

	/**
	 * Read a char from stream and append it to the inner buffer
	 * @return the next char read
	 * @throws IOException if an error occurs during read
	 */
	private int getChar() throws IOException {
		int c = reader.read();
		if (c >= 0)
			buffer.append((char) c);
		return c;
	}
	
	/**
	 * Get a copy of the errors found.
	 * users can manipulate this copy without affecting the original.
	 * 
	 * @return the list of errors found
	 */
	public List<String> getErrors() {
		return new ArrayList<String>(this.errors);
	}
}
