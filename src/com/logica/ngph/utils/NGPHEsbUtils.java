package com.logica.ngph.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.action.ISOMsgAction;
import com.logica.ngph.common.dtos.ErrorCodes;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.ResponseBean;
import com.logica.ngph.esb.daos.EsbServiceDao;

/**
 *  @author guptarb
 *  
 *   A Generic Utility Class to keep resources for NGPH
 */


public class NGPHEsbUtils {
	
	static Logger logger = Logger.getLogger(NGPHEsbUtils.class);
	private final static String propName = "System.properties";
	
	private static EsbServiceDao esbServiceDao;

	/**
	 * @param esbServiceDao the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}

	//A Global Time Stamp Variable that will be used in IMPS Handler Service which itself is a ESB Scheduler
	//public static Timestamp lastCommTS = new Timestamp(Calendar.getInstance().getTimeInMillis());
	public static Timestamp lastCommTS = null;

	public static Timestamp lastNPCILogOnTS = null;
	public static Timestamp lastNPCIECHOTS = null;
	
	// A global Stan and msgId Map that will be accessed by several Services. 
	public static Map<String, String> stanMsgMap = new HashMap<String, String>();
	
	// A global msgId and Response Object Map that will be accessed by several Services. 
	public static Map<String, ResponseBean> msgIdResObjMap = new HashMap<String, ResponseBean>();

	/* A Global Map that will contain info about error codes
	 * Key is errorCode+ErrorChnl and value is ErrorCode Pojo Object containing single row at a time*/
	public static HashMap<String, ErrorCodes> errorCodeMap= new HashMap<String, ErrorCodes>();

	/* A Global for Auto Response Management purpose.
	 * If Auto Response is false, set response bean object in this map with key as Canonical.getTxnRef() and value as response bean object
	 */
	public static HashMap<String, ResponseBean> CBSResponseMap= new HashMap<String, ResponseBean>();
	
	public static void populateCBSResponseMap(String txnRef, ResponseBean obj )
	{
		CBSResponseMap.put(txnRef, obj);
	}
	/*
	 * This method will store Stan as Key and Message Id as Value.
	 * This method will be invoked from both IMPS Adapter as well as IMPS Channel Service
	 */
	public static void populateStanMsgId(String stan, String msgId)
	{
		stanMsgMap.put(stan, msgId);
	}
	
	public static Map<String, String> connectionStatusMap = Collections.synchronizedMap(new HashMap<String, String>());
	

	/*
	 * This method will store and retrieve Message Id as Key and  ResponseBean as Value.
	 * Where ResponseBean object is a Pojo class that will hold response code and other
	 * useful info needed to process the response.
	 * This method will be invoked from both IMPS Adapter as well as IMPS Channel Service
	 */

	public static void populateResponseObject(String msgId, ResponseBean obj )
	{
		msgIdResObjMap.put(msgId, obj);
	}
	
	/*//FIXME For testing purpose
	public static void main(String[] args)
	{
		String res = getDerivedAccNum("BeneficiaryCustAcct","444488888888");
		System.out.println("Final Value returned is : " +res);
	}*/
	
	public static HashMap<String,String> getDerivedAccNum(String varName, String varValue)
	{
			//String result = null;
			Properties props = new Properties();
			HashMap<String, String> dataVals = new HashMap<String, String>();
			int startindex=0;
	
			try 
			{
				//Load Prop File
				props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
				
				//Read Canonical Variable dimensions
				String propVals = props.getProperty(varName+"FORMAT");
				
				//Split the String based on Delimiter ;
				String [] splitter = propVals.split(";"); 
				String propVal = null;
				int totLen = 0;
				for(int i=0;i<splitter.length;i++)
				{
					propVal = props.getProperty(splitter[i]);
					if(propVal!=null && StringUtils.isNotBlank(propVal) && StringUtils.isNotEmpty(propVal))
					{
						totLen = totLen + Integer.parseInt(propVal);
					}
				}
				logger.info("The total length required is" + totLen);
				if (totLen <= varValue.length())
				{
					//Iterate over loop and store variables and their value in ArrayList
					for(int i=0;i<splitter.length;i++)
					{
						propVal = props.getProperty(splitter[i]);
						if(propVal!=null && StringUtils.isNotBlank(propVal) && StringUtils.isNotEmpty(propVal))
						{
							String varData = varValue.substring(startindex, startindex + Integer.parseInt(propVal));
							dataVals.put(splitter[i], varData);
							startindex = startindex + Integer.parseInt(propVal);
						}
					}
					
					/*Iterator<String> itr = dataVals.keySet().iterator();
					while(itr.hasNext())
					{
						String key = itr.next(); 
						if(key.contains("ACNUM"))
						{
							result = dataVals.get(key);
						}
					}*/
				}
				else
				{
					logger.info("Length of value received does not match the sum of configured length of account number components");
					//result = varValue;
				}
			} 
			catch (Exception e) 
			{
				logger.error("Exception Occured While Fetching Derived Acc Num", e);
			}
			
		return dataVals;
	}

	public static void populateErrorCodes()
	{
		try
		{
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			List<ErrorCodes> errorPojoList = esbServiceDao.getErrorCodes();
			convertListToMap(errorPojoList);
		}
		catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	private static void convertListToMap(List<ErrorCodes> obj)
	{
		try
		{
			if(obj!=null && !obj.isEmpty())
			{
				for(int i=0;i<obj.size();i++)
				{
					//Fetch ErrorCodes bean Object from List 
					ErrorCodes beanObj =  obj.get(i);
					
					//Fetch ErrorCode and ErrChannle
					String errCode = beanObj.getErrCode();
					String errChnl = beanObj.getErrChnl();
					
					//If ErrChannel is null then do not add it as key in global Map
					if(errChnl!=null && StringUtils.isNotBlank(errChnl) && StringUtils.isNotEmpty(errChnl))
					{
						errorCodeMap.put(errCode+errChnl, beanObj);
					}
					else
					{
						errorCodeMap.put(errCode, beanObj);
					}
				}
			}
			else
			{
				logger.warn("Null ErrorCode List Fetched from DB");
			}
		}
		catch (Exception e) {
			logger.error(e, e);
		}
		
	}
	
	/*public static void main(String[] args) {
		
		ApplicationContextProvider.initializeContextProvider();
		System.out.println(filterCharSet("RajatT"));
	}*/
	public static String filterCharSet(String value)
	{
		StringBuilder result = new StringBuilder();
		String identifier = null;
		List<String> dbCharSets = null;
		try
		{
			if(StringUtils.isNotBlank(value) && StringUtils.isNotEmpty(value))
			{
				esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
				String impsCharSet = esbServiceDao.getInitialisedValue("IMPSNONCHARSET");
				logger.info("IMPS Char Sets fethced from DB : " + impsCharSet);
				
				char elements[] = impsCharSet.toCharArray();
				dbCharSets = new ArrayList<String>();
				for(int i=0;i<elements.length;i++)
				{
					dbCharSets.add(elements[i]+"");
				}
				
				char argsElemsnts[] = value.toCharArray();
				for(int j=0;j<argsElemsnts.length;j++)
				{
					if(dbCharSets.contains(argsElemsnts[j]+""))
					{
						identifier = " ";
					}
					else
					{
						identifier = argsElemsnts[j]+"";
					}
					
					result.append(identifier);
				}
				
				
			}
			else
			{
				logger.warn("filterCharSet function gets null or empty string");
			}
		}
		catch (Exception e) {
			logger.error(e,e);
		}
		
		return result.toString();
	}
}
