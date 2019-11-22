package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.DbPoller;

public class PollerRowMapper implements RowMapper<DbPoller>{

	public DbPoller mapRow(ResultSet rs, int arg1) throws SQLException 
	{
		DbPoller poller = new DbPoller();
		
		poller.setInTime(rs.getTimestamp("IN_TIME"));
		poller.setLastOrchSvIdCalled(rs.getString("LASTORCHSERVICEIDCALLED"));
		poller.setMarkedOutTime(rs.getTimestamp("MARKED_OUT_TIME"));
		poller.setMsgRef(rs.getString("MSGS_MSGREF"));
		poller.setPollReason(rs.getString("POLL_REASON"));
		poller.setPollStatus(rs.getString("POLL_STATUS"));
		
		return poller;
	}

}
