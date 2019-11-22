package com.logica.ngph.utils;

import java.sql.Timestamp;

import java.util.Calendar;

public class SodEodUtil {

	public static Timestamp getCurrentTime(){
		java.util.Date 	str_date = Calendar.getInstance().getTime();
		java.sql.Timestamp timeStampDate = new Timestamp(str_date.getTime());
		
		return timeStampDate;
	}


}
