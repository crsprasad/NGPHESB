package com.logica.ngph.esb.servicesImpl;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.RepairReasonsEnum;
import com.logica.ngph.esb.Dtos.Rules;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.enums.RuleActionsEnum;
import com.logica.ngph.esb.services.AutoRouterService;
import com.logica.ngph.esb.services.EntityControlService;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.utils.PaymentsHelper;

/**
 * 
 * @author mohdabdulaa
 * 
 */
public class EntityControlServiceImpl implements EntityControlService 
{
	static Logger logger = Logger.getLogger(EntityControlServiceImpl.class);	
	private EsbServiceDao esbServiceDao;
	private AutoRouterService autoRouterService;
	public void setAutoRouterService(AutoRouterService autoRouterService) 
	{
		this.autoRouterService = autoRouterService;
	}
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	public void populateBranchAndDepartmentDetails(NgphCanonical canonicalData) throws Exception
	{
		logger.info("populateBranchAndDepartmentDetails(...) Start...");
		String branchCode = null;

		if (StringUtils.isNotEmpty(canonicalData.getMsgDirection())) 
		{
			if (NgphEsbConstants.INWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection().trim())) 
			{
				branchCode = findBranchOfInwardMessage(canonicalData);
			} 
			else if (NgphEsbConstants.OUTWARD_PAYMENT.equalsIgnoreCase(canonicalData.getMsgDirection().trim())) 
			{
				branchCode = findBranchOfOutwardMessage(canonicalData);
			}
			if (StringUtils.isNotEmpty(branchCode)) 
			{
				// setting branch to canonical
				canonicalData.setMsgBranch(branchCode);
				String deptCode = null;
				// finding the dept
				if (StringUtils.isEmpty(canonicalData.getMsgDept())) 
				{
					// getting the dept code
					deptCode = getDeptOfMessage(canonicalData);
					if (StringUtils.isNotEmpty(deptCode)) 
					{
						// settig dept to canonical
						canonicalData.setMsgDept(deptCode);
					} 
					else 
					{
						// logging the Audit event
						EventLogger.logEvent("NGPHECTSVC0001", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Department could not be assigned to the payment.
						canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ECE0001);
						//setting the repair reason
						canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.DEPT_NOT_FOUND));
						logger.info("populateBranchAndDepartmentDetails(...) Dept code not found...");
					}
				}
			} 
			else 
			{
				// logging the Audit event
				EventLogger.logEvent("NGPHECTSVC0002", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Branch could not be assigned to the payment.
				canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ECE0002);
				//setting the repair reason
				canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.BRANCH_NOT_FOUND));
				logger.info("populateBranchAndDepartmentDetails(...) Branch code not found...");
			}
		} 
		else 
		{
			// logging the Audit event
			EventLogger.logEvent("NGPHECTSVC0003", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Direction unavailable for the payment, cannot allocate entity.
			canonicalData.setMsgErrorCode(NgphEsbConstants.NGPH_ECE0003);
			//setting the repair reason
			canonicalData.setRepairReason(RepairReasonsEnum.findRepairReasonsCodeByEnum(RepairReasonsEnum.INVALID_MSG_DIRECTION));
			logger.info("populateBranchAndDepartmentDetails(...) Invalid payment direction...");
		}
		logger.info("populateBranchAndDepartmentDetails(...) End...");
	}
	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	private String getDeptOfMessage(NgphCanonical canonicalData) throws Exception
	{
		logger.info("getDeptOfMessage(...) Start....");
		String deptCode = null;
		// getting deptCode by Rules
		deptCode = getDeptCodeByRules(canonicalData);
		// check deptCode whether found/not-found then going in
		if (StringUtils.isEmpty(deptCode)) 
		{
			// getting Initialized deptValue from INITIALISATIONM table
			deptCode = getInitialValue(NgphEsbConstants.INITIALISED_DEPT_VALUE);	
			if (StringUtils.isNotEmpty(deptCode))
			{
				// logging the Audit event
				EventLogger.logEvent("NGPHECTSVC0018", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Department assigned from the default department configuration.
			}
		}
		else
		{
			// logging the Audit event
			EventLogger.logEvent("NGPHECTSVC0004", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Department assigned after rules application.
		}
		logger.info("getDeptOfMessage(...) End....");
		return deptCode;
	}

	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	private String findBranchOfOutwardMessage(NgphCanonical canonicalData) throws Exception
	{
		logger.info("findBranchOfOutwardMessage(...) Start....");
		String branchCode = null;
		// finding the branchCode by orderingCustomer in BRANCHES table
		if (NgphEsbConstants.ORDERING_TYPE_F.equalsIgnoreCase(canonicalData.getOrderingType()))
		{
			branchCode = getBranchCodeByBic(canonicalData.getOrderingCustomerId());
		}
		// check branchCode whether found/not-found then going in
		if (StringUtils.isEmpty(branchCode)) 
		{
			branchCode = getBranchCodeByBic(canonicalData.getSenderBank());
			if (StringUtils.isEmpty(branchCode)) 
			{
				// finding the branchCode by orderingCustAccount in ACCOUNTS table
				branchCode = getBranchCodeByAccountNumber(canonicalData.getOrderingCustAccount());
				// check branchCode whether found/not-found then going in
				if (StringUtils.isEmpty(branchCode)) 
				{
					// finding the branchCode by orderingInstitution in BRANCHES table
					branchCode = getBranchCodeByBic(canonicalData.getOrderingInstitution());
					// check branchCode whether found/not-found then going in
					if (StringUtils.isEmpty(branchCode)) 
					{
						// finding the branchCode by OrderingInstitutionAcct in ACCOUNTS table
						branchCode = getBranchCodeByAccountNumber(canonicalData.getOrderingInstitutionAcct());
						// check branchCode whether found/not-found then going in
						if (StringUtils.isEmpty(branchCode)) 
						{
							// FINDING THE BRANCH THROUCH COMMON-WAY FOR INWARD&OUTWARD MESSAGES
							branchCode = findBranchOfMessage(canonicalData);
							if (StringUtils.isNotEmpty(branchCode))
							{
								//logging the Audit event
								EventLogger.logEvent("NGPHECTSVC0009", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Outbound Payment. Branch assigned. 
							}
						}
						else
						{
							//logging the Audit event
							EventLogger.logEvent("NGPHECTSVC0008", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Outbound payment. Branch assigned based on the ordering institution account number 
						}
					}
					else
					{
						//logging the Audit event
						EventLogger.logEvent("NGPHECTSVC0007", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Outbound payment. Branch assigned based on the ordering institution identification code.
					}
				}
				else
				{
					//logging the Audit event
					EventLogger.logEvent("NGPHECTSVC0006", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Outbound payment. Branch assigned based on customer account number
				}
			}
			else
			{
				//logging the Audit event
				EventLogger.logEvent("NGPHECTSVC0005", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Outbound payment. Branch assigned based on Ordering FI
			}
		}
		else
		{
			//logging the Audit event
			EventLogger.logEvent("NGPHECTSVC0019", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Outbound payment. Branch assigned based on sender bank / branch identification code
		}
		logger.info("findBranchOfOutwardMessage(...) End....");
		return branchCode;
	}

	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	private String findBranchOfInwardMessage(NgphCanonical canonicalData) throws Exception
	{
		logger.info("findBranchOfInwardMessage(...) Start....");
		String branchCode = null;
		// finding the branchCode by receiverBank in BRANCHES table
		branchCode = getBranchCodeByBic(canonicalData.getReceiverBank());
		// check branchCode whether found/not-found then going in
		if (StringUtils.isEmpty(branchCode)) 
		{
			// finding the branchCode by beneficiaryCustomer in BRANCHES table
			if (NgphEsbConstants.BENEFICIARY_TYPE_F.equalsIgnoreCase(canonicalData.getBeneficiaryType()))
			{
				branchCode = getBranchCodeByBic(canonicalData.getBeneficiaryCustomerID());
			}
			// check branchCode whether found/not-found then going in
			if (StringUtils.isEmpty(branchCode)) 
			{
				// finding the branchCode by beneficiaryCustAcct in ACCOUNTS table
				if (canonicalData.getBeneficiaryCustAcct() != null)
				{
					branchCode = getBranchCodeByAccountNumber(canonicalData.getBeneficiaryCustAcct());
				}
				if (StringUtils.isEmpty(branchCode) && canonicalData.getCustAccount() != null)
				{
					branchCode = getBranchCodeByAccountNumber(canonicalData.getCustAccount());
				}
				// check branchCode whether found/not-found then going in
				if (StringUtils.isEmpty(branchCode)) 
				{
					// finding the branchCode by accountWithInstitution in BRANCHES table
					branchCode = getBranchCodeByBic(canonicalData.getAccountWithInstitution());
					// check branchCode whether found/not-found then going in
					if (StringUtils.isEmpty(branchCode)) 
					{
						// finding the branchCode by accountWithInstitutionAcct in ACCOUNTS table
						branchCode = getBranchCodeByAccountNumber(canonicalData.getAccountWithInstitutionAcct());
						// check branchCode whether found/not-found then going in
						if (StringUtils.isEmpty(branchCode)) 
						{
							// FINDING THE BRANCH THROUCH COMMON-WAY FOR INWARD&OUTWARD MESSAGES
							branchCode = findBranchOfMessage(canonicalData);
							if (StringUtils.isNotEmpty(branchCode))
							{
								//logging the Audit event
								EventLogger.logEvent("NGPHECTSVC0017", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Inbound Payment. Branch assigned.
							}
						}
						else
						{
							//logging the Audit event
							EventLogger.logEvent("NGPHECTSVC0016", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Inbound payment. Branch assigned based on the account with institution account number
						}
					}
					else
					{
						//logging the Audit event
						EventLogger.logEvent("NGPHECTSVC0015", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Inbound payment. Branch assigned based on the account with institution identification code
					}
				}
				else
				{
					//logging the Audit event
					EventLogger.logEvent("NGPHECTSVC0014", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Inbound Payment. Branch assigned based on the customer account number.
				}
			}
			else
			{
				//logging the Audit event
				EventLogger.logEvent("NGPHECTSVC0013", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Inbound Payment.  Branch assigned from the beneficiary bank identification code.
			}
		}
		else
		{
			//logging the Audit event
			EventLogger.logEvent("NGPHECTSVC0012", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Inbound Payment. Branch assigned from the receiver bank identification code.
		}
		logger.info("findBranchOfInwardMessage(...) End....");
		return branchCode;
	}
	
	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	private String findBranchOfMessage(NgphCanonical canonicalData) throws Exception
	{
		logger.info("findBranchOfMessage(...) Start....");
		String branchCode = null;
		// finding the branchCode by intermediary1Bank in BRANCHES table
		branchCode = getBranchCodeByBic(canonicalData.getIntermediary1Bank());
		// check branchCode whether found/not-found then going in
		if (StringUtils.isEmpty(branchCode)) 
		{
			// finding the branchCode by intermediary1AgentAcct in ACCOUNTS table
			branchCode = getBranchCodeByAccountNumber(canonicalData.getIntermediary1AgentAcct());
			// check branchCode whether found/not-found then going in
			if (StringUtils.isEmpty(branchCode)) 
			{
				// finding the branchCode by intermediary2Bank in BRANCHES table
				branchCode = getBranchCodeByBic(canonicalData.getIntermediary2Bank());
				// check branchCode whether found/not-found then going in
				if (StringUtils.isEmpty(branchCode)) 
				{
					// finding the branchCode by intermediary2AgentAcct in ACCOUNTS table
					branchCode = getBranchCodeByAccountNumber(canonicalData.getIntermediary2AgentAcct());
					// check branchCode whether found/not-found then going in
					if (StringUtils.isEmpty(branchCode)) 
					{
						// finding the branchCode by intermediary3Bank in
						// BRANCHES table
						branchCode = getBranchCodeByBic(canonicalData.getIntermediary3Bank());
						// check branchCode whether found/not-found then going in
						if (StringUtils.isEmpty(branchCode)) 
						{
							// finding the branchCode by intermediary3AgentAcct in ACCOUNTS table
							branchCode = getBranchCodeByAccountNumber(canonicalData.getIntermediary3AgentAcct());

							// check branchCode whether found/not-found then going in
							if (StringUtils.isEmpty(branchCode)) 
							{
								// GET THE DEFAULT BRANCH VALUE.
								branchCode = getInitialValue(NgphEsbConstants.INITIALISED_BRANCH_VALUE);
								EventLogger.logEvent("NGPHECTSVC0011", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Branch assigned from the default branch configuration.
							}
							else
							{
								EventLogger.logEvent("NGPHECTSVC0010", canonicalData, EntityControlServiceImpl.class, canonicalData.getMsgRef());//Branch assigned based on the intermediary details.
							}
						}
					}
				}
			}
		}
		logger.info("findBranchOfMessage(...) End....");
		return branchCode;
	}

	/**
	 * 
	 * @param bankBic
	 * @return
	 */
	private String getBranchBic(String bankBic) throws Exception
	{
		logger.info("getBranchBic() Inside....");
		return bankBic.substring(NgphEsbConstants.NGPH_INT_EIGHT,NgphEsbConstants.INT_ELEVEN);
	}

	/**
	 * 
	 * @param initEntry
	 * @return
	 */
	private String getInitialValue(String initEntry)throws Exception 
	{
		logger.info("getInitialValue() Inside....");
		return esbServiceDao.getInitialisedValue(initEntry);
	}

	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	private String getDeptCodeByRules(NgphCanonical canonicalData)throws Exception 
	{
		logger.info("getDeptCodeByRules() Start....");
		List<Rules> messageRules = esbServiceDao.getRulesForSpecificMessageDeptTracing(canonicalData.getSrcMsgType().concat(canonicalData.getSrcMsgSubType()),canonicalData.getMsgBranch(),NgphEsbConstants.RULE_CATEGORY_DEPT);
		Rules applicableRule = null;
		String deptCode = null;
		if (messageRules != null && !messageRules.isEmpty()) 
		{
			applicableRule = autoRouterService.traceMessageRoute(canonicalData,messageRules);
		}
		if (applicableRule != null && RuleActionsEnum.ASSIGN == RuleActionsEnum.findRuleActionsEnumByName(applicableRule.getRuleAction())) 
		{
			deptCode = applicableRule.getRuleActParam();	
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
		}
		logger.info("getDeptCodeByRules() End....");
		return deptCode;
	}

	/**
	 * 
	 * @param accountNo
	 * @return
	 */
	private String getBranchCodeByAccountNumber(String accountNo)throws Exception 
	{
		logger.info("getBranchCodeByAccountNumber() Start....");
		String branchCode = null;
		try 
		{
			if (StringUtils.isNotEmpty(accountNo))
			{
				branchCode = esbServiceDao.findBranchCodeByAccountNumber(accountNo);
			}
		} 
		catch (Exception e) 
		{
			logger.error("Error occured in getting branch code by account number ");
			logger.error(e,e);
			throw new Exception(e);
		}
		logger.info("getBranchCodeByAccountNumber() End....");
		return branchCode;
	}

	/**
	 * 
	 * @param branchBic
	 * @return
	 */
	private String getBranchCodeByBic(String bankBic)throws Exception
	{
		logger.info("getBranchCodeByBic() Start....");
		String branchCode = null;
		String branchBic = null;
		if (StringUtils.isNotEmpty(bankBic) && NgphEsbConstants.INT_ELEVEN == bankBic.trim().length())
		{
			branchBic = getBranchBic(bankBic);
		}
		//if branchBic is not-equal to triple x then search in BRANCHES table
		try 
		{
			if (StringUtils.isNotEmpty(branchBic) && !NgphEsbConstants.TRIPLE_X.equalsIgnoreCase(branchBic) && (PaymentsHelper.isBic(bankBic) || PaymentsHelper.isThisValueIFSC(bankBic)))
			{
				branchCode = esbServiceDao.findBranchCodeByBic(bankBic);
			}
		} 
		catch (Exception e) 
		{
			logger.error("Error occured in getting branch code by BIC ");
			logger.error(e,e);
			throw new Exception(e);
		}
		logger.info("getBranchCodeByBic() End....");
		return branchCode;
	}
}
