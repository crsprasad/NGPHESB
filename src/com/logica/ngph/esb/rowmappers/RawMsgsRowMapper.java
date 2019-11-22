package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import com.logica.ngph.esb.Dtos.Raw_Msgs;

public class RawMsgsRowMapper implements RowMapper<Raw_Msgs>
{

	public Raw_Msgs mapRow(ResultSet resultSet, int arg1) throws SQLException {
		
		Raw_Msgs msgsPolled = new Raw_Msgs();
	
		msgsPolled.setRawChnl(resultSet.getString("RAW_CHNL"));
		msgsPolled.setRawDrctn(resultSet.getString("RAW_DIRECTION"));
		msgsPolled.setRawHost(resultSet.getString("RAW_HOST"));
		msgsPolled.setRawMsgs(resultSet.getClob("RAW_MSG"));
		msgsPolled.setRawMsgValStatus(resultSet.getBigDecimal("RAW_MSG_VALSTATUS"));
		msgsPolled.setRawRcvdTm(resultSet.getTimestamp("RAW_RECVDTIME"));
		
		return msgsPolled;
	}
}
