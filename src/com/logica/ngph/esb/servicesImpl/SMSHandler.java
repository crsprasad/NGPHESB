package com.logica.ngph.esb.servicesImpl;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.esb.TCPClient;
import com.logica.ngph.esb.TCPServer;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.services.ISMSHandler;
import com.logica.ngph.utils.ApplicationContextProvider;

/**
 *  @author guptarb
 */
public class SMSHandler implements ISMSHandler {
	
	static Logger logger = Logger.getLogger(SMSHandler.class);
	private EsbServiceDao esbServiceDao;
	private final static String propName = "System.properties";
	
	private static int accStartIndex;
	private static int accEndIndex;
	private static int mobStartIndex;
	private static int mobEndIndex;
	
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}

	public static void main(String[] args) {/*
		
		ApplicationContextProvider.initializeContextProvider();
		String data = "111111111119739925305";
		new SMSHandler().generateMMID(data);
	*/}
	
	static 
	{
		//loading property file in memory
		Properties props = new Properties();
		try 
		{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			accStartIndex=Integer.parseInt(props.getProperty("smsaccStartIndex"));
			accEndIndex = Integer.parseInt(props.getProperty("smsaccEndIndex"));
			mobStartIndex = Integer.parseInt(props.getProperty("smsmobStartIndex"));
			mobEndIndex = Integer.parseInt(props.getProperty("smsmobEndIndex"));
			
			logger.info("Account Start Index : " + accStartIndex);
			logger.info("Account End Index : " + accEndIndex);
			logger.info("Mobile Start Index : " + mobStartIndex);
			logger.info("Mobile End Index : " + mobEndIndex);
			
		} 
		catch (IOException e) 
		{
			logger.error(e, e);
		}
		catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	public String generateMMID(String val) throws Exception {
		
		String result = null;
	try
	{
		esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
		
		if(val!=null && StringUtils.isNotBlank(val) && StringUtils.isNotEmpty(val))
		{
			String accNo = val.substring(accStartIndex,accEndIndex);
			String mobNo = val.substring(mobStartIndex, mobEndIndex);
			
			logger.info("Account No : " + accNo + "\t Mobile No : " + mobNo);
			
			if(StringUtils.isNotBlank(accNo) && StringUtils.isNotBlank(mobNo) && StringUtils.isNotEmpty(accNo) && StringUtils.isNotEmpty(mobNo))
		        {
		              boolean isAccPsnt= esbServiceDao.validateAccNum(accNo);
		              boolean isMobPrsnt = esbServiceDao.validateMobNum(accNo, mobNo);
		              boolean isMMIdPrsnt = esbServiceDao.validateMMID(accNo, mobNo);
		              
		              if(isAccPsnt)
		              {
		            	  	String RandomNo="";
		            	    String MMID = esbServiceDao.getInitialisedValue("LOCNBIN");
		            	    
		            	  	if(isMobPrsnt)
		                    {
		                    	 if(isMMIdPrsnt)
		                    	 {
			                    	//Get max(mmid)
			                    	String maxMMID = esbServiceDao.maxMMID(accNo, mobNo);
			                    	logger.info("MAX MMID From DB: " + maxMMID);
			                    	
			                    	int last3Dgt = Integer.parseInt(maxMMID.substring(maxMMID.length()-3, maxMMID.length()));
			                    	last3Dgt=last3Dgt+1;
		                        
			                    	if((last3Dgt+"").length()==1)
		                              {
		                                    RandomNo="00"+last3Dgt; 
		                              }
		                              else if((last3Dgt+"").length()==2)
		                              {
		                                      RandomNo="0"+last3Dgt;
		                              }
		                              else
		                              {
		                                    RandomNo = last3Dgt+"";
		                              }
	
			                    	result = MMID + RandomNo;
			                    	
			                    	//update mmid in DB based on accNo and mobNo
			                    	esbServiceDao.updateMMID(result,accNo, mobNo);
		                    	 }
		                    	 else
		                    	 {
		                    		 esbServiceDao.updateMMID(MMID+"000",accNo, mobNo);
		                    	 }
		                    }
		                    else
		                    {
		                    	logger.error(mobNo + " : does not exists in DB");
		                    	result = MMID+"001"; 
		                    	//Insert mob no based on accNo
		                    	esbServiceDao.populateMobNoforMMID(accNo, mobNo);
		                    }
		                }
		                else
		                {
		                  	logger.error(accNo + " : does not exists in DB");
		                }
		        }
			else
			{
				logger.error("Either " + accNo + " or " + mobNo + " is null in the message received");
			}
		}
		else
		{
			logger.error("Null Message received by MMIDServiceImpl");
		}
	}
	catch (Exception e) 
	{
		logger.error(e, e);
		throw new Exception(e);
	}

	logger.error("Final MMID Val returned : " + result);
    return result;
}

}
