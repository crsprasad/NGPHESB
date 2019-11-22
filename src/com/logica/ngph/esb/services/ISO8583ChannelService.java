package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.ResponseBean;

/**
 * @author guptarb
 */

public interface ISO8583ChannelService {

	/*
	 *  Constructs the destination message in ISO 8583:1987 standards.
	 *  And puts into the destination ESB queue 
	 *  
	 *  This method takes a int Arguments which tells that we have to construct a message
	 *  as a Request Message or Response Message.
	 *  Request -> 0(Numeric Zero)
	 *  Response -> 1(Numeric One)
	 *  Verification Request->2
	 *	Verification Response->3
	 */
	
	public String createAndSendPaymentRequestOrResponse(NgphCanonical canonicalData, int req_res_Flag) throws Exception;
	
	public String createLogOnOrEcho(int req_res_Flag, int log_echo_Flag, NgphCanonical canonicalData) throws Exception;
	
	public void updateGlobalCacheMap(NgphCanonical canObj, ResponseBean resObj)throws Exception;

	public String getStan(String branch)throws Exception;
}
