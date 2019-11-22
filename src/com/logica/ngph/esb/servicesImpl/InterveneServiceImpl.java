package com.logica.ngph.esb.servicesImpl;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.common.enums.RepairReasonsEnum;
import com.logica.ngph.common.utils.RulesEvaluationHelper;
import com.logica.ngph.esb.Dtos.Rules;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.enums.RuleActionsEnum;
import com.logica.ngph.esb.services.InterveneService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;

/**
 * @author guptarb
 *
 */
public class InterveneServiceImpl implements InterveneService 
{
	static Logger logger = Logger.getLogger(InterveneServiceImpl.class);	
	private EsbServiceDao esbServiceDao;
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	private Rules traceMessageRoute(NgphCanonical canonicalData,List<Rules> messageRules) throws Exception
	{
		logger.info("traceMessageRoute() of Intervene Start....");
		Rules applicableRule = null;	
		for (Rules rule : messageRules) 
		{
			logger.info("traceMessageRoute() calling RulesEvaluationHelper.evaluateRuleCondition()....");
			if (RulesEvaluationHelper.evaluateRuleCondition(rule.getRuleCondition(), canonicalData)) 
			{
				logger.info("traceMessageRoute() calling Rule satisfied...." + rule.getRuleId());
				applicableRule = rule;
				break;
			}
			logger.info("traceMessageRoute() calling Rule not satisfied....");
		}
		logger.info("traceMessageRoute() of Intervene Ends....");
		return applicableRule;
	}
	// main Execution Point
	public void performRouting(NgphCanonical canonicalData)throws Exception
	{
		try
		{
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			logger.info("performRouting() of Intervene Start....");
			List<Rules> messageRules = esbServiceDao.getRulesForSpecificMessage(canonicalData.getSrcMsgType(), canonicalData.getSrcMsgSubType(), canonicalData.getMsgBranch(), canonicalData.getMsgDept(), NgphEsbConstants.RULE_CATEGORY_INTV, canonicalData.getMsgDirection());	
			Rules applicableRule = null;	
			if (messageRules != null && !messageRules.isEmpty()) 
			{
				applicableRule = traceMessageRoute(canonicalData,messageRules);
			}	
			if (applicableRule != null && RuleActionsEnum.MOVETO == RuleActionsEnum.findRuleActionsEnumByName(applicableRule.getRuleAction())) 
			{
				String status = null;	
				//setting applied/applicable rule-id to canonical
				if(StringUtils.isEmpty(canonicalData.getMsgRules()))
				{
					canonicalData.setMsgRules(applicableRule.getRuleId());
				}
				else
				{
					canonicalData.setMsgRules(canonicalData.getMsgRules().concat(NgphEsbConstants.NGPH_COMMA));
					canonicalData.setMsgRules(canonicalData.getMsgRules().concat(applicableRule.getRuleId()));
				}
				if ("REPAIR".equalsIgnoreCase(applicableRule.getRuleActParam())) 
				{
					// Inward Payment
					if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection()))
					{
						status = PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_I);
						EventLogger.logEvent("NGPHINTSVC0002", canonicalData, InterveneServiceImpl.class, canonicalData.getMsgRef());//Payment satisified intervention rule and available in inbound awaiting repair.
					}
					else
					{// Outward Payment
						status = PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.AWAITING_REPAIR_O);
						EventLogger.logEvent("NGPHINTSVC0003", canonicalData, InterveneServiceImpl.class, canonicalData.getMsgRef());//Payment satisified intervention rule and available in outbound awaiting repair.
					}
				}
				if(org.apache.commons.lang.StringUtils.isNotEmpty(status))
				{
					canonicalData.setMsgPrevStatus(canonicalData.getMsgStatus());
					canonicalData.setMsgStatus(status);
					//setting the repair reason
					canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.INTERVINE_SENT));
				}
				esbServiceDao.updatePaymentDetails(canonicalData);
				logger.info("Values Updated in TA_Messages_Tx as MsgStatus->" + status + " by Intervene Service for MsgRef : " + canonicalData.getMsgRef());
			}			
			else
			{
				logger.info("There was no applicable intervention Rule found for msgRef : " + canonicalData.getMsgRef());
				EventLogger.logEvent("NGPHINTSVC0001", canonicalData, InterveneServiceImpl.class, canonicalData.getMsgRef());//No applicable intervention rule found for the payment.
			}
		}
		catch (Exception e) 
		{
			logger.error("Exception occurred in intervention service processing for the payment - " + canonicalData.getMsgRef());
			logger.error(e, e);
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_IVE0001);
			EventLogger.logEvent("NGPHINTSVC0004", canonicalData, InterveneServiceImpl.class, canonicalData.getMsgRef());//Exception occurred in intervention processing.
			throw new Exception(e);
		}
	}
}

