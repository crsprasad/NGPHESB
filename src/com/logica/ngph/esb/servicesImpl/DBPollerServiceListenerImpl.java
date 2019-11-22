package com.logica.ngph.esb.servicesImpl;

import java.sql.Clob;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.schedule.ScheduledEventListener;
import org.jboss.soa.esb.schedule.SchedulingException;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.common.utils.PropertyReader;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.Dtos.DbPoller;
import com.logica.ngph.esb.Dtos.Raw_Msgs;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.InfoCanonicalService;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.esb.services.ServiceController;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;

@Service
public class DBPollerServiceListenerImpl implements ScheduledEventListener {

	static Logger logger = Logger.getLogger(DBPollerServiceListenerImpl.class);
	private final String propName = "System.properties";
	
	
	private SwiftParserDao swiftParserDao;
	private ServiceController serviceController;
	private EsbServiceDao esbParserDao;
	
	
	
	/**
	 * @return the esbParserDao
	 */
	public EsbServiceDao getEsbParserDao() {
		return esbParserDao;
	}

	/**
	 * @param esbParserDao the esbParserDao to set
	 */
	public void setEsbParserDao(EsbServiceDao esbParserDao) {
		this.esbParserDao = esbParserDao;
	}

	public void setServiceController(ServiceController serviceController) {
		this.serviceController = serviceController;
	}

	public void setSwiftParserDao(SwiftParserDao swiftParserDao) 
	{
		this.swiftParserDao = swiftParserDao;
	}
	
	public void initialize(ConfigTree arg0) throws ConfigurationException {	}
	public void uninitialize() {}

	/*public static void main(String[] args) {
		ApplicationContextProvider.initializeContextProvider();
		try
		{
			new DBPollerServiceListenerImpl().onSchedule();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	@Transactional(propagation = Propagation.REQUIRED,  rollbackFor={Exception.class,Throwable.class,RuntimeException.class})
	public void onSchedule() throws SchedulingException 
	{
		logger.info("start onSchedule method in DBPollerServiceListenerImpl");
		try
		{
			//logger.info("Poller Service Started");
			swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
			serviceController =(ServiceController)ApplicationContextProvider.getBean("serviceController");
			esbParserDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			
			
			
			//Fetch All Values from Database whose status is P
			List<DbPoller> msgList  = swiftParserDao.fetchPolledMsgs("P");
			if(msgList !=null && !msgList.isEmpty())
			{
				for (int i=0;i<msgList.size();i++)
				{
					DbPoller pollerObj = msgList.get(i);
					String msgRef = pollerObj.getMsgRef();
					
//					int noofProcIterations = esbParserDao.getNoofProcIteration(msgRef);
					int maxNoofProcIteration = Integer.parseInt(swiftParserDao.getNoofProcIteration());
					
					/*logger.info("Maximum Noof Process Interation from INITIALISATIONM TABLE is :: "+maxNoofProcIteration);
					logger.info("Noof Process Interation from TA_MESSAGES_TX TABLE  is :: "+noofProcIterations);*/
					/*if(noofProcIterations < maxNoofProcIteration )
					{*/
							NgphCanonical pollerCanonical = swiftParserDao.getCanonicalFromMessagesTxforMsgRef(msgRef);
							InfoCanonical infoCanonical = swiftParserDao.getInfoCanonicalFromMessageTxforMsgRef(msgRef);
							if(pollerCanonical!=null)
							{
								logger.info("Message Currency value after construct Canonocal object from Data Base Polled "+pollerCanonical.getMsgCurrency()+ "LC number is ::"+pollerCanonical.getLcNo());
								serviceController.performPaymentProcessing(pollerCanonical);
								
							}
							else if(infoCanonical!=null)
							{
								InfoCanonicalService infoCanonicalService = (InfoCanonicalService)ApplicationContextProvider.getBean("infoCanonicalService");
								infoCanonical = infoCanonicalService.enrichInfoCanonical(infoCanonical);
			                	//Calling channel Service based on Channel
			                	String baseFormat = PropertyReader.getMapValue(infoCanonical.getDstChnl());
			    				logger.info("Base Format received for Info Canonical : " + baseFormat);
			    				if("SWIFT".equalsIgnoreCase(baseFormat))
			    				{
			    					logger.info("Construct Swift Message for InfoCanonical");
			    				}
			    				else if("SFMS".equalsIgnoreCase(baseFormat))
			    				{
			
			    					SFMSChannelService sfmsChannelService = (SFMSChannelService) ApplicationContextProvider.getBean("sfmsChannelService");
			    					String infoCanMes = sfmsChannelService.buildRtgsMessageForInfoCan(infoCanonical);
			    					logger.info("infoCanMes Message : " + infoCanMes);			
			    					//populating MESSAGEMASTER_T Table for Information message
			    					if(infoCanonical.getDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
			    					{
			    						String infoMesWithoutNineBlock = infoCanMes.substring(0, infoCanMes.indexOf("{999:"));
			    						logger.info("Seq No is ::"+infoCanonical.getSeqNo());
			    						logger.info("infoMesWithoutNineBlock is :: "+infoMesWithoutNineBlock);
			        					esbParserDao.populateMesMaster_T(infoCanonical.getSeqNo(), infoMesWithoutNineBlock, null);
			    					}
			    					else
			    					{
			    						infoCanonical.setMessage_info(infoCanMes);
			    						new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, infoCanonical);
			    					}
			    						infoCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_ACK_O));// Awaiting Acknowledge
				                	try{
				                		// Insert the infoCanonical in DB
				                    	esbParserDao.updatedInfoMessagestatus(infoCanonical.getMsgRef(), infoCanonical.getMsgStatus());
				                    	esbParserDao.updatepollStatus(infoCanonical.getMsgRef(), "C");
				                    	//new ReportQueue().QueueCall(infoCanonical);
				                	}catch(Exception e){
				                		logger.error("Exception Occured while inserting data in Transaction table, Hence move to error messages");
				                	}
			    				}
							}
							else
							{
								logger.info("Null Canonical Received for MsgRef " + msgRef + " Hence Starting Re Processing");
								List<Raw_Msgs> rawMsgsData = swiftParserDao.getRaw_msgs(msgRef);
								
								if(rawMsgsData!=null && !rawMsgsData.isEmpty())
								{
									Properties props = new Properties();
									props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
									String swiftPosition = props.getProperty("SWIFTPosition");
									String swiftCharacter = props.getProperty("SWIFTCharacter");
									String sfmsPosition = props.getProperty("SFMSPosition");
									String sfmsCharacter = props.getProperty("SFMSCharacter");
									String isoPosition = props.getProperty("ISOPosition");
									String isoCharacter = props.getProperty("ISOCharacter");
									
									for(int j=0;j<rawMsgsData.size();j++)
									{
										Raw_Msgs obj = rawMsgsData.get(i);
										
										Clob objrawMsgs = obj.getRawMsgs();
										String message = objrawMsgs.getSubString(1, (int) objrawMsgs.length());
										logger.info("Raw Message read from Db : " + message);
										
										if(Character.toString(message.charAt(Integer.parseInt(swiftPosition))).equalsIgnoreCase(swiftCharacter))
										{
											EventLogger.logEvent("NGPHRHLSVC0002", null, DBPollerServiceListenerImpl.class,msgRef);//SWIFT message received, triggering SWIFT processing.
											new ReportRPTClient().call(NgphEsbConstants.SWIFTQ, message);
										}
										else if(Character.toString(message.charAt(Integer.parseInt(sfmsPosition))).equalsIgnoreCase(sfmsCharacter))
										{
											EventLogger.logEvent("NGPHRHLSVC0003", null, DBPollerServiceListenerImpl.class,msgRef);//SFMS message received, triggering SFMS processing.
											new ReportRPTClient().call(NgphEsbConstants.SFMSQ, message);
										}
										else if(Character.toString(message.charAt(Integer.parseInt(isoPosition))).equalsIgnoreCase(isoCharacter))
										{
											EventLogger.logEvent("NGPHRHLSVC0004", null, DBPollerServiceListenerImpl.class, msgRef);//ISO8583 message received, triggering ISO processing.
											new ReportRPTClient().call(NgphEsbConstants.ISO8583Q, message);
										}
										else	
										{
											EventLogger.logEvent("NGPHRHLSVC0005", null, DBPollerServiceListenerImpl.class, msgRef);//Un-recognised message received, No processing.
										}
									}
								}
								else
								{
									logger.warn("No Record Found in RawMsgs for msgRef : " + msgRef);
								}
								
							}
					//}
				/*	else
					{
						logger.info("Maximum noof Process Interations was completed.");
					}*/
				}
			}
		}
		catch(JobExecutionException je)
		{
			logger.error(je, je);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
		
		logger.info("End onSchedule method in DBPollerServiceListenerImpl");
	}
}
