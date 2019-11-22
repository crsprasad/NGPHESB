package com.logica.ngph.esb.daosImpl;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.ErrorCodes;
import com.logica.ngph.common.dtos.GenericFilePojo;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.esb.Dtos.BGMast;
import com.logica.ngph.esb.Dtos.CanonicalFieldPojo;
import com.logica.ngph.esb.Dtos.CustomerInfo;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.Dtos.FieldCanonicalAttribute;
import com.logica.ngph.esb.Dtos.LcMast;
import com.logica.ngph.esb.Dtos.MessageFormats;
import com.logica.ngph.esb.Dtos.Raw_Msgs;
import com.logica.ngph.esb.Dtos.ReportDto;
import com.logica.ngph.esb.Dtos.ResponseBean;
import com.logica.ngph.esb.Dtos.Rules;
import com.logica.ngph.esb.Dtos.TcpBean;
import com.logica.ngph.esb.Dtos.UserInfoBean;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.rowmappers.CanonicalFieldMapper;
import com.logica.ngph.esb.rowmappers.CustomerRowMapper;
import com.logica.ngph.esb.rowmappers.ErrorCodesRowMapper;
import com.logica.ngph.esb.rowmappers.IMPSRespRowMapper;
import com.logica.ngph.esb.rowmappers.MessageFieldsMapRowMapper;
import com.logica.ngph.esb.rowmappers.MessageFieldsResultSetMapper;
import com.logica.ngph.esb.rowmappers.MessageFormatsRowMapper;
import com.logica.ngph.esb.rowmappers.RawMsgsRowMapper;
import com.logica.ngph.esb.rowmappers.RulesSubMapper;
import com.logica.ngph.esb.rowmappers.TcpRowMapper;
import com.logica.ngph.esb.rowmappers.UserMapper;

/**
 * 
 * @author guptarb
 * 
 */

public class EsbServiceDaoImpl implements EsbServiceDao 
{	
	private JdbcTemplate jdbcTemplate;	
	static Logger logger = Logger.getLogger(EsbServiceDaoImpl.class);
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) 
	{
		this.jdbcTemplate = jdbcTemplate;
	}
	
	 private OracleLobHandler  oracleLobHandler;

     public void setOracleLobHandler(OracleLobHandler oracleLobHandler)
     {
             this.oracleLobHandler = oracleLobHandler;
     }
     
 	public void populateInfoCan(final InfoCanonical obj)throws Exception
 	{
		logInfo("populateInfoCan(...) START...");
		
		String query = "insert into TA_MSGS_INFORMATION(MSGS_MSGREF,MSGS_SRC_MSGTYPE,MSGS_SRC_MSGSUBTYPE,MSGS_INFORMATION,MSGS_SEQNO,MSGS_DIRECTION,MSGS_DEPT,MSGS_BRANCH,MSGS_PMTID_INSTRID,MSGS_PMTID_RELREF,MSGS_INSTDAGT_BKCD,MSGS_INSTGAGT_BKCD,MSGS_LASTMODIFIEDTIME,MSGS_DST_MSGTYPE,MSGS_DST_MSGSUBTYPE,MSGS_DST_CHNL,MSGS_EI_ID,MSGS_MUR,MSGS_SNDRPYMTPRIORITY,MSGS_MSGSTS)"
						+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try
		{
			 jdbcTemplate.update(query, new PreparedStatementSetter() 
			 {
	             public void setValues(PreparedStatement ps) throws SQLException
	             {
                     ps.setString(1, obj.getMsgRef());
                     ps.setString(2, obj.getSrcMsgType());
                     ps.setString(3, obj.getSrcMsgSubType());
                     ps.setString(4, obj.getInfo());
                     ps.setString(5, obj.getSeqNo());
                     ps.setString(6, obj.getDirection());
                     ps.setString(7, obj.getDept());
                     ps.setString(8, obj.getBranch());
                     ps.setString(9,obj.getPmtId_instrId());
                     ps.setString(10,obj.getPmtId_relRef());
                     ps.setString(11,obj.getInstdagt_bkcd());
                     ps.setString(12,obj.getInstgagt_bkcd());
                     ps.setTimestamp(13,obj.getLstModTime());
                     ps.setString(14,obj.getDstMsgType());
                     ps.setString(15,obj.getDstMsgSubType());
                     ps.setString(16,obj.getDstChnl());
                     ps.setString(17,obj.getEi_ID());
                     ps.setString(18,obj.getMsgMur());
                     ps.setString(19,obj.getSndrPymtPriority());
                     ps.setString(20,obj.getMsgStatus());
                     
	             }
			 });
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}

		logInfo("populateInfoCan(...) ENDS...");
 	}

	public void populateMesMaster_T(final String seqNum, final String mes, final String Reason)throws Exception
	{
		logInfo("populateMesMaster_T(...) START...");
		String actual_reasn = null;
		if(StringUtils.isNotBlank(Reason)&& StringUtils.isNotEmpty(Reason))
		{
			actual_reasn = Reason ;
		}
		else
		{
			actual_reasn = "default reason";
		}
		final String default_reason = actual_reasn;
		
		String query = "insert into MESSAGEMASTER_T (SEQUENCE_NUMBER, MESSAGE,REASON,STATUS)"
						+ " values (?,?,?,?)";
		try
		{
			 jdbcTemplate.update(query, new PreparedStatementSetter() 
			 {
	             public void setValues(PreparedStatement ps) throws SQLException
	             {
	                     ps.setString(1, seqNum);
	                     oracleLobHandler.getLobCreator().setClobAsString(ps, 2,mes);
	                     ps.setString(3, default_reason);
	                     ps.setString(4, "P");
	             }
			 });
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}

		logInfo("populateMesMaster_T(...) ENDS...");
	}

	public void populateFileData(ArrayList<GenericFilePojo> dataHolder, String lineData, String TableName) throws Exception
	{
		logInfo("populateFileData(...) START...");
		try
		{
			if(dataHolder!=null && !dataHolder.isEmpty() && dataHolder.size()>0 && StringUtils.isNotBlank(lineData)&& StringUtils.isNotEmpty(lineData))
			{
				int i = 0;
				Object[] valuesArray =	new Object[50];
				String query = null;
				StringBuilder columns = new StringBuilder();
				StringBuilder count = new StringBuilder();
	
				columns.append("insert into " + TableName);
				columns.append(NgphEsbConstants.NGPH_SPACE);
				columns.append("(");

				count.append("values");
				count.append(NgphEsbConstants.NGPH_SPACE);
				count.append("(");

				GenericFilePojo dataObj = null;
				int startIndex = 0;
				for(int j=0;j<dataHolder.size();j++)
				{
					dataObj = dataHolder.get(j);
					if(dataObj.getColoumnName() != null)
					{
						columns.append( dataObj.getColoumnName()+ ",");
						count.append("?,");
						if (dataObj.getStaticValue().equalsIgnoreCase("y"))
						{
							valuesArray[i] = dataObj.getColoumnValue();
						}	
						else
						{
							valuesArray[i] = StringUtils.trim(lineData.substring(startIndex, startIndex + Integer.parseInt(dataObj.getLength())));
							startIndex = startIndex + Integer.parseInt(dataObj.getLength());
						}
						i++;
					}
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
					throw new Exception(e);
				}
			}
			else
			{
				//logger.warn("Either Line Data or Columns names are empty");
			}
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}
		logInfo("populateFileData(...) ENDS...");
	}
	public int updateFileData(ArrayList<GenericFilePojo> dataHolder, String lineData,String TableName) throws Exception
	{
		logInfo("updateFileData(...) START...");
		int result = 0;
		try
		{
			if(dataHolder!=null && !dataHolder.isEmpty() && dataHolder.size()>0 && StringUtils.isNotBlank(lineData)&& StringUtils.isNotEmpty(lineData))
			{
				ArrayList<Object> valuesArray =	new ArrayList<Object>();
				
				//update Part of Query
				StringBuilder query = new StringBuilder();
				query.append("UPDATE " + TableName +" SET");
				query.append(NgphEsbConstants.NGPH_SPACE);
				ArrayList<Object> columnArray = new ArrayList<Object>();
				
				//Where Part of Query
				StringBuilder whereClause = new StringBuilder();
				whereClause.append("where");
				whereClause.append(NgphEsbConstants.NGPH_SPACE);
				ArrayList<Object> whereClauseArray = new ArrayList<Object>();
				
				GenericFilePojo dataObj = null;
				int startIndex = 0;
				
				for(int j=0;j<dataHolder.size();j++)
				{
					dataObj = dataHolder.get(j);
					if(dataObj.getColoumnName() != null)
					{
						query.append( dataObj.getColoumnName()+ " = ?,");
						if (dataObj.getStaticValue().equalsIgnoreCase("y"))
						{
							columnArray.add(dataObj.getColoumnValue());
						}
						else
						{
							logger.info("The start index is " + startIndex + " and the length is " + dataObj.getLength());
							columnArray.add(StringUtils.trim(lineData.substring(startIndex, startIndex + Integer.parseInt(dataObj.getLength()))));
						}
						if(dataObj.getKey().equalsIgnoreCase("y"))
						{
							whereClause.append( dataObj.getColoumnName()+ " = ?");
							whereClause.append(" AND ");
							whereClauseArray.add(lineData.substring(startIndex, startIndex + Integer.parseInt(dataObj.getLength())));
						}
						if (!dataObj.getStaticValue().equalsIgnoreCase("y"))
						{
							startIndex = startIndex + Integer.parseInt(dataObj.getLength());
						}
					}
				}
				//Fetch the final String removing the last extra , value
				String stringQuery  = query.toString().substring(0,query.toString().length()-1) + NgphEsbConstants.NGPH_SPACE + whereClause.toString().substring(0,whereClause.toString().length()-5);				
				logger.info("Query===> " + stringQuery);
				
				//Populating column array Values in Values Array from Starting
				for(int k=0;k<columnArray.size();k++)
				{
					if(columnArray.get(k)!=null && StringUtils.isNotBlank(columnArray.get(k).toString()) && StringUtils.isNotEmpty(columnArray.get(k).toString()))
					{
						valuesArray.add(columnArray.get(k));
					}
				}
				
				//Populating whereClause array Values in Values Array from Last
				for(int k=0;k<whereClauseArray.size();k++)
				{
					if(whereClauseArray.get(k)!=null && StringUtils.isNotBlank(whereClauseArray.get(k).toString()) && StringUtils.isNotEmpty(whereClauseArray.get(k).toString()))
					{
						valuesArray.add(whereClauseArray.get(k)); 
					}
				}
		
				//Storing the valuesArray into Object[]
				Object[] actualArray = new Object[valuesArray.size()];
				for(int j=0;j<valuesArray.size();j++)
				{
					actualArray[j] = valuesArray.get(j);
					//logger.info ("Element at " + j + " is " + valuesArray.get(j));
				}
				logger.info ("Size of values array == " + valuesArray.size());
				//Sending to garbage collection once not required in Memory 
				valuesArray = null;
				whereClauseArray = null;
				columnArray = null;					
				result = jdbcTemplate.update(stringQuery, actualArray); 
				logger.info("Values Updated " + result);
			}
			else
			{
				//logger.warn("Either Line Data or Columns names are empty ");
			}
			//If no data is updated, then insert the record
			if( result>0)
			{
				logger.info("Record Present and Updated");
			}
			else
			{
				logger.info("Record not Found, Hence insert into DB");
				//insert into DB
				populateFileData(dataHolder,lineData,TableName);
			}
		}
		catch (Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("updateFileData(...) ENDS...");
		return result;
	}
	
	public int checkFileData(ArrayList<GenericFilePojo> dataHolder, String lineData,String TableName) throws Exception
	{
		logInfo("checkFileData(...) START...");
		int result = 0;
		try
		{
			if(dataHolder!=null && !dataHolder.isEmpty() && dataHolder.size()>0 && StringUtils.isNotBlank(lineData)&& StringUtils.isNotEmpty(lineData))
			{
				ArrayList<Object> valuesArray =	new ArrayList<Object>();
				
				//update Part of Query
				StringBuilder query = new StringBuilder();
				query.append("select count(*) from " + TableName);
				query.append(NgphEsbConstants.NGPH_SPACE);
				ArrayList<Object> columnArray = new ArrayList<Object>();
				
				//Where Part of Query
				StringBuilder whereClause = new StringBuilder();
				whereClause.append("where");
				whereClause.append(NgphEsbConstants.NGPH_SPACE);
				ArrayList<Object> whereClauseArray = new ArrayList<Object>();
				
				GenericFilePojo dataObj = null;
				int startIndex = 0;
				
				for(int j=0;j<dataHolder.size();j++)
				{
					dataObj = dataHolder.get(j);
					if(dataObj.getColoumnName() != null)
					{
						//query.append( dataObj.getColoumnName()+ " = ?,");
						//columnArray.add(lineData.substring(startIndex, startIndex + Integer.parseInt(dataObj.getLength())));
						if(dataObj.getKey().equalsIgnoreCase("y"))
						{
							whereClause.append( dataObj.getColoumnName()+ " = ?");
							whereClause.append(" AND ");
							whereClauseArray.add(lineData.substring(startIndex, startIndex + Integer.parseInt(dataObj.getLength())));
						}
						startIndex = startIndex + Integer.parseInt(dataObj.getLength());
					}
				}
				//Fetch the final String removing the last extra , value
				String stringQuery  = query.toString().substring(0,query.toString().length()-1) + NgphEsbConstants.NGPH_SPACE + whereClause.toString().substring(0,whereClause.toString().length()-5);
				
				logger.info("Query===> " + stringQuery);
				
				//Populating column array Values in Values Array from Starting
				for(int k=0;k<columnArray.size();k++)
				{
					if(columnArray.get(k)!=null && StringUtils.isNotBlank(columnArray.get(k).toString()) && StringUtils.isNotEmpty(columnArray.get(k).toString()))
					{
						valuesArray.add(columnArray.get(k));
					}
				}
				
				//Populating whereClause array Values in Values Array from Last
				for(int k=0;k<whereClauseArray.size();k++)
				{
					if(whereClauseArray.get(k)!=null && StringUtils.isNotBlank(whereClauseArray.get(k).toString()) && StringUtils.isNotEmpty(whereClauseArray.get(k).toString()))
					{
						valuesArray.add(whereClauseArray.get(k)); 
					}
				}
		
				//Storing the valuesArray into Object[]
				Object[] actualArray = new Object[valuesArray.size()];
				for(int j=0;j<valuesArray.size();j++)
				{
					actualArray[j] = valuesArray.get(j);
				}
				
				//Sending to garbage collection once not required in Memory 
				valuesArray = null;
				whereClauseArray = null;
				columnArray = null;
					
				result = jdbcTemplate.queryForInt(stringQuery, actualArray); 
				logger.info("No of records found are " + result);
			}
			else
			{
				//logger.warn("Either Line Data or Columns names are empty ");
			}

		}
		catch (Exception e)
		{
			logger.error(e);
			throw new Exception(e);
		}
		logInfo("checkFileData(...) ENDS...");
		return result;
	}

	public void updateBgMast(final BGMast obj)throws Exception
	{
		logInfo("updateBgMast(...) START...");
		if(obj != null)
		{
			//String query="UPDATE TA_BG_MAST SET BG_ADVISINGBANK=?,BG_ISSUINGBANK=?,BG_NOOF_MSGS=?,BG_DIRECTION=?,BG_ISSUE_DT=?,BG_CREATE_TYPE=?,BG_RULES_CODE=?,BG_RULES_DESC=?,BG_DETAILS=?,BG_NARRATION=?,BG_AMOUNT=?,BG_STATUS=?,BG_NOOF_AMNDMNTS=?,MSGS_MSGREF=? WHERE BG_NUMBER = ?";
			int i = 0;
			Object[] valuesArray =	new Object[20];
			StringBuilder query = new StringBuilder();
			query.append("UPDATE TA_BG_MAST SET");
			query.append(NgphEsbConstants.NGPH_SPACE);
			
			if(obj.getBgAdvisingBank()!= null)
			{
				query.append("BG_ADVISINGBANK = ?,");
				valuesArray[i] = obj.getBgAdvisingBank();
				i++;
			}
			if(obj.getBgIssuingBank()!= null)
			{
				query.append("BG_ISSUINGBANK = ?,");
				valuesArray[i] = obj.getBgIssuingBank();
				i++;
			}
			if(obj.getBgAmount() != null)
			{
				query.append("BG_AMOUNT = ?,");
				
				valuesArray[i] = obj.getBgAmount();
				i++;
			}
			if(obj.getBgNoOfMsgs() !=null)
			{
				query.append("BG_NOOF_MSGS = ?,");
				valuesArray[i] = obj.getBgNoOfMsgs();
				i++;
			}
		
			if(obj.getBgDirection() != null)
			{
				query.append("BG_DIRECTION = ?,");
				valuesArray[i] = obj.getBgDirection();
				i++;
			}
			if(obj.getBgIssueDate() != null)
			{
				query.append("BG_ISSUE_DT = ?,");
				valuesArray[i] = obj.getBgIssueDate();
				i++;
			}
			if(obj.getBgCreateType() != null)
			{
				query.append("BG_CREATE_TYPE = ?,");
				valuesArray[i] = obj.getBgCreateType();
				i++;
			}
			if(obj.getBgRulesCode() != null)
			{
				query.append("BG_RULES_CODE = ?,");
				valuesArray[i] = obj.getBgRulesCode();
				i++;
			}
			if(obj.getBgRulesDesc() != null)
			{
				query.append("BG_RULES_DESC = ?,");
				valuesArray[i] = obj.getBgRulesDesc();
				i++;
			}
			if(obj.getBgDetails() != null)
			{
				query.append("BG_DETAILS = ?,");
				valuesArray[i] = obj.getBgDetails();
				i++;
			}
			if(obj.getBgNarration() !=null)
			{
				query.append("BG_NARRATION = ?,");
				valuesArray[i] = obj.getBgNarration();
				i++;
			}
			if(obj.getBgAmount() !=null)
			{
				query.append("BG_AMOUNT = ?,");
				valuesArray[i] = obj.getBgAmount();
				i++;
			}
			if(obj.getBgStatus() !=null)
			{
				query.append("BG_STATUS = ?,");
				valuesArray[i] = obj.getBgStatus();
				i++;
			}
			if(obj.getBgNumOfAmndments() !=null)
			{
				query.append("BG_NOOF_AMNDMNTS = ?,");
				valuesArray[i] = obj.getBgNumOfAmndments();
				i++;
			}
			if(obj.getMsgRef() != null)
			{
				query.append("MSGS_MSGREF = ?,");
				valuesArray[i] = obj.getMsgRef();
				i++;
			}
			if(obj.getBgSequenceNo() != null)
			{
				query.append("BG_SEQUENCE_NO = ?,");
				valuesArray[i] = obj.getBgSequenceNo();
				i++;
			}
			if(obj.getBgLastAmendmentRef() != null)
			{
				query.append("BG_LAST_AMENDMENT_REF = ?,");
				valuesArray[i] = obj.getBgLastAmendmentRef();
				i++;
			}
			if(obj.getBgLastAmendmentDate() != null)
			{
				query.append("BG_LAST_AMENDMENT_DT = ?,");
				valuesArray[i] = obj.getBgLastAmendmentDate();
				i++;
			}
			if(obj.getBgRelReferenceNo() != null)
			{
				query.append("BG_REL_REFERENCE_NO = ?,");
				valuesArray[i] = obj.getBgRelReferenceNo();
				i++;
			}
			if(obj.getBgAccIdentification() != null)
			{
				query.append("BG_ACC_IDENTIFICATION = ?,");
				valuesArray[i] = obj.getBgAccIdentification();
				i++;
			}
			if(obj.getBgAckDate() != null)
			{
				query.append("BG_ACK_DT = ?,");
				valuesArray[i] = obj.getBgAckDate();
				i++;
			}
			if(obj.getBgChargeDetails() != null)
			{
				query.append("BG_CHARGE_DETAILS = ?,");
				valuesArray[i] = obj.getBgChargeDetails();
				i++;
			}
			if(obj.getBgDebitDate() != null)
			{
				query.append("BG_DEBIT_DATE = ?,");
				valuesArray[i] = obj.getBgDebitDate();
				i++;
			}
			//Fetch the final String removing the last extra , value
			String stringQuery  = query.toString().substring(0,query.toString().length()-1);
			
			stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
			stringQuery = stringQuery.concat("WHERE BG_NUMBER = ?");
			valuesArray[i]= obj.getBgNo();
			logger.info("BG_NUMBER is  "+obj.getBgNo());
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
			final Object[] finalArray = actualArray;
			final String finalQuery = stringQuery;
			logger.info("Final Query is "+finalQuery);
			try
			{			
				//jdbcTemplate.update(stringQuery, actualArray);
				 jdbcTemplate.update(finalQuery, new PreparedStatementSetter()  
				 {
		             public void setValues(PreparedStatement ps) throws SQLException
		             {
		            	 for(int k=1;k<=finalArray.length;k++)
			     			{
			            		 if(finalArray[k-1]instanceof String)
			            		 {
			            			 if(obj.getBgDetails()!=null && (finalArray[k-1]+"").equalsIgnoreCase(obj.getBgDetails()))
			            			 {
			            				 oracleLobHandler.getLobCreator().setClobAsString(ps, k,obj.getBgDetails());
			            			 }
			            			 else
			            			 {
				                    	ps.setString(k, finalArray[k-1]+"");
			            			 }
			            		 }
			            		 else if(finalArray[k-1]instanceof BigDecimal)
			            		 {
			            			 ps.setBigDecimal(k, (BigDecimal)finalArray[k-1]);
			            		 }
			            		 else if(finalArray[k-1]instanceof Timestamp)
			            		 {
			            			 ps.setTimestamp(k, (Timestamp)finalArray[k-1]);
			            		 }
			     			}
		             }
				 });
			}
			catch (Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		}
		else
		{
			logDebuggers("BgMast Dto is null");
		}
		logInfo("updateBgMast(...) ENDS...");
	}
	
	public void updateLcMast(LcMast obj)throws Exception
	{
		logInfo("updateLcMast(...) START...");
		if(obj != null)
		{
			int i = 0;
			Object[] valuesArray =	new Object[20];
			StringBuilder query = new StringBuilder();
			query.append("UPDATE TA_LC_MAST SET");
			query.append(NgphEsbConstants.NGPH_SPACE);
			
			if(obj.getLcAdvisingBank()!= null)
			{
				logger.info("obj.getLcAdvisingBank() : " + obj.getLcAdvisingBank());
				
				query.append("LC_ADVISINGBANK = ?,");
				valuesArray[i] = obj.getLcAdvisingBank();
				i++;
			}
			if(obj.getLcIssuingBank()!= null)
			{
				logger.info("obj.getLcIssuingBank() : " + obj.getLcIssuingBank());
				
				query.append("LC_ISSUING_BANK = ?,");
				valuesArray[i] = obj.getLcIssuingBank();
				i++;
			}
			if(obj.getLcAmount() != null)
			{
				logger.info("obj.getLcAmount() : " + obj.getLcAmount());
				
				query.append("LC_AMOUNT = ?,");
				valuesArray[i] = obj.getLcAmount();
				i++;
			}
			if(obj.getLcAppicant() != null)
			{
				logger.info("obj.getLcAppicant() : " + obj.getLcAppicant());
				
				query.append("LC_APPLICANT = ?,");
				valuesArray[i] = obj.getLcAppicant();
				i++;
			}
			if(obj.getLcBenificiary() != null)
			{
				logger.info("obj.getLcBenificiary() : " + obj.getLcBenificiary());
				
				query.append("LC_BENFICIARY = ?,");
				valuesArray[i] = obj.getLcBenificiary();
				i++;
			}
		
			if(obj.getLcCurrency() != null)
			{
				logger.info("obj.getLcCurrency() : " + obj.getLcCurrency());
				
				query.append("LC_CURRENCY = ?,");
				valuesArray[i] = obj.getLcCurrency();
				i++;
			}
			if(obj.getLcDirection() != null)
			{
				logger.info("obj.getLcDirection() : " + obj.getLcDirection());
				
				query.append("LC_DIRECTION = ?,");
				valuesArray[i] = obj.getLcDirection();
				i++;
			}
			if(obj.getLcExpireDate() != null)
			{
				logger.info("obj.getLcExpireDate() : " + obj.getLcExpireDate());
				
				query.append("LC_EXP_DT = ?,");
				valuesArray[i] = obj.getLcExpireDate();
				i++;
			}
			if(obj.getLcIssueDate() != null)
			{
				logger.info("obj.getLcIssueDate() : " + obj.getLcIssueDate());
				
				query.append("LC_ISSUE_DT = ?,");
				valuesArray[i] = obj.getLcIssueDate();
				i++;
			}
			if(obj.getLcNarrative() != null)
			{
				logger.info("obj.getLcNarrative() : " + obj.getLcNarrative());
				
				query.append("LC_NARRATIVE = ?,");
				valuesArray[i] = obj.getLcNarrative();
				i++;
			}
			if(obj.getLcNo() != null)
			{
				logger.info("obj.getLcNo() : " + obj.getLcNo());
				
				query.append("LC_NUMBER = ?,");
				valuesArray[i] = obj.getLcNo();
				i++;
			}
			if(obj.getLcNumOfAmndments() !=null)
			{
				logger.info("obj.getLcNumOfAmndments() : " + obj.getLcNumOfAmndments());
				
				query.append("LC_NOOF_AMNDMNTS = ?,");
				valuesArray[i] = obj.getLcNumOfAmndments();
				i++;
			}
			if(obj.getLcNumOfMsgs() !=null)
			{
				logger.info("obj.getLcNumOfMsgs() : " + obj.getLcNumOfMsgs());
				
				query.append("LC_NOOF_MSGS = ?,");
				valuesArray[i] = obj.getLcNumOfMsgs();
				i++;
			}
			if(obj.getLcStatus() !=null)
			{
				logger.info("obj.getLcStatus() : " + obj.getLcStatus());
				
				query.append("LC_STATUS = ?,");
				valuesArray[i] = obj.getLcStatus();
				i++;
			}
			if(obj.getLcType() != null)
			{
				logger.info("obj.getLcType() : " + obj.getLcType());
				
				query.append("LC_TYPE = ?,");
				valuesArray[i] = obj.getLcType();
				i++;
			}
			if(obj.getMsgRef() != null)
			{
				logger.info("obj.getMsgRef() : " + obj.getMsgRef());
				
				query.append("MSGS_MSGREF = ?,");
				valuesArray[i] = obj.getMsgRef();
				i++;
			}
				
			if(obj.getLcAckDate() != null)
			{
				logger.info("getLcAckDate() : " + obj.getLcAckDate());
				
				query.append("LC_ACK_DATE = ?,");
				valuesArray[i] = obj.getLcAckDate();
				i++;
			}
			//Fetch the final String removing the last extra , value
			String stringQuery  = query.toString().substring(0,query.toString().length()-1);
			
			stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
			stringQuery = stringQuery.concat("WHERE LC_NUMBER = ?");
			valuesArray[i]= obj.getLcNo();
			
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
				jdbcTemplate.update(stringQuery, actualArray); 
				logger.info("updateLcMast :: 1 Row was updated");
			}
			catch (Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		}
		else
		{
			logDebuggers("LcMast Dto is null");
		}
		logInfo("updateLcMast(...) ENDS...");
		
	
	}
	public void populateLcMast(LcMast obj)throws Exception
	{
		logInfo("populateLcMast(...) START...");
		if(obj != null)
		{
			int i = 0;
			Object[] valuesArray =	new Object[20];
			String query = null;
			StringBuilder columns = new StringBuilder();
			StringBuilder count = new StringBuilder();
			
			columns.append("insert into TA_LC_MAST");
			columns.append(NgphEsbConstants.NGPH_SPACE);
			columns.append("(");
			
			count.append("values");
			count.append(NgphEsbConstants.NGPH_SPACE);
			count.append("(");
			
			if(obj.getLcAdvisingBank()!= null)
			{
				columns.append("LC_ADVISINGBANK,");
				count.append("?,");
				valuesArray[i] = obj.getLcAdvisingBank();
				i++;
			}
			if(obj.getLcIssuingBank()!= null)
			{
				columns.append("LC_ISSUING_BANK,");
				count.append("?,");
				valuesArray[i] = obj.getLcIssuingBank();
				i++;
			}
			if(obj.getLcAmount()!= null)
			{
				columns.append("LC_AMOUNT,");
				count.append("?,");
				valuesArray[i] = obj.getLcAmount();
				i++;
			}
			if(obj.getLcAppicant()!= null)
			{
				columns.append("LC_APPLICANT,");
				count.append("?,");
				valuesArray[i] = obj.getLcAppicant();
				i++;
			}
			if(obj.getLcBenificiary() != null)
			{
				columns.append("LC_BENFICIARY,");
				count.append("?,");
				valuesArray[i] = obj.getLcBenificiary();
				i++;
			}
			if(obj.getLcCurrency() != null)
			{
				columns.append("LC_CURRENCY,");
				count.append("?,");
				valuesArray[i] = obj.getLcCurrency();
				i++;
			}
			if(obj.getLcDirection() !=null)
			{
				columns.append("LC_DIRECTION,");
				count.append("?,");
				valuesArray[i] = obj.getLcDirection();
				i++;
			}
			if(obj.getLcExpireDate() !=null)
			{
				columns.append("LC_EXP_DT,");
				count.append("?,");
				valuesArray[i] = obj.getLcExpireDate();
				i++;
			}
			if(obj.getLcIssueDate() !=null)
			{
				columns.append("LC_ISSUE_DT,");
				count.append("?,");
				valuesArray[i] = obj.getLcIssueDate();
				i++;
			}
			if(obj.getLcNarrative()!=null)
			{
				columns.append("LC_NARRATIVE,");
				count.append("?,");
				valuesArray[i] = obj.getLcNarrative();
				i++;
			}
			if(obj.getLcNo() !=null)
			{
				columns.append("LC_NUMBER,");
				count.append("?,");
				valuesArray[i] = obj.getLcNo();
				i++;
			}
			if(obj.getLcNumOfAmndments() !=null)
			{
				columns.append("LC_NOOF_AMNDMNTS,");
				count.append("?,");
				valuesArray[i] = obj.getLcNumOfAmndments();
				i++;
			}
			if(obj.getLcNumOfMsgs() !=null)
			{
				columns.append("LC_NOOF_MSGS,");
				count.append("?,");
				valuesArray[i] = obj.getLcNumOfMsgs();
				i++;
			}
			if(obj.getLcStatus() !=null)
			{
				columns.append("LC_STATUS,");
				count.append("?,");
				valuesArray[i] = obj.getLcStatus();
				i++;
			}
			if(obj.getLcType()!=null)
			{
				columns.append("LC_TYPE,");
				count.append("?,");
				valuesArray[i] = obj.getLcType();
				i++;
			}
			if(obj.getMsgRef()!=null)
			{
				columns.append("MSGS_MSGREF,");
				count.append("?,");
				valuesArray[i] = obj.getMsgRef();
				i++;
			}
			if(obj.getLcAckDate()!=null)
			{
				columns.append("LC_ACK_DATE,");
				count.append("?,");
				valuesArray[i] = obj.getLcAckDate();
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
				throw new Exception(e);
			}
		}
		else
		{
			logDebuggers("NULL Dto Object received by populateLcMast");
		}
		logInfo("populateLcMast(...) ENDS...");
	}
	
	public void populateBGMast(final BGMast obj)throws Exception
	{
		logInfo("populateBGMast(...) START...");
		if(obj != null)
		{
			String query="insert into TA_BG_MAST (BG_ADVISINGBANK,BG_ISSUINGBANK,BG_NUMBER,BG_NOOF_MSGS,BG_DIRECTION,BG_ISSUE_DT,BG_CREATE_TYPE,BG_RULES_CODE,BG_RULES_DESC,BG_DETAILS,BG_NARRATION,BG_AMOUNT,BG_STATUS,BG_NOOF_AMNDMNTS,MSGS_MSGREF, BG_SEQUENCE_NO)values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
				 jdbcTemplate.update(query, new PreparedStatementSetter() 
				 {
		             public void setValues(PreparedStatement ps) throws SQLException
		             {
		                     ps.setString(1, obj.getBgAdvisingBank());
		                     ps.setString(2, obj.getBgIssuingBank());
		                     ps.setString(3, obj.getBgNo());
		                     ps.setBigDecimal(4, obj.getBgNoOfMsgs());
		                     ps.setString(5, obj.getBgDirection());
		                     ps.setTimestamp(6, obj.getBgIssueDate());
		                     ps.setString(7, obj.getBgCreateType());
		                     ps.setString(8, obj.getBgRulesCode());
		                     ps.setString(9,obj.getBgRulesDesc());
		                     oracleLobHandler.getLobCreator().setClobAsString(ps, 10,obj.getBgDetails());
		                     ps.setString(11,obj.getBgNarration());
		                     ps.setBigDecimal(12,obj.getBgAmount());
		                     ps.setBigDecimal(13,obj.getBgStatus());
		                     ps.setBigDecimal(14,obj.getBgNumOfAmndments());
		                     ps.setString(15,obj.getMsgRef());
		                     ps.setBigDecimal(16, obj.getBgSequenceNo());
		                    
		             }
				 });
			
			/*
			int i = 0;
			Object[] valuesArray =	new Object[20];
			String query = null;
			StringBuilder columns = new StringBuilder();
			StringBuilder count = new StringBuilder();
			
			columns.append("insert into TA_BG_MAST");
			columns.append(NgphEsbConstants.NGPH_SPACE);
			columns.append("(");
			
			count.append("values");
			count.append(NgphEsbConstants.NGPH_SPACE);
			count.append("(");
			
			if(obj.getBgAdvisingBank()!= null)
			{
				columns.append("BG_ADVISINGBANK,");
				count.append("?,");
				valuesArray[i] = obj.getBgAdvisingBank();
				i++;
			}
			if(obj.getBgIssuingBank()!= null)
			{
				columns.append("BG_ISSUINGBANK,");
				count.append("?,");
				valuesArray[i] = obj.getBgIssuingBank();
				i++;
			}
			if(obj.getBgAmount()!= null)
			{
				columns.append("BG_AMOUNT,");
				count.append("?,");
				valuesArray[i] = obj.getBgAmount();
				i++;
			}
			if(obj.getBgNo()!= null)
			{
				columns.append("BG_NUMBER,");
				count.append("?,");
				valuesArray[i] = obj.getBgNo();
				i++;
			}
			if(obj.getBgNoOfMsgs()!=null)
			{
				columns.append("BG_NOOF_MSGS,");
				count.append("?,");
				valuesArray[i] = obj.getBgNoOfMsgs();
				i++;
			}
			if(obj.getBgDirection() != null)
			{
				columns.append("BG_DIRECTION,");
				count.append("?,");
				valuesArray[i] = obj.getBgDirection();
				i++;
			}
			if(obj.getBgIssueDate() !=null)
			{
				columns.append("BG_ISSUE_DT,");
				count.append("?,");
				valuesArray[i] = obj.getBgIssueDate();
				i++;
			}
			if(obj.getBgCreateType() !=null)
			{
				columns.append("BG_CREATE_TYPE,");
				count.append("?,");
				valuesArray[i] = obj.getBgCreateType();
				i++;
			}
			if(obj.getBgRulesCode() !=null)
			{
				columns.append("BG_RULES_CODE,");
				count.append("?,");
				valuesArray[i] = obj.getBgRulesCode();
				i++;
			}
			if(obj.getBgRulesDesc()!=null)
			{
				columns.append("BG_RULES_DESC,");
				count.append("?,");
				valuesArray[i] = obj.getBgRulesDesc();
				i++;
			}
			if(obj.getBgDetails() !=null)
			{
				columns.append("BG_DETAILS,");
				count.append("?,");
				valuesArray[i] = obj.getBgDetails();
				i++;
			}
			if(obj.getBgNarration() != null)
			{
				columns.append("BG_NARRATION,");
				count.append("?,");
				valuesArray[i] = obj.getBgNarration();
				i++;
			}
			if(obj.getBgAmount() != null)
			{
				columns.append("BG_AMOUNT,");
				count.append("?,");
				valuesArray[i] = obj.getBgAmount();
				i++;
			}
			if(obj.getBgStatus() !=null)
			{
				columns.append("BG_STATUS,");
				count.append("?,");
				valuesArray[i] = obj.getBgStatus();
				i++;
			}
			if(obj.getBgNumOfAmndments() !=null)
			{
				columns.append("BG_NOOF_AMNDMNTS,");
				count.append("?,");
				valuesArray[i] = obj.getBgNumOfAmndments();
				i++;
			}
			if(obj.getMsgRef()!=null)
			{
				columns.append("MSGS_MSGREF,");
				count.append("?,");
				valuesArray[i] = obj.getMsgRef();
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
				
				for(int k=0;k<actualArray.length;k++)
				{
					
				}
			}
			catch (Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		*/}
		else
		{
			logDebuggers("NULL Dto Object received by populateLcMast");
		}
		logInfo("populateBGMast(...) ENDS...");
	}

	public String getDstQueue(String ei_id)throws Exception
	{
		String hostCat = null;
		String query = "select OUTPUT_DEST_QUEUE from ta_ei where EI_CODE =?";
		try
		{
			hostCat = jdbcTemplate.queryForObject(query, new Object[]{ei_id}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return hostCat;
	}
	public String getIsoCurrCode(String curr)throws Exception
	{
		 String result=null;
		 String Query="select CUR_ISOCODE from ta_currency_mast where CUR_CODE='" + curr +"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
		 }
			catch (EmptyResultDataAccessException e) {
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}
		 catch (Exception e) {
			 logger.error(e,e);
			 throw new Exception(e);
		}
		 return result;
	}
	 
	public int getCntCurrCode(String curr)throws Exception
	{
		 int result=0;
		 String Query="select count(*) from ta_currency_mast where CUR_CODE=?";
		 try
		 {
			 result = jdbcTemplate.queryForInt(Query, curr);
		 }
			catch (EmptyResultDataAccessException e) {
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}
		 catch (Exception e) {
			 logger.error(e,e);
			 throw new Exception(e);
		}
		 return result;
	}
	
	public BigDecimal getCurrDecimal(String curr)throws Exception
	{
		 BigDecimal result=null;
		 String Query="SELECT CUR_DECIMAL FROM TA_CURRENCY_MAST WHERE CUR_CODE = ?";;
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, new Object[]{curr},BigDecimal.class);
		 }
			catch (EmptyResultDataAccessException e) {
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}
		 catch (Exception e) {
			 logger.error(e,e);
			 throw new Exception(e);
		}
		 return result;
	}
	public String getIsoPartyCode(String isoPty)throws Exception
	{
		 String result=null;
		 String Query="SELECT party_isocode FROM TA_PARTIES WHERE PARTY_BIC ='"+isoPty+"' OR PARTY_CLRSYSMMBID_MMBID = '" + isoPty + "'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
		 }
			catch (EmptyResultDataAccessException e) {
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

		 catch (Exception e) {
			 logger.error(e,e);
			 throw new Exception(e);
		}
		 return result;
	}
	// Validate value against the BIC value stored in DB
	public int validateBIC(String bicVal)throws Exception
	{
		 int result=0;
		 String Query= "select count(*) from ta_parties where PARTY_BIC =? OR PARTY_CLRSYSMMBID_MMBID =?";;
		 try
		 {
			 result = jdbcTemplate.queryForInt(Query, new Object[]{bicVal,bicVal});
		 }
			catch (EmptyResultDataAccessException e) {
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

		 catch (Exception e) {
			 logger.error(e,e);
			 throw new Exception(e);
		}
		 return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#getbusday_Date(java.lang.String)
	 */
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
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return date;
	}

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#getcurrbusday_Date(java.lang.String)
	 */
	public Date getcurrbusday_Date(String identifier)throws Exception
	{
		Date busDay_Date = null;
		String query = "select TO_DATE(SEQ_LASTMODDATE) from ta_sequences where seq_identifier =?";
		try
		{
			busDay_Date = jdbcTemplate.queryForObject(query, new Object[]{identifier}, Date.class);
		}
		catch (EmptyResultDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		return busDay_Date;
	}
	
	public String getStan(String identifier)throws Exception
	{
		String stan = null;
		String query = "select SEQ_SEQUENCE from ta_sequences where SEQ_IDENTIFIER =?";
		try
		{
			stan = jdbcTemplate.queryForObject(query, new Object[]{identifier}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return stan;
	}

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#updateBusyDate(java.lang.String)
	 */
	public void updateStan(String bus_date, String stan, String identifier)throws Exception
	{
		String query = "UPDATE ta_sequences SET SEQ_SEQUENCE =?, SEQ_LASTMODDATE=? WHERE SEQ_IDENTIFIER =?"; 

			try
			{
				jdbcTemplate.update(query,new Object[]{stan,bus_date,identifier});
			}
			catch (Exception e) 
			{
				logger.error(e,e);
				throw new Exception(e);
			}
	}
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#getHostCategory(java.lang.String)
	 */
	public String getHostCategory(String hostId)throws Exception
	{
		String hostCat = null;
		String query = "select EI_HOST_CATGORY from ta_ei where EI_CODE =?";
		try
		{
			hostCat = jdbcTemplate.queryForObject(query, new Object[]{hostId}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return hostCat;
		
	}

	public String getValFmtReq(String hostId)throws Exception
	{
		logger.info("getValFmtReq.. Starts");
		String valFmtReq = null;
		String query = "select EI_VAL_FMT_REQ from ta_ei where EI_CODE =?";
		try
		{
			valFmtReq = jdbcTemplate.queryForObject(query, new Object[]{hostId}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logger.info("getValFmtReq.. ENDS");
		return valFmtReq;
	}
	// getting the serviceConfig details for a particular message from
	// SERVICEORCH table
	public List<String> getServiceConfigDetails(String messageType,	String msgSubType, String messageDirection, String srvcId)throws Exception {
		
		logInfo("getServiceConfigDetails() Start...");
		String query =null;
		List<String> serviceIds = null;
		try{
				if(srvcId==null)
				{
					query = "SELECT SRVC_SERVICEID FROM SERVICEORCH WHERE SRVC_MSG_TYPE=? AND SRVC_MSG_SUBTYPE=? AND SRVC_MSGDIRECTION=? ORDER BY SRVC_CALLSEQ";
					serviceIds = jdbcTemplate.queryForList(query, new Object[] { messageType, msgSubType, messageDirection }, String.class);
				}
				else
				{
					query = "select SRVC_SERVICEID FROM SERVICEORCH WHERE SRVC_MSG_TYPE=? AND SRVC_MSG_SUBTYPE=? AND SRVC_MSGDIRECTION=? and SRVC_CALLSEQ > (select SRVC_CALLSEQ from SERVICEORCH where SRVC_MSG_TYPE=? AND SRVC_MSG_SUBTYPE=? AND SRVC_MSGDIRECTION=? and SRVC_SERVICEID=?) ORDER BY SRVC_CALLSEQ";
					serviceIds = jdbcTemplate.queryForList(query, new Object[] { messageType, msgSubType, messageDirection,messageType, msgSubType, messageDirection,srvcId }, String.class);
				}
			
			}catch(EmptyResultDataAccessException e)
			{
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}catch(Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
			logInfo("getServiceConfigDetails() End...");
		return serviceIds;
	}
	/**
	 * This method will return the list of rules for a specific messageType
	 */
	public List<Rules> getRulesForSpecificMessage(String msgType, String msgSubType, String ruleBranch, String ruleDept, String ruleCategory, String ruleDirection) throws Exception
	{
		String query="";
		logger.info("getRulesForSpecificMessage() Start...");
		List<Rules> lis = null;
			try
			{
				if(msgType!=null && msgSubType != null)
				{
					query = "SELECT RULE_ID, RULE_TYPE, RULE_SYS_CONDITION, RULE_ACTION, RULE_ACT_PARAM FROM TA_RULES WHERE RULE_MSGTYPE=? AND RULE_SUBMSGTYPE=? AND RULE_BRANCH=? AND RULE_DEPT=? AND RULE_CATEGORY=? AND RULE_DIRECTION=?";
					lis = jdbcTemplate.query(query, new Object[] {msgType, msgSubType, ruleBranch, ruleDept, ruleCategory, ruleDirection },new RulesSubMapper());
					if (lis == null || (lis != null && lis.size()<=0))
					{
						query = "SELECT RULE_ID, RULE_TYPE, RULE_SYS_CONDITION, RULE_ACTION, RULE_ACT_PARAM FROM TA_RULES WHERE RULE_MSGTYPE=? AND RULE_SUBMSGTYPE=? AND RULE_CATEGORY=? AND RULE_DIRECTION=?";
						lis = jdbcTemplate.query(query, new Object[] {msgType, msgSubType, ruleCategory, ruleDirection },new RulesSubMapper());
					}
				}
				else
				{
					query = "SELECT RULE_ID, RULE_TYPE, RULE_SYS_CONDITION, RULE_ACTION, RULE_ACT_PARAM FROM TA_RULES WHERE RULE_MSGTYPE IS NULL AND RULE_SUBMSGTYPE IS NULL AND RULE_BRANCH=? AND RULE_DEPT=? AND RULE_CATEGORY=? AND RULE_DIRECTION=?";
					lis = jdbcTemplate.query(query, new Object[] {msgSubType, ruleBranch, ruleDept, ruleCategory, ruleDirection },new RulesSubMapper());
					if (lis == null || (lis != null && lis.size()<=0))
					{
						query = "SELECT RULE_ID, RULE_TYPE, RULE_SYS_CONDITION, RULE_ACTION, RULE_ACT_PARAM FROM TA_RULES WHERE RULE_MSGTYPE IS NULL AND RULE_SUBMSGTYPE IS NULL AND RULE_CATEGORY=? AND RULE_DIRECTION=?";
						lis = jdbcTemplate.query(query, new Object[] {msgType, msgSubType, ruleCategory, ruleDirection },new RulesSubMapper());
					}
				}
				
			}
			catch(EmptyResultDataAccessException e)
			{
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) 
			{
				logger.error(e,e);
			}
			catch(Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
			logger.info("getRulesForSpecificMessage() End...");
		return lis;
	}
	
	/**
	 * 
	 * @param ruleMessageType
	 * @param ruleBranch
	 * @param ruleCategory
	 * @return
	 */
	public List<Rules> getRulesForSpecificMessageDeptTracing(String ruleMessageType,
			String ruleBranch, String ruleCategory) throws Exception{
		logInfo("getRulesForSpecificMessageDeptTracing() Start...");
		String query = "SELECT RULE_ID, RULE_TYPE, RULE_CONDITION, RULE_ACTION, RULE_ACT_PARAM FROM TA_RULES WHERE RULE_MSGTYPE=? AND RULE_BRANCH=? AND RULE_CATEGORY=?";

		List<Rules> lis = null; 
			
		try{	
			lis = jdbcTemplate.query(query, new Object[] {
				ruleMessageType, ruleBranch, ruleCategory },
				new RulesSubMapper());
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getRulesForSpecificMessageDeptTracing() End...");
		return lis;
	}
	
	/**
	 * 
	 */
	public String getInitialisedValue(String initEntry)throws Exception
	{
		//logInfo("getInitialisedValue() Start...");
		String query = "SELECT INIT_VALUE FROM INITIALISATIONM WHERE INIT_ENTRY=?";
		
		String initialisedValue = null;
		Clob clob = null;
		
		try{
			clob = jdbcTemplate.queryForObject(query, new Object[] {initEntry}, Clob.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		
		if(clob != null)
		{
			StringBuffer strOut = new StringBuffer();
			String aux;
			try {
			BufferedReader br = new BufferedReader(clob.getCharacterStream());
			while ((aux=br.readLine())!=null)
				strOut.append(aux);
			} catch (java.sql.SQLException e) {
				logger.error(e,e);
				throw new Exception(e);
			} catch (java.io.IOException e) {
				logger.error(e,e);
				throw new Exception(e);
			}
			initialisedValue = strOut.toString();
		}
		//logInfo("getInitialisedValue() End...");
		return initialisedValue;
	}
	
	/**
	 * 
	 * @param accountNo
	 * @return
	 */
	
	public String findBranchCodeByAccountNumber(String accountNo)throws Exception{
		logInfo("findBranchCodeByAccountNumber() Start...");
		String query = "SELECT ACCT_OWN_BRANCH FROM TA_ACCOUNTS WHERE ACCT_NUM=?";
		
		String branchCode = null;
		
		try{ 
			branchCode = jdbcTemplate.queryForObject(query, new Object[] {accountNo}, String.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("findBranchCodeByAccountNumber() End...");
		return branchCode;
	}
	
	/**
	 * 
	 * @param branchBic
	 * @return
	 */
	public String findBranchCodeByBic(String branchBic) throws Exception{
		
		logInfo("findBranchCodeByBic() Start...");
		String query = "SELECT BRANCH_CODE FROM BRANCHES WHERE BRANCH_BIC=? OR BRANCH_CLRGMMBID=?";
		String branchCode = null;
		try{
			branchCode = jdbcTemplate.queryForObject(query, new Object[] {branchBic, branchBic}, String.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("findBranchCodeByBic() End...");
		return branchCode;
	}
	
	public String findBicByBranchCode(String branchCode)throws Exception
	{
		logInfo("findBicByBranchCode() Start...");
		String query = "SELECT BRANCH_BIC FROM BRANCHES WHERE BRANCH_CODE=?";
		
		String branchBic = null;
		
		try{
			branchBic = jdbcTemplate.queryForObject(query, new Object[] {branchCode}, String.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("findBicByBranchCode() End...");
		return branchBic;
	}
	
	/**
	 * 
	 * @param eventId
	 * @return String  eventDescription
	 */
	public String getEventDescription(String eventId) throws Exception{
	
		String eventDescription = null;
		try
		{
			logInfo("getEventDescription() Start...");
			String query = "SELECT EVENTM_DESC FROM TA_EVENT_MAST where EVENTM_EVENTID=?";
			 
			eventDescription = (String)jdbcTemplate.queryForObject(query, new Object[] { eventId }, String.class);
			logInfo("getEventDescription() End...");
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		
		return eventDescription;
		
	}

	/**
	 * 
	 * @param eventAudit
	 * @return
	 */
	public void saveEventAudit(EventAudit eventAudit) throws Exception
	{
		logInfo("saveEventAudit(...) START...");
		if(eventAudit != null)
		{
			int i = 0;
			Object[] valuesArray =	new Object[20];
			String query = null;
			StringBuilder columns = new StringBuilder();
			StringBuilder count = new StringBuilder();
			
			columns.append("insert into TA_EVENT_AUDIT");
			columns.append(NgphEsbConstants.NGPH_SPACE);
			columns.append("(");
			
			count.append("values");
			count.append(NgphEsbConstants.NGPH_SPACE);
			count.append("(");
			
			if(eventAudit.getAuditBranch()!= null)
			{
				columns.append("AUDIT_BRANCH,");
				count.append("?,");
				valuesArray[i] = eventAudit.getAuditBranch();
				i++;
			}
			if(eventAudit.getAuditDept()!= null)
			{
				columns.append("AUDIT_DEPT,");
				count.append("?,");
				valuesArray[i] = eventAudit.getAuditDept();
				i++;
			}
			if(eventAudit.getAuditEventDesc() != null)
			{
				columns.append("AUDIT_EVENTDESC,");
				count.append("?,");
				valuesArray[i] = eventAudit.getAuditEventDesc();
				i++;
			}
			if(eventAudit.getAuditEventId() != null)
			{
				columns.append("AUDIT_EVENTID,");
				count.append("?,");
				valuesArray[i] = eventAudit.getAuditEventId();
				i++;
			}
			if(eventAudit.getAuditMessageRef() != null)
			{
				columns.append("AUDIT_MSGREF,");
				count.append("?,");
				valuesArray[i] = eventAudit.getAuditMessageRef();
				i++;
			}
			if(eventAudit.getAuditSource() !=null)
			{
				columns.append("AUDIT_SOURCE,");
				count.append("?,");
				valuesArray[i] = eventAudit.getAuditSource();
				i++;
			}
			if(eventAudit.getAuditTransactionRef() !=null)
			{
				columns.append("AUDIT_TXNREF,");
				count.append("?,");
				valuesArray[i] = eventAudit.getAuditTransactionRef();
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
				throw new Exception(e);
			}
		}
		else
		{
			logDebuggers("NULL Message received by AuditServiceClient");
		}
		logInfo("saveEventAudit(...) ENDS...");
	}
	
	public String getSequenceNumber(String seqIdentifier, int totalLen)	throws Exception
	{
		logInfo("getSequenceNumber() Start...");
		String query = "SELECT SEQ_SEQUENCE FROM TA_SEQUENCES WHERE SEQ_IDENTIFIER=?";
		
		int seqNum = 0;
		String seqNumStr = null;
		try{
			seqNum = jdbcTemplate.queryForInt(query, seqIdentifier);
			seqNum = seqNum + NgphEsbConstants.CONSTANT_VALUE_INT_ONE;
			seqNumStr = String.valueOf(seqNum);
			seqNumStr = StringUtils.leftPad(seqNumStr, totalLen, "0");
			//Updating the new sequence value to tx_sequences table
			updateSequenceNumber(seqNum,seqIdentifier);
		
		}catch(EmptyResultDataAccessException e){
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getSequenceNumber() End...");
		return seqNumStr;
	}
	
	/**
	 * 
	 * @param newSeqNum
	 */
	private void updateSequenceNumber(int newSeqNum, String Identifier)	throws Exception
	{
		logInfo("updateSequenceNumber() Start...");
		String query = "UPDATE TA_SEQUENCES SET SEQ_SEQUENCE=? WHERE SEQ_IDENTIFIER=?";
		try{
		jdbcTemplate.update(query, new Object[] {newSeqNum, Identifier});
		}catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logInfo("updateSequenceNumber() End...");
	}
	
	/**
	 * 
	 * @param msgType
	 * @param msgSubType
	 * @param fieldNo
	 * @return
	 */
	public List<MessageFormats> getFieldsOfMessage(String msgType, String msgSubType, String fieldNo, String fieldTag)	throws Exception
	{
		logInfo("getFieldsOfMessage() Start...");
		//select * from TA_MSG_FORMATS where format_field_mandatory=1 and format_msgtype='MT'and format_msgsubtype='103' order by format_field_seq asc, FORMAT_TAG_SEQ asc, format_tag_component asc;
		String query = "SELECT * FROM TA_MSG_FORMATS WHERE FORMAT_MSGTYPE=? AND FORMAT_MSGSUBTYPE=? AND FORMAT_FIELD_NO=? AND FORMAT_FIELDTAG=? ORDER BY FORMAT_TAG_COMPONENT ASC";		
		List<MessageFormats> lis = null; 
		try
		{	
			lis = jdbcTemplate.query(query, new Object[] {msgType, msgSubType, fieldNo, fieldTag},new MessageFormatsRowMapper());
		}
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getFieldsOfMessage() End...");
		return lis;
	}
	
	/**
	 * 
	 */
	public List<String> getDistinctFieldTags(String msgType, String msgSubType, String fieldNo)	throws Exception
	{
		logInfo("getDistinctFieldTags() Start...");
		String query = "SELECT UNIQUE FORMAT_TAG_SEQ, FORMAT_FIELDTAG FROM TA_MSG_FORMATS WHERE FORMAT_MSGTYPE=? AND FORMAT_MSGSUBTYPE=? AND FORMAT_FIELD_NO=? ORDER BY FORMAT_TAG_SEQ ASC";
		List<String> lis = null; 
		try{	
			lis = jdbcTemplate.query(query, new Object[] {
					msgType, msgSubType, fieldNo},
				new MessageFieldsResultSetMapper());
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getDistinctFieldTags() Start...");
		return lis;
	}
	
	/**
	 * 
	 */
	public List<String> getApplicableTextBlockFormatFields(String msgType, String msgSubType)throws Exception
	{
		logInfo("getApplicableTextBlockFormatFields() Start...");
		String query = "SELECT FORMAT_FIELD_SEQ,FORMAT_FIELD_NO FROM TA_MSG_FORMATS WHERE FORMAT_MSGTYPE=? AND FORMAT_MSGSUBTYPE=? ORDER BY FORMAT_FIELD_SEQ";
		
		List<String> lis = null; 
		
		try{	
			lis = jdbcTemplate.query(query, new Object[] {
					msgType, msgSubType},
				new MessageFieldsResultSetMapper());
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getApplicableTextBlockFormatFields() End...");
		return lis;
	}
	
	/**
	 * 
	 * @param fieldNo
	 * @param tagName
	 * @param tagSeq
	 * @return
	 */
	public Map<String, String> getTagValue(String fieldNo, String tagName, String blockNo)	throws Exception
	{
		logInfo("getTagValue() Start...");
		
		String fieldTag = null;
		if(StringUtils.isNotEmpty(fieldNo))
		{
			fieldTag = fieldNo.trim();
		}
		
		if(StringUtils.isNotEmpty(tagName) && !NgphEsbConstants.NGPH_CONSTANT_HASH.equalsIgnoreCase(tagName.trim()))
		{
			if(StringUtils.isNotEmpty(fieldTag))
			{
				fieldTag = fieldTag.concat(tagName.trim());
			}else{
				fieldTag = tagName.trim();
			}
		}
		int i = 0;
		Object[] valuesArray =	new Object[2];
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT FIELDS_INSEQ, FIELDS_LOG_NAME FROM MSG_FIELDS WHERE");
		queryBuilder.append(NgphEsbConstants.NGPH_SPACE);
		
		if(StringUtils.isNotEmpty(fieldTag))
		{
			queryBuilder.append("FIELDS_TAG = ?");
			
			valuesArray[i] = fieldTag;
			i++;
		}
		
		if(StringUtils.isNotEmpty(blockNo))
		{
			queryBuilder.append(NgphEsbConstants.NGPH_SPACE);
			queryBuilder.append("AND");
			queryBuilder.append(NgphEsbConstants.NGPH_SPACE);
			queryBuilder.append("FIELDS_BLOCK = ?");
			valuesArray[i] = blockNo;
			i++;
		}
		//to avoid nullextra values and to avoid invalid column type errors
		//we need to have columns used in query and values in this array should be same in count
		Object[] actualArray =	null;
		if(i < valuesArray.length)
		{
			actualArray = new Object[i];
			for(int j=0; j<actualArray.length; j++)
			{
				actualArray[j] = valuesArray[j];
			}
			
		}else{
			actualArray = valuesArray;
		}
		//its not required then after so sending to garbage collection 
		valuesArray = null;
		
		//String query = "SELECT FIELDS_INSEQ, FIELDS_LOG_NAME FROM MSG_FIELDS WHERE FIELDS_TAG=? AND FIELDS_BLOCK=?";
		String query = queryBuilder.toString();
		Map<String, String> resultMap = null;
		
		if(StringUtils.isNotEmpty(fieldTag))
		{
			try{	
				resultMap = jdbcTemplate.query(query, actualArray,
					new MessageFieldsMapRowMapper());
			}catch(EmptyResultDataAccessException e)
			{
				logger.error(e,e);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}
			catch(Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		}
		logInfo("getTagValue() End...");
		return resultMap;
	}
	
	public String getUstrdValue(String msgRef)	throws Exception
	{
		logInfo("getUstrdValue() Start...");
		String ustrd = null;
		String query = "SELECT RMT_USTRD FROM RMT_INFO WHERE RMT_MSGS_MSGREF = ?";
		
		try{
			ustrd = jdbcTemplate.queryForObject(query, new Object[]{msgRef}, String.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getUstrdValue() End...");
		return ustrd;
	}
	
	/**
	 * 
	 */
	public String getIfscCodeByBic(String bic)	throws Exception
	{
		logInfo("getIfscCodeByBic() Start...");
		String fiscCode = null;
		String query = "SELECT PARTY_CLRSYSMMBID_MMBID FROM TA_PARTIES WHERE PARTY_BIC = ?";
		
		try{
			fiscCode = jdbcTemplate.queryForObject(query, new Object[]{bic}, String.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getIfscCodeByBic() End...");
		return fiscCode;
	}
	
	public String getIFSCByBranch(String brnCode)throws Exception
	{
		logInfo("getIFSCByBranch() Start...");
		String IFSC = null;
		String query = "SELECT BRANCH_CLRGMMBID FROM BRANCHES WHERE BRANCH_CODE = ?";
		try
		{
			IFSC = jdbcTemplate.queryForObject(query, new Object[]{brnCode}, String.class);
		}
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch(IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getIFSCByBranch() End...");
		return IFSC;
	}
	
	public String getBICByBranch(String brnCode)throws Exception
	{
		logInfo("getBICByBranch() Start...");
		String IFSC = null;
		String query = "SELECT BRANCH_BIC FROM BRANCHES WHERE BRANCH_CODE = ?";
		try
		{
			IFSC = jdbcTemplate.queryForObject(query, new Object[]{brnCode}, String.class);
		}
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch(IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getBICByBranch() End...");
		return IFSC;
	}
	
	public String getIsoCodeByBranch(String brnCode)throws Exception
	{
		logInfo("getIsoCodeByBranch() Start...");
		String isoCode = null;
		String query = "SELECT BRANCH_ISOCODE FROM BRANCHES WHERE BRANCH_CODE = ?";
		try
		{
			isoCode = jdbcTemplate.queryForObject(query, new Object[]{brnCode}, String.class);
		}
		catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch(IncorrectResultSizeDataAccessException e) 
		{
			logger.error(e,e);
		}
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getIsoCodeByBranch() End...");
		return isoCode;
	}
	
	public String getIsoPartyBIC(String isoPtyCode)throws Exception
	{

		 String result=null;
		 String Query="SELECT PARTY_BIC FROM TA_PARTIES WHERE party_isocode ='"+isoPtyCode+"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
		 }
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch (Exception e) 
			{
				logger.error(e,e);	
				throw new Exception(e);
			}
		 return result;
	}
	
	public String getIsoPartyIFSC(String isoPtyCode)throws Exception
	{
		String result=null;
		logger.info("getIsoPartyIFSC() Start and the filter value is : " + isoPtyCode);
		String Query="SELECT PARTY_CLRSYSMMBID_MMBID FROM TA_PARTIES WHERE PARTY_ISOCODE ='"+isoPtyCode+"'";
		try
		{
			 result = jdbcTemplate.queryForObject(Query, String.class);
		}
		catch(EmptyResultDataAccessException ex)
		{
			logger.error(ex,ex);
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
		logger.info("getIsoPartyIFSC() end");
		return result;
	}
	
	public String getBICByIFSC(String IFSC)	throws Exception
	{
		 String result=null;
		 String Query="SELECT PARTY_BIC FROM TA_PARTIES WHERE PARTY_CLRSYSMMBID_MMBID ='"+IFSC+"'";
		 try
		 {
			 result = jdbcTemplate.queryForObject(Query, String.class);
		 }
			catch(EmptyResultDataAccessException ex)
			{
				logger.error(ex,ex);
			}
			catch (IncorrectResultSizeDataAccessException e) {
				logger.error(e,e);
			}

			catch (Exception e) 
			{
				logger.error(e,e);	
				throw new Exception(e);
			}
		 return result;
	}
	/**
	 * 
	 * @param groupMsgId
	 * @return
	 */
	public int getPaymentsCountOfGroupPayment(String groupMsgId)throws Exception
	{
		logInfo("getPaymentsCountOfGroupPayment() Start...");
		int count = 0;
		String query = "SELECT COUNT(MSGS_GRP_MSGID) WHERE MSGS_GRP_MSGID = ?";
		
		try{
			String strCount = jdbcTemplate.queryForObject(query, new Object[]{groupMsgId}, String.class);
			
			if(StringUtils.isNotEmpty(strCount))
				count = Integer.parseInt(strCount);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getPaymentsCountOfGroupPayment() End...");
		return count;
	}
	/**
	 * 
	 * @param msgMur
	 * @return
	 */
	
	/**
	 * 
	 * @param msgRef
	 * @param txnRef
	 * @param msgMur
	 * @param msgStatus
	 */
	public void updateMessageStatus(String msgRef, String txnRef, String msgMur, String msgStatus, String msgPrevStatus, Timestamp msgTmStmp)throws Exception
	{
		//FIXME what to do with MUR
		logInfo("updateMessageStatus() Start...");
		String query = "UPDATE TA_MESSAGES_TX SET MSGS_MSGSTS = ?, TIME_ACK_RECEIVED=?, MSGS_PREVMSGSTS = ? WHERE MSGS_MSGREF = ? AND MSGS_PMTID_INSTRID = ? AND MSG_MUR = ? ";
		try{
		jdbcTemplate.update(query, new Object[] {msgStatus,msgTmStmp, msgPrevStatus, msgRef, txnRef, msgMur});
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);	
		}
		logInfo("updateMessageStatus() End...");
	}
	
	public int validateMsgRef(String msgRef)throws Exception
	{
		logInfo("validateMsgRef() Start...");
		 int result=0;
		 String Query="select count(*) from TA_MESSAGES_TX where MSGS_MSGREF=?";
		 try
		 {
			 result = jdbcTemplate.queryForInt(Query, msgRef);
		 }
		 catch(Exception e)
		 {
			logger.error(e,e);
			throw new Exception(e);	
		 }
		 logInfo("validateMsgRef() End...");
		 return result;
	}
	
	public void updateMessageStatusforCBS(String msgRef, String msgStatus, String msgPrevStatus)throws Exception
	{
		logInfo("updateMessageStatusforCBS() Start...");
		String query = "UPDATE TA_MESSAGES_TX SET MSGS_MSGSTS = ?,MSGS_PREVMSGSTS = ? WHERE MSGS_MSGREF = ?";
		try
		{
			jdbcTemplate.update(query, new Object[] {msgStatus,msgPrevStatus,msgRef});
		}
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);	
		}
		logInfo("updateMessageStatusforCBS() End...");
	}
	
	/**
	 * 
	 * @param msgType
	 * @param msgSubType
	 * @param hostId
	 * @return
	 */
	public List<String> getValidateServiceRulesOrder(String msgType, String msgSubType, String hostId)throws Exception
	{
		logInfo("getValidateServiceRulesOrder(...) Start...");
		String query = "SELECT VALIDATESEQ FROM TA_VALCONFIG WHERE MSGTYPE = ? AND SUBMSGTYPE = ? AND HOSTID = ?";
		List<String> ruleIds = null;
		String srulesSeq = null;
		try{
			srulesSeq = jdbcTemplate.queryForObject(query, new Object[]{msgType, msgSubType, hostId}, String.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.warn("No validation rules configured for this message type");
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		if(StringUtils.isNotEmpty(srulesSeq))
		{
			//Check if the Db Resultant String contains ;
			if(srulesSeq.contains(";"))
			{
				String[] rulesArray =  srulesSeq.split(";");
				ruleIds = Arrays.asList(rulesArray);
			}
			else
			{
				ruleIds = new ArrayList<String>();
				ruleIds.add(srulesSeq);
			}
		}
		logInfo("getValidateServiceRulesOrder(...) End...");
		return ruleIds;
	}
	
	/**
	 * 
	 * @param msgRef
	 * @param txnRef
	 * @param msgMur
	 * @param msgBranch
	 * @param msgDept
	 */
	public void updatePymentBranchAndDept(String msgRef, String txnRef, String msgMur, String msgBranch, String msgDept)throws Exception
	{
		logInfo("updatePymentBranchAndDept() Start...");
		String query = "UPDATE TA_MESSAGES_TX SET MSGS_BRANCH = ?, MSGS_DEPT = ? WHERE MSGS_MSGREF = ? AND MSGS_PMTID_INSTRID = ?";
		try{
			jdbcTemplate.update(query, new Object[] {msgBranch, msgDept, msgRef, txnRef });
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
					
		logInfo("updatePymentBranchAndDept() End...");
	}
	
	/**
	 * 
	 */
	public void updatePaymentMsgWithFxDetails(NgphCanonical ngphCanonical)throws Exception
	{
		logInfo("updatePaymentMsgWithFxDetails() Start...");
		String query = "UPDATE TA_MESSAGES_TX SET MSG_CRCUR = ?, MSG_DRCUR = ?, MSG_BASECURAMT = ?, MSG_MSGCURAMT = ?, MSG_INSTDCURAMT = ? WHERE MSGS_MSGREF = ? AND MSGS_PMTID_INSTRID = ?";
		
		try{
			jdbcTemplate.update(query, new Object[] {ngphCanonical.getCrCurrency(),ngphCanonical.getDrCurrency(),ngphCanonical.getBaseCcyAmount(),ngphCanonical.getMsgCurrencyAmount(), ngphCanonical.getInstructedCcyAmount(), ngphCanonical.getMsgRef(), ngphCanonical.getTxnReference() });
			}catch(Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		logInfo("updatePaymentMsgWithFxDetails() End...");	
	}
	
	/**
	 * 
	 * @param currencyCode
	 * @param indicator
	 * @return
	 */
	public BigDecimal getConversionPrice(String currencyCode, String indicator) throws Exception
	{
		logInfo("getConversionPrice() Start...");
		String query = null;
		BigDecimal conversionPrice = null;
		
		if(NgphEsbConstants.DEBITOR_CONSTANT.equalsIgnoreCase(indicator))
			query = "SELECT CUR_SELL FROM TA_CURRENCY_MAST WHERE CUR_CODE = ?";
		else if(NgphEsbConstants.CREDITOR_CONSTANT.equalsIgnoreCase(indicator))
			query = "SELECT CUR_BUY FROM TA_CURRENCY_MAST WHERE CUR_CODE = ?";
		
		try{
			conversionPrice = jdbcTemplate.queryForObject(query, new Object[]{currencyCode}, BigDecimal.class);
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logInfo("getConversionPrice() End...");
		return conversionPrice;
	}
	
	/**
	 * 
	 * @param canonicalData
	 */
	public void updateMessageStatusAndServiceId(NgphCanonical canonicalData)throws Exception
	{
		logInfo("updateMessageStatusAndServiceId() Start...");
		String query = "UPDATE TA_MESSAGES_TX SET MSGS_MSGSTS = ?, MSG_SRVID = ? WHERE MSGS_MSGREF = ? AND MSGS_PMTID_INSTRID = ?";
		try{
		jdbcTemplate.update(query, new Object[] {canonicalData.getMsgStatus(), canonicalData.getServiceID(), canonicalData.getMsgRef(), canonicalData.getTxnReference()});
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("updateMessageStatusAndServiceId() End...");
	}
	
	/**
	 * 
	 */
	public String fetchRemittenceInfoValue(String remitInfoRef)	throws Exception
	{
		logInfo("fetchRemittenceInfoValue() Start...");
		String remitInfoValue = null;
		String query = "SELECT RMT_USTRD FROM RMT_INFO WHERE RMT_INFO_REF = ?";
		
		try{
			remitInfoValue = jdbcTemplate.queryForObject(query, new Object[]{remitInfoRef}, String.class);
			
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("fetchRemittenceInfoValue() End...");
		return remitInfoValue;
	}
	
	/**
	 * NOTE Please do not edit/modify/align the code of this method
	 * @param canonicalData
	 */
	public void updatePaymentDetails(NgphCanonical canonicalObj)throws Exception
	{
		logInfo("updatePaymentDetails() Start...");
		boolean isRelCanPresent=false;
		
		if(canonicalObj != null)
		{
			int i = 0;
			Object[] valuesArray =	new Object[250];
			StringBuilder columns = new StringBuilder();
			columns.append("UPDATE TA_MESSAGES_TX SET");
			columns.append(NgphEsbConstants.NGPH_SPACE);
			
				if(canonicalObj.getLcDeparturePlace() !=null)
				{
					columns.append("LC_DEPARTURE_PLACE = ?,");
					valuesArray[i] = canonicalObj.getLcDeparturePlace();
					i++;
				}
				if(canonicalObj.getLcFinalDstn() !=null)
				{
					columns.append("LC_FINAL_DESTINATION = ?,");
					valuesArray[i] = canonicalObj.getLcFinalDstn();
					i++;
				}

				if(canonicalObj.getGrpMsgId()!= null)
				{
					columns.append("MSGS_GRP_MSGID = ?,");
					valuesArray[i] = canonicalObj.getGrpMsgId();
					i++;
				}
				if(canonicalObj.getGrpSeq()>= 0)
				{
					columns.append("MSGS_GRP_SEQ = ?,");
					valuesArray[i] = canonicalObj.getGrpSeq();
					i++;
				}
				if(canonicalObj.getMsgHost() != null)
				{
					columns.append("MSGS_HOSTID = ?,");
					valuesArray[i] = canonicalObj.getMsgHost();
					i++;
				}
				if(canonicalObj.getMsgChannel() != null)
				{
					columns.append("MSGS_CHANNELID = ?,");
					valuesArray[i] = canonicalObj.getMsgChannel();
					i++;
				}
				if(canonicalObj.getMsgChnlType() !=null)
				{
					columns.append("MSGS_MSGCHNLTYPE = ?,");
					valuesArray[i] = canonicalObj.getMsgChnlType();
					i++;
				}
				if(canonicalObj.getSrcMsgType() !=null)
				{
					columns.append("MSGS_SRC_MSGTYPE = ?,");
					valuesArray[i] = canonicalObj.getSrcMsgType();
					i++;
				}
				if(canonicalObj.getSrcMsgSubType()!= null)
				{
					columns.append("MSGS_SRC_MSGSUBTYPE = ?,");
					valuesArray[i] = canonicalObj.getSrcMsgSubType();
					i++;
				}
				if(canonicalObj.getDstMsgType()!= null)
				{
					columns.append("MSGS_DST_MSGTYPE = ?,");
					valuesArray[i] = canonicalObj.getDstMsgType();
					i++;
				}
				if(canonicalObj.getDstMsgSubType()!= null)
				{
					columns.append("MSGS_DST_SUBMSGTYPE = ?,");
					valuesArray[i] = canonicalObj.getDstMsgSubType();
					i++;
				}
				if(canonicalObj.getMsgStatus() !=null)
				{
					columns.append("MSGS_MSGSTS = ?,");
					logger.info("Message status in ESBServiceDaoIMPL is ::"+canonicalObj.getMsgStatus());
					valuesArray[i] = canonicalObj.getMsgStatus();
					i++;
				}
				if(canonicalObj.getMsgPrevStatus() !=null)
				{
					columns.append("MSGS_PREVMSGSTS = ?,");
					valuesArray[i] = canonicalObj.getMsgPrevStatus();
					i++;
				}
				if(canonicalObj.getMsgDirection() !=null)
				{
					columns.append("MSGS_DIRECTION = ?,");
					valuesArray[i] = canonicalObj.getMsgDirection();
					i++;
				}
				if(canonicalObj.getReceivedTime() !=null)
				{
					columns.append("MSGS_RECVDTIME = ?,");
					valuesArray[i] = canonicalObj.getReceivedTime();
					i++;
				}
				if(canonicalObj.getLastModTime() !=null)
				{
					columns.append("MSGS_LASTMODIFIEDTIME = ?,");
					valuesArray[i] = canonicalObj.getLastModTime();
					i++;
				}
				if(canonicalObj.getTxnReference() !=null)
				{
					columns.append("MSGS_PMTID_INSTRID = ?,");
					valuesArray[i] = canonicalObj.getTxnReference();
					i++;
				}
				if(canonicalObj.getRelReference() !=null)
				{
					columns.append("MSGS_PMTID_RELREF = ?,");
					valuesArray[i] = canonicalObj.getRelReference();
					i++;
				}
				if(canonicalObj.getCustTxnReference() !=null)
				{
					columns.append("MSGS_PMTID_ENDTOENDID = ?,");
					valuesArray[i] = canonicalObj.getCustTxnReference();
					i++;
				}
				if(canonicalObj.getSndrTxnId() !=null)
				{
					columns.append("MSGS_PMTID_INSTGID = ?,");
					valuesArray[i] = canonicalObj.getSndrTxnId();
					i++;
				}
				if(canonicalObj.getClrgSysReference() !=null)
				{
					columns.append("MSGS_PMTID_CLRSYSREF = ?,");
					valuesArray[i] = canonicalObj.getClrgSysReference();
					i++;
				}
				if(canonicalObj.getSndrPymtPriority() !=null)
				{
					columns.append("MSGS_PMTTPLINF_INSTRPRTY = ?,");
					valuesArray[i] = canonicalObj.getSndrPymtPriority();
					i++;
				}
				if(canonicalObj.getClrgChannel() !=null)
				{
					columns.append("MSGS_PMTTPLINF_CLRCHNL = ?,");
					valuesArray[i] = canonicalObj.getClrgChannel();
					i++;
				}
				if(canonicalObj.getSvcLevelCode() !=null)
				{
					columns.append("MSGS_PMTTPLINF_SVCLVL_CD = ?,");
					valuesArray[i] = canonicalObj.getSvcLevelCode();
					i++;
				}
				if(canonicalObj.getSvcLevelProperitary() !=null)
				{
					columns.append("MSGS_PMTTPLINF_SVCLVL_PRTRY = ?,");
					valuesArray[i] = canonicalObj.getSvcLevelProperitary();
					i++;
				}
				if(canonicalObj.getLclInstCode() !=null)
				{
					columns.append("MSGS_PMTTPLINF_LCLINSTRM_CD = ?,");
					valuesArray[i] = canonicalObj.getLclInstCode();
					i++;
				}
				if(canonicalObj.getLclInstProperitary() !=null)
				{
					columns.append("MSGS_PMTTPLINF_LCLINSTRM_PRTRY = ?,");
					valuesArray[i] = canonicalObj.getLclInstProperitary();
					i++;
				}
				if(canonicalObj.getCatgPurposeCode() !=null)
				{
					columns.append("MSGS_PMTTPLINF_CTGYPURP_CD = ?,");
					valuesArray[i] = canonicalObj.getCatgPurposeCode();
					i++;
				}
				if(canonicalObj.getCatgPurposeProperitary() !=null)
				{
					columns.append("MSGS_PMTTPLINF_CTGYPURP_PRTRY = ?,");
					valuesArray[i] = canonicalObj.getCatgPurposeProperitary();
					i++;
				}
				if(canonicalObj.getMsgCurrency() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMCCY = ?,");
					valuesArray[i] = canonicalObj.getMsgCurrency();
					i++;
				}
				if(canonicalObj.getMsgAmount() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMAMT = ?,");
					valuesArray[i] = canonicalObj.getMsgAmount();
					i++;
				}
				if(canonicalObj.getMsgValueDate() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMDT = ?,");
					valuesArray[i] = canonicalObj.getMsgValueDate();
					i++;
				}
				if(canonicalObj.getSndrSttlmntPriority() !=null)
				{
					columns.append("MSGS_INTRBKSTTLMPRTY = ?,");
					valuesArray[i] = canonicalObj.getSndrSttlmntPriority();
					i++;
				}
				if(canonicalObj.getDrDateTime() !=null)
				{
					columns.append("MSGS_STTLMTMINDCTN_DBTDTTM = ?,");
					valuesArray[i] = canonicalObj.getDrDateTime();
					i++;
				}
				if(canonicalObj.getCrDateTime() !=null)
				{
					columns.append("MSGS_STTLMTMINDCTN_CDTDTTM = ?,");
					valuesArray[i] = canonicalObj.getCrDateTime();
					i++;
				}
				if(canonicalObj.getClsDateTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_CLSTM = ?,");
					valuesArray[i] = canonicalObj.getClsDateTime();
					i++;
				}
				if(canonicalObj.getSttlmntTillTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_TILLTM = ?,");
					valuesArray[i] = canonicalObj.getSttlmntTillTime();
					i++;
				}
				if(canonicalObj.getSttlmntFromTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_FRTM = ?,");
					valuesArray[i] = canonicalObj.getSttlmntFromTime();
					i++;
				}
				if(canonicalObj.getSttlmntRejTime() !=null)
				{
					columns.append("MSGS_STTLMTMREQ_RJCTTM = ?,");
					valuesArray[i] = canonicalObj.getSttlmntRejTime();
					i++;
				}
				if(canonicalObj.getPymntAcceptedTime() !=null)
				{
					columns.append("MSGS_ACCPTNCDTTM = ?,");
					valuesArray[i] = canonicalObj.getPymntAcceptedTime();
					i++;
				}
				if(canonicalObj.getCashpoolAdjstmntTime() !=null)
				{
					columns.append("MSGS_POOLGADJSTMNTDT = ?,");
					valuesArray[i] = canonicalObj.getCashpoolAdjstmntTime();
					i++;
				}
				if(canonicalObj.getInstructedCurrency() !=null)
				{
					columns.append("MSGS_INSTDCCY = ?,");
					valuesArray[i] = canonicalObj.getInstructedCurrency();
					i++;
				}
				if(canonicalObj.getInstructedAmount() !=null)
				{
					columns.append("MSGS_INSTDAMT = ?,");
					valuesArray[i] = canonicalObj.getInstructedAmount();
					i++;
				}
				if(canonicalObj.getXchangeRate() !=null)
				{
					columns.append("MSGS_XCHGRATE = ?,");
					valuesArray[i] = canonicalObj.getXchangeRate();
					i++;
				}
				if(canonicalObj.getChargeBearer() !=null)
				{
					columns.append("MSGS_CHRGBR = ?,");
					valuesArray[i] = canonicalObj.getChargeBearer();
					i++;
				}
				if(canonicalObj.getPrevInstructingBank() !=null)
				{
					columns.append("MSGS_PRVSINSTGAGT_BKCD = ?,");
					valuesArray[i] = canonicalObj.getPrevInstructingBank();
					i++;
				}
				if(canonicalObj.getPrevInstructingAgentAcct() !=null)
				{
					columns.append("MSGS_PRVSINSTGAGT_ACCT = ?,");
					valuesArray[i] = canonicalObj.getPrevInstructingAgentAcct();
					i++;
				}
				if(canonicalObj.getSenderBank() !=null)
				{
					columns.append("MSGS_INSTGAGT_BKCD = ?,");
					valuesArray[i] = canonicalObj.getSenderBank();
					i++;
				}
				if(canonicalObj.getReceiverBank() !=null)
				{
					columns.append("MSGS_INSTDAGT_BKCD = ?,");
					valuesArray[i] = canonicalObj.getReceiverBank();
					i++;
				}
				if(canonicalObj.getIntermediary1Bank() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_BKCD = ?,");
					valuesArray[i] = canonicalObj.getIntermediary1Bank();
					i++;
				}
				if(canonicalObj.getIntermediary1BankId() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_ID = ?,");
					valuesArray[i] = canonicalObj.getIntermediary1BankId();
					i++;
				}
				if(canonicalObj.getIntermediary1BankClrgCd() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_CLRCODE = ?,");
					valuesArray[i] = canonicalObj.getIntermediary1BankClrgCd();
					i++;
				}
				if(canonicalObj.getIntermediary1BankName() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_NM = ?,");
					valuesArray[i] = canonicalObj.getIntermediary1BankName();
					i++;
				}
				if(canonicalObj.getIntermediary1AgentAcct() !=null)
				{
					columns.append("MSGS_INTRMYAGT1_ACCT = ?,");
					valuesArray[i] = canonicalObj.getIntermediary1AgentAcct();
					i++;
				}
				if(canonicalObj.getIntermediary2Bank() !=null)
				{
					columns.append("MSGS_INTRMYAGT2_BKCD = ?,");
					valuesArray[i] = canonicalObj.getIntermediary2Bank();
					i++;
				}
				if(canonicalObj.getIntermediary2AgentAcct() !=null)
				{
					columns.append("MSGS_INTRMYAGT2_ACCT = ?,");
					valuesArray[i] = canonicalObj.getIntermediary2AgentAcct();
					i++;
				}
				if(canonicalObj.getIntermediary2BankName() !=null)
				{
					columns.append("MSGS_INTRMYAGT2_NM = ?,");
					valuesArray[i] = canonicalObj.getIntermediary2BankName();
					i++;
				}
				if(canonicalObj.getIntermediary3Bank() !=null)
				{
					columns.append("MSGS_INTRMYAGT3_BKCD = ?,");
					valuesArray[i] = canonicalObj.getIntermediary3Bank();
					i++;
				}
				if(canonicalObj.getIntermediary3AgentAcct() !=null)
				{
					columns.append("MSGS_INTRMYAGT3_ACCT = ?,");
					valuesArray[i] = canonicalObj.getIntermediary3AgentAcct();
					i++;
				}
				if(canonicalObj.getIntermediary3BankName() !=null)
				{
					columns.append("MSGS_INTRMYAGT3_NM = ?,");
					valuesArray[i] = canonicalObj.getIntermediary3BankName();
					i++;
				}
				if(canonicalObj.getUltimateDebtorName() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_NM = ?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorName();
					i++;
				}
				if(canonicalObj.getUltimateDebtorAddress() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_PSTLADR = ?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorAddress();
					i++;
				}
				if(canonicalObj.getUltimateDebtorID() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_ID = ?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorID();
					i++;
				}
				if(canonicalObj.getUltimateDebtorCtry() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_CTRYOFRES = ?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorCtry();
					i++;
				}
				if(canonicalObj.getUltimateDebtorCtctDtls() !=null)
				{
					columns.append("MSGS_ULTMTDBTR_CTCTDTLS = ?,");
					valuesArray[i] = canonicalObj.getUltimateDebtorCtctDtls();
					i++;
				}
				if(canonicalObj.getInitiatingPartyName() !=null)
				{
					columns.append("MSGS_INITGPRTY_NM = ?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyName();
					i++;
				}
				if(canonicalObj.getInitiatingPartyAddress() !=null)
				{
					columns.append("MSGS_INITGPRTY_PSTLADDR = ?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyAddress();
					i++;
				}
				if(canonicalObj.getInitiatingPartyID() !=null)
				{
					columns.append("MSGS_INITGPRTY_ID = ?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyID();
					i++;
				}
				if(canonicalObj.getInitiatingPartyCtry() !=null)
				{
					columns.append("MSGS_INITGPRTY_CTRYOFRES = ?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyCtry();
					i++;
				}
				if(canonicalObj.getInitiatingPartyCtctDtls() !=null)
				{
					columns.append("MSGS_INITGPRTY_CTCTDTLS = ?,");
					valuesArray[i] = canonicalObj.getInitiatingPartyCtctDtls();
					i++;
				}
				if(canonicalObj.getOrderingCustomerName() !=null)
				{
					columns.append("MSGS_DBTR_NM = ?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerName();
					i++;
				}
				if(canonicalObj.getOrderingCustomerAddress() !=null)
				{
					columns.append("MSGS_DBTR_PSTLADDR = ?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerAddress();
					i++;
				}
				if(canonicalObj.getOrderingCustomerId() !=null)
				{
					columns.append("MSGS_DBTR_ID = ?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerId();
					i++;
				}
				if(canonicalObj.getOrderingCustomerCtry() !=null)
				{
					columns.append("MSGS_DBTR_CTRYOFRES = ?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerCtry();
					i++;
				}
				if(canonicalObj.getOrderingCustomerCtctDtls() !=null)
				{
					columns.append("MSGS_DBTR_CTCTDTLS = ?,");
					valuesArray[i] = canonicalObj.getOrderingCustomerCtctDtls();
					i++;
				}
				if(canonicalObj.getOrderingCustAccount() !=null)
				{
					columns.append("MSGS_DBTRACCT = ?,");
					valuesArray[i] = canonicalObj.getOrderingCustAccount();
					i++;
				}
				if(canonicalObj.getOrderingType() !=null)
				{
					columns.append("MSGS_DBTRTYPE = ?,");
					valuesArray[i] = canonicalObj.getOrderingType();
					i++;
				}
				if(canonicalObj.getOrderingAcType() !=null)
				{
					columns.append("MSGS_DBTR_ACTYPE = ?,");
					valuesArray[i] = canonicalObj.getOrderingAcType();
					i++;
				}
				if(canonicalObj.getOrderingInstitution() !=null)
				{
					columns.append("MSGS_DBTRAGT = ?,");
					valuesArray[i] = canonicalObj.getOrderingInstitution();
					i++;
				}
				if(canonicalObj.getOrderingInstitutionId() !=null)
				{
					columns.append("MSGS_DBTRAGT_ID = ?,");
					valuesArray[i] = canonicalObj.getOrderingInstitutionId();
					i++;
				}
				if(canonicalObj.getOrderingInstitutionName() !=null)
				{
					columns.append("MSGS_DBTRAGT_NAME = ?,");
					valuesArray[i] = canonicalObj.getOrderingInstitutionName();
					i++;
				}
				if(canonicalObj.getOrderingInstitutionAcct() !=null)
				{
					columns.append("MSGS_DBTRAGTACCT = ?,");
					valuesArray[i] = canonicalObj.getOrderingInstitutionAcct();
					i++;
				}
				if(canonicalObj.getAccountWithInstitution() !=null)
				{
					columns.append("MSGS_CDTRAGT = ?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitution();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionId() !=null)
				{
					columns.append("MSGS_CDTRAGT_ID = ?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionId();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionLoc() !=null)
				{
					columns.append("MSGS_CDTRAGT_LOC = ?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionLoc();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionClrgCd() !=null)
				{
					columns.append("MSGS_CDTRAGT_CLRCODE = ?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionClrgCd();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionName() !=null)
				{
					columns.append("MSGS_CDTRAGT_NM = ?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionName();
					i++;
				}
				if(canonicalObj.getAccountWithInstitutionAcct() !=null)
				{
					columns.append("MSGS_CDTRAGTACCT = ?,");
					valuesArray[i] = canonicalObj.getAccountWithInstitutionAcct();
					i++;
				}
				if(canonicalObj.getSenderCorrespondent() !=null)
				{
					columns.append("MSGS_SNDRAGT = ?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondent();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentId() !=null)
				{
					columns.append("MSGS_SNDRAGT_ID = ?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentId();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentLoc() !=null)
				{
					columns.append("MSGS_SNDRAGT_LOC = ?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentLoc();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentName() !=null)
				{
					columns.append("MSGS_SNDRAGT_NAME = ?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentName();
					i++;
				}
				if(canonicalObj.getSenderCorrespondentAcct() !=null)
				{
					columns.append("MSGS_SNDRAGTACCT = ?,");
					valuesArray[i] = canonicalObj.getSenderCorrespondentAcct();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondent() !=null)
				{
					columns.append("MSGS_RCVRAGT = ?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondent();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentId() !=null)
				{
					columns.append("MSGS_RCVRAGT_ID = ?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentId();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentLoc() !=null)
				{
					columns.append("MSGS_RCVRAGT_LOC = ?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentLoc();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentName() !=null)
				{
					columns.append("MSGS_RCVRAGT_NAME = ?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentName();
					i++;
				}
				if(canonicalObj.getReceiverCorrespondentAcct() !=null)
				{
					columns.append("MSGS_RCVRAGTACCT = ?,");
					valuesArray[i] = canonicalObj.getReceiverCorrespondentAcct();
					i++;
				}
				if(canonicalObj.getThirdCorrespondent() !=null)
				{
					columns.append("MSGS_THIRDAGT = ?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondent();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentId() !=null)
				{
					columns.append("MSGS_THIRDAGT_ID = ?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentId();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentLoc() !=null)
				{
					columns.append("MSGS_THIRDAGT_LOC = ?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentLoc();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentName() !=null)
				{
					columns.append("MSGS_THIRDAGT_NAME = ?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentName();
					i++;
				}
				if(canonicalObj.getThirdCorrespondentAcct() !=null)
				{
					columns.append("MSGS_THIRDAGTACCT = ?,");
					valuesArray[i] = canonicalObj.getThirdCorrespondentAcct();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerName() !=null)
				{
					columns.append("MSGS_CDTR_NM = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerName();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerAddress() !=null)
				{
					columns.append("MSGS_CDTR_PSTLADDR = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerAddress();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerID() !=null)
				{
					columns.append("MSGS_CDTR_ID = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerID();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerCtry() !=null)
				{
					columns.append("MSGS_CDTR_CTRYOFRES = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerCtry();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustomerCtctDtls() !=null)
				{
					columns.append("MSGS_CDTR_CTCTDTLS = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustomerCtctDtls();
					i++;
				}
				if(canonicalObj.getBeneficiaryCustAcct() !=null)
				{
					columns.append("MSGS_CDTRACCT = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryCustAcct();
					i++;
				}
				if(canonicalObj.getBeneficiaryType() !=null)
				{
					columns.append("MSGS_CDTRTYPE = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryType();
					i++;
				}
				if(canonicalObj.getBeneficiaryAcType() !=null)
				{
					columns.append("MSGS_CDTR_ACTYPE = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryAcType();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitution() !=null)
				{
					columns.append("MSGS_BENFINST_CD = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitution();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitutionName() !=null)
				{
					columns.append("MSGS_BENFINST_NAME = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitutionName();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitutionAcct() !=null)
				{
					columns.append("MSGS_BENFINST_ACCT = ?,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitutionAcct();
					i++;
				}
				if(canonicalObj.getUltimateCreditorName() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_NM = ?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorName();
					i++;
				}
				if(canonicalObj.getUltimateCreditorAddress() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_PSTLADDR = ?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorAddress();
					i++;
				}
				if(canonicalObj.getUltimateCreditorID() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_ID = ?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorID();
					i++;
				}
				if(canonicalObj.getUltimateCreditorCtry() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_CTRYOFRES = ?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorCtry();
					i++;
				}
				if(canonicalObj.getUltimateCreditorCtctDtls() !=null)
				{
					columns.append("MSGS_ULTMTCDTR_CTCTDTLS = ?,");
					valuesArray[i] = canonicalObj.getUltimateCreditorCtctDtls();
					i++;
				}
				if(canonicalObj.getInstructionsForCrdtrAgtCode() !=null)
				{
					columns.append("MSGS_INSTRFORCDTRAGT_CD = ?,");
					valuesArray[i] = canonicalObj.getInstructionsForCrdtrAgtCode();
					i++;
				}
				if(canonicalObj.getInstructionsForCrdtrAgtText() !=null)
				{
					columns.append("MSGS_INSTRFORCDTRAGT_INSTRINF = ?,");
					valuesArray[i] = canonicalObj.getInstructionsForCrdtrAgtText();
					i++;
				}
				if(canonicalObj.getInstructionsForNextAgtCode() !=null)
				{
					columns.append("MSGS_INSTRFORNXTAGT_CD = ?,");
					valuesArray[i] = canonicalObj.getInstructionsForNextAgtCode();
					i++;
				}
				if(canonicalObj.getInstructionsForNextAgtText() !=null)
				{
					columns.append("MSGS_INSTRFORNXTAGT_INSTRINF = ?,");
					valuesArray[i] = canonicalObj.getInstructionsForNextAgtText();
					i++;
				}
				if(canonicalObj.getMsgPurposeCode() !=null)
				{
					columns.append("MSGS_PURP_CD = ?,");
					valuesArray[i] = canonicalObj.getMsgPurposeCode();
					i++;
				}
				if(canonicalObj.getMsgPurposeText() !=null)
				{
					columns.append("MSGS_PURP_PRTRY = ?,");
					valuesArray[i] = canonicalObj.getMsgPurposeText();
					i++;
				}
				if(canonicalObj.getRegulatoryBankCode() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_BKID = ?,");
					valuesArray[i] = canonicalObj.getRegulatoryBankCode();
					i++;
				}
				if(canonicalObj.getRegulatoryReportDrCr() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_DRCR = ?,");
					valuesArray[i] = canonicalObj.getRegulatoryReportDrCr();
					i++;
				}
				if(canonicalObj.getRegulatoryReportCurrency() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_CCY = ?,");
					valuesArray[i] = canonicalObj.getRegulatoryReportCurrency();
					i++;
				}
				if(canonicalObj.getRegulatoryReportAmount() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_AMT = ?,");
					valuesArray[i] = canonicalObj.getRegulatoryReportAmount();
					i++;
				}
				if(canonicalObj.getRegulatoryInformation() !=null)
				{
					columns.append("MSGS_RGLTRYRPTG_INF = ?,");
					valuesArray[i] = canonicalObj.getRegulatoryInformation();
					i++;
				}
				if(canonicalObj.getInitiatorRemitReference() !=null)
				{
					columns.append("MSGS_R_RMTID = ?,");
					valuesArray[i] = canonicalObj.getInitiatorRemitReference();
					i++;
				}
				if(canonicalObj.getInitiatorRemitAdviceMethod() !=null)
				{
					columns.append("MSGS_R_RMTLCTNMTD = ?,");
					valuesArray[i] = canonicalObj.getInitiatorRemitAdviceMethod();
					i++;
				}
				if(canonicalObj.getRemitInfoEmail() !=null)
				{
					columns.append("MSGS_R_RMTLCTNELCTRNCADR = ?,");
					valuesArray[i] = canonicalObj.getRemitInfoEmail();
					i++;
				}
				if(canonicalObj.getRemitReceivingPartyName() !=null)
				{
					columns.append("MSGS_R_RMTLCTNPSTLADR_NM = ?,");
					valuesArray[i] = canonicalObj.getRemitReceivingPartyName();
					i++;
				}
				if(canonicalObj.getRemitReceivingPartyAddress() !=null)
				{
					columns.append("MSGS_R_RMTLCTNPSTLADR_ADR = ?,");
					valuesArray[i] = canonicalObj.getRemitReceivingPartyAddress();
					i++;
				}
				if(canonicalObj.getRelRemitInfoRef() !=null)
				{
					columns.append("MSGS_R_RMTINF_REF = ?,");
					valuesArray[i] = canonicalObj.getRelRemitInfoRef();
					i++;
				}
				if(canonicalObj.getRemitInfoRef() !=null)
				{
					columns.append("MSGS_RMTINF_REF = ?,");
					valuesArray[i] = canonicalObj.getRemitInfoRef();
					i++;
				}
				if(canonicalObj.getMsgTxnType() !=null)
				{
					columns.append("MSGS_TXNTYPE = ?,");
					valuesArray[i] = canonicalObj.getMsgTxnType();
					i++;
				}
				if(canonicalObj.getMsgReturnReference() !=null)
				{
					columns.append("MSGS_RETURNREF = ?,");
					valuesArray[i] = canonicalObj.getMsgReturnReference();
					i++;
				}
				if(canonicalObj.getCustAccount() !=null)
				{
					columns.append("MSGS_IDTFD_CUSTAC = ?,");
					valuesArray[i] = canonicalObj.getCustAccount();
					i++;
				}
				if(canonicalObj.getMsgBatchTime() !=null)
				{
					columns.append("MSGS_BATCHTIME = ?,");
					valuesArray[i] = canonicalObj.getMsgBatchTime();
					i++;
				}
				if(canonicalObj.getMsgDept() !=null)
				{
					columns.append("MSGS_DEPT = ?,");
					valuesArray[i] = canonicalObj.getMsgDept();
					i++;
				}
				if(canonicalObj.getMsgBranch() !=null)
				{
					columns.append("MSGS_BRANCH = ?,");
					valuesArray[i] = canonicalObj.getMsgBranch();
					i++;
				}
				if(canonicalObj.getMsgRules() !=null)
				{
					columns.append("MSGS_RULESAPPLIED = ?,");
					valuesArray[i] = canonicalObj.getMsgRules();
					i++;
				}
				if(canonicalObj.getRelUid() !=null)
				{
					columns.append("MSGS_RELMSG_MSGREF = ?,");
					valuesArray[i] = canonicalObj.getRelUid();
					i++;
				}
				if(canonicalObj.getMsgMur() !=null)
				{
					columns.append("MSG_MUR = ?,");
					valuesArray[i] = canonicalObj.getMsgMur();
					i++;
				}
				if(canonicalObj.getLastModifiedUser() !=null)
				{
					columns.append("MSGS_MODIFIED_USER = ?,");
					valuesArray[i] = canonicalObj.getLastModifiedUser();
					i++;
				}
				if(canonicalObj.getComments() !=null)
				{
					columns.append("MSGS_COMMENTS = ?,");
					valuesArray[i] = canonicalObj.getComments();
					i++;
				}
				if(canonicalObj.getCrCurrency() !=null)
				{
					columns.append("MSG_CRCUR = ?,");
					valuesArray[i] = canonicalObj.getCrCurrency();
					i++;
				}
				if(canonicalObj.getDrCurrency() !=null)
				{
					columns.append("MSG_DRCUR = ?,");
					valuesArray[i] = canonicalObj.getDrCurrency();
					i++;
				}
				if(canonicalObj.getBaseCcyAmount() !=null)
				{
					columns.append("MSG_BASECURAMT = ?,");
					valuesArray[i] = canonicalObj.getBaseCcyAmount();
					i++;
				}
				if(canonicalObj.getMsgCurrencyAmount() !=null)
				{
					columns.append("MSG_MSGCURAMT = ?,");
					valuesArray[i] = canonicalObj.getMsgCurrencyAmount();
					i++;
				}
				if(canonicalObj.getInstructedCcyAmount() !=null)
				{
					columns.append("MSG_INSTDCURAMT = ?,");
					valuesArray[i] = canonicalObj.getInstructedCcyAmount();
					i++;
				}
				if(canonicalObj.getServiceID() !=null)
				{
					columns.append("MSG_SRVID = ?,");
					valuesArray[i] = canonicalObj.getServiceID();
					i++;
				}
				if(canonicalObj.getRepairReason() !=null)
				{
					columns.append("MSG_RPR_RSN = ?,");
					valuesArray[i] = canonicalObj.getRepairReason();
					i++;
				}
				if(canonicalObj.getAccountingStatus() !=null)
				{
					columns.append("MSG_ACCTNG_STS = ?,");
					valuesArray[i] = canonicalObj.getAccountingStatus();
					i++;
				}
				if(canonicalObj.getAccountingReason() !=null)
				{
					columns.append("MSG_ACCTNG_RSN = ?,");
					valuesArray[i] = canonicalObj.getAccountingReason();
					i++;
				}
				if(canonicalObj.getPdeCount() >=0)
				{
					columns.append("MSGS_PDECOUNT = ?,");
					valuesArray[i] = canonicalObj.getPdeCount();
					i++;
				}
				if(canonicalObj.getReturnReasonCode() !=null)
				{
					columns.append("MSGS_RETURN_RSNCODE = ?,");
					valuesArray[i] = canonicalObj.getReturnReasonCode();
					i++;
				}
				if(canonicalObj.getReturnReasonDesc() !=null)
				{
					columns.append("MSGS_RETURN_RSNDESC = ?,");
					valuesArray[i] = canonicalObj.getReturnReasonDesc();
					i++;
				}
				if(canonicalObj.getDstMsgChnlType() !=null)
				{
					columns.append("MSGS_DST_CHNL_TYPE = ?,");
					valuesArray[i] = canonicalObj.getDstMsgChnlType();
					i++;
				}
				if(canonicalObj.getDstEiId() !=null)
				{
					columns.append("MSGS_DST_EI_ID = ?,");
					valuesArray[i] = canonicalObj.getDstEiId();
					i++;
				}
				if(canonicalObj.getSeqNo() !=null)
				{
					columns.append("MSGS_SEQNO = ?,");
					valuesArray[i] = canonicalObj.getSeqNo();
					i++;
				}
				if(canonicalObj.getMsgErrorDesc() !=null)
				{
					columns.append("MSGS_ERROR_DESC = ?,");
					valuesArray[i] = canonicalObj.getMsgErrorDesc();
					i++;
				}
				if(canonicalObj.getMsgIsReturn() >=0)
				{
					columns.append("MSGS_IS_RETURN = ?,");
					valuesArray[i] = canonicalObj.getMsgIsReturn();
					i++;
				}
				if(canonicalObj.getLcType() !=null)
				{
					columns.append("LC_TYPE = ?,");
					valuesArray[i] = canonicalObj.getLcType();
					i++;
				}
				if(canonicalObj.getLcNo() !=null)
				{
					columns.append("LC_NUMBER = ?,");
					valuesArray[i] = canonicalObj.getLcNo();
					i++;
				}
				if(canonicalObj.getLcPrevAdvRef() !=null)
				{
					columns.append("LC_PRE_ADV_REF = ?,");
					valuesArray[i] = canonicalObj.getLcPrevAdvRef();
					i++;
				}
				if(canonicalObj.getLcIssueDt() !=null)
				{
					columns.append("LC_ISSUE_DT = ?,");
					valuesArray[i] = canonicalObj.getLcIssueDt();
					i++;
				}
				if(canonicalObj.getLcExpDt() !=null)
				{
					columns.append("LC_EXP_DATE = ?,");
					valuesArray[i] = canonicalObj.getLcExpDt();
					i++;
				}
				if(canonicalObj.getLcExpPlace() !=null)
				{
					columns.append("LC_EXP_PLACE = ?,");
					valuesArray[i] = canonicalObj.getLcExpPlace();
					i++;
				}
				if(canonicalObj.getLcTolerance() !=null)
				{
					columns.append("LC_NEG_TOLERANCE = ?,");
					valuesArray[i] = canonicalObj.getLcTolerance();
					i++;
				}
				if(canonicalObj.getLcPosTolerance()!=null)
				{
					columns.append("LC_POS_TOLERANCE = ?,");
					valuesArray[i] = canonicalObj.getLcTolerance();
					i++;
				}
				if(canonicalObj.getLcMaxCrAmt() !=null)
				{
					columns.append("LC_MAX_CRAMT = ?,");
					valuesArray[i] = canonicalObj.getLcMaxCrAmt();
					i++;
				}
				if(canonicalObj.getLcAddlAmts() !=null)
				{
					columns.append("LC_ADDNL_AMTS = ?,");
					valuesArray[i] = canonicalObj.getLcAddlAmts();
					i++;
				}
				if(canonicalObj.getLcAuthBankCode() !=null)
				{
					columns.append("LC_AUTHBANK_CODE = ?,");
					valuesArray[i] = canonicalObj.getLcAuthBankCode();
					i++;
				}
				if(canonicalObj.getLcAuthBankAddr() !=null)
				{
					columns.append("LC_AUTHBANK_ADDR = ?,");
					valuesArray[i] = canonicalObj.getLcAuthBankAddr();
					i++;
				}
				if(canonicalObj.getLcAuthMode() !=null)
				{
					columns.append("LC_AUTH_MODE = ?,");
					valuesArray[i] = canonicalObj.getLcAuthMode();
					i++;
				}
				if(canonicalObj.getLcDispatchPlace() !=null)
				{
					columns.append("LC_DISPATCH_PLACE = ?,");
					valuesArray[i] = canonicalObj.getLcDispatchPlace();
					i++;
				}
				if(canonicalObj.getLcDstn() !=null)
				{
					columns.append("LC_DESTINATION = ?,");
					valuesArray[i] = canonicalObj.getLcDstn();
					i++;
				}
				if(canonicalObj.getLcLstShipDt() !=null)
				{
					columns.append("LC_LAST_SHIPDT = ?,");
					valuesArray[i] = canonicalObj.getLcLstShipDt();
					i++;
				}
				if(canonicalObj.getLcShipPeriod() !=null)
				{
					columns.append("LC_SHIP_PRD = ?,");
					valuesArray[i] = canonicalObj.getLcShipPeriod();
					i++;
				}
				if(canonicalObj.getLcShipTerms() !=null)
				{
					columns.append("LC_SHIP_TERMS = ?,");
					valuesArray[i] = canonicalObj.getLcShipTerms();
					i++;
				}
				if(canonicalObj.getLcDraftsAt() !=null)
				{
					columns.append("LC_DRAFTS_AT = ?,");
					valuesArray[i] = canonicalObj.getLcDraftsAt();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkPid() !=null)
				{
					columns.append("LC_DRAWEEBANK_PID = ?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkPid();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkCode() !=null)
				{
					columns.append("LC_DRAWEEBANK_CODE = ?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkCode();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkAddr() !=null)
				{
					columns.append("LC_DRAWEEBANK_ADDR = ?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkAddr();
					i++;
				}
				if(canonicalObj.getLcMixedPymtDet() !=null)
				{
					columns.append("LC_MIXED_PYMT_DET = ?,");
					valuesArray[i] = canonicalObj.getLcMixedPymtDet();
					i++;
				}
				if(canonicalObj.getLcDefPymtDet() !=null)
				{
					columns.append("LC_DEF_PYMT_DET = ?,");
					valuesArray[i] = canonicalObj.getLcDefPymtDet();
					i++;
				}
				if(canonicalObj.getLcPartialShipment() !=null)
				{
					columns.append("LC_PARTIAL_SHIPMENT = ?,");
					valuesArray[i] = canonicalObj.getLcPartialShipment();
					i++;
				}
				if(canonicalObj.getLcTransShipment() !=null)
				{
					columns.append("LC_TRANS_SHIPMENT = ?,");
					valuesArray[i] = canonicalObj.getLcTransShipment();
					i++;
				}
				if(canonicalObj.getLcDocsReq1() !=null)
				{
					columns.append("LC_DOCS_REQD_1 = ?,");
					valuesArray[i] = canonicalObj.getLcDocsReq1();
					i++;
				}
				if(canonicalObj.getLcDocsReq2() !=null)
				{
					columns.append("LC_DOCS_REQD_2 = ?,");
					valuesArray[i] = canonicalObj.getLcDocsReq2();
					i++;
				}
				if(canonicalObj.getLcAddnlCndt1() !=null)
				{
					columns.append("LC_ADDNL_CONDITIONS_1 = ?,");
					valuesArray[i] = canonicalObj.getLcAddnlCndt1();
					i++;
				}
				if(canonicalObj.getLcAddnlCndt2() !=null)
				{
					columns.append("LC_ADDNL_CONDITIONS_2 = ?,");
					valuesArray[i] = canonicalObj.getLcAddnlCndt2();
					i++;
				}
				if(canonicalObj.getLcCharges() !=null)
				{
					columns.append("LC_CHARGES = ?,");
					valuesArray[i] = canonicalObj.getLcCharges();
					i++;
				}
				if(canonicalObj.getLcPrsntnPrd() !=null)
				{
					columns.append("LC_PRSNTN_PRD = ?,");
					valuesArray[i] = canonicalObj.getLcPrsntnPrd();
					i++;
				}
				if(canonicalObj.getLcConfrmInstrns() !=null)
				{
					columns.append("LC_CONFRM_INSTRNS = ?,");
					valuesArray[i] = canonicalObj.getLcConfrmInstrns();
					i++;
				}
				if(canonicalObj.getLcInstrnTopay() !=null)
				{
					columns.append("LC_INSTRNS_TOPAY = ?,");
					valuesArray[i] = canonicalObj.getLcInstrnTopay();
					i++;
				}
				if(canonicalObj.getLcNarrative() !=null)
				{
					columns.append("LC_NARRATIVE = ?,");
					valuesArray[i] = canonicalObj.getLcNarrative();
					i++;
				}
				if(canonicalObj.getLcAmndmntNo() > 0)
				{
					columns.append("LC_AMNDMNT_NO = ?,");
					valuesArray[i] = canonicalObj.getLcAmndmntNo();
					i++;
				}
				if(canonicalObj.getLcAmndmntDt() !=null)
				{
					columns.append("LC_AMNDMNT_DATE = ?,");
					valuesArray[i] = canonicalObj.getLcAmndmntDt();
					i++;
				}
				if(canonicalObj.getLcOldExpDt() !=null)
				{
					columns.append("LC_OLD_EXP_DATE = ?,");
					valuesArray[i] = canonicalObj.getLcOldExpDt();
					i++;
				}
				if(canonicalObj.getLcAmndmntIncAmt() !=null)
				{
					columns.append("LC_AMNDMNT_INCAMT = ?,");
					valuesArray[i] = canonicalObj.getLcAmndmntIncAmt();
					i++;
				}
				if(canonicalObj.getLcAmndmntDecAmt() !=null)
				{
					columns.append("LC_AMNDMNT_DECAMT = ?,");
					valuesArray[i] = canonicalObj.getLcAmndmntDecAmt();
					i++;
				}
				if(canonicalObj.getLcAmndmntOldAmt() !=null)
				{
					columns.append("LC_AMNDMNT_OLDAMT = ?,");
					valuesArray[i] = canonicalObj.getLcAmndmntOldAmt();
					i++;
				}
				if(canonicalObj.getLcAccId() !=null)
				{
					columns.append("LC_ACC_ID = ?,");
					valuesArray[i] = canonicalObj.getLcAccId();
					i++;
				}
				if(canonicalObj.getLcAckDt() !=null)
				{
					columns.append("LC_ACK_DATE = ?,");
					valuesArray[i] = canonicalObj.getLcAckDt();
					i++;
				}
				if(canonicalObj.getLcChgsClaimed() !=null)
				{
					columns.append("LC_CHGS_CLAIMED = ?,");
					valuesArray[i] = canonicalObj.getLcChgsClaimed();
					i++;
				}
				if(canonicalObj.getLcToAmtClaimed() !=null)
				{
					columns.append("LC_TOTAMT_CLAIMED = ?,");
					valuesArray[i] = canonicalObj.getLcToAmtClaimed();
					i++;
				}
				if(canonicalObj.getLcTotalAmtClaimed() !=null)
				{
					columns.append("LC_ADDNLAMT_CLAIMED = ?,");
					valuesArray[i] = canonicalObj.getLcTotalAmtClaimed();
					i++;
				}
				if(canonicalObj.getLcNetAmtClaimed() !=null)
				{
					columns.append("LC_NETAMT_CLAIMED = ?,");
					valuesArray[i] = canonicalObj.getLcNetAmtClaimed();
					i++;
				}
				if(canonicalObj.getLcAmtPaid() !=null)
				{
					columns.append("LC_AMT_PAID = ?,");
					valuesArray[i] = canonicalObj.getLcAmtPaid();
					i++;
				}
				if(canonicalObj.getLcDiscrepancies() !=null)
				{
					columns.append("LC_DISCREPANCIES = ?,");
					valuesArray[i] = canonicalObj.getLcDiscrepancies();
					i++;
				}
				if(canonicalObj.getLcDispoDocs() !=null)
				{
					columns.append("LC_DISPO_DOCS = ?,");
					valuesArray[i] = canonicalObj.getLcDispoDocs();
					i++;
				}
				
				if(canonicalObj.getMsgRelStatus() !=null)
				{
					columns.append("MSGS_REL_STATUS = ?,");
					valuesArray[i] = canonicalObj.getMsgRelStatus();
					i++;
				}
				if(canonicalObj.getLcTypeAuthCode() !=null)
				{
					columns.append("LC_TYPE_AUTHCODE = ?,");
					valuesArray[i] = canonicalObj.getLcTypeAuthCode();
					i++;
				}
				if(canonicalObj.getLcNonBankIssuer() !=null)
				{
					columns.append("LC_NONBANK_ISSUER = ?,");
					valuesArray[i] = canonicalObj.getLcNonBankIssuer();
					i++;
				}
				if(canonicalObj.getLcDraweeBnkAcct() !=null)
				{
					columns.append("LC_DRAWEEBANK_ACCT = ?,");
					valuesArray[i] = canonicalObj.getLcDraweeBnkAcct();
					i++;
				}
				if(canonicalObj.getLcAppRulesCode() !=null)
				{
					columns.append("LC_APPLICABLE_RULES_CODE = ?,");
					valuesArray[i] = canonicalObj.getLcAppRulesCode();
					i++;
				}
				if(canonicalObj.getLcAppRulesDesc() !=null)
				{
					columns.append("LC_APPLICABLE_RULES_DESC = ?,");
					valuesArray[i] = canonicalObj.getLcAppRulesDesc();
					i++;
				}
				if(canonicalObj.getSendingInstId() !=null)
				{
					columns.append("MSGS_SENDING_INSTID = ? ,");
					valuesArray[i] = canonicalObj.getSendingInstId();
					i++;
				}
				if(canonicalObj.getSendingInstAcct() !=null)
				{
					columns.append("MSGS_SENDING_INSTAC = ? ,");
					valuesArray[i] = canonicalObj.getSendingInstAcct();
					i++;
				}
				if(canonicalObj.getSendingInst() !=null)
				{
					columns.append("MSGS_SENDING_INSTCODE = ? ,");
					valuesArray[i] = canonicalObj.getSendingInst();
					i++;
				}
				if(canonicalObj.getSendingInstLoc() !=null)
				{
					columns.append("MSGS_SENDING_INSTLOC = ? ,");
					valuesArray[i] = canonicalObj.getSendingInstLoc();
					i++;
				}
				if(canonicalObj.getSendingInstNameAdd() !=null)
				{
					columns.append("MSGS_SENDING_INSTNAMEADD = ? ,");
					valuesArray[i] = canonicalObj.getSendingInstNameAdd();
					i++;
				}
				if(canonicalObj.getBeneficiaryInstitutionPID() !=null)
				{
					columns.append("MSGS_BENFINST_PID = ? ,");
					valuesArray[i] = canonicalObj.getBeneficiaryInstitutionPID();
					i++;
				}
				/*if(canonicalObj.getNoofProcessIterations() > 0)
				{
					columns.append("NOOF_PROC_ITERATION = ? ,");
					valuesArray[i] = canonicalObj.getNoofProcessIterations();
					i++;
				}*/

	
			//check if Related Canonical is present or not, if present set the boolean status to true.
			if(canonicalObj.getRelCanonical() != null)
			{
				isRelCanPresent = true;
				canonicalObj.setRelUid(canonicalObj.getRelCanonical().getMsgRef());
				if (canonicalObj.getMsgIsReturn() > 0)
				{
					//If it is a return message then update the related message only if the current message is in finality status for exit to the destination system.
					if(canonicalObj.getMsgDirection().equalsIgnoreCase("O") && (canonicalObj.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_ACK_O)) 
							|| canonicalObj.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.COMPLETED_O))))
					{
						canonicalObj.setMsgReturnReference(canonicalObj.getRelCanonical().getTxnReference());
						canonicalObj.getRelCanonical().setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RETURNED_I));
					}
					else if(canonicalObj.getMsgDirection().equalsIgnoreCase("I") && canonicalObj.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.SENT_TO_HOST_I)))
					{
						canonicalObj.setMsgReturnReference(canonicalObj.getRelCanonical().getTxnReference());
						canonicalObj.getRelCanonical().setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RETURNED_O));
					}
				}
			}
			
			if(StringUtils.isNotEmpty(columns.toString()))
			{
				String stringQuery = columns.toString().trim();
				if(stringQuery.endsWith(NgphEsbConstants.NGPH_COMMA))
				{
					String str = columns.reverse().toString().trim();
					StringBuilder sb = new StringBuilder(str.replaceFirst(NgphEsbConstants.NGPH_COMMA, NgphEsbConstants.NGPH_EMPTY));
					stringQuery = sb.reverse().toString().trim();
				}
				stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
				stringQuery = stringQuery.concat("WHERE MSGS_MSGREF = ?");
				valuesArray[i]= canonicalObj.getMsgRef();
				
				//to avoid null extra values and to avoid invalid column type errors
				//we need to have columns used in query and values in this array should be same in count
				Object[] actualArray = null;
				if(i+1 < valuesArray.length)
				{
					actualArray = new Object[i+1];
					for(int j=0; j<actualArray.length; j++)
					{
						actualArray[j] = valuesArray[j];
					}
					//its not required then after so sending to garbage collection 
					valuesArray = null;
				}
				try{
					jdbcTemplate.update(stringQuery, actualArray);
				}catch(Exception e){
					logger.error(e,e);
					throw new Exception(e);
					}
			}
			
			//if Related Canonical is Present update the related Canonical
			if(isRelCanPresent==true)
			{
				updatePaymentDetails(canonicalObj.getRelCanonical());
			}
			
		}else{
			logDebuggers("Canonical object is NULL...");
		}
		logInfo("updatePaymentDetails() End...");
	}
	
	
	public void updateMessageStatusToRepair(NgphCanonical canonicalData)throws Exception
	{
		logInfo("updating updateMessageStatusToRepair for Intervene");
		String query = "UPDATE TA_MESSAGES_TX SET MSGS_MSGSTS = ? WHERE MSGS_MSGREF = ?";
		try
		{
			jdbcTemplate.update(query, new Object[] {canonicalData.getMsgStatus(), canonicalData.getMsgRef()});
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("updated updateMessageStatusToRepair for Intervene in TA_MESSAGES_TX");	
	}
	
	/**
	 * getting accountNumber from TA_Customers table
	 */
	public String getAccountNumberByCustomerId(String custId)throws Exception
	{
		logInfo("getAccountNumberByCustomerId(...) Start....");
		String accountNumber = null;
		String query = "SELECT CUST_DEFACC FROM TA_CUSTOMERS WHERE CUST_CODE = ?";
		try{
			accountNumber = jdbcTemplate.queryForObject(query, new Object[]{custId}, String.class);
		}
		catch(EmptyResultDataAccessException e){
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logInfo("getAccountNumberByCustomerId(...) End....");
		return accountNumber;
	}
	
	/**
	 * 
	 * @param accountName
	 * @return
	 * @throws NGPHException
	 * getting accountNumber from TA_Accounts table
	 */
	public String getAccountNumberByAccountName(String accountName, String baseCcy, String accOwnBranch)throws Exception
	{
		logInfo("getAccountNumberByAccountName(...) Start....");
		String accountNumber = null;
		String query = "SELECT ACCT_NUM FROM TA_ACCOUNTS WHERE ACCT_ACNAME = ? AND ACCT_CCY = ? AND ACCT_OWN_BRANCH = ?";
		try{
			accountNumber = jdbcTemplate.queryForObject(query, new Object[]{accountName, baseCcy, accOwnBranch}, String.class);
		}catch(EmptyResultDataAccessException e){
			logger.error(e,e);
		}catch(IncorrectResultSizeDataAccessException e){
			logger.error(e,e);
		}catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getAccountNumberByAccountName(...) End....");
		return accountNumber;
	}
	
	/**
	 * 
	 * @param partyCode
	 * @param ccyType
	 * @param accOwnBranch
	 * @return
	 * @throws NGPHException
	 * getting accountNumber from TA_Accounts table by partyCode
	 */
	public String getAccountNumberByPartyCode(String partyCode, String ccyType, String accOwnBranch)throws Exception
	{
		logInfo("getAccountNumberByAccountName(...) Start....");
		String accountNumber = null;
		String query = "SELECT ACCT_NUM FROM TA_ACCOUNTS WHERE ACCT_PARTIES_CODE = ? AND ACCT_CCY = ? AND ACCT_OWN_BRANCH = ?";
		try{
			accountNumber = jdbcTemplate.queryForObject(query, new Object[]{partyCode, ccyType, accOwnBranch}, String.class);
		}catch(EmptyResultDataAccessException e){
			logger.error(e,e);
		}catch(IncorrectResultSizeDataAccessException e){
			logger.error(e,e);
		}catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getAccountNumberByAccountName(...) End....");
		return accountNumber;
	}
	
	public List<String> getAcDetailsByMMIDAndMobile(String MMID, String mobNo)throws Exception
	{
		SqlRowSet srs = null;
		List<String> holder = new ArrayList<String>();;

		logInfo("getAcDetailsByMMIDAndMobile(...) Start...");
		String query = "select ACCT_ACNAME, ACCT_NUM, ACCT_CLOSED, ACCT_STATUS, ACCT_CRDT_ALLOWED, ACCT_ACTYPE, ADDR_MOBILE, ADDR_MMID, ADDR_PSTLADR_TWNNM, ADDR_PSTLADR_CTRYSUBDVSN, ADDR_PSTLADR_CTRY FROM TA_ACCOUNTS, TA_ADDRESSES " +
				"where TA_ACCOUNTS.ACCT_ADDRREF =  TA_ADDRESSES.ADDR_REF AND ADDR_MMID=? AND ADDR_MOBILE=?";
		try
		{
			System.out.println("The MMID for query is " + MMID);
			System.out.println("The mobile number for query is " + mobNo);
			srs = jdbcTemplate.queryForRowSet(query, new Object[]{MMID, mobNo});
			while(srs.next())
			{
				holder.add(srs.getString("ACCT_ACNAME"));
				holder.add(srs.getString("ACCT_NUM"));
				holder.add(Integer.toString((srs.getInt("ACCT_CLOSED"))));
				holder.add(srs.getString("ADDR_PSTLADR_TWNNM"));
				holder.add(srs.getString("ADDR_PSTLADR_CTRYSUBDVSN"));
				holder.add(srs.getString("ADDR_PSTLADR_CTRY"));
				holder.add(srs.getString("ACCT_STATUS"));
				holder.add(srs.getString("ACCT_CRDT_ALLOWED"));
				holder.add(srs.getString("ADDR_MOBILE"));
				holder.add(srs.getString("ADDR_MMID"));
				holder.add(srs.getString("ACCT_ACTYPE"));
			}
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getAcDetailsByMMIDAndMobile(...) End...");
		return holder;
	}
	
	public List<String> getAcDetailsByAccountAndMobile(String accountNo, String mobNo, int addrSeq)	throws Exception
	{
		SqlRowSet srs = null;
		String query = null;
		List<String> holder = new ArrayList<String>();;

		logInfo("getAcDetailsByAccountAndMobile(...) Start...");
		try
		{
			if (addrSeq > 0)
			{
				query = "select ACCT_ACNAME, ACCT_CLOSED, ACCT_STATUS, ACCT_CRDT_ALLOWED, ACCT_ACTYPE, ADDR_PSTLADR_TWNNM, ADDR_MOBILE, ADDR_MMID, ADDR_PSTLADR_CTRYSUBDVSN, ADDR_PSTLADR_CTRY FROM TA_ACCOUNTS, TA_ADDRESSES where TA_ACCOUNTS.ACCT_ADDRREF =  TA_ADDRESSES.ADDR_REF AND ACCT_NUM = ? AND ADDR_MOBILE=? AND ADDR_SEQ = ?";
				srs = jdbcTemplate.queryForRowSet(query, new Object[]{accountNo, mobNo, addrSeq});
			}
			else
			{
				query = "select ACCT_ACNAME, ACCT_CLOSED, ACCT_STATUS, ACCT_CRDT_ALLOWED, ACCT_ACTYPE, ADDR_PSTLADR_TWNNM, ADDR_MOBILE, ADDR_MMID, ADDR_PSTLADR_CTRYSUBDVSN, ADDR_PSTLADR_CTRY FROM TA_ACCOUNTS, TA_ADDRESSES where TA_ACCOUNTS.ACCT_ADDRREF =  TA_ADDRESSES.ADDR_REF AND ACCT_NUM = ? AND ADDR_MOBILE=?";
				srs = jdbcTemplate.queryForRowSet(query, new Object[]{accountNo, mobNo});
			}
			while(srs.next())
			{
				holder.add(srs.getString("ACCT_ACNAME"));
				holder.add(srs.getString("ACCT_NUM"));
				holder.add(Integer.toString((srs.getInt("ACCT_CLOSED"))));
				holder.add(srs.getString("ADDR_PSTLADR_TWNNM"));
				holder.add(srs.getString("ADDR_PSTLADR_CTRYSUBDVSN"));
				holder.add(srs.getString("ADDR_PSTLADR_CTRY"));
				holder.add(srs.getString("ACCT_STATUS"));
				holder.add(srs.getString("ACCT_CRDT_ALLOWED"));
				holder.add(srs.getString("ADDR_MOBILE"));
				holder.add(srs.getString("ADDR_MMID"));
				holder.add(srs.getString("ACCT_ACTYPE"));
			}
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getAcDetailsByAccountAndMobile(...) End...");
		return holder;
	}
	
	public List<String> getAcDetailsByAccountAndBranch(String accountNo, String brnCode, int addrSeq)throws Exception
	{
		SqlRowSet srs = null;
		String query = null;
		List<String> holder = new ArrayList<String>();;

		logInfo("getAcDetailsByAccountAndBranch(...) Start... for filter " + accountNo + "---" + brnCode);
		try
		{
			if (addrSeq > 0)
			{
				query = "select ACCT_ACNAME, ACCT_NUM, ACCT_CLOSED, ACCT_STATUS, ACCT_CRDT_ALLOWED, ACCT_ACTYPE, ADDR_MOBILE, ADDR_MMID, ADDR_PSTLADR_TWNNM, ADDR_PSTLADR_CTRYSUBDVSN, ADDR_PSTLADR_CTRY FROM TA_ACCOUNTS, TA_ADDRESSES where TA_ACCOUNTS.ACCT_ADDRREF =  TA_ADDRESSES.ADDR_REF AND ACCT_NUM = ? AND ACCT_OWN_BRANCH=? AND ADDR_SEQ = ?";
				srs = jdbcTemplate.queryForRowSet(query, new Object[]{accountNo, brnCode, addrSeq});
			}
			else
			{
				query = "select ACCT_ACNAME, ACCT_NUM, ACCT_CLOSED, ACCT_STATUS, ACCT_CRDT_ALLOWED, ACCT_ACTYPE, ADDR_MOBILE, ADDR_MMID, ADDR_PSTLADR_TWNNM, ADDR_PSTLADR_CTRYSUBDVSN, ADDR_PSTLADR_CTRY FROM TA_ACCOUNTS, TA_ADDRESSES where TA_ACCOUNTS.ACCT_ADDRREF =  TA_ADDRESSES.ADDR_REF AND ACCT_NUM = ? AND ACCT_OWN_BRANCH=? AND ADDR_SEQ = 1";
				srs = jdbcTemplate.queryForRowSet(query, new Object[]{accountNo, brnCode});
			}
		
		
			while(srs.next())
			{
				holder.add(srs.getString("ACCT_ACNAME"));
				holder.add(srs.getString("ACCT_NUM"));
				holder.add(Integer.toString((srs.getInt("ACCT_CLOSED"))));
				holder.add(srs.getString("ADDR_PSTLADR_TWNNM"));
				holder.add(srs.getString("ADDR_PSTLADR_CTRYSUBDVSN"));
				holder.add(srs.getString("ADDR_PSTLADR_CTRY"));
				holder.add(srs.getString("ACCT_STATUS"));
				holder.add(srs.getString("ACCT_CRDT_ALLOWED"));
				holder.add(srs.getString("ADDR_MOBILE"));
				holder.add(srs.getString("ADDR_MMID"));
				holder.add(srs.getString("ACCT_ACTYPE"));
			}
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getAcDetailsByAccountAndBranch(...) End...");
		return holder;
	}
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#loadFieldsData()
	 */
	public  HashMap<String, ArrayList<FieldCanonicalAttribute>> loadFieldsData()throws Exception
	{
		logInfo("loadFieldsData(...) Start....");
		
		List<CanonicalFieldPojo> polledVals = null;
		
		// This is where data will be populated.
		HashMap<String, ArrayList<FieldCanonicalAttribute>> data = null;
		
		String query = "select mf.CHANNELTYPE,mf.MSGTYPE,mf.MSGSUBTYPE,mf.FIELD_SEQ,ff.MSG_FIELD_ID,ff.field_eoc_ind,ff.FIELD_NO,ff.FIELD_CANONICAL,ff.field_comp_seq,ff.field_comp_mandopt,ff.FIELD_CONSIDERATION,ff.FIELD_COMP_FORMAT from ta_msg_format mf,ta_fields_format ff where mf.msg_field_id = ff.msg_field_id order by ff.msg_field_id,ff.FIELD_NO,ff.field_comp_seq";
		try
		{
			polledVals = jdbcTemplate.query(query, new CanonicalFieldMapper());
			
			System.out.println("No of Rows Fetched from DataBase : " + polledVals.size());
			data = doInstrumentation(polledVals);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch(IncorrectResultSizeDataAccessException e)
		{
			logger.error(e,e);
		}
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("loadFieldsData(...) End....");
		return data;
	}
	
	/**
	 * 
	 * @param List<CanonicalFieldPojo>
	 * @return HashMap<String, ArrayList<FieldCanonicalAttribute>>
	 * 
	 * This method will take a List Object and iterate on List.
	 * Each list index value contains a POJO Object from which
	 * we take field_Canonical and field_NO and set these attributes in FieldCanonicalAttribute(POJO Class) and store this POJO in a ArrayList(ArrayList<FieldCanonicalAttribute>).
	 * This Above ArrayList will be stored as a value in another map whose key is a combination of
	 * channelType+msgType+msgSubTytpe 
	 */
	private HashMap<String, ArrayList<FieldCanonicalAttribute>> doInstrumentation(List<CanonicalFieldPojo> obj)
	{
		ArrayList<FieldCanonicalAttribute> holder = new ArrayList<FieldCanonicalAttribute>();
		
		// Will take channeltype + msgtype + msgsubtype as key and value as holder(hashmap)
		HashMap<String, ArrayList<FieldCanonicalAttribute>> instrumentor = new HashMap<String, ArrayList<FieldCanonicalAttribute>>();
		

		for(int i=0;i<obj.size();i++)
		{
			CanonicalFieldPojo  nextobject = null;
			CanonicalFieldPojo  object = obj.get(i);
			if(i+1<obj.size())
			{
				nextobject = obj.get(i+1);
			}
				
			/*if(object.getChannelType().equalsIgnoreCase("SWIFT"))
			{*/
				
				//if(StringUtils.isNotBlank(object.getFieldCanonicalAtt()) && StringUtils.isNotEmpty(object.getFieldCanonicalAtt()) && StringUtils.isNotEmpty(object.getField_Seq()) && StringUtils.isNotEmpty(object.getFieldEocInd()) &&  object.getFieldEocInd()!=null )
				//{
					FieldCanonicalAttribute fieldCanonicalAttributeObj = new FieldCanonicalAttribute();
					
					//Store the Values in POJO Object
					fieldCanonicalAttributeObj.setFieldCanonicalAtt(object.getFieldCanonicalAtt());
					fieldCanonicalAttributeObj.setFieldNo(object.getFieldNo());
					fieldCanonicalAttributeObj.setFieldSeq(object.getField_Seq());
					fieldCanonicalAttributeObj.setFieldCompMandOpt(object.getFieldCompMandOpt());
					fieldCanonicalAttributeObj.setFieldCompSeq(object.getFieldCompSeq());
					fieldCanonicalAttributeObj.setFieldEocInd(object.getFieldEocInd());
					fieldCanonicalAttributeObj.setFieldcnsdr(object.getField_cnsdr());
					fieldCanonicalAttributeObj.setField_comp_fmt(object.getField_comp_fmt());
					fieldCanonicalAttributeObj.setMsg_field_id(object.getField_Id());
					
					// Store the Pojo Object in arrayList
					holder.add(fieldCanonicalAttributeObj);
					
					//Store the ArrayList Object as Value in HashMap
					//instrumentor.put(object.getChannelType() + object.getMsgType() + object.getMsgSubType(), swiftHolder);
				//}
				if (i+1 < obj.size())
				{
					if(!object.getChannelType().equalsIgnoreCase(nextobject.getChannelType()) || !object.getMsgType().equalsIgnoreCase(nextobject.getMsgType()) || !object.getMsgSubType().equalsIgnoreCase(nextobject.getMsgSubType()))
					{	
						instrumentor.put(object.getChannelType() + object.getMsgType() + object.getMsgSubType(), holder);
						holder=null;
						holder = new ArrayList<FieldCanonicalAttribute>();
					}
				}
				else
				{
					//this is the last arraylist so add it to the hashmap
					instrumentor.put(object.getChannelType() + object.getMsgType() + object.getMsgSubType(), holder);
				}
		}
		 
		return instrumentor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#getDstMsgtype(java.lang.String, java.lang.String)
	 */
	public List<String> getDstMsgtype(String srcMsgType, String srcSubMsgType, String dstChnlType, String direction)throws Exception
	{
		SqlRowSet srs = null;
		List<String> holder = new ArrayList<String>();;

		logInfo("getDstMsgtype(...) Start...");
		String query = "select DST_MSGTYPE,DST_SUB_MSGTYPE from TA_MSG_TYPE_MAPPING where SRC_MSGTYPE=? and SRC_SUB_MSGTYPE=? and CHNLTYPE=? and DIRECTION=?";
		try
		{
			srs = jdbcTemplate.queryForRowSet(query, new Object[]{srcMsgType, srcSubMsgType,dstChnlType,direction});
			while(srs.next())
			{
				holder.add(srs.getString("DST_MSGTYPE"));
				holder.add(srs.getString("DST_SUB_MSGTYPE"));
			}
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getDstMsgtype(...) End...");
		return holder;
	}
	
	public List<String> getAckMsgtype(String srcMsgType, String srcSubMsgType, String srcChnlType, String direction)throws Exception
	{
		SqlRowSet srs = null;
		List<String> holder = new ArrayList<String>();;

		logInfo("getDstMsgtype(...) Start...");
		String query = "select ACK_MSGTYPE,ACK_MSG_SUBTYPE from TA_MSG_TYPE_MAPPING where SRC_MSGTYPE=? and SRC_SUB_MSGTYPE=? and CHNLTYPE=? and DIRECTION=?";
		try
		{
			srs = jdbcTemplate.queryForRowSet(query, new Object[]{srcMsgType, srcSubMsgType,srcChnlType,direction});
			while(srs.next())
			{
				holder.add(srs.getString("ACK_MSGTYPE"));
				holder.add(srs.getString("ACK_MSG_SUBTYPE"));
			}
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getDstMsgtype(...) End...");
		return holder;
	}
	
	public List<String> getRtnMsgtype(String srcMsgType, String srcSubMsgType, String dstChnlType, String direction)throws Exception
	{
		SqlRowSet srs = null;
		List<String> holder = new ArrayList<String>();;

		logInfo("getDstMsgtype(...) Start...");
		String query = "select RTN_MSGTYPE,RTN_SUB_MSGTYPE from TA_MSG_TYPE_MAPPING where SRC_MSGTYPE=? and SRC_SUB_MSGTYPE=? and CHNLTYPE=? and DIRECTION=?";
		try
		{
			srs = jdbcTemplate.queryForRowSet(query, new Object[]{srcMsgType, srcSubMsgType,dstChnlType,direction});
			while(srs.next())
			{
				holder.add(srs.getString("RTN_MSGTYPE"));
				holder.add(srs.getString("RTN_SUB_MSGTYPE"));
			}
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getDstMsgtype(...) End...");
		return holder;
	}

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#getDstChnlType(java.lang.String)
	 */
	public String getDstChnlType(String dstEiId)throws Exception
	{
		String dstChnlType=null;
		logInfo("getDstChnlType(...) Start...");
		String query = "select EI_FORMAT from ta_ei where EI_CODE=?";
		try
		{
			dstChnlType = jdbcTemplate.queryForObject(query, String.class, new Object[]{dstEiId});
			
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getDstChnlType(...) End...");
		return dstChnlType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#getInitialisedValBranch(java.lang.String, java.lang.String)
	 */
	public String getInitialisedValBranch(String initEntry, String branch)throws Exception
	{
		String result=null;
		Clob clob = null;
		try
		{
			String bicQuery = "select init_value from initialisationm where init_entry = '" + initEntry + "' and init_branch='" +branch +"'";
			clob = jdbcTemplate.queryForObject(bicQuery, Clob.class);
			int clobLength = (int) clob.length();
			result = clob.getSubString(1, clobLength);
		}
		catch (EmptyResultDataAccessException e) {
			logger.info("Value not found for given branch");
			try {
				result = getInitialisedValue(initEntry);
			} catch (Exception e1) {
				logger.error(e1, e1);
				throw new Exception(e);
			}
		}
		
		catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		return result;
	
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.esb.daos.EsbServiceDao#getMobNo(java.lang.String)
	 */
	public String getMobNo(String acctNum)throws Exception
	{
		String result = null;
		//Fetch the first address if only account is available to fetch the mobile number - kind of default address and default mobile number
		String query = "select ADDR_MOBILE from ta_addresses where ADDR_REF = (select ACCT_ADDRREF from ta_accounts where ACCT_NUM='" + acctNum + "') AND ADDR_SEQ = 1";
		try
		{
			result = jdbcTemplate.queryForObject(query, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return result;
	}

	//Insert records in Ack table
	public boolean insertAckCanonicalDetails(AcknowledgementCanonical ackCan)throws Exception
	{
		logInfo("insertAckCanonicalDetails(...) START...");
		boolean result= true;
		
		if(ackCan != null)
		{
			int i = 0;
			Object[] valuesArray =	new Object[100];
			String query = null;
			StringBuilder columns = new StringBuilder();
			StringBuilder count = new StringBuilder();
			
			columns.append("insert into ta_acknowledgements");
			columns.append(NgphEsbConstants.NGPH_SPACE);
			columns.append("(");
			
			count.append("values");
			count.append(NgphEsbConstants.NGPH_SPACE);
			count.append("(");
			
			if(ackCan.getGrpId() != null)
			{
				columns.append("MSGS_GRP_MSGID,");
				count.append("?,");
				valuesArray[i] = ackCan.getGrpId();
				i++;
			}
			if(ackCan.getGrpSeq() != null)
			{
				columns.append("GRPSEQ,");
				count.append("?,");
				valuesArray[i] = ackCan.getGrpSeq();
				i++;
			}
			if(ackCan.getAckReceivedTmStmp() != null)
			{
				columns.append("msgackrcvdtimestamp,");
				count.append("?,");
				valuesArray[i] = ackCan.getAckReceivedTmStmp();
				i++;
			}
			if(ackCan.getAckReasonCode() != null)
			{
				columns.append("msgackreasoncode,");
				count.append("?,");
				valuesArray[i] = ackCan.getAckReasonCode();
				i++;
			}
			if(ackCan.getAckType() != null)
			{
				columns.append("msgacktype,");
				count.append("?,");
				valuesArray[i] = ackCan.getAckType();
				i++;
			}
			
			if(ackCan.getMsgBranch() != null)
			{
				columns.append("msgbranch,");
				count.append("?,");
				valuesArray[i] = ackCan.getMsgBranch();
				i++;
			}
			if(ackCan.getMsgDept() != null)
			{
				columns.append("msgdept,");
				count.append("?,");
				valuesArray[i] = ackCan.getMsgDept();
				i++;
			}
			if(ackCan.getMsgDirection() != null)
			{
				columns.append("msgdirection,");
				count.append("?,");
				valuesArray[i] = ackCan.getMsgDirection();
				i++;
			}
			
			if(ackCan.getDstChnlType() != null)
			{
				columns.append("msgdstchnltype,");
				count.append("?,");
				valuesArray[i] = ackCan.getDstChnlType();
				i++;
			}
			if(ackCan.getDstEiId() != null)
			{
				columns.append("msgdsteiid,");
				count.append("?,");
				valuesArray[i] = ackCan.getDstEiId();
				i++;
			}
			if(ackCan.getDstSubMsgType() != null)
			{
				columns.append("msgdstsubtype,");
				count.append("?,");
				valuesArray[i] = ackCan.getDstSubMsgType();
				i++;
			}if(ackCan.getDstMsgType() != null)
			{
				columns.append("msgdsttype,");
				count.append("?,");
				valuesArray[i] = ackCan.getDstMsgType();
				i++;
			}
			
			if(ackCan.getMsgId() != null)
			{
				columns.append("msgid,");
				count.append("?,");
				valuesArray[i] = ackCan.getMsgId();
				i++;
			}if(ackCan.getLastServiceId() != null)
			{
				columns.append("msglastsvid,");
				count.append("?,");
				valuesArray[i] = ackCan.getLastServiceId();
				i++;
			}
			if(ackCan.getMsgMur() != null)
			{
				columns.append("msgmur,");
				count.append("?,");
				valuesArray[i] = ackCan.getMsgMur();
				i++;
			}
			if(ackCan.getMsgOriginalId() != null)
			{
				columns.append("msgorgnlid,");
				count.append("?,");
				valuesArray[i] = ackCan.getMsgOriginalId();
				i++;
			}if(ackCan.getSeqNo() != null)
			{
				columns.append("msgseqno,");
				count.append("?,");
				valuesArray[i] = ackCan.getSeqNo();
				i++;
			}if(ackCan.getSessionNo() != null)
			{
				columns.append("msgsessionno,");
				count.append("?,");
				valuesArray[i] = ackCan.getSessionNo();
				i++;
			}
		
			if(ackCan.getSrcChnlType() != null)
			{
				columns.append("msgsrcchnltype,");
				count.append("?,");
				valuesArray[i] = ackCan.getSrcChnlType();
				i++;
			}if(ackCan.getSrcEiId() != null)
			{
				columns.append("msgsrceiid,");
				count.append("?,");
				valuesArray[i] =ackCan.getSrcEiId();
				i++;
			}
			
			if(ackCan.getSrcSubMsgType() != null)
			{
				columns.append("msgsrcsubtype,");
				count.append("?,");
				valuesArray[i] = ackCan.getSrcSubMsgType();
				i++;
			}if(ackCan.getSrcMsgType() != null)
			{
				columns.append("msgsrctype,");
				count.append("?,");
				valuesArray[i] = ackCan.getSrcMsgType();
				i++;
			}
			if(ackCan.getRulesApplied() != null)
			{
				columns.append("MSGRULESAPP,");
				count.append("?,");
				valuesArray[i] = ackCan.getRulesApplied();
				i++;
			}
		
			if(ackCan.getMsgTmstmp() != null)
			{
				columns.append("msgtimestamp,");
				count.append("?,");
				valuesArray[i] = ackCan.getMsgTmstmp();
				i++;
			}if(ackCan.getSndrTxnId() != null)
			{
				columns.append("MSGSNDRTXNID,");
				count.append("?,");
				valuesArray[i] = ackCan.getSndrTxnId();
				i++;
			}if(ackCan.getSndrPymtPriority() != null)
			{
				columns.append("MSGSNDRPYMTPR,");
				count.append("?,");
				valuesArray[i] = ackCan.getSndrPymtPriority();
				i++;
			}
			if(ackCan.getSenderBank() != null)
			{
				columns.append("MSGSNDRBANK,");
				count.append("?,");
				valuesArray[i] = ackCan.getSenderBank();
				i++;
			}if(ackCan.getReceiverBank() != null)
			{
				columns.append("MSGSRCVRBANK,");
				count.append("?,");
				valuesArray[i] = ackCan.getReceiverBank();
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
			

			//to avoid nullextra values and to avoid invalid column type errors
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
				logger.error(query);
				logger.error(e,e);
				result = false;
			}
		}
		else
		{
			logger.error("AckCanonical object is NULL and so record cannot be inserted");
			result = false;
		}
		logger.info("insertAckCanonicalDetails(...) ENDS...");
		return result;
}


	public String getAppId(String hostId)throws Exception
	{
		String appId = null;
		String query = "select EI_APPID from ta_ei where EI_CODE =?";
		try
		{
			appId = jdbcTemplate.queryForObject(query, new Object[]{hostId}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return appId;
	}
	
	public int getEIStatus(String eiCode)throws Exception
	{
		int eiStatus=0;
		String query = "select EI_STATUS from ta_ei where EI_CODE =?";
		try
		{
			eiStatus = jdbcTemplate.queryForObject(query, new Object[]{eiCode}, Integer.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return eiStatus;
	}
	
	public void populateIMPSData(ResponseBean obj,String msgRef, String stan)throws Exception
	{
		logInfo("populateIMPSData(...) START...");
		if(obj != null)
		{
			int i = 0;
			Object[] valuesArray =	new Object[20];
			String query = null;
			StringBuilder columns = new StringBuilder();
			StringBuilder count = new StringBuilder();
			
			columns.append("insert into TA_IMPS_TX");
			columns.append(NgphEsbConstants.NGPH_SPACE);
			columns.append("(");
			
			count.append("values");
			count.append(NgphEsbConstants.NGPH_SPACE);
			count.append("(");
			
			if(msgRef!= null)
			{
				columns.append("IMPS_MSGREF,");
				count.append("?,");
				valuesArray[i] = msgRef;
				i++;
			}
			if(stan!= null)
			{
				columns.append("IMPS_STAN,");
				count.append("?,");
				valuesArray[i] = stan;
				i++;
			}
			if(obj.getMsgType()!= null)
			{
				columns.append("IMPS_MSGTYPE,");
				count.append("?,");
				valuesArray[i] = obj.getMsgType();
				i++;
			}
			if(obj.getMsgSubType()!= null)
			{
				columns.append("IMPS_MSGSUBTYPE,");
				count.append("?,");
				valuesArray[i] = obj.getMsgSubType();
				i++;
			}
			if(obj.getReqTmStmp() != null)
			{
				columns.append("IMPS_REQTIMESTMP,");
				count.append("?,");
				valuesArray[i] = obj.getReqTmStmp();
				i++;
			}
			if(obj.getResCode() != null)
			{
				columns.append("IMPS_RESCODE,");
				count.append("?,");
				valuesArray[i] = obj.getResCode();
				i++;
			}
			if(obj.getMsgDirection() != null)
			{
				columns.append("IMPS_DIRECTION,");
				count.append("?,");
				valuesArray[i] = obj.getMsgDirection();
				i++;
			}
			if(obj.getResTmStmp() != null)
			{
				columns.append("IMPS_RESTIMESTMP,");
				count.append("?,");
				valuesArray[i] = obj.getResTmStmp();
				i++;
			}
			
			if(obj.getVerSendCt() >=0)
			{
				columns.append("IMPS_VERFCTNSENDCNT,");
				count.append("?,");
				valuesArray[i] = obj.getVerSendCt();
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
			System.out.println("Query Constructed : " + query);
			

			//to avoid nullextra values and to avoid invalid column type errors
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
				throw new Exception(e);
			}
		}
		else
		{
			logDebuggers("Response Bean  object is NULL...");
		}
		logInfo("populateIMPSData(...) ENDS...");
	}
	
	
	public void updateIMPSData(ResponseBean obj, String msgRef, String stan)throws Exception
	{

		logInfo("updateIMPSData(...) START...");
		if(obj != null)
		{
			int i = 0;
			Object[] valuesArray =	new Object[20];
			StringBuilder query = new StringBuilder();
			query.append("UPDATE TA_IMPS_TX SET");
			query.append(NgphEsbConstants.NGPH_SPACE);
			
			if(msgRef!= null)
			{
				query.append("IMPS_MSGREF = ?,");
				valuesArray[i] = msgRef;
				i++;
			}
			if(stan!= null)
			{
				query.append("IMPS_STAN = ?,");
				valuesArray[i] = stan;
				i++;
			}
			if(obj.getMsgType()!= null)
			{
				query.append("IMPS_MSGTYPE = ?,");
				valuesArray[i] = obj.getMsgType();
				i++;
			}
			if(obj.getMsgSubType()!= null)
			{
				query.append("IMPS_MSGSUBTYPE = ?,");
				valuesArray[i] = obj.getMsgSubType();
				i++;
			}
			if(obj.getReqTmStmp() != null)
			{
				query.append("IMPS_REQTIMESTMP = ?,");
				
				valuesArray[i] = obj.getReqTmStmp();
				i++;
			}
			if(obj.getResCode() != null)
			{
				query.append("IMPS_RESCODE = ?,");
				valuesArray[i] = obj.getResCode();
				i++;
			}
			if(obj.getMsgDirection() != null)
			{
				query.append("IMPS_DIRECTION = ?,");
				valuesArray[i] = obj.getMsgDirection();
				i++;
			}
			if(obj.getResTmStmp() != null)
			{
				query.append("IMPS_RESTIMESTMP = ?,");
				valuesArray[i] = obj.getResTmStmp();
				i++;
			}
			
			if(obj.getVerSendCt() >=0)
			{
				query.append("IMPS_VERFCTNSENDCNT =?,");
				valuesArray[i] = obj.getVerSendCt();
				i++;
			}
			
			//Fetch the final String removing the last extra , value
			String stringQuery  = query.toString().substring(0,query.toString().length()-1);
			
			stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
			stringQuery = stringQuery.concat("WHERE IMPS_MSGREF = ?");
			valuesArray[i]= msgRef;
			
			System.out.println("Query===> " + stringQuery);
			//to avoid nullextra values and to avoid invalid column type errors
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
				jdbcTemplate.update(stringQuery, actualArray); 
			}
			catch (Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		}
		else
		{
			logDebuggers("Response Bean  object is NULL...");
		}
		logInfo("updateIMPSData(...) ENDS...");
		
	}
	
	public 	void populateTCPStatus(String mes, String msgRef, int msgStatus)throws Exception
	{
		logInfo("populateTCPStatus(...) START...");
		String query = " insert into TA_TCP_QUEUE (RAW_MSG, MSGREF,MSGSTATUS)"
			+ " values ('" + mes +"','"+ msgRef +"'," + msgStatus+ ")";
		try
		{
			jdbcTemplate.execute(query);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}

		logInfo("populateTCPStatus(...) ENDS...");
	}
	
	public void populateMobNoforMMID(String accNo, String mobNo)throws Exception
	{
		logInfo("populateMobNoforMMID(...) START...");
		String query = "update ta_addresses set addr_mobile='" + mobNo + "' where ADDR_REF = (select ACCT_ADDRREF from ta_accounts where acct_num='" + accNo +"')";
		try
		{
			jdbcTemplate.execute(query);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}

		logInfo("populateMobNoforMMID(...) ENDS...");
	}
	
	public void updateMMID(String mmid, String accNo,String mobNo) throws Exception
	{
		logInfo("updateMMID(...) START...");
		String query = "update ta_addresses set addr_mmid = '" + mmid +"' where ADDR_REF = (select ACCT_ADDRREF from ta_accounts where acct_num='" + accNo +"') and addr_mobile='" + mobNo +"'";
		try
		{
			jdbcTemplate.execute(query);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}

		logInfo("updateMMID(...) ENDS...");
	}

	public List<ErrorCodes> getErrorCodes()throws Exception
	{
		logInfo("getErrorCodes(...) START...");
		List<ErrorCodes> errorCodesList = null;
		try
		{
			errorCodesList  = new ArrayList<ErrorCodes>();
			
			String query = "select * from TA_ERRMSGSM";
			errorCodesList = jdbcTemplate.query(query, new ErrorCodesRowMapper());
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logInfo("getErrorCodes(...) ENDS...");
		return errorCodesList;
		
	}
	
	public ResponseBean getIMPSResponseBeans(String msgRef)throws Exception
	{
		logInfo("getResponseBeans(...) START...");
		List<ResponseBean> respList = null;
		ResponseBean respObj = null;
		try
		{
			String query = "select * from TA_IMPS_TX where IMPS_MSGREF=?";
			respList = jdbcTemplate.query(query, new Object[] {msgRef}, new IMPSRespRowMapper());
			if (respList.size()>0)
			{
				respObj = respList.get(0);
			}
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
		logInfo("getResponseBeans(...) ENDS...");
		return respObj;
	}
	
	public List<TcpBean> getTCPMsgs(int msgstatus)throws Exception
	{
		logInfo("getTCPMsgs(...) START...");
		List<TcpBean> msgList = null;
		try
		{
			msgList = new ArrayList<TcpBean>();
			
			String query = "select RAW_MSG,MSGREF from TA_TCP_QUEUE where MSGSTATUS=?";
			msgList = jdbcTemplate.query(query, new Object[]{msgstatus}, new TcpRowMapper());
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logInfo("getTCPMsgs(...) ENDS...");
		return msgList;

	}
	
	public List<UserInfoBean> getUserEmailAndMob(String userId)throws Exception
	{
		logInfo("getUserEmailAndMob(...) START...");
		List<UserInfoBean> msgList = null;
		try
		{
			msgList = new ArrayList<UserInfoBean>();
			
			String query = "select EMAILID,MOBILENO from ta_sec_users where USR =?";
			msgList = jdbcTemplate.query(query, new Object[]{userId}, new UserMapper());
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logInfo("getUserEmailAndMob(...) ENDS...");
		return msgList;
	}
	
	public List<CustomerInfo> getCustomerInfo(String canVal)throws Exception
	{

		logInfo("getCustomerInfo(...) START...");
		List<CustomerInfo> msgList = null;
		try
		{
			msgList = new ArrayList<CustomerInfo>();
			
			String query = "select ADDR_EMAIL1,ADDR_EMAIL2,ADDR_EMAIL3,ADDR_MOBILE from ta_addresses where ADDR_REF = (select ACCT_ADDRREF from ta_accounts where ACCT_NUM=?)and (ADDR_SEQ is null or ADDR_SEQ=1)";
			msgList = jdbcTemplate.query(query, new Object[]{canVal}, new CustomerRowMapper());
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		
		logInfo("getCustomerInfo(...) ENDS...");
		return msgList;
	}
	
	public int getEIid_MsgConstReqd(String dstEiid)throws Exception
	{
		int isMsgConstReq=0;
		String result = null;
		String query = "select EI_DST_MSG_REQD from TA_EI where EI_CODE=?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{dstEiid}, String.class);
			isMsgConstReq = Integer.parseInt(result);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return isMsgConstReq;
	}
	public int isEventAlertable(String eventId)throws Exception
	{
		int isAlertable=0;
		String query = "select EVENTM_ALERTABLE from ta_event_mast where EVENTM_EVENTID=?";
		try
		{
			isAlertable = jdbcTemplate.queryForObject(query, new Object[]{eventId}, Integer.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return isAlertable;
	}
	
	public String EventAlertFor(String eventID)throws Exception
	{
		String alertFor = null;
		String query = "select EVENTM_ALERT_FOR from ta_event_mast where EVENTM_EVENTID=?";
		try
		{
			alertFor = jdbcTemplate.queryForObject(query, new Object[]{eventID}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return alertFor;
	}
	
	public String getUserId(String eventID)throws Exception
	{
		String userId = null;
		String query = "select EVENTM_ALERT_TO from ta_event_mast where EVENTM_EVENTID=?";
		try
		{
			userId = jdbcTemplate.queryForObject(query, new Object[]{eventID}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		return userId;
	}
	
	public void updateTCPStatus(int status, String mesRef)throws Exception

	{
		logInfo("updateTCPStatus(...) STARTS...");
		String query = "update TA_TCP_QUEUE set MSGSTATUS=? where MSGREF=?";
		try
		{
			jdbcTemplate.update(query, new Object[]{status, mesRef});
		}
		catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("updateTCPStatus(...) ENDS...");

	}
	
	public boolean validateAccNum(String accountNo)throws Exception
	{
		logInfo("validateAccNum(...) Start....");
		boolean isAccNoPresnt = false;
		String query = "SELECT ACCT_NUM FROM TA_ACCOUNTS WHERE ACCT_NUM = ?";
		try{
			isAccNoPresnt = jdbcTemplate.queryForObject(query, new Object[]{accountNo}, Boolean.class);
		}catch(EmptyResultDataAccessException e){
			//logger.error(e,e);
		}catch(IncorrectResultSizeDataAccessException e){
			//logger.error(e,e);
		}catch (NullPointerException e) {
			
		}
		catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("validateAccNum(...) End....");
		return isAccNoPresnt;
	}
	
	public boolean validateMobNum(String accNo, String mobNo)throws Exception
	{
		logInfo("validateMobNum(...) Start....");
		boolean ismobNoPresnt = false;
		String query = "SELECT ADDR_MOBILE FROM TA_ADDRESSES WHERE ADDR_MOBILE = ? and ADDR_REF = (select ACCT_ADDRREF from ta_accounts where acct_num=?)";
		try{
			ismobNoPresnt = jdbcTemplate.queryForObject(query, new Object[]{mobNo, accNo}, Boolean.class);
		}catch(EmptyResultDataAccessException e){
			//logger.error(e,e);
		}catch(IncorrectResultSizeDataAccessException e){
			//logger.error(e,e);
		}catch (NullPointerException e) {
			
		}
		catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("validateMobNum(...) End....");
		return ismobNoPresnt;
	}
	
	public void updatepollStatus(String msgref, String status)throws Exception
	{
		logInfo("updating updatepollStatus for DBPoller");
		String query = "update Ta_msgsPolled set POLL_STATUS=? where msgs_msgref=?";
		try
		{
			jdbcTemplate.update(query, new Object[] {status, msgref});
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("updated updatepollStatus for updatepollStatus");	
	}
	
	public void updateMessageStatusforAckByMsgRef_ReasonCode(String msgref, String reasonCode, String status)throws Exception
	{
			logInfo("updateMessageStatusforAckByMsgRef_ReasonCode(...) START...");
	
			int i = 0;
			Object[] valuesArray =	new Object[5];
			StringBuilder query = new StringBuilder();
			query.append("UPDATE TA_MESSAGES_TX SET");
			query.append(NgphEsbConstants.NGPH_SPACE);
			
			if(StringUtils.isNotBlank(reasonCode) && StringUtils.isNotEmpty(reasonCode))
			{
				query.append("MSGS_RETURN_RSNCODE = ?,");
				valuesArray[i] = reasonCode;
				i++;
			}
			if(StringUtils.isNotBlank(status) && StringUtils.isNotEmpty(status))
			{
				query.append("MSGS_MSGSTS = ?,");
				valuesArray[i] = status;
				i++;
			}
			//Fetch the final String removing the last extra , value
			String stringQuery  = query.toString().substring(0,query.toString().length()-1);
			
			stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
			stringQuery = stringQuery.concat("WHERE MSGS_MSGREF = ?");
			valuesArray[i]= msgref;
			
			System.out.println("Query===> " + stringQuery);
			//to avoid nullextra values and to avoid invalid column type errors
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
				jdbcTemplate.update(stringQuery, actualArray); 
			}
			catch (Exception e)
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		logInfo("updateMessageStatusforAckByMsgRef_ReasonCode(...) ENDS...");
	}

	public boolean validateMMID(String accNo, String mobNo)throws Exception
	{

		logInfo("validateMMID(...) Start....");
		boolean ismobNoPresnt = false;
		String query = "SELECT ADDR_MMID FROM TA_ADDRESSES WHERE ADDR_MOBILE = ? and ADDR_REF = (select ACCT_ADDRREF from ta_accounts where acct_num=?)";
		try{
			ismobNoPresnt = jdbcTemplate.queryForObject(query, new Object[]{mobNo, accNo}, Boolean.class);
		}catch(EmptyResultDataAccessException e){
			//logger.error(e,e);
		}catch(IncorrectResultSizeDataAccessException e){
			//logger.error(e,e);
		}catch (NullPointerException e) {
			
		}
		catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("validateMMID(...) End....");
		return ismobNoPresnt;
	}

	
	public String maxMMID(String accNo, String mobNo)throws Exception
	{
		logInfo("maxMMID(...) Start....");
		String mmid =null;
		
		String query = "select max(ADDR_MMID) from ta_addresses where addr_mobile=? and ADDR_REF = (select ACCT_ADDRREF from ta_accounts where acct_num=?)";
		try{
			mmid = jdbcTemplate.queryForObject(query, new Object[]{mobNo, accNo}, String.class);
		}catch(EmptyResultDataAccessException e){
			logger.error(e,e);
		}catch(IncorrectResultSizeDataAccessException e){
			logger.error(e,e);
		}catch(Exception e){
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("maxMMID(...) End....");
		return mmid;
	}
	public String getHostRepairable(String hostId)throws Exception
	{
		String result = null;
		String query = "select EI_MSGREPAIRABLE from TA_EI where EI_CODE =?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{hostId}, String.class);
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
		return result;
	}
	
	public String getIMPSMapBnkCode(String IMPS_NBIN)throws Exception
	{
		String result = null;
		String query = "select BNK_CODE from TA_BNK_BIN_MAP where IMPS_NBIN =?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{IMPS_NBIN}, String.class);
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
		return result;
	}
	
	public String getIMPSMapBnkNBIN(String bnkCode)throws Exception
	{
		String result = null;
		String query = "select IMPS_NBIN from TA_BNK_BIN_MAP where BNK_CODE =?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{bnkCode}, String.class);
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
		return result;
	}
	
	public String getBankFirstIFSC(String bnkCode)throws Exception
	{
		String result = null;
		String query = "SELECT PARTY_CLRSYSMMBID_MMBID FROM TA_PARTIES WHERE PARTY_CLRSYSMMBID_MMBID LIKE '" + bnkCode + "%' AND ROWNUM = 1";
		try
		{
			result = jdbcTemplate.queryForObject(query, String.class);
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
		return result;
	}
	
	public BigDecimal getAvailableCrLimit(String identifier)throws Exception
	{
		//FIXME - channel and limit for columns to be considered and taken as arguments
		BigDecimal result = null;
		String query = "SELECT AVAILABLE_CRLIMIT FROM TA_LIMITS WHERE IDENTIFIER=?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{identifier}, BigDecimal.class);
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
		return result;
	}
	
	public void updateAvailableCrLimit(String identifier, BigDecimal decAmount)throws Exception
	{
		if (identifier != null && decAmount != null)
		{
			String query = "UPDATE TA_LIMITS SET AVAILABLE_CRLIMIT = AVAILABLE_CRLIMIT - ? WHERE IDENTIFIER=?";
			try
			{
				jdbcTemplate.update(query, new Object[]{decAmount, identifier});
			}
			catch (Exception e) 
			{
				logger.error(e,e);
				throw new Exception(e);
			}
		}
	}
	
	public void logFileData(String fileName, String tableName, String Status)throws Exception
	{
		logger.info("logFileData() Starts");
		
		String query = "insert into TA_FILE_UPLOAD_T (BUSSINESS_DATE, SYSTIME,FILE_NAME,TABLE_NAME,FILE_STATUS) values (?,?,?,?,?)";
		
		/*logger.info(getbusday_Date(getInitialisedValue("DEFBRANCH")));
		logger.info(new Date());
		System.out.println(fileName);
		System.out.println(tableName);
		System.out.println(Status);*/
		try
		{	
			jdbcTemplate.update(query, new Object[]{getbusday_Date(getInitialisedValue("DEFBRANCH")),new Date(),fileName,tableName,Status});
		}
		catch (Exception e) {
			logger.error("Error Occured while inserting File Status " , e);
			throw new Exception(e);
		}
		logger.info("logFileData() Ends");
		
	}

	private void logInfo(String info)
	{
		
        if(logger.isInfoEnabled()){
              logger.info(info);   
        }
	}
	
	 /**
     * log the information when logger is in debug mode 
     * 
     */
	private void logDebuggers(String debugInfo)
	{
		
        if(logger.isDebugEnabled()){
              logger.debug(debugInfo);
        }
	}
	
	public void updateInfoMsgsStatusforAckByMsgRef_ReasonCode(String msgref, String reasonCode, String status)throws Exception
	{
		logInfo("updateInfoMsgsStatusforAckByMsgRef_ReasonCode(...) START...");
		
		int i = 0;
		Object[] valuesArray =	new Object[5];
		StringBuilder query = new StringBuilder();
		query.append("UPDATE TA_MSGS_INFORMATION SET");
		query.append(NgphEsbConstants.NGPH_SPACE);
		
		if(StringUtils.isNotBlank(reasonCode) && StringUtils.isNotEmpty(reasonCode))
		{
			query.append("MSGS_RETURN_RSNCODE = ?,");
			valuesArray[i] = reasonCode;
			i++;
		}
		if(StringUtils.isNotBlank(status) && StringUtils.isNotEmpty(status))
		{
			query.append("MSGS_MSGSTS = ?,");
			valuesArray[i] = status;
			i++;
		}
		//Fetch the final String removing the last extra , value
		String stringQuery  = query.toString().substring(0,query.toString().length()-1);
		
		stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
		stringQuery = stringQuery.concat("WHERE MSGS_MSGREF = ?");
		valuesArray[i]= msgref;
		
	
		//to avoid nullextra values and to avoid invalid column type errors
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
			jdbcTemplate.update(stringQuery, actualArray); 
		}
		catch (Exception e)
		{
			logger.error(":: updateInfoMsgsStatusforAckByMsgRef_ReasonCode -> Update failed " + e.toString());
			throw new Exception(e);
		}
		logInfo("updateInfoMsgsStatusforAckByMsgRef_ReasonCode(...) ENDS...");
	}
	
	//update information message status
	public void updatedInfoMessagestatus(String msgref, String status)throws Exception
	{
		logInfo("updateInfoMsgsStatus(...) START...");
		int i = 0;
		Object[] valuesArray =	new Object[5];
		StringBuilder query = new StringBuilder();
		query.append("UPDATE TA_MSGS_INFORMATION SET");
		query.append(NgphEsbConstants.NGPH_SPACE);
		
		if(StringUtils.isNotBlank(status) && StringUtils.isNotEmpty(status))
		{
			query.append("MSGS_MSGSTS = ?,");
			valuesArray[i] = status;
			i++;
		}
		
		String stringQuery  = query.toString().substring(0,query.toString().length()-1);
		
		stringQuery = stringQuery.concat(NgphEsbConstants.NGPH_SPACE);
		stringQuery = stringQuery.concat("WHERE MSGS_MSGREF = ?");
		valuesArray[i]= msgref;
			
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
			jdbcTemplate.update(stringQuery, actualArray); 
		}
		catch (Exception e)
		{
			logger.error(":: updatedInfoMessagestatus -> Update failed " + e.toString());
			throw new Exception(e);
		}
		logInfo("updatedInfoMessagestatus(...) ENDS...");
	}

	public BigDecimal getCrLimit(String identifier) throws Exception {
		
		BigDecimal result = null;
		String query = "SELECT CREDIT_LIMIT FROM TA_LIMITS WHERE IDENTIFIER=?";
		try
		{
			result = jdbcTemplate.queryForObject(query, new Object[]{identifier}, BigDecimal.class);
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
		return result;
	}

	//get reporting data from DB
	public List<String> getPDFReportData(String msgRef) throws Exception {
		List<String> reportList = null;
		SqlRowSet srs = null;
		logInfo("getPDFReportData(...) Start...");
		String query = "SELECT RPT.MSGS_PMTID_INSTRID, TMT.LC_TYPE, RPT.MSGS_INSTGAGT_BKCD,RPT.MSGS_INSTDAGT_BKCD, TMT.MSGS_DBTR_NM, TMT.MSGS_CDTR_NM, RPT.MSGS_INTRBKSTTLMDT, RPT.MSGS_INTRBKSTTLMCCY, RPT.MSGS_INTRBKSTTLMAMT, RPT.MSGS_MSGSTS, RPT.MSGS_TXNTYPE "
						+"FROM TA_MSGS_RPT RPT, TA_MESSAGES_TX TMT " +
						"WHERE RPT.MSGS_MSGREF = TMT.MSGS_MSGREF AND RPT.MSGS_MSGREF = ?";
		try
		{
			reportList = new ArrayList<String>();
			srs = jdbcTemplate.queryForRowSet(query, new Object[]{msgRef});
			while(srs.next())
			{
				reportList.add(srs.getString("MSGS_PMTID_INSTRID"));
				reportList.add(srs.getString("LC_TYPE"));
				reportList.add(srs.getString("MSGS_INSTGAGT_BKCD"));
				reportList.add(srs.getString("MSGS_INSTDAGT_BKCD"));
				reportList.add(srs.getString("MSGS_DBTR_NM"));
				reportList.add(srs.getString("MSGS_CDTR_NM"));
				reportList.add(srs.getString("MSGS_INTRBKSTTLMDT"));
				reportList.add(srs.getString("MSGS_INTRBKSTTLMCCY"));
				reportList.add(srs.getString("MSGS_INTRBKSTTLMAMT"));
				//reportList.add(PaymentStatusEnum.findPaymentStatusEnumByCode(srs.getString("MSGS_TXNTYPE")).toString());
				//reportList.add(srs.getString("MSGS_TXNTYPE"));
				
			}
		}catch(EmptyResultDataAccessException e)
		{
			logger.error(e,e);
		}
		catch (IncorrectResultSizeDataAccessException e) {
			logger.error(e,e);
		}catch(Exception e)
		{
			logger.error(e,e);
			throw new Exception(e);
		}
		logInfo("getPDFReportData(...) End...");
		return reportList;
	}
	
	
	public String getOriginalLcNo(String refNo)throws Exception
	{
		String LcNo =null;
		String query = "SELECT DISTINCT(LC_NUMBER) FROM TA_MESSAGES_TX WHERE LC_NUMBER=?";
		try
		{
			LcNo = jdbcTemplate.queryForObject(query, new Object[]{refNo}, String.class);
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
		return LcNo;
	}

	/*public int getNoofProcIteration(String msgRef) throws Exception {
		int noofProcInteration =0;
		
		String query = "SELECT NOOF_PROC_ITERATION FROM TA_MESSAGES_TX WHERE MSGS_MSGREF=?";
		try
		{
			noofProcInteration = jdbcTemplate.queryForObject(query, new Object[]{msgRef}, Integer.class);
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
		return noofProcInteration;
	}*/
	
}
