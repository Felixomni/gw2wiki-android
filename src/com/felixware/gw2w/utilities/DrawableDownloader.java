package com.felixware.gw2w.utilities;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class DrawableDownloader {

	private final Map<String, SoftReference<Drawable>> mCache = new HashMap<String, SoftReference<Drawable>>();
	private ExecutorService mThreadPool;
	private final Map<ImageView, String> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

	public static int MAX_CACHE_SIZE = 80;
	public int THREAD_POOL_SIZE = 3;
	private static volatile DrawableDownloader instance = null;

	/**
	 * Constructor
	 */
	private DrawableDownloader() {
		mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	}

	public static DrawableDownloader getInstance() {
		if (instance == null) {
			synchronized (DrawableDownloader.class) {
				if (instance == null) {
					instance = new DrawableDownloader();
				}
			}
		}
		return instance;
	}

	/**
	 * Clears all instance data and stops running threads
	 */
	public void Reset() {
		ExecutorService oldThreadPool = mThreadPool;
		mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		oldThreadPool.shutdownNow();
		mCache.clear();
		mImageViews.clear();
	}

	public void loadDrawable(final String url, final ImageView imageView, RelativeLayout loaderBox, Drawable placeholder) {
		mImageViews.put(imageView, url);
		Drawable drawable = getDrawableFromCache(url);

		// check in UI thread, so no concurrency issues
		if (drawable != null) {
			// Log.d(null, "Item loaded from mCache: " + url);
			imageView.setImageDrawable(drawable);
			if (loaderBox != null)
				loaderBox.setVisibility(View.GONE);
		} else {
			if (loaderBox != null)
				loaderBox.setVisibility(View.VISIBLE);
			imageView.setImageDrawable(placeholder);
			queueJob(url, imageView, loaderBox, placeholder);
		}
	}

	private Drawable getDrawableFromCache(String url) {
		if (mCache.containsKey(url)) {
			return mCache.get(url).get();
		}

		return null;
	}

	private synchronized void putDrawableInCache(String url, Drawable drawable) {
		mCache.put(url, new SoftReference<Drawable>(drawable));

	}

	private void queueJob(final String url, final ImageView imageView, final RelativeLayout loaderBox, final Drawable placeholder) {
		/* Create handler in UI thread. */
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String tag = mImageViews.get(imageView);
				if (tag != null && tag.equals(url)) {
					if (imageView.isShown())
						if (msg.obj != null) {
							imageView.setImageDrawable((Drawable) msg.obj);
							if (loaderBox != null)
								loaderBox.setVisibility(View.GONE);
						} else {
							if (loaderBox != null)
								loaderBox.setVisibility(View.VISIBLE);
							imageView.setImageDrawable(placeholder);
							// Log.d(null, "fail " + url);
						}
				}
			}
		};

		mThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				final Drawable bmp = downloadDrawable(url);
				// if the view is not visible anymore, the image will be ready
				// for next time in cache
				if (imageView.isShown()) {
					Message message = Message.obtain();
					message.obj = bmp;
					// Log.d(null, "Item downloaded: " + url);

					handler.sendMessage(message);
				}
			}
		});
	}

	private Drawable downloadDrawable(String url) {
		try {
			InputStream is = getInputStream(url);

			Drawable drawable = Drawable.createFromStream(new FlushedInputStream(is), url);
			putDrawableInCache(url, drawable);
			return drawable;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			Reset();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private InputStream getInputStream(String urlString) throws MalformedURLException, IOException {
		// URL url = new URL(urlString);
		// URLConnection connection;
		// connection = url.openConnection();
		// connection.setUseCaches(true);
		// connection.connect();
		// InputStream response = connection.getInputStream();

		HttpGet httpRequest = new HttpGet(urlString);
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(response.getEntity());
		return bufHttpEntity.getContent();

	}

	/**
	 * A patched InputSteam that tries harder to fully read the input stream.
	 */
	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L)
					break;
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
}
