package com.logica.ngph.esb.servicesImpl;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.daos.LmLinkDao;
import com.logica.ngph.esb.services.LmLinkService;
import com.logica.ngph.utils.ApplicationContextProvider;

/**
 * @author guptarb
 */
public class LmLinkServiceImpl implements LmLinkService {
	
static Logger logger = Logger.getLogger(LmLinkServiceImpl.class);
	
	private LmLinkDao lmLinkDao;
	
	/**
	  @param lmLinkDao the lmLinkDao to set
	 */

	public void setLmLinkDao(LmLinkDao lmLinkDao) 
	{
		this.lmLinkDao = lmLinkDao;
	}
	SqlRowSet srs = null;
	double debits = 0;
	double credits = 0;
	double openBal = 0;
	double closeBal = 0;
	String currency = null;
	String bus_date = null;
	
	public void doProcess(NgphCanonical ngphCanonical)throws Exception
	{
		try
		{
		
		lmLinkDao = (LmLinkDao)ApplicationContextProvider.getBean("lmLinkDao");
		
		if(ngphCanonical!=null)
		{
			// Check Whether data is present for message currency and business date in the TA_Liquidity table
			srs=lmLinkDao.validateTa_Liquidy(ngphCanonical);
	
			//check whether the row set returned any row or not
			if(srs.next())
			{
				debits = srs.getBigDecimal("DEBITS").doubleValue();
				credits =srs.getBigDecimal("CREDITS").doubleValue(); 
				openBal = srs.getBigDecimal("OPENING_BAL").doubleValue();
				closeBal = srs.getBigDecimal("CLOSING_BAL").doubleValue();
				currency = srs.getString("MSGS_CUR");
				bus_date = srs.getDate("BUS_DATE").toString();
			}
			else
			{
				logger.info("There was no data in the table hance Zero rows returned");
			}
			
			// Message Direction is Inbound
			if(ngphCanonical.getMsgDirection().equalsIgnoreCase("I"))
			{
				logger.info("Message Direction is Inbound");
				
				credits = credits + ngphCanonical.getMsgAmount().doubleValue();
				closeBal = closeBal + ngphCanonical.getMsgAmount().doubleValue();
				openBal = openBal + ngphCanonical.getMsgAmount().doubleValue();
			}
			// Message Direction is Outbound
			else
			{
				logger.info("Message Direction is OutBound");
				
				debits = debits + ngphCanonical.getMsgAmount().doubleValue();
				closeBal = closeBal - ngphCanonical.getMsgAmount().doubleValue();
				openBal = openBal - ngphCanonical.getMsgAmount().doubleValue();
			}
			
			// Check if currency and business date is null, if yes insert record 
			if(currency==null && bus_date==null)
			{
				logger.info("There was no data for currency..hence make a insertion");
				bus_date = lmLinkDao.getbusday_Date(ngphCanonical.getMsgBranch());
				lmLinkDao.populate_Ta_Liquidity(debits,credits ,openBal ,closeBal ,ngphCanonical.getMsgCurrency() ,bus_date);
			
				lmLinkDao.populate_Ta_Liquidity_Tx(ngphCanonical.getMsgRef(), ngphCanonical.getMsgCurrency(), bus_date, closeBal, ngphCanonical.getMsgDirection(), ngphCanonical.getMsgAmount().doubleValue());
			}
			// if currency and business date is not null, the update the record 
			else
			{
				logger.info("Currency exists for bussiness day hence update the record");
				bus_date = lmLinkDao.getbusday_Date(ngphCanonical.getMsgBranch());
				lmLinkDao.update_Ta_Liquidity(debits, credits, openBal, closeBal, ngphCanonical.getMsgCurrency(), bus_date);
				
				lmLinkDao.populate_Ta_Liquidity_Tx(ngphCanonical.getMsgRef(), ngphCanonical.getMsgCurrency(), bus_date, closeBal, ngphCanonical.getMsgDirection(), ngphCanonical.getMsgAmount().doubleValue());
			}
			
			/*
			 * Performing the Operations for TA_Party_Liquidity and TA_Party_Liquidity_TX table 
			 */
			
			srs=lmLinkDao.validateTa_PartyLiquidy(ngphCanonical);
			

		}
		else
		{
			logger.info("Null Canonical Object received by LmLinkServiceImpl");
		}
		}
		catch (Exception e) {
			logger.error(e, e);
			throw new Exception(e);
		}
	}
	
	public static void main(String[] args) 
	{/*
		ApplicationContextProvider.initializeContextProvider();
		NgphCanonical obj = new NgphCanonical();
		obj.setMsgRef("testmsgref");
		obj.setMsgChnlType("swift");
		obj.setMsgBranch("0047");
		obj.setMsgCurrency("INR");
		obj.setMsgDirection("0");
		obj.setMsgAmount(new BigDecimal(345));
		
		new LmLinkServiceImpl().doProcess(obj);
	*/}
	/*
	Previous code...
	
	static Logger logger = Logger.getLogger(LmLinkServiceImpl.class);
	
	private LmLinkDao lmLinkDao;
	
	*//**
	 * @param lmLinkDao the lmLinkDao to set
	 *//*

	public void setLmLinkDao(LmLinkDao lmLinkDao) {
		this.lmLinkDao = lmLinkDao;
	}

	public static void main(String[] args) 
	{
		NgphCanonical obj = new NgphCanonical();
		obj.setMsgChnlType("swift");
		obj.setMsgBranch("0047");
		obj.setMsgCurrency("INR");
		obj.setMsgDirection("i");
		obj.setMsgAmount(new BigDecimal(100));
		
		new LmLinkServiceImpl().doProcess(obj);
	}

	*//**
	 * Entry Point of the Service.
	 * This method will do all the Processing.
	 *//*
	public void doProcess(NgphCanonical ngphCanonical)
	{
		//ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		lmLinkDao = (LmLinkDao)ApplicationContextProvider.getBean("lmLinkDao");
		
		if(ngphCanonical!=null)
		{
			try
			{
				double newAmtVal =0;
				
				// Fetched Sequence Value
				String seqVal = lmLinkDao.getSeqVal();
				
				// insert data from Canonical to Liquidity_AT_T table 
				lmLinkDao.popudateLiquidity_AT_T(ngphCanonical, new BigDecimal(0),seqVal);
				
				// get Balance based on MsgCurrency AHID, Buss date and max(RowID)
				// If balance is found else balance will be taken as 0(Zero)
				BigDecimal bal = lmLinkDao.getLiquidity_AT_T_Bal(ngphCanonical);
				logger.info("Balance amount is : " + bal);
				
				logger.info("Msg Direction is : " + ngphCanonical.getMsgDirection());
				
				// Meesage Direction is Out bound
				if(ngphCanonical.getMsgDirection().equalsIgnoreCase("O"))
				{
					// Fetched the Sequence Value
					String newseqVal = lmLinkDao.getSeqVal();
					
					BigDecimal canonicalAmount = ngphCanonical.getMsgAmount();
					logger.info("Canonical Amount val : " + canonicalAmount);
					newAmtVal = Double.parseDouble(bal.toString()) - Double.parseDouble(canonicalAmount.toString());
					logger.info("New Amount Val is : " + newAmtVal);
					
					// insert new Record in Liquidity_AT_T with the new Balance Amount
					lmLinkDao.popudateLiquidity_AT_T(ngphCanonical, new BigDecimal(newAmtVal),newseqVal);
					
					//update Acct_Hldg_Inst_T for Outbound 
					lmLinkDao.updateAcct_Hldg_Inst_T_Outbound(ngphCanonical, newseqVal);
				}
				//Message Direction is In bound
				else
				{
					// Fetched the Sequence Value
					String newseqVal = lmLinkDao.getSeqVal();
					
					BigDecimal canonicalAmount = ngphCanonical.getMsgAmount();
					logger.info("Canonical Amount val : " + canonicalAmount);
					newAmtVal = Double.parseDouble(bal.toString()) + Double.parseDouble(canonicalAmount.toString());
					logger.info("New Amount Val is : " + newAmtVal);
					
					// insert new Record in Liquidity_AT_T with the new Balance Amount
					lmLinkDao.popudateLiquidity_AT_T(ngphCanonical, new BigDecimal(newAmtVal),newseqVal);
					
					//update Acct_Hldg_Inst_T for In bound
					lmLinkDao.updateAcct_Hldg_Inst_T_Inbound(ngphCanonical, newseqVal);
				}
				
				// check if record is present in LiquidityT
				boolean data = lmLinkDao.getLiquidityT_Bal(ngphCanonical);
				logger.info("LiquidityT result is : " + data);
				
				// If no Records in LiquidityT insert new record
				if(data==false)
				{
					logger.info("Data not found in Liquidity_T");
					lmLinkDao.insertLiquidity_T(ngphCanonical,newAmtVal);
				}
				
				// If Record was found in LiquidityT update that record with the new Balance
				else
				{
					logger.info("Data is present in Liquidity_T");
					lmLinkDao.updateLiquidity_T(ngphCanonical,newAmtVal);
					
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			logger.debug("Null Canonical Object received by LmLinkServiceImpl");
		}
	}

*/
}
