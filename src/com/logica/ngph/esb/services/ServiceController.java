package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.dtos.NgphGrpCanonical;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;

/**
 * 
 * @author mohdabdulaa
 *
 */
public interface ServiceController
{
	public void performPaymentProcessing(NgphCanonical canonicalData)throws Exception;
	
	public void performEntityControllerService(NgphCanonical canonicalData, String serviceId) throws Exception;
	
	public void performAutoRouterService(NgphCanonical canonicalData, String serviceId) throws Exception;
	
	public void performValidateService(NgphGrpCanonical ngphGrpCanonical,NgphCanonical canonicalData, String serviceId) throws Exception;
	
	public void checkFx(NgphCanonical canonicalData, String serviceId) throws Exception;
	
	public void performEnrichService(NgphCanonical canonicalData, String serviceId)throws Exception;
	
	public void performSwiftChannelService(NgphCanonical canonicalData) throws Exception;
	
	public void performInterveneService(NgphCanonical canonicalData, String serviceId)throws Exception;
	
	public void performSFMSChannelService(NgphCanonical canonicalData)throws Exception;
	
	public void performISO8583ChannelService(NgphCanonical canonicalData)throws Exception;
	
}
