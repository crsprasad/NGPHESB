package com.logica.ngph.esb.daos;

import java.util.Date;
import java.util.List;



import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.MsgsPolled;



public interface DBPollerServiceDao {
	
	public List<MsgsPolled> getMsgsPolled();
	
	//public PollerMessage getMessage(String msgRef);
	public String getBusinessDate(String branchName);
	public void performDBpoll(Date businessDate,String prevMsgStatus,String msgRef);
	
	public void insertEventMaster(String eventId);
	public NgphCanonical getCanonicalFromMessagesTx(String msgRef);
	//public void updatePollStatus(String pollerStatus,String msgRef);
			
	

}
