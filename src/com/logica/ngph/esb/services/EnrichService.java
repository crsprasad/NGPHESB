package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * 
 * @author mohdabdulaa
 *
 */
public interface EnrichService {
	
	public void performEnrichService(NgphCanonical canonicalData)throws Exception;

}
