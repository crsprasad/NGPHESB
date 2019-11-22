package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;

public interface ForeignExchangeService {
	public void performForeignExchange(NgphCanonical ngphCanonical)throws Exception;
	
	
}
