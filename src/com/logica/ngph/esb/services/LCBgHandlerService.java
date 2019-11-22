package com.logica.ngph.esb.services;

import com.logica.ngph.common.dtos.NgphCanonical;

public interface LCBgHandlerService {

	public void doProcess(NgphCanonical canonicalData)throws Exception; 
	
}
