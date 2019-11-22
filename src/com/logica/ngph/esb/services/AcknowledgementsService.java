package com.logica.ngph.esb.services;

import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * 
 * @author guptarb
 *
 */
public interface AcknowledgementsService {
	
	public void processAcknowledgement(AcknowledgementCanonical ackCan, NgphCanonical canonical)throws Exception;
	public void processInfoAcknowledgement(AcknowledgementCanonical ackCan, InfoCanonical infoCanonical)throws Exception;

}
