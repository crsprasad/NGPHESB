package com.logica.ngph.esb.servicesImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.RepairReasonsEnum;
import com.logica.ngph.common.utils.RulesEvaluationHelper;
import com.logica.ngph.esb.Dtos.Rules;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.enums.RuleActionsEnum;
import com.logica.ngph.esb.services.EnrichService;
import com.logica.ngph.utils.EventLogger;
/**
 * 
 * @author mohdabdulaa
 *
 */
public class EnrichServiceImpl implements EnrichService 
{	
	static Logger logger = Logger.getLogger(EnrichServiceImpl.class);
	private EsbServiceDao esbServiceDao;	
	private final String propName = "System.properties";
	private String bnkID_S = "IFSC";
	private String bnkID_R = "IFSC";
	private static String UIEI = null;
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}

	public void performEnrichService(NgphCanonical canonicalData) throws Exception
	{
		try
		{
			logger.info("performEnrichService(...) Start....");
			Properties props = new Properties();
			try 
			{
				props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			} 
			catch (IOException e) 
			{
				logger.error(e, e);
			}
			catch (Exception e) 
			{
				logger.error(e, e);
			}
			//Case Handled for IMPS and the below is required for outwards
			if (canonicalData.getMsgPurposeCode()==null  && StringUtils.isNotBlank(canonicalData.getDstMsgChnlType())&& canonicalData.getDstMsgChnlType().equalsIgnoreCase("IMPS"))
			{
				canonicalData.setMsgPurposeCode("900000");
			}
			if(canonicalData.getMsgPurposeCode()!=null &&  canonicalData.getMsgPurposeCode().startsWith("90"))
			{
				String accName = null;
				String accNum = null;
				String acClosed = null;
				String acCity = null;
				String acState = null;
				String acCtry = null;
				String acStatus = null;
				String acCreditAllowed = null;
				String mmId = null;
				String mobNo = null;
				String acType = null;
				if(canonicalData.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.INWARD_PAYMENT))
				{
					ArrayList<String> data = null;
					if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ))
					{
						mmId = canonicalData.getBeneficiaryCustomerID();
						mobNo = canonicalData.getBeneficiaryCustomerCtctDtls();
						data = (ArrayList<String>)esbServiceDao.getAcDetailsByMMIDAndMobile(mmId, mobNo);
					}
					else if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ))
					{
						//String brnCode = esbServiceDao.findBranchCodeByBic(canonicalData.getReceiverBank());
						String changeLookUpBrn = props.getProperty("CHANGELOOKUPBRN");
						if (changeLookUpBrn != null && changeLookUpBrn.equalsIgnoreCase("Y"))
						{
							String lookUpBranch = props.getProperty("BRANCH" + canonicalData.getMsgBranch());
							logger.info("Lookin up for branch " + lookUpBranch + " instead of " + canonicalData.getMsgBranch());
							if (lookUpBranch != null)
							{
								data = (ArrayList<String>)esbServiceDao.getAcDetailsByAccountAndBranch(canonicalData.getBeneficiaryCustAcct(), lookUpBranch, 0);
							}
							else
							{
								logger.info("Lookup changed branch is null");
								data = (ArrayList<String>)esbServiceDao.getAcDetailsByAccountAndBranch(canonicalData.getBeneficiaryCustAcct(), canonicalData.getMsgBranch(), 0);
							}
						}
						else
						{
							logger.info("Change lookup brn config not available");
							data = (ArrayList<String>)esbServiceDao.getAcDetailsByAccountAndBranch(canonicalData.getBeneficiaryCustAcct(), canonicalData.getMsgBranch(), 0);
						}
					}
					//if(data.get(0)!=null && data.get(1)!=null)
					if (data.size()==11)
					{
						accName = data.get(0);
						accNum = data.get(1);
						acClosed = data.get(2);
						acCity = data.get(3);
						acState = data.get(4);
						acCtry = data.get(5);
						acStatus = data.get(6);
						acCreditAllowed = data.get(7);
						if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ))
						{
							mobNo = data.get(8);
						}
						acType = data.get(10);
						if (acCity != null)
						{
							if (acCity.length() < 13)
							{
								StringUtils.rightPad(acCity, 13, " ");
							}
							else
							{
								acCity = acCity.substring(0, 13);
							}
						}
						if (acState != null)
						{
							if (acState.length() < 3)
							{
								StringUtils.rightPad(acState, 3, " ");
							}
							else
							{
								acState = acState.substring(0, 3);
							}
						}
						if (accNum != null)
						{
							canonicalData.setCustAccount(accNum);
						}
						if (accName != null)
						{
							canonicalData.setBeneficiaryCustomerName(accName);
						}
						if (acCity != null && acState != null && acCtry != null)
						{
							canonicalData.setBeneficiaryCustomerAddress(acCity + "\r\n" + acState + "\r\n" + acCtry);
						}
						else
						{
							if ((canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ))&& mobNo != null)
							{
								canonicalData.setBeneficiaryCustomerAddress(mobNo);
							}
						}
						if (mobNo != null)
						{
							canonicalData.setBeneficiaryCustomerCtctDtls(mobNo);
						}
						if (acType != null)
						{
							canonicalData.setBeneficiaryAcType(acType);
							//Set the processing code with account types in canonical only when transaction type is P2A
							if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ))
							{
								if (canonicalData.getOrderingAcType().equalsIgnoreCase("00"))
								{
									//FIXME change this also as configurable value - in initialisationm
									canonicalData.setOrderingAcType("11");
								}
								canonicalData.setMsgPurposeCode("90" + canonicalData.getOrderingAcType() + canonicalData.getBeneficiaryAcType());
							}
							String replaceAcType = props.getProperty("REPLACEBENACTYPE");
							if (replaceAcType != null && replaceAcType.equalsIgnoreCase("Y"))
							{
								if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ))
								{
									canonicalData.setLcType("2P");
								}
								else if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ))
								{
									canonicalData.setLcType("2A");
								}
								else if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ))
								{
									canonicalData.setLcType("2M");
								}
							}
						}
						logger.info("Benf cust acct " + accNum);
						logger.info("Benf cust name "+ accName);
						logger.info("Acclosed status " + acClosed);
						logger.info("Address fetched " + canonicalData.getBeneficiaryCustomerAddress());
						if (acClosed != null && acStatus!= null && acCreditAllowed != null)
						{
							if (acClosed.equals("1") || acStatus.equalsIgnoreCase(NgphEsbConstants.AC_STATUS_CLOSED) || acStatus.equalsIgnoreCase(NgphEsbConstants.AC_STATUS_RESERVED))
							{
								logger.error("In-bound Payment. The account number " + accNum + " with account name " +accName + "is closed");
								canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0002, canonicalData.getDstMsgChnlType());
								EventLogger.logEvent("NGPHENRSVC0002", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//In-bound Payment. Account {BeneficiaryCustAcct} associated to MMID {BeneficiaryCustomerID} and Mobile Number {BeneficiaryCustomerCtctDtls} is closed in QNG.
							}
							else if (acCreditAllowed.equalsIgnoreCase("N"))
							{
								logger.error("In-bound Payment. The account number " + accNum + " with account name " +accName + "does not allow credits");
								canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0011, canonicalData.getDstMsgChnlType());
								EventLogger.logEvent("NGPHENRSVC0011", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//In-bound Payment. Account {BeneficiaryCustAcct} not allowed for credit.
							}
						}
						else
						{
							logger.error("In-bound Payment. The account number " + accNum + " with account name " +accName + "is closed");
							canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0002, canonicalData.getDstMsgChnlType());
							EventLogger.logEvent("NGPHENRSVC0002", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//In-bound Payment. Account {BeneficiaryCustAcct} associated to MMID {BeneficiaryCustomerID} and Mobile Number {BeneficiaryCustomerCtctDtls} is closed in QNG.
						}
					}
					else
					{
						logger.error("In-bound Payment. Account details could not be found.");
						canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0001, canonicalData.getDstMsgChnlType());
						EventLogger.logEvent("NGPHENRSVC0001", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//In-bound Payment. Account details could not be found for the MMID {BeneficiaryCustomerID} and Mobile Number {BeneficiaryCustomerCtctDtls}
					}
				}
				//If it is an outward payment
				else
				{
					String IMPS_NBIN=null;
					String benfMobNo = null;
					if (canonicalData.getMsgHost().equalsIgnoreCase(props.getProperty("UIEI")))
					{
						IMPS_NBIN = canonicalData.getBeneficiaryCustomerID().substring(0,4);
						canonicalData.setReceiverBank(esbServiceDao.getBankFirstIFSC(esbServiceDao.getIMPSMapBnkCode(IMPS_NBIN)));
						benfMobNo = canonicalData.getBeneficiaryCustomerCtctDtls();
						mobNo = canonicalData.getOrderingCustomerCtctDtls();
					}
					else
					{
						IMPS_NBIN = esbServiceDao.getIMPSMapBnkNBIN(canonicalData.getReceiverBank().substring(0, 4));
						int idxCRLF = canonicalData.getBeneficiaryCustomerAddress().indexOf("\r");
						if (idxCRLF>-1)
						{
							benfMobNo = canonicalData.getBeneficiaryCustomerAddress().substring(0,idxCRLF);
						}
						else
						{
							benfMobNo = canonicalData.getBeneficiaryCustomerAddress();
						}
						mobNo = esbServiceDao.getMobNo(canonicalData.getOrderingCustAccount());
					}
					ArrayList<String> data = null;
					logger.info("Sender Bank is :: performEnrichService :: Outward :: "+canonicalData.getSenderBank());
					String brnCode = esbServiceDao.findBranchCodeByBic(canonicalData.getSenderBank());
					data = (ArrayList<String>)esbServiceDao.getAcDetailsByAccountAndBranch(canonicalData.getOrderingCustAccount(), brnCode, 1);
					if (mobNo != null && data.size()>0)
					{
						canonicalData.setOrderingCustomerCtctDtls(mobNo);
						if (data.size()==11)
						{
							accName = data.get(0);
							accNum = data.get(1);
							acClosed = data.get(2);
							acCity = data.get(3);
							acState = data.get(4);
							acCtry = data.get(5);
							acStatus = data.get(6);
							acCreditAllowed = data.get(7);
							mmId = data.get(9);
							acType = data.get(10);
							if (acClosed.equals("1"))
							{
								canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0004, canonicalData.getDstMsgChnlType());
								logger.error("Outbound Payment. The account number " + canonicalData.getOrderingCustAccount() + " with account name " +accName + "is closed");
								EventLogger.logEvent("NGPHENRSVC0004", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Outbound Payment. Account {OrderingCustAccount} is closed in QNG.
							}
							if (acType != null)
							{
								canonicalData.setOrderingAcType(acType);
								canonicalData.setBeneficiaryAcType("11");
								logger.info("canonicalData.getMsgTxnType() is ::"+canonicalData.getMsgTxnType());
								if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ))
								{
									canonicalData.setMsgPurposeCode("90" + canonicalData.getOrderingAcType() + canonicalData.getBeneficiaryAcType());
								}
								else
								{
									canonicalData.setMsgPurposeCode("900000");
								}
							}
							if (mmId != null)
							{
								canonicalData.setOrderingCustomerId(mmId);
							}
							if (acCity != null && acState!=null && acCtry!=null)
							{
								canonicalData.setOrderingCustomerAddress(acCity + "\r\n" + acState + "\r\n" + acCtry);
							}
							canonicalData.setBeneficiaryCustomerCtctDtls(benfMobNo);
							canonicalData.setCustAccount(accNum);
							logger.info("BeneficiaryCustAcct ::"+canonicalData.getBeneficiaryCustAcct() +" IMPS_NBIN :: "+IMPS_NBIN);
							if (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ))
							{
								canonicalData.setBeneficiaryCustomerID("000");
								//canonicalData.setUltimateCreditorID(canonicalData.getBeneficiaryCustAcct().substring(0, 4) + "00100" + canonicalData.getBeneficiaryCustAcct().substring(canonicalData.getBeneficiaryCustAcct().length()-10, canonicalData.getBeneficiaryCustAcct().length()));
								canonicalData.setUltimateCreditorID(IMPS_NBIN + "00100" + canonicalData.getBeneficiaryCustAcct().substring(canonicalData.getBeneficiaryCustAcct().length()-10, canonicalData.getBeneficiaryCustAcct().length()));
							}
							else
							{
								canonicalData.setBeneficiaryCustomerID(canonicalData.getBeneficiaryCustAcct());
								//canonicalData.setUltimateCreditorID(canonicalData.getBeneficiaryCustAcct().substring(0, 4) + "00100" + benfMobNo);
								canonicalData.setUltimateCreditorID(IMPS_NBIN + "00100" + benfMobNo);
							}
						}
						else
						{
							canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0003, canonicalData.getDstMsgChnlType());
							logger.error("Outbound Payment. Account details could not be found for the account " + canonicalData.getOrderingCustAccount() + " and Mobile Number " + mobNo);
							EventLogger.logEvent("NGPHENRSVC0003", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Outbound Payment. Account details could not be found for the Account number {OrderingCustAccount} and Mobile Number {OrderingCustomerCtctDtls}
						}
					}
					else
					{
						canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0005, canonicalData.getDstMsgChnlType());
						logger.error("Outbound Payment. Default mobile number is not found for the account " + canonicalData.getOrderingCustAccount());
						EventLogger.logEvent("NGPHENRSVC0005", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Outbound Payment. Default mobile number is not found for the Account number {OrderingCustAccount}
					}
				}
			}
			logger.info("Src Message type :: "+canonicalData.getSrcMsgType()+" Src Message SubType ::" +canonicalData.getSrcMsgSubType()+ " Msg Branch :: "+canonicalData.getMsgBranch()+ " Message Direction ::"+canonicalData.getMsgDirection());
			List<Rules> messageRules = esbServiceDao.getRulesForSpecificMessage(canonicalData.getSrcMsgType(),canonicalData.getSrcMsgSubType(), canonicalData.getMsgBranch(),canonicalData.getMsgDept(),NgphEsbConstants.RULE_CATEGORY_ENRICH, canonicalData.getMsgDirection());
			
			if(messageRules != null && messageRules.size()>0)
			{
				logger.info("performEnrichService(...) Some rules found....");
				for(Rules oneRule : messageRules)
				{
					if (RulesEvaluationHelper.evaluateRuleCondition(oneRule.getRuleCondition(), canonicalData)) {
						logger.info("performEnrichService(...) Applicable Condition:"+oneRule.getRuleCondition());
						
						//setting applied/applicable rule-id to canonical
						if(StringUtils.isEmpty(canonicalData.getMsgRules()))
						{
							canonicalData.setMsgRules(oneRule.getRuleId());
						}else{
							canonicalData.setMsgRules(canonicalData.getMsgRules().concat(NgphEsbConstants.NGPH_COMMA));
							canonicalData.setMsgRules(canonicalData.getMsgRules().concat(oneRule.getRuleId()));
						}
						performRuleAction(oneRule, canonicalData);
					}
				}
			}
			//Once the branch is assigned, if it is inward payment then set the receiver bank and if it is outward payment then set the sender bank 
			//as per branch IFSC in case of NEFT / RTGS and branch BIC in case of SWIFT
			logger.info("DstMsgChnlType :: "+canonicalData.getDstMsgChnlType()+" and MsgChnlType() :: "+canonicalData.getMsgChnlType());
			if (canonicalData.getDstMsgChnlType()!=null && (canonicalData.getDstMsgChnlType().equalsIgnoreCase("NEFT") || canonicalData.getDstMsgChnlType().equalsIgnoreCase("RTGS")))
			{
				if (canonicalData.getMsgChnlType().equalsIgnoreCase("IMPS"))
				{
					if (canonicalData.getReturnReasonCode() == null)
					{
						canonicalData.setRelReference(null);
					}
					else
					{
						if (canonicalData.getRelCanonical() != null)
						{
							canonicalData.setRelReference(canonicalData.getRelCanonical().getTxnReference());
						}
					}
				}
				bnkID_R = props.getProperty(canonicalData.getDstMsgChnlType() + "BNKID");
				bnkID_S = props.getProperty(canonicalData.getMsgChnlType() + "BNKID");
				if (canonicalData.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.INWARD_PAYMENT))
				{
					if (bnkID_R.equalsIgnoreCase("ISO"))
					{
						canonicalData.setReceiverBank(esbServiceDao.getIsoCodeByBranch(canonicalData.getMsgBranch()));
						if (bnkID_S.equalsIgnoreCase("ISO"))
						{
							//if both sending and receiving format requires ISO code then do nothing
						}
						else if (bnkID_S.equalsIgnoreCase("IFSC"))
						{
							canonicalData.setSenderBank(esbServiceDao.getIsoPartyCode(canonicalData.getSenderBank()));
						}
						else if (bnkID_S.equalsIgnoreCase("BIC"))
						{
							canonicalData.setSenderBank(esbServiceDao.getIsoPartyCode(canonicalData.getSenderBank()));
						}
					}
					if (bnkID_R.equalsIgnoreCase("IFSC"))
					{
						canonicalData.setMsgBatchTime(canonicalData.getLastModTime());
						canonicalData.setReceiverBank(esbServiceDao.getIFSCByBranch(canonicalData.getMsgBranch()));
						if (bnkID_S.equalsIgnoreCase("ISO"))
						{
							if (canonicalData.getMsgChnlType().equalsIgnoreCase("IMPS"))
							{
								String sndrIFSC = null;
								String sndrBnkCode = null;
								
									sndrBnkCode = esbServiceDao.getIMPSMapBnkCode(canonicalData.getOrderingCustomerId().substring(0, 4));
									
								if (sndrBnkCode != null)
								{
									sndrIFSC = esbServiceDao.getBankFirstIFSC(sndrBnkCode);
									logger.info("sndrIFSC is :: "+sndrIFSC);
									if (sndrIFSC == null)
									{
										canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0012);//Invalid NBIN
									}
									else
									{
										canonicalData.setSenderBank(sndrIFSC);
									}
								}
								else
								{
									canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0012);//Invalid NBIN
								}
							}
							else
							{
								canonicalData.setSenderBank(esbServiceDao.getIsoPartyIFSC(canonicalData.getSenderBank()));
							}
						}
						else if (bnkID_S.equalsIgnoreCase("IFSC"))
						{
							//if both sending and receiving format requires IFSC / local clearing member code then do nothing
						}
						else if (bnkID_S.equalsIgnoreCase("BIC"))
						{
							canonicalData.setSenderBank(esbServiceDao.getIfscCodeByBic(canonicalData.getSenderBank()));
						}
					}
					if (bnkID_R.equalsIgnoreCase("BIC"))
					{
						canonicalData.setReceiverBank(esbServiceDao.getBICByBranch(canonicalData.getMsgBranch()));
						if (bnkID_S.equalsIgnoreCase("ISO"))
						{
							if (canonicalData.getMsgChnlType().equalsIgnoreCase("IMPS"))
							{
								String sndrIFSC = null;
								String sndrBnkCode = null;
								sndrBnkCode = esbServiceDao.getIMPSMapBnkCode(canonicalData.getOrderingCustomerId().substring(0, 4));
								if (sndrBnkCode != null)
								{
									sndrIFSC = esbServiceDao.getBankFirstIFSC(sndrBnkCode);
									if (sndrIFSC == null)
									{
										canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0012);//Invalid NBIN
									}
									else
									{
										canonicalData.setSenderBank(sndrIFSC);
									}
								}
								else
								{
									canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0012);//Invalid NBIN
								}
							}
							else
							{
								canonicalData.setSenderBank(esbServiceDao.getIsoPartyBIC(canonicalData.getSenderBank()));
							}
						}
						else if (bnkID_S.equalsIgnoreCase("IFSC"))
						{
							canonicalData.setSenderBank(esbServiceDao.getBICByIFSC(canonicalData.getSenderBank()));
						}
						else if (bnkID_S.equalsIgnoreCase("BIC"))
						{
							//if both sending and receiving format requires BIC then do nothing
						}
					}
				}
				if (canonicalData.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
				{
					if (bnkID_S.equalsIgnoreCase("ISO"))
					{
						logger.info("MsgBranch is :: "+canonicalData.getMsgBranch());
						canonicalData.setSenderBank(esbServiceDao.getIsoCodeByBranch(canonicalData.getMsgBranch()));
						if (bnkID_R.equalsIgnoreCase("ISO"))
						{
							//if both sending and receiving format requires ISO code then do nothing
						}
						else if (bnkID_R.equalsIgnoreCase("IFSC"))
						{
							logger.info("ReceiverBank() is :: "+canonicalData.getReceiverBank());
							canonicalData.setReceiverBank(esbServiceDao.getIsoPartyIFSC(canonicalData.getReceiverBank()));
							canonicalData.setMsgBatchTime(canonicalData.getLastModTime());
						}
						else if (bnkID_R.equalsIgnoreCase("BIC"))
						{
							canonicalData.setReceiverBank(esbServiceDao.getIsoPartyBIC(canonicalData.getReceiverBank()));
						}
					}
					if (bnkID_S.equalsIgnoreCase("IFSC"))
					{
						canonicalData.setReceiverBank(esbServiceDao.getIFSCByBranch(canonicalData.getMsgBranch()));
						if (bnkID_R.equalsIgnoreCase("ISO"))
						{
							canonicalData.setSenderBank(esbServiceDao.getIsoPartyCode(canonicalData.getSenderBank()));
						}
						else if (bnkID_R.equalsIgnoreCase("IFSC"))
						{
							canonicalData.setMsgBatchTime(canonicalData.getLastModTime());
							//if both sending and receiving format requires IFSC / local clearing member code then do nothing
						}
						else if (bnkID_R.equalsIgnoreCase("BIC"))
						{
							canonicalData.setSenderBank(esbServiceDao.getIfscCodeByBic(canonicalData.getSenderBank()));
						}
					}
					if (bnkID_S.equalsIgnoreCase("BIC"))
					{
						logger.info("MsgBranch is :: "+canonicalData.getMsgBranch()+" Sender Bank is ::"+canonicalData.getSenderBank());
						canonicalData.setReceiverBank(esbServiceDao.getBICByBranch(canonicalData.getMsgBranch()));
						if (bnkID_R.equalsIgnoreCase("ISO"))
						{
							canonicalData.setSenderBank(esbServiceDao.getIsoPartyCode(canonicalData.getSenderBank()));
						}
						else if (bnkID_R.equalsIgnoreCase("IFSC"))
						{
							canonicalData.setSenderBank(esbServiceDao.getBICByIFSC(canonicalData.getSenderBank()));
							canonicalData.setMsgBatchTime(canonicalData.getLastModTime());
						}
						else if (bnkID_R.equalsIgnoreCase("BIC"))
						{
							//if both sending and receiving format requires BIC then do nothing
						}
					}
				}
			}
			logger.info("performEnrichService(...) End....");
		}
		catch (Exception e) 
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0006, canonicalData.getDstMsgChnlType());
			EventLogger.logEvent("NGPHENRSVC0006", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Exception occured in enrichment rules application.
			logger.error("Error occurred in enrichment");
			logger.error(e, e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 
	 * @param oneRule
	 * @param canonicalData
	 */
	private void performRuleAction(Rules oneRule, NgphCanonical canonicalData) {
		logger.info("performRuleAction(...) Start....");
		RuleActionsEnum ruleActionsEnum = null;
		
		if(StringUtils.isNotEmpty(oneRule.getRuleAction()))
		{
			ruleActionsEnum = RuleActionsEnum.findRuleActionsEnumByName(oneRule.getRuleAction().trim());
			if(ruleActionsEnum != null)
			{
				String[] actParams = null;
				if(StringUtils.isNotEmpty(oneRule.getRuleActParam()) && oneRule.getRuleActParam().contains(NgphEsbConstants.SYMBOL_CARRET))
				{
					StringTokenizer st = new StringTokenizer(oneRule.getRuleActParam(), NgphEsbConstants.SYMBOL_CARRET);
					actParams = new String[st.countTokens()];
					int i = 0;
					while(st.hasMoreTokens()) 
					{
						actParams[i] = st.nextToken();
						i++;
					}
					//getting field's present value
					String fieldCurrentValue = fetchAttributeValue(actParams[0], canonicalData);
					String fieldNewValue = null;
					
					switch(ruleActionsEnum)
					{
						case PREFIX: 
							fieldNewValue = actParams[1].concat(fieldCurrentValue); 
							break;
						case SUFFIX: 
							fieldNewValue = fieldCurrentValue.concat(actParams[1]); 
							break;
						case INSERTAT: 
							fieldNewValue = fieldCurrentValue.substring(0,Integer.parseInt(actParams[1])-1).concat(actParams[2]).concat(fieldCurrentValue.substring(Integer.parseInt(actParams[1])-1));
							break;
						case REMOVEFIRST: 
							fieldNewValue = fieldCurrentValue.substring(Integer.parseInt(actParams[1])); 
							break;
						case REMOVELAST: 
							fieldNewValue = fieldCurrentValue.substring(0, fieldCurrentValue.length()-Integer.parseInt(actParams[1]));
							break;
						case REMOVEMIDDLE: 
							fieldNewValue = fieldCurrentValue.substring(0, Integer.parseInt(actParams[1])-1).concat(fieldCurrentValue.substring(Integer.parseInt(actParams[1])+Integer.parseInt(actParams[2])-1));
							break;
						case REPLACEIN:	
							fieldNewValue = fieldCurrentValue.substring(0, Integer.parseInt(actParams[1])-1).concat(actParams[3]).concat(fieldCurrentValue.substring(Integer.parseInt(actParams[1])+Integer.parseInt(actParams[2])-1));
							break;
						default :  
							EventLogger.logEvent("NGPHENRSVC0007", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Enrichment rule action parameter is invalid.
							canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0007);
							//setting the repair reason
							canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.INVALID_RULEACTPARAM_ENRICHMENT));
							break;				
					}
					
					//setting the new value to the attribute
					if(StringUtils.isNotEmpty(fieldNewValue))
					setAttributeValue(actParams[0], fieldNewValue, canonicalData);
					
				}
				else
				{
					EventLogger.logEvent("NGPHENRSVC0007", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Enrichment rule action parameter is invalid.
					canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0007);
					//setting the repair reason
					canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.INVALID_RULEACTPARAM_ENRICHMENT));
				}
			}else{
				EventLogger.logEvent("NGPHENRSVC0008", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Empty action parameter for the Enrichment rule.
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0008);
				//setting the repair reason
				canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.INVALID_RULEACTION_ENRICHMENT));
			}
		}else{
			EventLogger.logEvent("NGPHENRSVC0008", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Empty action parameter for the Enrichment rule.
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0008);
			//setting the repair reason
			canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.INVALID_RULEACTION_ENRICHMENT));
		}
		logger.info("performRuleAction(...) End....");
	}
	
	/**
	 * 
	 * @param attributeName
	 * @param attributeValue
	 * @param canonicalData
	 */
	private void setAttributeValue(String attributeName, String attributeValue, NgphCanonical canonicalData)
	{
		logger.info("setAttributeValue(...) Start....");
		if(StringUtils.isNotEmpty(attributeName))
		{
			boolean isAnyThingWrong = false;
			String firstLetter = String.valueOf(attributeName.charAt(NgphEsbConstants.NGPH_INT_ZERO));
			String methodName = "set".concat(attributeName.replaceFirst(firstLetter, firstLetter.toUpperCase()));

			@SuppressWarnings("rawtypes")
			Class[] types = new Class[1];
			types[0] = String.class;
			
			Method method = null;
			try {
				method = canonicalData.getClass().getMethod(methodName, types);
				method.invoke(canonicalData, new Object[]{attributeValue});
			} catch (SecurityException e) {
				logger.error(e, e);
				isAnyThingWrong = true;
			} catch (NoSuchMethodException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			} catch (IllegalArgumentException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			} catch (IllegalAccessException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			} catch (InvocationTargetException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			}finally{
				if(isAnyThingWrong)
				{
					EventLogger.logEvent("NGPHENRSVC0009", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Exception in setting enriched value to the payment data. Refer error log for details.
					canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0009);
					//setting the repair reason
					canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.FAIL_CANONICAL_VAL_SET));
				}
			}
		}
		logger.info("setAttributeValue(...) End....");
	}
	
	/**
	 * 
	 * @param attributeName
	 * @param canonicalData
	 * @return
	 */
	private String fetchAttributeValue(String attributeName, NgphCanonical canonicalData)
	{
		logger.info("fetchAttributeValue(...) Start....");
		String val = null;
		if(StringUtils.isNotEmpty(attributeName))
		{
			boolean isAnyThingWrong = false;
			String firstLetter = String.valueOf(attributeName.charAt(NgphEsbConstants.NGPH_INT_ZERO));
			String methodName = NgphEsbConstants.NGPH_GET_METHOD_PREFIX.concat(attributeName.replaceFirst(firstLetter, firstLetter.toUpperCase()));

			@SuppressWarnings("rawtypes")
			Class[] types = new Class[]{};
			
			Method method = null;
			try {
				
				method = canonicalData.getClass().getMethod(methodName, types);
				Object obj = method.invoke(canonicalData, new Object[0]);
				if(obj != null)
				{
					val = String.valueOf(obj);
				}
			} catch (SecurityException e) {
				logger.error(e, e);
				isAnyThingWrong = true;
			} catch (NoSuchMethodException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			} catch (IllegalArgumentException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			} catch (IllegalAccessException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			} catch (InvocationTargetException e) {
				isAnyThingWrong = true;
				logger.error(e, e);
			}finally{
				//TODO if fail to get new value shall we set message statur/msgerror status?
				if(isAnyThingWrong)
				{
					EventLogger.logEvent("NGPHENRSVC0010", canonicalData, EnrichServiceImpl.class, canonicalData.getMsgRef());//Exception in fetching value from the payment data for enrichment. Refer error log for details.
					canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ESE0010);
					//setting the repair reason
					canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.FAIL_CANONICAL_VAL_GET));
				}
			}
		}
		logger.info("fetchAttributeValue(...) End....");
		return val;
	}
}
