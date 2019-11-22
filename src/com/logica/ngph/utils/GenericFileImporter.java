package com.logica.ngph.utils;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.esb.services.STPFileUpload;


public class GenericFileImporter extends AbstractActionLifecycle {
	
	protected ConfigTree	_config;
	public GenericFileImporter (ConfigTree config) { _config = config; } 
	public GenericFileImporter() { } 
	
	static Logger logger = Logger.getLogger(GenericFileImporter.class);
	
	private STPFileUpload stpFileUpload;
	
	public void setStpFileUpload(STPFileUpload stpFileUpload) {
		this.stpFileUpload = stpFileUpload;
	}
	
	public static void main(String[] args) {/*
		
		ApplicationContextProvider.initializeContextProvider();
		GenericFileImporter obj = new GenericFileImporter();

		//Read file data and do Processing
		String txtfileName = "C:/OFF_WORK/jboss-5.1.0.GA/bin/configFile/002.txt";

		  StringBuilder sb = new StringBuilder();

		  try{
			  FileInputStream fstream = new FileInputStream(txtfileName);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  while ((strLine = br.readLine()) != null)   
			  {
				  //System.out.println (strLine);
				  sb.append(strLine +"\r\n");
			  }
			  in.close();
			}catch (Exception e)
			{
			  System.err.println("Error: " + e.getMessage());
			 }
		
			System.out.println("Message Constructed : " + sb);
			obj.doProcess(sb.toString());
	*/}
	
	//public void doProcess(String dataFile)
	public void doProcess(Message message)
	{
		String Status = null;
		String filename = null;
		 try{
			 
			 String dataFile = new String((byte[])message.getBody().get());
			 filename =(String) message.getProperties().getProperty("org.jboss.soa.esb.gateway.original.file.name");
			   
			 //filename ="002.txt";
			 stpFileUpload = (STPFileUpload)ApplicationContextProvider.getBean("stpFileUpload");
			 Status = stpFileUpload.doProcess(dataFile, filename);
		 }
		 catch (Exception e)
		 {
			 logger.error("Exception Occured while file Upload", e);
			 Status = "F";
		 }
		 
		 try
		 {
			 stpFileUpload.logFileStatus(filename.substring(0, filename.indexOf(".")), Status);
		 }
		 catch (Exception e) {
			 logger.error("Exception Occured while file Upload in FileUpload_T ", e);
		}
	}
	
}
