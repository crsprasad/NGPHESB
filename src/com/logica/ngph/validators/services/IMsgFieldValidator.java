/**
 * 
 */
package com.logica.ngph.validators.services;

import java.util.Map;

/**
 * @author guptarb
 *
 */
public interface IMsgFieldValidator {
	
	String validate_Msg_Fields(Map<String, String> fieldMap,String msgChnlType, String srcMsgType, String  srcMsgSubType, String msgRef);

}
