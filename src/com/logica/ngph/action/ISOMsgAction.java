package com.logica.ngph.action;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.ChargesDetailsDto;
import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.common.enums.PaymentStatusEnum;
import com.logica.ngph.common.utils.NGPHUtil;
import com.logica.ngph.esb.ReportQueue;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.Dtos.ResponseBean;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.AcknowledgementsService;
import com.logica.ngph.esb.services.ISO8583ChannelService;
import com.logica.ngph.esb.services.SFMSChannelService;
import com.logica.ngph.esb.services.ServiceController;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;
import com.logica.ngph.utils.NGPHEsbUtils;
import com.logica.ngph.validators.services.IMsgFieldDataInitializer;
import com.logica.ngph.validators.services.IMsgFormatDataInitializer;
import com.logica.ngph.validators.services.IMsgFormatValidator;

public class ISOMsgAction extends AbstractActionLifecycle 
{
	protected ConfigTree	_config;
	public ISOMsgAction (ConfigTree config) { _config = config; } 
	public ISOMsgAction() { } 

	static Logger logger = Logger.getLogger(ISOMsgAction.class);
	private NgphCanonical canonicalObj = null;

	private SwiftParserDao swiftParserDao;
	private EsbServiceDao esbServiceDao;
	private ServiceController serviceController;
	private IMsgFormatValidator msgFormatValidator;
	private ISO8583ChannelService isoChannelService;
	private AcknowledgementsService acknowledgementsService;
	/**
	 * @param acknowledgementsService the acknowledgementsService to set
	 */
	public void setAcknowledgementsService(
			AcknowledgementsService acknowledgementsService) {
		this.acknowledgementsService = acknowledgementsService;
	}
	/**
	 * @return the esbServiceDao
	 */
	public EsbServiceDao getEsbServiceDao() {
		return esbServiceDao;
	}
	/**
	 * @param esbServiceDao the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}
	/**
	 * @param isoChannelService the isoChannelService to set
	 */
	public void setIsoChannelService(ISO8583ChannelService isoChannelService) {
		this.isoChannelService = isoChannelService;
	}

	private static IMsgFieldDataInitializer msgFieldDataInitializer;
	/**
	 * @return the msgFieldDataInitializer
	 */
	public static IMsgFieldDataInitializer getMsgFieldDataInitializer() {
		return msgFieldDataInitializer;
	}
	/**
	 * @return the msgFormatDataInitializer
	 */
	public static IMsgFormatDataInitializer getMsgFormatDataInitializer() {
		return msgFormatDataInitializer;
	}
	/**
	 * @return the sfmsChannelService
	 */
	public static SFMSChannelService getSfmsChannelService() {
		return sfmsChannelService;
	}
	/**
	 * @param msgFieldDataInitializer the msgFieldDataInitializer to set
	 */
	public static void setMsgFieldDataInitializer(
			IMsgFieldDataInitializer msgFieldDataInitializer) {
		ISOMsgAction.msgFieldDataInitializer = msgFieldDataInitializer;
	}
	/**
	 * @param msgFormatDataInitializer the msgFormatDataInitializer to set
	 */
	public static void setMsgFormatDataInitializer(
			IMsgFormatDataInitializer msgFormatDataInitializer) {
		ISOMsgAction.msgFormatDataInitializer = msgFormatDataInitializer;
	}
	/**
	 * @param sfmsChannelService the sfmsChannelService to set
	 */
	public static void setSfmsChannelService(SFMSChannelService sfmsChannelService) {
		ISOMsgAction.sfmsChannelService = sfmsChannelService;
	}

	private static IMsgFormatDataInitializer msgFormatDataInitializer;
	private static SFMSChannelService sfmsChannelService;

	/**
	 * @param msgFormatValidator the msgFormatValidator to set
	 */
	public void setMsgFormatValidator(IMsgFormatValidator msgFormatValidator) {
		this.msgFormatValidator = msgFormatValidator;
	}
	/**
	 * @param serviceController the serviceController to set
	 */
	public void setServiceController(ServiceController serviceController) {
		this.serviceController = serviceController;
	}

	/**
	 * @param swiftParserDao the swiftParserDao to set
	 */
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) {
		this.swiftParserDao = swiftParserDao;
	}
	
	private  HashMap<String, String> hexToDec= null;
	private  HashMap<String, String> fieldLen= null;
	
	private boolean secondBit=false;
	private String finalBit=null;
	private StringBuilder binaryRepOffinalBit=null;
	private String msgType=null;
	
	private String field2=null;
	private String field3=null;
	private String field4=null;
	private String field5=null;
	private String field6=null;
	private String field7=null;
	private String field8=null;
	private String field9=null;
	private String field10=null;
	private String field11=null;
	private String field12=null;
	private String field13=null;
	private String field14=null;
	private String field15=null;
	private String field16=null;
	private String field17=null;
	private String field18=null;
	private String field19=null;
	private String field20=null;
	private String field21=null;
	private String field22=null;
	private String field23=null;
	private String field24=null;
	private String field25=null;
	private String field26=null;
	private String field27=null;
	private String field28=null;
	private String field29=null;
	private String field30=null;
	private String field31=null;
	private String field32=null;
	private String field33=null;
	private String field34=null;
	private String field35=null;
	private String field36=null;
	private String field37=null;
	private String field38=null;
	private String field39=null;
	private String field40=null;
	private String field41=null;
	private String field42=null;
	private String field43=null;
	private String field44=null;
	private String field45=null;
	private String field46=null;
	private String field47=null;
	private String field48=null;
	private String field49=null;
	private String field50=null;
	private String field51=null;
	private String field52=null;
	private String field53=null;
	private String field54=null;
	private String field55=null;
	private String field56=null;
	private String field57=null;
	private String field58=null;
	private String field59=null;
	private String field60=null;
	private String field61=null;
	private String field62=null;
	private String field63=null;
	private String field64=null;
	private String field65=null;
	private String field66=null;
	private String field67=null;
	private String field68=null;
	private String field69=null;
	private String field70=null;
	private String field71=null;
	private String field72=null;
	private String field73=null;
	private String field74=null;
	private String field75=null;
	private String field76=null;
	private String field77=null;
	private String field78=null;
	private String field79=null;
	private String field80=null;
	private String field81=null;
	private String field82=null;
	private String field83=null;
	private String field84=null;
	private String field85=null;
	private String field86=null;
	private String field87=null;
	private String field88=null;
	private String field89=null;
	private String field90=null;
	private String field91=null;
	private String field92=null;
	private String field93=null;
	private String field94=null;
	private String field95=null;
	private String field96=null;
	private String field97=null;
	private String field98=null;
	private String field99=null;
	private String field100=null;
	private String field101=null;
	private String field102=null;
	private String field103=null;
	private String field104=null;
	private String field105=null;
	private String field106=null;
	private String field107=null;
	private String field108=null;
	private String field109=null;
	private String field110=null;
	private String field111=null;
	private String field112=null;
	private String field113=null;
	private String field114=null;
	private String field115=null;
	private String field116=null;
	private String field117=null;
	private String field118=null;
	private String field119=null;
	private String field120=null;
	private String field121=null;
	private String field122=null;
	private String field123=null;
	private String field124=null;
	private String field125=null;
	private String field126=null;
	private String field127=null;
	private String field128=null;
	
	private String fld001;
	private String fld001Len;
	private String fld001Value;

	private String fld002;
	private String fld002Len;
	private String fld002Value;
	
	private String fld046;
	private String fld046Len;
	private String fld046Value;

	private String fld045;
	private String fld045Len;
	private String fld045Value;

	private String fld047;
	private String fld047Len;
	private String fld047Value;

	private String fld049;
	private String fld049Len;
	private String fld049Value;

	private String fld050;
	private String fld050Len;
	private String fld050Value;

	private String fld051;
	private String fld051Len;
	private String fld051Value;

	private String fld054;
	private String fld054Len;
	private String fld054Value;

	private String fld056;
	private String fld056Len;
	private String fld056Value;
	
	private String fld059;
	private String fld059Len;
	private String fld059Value;
	
	private String fld062;
	private String fld062Len;
	private String fld062Value;
	
	private String providerESB =null;
	private String providerESBdirection =null;
	
	private String isoMessage =null;
	
	/**
	 * Default Configuration of Decimal to HexaDecimal Values.
	 * This HashMap takes Binary/Decimal value as 'Key' and its Corresponding 'value' as HexaDecimal Value.
	 * @return HashMap
	 */
	private HashMap<String, String> populatehexToDec()
	{
		HashMap<String, String> hexToDec = new HashMap<String, String>();
		
		hexToDec.put("0", "0000");
		hexToDec.put("1", "0001");
		hexToDec.put("2", "0010");
		hexToDec.put("3", "0011");
		hexToDec.put("4", "0100");
		hexToDec.put("5", "0101");
		hexToDec.put("6", "0110");
		hexToDec.put("7", "0111");
		hexToDec.put("8", "1000");
		hexToDec.put("9", "1001");
		hexToDec.put("A", "1010");
		hexToDec.put("B", "1011");
		hexToDec.put("C", "1100");
		hexToDec.put("D", "1101");
		hexToDec.put("E", "1110");
		hexToDec.put("F", "1111");

		return hexToDec;
	}

	private HashMap<String, String> populateFieldLength()
	{
		HashMap<String, String> lengthOfFileds = new HashMap<String, String>();
		
		//N..19
		lengthOfFileds.put("2", "2");
		lengthOfFileds.put("3", "6");
		lengthOfFileds.put("4", "12");
		//lengthOfFileds.put("5", "16");
		lengthOfFileds.put("5", "12");

		//lengthOfFileds.put("6", "16");
		lengthOfFileds.put("6", "12");
		
		lengthOfFileds.put("7", "10");
		//lengthOfFileds.put("8", "12");
		lengthOfFileds.put("8", "8");
		
		lengthOfFileds.put("9", "8");
		lengthOfFileds.put("10", "8");
		lengthOfFileds.put("11", "6");
		lengthOfFileds.put("12", "6");
		lengthOfFileds.put("13", "4");
		lengthOfFileds.put("14", "4");
		
		//lengthOfFileds.put("15", "8");
		lengthOfFileds.put("15", "4");
		
		lengthOfFileds.put("16", "4");
		lengthOfFileds.put("17", "4");
		lengthOfFileds.put("18", "4");
		lengthOfFileds.put("19", "3");
		lengthOfFileds.put("20", "3");
		lengthOfFileds.put("21", "3");
		lengthOfFileds.put("22", "3");
		lengthOfFileds.put("23", "3");
		lengthOfFileds.put("24", "3");
		lengthOfFileds.put("25", "2");
		lengthOfFileds.put("26", "2");
		lengthOfFileds.put("27", "1");
		//lengthOfFileds.put("28", "32");
		lengthOfFileds.put("28", "9");
		
		lengthOfFileds.put("29", "9");
		//lengthOfFileds.put("30", "32");
		lengthOfFileds.put("30", "9");
		
		lengthOfFileds.put("31", "9");
		//N..11
		lengthOfFileds.put("32", "2");
		//N..11
		lengthOfFileds.put("33", "2");
		//N..28
		lengthOfFileds.put("34", "2");
		lengthOfFileds.put("35", "37");
		lengthOfFileds.put("36", "104");
		lengthOfFileds.put("37", "12");
		lengthOfFileds.put("38", "6");
		lengthOfFileds.put("39", "2");
		//lengthOfFileds.put("40", "43");
		lengthOfFileds.put("40", "3");
		
		lengthOfFileds.put("41", "8");
		lengthOfFileds.put("42", "15");
		//Ans..99
		//lengthOfFileds.put("43", "2");
		lengthOfFileds.put("43", "40");
		
		//Ans....9999
		//lengthOfFileds.put("44", "4");
		lengthOfFileds.put("44", "25");
		
		//
		lengthOfFileds.put("45", "76");
		//Ans...999
		lengthOfFileds.put("46", "3");
		//Ans...999
		lengthOfFileds.put("47", "3");
		//Ans...999
		lengthOfFileds.put("48", "3");
		lengthOfFileds.put("49", "3");
		lengthOfFileds.put("50", "3");
		lengthOfFileds.put("51", "3");
		lengthOfFileds.put("52", "8");
		lengthOfFileds.put("53", "16");
		//Ans...120
		lengthOfFileds.put("54", "3");
		//Ans...999
		lengthOfFileds.put("55", "3");
		//N..43
		//lengthOfFileds.put("56", "2");
		
		//Ans...999
		lengthOfFileds.put("56", "3");
		
		//Ans...999
		lengthOfFileds.put("57", "3");
		//Ans...999
		lengthOfFileds.put("58", "3");
		
		//Ans...999
		lengthOfFileds.put("59", "3");
		//Ans...999
		lengthOfFileds.put("60", "3");
		//Ans...999
		lengthOfFileds.put("61", "3");
		//Ans...999
		lengthOfFileds.put("62", "3");
		//Ans..28
		//lengthOfFileds.put("63", "2");
		//Ans..999
		lengthOfFileds.put("63", "3");
		
		//Ans...999
		//lengthOfFileds.put("64", "3");
		
		lengthOfFileds.put("64", "8");
		
		lengthOfFileds.put("65", "1");
		//Ans...300
		//lengthOfFileds.put("66", "3");
		
		lengthOfFileds.put("66", "1");

		lengthOfFileds.put("67", "2");
		lengthOfFileds.put("68", "3");
		lengthOfFileds.put("69", "3");
		lengthOfFileds.put("70", "3");
		lengthOfFileds.put("71", "4");
		lengthOfFileds.put("72", "4");
		lengthOfFileds.put("73", "6");
		lengthOfFileds.put("74", "10");
		lengthOfFileds.put("75", "10");
		lengthOfFileds.put("76", "10");
		lengthOfFileds.put("77", "10");
		lengthOfFileds.put("78", "10");
		lengthOfFileds.put("79", "10");
		lengthOfFileds.put("80", "10");
		lengthOfFileds.put("81", "10");
		lengthOfFileds.put("82", "12");
		lengthOfFileds.put("83", "12");
		lengthOfFileds.put("84", "12");
		lengthOfFileds.put("85", "12");
		lengthOfFileds.put("86", "16");
		lengthOfFileds.put("87", "16");
		lengthOfFileds.put("88", "16");
		lengthOfFileds.put("89", "16");
		lengthOfFileds.put("90", "42");
		lengthOfFileds.put("91", "1");
		lengthOfFileds.put("92", "2");
		lengthOfFileds.put("93", "6");
		//N..11
		//lengthOfFileds.put("94", "2");
		lengthOfFileds.put("94", "7");
		
		lengthOfFileds.put("95", "42");
		lengthOfFileds.put("96", "16");
		lengthOfFileds.put("97", "17");
		lengthOfFileds.put("98", "25");
		//N..11
		lengthOfFileds.put("99", "2");
		lengthOfFileds.put("100", "11");
		lengthOfFileds.put("101", "17");
		//Ans..28
		lengthOfFileds.put("102", "2");
		//Ans..28
		lengthOfFileds.put("103", "2");
		//..100
		lengthOfFileds.put("104", "3");
		//..999
		lengthOfFileds.put("105", "3");
		//..999
		lengthOfFileds.put("106", "3");
		//..999
		lengthOfFileds.put("107", "3");
		//..999
		lengthOfFileds.put("108", "3");
		//..999
		lengthOfFileds.put("109", "3");
		//..999
		lengthOfFileds.put("110", "3");
		//..999
		lengthOfFileds.put("111", "3");
		//..999
		lengthOfFileds.put("112", "3");
		//..999
		lengthOfFileds.put("113", "3");
		//..999
		lengthOfFileds.put("114", "3");
		//..999
		lengthOfFileds.put("115", "3");
		//..999
		lengthOfFileds.put("116", "3");
		//..999
		lengthOfFileds.put("117", "3");
		//..999
		lengthOfFileds.put("118", "3");
		//..999
		lengthOfFileds.put("119", "3");
		//N...999
		lengthOfFileds.put("120", "3");
		//..999
		lengthOfFileds.put("121", "3");
		//..999
		lengthOfFileds.put("122", "3");
		//A..3
		//lengthOfFileds.put("123", "1");
		//..999
		lengthOfFileds.put("123", "3");

		lengthOfFileds.put("124", "3");
		//Ans...999
		lengthOfFileds.put("125", "3");
		//Ans...999
		lengthOfFileds.put("126", "3");
		//Ans...999
		lengthOfFileds.put("127", "3");
		//Ans...999
		//lengthOfFileds.put("128", "3");
		lengthOfFileds.put("128", "2");
		
		return lengthOfFileds;
	}
	private final static String propName = "System.properties";
	private static String cutoverNMCI = null;
	private static String logonNMC = null;
	private static String echoNMC = null;
	private static Properties props = null;
	static 
	{
		props = new Properties();
		try 
		{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			cutoverNMCI=props.getProperty("cutoverNMCI");
			logonNMC=props.getProperty("logonNMCI");
			echoNMC=props.getProperty("echoNMCI");
			logger.info("CUT OVER NMCI Val : " + cutoverNMCI);
		} 
		catch (IOException e) 
		{
			logger.error(e, e);
		}
		catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	//public void doProcess(String message)
	public void doProcess(Message message) throws Exception
	{
		if(message !=null)
		{
			canonicalObj = null;
			canonicalObj = new NgphCanonical();
			
			//Hard Coded value as 0810 does not have MsgPurposeCode
			canonicalObj.setMsgPurposeCode("900000");
			
			//initialize error Map in Canonical
			canonicalObj.setErrorCodeMap(NGPHEsbUtils.errorCodeMap);
		
			isoMessage = message.getBody().get().toString();
			//isoMessage = message;
			logger.info("Inside doProcess Method of ISOMSGACtion " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date()));
			swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
			serviceController = (ServiceController)ApplicationContextProvider.getBean("serviceController");
			msgFormatValidator = (IMsgFormatValidator)ApplicationContextProvider.getBean("msgFormatValidator");
		 
		 	String qngBlock = isoMessage.substring(isoMessage.indexOf("{999:")+5,isoMessage.length());
			String[] temp = qngBlock.substring(0, qngBlock.indexOf("}")).split(",");
			String msgRef =temp[0].trim(); 
			String hostID = temp[1].trim();
  			
  			canonicalObj.setMsgDirection(temp[2].trim());
  			logger.info("MsgDirection : " + temp[2]);
  			
  			canonicalObj.setMsgHost(hostID);
  			logger.info("hostID : " + hostID);
  			
  			//String msgRef = NGPHUtil.generateUUID();
  			canonicalObj.setMsgRef(msgRef);
  			logger.info("msgRef : " + msgRef);
  			
  			String eiType = swiftParserDao.getEiType(hostID);
  			String msgDirection = "I";
			if(eiType.equalsIgnoreCase("P"))
			{
				msgDirection ="I";
			}
			else if (eiType.equalsIgnoreCase("H"))
			{
				msgDirection ="O";
			}
			canonicalObj.setMsgDirection(msgDirection);
  			logger.info("MsgDirection : " + msgDirection);
			
  			swiftParserDao.insertRawMessage(hostID, msgRef, isoMessage,"IMPS",canonicalObj.getMsgDirection());
  			
  			// Gets the Populated HashMap (HexaDecimal/ Decimal Values) in a Local HaspMap at Class Level
  			//Fetch HexaDecimal Mappings
  			hexToDec = populatehexToDec();
		
  			//Populate Length of each Field
  			fieldLen = populateFieldLength();
		
  			msgType = isoMessage.substring(0,2);
  			logger.info("Message Type is : " + msgType);
  			canonicalObj.setSrcMsgType(msgType);
		
  			String subMsgType = isoMessage.substring(2,4);
  			logger.info("Sub mes type : " + subMsgType);
  			canonicalObj.setSrcMsgSubType(subMsgType );
  			
  			int iRet = swiftParserDao.msgIsReturn(canonicalObj.getSrcMsgType(), canonicalObj.getSrcMsgSubType(),canonicalObj.getMsgDirection());
			if (iRet > -1)
			{
				canonicalObj.setMsgIsReturn(iRet);
			}
			else
			{
				canonicalObj.setMsgIsReturn(0);
			}
		
  			String primaryBit = isoMessage.substring(4,20);
  			logger.info("Primary BIT is : " + primaryBit);
		
  			char [] arr = primaryBit.toCharArray();
  			binaryRepOffinalBit=new StringBuilder();
  			StringBuilder primaryBitBinaryVal = new StringBuilder();
  			for(int i=0;i<arr.length;i++)
  			{
  				primaryBitBinaryVal.append(hexToDec.get(arr[i]+""));
  			}
  			
  			String binaryValOfFirstBit = hexToDec.get(arr[0]+"");
  			logger.info("The Binary Format of First Bit Of Primary Bit '" + arr[0]+"' is : " + binaryValOfFirstBit);
  			if(binaryValOfFirstBit.startsWith("1"))
  			{
  				secondBit=true;
  			}
  			else
  			{
  				secondBit=false;
  			}
  			if(secondBit==true)
  			{
  				logger.info("Secondary Bit is Present");
  				String secondaryBit = isoMessage.substring(20,36);
  				logger.info("Secondary bit Val is : " + secondaryBit);
			
  				char [] scndBitarr = secondaryBit.toCharArray();
			
  				StringBuilder secondBitBinaryVal = new StringBuilder();
			
  				for(int i=0;i<scndBitarr.length;i++)
  				{
  					secondBitBinaryVal.append(hexToDec.get(scndBitarr[i]+""));
  				}
  				finalBit = primaryBit + secondaryBit;
  			}
  			else
  			{
  				logger.info("Secondary Bit does not Exists");
  				finalBit = primaryBit;
  			}
  			logger.info("Final Bit is : " + finalBit);
  			
  			char [] finalBitarr = finalBit.toCharArray();
  			for(int i=0;i<finalBitarr.length;i++)
  			{
  				binaryRepOffinalBit.append(hexToDec.get(finalBitarr[i]+""));			
  			}
  			logger.info("Binary Value of Final Bit '" + finalBit + "' is : " + binaryRepOffinalBit);
		
  			char [] binaryarr = binaryRepOffinalBit.toString().toCharArray();
		
  			List<String> presntFields = new ArrayList<String>();

  			for(int i=1;i<binaryarr.length;i++)
  			{
  				if(binaryarr[i]=='1')
  				{
  					presntFields.add((i+1)+"");
  				}
  			}
		
  			int startIndex = msgType.length() +subMsgType.length()+ finalBit.length();
  			logger.info("Starting index val is : " + startIndex);
  			isoChannelService = (ISO8583ChannelService)ApplicationContextProvider.getBean("impsChannelService");
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
  			boolean bFormatError = false;
  			logger.info("ISOMessage is :: "+isoMessage);
  			try
  			{
				Iterator it=null;
				for(it=presntFields.iterator();it.hasNext();)
				{
					int field = Integer.parseInt(it.next().toString());
					switch (field) 
					{
						case 2:
								String field2temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field2temp.length();
								field2 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field2temp)));
								startIndex = startIndex + field2.length();
								logger.info("field2 - > "+ field2);
								//Initially, the beneficiary account and mobile number fields are set as part of field 2 but later during field 120 parsing 
								//when it is discovered that this transaction is a P2A then it would be set again to a different value.
								canonicalObj.setBeneficiaryCustAcct(field2);
								canonicalObj.setBeneficiaryCustomerCtctDtls(field2.substring(field2.length()-10, field2.length()));
								canonicalObj.setUltimateCreditorID(field2);
								break;
						case 3:
								field3 = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field3.length();
								logger.info("field3 - > "+ field3);
								canonicalObj.setMsgPurposeCode(field3);
								String ordAcType = field3.substring(2, 4);
								String benAcType = field3.substring(4, 6);
								canonicalObj.setOrderingAcType(ordAcType);
								canonicalObj.setBeneficiaryAcType(benAcType);
								break;
						case 4: 
								field4 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								
								startIndex = startIndex + field4.length();
								logger.info("field4 - > "+ field4);
								BigDecimal dataVal = new BigDecimal(field4.toString().substring(0, field4.length()-2) + "." + field4.toString().substring(field4.length()-2));
								canonicalObj.setMsgAmount(dataVal);
								break;
						case 5: 
							 	field5 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field5.length();
							 	logger.info("field5 - > "+ field5);
							 	break;
						case 6: 
							 	field6 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field6.length();
							 	logger.info("field6 - > "+ field6);
							 	break;
						case 7: 
							 	field7 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field7.length();
							 	logger.info("field7 - > "+ field7);
								String MM = field7.substring(0,2);
								String DD = field7.substring(2,2+2);
								//int iDD = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
								//String DD = StringUtils.right("00" + Integer.toString(iDD),2);
								String HH = field7.substring(4,4+2);
								String mN = field7.substring(6,6+2);
								String SS = field7.substring(8,8+2);
								String YY = StringUtils.right("00" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)),2);
								Timestamp ts = Timestamp.valueOf("20" + YY + "-" + MM + "-" + DD + " " + HH + ":" + mN + ":" + SS);
								canonicalObj.setDrDateTime(ts);
								canonicalObj.setMsgValueDate(ts);
								canonicalObj.setLastModTime(ts);
							 	break;
						case 8: 
							 	field8 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field8.length();
							 	logger.info("field8 - > "+ field8);
							 	break;
						case 9: 
							 	field9 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field9.length();
							 	logger.info("field9 - > "+ field9);
							 	break;
						case 10: 
							 	field10 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field10.length();
							 	logger.info("field10 - > "+ field10);
							 	break;
						case 11:
								field11 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field11.length();
								logger.info("field11 - > "+ field11);
								canonicalObj.setSeqNo(field11);
								canonicalObj.setClrgSysReference(field11);
								break;
						case 12:
								field12 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field12.length();
								logger.info("field12 - > "+ field12);
								String YY1 = StringUtils.right("00" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)),2);
								String MM1 = StringUtils.right("00" + Integer.toString(Calendar.getInstance().get(Calendar.MONTH)),2);
								String DD1 = StringUtils.right("00" + Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),2);
								String HH1 = field12.substring(0,2);
								String mN1 = field12.substring(2,2+2);
								String SS1 = field12.substring(4,4+2);	
								Timestamp ts1 = Timestamp.valueOf("20" + YY1 + "-" + MM1 + "-" + DD1 + " " + HH1 + ":" + mN1 + ":" + SS1);
								canonicalObj.setDrDateTime(ts1);
								break;
						case 13:
								field13 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field13.length();
								logger.info("field13 - > "+ field13);
								break;
						case 14:
								field14 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field14.length();
								logger.info("field14 - > "+ field14);
								break;
						case 15:
								field15 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field15.length();
								logger.info("field15 - > "+ field15);
								break;
						case 16:
								field16 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field16.length();
								logger.info("field16 - > "+ field16);
								break;
						case 17:  
								field17 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field17.length();
								logger.info("field17 - > "+ field17);
								break;
						case 18:  
								field18 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field18.length();
								logger.info("field18 - > "+ field18);
								canonicalObj.setSvcLevelCode(field18);
								break;
						case 19:  
								field19 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field19.length();
								logger.info("field19 - > "+ field19);
								break;
						case 20:  
								field20 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field20.length();
								logger.info("field20 - > "+ field20);
								break;
						case 21:  
								field21 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field21.length();
								logger.info("field21 - > "+ field21);
								break;
						case 22:  
								field22 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field22.length();
								logger.info("field22 - > "+ field22);
								canonicalObj.setLclInstCode(field22);
								break;
						case 23:  
								field23 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field23.length();
								logger.info("field23 - > "+ field23);
								break;
						case 24:  
								field24 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field24.length();
								logger.info("field24 - > "+ field24);
								break;
						case 25:  
								field25 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field25.length();
								logger.info("field25 - > "+ field25);
								canonicalObj.setCatgPurposeCode(field25);
								break;
						case 26:  
								field26 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field26.length();
								logger.info("field26 - > "+ field26);
								break;
						case 27:  
								field27 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field27.length();
								logger.info("field27 - > "+ field27);
								break;
						case 28:  
								field28 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field28.length();
								logger.info("field28 - > "+ field28);
								break;
						case 29:  
								field29 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field29.length();
								logger.info("field29 - > "+ field29);
								break;
						case 30:  
								field30 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field30.length();
								logger.info("field30 - > "+ field30);
								break;
						case 31:  
								field31 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field31.length();
								logger.info("field31 - > "+ field31);
								break;
						case 32:  
								String field32temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field32temp.length();
								field32 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field32temp.trim())));
								startIndex = startIndex + field32.length();
								logger.info("field32 - > "+ field32);
								canonicalObj.setSenderBank(field32);
								canonicalObj.setSendingInst(field32);
								break;
						case 33:  
								String field33temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field33temp.length();
								field33 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field33temp)));
								startIndex = startIndex + field33.length();
								logger.info("field33 - > "+ field33);
								break;
						case 34:  
								String field34temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field34temp.length();
								field34 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field34temp)));
								startIndex = startIndex + field34.length();
								logger.info("field34 - > "+ field34);
								break;
						case 35:  
							 	field35 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field35.length();
							 	logger.info("field35 - > "+ field35);
								break;
						case 36:  
							 	field36 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field36.length();
							 	logger.info("field36 - > "+ field36);
								break;
						case 37:
								field37 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field37.length();
								logger.info("field37 - > "+ field37);
								//canonicalObj.setRelReference(field37);
								canonicalObj.setSndrTxnId(field37);
								canonicalObj.setTxnReference(field37);
								canonicalObj.setClrgSysReference(field37);
								break;
						case 38:
								field38 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field38.length();
								logger.info("field38 - > "+ field38);
								break;
						case 39:
								field39 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field39.length();
								logger.info("field39 - > "+ field39);
								if (canonicalObj.getSrcMsgType().equalsIgnoreCase("02"))
								{
									canonicalObj.setMsgErrorCode(field39);
								}
								else if (canonicalObj.getSrcMsgType().equalsIgnoreCase("04"))
								{
									canonicalObj.setReturnReasonCode(field39);
								}
								break;
						case 40:
								field40 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field40.length();
								logger.info("field40 - > "+ field40);
								break;
						case 41:  
								field41 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field41.length();
								logger.info("field41 - > "+ field41);
								canonicalObj.setInitiatorRemitReference(field41);
								break;
						case 42:  
								field42 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field42.length();
								logger.info("field42 - > "+ field42);
								canonicalObj.setInitiatingPartyID(field42);
								//For IMPS transaction the ordering customer account is his bank + his mobile number, so setting the same as his account
								if (canonicalObj.getMsgPurposeCode().startsWith("90"))
								{
									canonicalObj.setOrderingCustAccount(field42);
								}
								break;
						case 43:  
								field43 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field43.length();
								logger.info("field43 - > "+ field43);
								canonicalObj.setInitiatingPartyName(field43);
								break;
						case 44:  
								field44 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field44.length();
								logger.info("field44 - > "+ field44);
								break;
						case 45:  
								field45 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field45.length();
								logger.info("field45 - > "+ field45);
								break;
						case 46:  
								String field46temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field46temp.length();
								field46 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field46temp)));
								startIndex = startIndex + field46.length();
								logger.info("field46 - > "+ field46);
								ChargesDetailsDto obj = new ChargesDetailsDto();
								obj.setChargeAmount(field46);
								List<ChargesDetailsDto> chargesDetails = new ArrayList<ChargesDetailsDto>();
								chargesDetails.add(obj);
								canonicalObj.setChargesDetails(chargesDetails);
								break;
						case 47:  
								field47 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field47.length();
								logger.info("field47 - > "+ field47);
								break;
						case 48:  
								String field48temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field48temp.length();
								field48 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field48temp)));
								startIndex = startIndex + field48.length();
								logger.info("field48 - > "+ field48);
								break;
						case 49:
								field49 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field49.length();
								logger.info("field49 - > "+ field49);
								//get currency numeric code and fetch from DB its curr_code
								canonicalObj.setInstructedCurrency(swiftParserDao.getIsoCurr(field49));
								canonicalObj.setMsgCurrency(swiftParserDao.getIsoCurr(field49));
								break;
						case 50:
								field50 = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field50.length();
								logger.info("field50 - > "+ field50);
								break;
						case 51:
								field51 = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));
								startIndex = startIndex + field51.length();
								logger.info("field51 - > "+ field51);
								break;
						case 52: 
								 field52 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								 startIndex = startIndex + field52.length();
								 logger.info("field52 - > "+ field52);
								break;
						case 53: 
							 	field53 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field53.length();
							 	logger.info("field53 - > "+ field53);
							 	break;
						case 54: 
							 	field54 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field54.length();
							 	logger.info("field54 - > "+ field54);
							 	break;
						case 55: 
							 	field55 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field55.length();
							 	logger.info("field55 - > "+ field55);
							 	break;
						case 56: 
								String field56temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field56temp.length();
								field56 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field56temp)));
								startIndex = startIndex + field45.length();
								logger.info("field56 - > "+ field56);
								break;
						case 57: 
							 	field57 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field57.length();
							 	logger.info("field57 - > "+ field57);
							 	break;
						case 58: 
							 	field58 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field58.length();
							 	logger.info("field58 - > "+ field58);
							 	break;
						case 59:
								String field59temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field59temp.length();
								field59 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field59temp)));
								startIndex = startIndex + field59.length();
								logger.info("field59 - > "+ field59);
								break;
						case 60:
								field60 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field60.length();
								logger.info("field60 - > "+ field60);
								break;
						case 61:
								field61 = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field61.length();
								logger.info("field61 - > "+ field61);
								break;
						case 62: 
								String field62temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field62temp.length();
								field62 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field62temp)));
								startIndex = startIndex + field62.length();
								logger.info("field62 - > "+ field62);
								break;
						case 63: 
								String field63temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field63temp.length();
								field63 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field63temp)));
								startIndex = startIndex + field63.length();
								logger.info("field63 - > "+ field63);
								break;
						case 64: 
								String field64temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field64temp.length();
								field64 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field64temp)));
								startIndex = startIndex + field64.length();
								logger.info("field64 - > "+ field64);
								break;
						case 65: 
							 	field65 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field65.length();
							 	logger.info("field65 - > "+ field65);
							 	break;
						case 66: 
								String field66temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field66temp.length();
								field66 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field66temp)));
								startIndex = startIndex + field66.length();
								logger.info("field66 - > "+ field66);
								break;
						case 67: 
							 	field67 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field67.length();
							 	logger.info("field67 - > "+ field67);
							 	break;
						case 68: 
							 	field68 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field68.length();
							 	logger.info("field68 - > "+ field68);
							 	break;
						case 69:
								field69 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field69.length();
								logger.info("field69 - > "+ field69);
								break;
						case 70:
								field70 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field70.length();
								logger.info("field70 - > "+ field70);
								canonicalObj.setInitiatorRemitAdviceMethod(field70);
								break;
						case 71:
								field71 = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field71.length();
								logger.info("field71 - > "+ field71);
								break;
						case 72: 
								 field72 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								 startIndex = startIndex + field72.length();
								logger.info("field72 - > "+ field72);
								break;
						case 73: 
							 	field73 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field73.length();
								logger.info("field73 - > "+ field73);
							 	break;
						case 74: 
							 	field74 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field74.length();
								logger.info("field74 - > "+ field74);
							 	break;
						case 75: 
							 	field75 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field75.length();
								logger.info("field75 - > "+ field75);
							 	break;
						case 76: 
							 	field76 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field76.length();
								logger.info("field76 - > "+ field76);
							 	break;
						case 77: 
							 	field77 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field77.length();
								logger.info("field77 - > "+ field77);
							 	break;
						case 78: 
							 	field78 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field78.length();
								logger.info("field78 - > "+ field78);
							 	break;
						case 79:
								field79 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field79.length();
								logger.info("field79 - > "+ field79);
								break;
						case 80:
								field80 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field80.length();
								logger.info("field80 - > "+ field80);
								break;
						case 81:
								field81 = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field81.length();
								logger.info("field81 - > "+ field81);
								break;
						case 82: 
								 field82 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								 startIndex = startIndex + field82.length();
								logger.info("field82 - > "+ field82);
								break;
						case 83: 
							 	field83 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field83.length();
								logger.info("field83 - > "+ field83);
							 	break;
						case 84: 
							 	field84 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field84.length();
								logger.info("field84 - > "+ field84);
							 	break;
						case 85: 
							 	field85 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field85.length();
								logger.info("field85 - > "+ field85);
							 	break;
						case 86: 
							 	field86 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field86.length();
								logger.info("field86 - > "+ field86);
							 	break;
						case 87: 
							 	field87 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field87.length();
								logger.info("field87 - > "+ field87);
							 	break;
						case 88: 
							 	field88 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field88.length();
								logger.info("field88 - > "+ field88);
							 	break;
						case 89:
								field89 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field89.length();
								logger.info("field89 - > "+ field89);
								break;
						case 90:
								field90 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field90.length();
								logger.info("field90 - > "+ field90);
								if(canonicalObj.getMsgPurposeCode().equalsIgnoreCase("900000"))
								{
									parsefield90(field90);
								}
								break;
						case 91:
								field91 = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field91.length();
								logger.info("field91 - > "+ field91);
								break;
						case 92: 
								field92 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field92.length();
								logger.info("field92 - > "+ field92);
								break;
						case 93: 
							 	field93 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field93.length();
								logger.info("field93 - > "+ field93);
							 	break;
						case 94: 
								String field94temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field94temp.length();
								field94 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field94temp)));
								startIndex = startIndex + field94.length();
								logger.info("field94 - > "+ field94);
								break;
						case 95: 
							 	field95 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field95.length();
								logger.info("field95 - > "+ field95);
							 	break;
						case 96: 
							 	field96 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field96.length();
								logger.info("field96 - > "+ field96);
							 	break;
						case 97: 
							 	field97 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field97.length();
								logger.info("field97 - > "+ field97);
							 	break;
						case 98: 
							 	field98 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
							 	startIndex = startIndex + field98.length();
								logger.info("field98 - > "+ field98);
							 	break;
						case 99:
								String field99temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field99temp.length();
								field99 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field99temp)));
								startIndex = startIndex + field99.length();
								logger.info("field99 - > "+ field99);
								break;
						case 100:
								field100 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field100.length();
								logger.info("field100 - > "+ field100);
								break;
						case 101:
								field101 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field101.length();
								logger.info("field101 - > "+ field101);
								break;
						case 102:
								String field102temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field102temp.length();
								field102 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field102temp)));
								startIndex = startIndex + field102.length();
								logger.info("field102 - > "+ field102);
								canonicalObj.setOrderingCustAccount(field102);
								break;
						case 103:
								String field103temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field103temp.length();
								field103 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field103temp)));
								startIndex = startIndex + field103.length();
								logger.info("field103 - > "+ field103);
								canonicalObj.setBeneficiaryCustAcct(field103);
								break;
						case 104:
								field104 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field104.length();
								logger.info("field104 - > "+ field104);
								break;
						case 105:
								field105 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field105.length();
								logger.info("field105 - > "+ field105);
								break;
						case 106:
								field106 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field106.length();
								logger.info("field106 - > "+ field106);
								break;
						case 107:
								field107 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field107.length();
								logger.info("field107 - > "+ field107);
								break;
						case 108:
								field108 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field108.length();
								logger.info("field108 - > "+ field108);
								break;
						case 109:
								field109 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field109.length();
								logger.info("field109 - > "+ field109);
								break;
						case 110:
								field110 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field110.length();
								logger.info("field110 - > "+ field110);
								break;
						case 111:
								field111 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field111.length();
								logger.info("field111 - > "+ field111);
								break;
						case 112:
								field112 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field112.length();
								logger.info("field112 - > "+ field112);
								break;
						case 113:
								field113 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field113.length();
								logger.info("field113 - > "+ field113);
								break;
						case 114:
								field114 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field114.length();
								logger.info("field114 - > "+ field114);
								break;
						case 115:
								field115 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field115.length();
								logger.info("field115 - > "+ field115);
								break;
						case 116:
								field116 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field116.length();
								logger.info("field116 - > "+ field116);
								break;
						case 117:
								field117 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field117.length();
								logger.info("field117 - > "+ field117);
								break;
						case 118:
								field118 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field118.length();
								logger.info("field118 - > "+ field118);
								break;
						case 119:
								field119 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field119.length();
								logger.info("field119 - > "+ field119);
								break;
						case 120:
								String field120temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field120temp.length();
								field120 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field120temp.trim())));
								startIndex = startIndex + field120.length();
								logger.info("field120 - > "+ field120);
								
								//case handled specially for IMPS
								if(canonicalObj.getMsgPurposeCode().startsWith("90"))
								{
									bFormatError = !parsefield120(field120);
								}
								break;
						case 121:
								field121 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field121.length();
								logger.info("field121 - > "+ field121);
								break;
						case 122:
								field122 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field122.length();
								logger.info("field122 - > "+ field122);
								break;
						case 123:
								String field123temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field123temp.length();
								field123 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field123temp)));
								startIndex = startIndex + field123.length();
								logger.info("field123 - > "+ field123);
								break;
						case 124:
								field124 = isoMessage.substring(startIndex, startIndex + Integer.parseInt(fieldLen.get(field+""))); 
								startIndex = startIndex + field124.length();
								logger.info("field124 - > "+ field124);
								break;
						case 125:
								String field125temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field125temp.length();
								field125 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field125temp)));
								startIndex = startIndex + field125.length();
								logger.info("field125 - > "+ field125);
								break;
						case 126:
								String field126temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field126temp.length();
								field126 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field126temp)));
								startIndex = startIndex + field126.length();
								logger.info("field126 - > "+ field126);
								break;
						case 127:
								String field127temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field127temp.length();
								field127 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field127temp)));
								startIndex = startIndex + field127.length();
								logger.info("field127 - > "+ field127);
								break;
						case 128:
								String field128temp = isoMessage.substring(startIndex, (startIndex + Integer.parseInt(fieldLen.get(field+""))));  
								startIndex = startIndex + field128temp.length();
								field128 = isoMessage.substring(startIndex,(startIndex + Integer.parseInt(field128temp)));
								startIndex = startIndex + field128.length();
								logger.info("field128 - > "+ field128);
							break;
						default:
								break;
					}
				}
  			}
			catch (Exception e) 
			{
				logger.error("Exception occurred while parsing ISO8583 fields");
				bFormatError = true;
			}
			if (bFormatError == false)
			{
				//check if it is an IMPS Transaction
				if(canonicalObj.getMsgPurposeCode().startsWith("90"))
				{					
					canonicalObj.setMsgChnlType(esbServiceDao.getDstChnlType(canonicalObj.getMsgHost()));
					NgphCanonical orgCan = null;
					//check message type for 0200
					if(canonicalObj.getSrcMsgType()!=null && canonicalObj.getSrcMsgSubType()!=null && canonicalObj.getSrcMsgType().equalsIgnoreCase("02") && canonicalObj.getSrcMsgSubType().equalsIgnoreCase("00"))
					{
						//IMPS is always for customer accounts and never interbank, so setting the ordering type and beneficiary type
						canonicalObj.setOrderingType(NgphEsbConstants.ORDERING_TYPE_I);
						canonicalObj.setBeneficiaryType(NgphEsbConstants.ORDERING_TYPE_I);
						//beneficiary customer account should be set here itself because entity control might need it to find the branch and 
						//also in verification requests too the beneficiary name should go.
						try
						{
							if (canonicalObj.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.INWARD_PAYMENT))
							{
								ArrayList<String> data = null;
								if (canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER)
									|| canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ) || canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER))
								{
									data = (ArrayList<String>)esbServiceDao.getAcDetailsByMMIDAndMobile(canonicalObj.getBeneficiaryCustomerID(), canonicalObj.getBeneficiaryCustomerCtctDtls());
								}
								else if (canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER))
								{
									String brnCode = null;
									if (canonicalObj.getMsgBranch()== null)
									{	
										brnCode = esbServiceDao.findBranchCodeByBic(canonicalObj.getReceiverBank());
									}
									else
									{
										brnCode = canonicalObj.getMsgBranch();
									}
									if (brnCode == null)
									{
										EventLogger.logEvent("NGPHISOACT0034", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Inbound IMPS payment. Beneficiary IFSC is invalid.
										canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0008);
									}
									else
									{
										data = (ArrayList<String>)esbServiceDao.getAcDetailsByAccountAndBranch(canonicalObj.getBeneficiaryCustAcct(), brnCode, 0);
									}
								}
								if(data != null && data.size() > 0)
								{
									if (data.get(0)!=null)
									{
										canonicalObj.setBeneficiaryCustomerName(data.get(0));
									}
									if (data.get(1) != null)
									{
										canonicalObj.setBeneficiaryCustAcct(data.get(1));
										canonicalObj.setCustAccount(data.get(1));
									}
								}
								else
								{
									/*The above fetch is done only to make entity control identify the branch for request and populate beneficiary customer name for 
									the verification response, since verification response is returned from here itself and no service controller processing. 
									If the account is not found then the error in verification response is only "original transaction declined", which is done below. 
									For regular payments, the account fetch and enrichment + validation is happening in enrich service anyway, 
									which puts the appropriate error when fetch fails*/
									logger.error("Account details not found for the specified MMID and Mobile Number " + canonicalObj.getBeneficiaryCustomerID() + "  " + canonicalObj.getBeneficiaryCustomerCtctDtls());
									EventLogger.logEvent("NGPHISOACT0001", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Inbound IMPS payment. Account details not available for the MMID {beneficiaryCustomerID} and mobile number {beneficiaryCustomerCtctDtls}
								} 
							}
							else
							{
								//FIXME outward payment to set ordering customer details 
							}
						}
						catch (NGPHException e) 
						{
							logger.error(e, e);
							EventLogger.logEvent("NGPHISOACT0002", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Inbound IMPS payment. Exception occured while fetching Account details for the MMID {beneficiaryCustomerID} and mobile number {beneficiaryCustomerCtctDtls}
						}
						//check its a verification request by checking value of sub field47 of field 120
						//if(fld047Value!=null && StringUtils.isNotBlank(fld047Value)&& StringUtils.isNotEmpty(fld047Value))
						if (canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER) || canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER) || canonicalObj.getMsgTxnType().equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER))
						{
							if(StringUtils.isNotBlank(canonicalObj.getClrgSysReference()) && StringUtils.isNotEmpty(canonicalObj.getClrgSysReference()))
							{
								String QNGRef = swiftParserDao.getIMPSMsgRef(canonicalObj.getClrgSysReference());
								if(QNGRef!=null) 
								{
									ResponseBean resBean = null;
									resBean = esbServiceDao.getIMPSResponseBeans(QNGRef);
									if (resBean != null)
									{
										resBean.setVerSendCt(resBean.getVerSendCt() + 1);
									}
									else
									{
										resBean = new ResponseBean();
										resBean.setCanonicalObj(canonicalObj);
										resBean.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
										resBean.setMsgType(canonicalObj.getSrcMsgType());
										resBean.setMsgSubType(canonicalObj.getSrcMsgSubType());
										resBean.setMsgDirection(canonicalObj.getMsgDirection());

									}
									orgCan = swiftParserDao.getCanonicalFromMessagesTxforMsgRef(QNGRef);
									isoChannelService.updateGlobalCacheMap(canonicalObj, resBean);
									//check payment status
									//String paySts = swiftParserDao.getPayStatus(QNGRef);
									String paySts = null;
									if (orgCan != null)
									{
										paySts = orgCan.getMsgStatus();
									}
									if (paySts != null)
									{
										if (paySts.equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.SENT_TO_HOST_I)) || paySts.equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.COMPLETED_I)))
										{
											canonicalObj.setMsgErrorCode("00");
										}
										else if (paySts.equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I)) || paySts.equalsIgnoreCase(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_I)))
										{
											EventLogger.logEvent("NGPHISOACT0032", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Verification request processed but original transaction rejected, IMPS Ref {ClrgSysReference}
											canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0007);
										}
										else
										{
											EventLogger.logEvent("NGPHISOACT0006", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Verification request received for a yet-to-be sent to host IMPS payment {ClrgSysReference}
											//incrementing the PDECount by one
											canonicalObj.setPdeCount(canonicalObj.getPdeCount()+1);
											canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0002);
										}
									}
									else
									{
										EventLogger.logEvent("NGPHISOACT0018", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Original message not found for the RRN {ClrgSysReference} received in the repeat / verification request
										canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0001);
									}
								}
								else
								{
									EventLogger.logEvent("NGPHISOACT0018", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Original message not found for the RRN {ClrgSysReference} received in the repeat / verification request
									canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0001);
								}
								//calling iso Channel Service method
								try 
								{
									//setting destination Channel type and Sub Msg type
									canonicalObj.setDstMsgSubType("10");
									canonicalObj.setDstMsgType("02");
									//Sending ack back to the source of the message
									canonicalObj.setDstMsgChnlType(esbServiceDao.getDstChnlType(canonicalObj.getMsgHost()));
									canonicalObj.setDstEiId(canonicalObj.getMsgHost());
									canonicalObj.setBeneficiaryCustAcct(field2);
									canonicalObj.setRelReference(field37);
									canonicalObj.setMsgPurposeCode(field3);
									canonicalObj.setMsgDirection(NgphEsbConstants.OUTWARD_PAYMENT);
									//This is done just to clean up the response but should be sent in P2M 
									if (fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER))
									{
										canonicalObj.setOrderingCustAccount(null);
									}
									canonicalObj.setMsgPrevStatus(canonicalObj.getMsgStatus());
									canonicalObj.setMsgErrorCode(canonicalObj.getMsgErrorCode(),canonicalObj.getDstMsgChnlType());
									if (orgCan != null && orgCan.getMsgBranch() != null)
									{
										canonicalObj.setMsgBranch(orgCan.getMsgBranch());
									}
									else
									{
										canonicalObj.setMsgBranch(esbServiceDao.getInitialisedValue("DEFBRANCH"));
									}
									String isoResponse = isoChannelService.createAndSendPaymentRequestOrResponse(canonicalObj, 3);
									
									new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, isoResponse);
									EventLogger.logEvent("NGPHISOACT0003", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Verification response successfully sent for IMPS payment {ClrgSysReference} 
								} 
								catch (NGPHException e) 
								{
									EventLogger.logEvent("NGPHISOACT0004", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while creating Verification response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
								catch (Exception e) 
								{	
									EventLogger.logEvent("NGPHISOACT0005", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while sending Verification response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
							}
						}
						//If it is not a verification request then process as inward payment
						else
						{
							// Updating the Response Bean Object for Flag value=0
							ResponseBean resObj = null;
							resObj = new ResponseBean();
							//Set the Response bean members
							resObj.setCanonicalObj(canonicalObj);
							resObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
							resObj.setMsgType(canonicalObj.getSrcMsgType());
							resObj.setMsgSubType(canonicalObj.getSrcMsgSubType());
							resObj.setMsgDirection(canonicalObj.getMsgDirection());

							isoChannelService.updateGlobalCacheMap(canonicalObj, resObj);
							if (CheckDuplicate())
							{
								String QNGRef = swiftParserDao.getIMPSMsgRef(canonicalObj.getClrgSysReference());
								if(QNGRef!=null) 
								{
									orgCan = swiftParserDao.getCanonicalFromMessagesTxforMsgRef(QNGRef);
									if (orgCan != null)
									{
										canonicalObj.setMsgBranch(orgCan.getMsgBranch());
									}
								}
								canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0005);
								if (canonicalObj.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
								{
									canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_O));
								}
								else
								{
									canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_I));
								}
								swiftParserDao.insertParsedMessage(canonicalObj);
								new ReportQueue().QueueCall(canonicalObj);
								EventLogger.logEvent("NGPHISOACT0030", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG							
						  		EventLogger.logEvent("NGPHISOACT0031", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Duplicate IMPS message received
							}
							else
							{
								if (canonicalObj.getMsgErrorCode() != null)
								{
									if (canonicalObj.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.INWARD_PAYMENT))
									{
										canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
									}
									else
									{
										canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_O));
									}
								}
								swiftParserDao.insertParsedMessage(canonicalObj);
								new ReportQueue().QueueCall(canonicalObj);
								EventLogger.logEvent("NGPHISOACT0030", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG
								logger.info("***************Calling Service Controller***************");
								 //Calling Service Controller Service.Once the Parser has completed of processing the message.Next control is given to Service Controller for further Activities.
								if (canonicalObj.getMsgErrorCode() == null)
								{
									serviceController.performPaymentProcessing(canonicalObj);
									if (canonicalObj.getMsgErrorCode() == null)
									{
										EventLogger.logEvent("NGPHISOACT0007", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Your a/c no. {beneficiaryCustAcct} is credited by Rs. {MsgAmount} on {MsgValueDate} by a/c linked to mobile {OrderingCustomerCtctDtls} (IMPS Ref No {ClrgSysReference})
									}
								}
							}
							String autoIMPSRespond = esbServiceDao.getInitialisedValBranch("AUTORESPONDIMPS", canonicalObj.getMsgBranch());
							if (autoIMPSRespond.equalsIgnoreCase("Y"))
							{
								try
								{
									//By now the extra details required in the response 0210 message other than the info available in request  
									//namely response code (39), beneficiary name(sub field 46 of 120) would have been set. 
									//Remaining details and re-setting of original values are done here
									canonicalObj.setDstMsgSubType("10");
									canonicalObj.setDstMsgType("02");
									//Sending ack back to the source of the message
									canonicalObj.setDstMsgChnlType(esbServiceDao.getDstChnlType(canonicalObj.getMsgHost()));
									canonicalObj.setDstEiId(canonicalObj.getMsgHost());
									canonicalObj.setBeneficiaryCustAcct(field2);
									canonicalObj.setRelReference(field37);
									canonicalObj.setMsgPurposeCode(field3);
									canonicalObj.setMsgDirection(NgphEsbConstants.OUTWARD_PAYMENT);
									//This is done just to clean up the response but should be sent in P2M 
									if (fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ)|| fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER))
									{
										canonicalObj.setOrderingCustAccount(null);
									}
									canonicalObj.setMsgPrevStatus(canonicalObj.getMsgStatus());
									if (canonicalObj.getMsgBranch() == null)
									{
										canonicalObj.setMsgBranch(esbServiceDao.getInitialisedValue("DEFBRANCH"));
									}
									if (canonicalObj.getMsgErrorCode() == null)
									{
										canonicalObj.setMsgErrorCode("00");
									}
									else
									{
										canonicalObj.setMsgErrorCode(canonicalObj.getMsgErrorCode(),canonicalObj.getDstMsgChnlType());
									}
									String isoResponse = null;
									try 
									{	
										isoResponse = isoChannelService.createAndSendPaymentRequestOrResponse(canonicalObj, 1);
									}
									catch (NGPHException e) 
									{
										EventLogger.logEvent("NGPHISOACT0009", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while sending auto response for IMPS payment {ClrgSysReference}
										logger.error(e, e);
									}
									if (isoResponse != null)
									{
										new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, isoResponse);
										EventLogger.logEvent("NGPHISOACT0008", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Auto response for the IMPS payment {ClrgSysReference} sent
									}
								}
								catch (Exception e) 
								{
									EventLogger.logEvent("NGPHISOACT0010", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while creating auto response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
							}
							/* If Auto Response Flag is false.
							 * Create a new Response bean Object and populate Canonical and set in response bean.
							 * Set the Response bean object in Global txnRefResObj map.
							 */
							else
							{
								ResponseBean beanObj = null;
								try
								{
									//populate Canonical obejct
									canonicalObj.setDstMsgSubType("10");
									canonicalObj.setDstMsgType("02");
									//Sending ack back to the source of the message
									canonicalObj.setDstMsgChnlType(esbServiceDao.getDstChnlType(canonicalObj.getMsgHost()));
									canonicalObj.setDstEiId(canonicalObj.getMsgHost());
									canonicalObj.setRelReference(field37);
									canonicalObj.setMsgPurposeCode(field3);
									canonicalObj.setMsgDirection(NgphEsbConstants.OUTWARD_PAYMENT);
									//This is done just to clean up the response but should be sent in P2M 
									if (fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER))
									{
										canonicalObj.setOrderingCustAccount(null);
									}
									canonicalObj.setMsgPrevStatus(canonicalObj.getMsgStatus());
									if (canonicalObj.getMsgBranch() == null)
									{
										canonicalObj.setMsgBranch(esbServiceDao.getInitialisedValue("DEFBRANCH"));
									}
									
									//Create Response Bean Object and populate it
									beanObj = new ResponseBean();
									beanObj.setCanonicalObj(canonicalObj);
									beanObj.setMsgDirection(canonicalObj.getMsgDirection());
									beanObj.setMsgSubType(canonicalObj.getDstMsgSubType());
									beanObj.setMsgType(canonicalObj.getDstMsgType());
									beanObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
									//Setting response code as "Awaiting response from CBS" when there is no error code in canonical, which means payment sent to CBS
									if (canonicalObj.getMsgErrorCode() == null)
									{
										beanObj.setResCode("AW");
									}
									//Set the bean Object in global Map
									NGPHEsbUtils.populateCBSResponseMap(canonicalObj.getTxnReference(), beanObj);
								}
								catch (Exception e) {
									EventLogger.logEvent("NGPHISOACT0010", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while creating auto response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
							}
						}
					}
					//check message type for 0210
					else if(canonicalObj.getSrcMsgType()!=null && canonicalObj.getSrcMsgSubType()!=null && canonicalObj.getSrcMsgType().equalsIgnoreCase("02") && canonicalObj.getSrcMsgSubType().equalsIgnoreCase("10"))
					{
						if(canonicalObj.getClrgSysReference()!=null && StringUtils.isNotBlank(canonicalObj.getClrgSysReference()) && StringUtils.isNotEmpty(canonicalObj.getClrgSysReference()))
						{
							//get the Stan Value from CustTxnRef
							//String stan = canonicalObj.getClrgSysReference().substring(canonicalObj.getClrgSysReference().length()-6, canonicalObj.getClrgSysReference().length());
							String stan = canonicalObj.getClrgSysReference();
							if(NGPHEsbUtils.stanMsgMap.get(stan)!= null)
							{
								//get the MSgId From Global Cache
								String msgId = NGPHEsbUtils.stanMsgMap.get(stan);
								
								if(NGPHEsbUtils.msgIdResObjMap.get(msgId)!=null)
								{
									//fetch the Response Bean Object from Global Map
									ResponseBean obj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
									
									//updating the Bean Object
									obj.setResCode(field39);
									obj.setResTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
									//updating Global Cache
									if(obj!=null && msgId!=null && StringUtils.isNotBlank(msgId) && StringUtils.isNotEmpty(msgId))
									{
										NGPHEsbUtils.populateResponseObject(msgId, obj);
									}
									else
									{
										logger.info("Fail to populate Global Map as either MSg Id or ResObj is null for ISO Action");
									}
									
									//updating canonical Object
									NgphCanonical oldCanonical = obj.getCanonicalObj();
									//passing the AckCanonical to Acknowledgement Service
									acknowledgementsService = (AcknowledgementsService)ApplicationContextProvider.getBean("acknowledgementsService");
		                          	AcknowledgementCanonical acknowledgementCanonical = new AcknowledgementCanonical();		                          		
		                            try 
		                            {
		                            	 acknowledgementCanonical.setSrcEiId(hostID);
		                            	 //acknowledgementCanonical.setSeqNo(oldCanonical.getClrgSysReference().substring(oldCanonical.getClrgSysReference().length()-6, oldCanonical.getClrgSysReference().length()));
		                            	 if(field39.equalsIgnoreCase("00"))
		                            	 {
		                            		 EventLogger.logEvent("NGPHISOACT0011", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Positive response received for the outbound IMPS payment {ClrgSysReference}
		                            		 acknowledgementCanonical.setAckType("Y");
		                            	 }
		                            	 else
		                            	 {
		                            		 EventLogger.logEvent("NGPHISOACT0012", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Negative response received for the outbound IMPS payment {ClrgSysReference}
		                            		 acknowledgementCanonical.setAckType("N");
		                            	 }
		                            	 acknowledgementCanonical.setMsgId(NGPHUtil.generateUUID());
		                            	 acknowledgementCanonical.setAckReasonCode(field39);
		                            	 acknowledgementCanonical.setMsgTmstmp(oldCanonical.getLastModTime());
		                            	 acknowledgementCanonical.setSndrTxnId(oldCanonical.getClrgSysReference());
		                            	 acknowledgementCanonical.setSrcMsgType(canonicalObj.getSrcMsgType());
		                            	 acknowledgementCanonical.setSrcSubMsgType(canonicalObj.getSrcMsgSubType());
		                            	 acknowledgementsService.processAcknowledgement(acknowledgementCanonical, oldCanonical);
		                            	 EventLogger.logEvent("NGPHISOACT0013", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Your a/c no. {orderingCustAccount} is debited for Rs.{MsgAmount} on {MsgValueDate} and a/c linked to mobile {beneficiaryCustomerCtctDtls} credited (IMPS Ref no {ClrgSysReference}).
		                            }
		                            catch (NGPHException e1) 
		                            {
		                            	 EventLogger.logEvent("NGPHISOACT0014", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Error occured while sending ack message to host for the outbound IMPS payment {TxnReference}
										logger.error(e1, e1);
		                            }
									//update Payment Details
									try 
									{
										esbServiceDao.updatePaymentDetails(oldCanonical);
										//updating Report Data
										new ReportQueue().QueueCall(oldCanonical);
									} 
									catch (NGPHException e) 
									{ 
										 logger.error(e, e);
									}
									if(field39.equalsIgnoreCase("00"))
									{
										oldCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.FINALISED_O));
										oldCanonical.setMsgErrorCode(obj.getResCode());	
									}
									else if (field39.equalsIgnoreCase("M1"))
									{
										oldCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_BY_CHANNEL_O));
										logger.error("Beneficiary bank declined the transaction due to unavailability of MMID and mobile number" + oldCanonical.getClrgSysReference() + " -- " + oldCanonical.getMsgRef());
										EventLogger.logEvent("NGPHISOACT0022", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Your fund transfer for Rs. {MsgAmount} on {MsgValueDate} is declined as the beneficiary mobile no. or MMID is invalid (IMPS Ref no. {ClrgSysReference}).
									}
									else if (field39.equalsIgnoreCase("M0") || field39.equalsIgnoreCase("M2") || field39.equalsIgnoreCase("M3") || field39.equalsIgnoreCase("M4") || field39.equalsIgnoreCase("M5"))
									{
										oldCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_BY_CHANNEL_O));
										logger.error("Beneficiary bank declined the transaction for other reasons" + oldCanonical.getClrgSysReference() + " -- " + oldCanonical.getMsgRef());
										EventLogger.logEvent("NGPHISOACT0023", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Your fund transfer for Rs. {MsgAmount} on {MsgValueDate} is declined. Please refer to the beneficiary (IMPS Ref no. {ClrgSysReference}).
									}
									else if (field39.equalsIgnoreCase("M6") || field39.equalsIgnoreCase("08"))
									{
										oldCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_BY_CHANNEL_O));
										logger.error("Received response that the IMPS transaction could not processed" + oldCanonical.getClrgSysReference() + " -- " + oldCanonical.getMsgRef());
										EventLogger.logEvent("NGPHISOACT0024", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Your fund transfer for Rs. {MsgAmount} on {MsgValueDate} could not be processed. Please try later. (IMPS Ref no. {ClrgSysReference}).
									}
									else if (field39.equalsIgnoreCase("91"))
									{
										oldCanonical.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_BY_CHANNEL_O));
										logger.error("Received response that the IMPS transaction is timed out" + oldCanonical.getClrgSysReference() + " -- " + oldCanonical.getMsgRef());
										EventLogger.logEvent("NGPHISOACT0025", oldCanonical, ISOMsgAction.class, oldCanonical.getMsgRef());//Your fund transfer for Rs. {MsgAmount} on {MsgValueDate} is timed out. Please check with us. (IMPS Ref no. {ClrgSysReference}).
									}
									//update Payment Details
									try 
									{
										esbServiceDao.updatePaymentDetails(oldCanonical);
										//updating Report Data
										new ReportQueue().QueueCall(oldCanonical);
										//new ReportRPTAction().doProcess(oldCanonical);
									} 
									catch (NGPHException e) 
									{ 
										 logger.error(e, e);
									}
								}
								else
								{
									logger.error("Original message could not be found for the STAN received in the response message");
									EventLogger.logEvent("NGPHISOACT0016", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Original message not found for the RRN {ClrgSysReference} received in the response message.
								}
							}
							else
							{
								logger.error("Stan Value is Empty for Iso Action");
							}
						}
					}
					//Check for 0800
					else if(canonicalObj.getSrcMsgType()!=null && canonicalObj.getSrcMsgSubType()!=null && canonicalObj.getSrcMsgType().equalsIgnoreCase("08") && canonicalObj.getSrcMsgSubType().equalsIgnoreCase("00"))
					{
							try 
							{
								if(canonicalObj.getMsgErrorCode()==null)
								{
									canonicalObj.setMsgErrorCode("00");
								}
								canonicalObj.setDstEiId(canonicalObj.getMsgHost());
								canonicalObj.setDstMsgChnlType(canonicalObj.getMsgChnlType());
								canonicalObj.setDstMsgType("08");
								canonicalObj.setDstMsgSubType("10");
								
								ResponseBean resObj = null;
								resObj = new ResponseBean();
								resObj.setCanonicalObj(canonicalObj);
								resObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
								resObj.setMsgDirection(canonicalObj.getMsgDirection());
								isoChannelService.updateGlobalCacheMap(canonicalObj, resObj);
								
								String isoResMes = isoChannelService.createLogOnOrEcho(1, 0, canonicalObj);
								try 
								{
									new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, isoResMes);
								} 
								catch (Exception e) 
								{
									logger.error(e, e);
								}
								
								if(canonicalObj.getInitiatorRemitAdviceMethod().equalsIgnoreCase(cutoverNMCI))
								{
									//Call SODEOD Service..
									logger.info("SODEOD Service Starts...");
									swiftParserDao.updateEiStatus(canonicalObj.getMsgHost(),0);
									SOdEodAction sodEodAction = (SOdEodAction) ApplicationContextProvider.getBean("sodEodAction");
									sodEodAction.performSodEod();
									sodEodAction.performSodEod();
									swiftParserDao.updateEiStatus(canonicalObj.getMsgHost(),2);
								}
								else
								{
									swiftParserDao.updateEiStatus(canonicalObj.getMsgHost(),2);
								}
							}
							catch (NGPHException e) 
							{
								logger.error(e, e);
							}
					}
					//Check for 0810
					else if(canonicalObj.getSrcMsgType()!=null && canonicalObj.getSrcMsgSubType()!=null && canonicalObj.getSrcMsgType().equalsIgnoreCase("08") && canonicalObj.getSrcMsgSubType().equalsIgnoreCase("10"))
					{
						//Enable connection status for IMPS EI as logged on if the response code (field 39) is 00 AND create ack canonical and store, need not call service controller
						logger.info("In 0810 processing");
						if(canonicalObj.getClrgSysReference()!=null && StringUtils.isNotBlank(canonicalObj.getClrgSysReference()) && StringUtils.isNotEmpty(canonicalObj.getClrgSysReference()))
						{
							//get the Stan Value from CustTxnRef
							String stan = canonicalObj.getClrgSysReference();
							logger.info("The stan that gives problem is " + stan);
							if(NGPHEsbUtils.stanMsgMap.get(stan)!= null)
							{
								//get the MSgId From Global Cache
								String msgId = NGPHEsbUtils.stanMsgMap.get(stan);
								
								if(NGPHEsbUtils.msgIdResObjMap.get(msgId)!=null)
								{
									//fetch the Response Bean Object from Global Map
									ResponseBean obj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
									
									//updating the Bean Object
									obj.setResCode(field39);
									obj.setResTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
									//updating Global Cache
									if(obj!=null && msgId!=null && StringUtils.isNotBlank(msgId) && StringUtils.isNotEmpty(msgId))
									{
										NGPHEsbUtils.populateResponseObject(msgId, obj);
									}
									else
									{
										logger.info("Fail to populate Global Map as either MSg Id or ResObj is null for ISO Action");
									}
									if(field39.equalsIgnoreCase("00"))
									{
										if(canonicalObj.getInitiatorRemitAdviceMethod().equalsIgnoreCase(logonNMC))
										{
											NGPHEsbUtils.lastNPCILogOnTS = new Timestamp(Calendar.getInstance().getTimeInMillis());
										}
										else if(canonicalObj.getInitiatorRemitAdviceMethod().equalsIgnoreCase(echoNMC))
										{
											NGPHEsbUtils.lastNPCIECHOTS = new Timestamp(Calendar.getInstance().getTimeInMillis());
										}
										swiftParserDao.updateEiStatus(canonicalObj.getMsgHost(),2);
									}
									else
									{
										EventLogger.logEvent("NGPHISOACT0015", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Negative response received for Log on or Echo from IMPS.
									}
								}
								else
								{
									logger.info("Could not find the message in global map");
								}
							}
							else
							{
								logger.error("Original message could not be found for the STAN received in the response message");
								EventLogger.logEvent("NGPHISOACT0016", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Original message not found for the RRN {ClrgSysReference} received in the response message.
							}
						}
					}
					//Check for 04 type messages
					else if(canonicalObj.getSrcMsgType()!=null && canonicalObj.getSrcMsgSubType()!=null && canonicalObj.getSrcMsgType().equalsIgnoreCase("04"))
					{
						//If it is repeat request 
						if(canonicalObj.getSrcMsgSubType().equalsIgnoreCase("21"))
						{
							if(StringUtils.isNotBlank(canonicalObj.getClrgSysReference()) && StringUtils.isNotEmpty(canonicalObj.getClrgSysReference()))
							{
								String msgId=null;
								ResponseBean resObj = null;
								//String stan =canonicalObj.getClrgSysReference().substring(canonicalObj.getClrgSysReference().length()-6, canonicalObj.getClrgSysReference().length());
								String stan =canonicalObj.getClrgSysReference();
								if(stan != null && StringUtils.isNotBlank(stan)&& StringUtils.isNotEmpty(stan))
								{
									msgId = NGPHEsbUtils.stanMsgMap.get(stan);
									
									if(msgId!=null && StringUtils.isNotBlank(msgId)&& StringUtils.isNotEmpty(msgId))
									{
										resObj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
										resObj.setResTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
										resObj.setCanonicalObj(canonicalObj);
										int iCnt = resObj.getVerSendCt();
										iCnt++;
										resObj.setVerSendCt(iCnt);
										isoChannelService.updateGlobalCacheMap(canonicalObj, resObj);
									}
									else
									{
										logger.error("Response bean not found for the STAN of the reversal repeat request :" + stan);
										EventLogger.logEvent("NGPHISOACT0018", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Original message not found for the RRN {ClrgSysReference} received in the repeat / verification request.
									}
								}
								else
								{
									logger.error("Empty Stan is Received for Flag value 2");
								}
							}
							String paySts = swiftParserDao.getPayStatus(field37);
							
							//check payment status
							if(paySts!=null && paySts.equalsIgnoreCase("45"))
							{
								//setting destination Channel type and Sub Msg type
								canonicalObj.setDstMsgChnlType(esbServiceDao.getDstChnlType(canonicalObj.getMsgHost()));
								canonicalObj.setDstMsgSubType("30");
								canonicalObj.setDstMsgType("04");
								
								canonicalObj.getRelCanonical().setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RETURNED_O));
								
								//calling iso Channel Service method
								try 
								{
									String isoResponse = isoChannelService.createAndSendPaymentRequestOrResponse(canonicalObj, 3);
									new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, isoResponse);
									EventLogger.logEvent("NGPHISOACT0003", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Verification response successfully sent for IMPS payment {ClrgSysReference} 
								} 
								catch (NGPHException e) 
								{
									EventLogger.logEvent("NGPHISOACT0004", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while creating Verification response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
								catch (Exception e) 
								{	
									EventLogger.logEvent("NGPHISOACT0005", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while sending Verification response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
							}
							else
							{
								EventLogger.logEvent("NGPHISOACT0006", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Verification request received for a yet-to-be sent to host IMPS payment {ClrgSysReference}
								//incrementing the PDECount by one
								canonicalObj.setPdeCount(canonicalObj.getPdeCount()+1);
								canonicalObj.setDstMsgChnlType(canonicalObj.getMsgChnlType());
							}
						}
						//If it is not a repeat return request then process as inward return
						else if(canonicalObj.getSrcMsgSubType().equalsIgnoreCase("20"))
						{
							if (CheckDuplicate())
							{
								canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0005, canonicalObj.getMsgChnlType());
								if (canonicalObj.getMsgDirection().equalsIgnoreCase(NgphEsbConstants.OUTWARD_PAYMENT))
								{
									canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_O));
								}
								else
								{
									canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.DUPLICATES_I));
								}
								swiftParserDao.insertParsedMessage(canonicalObj);
								new ReportQueue().QueueCall(canonicalObj);
								EventLogger.logEvent("NGPHISOACT0030", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG							
						  		EventLogger.logEvent("NGPHISOACT0031", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Duplicate IMPS message received
							}
							else
							{
								swiftParserDao.insertParsedMessage(canonicalObj);
								new ReportQueue().QueueCall(canonicalObj);
								EventLogger.logEvent("NGPHISOACT0030", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//Payment data persisted into QNG
								// Updating the Response Bean Object for Flag value=0
								ResponseBean resObj = null;
								resObj = new ResponseBean();
								//Set the Response bean members
								resObj.setCanonicalObj(canonicalObj);
								resObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
								resObj.setMsgType(canonicalObj.getSrcMsgType());
								resObj.setMsgSubType(canonicalObj.getSrcMsgSubType());
								resObj.setMsgDirection(canonicalObj.getMsgDirection());
								isoChannelService.updateGlobalCacheMap(canonicalObj, resObj);
								
								logger.info("***************Calling Service Controller***************");
								 //Calling Service Controller Service.Once the Parser has completed of processing the message.Next control is given to Service Controller for further Activities.
								canonicalObj.getRelCanonical().setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.RETURNED_O));
								serviceController.performPaymentProcessing(canonicalObj);
								EventLogger.logEvent("NGPHISOACT0017", canonicalObj.getRelCanonical(), ISOMsgAction.class, canonicalObj.getMsgRef());//Your a/c no. {orderingCustAccount} is credited for Rs.{MsgAmount} on {MsgValueDate} for reversal of transaction (IMPS Ref no {ClrgSysReference}).
							}
							String autoIMPSRespond = esbServiceDao.getInitialisedValBranch("AUTORESPONDIMPS", canonicalObj.getMsgBranch());
							if (autoIMPSRespond.equalsIgnoreCase("Y"))
							{
								try
								{
									// by now the extra details required in the response 0210 message other than the info available in request  
									//namely response code (39), beneficiary name(sub field 46 of 120) would have been set. Message types and current time only to be set  
									canonicalObj.setDstMsgChnlType(esbServiceDao.getDstChnlType(canonicalObj.getMsgHost()));
									canonicalObj.setDstMsgSubType("30");
									canonicalObj.setDstMsgType("04");
									//Sending ack back to the source of the message
									canonicalObj.setDstEiId(canonicalObj.getMsgHost());
									canonicalObj.setBeneficiaryCustAcct(field2);
									canonicalObj.setRelReference(canonicalObj.getRelCanonical().getClrgSysReference());
									String isoResponse = isoChannelService.createAndSendPaymentRequestOrResponse(canonicalObj, 1);
									canonicalObj.setMsgPrevStatus(canonicalObj.getMsgStatus());
									
									try 
									{	//putting in Response queue
										new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, isoResponse);
										EventLogger.logEvent("NGPHISOACT0008", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Auto response for the IMPS payment {ClrgSysReference} sent
									}
									catch (Exception e) 
									{
										EventLogger.logEvent("NGPHISOACT0009", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while sending auto response for IMPS payment {ClrgSysReference}
										logger.error(e, e);
									}
								}
								catch (NGPHException e) 
								{
									EventLogger.logEvent("NGPHISOACT0010", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while creating auto response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
							}
							// If Auto Response is false
							else
							{
								ResponseBean beanObj = null;
								try
								{
									//populate Canonical Obejct
									canonicalObj.setDstMsgChnlType(esbServiceDao.getDstChnlType(canonicalObj.getMsgHost()));
									canonicalObj.setDstMsgSubType("30");
									canonicalObj.setDstMsgType("04");
									//Sending ack back to the source of the message
									canonicalObj.setDstEiId(canonicalObj.getMsgHost());
									canonicalObj.setRelReference(canonicalObj.getRelCanonical().getClrgSysReference());
									canonicalObj.setMsgPrevStatus(canonicalObj.getMsgStatus());
									
									//Create Response Bean Object and populate it
									beanObj = new ResponseBean();
									beanObj.setCanonicalObj(canonicalObj);
									beanObj.setMsgSubType(canonicalObj.getDstMsgSubType());
									beanObj.setMsgType(canonicalObj.getDstMsgType());
									beanObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
									beanObj.setMsgDirection(canonicalObj.getMsgDirection());
									
									//Set the bean Object in global Map
									NGPHEsbUtils.populateCBSResponseMap(canonicalObj.getTxnReference(), beanObj);
								}
								catch (Exception e) 
								{
									EventLogger.logEvent("NGPHISOACT0010", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while creating auto response for IMPS payment {ClrgSysReference}
									logger.error(e, e);
								}
							}
						}
						else if(canonicalObj.getSrcMsgSubType().equalsIgnoreCase("30"))
						{
							if(canonicalObj.getClrgSysReference()!=null && StringUtils.isNotBlank(canonicalObj.getClrgSysReference()) && StringUtils.isNotEmpty(canonicalObj.getClrgSysReference()))
							{
								//get the Stan Value from CustTxnRef
								//String stan = canonicalObj.getClrgSysReference().substring(canonicalObj.getClrgSysReference().length()-6, canonicalObj.getClrgSysReference().length());
								String stan = canonicalObj.getClrgSysReference();
								if(NGPHEsbUtils.stanMsgMap.get(stan)!= null)
								{
									//get the MSgId From Global Cache
									String msgId = NGPHEsbUtils.stanMsgMap.get(stan);
									
									if(NGPHEsbUtils.msgIdResObjMap.get(msgId)!=null)
									{
										//fetch the Response Bean Object from Global Map
										ResponseBean obj = NGPHEsbUtils.msgIdResObjMap.get(msgId);
										
										//updating the Bean Object
										obj.setResCode(field39);
										obj.setResTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
										//updating Global Cache
										if(obj!=null && msgId!=null && StringUtils.isNotBlank(msgId) && StringUtils.isNotEmpty(msgId))
										{
											NGPHEsbUtils.populateResponseObject(msgId, obj);
										}
										else
										{
											logger.error("Fail to populate Global Map as either MSg Id or ResObj is null for ISO Action");
										}
									}
								}
								else
								{
									logger.error("Original message not found for the STAN received in reversal response :" + stan);
									EventLogger.logEvent("NGPHISOACT0016", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Original message not found for the RRN {ClrgSysReference} received in the response message.
								}
							}
						}
					}
					//Any other type do normal processing
					else
					{
						swiftParserDao.insertParsedMessage(canonicalObj);
						new ReportQueue().QueueCall(canonicalObj);
						logger.info("***************Calling Service Controller***************");
						 //Calling Service Controller Service.Once the Parser has completed of processing the message.Next control is given to Service Controller for further Activities.
						serviceController.performPaymentProcessing(canonicalObj);
						EventLogger.logEvent("NGPHISOACT0019", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Payment processing of the IMPS message completed.
					}
				}
				//if it is not IMPS then do normal processing
				else
				{
					swiftParserDao.insertParsedMessage(canonicalObj);
					new ReportQueue().QueueCall(canonicalObj);
					logger.info("***************Calling Service Controller***************");
					 //Calling Service Controller Service.Once the Parser has completed of processing the message.Next control is given to Service Controller for further Activities.
					serviceController.performPaymentProcessing(canonicalObj);
					EventLogger.logEvent("NGPHISOACT0020", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Payment processing of the ISO message completed.
				}	
			}
			else
			{
				logger.error("ISO 8583 message format Validation Failed, Hence no More Processing");
		  		EventLogger.logEvent("NGPHISOACT0029", canonicalObj, SFMSAction.class, canonicalObj.getMsgRef());//ISO 8583 message format validation failed.
		  		swiftParserDao.updateMsgStatusForRaw_Msgs(1, msgRef, canonicalObj.getMsgErrorCode());
			}	
		}
		else
		{
			logger.error("Null Message Received by ISOMSgAction");
		}
	}
	
	// This method handles the parsing of filed 120. Field 120 is a special case and itself a set of multiple fields
	private boolean parsefield120(String field_120)
	{
		try
		{
			if(field_120!=null && StringUtils.isNotBlank(field_120)&& StringUtils.isNotEmpty(field_120))
			{
				int startIndex = 0;
	
				//001		Transaction Type
				fld001 = field_120.substring(startIndex, 3);
				startIndex = startIndex + fld001.length();
				
				fld001Len = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld001Len.length();
				
				fld001Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld001Len));
				startIndex = startIndex + fld001Value.length();
				canonicalObj.setMsgTxnType(fld001Value);
				
				logger.info(fld001 + "\t" + fld001Len + "\t" + fld001Value);
				
				//002		Product Indicator 
				fld002 = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld002.length();
				
				fld002Len = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld002Len.length();
				
				fld002Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld002Len));
				startIndex = startIndex + fld002Value.length();
				
				logger.info(fld002 + "\t" + fld002Len + "\t" + fld002Value);
				
				//045		Remitter Name
				fld045 = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld045.length();
				
				fld045Len = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld045Len.length();
				
				fld045Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld045Len));
				startIndex = startIndex + fld045Value.length();
				canonicalObj.setOrderingCustomerName(fld045Value);
				
				logger.info(fld045 + "\t" + fld045Len + "\t" + fld045Value);
	
				//046		Beneficiary Customer Name
				fld046 = field_120.substring(startIndex, startIndex + 3);
				//Checking the occurance of field 046 as this is an Optional Field.
				if(fld046.equalsIgnoreCase("046"))
				{
					startIndex = startIndex + fld046.length();
					
					fld046Len = field_120.substring(startIndex, startIndex + 3);
					startIndex = startIndex + fld046Len.length();
					
					fld046Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld046Len));
					startIndex = startIndex + fld046Value.length();
					
					canonicalObj.setBeneficiaryCustomerName(fld046Value);
					logger.info(fld046 + "\t" + fld046Len + "\t" + fld046Value);
				}
				
				//047
				fld047 = field_120.substring(startIndex, startIndex + 3);
				//Checking the occurance of field 047 as this is an Optional Field.
				if(fld047.equalsIgnoreCase("047"))
				{
					startIndex = startIndex + fld047.length();
					
					fld047Len = field_120.substring(startIndex, startIndex + 3);
					startIndex = startIndex + fld047Len.length();
					
					fld047Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld047Len));
					startIndex = startIndex + fld047Value.length();
					
					logger.info(fld047 + "\t" + fld047Len + "\t" + fld047Value);
				}
				//049		MAS
				fld049 = field_120.substring(startIndex, startIndex + 3);
				if(fld049.equalsIgnoreCase("049"))
				{
					startIndex = startIndex + fld049.length();
					
					fld049Len = field_120.substring(startIndex, startIndex + 3);
					startIndex = startIndex + fld049Len.length();
					
					fld049Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld049Len));
					startIndex = startIndex + fld049Value.length();
					
					logger.info(fld049 + "\t" + fld049Len + "\t" + fld049Value);
				}
				else
				{
					fld049Value = "000";
				}
				//050		Remitter NBIN + Mobile Number
				fld050 = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld050.length();
				
				fld050Len = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld050Len.length();
				
				fld050Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld050Len));
				startIndex = startIndex + fld050Value.length();
				canonicalObj.setOrderingCustomerId(fld050Value.substring(0, 7));
				canonicalObj.setOrderingCustomerCtctDtls(fld050Value.substring(fld050Value.length()-10, fld050Value.length()));
				
				if (fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER))
				{
					canonicalObj.setOrderingCustAccount(canonicalObj.getOrderingCustomerId() + "/" +canonicalObj.getOrderingCustomerCtctDtls());
				}
				canonicalObj.setInitiatingPartyAddress(fld045Value + "\r\n" + canonicalObj.getOrderingCustomerId() + "\r\n" +canonicalObj.getOrderingCustomerCtctDtls());
				canonicalObj.setBeneficiaryCustomerID(canonicalObj.getBeneficiaryCustAcct().substring(0, 4) + fld049Value);
				
				logger.info(fld050 + "\t" + fld050Len + "\t" + fld050Value);
	
				//051		Customer Transaction Reference
				fld051 = field_120.substring(startIndex, startIndex + 3);
				//Checking the occurance of field 051 as this is an Optional Field.
				if(fld051.equalsIgnoreCase("051"))
				{
					startIndex = startIndex + fld051.length();
					
					fld051Len = field_120.substring(startIndex, startIndex + 3);
					startIndex = startIndex + fld051Len.length();
					
					fld051Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld051Len));
					startIndex = startIndex + fld051Value.length();
					
					canonicalObj.setInstructionsForNextAgtText(fld051Value);
					if (fld001Value.equalsIgnoreCase("47"))
					{
						canonicalObj.setCustTxnReference(fld051Value);
					}
					
					logger.info(fld051 + "\t" + fld051Len + "\t" + fld051Value);
				}
	
				//054		Ordering Customer Address
				fld054 = field_120.substring(startIndex, startIndex + 3);
				//Checking the occurance of field 054 as this is an Optional Field.
				if(fld054.equalsIgnoreCase("054"))
				{
					startIndex = startIndex + fld054.length();
					
					fld054Len = field_120.substring(startIndex, startIndex + 3);
					startIndex = startIndex + fld054Len.length();
					
					fld054Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld054Len));
					startIndex = startIndex + fld054Value.length();
					
					canonicalObj.setOrderingCustomerAddress(fld054Value);
					logger.info(fld054 + "\t" + fld054Len + "\t" + fld054Value);
				}
				
				//056		Originating Channel
				fld056 = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld056.length();
				
				fld056Len = field_120.substring(startIndex, startIndex + 3);
				startIndex = startIndex + fld056Len.length();
				
				fld056Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld056Len));
				startIndex = startIndex + fld056Value.length();
				
				logger.info(fld056 + "\t" + fld056Len + "\t" + fld056Value);
				
				if (fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2A_VER))
				{
					//059		IFSC code
					fld059 = field_120.substring(startIndex, startIndex + 3);
					if (!fld059.equalsIgnoreCase("059"))
					{
						EventLogger.logEvent("NGPHISOACT0027", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Inbound IMPS payment. IFSC not available for the P2A transaction type.
						return false;
					}
					startIndex = startIndex + fld059.length();
					
					fld059Len = field_120.substring(startIndex, startIndex + 3);
					startIndex = startIndex + fld059Len.length();
					
					fld059Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld059Len));
					startIndex = startIndex + fld059Value.length();
					//Set the beneficiary IFSC
					String validateBnk = esbServiceDao.getInitialisedValue("VALIDATELOCBNK");
					if (validateBnk != null && validateBnk.equalsIgnoreCase("Y"))
					{
						String locBnkCode = esbServiceDao.getInitialisedValue("LOCBNKCODE");
						if (locBnkCode != null && !locBnkCode.equalsIgnoreCase(StringUtils.left(fld059Value, 4)))
						{
							canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0008);
							canonicalObj.setMsgStatus(PaymentStatusEnum.findPaymentStatusCodeByEnum(PaymentStatusEnum.REJECTED_I));
							EventLogger.logEvent("NGPHISOACT0033", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Beneficiary IFSC does not belong to bank.
						}
						logger.info("The local bank code fetched is :" + locBnkCode);
					}
					logger.info(fld059 + "\t" + fld059Len + "\t" + fld059Value);
					canonicalObj.setReceiverBank(fld059Value);
					canonicalObj.setBeneficiaryInstitution(fld059Value);
					
					//062
					fld062 = field_120.substring(startIndex, startIndex + 3);
					if (!fld062.equalsIgnoreCase("062"))
					{
						EventLogger.logEvent("NGPHISOACT0028", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Inbound IMPS payment. Account number not available for the P2A transaction type.
						return false;
					}
					startIndex = startIndex + fld062.length();
					
					fld062Len = field_120.substring(startIndex, startIndex + 3);
					startIndex = startIndex + fld062Len.length();
					
					fld062Value = field_120.substring(startIndex, startIndex + Integer.parseInt(fld062Len));
					startIndex = startIndex + fld062Value.length();
					//Set the beneficiary account and branch
					HashMap <String, String> brokenAcDetails = NGPHEsbUtils.getDerivedAccNum("BeneficiaryCustAcct", fld062Value);
					if (brokenAcDetails != null && brokenAcDetails.size()>0)
					{
						logger.info("Broken account number is " + brokenAcDetails.get("ACNUMINBeneficiaryCustAcct"));
						canonicalObj.setBeneficiaryCustAcct(brokenAcDetails.get("ACNUMINBeneficiaryCustAcct"));
						logger.info("Broken account branch is " + brokenAcDetails.get("BRNINBeneficiaryCustAcct"));
						canonicalObj.setMsgBranch(brokenAcDetails.get("BRNINBeneficiaryCustAcct"));
					}
					else
					{
						logger.info("Broken map is null");
						canonicalObj.setBeneficiaryCustAcct(fld062Value);
					}
					canonicalObj.setBeneficiaryInstitutionAcct(fld062Value);
					
					logger.info(fld062 + "\t" + fld062Len + "\t" + fld062Value);
				}
				else if (fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2P_VER) ||
						fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_REQ) || fld001Value.equalsIgnoreCase(NgphEsbConstants.IMPS_TXNTYPE_P2M_VER))
				{
					//Do Nothing
				}
				else
				{
					canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0004, "IMPS");
				}
			}
			else
			{
				logger.info("Field120 Vaue is not Present");
				return false;
			}
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			EventLogger.logEvent("NGPHISOACT0026", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while processing Field 120
			return false;
		}
		return true;
	}

	//Special case taken for field 90
	private boolean parsefield90(String s)
	{
		try
		{
			int startIndex=0;
			
			String orgMes = s.substring(startIndex, 4);
			startIndex = startIndex + orgMes.length();
			
			//construct retrefNo 
			String orgSerial = s.substring(startIndex, startIndex + 6);
			startIndex = startIndex + orgSerial.length();
			
			//month current year
			String localDate = s.substring(startIndex, startIndex + 4);;
			startIndex = startIndex + localDate.length();
			
			//time in HHMMSS
			String localTime = s.substring(startIndex, startIndex + 6);
			startIndex = startIndex + localTime.length();
	
			String acqInst = s.substring(startIndex, startIndex + 11);;
			startIndex = startIndex + acqInst.length();
			acqInst.trim();
	
			String forwardInst = s.substring(startIndex, startIndex + 11);;
			startIndex = startIndex + forwardInst.length();
			forwardInst.trim();
	
			
			logger.info(orgMes);
			logger.info(orgSerial);
			logger.info(localDate);
			logger.info(localTime);
			logger.info(acqInst);
			logger.info(forwardInst);
			
			if (!canonicalObj.getSrcMsgSubType().equalsIgnoreCase("30"))
			{
				String retRefNo = generateRetRefNo(localDate, localTime, orgSerial);
				
				//fetch MSgRef from IMPS based on this retRefNo
				String impsMsgRef = swiftParserDao.getIMPSMsgRef(retRefNo);
				
				//fetch Canonical Object from this MSGRef
				NgphCanonical impsCan = swiftParserDao.getCanonicalFromMessagesTxforMsgRef(impsMsgRef);
				canonicalObj.setRelReference(impsMsgRef);
			
				//set in Canonical Object as a variable holding another canonical
				if (impsCan != null)
				{
					canonicalObj.setRelCanonical(impsCan);
				}
				else
				{
					logger.error("Original payment could not be found for the return received :" + impsMsgRef);
					EventLogger.logEvent("NGPHISOACT0021", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Original payment could not be found for the return payment received with related reference {RelReference}
					return false;
				}
			}
		}
		catch (Exception e) 
		{
			logger.error(e,e);
			EventLogger.logEvent("NGPHISOACT0026", canonicalObj, ISOMsgAction.class, canonicalObj.getMsgRef());//Error occured while processing Field 120
			return false;
		}
		return true;
	}
	
	//YDDDHHSSSSSS
	//Last digit of the year + Julian date + hour + STAN
	private String generateRetRefNo(String localDate, String localTime, String orgSerial)
	{
		StringBuilder sb = new StringBuilder();
		 Calendar cal=Calendar.getInstance();
		 
		 //fetch current year and assign to a local variable
		 int localcurrYear = cal.get(Calendar.YEAR);
		 int localMonth = Integer.parseInt(localDate.substring(0, 2)) -1 ;
		 int localDt = Integer.parseInt(localDate.substring(localDate.length()-2, localDate.length()));
		 int localHr = Integer.parseInt(localTime.substring(0, 2));
		 int localMins = Integer.parseInt(localTime.substring(3, 4));;
		 int localSec = Integer.parseInt(localTime.substring(5, 6));
		 
		 //cal.set(2012, 10, 14, 12, 11, 31);
		 cal.set(localcurrYear, localMonth, localDt, localHr, localMins, localSec);

		 String year=cal.get(Calendar.YEAR) +"";
		 String hour = "0" + cal.get(Calendar.HOUR_OF_DAY);
		 String dayofyear = "000" + cal.get(Calendar.DAY_OF_YEAR)+"";
		
		 //Fetch last digit of year
		 String lastDgtOfyr = year.substring(year.length()-1, year.length());
		 sb.append(lastDgtOfyr);
		 
		 //fetch Julian date
		 String julianDate  = dayofyear.substring(dayofyear.length()-3, dayofyear.length());
		 sb.append(julianDate);

		 //fetch hour
		 String hr  = hour.substring(hour.length()-2, hour.length());
		 sb.append(hr);
		 
		 //append stan 
		 sb.append(orgSerial);
		 return sb.toString();
	}
	
	private boolean CheckDuplicate()throws Exception
	{
		//Check for reference duplicate
		int iCount = swiftParserDao.getIMPSMsgRefCount(canonicalObj.getClrgSysReference());
		if (iCount > 0)
		{
			return true;
		}
		else
		{
			if (esbServiceDao.getInitialisedValue("DATADUPCHKREQ").equalsIgnoreCase("Y"))
			{
				logger.info("Start checkDuplicatesByData");
				String dupFields = swiftParserDao.compose_dupVals(canonicalObj);
				if (swiftParserDao.IsDuplicateByData(dupFields) > 0)
				{
					canonicalObj.setMsgErrorCode(NgphEsbConstants.NGPH_IAE0006);
					return true;
				}
				logger.info("End checkDuplicatesByData");
			}
		}
		return false;
	}
	/**
	  *  This is the Default Exception Handler provided by ESB, mapping needs to be done in JbossEsb.xml
	  *  Whenever any Exception will Occurs Automatically this method will be invoked.
	  *  We need to set this method name as the property value for this class in JbossEsb.xml
	  */
	 
	  public void exceptionHandler(Message message, Throwable exception)
	  {
		  logger.error("=============================== ISO Parser ExceptionHandler Start==========================");
		  logger.error(message,exception);
		  logger.error("****************************** ISO Parser ExceptionHandler End ***************************");
	  }

	public static void main(String[] args) throws Exception 
	{/*
		ApplicationContextProvider.initializeContextProvider();

		msgFieldDataInitializer = (IMsgFieldDataInitializer) ApplicationContextProvider.getBean("msgFieldDataInitializer");
		msgFieldDataInitializer.getFileds();
		msgFormatDataInitializer = (IMsgFormatDataInitializer) ApplicationContextProvider.getBean("msgFormatDataInitializer");
		msgFormatDataInitializer.getMsgFileds();
		sfmsChannelService = (SFMSChannelService)ApplicationContextProvider.getBean("sfmsChannelService");
		sfmsChannelService.populatefieldsForCanonical();
		NGPHEsbUtils.populateErrorCodes();
		//String testMes = "0210F23844810AE0800000000000000001001990220010099860264849000000000000000000119120505165700000105165712054814019050610901121311700000100SBI26484        SBI9986026484  40STATE BANK OF INDIA      MOB9986026484IN35606900100245002003MOB045005Kumar04900378305001790220019986026484056003MOB{999:123456789123456789,9010,I}";
		//String Funds_Transfer_authorization_request ="0420F238440108C08000000000400000010019948500100998602648490000000000007890012050944450000040944451205481401906109011214421000004SBI25305        SBI9739925305  35602006419770523075958109011     0000000000000100246002003MOB046006JOHN A05001790221229739925305053005INDIA055001Y056003MOB{999:123456789123456789,9010,I}"; 
		String Funds_Transfer_authorization_request ="0200F238448108C0800000000000000001001994850010097399253059000000000000010001204092550641977092550120460119010006109011131811826277ANB01080ANB7299001080  35610700100248002003MOB045009sreekanth04900300005001790220017299001080056003MOB059011PUNB000620006201112838512838{999:123456789123456789,9010,I}";
		//String sfmsMs = "{A:RTGF01I298R90CANB0000001CANB0000001211000MURMURMURMURMURM2XXX2004121810152000000000XXXXXXXXXCANBH0335000001200}{4:\r\n"
							//+":2020:TRN0000000000001\r\n"
							//+":1076:Y\n\r"
							//+":6346:H00\r\n"
							//+"-}{999:1234567890123456,9010}";
		new ISOMsgAction().doProcess(Funds_Transfer_authorization_request);
		
	*/}
}
