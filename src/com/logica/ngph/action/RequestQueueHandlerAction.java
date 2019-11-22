package com.logica.ngph.action;

import java.util.List;
import java.util.Set;

import javax.jms.BytesMessage;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.common.utils.PropertyReader;
import com.logica.ngph.esb.AuditServiceClient;
import com.logica.ngph.esb.QueueInitializerManager;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.daos.RequestQueueHandlerDao;
import com.logica.ngph.esb.services.RequestQueueHandlerService;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.NGPHEsbUtils;
import com.logica.ngph.validators.services.IMsgFieldDataInitializer;
import com.logica.ngph.validators.services.IMsgFormatDataInitializer;

public class RequestQueueHandlerAction extends AbstractActionLifecycle {

	protected ConfigTree _config;           
	      
	// This counter value will help in loading Application context only once.
	private static int counter=0;   

	public RequestQueueHandlerAction(ConfigTree config) {	_config = config; }
	public RequestQueueHandlerAction() {}
	static Logger logger = Logger.getLogger(RequestQueueHandlerAction.class);

	private String providerESB = null;
	private String providerESBdirection=null;
	
	private RequestQueueHandlerDao requestQueueHandlerDao;
	
	private static RequestQueueHandlerDao requestQueueHandlerDaoforQueue;
	
	private static IMsgFieldDataInitializer msgFieldDataInitializer;
	private static IMsgFormatDataInitializer msgFormatDataInitializer;
	
	private static SFMSChannelService sfmsChannelService;


	/**
	 * @param sfmsChannelService the rtgsChannelService to set
	 */
	public static void setRtgsChannelService(SFMSChannelService sfmsChannelService) {
		RequestQueueHandlerAction.sfmsChannelService = sfmsChannelService;
	}

	/**
	 * @param msgFieldDataInitializer the msgFieldDataInitializer to set
	 */
	public void setMsgFieldDataInitializer(
			IMsgFieldDataInitializer msgFieldDataInitializer) {
		RequestQueueHandlerAction.msgFieldDataInitializer = msgFieldDataInitializer;
	}

	/**
	 * @param msgFormatDataInitializer the msgFormatDataInitializer to set
	 */
	public static void setMsgFormatDataInitializer(
			IMsgFormatDataInitializer msgFormatDataInitializer) {
		RequestQueueHandlerAction.msgFormatDataInitializer = msgFormatDataInitializer;
	}

	/**
	 * @param requestQueueHandlerDaoforQueue the requestQueueHandlerDaoforQueue to set
	 */
	public static void setRequestQueueHandlerDaoforQueue(RequestQueueHandlerDao requestQueueHandlerDaoforQueue) 
	{
		RequestQueueHandlerAction.requestQueueHandlerDaoforQueue = requestQueueHandlerDaoforQueue;
	}

	/**
	 * @param requestQueueHandlerDao the requestQueueHandlerDao to set
	 */
	public void setRequestQueueHandlerDao(RequestQueueHandlerDao requestQueueHandlerDao) {
		this.requestQueueHandlerDao = requestQueueHandlerDao;
	}

	private RequestQueueHandlerService requestQueueHandlerService;

	/**
	 * @param requestQueueHandlerService
	 *            the requestQueueHandlerService to set
	 */
	public void setRequestQueueHandlerService(
			RequestQueueHandlerService requestQueueHandlerService) {
		this.requestQueueHandlerService = requestQueueHandlerService;
	}

	/**
	 * This block will initialize Context Provider. This is one time Activity
	 * and will share only one instance of the Context Object within the
	 * Application.
	 */

	static {
		try 
		{
			logger.info("Application loaded value : " + counter);
			if(counter==0)
			{
				/* Initializes the Application context for the Entire Application.  
				 * This is only 1 time activity and will be executed as soon the application is deployed.
				 * It will last in memory until the Application is Undeployed or Server is shut down..
				 */
				ApplicationContextProvider.initializeContextProvider();
				
				/*
				 * This method will create a connection for AuditMessageQueueGW as soon as the Application is deployed
				 * It will last in memory until the Application is Undeployed or Server is shut down..
				 * Since it is cached in memory so performance hurt is removed. 
				 */
				AuditServiceClient.initializeQueueFactory();

				/*
				 * This method will create a connection for any Queue having Connection Factory loaded in cache.
				 */
				ReportRPTClient.initializeQueueFactory();
				
				ReportQueue.initializeQueueFactory();
				
				queueInitializer();
				
				/*
				 * Invoking the MsgFormatDataInitializer and MsgFieldDataInitializer, since this activity is required at Compile time.
				 * Thereby we are invoking it in Static Block, so as soon as the System is Up the Values are Initialized.
				 * When the System is Up already we have DB Operations Pojo Objects in Cache
				 */
				msgFieldDataInitializer = (IMsgFieldDataInitializer) ApplicationContextProvider.getBean("msgFieldDataInitializer");
				msgFieldDataInitializer.getFileds();
				msgFormatDataInitializer = (IMsgFormatDataInitializer) ApplicationContextProvider.getBean("msgFormatDataInitializer");
				msgFormatDataInitializer.getMsgFileds();
				sfmsChannelService = (SFMSChannelService)ApplicationContextProvider.getBean("sfmsChannelService");
				sfmsChannelService.populatefieldsForCanonical();
				//populate error codes in errorCodes GLobal Map
				NGPHEsbUtils.populateErrorCodes();
				
				//Property File Initializer
				new PropertyReader();
				
				//Start Thread for File Listener
				//new FileListener().start();
			}
			else
			{
				logger.info("Application Context has already been initailized, no need to initialize it again");
			}
			
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
		// Incrementing the counter
		counter++;
	}
	
	private static void queueInitializer() throws Exception
	{
		requestQueueHandlerDaoforQueue = (RequestQueueHandlerDao) ApplicationContextProvider.getBean("requestQueueHandlerDao");
		String providers = requestQueueHandlerDaoforQueue.getQueueInitializer();
		
		logger.info("Queues received by Request Handler are :" + providers);
		String queues[] = providers.split(";");
		for(int i=0;i<queues.length;i++)
		{
			logger.info("Queue read from DB are : " + queues[i]);
			QueueInitializerManager.initializeQueueFactory(queues[i]);
		}
	}

	// Main Execution Point
	public void doProcess(Message message) throws Exception {

		try {
			logger.info("RequestQueueHandlerAction Inside doProcess Method");
			logger.info("Header file :: "+message.getHeader().getCall().getReplyTo());
			if (message != null)
			{
				String mes = null;
				
				//If its a ByteMessage (as sent by SFMS)
				if(message.getBody().get() instanceof byte[])
				{
					logger.info("This is a Byte Array");
					byte[] text = (byte[])message.getBody().get();
					String temp = new String(text);
					if(temp.startsWith("{A:"))
					{
						mes = temp;
					}
					else
					{
						mes = temp.substring(2, temp.length());
					}
				}
				//If its a String value
				else if(message.getBody().get() instanceof String) 
				{
					logger.info("This is a String Format");
					mes = message.getBody().get().toString();
				}
				else if(message.getBody().get() instanceof BytesMessage)
				{
					logger.info("This is a Byte Message Type");
					BytesMessage data = (BytesMessage)message.getBody().get();
					mes = data.readUTF();
				}
				else
				{
					logger.info("Unknown Format");
					Object o = message.getBody().get();
					System.out.println(o.getClass() + "******");
					mes = o.toString();
				}
				
				logger.info("Original Message received : " + mes);
				
				// Fetching Bean Object
				requestQueueHandlerService = (RequestQueueHandlerService) ApplicationContextProvider.getBean("requestQueueHandlerService");
				requestQueueHandlerDao = (RequestQueueHandlerDao) ApplicationContextProvider.getBean("requestQueueHandlerDao");
				
				List<String> providers = requestQueueHandlerDao.getEsbProviders();
				logger.info("Providers list received from Ta_EI");
				
				for(int i=0;i<providers.size();i++)
				{
					logger.info(providers.get(i));
				}
				
				Set<String> names = _config.getAttributeNames();
	  			for (String attrName : names) 
	  			{
	  				String value = _config.getAttribute(attrName);
	  				
	  				logger.info("Config Attribute value : " + value);
	  				if(providers.contains(value))
	  				{
	  					providerESB = value;
	  				}
	  				if(attrName.matches("gatewayDirection"))
					{
						providerESBdirection = value;
					}
	  			}
				logger.info("ESB provider fetched : " + providerESB);
				//Ignoring signature component for now - FIXME - validate signature
				if (mes.indexOf("-}{UMAC:") > 0)
				{
					String tempMes = mes.substring(0, mes.indexOf("-}{UMAC:") + 2);
					mes = tempMes;
				}
				requestQueueHandlerService.execute(mes.trim(), providerESB,providerESBdirection);
			} 
			else 
			{
				logger.info("Null Message Received by RequestQueueHandlerAction");
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	/**
	 *  This is the Default Exception Handler provided by ESB, mapping needs to be done in JbossEsb.xml
	 *  Whenever any Exception will Occurs Automatically this method will be invoked.
	 *  We need to set this method name as the property value for this class in JbossEsb.xml
	 */

	 public void exceptionHandler(Message message, Throwable exception)
	 {
		  logger.error("=============================== Request action ExceptionHandler Start==========================");
		  logger.error(message, exception);
		  logger.error("For Message: ");
		  logger.error(message.getBody().get());
		  logger.error("****************************** Request action ExceptionHandler End ***************************");
	 }
}

 