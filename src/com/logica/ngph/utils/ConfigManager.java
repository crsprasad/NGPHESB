package com.logica.ngph.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logica.ngph.common.dtos.GenericFilePojo;



public class ConfigManager {
	
	public static void main(String[] args) {/*
		
		System.out.println(new ConfigManager().getConfig("C:/OFF_WORK/jboss-5.1.0.GA/bin/configFile/GenericConfig.xml"));
	*/}
	
	public LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<GenericFilePojo>>>> getConfig(String configFilePath_Name)
	{
		 try {

			 	//File Map
				LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<GenericFilePojo>>>> fileMap  = new LinkedHashMap<String, LinkedHashMap<String,LinkedHashMap<String,ArrayList<GenericFilePojo>>>>();

			 	//Table map
				LinkedHashMap<String, LinkedHashMap<String, ArrayList<GenericFilePojo>>> tableMap  = null;

				//Header,Body,Trailer Map
				LinkedHashMap<String, ArrayList<GenericFilePojo>> bodyMap  = null;;
			 
			 	//configFile/GenericConfig.xml
				File fXmlFile = new File(configFilePath_Name);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				doc.getDocumentElement().normalize();
				
				String tableName = null;
				String tableId = null;
				String tempTableName = null;
				
				 //Get table Tag and its Attribute as Table Name
	             NodeList tableList = doc.getElementsByTagName("Table");
	         for(int table = 0;table<tableList.getLength();table++)
	         {
	        	 bodyMap = new LinkedHashMap<String, ArrayList<GenericFilePojo>>();
	        	 tableMap = new LinkedHashMap<String, LinkedHashMap<String, ArrayList<GenericFilePojo>>>();
	        	 //System.out.println("tableList.getLength() : " + tableList.getLength());
	            
	        	 Node tableNode = tableList.item(table);
	             NamedNodeMap  tableNodeMap = tableNode.getAttributes();
	             Attr tableAttr1 = (Attr)tableNodeMap.item(0);
	             tableId = tableAttr1.getValue().trim();

         		 Attr tableAttr2 = (Attr)tableNodeMap.item(1);
        		 tableName = tableAttr2.getValue().trim();

	             //System.out.println("Table Atribute "+tableName);
	             //System.out.println("Table Atribute "+tableId);
           
	             tempTableName = tableName;
	             //Traverse through Header Tag and get its Attribute as Id which is header Identifier
	             if(doc.getElementsByTagName("Header").getLength()>=1)
	             {
		             NodeList headerList = doc.getElementsByTagName("Header");
		             Node headerNode = headerList.item(table);
		             NamedNodeMap  headerNodeMap = headerNode.getAttributes();
		             Attr headerAttr = (Attr)headerNodeMap.item(0);
	         		 String headerAttrValue = headerAttr.getValue().trim();
		             //System.out.println("Header Atribute "+ headerAttrValue);
		           
                	  for (int fields = 0; fields < headerList.getLength(); fields++) 
		             {
     	            	 //System.out.println("Header Length : " + headerList.getLength() + "******");
                		  Element fileElement = (Element) headerList.item(fields);
     	            	 Attr idTableAttr1 = (Attr)fileElement.getParentNode().getAttributes().item(1);
     		             String tableIdentifier = idTableAttr1.getValue().trim();
     		             //System.out.println("Table Identifier : " + tableIdentifier);
     		             //System.out.println("Temp Table Name Identifier : " + tempTableName);
     		            
                       if(tableIdentifier.equalsIgnoreCase(tempTableName))
     	              {
			             //Header ArrayList Holding Pojo Objects
			             ArrayList<GenericFilePojo> headerArrayList = new ArrayList<GenericFilePojo>(); 
	
		            	 Node fieldsNode = headerList.item(fields);
	                     Element fieldElement =(Element) fieldsNode;
	                     NodeList fieldNodeList = fieldElement.getElementsByTagName("FIELD");
	                     
	                     for (int field = 0; field < fieldNodeList.getLength(); field++)
	                     {
	        	             //Header Pojo List
	        	             GenericFilePojo headerPojo = new GenericFilePojo();
	
	                    	 Node fieldNode = fieldNodeList.item(field);
	                    	 //System.out.println("Node Value for header :- "+ fieldNode.getTextContent().trim());
	                         headerPojo.setFieldName(fieldNode.getTextContent().trim());
	                    	 NamedNodeMap  fieldNodeMap = fieldNode.getAttributes();
	                        for(int i = 0 ; i<fieldNodeMap.getLength() ; i++) 
	                        {
	                        	Attr fieldAttr = (Attr)fieldNodeMap.item(i);   
	                        	if(fieldAttr.getName().equals("delim"))
	                        	{
	                        		//System.out.println(" del "+fieldAttr.getValue().trim());
	                        		headerPojo.setDelim(fieldAttr.getValue().trim());
	                            }
	                            if(fieldAttr.getName().equals("column"))
	                            {
	                            	//System.out.println(" Column "+fieldAttr.getValue().trim());
	                        		headerPojo.setColoumnName(fieldAttr.getValue().trim());
	                            }
	                            if(fieldAttr.getName().equals("length"))
	                            {
	                            	//System.out.println(" length "+fieldAttr.getValue().trim());
	                        		headerPojo.setLength(fieldAttr.getValue().trim());
	                            }
	                           
	                         }
	                        //Holing multiple Pojo Objects in ArrayList.
	                        headerArrayList.add(headerPojo);
	                     }
	    	             //Holding arrayList as Value containing Pojo's with Header Id as key
	    	             bodyMap.put(headerAttrValue, headerArrayList);
		             }
		             //System.out.println("HeaderMap " + bodyMap);
	             }
	             }
	             else
	             {
	            	 //System.out.println("No Header Tag Found");
	             }

	             //Traverse through Body Tag and get its Attribute as Id which is Body Identifier
	             if(doc.getElementsByTagName("Body").getLength()>=1)
	             {
	            	 NodeList bodyList = doc.getElementsByTagName("Body");
		             Node bodyNode = bodyList.item(0);
		             NamedNodeMap  bodyNodeMap = bodyNode.getAttributes();
		             Attr bodyAttr = (Attr)bodyNodeMap.item(0);
	         		 String bodyAttrValue = bodyAttr.getValue().trim();
		             //System.out.println("Body Atribute "+ bodyAttrValue);
	
		             for (int fields = 0; fields < bodyList.getLength(); fields++) 
		             {
		            	 //System.out.println("Body Length : " + bodyList.getLength() + "******");
               		  	 Element fileElement = (Element) bodyList.item(fields);
    	            	 Attr idTableAttr1 = (Attr)fileElement.getParentNode().getAttributes().item(1);
    		             String tableIdentifier = idTableAttr1.getValue().trim();
    		             //System.out.println("Table Identifier : " + tableIdentifier);
    		             //System.out.println("Temp Table Name Identifier : " + tempTableName);
    		            
                      if(tableIdentifier.equalsIgnoreCase(tempTableName))
    	              {
			             //Body ArrayList Holding Pojo Objects
			             ArrayList<GenericFilePojo> bodyArrayList = new ArrayList<GenericFilePojo>(); 
	
		            	 Node fieldsNode = bodyList.item(fields);
	                     Element fieldElement =(Element) fieldsNode;
	                     NodeList fieldNodeList = fieldElement.getElementsByTagName("FIELD");
	                     
	                     for (int field = 0; field < fieldNodeList.getLength(); field++)
	                     {
	        	             //Body Pojo List
	        	             GenericFilePojo bodyPojo = new GenericFilePojo();
	
	                        Node fieldNode = fieldNodeList.item(field);
	                        //System.out.println("Node Value for Body :- "+ fieldNode.getTextContent().trim());
	                        bodyPojo.setFieldName(fieldNode.getTextContent().trim());
	                        NamedNodeMap  fieldNodeMap = fieldNode.getAttributes();
	                 
	                        for(int i = 0 ; i<fieldNodeMap.getLength() ; i++) 
	                        {
	                        	Attr fieldAttr = (Attr)fieldNodeMap.item(i);   
	                        	if(fieldAttr.getName().equals("delim"))
	                        	{
	                        		//System.out.println(" del "+fieldAttr.getValue().trim());
	                        		bodyPojo.setDelim(fieldAttr.getValue().trim());
	                            }
	                            if(fieldAttr.getName().equals("column"))
	                            {
	                            	//System.out.println(" Column "+fieldAttr.getValue().trim());
	                            	bodyPojo.setColoumnName(fieldAttr.getValue().trim());
	                            }
	                            if(fieldAttr.getName().equals("length"))
	                            {
	                            	//System.out.println(" length "+fieldAttr.getValue().trim());
	                            	bodyPojo.setLength(fieldAttr.getValue().trim());
	                            }
	                            if(fieldAttr.getName().equals("key"))
	                            {
	                            	//System.out.println(" key "+fieldAttr.getValue().trim());
	                            	bodyPojo.setKey(fieldAttr.getValue().trim());
	                            }
	                         }
	                        //Holing multiple Pojo Objects in ArrayList.
	                        bodyArrayList.add(bodyPojo);
	                        //System.out.println();
	                     }
	    	             //Holding arrayList as Value containing Pojo's with Body Id as key
	    	             bodyMap.put(bodyAttrValue, bodyArrayList);
		             }
		             
		             //System.out.println("bodyMap " + bodyMap);
	             }
	             }
	             else
	             {
	            	 //System.out.println("No Body Tag Found");
	             }
	             //Traverse through Trailer Tag and get its Attribute as Id which is Trailer Identifier
	             if(doc.getElementsByTagName("Trailer").getLength()>=1)
	             {
		             NodeList trailerList = doc.getElementsByTagName("Trailer");
		             Node trailerNode = trailerList.item(0);
		             NamedNodeMap  trailerNodeMap = trailerNode.getAttributes();
		             Attr trailerAttr = (Attr)trailerNodeMap.item(0);
	         		 String trailerAttrValue = trailerAttr.getValue().trim();
		             //System.out.println("Tralier Atribute "+ trailerAttrValue);
	
		             for (int fields = 0; fields < trailerList.getLength(); fields++) 
		             {
		            	 //System.out.println("Trailer Length : " + trailerList.getLength() + "******");
               		  	 Element fileElement = (Element) trailerList.item(fields);
    	            	 Attr idTableAttr1 = (Attr)fileElement.getParentNode().getAttributes().item(1);
    		             String tableIdentifier = idTableAttr1.getValue().trim();
    		             //System.out.println("Table Identifier : " + tableIdentifier);
    		            //System.out.println("Temp Table Name Identifier : " + tempTableName);
    		            
                      if(tableIdentifier.equalsIgnoreCase(tempTableName))
    	              {
		            	 
			             //Body ArrayList Holding Pojo Objects
			             ArrayList<GenericFilePojo> trailerArrayList = new ArrayList<GenericFilePojo>(); 
	
		            	 Node fieldsNode = trailerList.item(fields);
	                     Element fieldElement =(Element) fieldsNode;
	                     NodeList fieldNodeList = fieldElement.getElementsByTagName("FIELD");
	                     
	                     for (int field = 0; field < fieldNodeList.getLength(); field++)
	                     {
	        	             //Trailer Pojo List
	        	             GenericFilePojo trailerPojo = new GenericFilePojo();
	
	                        Node fieldNode = fieldNodeList.item(field);
	                        //System.out.println("Node Value for Trailer :- "+ fieldNode.getTextContent().trim());
	                        trailerPojo.setFieldName(fieldNode.getTextContent().trim());
	                        NamedNodeMap  fieldNodeMap = fieldNode.getAttributes();
	                 
	                        for(int i = 0 ; i<fieldNodeMap.getLength() ; i++) 
	                        {
	                        	Attr fieldAttr = (Attr)fieldNodeMap.item(i);   
	                        	if(fieldAttr.getName().equals("delim"))
	                        	{
	                        		//System.out.println(" del "+fieldAttr.getValue().trim());
	                        		trailerPojo.setDelim(fieldAttr.getValue().trim());
	                            }
	                            if(fieldAttr.getName().equals("column"))
	                            {
	                            	//System.out.println(" Column "+fieldAttr.getValue().trim());
	                            	trailerPojo.setColoumnName(fieldAttr.getValue().trim());
	                            }
	                            if(fieldAttr.getName().equals("length"))
	                            {
	                            	//System.out.println(" length "+fieldAttr.getValue().trim());
	                            	trailerPojo.setLength(fieldAttr.getValue().trim());
	                            }
	                         }
	                        //Holing multiple Pojo Objects in ArrayList.
	                        trailerArrayList.add(trailerPojo);
	                     }
	    	             bodyMap.put(trailerAttrValue, trailerArrayList);
		             }
		             
		             //System.out.println("TrailerMap " + bodyMap);
	             }
	         }
	             else
	             {
	            	 //System.out.println("No Trailer Tag Found");
	             }

	             	//System.out.println("Loop Val : " + table);
	             	// Storing Table name as Key with Value as Another HashMap holding info about header/Body/Trailer
		            tableMap.put(tableName, bodyMap);
		            
		            //System.out.println("Table Map : " + tableMap);
		            
		            fileMap.put(tableId, tableMap);
		            //System.out.println("File Map : " + fileMap);
	             }
	             
	     		return fileMap;
           }
		 catch (Exception e) 
		 {
                 e.printStackTrace();
         		return null;

         }
	}
}

