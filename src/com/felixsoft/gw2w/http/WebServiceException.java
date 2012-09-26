package com.felixsoft.gw2w.http;

public final class WebServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int mErrorCode = 0;
	private boolean mIsConnectionError = false;

	public WebServiceException(boolean isConnectionError, int errorCode) {

		mIsConnectionError = isConnectionError;
		mErrorCode = errorCode;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public boolean isConnectionError() {
		return mIsConnectionError;
	}

}
