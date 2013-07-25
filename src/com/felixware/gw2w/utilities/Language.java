package com.felixware.gw2w.utilities;

import java.net.URLEncoder;

/**
 * Supported languages.
 */
public enum Language {
	/**
	 * English wiki.
	 */
	ENGLISH(0, "EN", "", "Main Page"),

	/**
	 * German wiki.
	 */
	GERMAN(1, "DE", "-de", "Hauptseite"),

	/**
	 * Spanish wiki.
	 */
	SPANISH(2, "ES", "-es", "P‡gina principal"),

	/**
	 * French wiki.
	 */
	FRENCH(3, "FR", "-fr", "Accueil");

	private static final String SUBDOMAIN = "wiki";
	private static final String DOMAIN = ".guildwars2.com";

	private int id;
	private String languageCode;
	private String subdomainSuffix;
	private String host;
	private String baseUri;
	private String mainPage;

	/**
	 * Get the language from the internal id.
	 * 
	 * @param id
	 *            language id
	 * @return language with the given id
	 */
	public static Language fromId(int id) {
		switch (id) {
		case 0:
			return ENGLISH;
		case 1:
			return GERMAN;
		case 2:
			return SPANISH;
		case 3:
			return FRENCH;
		}
		return null;
	}

	private Language(int id, String languageCode, String subdomainSuffix, String mainPage) {
		this.id = id;
		this.languageCode = languageCode;
		this.subdomainSuffix = subdomainSuffix;
		this.mainPage = mainPage;

		StringBuilder host = new StringBuilder();
		host.append(SUBDOMAIN);
		host.append(subdomainSuffix);
		host.append(DOMAIN);

		this.host = host.toString();
		this.baseUri = "http://" + this.host;
	}

	/**
	 * Get the full link to the page with the given title.
	 * 
	 * @param title
	 *            page title
	 * @return full URI of the page
	 */
	public String getPageLink(String title) {
		StringBuilder uri = new StringBuilder(baseUri);
		uri.append("/wiki/");
		uri.append(URLEncoder.encode(title));

		return uri.toString();
	}

	/**
	 * Get the internal language id.
	 * 
	 * @return language id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the language code.
	 * 
	 * @return language code
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * Get the subdomain suffix.
	 * 
	 * @return subdomain suffix
	 */
	public String getSubdomainSuffix() {
		return subdomainSuffix;
	}

	/**
	 * Get the host name.
	 * 
	 * @return host name
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Get the base URI.
	 * 
	 * @return base URI
	 */
	public String getBaseUri() {
		return baseUri;
	}

	/**
	 * Get the title of the main page.
	 * 
	 * @return main page title
	 */
	public String getMainPage() {
		return mainPage;
	}
}