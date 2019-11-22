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

import org.apache.commons.lang.Validate;

/**
 * Base class for SWIFT <b>Application Header Block (block 2)
 * for OUTPUT (from SWIFT)</b>.<br>
 * This block is used to construct messages that have been 
 * <i>output</i> from the SWIFT network. From the application point
 * of view, it correspond to the <i>RECEIVED</i> messages.<br><br>
 * 
 * It's value is fixed-length and continuous with no field delimiters. 
 * This class contains its elements as individual attributes for 
 * easier management of the block value.<br>
 * This is an optional block.
 * 
 * @author zubri, mikkey
 * @since 4.0
 * @version $Revision: 1.7 $ 
 */
//TODO: add parameter checks (Validate.*) and complete javadocs 
public class SwiftBlock2Output extends SwiftBlock2 implements Serializable {
	//private static transient final java.util.logging.Logger log = java.util.logging.Logger.getLogger(SwiftBlock2.class);

	/** 
	 * String of 4 characters containing the input time with respect to the sender
	 */
	private String senderInputTime;
	
	/**
	 * String of 28 characters containing the Message Input Reference 
	 * (MIR), it is always local to the sender of the message.
	 * It includes the date the sender sent the message to SWIFT,
	 * followed by the full LT address of the sender of the 
	 * message, and the sender's session and sequence to SWIFT.
	 * YYMMDD BANKBEBBAXXX 2222 123456<br><br>
	 * This particular attribute is a 6 characters string containing 
	 * the date field of the MIR.
	 */
	private String MIRDate;
	
	/**
	 * String of 12 characters containing the logical terminal field of the MIR
	 * (address of the sender of the message).
	 * @see "MIR on the WIFE Wiki"
	 */
	private String MIRLogicalTerminal;
	
	/**
	 * String of 4 characters containing the session number field of the MIR.
	 * @see "MIR on the WIFE Wiki"
	 */
	private String MIRSessionNumber;
	
	/**
	 * String of 6 characters containing the sequence number field of the MIR.
	 * @see "MIR on the WIFE Wiki"
	 */
	private String MIRSequenceNumber;	
	
	/**
	 * String of 6 characters containing the Output date local 
	 * to the receiver, written in the following format: YYMMDD
	 */
	private String receiverOutputDate;
	
	/**
	 * String of 4 characters containing the Output time local 
	 * to the receiver, written in the following format: HHMM
	 */
	private String receiverOutputTime;

    /**
	 * Constructor for specific values
	 * 
	 * @param messageType the message type
	 * @param senderInputTime the input time
	 * @param MIRDate date
	 * @param MIRLogicalTerminal logical terminal
	 * @param MIRSessionNumber session number
	 * @param MIRSequenceNumber message sequence number
	 * @param receiverOutputDate receiver date
	 * @param receiverOutputTime receiver time
	 * @param messagePriority the message priority (S=system, U=urgent, N=normal)
	 */
	public SwiftBlock2Output(String messageType, String senderInputTime, String MIRDate, String MIRLogicalTerminal, String MIRSessionNumber, String MIRSequenceNumber, String receiverOutputDate, String receiverOutputTime, String messagePriority) {
		super();
		this.messageType 		= messageType;
		this.senderInputTime 	= senderInputTime;
		this.MIRDate 			= MIRDate;
		this.MIRLogicalTerminal = MIRLogicalTerminal;
		this.MIRSessionNumber 	= MIRSessionNumber;
		this.MIRSequenceNumber 	= MIRSequenceNumber;
		this.receiverOutputDate = receiverOutputDate;
		this.receiverOutputTime = receiverOutputTime;
		this.messagePriority 	= messagePriority;
	}
	
	/**
	 * Creates a block 2 output object setting attributes by parsing the fixed string argument;<br>
	 * for example "O1001200970103BANKBEBBAXXX22221234569701031201N" or
	 * "2:O1001200970103BANKBEBBAXXX22221234569701031201N"
	 * 
	 * @param value a fixed length string of 47 (starting with 'O') or 49 (starting with '2:O') characters containing the blocks value
	 */
	public SwiftBlock2Output(String value) {
		super();
		this.setValue(value);
	}
	
	/**
	 * Default Constructor
	 */
	public SwiftBlock2Output() {
		super();
	}
	
	/**
	 * Sets the input time with respect to the sender
	 * 
	 * @param senderInputTime 4 numbers HHMM
	 */
	public void setSenderInputTime(String senderInputTime) {
		this.senderInputTime = senderInputTime;
	}

	/**
	 * Returns the input time with respect to the sender
	 * 
	 * @return 4 numbers HHMM
	 */
	public String getSenderInputTime() {
		return senderInputTime;
	}

	/**
	 * Sets the date the sender sent the message to SWIFT,
	 * from the MIR field
	 * 
	 * @param MIRDate 6 numbers with date in format YYMMDD
	 */
	public void setMIRDate(String MIRDate) {
		this.MIRDate = MIRDate;
	}

	/**
	 * Gets the date the sender sent the message to SWIFT,
	 * from the MIR field, in the format YYMMDD
	 * 
	 * @return String with 6 numbers
	 */
	public String getMIRDate() {
		return this.MIRDate;
	}

	/**
	 * Sets the the full LT address of the sender of the 
	 * message, from the MIR field, for example: BANKBEBBAXXX
	 * 
	 * @param MIRLogicalTerminal
	 */
	public void setMIRLogicalTerminal(String MIRLogicalTerminal) {
		this.MIRLogicalTerminal = MIRLogicalTerminal;
	}

	/**
	 * Gets the the full LT address of the sender of the 
	 * message, from the MIR field, for example: BANKBEBBAXXX
	 * 
	 * @return LT address
	 */
	public String getMIRLogicalTerminal() {
		return this.MIRLogicalTerminal;
	}
	
	/**
	 * Sets the session number field of the MIR
	 * 
	 * @param MIRSessionNumber 4 numbers
	 */
	public void setMIRSessionNumber(String MIRSessionNumber) {
		this.MIRSessionNumber = MIRSessionNumber;
	}

	/**
	 * Gets the date the sender session number,
	 * from the MIR field, in the format NNNN
	 * 
	 * @return 4 numbers
	 */
	public String getMIRSessionNumber() {
		return this.MIRSessionNumber;
	}

	/**
	 * Sets the sequence number field of the MIR
	 * 
	 * @param MIRSequenceNumber 6 numbers
	 */
	public void setMIRSequenceNumber(String MIRSequenceNumber) {
		this.MIRSequenceNumber = MIRSequenceNumber;
	}

	/**
	 * Gets the date the sender sequence number,
	 * from the MIR field, in the format NNNNNN
	 * 	  
	 * @return 6 numbers
	 */
	public String getMIRSequenceNumber() {
		return MIRSequenceNumber;
	}
	
	/**
	 * Gets the full MIR (Message Input Reference) string of 28 
	 * characters containing the sender's date, LT address,
	 * session and sequence:<br>
	 * for example YYMMDDBANKBEBBAXXX2222123456<br>
	 */
	public String getMIR() {
		if (MIRDate == null && MIRLogicalTerminal == null && MIRSessionNumber == null && MIRSequenceNumber == null)
			return null;
		StringBuffer v = new StringBuffer();
		if (MIRDate != null)
			v.append(MIRDate);
		if (MIRLogicalTerminal != null)
			v.append(MIRLogicalTerminal);
		if (MIRSessionNumber != null)
			v.append(MIRSessionNumber);
		if (MIRSequenceNumber != null)
			v.append(MIRSequenceNumber);
		return v.toString();
	}
	
	/**
	 * Sets the full MIR (Message Input Reference) string of 28 
	 * characters containing the sender's date, LT address,
	 * session and sequence:<br>
	 * for example YYMMDDBANKBEBBAXXX2222123456<br>
	 * 
	 * @param mir complete MIR string
	 */
	public void setMIR(String mir) {
		Validate.notNull(mir);
		Validate.isTrue(mir.length() == 28, "expected a 28 characters string for MIR value and found a "+ mir.length() +" string:" +mir);
		StringBuffer sb = new StringBuffer(mir);
		
		int offset = 0;
		int len;
		
		len = 6;
		this.setMIRDate(String.valueOf(sb.subSequence(offset, offset+len)));
		offset+=len;
		
		len = 12;
		this.setMIRLogicalTerminal(String.valueOf(sb.subSequence(offset, offset+len)));
		offset+=len;
	
		len = 4;
		this.setMIRSessionNumber(String.valueOf(sb.subSequence(offset, offset+len)));
		offset+=len;
	
		len = 6;
		this.setMIRSequenceNumber(String.valueOf(sb.subSequence(offset, offset+len)));
		offset+=len;
	}
	
	/**
	 * Sets the Output date local to the receiver, written in the following format: YYMMDD
	 * 
	 * @param receiverOutputDate 6 characters in format YYMMDD
	 */
	public void setReceiverOutputDate(String receiverOutputDate) {
		this.receiverOutputDate = receiverOutputDate;
	}

	/**
	 * Gets the Output date local to the receiver
	 * 
	 * @return 6 characters in format YYMMDD
	 */
	public String getReceiverOutputDate() {
		return receiverOutputDate;
	}
	
	/**
	 * Sets the Output time local to the receiver, written in the following format: HHMM
	 * 
	 * @param receiverOutputTime String with 4 numbers
	 */
	public void setReceiverOutputTime(String receiverOutputTime) {
		this.receiverOutputTime = receiverOutputTime;
	}
	
	/**
	 * Gets the Output time local to the receiver, written in the following format: HHMM
	 * 
	 * @return String with 4 numbers
	 */
	public String getReceiverOutputTime() {
		return receiverOutputTime;
	}
	
	/**
	 * Tell if this block is empty or not.
	 * This block is considered to be empty if all its attributes are set to <code>null</code>.
	 * @return <code>true</code> if all fields are <code>null</code> and false in other case
	 */
	public boolean isEmpty() {
		return (messageType==null && senderInputTime==null && getMIR()==null && receiverOutputDate==null && receiverOutputTime==null && messagePriority==null);
	}
	
	/**
	 * Gets the fixed length block 2 value, as a result of
	 * concatenating its individual elements as follow:<br>
	 * Message Type +
	 * Sender Input Time +
	 * MIR +
	 * Receiver Output Date +
	 * Receiver Output Time +
	 * Message Priority.
	 */
	public String getValue() {
		if (isEmpty()) 
			return null;
		StringBuffer v = new StringBuffer("O");
		if (messageType        != null) v.append(messageType);
		if (senderInputTime    != null) v.append(senderInputTime);
		if (getMIR()           != null) v.append(getMIR());
		if (receiverOutputDate != null) v.append(receiverOutputDate);
		if (receiverOutputTime != null) v.append(receiverOutputTime);
		if (messagePriority    != null) v.append(messagePriority);
		return v.toString();
	}
	
	/**
	 * @see #getValue()
	 */
	public String getBlockValue() {
		return getValue();
	}
	
	/**
	 * Sets the block's attributes by parsing the string argument
	 * containing the blocks value. This value can be in different
	 * flavors because some fields are optional.<br />
	 * Example of supported values:<br />
	 * <pre>
	 *   "O1001200970103BANKBEBBAXXX22221234569701031201"  (46) or
	 * "2:O1001200970103BANKBEBBAXXX22221234569701031201"  (48)   // used for service/system messages
	 *   "O1001200970103BANKBEBBAXXX22221234569701031201N" (47) or
	 * "2:O1001200970103BANKBEBBAXXX22221234569701031201N" (49)
	 * </pre><br />
	 * 
	 * @param value a string with the value to split
	 */
	public void setValue(String value) {

		// check parameters
		int slen = value.length();
		Validate.notNull(value, "value must not be null");
		Validate.isTrue(slen == 46 || slen == 48 || slen == 47 || slen == 49,
						"expected a string value of 46 and up to 49 chars and obtained a " + slen + " chars string: '" + value + "'");

		// figure out the starting point and check the input value has proper optional
		int offset = 0;
		if (value.startsWith("2:O")) {			// accept 2:...
			offset = 2;
		};
		slen -= offset;
		if (slen != 46 && slen != 47) {
			 throw new IllegalArgumentException("Value must match: O<mt><time><mir><date><time>[<pri>]");
		};
		if (Character.toUpperCase(value.charAt(offset)) != 'O') {
			 throw new IllegalArgumentException("Value must match: O<mt><time><mir><date><time>[<pri>]");
		};
		offset++;			// skip the output mark

		// separate value fragments
		int len;
		len =  3; this.setMessageType       (this.getValuePart(value, offset, len)); offset += len;
		len =  4; this.setSenderInputTime   (this.getValuePart(value, offset, len)); offset += len;
		len = 28; this.setMIR               (this.getValuePart(value, offset, len)); offset += len;
		len =  6; this.setReceiverOutputDate(this.getValuePart(value, offset, len)); offset += len;
		len =  4; this.setReceiverOutputTime(this.getValuePart(value, offset, len)); offset += len;
		len =  1; this.setMessagePriority   (this.getValuePart(value, offset, len)); offset += len;	// optional (system messages)
	}

	/**
	 * @see #setValue(String)
	 */
	public void setBlockValue(String value) {
		setValue(value);
	}
	
	/**
	 * Returns true if this block 2 is an input block 2
	 * @return true if block 2 is input, false if is output
	 */
	public boolean isInput() {
		return false;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((MIRDate == null) ? 0 : MIRDate.hashCode());
		result = prime * result + ((MIRLogicalTerminal == null) ? 0 : MIRLogicalTerminal.hashCode());
		result = prime * result + ((MIRSequenceNumber == null) ? 0 : MIRSequenceNumber.hashCode());
		result = prime * result + ((MIRSessionNumber == null) ? 0 : MIRSessionNumber.hashCode());
		result = prime * result + ((messagePriority == null) ? 0 : messagePriority.hashCode());
		result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
		result = prime * result + ((receiverOutputDate == null) ? 0 : receiverOutputDate.hashCode());
		result = prime * result + ((receiverOutputTime == null) ? 0 : receiverOutputTime.hashCode());
		result = prime * result + ((senderInputTime == null) ? 0 : senderInputTime.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SwiftBlock2Output other = (SwiftBlock2Output) obj;
		if (MIRDate == null) {
			if (other.MIRDate != null)
				return false;
		} else if (!MIRDate.equals(other.MIRDate))
			return false;
		if (MIRLogicalTerminal == null) {
			if (other.MIRLogicalTerminal != null)
				return false;
		} else if (!MIRLogicalTerminal.equals(other.MIRLogicalTerminal))
			return false;
		if (MIRSequenceNumber == null) {
			if (other.MIRSequenceNumber != null)
				return false;
		} else if (!MIRSequenceNumber.equals(other.MIRSequenceNumber))
			return false;
		if (MIRSessionNumber == null) {
			if (other.MIRSessionNumber != null)
				return false;
		} else if (!MIRSessionNumber.equals(other.MIRSessionNumber))
			return false;
		if (messagePriority == null) {
			if (other.messagePriority != null)
				return false;
		} else if (!messagePriority.equals(other.messagePriority))
			return false;
		if (messageType == null) {
			if (other.messageType != null)
				return false;
		} else if (!messageType.equals(other.messageType))
			return false;
		if (receiverOutputDate == null) {
			if (other.receiverOutputDate != null)
				return false;
		} else if (!receiverOutputDate.equals(other.receiverOutputDate))
			return false;
		if (receiverOutputTime == null) {
			if (other.receiverOutputTime != null)
				return false;
		} else if (!receiverOutputTime.equals(other.receiverOutputTime))
			return false;
		if (senderInputTime == null) {
			if (other.senderInputTime != null)
				return false;
		} else if (!senderInputTime.equals(other.senderInputTime))
			return false;
		return true;
	}
}
