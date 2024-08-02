/**
 * ResponseMessageWithLink class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class ResponseMessageWithLink {

	private int status;
	private boolean success;
	private String message;
	
	@Schema(type = "string", format = "uri", description = "Link to download the file")
    private String downloadLink;

	public ResponseMessageWithLink(int status, boolean success, String message, String downloadLink) {
		this.status = status;
		this.success = success;
		this.message = message;
		this.downloadLink = downloadLink;
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

	public String getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}	
}