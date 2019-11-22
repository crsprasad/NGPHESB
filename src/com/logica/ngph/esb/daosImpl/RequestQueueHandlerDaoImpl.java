package com.logica.ngph.esb.daosImpl;

import java.sql.Clob;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.logica.ngph.esb.daos.RequestQueueHandlerDao;

public class RequestQueueHandlerDaoImpl implements RequestQueueHandlerDao{
	
	private  JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	static Logger logger = Logger.getLogger(RequestQueueHandlerDaoImpl.class);

	
	public String getProviderList()throws Exception
	{
		String result=null;
		Clob clob = null;
		try
		{
			String bicQuery = "select INIT_VALUE from initialisationm where INIT_ENTRY='PROVIDERS'";
			clob = jdbcTemplate.queryForObject(bicQuery, Clob.class);
			int clobLength = (int) clob.length();
			result = clob.getSubString(1, clobLength);
			logger.info("Provider Names fetched from DB are : " + result);
		
		}
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncompatibleClassChangeError e) {
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		return result;
	
		
	}
	
	public String retrieveEICode(String providerESB)throws Exception
	 {
		 String result=null;
		 String Query="select ei_code from TA_EI where INPUT_SRC_QUEUE ='" + providerESB +"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
			// System.out.println("Value is ::" + result);
		 }
		 catch(EmptyResultDataAccessException e)
			{
			 logger.error(e,e);
			}
		 catch (IncorrectResultSizeDataAccessException e) 
		 {
			 logger.error(e,e);
		 }
		 catch (Exception e) 
		 {
			 logger.error(e,e);
			 throw new Exception(e);
         }
		 return result;
	 }

	public List<String> FetchInput_Dest_Queue()throws Exception
	{
		List<String> EsbProviders = null;
		String query = "select INPUT_DEST_QUEUE from ta_ei";
		try 
		{
			EsbProviders = jdbcTemplate.queryForList(query, String.class);
		}
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		return EsbProviders;
	}
	
	public List<String> FetchOutput_Dest_Queue()throws Exception
	{
		List<String> EsbProviders = null;
		String query = "select OUTPUT_DEST_QUEUE from ta_ei";
		try 
		{
			EsbProviders = jdbcTemplate.queryForList(query, String.class);
			//logger.info("Values Fetched for INPUT_DEST_QUEUE from TA_EI "+ EsbProviders);
		} 
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		return EsbProviders;
	}
	
	public String getQueueInitializer()throws Exception
	{
		String result=null;
		Clob clob = null;
		try
		{
			String bicQuery = "select INIT_VALUE from initialisationm where INIT_ENTRY='QNGESBQS'";
			clob = jdbcTemplate.queryForObject(bicQuery, Clob.class);
			int clobLength = (int) clob.length();
			result = clob.getSubString(1, clobLength);
			logger.info("Queue Names fetched from DB are : " + result);
		
		} 
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			logger.debug("Exception Occured while fetching values from from TA_EI table ");
			throw new Exception(e);
		}
		return result;
	}
	public List<String> getEsbProviders()throws Exception 
	{
		List<String> EsbProviders = null;

		//String query = "select EI_ESB_PROVNAME from ta_ei";
		String query = "select INPUT_SRC_QUEUE from ta_ei";
		try 
		{
			EsbProviders = jdbcTemplate.queryForList(query, String.class);
			logger.info("Values Fetched for INPUT_SRC_QUEUE from TA_EI "+ EsbProviders);
		} 
		catch(EmptyResultDataAccessException e)
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
		return EsbProviders;
	}


	public String getProviderEsb(String hostID)throws Exception
	{
		 String result=null;
		 
		 String Query="select EI_ESB_PROVNAME from TA_EI where EI_CODE ='" + hostID +"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
			// System.out.println("Value is ::" + result);
		 }
		 catch(EmptyResultDataAccessException e)
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
		 return result;
	}
	
	public String getInput_Dest_Queue(String srcQueue)throws Exception
	{

		String dest_Queue = null;

		String query = "select INPUT_DEST_QUEUE from ta_ei where INPUT_SRC_QUEUE='" + srcQueue +"'";
		try 
		{
			dest_Queue = jdbcTemplate.queryForObject(query, String.class);
			logger.info("Values Fetched for INPUT_SRC_QUEUE from TA_EI "+ dest_Queue);
		} 
		catch(EmptyResultDataAccessException e)
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
		return dest_Queue;
	}
	
	public List<String> getOutput_SrcProviders()throws Exception
	{
		List<String> EsbProviders = null;

		//String query = "select EI_ESB_PROVNAME from ta_ei";
		String query = "select OUTPUT_SRC_QUEUE from ta_ei";
		try 
		{
			EsbProviders = jdbcTemplate.queryForList(query, String.class);
			logger.info("Values Fetched for OUTPUT_SRC_QUEUE from TA_EI "+ EsbProviders);
		}
		catch(EmptyResultDataAccessException e)
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
		return EsbProviders;
	}
	
	public String getOutPutQueueByEI(String eiCode) throws Exception
	{

		String dest_Queue = null;

		String query = "select OUTPUT_DEST_QUEUE from ta_ei where EI_CODE=?";
		try 
		{
			dest_Queue = jdbcTemplate.queryForObject(query, new Object[]{eiCode},String.class);
		}
		catch(EmptyResultDataAccessException e)
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
		return dest_Queue;
		
	}
	public String getOutput_Dest_Queue(String srcQueue)throws Exception
	{
		String dest_Queue = null;

		String query = "select OUTPUT_DEST_QUEUE from ta_ei where OUTPUT_SRC_QUEUE='" + srcQueue +"'";
		try 
		{
			dest_Queue = jdbcTemplate.queryForObject(query, String.class);
			logger.info("Values Fetched for OUTPUT_SRC_QUEUE from TA_EI "+ dest_Queue);
		}
		catch(EmptyResultDataAccessException e)
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
		return dest_Queue;
	}

	public String getDstHost(String dstid)throws Exception
	{
		String dest_Queue = null;

		String query = "select OUTPUT_DEST_QUEUE from ta_ei where EI_CODE='" + dstid +"'";
		try 
		{
			dest_Queue = jdbcTemplate.queryForObject(query, String.class);
			logger.info("Values Fetched for OUTPUT_SRC_QUEUE from TA_EI "+ dest_Queue);
		}
		catch(EmptyResultDataAccessException e)
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
		return dest_Queue;
	}

}
