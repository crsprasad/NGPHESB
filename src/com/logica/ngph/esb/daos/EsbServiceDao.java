package com.logica.ngph.esb.daos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.ErrorCodes;
import com.logica.ngph.common.dtos.GenericFilePojo;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.BGMast;
import com.logica.ngph.esb.Dtos.CustomerInfo;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.Dtos.FieldCanonicalAttribute;
import com.logica.ngph.esb.Dtos.LcMast;
import com.logica.ngph.esb.Dtos.MessageFormats;
import com.logica.ngph.esb.Dtos.ReportDto;
import com.logica.ngph.esb.Dtos.ResponseBean;
import com.logica.ngph.esb.Dtos.Rules;
import com.logica.ngph.esb.Dtos.TcpBean;
import com.logica.ngph.esb.Dtos.UserInfoBean;

/**
 * 
 * @author mohdabdulaa
 *
 */
public interface EsbServiceDao
{
	//public void testInsertSeq(String id ,int seq);
	//public Rules getRules();
	
	public List<String> getServiceConfigDetails(String messageType, String msgSubType, String messageDirection, String srvcId)throws Exception;
	
	//public List<Rules> getRulesForSpecificMessage(String msgType, String msgSubType, String ruleBranch, String ruleDept, String ruleCategory)throws NGPHException;
	public List<Rules> getRulesForSpecificMessage(String msgType, String msgSubType, String ruleBranch, String ruleDept, String ruleCategory, String ruleDirection) throws Exception;
	
	public String findBranchCodeByAccountNumber(String accountNo)throws Exception;
	
	public String findBranchCodeByBic(String branchBic)throws Exception;
	
	public List<Rules> getRulesForSpecificMessageDeptTracing(String ruleMessageType, String ruleBranch, String ruleCategory)throws Exception;
	
	public String getInitialisedValue(String initEntry)throws Exception;
	
	public String findBicByBranchCode(String branchCode)throws Exception;
	
	public String getEventDescription(String eventId) throws Exception;
	
	public void saveEventAudit(EventAudit eventAudit) throws Exception;
	
	public String getSequenceNumber(String seqIdentifier,int totalLen)throws Exception;
	
	public List<MessageFormats> getFieldsOfMessage(String msgType, String msgSubType, String fieldNo, String fieldTag)throws Exception;
	
	public List<String> getApplicableTextBlockFormatFields(String msgType, String msgSubType)throws Exception;
	
	public List<String> getDistinctFieldTags(String msgType, String msgSubType, String fieldNo)throws Exception;
	
	public Map<String, String> getTagValue(String fieldNo, String tagName, String blockNo)throws Exception;
	
	public String getIFSCByBranch(String brnCode)throws Exception;
	
	public String getBICByBranch(String brnCode)throws Exception;
	
	public String getIsoCodeByBranch(String brnCode)throws Exception;
	
	public String getIsoPartyBIC(String isoPtyCode)throws Exception;
	 
	public String getIsoPartyIFSC(String isoPtyCode)throws Exception;
	
	public String getIfscCodeByBic(String bic)throws Exception;
	
	public String getValFmtReq(String hostId)throws Exception;
	
	public String getBICByIFSC (String IFSC)throws Exception;
	
	public String getUstrdValue(String msgRef)throws Exception;
	
	public int getPaymentsCountOfGroupPayment(String groupMsgId)throws Exception;
	
	public void updateMessageStatus(String msgRef, String txnRef, String msgMur, String msgStatus, String msgPrevStatus, Timestamp msgTmStmp)throws Exception;

	public void updateMessageStatusforCBS(String msgRef, String msgStatus, String msgPrevStatus)throws Exception;

	public List<String> getValidateServiceRulesOrder(String msgType, String msgSubType, String hostId)throws Exception;

	public void updatePymentBranchAndDept(String msgRef, String txnRef, String msgMur, String msgBranch, String msgDept)throws Exception;
	
	public void updatePaymentMsgWithFxDetails(NgphCanonical ngphCanonical)throws Exception;
	
	public BigDecimal getConversionPrice(String currencyCode, String indicator)throws Exception;
	
	public void updateMessageStatusAndServiceId(NgphCanonical canonicalData)throws Exception;
	
	public String fetchRemittenceInfoValue(String remitInfoRef)throws Exception;
	
	public void updatePaymentDetails(NgphCanonical canonicalData)throws Exception;
	
	public void updateMessageStatusToRepair(NgphCanonical canonicalData)throws Exception;
	
	public String getAccountNumberByCustomerId(String custId)throws Exception;
	
	public String getAccountNumberByAccountName(String accountName, String baseCcy, String accOwnBranch)throws Exception;
	
	public String getAccountNumberByPartyCode(String partyCode, String ccyType, String accOwnBranch)throws Exception;
	
	 // Used to load field data for canonical Mapping
	public  HashMap<String, ArrayList<FieldCanonicalAttribute>> loadFieldsData()throws Exception;
	
	//Used to Check the Host Category by IMPS Channel Service
	public String getHostCategory(String hostId)throws Exception;

	//Used for STAN by IMPS Channel Service
	public String getbusday_Date(String branchCode)throws Exception;
	
	//Used for STAN by IMPS Channel Service
	public Date getcurrbusday_Date(String identifier)throws Exception;
	
	//Used by Auto Router Service to find DST_MES_TYPE and DST_SUB_MSGTYPE
	public List<String> getDstMsgtype(String srcMsgType, String srcSubMsgType, String dstChnlType, String direction)throws Exception;

	public List<String> getAckMsgtype(String srcMsgType, String srcSubMsgType, String srcChnlType, String direction)throws Exception;
	
	//Used by Auto Router Service to find DST_MES_TYPE and DST_SUB_MSGTYPE
	public List<String> getRtnMsgtype(String srcMsgType, String srcSubMsgType, String dstChnlType, String direction)throws Exception;
	
	//Used by Auto Router Service to find DST_Chnl_TYPE based on Dst Ei Id
	public String getDstChnlType(String dstEiId)throws Exception;
	
	//update Ta_Seq for busy date
	public void updateStan(String bus_date, String stan, String identifier)throws Exception;
	
	//get initialization value for a branch and init Entry
	public String getInitialisedValBranch(String initEntry, String branch)throws Exception;
	
	//used to fetch the customer mobile No based on Customer Acct Num
	public String getMobNo(String acctNum)throws Exception;
	
	String getIsoCurrCode(String curr)throws Exception;
	
	String getIsoPartyCode(String isoPty)throws Exception;
	
	String getDstQueue(String ei_id)throws Exception;
	
	public List<String> getAcDetailsByMMIDAndMobile(String MMID, String mobNo)throws Exception;
	
	public List<String> getAcDetailsByAccountAndMobile(String accountNo, String mobNo, int addrSeq)throws Exception;
	
	public List<String> getAcDetailsByAccountAndBranch(String accountNo, String brnCode, int addrSeq)throws Exception;
	
	public boolean insertAckCanonicalDetails(AcknowledgementCanonical ackCan)throws Exception;
	
	String getAppId(String hostId)throws Exception;
	
	int getEIStatus(String eiCode)throws Exception;
	
	void populateIMPSData(ResponseBean obj, String msgRef, String stan)throws Exception;
	
	void updateIMPSData(ResponseBean obj, String msgRef, String stan)throws Exception;
	
	void populateTCPStatus(String mes, String msgRef, int msgStatus)throws Exception;
	
	List<TcpBean> getTCPMsgs(int msgstatus)throws Exception;
	
	void updateTCPStatus(int status, String mesRef)throws Exception;
	
	String getStan(String identifier)throws Exception;
	
	List<ErrorCodes> getErrorCodes()throws Exception;
	
	int isEventAlertable(String eventId)throws Exception;
	
	String EventAlertFor(String eventID)throws Exception;
	
	String getUserId(String eventID)throws Exception;
	
	List<UserInfoBean> getUserEmailAndMob(String userId)throws Exception;
	
	List<CustomerInfo> getCustomerInfo(String canVal)throws Exception;
	
	boolean validateAccNum(String accountNo)throws Exception;
	
	boolean validateMobNum(String accNo, String mobNo)throws Exception;
	
	String maxMMID(String accNo, String mobNo)throws Exception;
	
	void populateMobNoforMMID(String accNo, String mobNo) throws Exception;
	
	void updateMMID(String mmid, String accNo,String mobNo) throws Exception;
	
	boolean validateMMID(String accNo, String mobNo)throws Exception;
	
	public void updatepollStatus(String msgref, String status)throws Exception;
	
	public void populateLcMast(LcMast obj)throws Exception;
	
	public void populateBGMast(final BGMast obj)throws Exception;
	
	public void updateMessageStatusforAckByMsgRef_ReasonCode(String msgref, String reasonCode, String status)throws Exception;
	
	public int getEIid_MsgConstReqd(String dstEiid)throws Exception;
	
	public void updateLcMast(LcMast obj)throws Exception;
	
	public void updateBgMast(final BGMast obj)throws Exception;
	
	void populateMesMaster_T(final String seqNum, final String mes, final String Reason)throws Exception;
	
	int updateFileData(ArrayList<GenericFilePojo> dataHolder, String lineData, String TableName) throws Exception;
	
	void populateFileData(ArrayList<GenericFilePojo> dataHolder, String lineData,String TableName) throws Exception;

	int checkFileData(ArrayList<GenericFilePojo> dataHolder, String lineData, String TableName) throws Exception;

	public String getHostRepairable(String hostId)throws Exception;
	
	public ResponseBean getIMPSResponseBeans(String msgRef)throws Exception;
	
	public BigDecimal getAvailableCrLimit(String identifier)throws Exception;
	
	public void updateAvailableCrLimit(String identifier, BigDecimal decAmount)throws Exception;
	
	public String getIMPSMapBnkCode(String IMPS_NBIN)throws Exception;
	
	public String getIMPSMapBnkNBIN(String bnkCode)throws Exception;
	
	public String getBankFirstIFSC(String bnkCode)throws Exception;
	
	public void logFileData(String fileName, String tableName, String Status)throws Exception;
	
	public int validateBIC(String bicVal)throws Exception;
	
	public int getCntCurrCode(String curr)throws Exception;
	
	public BigDecimal getCurrDecimal(String curr)throws Exception;

	public int validateMsgRef(String msgRef)throws Exception;
	
	public void populateInfoCan(final InfoCanonical obj)throws Exception;
	
	public void updateInfoMsgsStatusforAckByMsgRef_ReasonCode(String msgref, String reasonCode, String status )throws Exception;
	
	public void updatedInfoMessagestatus(String msgref, String status)throws Exception;
	
	public BigDecimal getCrLimit(String identifier)throws Exception;
	
	//setting reporting data from DB
	public List<String> getPDFReportData(String msgRef)throws Exception;
	
	public String getOriginalLcNo(String refNo)throws Exception; 
	
	//public int getNoofProcIteration(String msgRef)throws Exception;
		
}
