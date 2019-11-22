package com.logica.ngph.esb.servicesImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.esb.Dtos.BGMast;
import com.logica.ngph.esb.Dtos.LcMast;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.services.LCBgHandlerService;

public class LCBgHandlerServiceImpl implements LCBgHandlerService 
{
	Logger logger = Logger.getLogger(LCBgHandlerServiceImpl.class);	
	private EsbServiceDao esbServiceDao;
	private static String propName = "System.properties";
	private static String UIEI = null;
	private static Properties props = null;
	
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}

	public void doProcess(NgphCanonical canonicalData)throws Exception
	{
		try
		{
			if(canonicalData!=null)
			{
				props = new Properties();
				try 
				{
					props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
					UIEI=props.getProperty("UIEI");
					logger.info("UI EI is: " + UIEI);
				} 
				catch (IOException e) 
				{
					logger.error(e, e);
				}
				catch (Exception e) 
				{
					logger.error(e, e);
				}
				LcMast lcMastDto = new LcMast();
				BGMast bgMastDto = new BGMast();
				if (canonicalData.getDstMsgType() !=null && canonicalData.getDstMsgType().equalsIgnoreCase("705") || canonicalData.getDstMsgType().equalsIgnoreCase("700") || canonicalData.getDstMsgType().equalsIgnoreCase("701"))
				{	
					if(canonicalData.getReceiverBank()!=null)
					{
						lcMastDto.setLcAdvisingBank(canonicalData.getReceiverBank());
					}
					if(canonicalData.getSenderBank()!=null)
					{
						lcMastDto.setLcIssuingBank(canonicalData.getSenderBank());
					}
					if(canonicalData.getMsgAmount()!=null)
					{
						lcMastDto.setLcAmount(canonicalData.getMsgAmount());
					}
					if(canonicalData.getOrderingCustomerName()!=null)
					{
						lcMastDto.setLcAppicant(canonicalData.getOrderingCustomerName());
						if(canonicalData.getOrderingCustomerAddress()!=null)
						{
							lcMastDto.setLcAppicant(canonicalData.getOrderingCustomerName() + canonicalData.getOrderingCustomerAddress());
						}
					}
					if(canonicalData.getBeneficiaryCustomerName()!=null)
					{
						lcMastDto.setLcBenificiary(canonicalData.getBeneficiaryCustomerName());
						if(canonicalData.getBeneficiaryCustomerAddress()!=null)
						{
							lcMastDto.setLcBenificiary(canonicalData.getBeneficiaryCustomerAddress() + canonicalData.getBeneficiaryCustomerAddress());
						}
					}
					if(canonicalData.getMsgCurrency()!=null)
					{
						lcMastDto.setLcCurrency(canonicalData.getMsgCurrency());
					}
					if(canonicalData.getMsgDirection()!=null)
					{
						lcMastDto.setLcDirection(canonicalData.getMsgDirection());
					}
					if(canonicalData.getLcExpDt()!=null)
					{
						lcMastDto.setLcExpireDate(canonicalData.getLcExpDt());
					}
					if(canonicalData.getLcIssueDt()!=null)
					{
						lcMastDto.setLcIssueDate(canonicalData.getLcIssueDt());
					}
					if(canonicalData.getLcNarrative()!=null)
					{
						lcMastDto.setLcNarrative(canonicalData.getLcNarrative());
					}
					if(canonicalData.getLcNo()!=null)
					{
						lcMastDto.setLcNo(canonicalData.getLcNo());
					}
					lcMastDto.setLcNumOfAmndments(new BigDecimal(0));
					lcMastDto.setLcNumOfMsgs(new BigDecimal(1));
					if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("700")) 
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_OPENED));
					}
					else if (canonicalData.getDstMsgType() !=null && canonicalData.getDstMsgType().equalsIgnoreCase("705"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_PRE_ADVICE));
					}
					
					if(canonicalData.getLcType()!=null)
					{
						lcMastDto.setLcType(canonicalData.getLcType());
					}
					if(canonicalData.getMsgRef()!=null)
					{
						lcMastDto.setMsgRef(canonicalData.getMsgRef());
					}
					logger.info("The message status is " + canonicalData.getMsgStatus());
					logger.info("The previous message status is " + canonicalData.getMsgPrevStatus());
					if (canonicalData.getMsgHost().equalsIgnoreCase(UIEI) || canonicalData.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_RELEASE_O)))
					{
						esbServiceDao.updateLcMast(lcMastDto);
					}
					else
					{
						esbServiceDao.populateLcMast(lcMastDto);
					}
					logger.info("Message Status is :: "+canonicalData.getMsgStatus());
				}
				else if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("760") && canonicalData.getDstMsgSubType().equalsIgnoreCase("XXX"))
				{	
					if(canonicalData.getReceiverBank()!=null)
					{
						bgMastDto.setBgAdvisingBank(canonicalData.getReceiverBank());
					}
					if(canonicalData.getSenderBank()!=null)
					{
						bgMastDto.setBgIssuingBank(canonicalData.getSenderBank());
					}
					if(canonicalData.getMsgAmount()!=null)
					{
						bgMastDto.setBgAmount(canonicalData.getMsgAmount());
					}
					if(canonicalData.getLcType()!=null)
					{
						bgMastDto.setBgCreateType(canonicalData.getLcType());
					}
					if(canonicalData.getLcDocsReq1()!=null)
					{
						bgMastDto.setBgDetails(canonicalData.getLcDocsReq1());
						if(canonicalData.getLcDocsReq2()!=null)
						{
							bgMastDto.setBgDetails(canonicalData.getLcDocsReq1() + canonicalData.getLcDocsReq2());
							if(canonicalData.getLcAddnlCndt1()!=null)
							{
								bgMastDto.setBgDetails(canonicalData.getLcDocsReq1() + canonicalData.getLcDocsReq2() + canonicalData.getLcAddnlCndt1());
							}
						}
					}
					
					if(canonicalData.getMsgDirection()!=null)
					{
						bgMastDto.setBgDirection(canonicalData.getMsgDirection());
					}
					if(canonicalData.getLcIssueDt()!=null)
					{
						bgMastDto.setBgIssueDate(canonicalData.getLcIssueDt());
					}
					
					if(canonicalData.getInstructionsForCrdtrAgtCode()!=null)
					{
						bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode());
						if(canonicalData.getInstructionsForCrdtrAgtText()!=null)
						{
							bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode() + canonicalData.getInstructionsForCrdtrAgtText());
						}
					}
					if(canonicalData.getLcNo()!=null)
					{
						bgMastDto.setBgNo(canonicalData.getLcNo());
					}
					//bgMastDto.setBgNoOfMsgs(new BigDecimal(1)); to be updated
					//logger.info("LCBgHandlerServiceImpl :: is Sequence Total is :: "+canonicalData.getNoofMessages()+" "+canonicalData.getSequenceNo());
					bgMastDto.setBgSequenceNo(canonicalData.getSequenceNo());
					bgMastDto.setBgNoOfMsgs(canonicalData.getNoofMessages());					
					bgMastDto.setBgStatus(new BigDecimal(NgphEsbConstants.BG_CREATED));

					if(canonicalData.getLcAppRulesCode()!=null)
					{
						bgMastDto.setBgRulesCode(canonicalData.getLcAppRulesCode());
					}
					if(canonicalData.getLcAppRulesDesc()!=null)
					{
						bgMastDto.setBgRulesDesc(canonicalData.getLcAppRulesDesc());
					}
					if(canonicalData.getMsgRef()!=null)
					{
						bgMastDto.setMsgRef(canonicalData.getMsgRef());
					}
					logger.info("The message status is " + canonicalData.getMsgStatus());
					logger.info("The previous message status is " + canonicalData.getMsgPrevStatus());
				
						if ((canonicalData.getMsgHost().equalsIgnoreCase(UIEI) && esbServiceDao.getOriginalLcNo(canonicalData.getLcNo())!=null) || canonicalData.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_RELEASE_O)))
						{
							esbServiceDao.updateBgMast(bgMastDto);
						}
						else
						{
							esbServiceDao.populateBGMast(bgMastDto);
						}
					
					
				}
				//Start :: Added for LCBG-169
				else if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("760") && canonicalData.getDstMsgSubType().equalsIgnoreCase("COV") || canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("767") && canonicalData.getDstMsgSubType().equalsIgnoreCase("COV"))
				{	
					/*if(canonicalData.getTxnReference()!=null)
					{
						bgMastDto.setBgNo(canonicalData.getTxnReference());
					}
					if(canonicalData.getBgType()!=null)
					{
						bgMastDto.setBgCreateType(canonicalData.getBgType());
					}
					if(canonicalData.getBgAmt()!=null)
					{
						bgMastDto.setBgAmount(canonicalData.getBgAmt());
					}
					if(canonicalData.getBgFromDate()!=null)
					{
					}
					if(canonicalData.getBgToDate()!=null)
					{
					}
					if(canonicalData.getBgEffectiveDate()!=null)
					{
					}
					if(canonicalData.getBgLodgementEndDate()!=null)
					{
					}
					if(canonicalData.getBgLodgementPlace()!=null)
					{
					}
					if(canonicalData.getBgLodgementPlace()!=null)
					{
					}
					if(canonicalData.getReceiverBank()!=null)
					{
						bgMastDto.setBgAdvisingBank(canonicalData.getReceiverBank());
					}
					if(canonicalData.getSenderBank()!=null)
					{
						bgMastDto.setBgIssuingBank(canonicalData.getSenderBank());
					}		
					if(canonicalData.getIssueingBankAddr()!=null)
					{
						bgMastDto.setBgIssuingBank(canonicalData.getIssueingBankAddr());
					}	
					if(canonicalData.getMsgDirection()!=null)
					{
						bgMastDto.setBgDirection(canonicalData.getMsgDirection());
					}				
					bgMastDto.setBgSequenceNo(canonicalData.getSequenceNo());
					bgMastDto.setBgNoOfMsgs(canonicalData.getNoofMessages());					
					bgMastDto.setBgStatus(new BigDecimal(NgphEsbConstants.BG_CREATED));
					if(canonicalData.getMsgRef()!=null)
					{
						bgMastDto.setMsgRef(canonicalData.getMsgRef());
					}
					logger.info("The message status is " + canonicalData.getMsgStatus());
					logger.info("The previous message status is " + canonicalData.getMsgPrevStatus());
					
					if (canonicalData.getMsgHost().equalsIgnoreCase(UIEI) || canonicalData.getMsgStatus().equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_RELEASE_O)))
					{
						esbServiceDao.updateBgMast(bgMastDto);
					}
					else
					{
						esbServiceDao.populateBGMast(bgMastDto);
					}*/
				}
				//End :: Added for LCBG-169
				else if (canonicalData.getDstMsgType() !=null && canonicalData.getDstMsgType().equalsIgnoreCase("707") || canonicalData.getDstMsgType().equalsIgnoreCase("732") || canonicalData.getDstMsgType().equalsIgnoreCase("734") || canonicalData.getDstMsgType().equalsIgnoreCase("740") || canonicalData.getDstMsgType().equalsIgnoreCase("742") || canonicalData.getDstMsgType().equalsIgnoreCase("747") || canonicalData.getDstMsgType().equalsIgnoreCase("750") || canonicalData.getDstMsgType().equalsIgnoreCase("752") || canonicalData.getDstMsgType().equalsIgnoreCase("754") || canonicalData.getDstMsgType().equalsIgnoreCase("756"))
				{
					
					if (canonicalData.getDstMsgType().equalsIgnoreCase("707"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_AMENDED));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("732"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_ADVICE_ACCEPTED));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("734"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_ADVICE_REJECTED));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("740") || canonicalData.getDstMsgType().equalsIgnoreCase("747"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_PYMT_AUTHORISED));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("742"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_CLAIMED));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("750"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_DISCREPANT));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("752"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_DISCREPANT_APPROVED));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("754"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_PYMT_ADVICED));
					}
					if (canonicalData.getDstMsgType().equalsIgnoreCase("756"))
					{
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_DISCREPANT_PYMT_ADVICED));
					}
					if(canonicalData.getLcNarrative()!=null)
					{
						lcMastDto.setLcNarrative(canonicalData.getLcNarrative());
					}
					if(canonicalData.getLcNo()!=null)
					{
						lcMastDto.setLcNo(canonicalData.getLcNo());
					}
					if(canonicalData.getMsgAmount()!=null)
					{
						//FIXME calculate based on amendments in amount and then set for 707 and 747
						lcMastDto.setLcAmount(canonicalData.getMsgAmount());
					}
					if(canonicalData.getOrderingCustomerName()!=null)
					{
						lcMastDto.setLcAppicant(canonicalData.getOrderingCustomerName());
						if(canonicalData.getOrderingCustomerAddress()!=null)
						{
							lcMastDto.setLcAppicant(canonicalData.getOrderingCustomerName() + canonicalData.getOrderingCustomerAddress());
						}
					}
					if(canonicalData.getBeneficiaryCustomerName()!=null)
					{
						lcMastDto.setLcBenificiary(canonicalData.getBeneficiaryCustomerName());
						if(canonicalData.getBeneficiaryCustomerAddress()!=null)
						{
							lcMastDto.setLcBenificiary(canonicalData.getBeneficiaryCustomerAddress() + canonicalData.getBeneficiaryCustomerAddress());
						}
					}
					if(canonicalData.getLcExpDt()!=null)
					{
						lcMastDto.setLcExpireDate(canonicalData.getLcExpDt());
					}
					if(canonicalData.getMsgRef()!=null)
					{
						lcMastDto.setMsgRef(canonicalData.getMsgRef());
					}
					//update LC Mast
					esbServiceDao.updateLcMast(lcMastDto);
				}
				else if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("730"))
				{
					String LCno = null;
					LCno = esbServiceDao.getOriginalLcNo(canonicalData.getRelReference());
					logger.info("Lc Number is ::"+LCno);
					if(canonicalData.getRelReference().equalsIgnoreCase(LCno))
					{
						if(canonicalData.getLcAckDt()!=null)
						{
							lcMastDto.setLcAckDate(canonicalData.getLcAckDt());
						} 
						lcMastDto.setLcStatus(new BigDecimal(NgphEsbConstants.LC_ACKNOWLEDGED));
						if(canonicalData.getLcNo()!=null)
						{
							lcMastDto.setLcNo(canonicalData.getRelReference());
						}
					}
					//update LC Mast
					esbServiceDao.updateLcMast(lcMastDto);
				}
				else if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("767") && canonicalData.getDstMsgSubType().equalsIgnoreCase("XXX"))
				{
					
					if (canonicalData.getDstMsgType().equalsIgnoreCase("767"))
					{
						bgMastDto.setBgStatus(new BigDecimal(NgphEsbConstants.BG_AMENDED));
					}
					if(canonicalData.getLcNo()!=null && canonicalData.getRelReference()!=null)
					{
						bgMastDto.setBgNo(canonicalData.getRelReference());
						logger.info("RelReference is "+bgMastDto.getBgNo());
					}
					if(canonicalData.getLcDocsReq1()!=null)
					{
						bgMastDto.setBgDetails(canonicalData.getLcDocsReq1());
						if(canonicalData.getLcDocsReq2()!=null)
						{
							bgMastDto.setBgDetails(canonicalData.getLcDocsReq1() + canonicalData.getLcDocsReq2());
							if(canonicalData.getLcAddnlCndt1()!=null)
							{
								bgMastDto.setBgDetails(canonicalData.getLcDocsReq1() + canonicalData.getLcDocsReq2() + canonicalData.getLcAddnlCndt1());
							}
						}
					}
					if(canonicalData.getLcAmndmntDt()!=null)
					{
						bgMastDto.setBgLastAmendmentDate(canonicalData.getLcAmndmntDt());
					}
					if(canonicalData.getInstructionsForCrdtrAgtCode()!=null)
					{
						bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode());
						if(canonicalData.getInstructionsForCrdtrAgtText()!=null)
						{
							bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode() + canonicalData.getInstructionsForCrdtrAgtText());
						}
					}
								
					if(canonicalData.getMsgRef()!=null)
					{
						bgMastDto.setMsgRef(canonicalData.getMsgRef());
					}
					
					if(canonicalData.getLcAmndmntNo()>0)
					{
						bgMastDto.setBgNumOfAmndments(new BigDecimal(canonicalData.getLcAmndmntNo()));
					}
					
					if (canonicalData.getSequenceNo()!=null)
					{
						bgMastDto.setBgSequenceNo(canonicalData.getSequenceNo());
					}
					
					if (canonicalData.getNoofMessages()!=null)
					{
						bgMastDto.setBgNoOfMsgs(canonicalData.getNoofMessages());
					}					
					
					if(canonicalData.getRelReference()!=null)
					{
						bgMastDto.setBgLastAmendmentRef(canonicalData.getRelReference());
					}
									
					//Update BG Mast
					//Start :: Added for BG COV messages
					if(canonicalData.getSrcMsgSubType().equalsIgnoreCase("XXX"))
					{
						esbServiceDao.updateBgMast(bgMastDto);
					}
				}
				else if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("730"))
				{
					//get old LC status and based on that update the new status 
				}
				else if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("768"))
				{
					//get old BG status and based on that update the new status
					if(canonicalData.getLcNo()!=null)
					{
						bgMastDto.setBgNo(canonicalData.getLcNo());
					}	
					
					if(canonicalData.getRelReference()!=null)
					{
						bgMastDto.setBgRelReferenceNo(canonicalData.getRelReference());
					}
					if(canonicalData.getLcAccId()!=null)
					{
						bgMastDto.setBgAccIdentification(canonicalData.getLcAccId());
					}
					if(canonicalData.getLcAckDt()!=null)
					{
						bgMastDto.setBgAckDate(canonicalData.getLcAckDt());
					}
					if(canonicalData.getMsgValueDate()!=null)
					{
						bgMastDto.setBgAckDate(canonicalData.getMsgValueDate());
					}
					if(canonicalData.getLcChgsClaimed()!=null)
					{
						bgMastDto.setBgAmount(canonicalData.getLcChgsClaimed());
					}
					if(canonicalData.getLcCharges()!=null)
					{
						bgMastDto.setBgChargeDetails(canonicalData.getLcCharges());
					}
					if(canonicalData.getInstructionsForCrdtrAgtCode()!=null)
					{
						bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode());
						if(canonicalData.getInstructionsForCrdtrAgtText()!=null)
						{
							bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode() + canonicalData.getInstructionsForCrdtrAgtText());
						}
					}
					bgMastDto.setBgStatus(new BigDecimal(NgphEsbConstants.BG_CREATEDACK));
					
					esbServiceDao.updateBgMast(bgMastDto);
					//esbServiceDao.populateBGMast(bgMastDto);					
				}
			else if (canonicalData.getDstMsgType()!=null && canonicalData.getDstMsgType().equalsIgnoreCase("769"))
			{
				//get old BG status and based on that update the new status
				if(canonicalData.getMsgRef()!=null)
				{
					bgMastDto.setMsgRef(canonicalData.getMsgRef());
				}
				if(canonicalData.getMsgDirection()!=null)
				{
					bgMastDto.setBgDirection(canonicalData.getMsgDirection());
				}
				if(canonicalData.getLcNo()!=null)
				{
					bgMastDto.setBgNo(canonicalData.getLcNo());
				}	
				if(canonicalData.getRelReference()!=null)
				{
					bgMastDto.setBgRelReferenceNo(canonicalData.getRelReference());
				}
				if(canonicalData.getLcAccId()!=null)
				{
					bgMastDto.setBgAccIdentification(canonicalData.getLcAccId());
				}
				if(canonicalData.getLcAmndmntDt()!=null)
				{
					bgMastDto.setBgAckDate(canonicalData.getLcAmndmntDt());
				}	
				if(canonicalData.getMsgCurrency()!=null)
				{
					bgMastDto.setBgChargeCurrCode(canonicalData.getMsgCurrency());
				}
				if(canonicalData.getLcToAmtClaimed()!=null)
				{
					bgMastDto.setBgChargeAmt(canonicalData.getLcToAmtClaimed());
				}
				if(canonicalData.getMsgValueDate()!=null)
				{
					bgMastDto.setBgChargeDate(canonicalData.getMsgValueDate());
				}
				if(canonicalData.getLcAdditionalCurrency()!=null)
				{
					bgMastDto.setBgReductionCurrCode(canonicalData.getLcAdditionalCurrency());
				}
				if(canonicalData.getLcAdditionalAmt()!=null)
				{
					bgMastDto.setBgReductionAmt(canonicalData.getLcAdditionalAmt());
				}
				if(canonicalData.getInstructedCurrency()!=null)
				{
					bgMastDto.setBgOctStdCurrCode(canonicalData.getInstructedCurrency());
				}
				if(canonicalData.getLcTotalAmtClaimed()!=null)
				{
					bgMastDto.setBgOctStdAmt(canonicalData.getLcTotalAmtClaimed());
				}
				if(canonicalData.getLcAddlAmts()!=null)
				{
					bgMastDto.setBgAmtSpecification(canonicalData.getLcAddlAmts());
				}
				
				if(canonicalData.getLcCharges()!=null)
				{
					bgMastDto.setBgChargeDetails(canonicalData.getLcCharges());
				}
				if(canonicalData.getInstructionsForCrdtrAgtCode()!=null)
				{
					bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode());
					if(canonicalData.getInstructionsForCrdtrAgtText()!=null)
					{
						bgMastDto.setBgNarration(canonicalData.getInstructionsForCrdtrAgtCode() + canonicalData.getInstructionsForCrdtrAgtText());
					}
				}
				bgMastDto.setBgStatus(new BigDecimal(NgphEsbConstants.BG_REDUCED));
				
				esbServiceDao.updateBgMast(bgMastDto);
				//esbServiceDao.populateBGMast(bgMastDto);					
			}
			else
			{
				logger.error("Null Canonical received by LCBgHandlerService", new NullPointerException("canonical is null"));
			}
			}
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}	
	}
}
