/**
 * CustomException class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.exception;

public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 6346326010355275237L;

	private final int status;
	private final String code;

	public CustomException(String message) {
		super(message);
		this.code = "";
		this.status = 0;
	}

	public CustomException(String message, int status) {
		super(message);
		this.code = "";
		this.status = status;
	}

	public CustomException(int status, String code) {
		this.status = status;
		this.code = code;
	}

	public int getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

}
