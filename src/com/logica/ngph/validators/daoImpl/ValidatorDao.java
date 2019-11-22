package com.logica.ngph.validators.daoImpl;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.logica.ngph.validators.doa.IValidatorDao;
import com.logica.ngph.validators.dto.MsgField;
import com.logica.ngph.validators.dto.MsgFormat;
import com.logica.ngph.validators.rowMapper.MsgFieldRowMapper;
import com.logica.ngph.validators.rowMapper.MsgFormatRowMapper;

public class ValidatorDao implements IValidatorDao{
	
	static Logger logger = Logger.getLogger(ValidatorDao.class);
	
	private JdbcTemplate jdbcTemplate;
	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public static HashMap<String, MsgFormat> hm = new HashMap<String, MsgFormat>();

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.validators.doa.IValidatorDao#getMsgFiledsData(java.lang.String, java.lang.String, java.lang.String)
	 * 
	 * Will Fetch the data based on chnlTpe,msgType and subMsgtype and 
	 * Maps it to MsgFormatMapper and stores the Objects of MsgFormatMapper in an List.
	 */
	public List<MsgFormat> getMsgFiledsData()
	{
		List<MsgFormat> polledVals = null;
		
		String query = "select CHANNELTYPE, MSGTYPE,MSGSUBTYPE,FIELD_NO,FIELD_SEQ,FIELD_MANDATORY,FIELD_TAG_OPTION,MSG_FIELD_ID from ta_msg_format order by CHANNELTYPE,MSGTYPE,MSGSUBTYPE,FIELD_SEQ";

		MsgFormatRowMapper msgFormatRowMapper = new MsgFormatRowMapper();
		try {
				msgFormatRowMapper.setJdbcTemplate(jdbcTemplate); 
				polledVals = jdbcTemplate.query(query, msgFormatRowMapper );
				prcToMap(polledVals);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e, e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
		return polledVals;
	}
	
	private HashMap<String, MsgFormat> prcToMap(List<MsgFormat> listObj)
	{
		
		MsgFormat obj = null;;
		
		for(int i=0;i<listObj.size();i++)
		{
			
			obj = listObj.get(i);
			hm.put(obj.getChnlType() + obj.getMsgType() + obj.getSubMsgType() + obj.getFieldNo(), obj);
		}
		return hm;
	}
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.validators.doa.IValidatorDao#getFiledsData()
	 * 
	 * Returns all the rows from Ta_Fields_Fromat Table
	 */
	public List<MsgField> getFiledsData()
	{
		List<MsgField> fieldVals = null;

		String query = "select * from ta_fields_format";

		try {
			fieldVals = jdbcTemplate.query(query, new MsgFieldRowMapper());
		} 
		catch (EmptyResultDataAccessException e) {
			logger.error(e, e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
		return fieldVals;
	}

}
