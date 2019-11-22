package com.logica.ngph.esb;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

public class QueueInitializerManager {

	static Logger logger = Logger.getLogger(QueueInitializerManager.class);

	private static QueueConnection conn;
	private static InitialContext iniCtx;
	private static QueueSession session;
    private static Queue que;
    
	private static QueueConnectionFactory qcf;
    
	public static void initializeQueueFactory(String queueName)
	{
		try
		{
			setupConnection(queueName);
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
    private static void setupConnection(String queueName) throws JMSException, NamingException
    {
        Properties properties1 = new Properties();
		properties1.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		properties1.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
		properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		
		iniCtx = new InitialContext(properties1);

    	Object tmp = iniCtx.lookup("ConnectionFactory");
    	QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
    	conn = qcf.createQueueConnection();
    	que = (Queue) iniCtx.lookup("queue/" + queueName);
    	session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    	conn.start();
    	logger.info("QueueInitializerManager has started connection for queueName : " + queueName);
    }
 }
