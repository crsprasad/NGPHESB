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

import org.apache.log4j.Logger;

public class SwiftClient {
	
	static Logger logger = Logger.getLogger(SwiftClient.class);
	
	QueueConnection conn;
    QueueSession session;
    Queue que;
    
    
    public void setupConnection() throws JMSException, NamingException
    {
        Properties properties1 = new Properties();
		properties1.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		properties1.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
		properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		
		InitialContext iniCtx = new InitialContext(properties1);

    	Object tmp = iniCtx.lookup("ConnectionFactory");
    	QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
    	conn = qcf.createQueueConnection();
    	que = (Queue) iniCtx.lookup("queue/SwiftMessageQueueGW");
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
       
	public static void main(String args[]) throws Exception
    {   
    	SwiftClient sm = new SwiftClient();
    	sm.setupConnection();
    	
    	String mes = "";
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
		}
		sm.sendAMessage(mes.toString()); 
    	sm.stop();    	
    }

}
