package com.logica.ngph.esb.Dtos;

import java.io.Serializable;

public class CustomerInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	String email1;
	String email2;
	String email3;
	String mobNo;
	
	/**
	 * @return the email1
	 */
	public String getEmail1() {
		return email1;
	}
	/**
	 * @return the email2
	 */
	public String getEmail2() {
		return email2;
	}
	/**
	 * @return the email3
	 */
	public String getEmail3() {
		return email3;
	}
	/**
	 * @return the mobNo
	 */
	public String getMobNo() {
		return mobNo;
	}
	/**
	 * @param email1 the email1 to set
	 */
	public void setEmail1(String email1) {
		this.email1 = email1;
	}
	/**
	 * @param email2 the email2 to set
	 */
	public void setEmail2(String email2) {
		this.email2 = email2;
	}
	/**
	 * @param email3 the email3 to set
	 */
	public void setEmail3(String email3) {
		this.email3 = email3;
	}
	/**
	 * @param mobNo the mobNo to set
	 */
	public void setMobNo(String mobNo) {
		this.mobNo = mobNo;
	}

}
