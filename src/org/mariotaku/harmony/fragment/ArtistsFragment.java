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

import android.app.LoaderManager;
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
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.activity.AlbumsBrowserActivity;
import org.mariotaku.harmony.adapter.ArtistsAdapter;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.ServiceWrapper;

public class ArtistsFragment extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
LoaderManager.LoaderCallbacks<Cursor> {

	private ArtistsAdapter mAdapter;
	private GridView mGridView;
	private ServiceWrapper mService;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ArtistsAdapter(getActivity());
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = new String[] { MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST,
				MediaStore.Audio.Artists.NUMBER_OF_ALBUMS };
		Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		return new CursorLoader(getActivity(), uri, cols, null, null, MediaStore.Audio.Artists.ARTIST);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.base_grid_view, container, false);
		mGridView = (GridView) view.findViewById(android.R.id.list);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> view, View child, int position, long id) {
		final Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME_HARMONY_ALBUMS);
		builder.authority(AUTHORITY_ARTISTS);
		builder.appendPath(String.valueOf(id));
		final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
		intent.setClass(getActivity(), AlbumsBrowserActivity.class);
		startActivity(intent);
	}	

	@Override
	public boolean onItemLongClick(AdapterView<?> view, View child, int position, long id) {
		//final AlbumInfo album = mAdapter.getAlbumInfo(position);
		//Toast.makeText(getActivity(), "album " + album + " selected", Toast.LENGTH_SHORT).show();
		return true;
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
		mAdapter.setCurrentArtistId(track != null ? track.artist_id : -1);
	}

}
