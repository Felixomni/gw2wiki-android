package com.felixware.gw2w.utilities;

import java.util.regex.Pattern;


public class Regexer {
	private static final Pattern redLinkPattern = Pattern.compile("<a href=\"[^\"]+?\" class=\"new\" title=\"[^\"]+?\">(.+?)<\\/a>");
	private static final Pattern editSectionPattern = Pattern.compile("<span class=\"editsection\">.+?<\\/span>");

	// Wooo! Take it off!
	public static String strip(String input) {
		input = removeRedLinks(input);
		input = removeEditSections(input);

		return input;
	}

	private static String removeRedLinks(String input) {
		return redLinkPattern.matcher(input).replaceAll("$1");
	}

	private static String removeEditSections(String input) {
		return editSectionPattern.matcher(input).replaceAll("");
	}
}