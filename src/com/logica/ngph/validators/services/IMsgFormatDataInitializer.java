package com.logica.ngph.validators.services;

import java.util.List;

import com.logica.ngph.validators.dto.MsgFormat;

/**
 * @author guptarb
 *
 *A Generic Interface for Validator Service.
 */

public interface IMsgFormatDataInitializer {

	/* Fetches the Row Wise data from Ta_Msg_field table
	 * and Stores it in the MsgFormatMapper
	 * and Populates MsgFormatMapper Objects in List.
	 */
	 List<MsgFormat> getMsgFileds();
}
