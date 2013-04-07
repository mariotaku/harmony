package org.mariotaku.harmony.fragment;

import org.mariotaku.harmony.loader.NowPlayingLoader;
import org.mariotaku.harmony.model.NowPlayingCursor;
import org.mariotaku.harmony.util.ServiceWrapper;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.util.Log;

public class QueueFragment extends EditableTracksFragment {

	private ServiceWrapper mService;
	private NowPlayingCursor mCursor;

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new NowPlayingLoader(getActivity(), mService, AUDIO_COLUMNS);
	}
	
	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		super.onLoadFinished(loader, cursor);
		mCursor = cursor instanceof NowPlayingCursor ? (NowPlayingCursor) cursor : null;
	}
	

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mCursor = null;
		super.onLoaderReset(loader);
	}

	@Override
	public void onDrag(int from, int to) {
	}

	@Override
	public void onDrop(int from, int to) {
		if (mService == null || mCursor == null || mCursor.isClosed()) return;
		mCursor.moveItem(from, to);
		getListAdapter().notifyDataSetChanged();
	}

	@Override
	public void onRemove(int which) {
		if (mService == null || mCursor == null || mCursor.isClosed()) return;
		mCursor.removeItem(which);
		getListAdapter().notifyDataSetChanged();
	}
	
	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		super.onServiceConnected(service);
		mService = service;
		loadData();
	}

	@Override
	protected void onServiceDisconnected() {
		mService = null;
		super.onServiceDisconnected();
	}
	
	@Override
	protected void loadData() {
		if (mService == null) return;
		super.loadData();
	}
	

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mService == null) return;
		mService.setQueuePosition(position);
	}
}
