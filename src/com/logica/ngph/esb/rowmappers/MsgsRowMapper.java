package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;


import com.logica.ngph.esb.Dtos.PollerMessage;

public class MsgsRowMapper implements RowMapper<PollerMessage>{
	
	public PollerMessage mapRow(ResultSet resultSet, int arg1) throws SQLException {
		PollerMessage pollerMessage = new PollerMessage();
		pollerMessage.setBranchName(resultSet.getString("MSGS_BRANCH"));
		
		pollerMessage.setMsgValueDate(resultSet.getString("MSGS_INTRBKSTTLMDT"));
		
		pollerMessage.setMsgStatus(resultSet.getString("MSGS_MSGSTS"));
		pollerMessage.setPrevMsgStatus(resultSet.getString("MSGS_PREVMSGSTS"));
		
		return pollerMessage;
	}
	
	
}
