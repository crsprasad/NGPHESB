package com.logica.ngph.esb.servicesImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.dtos.ChargesDetailsDto;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.dtos.NgphGrpCanonical;
import com.logica.ngph.common.enums.RepairReasonsEnum;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.enums.ValidateServiceRuleCodeEnum;
import com.logica.ngph.esb.services.ValidateService;
import com.logica.ngph.utils.EventLogger;

public class ValidateServiceImpl implements ValidateService 
{
	static Logger logger = Logger.getLogger(ValidateServiceImpl.class);
	private EsbServiceDao esbServiceDao;
	private final String propName = "System.properties"; 
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	boolean IS_VALIDATION_FAIL = false;

	public void validateMessage(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception
	{
		logger.info("validateMessage(...) Start...");
		// get the configured list of validate service validation rules list
		List<String> validationsOrder = esbServiceDao.getValidateServiceRulesOrder(canonicalData.getSrcMsgType(), canonicalData.getSrcMsgSubType(), canonicalData.getMsgHost());
		if(validationsOrder != null && !validationsOrder.isEmpty())
		{
			logger.info("validateMessage(...) retrieved validationsOrder...");
			for (String vRule : validationsOrder) 
			{
				IS_VALIDATION_FAIL = false;
				// construct the enum of validateserviceruleCode
				ValidateServiceRuleCodeEnum vsr = ValidateServiceRuleCodeEnum.findValidateServiceRuleCodeEnumByCode(vRule);
				boolean isValidationPerformed = false;
				switch (vsr) 
				{
					case VSR0001:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkInstructedAgentRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0002:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkInstructingAgentRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0003:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkTotalInterbankSettlementAmountRule(ngphGrpCanonical,canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0004:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkTotalInterbankSettlementAmountAndSumRule(ngphGrpCanonical,canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0005:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkGroupHeaderInterbankSettlementDateRule(ngphGrpCanonical,canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0006:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkTransactionInterbankSettlementDateRule(ngphGrpCanonical,canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0007:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkPaymentTypeInformationRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0008:
						if(ngphGrpCanonical!=null && canonicalData!=null)
							checkNumberOfTransactionsAndCreditTransfersRule(ngphGrpCanonical, canonicalData);
						break;
					case VSR0009:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkTotalInterbankSettlementAmountAndDateRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0010:
						if(ngphGrpCanonical!=null && canonicalData!=null)
							checkThirdReimbursementAgentRule(ngphGrpCanonical,canonicalData);
						break;
					case VSR0011:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkSettlementMethodAgentRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0012:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkSettlementMethodCoverRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0013:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkSettlementMethodCoverAgentRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0014:
						if(ngphGrpCanonical!=null && canonicalData!=null)
							checkSettlementMethodClearingRule(ngphGrpCanonical, canonicalData);
						break;
					case VSR0015:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkInstructingReimbursementAgentAccountRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0016:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkInstructedReimbursementAgentAccountRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0017:
						if(ngphGrpCanonical!=null && canonicalData!=null)
						{
							checkThirdReimbursementAgentAccountRule(ngphGrpCanonical, canonicalData);
							isValidationPerformed = true;
						}
						break;
					case VSR0018:
						checkInstructedAmountAndExchangeRate1Rule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0019:
						checkInstructedAmountAndExchangeRate2Rule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0020:
						checkChargesInformationAndInstructedAmountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0021:
						checkChargesAmountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0022:
						checkChargeBearerAndChargesInformationRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0023:
						checkInstructionForCreditorAgentRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0024:
						checkIntermediaryAgent2Rule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0025:
						checkIntermediaryAgent3Rule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0026:
						checkIntermediaryAgent1AccountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0027:
						checkIntermediaryAgent2AccountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0028:
						checkIntermediaryAgent3AccountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0029:
						checkPreviousInstructingAgentAccountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0030:
						checkDebtorAgentAccountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0031:
						checkCreditorAgentAccountRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0032:
						checkInstructedAmountAndExchangeRate3Rule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0033:
						checkRemitterMobileRule(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0034:
						checkAcTypeAllowed(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0035:
						checkBenAcLimit(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0036:
						checkMemberCrLimit(canonicalData);
						isValidationPerformed = true;
						break;
					case VSR0037:
						if (canonicalData.getSenderBank().equalsIgnoreCase(canonicalData.getReceiverBank()))
						{
							canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0032);//Sender and Receiver bank codes cannot be same
							IS_VALIDATION_FAIL = true;
						}
						isValidationPerformed = true;
						break;
					case VSR0038:
						if (IsTxnTypeAllowedForAccount(canonicalData) == false)
						{
							IS_VALIDATION_FAIL = true;
						}
						isValidationPerformed = true;
						break;
					default:
						break;
				}
				//if validation error occurs then break the loop
				if (IS_VALIDATION_FAIL) 
				{
					EventLogger.logEvent("NGPHVALSVC0002", canonicalData, ValidateServiceImpl.class, canonicalData.getMsgRef());//Validation failed for the payment, moved to repair queue.
					canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.VALIDATION_FAIL));
					logger.error("Validation fail for..."+"ValidationRule:"+vRule+",Error/Warning Code:"+canonicalData.getMsgErrorCode());
					break;
				}
				else if(isValidationPerformed)
				{
					//Logging the AuditEvent for success of rule validation with validation rule code
					EventLogger.logEvent("NGPHVALSVC0001", canonicalData, ValidateServiceImpl.class, canonicalData.getMsgRef());//Payments validated successfully.
					logger.info("-validateMessage() Validation success for..."+"ValidationRule:"+vRule);
				}
			}
		}
		else
		{
			//Logging the AuditEvent saying no configured rules found.
			EventLogger.logEvent("NGPHVALSVC0003", canonicalData, ValidateServiceImpl.class, canonicalData.getMsgRef());//No applicable validations for the payment.
		}
		logger.info("validateMessage(...) End...");
	}
	
	
	private void checkRemitterMobileRule(NgphCanonical canonicalData) throws Exception
	{
		logger.info("checkRemitterMobileRule Starts");
		String mobNo = esbServiceDao.getMobNo(canonicalData.getOrderingCustAccount());
		
		if(mobNo!=null)
		{
			canonicalData.setOrderingCustomerCtctDtls(mobNo);
		}
		else
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0026);
			IS_VALIDATION_FAIL = true;
			logger.info("checkRemitterMobileRule Validation failed");
		}
		logger.info("checkRemitterMobileRule Ends");
	}

	private void checkInstructedAmountAndExchangeRate3Rule(NgphCanonical canonicalData)throws Exception 
	{
		logger.info("checkInstructedAmountAndExchangeRate3Rule Starts");

		//If instructedAmount > 0 then xchangeRate should be empty or 0.
		if(canonicalData.getInstructedAmount() != null && canonicalData.getInstructedAmount().compareTo(NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL)== NgphEsbConstants.CONSTANT_VALUE_INT_ONE )
		{
			if(canonicalData.getXchangeRate() != null && canonicalData.getXchangeRate().compareTo(NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL)!= NgphEsbConstants.CONSTANT_VALUE_INT_ONE )
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0016);
				IS_VALIDATION_FAIL = true;
				logger.info("checkInstructedAmountAndExchangeRate3Rule Validation Failed");
			}
		}
		logger.info("checkInstructedAmountAndExchangeRate3Rule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkCreditorAgentAccountRule(NgphCanonical canonicalData) throws Exception{
		//If accountWithInstitutionAcct is present 
		//then accountWithInstitution should not be empty.
		logger.info("checkCreditorAgentAccountRule Starts");
		if(StringUtils.isNotEmpty(canonicalData.getAccountWithInstitutionAcct()) && StringUtils.isEmpty(canonicalData.getAccountWithInstitution()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0023);
			IS_VALIDATION_FAIL = true;
			logger.info("checkCreditorAgentAccountRule Validation Failed");
		}
		logger.info("checkCreditorAgentAccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkDebtorAgentAccountRule(NgphCanonical canonicalData) throws Exception{

		logger.info("checkDebtorAgentAccountRule Starts");

		//If orderingInstitutionAcct is present then orderingInstitution should not be empty.
		if(StringUtils.isNotEmpty(canonicalData.getOrderingInstitutionAcct()) && StringUtils.isEmpty(canonicalData.getOrderingInstitution()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0022);
			IS_VALIDATION_FAIL = true;
			logger.info("checkDebtorAgentAccountRule Validation Failed");
		}
		logger.info("checkDebtorAgentAccountRule Ends");

	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkPreviousInstructingAgentAccountRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkPreviousInstructingAgentAccountRule Starts");

		//If prevInstructingAgentAcct is present, then prevInstructingBank should not be empty.
		if(StringUtils.isNotEmpty(canonicalData.getPrevInstructingAgentAcct()) && StringUtils.isEmpty(canonicalData.getPrevInstructingBank()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0021);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPreviousInstructingAgentAccountRule Validation Failed");
		}
		logger.info("checkPreviousInstructingAgentAccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkIntermediaryAgent3AccountRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkIntermediaryAgent3AccountRule Starts");

		//If intermediary3AgentAcct is present, then intermediary3Bank should not be empty.
		if(StringUtils.isNotEmpty(canonicalData.getIntermediary3AgentAcct()) && StringUtils.isEmpty(canonicalData.getIntermediary3Bank()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0020);
			IS_VALIDATION_FAIL = true;
			logger.info("checkIntermediaryAgent3AccountRule Validation Failed");
		}
		logger.info("checkIntermediaryAgent3AccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkIntermediaryAgent2AccountRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkIntermediaryAgent2AccountRule Starts");

		//If intermediary2AgentAcct is present, then intermediary2Bank should not be empty.
		if(StringUtils.isNotEmpty(canonicalData.getIntermediary2AgentAcct()) && StringUtils.isEmpty(canonicalData.getIntermediary2Bank()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0019);
			IS_VALIDATION_FAIL = true;
			logger.info("checkIntermediaryAgent2AccountRule Validation Failed");
		}
		logger.info("checkIntermediaryAgent2AccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkIntermediaryAgent1AccountRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkIntermediaryAgent1AccountRule Starts");

		//If intermediary1AgentAcct is present, then intermediary1Bank should not be empty.
		if(StringUtils.isNotEmpty(canonicalData.getIntermediary1AgentAcct()) && StringUtils.isEmpty(canonicalData.getIntermediary1Bank()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0003);
			IS_VALIDATION_FAIL = true;
			logger.info("checkIntermediaryAgent1AccountRule Validation failed");
		}
		logger.info("checkIntermediaryAgent1AccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkIntermediaryAgent3Rule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkIntermediaryAgent3Rule starts");

		//If intermediary3Bank is present, then intermediary2Bank should not be empty.
		if(StringUtils.isNotEmpty(canonicalData.getIntermediary3Bank()) && StringUtils.isEmpty(canonicalData.getIntermediary2Bank()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0025);
			IS_VALIDATION_FAIL = true;
			logger.info("checkIntermediaryAgent3Rule Validation Failed");
		}
		logger.info("checkIntermediaryAgent3Rule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkIntermediaryAgent2Rule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkIntermediaryAgent2Rule Starts");

		//If intermediary2Bank is present, then intermediary1Bank should not be empty.
		if(StringUtils.isNotEmpty(canonicalData.getIntermediary2Bank()) && StringUtils.isEmpty(canonicalData.getIntermediary1Bank()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0024);
			IS_VALIDATION_FAIL = true;
			logger.info("checkIntermediaryAgent2Rule Validation Failed");
		}
		logger.info("checkIntermediaryAgent2Rule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkInstructionForCreditorAgentRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkInstructionForCreditorAgentRule Starts");

		//If instructionsForCrdtrAgtCode contains CHQB, then beneficiaryCustAcct
		//should be empty.
		if(NgphEsbConstants.CREDITOR_AGENT_CODE_CHQB.equalsIgnoreCase(canonicalData.getInstructionsForCrdtrAgtCode()))
		{
			if(StringUtils.isEmpty(canonicalData.getBeneficiaryCustAcct()))
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0015);
				IS_VALIDATION_FAIL = true;
				logger.info("checkInstructionForCreditorAgentRule Validation Failed");
			}
		}
		logger.info("checkInstructionForCreditorAgentRule Ends");
	}

	/*
	 * 
	 * 
	 */
	private void checkChargeBearerAndChargesInformationRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkChargeBearerAndChargesInformationRule Starts");

		//If chargeBearer contains DEBT, then the count of charge details array should
		//be more than 1.
		if(NgphEsbConstants.CHARGE_BEARER_DEBT.equalsIgnoreCase(canonicalData.getChargeBearer()))
		{
			if(canonicalData.getChargesDetails() == null || canonicalData.getChargesDetails().size() <= NgphEsbConstants.CONSTANT_VALUE_INT_ONE)
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0017);
				IS_VALIDATION_FAIL = true;
				logger.info("checkChargeBearerAndChargesInformationRule Validation Failed");
				return;
			}
		}
		
		//If chargeBearer contains CRED, then the array of charge details should not be empty.
		if(NgphEsbConstants.CHARGE_BEARER_CRED.equalsIgnoreCase(canonicalData.getChargeBearer()))
		{
			if(canonicalData.getChargesDetails() == null)
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0018);
				IS_VALIDATION_FAIL = true;
				logger.info("checkChargeBearerAndChargesInformationRule Validation Failed");
			}
		}
		logger.info("checkChargeBearerAndChargesInformationRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkChargesAmountRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkChargesAmountRule Starts");

		//If chargeBearer is present, then chargeCurrency must be equal to msgCurrency.
		if(StringUtils.isNotEmpty(canonicalData.getChargeBearer()))
		{
			if(canonicalData.getChargesDetails()!=null && !canonicalData.getChargesDetails().isEmpty())
			{
				for(ChargesDetailsDto chargesDetailsDto: canonicalData.getChargesDetails())
				{
					if(StringUtils.isEmpty(chargesDetailsDto.getChargeCurrency()) || !chargesDetailsDto.getChargeCurrency().equalsIgnoreCase(canonicalData.getMsgCurrency()))
					{
						canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0016);
						IS_VALIDATION_FAIL = true;
						logger.info("checkChargesAmountRule Validation Failed");
						break;
					}
				}
			}
		}
		logger.info("checkChargesAmountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkChargesInformationAndInstructedAmountRule(NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkChargesInformationAndInstructedAmountRule Starts");

		//If chargeBearer is present, then instructedAmount must be present
		if(StringUtils.isNotEmpty(canonicalData.getChargeBearer()))
		{
			if(canonicalData.getInstructedAmount()== null || NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL == canonicalData.getInstructedAmount())
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0015);
				IS_VALIDATION_FAIL = true;
				logger.info("checkChargesInformationAndInstructedAmountRule Validation Failed");
			}
		}
		logger.info("checkChargesInformationAndInstructedAmountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkInstructedAmountAndExchangeRate2Rule(	NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkInstructedAmountAndExchangeRate2Rule Starts");

		//If instructedAmount is present and the instructedCurrency is equal to
		//msgCurrency, then xchangeRate should be empty.
		if(canonicalData.getInstructedAmount()!= null && NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL != canonicalData.getInstructedAmount())
		{
			if(StringUtils.isNotEmpty(canonicalData.getInstructedCurrency()) && StringUtils.isNotEmpty(canonicalData.getMsgCurrency()) && canonicalData.getInstructedCurrency().equalsIgnoreCase(canonicalData.getMsgCurrency()))
			{
				if(canonicalData.getXchangeRate()!= null)
				{
					if(NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL != canonicalData.getXchangeRate())
					{
						canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0014);
						IS_VALIDATION_FAIL = true;
						logger.info("checkInstructedAmountAndExchangeRate2Rule Validation Failed");
					}
				}
			}
		}
		logger.info("checkInstructedAmountAndExchangeRate2Rule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkInstructedAmountAndExchangeRate1Rule(	NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkInstructedAmountAndExchangeRate1Rule Starts");

		//If instructedAmount is present and the instructedCurrency is different 
		//from msgCurrency, then xchangeRate must not be empty.
		if(canonicalData.getInstructedAmount()!= null && NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL != canonicalData.getInstructedAmount())
		{
			if(StringUtils.isNotEmpty(canonicalData.getInstructedCurrency()) && StringUtils.isNotEmpty(canonicalData.getMsgCurrency()) && !canonicalData.getInstructedCurrency().equalsIgnoreCase(canonicalData.getMsgCurrency()))
			{
				if(canonicalData.getXchangeRate()== null || NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL == canonicalData.getXchangeRate())
				{
					canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0013);
					IS_VALIDATION_FAIL = true;
					logger.info("checkInstructedAmountAndExchangeRate1Rule Validation Failed");
				}
			}
		}
		logger.info("checkInstructedAmountAndExchangeRate1Rule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkThirdReimbursementAgentAccountRule(NgphGrpCanonical ngphGrpCanonical,	NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkThirdReimbursementAgentAccountRule Starts");

		//If grpThirdRmbrAgentAc is present, then grpThirdRmbrAgent must be present.
		if(StringUtils.isNotEmpty(ngphGrpCanonical.getGrpThirdRmbrAgentAc()) && StringUtils.isEmpty(ngphGrpCanonical.getGrpThirdRmbrAgent()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0012);
			IS_VALIDATION_FAIL = true;
			logger.info("checkThirdReimbursementAgentAccountRule Validation Failed");
		}
		logger.info("checkThirdReimbursementAgentAccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkInstructedReimbursementAgentAccountRule(NgphGrpCanonical ngphGrpCanonical,NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkInstructedReimbursementAgentAccountRule Starts");

		//If grpInstructedRmbrAgentAc is present, then grpInstructedRmbrAgent must be present.
		if(StringUtils.isNotEmpty(ngphGrpCanonical.getGrpInstructedRmbrAgentAc()) && StringUtils.isEmpty(ngphGrpCanonical.getGrpInstructedRmbrAgent()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0011);
			IS_VALIDATION_FAIL = true;
			logger.info("checkInstructedReimbursementAgentAccountRule Validation Failed");
		}
		logger.info("checkInstructedReimbursementAgentAccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkInstructingReimbursementAgentAccountRule(NgphGrpCanonical ngphGrpCanonical,NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkInstructingReimbursementAgentAccountRule Starts");

		//If grpInstructingRmbrAgentAc is present, then grpInstructingRmbrAgent must be present.
		if(StringUtils.isNotEmpty(ngphGrpCanonical.getGrpInstructingRmbrAgentAc()) && StringUtils.isEmpty(ngphGrpCanonical.getGrpInstructingRmbrAgent()))
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0010);
			IS_VALIDATION_FAIL = true;
			logger.info("checkInstructingReimbursementAgentAccountRule Validation Failed");
		}
		logger.info("checkInstructingReimbursementAgentAccountRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkSettlementMethodClearingRule(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkSettlementMethodClearingRule Starts");

		//If grpSettleMethod is equal to CLRG, then grpSettleAccount and grpInstructingRmbrAgent
		//and grpInstructedRmbrAgent  and grpThirdRmbrAgent should be empty.
		if(NgphEsbConstants.SETTLEMENT_METHOD_CLRG.equalsIgnoreCase(ngphGrpCanonical.getGrpSettleMethod()))
		{
			if(StringUtils.isNotEmpty(ngphGrpCanonical.getGrpSettleAccount()) || StringUtils.isNotEmpty(ngphGrpCanonical.getGrpInstructingRmbrAgent()) || StringUtils.isNotEmpty(ngphGrpCanonical.getGrpInstructedRmbrAgent()) || StringUtils.isNotEmpty(ngphGrpCanonical.getGrpThirdRmbrAgent()))
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0014);
				IS_VALIDATION_FAIL = true;
				logger.info("checkSettlementMethodClearingRule Validation Failed");
			}
		}
		logger.info("checkSettlementMethodClearingRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkSettlementMethodCoverAgentRule(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkSettlementMethodCoverAgentRule Starts");

		//If grpSettleMethod is equal to COVE, then grpInstructingRmbrAgent or 
		//grpInstructedRmbrAgent must be present.
		if(NgphEsbConstants.SETTLEMENT_METHOD_COVE.equalsIgnoreCase(ngphGrpCanonical.getGrpSettleMethod()))
		{
			if(StringUtils.isEmpty(ngphGrpCanonical.getGrpInstructedRmbrAgent())|| StringUtils.isEmpty(ngphGrpCanonical.getGrpInstructingRmbrAgent()))
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0009);
				IS_VALIDATION_FAIL = true;
				logger.info("checkSettlementMethodCoverAgentRule Validation Failed");
			}
		}
		logger.info("checkSettlementMethodCoverAgentRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkSettlementMethodCoverRule(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkSettlementMethodCoverRule Starts");

		//If grpSettleMethod is COVE, then grpSettleAccount and grpClearingSys are not allowed.
		if(NgphEsbConstants.SETTLEMENT_METHOD_COVE.equalsIgnoreCase(ngphGrpCanonical.getGrpSettleMethod()))
		{
			if(StringUtils.isNotEmpty(ngphGrpCanonical.getGrpSettleAccount())|| StringUtils.isNotEmpty(ngphGrpCanonical.getGrpClearingSys()))
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0013);
				IS_VALIDATION_FAIL = true;
				logger.info("checkSettlementMethodCoverRule Validation Failed");
			}
		}
		logger.info("checkSettlementMethodCoverRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkSettlementMethodAgentRule(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkSettlementMethodAgentRule Starts");

		// If grpSettleMethod is INDA or INGA then grpClearingSys and
		// grpInstructingRmbrAgent
		// and grpInstructedRmbrAgent and grpThirdRmbrAgent should be empty.
		if (NgphEsbConstants.SETTLEMENT_METHOD_INDA
				.equalsIgnoreCase(ngphGrpCanonical.getGrpSettleMethod())
				|| NgphEsbConstants.SETTLEMENT_METHOD_INGA
						.equalsIgnoreCase(ngphGrpCanonical.getGrpSettleMethod())) {
			if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpClearingSys())
					|| StringUtils.isNotEmpty(ngphGrpCanonical
							.getGrpInstructingRmbrAgent())
					|| StringUtils.isNotEmpty(ngphGrpCanonical
							.getGrpInstructedRmbrAgent())
					|| StringUtils.isNotEmpty(ngphGrpCanonical
							.getGrpThirdRmbrAgent())) {
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0012);
				IS_VALIDATION_FAIL = true;
				logger.info("checkSettlementMethodAgentRule Validation Failed");
			}
		}
		logger.info("checkSettlementMethodAgentRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkThirdReimbursementAgentRule(
			NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkThirdReimbursementAgentRule Starts");

		// If grpThirdRmbrAgent is present then
		// grpInstructingRmbrAgent and grpInstructedRmbrAgent should not be
		// empty.
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpThirdRmbrAgent())) {
			if (StringUtils.isEmpty(ngphGrpCanonical
					.getGrpInstructingRmbrAgent())
					|| StringUtils.isEmpty(ngphGrpCanonical
							.getGrpInstructedRmbrAgent())) {
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0008);
				IS_VALIDATION_FAIL = true;
				logger.info("checkThirdReimbursementAgentRule Validation Failed");
			}
		}
		logger.info("checkThirdReimbursementAgentRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkTotalInterbankSettlementAmountAndDateRule(
			NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkTotalInterbankSettlementAmountAndDateRule Starts");

		// If grpCurrency is present then grpTotalAmount and grpDate should also
		// be present.
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpCurrency())) {
			if (ngphGrpCanonical.getGrpTotalAmount() == null
					|| ngphGrpCanonical.getGrpDate() == null
					|| NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL == ngphGrpCanonical
							.getGrpTotalAmount()) {
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0006);
				IS_VALIDATION_FAIL = true;
				logger.info("checkTotalInterbankSettlementAmountAndDateRule Validation Failed");
				return;
			}
		}
		// If grpTotalAmount is present then grpCurrency and grpDate should also
		// be present
		if (ngphGrpCanonical.getGrpTotalAmount() != null
				&& NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL != ngphGrpCanonical
						.getGrpTotalAmount()) {
			if (ngphGrpCanonical.getGrpDate() == null
					|| StringUtils.isEmpty(ngphGrpCanonical.getGrpCurrency())) {
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0007);
				IS_VALIDATION_FAIL = true;
				logger.info("checkTotalInterbankSettlementAmountAndDateRule Validation Failed");
			}
		}
		logger.info("checkTotalInterbankSettlementAmountAndDateRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkNumberOfTransactionsAndCreditTransfersRule(NgphGrpCanonical ngphGrpCanonical,
			NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkNumberOfTransactionsAndCreditTransfersRule Starts");

		//If the number of collection objects is not equal to grpNoOfTxns then throw error.
		int paysCount = esbServiceDao.getPaymentsCountOfGroupPayment(canonicalData.getGrpMsgId());
		if(ngphGrpCanonical.getGrpNoOfTxns() != paysCount)
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0005);
			IS_VALIDATION_FAIL = true;
			logger.info("checkNumberOfTransactionsAndCreditTransfersRule Validation Failed");
		}
		logger.info("checkNumberOfTransactionsAndCreditTransfersRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkPaymentTypeInformationRule(
			NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{
		
		logger.info("checkPaymentTypeInformationRule Starts");

		// If grpInstructionPriority is present then sndrPymtPriority should not
		// be present
		if (StringUtils
				.isNotEmpty(ngphGrpCanonical.getGrpInstructionPriority())
				&& StringUtils.isNotEmpty(canonicalData.getSndrPymtPriority())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0004);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
			return;
		}
		// If grpClearingChannel is present then clrgChannel should not be
		// present
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpClearingChannel())
				&& StringUtils.isNotEmpty(canonicalData.getClrgChannel())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0005);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
			return;
		}
		// If grpSvcLevelCode is present then svcLevelCode should not be present
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpSvcLevelCode())
				&& StringUtils.isNotEmpty(canonicalData.getSvcLevelCode())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0006);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
			return;
		}
		// If grpSvcLevelProperitary is present then svcLevelProperitary should
		// not be present
		if (StringUtils
				.isNotEmpty(ngphGrpCanonical.getGrpSvcLevelProperitary())
				&& StringUtils.isNotEmpty(canonicalData
						.getSvcLevelProperitary())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0007);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
			return;
		}
		// If grpLocalInstCode is present then lclInstCode should not be present
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpLocalInstCode())
				&& StringUtils.isNotEmpty(canonicalData.getLclInstCode())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0008);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
			return;
		}
		// If grpLocalInstProperitary is present then lclInstProperitary should
		// not be present
		if (StringUtils.isNotEmpty(ngphGrpCanonical
				.getGrpLocalInstProperitary())
				&& StringUtils
						.isNotEmpty(canonicalData.getLclInstProperitary())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0009);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
			return;
		}
		// If grpCatgPurposeCode is present then catgPurposeCode should not be
		// present
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpCatgPurposeCode())
				&& StringUtils.isNotEmpty(canonicalData.getCatgPurposeCode())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0010);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
			return;
		}
		// If grpCatgPurposeProperitary is present then catgPurposeProperitary
		// should not be present
		if (StringUtils.isNotEmpty(ngphGrpCanonical
				.getGrpCatgPurposeProperitary())
				&& StringUtils.isNotEmpty(canonicalData
						.getCatgPurposeProperitary())) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0011);
			IS_VALIDATION_FAIL = true;
			logger.info("checkPaymentTypeInformationRule Validation Failed");
		}
		logger.info("checkPaymentTypeInformationRule Ends");
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private void checkTransactionInterbankSettlementDateRule(
			NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception{

		logger.info("checkTransactionInterbankSettlementDateRule Starts");

		// If grpDate is empty then msgValueDate in all objects in the
		// collection should be present
		if (ngphGrpCanonical.getGrpDate() == null
				&& canonicalData.getMsgValueDate() == null) {
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0004);
			IS_VALIDATION_FAIL = true;
			logger.info("checkTransactionInterbankSettlementDateRule Validation Failed");
		}
		logger.info("checkTransactionInterbankSettlementDateRule Ends");
	}
	private void checkGroupHeaderInterbankSettlementDateRule(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData)throws Exception 
	{
		logger.info("checkGroupHeaderInterbankSettlementDateRule Starts");

		// If grpDate is present then msgValueDate should not be present
		if (ngphGrpCanonical.getGrpDate() != null && canonicalData.getMsgValueDate() != null) 
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0003);
			IS_VALIDATION_FAIL = true;
			logger.info("checkGroupHeaderInterbankSettlementDateRule Validation Failed");
		}
		logger.info("checkGroupHeaderInterbankSettlementDateRule Ends");
	}
	private void checkTotalInterbankSettlementAmountAndSumRule(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception
	{
		logger.info("checkTotalInterbankSettlementAmountAndSumRule Starts");

		// If grpTotalAmount is not equal to sum of msgAmount of all objects in the collection
		if (ngphGrpCanonical.getGrpTotalAmount() != null && canonicalData.getMsgAmount() != null && NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL != canonicalData.getMsgAmount()	&& NgphEsbConstants.BIGDECIMAL_DEFAULT_VAL != ngphGrpCanonical.getGrpTotalAmount()) 
		{
			if (ngphGrpCanonical.getGrpTotalAmount() != canonicalData.getMsgAmount()) 
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0002);
				IS_VALIDATION_FAIL = true;
				logger.info("checkTotalInterbankSettlementAmountAndSumRule Validation Failed");
			}
		}
		logger.info("checkTotalInterbankSettlementAmountAndSumRule Ends");
	}
	private void checkTotalInterbankSettlementAmountRule(NgphGrpCanonical ngphGrpCanonical, NgphCanonical canonicalData) throws Exception
	{
		logger.info("checkTotalInterbankSettlementAmountRule Starts");

		// If msgCurrency of any of the NGPHCanonical objects in the collection is not equal to grpCurrency
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpCurrency()) && StringUtils.isNotEmpty(canonicalData.getMsgCurrency())) 
		{
			if (ngphGrpCanonical.getGrpCurrency().trim().equalsIgnoreCase(canonicalData.getMsgCurrency().trim())) 
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0001);
				IS_VALIDATION_FAIL = true;
				logger.info("checkTotalInterbankSettlementAmountRule Validation Failed");
			}
		} 
		else 
		{
			// TODO put error code for invalid message currency
			canonicalData.setMsgErrorCode("");
			IS_VALIDATION_FAIL = true;
			logger.info("checkTotalInterbankSettlementAmountRule Validation Failed");
		}
		logger.info("checkTotalInterbankSettlementAmountRule Ends");
	}
	private void checkInstructingAgentRule(NgphGrpCanonical ngphGrpCanonical,NgphCanonical canonicalData) throws Exception
	{
		logDebuggers("checkInstructingAgentRule(...) Start...");
		// If grpInstructingAgent of NGPHGrpCanonical is present then senderBank of NGPHCanonical should not be present
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpInstructingAgent()) && StringUtils.isNotEmpty(canonicalData.getSenderBank())) 
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0002);
			IS_VALIDATION_FAIL = true;
			logDebuggers("checkInstructingAgentRule(...) Validation Fail...");
		}
		logDebuggers("checkInstructingAgentRule(...) End...");
	}
	private void checkInstructedAgentRule(NgphGrpCanonical ngphGrpCanonical,NgphCanonical canonicalData) throws Exception
	{
		logDebuggers("checkInstructedAgentRule(...) Start...");
		// If grpInstructedAgent is present then receiverBank should not be
		// present
		if (StringUtils.isNotEmpty(ngphGrpCanonical.getGrpInstructedAgent()) && StringUtils.isNotEmpty(canonicalData.getReceiverBank())) 
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSW0001);
			IS_VALIDATION_FAIL = true;
			logDebuggers("checkInstructedAgentRule(...) Validation Fail...");
		}
		logDebuggers("checkInstructedAgentRule(...) Start...");
	}
	
	private void checkAcTypeAllowed (NgphCanonical canonicalData)throws Exception
	{
		logger.info("Check accountype START");
		Properties props = new Properties(); 
		final String acTypesConfig = "_RESACTYPES"; 
		try
		{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			String acTypes = props.getProperty(canonicalData.getMsgChnlType()+ acTypesConfig);
			
			ArrayList<String> acTypeArr = new ArrayList<String>();
			if(acTypes.contains(";"))
			{
				acTypeArr.addAll(Arrays.asList(acTypes.split(";"))); 
			}
			else
			{
				acTypeArr.add(acTypes);
			}
			String chkAcType = null;
			logger.info("The beneficiary account type being checked is : " + canonicalData.getBeneficiaryAcType());
			for(int i=0;i<acTypeArr.size();i++)
			{
				chkAcType = acTypeArr.get(i);
				if (canonicalData.getBeneficiaryAcType().equalsIgnoreCase(chkAcType))
				{
					canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0027);//Account type not allowed for this channel
					IS_VALIDATION_FAIL = true;
					logger.info("checkAcTypeAllowed Validation Failed");
					break;
				}
			}
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
		logger.info("Check accountype END");
	}
	
	private void checkBenAcLimit (NgphCanonical canonicalData)throws Exception
	{
		logger.info("checkBenAcLimit START");
		String identifier = canonicalData.getMsgBranch() + canonicalData.getBeneficiaryCustAcct();
		BigDecimal acLimit = null;
		acLimit = esbServiceDao.getAvailableCrLimit(identifier);
		if (acLimit != null)
		{
			logger.info("**** The fetched limit is " + acLimit.toString());
			logger.info("**** The message amount is " + canonicalData.getMsgAmount().toString());
			double diff = acLimit.doubleValue() - canonicalData.getMsgAmount().doubleValue();
			if (diff < 0.00)
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0028);//Daily allowed amount limit for the beneficiary is exceeded
				IS_VALIDATION_FAIL = true;
				logger.info("checkBenAcLimit Validation Failed");
			}
		}
		else
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0031);//Limit not available
			IS_VALIDATION_FAIL = true;
			logger.info("checkBenAcLimit Validation Failed");
		}
		logger.info("checkBenAcLimit END");
		acLimit = null;
	}
	
	private void checkMemberCrLimit(NgphCanonical canonicalData)throws Exception
	{
		logger.info("checkMemberCrLimit START");
		String identifier = esbServiceDao.getIMPSMapBnkCode(canonicalData.getOrderingCustomerId().substring(0, 4));
		if (identifier != null)
		{
			BigDecimal mmbrLimit = esbServiceDao.getAvailableCrLimit(identifier);
			if (mmbrLimit == null)
			{
				mmbrLimit = new BigDecimal(0);
			}
			logger.info("**** The ordering customer availablecredit limit is " + mmbrLimit.toString());
			logger.info("**** The message amount is " + canonicalData.getMsgAmount().toString());
			BigDecimal diff = mmbrLimit.subtract(canonicalData.getMsgAmount());
			BigDecimal zero = new BigDecimal(0);
			if (diff.compareTo(zero) < 0)
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0029);//Member limit exceeded
				IS_VALIDATION_FAIL = true;
				logger.info("checkMemberCrLimit Validation Failed");
			}
		}
		else
		{
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0030);//Invalid Remitter NBIN
			IS_VALIDATION_FAIL = true;
			logger.info("checkMemberCrLimit Validation Failed");
		}
		logger.info("checkMemberCrLimit END");
	}
	
	private boolean IsTxnTypeAllowedForAccount(NgphCanonical canonicalData)throws Exception
	{
		logger.info("IsTxnTypeAllowedForAccount START");
		if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER)))
		{
			if (canonicalData.getBeneficiaryAcType().equalsIgnoreCase(NgphEsbConstants.ACCOUNT_TYPE_CURRENT))
			{
				return true;
			}
			else
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0033);//P2M payments are allowed only for current account
				return false;
			}
		}
		else if (canonicalData.getMsgTxnType()!= null && (canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || canonicalData.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER)))
		{
			if (canonicalData.getBeneficiaryAcType().equalsIgnoreCase(NgphEsbConstants.ACCOUNT_TYPE_SAVINGS))
			{
				return true;
			}
			else
			{
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_VSE0034);//P2P payments are allowed only for savings account
				return false;
			}
		}
		logger.info("IsTxnTypeAllowedForAccount END");
		return true;
	}
	
	private void logDebuggers(String debugInfo)
	{		
        if(logger.isDebugEnabled())
        {
              logger.debug(debugInfo);
        }
	}	
}
