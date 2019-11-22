package com.logica.ngph.action;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.daos.ReportRPTDao;
import com.logica.ngph.utils.ApplicationContextProvider;

public class ReportRPTAction extends AbstractActionLifecycle{
	
	protected ConfigTree	_config;
	
	static Logger logger = Logger.getLogger(ReportRPTAction.class);
	private NgphCanonical canonicalObj = new NgphCanonical();
	public ReportRPTAction (ConfigTree config) { _config = config; } 
	public ReportRPTAction () { } 
	private ReportRPTDao reportRPTDao;
	
	
	public void setReportRPTDao(ReportRPTDao reportRPTDao) {
		this.reportRPTDao = reportRPTDao;
	}
	
	
	public void doProcess(Message message) throws Exception
	 {
		//logger.info("****************** ReportRPTAction STARTED*****************************");
		reportRPTDao = (ReportRPTDao)ApplicationContextProvider.getBean("reportRPTDao");
		/*
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("beans.xml");
		ReportRPTDao reportRPTDao =(ReportRPTDao) 	applicationContext.getBean("reportRPTDao");
		*/
		//logger.info(" Action Listener");
		
		//logger.info(message.getBody().get().toString());
		
		canonicalObj = (NgphCanonical)message.getBody().get();
		
		String msgRef = canonicalObj.getMsgRef();
		
		logger.info("Message ref is : " + msgRef);
		
		String check = reportRPTDao.validateData(msgRef);
		
		logger.info("The table returned val : " + check);
		
		if(check!=null)
		{
			//logger.info("***** CALL FOR UPDATE RECORDS  ******");
			reportRPTDao.insertRPTParsedMessage(canonicalObj, "update",msgRef);
			
		}
		else
		{
			//logger.info("***** CALL FOR INSERT RECORDS  ******");
			reportRPTDao.insertRPTParsedMessage(canonicalObj, "insert",msgRef);
		
		}
		//logger.info("****************** ReportRPTAction ENDS *****************************");
		//logger.info("The final value is : " + canonicalObj.getChennal()+"DIRCTION"+canonicalObj.getDirction()+"    "+canonicalObj.getMessageType()  );
		
	 
	 }
	

}
