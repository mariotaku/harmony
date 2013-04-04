package org.mariotaku.harmony.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class HarmonyImageDownloader extends ImageDownloader {

	private final Context context;
	private final ContentResolver resolver;

	public HarmonyImageDownloader(final Context context) {
		this.context = context;
		this.resolver = context.getContentResolver();
	}

	@Override
	protected InputStream getStreamFromNetwork(final URI uri) throws IOException {
		final InputStream is;
		try {
			if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())
					|| ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())
							|| ContentResolver.SCHEME_FILE.equals(uri.getScheme()))
				return resolver.openInputStream(Uri.parse(uri.toString()));
			is = uri.toURL().openStream();
		} catch (final Exception e) {
			throw new IOException(e);
		}
		return is;
	}

}
