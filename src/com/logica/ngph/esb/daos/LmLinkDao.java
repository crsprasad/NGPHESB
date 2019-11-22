package com.logica.ngph.esb.daos;

import java.math.BigDecimal;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * @author guptarb
 *
 */
public interface LmLinkDao {
	
	String getbusday_Date(String branchCode)throws Exception;

	SqlRowSet validateTa_Liquidy(NgphCanonical obj)throws Exception;
	
	void populate_Ta_Liquidity(double debits,double credits ,double openBal ,double closeBal ,String currency ,String bus_date)throws Exception;
	
	void update_Ta_Liquidity(double debits,double credits ,double openBal ,double closeBal ,String currency ,String bus_date)throws Exception;
	
	void populate_Ta_Liquidity_Tx(String msgRef,String currency,String bus_date,double closeBal, String direction, double msgAmt)throws Exception;
	
	SqlRowSet validateTa_PartyLiquidy(NgphCanonical obj)throws Exception;
	
}
