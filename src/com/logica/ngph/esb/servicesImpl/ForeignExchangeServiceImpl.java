package com.logica.ngph.esb.servicesImpl;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.RepairReasonsEnum;
import com.logica.ngph.esb.AuditServiceClient;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.services.ForeignExchangeService;
import com.logica.ngph.esb.services.SwiftChannelService;
/**
 * 
 * @author mohdabdulaa
 *
 */
public class ForeignExchangeServiceImpl implements ForeignExchangeService{
	
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(ForeignExchangeServiceImpl.class);
	
	/**
	 * 
	 */
	private EsbServiceDao esbServiceDao;
	
	/**
	 * 
	 * @param esbServiceDao
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}
	
	/**
	 * 
	 */
	public void performForeignExchange(NgphCanonical ngphCanonical)throws Exception
	{
		logInfo("performForeignExchange() Start....");
			String baseCurrency = esbServiceDao.getInitialisedValue(NgphEsbConstants.BASE_CUR_INIT_ENTRY);
			
			if(StringUtils.isNotEmpty(baseCurrency))
			{
					if(NgphEsbConstants.OUTWARD_PAYMENT.equalsIgnoreCase(ngphCanonical.getMsgDirection()))
					{
						if(baseCurrency.equals(ngphCanonical.getInstructedCurrency()))
						{
							logDebuggers("performForeignExchange() BaseCurrency equals to InstructedCurrency....TODO");
							//TODO
						}else{
							logDebuggers("performForeignExchange() BaseCurrency not-equals to InstructedCurrency....");
							ngphCanonical.setDrCurrency(baseCurrency);
							ngphCanonical.setCrCurrency(ngphCanonical.getInstructedCurrency());
							ngphCanonical.setBaseCcyAmount(getAmountInBaseCurrency(ngphCanonical, NgphEsbConstants.DEBITOR_CONSTANT));
							ngphCanonical.setMsgCurrencyAmount(ngphCanonical.getInstructedAmount().multiply(ngphCanonical.getXchangeRate()));
							ngphCanonical.setInstructedCcyAmount(ngphCanonical.getInstructedAmount());
							//FIXME remove me later once if part TODOs are completed 
							esbServiceDao.updatePaymentMsgWithFxDetails(ngphCanonical);
						}
					}else if(NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(ngphCanonical.getMsgDirection()))
					{
						if(baseCurrency.equals(ngphCanonical.getMsgCurrency()))
						{
							logDebuggers("performForeignExchange() BaseCurrency equals to MsgCurrency....TODO");
							//TODO
						}else{
							logDebuggers("performForeignExchange() BaseCurrency not equals to MsgCurrency....");
							ngphCanonical.setDrCurrency(ngphCanonical.getMsgCurrency());
							ngphCanonical.setCrCurrency(baseCurrency);
							ngphCanonical.setBaseCcyAmount(getAmountInBaseCurrency(ngphCanonical, NgphEsbConstants.CREDITOR_CONSTANT));
							ngphCanonical.setMsgCurrencyAmount(ngphCanonical.getMsgAmount());
							ngphCanonical.setInstructedCcyAmount(ngphCanonical.getMsgAmount().multiply(ngphCanonical.getXchangeRate()));
							
							//FIXME remove me later once if part TODOs are completed 
							esbServiceDao.updatePaymentMsgWithFxDetails(ngphCanonical);
						}
					}
				//updating the payment message with foreign exchange details 
				//FIXME later remove this comment and remove the same statement above
				//esbServiceDao.updatePaymentMsgWithFxDetails(ngphCanonical);
				
			}else{
				//set error code as 
				ngphCanonical.setMsgErrorCode("NGPHFXS0001");
				//setting the repair reason
				ngphCanonical.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.BASE_CURRENCY_NOT_FOUND));
				//AuditEventLogging
				performAuditEventLogging("NGPHFXS0001", ngphCanonical.getMsgRef(), ngphCanonical.getTxnReference(), ngphCanonical.getMsgDept(), ngphCanonical.getMsgBranch(), "");
				logDebuggers("getAmountInBaseCurrency(): Base currency is not available....");
			}
			logInfo("performForeignExchange() End....");
	}
	
	/**
	 * 
	 * @param currencyCode
	 * @param msgAmount
	 * @return
	 */
	private BigDecimal getAmountInBaseCurrency(NgphCanonical ngphCanonical, String indicator)throws Exception
	{
		logDebuggers("getAmountInBaseCurrency() Start....");
		BigDecimal amountInBaseCurr = null;
		String currencyType = null;
		
		if(NgphEsbConstants.DEBITOR_CONSTANT.equals(indicator))
			currencyType = ngphCanonical.getInstructedCurrency();
		else if(NgphEsbConstants.CREDITOR_CONSTANT.equals(indicator))
			currencyType = ngphCanonical.getMsgCurrency();
		
		BigDecimal conversionPrice = esbServiceDao.getConversionPrice(currencyType, indicator);
		if(conversionPrice != null)
		{
			if(NgphEsbConstants.DEBITOR_CONSTANT.equals(indicator))
				amountInBaseCurr = conversionPrice.multiply(ngphCanonical.getInstructedAmount());
			else if(NgphEsbConstants.CREDITOR_CONSTANT.equals(indicator))
				amountInBaseCurr = conversionPrice.multiply(ngphCanonical.getMsgAmount());
		}else{
			//set error code as 
			ngphCanonical.setMsgErrorCode("NGPHFXS0002");
			//setting the repair reason
			ngphCanonical.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.CURRENCY_CONVERSION_PRBLM));
			//AuditEventLogging
			performAuditEventLogging("NGPHFXS0002", ngphCanonical.getMsgRef(), ngphCanonical.getTxnReference(), ngphCanonical.getMsgDept(), ngphCanonical.getMsgBranch(), ngphCanonical.getMsgCurrency());
			logDebuggers("getAmountInBaseCurrency(): Currency conversion prices are not available for....:"+ngphCanonical.getMsgCurrency());
		}
		logDebuggers("getAmountInBaseCurrency() End....");
		return amountInBaseCurr;
	}
	
	/**
	 * 
	 * @param message
	 * @param code
	 */
	private void logError(String message, String code)
	{
		/**
         * log the information when logger is in debug mode 
         * 
         */
        if(logger.isDebugEnabled()){
              logger.debug(message+code);
        }
        /**
         * log the information when logger is in info mode 
         * 
         */
        if(logger.isInfoEnabled()){
              logger.info(message+code);   
        }
        /**
         * log the information when logger is in error mode 
         * 
         */
        if(logger.isEnabledFor(Level.ERROR)){
              logger.error(message+code);  
        }
	}
	
	 /**
     * log the information when logger is in info mode 
     * 
     */
	private void logInfo(String info)
	{
		
        if(logger.isInfoEnabled()){
              logger.info(info);   
        }
	}
	
	 /**
     * log the information when logger is in debug mode 
     * 
     */
	private void logDebuggers(String debugInfo)
	{
		
        if(logger.isDebugEnabled()){
              logger.debug(debugInfo);
        }
	}
	
	/**
	 * 
	 * @param eventId
	 * @param msgRef
	 * @param msgTxnRef
	 * @param msgBranch
	 * @param msgDept
	 */
	private void performAuditEventLogging(String eventId, String msgRef, String msgTxnRef, String msgDept, String msgBranch, String extras)
	{
		AuditServiceClient auditServiceClient = new AuditServiceClient();
		EventAudit audit = new EventAudit();
		audit.setAuditEventId(eventId);
		audit.setAuditMessageRef(msgRef);
		audit.setAuditTransactionRef(msgTxnRef);
		audit.setAuditSource(SwiftChannelService.class.toString().replace("com.logica.ngph.", "").trim());
		audit.setAuditBranch(msgBranch);
		audit.setAuditDept(msgDept);
		if(StringUtils.isNotEmpty(extras))
		{
			String[] extraInfo = extras.split(",");
			audit.setExtraInformation(extraInfo);
		}
		auditServiceClient.dbPollerQueueCall(null, "AUDIT", audit);
	}
}
