/**
 * ResponseMessage class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.response;

public class ResponseMessage {

	private int status;
	private boolean success;
	private String message;

	public ResponseMessage(int status, boolean success, String message) {
		this.status = status;
		this.success = success;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}