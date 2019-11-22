package com.logica.ngph.action;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.logica.ngph.common.ConstantUtil;
import com.logica.ngph.esb.Dtos.SodEodTaskTDto;
import com.logica.ngph.esb.services.SodEodService;
import com.logica.ngph.utils.ApplicationContextProvider;

public class SOdEodAction {

	private static final long serialVersionUID = -962141218821814713L;

	static Logger logger = Logger.getLogger(SOdEodAction.class);
	
	private String branch;
	private String businessDate;
	private String status;

	List<String> taskNameList = new ArrayList<String>();
	List<String> taskIdList = new ArrayList<String>();
	List<String> taskIdStatusList = new ArrayList<String>();
	

	public List<String> getTaskIdList() 
	{
		return taskIdList;
	}
	
	public void setTaskIdList(List<String> taskIdList) 
	{
		this.taskIdList = taskIdList;
		this.session.put("taskIdList", taskIdList);
	}
	
	private Map<String, Object> session = new HashMap<String, Object>();
	
	public Map<String, Object> getSession() 
	{
		return session;
	}
	public void setSession(Map<String, Object> session) 
	{
		this.session = session;
	}
	
	public String getStatus() 
	{
		return status;
	}
	
	public List<String> getTaskNameList() 
	{
		return taskNameList;
	}
	
	public void setTaskNameList(List<String> taskNameList) 
	{
		this.taskNameList = taskNameList;
		this.session.put("taskNameList", taskNameList);
	}
	
	public void setStatus(String status) 
	{
		this.status = status;
	}
	
	public String getBusinessDate() 
	{
		return businessDate;
	}
	
	public void setBusinessDate(String businessDate) 
	{
		this.businessDate = businessDate;
	}
	
	public String getBranch() 
	{
		return branch;
	}
	
	public void setBranch(String branch) 
	{
		this.branch = branch;
	}

	public List<String> getTaskIdStatusList() 
	{
		return taskIdStatusList;
	}
	public void setTaskIdStatusList(List<String> taskIdStatusList) 
	{
		this.taskIdStatusList = taskIdStatusList;
		this.session.put("taskIdStatusList", taskIdStatusList);
	}
	
	private SodEodService sodEodService;

	public void setSodEodService(SodEodService sodEodService) {
		this.sodEodService = sodEodService;
	}

	public static void main(String[] args) 
	{
		/*
		try
		{
			ApplicationContextProvider.initializeContextProvider();
			SOdEodAction sodEodAction = new SOdEodAction();
			sodEodAction.performSodEod();
			sodEodAction.performSodEod();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	//Only Call this method (will do all the processings)..
	public void performSodEod() throws Exception
	{
		//TODO comment this after testing as standAlone program
		//sodEodService = (SodEodService) ApplicationContextProvider.getBean("sodEodService");

		Map<String, Object> taskMap = null;
		taskMap = sodEodService.getLocalBranch();
	
		if(taskMap.get(ConstantUtil.BUSINESSDATE) != null)
		{
			setBusinessDate(taskMap.get(ConstantUtil.BUSINESSDATE).toString().substring(0, 10));
		}
		setBranch((String)taskMap.get(ConstantUtil.ACTION_BRANCH));
	
		String taskStatus = (String)taskMap.get(ConstantUtil.STATUS);
	
		if(taskStatus.equals(SODEODUtil.SOD))
		{
			setStatus(SODEODUtil.SOD_NAME)	;
		}
		else if(taskStatus.equals(SODEODUtil.EOD))
		{
			setStatus(SODEODUtil.EOD_NAME);
		}
		else if(taskStatus.equals(SODEODUtil.SOD_EOD))
		{
			setStatus(SODEODUtil.SOD_EOD_NAME);
		}
		else if(taskStatus.equals(SODEODUtil.NO_TASK))
		{
			setStatus(SODEODUtil.SOD_EOD_NONE);
		}
		
	@SuppressWarnings("unchecked")
	List<String> taskIdNameList = (List<String>)taskMap.get(ConstantUtil.TASKLIST);
	List<String> taskIdTempList = new ArrayList<String>();
	List<String> taskNameTempList = new ArrayList<String>();
	
		for(Object taskName:taskIdNameList)
		{
			if(taskName != null)
			{
				String[] taskNameWithId = 	taskName.toString().split("-");
				taskIdTempList.add(taskNameWithId[0]);
				taskNameTempList.add(taskNameWithId[1]);
			}
		}
	
		setTaskNameList(taskNameTempList);
		setTaskIdList(taskIdTempList);
		
		//perform Operation
		saveSodEodTask();
	}

	
	public void saveSodEodTask()throws Exception
	{
		@SuppressWarnings("unchecked")
		List<String> taskIDList = (List<String>)session.get("taskIdList");
		
		if(taskIDList != null)
		{
			
				SodEodTaskTDto sodEodTaskTDto = getSodEodTDto();
				String branchCode=	getBranchCode();
				Timestamp tempTime = sodEodTaskTDto.getBusinessDate();
				
				for(String taskId:taskIDList)
				{
					int serviceId = Integer.parseInt(taskId);
					
					sodEodTaskTDto.setTaskId(taskId);
					
					
					switch (serviceId) 
					{
						case 1001: 
							//update bussinessdayM for all branches.
							List<String> branches = sodEodService.getbranches();
							for(int i=0;i<branches.size();i++)
							{
								sodEodTaskTDto.setBranch(branches.get(i));
								Timestamp ts = sodEodService.getCurBusDayforBranch(branches.get(i));
								if (ts != null)
								{
									sodEodTaskTDto.setBusinessDate(ts);
								}
								else
								{
									sodEodTaskTDto.setBusinessDate(tempTime);
								}
								sodEodService.setNextBusinessDay(sodEodTaskTDto);
							}
							//Reset to the original branch
							sodEodTaskTDto.setBranch(branchCode);
							break;
						case 1002: 
							break;
						case 1003: 
							sodEodService.openCloseInboundOutboundFeeds(sodEodTaskTDto,1);	
							break;
						case 1004: 
							sodEodService.validatePaymentNonfinalityStatus(branchCode,sodEodTaskTDto);
							break;
						case 1005: 
							sodEodService.openCloseInboundOutboundFeeds(sodEodTaskTDto,0);
							break;
						case 1006: 
							sodEodService.moveFinalStatusToHistoryTable(branchCode,sodEodTaskTDto);
							break;
						case 1007: 
							sodEodService.updateTaLimits();
							break;
						case 1008: 
							break;
						default:
							break;
					}
				}
				
				String sodOrEod = getStatus();
				if(SODEODUtil.SOD_NAME.equals(sodOrEod))
				{
					sodEodService.updateBusinessDayM(sodEodTaskTDto.getBranch(),"E");
				}
				else if(SODEODUtil.EOD_NAME.equals(sodOrEod))
				{
					sodEodService.updateBusinessDayM(sodEodTaskTDto.getBranch(),"S");
				}
				
		}
	}

	private SodEodTaskTDto getSodEodTDto() throws Exception
	{
		SodEodTaskTDto sodEodTaskTDto = new SodEodTaskTDto();
	
		String tempBranch = getBranchCode();
		sodEodTaskTDto.setBranch(tempBranch);
		String businessDate = getBusinessDate();
		
		if(businessDate != null)
		{
			SimpleDateFormat valueDateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
			java.util.Date date = null;
			date = valueDateFormat.parse(businessDate);
			
			java.sql.Timestamp businessDateTimeStamp = new java.sql.Timestamp(date.getTime()); 
			sodEodTaskTDto.setBusinessDate(businessDateTimeStamp);
		}
		
		String sodOrEod = getStatus();
		if(SODEODUtil.SOD_NAME.equals(sodOrEod))
		{
			sodEodTaskTDto.setSodOrEod(SODEODUtil.SOD);
		}
		else if(SODEODUtil.EOD_NAME.equals(sodOrEod))
		{
			sodEodTaskTDto.setSodOrEod(SODEODUtil.EOD);
		}
	
		return sodEodTaskTDto;
	}
	
	/**
	* This method is used to get the branchCode from branchName and branchCode string
	* @return String tempBranch
	*/
	private String getBranchCode() throws Exception
	{
		String tempBranch = getBranch();
		String[] tempStr =	tempBranch.split("-");
		if(tempStr.length >0 && tempStr[0] != null)
		{
			tempBranch = tempStr[0];
		}
		return tempBranch;
	}
	
}
