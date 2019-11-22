/**
 * 
 */
package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * @author guptarb
 *
 */
public interface InterveneService {
	
	public void performRouting(NgphCanonical canonicalData)throws Exception;
	
}
