package com.felixware.gw2w.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.felixware.gw2w.R;

public class NoFavoritesDialog extends Dialog implements OnClickListener {
	private Button mConfirm;

	public NoFavoritesDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.no_favorites_dialog);

		bindViews();
	}

	private void bindViews() {

		mConfirm = (Button) findViewById(R.id.confirmBtn);
		mConfirm.setOnClickListener(this);

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
