package entity;

import java.io.Serializable;

public class Result implements Serializable{
	
	private Boolean success;//返回是否成功
	
	private String message;//返回信息

	public Result() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Result(Boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}

	public boolean isSuccess() {
		// TODO Auto-generated method stub
		return success;
	}
	
	
	
}
