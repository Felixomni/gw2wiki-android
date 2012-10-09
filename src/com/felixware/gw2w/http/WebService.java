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

	public interface GetImageUrlListener extends Listener {
		public void didGetImageUrl(RequestTask request, String url);
	}

	public RequestTask getImageUrl(final GetImageUrlListener listener, final String pageName) {
		RequestTask getImageUrlRequest = new GetRequestTask(mContext, "/api.php");

		getImageUrlRequest.setListener(new RequestListener() {
			@Override
			public void onRequestFailed(RequestTask request) {
				requestFailedNoConnection(request, listener);
			}

			@Override
			public void onRequestCompleted(RequestTask request, String response) {
				Log.i(TAG, response);
				try {
					JSONObject responseJSON = new JSONObject(response);
					JSONObject query = new JSONObject(responseJSON.getString("query"));
					JSONObject pages = new JSONObject(query.getString("pages"));
					JSONArray pageid = pages.names();
					JSONObject page = new JSONObject(pages.getString(pageid.getString(0)));
					JSONArray image_info = new JSONArray(page.getString("imageinfo"));
					JSONObject image = new JSONObject(image_info.getString(0));
					String image_url = image.getString("url");
					listener.didGetImageUrl(request, image_url);
				} catch (JSONException e) {
				}
			}
		});

		NameValuePair[] params = { new BasicNameValuePair("action", "query"), new BasicNameValuePair("prop", "imageinfo"), new BasicNameValuePair("iiprop", "url"), new BasicNameValuePair("format", "json"), new BasicNameValuePair("titles", pageName) };
		return makeRequest(getImageUrlRequest, params);
	}

	public RequestTask getContentEnglish(final GetContentListener listener, final String title) {
		RequestTask getContentEnglishRequest = new GetRequestTask(mContext, "/index.php");

		getContentEnglishRequest.setListener(new RequestListener() {
			@Override
			public void onRequestFailed(RequestTask request) {
				requestFailedNoConnection(request, listener);
			}

			@Override
			public void onRequestCompleted(RequestTask request, String response) {
				Log.i(TAG, response);
				WebService.getInstance(mContext).getTitleEnglish(listener, title, response);
			}
		});

		NameValuePair[] params = { new BasicNameValuePair("action", "render"), new BasicNameValuePair("title", title) };
		return makeRequest(getContentEnglishRequest, params);
	}

	public RequestTask getTitleEnglish(final GetContentListener listener, String title, final String content) {
		RequestTask getTitleEnglishRequest = new GetRequestTask(mContext, "/api.php");

		getTitleEnglishRequest.setListener(new RequestListener() {
			@Override
			public void onRequestFailed(RequestTask request) {
				requestFailedNoConnection(request, listener);
			}

			@Override
			public void onRequestCompleted(RequestTask request, String response) {
				Log.i(TAG, response);
				try {
					JSONObject responseJSON = new JSONObject(response);
					JSONObject query = new JSONObject(responseJSON.getString("query"));
					JSONObject pages = new JSONObject(query.getString("pages"));
					JSONArray pageid = pages.names();
					JSONObject page = new JSONObject(pages.getString(pageid.getString(0)));
					String response_title = page.getString("title");
					Log.i(TAG, response_title);
					listener.didGetContent(request, content, response_title);
				} catch (JSONException e) {
					requestFailed(request, listener, false, Constants.ERROR_PAGE_DOES_NOT_EXIST);
				}
			}
		});

		NameValuePair[] params = { new BasicNameValuePair("action", "query"), new BasicNameValuePair("prop", "info"), new BasicNameValuePair("format", "json"), new BasicNameValuePair("titles", title) };
		return makeRequest(getTitleEnglishRequest, params);
	}

}
