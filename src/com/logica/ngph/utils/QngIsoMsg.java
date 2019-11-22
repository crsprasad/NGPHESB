package com.logica.ngph.utils;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.BASE24Packager;

public class QngIsoMsg {

	public static void main(String[] args) 
	{
		try
		{
			/*GenericPackager packager = new GenericPackager("iso87binary.xml");
			 
			// Create ISO Message
			ISOMsg isoMsg = new ISOMsg();
			isoMsg.setPackager(packager);*/
			
			ISOPackager packager = new BASE24Packager();
			ISOMsg isoMsg = new ISOMsg();
			
			isoMsg.setPackager(packager);
			
			isoMsg.setMTI("0800");
			isoMsg.set(3, "201234");
			isoMsg.set(4, "10000");
			isoMsg.set(7, "110722180");
			isoMsg.set(11, "123456");
			isoMsg.set(44, "A5DFGR");
			isoMsg.set(105, "ABCDEFGHIJ 1234567890");
			//isoMsg.setHeader("204".getBytes());
			//isoMsg.setHeader(Integer.toHexString(39).getBytes());
			
	 
			// print the DE list
			logISOMsg(isoMsg);
	 
			// Get and print the output result
			byte[] data = isoMsg.pack();
			
			ISOMsg isoMsglen = new ISOMsg();
			isoMsglen.setPackager(packager);
			
			isoMsglen.setMTI("0800");
			isoMsglen.set(3, "201234");
			isoMsglen.set(4, "10000");
			isoMsglen.set(7, "110722180");
			isoMsglen.set(11, "123456");
			isoMsglen.set(44, "A5DFGR");
			isoMsglen.set(105, "ABCDEFGHIJ 1234567890");
			isoMsglen.setHeader((data.length +"").getBytes());
			
			byte[] datalen = isoMsglen.pack();
			
			
			String finalMessagelen = ISOUtil.hexString(datalen).length() + ISOUtil.hexString(datalen);
			System.out.println("Packed Data : " + new String(finalMessagelen));
			
			/*System.out.println("Hexa String in raw form is : "+ISOUtil.hexString(data));
			System.out.println("Hexa Len : " + Integer.toHexString(ISOUtil.hexString(data).length()));*/
			String finalMessage = "00" + Integer.toHexString(ISOUtil.hexString(data).length()) + ISOUtil.hexString(data);
			System.out.println("Message in Packed Hexa form : " + finalMessage);

			String Message = data.length + ISOUtil.hexString(data);
			System.out.println("Message in Hexa form with normal length : " + Message);

			String message = null;
			message = new String(data);
			System.out.println("Normal Message : " + message);
			
			byte[] l = (data.length +"").getBytes();
			System.out.println("length : " + ISOUtil.hexString(l));
			
			String len = data.length +"";
			System.out.println("len : " + new String(ISOUtil.hex2byte(len)));
			
			String hexval = Integer.toHexString(10004);
			System.out.println(hexval);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void logISOMsg(ISOMsg msg) {
		System.out.println("----ISO MESSAGE-----");
		try {
			System.out.println("  MTI : " + msg.getMTI());
			for (int i=1;i<=msg.getMaxField();i++) {
				if (msg.hasField(i)) {
					System.out.println("    Field-"+i+" : "+msg.getString(i));
				}
			}
		} catch (ISOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("--------------------");
		}
 
	}
}
