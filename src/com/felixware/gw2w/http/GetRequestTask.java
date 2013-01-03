package com.felixware.gw2w.http;

import java.io.IOException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;

import android.content.Context;
import android.util.Log;

public final class GetRequestTask extends RequestTask {

	public GetRequestTask(Context context, String endpoint) {
		super(context, endpoint);
	}

	@Override
	protected Response executeRequest(List<NameValuePair> params) {
		HttpGet getRequest = null;
		if (params != null) {
			getRequest = new HttpGet(mURL += "?" + URLEncodedUtils.format(params, "utf-8"));
		} else {
			getRequest = new HttpGet(mURL);
		}

		//Log.i(getClass().getSimpleName(), getRequest.getRequestLine().getUri());

		Response response = new Response();

		try {
			String responseString = mHttpClient.execute(getRequest, new BasicResponseHandler());
			response.responseSucceeded = true;
			response.responseString = responseString;
			//Log.i(GetRequestTask.class.getSimpleName(), responseString);
		} catch (IOException e) {
			e.printStackTrace();
			response.responseSucceeded = false;
		}

		return response;
	}

}
