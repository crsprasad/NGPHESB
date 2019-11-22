package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.ServiceConfigDetails;

public class ServiceOrchRowMapper implements RowMapper<ServiceConfigDetails>
{

	public ServiceConfigDetails mapRow(ResultSet rs, int arg1) throws SQLException {
		
		ServiceConfigDetails serviceOrch = new ServiceConfigDetails();
		
		serviceOrch.setServiceCallSeq(rs.getInt("SRVC_CALLSEQ"));
		serviceOrch.setServiceId(rs.getString("SRVC_SERVICEID"));
		serviceOrch.setServiceMessageType(rs.getString("SRVC_MSG_TYPE"));
		serviceOrch.setServiceMsgSubType(rs.getString("SRVC_MSG_SUBTYPE"));
		
		return serviceOrch;
	}

}
