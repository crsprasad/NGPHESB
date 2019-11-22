package com.logica.ngph.esb.constants;

import java.math.BigDecimal;

/**
 * 
 * @author guptarb
 *
 */
public interface NgphEsbConstants {
	
	//Lc Mast (LcStatus)
	int lcStatus = 1;
	
	String RTGS_LEAST_PRIORITY="99";
	//SwiftQueue
	String SWIFTQ = "SWIFTQUEUE.GW";
	int LC_PRE_ADVICE = 1;
	int LC_PRE_ADVICE_ACK = 2;
	int LC_OPENED = 3;
	int LC_REGISTERED = 4;
	int LC_AMENDED = 5;
	int LC_AMEND_REGISTERED = 6;
	int LC_PYMT_ADVICED = 7;
	int LC_PYMT_ADVICE_ACK = 8;
	int LC_PYMT_AUTHORISED = 9;
	int LC_PYMT_AUTHORISEACK = 10;
	int LC_CLAIMED = 11;
	int LC_CLAIM_PAID = 12;
	int LC_DISCREPANT = 13;
	int LC_DISCREPANT_APPROVED = 14;
	int LC_ADVICE_ACCEPTED = 15;
	int LC_ADVICE_REJECTED = 16;
	int LC_DISCREPANT_PYMT_ADVICED = 17;
	int LC_ACKNOWLEDGED  =18;
	
	int BG_CREATED = 1;
	int BG_REQUESTED = 2;
	int BG_CREATEDACK = 3;
	int BG_AMENDED = 4;
	int BG_AMENDED_REGISTERED = 5;
	int BG_REDUCED = 6;
	int BG_REDUCED_ACK = 7;
	int BG_RELEASE_REQUEST = 8;
	int BG_RELEASED = 9;

	//Report Queue 
	String reportQueue = "ReportRPTQueueGW";
	
	//SFMSQueue
	String SFMSQ = "SFMSQUEUE.GW";
	
	//ISOQueue
	String ISO8583Q = "ISO8583QUEUE.GW";
	
	String ISO20022Q = "ISO20022QUEUE.GW";
	
	//Response Handler Queue. All Channel Services will put message in this queue
	String ResHandlerQ = "RESPONSEQUEUE.GW";
	
	//Response Handler Queue. All Channel Services will put message in this queue
	String ReqHandlerQ = "9010.TO.QNG.GW";
	
	//Rule category for Routing Service
	String RULE_CATEGORY_INTV = "INTV";

	// Seq_Identifier for Transaction Reference
	String SEQUENCE_KEY_TXN = "TXNREF";
	
	// Seq_Identifier of Transaction Reference Date Format
	String NGPH_TXNREF_DATE_FORMAT = "ddMMyyyy";
	
	//Rule category for Routing Service
	String RULE_CATEGORY_RTNG = "RTNG";
	
	//Inwards payment
	String INWARD_PAYMENT = "I";
	
	//Outward payment
	String OUTWARD_PAYMENT = "O";
	
	//RULE CATEGORY FOR eNTITY SERVICE'S DEPT finding.
	String RULE_CATEGORY_DEPT = "DEPT";
	
	//INITIALISED STATIC VALUE FOR BRANCHCODE
	String INITIALISED_BRANCH_VALUE = "DEFBRANCH";
	
	//INITIALISED STATIC VALUE FOR DEPTCODE
	String INITIALISED_DEPT_VALUE = "DEFDEPT";
	
	//Triple-x constant
	String TRIPLE_X = "XXX";
	
	//
	int BANK_BIC_LENGTH = 12;
	
	//
	int INT_NINE = 9;
	int INT_ELEVEN = 11;
	//
	int INT_TWELVE = 12;
	
	//beneficiary Type as F constant
	String BENEFICIARY_TYPE_F = "F";
	
	//ordering Type as F constant
	String ORDERING_TYPE_F = "F";
	
	//ordering Type as I constant
	String ORDERING_TYPE_I = "I";
	
	String BLOCK_BEGINING = "{";
	
	String BASICHEADERBLOCK_BLOCKIDENTIFIER = "1:";
	
	String BASICHEADERBLOCK_APPLICATIONIDENTIFIER = "F";
	
	String BASICHEADERBLOCK_SERVICEIDENTIFIER = "01";
	
	String BASICHEADERBLOCK_SESSION_NUMBER="0000";
	
	String BASICHEADERBLOCK_SEQUENCE_NUMBER="000000";
	
	String BLOCK_CLOSING = "}";
	
	String APPLICATIONHEADERBLOCK_BLOCKIDENTIFIER = "2:";
	
	String APPLICATIONHEADERBLOCK_MSGPRIORITY = "N";
	
	String APPLICATIONHEADERBLOCK_DELIVERYMONITORING = "3";
	
	String APPLICATIONHEADERBLOCK_OBSOLESCENCE = "020";
	
	String USERHEADERBLOCK_BLOCKIDENTIFIER = "3:";
	
	String USERHEADERBLOCK_BANKINGPRIORITY_TAG = "113:";
	
	//Message User Reference TAG
	String USERHEADERBLOCK_MUR_TAG = "108:";
	
	String SEQUENCE_KEY_MUR = "MUR";
	
	String CONSTANT_VALUE_ZERO = "0";
	
	int CONSTANT_VALUE_INT_ONE = 1;
	
	String TRAILERBLOCK_BLOCKIDENTIFIER = "5:";
	
	String TEXTBLOCK_BLOCKIDENTIFIER = "4:";
	
	String NGPH_CHAR_EOL = System.getProperty("line.separator");

	String NGPH_CHAR_EOL_CTRF = System.getProperty("line.separator");
	
	String NGPH_SFMS_CRLF = "\r\n";

	String NGPH_CHAR_EOL_SLSH = "/";

	String NGPH_CHAR_HYPHEN = "-";
	
	String NGPH_MUR_SEQ_DATE_FORMAT = "yyyyMMdd";
	
	String TAG_20 = ":20:";
	
	String NGPH_COLON = ":";
	
	String NGPH_STRING_FOUR = "4";
	
	int NGPH_INT_ZERO = 0;
	
	String NGPH_GET_METHOD_PREFIX = "get";
	
	String NGPH_CONSTANT_HASH = "#";
	
	String MANDATORY_FIELD_VALUE_MISSING = "mandatoryFieldValueMissing";
	
	int NGPH_INT_FIFTY_ONE = 51;
	
	int NGPH_INT_FIFTY = 50;
	
	int NGPH_INT_SIXTY = 60;
	
	String NGPH_CONSTANT_A = "A";
	
	String NGPH_CONSTANT_K = "K";
	
	String NGPH_CONSTANT_SLASH = "/";
	
	String NGPH_CONSTANT_D = "D";
	
	int NGPH_INT_EIGHT = 8;
	
	int NGPH_INT_ELEVEN = 11;
	
	String NGPH_CONSTANT_B = "B";
	
	String NGPH_CONSTANT_C = "C";
	
	int NGPH_INT_THIRTYFIVE = 35;
	
	String NGPH_REGEX_ALPHA = "^[a-z]+[A-Z]";
	
	String NGPH_REGEX_ALPHA_NUMERIC = "\\w+";
	
	int NGPH_INT_SIX = 6;
	
	int NGPH_INT_SEVENTYT = 70;
	
	int NGPH_INT_SEVENTYT_NINE = 79;
	
	String NGPH_CONSTANT_F = "F";
	
	String NGPH_CONSTANT_G = "G";
	
	String NGPH_CONSTANT_ROC = "/ROC/";
	
	int NGPH_INT_THREE = 3;
	
	String NGPH_RTGS_MSG_IDENTIFIER = "F01";
	
	int NGPH_INT_ONE = 1;
	
	int NGPH_INT_TWO = 2;
	
	String NGPH_TRIPLE_ZERO = "000";
	
	String NGPH_RTGS_CONSTANT_SEQ_NUM = "000000000";
	
	String NGPH_RTGS_CONSTANT_FILLER = "XXXXXXXXX";
	
	String NGPH_DOUBLE_ZERO = "00";
	
	String NGPH_RTGS_FIELD_ONE = "2020";
	
	String NGPH_RTGS_FIELD_TWO = "4488";
	
	String NGPH_RTGS_FIELD_THREE = "5500";
	
	String NGPH_RTGS_FIELD_FOUR_ONE = "5517";
	
	String NGPH_RTGS_FIELD_FOUR_TWO = "5516";
	
	String NGPH_RTGS_FIELD_FIVE_ONE= "5518";
	
	String NGPH_RTGS_FIELD_FIVE_TWO= "6717";
	
	String NGPH_RTGS_FIELD_FIVE_THREE= "5521";
	
	String NGPH_RTGS_FIELD_SIXTH_ONE= "6500";
	
	String NGPH_RTGS_FIELD_SIXTH_TWO= "6718";
	
	String NGPH_RTGS_FIELD_SIXTH_THREE= "5526";
	
	String NGPH_RTGS_FIELD_SEVENTH_ONE= "6511";
	
	String NGPH_RTGS_FIELD_SEVENTH_TWO= "5546";
	
	String NGPH_RTGS_FIELD_EIGHTH_ONE= "6516";
	
	String NGPH_RTGS_FIELD_EIGHTH_TWO= "6719";
	
	String NGPH_RTGS_FIELD_EIGHTH_THREE= "5551";
	
	String NGPH_RTGS_FIELD_NINE= "5561";
	
	String NGPH_RTGS_FIELD_TEN= "7023";
	
	String NGPH_RTGS_FIELD_ELEVEN= "7028";
	
	String NGPH_RTGS_FIELD_TWELVE= "7495";
	
	String NGPH_STRING_ONE = "1";
	
	String NGPH_STRING_TWO = "2";
	
	String NGPH_STRING_THREE = "3";
	
	int NGPH_INT_TEN = 10;
	
	BigDecimal BIGDECIMAL_DEFAULT_VAL = new BigDecimal(0.0);
	
	String SETTLEMENT_METHOD_INDA = "INDA";
	
	String SETTLEMENT_METHOD_INGA = "INGA";
	
	String SETTLEMENT_METHOD_COVE = "COVE";
	
	String SETTLEMENT_METHOD_CLRG = "CLRG";
	
	String CHARGE_BEARER_DEBT = "DEBT";
	
	String CHARGE_BEARER_CRED = "CRED";
	
	String CREDITOR_AGENT_CODE_CHQB = "CHQB";

	//Network Field Validation Error Codes
	String NGPH_NFV0001 = "NFV0001";
	String NGPH_NFV0002 = "NFV0002";
	String NGPH_NFV0003 = "NFV0003";
	String NGPH_NFV0004 = "NFV0004";
	String NGPH_NFV0005 = "NFV0005";
	
	
	//Network Validation Error Codes
	String NGPH_NVS0001 = "NVS0001";
	String NGPH_NVS0002 = "NVS0002";
	String NGPH_NVS0003 = "NVS0003";
	String NGPH_NVS0004 = "NVS0004";
	String NGPH_NVS0005 = "NVS0005";
	String NGPH_NVS0006 = "NVS0006";
	String NGPH_NVS0007 = "NVS0007";
	String NGPH_NVS0008 = "NVS0008";
	String NGPH_NVS0009 = "NVS0009";
	String NGPH_NVS0010 = "NVS0010";
	String NGPH_NVS0011 = "NVS0011";
	String NGPH_NVS0012 = "NVS0012";
	String NGPH_NVS0013 = "NVS0013";
	String NGPH_NVS0014 = "NVS0014";
	String NGPH_NVS0015 = "NVS0015";
	String NGPH_NVS0016 = "NVS0016";
	String NGPH_NVS0017 = "NVS0017";
	String NGPH_NVS0018 = "NVS0018";
	String NGPH_NVS0019 = "NVS0019";
	String NGPH_NVS0020 = "NVS0020";
	String NGPH_NVS0021 = "NVS0021";
	String NGPH_NVS0022 = "NVS0022";
	String NGPH_NVS0023 = "NVS0023";
	String NGPH_NVS0024 = "NVS0024";
	String NGPH_NVS0025 = "NVS0025";
	String NGPH_NVS0026 = "NVS0026";
	String NGPH_NVS0027 = "NVS0027";
	String NGPH_NVS0028 = "NVS0028";
	String NGPH_NVS0029 = "NVS0029";
	String NGPH_NVS0030 = "NVS0030";
	String NGPH_NVS0031 = "NVS0031";
	
	//Message Validation Error Codes
	String NGPH_FLD0001 = "FLD0001";
	String NGPH_FLD0002 = "FLD0002";
	String NGPH_FLD0003 = "FLD0003";
	String NGPH_FLD0004 = "FLD0004";
	String NGPH_FLD0005 = "FLD0005";
	String NGPH_FLD0006 = "FLD0006";
	String NGPH_FLD0007 = "FLD0007";
	String NGPH_FLD0008 = "FLD0008";
	String NGPH_FLD0009 = "FLD0009";
	String NGPH_FLD0010 = "FLD0010";
	String NGPH_FLD0011 = "FLD0011";
	String NGPH_FLD0012 = "FLD0012";
	String NGPH_FLD0013 = "FLD0013";
	String NGPH_FLD0014 = "FLD0014";
	String NGPH_FLD0015 = "FLD0015";
	String NGPH_FLD0016 = "FLD0016";
	String NGPH_FLD0017 = "FLD0017";
	String NGPH_FLD0018 = "FLD0018";
	String NGPH_FLD0019 = "FLD0019";
	String NGPH_FLD0020 = "FLD0020";
	String NGPH_FLD0021 = "FLD0021";
	
	String NGPH_FMT0001 = "FMT0001";
	String NGPH_FMT0002 = "FMT0002";
	String NGPH_FMT0003 = "FMT0003";
	
	//Validate service Error codes
	String NGPH_VSE0001 = "VSE0001";
	String NGPH_VSE0002 = "VSE0002";
	String NGPH_VSE0003 = "VSE0003";
	String NGPH_VSE0004 = "VSE0004";
	String NGPH_VSE0005 = "VSE0005";
	String NGPH_VSE0006 = "VSE0006";
	String NGPH_VSE0007 = "VSE0007";
	String NGPH_VSE0008 = "VSE0008";
	String NGPH_VSE0009 = "VSE0009";
	String NGPH_VSE0010 = "VSE0010";
	String NGPH_VSE0011 = "VSE0011";
	String NGPH_VSE0012 = "VSE0012";
	String NGPH_VSE0013 = "VSE0013";
	String NGPH_VSE0014 = "VSE0014";
	String NGPH_VSE0015 = "VSE0015";
	String NGPH_VSE0016 = "VSE0016";
	String NGPH_VSE0017 = "VSE0017";
	String NGPH_VSE0018 = "VSE0018";
	String NGPH_VSE0019 = "VSE0019";
	String NGPH_VSE0020 = "VSE0020";
	String NGPH_VSE0021 = "VSE0021";
	String NGPH_VSE0022 = "VSE0022";
	String NGPH_VSE0023 = "VSE0023";
	String NGPH_VSE0024 = "VSE0024";
	String NGPH_VSE0025 = "VSE0025";
	String NGPH_VSE0026 = "VSE0026";
	String NGPH_VSE0027 = "VSE0027";
	String NGPH_VSE0028 = "VSE0028";
	String NGPH_VSE0029 = "VSE0029";
	String NGPH_VSE0030 = "VSE0030";
	String NGPH_VSE0031 = "VSE0031";
	String NGPH_VSE0032 = "VSE0032";
	String NGPH_VSE0033 = "VSE0033";
	String NGPH_VSE0034 = "VSE0034";
	String NGPH_VSE0035 = "VSE0035";

	
	//Validate service Warning codes
	String NGPH_VSW0001 = "VSW0001";
	String NGPH_VSW0002 = "VSW0002";
	String NGPH_VSW0003 = "VSW0003";
	String NGPH_VSW0004 = "VSW0004";
	String NGPH_VSW0005 = "VSW0005";
	String NGPH_VSW0006 = "VSW0006";
	String NGPH_VSW0007 = "VSW0007";
	String NGPH_VSW0008 = "VSW0008";
	String NGPH_VSW0009 = "VSW0009";
	String NGPH_VSW0010 = "VSW0010";
	String NGPH_VSW0011 = "VSW0011";
	String NGPH_VSW0012 = "VSW0012";
	String NGPH_VSW0013 = "VSW0013";
	String NGPH_VSW0014 = "VSW0014";
	String NGPH_VSW0015 = "VSW0015";
	String NGPH_VSW0016 = "VSW0016";
	
	//Enrich Service Error
	String NGPH_ESE0001 = "ESE0001";
	String NGPH_ESE0002 = "ESE0002";
	String NGPH_ESE0003 = "ESE0003";
	String NGPH_ESE0004 = "ESE0004";
	String NGPH_ESE0005 = "ESE0005";
	String NGPH_ESE0006 = "ESE0006";
	String NGPH_ESE0007 = "ESE0007";
	String NGPH_ESE0008 = "ESE0008";
	String NGPH_ESE0009 = "ESE0009";
	String NGPH_ESE0010 = "ESE0010";
	String NGPH_ESE0011 = "ESE0011";
	String NGPH_ESE0012 = "ESE0012";
	
	//Auto Router Error
	String NGPH_ARE0001 = "ARE0001";
	String NGPH_ARE0002 = "ARE0002";
	String NGPH_ARE0003 = "ARE0003";
	String NGPH_ARE0004 = "ARE0004";
	
	//Entity Control Error
	String NGPH_ECE0001 = "ECE0001";
	String NGPH_ECE0002 = "ECE0002";
	String NGPH_ECE0003 = "ECE0003";
	
	//Iso Adapter Error
	String NGPH_IAE0001 = "IAE0001";
	String NGPH_IAE0002 = "IAE0002";
	String NGPH_IAE0003 = "IAE0003";
	String NGPH_IAE0004 = "IAE0004";
	String NGPH_IAE0005 = "IAE0005";
	String NGPH_IAE0006 = "IAE0006";
	String NGPH_IAE0007 = "IAE0007";
	String NGPH_IAE0008 = "IAE0008";
	
	//SFMS Adapter Error
	String NGPH_SFE0001 = "SFE0001";
	String NGPH_SFE0002 = "SFE0002";
	
	//MQProvider errors
	String NGPH_MQP0001 = "MQP0001";
	
	//InterVention Error
	String NGPH_IVE0001 = "IVE0001";
	
	//IMPS Handler Service Error
	String NGPH_IHS0001 = "IHS0001";
	
	//Account Statuses
	String AC_STATUS_OPEN = "O";
	String AC_STATUS_CLOSED = "C";
	String AC_STATUS_PENDINGCLOSURE = "P";
	String AC_STATUS_DORMANT = "D";
	String AC_STATUS_RESERVED = "R";
	
	//MSG STATUSES
	String MSG_STATUS_AWAITING_REPAIR = "2";
	String MSG_STATUS_AWAITING_ACK = "12";
	//Mark as finalized
	String MSG_STATUS_COMPLETED = "13";
	//Move to Repair, Re-process
	String MSG_STATUS_EXCEPTIONS = "16";
	String MSG_STATUS_REJECTEDBYCHANNEL = "18";
	
	//Rule category for enrich service
	String RULE_CATEGORY_ENRICH = "ENRICH";
	
	String SYMBOL_CARRET = "^";
	
	String CREDITOR_CONSTANT = "Cr";
	
	String DEBITOR_CONSTANT = "Dr";
	
	//INIT_ENTRY value for base currency in Initialisationm table.
	String BASE_CUR_INIT_ENTRY = "BASECURRENCY";
	
	String PYMTSUNDAC_INIT_ENTRY ="PYMTSUNDAC";
	
	String PYMTSUSPAC_INIT_ENTRY="PYMTSUSPAC";
	
	//FOR LOCALBIC
	String LOCALBIC_INIT_ENTRY = "LOCALBIC";
	
	//PAYMENT MESSAGE STATUSES CODES
	String AWAITING_REPAIR = "2";
	//THIS CONSTANTE REPRESENTS THE ONE SPACE
	String NGPH_SPACE = " ";
	
	String NGPH_COMMA = ",";
	
	String NGPH_EMPTY = "";

	//PAYMENT MESSAGE STATUSES CODES
	// OutBound Payment
	String INBOUND_AWAITING_REPAIR = "33";

	//PAYMENT MESSAGE STATUSES CODES
	// OutBound Payment
	String OUTBOUND_AWAITING_REPAIR = "2";
	
	String NGPH_ACCOUNTING_SER_ERR = "AccountingService Failed To Find Account Numbers";

	String IMPS_TXNTYPE_P2P_REQ = "45";
	String IMPS_TXNTYPE_P2M_REQ = "47";
	String IMPS_TXNTYPE_P2A_REQ = "48";
	String IMPS_TXNTYPE_P2P_VER = "32";
	String IMPS_TXNTYPE_P2M_VER = "33";
	String IMPS_TXNTYPE_P2A_VER = "34";
	
	String ACCOUNT_TYPE_CURRENT = "11";
	String ACCOUNT_TYPE_SAVINGS = "10";
	
	String BANK_CODE = "BANKCODE";
}