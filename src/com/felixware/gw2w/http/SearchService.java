package com.felixware.gw2w.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

import com.felixware.gw2w.R;
import com.felixware.gw2w.models.Result;
import com.felixware.gw2w.utilities.Constants;

public class SearchService extends WebService {
	private static SearchService instance;

	private SearchService(Context context, WebServiceCallback callback) {
		this.context = context;
		this.callback = callback;
	}

	public static SearchService getInstance(Context context, WebServiceCallback callback) {
		if (instance == null) {
			return new SearchService(context, callback);
		}

		return instance;
	}

	@Override
	public void startService(Object object) {
		String searchText = (String) object;
		String URLString = Constants.getBaseURL(context) + Constants.ENDPOINT;

		NameValuePair[] params = { new BasicNameValuePair("action", "opensearch"),
				new BasicNameValuePair("limit", "10"), new BasicNameValuePair("search", searchText) };

		URLString += "?" + URLEncodedUtils.format(Arrays.asList(params), "utf-8");

		new AsyncWebTask().execute(URLString);

	}

	@Override
	protected void handleSuccess(String response) {
		List<String> resultsList = new ArrayList<String>();
		try {
			JSONArray responseJSON = new JSONArray(response);
			JSONArray results = responseJSON.getJSONArray(1);
			for (int i = 0; i < results.length(); i++) {
				resultsList.add(results.getString(i));
			}
			callback.onSuccess(resultsList);
		} catch (JSONException e) {
			resultsList.add(context.getResources().getString(R.string.no_search_results));
			callback.onSuccess(resultsList);
		}
	}

	@Override
	protected void handleError(Result result) {
		callback.onError(null);

	}

}
