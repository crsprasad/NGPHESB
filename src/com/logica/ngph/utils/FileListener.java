package com.logica.ngph.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.commons.lang.*;

import com.logica.ngph.esb.services.STPFileUpload;
import com.logica.ngph.utils.EventLogger;

public class FileListener extends Thread 
{              
	static Logger logger = Logger.getLogger(FileListener.class);
	private final static String propName = "System.properties";
    
	private STPFileUpload stpFileUpload;
	
	public void setStpFileUpload(STPFileUpload stpFileUpload) 
	{
		this.stpFileUpload = stpFileUpload;
	}
	
	  
	private boolean runSignal = true;           

	private static int fileListenerSleepTime;
	private static String fileListenerInputPath;
	private static String fileListenerOutputPath;
	private static String fileListenerErrorPath;
	
	static
	{
		Properties props = new Properties();
		try 
		{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			fileListenerSleepTime=Integer.parseInt(props.getProperty("FileListenerSleepTime"));
			fileListenerInputPath=props.getProperty("FileListenerInputPath");
			fileListenerOutputPath=props.getProperty("FileListenerOutputPath");
			fileListenerErrorPath=props.getProperty("FileListenerErrorPath");
			
			logger.info("fileListenerSleepTime is : " + fileListenerSleepTime);
			logger.info("fileListenerInputPath is : " + fileListenerInputPath);
			logger.info("fileListenerOutputPath is : " + fileListenerOutputPath);
			logger.info("fileListenerErrorPath is : " + fileListenerErrorPath);
			
		}
		catch (IOException e) 
		{
			logger.error(e, e);
		}
		catch (Exception e) {
			logger.error(e, e);
		}
		
	}
	
	public static void main(String[] args) 
	{
		ApplicationContextProvider.initializeContextProvider();
		
		new FileListener().start();
	}
	
	public void run() 
	{                   
		while(runSignal)
		{        
		  File fileRef = null;
		  String [] auditParams = new String[1];
	      try
	      {
	    	//logger.info("Checking for new file...");                           
			File folder = new File(fileListenerInputPath);                           
			
			//check file names in dir             
			if (folder!=null && folder.isDirectory())
			{                 
			  File[] fileList = folder.listFiles();  
              
			  if(fileList!=null && fileList.length>0)
              {
				for(File one : fileList)
				{   
					fileRef = one;
					String filePath_Name = null;
					if (org.apache.commons.lang.SystemUtils.IS_OS_LINUX)
					{
						filePath_Name =folder +"/" + fileRef.getName();
					}
					else
					{
						filePath_Name =folder +"\\" + fileRef.getName();
					}
					logger.info("File name that is getting processed is : " + filePath_Name);
					auditParams[0] = filePath_Name;
					EventLogger.logEvent("NGPHFILACT0001", FileListener.class, null, auditParams);
					StringBuilder sb = new StringBuilder();

					FileInputStream fstream = new FileInputStream(filePath_Name);
					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String strLine;
					while ((strLine = br.readLine()) != null)   
					{
						  sb.append(strLine +"\r\n");
					}
					in.close();
					//Process file Contents
					doProcess(fileRef.getName(), sb.toString());
					
					//move the file to Output Directory
					Timestamp tm = new Timestamp(Calendar.getInstance().getTimeInMillis());
					String processedtimeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(tm); 
					String outPutFileName = fileRef.getName().substring(0, fileRef.getName().indexOf(".")) +".processed"+ processedtimeStamp;
					if (org.apache.commons.lang.SystemUtils.IS_OS_LINUX)
					{
						fileRef.renameTo(new File(fileListenerOutputPath + "/" + outPutFileName));
					}
					else
					{
						fileRef.renameTo(new File(fileListenerOutputPath + "\\" + outPutFileName));
					}
					logger.info("File Moved to Output Folder");
					EventLogger.logEvent("NGPHFILACT0002", FileListener.class, null, auditParams);
					   
					// Delete the file from Input Folder
					fileRef.delete();
				} 
              }
			}                           
            // Make the Current thread wait for time given period
			sleep(fileListenerSleepTime);
	      }
	      catch (Exception e) 
	      {
			logger.error(e, e);
			
			//move the file to Error Directory
			Timestamp tm = new Timestamp(Calendar.getInstance().getTimeInMillis());
			String errortimeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(tm); 
		    String errorFileName = fileRef.getName().substring(0, fileRef.getName().indexOf(".")) +".error"+ errortimeStamp;
		    if (org.apache.commons.lang.SystemUtils.IS_OS_LINUX)
			{
		    	fileRef.renameTo(new File(fileListenerErrorPath + "/" + errorFileName));
			}
			else
			{
				fileRef.renameTo(new File(fileListenerErrorPath + "\\" + errorFileName));
			}
			logger.info("File Moved to Error Folder");
			EventLogger.logEvent("NGPHFILACT0003", FileListener.class, null, auditParams);
			// Delete the file from Input Folder
			fileRef.delete();
			
		} 
		  }
		}//While Loop Ends          
	    
	
	

	public void doProcess(String fileName, String fileData) throws Exception 
	{
		String Status = null;
		 try
		 {
			 //filename ="002.txt";
			 stpFileUpload = (STPFileUpload)ApplicationContextProvider.getBean("stpFileUpload");
			 Status = stpFileUpload.doProcess(fileData, fileName);
		 }
		 catch (Exception e)
		 {
			 logger.error("Exception Occured while file Upload", e);
			 Status = "F";
			 throw new Exception();
		 }
		 
		 try
		 {
			 stpFileUpload.logFileStatus(fileName.substring(0, fileName.indexOf(".")), Status);
		 }
		 catch (Exception e) {
			 logger.error("Exception Occured while file Upload in FileUpload_T ", e);
			 throw new Exception();
		}
	}
}             
