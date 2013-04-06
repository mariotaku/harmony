package org.mariotaku.harmony.fragment;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import org.mariotaku.harmony.adapter.TracksAdapter;
import android.content.ContentResolver;
import com.mobeta.android.dslv.DragSortListView;

public abstract class AbsTracksFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String[] AUDIO_COLUMNS = new String[] { Audio.AudioColumns._ID, Audio.AudioColumns.TITLE, Audio.AudioColumns.DATA,
		Audio.AudioColumns.ALBUM, Audio.AudioColumns.ARTIST, Audio.AudioColumns.ARTIST_ID, Audio.AudioColumns.DURATION };

	private ContentResolver mResolver;
	protected TracksAdapter mAdapter;
	private boolean mLoaderInitialized;

	@Override
	public final TracksAdapter getListAdapter() {
		return (TracksAdapter) super.getListAdapter();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mResolver = getActivity().getContentResolver();
		mAdapter = new TracksAdapter(getActivity(), this instanceof DragSortListView.OnDragListener);
		setListAdapter(mAdapter);
		loadData();
	}

	@Override
	public final Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_COLUMNS, getWhereClause(args), getWhereArgs(args), getSortOrder(args));
	}

	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.changeCursor(cursor);
	}

	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}
	
	protected abstract String getWhereClause(final Bundle args);
	
	protected abstract String[] getWhereArgs(final Bundle args);
	
	protected String getSortOrder(final Bundle args) {
		return null;
	}
	
	protected void loadData() {
		final LoaderManager lm = getLoaderManager();
		final Bundle args = getArguments();
		if (mLoaderInitialized) {
			lm.restartLoader(0, args, this);
		} else {
			lm.initLoader(0, args, this);
			mLoaderInitialized = true;
		}
	}
}
