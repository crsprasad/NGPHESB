package com.logica.ngph.validators.serviceImpl;

import java.util.List;

import org.apache.log4j.Logger;

import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.validators.doa.IValidatorDao;
import com.logica.ngph.validators.dto.MsgFormat;
import com.logica.ngph.validators.services.IMsgFormatDataInitializer;

public class MsgFormatDataInitializer implements IMsgFormatDataInitializer {

	static Logger logger = Logger.getLogger(MsgFormatDataInitializer.class);
	
	/*
	 * Creating Dependency Injection(IOC)
	 */
	private IValidatorDao daoObj;
	
	/**
	 * @param daoObj the daoObj to set
	 */
	public void setValidatorDao(IValidatorDao daoObj) {
		this.daoObj = daoObj;
	}
	
	public static List<MsgFormat> data=null;

	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.validators.services.IDataInitializer#getMsgFileds(java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<MsgFormat> getMsgFileds()
	{
		try
		{
			daoObj = (IValidatorDao)ApplicationContextProvider.getBean("validatorDao");
			data= daoObj.getMsgFiledsData();
			logger.info("List Object size is : " + data.size());
		}
		catch (Exception e) {
			logger.error(e, e);
		}
		return data;
	}
	
	/*
	 * Code Testing Purpose
	 */
	public static void main(String[] args) 
	{/*
		ApplicationContextProvider.initializeContextProvider();
		new MsgFormatDataInitializer().getMsgFileds();
		
	*/}

}
