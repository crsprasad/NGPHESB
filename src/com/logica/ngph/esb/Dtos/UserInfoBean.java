package com.logica.ngph.esb.Dtos;

import java.io.Serializable;

public class UserInfoBean implements Serializable{

	private static final long serialVersionUID = 1L;

	String email;
	String mobNo;
	
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @return the mobNo
	 */
	public String getMobNo() {
		return mobNo;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @param mobNo the mobNo to set
	 */
	public void setMobNo(String mobNo) {
		this.mobNo = mobNo;
	}
	
}
