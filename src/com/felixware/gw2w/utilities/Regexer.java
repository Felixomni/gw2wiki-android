package com.felixware.gw2w.utilities;

import java.util.regex.Pattern;


public class Regexer {
	private static final Pattern redLinkPattern = Pattern.compile("<a href=\"[^\"]+?\" class=\"new\" title=\"[^\"]+?\">(.+?)<\\/a>");
	private static final Pattern editSectionPattern = Pattern.compile("<span class=\"editsection\">.+?<\\/span>");
	private static final Pattern fileLinkPattern = Pattern.compile("<a href=\"[^\"]+?\" class=\"image\">(.+?)<\\/a>");
	private static final Pattern fileInclusionPattern = Pattern.compile("(<img alt=\"[^\"]+?\" src=\"([^\"]+?)\".+?/>)");
	private static final Pattern imageUrlPattern = Pattern.compile("(.+?)thumb/(.+?)\\..+?\\.(.+?)");

	// Wooo! Take it off!
	public static String strip(String input) {
		input = removeRedLinks(input);
		input = removeEditSections(input);
		input = removeFileLinks(input);

		return input;
	}

	private static String removeRedLinks(String input) {
		return redLinkPattern.matcher(input).replaceAll("$1");
	}

	private static String removeEditSections(String input) {
		return editSectionPattern.matcher(input).replaceAll("");
	}

	private static String removeFileLinks(String input) {
		input = fileLinkPattern.matcher(input).replaceAll("$1");
		return fileInclusionPattern.matcher(input).replaceAll("<a href=\"$2\">$1</a>");
	}

	public static String getImageUrl(String url) {
		if (url.contains("thumb/"))
			return imageUrlPattern.matcher(url).replaceFirst("$1$2.$3");
		else
			return url;
	}

}
