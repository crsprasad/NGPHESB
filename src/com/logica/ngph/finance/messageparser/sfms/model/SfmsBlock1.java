package com.logica.ngph.finance.messageparser.sfms.model;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;

import com.logica.ngph.finance.messageparser.sfms.exceptions.SfmsException;

/**
 * Base class for SFMS Basic Header Block (block 1). It contains information
 * about the source of the message.
 * 
 * The basic header block is fixed-length and continuous with no field
 * delimiters. This class contains its elements as individual attributes for
 * easier management of the block value. This block is mandatory for all SFMS
 * messages.
 * 
 * @author Logica
 * @version
 */
public class SfmsBlock1 extends SfmsValueBlock implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1670169225672179706L;

	public static transient final String BLOCK_IDENTIFIER = "{A:";
	public static transient final String BLOCK_END_IDENTIFIER = "}";
	public static transient final String FILLER = "         ";
	
	/**
	 * String of 3 character containing the Block Identifier Value "{A:"
	 */
	private String blockIdentifier;

	/**
	 * String of 3 character containing the Application Identifier as follows:
	 * Valid values are "F01" It's recommended to set the value in this field to
	 * "F01"
	 */
	private String bankApplicationId;

	/**
	 * String of 3 character containing the Message Identifier
	 * 
	 */
	private String messageIdentifier;

	/**
	 * String of 1 character containing the mode of transition as follows: O -
	 * Output (towards RTGS) I - Input (towards Bank)
	 */
	private String inputOutputIdentifier;

	/**
	 * String of 3 characters containing Message Type 298 = ???
	 */
	private String messageType;

	/**
	 * String of 3 characters containing Sub Message Type 298 = ???
	 */
	private String subMessageType;

	/**
	 * String of 11 characters containing the IFSC Code of Sender
	 * 
	 */
	private String senderAddress;

	/**
	 * String of 11 characters containing the IFSC Code of Receiver
	 * 
	 */
	private String receiverAddress;

	/**
	 * String of 1 character containing Delivery Monitoring Flag 1 = Yes 2 = No
	 */
	private String deliveryMonFlag;
	/**
	 * String of 1 character containing Open Notification Flag 1 = Yes 2 = No
	 */
	private String openNotifyFlag;
	/**
	 * String of 1 character containing Non Delivery Warning Flag 1 = Yes 2 = No
	 */
	private String nonDeliveryWarnFlag;

	/**
	 * String of 3 character containing Obsolescence Period Valid Values are 000
	 * - 999
	 */
	private String obsolescencePeriod;

	/**
	 * String of 16 character containing Message User Reference
	 * 
	 */
	private String messageUserReference;

	/**
	 * String of 1 character containing Possible Duplicate Flag 1 = Yes 2 = No
	 */
	private String possibleDuplicateFlag;
	/**
	 * String of 3 character containing Possible Duplicate Flag "RTG" = Inward
	 * Messages "X X X"(without Spaces) = Outward Message
	 */
	private String serviceIdentifier;

	/**
	 * String of 8 character containing Originating Date Format = YYYYMMDD
	 */
	private String originatingDate;

	/**
	 * String of 4 character containing Originating Time Format = HHMM
	 */
	private String originatingTime;

	/**
	 * String of 1 character containing Testing and Training Flag 1 = Yes 2 = No
	 */
	private String testingAndTrainingFlag;

	/**
	 * String of 9 character containing Sequence Number
	 */
	private String sequenceNumber;

	/**
	 * String of 9 character Filler
	 * 
	 */
	private String filler;

	/**
	 * String of 16 character containing the Unique Transaction Reference
	 * 
	 */
	private String uniqueTransactionReference;
	/**
	 * String of 2 character containing the RTGS Priority
	 * 
	 */
	private String priority;

	/**
	 * String of 1 character containing the Block End Identifier
	 * 
	 */
	private String blockEndIdentifier;

	//F Series related messages. (F27)
	private String bankAPIResponseCode;
	
	/**
	 * Default constructor
	 */
	public SfmsBlock1() {

	}

	/**
	 * Creates a block A object setting attributes by parsing the fixed string
	 * argument;
	 * 
	 * @param value
	 *            a fixed length string of 114 (which must start with '{A:')
	 *            characters containing the blocks value
	 */
	public SfmsBlock1(String value) {

		this.setValue(value);
	}

	/**
	 * Constructor for specific values
	 * 
	 */
	public SfmsBlock1(String applicationId, String messageId,
			String ioIdentifier, String messageType, String subMessageType,
			String sender, String receiver, String monFlag, String notifyFlag,
			String warnFlag, String ObsolPeriod, String mur, String dupFlag,
			String serviceId, String originDate, String originTime,
			String ttFlag, String seqNo, String utr, String priority) {

		this.blockIdentifier = BLOCK_IDENTIFIER;
		this.bankApplicationId = applicationId;
		this.messageIdentifier = messageId;
		this.inputOutputIdentifier = ioIdentifier;
		this.messageType = messageType;
		this.subMessageType = subMessageType;
		this.senderAddress = sender;
		this.receiverAddress = receiver;
		this.deliveryMonFlag = monFlag;
		this.openNotifyFlag = notifyFlag;
		this.nonDeliveryWarnFlag = warnFlag;
		this.obsolescencePeriod = ObsolPeriod;
		this.messageUserReference = mur;
		this.possibleDuplicateFlag = dupFlag;
		this.serviceIdentifier = serviceId;
		this.originatingDate = originDate;
		this.originatingTime = originTime;
		this.testingAndTrainingFlag = ttFlag;
		this.sequenceNumber = seqNo;
		this.filler = FILLER;
		this.uniqueTransactionReference = utr;
		this.priority = priority;
		this.blockEndIdentifier = BLOCK_END_IDENTIFIER;
	}

	@Override
	public boolean equals(Object obj) {
		if (this != null && obj != null) {
			if (CompareToBuilder.reflectionCompare(this, obj) == 0)
				return true;
			else
				return false;
		} else
			return false;
	}

	/**
	 * @return the filler
	 */
	public String getFiller() {
		return filler;
	}

	/**
	 * @param filler
	 *            the filler to set
	 */
	public void setFiller(String filler) {
		this.filler = filler;
	}

	/**
	 * @return the bankApplicationId
	 */
	public String getBankApplicationId() {
		return bankApplicationId;
	}

	/**
	 * @return the blockEndIdentifier
	 */
	public String getBlockEndIdentifier() {
		return blockEndIdentifier;
	}

	/**
	 * @return the blockIdentifier
	 */
	public String getBlockIdentifier() {
		return blockIdentifier;
	}

	/**
	 */
	@Override
	public String getBlockValue() {
		return getValue();
	}

	/**
	 * @return the deliveryMonFlag
	 */
	public String getDeliveryMonFlag() {
		return deliveryMonFlag;
	}

	/**
	 * @return the inputOutputIdentifier
	 */
	public String getInputOutputIdentifier() {
		return inputOutputIdentifier;
	}

	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * @return the messageUserReference
	 */
	public String getMessageUserReference() {
		return messageUserReference;
	}

	/**
	 * @return the messageIdentifier
	 */
	public String getMessageIdentifier() {
		return messageIdentifier;
	}

	/**
	 * @param messageIdentifier
	 *            the messageIdentifier to set
	 */
	public void setMessageIdentifier(String messageIdentifier) {
		this.messageIdentifier = messageIdentifier;
	}

	/**
	 * Returns the block name (the value 1 as a string)
	 * 
	 * @return block name
	 * 
	 */
	@Override
	public String getName() {
		return ("A");
	}

	/**
	 * @return the nonDeliveryWarnFlag
	 */
	public String getNonDeliveryWarnFlag() {
		return nonDeliveryWarnFlag;
	}

	/**
	 * Returns the block number (the value 1 as an integer)
	 * 
	 * @return Integer containing the block's number
	 */
	@Override
	public Integer getNumber() {
		return new Integer(1);
	}

	/**
	 * @return the obsolescencePeriod
	 */
	public String getObsolescencePeriod() {
		return obsolescencePeriod;
	}

	/**
	 * @return the openNotifyFlag
	 */
	public String getOpenNotifyFlag() {
		return openNotifyFlag;
	}

	/**
	 * @return the originatingDate
	 */
	public String getOriginatingDate() {
		return originatingDate;
	}

	/**
	 * @return the originatingTime
	 */
	public String getOriginatingTime() {
		return originatingTime;
	}

	/**
	 * @return the possibleDuplicateFlag
	 */
	public String getPossibleDuplicateFlag() {
		return possibleDuplicateFlag;
	}

	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * @return the receiverAddress
	 */
	public String getReceiverAddress() {
		return receiverAddress;
	}

	/**
	 * @return the senderAddress
	 */
	public String getSenderAddress() {
		return senderAddress;
	}

	/**
	 * @return the sequenceNumber
	 */
	public String getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @return the serviceIdentifier
	 */
	public String getServiceIdentifier() {
		return serviceIdentifier;
	}

	/**
	 * @return the subMessageType
	 */
	public String getSubMessageType() {
		return subMessageType;
	}

	/**
	 * @return the testingAndTrainingFlag
	 */
	public String getTestingAndTrainingFlag() {
		return testingAndTrainingFlag;
	}

	/**
	 * @return the uniqueTransactionReference
	 */
	public String getUniqueTransactionReference() {
		return uniqueTransactionReference;
	}

	/** F27 Related member variable methods **/
	public String getBankAPIResponseCode() {
		return bankAPIResponseCode;
	}

	public void setBankAPIResponseCode(String bankAPIResponseCode) {
		this.bankAPIResponseCode = bankAPIResponseCode;
	}

	/**
	 * Gets the fixed length block 1 value, as a result of concatenating its
	 * individual elements as follow:<br>
	 * Application ID Service ID + Logical terminal (LT) address + Session
	 * number + Sequence number.<br>
	 * Notice that this method does not return the "1:" string.
	 */
	@Override
	public String getValue() {
		if (isEmpty())
			return null;
		StringBuffer v = new StringBuffer();

		if (blockIdentifier != null)
			v.append(blockIdentifier);
		if (bankApplicationId != null)
			v.append(bankApplicationId);
		if (messageIdentifier != null)
			v.append(messageIdentifier);
		if (inputOutputIdentifier != null)
			v.append(inputOutputIdentifier);
		if (messageType != null)
			v.append(messageType);
		if (subMessageType != null)
			v.append(subMessageType);
		if (senderAddress != null)
			v.append(senderAddress);
		if (receiverAddress != null)
			v.append(receiverAddress);
		if (deliveryMonFlag != null)
			v.append(deliveryMonFlag);
		if (openNotifyFlag != null)
			v.append(openNotifyFlag);
		if (nonDeliveryWarnFlag != null)
			v.append(nonDeliveryWarnFlag);
		if (obsolescencePeriod != null)
			v.append(obsolescencePeriod);
		if (messageUserReference != null)
			v.append(messageUserReference);
		if (possibleDuplicateFlag != null)
			v.append(possibleDuplicateFlag);
		if (serviceIdentifier != null)
			v.append(serviceIdentifier);
		if (originatingDate != null)
			v.append(originatingDate);
		if (originatingTime != null)
			v.append(originatingTime);
		if (testingAndTrainingFlag != null)
			v.append(testingAndTrainingFlag);
		if (sequenceNumber != null)
			v.append(sequenceNumber);
		if (filler != null)
			v.append(filler);
		if (uniqueTransactionReference != null)
			v.append(uniqueTransactionReference);
		if (priority != null)
			v.append(priority);
		if (blockEndIdentifier != null)
			v.append(blockEndIdentifier);
		return v.toString();
	}

	/**
	 * Tell if this block is empty or not. This block is considered to be empty
	 * if all its attributes are set to null.
	 * 
	 * @return true if all fields are null and false in other case
	 */
	@Override
	public boolean isEmpty() {
		return (blockIdentifier == null && bankApplicationId == null
				&& messageIdentifier == null && messageType == null && subMessageType == null);
	}

	/**
	 * @param bankApplicationId
	 *            the bankApplicationId to set
	 */
	public void setBankApplicationId(String bankApplicationId) {
		this.bankApplicationId = bankApplicationId;
	}

	/**
	 * @param blockEndIdentifier
	 *            the blockEndIdentifier to set
	 */
	public void setBlockEndIdentifier(String blockEndIdentifier) {
		this.blockEndIdentifier = blockEndIdentifier;
	}

	/**
	 * @param blockIdentifier
	 *            the blockIdentifier to set
	 */
	public void setBlockIdentifier(String blockIdentifier) {
		this.blockIdentifier = blockIdentifier;
	}

	/**
	 * Sets the block name. Will cause an exception unless setting block number
	 * to A.
	 * 
	 * @param blockName the block name to set
	 * @throws IllegalArgumentException if parameter blockName is not the string "1"
	 */
	@Override
	protected void setBlockName(String blockName) {
		// sanity check
		Validate.notNull(blockName, "parameter 'blockName' cannot be null");
		Validate.isTrue(blockName.compareTo("A") == 0, "blockName must be string 'A'");
	}

	/**
	 * Sets the block number.
	 * 
	 * @param blockNumber
	 *            the block number to set
	 * @throws IllegalArgumentException
	 *             if parameter blockName is not the integer 1
	 */
	@Override
	protected void setBlockNumber(Integer blockNumber) {
		// sanity check
		Validate.notNull(blockNumber, "parameter 'blockNumber' cannot be null");
		Validate.isTrue(blockNumber.intValue() == 1, "blockNumber must be 1");
	}

	/**
	 * @see #setValue(String)
	 */
	@Override
	public void setBlockValue(String value) {
		setValue(value);
	}

	/**
	 * @param deliveryMonFlag
	 *            the deliveryMonFlag to set
	 */
	public void setDeliveryMonFlag(String deliveryMonFlag) {
		this.deliveryMonFlag = deliveryMonFlag;
	}

	/**
	 * @param inputOutputIdentifier
	 *            the inputOutputIdentifier to set
	 */
	public void setInputOutputIdentifier(String inputOutputIdentifier) {
		this.inputOutputIdentifier = inputOutputIdentifier;
	}

	/**
	 * @param messageType
	 *            the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * @param messageUserReference
	 *            the messageUserReference to set
	 */
	public void setMessageUserReference(String messageUserReference) {
		this.messageUserReference = messageUserReference;
	}

	/**
	 * @param nonDeliveryWarnFlag
	 *            the nonDeliveryWarnFlag to set
	 */
	public void setNonDeliveryWarnFlag(String nonDeliveryWarnFlag) {
		this.nonDeliveryWarnFlag = nonDeliveryWarnFlag;
	}

	/**
	 * @param obsolescencePeriod
	 *            the obsolescencePeriod to set
	 */
	public void setObsolescencePeriod(String obsolescencePeriod) {
		this.obsolescencePeriod = obsolescencePeriod;
	}

	/**
	 * @param openNotifyFlag
	 *            the openNotifyFlag to set
	 */
	public void setOpenNotifyFlag(String openNotifyFlag) {
		this.openNotifyFlag = openNotifyFlag;
	}

	/**
	 * @param originatingDate
	 *            the originatingDate to set
	 */
	public void setOriginatingDate(String originatingDate) {
		this.originatingDate = originatingDate;
	}

	/**
	 * @param originatingTime
	 *            the originatingTime to set
	 */
	public void setOriginatingTime(String originatingTime) {
		this.originatingTime = originatingTime;
	}

	/**
	 * @param possibleDuplicateFlag
	 *            the possibleDuplicateFlag to set
	 */
	public void setPossibleDuplicateFlag(String possibleDuplicateFlag) {
		this.possibleDuplicateFlag = possibleDuplicateFlag;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	/**
	 * @param receiverAddress
	 *            the receiverAddress to set
	 */
	public void setReceiverAddress(String receiverAddress) {
		this.receiverAddress = receiverAddress;
	}

	/**
	 * @param senderAddress
	 *            the senderAddress to set
	 */
	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @param serviceIdentifier
	 *            the serviceIdentifier to set
	 */
	public void setServiceIdentifier(String serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}

	/**
	 * @param subMessageType
	 *            the subMessageType to set
	 */
	public void setSubMessageType(String subMessageType) {
		this.subMessageType = subMessageType;
	}

	/**
	 * @param testingAndTrainingFalg
	 *            the testingAndTrainingFalg to set
	 */
	public void setTestingAndTrainingFlag(String testingAndTrainingFalg) {
		this.testingAndTrainingFlag = testingAndTrainingFalg;
	}

	/**
	 * @param uniqueTransactionReference
	 *            the uniqueTransactionReference to set
	 */
	public void setUniqueTransactionReference(String uniqueTransactionReference) {
		this.uniqueTransactionReference = uniqueTransactionReference;
	}

	/**
	 * Sets the block's attributes by parsing the fixed string argument;<br>
	 * 
	 * @param value
	 *            a fixed length string of 114 (which must start with '{A:')
	 *            characters containing the blocks value
	 */
	@Override
	public void setValue(String value) {
		//The Maximum length of any F-Series message is 56 chars including Start and End braces ("{")
		if (value.length() > 54) {
			setGeneralHeader(value);
		} else {
			//This message may belong to NEFT's F series.
			String messageCode = value.substring(5, 8);
			if (messageCode.equalsIgnoreCase("F25") || messageCode.equalsIgnoreCase("F26") || messageCode.equalsIgnoreCase("F27")) 
			{
				setF27MessageData(value);
			}
			else if(messageCode.equalsIgnoreCase("F20") || messageCode.equalsIgnoreCase("F22") || messageCode.equalsIgnoreCase("F23") || messageCode.equalsIgnoreCase("F24"))
			{
				setF20_23MessageData(value);
			}
			else 
			{
				//Currently the system would deal with only F27-message.
				throw new SfmsException("The message " + messageCode + " can't be processed by the PaymentHub");
			}
		}
	}
	
	/**
	 * Sets the header data to the local member variables by parsing it by using fixed length format.
	 * This header data may belong to any kind of SFMS message except for F-series messages.
	 * @param headerData {@link String}
	 */
	private void setGeneralHeader(String headerData) {
		// check parameters
		int slen = headerData.length();
		Validate.notNull(headerData, "value must not be null");
		Validate.isTrue(slen == 112,
						"expected a 112 chars string and obtained a " + headerData.length() + " chars string: " + headerData);

		
		int offset = 0;
		int len;

		// separate value fragments
		len = 2;
		this.setBlockIdentifier(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 3;
		this.setBankApplicationId(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 3;
		this.setMessageIdentifier(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 1;
		this.setInputOutputIdentifier(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 3;
		this.setMessageType(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 3;
		this.setSubMessageType(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 11;
		this.setSenderAddress(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 11;
		this.setReceiverAddress(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 1;
		this.setDeliveryMonFlag(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 1;
		this.setOpenNotifyFlag(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 1;
		this.setNonDeliveryWarnFlag(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 3;
		this.setObsolescencePeriod(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 16;
		this.setMessageUserReference(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 1;
		//Eliminating the newline character -- Prasad -- TBD: Remove newline characters from the header block
		String strValue = this.getValuePart(headerData, offset, len);
		
		if (strValue.equalsIgnoreCase("\n")|| strValue.equalsIgnoreCase("\r")) {
			offset += len;
			len = 1;
			this.setPossibleDuplicateFlag(this.getValuePart(headerData, offset, len));
		} else {
			this.setPossibleDuplicateFlag(strValue);
		}
		
				
		offset += len;
		len = 3;
		this.setServiceIdentifier(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 8;
		this.setOriginatingDate(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 4;
		this.setOriginatingTime(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 1;
		this.setTestingAndTrainingFlag(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 9;
		this.setSequenceNumber(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 9;
		this.setFiller(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 16;
		this.setUniqueTransactionReference(this.getValuePart(headerData, offset, len));
		
		offset += len;
		len = 2;
		this.setPriority(this.getValuePart(headerData, offset, len));
		
		this.setBlockEndIdentifier("}");
		offset += len;
	}

	/**
	 * Sets the F20 and F23 messages' data to the relevant member variables.
	 */
	private void setF20_23MessageData(String value) {
		// check parameters
		int slen = value.length();
		Validate.notNull(value, "value must not be null");
		Validate.isTrue(slen == 41,"expected a 41 chars string and obtained a " + value.length() + " chars string: " + value);

		int offset = 0;
		int len;

		// separate value fragments
		len = 2;
		this.setBlockIdentifier(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 3;
		this.setBankApplicationId(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 3;
		this.setMessageIdentifier(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 1;
		this.setInputOutputIdentifier(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 9;
		this.setSequenceNumber(this.getValuePart(value, offset, len));

		offset += len;
		len = 11;
		this.setSenderAddress(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 8;
		this.setOriginatingDate(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 4;
		this.setOriginatingTime(this.getValuePart(value, offset, len));

		this.setBlockEndIdentifier("}");
		offset += len;
	}

	/**
	 * Sets the F27 messages' data to the relevant member variables.
	 */
	private void setF27MessageData(String value) {
		// check parameters
		int slen = value.length();
		Validate.notNull(value, "value must not be null");
		Validate.isTrue(slen == 54,
						"expected a 54 chars string and obtained a " + value.length() + " chars string: " + value);

		int offset = 0;
		int len;

		// separate value fragments
		len = 2;
		this.setBlockIdentifier(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 3;
		this.setBankApplicationId(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 3;
		this.setMessageIdentifier(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 1;
		this.setInputOutputIdentifier(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 9;
		this.setSequenceNumber(this.getValuePart(value, offset, len));

		offset += len;
		len = 11;
		this.setSenderAddress(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 8;
		this.setOriginatingDate(this.getValuePart(value, offset, len));
		
		offset += len;
		len = 4;
		this.setOriginatingTime(this.getValuePart(value, offset, len));

		offset += len;
		if (getMessageIdentifier().equalsIgnoreCase("F25") || getMessageIdentifier().equalsIgnoreCase("F26"))
		{
			len = 4;
		}
		else
		{
			len = 11;
		}
		this.setBankAPIResponseCode(this.getValuePart(value, offset, len));
		
		offset += len;
		if (getMessageIdentifier().equalsIgnoreCase("F25") || getMessageIdentifier().equalsIgnoreCase("F26"))
		{
			len = 9;
		}
		else
		{
			len = 2;
		}
		this.setFiller(this.getValuePart(value, offset, len));
		
		this.setBlockEndIdentifier("}");
		offset += len;
	}
	
	/**
	 * Checks whether Block4 exists or not in a given finance message.
	 * @param financeMessage {@link String}
	 * @author Prasad. B.S.R. 11/May/2010
	 * @return boolean
	 */
	@SuppressWarnings("unused")
	private boolean isBlock4Exsit(String financeMessage) {
		final String regEx = "4:";
		final Pattern regExPattern;
		final Matcher regExMatcher;

		regExPattern = Pattern.compile(regEx);
		regExMatcher = regExPattern.matcher(financeMessage);
		return regExMatcher.find();
	}
}
