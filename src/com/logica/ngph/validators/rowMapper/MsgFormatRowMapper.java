package com.logica.ngph.validators.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.validators.dto.MsgField;
import com.logica.ngph.validators.dto.MsgFormat;
/**
 * @author guptarb
 *
 */
public class MsgFormatRowMapper implements RowMapper<MsgFormat> {
	
	static Logger logger = Logger.getLogger(MsgFormatRowMapper.class);
	
	private JdbcTemplate jdbcTemplate;
	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	private ArrayList<MsgField> populateMsgFieldObj(String field_ID)
	{

		ArrayList<MsgField> fieldVals = null;

		String query = "select * from ta_fields_format where MSG_FIELD_ID =?";

		try {
			fieldVals = (ArrayList<MsgField>)jdbcTemplate.query(query,new Object[]{field_ID}, new MsgFieldRowMapper());
		} catch (Exception e) 
		{
			logger.error(e, e);
		}
		
		return fieldVals;
		
	}
	
	private HashMap<String, MsgField> prcToMap(ArrayList<MsgField> listObj)
	{
		HashMap<String, MsgField> hm = new HashMap<String, MsgField>();
		MsgField obj = null;;
		
		for(int i=0;i<listObj.size();i++)
		{
			obj = listObj.get(i);
			hm.put(obj.getFldNo()+obj.getFldCompSeq(), obj);
		}
		return hm;
		
		
	}
	public MsgFormat mapRow(ResultSet resultSet, int arg1) throws SQLException {
		
		MsgFormat msgsPolled = new MsgFormat();
	
		/**
		 * chnlType;
		 * msgType;
		 * subMsgType;
		 * fieldNo;
	     * fieldMand;
	     * fieldSeq;
		 * fieldTagOpt;
		 */
		
		msgsPolled.setChnlType(resultSet.getString("CHANNELTYPE"));
		msgsPolled.setMsgType(resultSet.getString("MSGTYPE"));
		msgsPolled.setSubMsgType(resultSet.getString("MSGSUBTYPE"));
		msgsPolled.setFieldNo(resultSet.getString("FIELD_NO"));
		msgsPolled.setFieldSeq(resultSet.getString("FIELD_SEQ"));
		msgsPolled.setFieldMand(resultSet.getString("FIELD_MANDATORY"));
		msgsPolled.setFieldTagOpt(resultSet.getString("FIELD_TAG_OPTION"));
		msgsPolled.setFieldId(resultSet.getString("MSG_FIELD_ID"));
		
		ArrayList<MsgField> msgfieldObj= populateMsgFieldObj(resultSet.getString("MSG_FIELD_ID"));
		HashMap<String, MsgField>  mapObj = prcToMap(msgfieldObj);
		msgsPolled.setMsgFieldMapper(mapObj);

		return msgsPolled;
	}
}
