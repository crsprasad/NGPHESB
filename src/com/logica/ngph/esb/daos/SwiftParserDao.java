package com.logica.ngph.esb.daos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.DbPoller;
import com.logica.ngph.esb.Dtos.Raw_Msgs;

public interface SwiftParserDao {
	 
	 String getEiType(String hostId)throws Exception;
	
	 String retrieveEICode(String providerESB)throws Exception;

	 String retrieveBICDetails()throws Exception;
	 
	 void insertRemitInfoDetails(String remitInfoRef,String msgRef,String aValue)throws Exception;
	 
	 void insertRawMessage(final String aHostID, final String msgRef, final String message, final String msgChnl, final String msgDirection)throws Exception;
	 
	 void insertParsedMessage(NgphCanonical canonicalObj)throws Exception;
	 
	// Required for Received Inbound MT 910
	 String validateTa_Msg_Tx(String relRef, String msgChnlType, String msgCurrency , BigDecimal msgAmount, Timestamp msgValueDate)throws Exception;
	 
	 void updateTa_Msg_TxforExcp(String msgRef, String msgStatus)throws Exception;
	 
	 void updateTa_Msg_tx(String msgRef910, String msgRef, String status)throws Exception;
	 
	 boolean validateTa_Accounts(String beneficiaryCustAcct)throws Exception;
	 
	 String getCovGen()throws Exception;
	 
	 String getHostID()throws Exception;
	 
	 String isCurrSuptByAcct(String account)throws Exception;
	 
	 int isPtyCorrspndtByAcct(String account)throws Exception;
	 
	 int isPtyCorrspndtByBIC(String bic)throws Exception;
	 
	 String isCurrSuptByBIC(String bic)throws Exception;
	 
	 int isPtyCorrspndtByNameAdd(String nameAdd)throws Exception;
	 
	 String isCurrSuptByNameAdd(String nameAdd)throws Exception;
	 
	 String getSequenceNumber(String seqIdentifier)throws Exception;
	 
	 String getIsoCurr(String currCode)throws Exception;
	 
	 String getPayStatus(String msg_PMTID_ENDTOENDID)throws Exception;
	 
	 public String getHostFormat(String hostId)throws Exception;
	 
	 void updateEiStatus(String eiCode, int istatus)throws Exception; 
	
	public int validateAckNowledgement(String mestype, String SubMesType, String MsgDirection)throws Exception;

	public int validateAutoAckNowledgement(String hostId)throws Exception;

	public int msgIsReturn(String mestype, String SubMesType, String MsgDirection)throws Exception;
	
	public int getIMPSMsgRefCount(String stan)throws Exception;
	
	public String getIMPSMsgRef(String stan)throws Exception;

	public NgphCanonical getCanonicalFromMessagesTx(String msgMur)throws Exception;
	
	public NgphCanonical getCanonicalFromMessagesTxforMsgRef(String msgref)throws Exception; 
	
	public NgphCanonical getCanonicalFromMessagesTxforTxnRef(String txnRef)throws Exception;
	
	public NgphCanonical getCanonicalFromMessagesTxForSeq(String seqNo)throws Exception;
	
	void updateMsgStatusForRaw_Msgs(int msgStatus, String msgRef, String errorCode)throws Exception;

	public List<DbPoller> fetchPolledMsgs(String pollStatus)throws Exception;
	
	int getmsgCount(String txnref, String msgDirection, String senderBank)throws Exception;
	
	public String compose_dupVals(NgphCanonical canonicalData)throws Exception;
	
	public int IsDuplicateByData (String dupFieldsData)throws Exception;
	
	public List<Raw_Msgs> getRaw_msgs(String msgRef)throws Exception;
	 
	public InfoCanonical getInfoCanonicalFromTaMsgInformation(String msgMur)throws Exception;
	
	public InfoCanonical getInfoCanonicalFromTaMsgInformationForSeq(String seqNo) throws Exception;
	
	public String getSRCMsgType(String txnref, String msgDirection) throws Exception;
	
	public InfoCanonical getInfoCanonicalFromMessageTxforMsgRef(String msgref)throws Exception; 
	
	public String getNoofProcIteration()throws Exception;
	
}
