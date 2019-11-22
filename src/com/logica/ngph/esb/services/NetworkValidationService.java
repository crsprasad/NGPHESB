package com.logica.ngph.esb.services;

import java.util.Map;

public interface NetworkValidationService {

	String validateNetworkRules(Map<String, String> fieldMap, String msgType, String subMsgType, String hostId) throws Exception;
}
