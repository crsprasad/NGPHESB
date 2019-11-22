package com.logica.ngph.esb.daosImpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.daos.LmLinkDao;

/**
 * @author guptarb
 * 
 */
public class LmLinkDaoImpl implements LmLinkDao{
	
	SqlRowSet srs = null;
	private JdbcTemplate jdbcTemplate;
	static Logger logger = Logger.getLogger(LmLinkDaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	// Returns business date based on branch we get from Canonical
	public String getbusday_Date(String branchCode)throws Exception
	{
		Date busDay_Date = null;
		String date=null;
		
		String query = "select TO_DATE(BUSDAY_DATE) from ta_businessdaym where busday_branch=?";
		try
		{
			busDay_Date = jdbcTemplate.queryForObject(query, new Object[]{branchCode}, Date.class);

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
			date = sdf.format(busDay_Date);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.error(e, e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e, e);
		}
		catch (Exception e) {
			logger.error(e, e);
			throw new Exception(e);
		}
		return date;
	}
	
	// Validates data is present or not
	public SqlRowSet validateTa_Liquidy(NgphCanonical canonical)throws Exception
	{
		// Returns the business date based on msg branch.
		String busDate = getbusday_Date(canonical.getMsgBranch());
		
		String query="SELECT MSGS_CUR,DEBITS,CREDITS,OPENING_BAL,CLOSING_BAL,TO_DATE(BUS_DATE) as  BUS_DATE FROM ta_liquidity where MSGS_CUR=? and BUS_DATE=?";
		try
		{
			srs = jdbcTemplate.queryForRowSet(query,new Object[]{canonical.getMsgCurrency(),busDate});
			System.out.println(srs.getRow());
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.error(e, e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}
		
		return srs;
	}

	//inserts the data into Ta_Liquidity table
	public void populate_Ta_Liquidity(double debits,double credits ,double openBal ,double closeBal ,String currency ,String bus_date)throws Exception
	{
		String sql="INSERT INTO TA_LIQUIDITY(MSGS_CUR, DEBITS, CREDITS, OPENING_BAL, CLOSING_BAL, BUS_DATE) VALUES (?,?,?,?,?,?)";
		try
		{
			jdbcTemplate.update(sql, new Object[]{currency,debits,credits,openBal,closeBal,bus_date});
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}
	}
	
	//updates the records in Ta_Liquidity table
	public void update_Ta_Liquidity(double debits,double credits ,double openBal ,double closeBal ,String currency ,String bus_date)throws Exception
	{
		String sql="UPDATE TA_LIQUIDITY SET DEBITS = ?, CREDITS=?, OPENING_BAL=?, CLOSING_BAL=? where BUS_DATE=? and MSGS_CUR=?";
		
		try
		{
			jdbcTemplate.update(sql, new Object[]{debits,credits,openBal,closeBal,bus_date,currency});
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}
		
	}
	
	//inserts the data into Ta_Liquidity_Tx table
	public void populate_Ta_Liquidity_Tx(String msgRef,String currency,String bus_date,double closeBal, String direction, double msgAmt)throws Exception
	{
		String sql="INSERT INTO TA_LIQUIDITY_TX(MSGS_MSGREF, MSGS_CUR, BUSDAY_DATE, CLOSING_BAL, MOFIFIED_TIME, MSGS_DIRECTION,MSGS_AMT) VALUES (?,?,?,?,sysdate,?,?)";
		try
		{
			System.out.println(msgRef);
			System.out.println(currency);
			System.out.println(bus_date);
			System.out.println(closeBal);
			System.out.println(direction);
			System.out.println(msgAmt);
			
			jdbcTemplate.update(sql, new Object[]{msgRef,currency,bus_date,closeBal,direction,msgAmt});
		}
		catch (Exception e) {
			logger.error(e, e);
			throw new Exception(e);
		}
	}
	
	public SqlRowSet validateTa_PartyLiquidy(NgphCanonical canonical)throws Exception
	{

		// Returns the business date based on msg branch.
		String busDate = getbusday_Date(canonical.getMsgBranch());
		
		String query="SELECT MSGS_CUR,DEBITS,CREDITS,OPENING_BAL,CLOSING_BAL,TO_DATE(BUS_DATE) as  BUS_DATE,PARTY_BIC,PARTY_BANK,PARTY_CTRY,PARTY_LOC FROM ta_party_liquidity where MSGS_CUR=? and BUS_DATE=?";
		try
		{
			srs = jdbcTemplate.queryForRowSet(query,new Object[]{canonical.getMsgCurrency(),busDate});
			System.out.println(srs.getRow());
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.error(e, e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}
		
		return srs;
	}

	

	/*
	
	Previous code..
	private JdbcTemplate jdbcTemplate;
	static Logger logger = Logger.getLogger(LmLinkDaoImpl.class);

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Returns the Sequence Value
	public String getSeqVal()
	{
		try
		{
			String seqNextVal = "select BIPN_ATT_ROWID_SEQ.nextVal from dual";
			String seqVal = jdbcTemplate.queryForObject(seqNextVal, String.class);
			return seqVal;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	// inserts the data in Liquidity_AT_T from Canonical
	public void popudateLiquidity_AT_T(NgphCanonical ngphCanonical, BigDecimal balance, String seqVal)
	{
		if(ngphCanonical.getMsgChnlType()!=null && ngphCanonical.getMsgBranch()!=null && ngphCanonical.getMsgCurrency()!=null )
		{
			try
			{
				String channelType = ngphCanonical.getMsgChnlType(); 
				String branch = ngphCanonical.getMsgBranch();
				String date = getbusday_Date(branch);
				String currency = ngphCanonical.getMsgCurrency();
				
				System.out.println("channelType : " + channelType);
				System.out.println("branch : " + branch);
				System.out.println("busDay_Date : " + date);
				System.out.println("currency : " + currency);
				System.out.println("seqVal : " + seqVal);
				
				
				String populateLiquidity_AT_T ="INSERT INTO LIQUIDITY_AT_T (AHIID, BUSINESS_DATE, AS_AT, SHADOW_BALANCE, RESERVED_LIQUIDITY, SCHEDULER_BALANCE, AHI_ACCOUNT_LIMIT, LIQUIDITY_CHECK_STATUS, UPDATE_COUNTER, ROW_ID, IDL_LIMIT, IDL_USED, IDL_MODE, CURRENCY)"
											   + " VALUES"
											   +" ('" + channelType +"','" + date +"' , sysdate ," + balance +", 0,"+ balance +", 0, 'A', 0," + seqVal + ", 0, 0, 0, '" + currency +"')";
				int result = jdbcTemplate.update(populateLiquidity_AT_T);
				
				System.out.println("No of Rows Updated : "  +result);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			logger.debug("MsgChannelType, Currency and MsgBranch was null in Canonical Object");
		}
		
	}

	// Returns Balance amount in Liquidity_AT_T_Bal based on Ahiid, currency, business date and max(rowID)
	public BigDecimal getLiquidity_AT_T_Bal(NgphCanonical ngphCanonical)
	{
		BigDecimal balance= new BigDecimal(0);
		
		String channelType = ngphCanonical.getMsgChnlType(); //swift 
		String branch = ngphCanonical.getMsgBranch();// branch for business date
		String date = getbusday_Date(branch);// business date
		String currency = ngphCanonical.getMsgCurrency();// currency
		
		System.out.println("channelType : " + channelType);
		System.out.println("branch : " + branch);
		System.out.println("busDay_Date : " + date);
		System.out.println("currency : " + currency);
		
		long rowId = getRowID(channelType);
		System.out.println("MAX(RowId) : " + rowId);
		
		String query="select SHADOW_BALANCE from liquidity_at_t where CURRENCY='" + currency + "' and ahiid='" + channelType + "' and row_id=" + rowId +" and BUSINESS_DATE='" + date + "'";
		try
		{
			balance = jdbcTemplate.queryForObject(query, BigDecimal.class);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.warn("There was no balance found for the Swift Ahid");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return balance;
		
	}
	
	// if no record found in Liquidity_T for Swift and currency and Business date insert new Record
	public void insertLiquidity_T(NgphCanonical ngphCanonical, double balance)
	{
		try
		{
			String channelType = ngphCanonical.getMsgChnlType(); 
			String branch = ngphCanonical.getMsgBranch();
			String date = getbusday_Date(branch);
			String currency = ngphCanonical.getMsgCurrency();
			
			System.out.println("channelType : " + channelType);
			System.out.println("branch : " + branch);
			System.out.println("busDay_Date : " + date);
			System.out.println("currency : " + currency);
			
			String query="INSERT INTO LIQUIDITY_T(AHI_ID, BUSINESS_DATE, LAST_CLSG_BALANCE, LIMIT_DEFN, RESERVED_LQDTY, LAST_NOTI_BALANCE, SNAPSHOT_DAY_NBR, UPDATE_COUNTER, CURRENCY)"
						 +" VALUES ('" + channelType + "','" + date +"', '" + balance + "', 'RBI_LIMIT', '0', '0', '0', '0', '" + currency +"')";
			int result = jdbcTemplate.update(query);
			
			System.out.println("result of Liquidity_T : " + result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean getLiquidityT_Bal(NgphCanonical ngphCanonical)
	{
		boolean result = false;
		
		String channelType = ngphCanonical.getMsgChnlType(); 
		String branch = ngphCanonical.getMsgBranch();
		String date = getbusday_Date(branch);
		String currency = ngphCanonical.getMsgCurrency();
		
		System.out.println("channelType : " + channelType);
		System.out.println("branch : " + branch);
		System.out.println("busDay_Date : " + date);
		System.out.println("currency : " + currency);
		
		try
		{
			String query="select LAST_CLSG_BALANCE from liquidity_t where ahi_id='" + channelType +"' and currency='" + currency +"' and business_date='" + date +"'";
			BigDecimal balance = jdbcTemplate.queryForObject(query, BigDecimal.class);
			result = true;
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.warn("There was data found in LiquidityT for the Swift and currency and bussiness date");
			result=false;
			//e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// if record found in Liquidity_T for Swift and currency and Business date update that Record
	public void updateLiquidity_T(NgphCanonical ngphCanonical, double balance)
	{
		try
		{
			String channelType = ngphCanonical.getMsgChnlType(); 
			String branch = ngphCanonical.getMsgBranch();
			String date = getbusday_Date(branch);
			String currency = ngphCanonical.getMsgCurrency();
			
			System.out.println("channelType : " + channelType);
			System.out.println("branch : " + branch);
			System.out.println("busDay_Date : " + date);
			System.out.println("currency : " + currency);
			
			String query= "update liquidity_t set LAST_CLSG_BALANCE= '" + balance +"' where ahi_id='" + channelType +"' and currency='" + currency +"' and business_date='" + date +"'";
			int result = jdbcTemplate.update(query);
			
			System.out.println("No of rows updated in Liquidity_T are : " + result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	// Returns business date based on branch we get from Canonical
	private String getbusday_Date(String branchCode)
	{
		Date busDay_Date = null;
		String date=null;
		
		String query = "select TO_DATE(BUSDAY_DATE) from ta_businessdaym where busday_branch=?";
		try
		{
			busDay_Date = jdbcTemplate.queryForObject(query, new Object[]{branchCode}, Date.class);

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
			date = sdf.format(busDay_Date);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.warn("There was no default configuration for BUSDAY_DATE in ta_businessdaym");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}
	
	//returns max(RowID) from Liquidity_AT_T based on Channel type(Swift)
	private long getRowID(String ahid)
	{
		long rowId=0;
		
		String query = "select max(ROW_ID) from liquidity_at_t where ahiid='" + ahid + "'";
		
		try
		{
			rowId = jdbcTemplate.queryForLong(query);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.warn("There was no max of Row id found for swift ahid");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return rowId;
	}
	
	// update IN_LIQ_ROWID of Acct_Hldg_Inst_T for Inbound Direction based on Ahid and Currency
	public void updateAcct_Hldg_Inst_T_Inbound(NgphCanonical ngphCanonical,String seqVal)
	{
		String channelType = ngphCanonical.getMsgChnlType(); 
		String currency = ngphCanonical.getMsgCurrency();

		try
		{
			String getUpdateCounter= "select UPDATE_COUNTER from ACCT_HLDG_INST_T where OBJECT_ID=? and CURRENCY=?";
			Integer updateCounter = jdbcTemplate.queryForObject(getUpdateCounter, new Object[]{channelType,currency}, Integer.class);
			System.out.println("The Update Counter is : " + updateCounter);

			int newUpdateCounter = Integer.parseInt(updateCounter+"")+1;
			System.out.println("New Update Counter is : "  +newUpdateCounter);
			
			String updateAcct_Hldg_Inst_T = "update ACCT_HLDG_INST_T set IN_LIQ_ROWID=" + seqVal + ",UPDATE_COUNTER=" + newUpdateCounter +" where OBJECT_ID='" + channelType +"' and CURRENCY='" + currency +"'";
			int result = jdbcTemplate.update(updateAcct_Hldg_Inst_T);
			System.out.println("No of Records Updated in  Acct_Hldg_Inst_T is : " + result);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.warn("There was no data found in Acct_Hldg_Inst_T");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	// update IN_LIQ_ROWID of Acct_Hldg_Inst_T for Inbound Direction based on Ahid and Currency
	public void updateAcct_Hldg_Inst_T_Outbound(NgphCanonical ngphCanonical,String seqVal)
	{
		String channelType = ngphCanonical.getMsgChnlType(); 
		String currency = ngphCanonical.getMsgCurrency();

		try
		{
			String getUpdateCounter= "select UPDATE_COUNTER from ACCT_HLDG_INST_T where OBJECT_ID=? and CURRENCY=?";
			Integer updateCounter = jdbcTemplate.queryForObject(getUpdateCounter, new Object[]{channelType,currency}, Integer.class);
			System.out.println("The Update Counter is : " + updateCounter);

			int newUpdateCounter = Integer.parseInt(updateCounter+"")+1;
			System.out.println("New Update Counter is : "  +newUpdateCounter);
			
			String updateAcct_Hldg_Inst_T = "update ACCT_HLDG_INST_T set OUT_LIQ_ROWID=" + seqVal + ",UPDATE_COUNTER=" + newUpdateCounter +" where OBJECT_ID='" + channelType +"' and CURRENCY='" + currency +"'";
			int result = jdbcTemplate.update(updateAcct_Hldg_Inst_T);
			System.out.println("No of Records Updated in  Acct_Hldg_Inst_T is : " + result);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.warn("There was no data found in Acct_Hldg_Inst_T");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
*/}
