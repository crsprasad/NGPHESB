package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.MsgsPolled;





public class MsgsPollRowMapper implements RowMapper<MsgsPolled>{
	
	public MsgsPolled mapRow(ResultSet resultSet, int arg1) throws SQLException {
	MsgsPolled msgsPolled = new MsgsPolled();
	
	msgsPolled.setMsgsRef(resultSet.getString("MSGS_MSGREF"));
	
	msgsPolled.setLastOrchServiceIdCalled(resultSet.getString("LASTORCHSERVICEIDCALLED"));
	msgsPolled.setSettlementDate(resultSet.getString("MSGS_INTRBKSTTLMDT"));
	msgsPolled.setMsgsStatus(resultSet.getString("MSGS_MSGSTS"));
	msgsPolled.setMsgsPrevStatus(resultSet.getString("MSGS_PREVMSGSTS"));
	msgsPolled.setBranchName(resultSet.getString("MSGS_BRANCH"));
	

		return msgsPolled;
	}

}
