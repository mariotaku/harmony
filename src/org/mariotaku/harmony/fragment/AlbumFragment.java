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
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.io.File;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.ImageLoaderWrapper;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.view.holder.AlbumViewHolder;
import org.mariotaku.harmony.adapter.AlbumsAdapter;
import org.mariotaku.harmony.app.TrackBrowserActivity;

public class AlbumFragment extends Fragment implements Constants, OnItemClickListener,
		LoaderCallbacks<Cursor> {

	private AlbumsAdapter mAdapter;

	private GridView mGridView;
	private int mSelectedPosition;
	private long mSelectedId;
	private String mCurrentAlbumName, mCurrentArtistNameForAlbum;

	private BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mGridView.invalidateViews();
		}

	};

	public AlbumFragment() {

	}

	public AlbumFragment(Bundle args) {
		setArguments(args);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new AlbumsAdapter(getActivity());
		View fragmentView = getView();
		mGridView = (GridView) fragmentView.findViewById(android.R.id.list);
		mGridView.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
//
//		if (mCursor == null) return false;
//
//		Intent intent;
//
//		switch (item.getItemId()) {
//			case PLAY_SELECTION:
//				int position = mSelectedPosition;
//				long[] list = MusicUtils.getSongListForAlbum(getActivity(), mSelectedId);
//				MusicUtils.playAll(getActivity(), list, position);
//				return true;
//			case DELETE_ITEMS:
//				intent = new Intent(INTENT_DELETE_ITEMS);
//				Bundle bundle = new Bundle();
//				bundle.putString(
//						INTENT_KEY_PATH,
//						Uri.withAppendedPath(Audio.Albums.EXTERNAL_CONTENT_URI,
//								Uri.encode(String.valueOf(mSelectedId))).toString());
//				intent.putExtras(bundle);
//				startActivity(intent);
//				return true;
//			case SEARCH:
//				doSearch();
//				return true;
//		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {

//		if (mCursor == null) return;
//
//		getActivity().getMenuInflater().inflate(R.menu.music_browser_item, menu);
//
//		AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) info;
//		mSelectedPosition = adapterinfo.position;
//		mCursor.moveToPosition(mSelectedPosition);
//		try {
//			mSelectedId = mCursor.getLong(mIdIdx);
//		} catch (IllegalArgumentException ex) {
//			mSelectedId = adapterinfo.id;
//		}
//
//		mCurrentArtistNameForAlbum = mCursor.getString(mArtistIdx);
//
//		mCurrentAlbumName = mCursor.getString(mAlbumIdx);
//
//		menu.setHeaderTitle(mCurrentAlbumName);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String[] cols = new String[] { Audio.Albums._ID, Audio.Albums.ALBUM, Audio.Albums.ARTIST,
				Audio.Albums.ALBUM_ART };
		Uri uri = Audio.Albums.EXTERNAL_CONTENT_URI;
		return new CursorLoader(getActivity(), uri, cols, null, null,
				Audio.Albums.DEFAULT_SORT_ORDER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.albums_browser, container, false);
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
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(getArguments() != null ? getArguments() : new Bundle());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();

		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_MEDIA_CHANGED);
		filter.addAction(BROADCAST_QUEUE_CHANGED);
		getActivity().registerReceiver(mMediaStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mMediaStatusReceiver);
		super.onStop();
	}

	private void doSearch() {

		CharSequence title = null;
		String query = null;

		Intent i = new Intent();
		i.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		title = mCurrentAlbumName;
		if (MediaStore.UNKNOWN_STRING.equals(mCurrentArtistNameForAlbum)) {
			query = mCurrentAlbumName;
		} else {
			query = mCurrentArtistNameForAlbum + " " + mCurrentAlbumName;
			i.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, mCurrentArtistNameForAlbum);
		}
		if (MediaStore.UNKNOWN_STRING.equals(mCurrentAlbumName)) {
			i.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, mCurrentAlbumName);
		}
		i.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "audio/*");
		title = getString(R.string.mediasearch, title);
		i.putExtra(SearchManager.QUERY, query);

		startActivity(Intent.createChooser(i, title));
	}

	private void showDetails(int index, long id) {

		Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_TYPE, MediaStore.Audio.Albums.CONTENT_TYPE);
		bundle.putLong(Audio.Albums._ID, id);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(getActivity(), TrackBrowserActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
	}

}
