package org.mariotaku.harmony.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import java.io.IOException;
import org.mariotaku.harmony.model.Lyrics;
import org.mariotaku.harmony.util.LyricsParser;

public class LyricsLoader extends AsyncTaskLoader<Lyrics> {

	private final String mLyricsPath;

	public LyricsLoader(final Context context, final String path) {
		super(context);
		mLyricsPath = path;
	}

	public Lyrics loadInBackground() {
		try {
			return LyricsParser.parse(mLyricsPath);
		} catch (IOException e) {}
		return null;
	}

	public void onStartLoading() {
		forceLoad();
	}	

}
