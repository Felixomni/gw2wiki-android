package com.felixware.gw2w.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

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
		public void didGetFileUrl(RequestTask request, String url, String title);
		public void didGetCategories(RequestTask request, List<String> categories, String title);
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
					JSONObject pages = responseJSON.getJSONObject("query").getJSONObject("pages");
					JSONObject page = pages.getJSONObject(pages.names().getString(0));
					String title = page.getString("title");

					if (page.has("missing")) {
						throw new JSONException(null);
					}

					if (page.getInt("ns") == 6) {
						JSONObject imageInfo = page.getJSONArray("imageinfo").getJSONObject(0);
						listener.didGetFileUrl(request, imageInfo.getString("url"), title);
					} else {
						// categories
						if (page.has("categories")) {
							JSONArray categories = page.getJSONArray("categories");
							ArrayList<String> cats = new ArrayList<String>(categories.length());

							for (int i = 0; i < categories.length(); i++) {
								cats.add(categories.getJSONObject(i).getString("title"));
							}
							listener.didGetCategories(request, cats, title);
						}

						// page content
						JSONObject revision = page.getJSONArray("revisions").getJSONObject(0);
						listener.didGetContent(request, revision.getString("*"), title);
					}
				} catch (JSONException e) {
					requestFailed(request, listener, false, Constants.ERROR_PAGE_DOES_NOT_EXIST);
				}
			}
		});

		NameValuePair[] params = {
				new BasicNameValuePair("format", "json"),
				new BasicNameValuePair("action", "query"),
				new BasicNameValuePair("prop", "revisions|categories|imageinfo"),
				new BasicNameValuePair("rvprop", "content"),
				new BasicNameValuePair("rvparse", "1"),
				new BasicNameValuePair("iiprop", "url"),
				new BasicNameValuePair("titles", title),
				new BasicNameValuePair("redirects", "1") };
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

	public RequestTask getContentEnglish(final GetContentListener listener, final String title) {
		RequestTask getContentEnglishRequest = new GetRequestTask(mContext, "/index.php");

		getContentEnglishRequest.setListener(new RequestListener() {
			@Override
			public void onRequestFailed(RequestTask request) {
				requestFailedNoConnection(request, listener);
			}

			@Override
			public void onRequestCompleted(RequestTask request, String response) {
				listener.didGetContent(request, response, title);
			}
		});

		NameValuePair[] params = { new BasicNameValuePair("action", "render"), new BasicNameValuePair("title", title) };
		return makeRequest(getContentEnglishRequest, params);
	}

	public RequestTask getTitleEnglish(final GetContentListener listener, String title) {
		RequestTask getTitleEnglishRequest = new GetRequestTask(mContext, "/api.php");

		getTitleEnglishRequest.setListener(new RequestListener() {
			@Override
			public void onRequestFailed(RequestTask request) {
				requestFailedNoConnection(request, listener);
			}

			@Override
			public void onRequestCompleted(RequestTask request, String response) {
				try {
					JSONObject responseJSON = new JSONObject(response);
					JSONObject pages = responseJSON.getJSONObject("query").getJSONObject("pages");
					JSONObject page = pages.getJSONObject(pages.names().getString(0));
					String title = page.getString("title");

					if (page.has("missing")) {
						throw new JSONException(null);
					}

					if (page.getInt("ns") == 6) {
						JSONObject imageInfo = page.getJSONArray("imageinfo").getJSONObject(0);
						listener.didGetFileUrl(request, imageInfo.getString("url"), title);
					} else {
						// categories
						if (page.has("categories")) {
							JSONArray categories = page.getJSONArray("categories");
							ArrayList<String> cats = new ArrayList<String>(categories.length());

							for (int i = 0; i < categories.length(); i++) {
								cats.add(categories.getJSONObject(i).getString("title"));
							}
							listener.didGetCategories(request, cats, title);
						}

						// page content
						WebService.getInstance(mContext).getContentEnglish(listener, title);
					}
				} catch (JSONException e) {
					requestFailed(request, listener, false, Constants.ERROR_PAGE_DOES_NOT_EXIST);
				}
			}
		});

		NameValuePair[] params = {
				new BasicNameValuePair("format", "json"),
				new BasicNameValuePair("action", "query"),
				new BasicNameValuePair("prop", "categories|imageinfo"),
				new BasicNameValuePair("iiprop", "url"),
				new BasicNameValuePair("titles", title),
				new BasicNameValuePair("redirects", "1") };
		return makeRequest(getTitleEnglishRequest, params);
	}

}
