package com.logica.ngph.esb.servicesImpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.schedule.ScheduledEventListener;
import org.jboss.soa.esb.schedule.SchedulingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.TCPClient;
import com.logica.ngph.esb.Dtos.ResponseBean;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.ISO8583ChannelService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.utils.NGPHEsbUtils;


@Service
public class IMPSHandlerService implements ScheduledEventListener 
{
	static Logger logger = Logger.getLogger(IMPSHandlerService.class);
	public static int timeIntvl=0; 
	private EsbServiceDao esbServiceDao;
	private ISO8583ChannelService isoChannelService;
	private SwiftParserDao swiftParserDao;
	private NgphCanonical canInward;

	/**
	 * @param swiftParserDao the swiftParserDao to set
	 */
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) 
	{
		this.swiftParserDao = swiftParserDao;
	}
	/**
	 * @param isoChannelService the isoChannelService to set
	 */
	public void setIsoChannelService(ISO8583ChannelService isoChannelService) 
	{
		this.isoChannelService = isoChannelService;
	}
	/**
	 * @param esbServiceDao the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}

	//Initialize if certain Activity needs to be done
	public void initialize(ConfigTree arg0) throws ConfigurationException 
	{	
		try 
		{
			Thread.currentThread().sleep(60000);
		} 
		catch (InterruptedException e) 
		{
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
	}
	
	//Un Initialize certain things to clear the memory, This will perform task as soon as the Server is down
	public void uninitialize() 
	{
		try
		{
		// when the Server is Down, set the EI Status for EI Code in TCP Client to 0
		swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
		swiftParserDao.updateEiStatus(TCPClient.providerEsb,0);
		
		// Check Whether Global Maps are not Empty or null
		if(NGPHEsbUtils.msgIdResObjMap!= null && !NGPHEsbUtils.msgIdResObjMap.isEmpty() && NGPHEsbUtils.stanMsgMap!=null && !NGPHEsbUtils.stanMsgMap.isEmpty())
		{
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			for (Map.Entry<String, String> stanEntry : NGPHEsbUtils.stanMsgMap.entrySet()) 
			{
				String stanKey = stanEntry.getKey(); // Stan
				String msgId = stanEntry.getValue(); // msgId
				if(msgId!=null && StringUtils.isNotBlank(msgId)&& StringUtils.isNotEmpty(msgId))
				{
					ResponseBean rsObj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
					if(rsObj!=null)
					{
						esbServiceDao.populateIMPSData(rsObj, msgId, rsObj.getCanonicalObj().getClrgSysReference());
					}
					else
					{
						logger.warn("Response Bean Object was Null for MsgRef : " + msgId);
					}
				}
				else
				{
					logger.warn("MsgRef was Null for Stan Val : " + stanKey);
				}
			}
		}
		else
		{
			logger.info("Global Maps were Empty, There was no Data to Insert in DB");
		}
	}
	catch (Exception e) {
		logger.error(e, e);
	}
	}
	//main OnGoing Method that will be executed by the Esb Handler as a Scheduler
 @Transactional(propagation = Propagation.REQUIRED,  rollbackFor={Exception.class,Throwable.class,RuntimeException.class})
	public void onSchedule() throws SchedulingException 
	{
		//logger.info("IMPS Handler Service Starts");
		ResponseBean obj = null;
		try
		{
			isoChannelService = (ISO8583ChannelService)ApplicationContextProvider.getBean("impsChannelService");
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			//Check whether Global Map has atleast 1 Object
			if(!NGPHEsbUtils.msgIdResObjMap.isEmpty())
			{
				String msgDir = null;
				ArrayList<String> keysToRemove= new ArrayList<String>();
				ArrayList<ResponseBean> objsToAdd = new ArrayList<ResponseBean>();
				//Fetch the value from Global Map
				for (Map.Entry<String, ResponseBean> entry : NGPHEsbUtils.msgIdResObjMap.entrySet()) 
				{
					String key = entry.getKey();
					if(key!=null && StringUtils.isNotBlank(key) && StringUtils.isNotEmpty(key))
					{
						obj = entry.getValue();
						logger.info("Key = " +key  + ", Value = " + obj);
					}
					if (obj!=null)
					{
						canInward = obj.getCanonicalObj();
						msgDir = canInward.getMsgDirection();
					}
					//check if Response code is not null
					if(obj!=null && obj.getResCode()!=null && StringUtils.isNotBlank(obj.getResCode()) && StringUtils.isNotEmpty(obj.getResCode()))
					{
						logger.info("Key = " +key  + ", Response Code = " + obj.getResCode());
						String tempStan = canInward.getClrgSysReference();
						//tempStan = tempStan.substring(tempStan.length()-6, tempStan.length());
						//check if response code is positive or it is a response sent by QNG
						if(obj.getResCode().equalsIgnoreCase("00") || obj.getCanonicalObj().getDstMsgSubType().equalsIgnoreCase("10") || obj.getCanonicalObj().getDstMsgSubType().equalsIgnoreCase("30"))
						{
							if(key!=null && StringUtils.isNotBlank(key) && StringUtils.isNotEmpty(key))
							{
								esbServiceDao.populateIMPSData(obj, obj.getCanonicalObj().getMsgRef(), tempStan);
								keysToRemove.add(key);
							}
						}
						//check if response code is T0 or 91
						else if(obj.getResCode().equalsIgnoreCase("T0") || obj.getResCode().equalsIgnoreCase("91"))
						{
							//If already 3 verification requests has been sent then remove the message from global map to stop further retrying.
							int allowedVerCount = Integer.parseInt(esbServiceDao.getInitialisedValue("VERIFYCOUNT"));
							if (obj.getVerSendCt() >= allowedVerCount || msgDir.equalsIgnoreCase(NgphEsbConstants.INWARD_PAYMENT) || obj.getCanonicalObj().getDstMsgType().equalsIgnoreCase("08"))
							{
								esbServiceDao.populateIMPSData(obj, obj.getCanonicalObj().getMsgRef(), tempStan);
								EventLogger.logEvent("IMPSHNDSVC0001", obj.getCanonicalObj(), IMPSHandlerService.class, obj.getCanonicalObj().getMsgRef());//Verification attempt exhausted for the IMPS payment with QNG Ref {MsgRef}, Host Ref {TxnReference} and IMPS Ref {ClrgSysReference}
								//remove from Global Map
								keysToRemove.add(key);
							}
							else
							{
								if (msgDir.equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT) && esbServiceDao.getEIStatus(obj.getCanonicalObj().getDstEiId()) == 2 && !obj.getCanonicalObj().getDstMsgType().equalsIgnoreCase("08"))
								{
									//call SendVerification Function
									String message =null;
									message  = isoChannelService.createAndSendPaymentRequestOrResponse(obj.getCanonicalObj(), 2);
									logger.info("Message received in Verification Block: " + message);
									new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, message);
									
									if(StringUtils.isNotBlank(canInward.getClrgSysReference()) && StringUtils.isNotEmpty(canInward.getClrgSysReference()))
									{
										String msgId=null;
										//String oldStan =canInward.getClrgSysReference().substring(canInward.getClrgSysReference().length()-6, canInward.getClrgSysReference().length());
										String oldStan =canInward.getClrgSysReference();
										if(oldStan != null && StringUtils.isNotBlank(oldStan)&& StringUtils.isNotEmpty(oldStan))
										{
											msgId = NGPHEsbUtils.stanMsgMap.get(oldStan);
											if(msgId!=null && StringUtils.isNotBlank(msgId)&& StringUtils.isNotEmpty(msgId))
											{
												obj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
											}
											else
											{
												logger.info("MsgId is null for Flag value 2");
											}
										}
										else
										{
											logger.info("Empty Stan is Received for Flag value 2");
										}
										//delete old Stan Value from global cache and previous msgID
										if(NGPHEsbUtils.stanMsgMap.containsKey(oldStan))
										{
											keysToRemove.add(oldStan);
										}
										//create new Stan Value
										String newStan = isoChannelService.getStan(canInward.getMsgBranch());
										canInward.setSeqNo(newStan);
										//update Canonical Object
										String beforeStanVal = canInward.getClrgSysReference().substring(0, canInward.getClrgSysReference().length()-6);
										canInward.setClrgSysReference(beforeStanVal+newStan);
										// Update the Response Object
										int count = obj.getVerSendCt();
										count = count +1;
										obj.setVerSendCt(count);
										obj.setCanonicalObj(canInward);
										obj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
										objsToAdd.add(obj);
									}
								}
							}
						}
					}
					//check is response code is Blank or null
					else
					{
						//check reqTmStmp and curentTmpStmp
						if(obj!=null && obj.getReqTmStmp()!=null)
						{
							Timestamp reqTmStmp = obj.getReqTmStmp();
							Timestamp currTmStmp = new Timestamp(Calendar.getInstance().getTimeInMillis());
							
							//get the time interval from DB
							double timeIntvl = Integer.parseInt(esbServiceDao.getInitialisedValue("IMPSTIMEOUTINTV"));
							//get the time difference in seconds
							double diffSeconds = (currTmStmp.getTime () / 1000) - (reqTmStmp.getTime () / 1000);
							//compare the time diff with Db val
							if(diffSeconds>=timeIntvl && msgDir.equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
							{
								obj.setResCode("T0");
								EventLogger.logEvent("IMPSHNDSVC0002", obj.getCanonicalObj(), IMPSHandlerService.class, obj.getCanonicalObj().getMsgRef());//IMPS Payment request timed out for the IMPS payment with QNG Ref {MsgRef}, Host Ref {TxnReference} and IMPS Ref {ClrgSysReference}
								//A message had timed out this could be due to broken connection, so better close and re-connect. This assumption is made
								//since in all other cases there is always a response from NPCI server.
								if (TCPClient.requestSocket!=null)
								{
									if(TCPClient.providerEsb!=null)
									{
										swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
										swiftParserDao.updateEiStatus(TCPClient.providerEsb,0);
									}
									//Once the socket is closed then the read message in server thread will fire the connect again and again as long as connection 
									//is made available
									logger.info("A request timed out so close the socket");
									TCPClient.requestSocket.close();
									TCPClient.requestSocket = null;
									NGPHEsbUtils.lastCommTS = new Timestamp(Calendar.getInstance().getTimeInMillis());
								}
								//update the Global Map with new Object Value
								if(key!=null && StringUtils.isNotBlank(key) && StringUtils.isNotEmpty(key))
								{
									NGPHEsbUtils.msgIdResObjMap.put(key, obj);	
								}
							}
						}
					}
					//Check whether lastCommTS is less than ReqTimeStamp  
					if(obj!=null && obj.getReqTmStmp()!=null)
					{
						if(NGPHEsbUtils.lastCommTS.compareTo(obj.getReqTmStmp())<0)
						{
							logger.info("last comm ts is less than the request timestamp" + NGPHEsbUtils.lastCommTS.toString() + "   " + obj.getReqTmStmp().toString());
							NGPHEsbUtils.lastCommTS = obj.getReqTmStmp();
						}
					}
					//Check whether lastCommTS is less than ResTimeStamp  
					if(obj!=null&& obj.getResTmStmp()!=null)
					{
						if(NGPHEsbUtils.lastCommTS.compareTo(obj.getResTmStmp())<0)
						{
							logger.info("last comm ts is less than the response timestamp" + NGPHEsbUtils.lastCommTS.toString() + "   " + obj.getResTmStmp().toString());
							NGPHEsbUtils.lastCommTS = obj.getResTmStmp();
						}
					}
				}//For Loop
				for (int i=0;i<keysToRemove.size();i++)
				{
					//remove the STAN key
					NGPHEsbUtils.msgIdResObjMap.remove(keysToRemove.get(i));	
					//remove the Stan Map also
					NGPHEsbUtils.stanMsgMap.values().remove(keysToRemove.get(i));
				}
				for (int i=0;i<objsToAdd.size();i++)
				{
					//String stan =objsToAdd.get(i).getCanonicalObj().getClrgSysReference().substring(objsToAdd.get(i).getCanonicalObj().getClrgSysReference().length()-6, objsToAdd.get(i).getCanonicalObj().getClrgSysReference().length());
					String stan =objsToAdd.get(i).getCanonicalObj().getClrgSysReference();
					NGPHEsbUtils.populateStanMsgId(stan, objsToAdd.get(i).getCanonicalObj().getMsgRef());
					NGPHEsbUtils.populateResponseObject(objsToAdd.get(i).getCanonicalObj().getMsgRef(), objsToAdd.get(i));
				}
			}//If checking For Map Size
			else
			{
				NGPHEsbUtils.connectionStatusMap.put("ConnStatus", "Y");
			}
			// Check the case for Global Map (If Auto Response is false)
			if(!NGPHEsbUtils.CBSResponseMap.isEmpty())// If map is not Empty 
			{
				NgphCanonical CBSCanObj=null;
				ArrayList<String> CBSMapKeys = new ArrayList<String>();
				
				try
				{
					for (Map.Entry<String, ResponseBean> entry : NGPHEsbUtils.CBSResponseMap.entrySet()) 
					{
						String key = entry.getKey();
						if(key!=null && StringUtils.isNotBlank(key) && StringUtils.isNotEmpty(key))
						{
							obj = entry.getValue();
							logger.info("Key = " +key  + ", Value = " + obj);
						}
						if (obj!=null)
						{
							CBSCanObj = obj.getCanonicalObj();
						}
						//check reqTmStmp and curentTmpStmp
						if(obj!=null && obj.getReqTmStmp()!=null)
						{
							Timestamp reqTmStmp = obj.getReqTmStmp();
							Timestamp currTmStmp = new Timestamp(Calendar.getInstance().getTimeInMillis());
							
							//get the time interval from DB
							double timeIntvl = Integer.parseInt(esbServiceDao.getInitialisedValue("IMPSHOSTTIMEOUTINTV"));
							//get the time difference in seconds
							double diffSeconds = (currTmStmp.getTime () / 1000) - (reqTmStmp.getTime () / 1000);
							//compare the time diff with Db val
							if(diffSeconds>=timeIntvl)
							{
								//Set Message Error Code in Canonical
								CBSCanObj.setMsgErrorCode(NgphEsbConstants.NGPH_IHS0001); 
								CBSCanObj.setMsgPrevStatus(CBSCanObj.getMsgStatus());
								CBSCanObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
								obj.setResCode("T0");
								EventLogger.logEvent("IMPSHNDSVC0002", obj.getCanonicalObj(), IMPSHandlerService.class, obj.getCanonicalObj().getMsgRef());//IMPS Payment request timed out for the IMPS payment with QNG Ref {MsgRef}, Host Ref {TxnReference} and IMPS Ref {ClrgSysReference}
								//A message had timed out this could be due to broken connection, so better close and re-connect. This assumption is made
								//since in all other cases there is always a response from NPCI server.
								
								//update DB for new Status
								esbServiceDao.updateMessageStatusforCBS(CBSCanObj.getMsgRef(), CBSCanObj.getMsgStatus(), CBSCanObj.getMsgPrevStatus());
								//new ReportRPTAction().doProcess(CBSCanObj);
								new ReportQueue().QueueCall(CBSCanObj);
								// Add to keyArr so after processing the keys can be removed
								if(NGPHEsbUtils.CBSResponseMap.containsKey(key))
								{
									CBSMapKeys.add(key);
								}
							}
							
							String isoResponse = null;
							//Check if response from CBS is awaited from this object if yes continue to next object
							//The objects which has got some CBS response or error from QNG or timeout with CBS only should be responded to NPCI
							if (!(obj.getResCode().equalsIgnoreCase("AW")))
							{
								try 
								{	
									if (CBSCanObj.getMsgErrorCode() == null)
									{
										CBSCanObj.setMsgErrorCode("00");
									}
									else
									{
										CBSCanObj.setMsgErrorCode(CBSCanObj.getMsgErrorCode(),CBSCanObj.getDstMsgChnlType());
									}
									// Add to keyArr so after processing the keys can be removed
									if(NGPHEsbUtils.CBSResponseMap.containsKey(key))
									{
										CBSMapKeys.add(key);
									}
									isoResponse = isoChannelService.createAndSendPaymentRequestOrResponse(CBSCanObj, 1);
								}
								catch (NGPHException e) 
								{
									EventLogger.logEvent("IMPSHNDSVC0005", CBSCanObj, IMPSHandlerService.class, CBSCanObj.getMsgRef());//Error occured while sending response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
								if (isoResponse != null)
								{
									String isTestFlag = esbServiceDao.getInitialisedValue("ISSIMULATOR");
									if (isoResponse != null)
									{
										if(isTestFlag!=null && StringUtils.isNotBlank(isTestFlag) && StringUtils.isNotEmpty(isTestFlag)&& isTestFlag.equalsIgnoreCase("Y"))
										{
											//new TCPClient().doProcess(isoResponse);
										}
										
										EventLogger.logEvent("IMPSHNDSVC0006", CBSCanObj, IMPSHandlerService.class, CBSCanObj.getMsgRef());//Response for the IMPS payment {ClrgSysReference} sent
									}
								}
								else
								{
									logger.error("Message Construction returned Null, So no Processing");
								}
							}//End of if for awaiting CBS response status check
						}
					}//For Loop
					
					for (int i=0;i<CBSMapKeys.size();i++)
					{
						//remove the STAN key
						NGPHEsbUtils.CBSResponseMap.remove(CBSMapKeys.get(i));	
						//remove the Stan Map also
						NGPHEsbUtils.CBSResponseMap.values().remove(CBSMapKeys.get(i));
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
				
				
			}
			//get the Echo time interval from DB
			double echoTimeIntvl = Integer.parseInt(esbServiceDao.getInitialisedValue("IMPSTIMEECHOINTV"));
			double logonTimeIntvl = Integer.parseInt(esbServiceDao.getInitialisedValue("IMPSTIMELOGONINTV"));
			Timestamp currTmStmp = new Timestamp(Calendar.getInstance().getTimeInMillis());
			//get the time difference in minutes
			if (NGPHEsbUtils.lastCommTS != null)
			{
				double diffmins = (currTmStmp.getTime()/1000) - (NGPHEsbUtils.lastCommTS.getTime()/1000);
				//compare the time diff with Db val
				if(diffmins>=echoTimeIntvl)
				{
					//update lastCommTS with current TimeStamp
					NGPHEsbUtils.lastCommTS = currTmStmp;
					//createLogOnOrEcho of ISO Channel Service
					if (esbServiceDao.getEIStatus(ISO8583ChannelServiceImpl.impsServerEi) == 2)
					{
						logger.info("Difference time for echo interval " + diffmins + ". Echo message Sent");
						String echoMes = isoChannelService.createLogOnOrEcho(0, 1, null);
						//EventLogger.logEvent("IMPSHNDSVC0003", null, IMPSHandlerService.class, null);//Echo message sent.
						//new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, echoMes);
						String isTestFlag = esbServiceDao.getInitialisedValue("ISSIMULATOR");
						if(isTestFlag!=null && StringUtils.isNotBlank(isTestFlag) && StringUtils.isNotEmpty(isTestFlag)&& isTestFlag.equalsIgnoreCase("Y"))
						{
							//new TCPClient().doProcess(echoMes);
						}
						else
						{
							//new ISOClient().doProcess(echoMes);
						}
						//update lastCommTS with current TimeStamp
						NGPHEsbUtils.lastNPCIECHOTS = currTmStmp;
					}
				}
			}
			double diffLogOnmins = 0.00;
			if (NGPHEsbUtils.lastNPCILogOnTS != null)
			{
				diffLogOnmins = (currTmStmp.getTime()/1000) - (NGPHEsbUtils.lastNPCILogOnTS.getTime()/1000);
				if(diffLogOnmins>=logonTimeIntvl)
				{
					//createLogOnOrEcho of ISO Channel Service
					if (esbServiceDao.getEIStatus(ISO8583ChannelServiceImpl.impsServerEi) == 2)
					{
						logger.info("Difference time for logon interval " + diffLogOnmins + ". Logon message Sent");
						String logonMes = isoChannelService.createLogOnOrEcho(0, 0, null);
						//new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, logonMes);
						String isTestFlag = esbServiceDao.getInitialisedValue("ISSIMULATOR");
						if(isTestFlag!=null && StringUtils.isNotBlank(isTestFlag) && StringUtils.isNotEmpty(isTestFlag)&& isTestFlag.equalsIgnoreCase("Y"))
						{
							//new TCPClient().doProcess(logonMes);
						}
						else
						{
							//new ISOClient().doProcess(logonMes);
						}
						//update lastCommTS with current TimeStamp
						NGPHEsbUtils.lastNPCILogOnTS = currTmStmp;
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in IMPS scheduler");
			EventLogger.logEvent("IMPSHNDSVC0004", null, IMPSHandlerService.class, null);//Exception occurred in IMPS handler service. Refer error log for details.
			logger.error(e, e);
		}			
		//logger.info("IMPS Handler Service Ends Here");	
	}
}
