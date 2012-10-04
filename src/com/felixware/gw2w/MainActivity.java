package com.felixware.gw2w;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.felixware.gw2w.dialogs.ErrorDialog;
import com.felixware.gw2w.dialogs.ExternalLinkWarningDialog;
import com.felixware.gw2w.fragments.ImageDialogFragment;
import com.felixware.gw2w.fragments.NavMenuFragment;
import com.felixware.gw2w.http.RequestTask;
import com.felixware.gw2w.http.WebService;
import com.felixware.gw2w.http.WebService.GetContentListener;
import com.felixware.gw2w.http.WebService.GetImageUrlListener;
import com.felixware.gw2w.http.WebService.GetSearchResultsListener;
import com.felixware.gw2w.http.WebServiceException;
import com.felixware.gw2w.listeners.MainListener;
import com.felixware.gw2w.utilities.ArticleWebViewClient;
import com.felixware.gw2w.utilities.Constants;
import com.felixware.gw2w.utilities.LinkStripper;
import com.felixware.gw2w.utilities.PrefsManager;

public class MainActivity extends FragmentActivity implements OnClickListener, MainListener, OnEditorActionListener, GetContentListener, GetSearchResultsListener, OnItemClickListener, OnFocusChangeListener, GetImageUrlListener {
	private static final String NAV_STATE = "nav_state";

	private WebView mWebContent;
	private FrameLayout mNavContent;
	private LinearLayout mSearchResultsLayout;
	private RelativeLayout mNavBar, mWebSpinner;
	private EditText mSearchBox;
	private TextView mPageTitle;
	private ImageButton mSearchBtn;
	private ImageView mHomeLogo, mNavBtn, mFavoriteBtn, mCancelSearchBtn;
	private ProgressBar mSearchSpinner;
	private ExternalLinkWarningDialog mExtDialog;
	private ErrorDialog mErrDialog;
	private Boolean isNavShown = false, isGoingBack = false, isViewDown = false, isNotSelectedResult = true, isFavorite = false;
	private NavMenuFragment mNavFragment = new NavMenuFragment();
	private List<String> backHistory = new ArrayList<String>(), favorites = new ArrayList<String>();
	private String currentPageTitle;
	private int lastLanguage = -1;
	private Handler mSearchHandle;
	private ListView mSearchResultsListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		bindViews();
		mSearchHandle = new Handler();

		mWebContent = (WebView) findViewById(R.id.webContent);
		mWebContent.setWebViewClient(new ArticleWebViewClient(this));

		if (savedInstanceState != null) {
			if (mWebContent.restoreState(savedInstanceState) == null) {
				Log.i("Something broke", "Dang");
			}
			if (savedInstanceState.getBoolean(NAV_STATE)) {
				toggleNav();
			}
		} else {
			getContent(PrefsManager.getInstance(this).getStartPage());
		}
	}

	private void bindViews() {

		mHomeLogo = (ImageView) findViewById(R.id.logo);
		mHomeLogo.setOnClickListener(this);

		mSearchResultsListView = (ListView) findViewById(R.id.searchResultsListView);
		mSearchResultsListView.setOnItemClickListener(this);

		mNavBar = (RelativeLayout) findViewById(R.id.navBar);

		mWebSpinner = (RelativeLayout) findViewById(R.id.webSpinnerLayout);

		mNavContent = (FrameLayout) findViewById(R.id.navMenuContent);

		mSearchResultsLayout = (LinearLayout) findViewById(R.id.searchResultsLayout);

		mNavBtn = (ImageView) findViewById(R.id.navBtn);
		mNavBtn.setOnClickListener(this);

		mFavoriteBtn = (ImageView) findViewById(R.id.favoritesBtn);
		mFavoriteBtn.setOnClickListener(this);

		mSearchBox = (EditText) findViewById(R.id.searchET);
		mSearchBox.setOnEditorActionListener(this);
		mSearchBox.addTextChangedListener(new SearchTextWatcher(mSearchBox));
		mSearchBox.setOnFocusChangeListener(this);

		mSearchBtn = (ImageButton) findViewById(R.id.searchBtn);
		mSearchBtn.setOnClickListener(this);

		mCancelSearchBtn = (ImageView) findViewById(R.id.cancelSearchBtn);
		mCancelSearchBtn.setOnClickListener(this);

		mSearchSpinner = (ProgressBar) findViewById(R.id.spinner);

		mPageTitle = (TextView) findViewById(R.id.pageTitle);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (isViewDown) {
				animateViewUp();
				return true;
			} else if (backHistory.size() > 1) {
				navigateBack();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_MENU:
			toggleNav();
			return true;
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
		outState.putBoolean(NAV_STATE, isNavShown);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.searchBtn:
			searchForTerm();
			break;
		case R.id.logo:
		case R.id.navBtn:
			toggleNav();
			break;
		case R.id.cancelSearchBtn:
			hideKeyboard();
			mSearchBox.setText("");
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

	private void toggleNav() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (isNavShown) {
			isNavShown = false;
			mNavBtn.setImageResource(R.drawable.nav_arrow_down);
			ft.remove(mNavFragment);
		} else {
			isNavShown = true;
			mNavBtn.setImageResource(R.drawable.nav_arrow_up);
			ft.add(R.id.navMenuContent, mNavFragment);
			mNavContent.bringToFront();
		}
		ft.commit();

	}

	public void getContent(String title) {
		if (title != null && !title.equals("")) {
			WebService.getInstance(this).cancelAllRequests();
			mWebSpinner.setVisibility(View.VISIBLE);
			WebService.getInstance(this).getContent(this, title);
		}
	}

	private void searchForTerm() {
		mSearchSpinner.setVisibility(View.GONE);
		hideKeyboard();
		getContent(mSearchBox.getText().toString());
		mSearchBox.setText("");
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
		mSearchBox.clearFocus();
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
			mExtDialog = new ExternalLinkWarningDialog(this, url);
			mExtDialog.show();
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
	public void onNavItemSelected(String pageName) {
		getContent(pageName);
	}

	@Override
	public void onRequestError(RequestTask request, WebServiceException e) {
		mSearchSpinner.setVisibility(View.GONE);
		mWebSpinner.setVisibility(View.GONE);
		mErrDialog = new ErrorDialog(this, e.getErrorCode());
		mErrDialog.show();

	}

	@Override
	public void didGetContent(RequestTask request, String content, String title) {
		mWebContent.loadDataWithBaseURL(Constants.getBaseURL(this), LinkStripper.strip(content), "text/html", "UTF-8", title);
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
		lastLanguage = PrefsManager.getInstance(this).getWikiLanguage();
		hideKeyboard();
		mSearchBox.setText("");
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
			getContent(PrefsManager.getInstance(this).getStartPage());
		}
	}

	@Override
	public void onSettingsSelected() {
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
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
		animateViewDown();
		mSearchResultsListView.setAdapter(new ArrayAdapter<String>(this, R.layout.search_results_item, list));

	}

	private void animateViewDown() {
		if (mNavBar.getLayoutParams() instanceof MarginLayoutParams && !isViewDown) {
			final MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mNavBar.getLayoutParams();

			final int startValueY = marginLayoutParams.topMargin;
			final int endValueY = marginLayoutParams.topMargin + 250;

			mNavBar.clearAnimation();

			Animation animation = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					int topMarginInterpolatedValue = (int) (startValueY + (endValueY - startValueY) * interpolatedTime);
					marginLayoutParams.topMargin = topMarginInterpolatedValue;

					mNavBar.requestLayout();
				}
			};
			animation.setDuration(500);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mSearchResultsLayout.setVisibility(View.VISIBLE);

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationStart(Animation animation) {
					isViewDown = true;

				}

			});
			mNavBar.startAnimation(animation);

		}

	}

	private void animateViewUp() {
		if (mNavBar.getLayoutParams() instanceof MarginLayoutParams && isViewDown) {
			final MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mNavBar.getLayoutParams();

			final int startValueY = marginLayoutParams.topMargin;
			final int endValueY = marginLayoutParams.topMargin - 250;

			mNavBar.clearAnimation();

			Animation animation = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					int topMarginInterpolatedValue = (int) (startValueY + (endValueY - startValueY) * interpolatedTime);
					marginLayoutParams.topMargin = topMarginInterpolatedValue;

					mNavBar.requestLayout();
				}
			};
			animation.setDuration(500);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mSearchResultsLayout.setVisibility(View.GONE);

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationStart(Animation animation) {
					isViewDown = false;

				}

			});
			mNavBar.startAnimation(animation);

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		isNotSelectedResult = false;
		mSearchBox.setText(((TextView) v).getText().toString());
		isNotSelectedResult = true;

	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.searchET:
			if (hasFocus) {
				mCancelSearchBtn.setVisibility(View.VISIBLE);
			} else {
				mCancelSearchBtn.setVisibility(View.INVISIBLE);
				animateViewUp();
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onFavoritesSelected() {
		favorites = Constants.getFavoritesListFromJSON(this);
		if (favorites.isEmpty()) {
			// TODO
		} else {
			buildFavoritesDialog();
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

	@Override
	public void onImageSelected(String url) {
		Matcher matcher = Pattern.compile("(?<=wiki/).*").matcher(url);
		matcher.find();
		WebService.getInstance(this).getImageUrl(this, matcher.group());
	}

	@Override
	public void didGetImageUrl(RequestTask request, String url) {
		ImageDialogFragment newFragment = ImageDialogFragment.newInstance(url);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

}
