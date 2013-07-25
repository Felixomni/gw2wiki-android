package com.felixware.gw2w.http;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.felixware.gw2w.models.Content;
import com.felixware.gw2w.models.Result;
import com.felixware.gw2w.utilities.Constants;

public class ArticleService extends WebService {
	private String title;
	private static ArticleService instance;

	private ArticleService(Context context, WebServiceCallback callback) {
		this.context = context;
		this.callback = callback;
	}

	public static ArticleService getInstance(Context context, WebServiceCallback callback) {
		if (instance == null) {
			instance = new ArticleService(context, callback);
		}

		return instance;
	}

	@Override
	public void startService(Object object) {
		title = (String) object;
		String URLString = Constants.getBaseURL(context) + Constants.ENDPOINT;

		NameValuePair[] params = { new BasicNameValuePair("format", "json"), new BasicNameValuePair("action", "query"),
				new BasicNameValuePair("prop", "revisions|categories|imageinfo"),
				new BasicNameValuePair("rvprop", "content"), new BasicNameValuePair("rvparse", "1"),
				new BasicNameValuePair("iiprop", "url"), new BasicNameValuePair("titles", title),
				new BasicNameValuePair("redirects", "1") };

		URLString += "?" + URLEncodedUtils.format(Arrays.asList(params), "utf-8");

		new AsyncWebTask().execute(URLString);

	}

	@Override
	protected void handleSuccess(String response) {
		Content content = new Content();
		try {
			JSONObject responseJSON = new JSONObject(response);
			JSONObject pages = responseJSON.getJSONObject("query").getJSONObject("pages");
			JSONObject page = pages.getJSONObject(pages.names().getString(0));
			content.setTitle(page.getString("title"));

			if (page.has("missing")) {
				throw new JSONException(null);
			}

			if (page.getInt("ns") == 6) {
				JSONObject imageInfo = page.getJSONArray("imageinfo").getJSONObject(0);
				content.setFileUrl(imageInfo.getString("url"));
			} else {
				// categories
				if (page.has("categories")) {
					JSONArray categories = page.getJSONArray("categories");
					ArrayList<String> cats = new ArrayList<String>(categories.length());

					for (int i = 0; i < categories.length(); i++) {
						cats.add(categories.getJSONObject(i).getString("title"));
					}
					content.setCategories(cats);
				}
				// page content
				JSONObject revision = page.getJSONArray("revisions").getJSONObject(0);
				content.setContent(revision.getString("*"));
			}
			callback.onSuccess(content);
		} catch (JSONException e) {
			callback.onError(Constants.ERROR_PAGE_DOES_NOT_EXIST);
		}

	}

	@Override
	protected void handleError(Result result) {
		Log.i("ArticleService", "Error code is " + String.valueOf(result.getResponseCode()));
		callback.onError(result.getResponseCode());
	}

}
