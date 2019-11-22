package com.logica.ngph.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.logica.ngph.esb.Dtos.AuthSupBean;

// Implements Comparator and perform the bean sorting for man or optional sup
 class MyComparable implements Comparator<AuthSupBean>{       
	 public int compare(AuthSupBean o1, AuthSupBean o2) { 
		int s1= new Integer(o1.getSupSeq()).intValue();
		int s2= new Integer(o2.getSupSeq()).intValue();
		if(s1<s2){
			return -1;
		}else if(s1==s2){
			int g1= new Integer(o1.getGroupID()).intValue();
			int g2= new Integer(o2.getGroupID()).intValue();
			 return (g1<g2 ? -1 : (g1==g2 ? 0 : 1));   
		}else{
			return 1;
		}
		 
		}

	}
public class AuthSupMapper {

	/*public List<Object> init(){
		ArrayList<Object> inputList= new ArrayList<Object>();
		AuthSupBean b1=new AuthSupBean("1001","Group1","101","1","1");
		AuthSupBean b2=new AuthSupBean("1001","Group1","102","2","1");
		AuthSupBean b3=new AuthSupBean("1001","Group1","103","0","0");
		
		AuthSupBean b4=new AuthSupBean("1002","group2","101","0","0");
		AuthSupBean b5=new AuthSupBean("1002","group2","102","1","1");
		AuthSupBean b6=new AuthSupBean("1003","Group3","103","1","0");
		AuthSupBean b7=new AuthSupBean("1003","Group3","104","2","1");
		AuthSupBean b8=new AuthSupBean("1003","Group3","105","0","0");
		AuthSupBean b9=new AuthSupBean("1003","Group3","106","3","1");
		inputList.add(b1);
		inputList.add(b2);
		inputList.add(b3);
		inputList.add(b4);
		inputList.add(b5);
		inputList.add(b6);
		inputList.add(b7);
		inputList.add(b8);
		inputList.add(b9);
		return inputList;
		
	}
*/

	// Once the beans are sorted stored in a final arraylist
	public List processMap(List<Object> inputList){
		ArrayList<String> supIDList = new ArrayList<String>();
		List <AuthSupBean> outputList= new ArrayList<AuthSupBean>();
		AuthSupBean bean = null;
		for(int i=0;i<inputList.size();i++){
			bean = (AuthSupBean)inputList.get(i);
			if(!supIDList.contains(bean.getSupId())){
				supIDList.add(bean.getSupId());
				if(bean.getIsMan().equalsIgnoreCase("0")){
					bean.setSupSeq("0");
				}
				outputList.add(bean);
			}
		}
		Collections.sort(outputList,new MyComparable());
		outputList=updateSupSeqId(outputList);
		Iterator<AuthSupBean> it=outputList.iterator();
		
		while(it.hasNext()){
			AuthSupBean beant= it.next();
		//System.out.println( beant.getGroupID()+"	"+beant.getGroupName()+"	"+ beant.getSupId()+" " +beant.getSupSeq() +" "+ beant.getIsMan()); 
		} 
		return outputList;
	}
	
	
		// Update the original bean to new bean
	private List<AuthSupBean> updateSupSeqId(List<AuthSupBean> beanList1) {
		List<AuthSupBean> l1 = new ArrayList<AuthSupBean>();
		AuthSupBean b = null;
		if(beanList1.size()>1){
			for(int i=0, j=1;i<beanList1.size()-1&&j<beanList1.size();i++,j++){
				b=updateBean(beanList1.get(i),beanList1.get(j));
			}
		}
		return beanList1;
		}
	// Comparing the bean values
	 public AuthSupBean updateBean(AuthSupBean o1, AuthSupBean o2) { 
			int s1= new Integer(o1.getSupSeq()).intValue();
			int s2= new Integer(o2.getSupSeq()).intValue();
			if(s1==s2&&s1!=0&&s2!=0){
				o2.setSupSeq(new Integer(new Integer(o2.getSupSeq()).intValue()+1).toString());
			}
			return o2;
	 }

	/*public static void main(String args[]){
		AuthSupMapper obj1 = new AuthSupMapper();
		obj1.processMap(obj1.init());
	}*/

// Removing duplicates from original haspmap
	public LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
	    List mapKeys = new ArrayList(passedMap.keySet());
	    List mapValues = new ArrayList(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	        
	    LinkedHashMap sortedMap = 
	        new LinkedHashMap();
	    
	    Iterator valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Object val = valueIt.next();
	        Iterator keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            String comp1 = passedMap.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                passedMap.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, (Double)val);
	                break;
	            }

	        }

	    }
	    return sortedMap;
	}
}
