package com.logica.ngph.esb.servicesImpl;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.logica.ngph.common.utils.NGPHUtil;
import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.RequestQueueHandlerDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.RequestQueueHandlerService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.EventLogger;

/**
 * @author guptarb
 * 
 * This Class will Listen any message send to the NGPH System.
 * It will route the appropriate message to the respective Queue based on some business rules.
 */
public class RequestQueueHandlerServiceImpl implements RequestQueueHandlerService 
{
	static Logger logger = Logger.getLogger(RequestQueueHandlerServiceImpl.class);	
	private String msgRef = null;
	private String hostId = null;
	private String finalMes = null;
	private final String propName = "System.properties";
	private String swiftPosition = null;
	private String swiftCharacter = null;
	private String sfmsPosition = null;
	private String sfmsCharacter = null;
	private String isoPosition = null;
	private String isoCharacter = null;
	private String destQName = null;
	private String iso20022Character = null;
	private String iso20022Version = null;
	private RequestQueueHandlerDao requestQueueHandlerDao;
	private SwiftParserDao swiftParserDao;

	/**
	 * @param requestQueueHandlerDao the requestQueueHandlerDao to set
	 */
	public void setRequestQueueHandlerDao(RequestQueueHandlerDao requestQueueHandlerDao) 
	{
		this.requestQueueHandlerDao = requestQueueHandlerDao;
	}

	public void execute(String message, String providerName, String direction) throws Exception
	{
		try
		{
			if(StringUtils.isNotBlank(providerName) && StringUtils.isNotEmpty(providerName))
			{
				//convert String array to ArrayList and fetch the Provider List from DB
				String []providersArr = requestQueueHandlerDao.getProviderList().split(";");
				ArrayList<String> providers = new ArrayList<String>();
				for(int i=0;i<providersArr.length;i++)
				{
					providers.add(providersArr[i].toString());
				}
				if(providers.contains(providerName))
				{
					EventLogger.logEvent("NGPHRHLSVC0006", null, RequestQueueHandlerServiceImpl.class, null);//QPH message received, forwarding to QPH.
					requestQueueHandlerDao = (RequestQueueHandlerDao)ApplicationContextProvider.getBean("requestQueueHandlerDao");
					destQName = requestQueueHandlerDao.getInput_Dest_Queue(providerName);
					logger.info("Input Destination queue is : " + destQName + " for Input Src Queue : " + providerName);
					if (destQName != null)
					{
						new ReportRPTClient().call(destQName, message);
					}
					else
					{
						logger.error("Destination queue name not configured for QPH message");
						EventLogger.logEvent("NGPHRHLSVC0007", null, RequestQueueHandlerServiceImpl.class, null);//Destination queue not configured for QPH message.
					}
				}
				else
				{	
					requestQueueHandlerDao = (RequestQueueHandlerDao)ApplicationContextProvider.getBean("requestQueueHandlerDao");
					swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");

					StringBuilder sb = new StringBuilder(message);
					hostId = requestQueueHandlerDao.retrieveEICode(providerName);
					msgRef = NGPHUtil.generateUUID();
					EventLogger.logEvent("NGPHRHLSVC0001", null, RequestQueueHandlerServiceImpl.class, msgRef);//Message received in QNG, processing start.
					
					String msgDirection = null;
					String eiType = swiftParserDao.getEiType(hostId);
					if(eiType.equalsIgnoreCase("P"))
					{
						msgDirection ="I";
					}
					else if (eiType.equalsIgnoreCase("H"))
					{
						msgDirection ="O";
					}
					sb.append("{999:" + msgRef + "," + hostId + ","+msgDirection +"}");
					finalMes = sb.toString();	
					
					Properties props = new Properties();
					props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
					
					swiftPosition = props.getProperty("SWIFTPosition");
					swiftCharacter = props.getProperty("SWIFTCharacter");
					sfmsPosition = props.getProperty("SFMSPosition");
					sfmsCharacter = props.getProperty("SFMSCharacter");
					isoPosition = props.getProperty("ISOPosition");
					isoCharacter = props.getProperty("ISOCharacter");
					logger.info("SwiftPosition : " + swiftPosition );
					logger.info("SwiftCharacter : " + swiftCharacter);
					logger.info("SFMSPosition : " + sfmsPosition);
					logger.info("SFMSCharacter : " + sfmsCharacter);
					logger.info("ISOPosition : " + isoPosition);
					logger.info("ISOCharacter : " + isoCharacter);
					String msgChnl = swiftParserDao.getHostFormat(hostId);
					//finalMes = StringUtils.replace(finalMes, "'", "`");
					//message = StringUtils.replace(message, "'", "`");
					if(Character.toString(message.charAt(Integer.parseInt(swiftPosition))).equalsIgnoreCase(swiftCharacter))
					{
						EventLogger.logEvent("NGPHRHLSVC0002", null, RequestQueueHandlerServiceImpl.class,msgRef);//SWIFT message received, triggering SWIFT processing.
						new ReportRPTClient().call(NgphEsbConstants.SWIFTQ, finalMes);
						
						logger.info("Inserting Raw Message for SWIFT with Message Reference : " + msgRef);
						swiftParserDao.insertRawMessage(hostId, msgRef, message,msgChnl,msgDirection);
						
					}
					else if(Character.toString(message.charAt(Integer.parseInt(sfmsPosition))).equalsIgnoreCase(sfmsCharacter))
					{
						EventLogger.logEvent("NGPHRHLSVC0003", null, RequestQueueHandlerServiceImpl.class,msgRef);//SFMS message received, triggering SFMS processing.
						new ReportRPTClient().call(NgphEsbConstants.SFMSQ, finalMes);
						
						logger.info("Inserting Raw Message for SFMS with Message Reference : " + msgRef);
						swiftParserDao.insertRawMessage(hostId, msgRef, message,msgChnl,msgDirection);
					}
					else if(Character.toString(message.charAt(Integer.parseInt(isoPosition))).equalsIgnoreCase(isoCharacter))
					{
						EventLogger.logEvent("NGPHRHLSVC0004", null, RequestQueueHandlerServiceImpl.class, msgRef);//ISO8583 message received, triggering ISO processing.
						new ReportRPTClient().call(NgphEsbConstants.ISO8583Q, finalMes);
						
						logger.info("Inserting Raw Message for IMPS with Message Reference : " + msgRef);
						swiftParserDao.insertRawMessage(hostId, msgRef, message,msgChnl,msgDirection);
					}
					else if(message.contains(iso20022Character)&&message.contains(iso20022Version)){
						new ReportRPTClient().call(NgphEsbConstants.ISO20022Q, message);
					}
					else	
					{
						EventLogger.logEvent("NGPHRHLSVC0005", null, RequestQueueHandlerServiceImpl.class, msgRef);//Un-recognised message received, No processing.
					}
				}
			}
			else
			{
				logger.info("Null providerName received by RequestQueueHandlerServiceImpl");
			}
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}
	}

}
