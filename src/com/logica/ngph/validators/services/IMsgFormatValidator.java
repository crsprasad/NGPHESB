/**
 * 
 */
package com.logica.ngph.validators.services;

import java.util.Map;

/**
 * @author guptarb
 *
 */
public interface IMsgFormatValidator {
	
	String validate_Field(Map<String, String> fieldMap, String chnlType, String msgType, String  msgSubType, String msgRef);

}
