package com.felixware.gw2w.utilities;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.felixware.gw2w.MainActivity;
import com.felixware.gw2w.R;

public class Dialogs {
	private Context mContext;
	private MainActivity mActivity;

	public Dialogs(Context context) {
		mContext = context;
		mActivity = (MainActivity) context;
	}

	public static void buildErrorDialog(Context context, int errorCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		switch (errorCode) {
		case Constants.ERROR_CONNECTION:
			builder.setTitle(R.string.error_connect_title);
			builder.setMessage(R.string.error_connect_text);
			break;
		case Constants.ERROR_PAGE_DOES_NOT_EXIST:
			builder.setTitle(R.string.error_no_page_title);
			builder.setMessage(R.string.error_no_page_text);
			break;
		default:
			builder.setTitle(R.string.error_generic_title);
			builder.setMessage(R.string.error_generic_text);
			break;
		}
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// nothing

			}

		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void buildFavoritesDialog(List<String> favorites) {
		final String favoritesArray[] = new String[favorites.size()];
		favorites.toArray(favoritesArray);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.favorites_dialog_title);
		builder.setItems(favoritesArray, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mActivity.getContent(favoritesArray[which]);
			}

		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	public void buildCategoriesDialog(List<String> categories) {
		final String categoriesArray[] = new String[categories.size()];
		categories.toArray(categoriesArray);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.title_categories);
		builder.setItems(categoriesArray, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mActivity.getContent(categoriesArray[which]);
			}

		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	public void buildNoFavoritesDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.no_favorites_dialog_title);
		builder.setView(mActivity.getLayoutInflater().inflate(R.layout.no_favorites_dialog_view, null));
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// nothing
			}

		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void buildExternalLinkDialog(final String url) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.external_link_title);
		builder.setMessage(String.format(mActivity.getResources().getString(R.string.external_link_text), url));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mActivity.externalLink(url);
			}

		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// nothing
			}

		});

		AlertDialog alert = builder.create();
		alert.show();

	}

}
