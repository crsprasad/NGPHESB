package com.logica.ngph.esb;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.RequestQueueHandlerDao;
import com.logica.ngph.utils.ApplicationContextProvider;

/** 
 * @author guptarb 
 * */

public class MQProvider extends AbstractActionLifecycle 
{	
	protected ConfigTree	_config;
	static Logger logger = Logger.getLogger(MQProvider.class);
	public MQProvider (ConfigTree config) { _config = config; } 
	public MQProvider () { } 
	
	private final String propName = "System.properties";
	private String mes=null; 
	private String ConfigQueuevalue=null;
	private String isMqServer = null;
	private String hostname = null; // define the name of your host to connect to
	private String channel = null; // define name of channel for client to use
	private String qManager = null; // define name of queue manager object to connect to.
	private String qName = null; // define name of queue to connect to.
	private MQQueueManager qMgr; // define a queue manager object
	
	private EsbServiceDao esbServiceDao;
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	
	private RequestQueueHandlerDao requestQueueHandlerDao;
	public void setRequestQueueHandlerDao(RequestQueueHandlerDao requestQueueHandlerDao) 
	{
		this.requestQueueHandlerDao = requestQueueHandlerDao;
	}
	
	private void init_Client() 
	{
		// Set up MQSeries environment
		MQEnvironment.hostname = hostname; 
		MQEnvironment.channel = channel; 
		MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY,MQC.TRANSPORT_MQSERIES);// Connection
		
		logger.info("Inside Init Method() of MQ Provider..");
		logger.info("MQEnvironment.hostname :" + MQEnvironment.hostname);
		logger.info("MQEnvironment.channel :" + MQEnvironment.channel);
		logger.info("MQ Client Resources Initialized...");
	} 

	public void doProcess(Message message) 
	{
		NgphCanonical canonicalData = null;
		Object rcvdObj = null;
		try 
		{
			if(message!=null)
			{
				logger.info("Inside Do Process of Mq Provider.......");
				rcvdObj = message.getBody().get();
				if(rcvdObj instanceof NgphCanonical)
				{
					canonicalData = (NgphCanonical)rcvdObj;
					mes = canonicalData.getMessage_so_far();
				}
				else if(rcvdObj instanceof AcknowledgementCanonical)
				{
					AcknowledgementCanonical ackCanonical = (AcknowledgementCanonical)rcvdObj;
					mes = ackCanonical.getMsgSoFar();
				}
				else if(rcvdObj instanceof InfoCanonical)
				{
					InfoCanonical infoCanonical = (InfoCanonical)rcvdObj;
					mes = infoCanonical.getMessage_info();
				}
				else
				{
					mes = rcvdObj.toString();
				}
				
				logger.info("Message received by MQ Provider : " + mes);
				
				requestQueueHandlerDao = (RequestQueueHandlerDao) ApplicationContextProvider.getBean("requestQueueHandlerDao");
				List<String> input_providers = requestQueueHandlerDao.FetchInput_Dest_Queue();
				List<String> output_providers = requestQueueHandlerDao.FetchOutput_Dest_Queue();
				
				Set<String> names = _config.getAttributeNames();
	  			for (String attrName : names) 
	  			{
	  				String value = _config.getAttribute(attrName);
	  				
	  				logger.info("Config Attribute value : " + value);
	  				if(input_providers.contains(value))
	  				{
	  					ConfigQueuevalue = value;
	  					break;
	  				}
	  				if(output_providers.contains(value))
	  				{
	  					ConfigQueuevalue = value;
	  					break;
	  				}
	  			}
	  			
	  			logger.info("The Config Queue read from _config params is : " + ConfigQueuevalue);
				Properties props = new Properties();
				props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
				hostname = props.getProperty("MQhostname");
				channel = props.getProperty("MQchannel");
				qManager = props.getProperty("MQqManager");
				isMqServer= props.getProperty("MQSERVER");
				qName = props.getProperty(ConfigQueuevalue);
				
				logger.info("hostname" + hostname);
				logger.info("channel" + channel);
				logger.info("qManager" + qManager);
				logger.info("qName" + qName);
				logger.info("isMqServer" + isMqServer);
				
				if(isMqServer!=null && StringUtils.isNotBlank(isMqServer) && StringUtils.isNotEmpty(isMqServer) && isMqServer.equalsIgnoreCase("N"))
				{
					// Initializing the Server Resources for IBM MQ
					init_Client();
				}
				else
				{
					// Initializing the Client Resources for IBM MQ
					MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY,MQC.TRANSPORT_MQSERIES_BINDINGS);
				}
				qMgr = new MQQueueManager(qManager);
				logger.info(qMgr);
				// Note. All MQSeries Options are prefixed with MQC in Java.
				//MQC.MQOO_INPUT_AS_Q_DEF | 
				int openOptions = MQC.MQOO_OUTPUT;
				MQQueue system_default_local_queue = qMgr.accessQueue(qName, openOptions);
				MQMessage hello_world = new MQMessage();
				hello_world.format = MQC.MQFMT_STRING; 
				hello_world.writeString(mes);
				MQPutMessageOptions pmo = new MQPutMessageOptions();
				logger.info("putting Message in WebSphere queue.......");
				system_default_local_queue.put(hello_world, pmo);
				new QngFileWriter().writeToFile(mes);
				
				//logger.info("Reading Message from queue****");
				// Reading message from the MQ Queue...
				// get the message back again...
				// First define a MQSeries message buffer to receive the message
				//MQMessage retrievedMessage = new MQMessage();
				//retrievedMessage.messageId = hello_world.messageId;
				// Set the get message options..
				//MQGetMessageOptions gmo = new MQGetMessageOptions(); // get the message off the queue..
				//system_default_local_queue.get(retrievedMessage, gmo);
				// And prove we have the message by displaying the UTF message text
				//String msgText = retrievedMessage.readUTF();
				//logger.info("The message read from Queue is : " + msgText);
				// Close the queue
				system_default_local_queue.close();
				// Disconnect from the queue manager
				qMgr.disconnect();
			}
			else
			{
				 logger.warn("Null Value Received by the Mq Provider");
				 logger.warn("Null Message received by MQ Provider");
			} 
		}
		catch (Exception e) 
		{
			if(rcvdObj!=null && rcvdObj instanceof NgphCanonical)
			{
				canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
				canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.EXCEPTION_O));
				try
				{
					esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
					esbServiceDao.updatePaymentDetails(canonicalData);
					new ReportQueue().QueueCall(canonicalData);
					//new ReportRPTAction().doProcess(canonicalData);
				}
				catch(Exception ex)
				{
					logger.error("Exception Occured while updating Payment details ", ex);
				}
			}
			logger.error("Exception Occured in MQ Provider" ,e);
		}
	} 
}