package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.logica.ngph.esb.Dtos.TcpBean;

public class TcpRowMapper implements RowMapper<TcpBean>{

	public TcpBean mapRow(ResultSet resultSet, int arg1) throws SQLException {
		
		TcpBean tcpObj = new TcpBean();

		tcpObj.setMsg(resultSet.getString("RAW_MSG"));
		tcpObj.setMsg(resultSet.getString("MSGREF"));
		
		return tcpObj;

	}

}
