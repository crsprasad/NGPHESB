package com.logica.ngph.esb.Dtos;

import java.io.Serializable;
import java.sql.Timestamp;

public class SodEodTaskTDto implements Serializable{

	private static final long serialVersionUID = 1L;

	private String branch;
	private Timestamp businessDate;
	private String userId;
	private String taskId;
	private String sodOrEod;
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public Timestamp getBusinessDate() {
		return businessDate;
	}
	public void setBusinessDate(Timestamp businessDate) {
		this.businessDate = businessDate;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getSodOrEod() {
		return sodOrEod;
	}
	public void setSodOrEod(String sodOrEod) {
		this.sodOrEod = sodOrEod;
	}
}
