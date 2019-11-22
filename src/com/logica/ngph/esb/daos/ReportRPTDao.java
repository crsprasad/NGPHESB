package com.logica.ngph.esb.daos;

import com.logica.ngph.common.dtos.NgphCanonical;

public interface ReportRPTDao {
	
	// check if records are present in the table or not
	String validateData(String msgref)throws Exception;
	
	// will insert into table if no records found else will update the current records
	void insertRPTParsedMessage(NgphCanonical canonicalObj,String action,String msgRef)throws Exception;

}
