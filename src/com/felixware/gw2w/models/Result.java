package com.felixware.gw2w.models;

public class Result {
	private Integer responseCode;
	private String response;

	public Result(Integer responseCode, String response) {
		this.responseCode = responseCode;
		this.response = response;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public String getResponse() {
		return response;
	}

}
