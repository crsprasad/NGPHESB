package com.logica.ngph.esb;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author guptarb
 
 This is a Generic Class that will take QueueName as an Argument 
 and will put the message/Object in the Respective Queue.
*/ 

public class QueueHandler {

	QueueConnection conn;
    QueueSession session;
    Queue que;
    
	static Logger logger = Logger.getLogger(QueueHandler.class);

	/** Will Look Up for the Queue in Jboss Esb using JNDI and bind to the Particular RMI Port
	 * @param QueueName
	 * @throws JMSException
	 * @throws NamingException
	 */
    public void setupConnection(String QueueName) throws JMSException, NamingException
    {
        Properties properties1 = new Properties();
		properties1.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		properties1.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
		properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		
		InitialContext iniCtx = new InitialContext(properties1);

    	Object tmp = iniCtx.lookup("ConnectionFactory");
    	QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
    	conn = qcf.createQueueConnection();
    	que = (Queue) iniCtx.lookup("queue/" + QueueName);
    	session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    	conn.start();
    	logger.info("Connection Started");
    }
    
    public void stop() throws JMSException 
    { 
        conn.stop();
        session.close();
        conn.close();
    }
    
    
	public void sendAMessage(String msg) throws JMSException {
	    	
	        QueueSender send = session.createSender(que);        
	        ObjectMessage tm = session.createObjectMessage(msg);
	        
	        send.send(tm);        
	        send.close();
	    }
	
	/**
	 * This Method will take QueueName and Object as Arguments will invoke the Appropriate Queue 
	 * and will put the Message/Object in that Queue. 
	 * @param queueName
	 * @param object
	 */
	public void invokeQueue(String queueName, Object object)
	{
		try
		{
			setupConnection(queueName);
			sendAMessage(object.toString()); 
	    	stop(); 
		}
		catch (JMSException e) 
		{
			logger.error(e, e);
		}
		catch (NamingException e) 
		{
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
		
	}
    
	/**
	 * Main method for Testing Purpose
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception
    {    
    	QueueHandler sm = new QueueHandler();
    	String mes = "{A:ILCF01O707XXXUBIN0549584UTIB0000016111002XXXXXXXXXXXXXXXX2ILC2013011517142000007777XXXXXXXXXAA407722XXXXXXXX99}{4:"+
":20:32010ILC0000113"+
":21:NONREF"+
":52A:UBIN0532011"+
":31C:20130104"+
":30:20130115"+
":26E:1"+
":59:TCF-NADI INDUSTRIAL FANS PVT.LTD.UN"+
"GATE NO.3-57-58, THANTAKULAM ROAD,"+
"MADHAVARAM, CHENNAI-  600 060."+
":39A:0/0"+
":44E:CHENNAI"+
":44B:MUMBAI"+
"-}";
    	
    	//String mes = "0200F238448108C0800000000000000001001994850010097399253059000000000000010001204092550641977092550120460119010006109011131811826222ANB01080        ANB7299001080  35607300100247002003MOB045009sreekanth04900360705001790220017299001080056003ATM";
    	/*String mes = null;
		try {
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream("C:/MessageFormats/MT_103.txt");
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				  mes = mes + strLine + "\r\n"; 
			  logger.info (strLine);
			  }

			  //Close the input stream
			  in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		// Test Your Code by Putting the Queuename and Message/Object
		sm.invokeQueue("9001.TO.QNG", mes.toString());
    }
}
