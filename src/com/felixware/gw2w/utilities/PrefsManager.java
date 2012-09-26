package com.felixware.gw2w.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefsManager {
	private static final String EXTERNAL_LINK_WARNING = "ext_warning";
	private static final String START_PAGE = "start_page";
	private static final String WIKI_LANGUAGE = "wiki_language";

	private volatile static PrefsManager sUniqueInstance;

	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;

	private PrefsManager(Context context) {
		mSharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
	}

	public static PrefsManager getInstance(Context context) {
		if (sUniqueInstance == null) {
			synchronized (PrefsManager.class) {
				sUniqueInstance = new PrefsManager(context);
			}
		}
		return sUniqueInstance;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public boolean getExternalWarning() {
		return mSharedPreferences.getBoolean(EXTERNAL_LINK_WARNING, true);
	}

	public void setExternalWarning(boolean bool) {
		mEditor.putBoolean(EXTERNAL_LINK_WARNING, bool);
		mEditor.commit();
	}

	public String getStartPage() {
		return mSharedPreferences.getString(START_PAGE, "");
	}

	public void setStartPage(String page) {
		mEditor.putString(START_PAGE, page);
		mEditor.commit();
	}

	public int getWikiLanguage() {
		return mSharedPreferences.getInt(WIKI_LANGUAGE, Constants.GERMAN);
	}

	public void setWikiLanguage(int language) {
		mEditor.putInt(WIKI_LANGUAGE, language);
	}
}
