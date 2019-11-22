package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.AuthMsgPolled;

public class AuthMsgPollRowMapper implements RowMapper<AuthMsgPolled>{

	public AuthMsgPolled mapRow(ResultSet resultSet, int arg1) throws SQLException {
	
		AuthMsgPolled msgsPolled = new AuthMsgPolled();
	
		/**
		 * MSGS_HOSTID, 
		 * MSGS_MSGCHNLTYPE,
		 * MSGS_SRC_MSGTYPE,
		 * MSGS_DIRECTION,
		 * MSGS_INTRBKSTTLMCCY,
		 * MSGS_INTRBKSTTLMAMT
		 * MSGS_BRANCH
		 * MSGS_DEPT
		 * MSGS_SRC_MSGSUBTYPE
		 */
		msgsPolled.setHosiId(resultSet.getString("MSGS_HOSTID"));
		msgsPolled.setChannelType(resultSet.getString("MSGS_MSGCHNLTYPE"));
		msgsPolled.setMsgType(resultSet.getString("MSGS_SRC_MSGTYPE"));
		msgsPolled.setDirection(resultSet.getString("MSGS_DIRECTION"));
		msgsPolled.setCurrency(resultSet.getString("MSGS_INTRBKSTTLMCCY"));
		msgsPolled.setBranch(resultSet.getString("MSGS_BRANCH"));
		msgsPolled.setDept(resultSet.getString("MSGS_DEPT"));
		msgsPolled.setSubMSgType(resultSet.getString("MSGS_SRC_MSGSUBTYPE"));
		msgsPolled.setAmt(resultSet.getBigDecimal("MSGS_INTRBKSTTLMAMT"));

		return msgsPolled;
	}
	
	



}
