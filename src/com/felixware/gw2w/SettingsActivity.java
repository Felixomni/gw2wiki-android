package com.felixware.gw2w;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felixware.gw2w.utilities.Constants;
import com.felixware.gw2w.utilities.PrefsManager;

public class SettingsActivity extends Activity implements OnClickListener {
	private EditText mStartPageBox;
	private TextView mCurrentLanguage;
	private Button mChangeLanguageBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);

		bindViews();
	}

	private void bindViews() {
		mStartPageBox = (EditText) findViewById(R.id.startPageBox);
		mCurrentLanguage = (TextView) findViewById(R.id.setWikiLanguageCurrent);
		mChangeLanguageBtn = (Button) findViewById(R.id.setWikiLanguageBtn);
		mChangeLanguageBtn.setOnClickListener(this);

	}

	private void setupViews() {
		mStartPageBox.setText(PrefsManager.getInstance(this).getStartPage());
		mCurrentLanguage.setText(String.format(getResources().getString(R.string.settings_wiki_language_current), Constants.getLanguage(this, PrefsManager.getInstance(this).getWikiLanguage())));

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setWikiLanguageBtn:
			buildWikiLanguageDialog();
			break;
		}

	}

	private void buildWikiLanguageDialog() {
		final String languages[] = this.getResources().getStringArray(R.array.Settings_wiki_languages);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.settings_wiki_language_dialog_title);
		builder.setItems(languages, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setLanguage(which);
			}

		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	protected void setLanguage(int language) {
		// Toast.makeText(this, Integer.toString(language), Toast.LENGTH_SHORT).show();
		PrefsManager.getInstance(this).setWikiLanguage(language);
		mCurrentLanguage.setText(String.format(getResources().getString(R.string.settings_wiki_language_current), Constants.getLanguage(this, language)));
	}

	@Override
	protected void onResume() {
		setupViews();
		super.onResume();
	}

	@Override
	protected void onPause() {
		PrefsManager.getInstance(this).setStartPage(mStartPageBox.getText().toString());
		super.onPause();
	}

}
