package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.MessageFormats;

public class MessageFormatsRowMapper implements RowMapper<MessageFormats>{

	public MessageFormats mapRow(ResultSet resultSet, int arg1) throws SQLException {

		MessageFormats messageFormats = new MessageFormats();
		
		messageFormats.setComponentDefaultVal(resultSet.getString("FORMAT_COMP_DEFAULTVALUE"));
		messageFormats.setComponentMaxLength(resultSet.getInt("FORMAT_COMP_MAXLENGTH"));
		messageFormats.setComponentMinLength(resultSet.getInt("FORMAT_COMP_MINLENGTH"));
		messageFormats.setComponentOutLef(resultSet.getInt("FORMAT_COMP_OUTLF"));
		messageFormats.setComponentPrefix(resultSet.getString("FORMAT_COMP_PREFIX"));
		messageFormats.setComponentSufix(resultSet.getString("FORMAT_COMP_SUFFIX"));
		messageFormats.setComponentType(resultSet.getInt("FORMAT_COMP_TYPE"));
		messageFormats.setFieldNo(resultSet.getString("FORMAT_FIELD_NO"));
		messageFormats.setFieldSeq(resultSet.getInt("FORMAT_FIELD_SEQ"));
		messageFormats.setFieldTag(resultSet.getString("FORMAT_FIELDTAG"));
		messageFormats.setIsComponentMandatory(resultSet.getInt("FORMAT_COMPONENT_MANDATORY"));
		messageFormats.setIsFieldMandatory(resultSet.getInt("FORMAT_FIELD_MANDATORY"));
		messageFormats.setIsFieldRecursive(resultSet.getInt("FORMAT_FIELD_ISRECURSIVE"));
		messageFormats.setMsgChannel(resultSet.getString("FORMAT_CHANNEL"));
		messageFormats.setMsgSubType(resultSet.getString("FORMAT_MSGSUBTYPE"));
		messageFormats.setMsgType(resultSet.getString("FORMAT_MSGTYPE"));
		messageFormats.setTagComponent(resultSet.getInt("FORMAT_TAG_COMPONENT"));
		messageFormats.setTagSeq(resultSet.getInt("FORMAT_TAG_SEQ"));
		
		return messageFormats;
	}

}
