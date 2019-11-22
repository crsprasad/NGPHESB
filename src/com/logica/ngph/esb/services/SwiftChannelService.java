package com.logica.ngph.esb.services;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;

public interface SwiftChannelService {
	
	public String buildSwiftMessage(NgphCanonical canonicalData) throws Exception;

	public String buildSwiftMessageForAck(AcknowledgementCanonical ackCanonical) throws Exception;

}
