package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.dtos.NgphGrpCanonical;
/**
 * 
 * @author mohdabdulaa
 *
 */
public interface ValidateService {
	
	public void validateMessage(NgphGrpCanonical ngphGrpCanonical,	NgphCanonical canonicalData)throws Exception;

}
