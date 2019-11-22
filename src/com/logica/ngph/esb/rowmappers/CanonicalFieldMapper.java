package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.CanonicalFieldPojo;

public class CanonicalFieldMapper implements RowMapper<CanonicalFieldPojo> {
	
	public CanonicalFieldPojo mapRow(ResultSet resultSet, int arg1) throws SQLException {
		
		CanonicalFieldPojo pollerMessage = new CanonicalFieldPojo();
		
		pollerMessage.setChannelType(resultSet.getString("CHANNELTYPE"));
		pollerMessage.setMsgType(resultSet.getString("MSGTYPE"));
		pollerMessage.setMsgSubType(resultSet.getString("MSGSUBTYPE"));
		pollerMessage.setFieldNo(resultSet.getString("FIELD_NO"));
		pollerMessage.setFieldCanonicalAtt(resultSet.getString("FIELD_CANONICAL"));
		pollerMessage.setField_Seq(resultSet.getString("FIELD_SEQ"));
		pollerMessage.setFieldCompSeq(resultSet.getString("FIELD_COMP_SEQ"));
		pollerMessage.setFieldCompMandOpt(resultSet.getString("FIELD_COMP_MANDOPT"));
		pollerMessage.setFieldEocInd(resultSet.getString("field_eoc_ind"));
		pollerMessage.setField_cnsdr(resultSet.getString("FIELD_CONSIDERATION"));
		pollerMessage.setField_comp_fmt(resultSet.getString("FIELD_COMP_FORMAT"));
		pollerMessage.setField_Id(resultSet.getString("MSG_FIELD_ID"));
		
		return pollerMessage;
	}
}
