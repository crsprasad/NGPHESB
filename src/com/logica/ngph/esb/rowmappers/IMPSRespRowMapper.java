package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import com.logica.ngph.esb.Dtos.ResponseBean;

public class IMPSRespRowMapper implements RowMapper<ResponseBean>
{
	public ResponseBean mapRow(ResultSet rs, int arg1) throws SQLException 
	{
		ResponseBean obj = new ResponseBean();
		obj.setMsgSubType(rs.getString("IMPS_MSGSUBTYPE"));
		obj.setMsgType(rs.getString("IMPS_MSGTYPE"));
		obj.setReqTmStmp(rs.getTimestamp("IMPS_REQTIMESTMP"));
		obj.setResTmStmp(rs.getTimestamp("IMPS_RESTIMESTMP"));
		obj.setResCode(rs.getString("IMPS_RESCODE"));
		obj.setVerSendCt(rs.getInt("IMPS_VERFCTNSENDCNT"));
		obj.setMsgDirection(rs.getString("IMPS_DIRECTION"));
		
		return obj;
	}
}
