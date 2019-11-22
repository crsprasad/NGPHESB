package com.logica.ngph.esb.services;

import java.util.ArrayList;

public interface STPFileUpload {

	public String doProcess(String data, String fileName) throws Exception;
	public String performexecute(String data, String fileName) throws Exception;
	public void logFileStatus(String fileName, String fileStatus) throws Exception;

}
