package com.logica.ngph.esb.daos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import javax.naming.InitialContext;


public class WareHouseDAO {
	
	public Connection conn = null;
	public Statement stmt = null;
	public javax.sql.DataSource ds=null;
	public InitialContext context = null;
	
	public WareHouseDAO()
	{
		try
		{
			this.context = new InitialContext();
			ds = (javax.sql.DataSource) context.lookup("java:/OracleDS");
			conn = ds.getConnection();
			stmt = conn.createStatement();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public ResultSet selectOperation()
	{
		ResultSet rs = null;
		
		try
		{
			rs = stmt.executeQuery("select BUSDAY_BRANCH, BUSDAY_DATE from businessdayM");		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return rs;
	}
	
	public void close()
	{
		try
		{
			System.out.println(" query selected here ==== ");
			stmt.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	} 

}
