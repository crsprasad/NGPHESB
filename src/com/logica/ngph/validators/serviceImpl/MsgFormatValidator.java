package com.logica.ngph.validators.serviceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.validators.daoImpl.ValidatorDao;
import com.logica.ngph.validators.dto.MsgFormat;
import com.logica.ngph.validators.services.IMsgFieldValidator;
import com.logica.ngph.validators.services.IMsgFormatValidator;

/**
 * @author guptarb
 *
 */
public class MsgFormatValidator implements IMsgFormatValidator {
	
	static Logger logger = Logger.getLogger(MsgFormatValidator.class);
	
	private String chnlType;
	private String msgType;
	private String subMsgType;
	private String fieldNo;
	private String fieldMand;
	private String fieldSeq;
	private String fieldTagOpt;
	
	private IMsgFieldValidator msgFieldValidor;
	
	/**
	 * @param msgFieldValidor the msgFieldValidor to set
	 */
	public void setMsgFieldValidor(IMsgFieldValidator msgFieldValidor) {
		this.msgFieldValidor = msgFieldValidor;
	}
	
	private List<String> getFieldKeys(Map<String, String> fieldMap)
	{
		List<String> fieldKey = new ArrayList<String>();

		try
		{
	        Iterator iterator = fieldMap.keySet().iterator();
	       
	        while(iterator.hasNext())
	        {        
	            fieldKey.add(iterator.next().toString());
	        }
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
        
        return fieldKey;
	}
	/*
	 * (non-Javadoc)
	 * @see com.logica.ngph.validators.serviceImpl.IFieldValidor#validate_Field(java.util.Map)
	 */
	public String validate_Field(Map<String, String> fieldMap, String msgChnlType, String srcMsgType, String  srcMsgSubType, String msgRef)
	{
		boolean validate_Status = false;
		String errorMessage = null;  
		try
		{
		List<String> allFieldList = new ArrayList<String>(); 
		
		List<String> fieldKeyList = getFieldKeys(fieldMap);
		logger.info(fieldKeyList);
		
		//List<MsgFormat> list = MsgFormatDataInitializer.data;
		MsgFormat msgFieldObj=null;
		
		//Iterator i = list.iterator();
		String fieldValue=null;
		String tempFldNo=null;
		
		LabelledLoop:
        for(int i=0;i<fieldKeyList.size();i++)
        {
        	if (msgChnlType.equalsIgnoreCase("SWIFT"))
        	{
	        	if (fieldKeyList.get(i).startsWith("5") && (fieldKeyList.get(i).contains("A") || fieldKeyList.get(i).contains("B") || fieldKeyList.get(i).contains("C") || fieldKeyList.get(i).contains("D") || fieldKeyList.get(i).contains("K")))
	        	{
	        		tempFldNo = fieldKeyList.get(i).substring(0, fieldKeyList.get(i).length()-1);
	        	}
	        	else
	        	{
	        		tempFldNo = fieldKeyList.get(i);
	        	}
        	}
        	else
        	{
        		tempFldNo = fieldKeyList.get(i);
        	}
        	//msgFieldObj = (MsgFormat)i.next();
        	logger.info(msgChnlType+srcMsgType+srcMsgSubType+tempFldNo);
			msgFieldObj = ValidatorDao.hm.get(msgChnlType+srcMsgType+srcMsgSubType+tempFldNo);
        	if (msgFieldObj == null)
        	{
        		tempFldNo = fieldKeyList.get(i).substring(0, fieldKeyList.get(i).length()-1);
        		msgFieldObj = ValidatorDao.hm.get(msgChnlType+srcMsgType+srcMsgSubType+tempFldNo);
        		logger.info("MsgFormatValidator :: validate_Field() in msgFieldObj ::"+msgFieldObj.toString());
        	}
        	chnlType = msgFieldObj.getChnlType();
        	msgType = msgFieldObj.getMsgType();
        	subMsgType = msgFieldObj.getSubMsgType();
        	fieldNo = msgFieldObj.getFieldNo();
        	fieldMand = msgFieldObj.getFieldMand();
        	fieldSeq = msgFieldObj.getFieldSeq();
        	fieldTagOpt = msgFieldObj.getFieldTagOpt();

        	logger.info(chnlType + "\t" +msgType + "\t" + subMsgType + "\t" + fieldNo + "\t" + fieldMand + "\t" + fieldSeq + "\t" + fieldTagOpt);
        	
        	/* Check for the Occurrence of Extra Field in the Message
        	 * If Tag option is null then field has to Letter Option, hence store the field in allFieldList
        	 * Else
        	 * If Tag Option is not null, break the tag option and store them along with field value in allFieldList
        	 */
        	if(fieldTagOpt!=null)
    		{
    			String [] tagOpt = fieldTagOpt.split("~");
    			for(int k=0;k<tagOpt.length;k++)
    			{
    				fieldValue = fieldNo + tagOpt[k].trim();
    				//logger.info(fieldValue);
    				allFieldList.add(fieldValue);
    			}	
    		}
    		else
    		{
    			allFieldList.add(fieldNo);
    		}
    		
        	//  Check Mandatory Fields are present or not
        	if(fieldMand.equalsIgnoreCase("M"))
        	{
        		logger.info(fieldNo + " is Mandatory");
        		/*
        		 *  If tag option is present or not
        		 *  If Present check for all tag Options
        		 */
        		if(fieldTagOpt!=null)
        		{
        			logger.info("Field Tag Option Value Present");
        			
        			String[] fieldKeyArr = Arrays.copyOf(fieldKeyList.toArray(), fieldKeyList.toArray().length, String[].class);
        			
        			List<String> fieldTagOptionList = new ArrayList<String>();

        			String [] tagOpt = fieldTagOpt.split("~");
        			for(int k=0;k<tagOpt.length;k++)
        			{
        				fieldValue = fieldNo + tagOpt[k].trim();
        				logger.info(fieldValue);
        				fieldTagOptionList.add(fieldValue);
        			}	
        			
        				String fieldKeyArrVal =null;
        			
        				for(int a=0;a<fieldKeyArr.length;a++)
        				{
        					if(fieldKeyArr[a].startsWith(fieldNo))
        					{
        						fieldKeyArrVal = fieldKeyArr[a];
        						break;
        					
        					}	
        				}
        				if(fieldKeyArrVal==null)
        				{
        					errorMessage = NgphEsbConstants.NGPH_FMT0001; 
    						validate_Status=false;
    						break LabelledLoop;
        				}
        				else
        				{
        						if(fieldTagOptionList.contains(fieldKeyArrVal))
        						{
            						validate_Status=true;
        						}
        						else
        						{
        							errorMessage = NgphEsbConstants.NGPH_FMT0002; 
            						validate_Status=false;
            						break LabelledLoop;
        						}
        				}
           		}
        		/*
        		 * If Field tag Option is null
        		 * field No is the direct field to check in FieldKeyArrayList 
        		 */
        		else
        		{
        			logger.info("Field Tag Option Value Absent**");
        			
        			fieldValue = fieldNo;
        			if(fieldKeyList.contains(fieldValue))
                	{
                		validate_Status=true;
                	}
        			else
        			{
        				errorMessage = NgphEsbConstants.NGPH_FMT0001; 
        				validate_Status=false;
        				break LabelledLoop;
        			}
        		}
        	}
        	
        //}
 
        /*
         *  If Mandatory Field Case is Passed only then this code will be executed
         *  If validate_Status is already false, that means the IS Mandatory Field case is Failed, then this code will not be executed
         */
       /* if(validate_Status==true)
        {
        	for(int z=0;z<fieldKeyList.size();z++)
        	{
        		if(allFieldList.contains(fieldKeyList.get(z)))
        		{
        			logger.info("Present : " + fieldKeyList.get(z));
        			validate_Status=true;
        		}
        		else
        		{
        			logger.info("Field Value not Present : " + fieldKeyList.get(z));
    				validate_Status=false;
    				break;
        		}
        	}
        }*/
        }
        
        /*
         * Calling the Field Validate Service, 
         * as it should get invoked only when MsgFormatValidator Service returns true
         * Else no need to go For Field Validator Service
         */
        if(validate_Status==true)
        {
        	msgFieldValidor = (IMsgFieldValidator) ApplicationContextProvider.getBean("msgFieldValidator");
        	errorMessage = msgFieldValidor.validate_Msg_Fields(fieldMap,msgChnlType, srcMsgType, srcMsgSubType,msgRef);
        }
        
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			validate_Status = false;
		}
		logger.info("Error Message Returned from MsgFomratVal is : " + errorMessage);
		return errorMessage;
	}

	/*
	 * Code Testing Purpose
	 */
	public static void main(String[] args) 
	{/*
		Map<String,String> objMap = new HashMap<String, String>();
		objMap.put("20", "");
		objMap.put("13C", "");
		objMap.put("23B", "");
		objMap.put("23E", "");
		objMap.put("26T", "");
		objMap.put("32A", "");
		objMap.put("33B", "");
		objMap.put("36", "");
		objMap.put("50A", "");
		objMap.put("51A", "");
		objMap.put("52A", "");
		objMap.put("53A", "");
		objMap.put("54A", "");
		objMap.put("55A", "");
		objMap.put("56A", "");
		objMap.put("57A", "");
		objMap.put("59", "");// with A or no letter
		objMap.put("70", "");
		objMap.put("71A", "");
		objMap.put("71F", "");
		objMap.put("71G", "");
		objMap.put("72", "");
		objMap.put("77B", "");
		objMap.put("77T", "");
		
		ApplicationContextProvider.initializeContextProvider();
		List<MsgFormat> obj = new MsgFormatDataInitializer().getMsgFileds();
		new MsgFormatValidor().validate_Field(objMap);
	*/}
}
