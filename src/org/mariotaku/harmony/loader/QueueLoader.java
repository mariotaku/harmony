package org.mariotaku.harmony.loader;
import android.content.CursorLoader;
import android.content.Context;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.model.QueueCursor;
import android.database.Cursor;

public class QueueLoader extends CursorLoader {

	private ServiceWrapper mService;

	private String[] mColumns;

	public QueueLoader(final Context context, final ServiceWrapper service, final String[] cols) {
		super(context);
		mService = service;
		mColumns = cols;
	}
	
	public Cursor loadInBackground() {
		if (mService == null) return null;
		return new QueueCursor(getContext(), mService, mColumns);
	}

}
