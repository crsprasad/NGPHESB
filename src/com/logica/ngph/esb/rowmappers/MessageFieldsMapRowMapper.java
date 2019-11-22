package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
/**
 * 
 * @author mohdabdulaa
 *
 */
public class MessageFieldsMapRowMapper implements
		ResultSetExtractor<Map<String, String>> {

	public Map<String, String> extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		Map<String, String> result = new HashMap<String, String>();

		while (rs.next()) {
			result.put(rs.getString(1), rs.getString(2));
		}
		return result;
	}

}
