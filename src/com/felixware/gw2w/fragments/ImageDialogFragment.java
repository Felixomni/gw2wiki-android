package com.felixware.gw2w.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.felixware.gw2w.R;
import com.felixware.gw2w.utilities.DrawableDownloader;
import com.felixware.gw2w.utilities.DrawableDownloader.DrawableListener;

public class ImageDialogFragment extends DialogFragment implements OnClickListener, DrawableListener {
	private static final String IMAGE_URL = "image_url";
	private String image_url;
	private ImageView mImageView;
	private Button mCloseBtn;
	private ProgressBar mSpinner;

	public static ImageDialogFragment newInstance(String url) {
		ImageDialogFragment f = new ImageDialogFragment();
		Bundle args = new Bundle();
		args.putString(IMAGE_URL, url);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		image_url = getArguments().getString(IMAGE_URL);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.image_dialog_fragment, container, false);

		mImageView = (ImageView) v.findViewById(R.id.image);

		mSpinner = (ProgressBar) v.findViewById(R.id.spinner);

		mCloseBtn = (Button) v.findViewById(R.id.closeBtn);
		mCloseBtn.setOnClickListener(this);
		DrawableDownloader.getInstance(this).loadDrawable(image_url, mImageView, null, null);

		return v;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.closeBtn) {
			this.dismiss();
		}

	}

	@Override
	public void onDrawableLoaded() {
		mSpinner.setVisibility(View.GONE);
	}

}
