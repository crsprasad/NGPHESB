package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.common.dtos.ErrorCodes;

public class ErrorCodesRowMapper implements RowMapper<ErrorCodes>{

	public ErrorCodes mapRow(ResultSet rs, int arg1) throws SQLException 
	{
		ErrorCodes obj = new ErrorCodes();
		
		obj.setErrChnl(rs.getString("ERR_CHNL"));
		obj.setErrCode(rs.getString("ERR_MSGCODE"));
		obj.setErrDes(rs.getString("ERR_MSGDESC"));
		obj.setErrMapCode(rs.getString("ERR_MAPCODE"));
		
		return obj;
	}

}
