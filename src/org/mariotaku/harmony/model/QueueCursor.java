package org.mariotaku.harmony.model;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.MediaStore.*;
import android.util.Log;
import java.util.Arrays;
import org.mariotaku.harmony.activity.TracksBrowserActivity;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.util.ServiceWrapper;
import android.content.ContentResolver;

public class QueueCursor extends AbstractCursor {

	private String [] mCols;
	private Cursor mCursor;
	private int mSize;
	private int mCurPos;
	private long[] mQueue, mIndices;
	private ServiceWrapper mService;
	private ContentResolver mResolver;

	public QueueCursor(final Context context, ServiceWrapper service, String [] cols) {
		mCols = cols;
		mService  = service;
		mResolver = context.getContentResolver();
		makeNowPlayingCursor();
	}

	private void makeNowPlayingCursor() {
		mCursor = null;
		mQueue = mService.getQueue();
		if (mQueue == null) {
			mQueue = new long[0];
		}
		mSize = mQueue.length;
		if (mSize == 0) {
			return;
		}

		StringBuilder where = new StringBuilder();
		where.append(Audio.Media._ID + " IN (");
		for (int i = 0; i < mSize; i++) {
			where.append(mQueue[i]);
			if (i < mSize - 1) {
				where.append(",");
			}
		}
		where.append(")");
		mCursor = mResolver.query(Audio.Media.EXTERNAL_CONTENT_URI, mCols, where.toString(), null, Audio.Media._ID);

		if (mCursor == null) {
			mSize = 0;
			return;
		}

		final int size = mCursor.getCount();
		mIndices = new long[size];
		mCursor.moveToFirst();
		final int colidx = mCursor.getColumnIndexOrThrow(Audio.Media._ID);
		for (int i = 0; i < size; i++) {
			mIndices[i] = mCursor.getLong(colidx);
			mCursor.moveToNext();
		}
		mCursor.moveToFirst();
		mCurPos = -1;

		// At this point we can verify the 'now playing' list we got
		// earlier to make sure that all the items in there still exist
		// in the database, and remove those that aren't. This way we
		// don't get any blank items in the list.
		int removed = 0;
		for (int i = mQueue.length - 1; i >= 0; i--) {
			long trackid = mQueue[i];
			int crsridx = Arrays.binarySearch(mIndices, trackid);
			if (crsridx < 0) {
				//Log.i("@@@@@", "item no longer exists in db: " + trackid);
				removed += mService.removeTrack(trackid);
			}
		}
		if (removed > 0) {
			mQueue = mService.getQueue();
			mSize = mQueue.length;
			if (mSize == 0) {
				mIndices = null;
				return;
			}
		}
		if (mQueue == null) {
			mQueue = new long[0];
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

		if (mQueue == null || mIndices == null || newPosition >= mQueue.length) {
			return false;
		}

		// The cursor doesn't have any duplicates in it, and is not ordered
		// in queue-order, so we need to figure out where in the cursor we
		// should be.

		long newid = mQueue[newPosition];
		int crsridx = Arrays.binarySearch(mIndices, newid);
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
			mQueue[i] = mQueue[i + 1];
			i++;
		}
		onMove(-1, mCurPos);
		return true;
	}

	public void moveItem(int from, int to) {
		mService.moveQueueItem(from, to);
		mQueue = mService.getQueue();
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
