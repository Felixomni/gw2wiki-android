package com.felixsoft.gw2w.utilities;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.felixsoft.gw2w.listeners.MainListener;

public class ArticleWebViewClient extends WebViewClient {
	private Context mContext;
	private MainListener mListener;

	public ArticleWebViewClient(Context context) {
		mContext = context;
		mListener = (MainListener) context;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (Uri.parse(url).getHost().equals(Constants.getDomain(mContext))) {
			if (Uri.parse(url).getQueryParameter("action") != null && Uri.parse(url).getQueryParameter("action").equals("edit")) {
				Toast.makeText(mContext, "Sorry, editing not supported yet", Toast.LENGTH_SHORT).show();
			} else {
				mListener.onLink(url);
			}
		} else {
			mListener.onExternalLink(url);
		}
		return true;
	}

}
