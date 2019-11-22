package com.logica.ngph.esb.daosImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.logica.ngph.esb.AuditServiceClient;
import com.logica.ngph.esb.Dtos.AuthMsgPolled;
import com.logica.ngph.esb.Dtos.AuthSupBean;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.daos.AuthStpServiceDao;
import com.logica.ngph.esb.rowmappers.AuthMsgPollRowMapper;
import com.logica.ngph.esb.services.AuthStpService;

public class AuthStpServiceDaoImpl implements AuthStpServiceDao {

	private JdbcTemplate jdbcTemplate;
	static Logger logger = Logger.getLogger(AuthStpServiceDaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<AuthMsgPolled> getMsgsPolled(String key)throws Exception {
		List<AuthMsgPolled> polledVals = null;

		String query = "select MSGS_HOSTID, MSGS_MSGCHNLTYPE,MSGS_SRC_MSGTYPE,MSGS_DIRECTION,MSGS_INTRBKSTTLMCCY,MSGS_INTRBKSTTLMAMT,MSGS_BRANCH,MSGS_DEPT,MSGS_SRC_MSGSUBTYPE"
				+ " from ta_messages_tx where msgs_msgref = '" + key + "'";

		try {
			polledVals = jdbcTemplate.query(query, new AuthMsgPollRowMapper());
			logger.info("Values Fetched from DB from message_tx");
		} 
		catch (EmptyResultDataAccessException ex) {
			logger.error(ex, ex);
		}catch (Exception e) {
			logger.error(e, e);
			throw new Exception(e);
		}
		return polledVals;
	}

	public Map<String, Object> getGroupInfo(List<AuthMsgPolled> vals, String key)throws Exception {
		String hosiId = null;
		String channelType = null;
		String msgType = null;
		String direction = null;
		String currency = null;
		String branch = null;
		String dept = null;
		String subMsgType = null;
		BigDecimal amt = new BigDecimal(0);

		Map<String, Object> result = new TreeMap<String, Object>();
		SqlRowSet srs = null;
		boolean check = false;

		for (int i = 0; i < vals.size(); i++) {

			AuthMsgPolled authMsgP = (AuthMsgPolled) vals.get(i);

			hosiId = authMsgP.getHosiId();
			channelType = authMsgP.getChannelType();
			msgType = authMsgP.getMsgType();
			direction = authMsgP.getDirection();
			currency = authMsgP.getCurrency();
			branch = authMsgP.getBranch();
			dept = authMsgP.getDept();
			subMsgType = authMsgP.getSubMSgType();
			amt = authMsgP.getAmt();
		}
/*		System.out.println(hosiId);
		System.out.println(channelType);
		System.out.println(msgType);
		System.out.println(direction);
		System.out.println(currency);
		System.out.println(branch);
		System.out.println(dept);
		System.out.println(subMsgType);
		System.out.println(amt);
*/
		try {
			String Query = "select GROUPSEQUENCE,GROUPID from ta_authSchemam"
					+ " where CHNLTYPE='" + channelType + "' and MSGTYPE='"
					+ msgType + "' and DIRECTION='" + direction
					+ "' and HOSTID='" + hosiId + "' and CURRENCY='" + currency
					+ "'" + " and BRANCH='" + branch 
					+ "' and SUBMSGTYPE='" + subMsgType + "' and from_amount<="
					+ amt + " and to_amount>=" + amt
					+ " order by GROUPSEQUENCE";

			srs = jdbcTemplate.queryForRowSet(Query);

			while (srs.next()) {
				check = true;
				String objKey = srs.getString("GROUPSEQUENCE");
				String val = srs.getString("GROUPID");
				// System.out.println("Key-->"+ objKey + "\t Value--->" + val);
				// System.out.println("111111111111111111111");
				result.put(objKey, val);
			}

			// If we dont get values for all the param match
			if (check == false) {
				/*
				 * String nextQuery =
				 * "select GROUPSEQUENCE,GROUPID from ta_authSchemam"+
				 * " where MSGTYPE='"+msgType+"' and SUBMSGTYPE='"+
				 * subMsgType+"' order by GROUPSEQUENCE";
				 */
				String nextQuery = "select GROUPSEQUENCE,GROUPID from ta_authSchemam"
						+ " where CHNLTYPE is null and MSGTYPE='"
						+ msgType
						+ "' and DIRECTION is null and HOSTID is null and CURRENCY is null"
						+ " and BRANCH is null and SUBMSGTYPE='"
						+ subMsgType
						+ "' and from_amount is null and to_amount is null order by GROUPSEQUENCE";

				srs = jdbcTemplate.queryForRowSet(nextQuery);

				while (srs.next()) {
					check = true;
					String objKey = srs.getString("GROUPSEQUENCE");
					String val = srs.getString("GROUPID");
					// System.out.println("Key-->"+ objKey + "\t Value--->" +
					// val);
					// System.out.println("2222222222222222222222222");
					result.put(objKey, val);
				}

				// if based on msgtype and submes type also we did not get any
				// supervisor
				if (check == false) {
					/*
					 * Exit the program and log a event in the events table Here
					 * Kesavan service will be invoked for logging the event
					 * This code will add the message into the Audit Message
					 * Queue.
					 */
					// System.out.println("3333333333333333333333333333");
					AuditServiceClient auditServiceClient = new AuditServiceClient();
					EventAudit audit = new EventAudit();

					audit.setAuditEventId("NGPHAUTHEV001");
					String[] extraInformation = {key };
					audit.setExtraInformation(extraInformation);
					audit.setAuditMessageRef(key);
					audit.setAuditSource(AuthStpService.class.toString());
					audit.setAuditBranch(branch);
					audit.setAuditDept(dept);

					auditServiceClient.dbPollerQueueCall(null, "AUDIT", audit);

					return result;
				}

				// Here we got the values based on the msgtype and submes type
				// i.e default group
				else {
					return result;
				}
			}

			// we got the group values on a first fetch based on the parameters
			else {
				return result;
			}

		} 
		catch (EmptyResultDataAccessException ex) {
			logger.error(ex, ex);
			return result;
		}
		catch (Exception e) {
			logger.error(e, e);
			return result;
			
		}
	}

	public ArrayList<Object> getSupInfo(Map<String, Object> hm)throws Exception
	{
		ArrayList<Object> al = new ArrayList<Object>();

		try {
			StringBuilder sb = new StringBuilder();
			SqlRowSet srs = null;

			Iterator it = hm.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				Object val = hm.get(key);
				sb.append(val.toString() + ",");
			}
			String inClauseVal = sb.substring(0, sb.length() - 1).toString();
			//System.out.println(inClauseVal);

			String Query = "select GROUPID,GROUPNAME,SUPID,SUPSEQ,ISMANDATORY from ta_authgrpm"
					+ " where GROUPID in(" + inClauseVal + ") order by GROUPID";

			srs = jdbcTemplate.queryForRowSet(Query);
			//System.out.println("GroupID" + "\t" + "GroupName" + "\t" + "SupID"	+ "\t" + "SupSeq" + "\t" + "IsMan");
			
			while (srs.next()) {

				String groupID = srs.getString("GROUPID");
				String groupName = srs.getString("GROUPNAME");
				String supID = srs.getString("SUPID");
				String supSeq = srs.getString("SUPSEQ");
				String isMan = srs.getString("ISMANDATORY");

				//System.out.println(groupID + "\t" + groupName + "\t" + supID+ "\t" + supSeq + "\t" + isMan);
				
				AuthSupBean beanObj = new AuthSupBean(groupID,groupName,supID,supSeq,isMan);
				
				al.add(beanObj);
			}
		} 
		catch (EmptyResultDataAccessException ex) {
			logger.error(ex, ex);
		}catch (Exception e) {
			logger.error(e, e);
			throw new Exception(e);
		}
		return al;
	}
	
	public void insertData(List<Object> sortedVals, Map<String, Object> GroupInfo, String key,  List<AuthMsgPolled> vals)throws Exception
	{
		try
		{
		String branch =null;
		String dept =null;
		String hostId =null;
		String channelType =null;
		String msgType =null;
		String subMsgType =null;
		String direction =null;
		String curency =null;
		BigDecimal amount =null;
		int authCycle=1;
		String temp = "00000"+ authCycle;
		String authRef=key +temp.substring(temp.length()-6, temp.length());
		
		// Query for the Respective tables
		String statusT = null;
		String statusD = null;
		
		// related to ta_authgrpm table
		String groupID = null;
		String supId=null;
		String supSeq=null;
		String isMan=null;
		
		//related to ta_authSchemam table
		String grpId =null;
		String grpSeq=null;
		
		
		for(int i=0;i<vals.size();i++)
		{
			AuthMsgPolled obj = (AuthMsgPolled) vals.get(i);
				branch = obj.getBranch();
				dept = obj.getDept();
				msgType = obj.getMsgType();
				subMsgType = obj.getSubMSgType();
				hostId =obj.getHosiId();
				channelType =obj.getChannelType();
				direction =obj.getDirection();
				curency =obj.getCurrency();
				amount =obj.getAmt();
				
/*				System.out.println("branch" + branch);
				System.out.println("dept" + dept);
				System.out.println("msgType" + msgType);
				System.out.println("subMsgType" + subMsgType);
				System.out.println("hostId" + hostId);
				System.out.println("channelType" + channelType);
				System.out.println("direction" + direction);
				System.out.println("curency" + curency);
				System.out.println("amount" + amount);
*/		}
		
		//System.out.println("Sorted Arraylist values------------>" + sortedVals.size() );
		
		String checkMsgRef = "select msgref from ta_authstatust where msgref='"+ key +"'";
		SqlRowSet srs = jdbcTemplate.queryForRowSet(checkMsgRef);
		
		if(srs.next())
		{
			logger.info("MsgRef Key exists");
			
			String lastAuthCycleValQuery="select max(AUTHCYCLE) from TA_AUTHSTATUST where msgref='"+key+"'";
			int lastAuthCycleVal=jdbcTemplate.queryForInt(lastAuthCycleValQuery);
			
			
			logger.info("lastAuthCycleVal--" + lastAuthCycleVal);
			
			authCycle = lastAuthCycleVal+1;
			temp = "00000"+ authCycle;
			authRef=key +temp.substring(temp.length()-6, temp.length());
			//authRef=key +"00000" +authCycle;
			
			logger.info("New authCycle-->" + authCycle);
			logger.info("New authRef-->" + authRef);
		}
		
		/**
		 * If the Authorization request is fresh.
		 */
		else
		{
			logger.info("MsgRef Key is not present in the Table, Hence this is a fresh Payment Authorization Request");
		}
		
		/**
		 * Populate TA_AUTHSTATUST Table
		 */
			statusT ="INSERT INTO TA_AUTHSTATUST (BRANCH, DEPT, MSGREF, AUTHREF, HOSTID, CHNLTYPE, MSGTYPE, DIRECTION, CURRENCY, AMOUNT, RECVDTIME, REJECTEDTIME,AUTHTIME,COMMENTS,SUBMSGTYPE,AUTHCYCLE)"+ 
        		" VALUES "+ 
        		"('"+branch+"', '"+dept+"', '"+key+"', '"+authRef+"', '"+hostId+"', '"+channelType+"', '"+msgType+"', '"+direction+"', '"+curency+"', "+amount+", sysdate,null, null, null,'"+subMsgType+"','"+authCycle+"')";
			
			int statusTResult = jdbcTemplate.update(statusT);
			
			logger.info("statusTResult----->"+ statusTResult);

		Iterator itr = GroupInfo.keySet().iterator();
		while(itr.hasNext())
		{
			grpSeq = itr.next().toString();
			grpId = GroupInfo.get(grpSeq).toString();
			
			/*System.out.println("grpSeq-->" +grpSeq);
			System.out.println("grpId-->" +grpId);*/
			
			for(int i=0;i<sortedVals.size();i++)
			{
				AuthSupBean obj = (AuthSupBean) sortedVals.get(i);
				
				groupID = obj.getGroupID();
				supId=obj.getSupId();
				supSeq=obj.getSupSeq();
				isMan=obj.getIsMan();
				
				/*
				 * This is first Sequence and needs special cases to populate data in table
				 */
				
			if(grpId.matches(groupID))
			{
				//System.out.println("Group Ids : " + grpId + "\t" + groupID);
				
				if(grpSeq.matches("1") && grpId.matches(groupID))
				{
					//System.out.println("Group Sequence is " + grpSeq + "\t loop value : "+ i);
					// If first group and is optional hence put AUTHSTATUS as P
					if(isMan.matches("0"))
					{
						statusD = "INSERT INTO TA_AUTHSTATUSD (BRANCH, DEPT, AUTHREF, GROUPID, GROUPSEQ, GROUP_AUTHSTATUS, SUPID, SUPSEQ, SUP_MANDATORY, AUTHSTATUS, RECVDTIME, AUTHTIME)"+ 
						" VALUES "+ 
						"('"+branch+"', '"+dept+"', '"+authRef+"', '"+grpId+"', '"+grpSeq+"', '"+"P"+"', '"+supId+"', '"+supSeq+"', '"+isMan+"', '"+"P"+"', sysdate, null)";
					}
					// If first group and mandatory as 1 hence put AUTHSTATUS as P
					else if(isMan.matches("1") && supSeq.matches("1"))
					{
						statusD = "INSERT INTO TA_AUTHSTATUSD (BRANCH, DEPT, AUTHREF, GROUPID, GROUPSEQ, GROUP_AUTHSTATUS, SUPID, SUPSEQ, SUP_MANDATORY, AUTHSTATUS, RECVDTIME, AUTHTIME)"+ 
						" VALUES "+ 
						"('"+branch+"', '"+dept+"', '"+authRef+"', '"+grpId+"', '"+grpSeq+"', '"+"P"+"', '"+supId+"', '"+supSeq+"', '"+isMan+"', '"+"P"+"', sysdate, null)";
					}
					// This is neither optional nor mandatory with SupSeq as 1, hence put the AUTHSTATUS as NULL
					else
					{
						statusD = "INSERT INTO TA_AUTHSTATUSD (BRANCH, DEPT, AUTHREF, GROUPID, GROUPSEQ, GROUP_AUTHSTATUS, SUPID, SUPSEQ, SUP_MANDATORY, AUTHSTATUS, RECVDTIME, AUTHTIME)"+ 
						" VALUES "+ 
						"('"+branch+"', '"+dept+"', '"+authRef+"', '"+grpId+"', '"+grpSeq+"', '"+"P"+"', '"+supId+"', '"+supSeq+"', '"+isMan+"', '"+"null"+"', sysdate, null)";
					}
				}
				
				// This is not the first group, hence all other groups will have AUTHSTATUS as NULL 
				if(!grpSeq.matches("1"))
				{
					//logger.info("Group Sequence is not the first group" + grpSeq);
					
					statusD = "INSERT INTO TA_AUTHSTATUSD (BRANCH, DEPT, AUTHREF, GROUPID, GROUPSEQ, GROUP_AUTHSTATUS, SUPID, SUPSEQ, SUP_MANDATORY, AUTHSTATUS, RECVDTIME, AUTHTIME)"+ 
					" VALUES "+ 
					"('"+branch+"', '"+dept+"', '"+authRef+"', '"+grpId+"', '"+grpSeq+"', '"+null+"', '"+supId+"', '"+supSeq+"', '"+isMan+"', '"+"null"+"', sysdate, null)";
				}
				
				int statusDResult = jdbcTemplate.update(statusD);
				logger.info("statusDResult-->" + statusDResult);
			}
			if(!grpId.matches(groupID))
			{
				//System.out.println("The group Id which does not matches is : " + groupID +"\t Loop Value : " + i);
				logger.warn("The sorted list does not contain the group Id as that group was having repeated supervisors");
			}
				
				
				
				}// For loop closed
			
			}// while loop closed
	}
	catch (EmptyResultDataAccessException ex) {
		logger.error(ex, ex);
	}
	catch (Exception e) {
		logger.error(e, e);
		throw new Exception(e);
	}
	}// method closed
}// class closed
		
	