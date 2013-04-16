package org.mariotaku.harmony.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.mariotaku.harmony.R;
import android.util.Log;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import android.text.TextUtils;
import android.graphics.BitmapFactory;

public class ImageLoaderWrapper {
	
	private final ImageLoader mImageLoader;
	private final DisplayImageOptions mImageDisplayOptions;

	public ImageLoaderWrapper(final ImageLoader loader) {
		mImageLoader = loader;
		final DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
		final BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inDither = true;
		builder.cacheInMemory();
		builder.cacheOnDisc();
		builder.showStubImage(R.drawable.ic_mp_albumart_unknown);
		builder.bitmapConfig(Bitmap.Config.RGB_565);
		builder.decodingOptions(opts);
		//builder.displayer(new FadeInBitmapDisplayer(400));
		mImageDisplayOptions = builder.build();
	}

	public void clearFileCache() {
		mImageLoader.clearDiscCache();
	}

	public void clearMemoryCache() {
		mImageLoader.clearMemoryCache();
	}

	public void displayImage(final ImageView view, final String url) {
		final String url_fixed = !TextUtils.isEmpty(url) && url.startsWith("/") ? "file://" + url : url;
		mImageLoader.displayImage(url_fixed, view, mImageDisplayOptions);
	}
	
}
