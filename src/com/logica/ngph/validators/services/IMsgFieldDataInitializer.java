package com.logica.ngph.validators.services;

import java.util.TreeMap;

import com.logica.ngph.validators.dto.MsgField;

public interface IMsgFieldDataInitializer {

	/* 
	 * Fetches the Row Wise data from Ta_fields_Format table
	 */

	TreeMap<String, MsgField> getFileds();

}
