package com.logica.ngph.esb.services;


import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.logica.ngph.esb.Dtos.SodEodTaskTDto;

public interface SodEodService {

	public Map<String,Object> getLocalBranch() throws Exception;

	public void setNextBusinessDay(SodEodTaskTDto sodEodDto)throws Exception;
	
	public void releaseWarehouse()throws Exception;
	
	public void openCloseInboundOutboundFeeds(SodEodTaskTDto sodEodDto,int status)throws Exception;
	
	public void validatePaymentNonfinalityStatus(String branchCode,SodEodTaskTDto sodEodDto) throws Exception;
	
	public void moveFinalStatusToHistoryTable(String branchCode,SodEodTaskTDto sodEodDto)throws Exception;
	
	public void deleteHistoryDataBeyond(String branchCode,SodEodTaskTDto sodEodDto)throws Exception;
	
	public void updateSodEodT(String errorMessage,String taskId)throws Exception;
	
	public void updateSodEodTComp(String taskId)throws Exception;
	
	public void updateBusinessDayM(String branchCode,String businessDayStatus)throws Exception;
	
	public List<Integer> getSodEodStatus(List<String> taskIdList)throws Exception;
	
	public void updateTaLimits()throws Exception;

	public List<String> getbranches()throws Exception;

	public Timestamp getCurBusDayforBranch(String branchCode)throws Exception;
}
