package com.felixware.gw2w.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.felixware.gw2w.R;

public class DropDownAdapter extends ArrayAdapter<String> {
	private LayoutInflater inflater;
	private String[] items;
	private String[] codes;

	public DropDownAdapter(Context context, String[] items, String[] codes) {
		super(context, 0, items);
		this.inflater = LayoutInflater.from(context);
		this.items = items;
		this.codes = codes;
	}

	private static class ViewHolder {
		TextView language;
		TextView languageCode;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();

			convertView = inflater.inflate(R.layout.language_ab_item, null);

			holder.language = (TextView) convertView.findViewById(R.id.language);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.language.setText(items[pos]);

		return convertView;
	}

	@Override
	public View getDropDownView(int pos, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();

			convertView = inflater.inflate(R.layout.language_dropdown_item, null);

			holder.language = (TextView) convertView.findViewById(R.id.language);

			holder.languageCode = (TextView) convertView.findViewById(R.id.languageCode);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.language.setText(items[pos]);
		holder.languageCode.setText(codes[pos]);

		return convertView;
	}

}
