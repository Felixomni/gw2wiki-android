package com.felixware.gw2w.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.felixware.gw2w.R;
import com.felixware.gw2w.listeners.MainListener;
import com.felixware.gw2w.models.NavMenuItem;
import com.felixware.gw2w.utilities.Constants;
import com.felixware.gw2w.utilities.PrefsManager;

public class NavMenuFragment extends Fragment implements OnClickListener {

	private MainListener mListener;
	private View mView;
	private LinearLayout mNavigationLayout;
	private RelativeLayout mSettings, mFavorites;
	private List<NavMenuItem> mNavigationItems = new ArrayList<NavMenuItem>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (MainListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		mView = inflater.inflate(R.layout.nav_menu_fragment, container, false);

		bindViews();

		return mView;
	}

	private void bindViews() {
		mSettings = (RelativeLayout) mView.findViewById(R.id.appSettingsLayout);
		mSettings.setOnClickListener(this);

		mFavorites = (RelativeLayout) mView.findViewById(R.id.favoritesLayout);
		mFavorites.setOnClickListener(this);

		mNavigationLayout = (LinearLayout) mView.findViewById(R.id.navigationLayout);
	}

	@Override
	public void onResume() {
		super.onResume();
		setupViews();
	}

	private void setupViews() {
		setupList();
		addItems(mNavigationLayout, mNavigationItems);
	}

	private void addItems(LinearLayout layout, final List<NavMenuItem> list) {
		layout.removeViews(1, layout.getChildCount() - 1);
		LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int count = 0;

		for (NavMenuItem item : list) {
			View itemView = inflater.inflate(R.layout.nav_menu_item, null);
			itemView.setTag(count);
			itemView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onNavItemSelected(list.get((Integer) v.getTag()).pageName);

				}

			});

			TextView text = (TextView) itemView.findViewById(R.id.text);
			text.setText(item.displayName);
			layout.addView(itemView);
			count++;
		}

	}

	private void setupList() {
		int language = PrefsManager.getInstance(getActivity()).getWikiLanguage();
		int display_id = 0;
		int page_id = 0;

		switch (language) {
		case Constants.GERMAN:
			display_id = R.array.de_Navigation_display;
			page_id = R.array.de_Navigation_pagename;
			break;
		case Constants.ENGLISH:
			display_id = R.array.en_Navigation_display;
			page_id = R.array.en_Navigation_pagename;
			break;
		case Constants.SPANISH:
			display_id = R.array.es_Navigation_display;
			page_id = R.array.es_Navigation_pagename;
			break;
		case Constants.FRENCH:
			display_id = R.array.fr_Navigation_display;
			page_id = R.array.fr_Navigation_pagename;
			break;
		}
		mNavigationItems.clear();
		String navdisplay[] = getResources().getStringArray(display_id);
		String navpage[] = getResources().getStringArray(page_id);
		for (int i = 0; i < navdisplay.length; i++) {
			mNavigationItems.add(new NavMenuItem(navdisplay[i], navpage[i]));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.appSettingsLayout:
			mListener.onSettingsSelected();
			break;
		case R.id.favoritesLayout:
			mListener.onFavoritesSelected();
			break;
		}
	}
}
