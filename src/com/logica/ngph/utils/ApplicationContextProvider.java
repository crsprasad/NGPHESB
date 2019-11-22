package com.logica.ngph.utils;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author guptarb
 *
 * This Generic Class will Initialize Application Context and Returns bean Name taking beanId as an Argument.
 */

public class ApplicationContextProvider {
	
	static Logger logger = Logger.getLogger(ApplicationContextProvider.class);;
	
	public static ApplicationContext context = null;
	public static Object bean = null;
	public static HashMap<String, Object> beanInitializer = new HashMap<String, Object>();
	
	/**
	 * This Static method will Initialize Application Context Object.
	 */
	public static void initializeContextProvider()
	{
		try
		{
			context = new ClassPathXmlApplicationContext("beans.xml");
			logger.info("Context Object Initialized");
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
	}
	
	/**
	 * @param beanId
	 * @return Object
	 * 
	 * This method will return Object(Bean Name) taking beanId as an Argument
	 */
	public static Object getBean(String beanId)
	{
		try
		{
			if(StringUtils.isNotBlank(beanId) && StringUtils.isNotEmpty(beanId) && beanId!=null)
			{
				bean = context.getBean(beanId);
			}
			else
			{
				logger.info("BeanId is Null");
			}
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
		return bean;
	}
	
}
