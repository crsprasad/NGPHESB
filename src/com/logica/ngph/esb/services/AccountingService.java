package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.NgphCanonical;

public interface AccountingService {
	public void performAccounting(NgphCanonical canonicalData)throws Exception;
}
