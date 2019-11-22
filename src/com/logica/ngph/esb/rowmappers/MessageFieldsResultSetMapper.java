package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.logica.ngph.esb.constants.NgphEsbConstants;

public class MessageFieldsResultSetMapper implements ResultSetExtractor<List<String>> {

	public List<String> extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		List<String> result = new LinkedList<String>();
		int i = 0;
		while (rs.next()) {
			String object = rs.getString(2);
			if(StringUtils.isNotEmpty(object) && !result.contains(object))
			{
				if(NgphEsbConstants.NGPH_CONSTANT_HASH.equalsIgnoreCase(object.trim()))
				{
					result.add(NgphEsbConstants.NGPH_INT_ZERO, object);
				}else{
					result.add(i, object);	
				}
				 i++;
			}
		}
		return result;
	}
}
