package com.logica.ngph.esb.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.logica.ngph.esb.Dtos.AuthMsgPolled;

public interface AuthStpServiceDao {
	
	public List<AuthMsgPolled> getMsgsPolled(String key) throws Exception;
	
	public Map<String, Object> getGroupInfo(List<AuthMsgPolled> vals, String key)throws Exception;
	
	public ArrayList<Object> getSupInfo(Map<String,Object> hm)throws Exception;
	
	public void insertData(List<Object> al,Map<String, Object> GroupInfo, String key, List<AuthMsgPolled> vals)throws Exception;
}
