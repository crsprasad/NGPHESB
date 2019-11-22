package com.logica.ngph.esb.servicesImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.AuthSupMapper;
import com.logica.ngph.esb.Dtos.AuthMsgPolled;
import com.logica.ngph.esb.daos.AuthStpServiceDao;
import com.logica.ngph.esb.services.AuthStpService;

public class AuthStpServiceImpl extends AbstractActionLifecycle implements
		AuthStpService {

	protected ConfigTree _config;
	static Logger logger = Logger.getLogger(AuthStpServiceImpl.class);
	
	public AuthStpServiceImpl(ConfigTree config) {
		_config = config;
	}

	public AuthStpServiceImpl() {
	}

	private AuthStpServiceDao authStpServiceDao;
	/**
	 * @param authStpServiceDao the authStpServiceDao to set
	 */
	public void setAuthStpServiceDao(AuthStpServiceDao authStpServiceDao) {
		this.authStpServiceDao = authStpServiceDao;
	}

	/**
	 * This is the main method which is invoked by the verifyMsgQueue. This is
	 * the main processing unit of the Service.
	 */
	public void execute(Message message) throws Exception {

		try {
			
			//ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
			//authStpServiceDao = (AuthStpServiceDao)context.getBean("authStpServiceDao");

			authStpServiceDao = (AuthStpServiceDao)ApplicationContextProvider.getBean("authStpServiceDao");

			String key = message.getBody().get().toString();
			logger.info(key);

			List<AuthMsgPolled> vals = authStpServiceDao.getMsgsPolled(key);
			logger.info("MEssaglogger.info "+ vals);

			Map<String, Object> grpres = authStpServiceDao.getGroupInfo(vals,
					key);
			logger.info("Ta_AUthSchemam_tx table values " + grpres);

			/*
			 * Here we check if we dont get any Default supervisor, the system
			 * will stop further processing and log the warning
			 */
			if (grpres != null) 
			{
				ArrayList<Object> supvals = authStpServiceDao.getSupInfo(grpres);
				List<Object> sortedVals = new AuthSupMapper().processMap(supvals);
				
				authStpServiceDao.insertData(sortedVals, grpres, key,vals);
				logger.info("Auth Table has been populated Appropriately for Message Ref : " + key);
			} 
			else 
			{
				// TODO: Initializationm concept for default supervisor else further log
				logger.warn("Service has been terminated due to non availability of Default supervisor for Message Key : "+ key);
			}
		} catch (Exception e) 
		{
			logger.error(e, e);
			throw new Exception(e);
		}
	}
	
	public static void main(String[] args) {/*
		try {
			new AuthStpServiceImpl()
					.execute("7351e840-be2d-4684-a54b-7e49b95ef713");
		} catch (Exception e) {
			e.printStackTrace();
		}
	*/}
}
