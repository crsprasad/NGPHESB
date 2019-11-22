package com.logica.ngph.validators.serviceImpl;

import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.validators.doa.IValidatorDao;
import com.logica.ngph.validators.dto.MsgField;
import com.logica.ngph.validators.services.IMsgFieldDataInitializer;

public class MsgFieldDataInitializer implements IMsgFieldDataInitializer{
	
	static Logger logger = Logger.getLogger(MsgFieldDataInitializer.class);
	
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
	
	public static List<MsgField> dataHolder=null;
	public static TreeMap<String, MsgField> data=null;
	
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.validators.services.IMsgFieldDataInitializer#getFileds()
	 */
	public TreeMap<String, MsgField> getFileds()
	{
		try
		{
			daoObj = (IValidatorDao)ApplicationContextProvider.getBean("validatorDao");
			dataHolder= daoObj.getFiledsData();
			data = cnvtToMap(dataHolder);
			logger.info("HashMap : " + data);
		}
		catch (Exception e) {
			logger.error(e, e);
		}
		return data;
	}
	
	/*
	 * 	Storing the values locally in a HashMap based on field no as key and value as ArrayList consisting of MsgFields Objects.
	 */
	private TreeMap<String, MsgField> cnvtToMap(List<MsgField> listObj)
	{
		TreeMap<String, MsgField> optMap = new TreeMap<String, MsgField>();
		
		MsgField fieldObj = null;
		for(int i=0;i<listObj.size();i++)
		{
			fieldObj = (MsgField)listObj.get(i);
			optMap.put(fieldObj.getFldNo() + fieldObj.getFldCompSeq(), fieldObj);
		}
		
		/*Iterator it = optMap.keySet().iterator(); 
		MsgField msgField = null;
		while(it.hasNext()) 
		{ 
			String key = it.next().toString(); 
			MsgField val = optMap.get(key);
			String format = val.getFldCompFmt();
			logger.info("Key : "  + key + " Format : " + format);

		}*/
		return optMap;
	}
	
	/*
	 * Code Testing Purpose
	 */
	
	public static void main(String[] args) 
	{/*
		ApplicationContextProvider.initializeContextProvider();
		new MsgFieldDataInitializer().getFileds();
		new MsgFieldDataInitializer().cnvtToMap(dataHolder);
		
		Iterator i = data.iterator();
		MsgField obj = null;
		 while (i.hasNext()) 
	        {
			 	obj = (MsgField)i.next();
	        	
			 	logger.info("Field Component Format--> " + obj.getFldCompFmt());
			 	logger.info("Field Component Mandatory/Optional -> "+ obj.getFldCompManOpt());
			 	logger.info("Field Comp Seq-> " + obj.getFldCompSeq());
			 	logger.info("Field No--> " + obj.getFldNo());
			 	logger.info("Length of Field--> " + obj.getLengthOfField());
			 	logger.info("No of Lines Present in field--> " + obj.getNoOfLines());
			 	logger.info("Is Slash Val--> " + obj.isSlash());
			 	logger.info("Is Pling Val-> " + obj.isPling());
			 	logger.info("Charater type -> " + obj.getCharType());
			 	
			 	logger.info("**********************************************************************");
	        	
	        }
		
	*/}

}
