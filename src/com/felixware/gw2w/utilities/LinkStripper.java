package com.felixware.gw2w.utilities;

import jregex.Matcher;
import jregex.Pattern;
import jregex.Replacer;
import android.util.Log;

public class LinkStripper {
	private static String mInput;

	// Wooo! Take it off!
	public static String strip(String input) {
		mInput = input;

		removeRedLinks();

		removeEditSections();

		// Log.i("Regex", mInput);

		return mInput;
	}

	private static void removeRedLinks() {
		Pattern pattern = new Pattern("<a href=\".+?\" class=\"new\" title=\".+?\">(.+?)<\\/a>");
		Replacer replacer = pattern.replacer("$1");
		String result = replacer.replace(mInput);
		mInput = result;

	}

	private static void removeEditSections() {
		Pattern pattern = new Pattern("<span class=\"editsection\">.+?<\\/span>");
		Replacer replacer = pattern.replacer("");
		String result = replacer.replace(mInput);
		mInput = result;
	}

	// Ended up not using this, but it might be useful eventually
	private static void removeFileLinks() {
		Pattern pattern = new Pattern("<a href=\".+?\" class=\"image\">(.+?)<\\/a>");
		Replacer replacer = pattern.replacer("$1");
		String result = replacer.replace(mInput);

		Pattern pattern2 = new Pattern("(<img alt=\".+?\" src=\"(.+?)\".+?/>)");
		Replacer replacer2 = pattern2.replacer("<a href=\"$2\">$1</a>");
		String result2 = replacer2.replace(result);
		mInput = result2;

	}

	public static String resolveRedirect(String content) {
		Pattern pattern = new Pattern("<ol><li>REDIRECT <a href=.+?\">(.+?)<\\/a>");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			Log.i("EJIFE", matcher.group(1));
			return matcher.group(1);
		} else {
			return content;
		}
	}

}
