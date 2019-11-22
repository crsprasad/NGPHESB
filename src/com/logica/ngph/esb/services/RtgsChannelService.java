package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * 
 * @author mohdabdulaa
 *
 */
public interface RtgsChannelService {
	
	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	public String buildRtgsMessage(NgphCanonical canonicalData)throws Exception;

}
