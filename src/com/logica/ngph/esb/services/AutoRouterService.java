/**
 * 
 */
package com.logica.ngph.esb.services;

import java.util.List;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.Rules;

/**
 * @author mohdabdulaa
 *
 */
public interface AutoRouterService {
	
	public Rules traceMessageRoute(NgphCanonical canonicalData,	List<Rules> messageRules)throws Exception;
	
	public void performRouting(NgphCanonical canonicalData)throws Exception;

}
