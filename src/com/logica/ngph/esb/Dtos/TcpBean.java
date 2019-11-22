package com.logica.ngph.esb.Dtos;

import java.io.Serializable;

public class TcpBean implements Serializable{

	private static final long serialVersionUID = 1L;

	private String msg;
	private String msgRef;
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @return the msgRef
	 */
	public String getMsgRef() {
		return msgRef;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * @param msgRef the msgRef to set
	 */
	public void setMsgRef(String msgRef) {
		this.msgRef = msgRef;
	}
}
