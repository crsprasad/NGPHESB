package com.logica.ngph.esb;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.common.NGPHException;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.esb.services.ISO8583ChannelService;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.NGPHEsbUtils;

/**
 * @author guptarb
 */
public class TCPClient extends AbstractActionLifecycle 
{	
	static Logger logger = Logger.getLogger(TCPClient.class);
	
	public static Socket requestSocket;
	static String serverIP;
	public static TCPServer serverTCP;
	public static Thread tcpServerthread;
	public static String providerEsb;
	private EsbServiceDao esbServiceDao;
	
	//PrintWriter out;
	OutputStreamWriter out;
	String message;
	protected ConfigTree	_config;
	public TCPClient (ConfigTree config) { _config = config; } 
	public TCPClient () { }
	private static int port;
	private final static String propName = "System.properties";
	
	private SwiftParserDao swiftParserDao;
	private ISO8583ChannelService impsChannelService;
	
	/**
	 * @param impsChannelService the impsChannelService to set
	 */
	public void setImpsChannelService(ISO8583ChannelService impsChannelService) {
		this.impsChannelService = impsChannelService;
	}
	/**
	 * @param swiftParserDao the swiftParserDao to set
	 */
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) {
		this.swiftParserDao = swiftParserDao;
	}

	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}
	
	static 
	{
		//loading property file in memory
		Properties props = new Properties();
		try 
		{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			serverIP=props.getProperty("TCPServer");
			port=Integer.parseInt(props.getProperty("TCPport"));
			providerEsb = props.getProperty("impsServerEi");
			logger.info("TCP Server is : " + serverIP);
			logger.info("TCP Port is : " + port);
			//Commented to avoid the IMPS channel for PNB
			
			new TCPClient().connect();
			serverTCP = new TCPServer();
			tcpServerthread = new Thread(serverTCP);
			tcpServerthread.start();
			
		} 
		catch (IOException e) 
		{
			logger.error(e, e);
		}
		catch (Exception e) {
			logger.error(e, e);
		}
	}
	public boolean connect()throws Exception
	{
		boolean isConnectionEstablished = false;
		boolean isFirstTime = false;
		try 
		{
			if (TCPClient.requestSocket != null)
			{
				TCPClient.requestSocket.close();
				TCPClient.requestSocket = null;
			}
			
			else
			{
				//first time the socket is null so set this variable to true so that last communication time can be set if the connection is established and log on is sent.
				isFirstTime = true;
			}
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			//Thread.sleep(Integer.parseInt(esbServiceDao.getInitialisedValue("IMPSTIMEECHOINTV")) * 1000);
			TCPClient.requestSocket = new Socket(serverIP, port);
			isConnectionEstablished= true;
		} 
		catch (UnknownHostException e) 
		{
			//logger.error(e, e);
		}
		catch (IOException e) 
		{
			//logger.error(e, e);
		}
		catch (Exception e) 
		{
			//logger.error(e, e);
		}
		
		if(isConnectionEstablished==true)
		{
			swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
			if(providerEsb!=null)
			{
				try {
					swiftParserDao.updateEiStatus(providerEsb,1);
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
			logger.info("Calling LOGON");
			impsChannelService = (ISO8583ChannelService)ApplicationContextProvider.getBean("impsChannelService");
			String logOnMes=null;
			try 
			{
				logOnMes = impsChannelService.createLogOnOrEcho(0, 0, null);
			} 
			catch (NGPHException e) 
			{
				logger.error(e, e);
			}
			if(logOnMes!=null && StringUtils.isNotEmpty(logOnMes) && StringUtils.isNotBlank(logOnMes))
			{
				try 
				{
					new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, logOnMes);
					if (isFirstTime == true)
					{
						NGPHEsbUtils.lastCommTS = new Timestamp(Calendar.getInstance().getTimeInMillis());
					}
				} 
				catch (Exception e) 
				{
					logger.error(e, e);
				}
				
			}
			else
			{
				logger.warn("Null Message received by TCP Client after creating LOGON message");
			}
		}
		return isConnectionEstablished;
	}
	
	public void doProcess(Message mes) 
	{
		if(mes!=null)
		{
			message = mes.getBody().get().toString();
			try
			{
				Set<String> names = _config.getAttributeNames();
	  			for (String attrName : names) 
	  			{
	  				String value = _config.getAttribute(attrName);
	  				
	  				logger.info("Config Attribute value : " + value);
	  				
	  				if(attrName.matches("connectedEI"))
					{
	  					providerEsb = value;
	  					break;
					}
	  			}
	  			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
	  			//out = new PrintWriter(new OutputStreamWriter(requestSocket.getOutputStream()));
	  			if (requestSocket != null)
	  			{
	  				out = new OutputStreamWriter(requestSocket.getOutputStream());
	  				sendMessage(message);
	  			}
			}
			catch (Exception e) {
				logger.error(e, e);
			}
		}
		else
		{
			logger.warn("Null Message received by TCP Client");
		}
	}
	
	void sendMessage(String msg) 
	{
		try 
		{
			//out.write("header");
			//out.write("\r\n");
			out.write(msg);
			logger.info("Message has been sent by TCP Client ->" + msg);
			out.flush();
			logger.info("Flushed");
		} 
		catch (Exception e) 
		{
			logger.error(e, e);
		}
	}
}
