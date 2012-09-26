package com.felixware.gw2w.http;

import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

import com.felixware.gw2w.utilities.Constants;

public abstract class RequestTask {

	public interface RequestListener {
		public void onRequestCompleted(RequestTask request, String response);

		public void onRequestFailed(RequestTask request);
	}

	protected RequestListener mListener;

	protected AndroidHttpClient mHttpClient;

	protected String mURL;

	public RequestTask(Context context, String endpoint) {
		mURL = Constants.getBaseURL(context) + endpoint;
	}

	void setListener(RequestListener listener) {
		mListener = listener;
	}

	public final void execute() {
		execute(null);
	}

	public final void execute(NameValuePair[] params) {
		mAsyncTask.execute(params);
	}

	public final void cancel() {
		mAsyncTask.cancel(true);
	}

	protected static class Response {
		public String responseString;
		public boolean responseSucceeded;
	}

	protected abstract Response executeRequest(List<NameValuePair> params);

	private AsyncTask<NameValuePair, Void, Response> mAsyncTask = new AsyncTask<NameValuePair, Void, Response>() {

		@Override
		protected Response doInBackground(NameValuePair... params) {
			mHttpClient = AndroidHttpClient.newInstance(null);
			Response response = null;

			if (params != null) {
				response = executeRequest(Arrays.asList(params));
			} else {
				response = executeRequest(null);
			}
			mHttpClient.close();
			return response;
		}

		@Override
		protected void onPostExecute(Response result) {
			if (result.responseSucceeded) {
				mListener.onRequestCompleted(RequestTask.this, result.responseString);
			} else {
				mListener.onRequestFailed(RequestTask.this);
			}
		}

	};
}
