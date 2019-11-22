package com.logica.ngph.esb.servicesImpl;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.daos.RequestQueueHandlerDao;
import com.logica.ngph.esb.services.ResponseQueueHandlerService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;

/**
 * @author guptarb
 *
 */
public class ResponseQueueHandlerServiceImpl extends AbstractActionLifecycle implements ResponseQueueHandlerService
{
	static Logger logger = Logger.getLogger(ResponseQueueHandlerServiceImpl.class);	
	protected ConfigTree _config;
	public ResponseQueueHandlerServiceImpl(ConfigTree config) {	_config = config; }
	public ResponseQueueHandlerServiceImpl(){}
	private String providerESB = null;
	private String destQueue = null;
	private RequestQueueHandlerDao requestQueueHandlerDao;
	/**
	 * @param requestQueueHandlerDao the requestQueueHandlerDao to set
	 */
	public void setRequestQueueHandlerDao(RequestQueueHandlerDao requestQueueHandlerDao) 
	{
		this.requestQueueHandlerDao = requestQueueHandlerDao;
	}
	
	
	public void execute(Message mes) throws Exception
	{
		try
		{
			if(mes!=null)
			{
				NgphCanonical canObjct = null;
				AcknowledgementCanonical ackObjct = null;
				InfoCanonical infoObject = null;
				String message = null;
				Object rcvdObj = mes.getBody().get();
				
				if (rcvdObj != null)
				{
					if(rcvdObj instanceof NgphCanonical)
					{
						canObjct = (NgphCanonical)rcvdObj;
						message = canObjct.getMessage_so_far();
					}
					else if(rcvdObj instanceof AcknowledgementCanonical)
					{
						ackObjct = (AcknowledgementCanonical)rcvdObj;
						message = ackObjct.getMsgSoFar();
					}
					else if(rcvdObj instanceof InfoCanonical)
					{
						infoObject = (InfoCanonical)rcvdObj;
						message = infoObject.getMessage_info();
					}
					else
					{
						message = mes.getBody().get().toString();
					}
					
					logger.info("Message Received by ResponseQueueHandlerServiceImpl for the current message : " + message);
					// Case taken care for QNG
					// Removing 999 tag get the value it contains
					if(message!=null && StringUtils.isNotBlank(message) && StringUtils.isNotEmpty(message) && message.contains("{999:"))
					{
						int start = message.indexOf("{999:")+5;
						int last = message.indexOf("}",start)+1;
						String block999 = message.substring(start, last);
						String[] temp = block999.substring(0, block999.indexOf("}")).split(",");
						//String tempEiId = message.substring(message.indexOf("{999:") + 5 );
						//String eiId = tempEiId.substring(0, tempEiId.indexOf("}"));
						//String eiId = message.substring(message.indexOf("{999:") + 5 , message.indexOf("}"));
						String eiId  = temp[0].trim();
						String msgRef =temp[1].trim(); 
						logger.info("Sending Message to host : " + eiId);
						requestQueueHandlerDao = (RequestQueueHandlerDao)ApplicationContextProvider.getBean("requestQueueHandlerDao");
						if (eiId != null && !eiId.trim().isEmpty())
						{
							String dest_queue = requestQueueHandlerDao.getDstHost(eiId);
							logger.info("Destination Queue for QNG is  : " + dest_queue);	
							StringBuilder finalMes = new StringBuilder(message);
							finalMes.delete(message.indexOf("{999:") , (message.indexOf("}", message.indexOf("{999:"))+1));
							
							if(rcvdObj instanceof NgphCanonical)
							{
								logger.info("Sending Message as NGPHCanonical Object with contents as : " + finalMes.toString());
								canObjct.setMessage_so_far(finalMes.toString());
								new ReportRPTClient().call(dest_queue, canObjct);
								EventLogger.logEvent("NGPHRSPHND0003", canObjct, ResponseQueueHandlerServiceImpl.class, msgRef);//Response handler successfully delivered the message to the destination queue in ESB.
							}
							else if(rcvdObj instanceof AcknowledgementCanonical)
							{
								logger.info("Sending Message as AcknowledgementCanonical Object with contents as : " + finalMes.toString());
								ackObjct.setMsgSoFar(finalMes.toString());
								new ReportRPTClient().call(dest_queue, ackObjct);
								EventLogger.logEvent("NGPHRSPHND0003", null, ResponseQueueHandlerServiceImpl.class, msgRef);//Response handler successfully delivered the message to the destination queue in ESB.
							}
							else if(rcvdObj instanceof InfoCanonical)
							{
								logger.info("Sending Message as AcknowledgementCanonical Object with contents as : " + finalMes.toString());
								infoObject.setMessage_info(finalMes.toString());
								new ReportRPTClient().call(dest_queue, infoObject);
								EventLogger.logEvent("NGPHRSPHND0003", null, ResponseQueueHandlerServiceImpl.class, msgRef);//Response handler successfully delivered the message to the destination queue in ESB.
							}
							else
							{
								logger.info("Sending Message as String Object with contents as : " + finalMes.toString());
								new ReportRPTClient().call(dest_queue, finalMes.toString());
								EventLogger.logEvent("NGPHRSPHND0003", null, ResponseQueueHandlerServiceImpl.class, msgRef);//Response handler successfully delivered the message to the destination queue in ESB.
							}
							// This is done purposly so that message should be passed further to QPH System
							message = null;
						}
						else
						{
							EventLogger.logEvent("NGPHRSPHND0001",null, ResponseQueueHandlerServiceImpl.class, msgRef);//Response handler did not find the Destination External Interface ID in the internal message.
						}
					}
					else
					{
						EventLogger.logEvent("NGPHRSPHND0002", null, ResponseQueueHandlerServiceImpl.class, null);//Response handler received empty message or without QNG block.
					}
					// Case taken care for QPH
					if(message!=null && StringUtils.isNotBlank(message) && StringUtils.isNotEmpty(message))
					{
						requestQueueHandlerDao = (RequestQueueHandlerDao)ApplicationContextProvider.getBean("requestQueueHandlerDao");
						List<String> providers = requestQueueHandlerDao.getOutput_SrcProviders();
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
			  					break;
			  				}
			  			}
						logger.info("ESB provider fetched : " + providerESB);
						destQueue = requestQueueHandlerDao.getOutput_Dest_Queue(providerESB);
						logger.info("Destination Queue is : " + destQueue);
						new ReportRPTClient().call(destQueue, message);
						if(rcvdObj instanceof NgphCanonical)
						{
							EventLogger.logEvent("NGPHRSPHND0004", canObjct, ResponseQueueHandlerServiceImpl.class, null);//Response handler successfully delivered the message to the destination QPH queue.
						}
					}
				}
			}
			else
			{
				logger.error("Null Message Received by ResponseQueueHandlerServiceImpl");
			}	
		}
		catch (Exception e) 
		{
			logger.error("Exception occured while response handler tried to transmit the message to the destination system");
			logger.error(e, e);
			EventLogger.logEvent("NGPHRSPHND0005", null, ResponseQueueHandlerServiceImpl.class, null);//Exception occured when response handler tried to transmit the message to the destination system. Refer error log for details.
			throw new Exception(e);
		}
	}
}
