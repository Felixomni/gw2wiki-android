package com.felixware.gw2w.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.AsyncTask;

import com.felixware.gw2w.models.Result;
import com.felixware.gw2w.utilities.Constants;

public abstract class WebService {
	protected WebServiceCallback callback;
	protected Context context;
	private HttpURLConnection connection;

	protected class AsyncWebTask extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... params) {
			StringBuilder response = new StringBuilder();

			if (params != null) {
				URL url = null;
				try {
					url = new URL(params[0]);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				try {
					connection = (HttpURLConnection) url.openConnection();
					int statusCode = connection.getResponseCode();
					if (statusCode == 200) {
						BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						String line;
						while ((line = r.readLine()) != null) {
							response.append(line);
						}
						r.close();

					} else {
						return new Result(statusCode, response.toString());
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return new Result(Constants.ERROR_CONNECTION, response.toString());
				} catch (IOException e) {
					e.printStackTrace();
					return new Result(Constants.ERROR_UNKNOWN, response.toString());
				}

			}
			return new Result(null, response.toString());
		}

		@Override
		protected void onPostExecute(Result result) {
			if (result.getResponseCode() == null) {
				handleSuccess(result.getResponse());
			} else {
				handleError(result);
			}
		}

	};

	public abstract void startService(Object object);

	protected abstract void handleSuccess(String response);

	protected abstract void handleError(Result result);

	public static interface WebServiceCallback {
		public void onSuccess(Object response);

		public void onError(Object response);
	}
}
