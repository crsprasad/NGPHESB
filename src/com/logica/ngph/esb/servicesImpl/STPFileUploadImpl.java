package com.logica.ngph.esb.servicesImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.logica.ngph.common.dtos.GenericFilePojo;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.services.STPFileUpload;
import com.logica.ngph.utils.ConfigManager;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.utils.FileListener;

public class STPFileUploadImpl implements STPFileUpload {

	static Logger logger = Logger.getLogger(STPFileUploadImpl.class);
	private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<GenericFilePojo>>>> config = null;
	
	private EsbServiceDao esbServiceDao;
	
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}
	
	private PlatformTransactionManager platformTransactionManager;
	
	public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
		this.platformTransactionManager = platformTransactionManager;
	}
	
	private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<GenericFilePojo>>>>  getConfigData(String filename)
	{
		 config = new ConfigManager().getConfig(filename);
		 logger.info("Map Returned from Congif File : " + config);
		 return config;
	}

	@Transactional (propagation = Propagation.REQUIRED, readOnly=false, rollbackFor={Exception.class,RuntimeException.class,Throwable.class})
	public String doProcess(String data, String fileName) throws Exception
	{
		String status = null; 

		/* Programatic Transaction Management
 		 DefaultTransactionDefinition paramTransactionDefinition = new DefaultTransactionDefinition();
		 TransactionStatus status=platformTransactionManager.getTransaction(paramTransactionDefinition );
		 */		 
		try 
		{
		    // execute your business logic here
				performexecute(data, fileName);
				status = "U";
			 //platformTransactionManager.commit(status);
		}
		catch (Exception ex) 
		{
			 //platformTransactionManager.rollback(status);
			 logger.error("Exception occured while file Upload", ex);
			 throw new Exception(ex);
		}
		return status;
	}

	public void logFileStatus(String fileName, String fileStatus) throws Exception
	{
		try
		{	
			// getConfigData("C:/OFF_WORK/jboss-5.1.0.GA/bin/configFile/GenericConfig.xml");
			 getConfigData("configFile/GenericConfig.xml");
			 String dupCheck=null;
			 String foundKey = null;
			 String[] keys = (String[])( config.keySet().toArray( new String[config.size()] ) );
			    for(int i=0;i<keys.length;i++)
			    {
			    	if(fileName.startsWith(keys[i].substring(0, keys[i].length()-1)))
			    	{
			    		foundKey = keys[i];
			    		dupCheck = keys[i].substring(keys[i].length()-1, keys[i].length());
			    		break;
			    	}
			    }
			 
			esbServiceDao.logFileData(fileName, config.get(foundKey).keySet().toArray()[0].toString(), fileStatus);
		}
		catch (Exception e) 
		{
			logger.error("Error Occured while Loading data in File Transaction Table", e);
			throw new Exception(e);
		}
		
	}
	
	public String performexecute(String data, String filename) throws Exception
	{
		int bodyRecordcount = 0;
		String table = null;
		try
		{
			String dataFile = data;  
			getConfigData("configFile/GenericConfig.xml");
			filename = filename.substring(0, filename.indexOf("."));
			logger.info("FileName : " + filename);
			if (config != null)
			{
				String dupCheck = null;
				boolean isFilePrsnt = false;
				String[] keys = (String[])( config.keySet().toArray( new String[config.size()] ) );
				String foundKey = null;
				for(int i=0;i<keys.length;i++)
				{
					if(filename.startsWith(keys[i].substring(0, keys[i].length()-1)))
					{
						isFilePrsnt = true;
						foundKey = keys[i];
						dupCheck = keys[i].substring(keys[i].length()-1, keys[i].length());
						break;
					}
				}
				if(isFilePrsnt == true)
				{
					// Fetch the Map Attribute based on file Name and get Table Name.
					LinkedHashMap<String, LinkedHashMap<String, ArrayList<GenericFilePojo>>> tableName = config.get(foundKey);
					logger.info(" Table Map Retrived from Config File : " + tableName);
					Iterator<String> iterator = tableName.keySet().iterator();
					while (iterator.hasNext())
					{
						table = ( String ) iterator.next();
						logger.info("Table name Read from Configuration : " + table); 
						// Fetch the Map Attribute based on fine Name and get Table Info.
						LinkedHashMap<String, ArrayList<GenericFilePojo>> tableData = tableName.get(table);
						logger.info("Map Retrived from Config File for tableName " + table + " : " + tableData); 
						String rawData[]  = dataFile.split("\r\n");
						String strLine;
						for(int k=0;k<rawData.length;k++)
						{
							strLine = rawData[k];
							if (!StringUtils.trim(strLine).isEmpty())
							{
								String actualData = null;
								String id = strLine.substring(0, 2);
								if (id.equalsIgnoreCase("10") || id.equalsIgnoreCase("99") || id.equalsIgnoreCase("01"))
								{
									// Line Value After removing id (first 2 Characters)
									actualData = strLine.substring(id.length(), strLine.length());
								}
								else
								{
									id = "10";
									actualData = strLine;
								}
								//Check if the key is present in Map
								if(tableData.containsKey(id))
								{
									ArrayList<GenericFilePojo> columns = tableData.get(id);
									//Local list
									ArrayList<GenericFilePojo> dataHolder = new ArrayList<GenericFilePojo>();
									for(int i=0;i<columns.size();i++)
									{
										GenericFilePojo obj = columns.get(i);
										if(obj!=null)
										{
											if(StringUtils.isNotBlank(obj.getColoumnName()) && StringUtils.isNotEmpty(obj.getColoumnName()))
											{
												if(id.equalsIgnoreCase("10"))
												{
													dataHolder.add(obj);
												}
											}
											else
											{
												//String [] auditParams = new String [3];
												//auditParams[0] = filename;
												//auditParams[1] = id;
												//auditParams[2] = "" + i;
												//EventLogger.logEvent("NGPHFILACT0008", FileListener.class, null, auditParams);//Column name blank in configuration for received file {0} for row identifier {1} and field number {2}
											}
										}
										else
										{
											//logger.info("Null Pojo Object Received for Header Id : " + id);
										}
									}
									if(dupCheck.equalsIgnoreCase("E") && esbServiceDao.checkFileData(dataHolder, actualData, table)>0)
									{
										logger.error("Duplicate Check Status with Record Found : " + dupCheck);
										String [] auditParams = new String [2];
										auditParams[0] = filename;
										auditParams[1] = "" + k;
										EventLogger.logEvent("NGPHFILACT0009", FileListener.class, null, auditParams);//Duplicate data in the received file {0} for row {1}
										throw new Exception("Duplicate Record");
									}
									//Normal Functionality
									else
									{
										if(id.equalsIgnoreCase("10"))
										{
											//Update record in DB only for body records
											esbServiceDao.updateFileData(dataHolder,actualData,table);
											++bodyRecordcount;
										}
										//Check for record count in Trailer Block and Roll Back Transaction if record count do not match
										if(id.equalsIgnoreCase("99"))
										{
											int recordCount = Integer.parseInt(actualData.substring(0, 6));
											logger.info("Record Count read from trailer Block : " + recordCount );
											logger.info("Record Count read from Body Block : " + bodyRecordcount);  
											if(bodyRecordcount==recordCount)
											{
												logger.info("Record count match");
											}
											else
											{
												logger.error("Record Count does not match");
												String [] auditParams = new String [1];
												auditParams[0] = filename;
												EventLogger.logEvent("NGPHFILACT0010", FileListener.class, null, auditParams);//Number of records in the details does not match the control count for file {0}.
												throw new Exception("Record Mismatch");
											}
										}
									}
								}
								else
								{
									logger.warn("No key Match found for ID : " + id);
								}
							}
						}
					}
					String [] auditParams = new String [1];
					auditParams[0] = filename;
					EventLogger.logEvent("NGPHFILACT0004", FileListener.class, null, auditParams);
				}
				else
				{
					String [] auditParams = new String [1];
					auditParams[0] = filename;
					EventLogger.logEvent("NGPHFILACT0005", FileListener.class, null, auditParams);
					logger.warn("File Name does not Exists in Config File " + filename);
				}
			}
			else
			{
				String [] auditParams = new String [1];
				auditParams[0] = filename;
				EventLogger.logEvent("NGPHFILACT0007", FileListener.class, null, auditParams);
			}
		}
		catch (Exception e)
		{
			logger.error(e, e);
			String [] auditParams = new String [1];
			auditParams[0] = filename;
			EventLogger.logEvent("NGPHFILACT0006", FileListener.class, null, auditParams);
			throw new Exception(e);
		}
		return table;
	}
}
