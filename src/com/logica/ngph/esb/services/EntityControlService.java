package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;

public interface EntityControlService {
	
	public void populateBranchAndDepartmentDetails(NgphCanonical canonicalData)throws Exception;
}
