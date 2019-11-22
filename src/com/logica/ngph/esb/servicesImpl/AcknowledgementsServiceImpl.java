package com.logica.ngph.esb.servicesImpl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.Dtos.ReportDto;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.AcknowledgementsService;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.esb.services.SwiftChannelService;
import com.logica.ngph.utils.EventLogger;

/**
 * 
 * @author guptarb
 *
 */
public class AcknowledgementsServiceImpl implements AcknowledgementsService{
	
	static Logger logger = Logger.getLogger(AcknowledgementsServiceImpl.class);
	private final String propName = "System.properties";
	
	private EsbServiceDao esbServiceDao;
	private SwiftParserDao swiftParserDao;
	
	/**
	 * @param swiftParserDao the swiftParserDao to set
	 */
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) {
		this.swiftParserDao = swiftParserDao;
	}

	private SwiftChannelService swiftChannelService;
	private SFMSChannelService sfmsChannelService;
	/**
	 * @param esbServiceDao the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}

	/**
	 * @param swiftChannelService the swiftChannelService to set
	 */
	public void setSwiftChannelService(SwiftChannelService swiftChannelService) {
		this.swiftChannelService = swiftChannelService;
	}

	/**
	 * @param sfmsChannelService the sfmsChannelService to set
	 */
	public void setSfmsChannelService(SFMSChannelService sfmsChannelService) {
		this.sfmsChannelService = sfmsChannelService;
	}
	//This method will Update AckCanonical from Canonical Fetched from db
	public void processAcknowledgement(AcknowledgementCanonical ackCan, NgphCanonical canonical)throws Exception
	{
		try
		{
			String ackMes = null;
			
			List<String> reportList= null;
			//update Acknowledgment
			//For Ack both Sender and Receiver Bank will be our Db configured Val only
			ackCan.setSeqNo(canonical.getSeqNo());
			ackCan.setAckReceivedTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			ackCan.setDstChnlType(canonical.getMsgChnlType());
			ackCan.setDstEiId(canonical.getMsgHost());
			ackCan.setMsgBranch(canonical.getMsgBranch());
			ackCan.setMsgDept(canonical.getMsgDept());
			if(canonical.getMsgDirection()!=null && canonical.getMsgDirection().equalsIgnoreCase("I"))
			{
				ackCan.setMsgDirection("O");
			}
			else
			{
				ackCan.setMsgDirection("I");
			}
			
			ackCan.setMsgMur(canonical.getMsgMur());
			ackCan.setMsgOriginalId(canonical.getMsgRef());
			ackCan.setSrcChnlType(canonical.getDstMsgChnlType());
			ackCan.setSrcEiId(canonical.getDstEiId());
			
			logger.info("ackCan.getSrcMsgType() : " + ackCan.getSrcMsgType());
			logger.info("ackCan.getSrcSubMsgType() : " + ackCan.getSrcSubMsgType());
			logger.info("ackCan.getDstChnlType() : " + ackCan.getDstChnlType());
			logger.info("ackCan.getMsgDirection() : " + ackCan.getMsgDirection());
			logger.info("Original Message Destination EI-ID is"+canonical.getDstEiId());
			if (ackCan.getAckReasonCode() != null)
			{
				if(ackCan.getAckReasonCode().equalsIgnoreCase("PBAPI000000") || ackCan.getAckReasonCode().equalsIgnoreCase("00") || (ackCan.getAckType() != null && ackCan.getAckType().equalsIgnoreCase("Y")))
				{
					//set status as Completed
					canonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.COMPLETED_O));
					if(ackCan.getAckType() == null || StringUtils.isEmpty(ackCan.getAckType()) || StringUtils.isBlank(ackCan.getAckType()))
					{
						ackCan.setAckType("Y");
					}
				}
				else
				{
					//canonical.setRepairReason(ackCan.getAckReasonCode());
					canonical.setMsgErrorCode(ackCan.getAckReasonCode(), ackCan.getDstChnlType());
					//set status as Rejected By Channel
					canonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_BY_CHANNEL_O));
					if(ackCan.getAckType() == null || StringUtils.isEmpty(ackCan.getAckType()) || StringUtils.isBlank(ackCan.getAckType()))
					{
						ackCan.setAckType("N");
					}
				}
			}
			//fetch the dstMsgtype and DstSubMestype from DB based on configuration
			List<String> dstMsgInfo = esbServiceDao.getDstMsgtype(ackCan.getSrcMsgType(), ackCan.getSrcSubMsgType(),ackCan.getDstChnlType(),ackCan.getMsgDirection());
			if(dstMsgInfo!=null && dstMsgInfo.size()>1)
			{
				ackCan.setDstMsgType(dstMsgInfo.get(0));
				ackCan.setDstSubMsgType(dstMsgInfo.get(1));		
				//calling Channel Services
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
				catch (Exception e) 
				{
					logger.error(e, e);
					throw new Exception(e);
				}
				logger.info("Destination Channel type : " + ackCan.getDstChnlType());
				String baseFormat = props.getProperty(ackCan.getDstChnlType());
				logger.info("Base Format : " + baseFormat);
				if("SWIFT".equalsIgnoreCase(baseFormat))
				{
					if(ackCan.getSenderBank()==null || StringUtils.isEmpty(ackCan.getSenderBank()) || StringUtils.isBlank(ackCan.getSenderBank()))
					{
						ackCan.setSenderBank(swiftParserDao.retrieveBICDetails());
					}
					if(ackCan.getReceiverBank()==null || StringUtils.isEmpty(ackCan.getReceiverBank()) || StringUtils.isBlank(ackCan.getReceiverBank()))
					{
						ackCan.setReceiverBank(swiftParserDao.retrieveBICDetails());
					}
					ackMes = swiftChannelService.buildSwiftMessageForAck(ackCan);
					logger.info("SWIFT Ack Message : " + ackMes);
					if (ackMes == null)
					{
						EventLogger.logEvent("NGPHACKSVC0005", canonical, AcknowledgementsServiceImpl.class, canonical.getMsgRef());//SWIFT acknowledgement message could not be constructed.
					}
				}
				else if("SFMS".equalsIgnoreCase(baseFormat))
				{
					if(ackCan.getSenderBank()==null || StringUtils.isEmpty(ackCan.getSenderBank()) || StringUtils.isBlank(ackCan.getSenderBank()))
					{
						ackCan.setSenderBank(esbServiceDao.getInitialisedValue("LOCALIFSC"));
					}
					if(ackCan.getReceiverBank()==null || StringUtils.isEmpty(ackCan.getReceiverBank()) || StringUtils.isBlank(ackCan.getReceiverBank()))
					{
						ackCan.setReceiverBank(esbServiceDao.getInitialisedValue("LOCALIFSC"));
					}
					ackMes = sfmsChannelService.buildRtgsMessageforAck(ackCan);
					logger.info("SFMS Ack Message : " + ackMes);
					if (ackMes == null)
					{
						EventLogger.logEvent("NGPHACKSVC0006", canonical, AcknowledgementsServiceImpl.class, canonical.getMsgRef());//SFMS acknowledgement message could not be constructed.
					}
				}
				else if("ISO8583".equalsIgnoreCase(baseFormat))
				{
					//iSO8583ChannelService;
				}
				else
				{
					logger.error("Invalid Channel");
				}
				if (ackMes!=null)
				{
					try 
					{
						canonical.setMessage_so_far(ackMes);
						new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, canonical);
						
						//Generate PDF Report 
						if(canonical.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.COMPLETED_O)) || canonical.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.COMPLETED_I)));
						{
							reportList = new ArrayList<String>();
							reportList = esbServiceDao.getPDFReportData(canonical.getMsgRef());
							logger.info("ArrayList size is ::"+reportList.size());
						}
					}
					catch (Exception e) 
					{
						logger.error("Error occured while sending the acknowledgment to destination");
						logger.error(e, e);
						EventLogger.logEvent("NGPHACKSVC0004", null,  AcknowledgementsServiceImpl.class,ackCan.getMsgId());//QNG could not send the acknowledgment to the destination. Refer error log for details.
						throw new Exception(e);
					}
				}
			}
			else
			{
				logger.error("Destination message type and sub message type could not be found for the source details " + ackCan.getSrcMsgType() + " -- " + ackCan.getSrcSubMsgType() + " -- " + ackCan.getDstChnlType() + " -- " + ackCan.getMsgDirection());
				EventLogger.logEvent("NGPHACKSVC0001", null, AcknowledgementsServiceImpl.class, ackCan.getMsgId());//Destination message type mapping not found for acknowledgment. Refer error log for details.
			}
			//populating table
			if (!esbServiceDao.insertAckCanonicalDetails(ackCan))
			{
				logger.error("Error occured during insert of the acknowledgment data");
				EventLogger.logEvent("NGPHACKSVC0003", null,  AcknowledgementsServiceImpl.class, ackCan.getMsgId());//QNG could not persist the acknowledgment data. Refer error log for details.
			}
			esbServiceDao.updateMessageStatusforAckByMsgRef_ReasonCode(canonical.getMsgRef(), ackCan.getAckReasonCode(), canonical.getMsgStatus());
			new ReportQueue().QueueCall(canonical);
		}
		catch (Exception e) 
		{
			logger.error("Exception occured in acknowledgment processing");
			logger.error(e, e);
			EventLogger.logEvent("NGPHACKSVC0002", null, AcknowledgementsServiceImpl.class, ackCan.getMsgId());//Exception occured in acknowledgment processing. Refer error log for details.
			throw new Exception(e);
		}
	}
	
	//This method will Update AckCanonical from InfoCanonical Fetched from db
	public void processInfoAcknowledgement(AcknowledgementCanonical ackCan,InfoCanonical infoCanonical) throws Exception
	{	
		try
		{
			//loading property file in memory
			logger.info("Start processInfoAcknowledgement");
			Properties props = new Properties();
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			String ackMes = null;
			ackCan.setSeqNo(infoCanonical.getSeqNo());
			ackCan.setAckReceivedTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			ackCan.setDstChnlType(infoCanonical.getDstChnl());
			if(infoCanonical.getDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
			{
				ackCan.setDstEiId(props.getProperty("INFO_EI_I"));
			}
			else
			{
				ackCan.setDstEiId(props.getProperty("INFO_EI_O"));
			}
			ackCan.setMsgBranch(infoCanonical.getBranch());
			ackCan.setMsgDept(infoCanonical.getDept());
			
			if(infoCanonical.getDirection()!=null && infoCanonical.getDirection().equalsIgnoreCase("I"))
			{
				ackCan.setMsgDirection("O");
			}
			else
			{
				ackCan.setMsgDirection("I");
			}
			
			ackCan.setMsgMur(infoCanonical.getMsgMur());
			ackCan.setMsgOriginalId(infoCanonical.getMsgRef());
			ackCan.setSrcChnlType(props.getProperty("SFMS"));
			if(infoCanonical.getDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
			{
				ackCan.setSrcEiId(props.getProperty("INFO_EI_O"));
			}
			else
			{
				ackCan.setSrcEiId(props.getProperty("INFO_EI_I"));
			}
			logger.info("ackCan.setSrcEiId is "+ackCan.getSrcEiId());
			logger.info("ackCan.getSrcMsgType() : " + ackCan.getSrcMsgType());
			logger.info("infoCanonical.getMsgRef() : " + infoCanonical.getMsgRef());
			logger.info("ackCan.getDstChnlType() : " + ackCan.getDstChnlType());
			logger.info("ackCan.getMsgDirection() : " + ackCan.getMsgDirection());
			logger.info("ackCan.getAckReasonCode "+ ackCan.getAckReasonCode());
			
			if (ackCan.getAckReasonCode() != null)
			{
				if(ackCan.getAckReasonCode().equalsIgnoreCase("PBAPI000000") || ackCan.getAckReasonCode().equalsIgnoreCase("00") || (ackCan.getAckType() != null && ackCan.getAckType().equalsIgnoreCase("Y")))
				{
					//set status as Completed
					infoCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.COMPLETED_O));
					if(ackCan.getAckType() == null || StringUtils.isEmpty(ackCan.getAckType()) || StringUtils.isBlank(ackCan.getAckType()))
					{
						ackCan.setAckType("Y");
					}
				}
				else
				{
					//set status as Rejected By Channel
					infoCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_BY_CHANNEL_O));
					if(ackCan.getAckType() == null || StringUtils.isEmpty(ackCan.getAckType()) || StringUtils.isBlank(ackCan.getAckType()))
					{
						ackCan.setAckType("N");
					}
				}
			}
			//fetch the dstMsgtype and DstSubMestype from DB based on configuration
			List<String> dstMsgInfo = esbServiceDao.getDstMsgtype(ackCan.getSrcMsgType(), ackCan.getSrcSubMsgType(),ackCan.getDstChnlType(),ackCan.getMsgDirection());
			if(dstMsgInfo!=null && dstMsgInfo.size()>1)
			{
				ackCan.setDstMsgType(dstMsgInfo.get(0));
				ackCan.setDstSubMsgType(dstMsgInfo.get(1));		
				//calling Channel Services				
				try 
				{
					props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
				} 
				catch (IOException e) 
				{
					logger.error(e, e);
					throw new Exception(e);
				}
				catch (Exception e) 
				{
					logger.error(e, e);
					throw new Exception(e);
				}
				
				if(ackCan.getSenderBank()==null || StringUtils.isEmpty(ackCan.getSenderBank()) || StringUtils.isBlank(ackCan.getSenderBank()))
				{
					ackCan.setSenderBank(esbServiceDao.getInitialisedValue("LOCALIFSC"));
				}
				if(ackCan.getReceiverBank()==null || StringUtils.isEmpty(ackCan.getReceiverBank()) || StringUtils.isBlank(ackCan.getReceiverBank()))
				{
					ackCan.setReceiverBank(esbServiceDao.getInitialisedValue("LOCALIFSC"));
				}
				ackMes = sfmsChannelService.buildRtgsMessageforAck(ackCan);
				logger.info("SFMS Info Ack Message : " + ackMes);
				if(ackMes!=null)
				{
					try 
					{
						infoCanonical.setMessage_info(ackMes);
						new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, infoCanonical);
					}
					catch (Exception e) 
					{
						logger.error("Error occured while sending the acknowledgment to destination ->"+e.toString());
						EventLogger.logEvent("NGPHACKSVC0004", null,  AcknowledgementsServiceImpl.class,ackCan.getMsgId());//QNG could not send the acknowledgment to the destination. Refer error log for details.
						throw new Exception(e);
					}
				}
				
			}
			else
			{
				logger.error("Destination message type and sub message type could not be found for the source details " + ackCan.getSrcMsgType() + " -- " + ackCan.getSrcSubMsgType() + " -- " + ackCan.getDstChnlType() + " -- " + ackCan.getMsgDirection());
				EventLogger.logEvent("NGPHACKSVC0001", null, AcknowledgementsServiceImpl.class, ackCan.getMsgId());//Destination message type mapping not found for acknowledgment. Refer error log for details.
			}
			
			//populating table
			if (!esbServiceDao.insertAckCanonicalDetails(ackCan))
			{
				logger.error("Error occured during insert of the acknowledgment data");
				EventLogger.logEvent("NGPHACKSVC0003", null,  AcknowledgementsServiceImpl.class, ackCan.getMsgId());//QNG could not persist the acknowledgment data. Refer error log for details.
			}
			esbServiceDao.updateInfoMsgsStatusforAckByMsgRef_ReasonCode(infoCanonical.getMsgRef(), ackCan.getAckReasonCode(), infoCanonical.getMsgStatus());
			logger.info("End processInfoAcknowledgement");
		}
		
		catch (Exception e) 
		{
			logger.error("Exception occured in Infoacknowledgment processing "+e.toString());
			EventLogger.logEvent("NGPHACKSVC0002", null, AcknowledgementsServiceImpl.class, ackCan.getMsgId());//Exception occured in acknowledgment processing. Refer error log for details.
			throw new Exception(e);
		}
	}
}
