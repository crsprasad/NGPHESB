package com.logica.ngph.utils;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.BASE24Packager;
import org.jpos.iso.packager.GenericPackager;

public class ParseISOMessage {

	public static void main(String[] args) {
		try
		{

			ISOPackager packager = new BASE24Packager();
			
			String data ="3038303042323230303030303030313030303030303030303030303030303830303030303230313233343030303030303031303030303031313037323231383031323334353630364135444647523032314142434445464748494A2031323334353637383930"; 
			System.out.println("Packed Hexa Format : " + data);

			String hextoNormal = new String(ISOUtil.hex2byte(data));
			
			System.out.println("Hex to Normal Message : " + hextoNormal);
			// Create ISO Message
			ISOMsg isoMsg = new ISOMsg();
			isoMsg.setPackager(packager);
			isoMsg.unpack(hextoNormal.getBytes());
			
	
			// print the DE list
			logISOMsg(isoMsg);
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
