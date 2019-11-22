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

import com.logica.ngph.common.dtos.AcknowledgementCanonical;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.dtos.NgphCanonical;

public class ReportRPTClient {
	
	private QueueConnection conn;
    private QueueSession session;
    private Queue que;
    
    private static InitialContext iniCtx;
    private static QueueConnectionFactory qcf;
	static Logger logger = Logger.getLogger(ReportRPTClient.class);
    
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
    /**
     * 
     * @throws JMSException
     * @throws NamingException
     */
    private static void setupConnection() throws JMSException, NamingException
    {
        Properties properties1 = new Properties();
		properties1.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		properties1.put(Context.URL_PKG_PREFIXES,
				"org.jboss.naming:org.jnp.interfaces");
		properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");

		properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		iniCtx = new InitialContext(properties1);

    	Object tmp = iniCtx.lookup("ConnectionFactory");
    	qcf = (QueueConnectionFactory) tmp;
    }
    
    private void stop() throws JMSException 
    { 
        conn.stop();
        session.close();
        conn.close();
    }
    
    /**
     * 
     * @param msg
     * @throws JMSException
     */
	private void sendAMessage(NgphCanonical msg) throws JMSException 
	{
	        QueueSender send = session.createSender(que);        
	        ObjectMessage tm = session.createObjectMessage(msg);
	        send.send(tm);        
	        send.close();
	}
	private void sendAMessage(AcknowledgementCanonical msg) throws JMSException 
	{
	        QueueSender send = session.createSender(que);        
	        ObjectMessage tm = session.createObjectMessage(msg);
	        send.send(tm);        
	        send.close();
	}
	private void sendAMessage(String msg) throws JMSException 
	{
	        QueueSender send = session.createSender(que);        
	        ObjectMessage tm = session.createObjectMessage(msg);
	        send.send(tm);        
	        send.close();
	}
	private void sendAMessage(InfoCanonical msg) throws JMSException 
	{
	        QueueSender send = session.createSender(que);        
	        ObjectMessage tm = session.createObjectMessage(msg);
	        send.send(tm);        
	        send.close();
	}
	public void call(String queueName,Object mes) throws Exception
    {
		logger.info("Inside ReportRPT Client...");
    	conn = qcf.createQueueConnection();
		que = (Queue) iniCtx.lookup("queue/" + queueName);
    	session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    	conn.start();
    	if(mes instanceof NgphCanonical)
    	{
    		sendAMessage((NgphCanonical)mes);
    	}
    	else if (mes instanceof AcknowledgementCanonical)
    	{
    		sendAMessage((AcknowledgementCanonical)mes);
    	}
    	else if (mes instanceof InfoCanonical)
    	{
    		sendAMessage((InfoCanonical)mes);
    	}
    	else
    	{
    		sendAMessage(mes.toString());
    	}
    }
}
