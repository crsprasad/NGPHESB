package com.logica.ngph.esb.servicesImpl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.schedule.ScheduledEventListener;
import org.jboss.soa.esb.schedule.SchedulingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.logica.ngph.esb.ReportRPTClient;
import com.logica.ngph.esb.Dtos.ResponseBean;
import com.logica.ngph.esb.Dtos.TcpBean;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.utils.ApplicationContextProvider;
import com.logica.ngph.utils.NGPHEsbUtils;

@Service
public class TCPFlusher implements ScheduledEventListener {

	static Logger logger = Logger.getLogger(TCPFlusher.class);
	private EsbServiceDao esbServiceDao;
	
	/**
	 * @param esbServiceDao the esbServiceDao to set
	 */
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}
	public void initialize(ConfigTree arg0) throws ConfigurationException {	}
	public void uninitialize() {}

 @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class,Throwable.class,RuntimeException.class})
	public void onSchedule() throws SchedulingException {
		try
		{
			String eiCode = ISO8583ChannelServiceImpl.impsServerEi;
			logger.info("EI_Code received is : " + eiCode);
			
			esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
			int eiStatus = esbServiceDao.getEIStatus(eiCode);
			logger.info("EI Status received from DB is : " + eiStatus);
			
			//check if status is 2 in TA_EI
			if(eiStatus==2)
			{
				//select all msg for status = 1
				List<TcpBean> msgList = esbServiceDao.getTCPMsgs(1);
				
				//check for Null List
				if(msgList!=null && !msgList.isEmpty())
				{
					for(int i=0;i<msgList.size();i++)
					{
						String msgRef =msgList.get(i).getMsgRef();
						String tcpMes = msgList.get(i).getMsg();
						
						logger.info("Sending " + tcpMes + " in " + NgphEsbConstants.ResHandlerQ);
						new ReportRPTClient().call(NgphEsbConstants.ResHandlerQ, tcpMes);
						//After Sending to Queues update status = 2 in TCPQUEUE
						esbServiceDao.updateTCPStatus(2, msgRef);
						//updating Response Bean
						if(NGPHEsbUtils.msgIdResObjMap.get(msgRef)!=null)
						{
							ResponseBean resObj = NGPHEsbUtils.msgIdResObjMap.get(msgRef);
							resObj.setReqTmStmp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
							//populate new Bean Object in Global Map
							NGPHEsbUtils.populateResponseObject(msgRef, resObj);
						}
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
		
		
	}

}
