package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * 
 * @author mohdabdulaa
 *
 */
public interface SFMSChannelService {
	
	/**
	 * 
	 * @param canonicalData
	 * @return
	 */
	public String buildRtgsMessage(NgphCanonical canonicalData)throws Exception;
	
	public String buildRtgsMessageforAck(AcknowledgementCanonical ackObj)throws Exception;
	
	public String buildRtgsMessageForInfoCan(InfoCanonical infoObj)throws Exception;
	
	public void populatefieldsForCanonical()throws Exception;


}
