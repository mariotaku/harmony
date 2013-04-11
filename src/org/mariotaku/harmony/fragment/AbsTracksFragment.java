package org.mariotaku.harmony.fragment;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.mobeta.android.dslv.DragSortListView;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.adapter.TracksAdapter;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.util.MusicUtils;

public abstract class AbsTracksFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	protected static final String[] AUDIO_COLUMNS = new String[] { Audio.Media._ID, Audio.Media.TITLE, Audio.Media.DATA,
		Audio.Media.ALBUM, Audio.Media.ARTIST, Audio.Media.ARTIST_ID, Audio.Media.DURATION };

	private ContentResolver mResolver;
	private TracksAdapter mAdapter;
	private boolean mLoaderInitialized;
	private ServiceWrapper mService;

	private Cursor mCursor;

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
		final ListView listView = getListView();
		listView.setFastScrollEnabled(true);
		loadData();
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_COLUMNS, getWhereClause(args), getWhereArgs(args), getSortOrder(args));
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tracks_browser_editable, container, false);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mService == null || mCursor == null || mCursor.isClosed()) return;
		MusicUtils.playAll(getActivity(), mService, mCursor, position);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.changeCursor(mCursor = cursor);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.changeCursor(mCursor = null);
	}
	
	@Override
	protected String getWhereClause(final Bundle args) {
		return null;
	}
	
	@Override
	protected String[] getWhereArgs(final Bundle args) {
		return null;
	}

	@Override
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
