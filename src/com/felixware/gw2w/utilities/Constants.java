package com.felixware.gw2w.utilities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

import com.felixware.gw2w.R;

public class Constants {
	// not currently used
	public static String userAgentString = "GW2WApp/1.0 (https://github.com/Felixomni/gw2wiki-android; felixomni@gmail.com)";

	public static final int ERROR_CONNECTION = 0;
	public static final int ERROR_PAGE_DOES_NOT_EXIST = 1;
	public static final int ERROR_SERVER = 2;
	public static final int ERROR_UNKNOWN = 3;

	public static final String ENDPOINT = "/api.php";

	public static String getLanguage(Context context, int language) {
		return getLanguage(context, Language.fromId(language));
	}

	public static String getLanguage(Context context, Language language) {
		switch (language) {
		case ENGLISH:
			return context.getResources().getString(R.string.language_english);
		case GERMAN:
			return context.getResources().getString(R.string.language_german);
		case SPANISH:
			return context.getResources().getString(R.string.language_spanish);
		case FRENCH:
			return context.getResources().getString(R.string.language_french);
		default:
			return null;
		}
	}

	public static String getDomain(Context context) {
		Language language = PrefsManager.getInstance(context).getLanguage();
		return language.getHost();
	}

	public static String getBaseURL(Context context) {
		Language language = PrefsManager.getInstance(context).getLanguage();
		return language.getBaseUri();
	}

	public static String getStartPage(Context context) {
		Language language = PrefsManager.getInstance(context).getLanguage();
		return language.getMainPage();
	}

	public static List<String> getFavoritesListFromJSON(Context context) {
		List<String> favoritesList = new ArrayList<String>();
		String favoritesJSONString = PrefsManager.getInstance(context).getFavorites();
		if (favoritesJSONString == null) {
			return favoritesList;
		}
		try {
			JSONArray favoritesJSONArray = new JSONArray(favoritesJSONString);
			for (int i = 0; i < favoritesJSONArray.length(); i++) {
				favoritesList.add(favoritesJSONArray.getString(i));
			}
		} catch (JSONException e) {
		}
		return favoritesList;
	}

	public static String getJSONStringFromList(List<String> favorites) {
		JSONArray favoritesJSONArray = new JSONArray();
		for (String pageName : favorites) {
			favoritesJSONArray.put(pageName);
		}
		return favoritesJSONArray.toString();
	}
}
