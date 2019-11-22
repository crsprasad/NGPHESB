package com.logica.ngph.esb.daosImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.ReportRPTDao;

public class ReportRPTDaoImpl implements ReportRPTDao 
{
	private JdbcTemplate jdbcTemplate;
	static Logger logger = Logger.getLogger(ReportRPTDaoImpl.class);
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) 
	{
		this.jdbcTemplate = jdbcTemplate;
	}
	public String validateData(String msgref)throws Exception
	{
		 String result=null;
		 String Query="select MSGS_MSGREF from TA_MSGS_RPT where MSGS_MSGREF ='" + msgref +"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
			 //System.out.println("Value is ::" + result);
		 }
		 catch(EmptyResultDataAccessException e)
		 {
			 return null;
		 }
		 catch (Exception e) 
		 {
			 logger.error(e, e);
			 throw new Exception(e);
		 }
		 return result;
	}

	public void insertRPTParsedMessage(NgphCanonical canonicalObj,String action,String msgRef)throws Exception
	{
		logger.info("ReportRptDOA Starts");
		logger.info("ReportRptDOA Starts :: Action is "+action);
		String MSGS_MSGREF = "";
		String MSGS_INTRBKSTTLMCCY = "";
		BigDecimal MSGS_INTRBKSTTLMAMT = new BigDecimal(0.00);
		String MSGS_SRC_MSGTYPE = "";
		String MSGS_SRC_MSGSUBTYPE = "";
		String MSGS_DIRECTION = "";
		String MSGS_DEPT = "";
		String MSGS_BRANCH = "";
		String MSGS_MSGSTS = "";
		String MSGS_CHANNELID = "";
		String MSGS_HOSTID = "";
		String MSGS_PMTID_RELREF = "";
		String MSGS_INSTDAGT_BKCD = "";
		Timestamp MSGS_INTRBKSTTLMDT = new Timestamp(Calendar.getInstance().getTimeInMillis());
		String MSGS_DBTR_CTRYOFRES = "";
		String MSGS_DBTRAGTACCT = "";
		String MSGS_INSTGAGT_BKCD = "";
		String MSGS_CDTR_CTRYOFRES = "";
		String MSGS_INSTRFORCDTRAGT_INSTRINF = "";
		String MSGS_INSTRFORNXTAGT_INSTRINF = "";
		String MSGS_ULTMTCDTR_NM = "";
		String MSG_MUR = "";
		String MSGS_PRVSINSTGAGT_ACCT = "";
		String MSGS_PRVSINSTGAGT_BKCD = "";
		String MSGS_PMTID_INSTRID = "";
		String MSGS_DST_MSGTYPE = "";
		String MSGS_DST_MSGSUBTYPE = "";
		String MSGS_DST_CHANNELID = "";
		String MSGS_DST_EIID = "";
		String MSGS_DBTR_NM = "";
		String MSGS_DBTR_PSTLADDR = "";
		String MSGS_DBTR_ID = "";
		String MSGS_DBTRACCT = "";
		String MSGS_DBTRAGT = "";
		String MSGS_CDTRACCT = "";
		String MSGS_CDTR_NM = "";
		String MSGS_CDTR_PSTLADDR = "";
		String MSGS_CDTR_ID = "";
		String MSGS_ULTMTCDTR_ID = "";
		String MSGS_ULTMTCDTR_PSTLADDR = "";
		String MSGS_INSTRFORNXTAGT_CD = "";
		Timestamp MSGS_RECVDTIME = new Timestamp(Calendar.getInstance().getTimeInMillis());
		String MSGS_CDTRAGT = "";
		String MSGS_ERROR_CODE = "";
		String MSGS_TXNTYPE = "";
		String BG_BENIFICIARY_NAME = "";
		String BG_APPLICENT_NAME ="";
		
		//Mizuho :: Start :: Added new report fields
		Date LC_ISSUE_DT = new Date();
		String MSGS_SENDING_INSTCODE = "";
		String MSGS_SENDING_INSTLOC = "";
		Date LC_EXP_DATE = new Date();
		Date LC_LAST_SHIPDT = new Date();
		Date LC_AMNDMNT_DATE = new Date();
		int LC_AMNDMNT_NO = 0;
		String LC_ADDITIONAL_CURR_CODE = "";
		BigDecimal LC_ADDITIONAL_AMT = new BigDecimal(0.00);
		String MSGS_INSTDCCY = "";
		BigDecimal LC_ADDNLAMT_CLAIMED = new BigDecimal(0.00);
		
		//Mizuho :: End :: Added new report fields
		
		
		int i = 0;
		Object[] valuesArray =	new Object[200];
		StringBuilder updQuery = new StringBuilder();
		updQuery.append("UPDATE TA_MSGS_RPT SET");
		updQuery.append(NgphEsbConstants.NGPH_SPACE);

		if (canonicalObj.getMsgRef() != null) 
		{
			MSGS_MSGREF = canonicalObj.getMsgRef();
		}
		if (canonicalObj.getMsgCurrency() != null) 
		{
			MSGS_INTRBKSTTLMCCY = canonicalObj.getMsgCurrency();
			updQuery.append("MSGS_INTRBKSTTLMCCY = ?,");
			valuesArray[i] = MSGS_INTRBKSTTLMCCY;
			i++;
		}
		if (canonicalObj.getMsgAmount() != null) 
		{
			MSGS_INTRBKSTTLMAMT = canonicalObj.getMsgAmount();
			updQuery.append("MSGS_INTRBKSTTLMAMT = ?,");
			valuesArray[i] = MSGS_INTRBKSTTLMAMT;
			i++;
		}
		if (canonicalObj.getSrcMsgType() != null) 
		{
			MSGS_SRC_MSGTYPE = canonicalObj.getSrcMsgType();
			updQuery.append("MSGS_SRC_MSGTYPE = ?,");
			valuesArray[i] = MSGS_SRC_MSGTYPE;
			i++;
		}
		if (canonicalObj.getSrcMsgSubType() != null) 
		{
			MSGS_SRC_MSGSUBTYPE = canonicalObj.getSrcMsgSubType();
			updQuery.append("MSGS_SRC_MSGSUBTYPE = ?,");
			valuesArray[i] = MSGS_SRC_MSGSUBTYPE;
			i++;
		}
		if (canonicalObj.getMsgDirection() != null) 
		{
			MSGS_DIRECTION = canonicalObj.getMsgDirection();
			updQuery.append("MSGS_DIRECTION = ?,");
			valuesArray[i] = MSGS_DIRECTION;
			i++;
		}
		if (canonicalObj.getMsgDept() != null) 
		{
			MSGS_DEPT = canonicalObj.getMsgDept();
			updQuery.append("MSGS_DEPT = ?,");
			valuesArray[i] = MSGS_DEPT;
			i++;
		}
		if (canonicalObj.getMsgBranch() != null) 
		{
			MSGS_BRANCH = canonicalObj.getMsgBranch();
			updQuery.append("MSGS_BRANCH = ?,");
			valuesArray[i] = MSGS_BRANCH;
			i++;
		}
		if (canonicalObj.getMsgStatus() != null) 
		{
			MSGS_MSGSTS = canonicalObj.getMsgStatus();
			updQuery.append("MSGS_MSGSTS = ?,");
			valuesArray[i] = MSGS_MSGSTS;
			i++;
		}
		if (canonicalObj.getMsgChnlType() != null) 
		{
			MSGS_CHANNELID = canonicalObj.getMsgChnlType();
			updQuery.append("MSGS_CHANNELID = ?,");
			valuesArray[i] = MSGS_CHANNELID;
			i++;
		}
		if (canonicalObj.getMsgHost() != null) 
		{
			MSGS_HOSTID = canonicalObj.getMsgHost();
			updQuery.append("MSGS_HOSTID = ?,");
			valuesArray[i] = MSGS_HOSTID;
			i++;
		}
		if (canonicalObj.getRelReference() != null) 
		{
			MSGS_PMTID_RELREF = canonicalObj.getRelReference();
			updQuery.append("MSGS_PMTID_RELREF = ?,");
			valuesArray[i] = MSGS_PMTID_RELREF;
			i++;
		}
		if (canonicalObj.getReceiverBank() != null) 
		{
			MSGS_INSTDAGT_BKCD = canonicalObj.getReceiverBank();
			updQuery.append("MSGS_INSTDAGT_BKCD = ?,");
			valuesArray[i] = MSGS_INSTDAGT_BKCD;
			i++;
		}
		if (canonicalObj.getMsgValueDate() != null) 
		{
			MSGS_INTRBKSTTLMDT = canonicalObj.getMsgValueDate();
			updQuery.append("MSGS_INTRBKSTTLMDT = ?,");
			valuesArray[i] = MSGS_INTRBKSTTLMDT;
			i++;
		}
		if (canonicalObj.getOrderingCustomerCtry() != null) 
		{
			MSGS_DBTR_CTRYOFRES = canonicalObj.getOrderingCustomerCtry();
			updQuery.append("MSGS_DBTR_CTRYOFRES = ?,");
			valuesArray[i] = MSGS_DBTR_CTRYOFRES;
			i++;
		}
		if (canonicalObj.getOrderingInstitutionAcct() != null) 
		{
			MSGS_DBTRAGTACCT = canonicalObj.getOrderingInstitutionAcct();
			updQuery.append("MSGS_DBTRAGTACCT = ?,");
			valuesArray[i] = MSGS_DBTRAGTACCT;
			i++;
		}
		if (canonicalObj.getSenderBank() != null) 
		{
			MSGS_INSTGAGT_BKCD = canonicalObj.getSenderBank();
			updQuery.append("MSGS_INSTGAGT_BKCD = ?,");
			valuesArray[i] = MSGS_INSTGAGT_BKCD;
			i++;
		}
		if (canonicalObj.getBeneficiaryCustomerCtry() != null) 
		{
			MSGS_CDTR_CTRYOFRES = canonicalObj.getBeneficiaryCustomerCtry();
			updQuery.append("MSGS_CDTR_CTRYOFRES = ?,");
			valuesArray[i] = MSGS_CDTR_CTRYOFRES;
			i++;
		}
		if (canonicalObj.getInstructionsForCrdtrAgtText() != null) 
		{
			MSGS_INSTRFORCDTRAGT_INSTRINF = canonicalObj.getInstructionsForCrdtrAgtText();
			updQuery.append("MSGS_INSTRFORCDTRAGT_INSTRINF = ?,");
			valuesArray[i] = MSGS_INSTRFORCDTRAGT_INSTRINF;
			i++;
		}
		if (canonicalObj.getInstructionsForNextAgtText() != null) 
		{
			MSGS_INSTRFORNXTAGT_INSTRINF = canonicalObj.getInstructionsForNextAgtText();
			updQuery.append("MSGS_INSTRFORNXTAGT_INSTRINF = ?,");
			valuesArray[i] = MSGS_INSTRFORNXTAGT_INSTRINF;
			i++;
		}
		if (canonicalObj.getUltimateCreditorName() != null) 
		{
			MSGS_ULTMTCDTR_NM = canonicalObj.getUltimateCreditorName();
			updQuery.append("MSGS_ULTMTCDTR_NM = ?,");
			valuesArray[i] = MSGS_ULTMTCDTR_NM;
			i++;
		}
		if (canonicalObj.getMsgMur() != null) 
		{
			MSG_MUR = canonicalObj.getMsgMur();
			updQuery.append("MSG_MUR = ?,");
			valuesArray[i] = MSG_MUR;
			i++;
		}
		if (canonicalObj.getPrevInstructingAgentAcct() != null) 
		{
			MSGS_PRVSINSTGAGT_ACCT = canonicalObj.getPrevInstructingAgentAcct();
			updQuery.append("MSGS_PRVSINSTGAGT_ACCT = ?,");
			valuesArray[i] = MSGS_PRVSINSTGAGT_ACCT;
			i++;
		}
		if (canonicalObj.getPrevInstructingBank() != null) 
		{
			MSGS_PRVSINSTGAGT_BKCD = canonicalObj.getPrevInstructingBank();
			updQuery.append("MSGS_PRVSINSTGAGT_BKCD = ?,");
			valuesArray[i] = MSGS_PRVSINSTGAGT_BKCD;
			i++;
		}
		if (canonicalObj.getTxnReference() != null) 
		{
			MSGS_PMTID_INSTRID = canonicalObj.getTxnReference();
			updQuery.append("MSGS_PMTID_INSTRID = ?,");
			valuesArray[i] = MSGS_PMTID_INSTRID;
			i++;
		}
		if (canonicalObj.getDstMsgType() != null)
		{
			MSGS_DST_MSGTYPE = canonicalObj.getDstMsgType();
			updQuery.append("MSGS_DST_MSGTYPE = ?,");
			valuesArray[i] = MSGS_DST_MSGTYPE;
			i++;
		}
		if (canonicalObj.getDstMsgSubType() != null)
		{
			MSGS_DST_MSGSUBTYPE = canonicalObj.getDstMsgSubType();
			updQuery.append("MSGS_DST_MSGSUBTYPE = ?,");
			valuesArray[i] = MSGS_DST_MSGSUBTYPE;
			i++;
		}
		if (canonicalObj.getDstMsgChnlType() != null)
		{
			MSGS_DST_CHANNELID = canonicalObj.getDstMsgChnlType();
			updQuery.append("MSGS_DST_CHANNELID = ?,");
			valuesArray[i] = MSGS_DST_CHANNELID;
			i++;
		}
		if (canonicalObj.getDstEiId() != null)
		{
			MSGS_DST_EIID = canonicalObj.getDstEiId();
			updQuery.append("MSGS_DST_EIID = ?,");
			valuesArray[i] = MSGS_DST_EIID;
			i++;
		}
		if (canonicalObj.getOrderingCustomerName() != null || canonicalObj.getBgApplicentName()!=null)
		{
			if(canonicalObj.getOrderingCustomerName()!=null)
			{
				MSGS_DBTR_NM=canonicalObj.getOrderingCustomerName();
			}
			else
			{
				MSGS_DBTR_NM = canonicalObj.getBgApplicentName();
			}
			updQuery.append("MSGS_DBTR_NM = ?,");
			valuesArray[i] = MSGS_DBTR_NM;
			i++;
		}
		if (canonicalObj.getOrderingCustomerAddress() != null)
		{
			MSGS_DBTR_PSTLADDR = canonicalObj.getOrderingCustomerAddress();
			updQuery.append("MSGS_DBTR_PSTLADDR = ?,");
			valuesArray[i] = MSGS_DBTR_PSTLADDR;
			i++;
		}
		if (canonicalObj.getOrderingCustomerId() != null)
		{
			MSGS_DBTR_ID = canonicalObj.getOrderingCustomerId();
			updQuery.append("MSGS_DBTR_ID = ?,");
			valuesArray[i] = MSGS_DBTR_ID;
			i++;
		}
		if(canonicalObj.getOrderingCustAccount()!=null)
		{
			MSGS_DBTRACCT=canonicalObj.getOrderingCustAccount();
			updQuery.append("MSGS_DBTRACCT = ?,");
			valuesArray[i] = MSGS_DBTRACCT;
			i++;
		}
		if (canonicalObj.getOrderingInstitution() != null)
		{
			MSGS_DBTRAGT = canonicalObj.getOrderingInstitution();
			updQuery.append("MSGS_DBTRAGT = ?,");
			valuesArray[i] = MSGS_DBTRAGT;
			i++;
		}
		if(canonicalObj.getBeneficiaryCustAcct()!=null)
		{
			MSGS_CDTRACCT = canonicalObj.getBeneficiaryCustAcct();
			updQuery.append("MSGS_CDTRACCT = ?,");
			valuesArray[i] = MSGS_CDTRACCT;
			i++;
		}
		if(canonicalObj.getBeneficiaryCustomerName()!=null || canonicalObj.getBgBenificiaryName()!= null)
		{
			if(canonicalObj.getBeneficiaryCustomerName()!=null)
			{
				MSGS_CDTR_NM=canonicalObj.getBeneficiaryCustomerName();
			}
			else
			{
				MSGS_CDTR_NM = canonicalObj.getBgBenificiaryName();
			}
			updQuery.append("MSGS_CDTR_NM = ?,");
			valuesArray[i] = MSGS_CDTR_NM;
			i++;
		}
		if(canonicalObj.getBeneficiaryCustomerAddress()!=null)
		{
			MSGS_CDTR_PSTLADDR=canonicalObj.getBeneficiaryCustomerAddress();
			updQuery.append("MSGS_CDTR_PSTLADDR = ?,");
			valuesArray[i] = MSGS_CDTR_PSTLADDR;
			i++;
		}
		if(canonicalObj.getBeneficiaryCustomerID()!=null)
		{
			MSGS_CDTR_ID = canonicalObj.getBeneficiaryCustomerID();
			updQuery.append("MSGS_CDTR_ID = ?,");
			valuesArray[i] = MSGS_CDTR_ID;
			i++;
		}
		if(canonicalObj.getUltimateCreditorID()!=null)
		{
			MSGS_ULTMTCDTR_ID=canonicalObj.getUltimateCreditorID();
			updQuery.append("MSGS_ULTMTCDTR_ID = ?,");
			valuesArray[i] = MSGS_ULTMTCDTR_ID;
			i++;
		}
		if(canonicalObj.getUltimateCreditorAddress()!=null)
		{
			MSGS_ULTMTCDTR_PSTLADDR=canonicalObj.getUltimateCreditorAddress();
			updQuery.append("MSGS_ULTMTCDTR_PSTLADDR = ?,");
			valuesArray[i] = MSGS_ULTMTCDTR_PSTLADDR;
			i++;
		}
		if (canonicalObj.getInstructionsForNextAgtCode() != null)
		{
			MSGS_INSTRFORNXTAGT_CD = canonicalObj.getInstructionsForNextAgtCode();
			updQuery.append("MSGS_INSTRFORNXTAGT_CD = ?,");
			valuesArray[i] = MSGS_INSTRFORNXTAGT_CD;
			i++;
		}
		if (canonicalObj.getReceivedTime() != null)
		{
			MSGS_RECVDTIME = canonicalObj.getReceivedTime();
			updQuery.append("MSGS_RECVDTIME = ?,");
			valuesArray[i] = MSGS_RECVDTIME;
			i++;
		}
		if (canonicalObj.getAccountWithInstitution() != null)
		{
			MSGS_CDTRAGT = canonicalObj.getAccountWithInstitution();
			updQuery.append("MSGS_CDTRAGT = ?,");
			valuesArray[i] = MSGS_CDTRAGT;
			i++;
		}
		if (canonicalObj.getMsgErrorCode()!= null)
		{
			MSGS_ERROR_CODE = canonicalObj.getMsgErrorCode();
			updQuery.append("MSGS_ERROR_CODE = ?,");
			valuesArray[i] = MSGS_ERROR_CODE;
			i++;
		}
		if (canonicalObj.getMsgTxnType()!= null)
		{
			MSGS_TXNTYPE = canonicalObj.getMsgTxnType();
			updQuery.append("MSGS_TXNTYPE = ?,");
			valuesArray[i] = MSGS_TXNTYPE;
			i++;
		}
		//Mizuho :: Start:: Added new report fields
		
		if (canonicalObj.getLcIssueDt()!= null)
		{
			LC_ISSUE_DT = new java.sql.Date(canonicalObj.getLcIssueDt().getTime());
			updQuery.append("LC_ISSUE_DT = ?,");
			valuesArray[i] = LC_ISSUE_DT;
			i++;
		}
		if (canonicalObj.getSendingInst()!= null)
		{
			MSGS_SENDING_INSTCODE = canonicalObj.getSendingInst();
			updQuery.append("MSGS_SENDING_INSTCODE = ?,");
			valuesArray[i] = MSGS_SENDING_INSTCODE;
			i++;
		}
		if (canonicalObj.getSendingInstLoc()!= null)
		{
			MSGS_SENDING_INSTLOC = canonicalObj.getSendingInstLoc();
			updQuery.append("MSGS_SENDING_INSTLOC = ?,");
			valuesArray[i] = MSGS_SENDING_INSTLOC;
			i++;
		}
		if (canonicalObj.getLcExpDt()!= null)
		{
			LC_EXP_DATE = new java.sql.Date(canonicalObj.getLcExpDt().getTime());
			updQuery.append("LC_EXP_DATE = ?,");
			valuesArray[i] = LC_EXP_DATE;
			i++;
		}
		if (canonicalObj.getLcLstShipDt()!= null)
		{
			LC_LAST_SHIPDT = new java.sql.Date(canonicalObj.getLcLstShipDt().getTime());
			updQuery.append("LC_LAST_SHIPDT = ?,");
			valuesArray[i] = LC_LAST_SHIPDT;
			i++;
		}
		if (canonicalObj.getLcAmndmntDt()!= null)
		{
			LC_AMNDMNT_DATE = new java.sql.Date(canonicalObj.getLcAmndmntDt().getTime());
			updQuery.append("LC_AMNDMNT_DATE = ?,");
			valuesArray[i] = LC_AMNDMNT_DATE;
			i++;
		}
		if (canonicalObj.getLcAmndmntNo() > 0)
		{
			LC_AMNDMNT_NO = canonicalObj.getLcAmndmntNo();
			updQuery.append("LC_AMNDMNT_NO = ?,");
			valuesArray[i] = LC_AMNDMNT_NO;
			i++;
		}
		if (canonicalObj.getLcAdditionalCurrency()!= null)
		{
			LC_ADDITIONAL_CURR_CODE = canonicalObj.getLcAdditionalCurrency();
			updQuery.append("LC_ADDITIONAL_CURR_CODE = ?,");
			valuesArray[i] = LC_ADDITIONAL_CURR_CODE;
			i++;
		}
		if (canonicalObj.getLcAdditionalAmt()!= null)
		{
			LC_ADDITIONAL_AMT = canonicalObj.getLcAdditionalAmt();
			updQuery.append("LC_ADDITIONAL_AMT = ?,");
			valuesArray[i] = LC_ADDITIONAL_AMT;
			i++;
		}
		if (canonicalObj.getInstructedCurrency()!= null)
		{
			MSGS_INSTDCCY = canonicalObj.getInstructedCurrency();
			updQuery.append("MSGS_INSTDCCY = ?,");
			valuesArray[i] = MSGS_INSTDCCY;
			i++;
		}
		if (canonicalObj.getLcTotalAmtClaimed()!= null)
		{
			LC_ADDNLAMT_CLAIMED = canonicalObj.getLcTotalAmtClaimed();
			updQuery.append("LC_ADDNLAMT_CLAIMED = ?,");
			valuesArray[i] = LC_ADDNLAMT_CLAIMED;
			i++;
		}
		//Mizuho :: End:: Added new report fields
		
		try 
		{
			if(action.equals("insert"))
			{
				String query = " insert into TA_MSGS_RPT(MSGS_MSGREF," +
					"MSGS_INTRBKSTTLMCCY, " +
					"MSGS_INTRBKSTTLMAMT, " +
					"MSGS_SRC_MSGTYPE,"+
					"MSGS_SRC_MSGSUBTYPE," +
					"MSGS_DIRECTION, " +
					"MSGS_DEPT, " +
					"MSGS_BRANCH, " +
					"MSGS_MSGSTS," +
					"MSGS_CHANNELID," +
					"MSGS_HOSTID," +
					"MSGS_PMTID_RELREF," +
					"MSGS_INSTDAGT_BKCD," +
					"MSGS_INTRBKSTTLMDT," +
					"MSGS_DBTR_CTRYOFRES,"+
					"MSGS_DBTRAGTACCT," +
					"MSGS_INSTGAGT_BKCD," +
					"MSGS_CDTR_CTRYOFRES," +
					"MSGS_INSTRFORCDTRAGT_INSTRINF," +
					"MSGS_INSTRFORNXTAGT_INSTRINF," +
					"MSGS_ULTMTCDTR_NM," +
					"MSG_MUR," +
					"MSGS_PRVSINSTGAGT_ACCT," +
					"MSGS_PRVSINSTGAGT_BKCD," +
					"MSGS_PMTID_INSTRID," +
					"MSGS_DST_MSGTYPE," +
					"MSGS_DST_MSGSUBTYPE," +
					"MSGS_DST_CHANNELID," +
					"MSGS_DST_EIID," +
					"MSGS_DBTR_NM," +
					"MSGS_DBTR_PSTLADDR," +
					"MSGS_DBTR_ID," +
					"MSGS_DBTRACCT," +
					"MSGS_DBTRAGT," +
					"MSGS_CDTRACCT," +
					"MSGS_CDTR_NM," +
					"MSGS_CDTR_PSTLADDR," +
					"MSGS_CDTR_ID, " +
					"MSGS_ULTMTCDTR_ID, " +
					"MSGS_ULTMTCDTR_PSTLADDR, " +
					"MSGS_INSTRFORNXTAGT_CD, " +
					"MSGS_RECVDTIME," +
					"MSGS_CDTRAGT,"+ 
					"MSGS_ERROR_CODE," +
					"MSGS_TXNTYPE, " +
					//Mizuho :: Start :: Added for report fields
					"LC_ISSUE_DT, " +
					"MSGS_SENDING_INSTCODE, " +
					"MSGS_SENDING_INSTLOC, " +
					"LC_EXP_DATE, " +
					"LC_LAST_SHIPDT, " +
					"LC_AMNDMNT_DATE, " +
					"LC_AMNDMNT_NO, " +
					"LC_ADDITIONAL_CURR_CODE, " +
					"LC_ADDITIONAL_AMT, " +
					"MSGS_INSTDCCY, " +
					"LC_ADDNLAMT_CLAIMED)" +
					//Mizuho :: End :: Added for report fields
					" values (" +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?," +
							"?,?,?,?,?," +
							"?,?,?,?,?," +
							"?,?,?,?,?,?)";
				jdbcTemplate.update(query, new Object[] { MSGS_MSGREF, 
						MSGS_INTRBKSTTLMCCY, 
						MSGS_INTRBKSTTLMAMT, 
						MSGS_SRC_MSGTYPE, 
						MSGS_SRC_MSGSUBTYPE, 
						MSGS_DIRECTION, 
						MSGS_DEPT, 
						MSGS_BRANCH, 
						MSGS_MSGSTS, 
						MSGS_CHANNELID, 
						MSGS_HOSTID, 
						MSGS_PMTID_RELREF, 
						MSGS_INSTDAGT_BKCD, 
						MSGS_INTRBKSTTLMDT, 
						MSGS_DBTR_CTRYOFRES, 
						MSGS_DBTRAGTACCT, 
						MSGS_INSTGAGT_BKCD, 
						MSGS_CDTR_CTRYOFRES, 
						MSGS_INSTRFORCDTRAGT_INSTRINF, 
						MSGS_INSTRFORNXTAGT_INSTRINF, 
						MSGS_ULTMTCDTR_NM, 
						MSG_MUR, 
						MSGS_PRVSINSTGAGT_ACCT, 
						MSGS_PRVSINSTGAGT_BKCD, 
						MSGS_PMTID_INSTRID, 
						MSGS_DST_MSGTYPE, 
						MSGS_DST_MSGSUBTYPE, 
						MSGS_DST_CHANNELID, 
						MSGS_DST_EIID, 
						MSGS_DBTR_NM, 
						MSGS_DBTR_PSTLADDR, 
						MSGS_DBTR_ID, 
						MSGS_DBTRACCT, 
						MSGS_DBTRAGT, 
						MSGS_CDTRACCT, 
						MSGS_CDTR_NM, 
						MSGS_CDTR_PSTLADDR, 
						MSGS_CDTR_ID, 
						MSGS_ULTMTCDTR_ID, 
						MSGS_ULTMTCDTR_PSTLADDR, 
						MSGS_INSTRFORNXTAGT_CD, 
						MSGS_RECVDTIME, 
						MSGS_CDTRAGT,
						MSGS_ERROR_CODE,
						MSGS_TXNTYPE,
						LC_ISSUE_DT,
						MSGS_SENDING_INSTCODE,
						MSGS_SENDING_INSTLOC,
						LC_EXP_DATE,
						LC_LAST_SHIPDT,
						LC_AMNDMNT_DATE,
						LC_AMNDMNT_NO,
						LC_ADDITIONAL_CURR_CODE,
						LC_ADDITIONAL_AMT,
						MSGS_INSTDCCY,
						LC_ADDNLAMT_CLAIMED});// Query Closed
			}
			else if(action.equals("update"))
			{
				String updateQuery="";
				/*String updateQuery = "update TA_MSGS_RPT set MSGS_HOSTID = '"+ MSGS_HOSTID + 
				"', MSGS_INTRBKSTTLMCCY	= '" +	MSGS_INTRBKSTTLMCCY	+
				"', MSGS_INTRBKSTTLMAMT	= " +	MSGS_INTRBKSTTLMAMT	+
				", MSGS_SRC_MSGTYPE	= '" +	MSGS_SRC_MSGTYPE +
				"', MSGS_SRC_MSGSUBTYPE	= '" +	MSGS_SRC_MSGSUBTYPE	+
				"', MSGS_DIRECTION	= '" +	MSGS_DIRECTION	+
				"', MSGS_DEPT	= '" +	MSGS_DEPT	+
				"', MSGS_BRANCH	= '" +	MSGS_BRANCH	+
				"', MSGS_MSGSTS	= '" +	MSGS_MSGSTS	+
				"', MSGS_CHANNELID	= '" +	MSGS_CHANNELID	+
				"', MSGS_PMTID_RELREF	= '" +	MSGS_PMTID_RELREF	+
				"', MSGS_INSTDAGT_BKCD	= '" +	MSGS_INSTDAGT_BKCD	+
				"', MSGS_INTRBKSTTLMDT	= " +	MSGS_INTRBKSTTLMDT	+
				", MSGS_DBTR_CTRYOFRES	= '" +	MSGS_DBTR_CTRYOFRES	+
				"', MSGS_DBTRAGTACCT	= '" +	MSGS_DBTRAGTACCT	+
				"', MSGS_INSTGAGT_BKCD	= '" +	MSGS_INSTGAGT_BKCD	+
				"', MSGS_CDTR_CTRYOFRES	= '" +	MSGS_CDTR_CTRYOFRES	+
				"', MSGS_INSTRFORCDTRAGT_INSTRINF	= '" +	MSGS_INSTRFORCDTRAGT_INSTRINF	+
				"', MSGS_INSTRFORNXTAGT_INSTRINF	= '" +	MSGS_INSTRFORNXTAGT_INSTRINF	+
				"', MSGS_ULTMTCDTR_NM	= '" +	MSGS_ULTMTCDTR_NM	+
				"', MSG_MUR	= '" +	MSG_MUR	+
				"', MSGS_PRVSINSTGAGT_ACCT	= '" +	MSGS_PRVSINSTGAGT_ACCT	+
				"', MSGS_PRVSINSTGAGT_BKCD	= '" +	MSGS_PRVSINSTGAGT_BKCD	+
				"', MSGS_PMTID_INSTRID	= '" +	MSGS_PMTID_INSTRID	+
				"', MSGS_DST_MSGTYPE	= '" +	MSGS_DST_MSGTYPE	+
				"', MSGS_DST_MSGSUBTYPE	= '" +	MSGS_DST_MSGSUBTYPE	+
				"', MSGS_DST_CHANNELID	= '" +	MSGS_DST_CHANNELID	+
				"', MSGS_DST_EIID	= '" +	MSGS_DST_EIID	+
				"', MSGS_DBTR_NM	= '" +	MSGS_DBTR_NM	+
				"', MSGS_DBTR_PSTLADDR	= '" +	MSGS_DBTR_PSTLADDR	+
				"', MSGS_DBTR_ID	= '" +	MSGS_DBTR_ID	+
				"', MSGS_DBTRACCT	= '" +	MSGS_DBTRACCT	+
				"', MSGS_DBTRAGT	= '" +	MSGS_DBTRAGT	+
				"', MSGS_CDTRACCT	= '" +	MSGS_CDTRACCT	+
				"', MSGS_CDTR_NM	= '" +	MSGS_CDTR_NM	+
				"', MSGS_CDTR_PSTLADDR	= '" +	MSGS_CDTR_PSTLADDR	+
				"', MSGS_CDTR_ID	= '" +	MSGS_CDTR_ID	+
				"', MSGS_ULTMTCDTR_ID	= '" +	MSGS_ULTMTCDTR_ID	+
				"', MSGS_ULTMTCDTR_PSTLADDR	= '" +	MSGS_ULTMTCDTR_PSTLADDR	+
				"', MSGS_INSTRFORNXTAGT_CD	= '" +	MSGS_INSTRFORNXTAGT_CD	+
				"', MSGS_RECVDTIME	= " +	MSGS_RECVDTIME	+
				", MSGS_CDTRAGT	= '" +	MSGS_CDTRAGT	+
				
				"' WHERE MSGS_MSGREF='" + msgRef +"'";*/
				
				if(StringUtils.isNotEmpty(updQuery.toString()))
				{
					updateQuery = updQuery.toString().trim();
					if(updateQuery.endsWith(NgphEsbConstants.NGPH_COMMA))
					{
						String str = updQuery.reverse().toString().trim();
						StringBuilder sb = new StringBuilder(str.replaceFirst(NgphEsbConstants.NGPH_COMMA, NgphEsbConstants.NGPH_EMPTY));
						updateQuery = sb.reverse().toString().trim();
					}
					updateQuery = updateQuery.concat(NgphEsbConstants.NGPH_SPACE);
					updateQuery = updateQuery.concat("WHERE MSGS_MSGREF = ?");
					valuesArray[i]= canonicalObj.getMsgRef();
					
					//to avoid nullextra values and to avoid invalid column type errors
					//we need to have columns used in query and values in this array should be same in count
					Object[] actualArray = null;
					if(i+1 < valuesArray.length)
					{
						actualArray = new Object[i+1];
						for(int j=0; j<actualArray.length; j++)
						{
							actualArray[j] = valuesArray[j];
						}
						//its not required then after so sending to garbage collection 
						valuesArray = null;
					}
					jdbcTemplate.update(updateQuery, actualArray);
				}
			}
		}
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e); 
		}
		logger.info("ReportRptDOA ENDS");

	}

}
