package org.mariotaku.harmony.app;

import android.app.Application;
import android.content.Context;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.HttpClientImageDownloader;
import java.io.File;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.util.ImageLoaderWrapper;
import org.mariotaku.harmony.util.ImageMemoryCache;
import org.mariotaku.harmony.util.URLFileNameGenerator;

public class HarmonyApplication extends Application implements Constants {

	private ImageLoaderWrapper mImageLoaderWrapper;
	private ImageLoader mImageLoader;
	
	public ImageLoader getImageLoader() {
		if (mImageLoader != null) return mImageLoader;
		final File cache_dir = new File(getCacheDir(), CACHE_DIR_NAME_ALBUMART);
		if (!cache_dir.exists()) {
			cache_dir.mkdirs();
		}
		final ImageLoader loader = ImageLoader.getInstance();
		final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(this);
		cb.threadPoolSize(8);
		cb.memoryCache(new ImageMemoryCache(40));
		cb.discCache(new UnlimitedDiscCache(cache_dir, new URLFileNameGenerator()));
		cb.imageDownloader(new BaseImageDownloader(this));
		loader.init(cb.build());
		return mImageLoader = loader;
	}

	public ImageLoaderWrapper getImageLoaderWrapper() {
		if (mImageLoaderWrapper != null) return mImageLoaderWrapper;
		return mImageLoaderWrapper = new ImageLoaderWrapper(getImageLoader());
	}
	
	public static HarmonyApplication getInstance(final Context context) {
		final Context app = context != null ? context.getApplicationContext() : null;
		return app instanceof HarmonyApplication ? (HarmonyApplication) app : null;
	}
}
