/*
 *  YAMMP - Yet Another Multi Media Player for android
 *  Copyright (C) 2011-2012  Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This file is part of YAMMP.
 *
 *  YAMMP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAMMP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAMMP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.harmony.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.adapter.AlbumsAdapter;
import org.mariotaku.harmony.app.TrackBrowserActivity;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.model.TrackInfo;

public class AlbumsFragment extends BaseFragment implements Constants, AdapterView.OnItemClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	private AlbumsAdapter mAdapter;
	private GridView mGridView;
	private ServiceWrapper mService;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new AlbumsAdapter(getActivity());
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM_ART };
		Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		return new CursorLoader(getActivity(), uri, cols, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.albums_browser, container, false);
		mGridView = (GridView) view.findViewById(android.R.id.list);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		showDetails(position, id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
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
		mAdapter.setCurrentAlbumId(track != null ? track.album_id : -1);
	}

	private void showDetails(int index, long id) {

		Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_TYPE, MediaStore.Audio.Albums.CONTENT_TYPE);
		bundle.putLong(MediaStore.Audio.Albums._ID, id);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClass(getActivity(), TrackBrowserActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
