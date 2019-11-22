package com.logica.ngph.esb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jpos.iso.ISOUtil;

import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.daos.SwiftParserDao;
import com.logica.ngph.utils.ApplicationContextProvider;

/** 
 * @author guptarb
 */

public class TCPServer implements Runnable 
{
	static Logger logger = Logger.getLogger(TCPServer.class);
	
	BufferedReader in=null;
	StringBuilder mes=null;
	TCPClient client=null;
	
	public TCPServer() {
		
	}

	boolean bReadResult=true;
	//boolean exitThread = false;
	boolean connectionStatus=false;

	private SwiftParserDao swiftParserDao;
	/**
	 * @param swiftParserDao the swiftParserDao to set
	 */
	public void setSwiftParserDao(SwiftParserDao swiftParserDao) {
		this.swiftParserDao = swiftParserDao;
	}
	private EsbServiceDao esbServiceDao;
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) 
	{
		this.esbServiceDao = esbServiceDao;
	}
	
	public void run() 
	{
		if (client==null)
		{
			client = new TCPClient();
		}
		while(true)
		{
			try 
			{
				do
				{
					bReadResult = readMessage();
				}while(bReadResult);
				//logger.info("Read message returned false so calling connect again");
				connectionStatus = client.connect();
			}
			catch (Exception e) 
			{
				if (TCPClient.requestSocket != null)
				{
					try 
					{
						TCPClient.requestSocket.close();
						TCPClient.requestSocket = null;
					} 
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
				}
			}
		}
	}
	private boolean readMessage()
	{
		try
		{
			if (TCPClient.requestSocket != null)
			{
				while (TCPClient.requestSocket.getInputStream() != null && TCPClient.requestSocket.getInputStream().available()> 0)
				{
					in = new BufferedReader(new InputStreamReader(TCPClient.requestSocket.getInputStream()));
					//Read the first four character
					
					int firstAsciiValue = in.read();
					int secondAsciiValue = in.read();
					int thirdAsciiValue = in.read();
					int fourthAsciiValue = in.read();
					
					logger.info("First Ascii Val : " + firstAsciiValue);
					logger.info("Second Ascii Val : " + secondAsciiValue);
					logger.info("Third Ascii Val : " + thirdAsciiValue);
					logger.info("Fourth Ascii Val : " + fourthAsciiValue);

					//Convert the Ascii value to Character Value
					char firstchar = (char) firstAsciiValue;
					char secondchar = (char) secondAsciiValue;
					char thirdchar = (char) thirdAsciiValue;
					char fourthchar = (char) fourthAsciiValue;
					
					//Concat the two Char vals to form a String val
					String charVals = firstchar+"" + secondchar+"" + thirdchar+"" + fourthchar+"";					
					logger.info("Character Value of First,Second, Third and Fourth bit : " + charVals);
					//Converting String in HexaDecimal Format to Decimal Format
					int decimalVal = Integer.parseInt(charVals,16);
					logger.info("Decimal Value : " + decimalVal);
					 
					//Instantiating new String Builder Object
					mes = new StringBuilder();
					 
					int asciiVal;
					char charVal;
					//run the loop upto Decimal Val and append into String builder Object
					for(int j=0;j<decimalVal;j++)
					{
						 asciiVal = in.read();
						 //Check whether the end of the stream has been reached 
						 if(asciiVal!= -1)
						 {
							 charVal = (char)asciiVal;
							 mes.append(charVal);
							 //logger.info("Message Constructed so Far : " + mes);
						 }
						 else
						 {
							 break;
						 }
					 }
					logger.info("Final Message Extracted : " + mes);
					//put the message in JMS Queue
					logger.info("Putting message in " + NgphEsbConstants.ReqHandlerQ +" Queue by TCP Client");
					
					esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
					String isTestFlag = esbServiceDao.getInitialisedValue("ISSIMULATOR");
					
					//If test
					if(isTestFlag!=null && StringUtils.isNotBlank(isTestFlag) && StringUtils.isNotEmpty(isTestFlag)&& isTestFlag.equalsIgnoreCase("Y"))
					{
						new ReportRPTClient().call(NgphEsbConstants.ReqHandlerQ, mes.toString());
					}
					//Production (NPCI)
					else
					{
						//Converting the Packed BCD Hexa Form to Hexa Form and pass control to Adapter for normal Hexa Processing..
						new ReportRPTClient().call(NgphEsbConstants.ReqHandlerQ, new String(ISOUtil.hex2byte(mes.toString())));
					}
					mes = null;
				}
			}
			else
			{
				if (TCPClient.providerEsb != null)
				{
					 swiftParserDao = (SwiftParserDao)ApplicationContextProvider.getBean("swiftParserDao");
					 swiftParserDao.updateEiStatus(TCPClient.providerEsb, 0);
					 return false;
				}
			}
		}
		catch (Exception e) 
		{
			//logger.error(e, e);
			return false;
		}
		return true;
	}
}
