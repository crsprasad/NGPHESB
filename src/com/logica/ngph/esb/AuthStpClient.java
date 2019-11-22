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

public class AuthStpClient {
	
	static Logger logger = Logger.getLogger(AuthStpClient.class);
	
	QueueConnection conn;
    QueueSession session;
    Queue que;
    
    
    public void setupConnection() throws JMSException, NamingException
    {
    	//logger.info("1111");
        Properties properties1 = new Properties();
		properties1.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		properties1.put(Context.URL_PKG_PREFIXES,
				"org.jboss.naming:org.jnp.interfaces");
		
		
		properties1.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		InitialContext iniCtx = new InitialContext(properties1);

    	Object tmp = iniCtx.lookup("ConnectionFactory");
    	QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
    	conn = qcf.createQueueConnection();
    	que = (Queue) iniCtx.lookup("queue/VerifyMsgQueueGW");
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
    {/*        	    	
		AuthStpClient sm = new AuthStpClient();
    	logger.info("Staring***********************");
		sm.setupConnection();
		sm.sendAMessage("7351e840-be2d-4684-a54b-7e49b95ef713"); 
    	sm.stop();    	
    */}



}
