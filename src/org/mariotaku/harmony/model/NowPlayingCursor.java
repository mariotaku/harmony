package org.mariotaku.harmony.model;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import java.util.Arrays;
import org.mariotaku.harmony.app.TrackBrowserActivity;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.util.ServiceWrapper;
import android.content.ContentResolver;

public class NowPlayingCursor extends AbstractCursor {

	private String [] mCols;
	private Cursor mCursor;     // updated in onMove
	private int mSize;          // size of the queue
	private long[] mNowPlaying;
	private long[] mCursorIdxs;
	private int mCurPos;
	private ServiceWrapper mService;
	private ContentResolver mResolver;

	public NowPlayingCursor(final Context context, ServiceWrapper service, String [] cols) {
		mCols = cols;
		mService  = service;
		mResolver = context.getContentResolver();
		makeNowPlayingCursor();
	}

	private void makeNowPlayingCursor() {
		mCursor = null;
		mNowPlaying = mService.getQueue();
		if (mNowPlaying == null) {
			mNowPlaying = new long[0];
		}
		mSize = mNowPlaying.length;
		if (mSize == 0) {
			return;
		}

		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media._ID + " IN (");
		for (int i = 0; i < mSize; i++) {
			where.append(mNowPlaying[i]);
			if (i < mSize - 1) {
				where.append(",");
			}
		}
		where.append(")");
		mCursor = mResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCols, where.toString(), null, MediaStore.Audio.Media._ID);

		if (mCursor == null) {
			mSize = 0;
			return;
		}

		int size = mCursor.getCount();
		mCursorIdxs = new long[size];
		mCursor.moveToFirst();
		int colidx = mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
		for (int i = 0; i < size; i++) {
			mCursorIdxs[i] = mCursor.getLong(colidx);
			mCursor.moveToNext();
		}
		mCursor.moveToFirst();
		mCurPos = -1;

		// At this point we can verify the 'now playing' list we got
		// earlier to make sure that all the items in there still exist
		// in the database, and remove those that aren't. This way we
		// don't get any blank items in the list.
		int removed = 0;
		for (int i = mNowPlaying.length - 1; i >= 0; i--) {
			long trackid = mNowPlaying[i];
			int crsridx = Arrays.binarySearch(mCursorIdxs, trackid);
			if (crsridx < 0) {
				//Log.i("@@@@@", "item no longer exists in db: " + trackid);
				removed += mService.removeTrack(trackid);
			}
		}
		if (removed > 0) {
			mNowPlaying = mService.getQueue();
			mSize = mNowPlaying.length;
			if (mSize == 0) {
				mCursorIdxs = null;
				return;
			}
		}
		if (mNowPlaying == null) {
			mNowPlaying = new long[0];
		}
	}

	@Override
	public int getCount() {
		return mSize;
	}

	@Override
	public boolean onMove(int oldPosition, int newPosition) {
		if (oldPosition == newPosition)
			return true;

		if (mNowPlaying == null || mCursorIdxs == null || newPosition >= mNowPlaying.length) {
			return false;
		}

		// The cursor doesn't have any duplicates in it, and is not ordered
		// in queue-order, so we need to figure out where in the cursor we
		// should be.

		long newid = mNowPlaying[newPosition];
		int crsridx = Arrays.binarySearch(mCursorIdxs, newid);
		mCursor.moveToPosition(crsridx);
		mCurPos = newPosition;

		return true;
	}

	public boolean removeItem(int which) {
		if (mService.removeTracks(which, which) == 0) {
			return false; // delete failed
		}
		int i = which;
		mSize--;
		while (i < mSize) {
			mNowPlaying[i] = mNowPlaying[i + 1];
			i++;
		}
		onMove(-1, mCurPos);
		return true;
	}

	public void moveItem(int from, int to) {
		mService.moveQueueItem(from, to);
		mNowPlaying = mService.getQueue();
		onMove(-1, mCurPos); // update the underlying cursor
	}

	@Override
	public String getString(int column) {
		try {
			return mCursor.getString(column);
		} catch (Exception ex) {
			onChange(true);
			return "";
		}
	}

	@Override
	public short getShort(int column) {
		return mCursor.getShort(column);
	}

	@Override
	public int getInt(int column) {
		try {
			return mCursor.getInt(column);
		} catch (Exception ex) {
			onChange(true);
			return 0;
		}
	}

	@Override
	public long getLong(int column) {
		try {
			return mCursor.getLong(column);
		} catch (Exception ex) {
			onChange(true);
			return 0;
		}
	}

	@Override
	public float getFloat(int column) {
		return mCursor.getFloat(column);
	}

	@Override
	public double getDouble(int column) {
		return mCursor.getDouble(column);
	}

	@Override
	public int getType(int column) {
		return mCursor.getType(column);
	}

	@Override
	public boolean isNull(int column) {
		return mCursor.isNull(column);
	}

	@Override
	public String[] getColumnNames() {
		return mCols;
	}
	
	@Override
	public void close() {
		if (mCursor != null) {
			mCursor.close();
		}
	}

	@Override
	public void deactivate() {
		if (mCursor != null) {
			mCursor.deactivate();
		}
	}

	@Override
	public boolean requery() {
		makeNowPlayingCursor();
		return true;
	}
}
