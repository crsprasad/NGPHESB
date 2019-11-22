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

public class ReportQueue {


	private static QueueConnection conn;
	private static InitialContext iniCtx;
	private static QueueSession session;
    private static Queue que;
    
	static Logger logger = Logger.getLogger(ReportQueue.class);
	
	public static void initializeQueueFactory()
	{
		try
		{
			setupConnection();
		}
		catch (Exception e) 
		{
			logger.error(e, e);
			
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
    	que = (Queue) iniCtx.lookup("queue/ReportRPTQueueGW");
    	session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    	conn.start();
    	logger.info("EventLoggerQueueHandler has started connection for Queue ReportRPTQueueGW");
    }
    
	private void sendAMessage(NgphCanonical eventAudit) throws JMSException 
	{
		QueueSender send = session.createSender(que);
		ObjectMessage objMessage = session.createObjectMessage(eventAudit);
		send.send(objMessage);        
		send.close();
	}

	public void QueueCall(NgphCanonical ngphCanonical)
	{

	try
	{
		sendAMessage(ngphCanonical); 
	}
	catch (Exception e) 
	{
		logger.error(e, e);
	}
}	
}
