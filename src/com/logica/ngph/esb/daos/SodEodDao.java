package com.logica.ngph.esb.daos;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.logica.ngph.esb.Dtos.SodEodTaskTDto;

public interface SodEodDao {

public Map<String,Object> getLocalBranch() throws Exception;
	
	public void setNextBusinessDate(SodEodTaskTDto sodEodTaskTDto)throws Exception;
	
	public void openCloseInboundOutboundFeeds(SodEodTaskTDto sodEodTaskTDto,int status)throws Exception;
	
	public void validatePaymentNonfinalityStatus(String branchCode,SodEodTaskTDto sodEodTaskTDto) throws Exception;
	
	public void moveFinalStatusToHistoryTable(String branchCode,SodEodTaskTDto sodEodTaskTDto)throws Exception;
	
	public void deleteHistoryDataBeyond(String branchCode,SodEodTaskTDto sodEodTaskTDto)throws Exception;
	
	public void updateSodEodT(String errorMessage,String taskId)throws Exception;
	
	public void updateSodEodTComp(String taskId)throws Exception;
	
	public void updateBusinessDayM(String branchCode,String status)throws Exception;
	
	public List<Integer> getSodEodStatus(List<String> taskIdList) throws Exception;
	
	public void updateTaLimits() throws Exception;
	
	public List<String> getbranches()throws Exception;
	
	public Timestamp getCurBusDayforBranch (String Branch) throws Exception;
}
