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
import android.provider.MediaStore.Audio;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.adapter.ArtistsAdapter;
import org.mariotaku.harmony.app.TrackBrowserActivity;
import org.mariotaku.harmony.model.AlbumInfo;
import org.mariotaku.harmony.model.ArtistInfo;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.ServiceWrapper;

public class ArtistsFragment extends BaseFragment implements Constants, LoaderManager.LoaderCallbacks<Cursor>, ExpandableListView.OnChildClickListener,
		ArtistsAdapter.OnChildLongClickListener, AdapterView.OnItemLongClickListener {

	private ArtistsAdapter mAdapter;
	private ExpandableListView mListView;
	private ServiceWrapper mService;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ArtistsAdapter(getActivity(), mListView, this, this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public boolean onChildClick(ExpandableListView listView, View child, int groupPos, int childPos, long id) {
		final Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_TYPE, MediaStore.Audio.Albums.CONTENT_TYPE);
		bundle.putLong(MediaStore.Audio.Albums._ID, id);
		final Intent intent = new Intent(getActivity(), TrackBrowserActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
		return true;
	}

	@Override
	public boolean onChildLongClick(final ExpandableListView listView, final View view, final int groupPos, final int childPos, final long id) {
		// TODO: Implement this method
		final AlbumInfo album = mAdapter.getAlbumInfo(groupPos, childPos);
		Toast.makeText(getActivity(), "album " + album + " selected", Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String[] cols = new String[] { Audio.Artists._ID, Audio.Artists.ARTIST,
				Audio.Artists.NUMBER_OF_ALBUMS, Audio.Artists.NUMBER_OF_TRACKS };
		final Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		return new CursorLoader(getActivity(), uri, cols, null, null,
				Audio.Artists.DEFAULT_SORT_ORDER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.artist_album_browser, container, false);
		mListView = (ExpandableListView) view.findViewById(R.id.artist_expandable_list);
		return view;
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> view, final View child, final int position, final long id) {
		final long packed_pos = mListView.getExpandableListPosition(position);
		final int type = ExpandableListView.getPackedPositionType(packed_pos);
		if (type != ExpandableListView.PACKED_POSITION_TYPE_GROUP) return false;
		final int group_pos = ExpandableListView.getPackedPositionGroup(packed_pos);
		final ArtistInfo artist = mAdapter.getArtistInfo(group_pos);
		Toast.makeText(getActivity(), "artist " + artist + " selected", Toast.LENGTH_SHORT).show();
		return true;
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.setGroupCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
	}

	@Override
	protected void onCurrentMediaChanged() {
		updateNowPlaying();
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

	private void updateNowPlaying() {
		final TrackInfo track = mService != null ? mService.getTrackInfo() : null;
		mAdapter.setCurrentArtistId(track != null ? track.artist_id : -1);
		mAdapter.setCurrentAlbumId(track != null ? track.album_id : -1);
	}
	
}