package com.felixware.gw2w;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.felixware.gw2w.adapters.DropDownAdapter;
import com.felixware.gw2w.dialogs.ErrorDialog;
import com.felixware.gw2w.dialogs.NoFavoritesDialog;
import com.felixware.gw2w.fragments.ImageDialogFragment;
import com.felixware.gw2w.http.RequestTask;
import com.felixware.gw2w.http.WebService;
import com.felixware.gw2w.http.WebService.GetContentListener;
import com.felixware.gw2w.http.WebService.GetSearchResultsListener;
import com.felixware.gw2w.http.WebServiceException;
import com.felixware.gw2w.listeners.MainListener;
import com.felixware.gw2w.utilities.ArticleWebViewClient;
import com.felixware.gw2w.utilities.Constants;
import com.felixware.gw2w.utilities.PrefsManager;
import com.felixware.gw2w.utilities.Regexer;

public class MainActivity extends SherlockFragmentActivity implements OnNavigationListener, OnActionExpandListener, OnClickListener, MainListener, OnEditorActionListener, GetContentListener, GetSearchResultsListener, OnItemClickListener, OnFocusChangeListener {

	private WebView mWebContent;
	private RelativeLayout mNavBar, mWebSpinner;
	private EditText mSearchBox;
	private TextView mPageTitle;
	private ImageButton mSearchBtn;
	private ImageView mFavoriteBtn;
	private ProgressBar mSearchSpinner;
	private ErrorDialog mErrDialog;
	private Boolean isGoingBack = false, isNotSelectedResult = true, isFavorite = false;
	private List<String> backHistory = new ArrayList<String>(), favorites = new ArrayList<String>();
	private String currentPageTitle;
	private int lastLanguage = -1;
	private Handler mSearchHandle;
	private ListView mSearchResultsListView;
	private List<String> mList = new ArrayList<String>();
	private DropDownAdapter mAdapter;
	private ActionBar mActionBar;
	private MenuItem mSearch;
	private View mSearchView;
	private FrameLayout dummyView;
	private InputMethodManager imm;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_activity, menu);

		mSearch = menu.findItem(R.id.search);

		mSearchView = (View) mSearch.getActionView();

		mSearch.setOnActionExpandListener(this);

		mSearchBox = (EditText) mSearchView.findViewById(R.id.searchET);
		mSearchBox.setOnEditorActionListener(this);
		mSearchBox.addTextChangedListener(new SearchTextWatcher(mSearchBox));
		mSearchBox.setOnFocusChangeListener(this);

		mSearchBtn = (ImageButton) mSearchView.findViewById(R.id.searchBtn);
		mSearchBtn.setOnClickListener(this);

		mSearchSpinner = (ProgressBar) mSearchView.findViewById(R.id.spinner);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.favorites:
			favorites = Constants.getFavoritesListFromJSON(this);
			if (favorites.isEmpty()) {
				NoFavoritesDialog dialog = new NoFavoritesDialog(this);
				dialog.show();
			} else {
				buildFavoritesDialog();
			}
			return true;
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		mActionBar = getSupportActionBar();

		String array[] = getResources().getStringArray(R.array.Settings_wiki_languages);
		for (int i = 0; i < array.length; i++) {
			mList.add(array[i]);
		}

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		bindViews();

		mSearchHandle = new Handler();

		mWebContent = (WebView) findViewById(R.id.webContent);
		mWebContent.setWebViewClient(new ArticleWebViewClient(this));

		if (savedInstanceState != null) {
			if (mWebContent.restoreState(savedInstanceState) == null) {
				Log.i("Something broke", "Dang");
			}
		} else {
			getContent(Constants.getStartPage(this));
		}
	}

	private void bindViews() {

		dummyView = (FrameLayout) findViewById(R.id.dummy);

		Typeface tf = Typeface.createFromAsset(this.getAssets(), "easonpro-bold-webfont.ttf");

		mSearchResultsListView = (ListView) findViewById(R.id.searchResultsListView);
		mSearchResultsListView.setOnItemClickListener(this);

		mNavBar = (RelativeLayout) findViewById(R.id.navBar);
		mNavBar.bringToFront();

		mWebSpinner = (RelativeLayout) findViewById(R.id.webSpinnerLayout);

		mFavoriteBtn = (ImageView) findViewById(R.id.favoritesBtn);
		mFavoriteBtn.setOnClickListener(this);

		mPageTitle = (TextView) findViewById(R.id.pageTitle);
		mPageTitle.setTypeface(tf);

		mAdapter = new DropDownAdapter(this, mList);

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setListNavigationCallbacks(mAdapter, this);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (backHistory.size() > 1) {
				navigateBack();
				return true;
			}
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void navigateBack() {
		backHistory.remove(backHistory.size() - 1);
		getContent(backHistory.get(backHistory.size() - 1));
		isGoingBack = true;
	}

	protected void onSaveInstanceState(Bundle outState) {
		mWebContent.saveState(outState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.searchBtn:
			searchForTerm();
			break;

		case R.id.favoritesBtn:
			if (currentPageTitle != null) {
				if (!isFavorite) {
					favorites.add(currentPageTitle);
					PrefsManager.getInstance(this).setFavorites(Constants.getJSONStringFromList(favorites));
					mFavoriteBtn.setImageResource(R.drawable.nav_favorites_on);
					isFavorite = true;
				} else {
					favorites.remove(currentPageTitle);
					PrefsManager.getInstance(this).setFavorites(Constants.getJSONStringFromList(favorites));
					mFavoriteBtn.setImageResource(R.drawable.nav_favorites_off);
					isFavorite = false;
				}
			}
			break;
		}

	}

	public void getContent(String title) {
		if (title != null && !title.equals("")) {
			WebService.getInstance(this).cancelAllRequests();
			mWebSpinner.setVisibility(View.VISIBLE);
			if (PrefsManager.getInstance(this).getWikiLanguage() == Constants.ENGLISH) {
				WebService.getInstance(this).getTitleEnglish(this, title);
			} else {
				WebService.getInstance(this).getContent(this, title);
			}
		}
	}

	private void searchForTerm() {
		getContent(mSearchBox.getText().toString());
		mSearch.collapseActionView();
	}

	@Override
	public void onLink(String url) {
		Matcher matcher = Pattern.compile("(?<=wiki/).*").matcher(url);
		matcher.find();
		getContent(matcher.group());
	}

	@Override
	public void onExternalLink(String url) {
		if (PrefsManager.getInstance(this).getExternalWarning()) {
			buildExternalLinkDialog(url);
		} else {
			externalLink(url);
		}

	}

	@Override
	public void onExternalOkay(String url) {
		externalLink(url);
	}

	public void externalLink(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			searchForTerm();
			return true;
		}
		return false;
	}

	@Override
	public void onRequestError(RequestTask request, WebServiceException e) {
		mWebSpinner.setVisibility(View.GONE);
		mErrDialog = new ErrorDialog(this, e.getErrorCode());
		mErrDialog.show();

	}

	@Override
	public void didGetContent(RequestTask request, String content, String title) {
		mWebContent.loadDataWithBaseURL(Constants.getBaseURL(this), Regexer.strip(content), "text/html", "UTF-8", title);
		mPageTitle.setText(title);
		// Log.i("checking titles", "current page title is " + currentPageTitle + " new title is " + title);
		if (!isGoingBack && (currentPageTitle == null || !currentPageTitle.equals(title))) {
			// Log.i("back history", "Adding " + title + " to the back history");
			backHistory.add(title);
		} else {
			isGoingBack = false;
		}
		currentPageTitle = title;
		mFavoriteBtn.setImageResource(R.drawable.nav_favorites_off);
		isFavorite = false;
		determineFavoriteStatus();
		mWebSpinner.setVisibility(View.GONE);
	}

	private void determineFavoriteStatus() {
		favorites = Constants.getFavoritesListFromJSON(this);
		for (String pageName : favorites) {
			if (pageName.equals(currentPageTitle)) {
				isFavorite = true;
				mFavoriteBtn.setImageResource(R.drawable.nav_favorites_on);
				break;
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		WebService.getInstance(this).cancelAllRequests();
		mSearchSpinner.setVisibility(View.GONE);
		mWebSpinner.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lastLanguage != PrefsManager.getInstance(this).getWikiLanguage()) {
			backHistory.clear();
			mWebContent.clearView();
			mFavoriteBtn.setImageResource(R.drawable.nav_favorites_off);
			currentPageTitle = null;
			mPageTitle.setText("");
			getContent(Constants.getStartPage(this));
		}
	}

	private class SearchTextWatcher implements TextWatcher {

		private Runnable mSearchRunnable;

		public SearchTextWatcher(final EditText e) {
			mSearchRunnable = new Runnable() {
				public void run() {
					String searchText = e.getText().toString().trim();
					if (searchText != null && searchText.length() > 1) {
						mSearchSpinner.setVisibility(View.VISIBLE);
						WebService.getInstance(MainActivity.this).getSearchResults(MainActivity.this, searchText, 10);
					} else {

					}
				}
			};
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// nothing
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// nothing
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (isNotSelectedResult) {
				try {
					mSearchHandle.removeCallbacks(mSearchRunnable);
					mSearchHandle.postDelayed(mSearchRunnable, 1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	@Override
	public void didGetSearchResults(RequestTask request, List<String> list) {
		mSearchSpinner.setVisibility(View.GONE);
		mSearchResultsListView.setVisibility(View.VISIBLE);
		mSearchResultsListView.setAdapter(new ArrayAdapter<String>(this, R.layout.search_results_item, list));

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		mSearch.collapseActionView();
		getContent(((TextView) v).getText().toString());
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.searchET:
			if (hasFocus) {
				imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
			} else {
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
			break;
		default:
			break;
		}

	}

	private void buildFavoritesDialog() {
		final String favoritesArray[] = new String[favorites.size()];
		favorites.toArray(favoritesArray);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.favorites_dialog_title);
		builder.setItems(favoritesArray, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.this.getContent(favoritesArray[which]);
			}

		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void buildExternalLinkDialog(final String url) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.external_link_title);
		builder.setMessage(String.format(getResources().getString(R.string.external_link_text), url));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				externalLink(url);
			}

		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// nothing
			}

		});

		AlertDialog alert = builder.create();
		alert.show();

	}

	@Override
	public void onImageSelected(String url) {
		Log.i("Image URL", Regexer.getImageUrl(url));
		ImageDialogFragment newFragment = ImageDialogFragment.newInstance(url);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (PrefsManager.getInstance(this).getWikiLanguage() != itemPosition) {
			PrefsManager.getInstance(this).setWikiLanguage(itemPosition);
			backHistory.clear();
			mWebContent.clearView();
			currentPageTitle = null;
			mPageTitle.setText("");
			getContent(Constants.getStartPage(this));
		}
		return false;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		mSearchSpinner.setVisibility(View.INVISIBLE);
		mSearchBox.setText("");
		dummyView.requestFocus();
		mSearchResultsListView.setVisibility(View.GONE);
		return true;
	}

}
