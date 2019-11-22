/**
 * 
 */
package com.logica.ngph.esb.daos;

import java.util.List;

/**
 * @author guptarb
 *
 */
public interface RequestQueueHandlerDao {
	
	public String retrieveEICode(String providerESB)throws Exception;
	
	public List<String> getEsbProviders()throws Exception;
	
	public String getProviderEsb(String hostID)throws Exception;
	
	public String getInput_Dest_Queue(String srcQueue)throws Exception;
	
	public List<String> getOutput_SrcProviders()throws Exception;
	
	public String getOutput_Dest_Queue(String srcQueue)throws Exception;
	
	public List<String> FetchInput_Dest_Queue()throws Exception;
	
	public List<String> FetchOutput_Dest_Queue()throws Exception;
	
	public String getQueueInitializer()throws Exception;
	
	public String getDstHost(String dstid)throws Exception;
	
	public String getProviderList()throws Exception;
	
	public String getOutPutQueueByEI(String eiCode) throws Exception;

}
