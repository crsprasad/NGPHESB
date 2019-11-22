package com.logica.ngph.action;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import com.logica.ngph.iso.jaxb.pacs008001v02.generated.CreditTransferTransactionInformation11;
import com.logica.ngph.iso.jaxb.pacs008001v02.generated.Document;



public class ISO20022MsgAction extends AbstractActionLifecycle{

	protected ConfigTree	_config;
	public ISO20022MsgAction (ConfigTree config) { _config = config; } 
	public ISO20022MsgAction(){
		
	}

	static Logger logger = Logger.getLogger(ISO20022MsgAction.class);
	
	public void doProcess(Message message){		
		Document document = parseXml(message);
		if(document!=null){
			//Need to implement setting to Canonical Object here
			List<CreditTransferTransactionInformation11> crTrReInList= document.getFIToFICstmrCdtTrf().getCdtTrfTxInf();
			for(CreditTransferTransactionInformation11 crTrReIn: crTrReInList){
				for(Method method: crTrReIn.getClass().getMethods()){
					System.out.println(method.getName());
				}
				System.out.println(crTrReIn.getPmtId().getInstrId());
			}
		}
	}
	public void exceptionHandler(Message message, Throwable exception){
		  logger.error("=============================== ISO Parser ExceptionHandler Start==========================");
		  logger.error(message,exception);
		  logger.error("****************************** ISO Parser ExceptionHandler End ***************************");
	  }
	private Document parseXml(Message message){
		try{
		JAXBContext jaxbContext = JAXBContext.newInstance("com.logica.ngph.iso.jaxb.pacs008001v02.generated");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		JAXBElement element = (JAXBElement)unmarshaller.unmarshal(new StreamSource(new StringReader(message.getBody().get().toString())));
		Document document = (Document) element.getValue();
		return document;
		}catch(JAXBException jaxExc){
			jaxExc.printStackTrace();			
		}
		return null;
	}
}
