package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import com.logica.ngph.esb.Dtos.UserInfoBean;

public class UserMapper implements RowMapper<UserInfoBean>{

	public UserInfoBean mapRow(ResultSet rs, int arg1) throws SQLException
	{
		UserInfoBean userBean = new UserInfoBean();
		
		userBean.setEmail(rs.getString("EMAILID"));
		userBean.setMobNo(rs.getString("MOBILENO"));
		
		return userBean;
		
	}
}
