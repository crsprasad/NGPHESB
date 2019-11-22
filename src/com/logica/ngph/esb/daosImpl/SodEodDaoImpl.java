package com.logica.ngph.esb.daosImpl;

import java.sql.Clob;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.logica.ngph.common.ConstantUtil;
import com.logica.ngph.esb.Dtos.SodEodTaskTDto;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.SodEodDao;
import com.logica.ngph.utils.SodEodUtil;

public class SodEodDaoImpl implements SodEodDao{
	
	static Logger logger = Logger.getLogger(SodEodDaoImpl.class);

	private JdbcTemplate jdbcTemplate;	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) 
	{
		this.jdbcTemplate = jdbcTemplate;
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String,Object> getLocalBranch() throws Exception
	{
		logger.info("getLocalBranch STARTS..");
		
		Map<String,Object> taskMap = new HashMap<String, Object>();
		
		String branchCode = null;
		String branchName = null;
		SqlRowSet srs = null;
		String status = null;
		List<String>  sodeodtaskList = null;
		
		Clob clob = null;
		
		String branchQuery = "select INIT_VALUE from INITIALISATIONM where INIT_ENTRY=?";
		clob= jdbcTemplate.queryForObject(branchQuery, new Object[]{"DEFBRANCH"}, Clob.class);
		int clobLength = (int) clob.length();
		branchCode = clob.getSubString(1, clobLength);
		
		String branchNameQuery = "select BRANCH_NAME from BRANCHES where BRANCH_CODE=?";
		branchName= jdbcTemplate.queryForObject(branchNameQuery, new Object[]{branchCode},String.class);

		taskMap.put(ConstantUtil.ACTION_BRANCH, branchCode+"-"+branchName);

		String businessDayMQuery = "select BUSDAY_DATE ,BUSDAY_STATUS from TA_BUSINESSDAYM where BUSDAY_BRANCH=?";
		srs = jdbcTemplate.queryForRowSet(businessDayMQuery, new Object[]{branchCode});
		
		while (srs.next()) 
		{
			status = srs.getString("BUSDAY_STATUS");
			taskMap.put(ConstantUtil.BUSINESSDATE, srs.getDate("BUSDAY_DATE"));
			taskMap.put(ConstantUtil.STATUS, status);
		}
		
		if(status.equals("B"))
		{}
		else if(status.equals("N"))
		{}
	
		else
		{
			System.out.println(branchCode + "\t" + status + "\t" + 0);
			//FIXME branch specific SOD EOD to be done BRANCH =? and branchCode,
			String branchTaskQuery = "select concat(ID,concat('-',SODEOD_DESC)) as taskName from TA_SODEODTASKM where SODEOD_FOR = ? and DELETED =?  order by SODEOD_SEQUENCE";
			sodeodtaskList = jdbcTemplate.queryForList(branchTaskQuery, String.class, new Object[]{status,0});
			
			String transanctionQuery = "select ID from TA_SODEODTASKT where BRANCH=? and STATUS ='2' and SODEODT_FOR=? and BUSDATE=?";
			List<String> sodEodTransactionList = jdbcTemplate.queryForList(transanctionQuery,String.class, new Object[]{branchCode,status,taskMap.get(ConstantUtil.BUSINESSDATE)});
			
			List<String> tempList = new ArrayList<String>();
		
			for(int i=0;i<sodeodtaskList.size();i++)
			{
				String[] taskNameArray = sodeodtaskList.get(i).split("-");
			
				for(String transac:sodEodTransactionList)
				{
					if(transac.equals(taskNameArray[0]))
					{
						tempList.add(sodeodtaskList.get(i));
					}
				}
			}
			sodeodtaskList.removeAll(tempList);
		}
	
		taskMap.put(ConstantUtil.TASKLIST, sodeodtaskList);

		logger.info("getLocalBranch ENDS..");
		return taskMap;
	}
	

	private int updateSodEodDto(SodEodTaskTDto obj)
	{
		int result = 0;
		int i = 0;
		Object[] valuesArray =	new Object[20];
		StringBuilder query = new StringBuilder();
		query.append("UPDATE TA_SODEODTASKT SET");
		query.append(NgphEsbConstants.NGPH_SPACE);
		
		if(obj.getBranch()!= null)
		{
			query.append("BRANCH = ?,");
			valuesArray[i] = obj.getBranch();
			i++;
		}
		if(obj.getBusinessDate()!= null)
		{
			query.append("BUSDATE = ?,");
			valuesArray[i] = obj.getBusinessDate();
			i++;
		}
		if(obj.getSodOrEod() != null)
		{
			query.append("SODEODT_FOR = ?,");
			valuesArray[i] = obj.getSodOrEod();
			i++;
		}
		if(obj.getUserId() != null)
		{
			query.append("USERID = ?,");
			valuesArray[i] = obj.getUserId();
			i++;
		}
					
		//Fetch the final String removing the last extra , value
		String stringQuery  = query.toString().substring(0,query.toString().length()-1);
		
		stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
		stringQuery = stringQuery.concat("WHERE ID = ?");
		valuesArray[i]= obj.getTaskId();
		
		System.out.println("Query===> " + stringQuery);
		//to avoid null extra values and to avoid invalid column type errors
		//we need to have columns used in query and values in this array should be same in count
		Object[] actualArray = null;
		if(i < valuesArray.length)
		{
			actualArray = new Object[i+1];
			for(int j=0; j<actualArray.length; j++)
			{
				actualArray[j] = valuesArray[j];
			}
			//its not required then after so sending to garbage collection 
			valuesArray = null;
		}
		try
		{			
			result = jdbcTemplate.update(stringQuery, actualArray); 
		}
		catch (Exception e)
		{
			logger.error(e,e);
		}
		return result;
	}
	
	private void insertSodEodDto(SodEodTaskTDto obj)
	{
			int i = 0;
			Object[] valuesArray =	new Object[20];
			String query = null;
			StringBuilder columns = new StringBuilder();
			StringBuilder count = new StringBuilder();
			
			columns.append("insert into TA_SODEODTASKT");
			columns.append(NgphEsbConstants.NGPH_SPACE);
			columns.append("(");
			
			count.append("values");
			count.append(NgphEsbConstants.NGPH_SPACE);
			count.append("(");
			
			if(obj.getBranch()!= null)
			{
				columns.append("BRANCH,");
				count.append("?,");
				valuesArray[i] = obj.getBranch();
				i++;
			}
			if(obj.getBusinessDate()!= null)
			{
				columns.append("BUSDATE,");
				count.append("?,");
				valuesArray[i] = obj.getBusinessDate();
				i++;
			}
			if(obj.getUserId()!= null)
			{
				columns.append("USERID,");
				count.append("?,");
				valuesArray[i] = obj.getUserId();
				i++;
			}
			if(obj.getTaskId()!= null)
			{
				columns.append("ID,");
				count.append("?,");
				valuesArray[i] = obj.getTaskId();
				i++;
			}
			if(obj.getSodOrEod() != null)
			{
				columns.append("SODEODT_FOR,");
				count.append("?,");
				valuesArray[i] = obj.getSodOrEod();
				i++;
			}
			//Fetch the final String removing the last extra , value
			String colVal = columns.toString().substring(0,columns.toString().length()-1);
			String countVal = count.toString().substring(0,count.toString().length()-1);
			//Add closing braces to final String
			colVal = colVal.concat(")");
			countVal = countVal.concat(")");
			
			//Constructing the final Query Value
			query = colVal + countVal;
			logger.info("Query Constructed : " + query);
			

			//to avoid null extra values and to avoid invalid column type errors
			//we need to have columns used in query and values in this array should be same in count
			Object[] actualArray = null;
			if(i < valuesArray.length)
			{
				actualArray = new Object[i];
				for(int j=0; j<actualArray.length; j++)
				{
					actualArray[j] = valuesArray[j];
				}
				//its not required then after so sending to garbage collection 
				valuesArray = null;
			}
			try
			{			
				jdbcTemplate.update(query, actualArray); 
			}
			catch (Exception e)
			{
				logger.error(e,e);
			}
		}
		
	
	private void saveSodEodT(SodEodTaskTDto sodEodTaskTDto) 
	{
		int result = updateSodEodDto(sodEodTaskTDto);
		{
			if(result==0)
			{
				logger.info("No Update, hence insert");
				insertSodEodDto(sodEodTaskTDto);
			}
		}
		//getHibernateTemplate().saveOrUpdate(SodEodUtil.converSOdEodTDtoToEntity(sodEodTaskTDto));	
	}
	

	@SuppressWarnings("rawtypes")
	public void updateSodEodT(final String errorMessage,final String taskId)throws Exception
	{
		logger.info("updateSodEodT STARTS..");
		
		String updateSodEodT = "update TA_SODEODTASKT set ERROR = ?,ENDTIME = ?,STATUS=? where ID=?";
        int result = jdbcTemplate.update(updateSodEodT, new Object[]{errorMessage,SodEodUtil.getCurrentTime(),"3",taskId});
        logger.info("No of Rows Updated are : " + result);

		logger.info("updateSodEodT ENDS..");

	/*	getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session) throws HibernateException, SQLException 
			{
				  Query query ;
		    	  query = session.createQuery("update SODEODTASKT set error = :errorMessage ,endTime = :currentTime ,status = :status where taskId = :taskId");
	              query.setTimestamp("currentTime",SodEodUtil.getCurrentTime());
	              query.setString("errorMessage",errorMessage);
	              query.setString("status","3");
	              query.setString("taskId",taskId);
	              int i=  query.executeUpdate();
	              
	              logger.debug("number of rows affected"+i);
	              return null;
           }
       });*/
	}
	
	@SuppressWarnings("rawtypes")
	public void updateSodEodTComp(final String taskId)throws Exception
	{

		logger.info("updateSodEodTComp STARTS..");
		
		String updateSodEodTComp = "update TA_SODEODTASKT set ENDTIME = ?,STATUS=? where ID=?";
        int result = jdbcTemplate.update(updateSodEodTComp, new Object[]{SodEodUtil.getCurrentTime(),"2",taskId});
        logger.info("No of Rows Updated are : " + result);

		logger.info("updateSodEodTComp ENDS..");
		
		
		/*getHibernateTemplate().executeFind(new HibernateCallback() 
		{
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
			 Query query ;
		
		    	  query = session.createQuery("update SODEODTASKT set endTime = :currentTime ,status = :status where taskId = :taskId");
	              query.setTimestamp("currentTime",SodEodUtil.getCurrentTime());
	              query.setString("status","2");
	              query.setString("taskId",taskId);
	              int i=  query.executeUpdate();

	              if(logger.isDebugEnabled())
	              {
	      			logger.debug("Number of Rows Affected"+i);
	              }
	      		  if(logger.isInfoEnabled())
	      		  {
	      			logger.info("Number of Rows Affected"+i);
	      		  }
	      		  if(logger.isEnabledFor(Level.ERROR))
	      		  {
	      			logger.error("Number of Rows Affected"+i);
	      		  }
	              
               return null;
           }
       });*/
}

	@SuppressWarnings("rawtypes")
	private void updateBusinessDayM(final Timestamp businessDay,final String branch)throws Exception
	{
		logger.info("updateBusinessDayM STARTS..");
		
		String updateBusinessDayM = "update TA_BUSINESSDAYM set BUSDAY_DATE = ? where BUSDAY_BRANCH=?";
        int result = jdbcTemplate.update(updateBusinessDayM, new Object[]{businessDay,branch});
        logger.info("No of Rows Updated are : " + result);

		logger.info("updateBusinessDayM ENDS..");
		
		
		/*getHibernateTemplate().executeFind(new HibernateCallback() {
		
		 public Object doInHibernate(Session session) throws HibernateException, SQLException 
		 {
			 Query businessDayMquery ;
		
			 businessDayMquery = session.createQuery("update BusinessDayM set businessDay = :businessDay  where branch = :branch");
			 businessDayMquery.setTimestamp("businessDay",businessDay); 
			 businessDayMquery.setString("branch",branch);
	         int i=  businessDayMquery.executeUpdate();
	         logger.debug("number of rows affected in TA_BUSINESSDAYM"+i);
              return null;
          }
      });*/
}

	@SuppressWarnings("rawtypes")
	private void updateEI(final int status)throws Exception
	{
		logger.info("updateEI STARTS..");
		
		String updateEI = "update TA_EI set EI_FEEDIN = ?,EI_FEEDOUT=?";
        int result = jdbcTemplate.update(updateEI, new Object[]{status,status});
        logger.info("No of Rows Updated are : " + result);

		logger.info("updateEI ENDS..");
		
		
		/*getHibernateTemplate().executeFind(new HibernateCallback() 
		{
			public Object doInHibernate(Session session) throws HibernateException, SQLException 
			{
				Query eiQuery ;
				eiQuery = session.createQuery("update EI set feedIn = :feedIn , feedout =:feedout");
				eiQuery.setInteger("feedIn", status);
				eiQuery.setInteger("feedout", status);
			
	              int i=  eiQuery.executeUpdate();
	              if(logger.isDebugEnabled())
	              {
		      			logger.debug("Number of Rows Affected in TA_EI"+i);
		      	  }
	              if(logger.isInfoEnabled())
	              {
		      			logger.info("Number of Rows Affected in TA_EI"+i);
		      	  }
	              if(logger.isEnabledFor(Level.ERROR))
	              {
		      			logger.error("Number of Rows Affected in TA_EI"+i);
		      	  }
	             
              return null;
          }
      });*/
}
	

	@SuppressWarnings({ "unchecked", "deprecation" })
	public void setNextBusinessDate(SodEodTaskTDto sodEodTaskTDto) throws Exception
	{
		logger.info("setNextBusinessDate STARTS..");
		
		String taskId = sodEodTaskTDto.getTaskId();
		/*saveSodEodT(sodEodTaskTDto);*/
		boolean isError = false;
		try
		{
			String holidayMQuery = "select HOLIDAYDATE from  TA_HOLIDAYM where BRANCH=? and CURRENCY is null and HOLIDAYDATE > current_date and HOLIDAYDATE >? order by HOLIDAYDATE";
			String branch = sodEodTaskTDto.getBranch();
			
			List<Object> holidayList =	jdbcTemplate.queryForList(holidayMQuery, Object.class, new Object[]{branch,sodEodTaskTDto.getBusinessDate()});
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
			
			Timestamp businessDay = getUpdateBusinessDay(sodEodTaskTDto.getBusinessDate());
		
			for(Object holiday:holidayList)
			{
				try 
				{
					Date parsedHoliday = 	dateFormat.parse(holiday.toString());
					java.sql.Timestamp holidayTimeStamp = new java.sql.Timestamp(parsedHoliday.getTime()); 
			
					int compareResult = businessDay.compareTo(holidayTimeStamp);
					
					if(compareResult == 0)
					{
						long oneDay = 1 * 24 * 60 * 60 * 1000;	
						businessDay.setTime(businessDay.getTime()+oneDay);
					}
					logger.info("Next business day : "+businessDay);
			
				} catch (Exception parseException) 
				{
					isError = true;
					logger.error(parseException, parseException);
				}
		}

			String weeklyHolidayQry = "select CURRENTDAY from TA_WEEKLYHOLIDAY where BRANCH=? and ISWORKING=?";
			List<Integer> weeklyHolidayList = jdbcTemplate.queryForList(weeklyHolidayQry, Integer.class, new Object[]{branch,0});
			for(Integer week:weeklyHolidayList)
			{
				if(week.equals(businessDay.getDay()))
				{
					long oneDay = 1 * 24 * 60 * 60 * 1000;	
					businessDay.setTime(businessDay.getTime()+oneDay);
				}
			}
		
			updateBusinessDayM(businessDay, branch);
		}
		catch(Exception exception)
		{
			updateSodEodT(exception.getMessage(),taskId);	
			isError = true;
			logger.error(exception,exception);
		}
		
		if(!isError)
		{
			updateSodEodTComp(taskId);
		}

		logger.info("setNextBusinessDate ENDS..");
}


	private Timestamp getUpdateBusinessDay(Timestamp businessDay) throws Exception
	{
	
		logger.info("getUpdateBusinessDay STARTS..");
		
		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
		int result = currentTimestamp.compareTo(businessDay);
		long oneDay = 1 * 24 * 60 * 60 * 1000;	
  
		if(result == 0)
		{
			businessDay.setTime(businessDay.getTime()+oneDay);
		}
		else if(result > 0)
		{
			businessDay.setTime(currentTimestamp.getTime()+oneDay);
		}

		logger.info("getUpdateBusinessDay ENDS..");

		return  businessDay;
	}



	@SuppressWarnings("unchecked")
	public void validatePaymentNonfinalityStatus(String branchCode,SodEodTaskTDto sodEodTaskTDto)throws Exception
	{
		logger.info("validatePaymentNonfinalityStatus STARTS..");

		saveSodEodT(sodEodTaskTDto);
		boolean isError = false;
		String taskId = sodEodTaskTDto.getTaskId();
		
		String nonFinalityQuery = "select INIT_VALUE from INITIALISATIONM where INIT_BRANCH=? and INIT_ENTRY=?";
		Clob clob = jdbcTemplate.queryForObject(nonFinalityQuery, Clob.class, new Object[]{branchCode,"NONFINSTATUSES"});
		int clobLength = (int) clob.length();
		String nonFinalityt = clob.getSubString(1, clobLength);
		
		String[] initValueArray = nonFinalityt.split(";");
		
		String messageTxQuery = "select MSGS_MSGSTS from TA_MESSAGES_TX where MSGS_BRANCH=?";
		List<String> messageTxStatusList= jdbcTemplate.queryForList(messageTxQuery, String.class, new Object[]{branchCode});
		
		for(String msgStatus:messageTxStatusList)
		{
			for(int i=0;i<initValueArray.length;i++)
			{
				if(msgStatus.equals(initValueArray[i]))
				{
					isError = true;
					updateSodEodT("Payments in Non Finality Status",taskId);	
					throw new Exception("Payments in Non Finality Status");
				}
			}
		}
	
		if(!isError)
		{
			updateSodEodTComp(taskId);
		}
		
		logger.info("validatePaymentNonfinalityStatus ENDS..");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void moveFinalStatusToHistoryTable(final String branchCode,final SodEodTaskTDto sodEodTaskTDto) throws Exception
	{
		logger.info("moveFinalStatusToHistoryTable STARTS..");
		
		boolean isError = false;
		String taskId = sodEodTaskTDto.getTaskId();
		try
		{
			saveSodEodT(sodEodTaskTDto);
			int result = this.jdbcTemplate.update("call PROCEOD()");
			logger.info("No of Rows Affected : " + result);
			
			
			/*getHibernateTemplate().execute(new HibernateCallback() 
			{
				public Object doInHibernate(Session session) throws HibernateException, SQLException
				{
					saveSodEodT(sodEodTaskTDto);
					Connection connection=session.connection(); 
					CallableStatement st = connection.prepareCall("call PROCEOD()");
					//st.setString(1, branchCode);

					int i =	st.executeUpdate();
					connection.close();

					if(logger.isDebugEnabled())
					{
		      			logger.debug("Number of Rows Affected"+i);
		      		}
		      		if(logger.isInfoEnabled())
		      		{
		      			logger.info("Number of Rows Affected"+i);
		      		}
		      		if(logger.isEnabledFor(Level.ERROR))
		      		{
		      			logger.error("Number of Rows Affected"+i);
		      		}
		      		return null;
	          
				}
			}
			);*/
		}
		catch (Exception e) 
		{
			isError = true;	
			updateSodEodT(e.getMessage(),taskId);
			logger.error(e, e);
		}
		
		if(!isError)
		{
			updateSodEodTComp(taskId);
		}
		
		logger.info("moveFinalStatusToHistoryTable ENDS..");
	}


	public void openCloseInboundOutboundFeeds(SodEodTaskTDto sodEodTaskTDto,int status) throws Exception
	{
		logger.info("openCloseInboundOutboundFeeds STARTS..");
		
		boolean isError = false;
		String taskId = sodEodTaskTDto.getTaskId();
		try
		{
			saveSodEodT(sodEodTaskTDto);
			updateEI(status);
		}
		catch (Exception e) 
		{
			isError = true;	
			updateSodEodT(e.getMessage(),taskId);
			logger.error(e, e);
		}
		
		if(!isError)
		{
			updateSodEodTComp(taskId);	
		}
		logger.info("openCloseInboundOutboundFeeds ENDS..");
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteHistoryDataBeyond(final String branchCode,final SodEodTaskTDto sodEodTaskTDto) throws Exception
	{/*
		boolean isError = false;
		String taskId = sodEodTaskTDto.getTaskId();
		try
		{
			getHibernateTemplate().execute(new HibernateCallback() 
			{
				public Object doInHibernate(Session session) throws HibernateException, SQLException
				{
					saveSodEodT(sodEodTaskTDto);
					Connection connection=session.connection(); 
					CallableStatement st = connection.prepareCall("call ARCHTABLEMOVE(?)");
					st.setString(1, branchCode);

					int i =	st.executeUpdate();
					connection.close();
					logger.info("updated rows"+i) ;
					return null;
	          
				}
			});
		}
		catch (Exception e) 
		{
			isError = true;	
			updateSodEodT(e.getMessage(),taskId);
			logger.error(e,e);
		}
	
		if(!isError)
		{
			updateSodEodTComp(taskId);
		}
	*/}



	@SuppressWarnings("rawtypes")
	public void updateBusinessDayM(final String branchCode, final String businessDaystatus) throws Exception
	{
		logger.info("updateBusinessDayM STARTS..");

		String updateBusinessDayM = "update TA_BUSINESSDAYM set BUSDAY_STATUS = ? where BUSDAY_BRANCH=?";
        int result = jdbcTemplate.update(updateBusinessDayM, new Object[]{businessDaystatus,branchCode});
        logger.info("No of Rows Updated are : " + result);

		/*getHibernateTemplate().executeFind(new HibernateCallback() 
		{
		
			public Object doInHibernate(Session session) throws HibernateException, SQLException 
			{
				Query businessDayStatusquery ;
				businessDayStatusquery = session.createQuery("update BusinessDayM set businessDayStatus = :businessDayStatus  where branch = :branch");
				businessDayStatusquery.setString("businessDayStatus",businessDaystatus); 
				businessDayStatusquery.setString("branch",branchCode);
	              
	            int i=  businessDayStatusquery.executeUpdate();
	              
	            logger.info("number of rows affected in TA_BUSINESSDAYM"+i);

	            return null;
			}
		});*/

		logger.info("updateBusinessDayM ENDS..");
	}

	public void updateTaLimits() throws Exception
	{
		logger.info("updateTaLimits STARTS..");

		String updateTaLimits = "update ta_limits set available_crlimit=credit_limit, available_drlimit=debit_limit";
        int result = jdbcTemplate.update(updateTaLimits);
        logger.info("No of Rows Updated are : " + result);
		logger.info("updateTaLimits ENDS..");
	}
	
	public List<String> getbranches()throws Exception
	{
		logger.info("getbranches STARTS..");
		String branchesQuery = "select branch_code from branches";
		List<String> branchList = new ArrayList<String>();
		
		SqlRowSet srs = jdbcTemplate.queryForRowSet(branchesQuery); 
		while(srs.next())
		{
			branchList.add(srs.getString("branch_code"));
		}
		
		logger.info("getbranches ENDS..");
		return branchList;
	}

	public List<Integer> getSodEodStatus(List<String> taskIdList) throws Exception 		    
	{
		logger.info("getSodEodStatus STARTS..");
		
		String taskStatusQuery = "select STATUS from TA_SODEODTASKT where ID=?";
		List<Integer> taskStatusList = new ArrayList<Integer>();
		
		for(int i=0;i<taskIdList.size();i++)
		{
			String taskId = taskIdList.get(i);
	
			@SuppressWarnings("unchecked")
			List<Integer> statusList = 	jdbcTemplate.queryForList(taskStatusQuery, Integer.class,new Object[]{taskId});
		
			if(statusList !=null && !statusList.isEmpty() )
			{
				taskStatusList.add(statusList.get(0));
			}
		}
		
		logger.info("getSodEodStatus ENDS..");
		return taskStatusList;

	}

	public Timestamp getCurBusDayforBranch (String Branch) throws Exception
	{
		Timestamp ts = null;
		try
		{
			String busDayQuery = "Select BUSDAY_DATE FROM TA_BUSINESSDAYM WHERE BUSDAY_BRANCH = ?";
			ts = jdbcTemplate.queryForObject(busDayQuery, new Object[]{Branch}, Timestamp.class);
		}
		catch (EmptyResultDataAccessException e) 
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
		return ts;
	}
}
