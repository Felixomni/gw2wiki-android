package com.felixware.gw2w.models;

import java.util.ArrayList;

public class Content {
	private String title;
	private String content;
	private String fileUrl;
	private ArrayList<String> categories;

	public Content() {
	};

	public Content(String title, String content, String fileUrl, ArrayList<String> categories) {
		this.title = title;
		this.content = content;
		this.fileUrl = fileUrl;
		this.categories = categories;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setCategories(ArrayList<String> categories) {
		this.categories = categories;
	}

	public ArrayList<String> getCategories() {
		return categories;
	}
}
