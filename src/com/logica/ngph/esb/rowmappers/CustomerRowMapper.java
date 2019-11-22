package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.CustomerInfo;

public class CustomerRowMapper implements RowMapper<CustomerInfo>{

	public CustomerInfo mapRow(ResultSet rs, int arg1) throws SQLException
	{
		CustomerInfo custBean = new CustomerInfo();
		
		custBean.setEmail1(rs.getString("ADDR_EMAIL1"));
		custBean.setEmail2(rs.getString("ADDR_EMAIL2"));
		custBean.setEmail3(rs.getString("ADDR_EMAIL3"));
		custBean.setMobNo(rs.getString("ADDR_MOBILE"));
		
		return custBean;
	}
}
