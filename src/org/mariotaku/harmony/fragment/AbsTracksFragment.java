package org.mariotaku.harmony.fragment;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import com.mobeta.android.dslv.DragSortListView;
import org.mariotaku.harmony.adapter.TracksAdapter;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.ServiceWrapper;

public abstract class AbsTracksFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	protected static final String[] AUDIO_COLUMNS = new String[] { Audio.AudioColumns._ID, Audio.AudioColumns.TITLE, Audio.AudioColumns.DATA,
		Audio.AudioColumns.ALBUM, Audio.AudioColumns.ARTIST, Audio.AudioColumns.ARTIST_ID, Audio.AudioColumns.DURATION };

	private ContentResolver mResolver;
	private TracksAdapter mAdapter;
	private boolean mLoaderInitialized;
	private ServiceWrapper mService;

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
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_COLUMNS, getWhereClause(args), getWhereArgs(args), getSortOrder(args));
	}

	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.changeCursor(cursor);
	}

	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}
	
	protected String getWhereClause(final Bundle args) {
		return null;
	}
	
	protected String[] getWhereArgs(final Bundle args) {
		return null;
	}
	
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

	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		mService = service;
		updateNowPlaying();
	}

	@Override
	protected void onServiceDisconnected() {
		mService = null;
	}
	
	@Override
	protected void onCurrentMediaChanged() {
		updateNowPlaying();
	}

	private void updateNowPlaying() {
		final TrackInfo track = mService != null ? mService.getTrackInfo() : null;
		mAdapter.setCurrentTrackId(track != null ? track.id : -1);
	}
}
