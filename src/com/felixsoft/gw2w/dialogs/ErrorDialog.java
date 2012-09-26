package com.felixsoft.gw2w.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.felixsoft.gw2w.R;
import com.felixsoft.gw2w.utilities.Constants;

public class ErrorDialog extends Dialog implements OnClickListener {
	private Context mContext;
	private TextView mText, mTitle;
	private Button mConfirm;
	private int errorCode;

	public ErrorDialog(Context context, int errorCode) {
		super(context);
		mContext = context;
		this.errorCode = errorCode;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.error_dialog);

		bindViews();
	}

	private void bindViews() {
		mTitle = (TextView) findViewById(R.id.title);
		mText = (TextView) findViewById(R.id.text);
		mConfirm = (Button) findViewById(R.id.confirmBtn);
		mConfirm.setOnClickListener(this);

		setupViews();

	}

	private void setupViews() {
		switch (errorCode) {
		case Constants.ERROR_CONNECTION:
			mTitle.setText(R.string.error_connect_title);
			mText.setText(R.string.error_connect_text);
			break;
		case Constants.ERROR_PAGE_DOES_NOT_EXIST:
			mTitle.setText(R.string.error_no_page_title);
			mText.setText(R.string.error_no_page_text);
			break;
		default:
			mTitle.setText(R.string.error_generic_title);
			mText.setText(R.string.error_generic_text);
			break;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.confirmBtn:
			this.dismiss();
			break;
		}

	}
}
