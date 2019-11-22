package com.logica.ngph.esb.daosImpl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.logica.ngph.action.SFMSAction;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.utils.DateHelper;
import com.logica.ngph.esb.Dtos.DbPoller;
import com.logica.ngph.esb.Dtos.Raw_Msgs;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.rowmappers.CanonicalRowMapper;
import com.logica.ngph.esb.rowmappers.InfoCanonicalRowMapper;
import com.logica.ngph.esb.rowmappers.PollerRowMapper;
import com.logica.ngph.esb.rowmappers.RawMsgsRowMapper;
import com.logica.ngph.utils.EventLogger;

public class SwiftParserDaoImpl implements SwiftParserDao{

	private JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	   private OracleLobHandler  oracleLobHandler;

       public void setOracleLobHandler(OracleLobHandler oracleLobHandler)
       {
               this.oracleLobHandler = oracleLobHandler;
       }


	static Logger logger = Logger.getLogger(SwiftParserDaoImpl.class);
	
	public List<Raw_Msgs> getRaw_msgs(String msgRef) throws Exception
	{
		List<Raw_Msgs> rawMsgsData = null;
		
		String query = "select * from raw_msgs where RAW_MSGSREF=?";
		try
		{
			rawMsgsData = jdbcTemplate.query(query, new Object[]{msgRef}, new RawMsgsRowMapper());
		}
		catch(EmptyResultDataAccessException ex)
		{
			logger.warn("No Record found for msgRef : " + msgRef);
		}
		catch (IncorrectResultSizeDataAccessException e) 
		{
			logger.warn("No Record found for msgRef : " + msgRef);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return rawMsgsData;
	}

	public int getmsgCount(String txnref, String msgDirection, String senderBank)throws Exception
	{
		logger.info("GetmsgCount method() starts");

		 int result = 0;
		 String query="select count(*) from TA_MSGS_DUPCHECK where msgs_txnref=? AND MSGS_DIRECTION=? AND MSGS_INSTGAGT_BKCD=?";
		 try
		 {
			result = jdbcTemplate.queryForObject(query, new Object[]{txnref, msgDirection, senderBank}, Integer.class);
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (Exception e)
		 {
			 logger.error(e,e);
			 throw new Exception(e);
		 }
		 logger.info("GetmsgCount method() ENDS");
		 return result;
	}

	public int IsDuplicateByData(String dupFieldsData)throws Exception
	{
		logger.info("IsDuplicateByData() starts");
		int result = 0;
		String query="select count(*) from TA_MSGS_DUPCHECK where MSGS_DUPFIELDS=?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{dupFieldsData}, Integer.class);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch (Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logger.info("IsDuplicateByData() ENDS");
		return result;
	}
	
	public List<DbPoller> fetchPolledMsgs(String pollStatus)throws Exception
	{
		List<DbPoller> poller = null;
		String query = "select * from ta_msgspolled where POLL_STATUS=?";
		try
		{
			poller = jdbcTemplate.query(query, new Object[]{pollStatus}, new PollerRowMapper());
		}
		catch(EmptyResultDataAccessException ex)
		{
			logger.error(ex,ex);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return poller;
		
	}
	public void updateMsgStatusForRaw_Msgs(int msgStatus, String msgRef, String errorCode)throws Exception
	{
		logger.info("updateMsgStatusForRaw_Msgs Starts with mesRef : " + msgRef + " , status " + msgStatus + " , errorcode : " + errorCode);
		String query = "update RAW_MSGS set RAW_MSG_VALSTATUS=?, RAW_ERRCODE=? where RAW_MSGSREF=?";
		try
		{
			int result = jdbcTemplate.update(query, new Object[]{msgStatus,errorCode,msgRef});
			logger.info("updateMsgStatusForRaw_Msgs Ends with update status as : " + result);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
 
	}
	public int getIMPSMsgRefCount(String stan)throws Exception
	{
		int result = 0;
		String query = "select count(IMPS_MSGREF) from ta_imps_tx where IMPS_STAN =?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{stan}, Integer.class);
		}
		catch(EmptyResultDataAccessException ex)
		{
			logger.error(ex,ex);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return result;
 	}
	
	public String getIMPSMsgRef(String stan)throws Exception
	{
		String result = null;
		String query = "select IMPS_MSGREF from ta_imps_tx where IMPS_STAN =? AND ROWNUM = 1";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{stan}, String.class);
		}
		catch(EmptyResultDataAccessException ex)
		{
			logger.error(ex,ex);
		}
		catch (IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new  Exception(e);
		}
		return result;
 	}
	
	public int validateAckNowledgement(String mestype, String SubMesType, String MsgDirection)throws Exception
	{
		int result=0;
		String query = "select SUPPORT_ACK from msg_support where support_msgtype =? and support_submsgtype=? and SUPPORT_MSG_DIRECTION =?";
		try
		{
			result = jdbcTemplate.queryForInt(query, new Object[]{mestype,SubMesType,MsgDirection});
			logger.info("Value for Acknowledgement returned from DB is :" + result);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.SwiftParserDao#validateAckNowledgement(java.lang.String, java.lang.String)
	 * This method takes mesType and MesSubtype as Argument and return 0 or 1 
	 * Which tells whether Ack is supported or not
	 */
	public int validateAutoAckNowledgement(String hostId)throws Exception
	{
		int result=0;
		String query = "select EI_ACK_REQ from ta_ei where EI_CODE =?";
		try
		{
			result = jdbcTemplate.queryForInt(query, new Object[]{hostId});
			logger.info("Value for Auto Acknowledgement returned from DB is :" + result);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return result;
	}
	public int msgIsReturn(String mestype, String SubMesType, String MsgDirection)throws Exception
	{
		int result=-1;
		String query = "select SUPPORT_ISRETURN from msg_support where support_msgtype =? and support_submsgtype=? and SUPPORT_MSG_DIRECTION=?";
		try
		{
			result = jdbcTemplate.queryForInt(query, new Object[]{mestype,SubMesType,MsgDirection});
			logger.info("Value for Acknowledgement returned from DB is :" + result);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new  Exception(e);
		}
		return result;
	}
	
	public NgphCanonical getCanonicalFromMessagesTxforTxnRef(String txnRef)throws Exception
	{
		NgphCanonical ngphCanonical = null;
		try
		{
			String query = "select * from TA_MESSAGES_TX where MSGS_PMTID_INSTRID = ? and MSGS_MSGSTS=43 OR  MSGS_MSGSTS=44";
			ngphCanonical = (NgphCanonical)jdbcTemplate.queryForObject(query, new Object[]{txnRef}, new CanonicalRowMapper());
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return ngphCanonical;
	}
	
	public NgphCanonical getCanonicalFromMessagesTxforMsgRef(String msgref)throws Exception
	{
		List<String> listCommodity =null;
		NgphCanonical ngphCanonical = null;
		String[] commodityArray = null;
		try
		{
			String query = "select * from TA_MESSAGES_TX where MSGS_MSGREF = ?";
			ngphCanonical = (NgphCanonical)jdbcTemplate.queryForObject(query, new Object[]{msgref}, new CanonicalRowMapper());
			
			 String commArrQuery = "select LC_COMMODITY from TA_LC_COMMODITY where MSGS_MSGREF = ?";
				SqlRowSet com = jdbcTemplate.queryForRowSet(commArrQuery ,new Object[]{msgref});			
				
				 listCommodity = new ArrayList<String>();			
				while (com.next())
				{				
					String lcComm = com.getString("LC_COMMODITY");
					listCommodity.add(lcComm);				
				}
				commodityArray = new String[listCommodity.size()];
				listCommodity.toArray(commodityArray);
				ngphCanonical.setLcArrCommodity(commodityArray);
				
				for(int j=0; j<ngphCanonical.getLcArrCommodity().length; j++)
				{
					logger.info("LCCommodity Values are :: "+ngphCanonical.getLcArrCommodity()[j]);
				}			
		}
		catch (EmptyResultDataAccessException e) {

			logger.warn("No data in TA_Message_tx or TA_LC_Commodity for MsgRef : " + msgref);
			//logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			//logger.error(e,e);
			logger.warn("No data in TA_Message_tx or TA_LC_Commodity for MsgRef : " + msgref);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return ngphCanonical;
	}

	/**
	* This method is used to get all the data from TA_MESSAGES_TX table
	* @return NgphCanonical ngphCanonical
	*/
	public NgphCanonical getCanonicalFromMessagesTx(String msgmur)throws Exception 
	{
		NgphCanonical ngphCanonical = null;
		try
		{
			String query = "select * from TA_MESSAGES_TX where MSG_MUR = ?";
			ngphCanonical = (NgphCanonical)jdbcTemplate.queryForObject(query, new Object[]{msgmur}, new CanonicalRowMapper());
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return ngphCanonical;
	}
	
	public NgphCanonical getCanonicalFromMessagesTxForSeq(String seqNo)throws Exception 
	{
		NgphCanonical ngphCanonical = null;
		try
		{
			String query = "select * from TA_MESSAGES_TX where MSGS_SEQNO = ? AND MSGS_MSGSTS <> '99' AND MSGS_MSGSTS ='12'";
			ngphCanonical = (NgphCanonical)jdbcTemplate.queryForObject(query, new Object[]{seqNo}, new CanonicalRowMapper());
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return ngphCanonical;
	}
	
	 public void updateEiStatus(String eiCode, int istatus)throws Exception
	 {
			String query = "update ta_ei set EI_STATUS=? where ei_code=?";
			try
			{
				jdbcTemplate.update(query, new Object[]{istatus,eiCode});
			}
			catch (Exception e) {
				logger.error(e,e);
				throw new  Exception(e);
			}
	 }
	 
	 public String getPayStatus(String msg_PMTID_ENDTOENDID)throws Exception
	 {
			String payStatus = null;
			String query = "select MSGS_MSGSTS from ta_messages_tx where MSGS_PMTID_ENDTOENDID =?";
			try
			{
				payStatus = jdbcTemplate.queryForObject(query, new Object[]{msg_PMTID_ENDTOENDID}, String.class);
			}
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}
			catch (Exception e) {
				logger.error(e,e);
				throw new  Exception(e);
			}
			return payStatus;
	 }
	  public String getEiType(String hostId)throws Exception
	  {
			String eiType = null;
			String query = "select EI_TYPE from ta_ei where EI_CODE =?";
			try
			{
				eiType = jdbcTemplate.queryForObject(query, new Object[]{hostId}, String.class);
			}
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch (Exception e) {
				logger.error(e,e);
				throw new  Exception(e);
			}
			return eiType;
	  }

	  public String getHostFormat(String hostId)throws Exception
		{
			String hostFormat = null;
			String query = "select EI_FORMAT from ta_ei where EI_CODE =?";
			try
			{
				hostFormat = jdbcTemplate.queryForObject(query, new Object[]{hostId}, String.class);
			}
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch (Exception e) {
				logger.error(e,e);
				throw new  Exception(e);
			}
			return hostFormat;
			
		}
	public String getDstQueue(String ei_id)throws Exception
	{
		String hostCat = null;
		String query = "select OUTPUT_DEST_QUEUE from ta_ei where EI_CODE =?";
		try
		{
			hostCat = jdbcTemplate.queryForObject(query, new Object[]{ei_id}, String.class);
		}
		catch(EmptyResultDataAccessException ex)
		{
			logger.error(ex,ex);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}

		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
		return hostCat;
	}
	public String getIsoParty(String isoPtyCode)throws Exception
	{

		 String result=null;
		 String Query="SELECT PARTY_BIC FROM TA_PARTIES WHERE party_isocode ='"+isoPtyCode+"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
		 }
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch (Exception e) 
			{
				logger.error(e,e);	
				throw new  Exception(e);
			}
		 return result;
	}
	public String getIsoCurr(String currCode)throws Exception
	{
		 String result=null;
		 String Query="select CUR_CODE from ta_currency_mast where CUR_ISOCODE='" + currCode +"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
		 }
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch (Exception e) 
			{
				logger.error(e,e);
				throw new  Exception(e);
			}
		 return result;
	}
	// Methods Required For Parser
	 public String retrieveEICode(String providerESB)throws Exception
	 {
		 String result=null;
		 String Query="select ei_code from TA_EI where INPUT_SRC_QUEUE ='" + providerESB +"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
			// System.out.println("Value is ::" + result);
		 }
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

		 catch (Exception e) {
			 logger.error(e,e);
			 throw new  Exception(e);
		}
		 return result;
	 }
	 
	 public String retrieveBICDetails()	throws Exception
	 {
			String result=null;
			Clob clob = null;
			try
			{
				String bicQuery = "select init_value from initialisationm where init_entry = 'LOCALBIC'";
				clob = jdbcTemplate.queryForObject(bicQuery, Clob.class);
				int clobLength = (int) clob.length();
				result = clob.getSubString(1, clobLength);
			}
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch(Exception e)
			{
				logger.error(e,e);
				throw new  Exception(e);
			}
			return result;
		}
	
	 public void insertRemitInfoDetails(String remitInfoRef,String msgRef,String aValue)throws Exception
		{
				String query = " insert into rmt_info (RMT_INFO_REF, RMT_MSGS_MSGREF,RMT_USTRD)"
					+ " values ('" + remitInfoRef +"','"+ msgRef +"','" + aValue + "')";
				try
				{
					jdbcTemplate.execute(query);
				}
				catch (Exception e) {
					logger.error(e,e);
					throw new  Exception(e);
				}
		}	
	 
	 public void insertRawMessage(final String aHostID, final String msgRef, final String message, final String msgChnl, final String msgDirection)throws Exception
	 {
		 String sql = " Insert INTO raw_msgs " + " (RAW_RECVDTIME, RAW_DIRECTION, RAW_CHNL, RAW_HOST,RAW_MSGSREF,RAW_MSG ) " + " Values   (?,?,?,?,?,?) ";
		try
		{
		 jdbcTemplate.update(sql, new PreparedStatementSetter() 
		 {
              public void setValues(PreparedStatement ps) throws SQLException
              {
                      ps.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
                      ps.setString(2, msgDirection);
                      ps.setString(3, msgChnl);
                      ps.setString(4, aHostID);
                      ps.setString(5, msgRef);
                      oracleLobHandler.getLobCreator().setClobAsString(ps, 6,message);
              }
		 });
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
	 }

	 /*
	  * (non-Javadoc)
	  * @see com.logica.ngph.esb.daos.SwiftParserDao#insertParsedMessage(com.logica.ngph.common.dtos.NgphCanonical)
	  * 
	  * This method is made Generic for all Parsers.
	  * Inserting the Data for all the Canonical Variables. 
	  * Because some Parser may populate some canonical value while other may populate other values.
	  * So dumping all the Canonical Variables to Table will work for all Parsers.
	  */
	// @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class,RuntimeException.class,Throwable.class})
	 public void insertParsedMessage(NgphCanonical canonicalObj)throws Exception
	 {
			logger.info("Insert Parsed Message starts");

			if(canonicalObj != null)
			{
				int i = 0;
				Object[] valuesArray =	new Object[250];
				String query = null;
				StringBuilder columns = new StringBuilder();
				StringBuilder count = new StringBuilder();
				
				columns.append("insert into TA_MESSAGES_TX");
				columns.append(NgphEsbConstants.NGPH_SPACE);
				columns.append("(");
				
				count.append("values");
				count.append(NgphEsbConstants.NGPH_SPACE);
				count.append("(");
				
				if(canonicalObj.getMsgRef()!= null)
				{
					columns.append("MSGS_MSGREF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgRef();
					i++;
				}
				if(canonicalObj.getGrpMsgId()!= null)
				{
					columns.append("MSGS_GRP_MSGID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getGrpMsgId();
					i++;
				}
				if(canonicalObj.getGrpSeq()>= 0)
				{
					columns.append("MSGS_GRP_SEQ,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getGrpSeq();
					i++;
				}
				if(canonicalObj.getMsgHost() != null)
				{
					columns.append("MSGS_HOSTID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgHost();
					i++;
				}
				if(canonicalObj.getMsgChannel() != null)
				{
					columns.append("MSGS_CHANNELID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgChannel();
					i++;
				}
				if(canonicalObj.getMsgChnlType() !=null)
				{
					columns.append("MSGS_MSGCHNLTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgChnlType();
					i++;
				}
				if(canonicalObj.getSrcMsgType() !=null)
				{
					columns.append("MSGS_SRC_MSGTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSrcMsgType();
					i++;
				}
				if(canonicalObj.getSrcMsgSubType()!= null)
				{
					columns.append("MSGS_SRC_MSGSUBTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSrcMsgSubType();
					i++;
				}
				if(canonicalObj.getDstMsgType()!= null)
				{
					columns.append("MSGS_DST_MSGTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getDstMsgType();
					i++;
				}
				if(canonicalObj.getDstMsgSubType()!= null)
				{
					columns.append("MSGS_DST_SUBMSGTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getDstMsgSubType();
					i++;
				}
				if(canonicalObj.getMsgStatus() !=null)
				{
					columns.append("MSGS_MSGSTS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgStatus();
					i++;
				}
				if(canonicalObj.getMsgPrevStatus() !=null)
				{
					columns.append("MSGS_PREVMSGSTS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgPrevStatus();
					i++;
				}
				if(canonicalObj.getMsgDirection() !=null)
				{
					columns.append("MSGS_DIRECTION,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgDirection();
					i++;
				}
				if(canonicalObj.getReceivedTime() !=null)
				{
					columns.append("MSGS_RECVDTIME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReceivedTime();
					i++;
				}
				if(canonicalObj.getLastModTime() !=null)
				{
					columns.append("MSGS_LASTMODIFIEDTIME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLastModTime();
					i++;
				}
				if(canonicalObj.getTxnReference() !=null)
				{
					columns.append("MSGS_PMTID_INSTRID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getTxnReference();
					logger.info("MSGS_PMTID_INSTRID is "+canonicalObj.getTxnReference());
					i++;
				}
				if(canonicalObj.getRelReference() !=null)
				{
					columns.append("MSGS_PMTID_RELREF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRelReference();
					logger.info("MSGS_PMTID_RELREF is "+canonicalObj.getRelReference());
					i++;
				}
				if(canonicalObj.getCustTxnReference() !=null)
				{
					columns.append("MSGS_PMTID_ENDTOENDID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getCustTxnReference();
					i++;
				}
				if(canonicalObj.getSndrTxnId() !=null)
				{
					columns.append("MSGS_PMTID_INSTGID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSndrTxnId();
					i++;
				}
				if(canonicalObj.getClrgSysReference() !=null)
				{
					columns.append("MSGS_PMTID_CLRSYSREF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getClrgSysReference();
					i++;
				}
				if(canonicalObj.getSndrPymtPriority() !=null)
				{
					columns.append("MSGS_PMTTPLINF_INSTRPRTY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSndrPymtPriority();
					i++;
				}
				if(canonicalObj.getClrgChannel() !=null)
				{
					columns.append("MSGS_PMTTPLINF_CLRCHNL,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getClrgChannel();
					i++;
				}
				if(canonicalObj.getSvcLevelCode() !=null)
				{
					columns.append("MSGS_PMTTPLINF_SVCLVL_CD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSvcLevelCode();
					i++;
				}
				if(canonicalObj.getSvcLevelProperitary() !=null)
				{
					columns.append("MSGS_PMTTPLINF_SVCLVL_PRTRY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSvcLevelProperitary();
					i++;
				}
				if(canonicalObj.getLclInstCode() !=null)
				{
					columns.append("MSGS_PMTTPLINF_LCLINSTRM_CD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLclInstCode();
					i++;
				}
				if(canonicalObj.getLclInstProperitary() !=null)
				{
					columns.append("MSGS_PMTTPLINF_LCLINSTRM_PRTRY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLclInstProperitary();
					i++;
				}
				if(canonicalObj.getCatgPurposeCode() !=null)
				{
					columns.append("MSGS_PMTTPLINF_CTGYPURP_CD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getCatgPurposeCode();
					i++;
				}
				if(canonicalObj.getCatgPurposeProperitary() !=null)
				{
					columns.append("MSGS_PMTTPLINF_CTGYPURP_PRTRY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getCatgPurposeProperitary();
					i++;
				}
				if(canonicalObj.getMsgCurrency() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMCCY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgCurrency();
					i++;
				}
				if(canonicalObj.getMsgAmount() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgAmount();
					i++;
				}
				if(canonicalObj.getMsgValueDate() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMDT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgValueDate();
					i++;
				}
				if(canonicalObj.getSndrSttlmntPriority() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMPRTY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSndrSttlmntPriority();
					i++;
				}
				if(canonicalObj.getDrDateTime() !=null)
				{
					columns.append("MSGS_STTLMTMINDCTN_DBTDTTM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getDrDateTime();
					i++;
				}
				if(canonicalObj.getCrDateTime() !=null)
				{
					columns.append("MSGS_STTLMTMINDCTN_CDTDTTM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getCrDateTime();
					i++;
				}
				if(canonicalObj.getClsDateTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_CLSTM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getClsDateTime();
					i++;
				}
				if(canonicalObj.getSttlmntTillTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_TILLTM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSttlmntTillTime();
					i++;
				}
				if(canonicalObj.getSttlmntFromTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_FRTM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSttlmntFromTime();
					i++;
				}
				if(canonicalObj.getSttlmntRejTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_RJCTTM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSttlmntRejTime();
					i++;
				}
				if(canonicalObj.getPymntAcceptedTime() !=null)
				{
					columns.append("MSGS_ACCPTNCDTTM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getPymntAcceptedTime();
					i++;
				}
				if(canonicalObj.getCashpoolAdjstmntTime() !=null)
				{
					columns.append("MSGS_POOLGADJSTMNTDT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getCashpoolAdjstmntTime();
					i++;
				}
				if(canonicalObj.getInstructedCurrency() !=null)
				{
					columns.append("MSGS_INSTDCCY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInstructedCurrency();
					i++;
				}
				if(canonicalObj.getInstructedAmount() !=null)
				{
					columns.append("MSGS_INSTDAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInstructedAmount();
					i++;
				}
				if(canonicalObj.getXchangeRate() !=null)
				{
					columns.append("MSGS_XCHGRATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getXchangeRate();
					i++;
				}
				if(canonicalObj.getChargeBearer() !=null)
				{
					columns.append("MSGS_CHRGBR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getChargeBearer();
					i++;
				}
				if(canonicalObj.getPrevInstructingBank() !=null)
				{
					columns.append("MSGS_PRVSINSTGAGT_BKCD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getPrevInstructingBank();
					i++;
				}
				if(canonicalObj.getPrevInstructingAgentAcct() !=null)
				{
					columns.append("MSGS_PRVSINSTGAGT_ACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getPrevInstructingAgentAcct();
					i++;
				}
				if(canonicalObj.getSenderBank() !=null)
				{
					columns.append("MSGS_INSTGAGT_BKCD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSenderBank();
					i++;
				}
				if(canonicalObj.getReceiverBank() !=null)
				{
					columns.append("MSGS_INSTDAGT_BKCD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReceiverBank();
					i++;
				}
				if(canonicalObj.getIntermediary1Bank() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_BKCD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary1Bank();
					i++;
				}
				if(canonicalObj.getIntermediary1BankId() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary1BankId();
					i++;
				}
				if(canonicalObj.getIntermediary1BankClrgCd() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_CLRCODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary1BankClrgCd();
					i++;
				}
				if(canonicalObj.getIntermediary1BankName() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary1BankName();
					i++;
				}
				if(canonicalObj.getIntermediary1AgentAcct() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_ACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary1AgentAcct();
					i++;
				}
				if(canonicalObj.getIntermediary2Bank() !=null)
				{
					columns.append("MSGS_INTRMYAGT2_BKCD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary2Bank();
					i++;
				}
				if(canonicalObj.getIntermediary2AgentAcct() !=null)
				{
					columns.append("MSGS_INTRMYAGT2_ACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary2AgentAcct();
					i++;
				}
				if(canonicalObj.getIntermediary2BankName() !=null)
				{
					columns.append("MSGS_INTRMYAGT2_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary2BankName();
					i++;
				}
				if(canonicalObj.getIntermediary3Bank() !=null)
				{
					columns.append("MSGS_INTRMYAGT3_BKCD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary3Bank();
					i++;
				}
				if(canonicalObj.getIntermediary3AgentAcct() !=null)
				{
					columns.append("MSGS_INTRMYAGT3_ACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary3AgentAcct();
					i++;
				}
				if(canonicalObj.getIntermediary3BankName() !=null)
				{
					columns.append("MSGS_INTRMYAGT3_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIntermediary3BankName();
					i++;
				}
				if(canonicalObj.getUltimateDebtorName() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorName();
					i++;
				}
				if(canonicalObj.getUltimateDebtorAddress() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_PSTLADR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorAddress();
					i++;
				}
				if(canonicalObj.getUltimateDebtorID() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorID();
					i++;
				}
				if(canonicalObj.getUltimateDebtorCtry() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_CTRYOFRES,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorCtry();
					i++;
				}
				if(canonicalObj.getUltimateDebtorCtctDtls() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_CTCTDTLS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorCtctDtls();
					i++;
				}
				if(canonicalObj.getInitiatingPartyName() !=null)
				{
					columns.append("MSGS_INITGPRTY_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyName();
					i++;
				}
				if(canonicalObj.getInitiatingPartyAddress() !=null)
				{
					columns.append("MSGS_INITGPRTY_PSTLADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyAddress();
					i++;
				}
				if(canonicalObj.getInitiatingPartyID() !=null)
				{
					columns.append("MSGS_INITGPRTY_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyID();
					i++;
				}
				if(canonicalObj.getInitiatingPartyCtry() !=null)
				{
					columns.append("MSGS_INITGPRTY_CTRYOFRES,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyCtry();
					i++;
				}
				if(canonicalObj.getInitiatingPartyCtctDtls() !=null)
				{
					columns.append("MSGS_INITGPRTY_CTCTDTLS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyCtctDtls();
					i++;
				}
				if(canonicalObj.getOrderingCustomerName() !=null)
				{
					columns.append("MSGS_DBTR_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerName();
					i++;
				}
				if(canonicalObj.getOrderingCustomerAddress() !=null)
				{
					columns.append("MSGS_DBTR_PSTLADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerAddress();
					i++;
				}
				if(canonicalObj.getOrderingCustomerId() !=null)
				{
					columns.append("MSGS_DBTR_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerId();
					i++;
				}
				if(canonicalObj.getOrderingCustomerCtry() !=null)
				{
					columns.append("MSGS_DBTR_CTRYOFRES,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerCtry();
					i++;
				}
				if(canonicalObj.getOrderingCustomerCtctDtls() !=null)
				{
					columns.append("MSGS_DBTR_CTCTDTLS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerCtctDtls();
					i++;
				}
				if(canonicalObj.getOrderingCustAccount() !=null)
				{
					columns.append("MSGS_DBTRACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingCustAccount();
					i++;
				}
				if(canonicalObj.getOrderingType() !=null)
				{
					columns.append("MSGS_DBTRTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingType();
					i++;
				}
				if(canonicalObj.getOrderingAcType() !=null)
				{
					columns.append("MSGS_DBTR_ACTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingAcType();
					i++;
				}
				if(canonicalObj.getOrderingInstitution() !=null)
				{
					columns.append("MSGS_DBTRAGT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingInstitution();
					i++;
				}
				if(canonicalObj.getOrderingInstitutionId() !=null)
				{
					columns.append("MSGS_DBTRAGT_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingInstitutionId();
					i++;
				}
				if(canonicalObj.getOrderingInstitutionName() !=null)
				{
					columns.append("MSGS_DBTRAGT_NAME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingInstitutionName();
					i++;
				}
				if(canonicalObj.getOrderingInstitutionAcct() !=null)
				{
					columns.append("MSGS_DBTRAGTACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getOrderingInstitutionAcct();
					i++;
				}
				if(canonicalObj.getAccountWithInstitution() !=null)
				{
					columns.append("MSGS_CDTRAGT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitution();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionId() !=null)
				{
					columns.append("MSGS_CDTRAGT_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionId();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionLoc() !=null)
				{
					columns.append("MSGS_CDTRAGT_LOC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionLoc();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionClrgCd() !=null)
				{
					columns.append("MSGS_CDTRAGT_CLRCODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionClrgCd();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionName() !=null)
				{
					columns.append("MSGS_CDTRAGT_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionName();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionAcct() !=null)
				{
					columns.append("MSGS_CDTRAGTACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionAcct();
					i++;
				}
				if(canonicalObj.getSenderCorrespondent() !=null)
				{
					columns.append("MSGS_SNDRAGT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondent();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentId() !=null)
				{
					columns.append("MSGS_SNDRAGT_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentId();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentLoc() !=null)
				{
					columns.append("MSGS_SNDRAGT_LOC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentLoc();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentName() !=null)
				{
					columns.append("MSGS_SNDRAGT_NAME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentName();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentAcct() !=null)
				{
					columns.append("MSGS_SNDRAGTACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentAcct();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondent() !=null)
				{
					columns.append("MSGS_RCVRAGT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondent();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentId() !=null)
				{
					columns.append("MSGS_RCVRAGT_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentId();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentLoc() !=null)
				{
					columns.append("MSGS_RCVRAGT_LOC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentLoc();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentName() !=null)
				{
					columns.append("MSGS_RCVRAGT_NAME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentName();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentAcct() !=null)
				{
					columns.append("MSGS_RCVRAGTACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentAcct();
					i++;
				}
				if(canonicalObj.getThirdCorrespondent() !=null)
				{
					columns.append("MSGS_THIRDAGT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondent();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentId() !=null)
				{
					columns.append("MSGS_THIRDAGT_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentId();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentLoc() !=null)
				{
					columns.append("MSGS_THIRDAGT_LOC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentLoc();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentName() !=null)
				{
					columns.append("MSGS_THIRDAGT_NAME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentName();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentAcct() !=null)
				{
					columns.append("MSGS_THIRDAGTACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentAcct();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerName() !=null)
				{
					columns.append("MSGS_CDTR_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerName();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerAddress() !=null)
				{
					columns.append("MSGS_CDTR_PSTLADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerAddress();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerID() !=null)
				{
					columns.append("MSGS_CDTR_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerID();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerCtry() !=null)
				{
					columns.append("MSGS_CDTR_CTRYOFRES,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerCtry();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerCtctDtls() !=null)
				{
					columns.append("MSGS_CDTR_CTCTDTLS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerCtctDtls();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustAcct() !=null)
				{
					columns.append("MSGS_CDTRACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustAcct();
					i++;
				}
				if(canonicalObj.getBeneficiaryType() !=null)
				{
					columns.append("MSGS_CDTRTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryType();
					i++;
				}
				if(canonicalObj.getBeneficiaryAcType() !=null)
				{
					columns.append("MSGS_CDTR_ACTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryAcType();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitution() !=null)
				{
					columns.append("MSGS_BENFINST_CD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitution();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitutionName() !=null)
				{
					columns.append("MSGS_BENFINST_NAME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitutionName();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitutionAcct() !=null)
				{
					columns.append("MSGS_BENFINST_ACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitutionAcct();
					i++;
				}
				if(canonicalObj.getUltimateCreditorName() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorName();
					i++;
				}
				if(canonicalObj.getUltimateCreditorAddress() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_PSTLADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorAddress();
					i++;
				}
				if(canonicalObj.getUltimateCreditorID() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorID();
					i++;
				}
				if(canonicalObj.getUltimateCreditorCtry() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_CTRYOFRES,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorCtry();
					i++;
				}
				if(canonicalObj.getUltimateCreditorCtctDtls() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_CTCTDTLS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorCtctDtls();
					i++;
				}
				if(canonicalObj.getInstructionsForCrdtrAgtCode() !=null)
				{
					columns.append("MSGS_INSTRFORCDTRAGT_CD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInstructionsForCrdtrAgtCode();
					i++;
				}
				if(canonicalObj.getInstructionsForCrdtrAgtText() !=null)
				{
					columns.append("MSGS_INSTRFORCDTRAGT_INSTRINF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInstructionsForCrdtrAgtText();
					i++;
				}
				if(canonicalObj.getInstructionsForNextAgtCode() !=null)
				{
					columns.append("MSGS_INSTRFORNXTAGT_CD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInstructionsForNextAgtCode();
					i++;
				}
				if(canonicalObj.getInstructionsForNextAgtText() !=null)
				{
					columns.append("MSGS_INSTRFORNXTAGT_INSTRINF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInstructionsForNextAgtText();
					i++;
				}
				if(canonicalObj.getMsgPurposeCode() !=null)
				{
					columns.append("MSGS_PURP_CD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgPurposeCode();
					i++;
				}
				if(canonicalObj.getMsgPurposeText() !=null)
				{
					columns.append("MSGS_PURP_PRTRY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgPurposeText();
					i++;
				}
				if(canonicalObj.getRegulatoryBankCode() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_BKID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRegulatoryBankCode();
					i++;
				}
				if(canonicalObj.getRegulatoryReportDrCr() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_DRCR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRegulatoryReportDrCr();
					i++;
				}
				if(canonicalObj.getRegulatoryReportCurrency() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_CCY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRegulatoryReportCurrency();
					i++;
				}
				if(canonicalObj.getRegulatoryReportAmount() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_AMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRegulatoryReportAmount();
					i++;
				}
				if(canonicalObj.getRegulatoryInformation() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_INF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRegulatoryInformation();
					i++;
				}
				if(canonicalObj.getInitiatorRemitReference() !=null)
				{
					columns.append("MSGS_R_RMTID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInitiatorRemitReference();
					i++;
				}
				if(canonicalObj.getInitiatorRemitAdviceMethod() !=null)
				{
					columns.append("MSGS_R_RMTLCTNMTD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInitiatorRemitAdviceMethod();
					i++;
				}
				if(canonicalObj.getRemitInfoEmail() !=null)
				{
					columns.append("MSGS_R_RMTLCTNELCTRNCADR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRemitInfoEmail();
					i++;
				}
				if(canonicalObj.getRemitReceivingPartyName() !=null)
				{
					columns.append("MSGS_R_RMTLCTNPSTLADR_NM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRemitReceivingPartyName();
					i++;
				}
				if(canonicalObj.getRemitReceivingPartyAddress() !=null)
				{
					columns.append("MSGS_R_RMTLCTNPSTLADR_ADR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRemitReceivingPartyAddress();
					i++;
				}
				if(canonicalObj.getRelRemitInfoRef() !=null)
				{
					columns.append("MSGS_R_RMTINF_REF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRelRemitInfoRef();
					i++;
				}
				if(canonicalObj.getRemitInfoRef() !=null)
				{
					columns.append("MSGS_RMTINF_REF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRemitInfoRef();
					i++;
				}
				if(canonicalObj.getMsgTxnType() !=null)
				{
					columns.append("MSGS_TXNTYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgTxnType();
					i++;
				}
				if(canonicalObj.getMsgReturnReference() !=null)
				{
					columns.append("MSGS_RETURNREF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgReturnReference();
					i++;
				}
				if(canonicalObj.getCustAccount() !=null)
				{
					columns.append("MSGS_IDTFD_CUSTAC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getCustAccount();
					i++;
				}
				if(canonicalObj.getMsgBatchTime() !=null)
				{
					columns.append("MSGS_BATCHTIME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgBatchTime();
					i++;
				}
				if(canonicalObj.getMsgDept() !=null)
				{
					columns.append("MSGS_DEPT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgDept();
					i++;
				}
				if(canonicalObj.getMsgBranch() !=null)
				{
					columns.append("MSGS_BRANCH,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgBranch();
					i++;
				}
				if(canonicalObj.getMsgRules() !=null)
				{
					columns.append("MSGS_RULESAPPLIED,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgRules();
					i++;
				}
				if(canonicalObj.getRelUid() !=null)
				{
					columns.append("MSGS_RELMSG_MSGREF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRelUid();
					i++;
				}
				if(canonicalObj.getMsgMur() !=null)
				{
					columns.append("MSG_MUR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgMur();
					i++;
				}
				if(canonicalObj.getLastModifiedUser() !=null)
				{
					columns.append("MSGS_MODIFIED_USER,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLastModifiedUser();
					i++;
				}
				if(canonicalObj.getComments() !=null)
				{
					columns.append("MSGS_COMMENTS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getComments();
					i++;
				}
				if(canonicalObj.getCrCurrency() !=null)
				{
					columns.append("MSG_CRCUR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getCrCurrency();
					i++;
				}
				if(canonicalObj.getDrCurrency() !=null)
				{
					columns.append("MSG_DRCUR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getDrCurrency();
					i++;
				}
				if(canonicalObj.getBaseCcyAmount() !=null)
				{
					columns.append("MSG_BASECURAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBaseCcyAmount();
					i++;
				}
				if(canonicalObj.getMsgCurrencyAmount() !=null)
				{
					columns.append("MSG_MSGCURAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgCurrencyAmount();
					i++;
				}
				if(canonicalObj.getInstructedCcyAmount() !=null)
				{
					columns.append("MSG_INSTDCURAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getInstructedCcyAmount();
					i++;
				}
				if(canonicalObj.getServiceID() !=null)
				{
					columns.append("MSG_SRVID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getServiceID();
					i++;
				}
				if(canonicalObj.getRepairReason() !=null)
				{
					columns.append("MSG_RPR_RSN,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getRepairReason();
					i++;
				}
				if(canonicalObj.getAccountingStatus() !=null)
				{
					columns.append("MSG_ACCTNG_STS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountingStatus();
					i++;
				}
				if(canonicalObj.getAccountingReason() !=null)
				{
					columns.append("MSG_ACCTNG_RSN,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getAccountingReason();
					i++;
				}
				if(canonicalObj.getPdeCount() >=0)
				{
					columns.append("MSGS_PDECOUNT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getPdeCount();
					i++;
				}
				if(canonicalObj.getReturnReasonCode() !=null)
				{
					columns.append("MSGS_RETURN_RSNCODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReturnReasonCode();
					i++;
				}
				if(canonicalObj.getReturnReasonDesc() !=null)
				{
					columns.append("MSGS_RETURN_RSNDESC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getReturnReasonDesc();
					i++;
				}
				if(canonicalObj.getDstMsgChnlType() !=null)
				{
					columns.append("MSGS_DST_CHNL_TYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getDstMsgChnlType();
					i++;
				}
				if(canonicalObj.getDstEiId() !=null)
				{
					columns.append("MSGS_DST_EI_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getDstEiId();
					i++;
				}
				if(canonicalObj.getSeqNo() !=null)
				{
					columns.append("MSGS_SEQNO,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSeqNo();
					i++;
				}
				if(canonicalObj.getMsgErrorDesc() !=null)
				{
					columns.append("MSGS_ERROR_DESC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgErrorDesc();
					i++;
				}
				if(canonicalObj.getMsgIsReturn() >=0)
				{
					columns.append("MSGS_IS_RETURN,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgIsReturn();
					i++;
				}
				if(canonicalObj.getLcType() !=null)
				{
					columns.append("LC_TYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcType();
					i++;
				}
				if(canonicalObj.getLcNo() !=null)
				{
					columns.append("LC_NUMBER,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcNo();
					i++;
				}
				if(canonicalObj.getLcPrevAdvRef() !=null)
				{
					columns.append("LC_PRE_ADV_REF,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcPrevAdvRef();
					i++;
				}
				if(canonicalObj.getLcIssueDt() !=null)
				{
					columns.append("LC_ISSUE_DT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcIssueDt();
					i++;
				}
				if(canonicalObj.getLcExpDt() !=null)
				{
					columns.append("LC_EXP_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcExpDt();
					i++;
				}
				if(canonicalObj.getLcExpPlace() !=null)
				{
					columns.append("LC_EXP_PLACE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcExpPlace();
					i++;
				}
				if(canonicalObj.getLcTolerance() !=null)
				{
					columns.append("LC_NEG_TOLERANCE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcTolerance();
					i++;
				}
				if(canonicalObj.getLcPosTolerance()!=null)
				{
					columns.append("LC_POS_TOLERANCE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcTolerance();
					i++;
				}
				if(canonicalObj.getLcMaxCrAmt() !=null)
				{
					columns.append("LC_MAX_CRAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcMaxCrAmt();
					i++;
				}
				if(canonicalObj.getLcAddlAmts() !=null)
				{
					columns.append("LC_ADDNL_AMTS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAddlAmts();
					i++;
				}
				if(canonicalObj.getLcAuthBankCode() !=null)
				{
					columns.append("LC_AUTHBANK_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAuthBankCode();
					i++;
				}
				if(canonicalObj.getLcAuthBankAddr() !=null)
				{
					columns.append("LC_AUTHBANK_ADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAuthBankAddr();
					i++;
				}
				if(canonicalObj.getLcAuthMode() !=null)
				{
					columns.append("LC_AUTH_MODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAuthMode();
					i++;
				}
				if(canonicalObj.getLcDispatchPlace() !=null)
				{
					columns.append("LC_DISPATCH_PLACE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDispatchPlace();
					i++;
				}
				if(canonicalObj.getLcDeparturePlace() !=null)
				{
					columns.append("LC_DEPARTURE_PLACE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDeparturePlace();
					i++;
				}
				if(canonicalObj.getLcFinalDstn() !=null)
				{
					columns.append("LC_FINAL_DESTINATION,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcFinalDstn();
					i++;
				}

				if(canonicalObj.getLcDstn() !=null)
				{
					columns.append("LC_DESTINATION,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDstn();
					i++;
				}
				if(canonicalObj.getLcLstShipDt() !=null)
				{
					columns.append("LC_LAST_SHIPDT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcLstShipDt();
					i++;
				}
				if(canonicalObj.getLcShipPeriod() !=null)
				{
					columns.append("LC_SHIP_PRD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcShipPeriod();
					i++;
				}
				if(canonicalObj.getLcShipTerms() !=null)
				{
					columns.append("LC_SHIP_TERMS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcShipTerms();
					i++;
				}
				if(canonicalObj.getLcDraftsAt() !=null)
				{
					columns.append("LC_DRAFTS_AT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDraftsAt();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkPid() !=null)
				{
					columns.append("LC_DRAWEEBANK_PID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkPid();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkCode() !=null)
				{
					columns.append("LC_DRAWEEBANK_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkCode();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkAddr() !=null)
				{
					columns.append("LC_DRAWEEBANK_ADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkAddr();
					i++;
				}
				if(canonicalObj.getLcMixedPymtDet() !=null)
				{
					columns.append("LC_MIXED_PYMT_DET,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcMixedPymtDet();
					i++;
				}
				if(canonicalObj.getLcDefPymtDet() !=null)
				{
					columns.append("LC_DEF_PYMT_DET,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDefPymtDet();
					i++;
				}
				if(canonicalObj.getLcPartialShipment() !=null)
				{
					columns.append("LC_PARTIAL_SHIPMENT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcPartialShipment();
					i++;
				}
				if(canonicalObj.getLcTransShipment() !=null)
				{
					columns.append("LC_TRANS_SHIPMENT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcTransShipment();
					i++;
				}
				if(canonicalObj.getLcDocsReq1() !=null)
				{
					columns.append("LC_DOCS_REQD_1,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDocsReq1();
					i++;
				}
				if(canonicalObj.getLcDocsReq2() !=null)
				{
					columns.append("LC_DOCS_REQD_2,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDocsReq2();
					i++;
				}
				if(canonicalObj.getLcAddnlCndt1() !=null)
				{
					columns.append("LC_ADDNL_CONDITIONS_1,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAddnlCndt1();
					i++;
				}
				if(canonicalObj.getLcAddnlCndt2() !=null)
				{
					columns.append("LC_ADDNL_CONDITIONS_2,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAddnlCndt2();
					i++;
				}
				if(canonicalObj.getLcCharges() !=null)
				{
					columns.append("LC_CHARGES,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcCharges();
					i++;
				}
				if(canonicalObj.getLcPrsntnPrd() !=null)
				{
					columns.append("LC_PRSNTN_PRD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcPrsntnPrd();
					i++;
				}
				if(canonicalObj.getLcConfrmInstrns() !=null)
				{
					columns.append("LC_CONFRM_INSTRNS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcConfrmInstrns();
					i++;
				}
				if(canonicalObj.getLcInstrnTopay() !=null)
				{
					columns.append("LC_INSTRNS_TOPAY,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcInstrnTopay();
					i++;
				}
				if(canonicalObj.getLcNarrative() !=null)
				{
					columns.append("LC_NARRATIVE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcNarrative();
					i++;
				}
				if(canonicalObj.getLcAmndmntNo() > 0)
				{
					columns.append("LC_AMNDMNT_NO,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAmndmntNo();
					i++;
				}
				if(canonicalObj.getLcAmndmntDt() !=null)
				{
					columns.append("LC_AMNDMNT_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAmndmntDt();
					i++;
				}
				if(canonicalObj.getLcOldExpDt() !=null)
				{
					columns.append("LC_OLD_EXP_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcOldExpDt();
					i++;
				}
				if(canonicalObj.getLcAmndmntIncAmt() !=null)
				{
					columns.append("LC_AMNDMNT_INCAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAmndmntIncAmt();
					i++;
				}
				if(canonicalObj.getLcAmndmntDecAmt() !=null)
				{
					columns.append("LC_AMNDMNT_DECAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAmndmntDecAmt();
					i++;
				}
				if(canonicalObj.getLcAmndmntOldAmt() !=null)
				{
					columns.append("LC_AMNDMNT_OLDAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAmndmntOldAmt();
					i++;
				}
				if(canonicalObj.getLcAccId() !=null)
				{
					columns.append("LC_ACC_ID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAccId();
					i++;
				}
				if(canonicalObj.getLcAckDt() !=null)
				{
					columns.append("LC_ACK_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAckDt();
					i++;
				}
				if(canonicalObj.getLcChgsClaimed() !=null)
				{
					columns.append("LC_CHGS_CLAIMED,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcChgsClaimed();
					i++;
				}
				if(canonicalObj.getLcToAmtClaimed() !=null)
				{
					columns.append("LC_TOTAMT_CLAIMED,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcToAmtClaimed();
					i++;
				}
				if(canonicalObj.getLcTotalAmtClaimed() !=null)
				{
					columns.append("LC_ADDNLAMT_CLAIMED,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcTotalAmtClaimed();
					i++;
				}
				if(canonicalObj.getLcNetAmtClaimed() !=null)
				{
					columns.append("LC_NETAMT_CLAIMED,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcNetAmtClaimed();
					i++;
				}
				if(canonicalObj.getLcAmtPaid() !=null)
				{
					columns.append("LC_AMT_PAID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAmtPaid();
					i++;
				}
				if(canonicalObj.getLcDiscrepancies() !=null)
				{
					columns.append("LC_DISCREPANCIES,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDiscrepancies();
					i++;
				}
				if(canonicalObj.getLcDispoDocs() !=null)
				{
					columns.append("LC_DISPO_DOCS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDispoDocs();
					i++;
				}
				if(canonicalObj.getMsgRelStatus() !=null)
				{
					columns.append("MSGS_REL_STATUS,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMsgRelStatus();
					i++;
				}
				if(canonicalObj.getLcTypeAuthCode() !=null)
				{
					columns.append("LC_TYPE_AUTHCODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcTypeAuthCode();
					i++;
				}
				if(canonicalObj.getLcNonBankIssuer() !=null)
				{
					columns.append("LC_NONBANK_ISSUER,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcNonBankIssuer();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkAcct() !=null)
				{
					columns.append("LC_DRAWEEBANK_ACCT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkAcct();
					i++;
				}
				if(canonicalObj.getLcAppRulesCode() !=null)
				{
					columns.append("LC_APPLICABLE_RULES_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAppRulesCode();
					i++;
				}
				if(canonicalObj.getLcAppRulesDesc() !=null)
				{
					columns.append("LC_APPLICABLE_RULES_DESC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAppRulesDesc();
					i++;
				}
			
				if(canonicalObj.getSendingInstId() !=null)
				{
					columns.append("MSGS_SENDING_INSTID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSendingInstId();
					i++;
				}
				if(canonicalObj.getSendingInstAcct() !=null)
				{
					columns.append("MSGS_SENDING_INSTAC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSendingInstAcct();
					i++;
				}
				if(canonicalObj.getSendingInst() !=null)
				{
					columns.append("MSGS_SENDING_INSTCODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSendingInst();
					i++;
				}
				if(canonicalObj.getSendingInstLoc() !=null)
				{
					columns.append("MSGS_SENDING_INSTLOC,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSendingInstLoc();
					i++;
				}
				if(canonicalObj.getSendingInstNameAdd() !=null)
				{
					columns.append("MSGS_SENDING_INSTNAMEADD,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getSendingInstNameAdd();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitutionPID() !=null)
				{
					columns.append("MSGS_BENFINST_PID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitutionPID();
					i++;
				}
				//Start :: Added for BG COV messages
				if(canonicalObj.getBgFormNumber() !=null)
				{
					columns.append("BG_FORM_NUMBER,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgFormNumber();
					i++;
				}
				if(canonicalObj.getBgType() !=null)
				{
					columns.append("BG_TYPE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgType();
					i++;
				}
				if(canonicalObj.getBgAmt() !=null)
				{
					columns.append("BG_AMOUNT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgAmt();
					i++;
				}
				if(canonicalObj.getBgCurrency() !=null)
				{
					columns.append("BG_CURR_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgCurrency();
					i++;
				}
				if(canonicalObj.getBgFromDate() !=null)
				{
					columns.append("BG_GUARANTEE_FROM_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgFromDate();
					i++;
				}
				if(canonicalObj.getBgToDate() !=null)
				{
					columns.append("BG_GUARANTEE_TO_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgToDate();
					i++;
				}
				if(canonicalObj.getBgEffectiveDate() !=null)
				{
					columns.append("BG_EFFECTIVE_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgEffectiveDate();
					i++;
				}
				if(canonicalObj.getBgLodgementEndDate() !=null)
				{
					columns.append("BG_LODGEMENT_END_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgLodgementEndDate();
					i++;
				}
				if(canonicalObj.getBgLodgementPlace() !=null)
				{
					columns.append("BG_LODGEMENT_PLACE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgLodgementPlace();
					i++;
				}
				if(canonicalObj.getIssuingBankCode() !=null)
				{
					columns.append("ISSUINGBANK_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIssuingBankCode();
					i++;
				}
				if(canonicalObj.getIssueingBankAddr() !=null)
				{
					columns.append("ISSUINGBANK_ADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getIssueingBankAddr();
					i++;
				}
				if(canonicalObj.getBgApplicentName() !=null)
				{
					columns.append("BG_APPLICENT_NAME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgApplicentName();
					i++;
				}
				if(canonicalObj.getBgBenificiaryName() !=null)
				{
					columns.append("BG_BENIFICIARY_NAME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgBenificiaryName();
					i++;
				}
				if(canonicalObj.getBgBenificiaryBankCode() !=null)
				{
					columns.append("BENIFICIARY_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgBenificiaryBankCode();
					i++;
				}
				if(canonicalObj.getBgBenificiaryBankAddr() !=null)
				{
					columns.append("BENIFICIARY_ADDR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgBenificiaryBankAddr();
					i++;
				}
				if(canonicalObj.getBgPurpose() !=null)
				{
					columns.append("BG_GUARANTEE_PURPOSE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgPurpose();
					i++;
				}
				if(canonicalObj.getContractReference() !=null)
				{
					columns.append("BG_CONTRACT_REFERENCE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getContractReference();
					i++;
				}
				if(canonicalObj.getStampDutyPaid() !=null)
				{
					columns.append("BG_STAMP_DUTY_PAID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getStampDutyPaid();
					i++;
				}
				if(canonicalObj.getStampDutyNum() !=null)
				{
					columns.append("BG_STAMP_CERTIFICATE_NUM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getStampDutyNum();
					i++;
				}
				if(canonicalObj.getStampDutyDateTime() !=null)
				{
					columns.append("BG_STAMP_CERT_DATE_TIME,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getStampDutyDateTime();
					i++;
				}
				if(canonicalObj.getBgPaidAmt() !=null)
				{
					columns.append("BG_AMOUNT_PAID,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgPaidAmt();
					i++;
				}
				if(canonicalObj.getBgStateCode() !=null)
				{
					columns.append("BG_STATE_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgStateCode();
					i++;
				}
				if(canonicalObj.getBgArticleNum() !=null)
				{
					columns.append("BG_ARTICLE_NUM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgArticleNum();
					i++;
				}
				if(canonicalObj.getBgPaymentDate() !=null)
				{
					columns.append("BG_PAYMENT_DATE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgPaymentDate();
					i++;
				}
				if(canonicalObj.getBgPaymentPlace() !=null)
				{
					columns.append("BG_PAYMENT_PLACE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgPaymentPlace();
					i++;
				}
				if(canonicalObj.getBgDematForm() !=null)
				{
					columns.append("BG_DEMAT_FORM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgDematForm();
					i++;
				}
				if(canonicalObj.getBgCostodianProvider() !=null)
				{
					columns.append("BG_COSTODIAN_PROVIDER,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgCostodianProvider();
					i++;
				}
				if(canonicalObj.getBgDematAccNum() !=null)
				{
					columns.append("BG_DEMAT_ACC_NUM,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getBgDematAccNum();
					i++;
				}
				//End :: Added for BG COV messages
				//Start :: Added for 769 message enable
				if(canonicalObj.getLcAdditionalCurrency() !=null)
				{
					columns.append("LC_ADDITIONAL_CURR_CODE,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAdditionalCurrency();
					i++;
				}
				if(canonicalObj.getLcAdditionalAmt() !=null)
				{
					columns.append("LC_ADDITIONAL_AMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcAdditionalAmt();
					i++;
				}
				//End :: Added for 769 message enable
				//Start ::  Added for 730 message field 32D
			/*	if(canonicalObj.getMessageCurrency() !=null)
				{
					columns.append("MSGS_MESSAGECURR,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getMessageCurrency();
					i++;
				}
				if(canonicalObj.getLcChargesClaimed() !=null)
				{
					columns.append("MSGS_CHARGEAMT,");
					count.append("?,");
					valuesArray[i] = canonicalObj.getLcChargesClaimed();
					i++;
				}*/
				if(canonicalObj.getBeneficiaryInstitutionClrgCd() !=null)
				{
					columns.append("MSGS_BENFINST_BKCD,");//58A IFSC
					count.append("?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitutionClrgCd();
					i++;
				}
				//End :: Added for 730 message filed 32D
				
				
				
			//Fetch the final String removing the last extra , value
				String colVal = columns.toString().substring(0,columns.toString().length()-1);
				String countVal = count.toString().substring(0,count.toString().length()-1);
				//Add closing braces to final String
				colVal = colVal.concat(")");
				countVal = countVal.concat(")");
				
				//Constructing the final Query Value
				query = colVal + countVal;
				logger.info("Query Constructed : " + query);
				

				//to avoid null extra values and to avoid invalid column type errors
				//we need to have columns used in query and values in this array should be same in count
				Object[] actualArray = null;
				if(i < valuesArray.length)
				{
					actualArray = new Object[i];
					for(int j=0; j<actualArray.length; j++)
					{
						actualArray[j] = valuesArray[j];
					}
					//its not required then after so sending to garbage collection 
					valuesArray = null;
				}
				String dupCheck = "insert into TA_MSGS_DUPCHECK values (?,?,?,?,?)";
				String lcComm = "insert into TA_LC_COMMODITY(LC_COMMODITY,MSGS_MSGREF,LC_NUMBER) values(?,?,?)";
				try
				{			
					String dupVals = compose_dupVals(canonicalObj);
					if (dupVals != null)
					{
						jdbcTemplate.update(dupCheck, new Object[]{canonicalObj.getMsgRef(),canonicalObj.getTxnReference(), canonicalObj.getMsgDirection(), dupVals, canonicalObj.getSenderBank()});
					}
					if(canonicalObj.getLcArrCommodity()!=null && canonicalObj.getLcArrCommodity().length>0)
					{
						for(int j=0; j<canonicalObj.getLcArrCommodity().length; j++)
						{
							if(canonicalObj.getLcArrCommodity()[j]!=null && StringUtils.isNotBlank(canonicalObj.getLcArrCommodity()[j]) && StringUtils.isNotEmpty(canonicalObj.getLcArrCommodity()[j]));
								{
									jdbcTemplate.update(lcComm, new Object[]{canonicalObj.getLcArrCommodity()[j],canonicalObj.getMsgRef(),canonicalObj.getLcNo()});
								}
						}
					}
					else
					{
						logger.info("LC Commodity Array was null, no need for DB Insertion");
					}
					jdbcTemplate.update(query, actualArray); 
				}
				catch (Exception e)
				{
					logger.error("Exception Occured while inserting data in Transaction table, Hence Setting to Error Message",e);
			  		EventLogger.logEvent("NGPHSFMACT0013", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//SFMS message format parser error for payment QNG reference {msgRef} with irrecoverable error {msgErrorDesc}. Please refer error log for details.
	  				String error = e.getMessage();
	  				String errorCodes = canonicalObj.getMsgErrorCode();
	  				String final_err = error.substring(error.lastIndexOf("java.sql.SQLException:") + "java.sql.SQLException:".length()+1, error.length()); 
			  		if(errorCodes!=null && StringUtils.isNotBlank(errorCodes) && StringUtils.isNotEmpty(errorCodes))
			  		{
			  			if(errorCodes.endsWith(";"))
			  			{}
			  			else
			  			{
			  				errorCodes = errorCodes+";";
			  			}
			  			logger.info("The Error Codes that is being updated as Error Messages is : " + errorCodes + final_err);
			  			updateMsgStatusForRaw_Msgs(1, canonicalObj.getMsgRef(), canonicalObj.getMsgErrorCode() + final_err);
			  		}
			  		else
			  		{	
			  			logger.info("The Error Codes that is being updated as Error Messages is : " + final_err);
			  			updateMsgStatusForRaw_Msgs(1, canonicalObj.getMsgRef(), final_err);
			  		}
					throw new  Exception(e);
				}
			}
			else
			{
				logger.error("Canonical Object is null");
			}
			logger.info("Insert Parsed Message Ends");
		
}

	 public String compose_dupVals(NgphCanonical canonicalData)throws Exception
	 {
		 String dupVals = null;
		 Properties props = new Properties();
			try 
			{
				final String propName = "System.properties"; 
				final String dupFields = "_DUPFIELDS"; 
				
				props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
				String chnl_dupFlds = props.getProperty(canonicalData.getMsgChnlType()+ dupFields);
				
				if (chnl_dupFlds != null)
				{
					String dupValArr[] = chnl_dupFlds.split(";");
					Class c = Class.forName(canonicalData.getClass().getName());
					Method  method = null;
					for(int i=0;i<dupValArr.length;i++)
					{
						method = c.getDeclaredMethod ("get" + dupValArr[i], new Class[] {});
						Object obj = method.invoke(canonicalData,new Object[]{});
						if(obj!=null)
						{
							if(obj instanceof BigDecimal)
							{
								obj = DateHelper.bigDecimalFormatter(obj);	
							}
							else if(obj instanceof Timestamp)
							{
								obj = DateHelper.timeStampFormatter(obj);	
							}
	
							if(StringUtils.isNotBlank(dupVals) && StringUtils.isNotEmpty(dupVals))
							{
								dupVals = dupVals + obj;
							}
							else
							{
								dupVals = obj.toString();
							}
						}
							
						else
						{
							logger.info("Null value for : " + dupValArr[i]);
						}
					}
				}
			} 
			catch (IOException e) 
			{
				logger.error(e, e);
				throw new  Exception(e);
			}
			catch (Exception e) {
				logger.error(e, e);
				throw new  Exception(e);
			}
		 return dupVals;
		 
		 
	 }
	 public String validateTa_Msg_Tx(String relRef, String msgChnlType, String msgCurrency , BigDecimal msgAmount, Timestamp msgValueDate)throws Exception
	 {
		 String result = null;
		 try
		 {
			
			 //String Query = "select MSGS_MSGREF from ta_messages_tx where MSGS_PMTID_INSTRID='" + relRef +"' and MSGS_SRC_MSGTYPE = 'MT' and MSGS_SRC_MSGSUBTYPE='103' and MSGS_MSGCHNLTYPE='" + msgChnlType + "' and MSGS_INTRBKSTTLMCCY='" + msgCurrency + "' and MSGS_INTRBKSTTLMDT  like (to_date('" + msgValueDate +"','YYYY-MM-dd hh24:mi:ss')) and MSGS_INTRBKSTTLMAMT="+msgAmount+"";

			 String Query = "select MSGS_MSGREF from ta_messages_tx where MSGS_PMTID_INSTRID=? and MSGS_SRC_MSGTYPE = ? and MSGS_SRC_MSGSUBTYPE=? and MSGS_MSGCHNLTYPE=? and MSGS_INTRBKSTTLMCCY=? and MSGS_INTRBKSTTLMDT=? and MSGS_INTRBKSTTLMAMT=?";

			 String dataVal = jdbcTemplate.queryForObject(Query, new Object[]{relRef,"MT","103",msgChnlType,msgCurrency,msgValueDate,msgAmount}, String.class);

			 logger.info("MsgRef of 103--> " + dataVal);
			 result = dataVal;
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) {
			 logger.error(e,e);
		}
		 catch (Exception e)
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		 
		 return result;
	 }
	 
	 public void updateTa_Msg_TxforExcp(String msgRef, String msgSatus)throws Exception
	 {
		try
		{
            SqlRowSet srs = null;
			String currSts = null;;
			String prevSts = null;
			 
			 String FetchMsgStatus="select MSGS_MSGSTS,MSGS_PREVMSGSTS from ta_messages_tx where MSGS_MSGREF='" + msgRef +"'";
			 srs = jdbcTemplate.queryForRowSet(FetchMsgStatus);
	
				while (srs.next()) 
				{
					currSts = srs.getString("MSGS_MSGSTS");
					prevSts = srs.getString("MSGS_PREVMSGSTS");
				}
				//logger.info(currSts);
				//logger.info(prevSts);
				
			// FIXME Problem inserting in null values
			if(currSts!=null)
			{
				String updatePrevMsgStatus = "update ta_messages_tx set MSGS_PREVMSGSTS = '"+ currSts + "', MSGS_LASTMODIFIEDTIME= sysdate where MSGS_MSGREF='" + msgRef +"'";
	            int prevMsgUpdateResult = jdbcTemplate.update(updatePrevMsgStatus);
	            //System.out.println(prevMsgUpdateResult);
			}
			 
			String updateMsgSts = "update ta_messages_tx set MSGS_MSGSTS = '" + msgSatus + "', MSGS_LASTMODIFIEDTIME= sysdate where MSGS_MSGREF='" + msgRef + "'";
	        int updateMsgStsResult = jdbcTemplate.update(updateMsgSts);
	        //System.out.println(updateMsgStsResult);
			 
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new  Exception(e);
		}
	 }
	 
	 public void updateTa_Msg_tx(String msgRef910, String msgRefof103, String status)throws Exception
	 {
		 try
		 {
			    SqlRowSet srs = null;
				String currSts103 = null;;
				String prevSts103 = null;

				// Fetch status for 103
				 String FetchMsgStatus103="select MSGS_MSGSTS,MSGS_PREVMSGSTS from ta_messages_tx where MSGS_MSGREF='" + msgRefof103 +"'";
				 srs = jdbcTemplate.queryForRowSet(FetchMsgStatus103);

					while (srs.next()) 
					{
						currSts103 = srs.getString("MSGS_MSGSTS");
						prevSts103 = srs.getString("MSGS_PREVMSGSTS");
					}
					//System.out.println(currSts103);
					//System.out.println(prevSts103);
					
				// FIXME Problem inserting in null values
				if(currSts103!=null)
				{
					String updatePrevMsgStatus = "update ta_messages_tx set MSGS_PREVMSGSTS = '"+ currSts103 + "' , MSGS_LASTMODIFIEDTIME= sysdate where MSGS_MSGREF='" + msgRefof103 +"'";
		            int prevMsgUpdateResult = jdbcTemplate.update(updatePrevMsgStatus);
		            //System.out.println(prevMsgUpdateResult);
				}
				
				String currSts910 = null;;
				String prevSts910 = null;
				
				// Fetch status for 910
				 String FetchMsgStatus910="select MSGS_MSGSTS,MSGS_PREVMSGSTS from ta_messages_tx where MSGS_MSGREF='" + msgRef910 +"'";
				 srs = jdbcTemplate.queryForRowSet(FetchMsgStatus910);
					while (srs.next()) 
					{
						currSts910 = srs.getString("MSGS_MSGSTS");
						prevSts910 = srs.getString("MSGS_PREVMSGSTS");
					}
					//System.out.println(currSts910);
					//System.out.println(prevSts910);
					
				// FIXME Problem inserting in null values
				if(currSts910!=null)
				{
					String updatePrevMsgStatus = "update ta_messages_tx set MSGS_PREVMSGSTS = '"+ currSts910 + "', MSGS_LASTMODIFIEDTIME= sysdate where MSGS_MSGREF='" + msgRef910 +"'";
		            int prevMsgUpdateResult = jdbcTemplate.update(updatePrevMsgStatus);
		            //System.out.println(prevMsgUpdateResult);
				}
	
				
			// Updating the msgStatus for 103
			String updateMsgSts103 = "update ta_messages_tx set MSG_RELATED_REF = '" + msgRef910 + "', MSGS_MSGSTS = '" + status +"' ,MSGS_LASTMODIFIEDTIME= sysdate  where MSGS_MSGREF='" + msgRefof103 + "'";
	        int updateMsgStsResult103 = jdbcTemplate.update(updateMsgSts103);
	        //System.out.println(updateMsgStsResult103);
	        
	     // Updating the msgStatus for 910
			String updateMsgSts910 = "update ta_messages_tx set MSGS_MSGSTS = '" + status +"' ,MSGS_LASTMODIFIEDTIME= sysdate  where MSGS_MSGREF='" + msgRef910 + "'";
	        int updateMsgStsResult910 = jdbcTemplate.update(updateMsgSts910);
	        //System.out.println(updateMsgStsResult910);
	        
		 }catch (Exception e) 
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		}
	 }
	 
	 public boolean validateTa_Accounts(String beneficiaryCustAcct)throws Exception
	 {
		 boolean result = false;
		 
		 try
		 {
			 String Query = "select ACCT_NUM from accounts where ACCT_NUM='" + beneficiaryCustAcct+ "'";
			 String dataVal = jdbcTemplate.queryForObject(Query,String.class);
			 logger.info("Value for Related Ref in Db is : " + dataVal);
			 result=true;
		 } 
		 
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) {
			 logger.error(e,e);
		}
		 catch (Exception e) 
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		return result;
	 }

	 public String getCovGen()throws Exception
	 {
			String result=null;
			Clob clob = null;
			try
			{
				String bicQuery = "select init_value from initialisationm where init_entry = '202COVGEN' and init_branch = '47'";
				clob = jdbcTemplate.queryForObject(bicQuery, Clob.class);
				int clobLength = (int) clob.length();
				result = clob.getSubString(1, clobLength);
			}
			catch(EmptyResultDataAccessException e)
			{
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch(Exception e)
			{
				logger.error(e,e);
				throw new  Exception(e);
			}
			return result;
	 }
	 
	 public String getHostID()throws Exception
	 {
			String result=null;
			Clob clob = null;
			try
			{
				String bicQuery = "select init_value from initialisationm where init_entry = '202COVHOSTID' and init_branch = '47'";
				clob = jdbcTemplate.queryForObject(bicQuery, Clob.class);
				int clobLength = (int) clob.length();
				result = clob.getSubString(1, clobLength);
			}
			catch(EmptyResultDataAccessException e)
			{
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch(Exception e)
			{
				logger.error(e,e);
				throw new  Exception(e);
			}
			return result;
	 }
	 
	 public String isCurrSuptByAcct(String account)throws Exception
	 {
		 String result = null;
		 
		 String Query = "select ACCT_CCY from accounts where ACCT_NUM=?";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, new Object[] {account},String.class);
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 
		 catch (Exception e) 
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		 return result;
	 }
	 
	 public int isPtyCorrspndtByAcct(String account)throws Exception
	 {
		 int result=0;
		 String Query="select PARTY_ISCORRESPONDENT from ta_parties where party_bic=(select ACCT_PARTIES_CODE from accounts where acct_num=?)";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, new Object[]{account}, int.class);
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) {
			 logger.error(e,e);
		}
		 catch (Exception e) 
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		 return result;
	 }
	
	 public int isPtyCorrspndtByBIC(String bic)throws Exception
	 {
		 int result = 0;
		 String Query = "select PARTY_ISCORRESPONDENT from ta_parties where party_bic=?";
		 try
		 {
			int res = jdbcTemplate.queryForObject(Query, new Object[]{bic}, int.class); 
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) {
			 logger.error(e,e);
		}
		 catch (Exception e)
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		 return result;
	 }

	 public String isCurrSuptByBIC(String bic)throws Exception
	 {
		 String result = null;
		 String Query = "select ACCT_CCY from accounts where ACCT_PARTIES_CODE=?";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, new Object[]{bic}, String.class);
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) {
			 logger.error(e,e);
		}
		 catch (Exception e) 
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		return result;
		 
		 
	 }
	 
	 public int isPtyCorrspndtByNameAdd(String nameAdd)throws Exception
	 {
		 // FIXME It is assumed that there will be only 1 value retreived, not handled for multiple rows
		 int result = 0;
		 String query="select PARTY_ISCORRESPONDENT from ta_parties where concat(PARTY_NM,PARTY_ADDRREF) like '%" + nameAdd + "%'";
		 try
		 {
			int res = jdbcTemplate.queryForObject(query, int.class);
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) {
			 logger.error(e,e);
		}
		 catch (Exception e)
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		 return result;
	 }

	 public String isCurrSuptByNameAdd(String nameAdd)throws Exception
	 {
		 String result = null;
		 String Query = "select ACCT_CCY from accounts where ACCT_PARTIES_CODE=(select PARTY_BIC from ta_parties where concat(PARTY_NM,PARTY_ADDRREF) like '%" + nameAdd + "%'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
		 }
		 catch (EmptyResultDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (IncorrectResultSizeDataAccessException e) {
			 logger.error(e,e);
		}
		 catch (Exception e) 
		 {
			 logger.error(e,e);
			 throw new  Exception(e);
		 }
		return result;
	 }

	 public String getSequenceNumber(String seqIdentifier)throws Exception
	 {
		 	int seq=0;
		 	int inc_Val =0;
			String query = "SELECT SEQ_SEQUENCE FROM TA_SEQUENCES WHERE SEQ_IDENTIFIER=?";
			
			try
			{
				 seq = jdbcTemplate.queryForInt(query, seqIdentifier);
				 inc_Val= seq+1;

				 System.out.println("The Database Sequence val : " + seq);
				 System.out.println("The Incremented Sequence val : " + inc_Val);
				 
				 updateSequenceNumber(inc_Val);
				 
			}
			catch (EmptyResultDataAccessException e) 
			{
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}
			catch (Exception e) {
				logger.error(e,e);
				throw new  Exception(e);
			}
			return inc_Val+"";
	 }

	 private void updateSequenceNumber(int newSeqNum)throws Exception
		{
		 try
		 {
			String query = "UPDATE TA_SEQUENCES SET SEQ_SEQUENCE=? WHERE SEQ_IDENTIFIER=?";
			jdbcTemplate.update(query, new Object[] {newSeqNum, NgphEsbConstants.SEQUENCE_KEY_TXN});
		 }
		 
		 catch (Exception e) 
		 {
			 logger.error(e,e);	
			 throw new  Exception(e);
		 }
		}

	 /**
		* This method is used to get all the data from TA_MSGS_INFORMATION table
		* @return InfoCanonical infoCanonocal
		* @param msgmur
		*/
		public InfoCanonical getInfoCanonicalFromTaMsgInformation(String msgmur)throws Exception 
		{
			InfoCanonical infoCanonocal = null;
			try
			{
				String query = "select * from TA_MSGS_INFORMATION where MSGS_MUR = ?";
				infoCanonocal = (InfoCanonical)jdbcTemplate.queryForObject(query, new Object[]{msgmur}, new InfoCanonicalRowMapper());
			}
			catch (EmptyResultDataAccessException e) {
				logger.error("::getInfoCanonicalFromTaMsgInformation ->  " + e.toString());
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error("::getInfoCanonicalFromTaMsgInformation ->  " + e.toString());
			}
			catch (Exception e) {
				logger.error("::getInfoCanonicalFromTaMsgInformation ->  " + e.toString());
				throw new  Exception(e);
			}
			return infoCanonocal;
		}
		
		/**
		* This method is used to get all the data from TA_MSGS_INFORMATION table
		* @return InfoCanonical infoCanonocal
		* @param seqNo
		*/
		public InfoCanonical getInfoCanonicalFromTaMsgInformationForSeq(String seqNo)throws Exception 
		{
			InfoCanonical infoCanonical = null;
			try
			{
				String query = "select * from TA_MSGS_INFORMATION where MSGS_SEQNO = ? AND MSGS_MSGSTS <> '99'";
				infoCanonical = (InfoCanonical)jdbcTemplate.queryForObject(query, new Object[]{seqNo}, new InfoCanonicalRowMapper());
			}
			catch (EmptyResultDataAccessException e) {
				logger.error("::getInfoCanonicalFromTaMsgInformationForSeq ->  " + e.toString());
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error("::getInfoCanonicalFromTaMsgInformationForSeq ->  " + e.toString());
			}
			catch (Exception e) {
				logger.error("::getInfoCanonicalFromTaMsgInformationForSeq ->  " + e.toString());
				throw new  Exception(e);
			}
			return infoCanonical;
		}
		
		/**
		* This method is used to get srcMsgtype data from TA_MESSAGES_TX table
		* @return String result
		* @param txnref, msgDirection
		*/
		public String getSRCMsgType(String txnref, String msgDirection)throws Exception
		{
			logger.info("GetSRCMsgType method() starts");

			 String result = "";
			 String query="select MSGS_SRC_MSGTYPE from TA_MESSAGES_TX where LC_NUMBER=? AND MSGS_DIRECTION=?";
			 try
			 {
				result = jdbcTemplate.queryForObject(query, new Object[]{txnref, msgDirection}, String.class);
			 }
			 catch (EmptyResultDataAccessException e) 
			 {
				 logger.error(e,e);
			 }
			 catch (Exception e)
			 {
				 logger.error(e,e);
				 throw new Exception(e);
			 }
			 logger.info("GetSRCMsgType method() ENDS");
			 return result;
		}

		public InfoCanonical getInfoCanonicalFromMessageTxforMsgRef(String msgRef)
	    throws Exception
	  {
	    InfoCanonical infoCan = new InfoCanonical();
	    try
	    {
	      String query = "select * from TA_MSGS_INFORMATION where MSGS_MSGREF = ?";
	      infoCan = (InfoCanonical)this.jdbcTemplate.queryForObject(query, new Object[] { msgRef }, new InfoCanonicalRowMapper());
	    }
	    catch (EmptyResultDataAccessException e)
	    {
	      logger.warn("No data in TA_MSGS_INFORMATION for MsgRef : " + msgRef);
	    }
	    catch (IncorrectResultSizeDataAccessException e)
	    {
	      logger.warn("No data in TA_MSGS_INFORMATION for MsgRef : " + msgRef);
	    }
	    catch (Exception e)
	    {
	      logger.error(e, e);
	      throw new Exception(e);
	    }
	    return infoCan;
	  }
		
		 public String getNoofProcIteration()	throws Exception
		 {
				String result=null;
				Clob clob = null;
				try
				{
					String bicQuery = "select init_value from initialisationm where init_entry = 'MAXNOOFPROCITERAION'";
					clob = jdbcTemplate.queryForObject(bicQuery, Clob.class);
					int clobLength = (int) clob.length();
					result = clob.getSubString(1, clobLength);
				}
				catch(EmptyResultDataAccessException ex)
				{
					logger.error(ex,ex);
				}
				catch (IncorrectResultSizeDataAccessException e) {
					logger.error(e,e);
				}

				catch(Exception e)
				{
					logger.error(e,e);
					throw new  Exception(e);
				}
				return result;
			}
}
