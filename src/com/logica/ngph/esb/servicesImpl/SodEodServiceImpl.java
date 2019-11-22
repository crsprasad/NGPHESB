package com.logica.ngph.esb.servicesImpl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.logica.ngph.esb.Dtos.SodEodTaskTDto;
import com.logica.ngph.esb.daos.SodEodDao;
import com.logica.ngph.esb.services.SodEodService;

public class SodEodServiceImpl implements SodEodService {
	
	static Logger logger = Logger.getLogger(SodEodServiceImpl.class);
	
	SodEodDao sodEodTaskDao;
	public SodEodDao getSodEodTaskDao() 
	{
		return sodEodTaskDao;
	}
	public void setSodEodTaskDao(SodEodDao sodEodTaskDao) {
		this.sodEodTaskDao = sodEodTaskDao;
	}
	
	public Map<String,Object> getLocalBranch() throws Exception
	{
		return sodEodTaskDao.getLocalBranch();
	}
	
	public void releaseWarehouse()throws Exception
	{
		
	}
	
	public void setNextBusinessDay(SodEodTaskTDto sodEodDto) throws Exception
	{
		sodEodTaskDao.setNextBusinessDate(sodEodDto);
	}

	public void validatePaymentNonfinalityStatus(String branchCode,SodEodTaskTDto sodEodDto) throws Exception
	{
		sodEodTaskDao.validatePaymentNonfinalityStatus(branchCode,sodEodDto);
	}

	public void moveFinalStatusToHistoryTable(String branchCode,SodEodTaskTDto sodEodDto) throws Exception
	{
		sodEodTaskDao.moveFinalStatusToHistoryTable(branchCode,sodEodDto);
	}

	public void openCloseInboundOutboundFeeds(SodEodTaskTDto sodEodDto,int status)throws Exception 
	{
		sodEodTaskDao.openCloseInboundOutboundFeeds(sodEodDto,status);
	}
	
	public void deleteHistoryDataBeyond(String branchCode,SodEodTaskTDto sodEodDto) throws Exception
	{
		sodEodTaskDao.deleteHistoryDataBeyond(branchCode,sodEodDto);
	}

	public void updateSodEodT(String errorMessage, String taskId) throws Exception
	{
		sodEodTaskDao.updateSodEodT(errorMessage, taskId);
	}

	public void updateSodEodTComp(String taskId)throws Exception 
	{
		sodEodTaskDao.updateSodEodTComp(taskId);
	}
	
	public void updateBusinessDayM(String branchCode,String businessDayStatus) throws Exception
	{
		sodEodTaskDao.updateBusinessDayM(branchCode, businessDayStatus);
	}

	public List<Integer> getSodEodStatus(List<String> taskIdList) throws Exception
	{
		return sodEodTaskDao.getSodEodStatus(taskIdList);
	}
	
	public void updateTaLimits()throws Exception
	{
		sodEodTaskDao.updateTaLimits();
	}
	
	public List<String> getbranches()throws Exception
	{
		return sodEodTaskDao.getbranches();
	}
	public Timestamp getCurBusDayforBranch(String branchCode)throws Exception
	{
		return sodEodTaskDao.getCurBusDayforBranch(branchCode);
	}

}
