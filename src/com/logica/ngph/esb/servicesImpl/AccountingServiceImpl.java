package com.logica.ngph.esb.servicesImpl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AccountingEntry;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.AuditServiceClient;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.services.AccountingService;

/**
 * 
 * @author guptarb
 * 
 */
public class AccountingServiceImpl implements AccountingService {

	static Logger logger = Logger.getLogger(AccountingServiceImpl.class);

	private EsbServiceDao esbServiceDao;

	/**
	 * @param esbServiceDao the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}

	/**
	 * 
	 * @param canonicalData
	 */
	public void performAccounting(NgphCanonical canonicalData)throws Exception{
		logInfo("performAccounting(...) Start....");
		if (StringUtils.isNotEmpty(canonicalData.getMsgDirection())) {
			AccountingEntry[] accountingEntries = null;
			if (NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData
					.getMsgDirection().trim())) {
				accountingEntries = findAccountingDetailsForInwardPayment(canonicalData);

			} else if (NgphEsbConstants.OUTWARD_PAYMENT
					.equalsIgnoreCase(canonicalData.getMsgDirection().trim())) {

				accountingEntries = findAccountingDetailsForOutwardPayment(canonicalData);
			}

			if (accountingEntries != null && accountingEntries.length > 0) {
				canonicalData.setAccountingEntry(accountingEntries);
			} else {
				canonicalData
						.setMsgErrorCode(NgphEsbConstants.NGPH_ACCOUNTING_SER_ERR);
				logError("performAccounting(...) Error", NgphEsbConstants.NGPH_ACCOUNTING_SER_ERR);
			}
		} else {
			logError("performAccounting(...) Error", NgphEsbConstants.NGPH_ACCOUNTING_SER_ERR);
			//log an event saying invalid msgDirection NGPHACS0001
			canonicalData
					.setMsgErrorCode(NgphEsbConstants.NGPH_ACCOUNTING_SER_ERR);
			// logging Event
			performAuditEventLogging("NGPHACS0001", canonicalData.getMsgRef(),
					canonicalData.getTxnReference(),
					canonicalData.getMsgBranch(), canonicalData.getMsgDept());
		}
		logInfo("performAccounting(...) End....");
	}

	private AccountingEntry[] findAccountingDetailsForInwardPayment(NgphCanonical canonicalData) throws Exception 
	{	logDebuggers("findAccountingDetailsForInwardPayment(...)  Start....");
		AccountingEntry[] accountingEntries = new AccountingEntry[2];
		AccountingEntry accountingEntryDr = new AccountingEntry();
		AccountingEntry accountingEntryCr = new AccountingEntry();

		// IDENTIFYING WHO IS TO BE DEBITED
		String drAccountNumber = null;
		drAccountNumber = findInBoundPaymentDrAccountNumber(canonicalData);

		// if dr-account number did't found then
		if (StringUtils.isEmpty(drAccountNumber)) {
			logDebuggers("findAccountingDetailsForInwardPayment(...)  Dr Account Number Not Found....");
			//logging an event saying dr-account number not
			// found.NGPHACS0002
			performAuditEventLogging("NGPHACS0002", canonicalData.getMsgRef(),
					canonicalData.getTxnReference(),
					canonicalData.getMsgBranch(), canonicalData.getMsgDept());
			accountingEntries = null;
			accountingEntryDr = null;
			accountingEntryCr = null;
			return null;
		} else {
			// constructing dr-accountEntry array
			// TODO complete this construction
			accountingEntryCr.setAccountNum(drAccountNumber);
			accountingEntryCr.setTxIndicator(NgphEsbConstants.DEBITOR_CONSTANT);

			// IDENTIFYING WHO IS TO BE CREDITED
			String crAccountNumber = null;
			crAccountNumber = findInBoundPaymentCrAccountNumber(canonicalData);

			if (StringUtils.isEmpty(crAccountNumber)) {
				logDebuggers("findAccountingDetailsForInwardPayment(...)  Cr Account Number Not Found....");
				//log an Event saying crAccountNumber not
				// found.NGPHACS0003
				performAuditEventLogging("NGPHACS0003",
						canonicalData.getMsgRef(),
						canonicalData.getTxnReference(),
						canonicalData.getMsgBranch(),
						canonicalData.getMsgDept());
				accountingEntries = null;
				accountingEntryDr = null;
				accountingEntryCr = null;
				return null;
			} else {
				// TODO complete this
				accountingEntryCr.setAccountNum(crAccountNumber);
				accountingEntryCr
						.setTxIndicator(NgphEsbConstants.CREDITOR_CONSTANT);

				// ADDING CR,DR ACCOUNT ENTRIES TO ARRAY
				accountingEntries[0] = accountingEntryDr;
				accountingEntries[1] = accountingEntryCr;
			}
		}
		logDebuggers("findAccountingDetailsForInwardPayment(...)  End....");
		return accountingEntries;
	}

	/**
	 * 
	 * @param canonicalData
	 */
	private AccountingEntry[] findAccountingDetailsForOutwardPayment(NgphCanonical canonicalData) throws Exception 
	{
		logDebuggers("findAccountingDetailsForOutwardPayment(...)  Start....");
		AccountingEntry[] accountingEntries = new AccountingEntry[2];
		AccountingEntry accountingEntryDr = new AccountingEntry();
		AccountingEntry accountingEntryCr = new AccountingEntry();

		// IDENTIFYING WHO IS TO BE DEBITED
		String drAccountNumber = null;
		drAccountNumber = findOutBoundPaymentDrAccountNumber(canonicalData);

		// if dr-account number did't found then
		if (StringUtils.isEmpty(drAccountNumber)) {
			logDebuggers("findAccountingDetailsForOutwardPayment(...)  Dr Account Number Not Found....");
			//logging an event saying dr-account number not found.NGPHACS0002
			performAuditEventLogging("NGPHACS0002", canonicalData.getMsgRef(),
					canonicalData.getTxnReference(),
					canonicalData.getMsgBranch(), canonicalData.getMsgDept());
			accountingEntries = null;
			accountingEntryDr = null;
			accountingEntryCr = null;
			return null;
		} else {
			// constructing dr-accountEntry array
			// TODO complete this construction
			accountingEntryCr.setAccountNum(drAccountNumber);
			accountingEntryCr.setTxIndicator(NgphEsbConstants.DEBITOR_CONSTANT);

			// IDENTIFYING WHO IS TO BE CREDITED
			String crAccountNumber = null;
			crAccountNumber = findOutBoundPaymentCrAccountNumber(canonicalData);

			if (StringUtils.isEmpty(crAccountNumber)) {
				logDebuggers("findAccountingDetailsForOutwardPayment(...)  Cr Account Number Not Found....");
				//log an Event saying crAccountNumber not found.NGPHACS0003
				performAuditEventLogging("NGPHACS0003",
						canonicalData.getMsgRef(),
						canonicalData.getTxnReference(),
						canonicalData.getMsgBranch(),
						canonicalData.getMsgDept());
				accountingEntries = null;
				accountingEntryDr = null;
				accountingEntryCr = null;
				return null;
			} else {
				// TODO complete this
				accountingEntryCr.setAccountNum(crAccountNumber);
				accountingEntryCr
						.setTxIndicator(NgphEsbConstants.CREDITOR_CONSTANT);

				// ADDING CR,DR ACCOUNT ENTRIES TO ARRAY
				accountingEntries[0] = accountingEntryDr;
				accountingEntries[1] = accountingEntryCr;
			}
		}
		logDebuggers("findAccountingDetailsForOutwardPayment(...)  End....");
		return accountingEntries;
	}

	private String findInBoundPaymentCrAccountNumber(NgphCanonical canonicalData)throws Exception 
	{
		logDebuggers("findInBoundPaymentCrAccountNumber(...)  Start....");
		String crAccountNumber = null;
		// considering beneficiaryCustAcct as Cr-accountNumber
		crAccountNumber = canonicalData.getBeneficiaryCustAcct();

		if (StringUtils.isEmpty(crAccountNumber)) {
			// getting Cr-accountNumber by BeneficiaryCustomerId
			if (StringUtils
					.isNotEmpty(canonicalData.getBeneficiaryCustomerID())) {
				crAccountNumber = esbServiceDao
						.getAccountNumberByCustomerId(canonicalData
								.getBeneficiaryCustomerID());
			}
		}

		// getting
		if (StringUtils.isEmpty(crAccountNumber)) {
			// retrieving localBIC
			String localBic = esbServiceDao
					.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);
			if (NgphEsbConstants.ORDERING_TYPE_F.equalsIgnoreCase(canonicalData
					.getBeneficiaryType())
					|| (StringUtils.isNotEmpty(canonicalData
							.getAccountWithInstitution()) && canonicalData
							.getAccountWithInstitution().trim().substring(0, 8)
							.equalsIgnoreCase(localBic.trim().substring(0, 8)))) {
				crAccountNumber = esbServiceDao
						.getInitialisedValue(NgphEsbConstants.PYMTSUNDAC_INIT_ENTRY);
			}
		}
		logDebuggers("findInBoundPaymentCrAccountNumber(...)  End....");
		return crAccountNumber;
	}

	private String findInBoundPaymentDrAccountNumber(NgphCanonical canonicalData)throws Exception 
	{
		logDebuggers("findInBoundPaymentDrAccountNumber(...)  Start....");
		String drAccountNumber = null;
		// retrieving localBIC Cr-accountNumber by Initialized value
		String localBic = esbServiceDao
				.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);

		if ((NgphEsbConstants.ORDERING_TYPE_I.equalsIgnoreCase(canonicalData
				.getBeneficiaryType())
				&& StringUtils.isNotEmpty(canonicalData
						.getAccountWithInstitution()) && canonicalData
				.getAccountWithInstitution().trim().substring(0, 8)
				.equalsIgnoreCase(localBic.trim().substring(0, 8)))
				|| (NgphEsbConstants.ORDERING_TYPE_F
						.equalsIgnoreCase(canonicalData.getBeneficiaryType())
						&& StringUtils.isNotEmpty(canonicalData
								.getBeneficiaryCustomerID()) && canonicalData
						.getBeneficiaryCustomerID().trim().substring(0, 8)
						.equalsIgnoreCase(localBic.trim().substring(0, 8)))) {
			// getting Dr-accountNumber By ReceiverCorrespondent
			drAccountNumber = getAccountNumberByReceiverCorrespondent(canonicalData);

			// getting Dr-accountNumber By ThirdCorrespondent
			if (StringUtils.isEmpty(drAccountNumber)) {
				// getting Dr-accountNumber by ThirdCorrespondent
				drAccountNumber = getAccountNumberByThirdCorrespondent(canonicalData);
			}

			// getting Dr-accountNumber By SenderCorrespondent
			if (StringUtils.isEmpty(drAccountNumber)) {
				drAccountNumber = getAccountNumberBySenderCorrespondent(canonicalData);
			}
		} else if ((NgphEsbConstants.ORDERING_TYPE_I
				.equalsIgnoreCase(canonicalData.getBeneficiaryType())
				&& StringUtils.isNotEmpty(canonicalData
						.getAccountWithInstitution()) && !canonicalData
				.getAccountWithInstitution().trim().substring(0, 8)
				.equalsIgnoreCase(localBic.trim().substring(0, 8)))
				|| (NgphEsbConstants.ORDERING_TYPE_F
						.equalsIgnoreCase(canonicalData.getBeneficiaryType())
						&& StringUtils.isNotEmpty(canonicalData
								.getBeneficiaryCustomerID()) && !canonicalData
						.getBeneficiaryCustomerID().trim().substring(0, 8)
						.equalsIgnoreCase(localBic.trim().substring(0, 8)))) {
			// getting Dr-accountNumber By SenderBank Code
			if (StringUtils.isNotEmpty(canonicalData.getSenderBank())) {
				drAccountNumber = esbServiceDao.getAccountNumberByPartyCode(
						canonicalData.getSenderBank(),
						canonicalData.getMsgCurrency(),
						canonicalData.getMsgBranch());
			}
		}
		logDebuggers("findInBoundPaymentDrAccountNumber(...)  End....");
		return drAccountNumber;
	}

	/**
	 * @param canonicalData
	 * @return
	 * @throws NGPHException
	 */
	private String findOutBoundPaymentCrAccountNumber(	NgphCanonical canonicalData) throws Exception 
	{
		logDebuggers("findOutBoundPaymentCrAccountNumber(...)  Start....");
		String crAccountNumber = null;

		// getting Cr-accountNumber by ThirdCorrespondent
		crAccountNumber = getAccountNumberByThirdCorrespondent(canonicalData);

		// getting Cr-accountNumber By ReceiverCorrespondent
		if (StringUtils.isEmpty(crAccountNumber)) {
			crAccountNumber = getAccountNumberByReceiverCorrespondent(canonicalData);
		}

		// Considering AccountWithInstitutionAcct value as Cr-account number
		if (StringUtils.isNotEmpty(canonicalData
				.getAccountWithInstitutionAcct())) {
			crAccountNumber = canonicalData.getAccountWithInstitutionAcct();
		} else if (StringUtils.isNotEmpty(canonicalData
				.getAccountWithInstitution())) {
			// getting Cr-account number from TA_Accounts table by
			// AccountWithInstitution value
			crAccountNumber = esbServiceDao.getAccountNumberByPartyCode(
					canonicalData.getAccountWithInstitution(),
					canonicalData.getMsgCurrency(),
					canonicalData.getMsgBranch());
		}

		if (StringUtils.isEmpty(crAccountNumber)) {
			crAccountNumber = getAccountNumberBySenderCorrespondent(canonicalData);
		}
		logDebuggers("findOutBoundPaymentCrAccountNumber(...)  End....");
		return crAccountNumber;
	}

	/**
	 * @param canonicalData
	 * @param crAccountNumber
	 * @return
	 * @throws NGPHException
	 */
	private String getAccountNumberBySenderCorrespondent(NgphCanonical canonicalData) throws Exception 
	{
		logDebuggers("getAccountNumberBySenderCorrespondent(...)  Start....");
		String accountNumber = null;
		// Considering SenderCorrespondentAcct value as Cr-account
		// number
		if (StringUtils.isNotEmpty(canonicalData.getSenderCorrespondentAcct())) {
			accountNumber = canonicalData.getSenderCorrespondentAcct();
		} else if (StringUtils.isNotEmpty(canonicalData
				.getSenderCorrespondent())) {
			// getting Cr-account number from TA_Accounts table by
			// SenderCorrespondent value
			accountNumber = esbServiceDao.getAccountNumberByPartyCode(
					canonicalData.getSenderCorrespondent(),
					canonicalData.getMsgCurrency(),
					canonicalData.getMsgBranch());
		}
		logDebuggers("getAccountNumberBySenderCorrespondent(...)  End....");
		return accountNumber;
	}

	/**
	 * @param canonicalData
	 * @return
	 * @throws NGPHException
	 */
	private String getAccountNumberByThirdCorrespondent(NgphCanonical canonicalData)throws Exception 
	{
		logDebuggers("getAccountNumberByThirdCorrespondent(...)  Start....");
		String accountNumber = null;
		if (StringUtils.isNotEmpty(canonicalData.getThirdCorrespondent())) {
			// Considering ThirdCorrespondentAcct value as Cr-account number
			if (StringUtils.isNotEmpty(canonicalData
					.getThirdCorrespondentAcct()))
				accountNumber = canonicalData.getThirdCorrespondentAcct();
			else {
				// getting Cr-account number from TA_Accounts table by
				// ThirdCorrespondent
				accountNumber = esbServiceDao.getAccountNumberByPartyCode(
						canonicalData.getThirdCorrespondent(),
						canonicalData.getMsgCurrency(),
						canonicalData.getMsgBranch());
			}
		}
		logDebuggers("getAccountNumberByThirdCorrespondent(...)  Start....");
		return accountNumber;
	}

	/**
	 * @param canonicalData
	 * @param accountNumber
	 * @return
	 * @throws NGPHException
	 */
	private String getAccountNumberByReceiverCorrespondent(NgphCanonical canonicalData) throws Exception 
	{
		logDebuggers("getAccountNumberByReceiverCorrespondent(...)  Start....");
		String accountNumber = null;
		if (StringUtils.isNotEmpty(canonicalData.getReceiverCorrespondent())) {
			// Considering ReceiverCorrespondentAcct value as Cr-account
			// number
			if (StringUtils.isNotEmpty(canonicalData
					.getReceiverCorrespondentAcct()))
				accountNumber = canonicalData.getReceiverCorrespondentAcct();
			else {
				// //getting Cr-account number from TA_Accounts table by
				// ReceiverCorrespondent value
				accountNumber = esbServiceDao.getAccountNumberByPartyCode(
						canonicalData.getReceiverCorrespondent(),
						canonicalData.getMsgCurrency(),
						canonicalData.getMsgBranch());
			}
		}
		logDebuggers("getAccountNumberByReceiverCorrespondent(...)  End....");
		return accountNumber;
	}

	/**
	 * @param canonicalData
	 * @return
	 * @throws NGPHException
	 */
	private String findOutBoundPaymentDrAccountNumber(NgphCanonical canonicalData) throws Exception 
	{
		logDebuggers("findOutBoundPaymentDrAccountNumber(...)  Start....");
		String drAccountNumber = null;
		if (NgphEsbConstants.ORDERING_TYPE_I.equalsIgnoreCase(canonicalData
				.getOrderingType())) {
			// taking orderingCustAccount as Dr-account number
			if (StringUtils.isNotEmpty(canonicalData.getOrderingCustAccount())) {
				drAccountNumber = canonicalData.getOrderingCustAccount();
			} else if (StringUtils.isNotEmpty(canonicalData
					.getOrderingCustomerId())) {
				// taking Dr-account number from TA_Customer table
				drAccountNumber = esbServiceDao
						.getAccountNumberByCustomerId(canonicalData
								.getOrderingCustomerId());
			}
			// taking Dr-account number from TA_Accounts table
			if (StringUtils.isEmpty(drAccountNumber)
					&& StringUtils.isNotEmpty(canonicalData
							.getOrderingCustomerName())) {
				String baseCurrency = esbServiceDao
						.getInitialisedValue(NgphEsbConstants.BASE_CUR_INIT_ENTRY);

				if (StringUtils.isNotEmpty(baseCurrency))
					drAccountNumber = esbServiceDao
							.getAccountNumberByAccountName(
									canonicalData.getOrderingCustomerName(),
									baseCurrency, canonicalData.getMsgBranch());
			}
			// taking Dr-account number from INITIALISATIONM table
			if (StringUtils.isEmpty(drAccountNumber)) {
				drAccountNumber = esbServiceDao
						.getInitialisedValue(NgphEsbConstants.PYMTSUNDAC_INIT_ENTRY);
			}
		} else if (NgphEsbConstants.ORDERING_TYPE_F
				.equalsIgnoreCase(canonicalData.getOrderingType())) {
			if (StringUtils.isNotEmpty(canonicalData.getOrderingInstitution())) {
				String localBic = esbServiceDao
						.getInitialisedValue(NgphEsbConstants.LOCALBIC_INIT_ENTRY);
				// taking Dr-account number from INITIALISATIONM table
				if (StringUtils.isNotEmpty(localBic)
						&& localBic
								.trim()
								.substring(0, 8)
								.equalsIgnoreCase(
										canonicalData.getOrderingInstitution()
												.trim().substring(0, 8))) {
					drAccountNumber = esbServiceDao
							.getInitialisedValue(NgphEsbConstants.PYMTSUSPAC_INIT_ENTRY);
				} else {
					// considering OrderingInstitutionAcct value as Dr-account
					// number
					if (StringUtils.isNotEmpty(canonicalData
							.getOrderingInstitutionAcct())) {
						drAccountNumber = canonicalData
								.getOrderingInstitutionAcct();
					}
					// taking Dr-account number from TA_Accounts table
					if (StringUtils.isEmpty(drAccountNumber)) {
						drAccountNumber = esbServiceDao
								.getAccountNumberByPartyCode(
										canonicalData.getOrderingInstitution(),
										canonicalData.getMsgCurrency(),
										canonicalData.getMsgBranch());
					}
				}
			} else {
				// DO-NOTHING We are logging it below
				// FIXME if required/exist collect the business
			}
		} else {
			//log an event saying invalid OrderingType
			performAuditEventLogging("NGPHACS0004", canonicalData.getMsgRef(),
					canonicalData.getTxnReference(),
					canonicalData.getMsgBranch(), canonicalData.getMsgDept());
			return null;
		}
		logDebuggers("findOutBoundPaymentDrAccountNumber(...)  End....");
		return drAccountNumber;
	}

	/**
	 * 
	 * @param eventId
	 * @param msgRef
	 * @param msgTxnRef
	 * @param msgBranch
	 * @param msgDept
	 */
	private void performAuditEventLogging(String eventId, String msgRef,
			String msgTxnRef, String msgBranch, String msgDept) {
		logDebuggers("performAuditEventLogging(...)  Start....");
		AuditServiceClient auditServiceClient = new AuditServiceClient();
		EventAudit audit = new EventAudit();
		audit.setAuditEventId(eventId);
		audit.setAuditMessageRef(msgRef);
		audit.setAuditTransactionRef(msgTxnRef);
		audit.setAuditSource(AccountingService.class.toString()
				.replace("interface", "").trim());
		audit.setAuditBranch(msgBranch);
		audit.setAuditDept(msgDept);
		auditServiceClient.dbPollerQueueCall(null, "AUDIT", audit);
		logDebuggers("performAuditEventLogging(...)  End....");
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
}
