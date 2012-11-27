package com.felixware.gw2w.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefsManager {
	private static final String EXTERNAL_LINK_WARNING = "ext_warning";
	private static final String WIKI_LANGUAGE = "wiki_language";
	private static final String FAVORITES_ENGLISH = "favorites_english";
	private static final String FAVORITES_GERMAN = "favorites_german";
	private static final String FAVORITES_SPANISH = "favorites_spanish";
	private static final String FAVORITES_FRENCH = "favorites_french";

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

	public int getWikiLanguage() {
		return mSharedPreferences.getInt(WIKI_LANGUAGE, 0);
	}

	public void setWikiLanguage(int language) {
		mEditor.putInt(WIKI_LANGUAGE, language);
		mEditor.commit();
	}

	public Language getLanguage() {
		return Language.fromId(mSharedPreferences.getInt(WIKI_LANGUAGE, 0));
	}

	public void setLanguage(Language language) {
		mEditor.putInt(WIKI_LANGUAGE, language.getId());
		mEditor.commit();
	}

	public String getFavorites() {
		switch (getLanguage()) {
			case ENGLISH:
				return mSharedPreferences.getString(FAVORITES_ENGLISH, null);
			case GERMAN:
				return mSharedPreferences.getString(FAVORITES_GERMAN, null);
			case SPANISH:
				return mSharedPreferences.getString(FAVORITES_SPANISH, null);
			case FRENCH:
				return mSharedPreferences.getString(FAVORITES_FRENCH, null);
		}
		return null;
	}

	public void setFavorites(String favoritesJSON) {
		switch (getLanguage()) {
			case ENGLISH:
				mEditor.putString(FAVORITES_ENGLISH, favoritesJSON);
				break;
			case GERMAN:
				mEditor.putString(FAVORITES_GERMAN, favoritesJSON);
				break;
			case SPANISH:
				mEditor.putString(FAVORITES_SPANISH, favoritesJSON);
				break;
			case FRENCH:
				mEditor.putString(FAVORITES_FRENCH, favoritesJSON);
				break;
		}
		mEditor.commit();
	}
}
