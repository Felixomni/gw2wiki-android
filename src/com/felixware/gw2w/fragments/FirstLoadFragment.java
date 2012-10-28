package com.felixware.gw2w.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.felixware.gw2w.R;

public class FirstLoadFragment extends Fragment {
	private ImageView mImage;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.first_load_fragment, container, false);

		mImage = (ImageView) v.findViewById(R.id.loadingImage);
		mImage.setBackgroundResource(R.drawable.anim_dragon);

		AnimationDrawable frameAnimation = (AnimationDrawable) mImage.getBackground();

		frameAnimation.start();

		return v;
	}

}
