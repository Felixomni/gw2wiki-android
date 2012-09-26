package com.felixsoft.gw2w.listeners;

public interface MainListener {

	public void onLink(String url);

	public void onExternalLink(String url);

	public void onExternalOkay(String url);

	public void onNavItemSelected(String pageName);

	public void onSettingsSelected();
}
