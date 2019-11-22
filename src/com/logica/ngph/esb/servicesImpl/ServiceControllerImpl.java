package com.logica.ngph.esb.servicesImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.dtos.NgphGrpCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.RequestQueueHandlerDao;
import com.logica.ngph.esb.services.AutoRouterService;
import com.logica.ngph.esb.services.EnrichService;
import com.logica.ngph.esb.services.EntityControlService;
import com.logica.ngph.esb.services.ForeignExchangeService;
import com.logica.ngph.esb.services.ISO8583ChannelService;
import com.logica.ngph.esb.services.InterveneService;
import com.logica.ngph.esb.services.LCBgHandlerService;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.esb.services.ServiceController;
import com.logica.ngph.esb.services.SwiftChannelService;
import com.logica.ngph.esb.services.ValidateService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.CertificateVerifier;
import com.logica.ngph.utils.EventLogger;

/**
 * 
 * @author mohdabdulaa
 *  This controller reads message configuration from
 *  database/cache based on messageTye calls different services
 * 
 */
@Service
public class ServiceControllerImpl implements ServiceController 
{
	static Logger logger = Logger.getLogger(ServiceControllerImpl.class);	
	private final String propName = "System.properties";
	private String baseFormat;
	boolean IS_ERROR_TO_BREAK_LOOP = false;
	private EsbServiceDao esbServiceDao;
	private AutoRouterService autoRouterService;
	private EntityControlService entityControlService;
	private EnrichService enrichService;
	private ValidateService validateService;
	private SwiftChannelService swiftChannelService;
	private ForeignExchangeService foreignExchangeService;
	private InterveneService interveneService;
	private SFMSChannelService rtgsChannelService;
	private ISO8583ChannelService isoChannelService;
	private LCBgHandlerService lcbgHandler;
	
	/**
	 * @param lcbgHandler the lcbgHandler to set
	 */
	public void setLcbgHandler(LCBgHandlerService lcbgHandler) {
		this.lcbgHandler = lcbgHandler;
	}

	/**
	 * @param impsChannelService the impsChannelService to set
	 */
	public void setImpsChannelService(ISO8583ChannelService isoChannelService) 
	{
		this.isoChannelService = isoChannelService;
	}

	/**
	 * @param rtgsChannelService the rtgsChannelService to set
	 */
	public void setSfmsChannelService(SFMSChannelService rtgsChannelService) 
	{
		this.rtgsChannelService = rtgsChannelService;
	}

	/**
	 * @param interveneService the interveneService to set
	 */
	public void setInterveneService(InterveneService interveneService) 
	{
		this.interveneService = interveneService;
	}

	/**
	 * 
	 * @param foreignExchangeService
	 */
	public void setForeignExchangeService(ForeignExchangeService foreignExchangeService) 
	{
		this.foreignExchangeService = foreignExchangeService;
	}

	/**
	 * 
	 * @param swiftChannelService
	 */
	public void setSwiftChannelService(SwiftChannelService swiftChannelService) 
	{
		this.swiftChannelService = swiftChannelService;
	}

	/**
	 * 
	 * @param validateService
	 */
	public void setValidateService(ValidateService validateService) 
	{
		this.validateService = validateService;
	}

	/**
	 * 
	 * @param enrichService
	 */
	public void setEnrichService(EnrichService enrichService) 
	{
		this.enrichService = enrichService;
	}

	/**
	 * 
	 * @param entityControlService
	 */
	public void setEntityControlService(EntityControlService entityControlService) 
	{
		this.entityControlService = entityControlService;
	}

	/**
	 * @param autoRouterService
	 *            the autoRouterService to set
	 */
	public void setAutoRouterService(AutoRouterService autoRouterService) 
	{
		this.autoRouterService = autoRouterService;
	}

	/**
	 * @param esbServiceDao
	 *            the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	
	/**
	 * This Controller reads message-configuration from database/cache based on
	 * messageTye. Then calls different services based on message-configuration
	 */
@Transactional (propagation = Propagation.REQUIRED, readOnly=false, rollbackFor={Exception.class,RuntimeException.class,Throwable.class})
	public void performPaymentProcessing(NgphCanonical canonicalData) throws Exception
	{
		logger.info("performPaymentProcessing() Start....");
		// checking the messageTye and retrieving the message-configuration
		if (canonicalData != null)
		{
			IS_ERROR_TO_BREAK_LOOP = false;
			canonicalData.setMsgErrorCode(null);
			//Check Whether Service Id is null or not. If null, its a fresh payment , if not null then called by Scheduler
			if(canonicalData.getServiceID()==null)
			{
				try 
				{
					logger.info("Msg Currency before performEntityControllerService in serviceControllerImpl :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo());
					performEntityControllerService(canonicalData, "9001");
					logger.info("Msg Currency After performEntityControllerService in serviceControllerImpl :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo());
					publishReportingData(canonicalData);
				} 
				catch (Exception e) 
				{
					logger.error("Exception occurred while executing Entity Controller service");
					logger.error(e, e);
					EventLogger.logEvent("NGPHSVCCNT0001", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Exception occured when executing entity controller service. Refer error log for details.
					throw new Exception(e);
				}
			}
			if(canonicalData.getServiceID().equalsIgnoreCase("9001") && !IS_ERROR_TO_BREAK_LOOP)
			{
				try 
				{
					logger.info("Msg Currency before performAutoRouterService in serviceControllerImpl :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo());
					performAutoRouterService(canonicalData, "9002");
					logger.info("Msg Currency After performAutoRouterService in serviceControllerImpl :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo());
					publishReportingData(canonicalData);
				} 
				catch (Exception e) 
				{
					logger.error("Exception occurred while executing Router service");
					logger.error(e, e);
					EventLogger.logEvent("NGPHSVCCNT0002", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Exception occured when executing Router service. Refer error log for details.
					throw new Exception(e);
				}
			}
			
			if(StringUtils.isNotEmpty(canonicalData.getDstMsgType())&& StringUtils.isNotEmpty(canonicalData.getDstMsgSubType()) && !IS_ERROR_TO_BREAK_LOOP) 
			{
				try
				{
					// get the message-config for this messageType
					List<String> serviceConfigs = getMessageConfig(canonicalData.getDstMsgType(),canonicalData.getDstMsgSubType(),canonicalData.getMsgDirection(),canonicalData.getServiceID());
		            logger.info("retrieved serviceConfigs....");
					logger.info("DstMsgType() : " + canonicalData.getDstMsgType() + "\t" + "DstMsgSubType() : " + canonicalData.getDstMsgSubType() + "\t" + "MsgDirection()" + canonicalData.getMsgDirection() + "\t" +"ServiceID() : " + canonicalData.getServiceID());
					logger.info("Service Controller config : " + serviceConfigs);
					if (serviceConfigs != null && serviceConfigs.size() > 0)
					{
						for (String serviceConfig : serviceConfigs) 
						{
							int serviceId = Integer.parseInt(serviceConfig);
							switch (serviceId) 
							{
								case 9003: 
									performValidateService(null, canonicalData, serviceConfig);
									break;
								case 9004: 
									performInterveneService(canonicalData, serviceConfig);
									break;
								case 9005:
									checkWarehouse();
									break;
								case 9006: 
									checkFx(canonicalData, serviceConfig);
									break;
								case 9007:
									performAccounting();
									break;
								case 9008: 
									performEnrichService(canonicalData, serviceConfig);
									break;
								case 9009:
									performCutOffControll();
									break;
								case 9010:
									performMandateControl();
									break;
								case 9011:
									performReturns();
									break;
								case 9012:
									performConfirmations();
									break;
								case 9013:
									checkLmLinks();
									break;
								case 9014:
									checkAmlLinks();
									break;
								case 9015:
									checkBillingLinks();
									break;
								case 9016:
									performAuthentication();
									break;
								case 9017:
									performBulkActivity();
									break;
								case 9018:
									performAuthorisation();
									break;
								case 9019:
									generateReference();
									break;
								case 9020:
									performAuditing();
									break;
								case 9021:
									performlcBgService(canonicalData, serviceConfig);
									break;
								default:
									break;
							}
							//publishing the data for reporting
							publishReportingData(canonicalData);
							if (IS_ERROR_TO_BREAK_LOOP)
							{
								break;
							}
						}
					}
					//if all services performed successfully then
					if (!IS_ERROR_TO_BREAK_LOOP)
					{
						int isMsgConstReq = esbServiceDao.getEIid_MsgConstReqd(canonicalData.getDstEiId());
						logger.info("Message Construction required for : " + canonicalData.getDstEiId() + " is " + isMsgConstReq);
						
						if(isMsgConstReq==1)
						{
							//loading property file in memory
							Properties props = new Properties();
							try 
							{
								props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
							} 
							catch (IOException e) 
							{
								logger.error(e, e);
								throw new Exception(e);
							}
							catch (Exception e) {
								logger.error(e, e);
								throw new Exception(e);
							}
	
							logger.info("Destination Channel Type : " + canonicalData.getDstMsgChnlType());
							if(canonicalData.getDstMsgChnlType()!=null && StringUtils.isNotBlank(canonicalData.getDstMsgChnlType()))
							{
								baseFormat = props.getProperty(canonicalData.getDstMsgChnlType());
							}
							logger.info("Base Format : " + baseFormat);
							
							if("SWIFT".equalsIgnoreCase(baseFormat))
							{
								performSwiftChannelService(canonicalData);
							}
							else if("SFMS".equalsIgnoreCase(baseFormat))
							{
								performSFMSChannelService(canonicalData);
							}
							else if("ISO8583".equalsIgnoreCase(baseFormat))
							{
								performISO8583ChannelService(canonicalData);
							}
							else
							{
								logger.info("Invalid Channel");
							}
					}
					else
					{
						logger.info("No Need to Construct the message for Destination EIID" + canonicalData.getDstEiId());
					}
					}
					else
					{
						EventLogger.logEvent("NGPHSVCCNT0003", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Error occured during payments processing. Refer previous events.
					}
					//publishing the new data for reporting
					publishReportingData(canonicalData);
					//update status for Poller
					esbServiceDao.updatepollStatus(canonicalData.getMsgRef(), "C");
				}
				catch(Exception e)
				{
					logger.error("Exception occured in payments processing in Service Controller");
					logger.error(e, e);
					EventLogger.logEvent("NGPHSVCCNT0004", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Exception occured in payments processing. Refer error log for details.
					canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.EXCEPTION_O));
					try
					{
						esbServiceDao.updatePaymentDetails(canonicalData);
						publishReportingData(canonicalData);
					}
					catch(Exception ex)
					{
						logger.error("Exception occured in updating the exception status for the payment that encountered an exception in processing in Service Controller");
						logger.error(e, e);
						EventLogger.logEvent("NGPHSVCCNT0005", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Exception occured in setting exception status for payment that encountered exception in processing. Refer error log for details.
						throw new Exception(e);
					}
					throw new Exception(e);
				}
			}
		}
		logger.info("performPaymentProcessing() End...."+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date()));
		EventLogger.logEvent("NGPHSVCCNT0006", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Payments processing completed on canonical. 
	}
	
	public void performlcBgService(NgphCanonical canonicalData, String serviceId)throws Exception 
	{
		logger.info("performLcBgService Start....");
		lcbgHandler.doProcess(canonicalData);
		if (StringUtils.isNotEmpty(canonicalData.getMsgErrorCode())) 
		{
			IS_ERROR_TO_BREAK_LOOP = true;
			
			//get noof process interaions from DB
		//	int  noofProcIterations = esbServiceDao.getNoofProcIteration(canonicalData.getMsgRef());
			
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				EventLogger.logEvent("NGPHSVCCNT0021", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound Payment with Host Reference {TxnReference} moved to repair queue due to Enrich service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
				}	
			}
			else
			{
				EventLogger.logEvent("NGPHSVCCNT0022", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound Payment with Host Reference {TxnReference} moved to repair queue due to Enrich service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_O));
				}
			}
            esbServiceDao.updatePaymentDetails(canonicalData);
		}
		else
		{
			canonicalData.setServiceID(serviceId);
		}
		logger.info("performlcBgService End....");
	}
	private void performAuditing() 
	{
		logger.info("performAuditing");
	}

	private void generateReference() 
	{
		logger.info("generateReference");
	}
	
	private void performAuthorisation() 
	{
		logger.info("performAuthorisation");
	}

	private void performBulkActivity() 
	{
		logger.info("performBulkActivity");
	}

	private void performAuthentication() 
	{
		logger.info("performAuthentication");
	}

	private void performConfirmations() 
	{
		logger.info("performConfirmations");
	}

	private void performReturns() 
	{
		logger.info("performReturns");
	}

	private void performMandateControl() 
	{
		logger.info("performMandateControl");
	}

	private void performCutOffControll() 
	{
		logger.info("performCutOffControll");
	}

	public void performValidateService(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData, String serviceId) throws Exception
	{
		logger.info("performValidateService() Start....");
		validateService.validateMessage(ngphGrpCanonical, canonicalData);	
		if (StringUtils.isNotEmpty(canonicalData.getMsgErrorCode())) 
		{
			IS_ERROR_TO_BREAK_LOOP = true;
			
			//get noof process interaions from DB
			//int  noofProcIterations = esbServiceDao.getNoofProcIteration(canonicalData.getMsgRef());
			//logger.info("noofProcIterations is "+noofProcIterations);
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				EventLogger.logEvent("NGPHSVCCNT0007", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//InboundPayment with Host Reference {TxnReference} moved to repair queue due to validation service error {MsgErrorCode}.	
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
					
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
				}	
			}
			else
			{
				EventLogger.logEvent("NGPHSVCCNT0008", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound Payment with Host Reference {TxnReference} moved to repair queue due to validation service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);
					logger.info("noofProcIterations in performValidateService method is "+noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_O));
				}
			}
			logger.error("performValidateService() ValidationError....");
			esbServiceDao.updatePaymentDetails(canonicalData);
		}
		else
		{
			canonicalData.setServiceID(serviceId);
		}
		logger.info("performValidateService() End....");
	}
	
	/**
	 * 
	 * @param canonicalData
	 * @throws NGPHException
	 */
	public void performISO8583ChannelService(NgphCanonical canonicalData)throws Exception 
	{
		logger.info("performIMPSChannelService() Start....");
		/*
		 * This method takes a int Argument which tells that we have to construct a message
		 *  as a Request Message or Response Message.
		 *  Request -> 0(Numeric Zero)
		 *  Response -> 1(Numeric One)
		 *  Verification Request->2
		 *  Verification Response->3
		 *  So here Calling as a request message construction
		 */
		String isoMessage = isoChannelService.createAndSendPaymentRequestOrResponse(canonicalData,0);
		logger.info("ISO Channel Services builds this message : " + isoMessage);
		try
		{
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			if(StringUtils.isEmpty(isoMessage))
			{
				logger.info("Setting message status as Exception....");
				EventLogger.logEvent("NGPHSVCCNT0009", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//ISO8583 Payment moved to Exception queue since the constructed ISO8583 message is blank.
				canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.EXCEPTION_O));
			}
			else
			{
				logger.info("sending a message to ISOChaneelServiceQueue....");
				new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, isoMessage);
				logger.info("Setting message status as Awaiting Acknowledgement....");
				if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
				{
					EventLogger.logEvent("NGPHSVCCNT0010", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound ISO8583 Payment successfully sent to host {DstEiId}.
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.SENT_TO_HOST_I));
				}
				else
				{
					EventLogger.logEvent("NGPHSVCCNT0011", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound ISO8583 Payment successfully sent to payment channel {DstEiId}.
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_ACK_O));
				}
			}
			esbServiceDao.updatePaymentDetails(canonicalData);
			if (NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				esbServiceDao.updateAvailableCrLimit(canonicalData.getMsgBranch() + canonicalData.getBeneficiaryCustAcct(), canonicalData.getMsgAmount());
				esbServiceDao.updateAvailableCrLimit(canonicalData.getSenderBank().substring(0, 4), canonicalData.getMsgAmount());
			}
			logger.info("Updated message status....");
		}
		catch (Exception e) 
		{
			logger.error("Exception occurred in performISO8583ChannelService of service controller");
			logger.error(e, e);
			EventLogger.logEvent("NGPHSVCCNT0012", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Exception occurred while creating and sending ISO8583 Payment with QNG reference {MsgRef} to {DstEiId}. Refer error log for details.
			throw new Exception(e);
		}
		logger.info("performIMPSChannelService() End....");
	}

	/**
	 * 
	 * @param canonicalData
	 * @throws NGPHException
	 */
	public void performSFMSChannelService(NgphCanonical canonicalData)throws Exception 
	{
		logger.info("performRtgsChannelService() Start....");

		String tempSndrBnk = canonicalData.getSenderBank();
		if(esbServiceDao.getInitialisedValue("REPLACECTRLIFSC").equalsIgnoreCase("Y") && canonicalData.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
		{
			canonicalData.setSenderBank(esbServiceDao.getInitialisedValue("LOCALIFSC"));
		}
		String sfmsSeqNo = null;
		//Sequence generation is only for outbound SFMS messages
		if (canonicalData.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT) && canonicalData.getMsgHost().equalsIgnoreCase("9999"))
		{
			if (canonicalData.getSeqNo() == null || StringUtils.isEmpty(canonicalData.getSeqNo()) || StringUtils.isBlank(canonicalData.getSeqNo()) || esbServiceDao.getInitialisedValue("GENOWNSFMSSEQ").equalsIgnoreCase("Y"))
			{
				sfmsSeqNo = esbServiceDao.getInitialisedValue(NgphEsbConstants.BANK_CODE)+esbServiceDao.getSequenceNumber("SFMSSEQ",5);
				canonicalData.setSeqNo(sfmsSeqNo);
			}
		}
		String rtgsMessage = rtgsChannelService.buildRtgsMessage(canonicalData);
		logger.info("RTGS Channel Services builds this message : " + rtgsMessage);
		
		canonicalData.setSenderBank(tempSndrBnk);
		
		String signedMessage = null;
		String rtgsMesWithoutNineBlock = rtgsMessage.substring(0, rtgsMessage.indexOf("{999:"));
		try
		{
			RequestQueueHandlerDao requestQueueHandlerDao = (RequestQueueHandlerDao) ApplicationContextProvider.getBean("requestQueueHandlerDao");
			String destQ = requestQueueHandlerDao.getOutPutQueueByEI(canonicalData.getDstEiId());
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			if(StringUtils.isEmpty(rtgsMessage))
			{
				logger.info("Setting message status as Exception....");
				EventLogger.logEvent("NGPHSVCCNT0013", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//SFMS Payment moved to Exception queue since the constructed message is blank.
				canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.EXCEPTION_O));
			}
			else
			{
					String isSignReq = esbServiceDao.getInitialisedValue("SIGNREQ");
					if(StringUtils.isNotBlank(isSignReq) && StringUtils.isNotEmpty(isSignReq) && isSignReq.equalsIgnoreCase("Y"))
					{
						logger.info("Adding Signature to the message");
						if (canonicalData.getMsgDirection().equalsIgnoreCase("O"))
						{
							signedMessage =  rtgsMessage  +"{UMAC:-----BEGIN PKCS7-----\r\n" + CertificateVerifier.validateCertificate(rtgsMesWithoutNineBlock) + "\r\n-----END PKCS7-----\r\n}";
							logger.info("sending a message to RtgsChaneelServiceQueue....");
							canonicalData.setMessage_so_far(signedMessage);
							new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, canonicalData);
						}
						else
						{
							canonicalData.setMessage_so_far(rtgsMessage);
							new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, canonicalData);
						}
					}
					//Signature Not Required
					else
					{
						if (canonicalData.getMsgDirection().equalsIgnoreCase("O"))
						{
							//populate table in DB with Seq num
							esbServiceDao.populateMesMaster_T(canonicalData.getSeqNo(), rtgsMesWithoutNineBlock, null);
						}
						else
						{
							canonicalData.setMessage_so_far(rtgsMessage);
							new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, canonicalData);

						}
					}
						logger.info("Setting message status as Awaiting Acknowledgement....");
						if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
							{
								EventLogger.logEvent("NGPHSVCCNT0014", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound SFMS Payment successfully sent to host {DstEiId}.
								canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.SENT_TO_HOST_I));
							}
						else
							{
								EventLogger.logEvent("NGPHSVCCNT0015", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound SFMS Payment successfully sent to payment channel {DstEiId}.
								canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_ACK_O));
							}
			}
				esbServiceDao.updatePaymentDetails(canonicalData);
			if (NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				esbServiceDao.updateAvailableCrLimit(canonicalData.getMsgBranch() + canonicalData.getBeneficiaryCustAcct(), canonicalData.getMsgAmount());
				esbServiceDao.updateAvailableCrLimit(canonicalData.getSenderBank().substring(0, 4), canonicalData.getMsgAmount());
			}
			logger.info("Updated message status....");
		}
		catch (Exception e) 
		{
			logger.error("Exception occurred in performSFMSChannelService of service controller");
			logger.error(e, e);
			EventLogger.logEvent("NGPHSVCCNT0016", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Exception occurred while creating and sending SFMS Payment to {DstEiId}. Refer error log for details.
			throw new Exception(e);
		}
		logger.info("performRtgsChannelService() End....");
	}

	/**
	 * 
	 */
	public void performSwiftChannelService(NgphCanonical canonicalData) throws Exception
	{
		logger.info("performSwiftChannelService() Start....");
		String swiftMessage = swiftChannelService.buildSwiftMessage(canonicalData);
		logger.info("Swift Channel Services builds this message : " + swiftMessage);
		try
		{
			RequestQueueHandlerDao requestQueueHandlerDao = (RequestQueueHandlerDao) ApplicationContextProvider.getBean("requestQueueHandlerDao");
			String destQ = requestQueueHandlerDao.getOutPutQueueByEI(canonicalData.getDstEiId());
			
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			if(StringUtils.isEmpty(swiftMessage))
			{
				logger.info("Setting message status as Exception....");
				EventLogger.logEvent("NGPHSVCCNT0017", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//SWIFT Payment moved to Exception queue since the constructed message is blank.
				canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.EXCEPTION_O));
			}
			else
			{
				logger.info("sending a message to SwiftChaneelServiceQueue....");
				canonicalData.setMessage_so_far(swiftMessage);
				new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, canonicalData);
				logger.info("Setting message status as Awaiting Acknowledgement....");
				if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
				{
					EventLogger.logEvent("NGPHSVCCNT0018", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound SWIFT Payment successfully sent to host {DstEiId}.
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.SENT_TO_HOST_I));
				}
				else
				{
					EventLogger.logEvent("NGPHSVCCNT0019", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound SWIFT Payment successfully sent to payment channel {DstEiId}.
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_ACK_O));
				}
			}
			esbServiceDao.updatePaymentDetails(canonicalData);
			if (NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				esbServiceDao.updateAvailableCrLimit(canonicalData.getMsgBranch() + canonicalData.getBeneficiaryCustAcct(), canonicalData.getMsgAmount());
				esbServiceDao.updateAvailableCrLimit(canonicalData.getSenderBank().substring(0, 4), canonicalData.getMsgAmount());
			}
			logger.info("Updated message status....");
		}
		catch (Exception e) 
		{
			logger.error("Exception occurred in performSFMSChannelService of service controller");
			logger.error(e, e);
			EventLogger.logEvent("NGPHSVCCNT0020", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Exception occurred while creating and sending SWIFT Payment to {DstEiId}. Refer error log for details.
			throw new Exception(e);
		}
		logger.info("performSwiftChannelService() End....");
	}
	
	/**
	 * 
	 * @param canonicalData
	 */
	private void publishReportingData(NgphCanonical canonicalData)throws Exception
	{
		try
		{
			logger.info("sending a message to ReportRPTQueue....");
			//publising the data to reporting queue
			new ReportQueue().QueueCall(canonicalData);
		}
		catch(Exception e)
		{
			logger.error(e, e);	
			throw new Exception(e);
		}
	}

	/**
	 * 
	 */
	private void checkBillingLinks() 
	{
		logger.info("checkBillingLinks");
	}

	/**
	 * 
	 */
	private void checkAmlLinks() 
	{
		logger.info("checkAmlLinks");
	}

	/**
	 * 
	 */
	private void checkLmLinks() 
	{
		logger.info("checkLmLinks");
	}

	/**
	 * 
	 */
	public void performEnrichService(NgphCanonical canonicalData, String serviceId)throws Exception 
	{
		logger.info("performEnrichService() Start....");
		
		// setting the trasnaction type as P2A as system (IMPS-->outward) receives NEFT/RTGS/SWIFT messages where it does not have a field to contain transaction type.
		// to be confirmed.
		if(canonicalData.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
		{
			canonicalData.setMsgTxnType(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ);
		}
		
		
		enrichService.performEnrichService(canonicalData);
		logger.info("Error Code :: is "+canonicalData.getMsgErrorCode());
		if (StringUtils.isNotEmpty(canonicalData.getMsgErrorCode())) 
		{
			IS_ERROR_TO_BREAK_LOOP = true;
			
			//get noof process interaions from DB
			//int  noofProcIterations = esbServiceDao.getNoofProcIteration(canonicalData.getMsgRef());
			
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				EventLogger.logEvent("NGPHSVCCNT0021", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound Payment with Host Reference {TxnReference} moved to repair queue due to Enrich service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
				}	
			}
			else
			{
				EventLogger.logEvent("NGPHSVCCNT0022", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound Payment with Host Reference {TxnReference} moved to repair queue due to Enrich service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_O));
				}
			}
			logger.error("performEnrichService() Error...");
            esbServiceDao.updatePaymentDetails(canonicalData);
		}
		else
		{
			canonicalData.setServiceID(serviceId);
		}
		logger.info("performEnrichService() End....");
	}
	
	/**
	 * 
	 */
	private void performAccounting() 
	{
		logger.info("performAccounting");
	}

	/**
	 * 
	 */
	public void checkFx(NgphCanonical canonicalData, String serviceId) throws Exception{
		logger.info("checkFx() Start....");	
		foreignExchangeService.performForeignExchange(canonicalData);
		if(logger.isDebugEnabled())
		{
			logger.debug("checkFx() CurrencyDr...."+canonicalData.getDrCurrency());
			logger.debug("checkFx() CurrencyCr...."+canonicalData.getCrCurrency());
			logger.debug("checkFx() BaseCcyAmt...."+canonicalData.getBaseCcyAmount());
			logger.debug("checkFx() msgCurrencyAmt...."+canonicalData.getMsgCurrencyAmount());
			logger.debug("checkFx() InstructedCcyAmt...."+canonicalData.getInstructedCcyAmount());
		}
		if (StringUtils.isNotEmpty(canonicalData.getMsgErrorCode())) 
		{
			//get noof process interaions from DB
			//int  noofProcIterations = esbServiceDao.getNoofProcIteration(canonicalData.getMsgRef());
			
			IS_ERROR_TO_BREAK_LOOP = true;
			//Sending to repair
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				EventLogger.logEvent("NGPHSVCCNT0023", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound Payment with Host Reference {TxnReference} moved to repair queue due to FX service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
				}	
			}
			else
			{
				EventLogger.logEvent("NGPHSVCCNT0024", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound Payment with Host Reference {TxnReference} moved to repair queue due to FX service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_O));
				}
			}
			logger.error("checkFx() Error....");
			esbServiceDao.updatePaymentDetails(canonicalData);
		}
		else
		{
			canonicalData.setServiceID(serviceId);
		}
		logger.info("checkFx() End....");
	}

	/**
	 * 
	 */
	private void checkWarehouse() 
	{
		logger.info("checkWarehouse");
	}

	/**
	 * 
	 */
	public void performInterveneService(NgphCanonical canonicalData, String serviceId)throws Exception 
	{
		logger.info("performInterveneService() Start....");
			interveneService.performRouting(canonicalData);
			if(StringUtils.isNotEmpty(canonicalData.getMsgErrorCode()))
			{
				logger.info("performInterveneService() ........MsgErrorCode:"+canonicalData.getMsgErrorCode());
				IS_ERROR_TO_BREAK_LOOP = true;
				EventLogger.logEvent("NGPHSVCCNT0025", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Payment moved to Exception queue due to Intervene service error {MsgErrorCode}.
				canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.EXCEPTION_O));
				//no need to update the payment details here as it is already updated in InterveneServiceImpl
			}
		logger.info("performInterveneService() End....");
	}

	/**
	 * 
	 */
	public void performAutoRouterService(NgphCanonical canonicalData, String serviceId) throws Exception
	{
		
		logger.info("performAutoRouterService() Start....");
		logger.info("performAutoRouterService() calling autoRouterService.performRouting()....");
		logger.info("Msg Currecy before performRouting() method :: "+canonicalData.getMsgCurrency());
		autoRouterService.performRouting(canonicalData);
		logger.info("Msg Currecy after performRouting() method :: "+canonicalData.getMsgCurrency());
		if (StringUtils.isNotEmpty(canonicalData.getMsgErrorCode()))
		{
			IS_ERROR_TO_BREAK_LOOP = true;
			canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
			
			//get noof process interaions from DB
			//int  noofProcIterations = esbServiceDao.getNoofProcIteration(canonicalData.getMsgRef());
			
			if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
			{
				EventLogger.logEvent("NGPHSVCCNT0026", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound Payment with Payment channel Reference {TxnReference} moved to repair queue due to Routing service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
				}	
			}
			else
			{
				EventLogger.logEvent("NGPHSVCCNT0027", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound Payment with Host Reference {TxnReference} moved to repair queue due to Routing service error {MsgErrorCode}.
				if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O));
					/*noofProcIterations = noofProcIterations+1;
					canonicalData.setNoofProcessIterations(noofProcIterations);*/
				}
				else
				{
					canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_O));
				}
			}
			logger.error("performAutoRouterService() Error...");
			esbServiceDao.updatePaymentDetails(canonicalData);
		}
		else
		{
			canonicalData.setServiceID(serviceId);
		}
		logger.info("performAutoRouterService() End....");
	}

	/**
	 *   
	 */
	public void performEntityControllerService(NgphCanonical canonicalData, String serviceId) throws Exception
	{
		logger.info("performEntityControllerService() Start....");
		logger.info("Msg Currency before populateBranchAndDepartmentDetails in performEntityControllerService() method :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo());
			entityControlService.populateBranchAndDepartmentDetails(canonicalData);
			logger.info("Msg Currency After populateBranchAndDepartmentDetails in performEntityControllerService() method :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo());
			logger.info("performEntityControllerService() BranchCode:" + canonicalData.getMsgBranch());
			logger.info("performEntityControllerService() DeptCode:" + canonicalData.getMsgDept());
			
			//get noof process interaions from DB
			//int  noofProcIterations = esbServiceDao.getNoofProcIteration(canonicalData.getMsgRef());
			
			if (StringUtils.isNotEmpty(canonicalData.getMsgErrorCode())) 
			{
				IS_ERROR_TO_BREAK_LOOP = true;
				canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
				if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
				{
					EventLogger.logEvent("NGPHSVCCNT0028", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Inbound Payment with Payment channel Reference {TxnReference} moved to repair queue due to Entity Control service error {MsgErrorCode}.
					if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
					{
						canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I));
						/*noofProcIterations = noofProcIterations+1;
						canonicalData.setNoofProcessIterations(noofProcIterations);*/
					}
					else
					{
						canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
					}
				}
				else
				{
					EventLogger.logEvent("NGPHSVCCNT0029", canonicalData, ServiceControllerImpl.class, canonicalData.getMsgRef());//Outbound Payment with Host Reference {TxnReference} moved to repair queue due to Entity Control service error {MsgErrorCode}.
					if (esbServiceDao.getHostRepairable(canonicalData.getMsgHost()).equalsIgnoreCase("Y"))
					{
						canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O));
						/*noofProcIterations = noofProcIterations+1;
						canonicalData.setNoofProcessIterations(noofProcIterations);*/
					}
					else
					{
						canonicalData.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_O));
					}
				}
				logger.error("performEntityControllerService() Error....");	
				esbServiceDao.updatePaymentDetails(canonicalData);
				logger.info("Msg Currency after updatePaymentDetails in  performEntityControllerService() method :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo()); 
			}
			else
			{
				canonicalData.setServiceID(serviceId);
			}
		logger.info("performEntityControllerService() End....");
	}

	/**
	 * 
	 * @param messageType
	 * @return ServiceConfigDetails
	 */
	private List<String> getMessageConfig(String messageType, String msgSubType, String messageDirection, String srvcId)throws Exception 
	{	
		logger.info("getMessageConfig()......");
		return esbServiceDao.getServiceConfigDetails(messageType, msgSubType, messageDirection,srvcId);
	}
}
