package com.felixsoft.gw2w;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

/*
 * This is a vanity splash screen. It's mainly for presentation, but it's also in case
 * the app ever needs to do something during loading. This way I won't have to add a new
 * screen in an update, which would confuse users. But who am I kidding- I just like the
 * way it looks.
 */

public class LoadingActivity extends Activity {
	private ImageView mImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_activity);

		bindViews();

		reticulateSplines();
	}

	private void reticulateSplines() {
		Timer splines = new Timer();
		splines.schedule(new SplinesReticulated(), 5000);

	}

	private void goForward() {
		finish();
		Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
		startActivity(intent);
	}

	private void bindViews() {
		mImage = (ImageView) findViewById(R.id.loadingImage);
		mImage.setBackgroundResource(R.drawable.anim_dragon);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			AnimationDrawable frameAnimation = (AnimationDrawable) mImage.getBackground();

			frameAnimation.start();
		}
	}

	class SplinesReticulated extends TimerTask {
		@Override
		public void run() {
			goForward();
		}
	}

}
