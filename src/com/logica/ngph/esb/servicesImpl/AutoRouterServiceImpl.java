package com.logica.ngph.esb.servicesImpl;

import java.util.List;
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
import com.logica.ngph.esb.services.AutoRouterService;
import com.logica.ngph.utils.EventLogger;

/**
 * @author guptarb
 * 
 * This service identifies the destination of the message based on
 * the rules defined in RULES table for the particular message.
 */
public class AutoRouterServiceImpl implements AutoRouterService 
{
	static Logger logger = Logger.getLogger(AutoRouterServiceImpl.class);
	private EsbServiceDao esbServiceDao;
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	 /**
	 * 
	 * @param canonicalData
	 */
	public void performRouting(NgphCanonical canonicalData) throws Exception
	{
		logger.info("performRouting() Start....");
		List<Rules> messageRules = esbServiceDao.getRulesForSpecificMessage(canonicalData.getSrcMsgType(),canonicalData.getSrcMsgSubType(),canonicalData.getMsgBranch(),
																			canonicalData.getMsgDept(),NgphEsbConstants.RULE_CATEGORY_RTNG, canonicalData.getMsgDirection());
		Rules applicableRule = null;
		String dstEiId = null;
		if (messageRules != null && !messageRules.isEmpty()) 
		{
			logger.info("Message rules fetched : "+ messageRules.size() + "\t" +  messageRules);
			for(int k=0;k<messageRules.size();k++)
			{
				logger.info(messageRules.get(k).getRuleId());
			}

			applicableRule = traceMessageRoute(canonicalData, messageRules);
			logger.info("Applicable rule is "+applicableRule);
			if (applicableRule != null && RuleActionsEnum.SENDTO == RuleActionsEnum.findRuleActionsEnumByName(applicableRule.getRuleAction())) 
			{
				// setting applied/applicable rule-id to canonical
				if (StringUtils.isEmpty(canonicalData.getMsgRules())) 
				{
					canonicalData.setMsgRules(applicableRule.getRuleId());
				}
				else 
				{
					canonicalData.setMsgRules(canonicalData.getMsgRules().concat(NgphEsbConstants.NGPH_COMMA));
					canonicalData.setMsgRules(canonicalData.getMsgRules().concat(applicableRule.getRuleId()));
				}
				logger.info("Message rules set by AutoRouter is : " + canonicalData.getMsgRules());
				
				if (StringUtils.isNotEmpty(applicableRule.getRuleActParam())) 
				{
					// Store the dstEI_EI which we get from TA_Rules table from column ActParam.
					dstEiId = applicableRule.getRuleActParam();
					canonicalData.setDstEiId(dstEiId);

					logger.info("dstEiId set by AutoRouter is : " + canonicalData.getDstEiId());

					//Fetch the Destination Channel Type based on dstEiId
					String dstChnlType = esbServiceDao.getDstChnlType(canonicalData.getDstEiId());
					// Store the Destination Channel type in Canonical
					canonicalData.setDstMsgChnlType(dstChnlType);
					// Fetch dst Msg type and Dst Sub Msg type from Ta_Msgs_Mapping passing srcmsgtype and srcmsgsubtype and dst Chnl type
					List<String> dstMsgInfo = esbServiceDao.getDstMsgtype(canonicalData.getSrcMsgType(), canonicalData.getSrcMsgSubType(),canonicalData.getDstMsgChnlType(),canonicalData.getMsgDirection());
					if(dstMsgInfo!=null && dstMsgInfo.size() > 0)
					{
						canonicalData.setDstMsgType(dstMsgInfo.get(0));
						canonicalData.setDstMsgSubType(dstMsgInfo.get(1));
					}
					else
					{
						logger.error("Failed to fetch the Destination message type for the source details " + canonicalData.getSrcMsgType() + " -- " + canonicalData.getSrcMsgSubType() + " -- " + canonicalData.getDstMsgChnlType() + " -- " + canonicalData.getMsgDirection());
						canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ARE0001);
						EventLogger.logEvent("NGPHARTSVC0001", canonicalData, AutoRouterServiceImpl.class, canonicalData.getMsgRef());//Destination message type mapping not found for the payment. Refer error log for details.
					}
				}
				else 
				{
					// logging an event saying invalid ActParam
					EventLogger.logEvent("NGPHARTSVC0002", canonicalData,  AutoRouterServiceImpl.class, canonicalData.getMsgRef());//Rule action parameter for the routing rule is not configured.
					canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ARE0002);
					// setting the repair reason
					canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.INVALID_RULEACTPARAM_ROUTING));
				}
				logger.info("performRouting() RULE-ACTION:"	+ applicableRule.getRuleAction());
				logger.info("performRouting() RULE-ACTPARAM:" + applicableRule.getRuleActParam());
			} 
			else 
			{
				// logging an event saying none of the configured rule
				// applicable for this payment message type
				EventLogger.logEvent("NGPHARTSVC0003", canonicalData, AutoRouterServiceImpl.class, canonicalData.getMsgRef());//No applicable routing rule found for the source message type {SrcMsgType} and source message sub type {SrcMsgSubType} so setting the same as destination types.
				// setting message destination types as message source types
				canonicalData.setDstMsgType(canonicalData.getSrcMsgType());
				canonicalData.setDstMsgSubType(canonicalData.getSrcMsgSubType());
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ARE0003);
			}
		} 
		else 
		{
			if (canonicalData.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT) && canonicalData.getMsgIsReturn() == 1)
			{
				List<String> dstMsgInfo = esbServiceDao.getRtnMsgtype(canonicalData.getSrcMsgType(), canonicalData.getSrcMsgSubType(),canonicalData.getDstMsgChnlType(),canonicalData.getMsgDirection());
				if(dstMsgInfo!=null && dstMsgInfo.size() > 0)
				{
					canonicalData.setDstMsgType(dstMsgInfo.get(0));
					canonicalData.setDstMsgSubType(dstMsgInfo.get(1));
				}
				else
				{
					logger.error("Failed to fetch the Return message type for the source details " + canonicalData.getSrcMsgType() + " -- " + canonicalData.getSrcMsgSubType() + " -- " + canonicalData.getDstMsgChnlType() + " -- " + canonicalData.getMsgDirection());
					canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ARE0001);
					EventLogger.logEvent("NGPHARTSVC0004", canonicalData, AutoRouterServiceImpl.class, canonicalData.getMsgRef());//Return message type mapping not found for the payment. Refer error log for details.
				}
			}
			else
			{
				// logging an event saying no rules configured for this payment
				// message type
				EventLogger.logEvent("NGPHARTSVC0003", canonicalData, AutoRouterServiceImpl.class, canonicalData.getMsgRef());//No applicable routing rule found for the source message type {SrcMsgType} and source message sub type {SrcMsgSubType} so setting the same as destination types.
				// setting message destination types as message source types
				canonicalData.setDstMsgType(canonicalData.getSrcMsgType());
				canonicalData.setDstMsgSubType(canonicalData.getSrcMsgSubType());
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ARE0003);
			}
		}
		logger.info("performRouting() End....");
	}
	/**
	 * param name begin&ends with $ oprators begin&ends with ~ (Eg:~CONTAINS~,
	 * ~>~ ETC...)
	 * 
	 */
	public Rules traceMessageRoute(NgphCanonical canonicalData,List<Rules> messageRules) throws Exception 
	{
		logger.info("traceMessageRoute() Start....");
		Rules applicableRule = null;
		for (Rules rule : messageRules) 
		{
			logger.info("traceMessageRoute() calling RulesEvaluationHelper.evaluateRuleCondition()....");
			logger.info("Rule Condition is ====>"+rule.getRuleCondition());
			logger.info("MsgCurrency before evaluateRule Condition :: "+canonicalData.getMsgCurrency()+ "LC number is ::"+canonicalData.getLcNo());
			if (RulesEvaluationHelper.evaluateRuleCondition(rule.getRuleCondition(), canonicalData)) 
			{
				logger.info("traceMessageRoute() calling Rule satisfied...." + rule.getRuleId());
				applicableRule = rule;
				break;
			}
			logger.info("traceMessageRoute() calling Rule not satisfied....");
		}
		logger.info("traceMessageRoute() End....");
		return applicableRule;
	}
}
