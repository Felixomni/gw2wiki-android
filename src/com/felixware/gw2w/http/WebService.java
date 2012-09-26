package com.felixware.gw2w.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.felixware.gw2w.R;
import com.felixware.gw2w.http.RequestTask.RequestListener;
import com.felixware.gw2w.utilities.Constants;

public final class WebService {
	private static final String TAG = WebService.class.getSimpleName();

	private static volatile WebService sInstance;

	private Context mContext = null;

	public interface Listener {
		public void onRequestError(RequestTask request, WebServiceException e);
	}

	private WebService(Context context) {
		mContext = context;
	}

	public static WebService getInstance(Context context) {
		if (sInstance == null) {
			synchronized (WebService.class) {
				if (sInstance == null) {
					sInstance = new WebService(context);
				}
			}
		}
		return sInstance;
	}

	private List<RequestTask> mRequestTasks = new ArrayList<RequestTask>();

	public void cancelAllRequests() {
		for (RequestTask task : mRequestTasks) {
			task.cancel();
		}
		mRequestTasks.clear();
	}

	private RequestTask makeRequest(RequestTask request, NameValuePair[] params) {
		mRequestTasks.add(request);

		if (params == null) {
			request.execute();
		} else {
			request.execute(params);
		}

		return request;
	}

	private void requestFailed(RequestTask request, Listener listener, boolean isConnectionError, int errorCode) {

		if (listener != null) {
			listener.onRequestError(request, new WebServiceException(isConnectionError, errorCode));
		}
	}

	private void requestFailedNoConnection(RequestTask request, Listener listener) {
		requestFailed(request, listener, true, Constants.ERROR_CONNECTION);
	}

	public interface GetContentListener extends Listener {
		public void didGetContent(RequestTask request, String content, String title);
	}

	public RequestTask getContent(final GetContentListener listener, String title) {
		RequestTask getContentRequest = new GetRequestTask(mContext, "/api.php");

		getContentRequest.setListener(new RequestListener() {
			@Override
			public void onRequestFailed(RequestTask request) {
				requestFailedNoConnection(request, listener);
			}

			@Override
			public void onRequestCompleted(RequestTask request, String response) {
				try {
					JSONObject responseJSON = new JSONObject(response);
					JSONObject query = new JSONObject(responseJSON.getString("query"));
					JSONObject pages = new JSONObject(query.getString("pages"));
					JSONArray pageid = pages.names();
					JSONObject page = new JSONObject(pages.getString(pageid.getString(0)));
					String title = page.getString("title");
					JSONArray revisions = page.getJSONArray("revisions");
					JSONObject string = revisions.getJSONObject(0);
					String newstring = string.getString("*");

					listener.didGetContent(request, newstring, title);
				} catch (JSONException e) {
					requestFailed(request, listener, false, Constants.ERROR_PAGE_DOES_NOT_EXIST);
				}
			}
		});

		NameValuePair[] params = { new BasicNameValuePair("action", "query"), new BasicNameValuePair("prop", "revisions"), new BasicNameValuePair("rvprop", "content"), new BasicNameValuePair("rvparse", "1"), new BasicNameValuePair("format", "json"), new BasicNameValuePair("titles", title) };
		return makeRequest(getContentRequest, params);
	}

	public interface GetSearchResultsListener extends Listener {
		public void didGetSearchResults(RequestTask request, List<String> results);
	}

	public RequestTask getSearchResults(final GetSearchResultsListener listener, final String searchText, final int limit) {
		RequestTask getSearchResultsRequest = new GetRequestTask(mContext, "/api.php");
		final List<String> resultsList = new ArrayList<String>();

		getSearchResultsRequest.setListener(new RequestListener() {
			@Override
			public void onRequestFailed(RequestTask request) {
				requestFailedNoConnection(request, listener);
			}

			@Override
			public void onRequestCompleted(RequestTask request, String response) {
				Log.i(TAG, response);
				try {
					JSONArray responseJSON = new JSONArray(response);
					JSONArray results = responseJSON.getJSONArray(1);
					for (int i = 0; i < results.length(); i++) {
						resultsList.add(results.getString(i));
					}
					listener.didGetSearchResults(request, resultsList);
				} catch (JSONException e) {
					resultsList.add(mContext.getResources().getString(R.string.no_search_results));
					listener.didGetSearchResults(request, resultsList);
				}
			}
		});

		NameValuePair[] params = { new BasicNameValuePair("action", "opensearch"), new BasicNameValuePair("limit", Integer.toString(limit)), new BasicNameValuePair("search", searchText) };
		return makeRequest(getSearchResultsRequest, params);
	}
}
