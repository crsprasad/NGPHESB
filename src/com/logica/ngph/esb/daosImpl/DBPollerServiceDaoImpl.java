package com.logica.ngph.esb.daosImpl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;



import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.MsgsPolled;

import com.logica.ngph.esb.daos.DBPollerServiceDao;
import com.logica.ngph.esb.rowmappers.CanonicalRowMapper;
import com.logica.ngph.esb.rowmappers.MsgsPollRowMapper;



public class DBPollerServiceDaoImpl implements DBPollerServiceDao{
	private JdbcTemplate jdbcTemplate;

	static Logger logger = Logger.getLogger(DBPollerServiceDaoImpl.class);
	/**
	* This method is used to get the data from TA_MSGSPOLLED table
	* @return  List<MsgsPolled>
	*/
	public List<MsgsPolled> getMsgsPolled(){
	
		String query = "select  P.MSGS_MSGREF, P.LASTORCHSERVICEIDCALLED , T.MSGS_INTRBKSTTLMDT , T.MSGS_MSGSTS , T.MSGS_PREVMSGSTS,T.MSGS_BRANCH" +
				" from TA_MSGSPOLLED P   inner join TA_MESSAGES_TX T on  P.MSGS_MSGREF = T.MSGS_MSGREF and P.MARKED_OUT_TIME is null and P.POLL_STATUS is null "+ 

		"and T.MSGS_MSGSTS = 3";
		
		
		
		/*String query ="SELECT MSGS_MSGREF, LASTORCHSERVICEIDCALLED " +
				"from TA_MSGSPOLLED where  MARKED_OUT_TIME is null and (POLL_STATUS is  null or POLL_STATUS != 'I')"+
		"order by IN_TIME";*/
		
		
		
		List<MsgsPolled> msgsPolled  = null; 
		
		try{	
			msgsPolled = jdbcTemplate.query(query,
				new MsgsPollRowMapper());
		}catch(EmptyResultDataAccessException emptyResultDataAccessException)
		{
			logger.info(emptyResultDataAccessException);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return msgsPolled;
	}
	/**
	* This method is used to get the data from TA_MESSAGES_TX table
	* @param String msgRef
	* @return  PollerMessage
	*/
	
	/*public PollerMessage getMessage(String msgRef){
		
		String query ="SELECT MSGS_BRANCH ,MSGS_INTRBKSTTLMDT ,MSGS_MSGSTS,MSGS_PREVMSGSTS  from TA_MESSAGES_TX where MSGS_MSGREF =?";
		
		PollerMessage polMessage = null;
		try{	
			 polMessage = (PollerMessage)jdbcTemplate.queryForObject(query, new MsgsRowMapper(), msgRef);
		}catch(EmptyResultDataAccessException emptyResultDataAccessException)
		{
		logger.info(emptyResultDataAccessException);
		}
		return polMessage;
	}*/
	
	
	/**
	* This method is used to get the BUSDAY_DATE from ta_businessdaym table
	* @param String branchName
	* @return  String businessDate
	*/
	public String getBusinessDate(String branchName){
		
		String query = "select BUSDAY_DATE from ta_businessdaym where  BUSDAY_BRANCH = ?";
		
		String businessDate = null;
		try{ 
			businessDate = jdbcTemplate.queryForObject(query, new Object[] {branchName}, String.class);
		}catch(EmptyResultDataAccessException emptyResultDataAccessException)
		{
			logger.info(emptyResultDataAccessException);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return businessDate;
	}
	/**
	* This method is used to update the data  from TA_MESSAGES_TX table
	* @param DateTest businessDate
	* @param String prevMsgStatus
	* @param String msgRef
	* @return void
	*/
	public void performDBpoll(Date businessDate,String prevMsgStatus,String msgRef){
		
		try
		{
		java.sql.Date sqlDate = new java.sql.Date(businessDate.getTime());
		
		String query = "update TA_MESSAGES_TX set MSGS_INTRBKSTTLMDT = ? ,MSGS_LASTMODIFIEDTIME = sysdate ,MSGS_PREVMSGSTS = 'WAREHOUSED',MSGS_MSGSTS=? where MSGS_MSGREF = ?";
			
		jdbcTemplate.update(query,  new Object[] {sqlDate,prevMsgStatus,msgRef})	;
		String dbPollerQry = "update ta_msgspolled set MARKED_OUT_TIME = sysdate ,POLL_STATUS='C' where MSGS_MSGREF = ?";
		jdbcTemplate.update(dbPollerQry,  new Object[] {msgRef})	;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	* This method is used to insert the data into TA_EVENT_MAST table
	* @return void
	*/
	public void insertEventMaster(String eventId){
		
		try
		{
		//String eventId = "NGPHEV006";
		String eventDesc = "Msg Ref {0} from valueDate {1} changed to current value date {2} for processing";
		String query = "insert into TA_EVENT_MAST (EVENTM_EVENTID,EVENTM_DESC,"
			+"EVENTM_ALERTABLE,EVENTM_SEVERITY) values (?,?,?,?)";
		jdbcTemplate.update(query, new Object[] {eventId,eventDesc,"0","W"});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	/**
	* This method is used to get all the data from TA_MESSAGES_TX table
	* @return NgphCanonical ngphCanonical
	*/
	public NgphCanonical getCanonicalFromMessagesTx(String msgRef) {
		NgphCanonical ngphCanonical = null;
		try
		{
		String query = "select * from TA_MESSAGES_TX where MSGS_MSGREF = ?";
		ngphCanonical = (NgphCanonical)jdbcTemplate.queryForObject(query, new CanonicalRowMapper(), msgRef);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return ngphCanonical;
	}
	/*public void updatePollStatus(String pollerStatus,String msgRef) {
		String dbPollerQry = "update ta_msgspolled set POLL_STATUS = ? where MSGS_MSGREF = ?";
		jdbcTemplate.update(dbPollerQry,  new Object[] {pollerStatus,msgRef})	;
		
	}*/



	
	
	
	
	
}
