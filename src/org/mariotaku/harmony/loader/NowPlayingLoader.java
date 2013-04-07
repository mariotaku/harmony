package org.mariotaku.harmony.loader;
import android.content.CursorLoader;
import android.content.Context;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.model.NowPlayingCursor;
import android.database.Cursor;

public class NowPlayingLoader extends CursorLoader {

	private ServiceWrapper mService;

	private String[] mColumns;

	public NowPlayingLoader(final Context context, final ServiceWrapper service, final String[] cols) {
		super(context);
		mService = service;
		mColumns = cols;
	}
	
	public Cursor loadInBackground() {
		if (mService == null) return null;
		return new NowPlayingCursor(getContext(), mService, mColumns);
	}

}
