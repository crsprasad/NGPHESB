package com.logica.ngph.action;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.springframework.dao.EmptyResultDataAccessException;

import com.logica.ngph.common.dtos.NgphCanonical;
import com.logica.ngph.esb.Dtos.EventAudit;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.utils.ApplicationContextProvider;

/**
 * 
 * @author guptarb
 * The Event Logger Class.
 * This Class will pick message from a JMS Queue and store the Message in DB
 */

public class AuditServiceAction extends AbstractActionLifecycle 
{
	static Logger logger = Logger.getLogger(AuditServiceAction.class);
	protected ConfigTree	_config;
	public AuditServiceAction(ConfigTree config) { _config = config; }
	
	private EsbServiceDao esbServiceDao;

	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}

	
	public void performAudit(Message message) throws Exception
	{
		if(message!=null)
		{
			try
			{
				esbServiceDao = (EsbServiceDao)ApplicationContextProvider.getBean("esbServiceDao");
				
				EventAudit eventAudit = (EventAudit)message.getBody().get();
				String eventId = eventAudit.getAuditEventId();
				logger.info("The event id that is being looked for is " + eventId);
				
				NgphCanonical canObj = eventAudit.getCanonical();
				
				if (eventId !=null && StringUtils.isNotBlank(eventId ) && StringUtils.isNotEmpty(eventId ))
				{
					String eventDesc  = esbServiceDao.getEventDescription(eventId);
					if(canObj !=null && eventAudit!=null)
					{
						logger.info("Event Description read from DB : " + eventDesc + "for Event Id : " + eventId);
						
						String substutuedEventDec = eventDesc;
						char [] data = eventDesc.toCharArray();
						int first=0,last=0;
						//get the Class using Class loader  
						Class<?> c = Class.forName(canObj.getClass().getName());  
						
						for(int i=0;i<data.length;i++)
						{
							if(data[i]=='{')
							{
								first=i+1;
							}
							if(data[i]=='}')
							{
								last=i;
								String val = eventDesc.substring(first, last);
								Method method = c.getMethod("get" +val);
								logger.info("The method name is " + method.getName());
								Object canVal = method.invoke(canObj);
								if(canVal!=null)
								{
									substutuedEventDec = substutuedEventDec.replace(val, canVal.toString());
								}
								else
								{
									logger.error("Canonical Variable : get" +val + " was null");
								}
							}
						}
						logger.info("Dynamically Constructed Event Desc : " + substutuedEventDec);
						
						//updating the Audit Object from Canonical
						eventAudit.setAuditEventDesc(substutuedEventDec);
						eventAudit.setAuditBranch(canObj.getMsgBranch());
						eventAudit.setAuditDept(canObj.getMsgDept());
						eventAudit.setAuditMessageRef(canObj.getMsgRef());
						eventAudit.setAuditTransactionRef(canObj.getTxnReference());
						esbServiceDao.saveEventAudit(eventAudit);
					}
					else
					{
						String substutuedEventDec = eventDesc;
						char [] data = eventDesc.toCharArray();
						int first=0,last=0;
						//get the Class using Class loader    				
						for(int i=0;i<data.length;i++)
						{
							if(data[i]=='{')
							{
								first=i+1;
							}
							if(data[i]=='}')
							{
								last=i;
								String val = eventDesc.substring(first, last);
								int iArr = Integer.parseInt(val);
								String canVal = eventAudit.getExtraInformation()[iArr];
								if(canVal!=null)
								{
									substutuedEventDec = substutuedEventDec.replace(val, canVal.toString());
								}
								else
								{
									logger.error("Canonical Variable : get" +val + " was null");
								}
							}
						}
						logger.info("Dynamically Constructed Event Desc : " + substutuedEventDec);
						
						//updating the Audit Object from Canonical
						eventAudit.setAuditEventDesc(substutuedEventDec);
						esbServiceDao.saveEventAudit(eventAudit);
					}
				}
			}
			catch(EmptyResultDataAccessException emptyResultDataAccessException)
			{
				logger.error(emptyResultDataAccessException, emptyResultDataAccessException);
				throw new EmptyResultDataAccessException(emptyResultDataAccessException.getLocalizedMessage(),1);
			}
			catch (SQLException sqlException)
			{
				logger.error(sqlException, sqlException);
				throw new SQLException("Auding Service Exception"+sqlException);
			}
			catch (Exception e) 
			{
				logger.error(e, e);
			}
		}
		else
		{
			logger.error("Null Message received by AuditServiceAction");
		}

	}
}