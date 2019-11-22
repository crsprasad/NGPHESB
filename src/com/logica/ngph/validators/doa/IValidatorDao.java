package com.logica.ngph.validators.doa;

import java.util.List;

import com.logica.ngph.validators.dto.MsgField;
import com.logica.ngph.validators.dto.MsgFormat;


public interface IValidatorDao {
	
	List<MsgFormat> getMsgFiledsData();

	List<MsgField> getFiledsData();

}
