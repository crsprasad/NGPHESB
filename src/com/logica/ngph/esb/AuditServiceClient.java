package com.logica.ngph.esb;

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

import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.EventAudit;

public class AuditServiceClient {

	private static QueueConnection conn;
	private static InitialContext iniCtx;
	private static QueueSession session;
    private static Queue que;
    
	static Logger logger = Logger.getLogger(AuditServiceClient.class);
	
	public static void initializeQueueFactory()
	{
		try    
		{
			setupConnection();
		}
		catch (Exception e) 
		{
			logger.info("Exception Occured while initializing Queues Connection");
			e.printStackTrace();
		}
	}

	/** Will Look Up for the Queue in Jboss Esb using JNDI and bind to the Particular RMI Port
	 * @param QueueName
	 * @throws JMSException
	 * @throws NamingException
	 */
    private static void setupConnection() throws JMSException, NamingException
    {
        Properties properties1 = new Properties();
		properties1.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		properties1.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
		properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		
		iniCtx = new InitialContext(properties1);

    	Object tmp = iniCtx.lookup("ConnectionFactory");
    	QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
    	conn = qcf.createQueueConnection();
    	que = (Queue) iniCtx.lookup("queue/AuditMessageQueueGW");
    	session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    	conn.start();
    	logger.info("EventLoggerQueueHandler has started connection for Queue AuditMessageQueueGW");
    }
    
	private void sendAMessage(EventAudit eventAudit) throws JMSException 
	{
		QueueSender send = session.createSender(que);
		ObjectMessage objMessage = session.createObjectMessage(eventAudit);
		send.send(objMessage);        
		send.close();
	}

	// A NEW Logger method impleneted to send audit message in queue
	public void sendEventLog(EventAudit eventAudit)
	{
		try
		{
			sendAMessage(eventAudit); 
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
	}

	public void dbPollerQueueCall(NgphCanonical ngphCanonical,String queueName,EventAudit eventAudit){

	try
	{
		sendAMessage(eventAudit); 
	}
	catch (Exception e) 
	{
		logger.error(e, e);
	}
}

	
	/**
	 * This Method will take QueueName and Object as Arguments will invoke the Appropriate Queue 
	 * and will put the Message/Object in that Queue. 
	 * @param queueName
	 * @param object
	 */
/*
public void dbPollerQueueCall(NgphCanonical ngphCanonical,String queueName, Object object)
	{
		try
		{
			sendAMessage(object.toString()); 
		}
		catch (Exception e) 
		{
             // log the information when logger is in debug mode 
            if(logger.isDebugEnabled())
            {
                  logger.debug(e);
            }
             // log the information when logger is in info mode 
            if(logger.isInfoEnabled())
            {
                  logger.info(e);   
            }
             // log the information when logger is in error mode 
            if(logger.isEnabledFor(Level.ERROR))
            {
                  logger.error(e);  
            }
		}
		
	}
    
	 * Main method for Testing Purpose
	 * @param args
	 * @throws Exception
	 
	public static void main(String args[]) throws Exception
    {    
    	QueueHandler sm = new QueueHandler();
    	
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
		// Test Your Code by Putting the Queuename and Message/Object
		sm.invokeQueue("IBSQueueGW", mes.toString());
    }

	*/
	/* previous code
	static Logger logger = Logger.getLogger(AuditServiceClient.class);
	 QueueConnection queueConnection;
     QueueSession session;
     Queue queue;
	
	 private  void  setupConnection(String queueName) throws JMSException, NamingException
	    {
		
		 	Properties properties1 = new Properties();
			properties1.put(Context.INITIAL_CONTEXT_FACTORY,
					"org.jnp.interfaces.NamingContextFactory");
			properties1.put(Context.URL_PKG_PREFIXES,
					"org.jboss.naming:org.jnp.interfaces");
			
			properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");
			InitialContext iniCtx = new InitialContext(properties1);

	    	Object tmp = iniCtx.lookup("ConnectionFactory");
	    	QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
	    	queueConnection = qcf.createQueueConnection();
	    	if(queueName.equals("DBPOLLER")){
	    		queue = (Queue) iniCtx.lookup("queue/ResumeMsgQueueGW");
	    	}else{
	    		queue = (Queue) iniCtx.lookup("queue/AuditMessageQueueGW");
	    	}
	    	
	    	session = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
	    	queueConnection.start();
	    	
	    }
	private void sendAMessage(NgphCanonical ngphCanonical) throws JMSException {
	    	
	        QueueSender send = session.createSender(queue);
	        
	        ObjectMessage objMessage = session.createObjectMessage(ngphCanonical);
	        
	        send.send(objMessage);        
	        send.close();
	    }
	private void sendAMessage(EventAudit eventAudit) throws JMSException {
    	
        QueueSender send = session.createSender(queue);
        
        ObjectMessage objMessage = session.createObjectMessage(eventAudit);
        
        send.send(objMessage);        
        send.close();
    }
	public void dbPollerQueueCall(NgphCanonical ngphCanonical,String queueName,EventAudit eventAudit){
		try {
			setupConnection(queueName);
			if(queueName.equals("DBPOLLER")){
				
				sendAMessage(ngphCanonical);
			}else{
				
				sendAMessage(eventAudit);
			}
			stop();
			
		} catch (JMSException jmsException) {
			// TODO Autogenerated catch block
			logger.info(jmsException);
		} catch (NamingException namingException) {
			logger.info(namingException);
		}
	}
	 private void stop() throws JMSException 
	    { 
		 queueConnection.stop();
	        session.close();
	        queueConnection.close();
	    }
	    */
}
