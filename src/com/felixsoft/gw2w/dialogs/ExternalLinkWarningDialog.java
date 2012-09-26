package com.felixsoft.gw2w.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.felixsoft.gw2w.R;
import com.felixsoft.gw2w.listeners.MainListener;
import com.felixsoft.gw2w.utilities.PrefsManager;

public class ExternalLinkWarningDialog extends Dialog implements OnClickListener, OnDismissListener {
	private Context mContext;
	private String url;
	private TextView mText;
	private CheckBox mCheck;
	private Button mPos, mNeg;
	private MainListener mListener;

	public ExternalLinkWarningDialog(Context context, String url) {
		super(context);
		mContext = context;
		mListener = (MainListener) context;
		this.url = url;
		this.setOnDismissListener(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.external_link_warning_dialog);

		bindViews();
	}

	private void bindViews() {
		mText = (TextView) findViewById(R.id.text);
		mCheck = (CheckBox) findViewById(R.id.dontShowCB);
		mPos = (Button) findViewById(R.id.posBtn);
		mPos.setOnClickListener(this);
		mNeg = (Button) findViewById(R.id.negBtn);
		mNeg.setOnClickListener(this);

		mText.setText(String.format(mContext.getResources().getString(R.string.external_link_text), url));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.posBtn:
			mListener.onExternalOkay(url);
		case R.id.negBtn:
			this.dismiss();
			break;
		}

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mCheck.isChecked()) {
			PrefsManager.getInstance(mContext).setExternalWarning(false);
		}
	}

}
