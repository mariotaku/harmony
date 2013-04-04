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

import java.io.File;

import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.ImageLoaderWrapper;
import org.mariotaku.harmony.util.MusicUtils;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import org.mariotaku.harmony.view.holder.AlbumViewHolder;
import android.text.TextUtils;
import org.mariotaku.harmony.adapter.AlbumsAdapter;
import android.content.ContentResolver;
import android.widget.Adapter;
import android.widget.Toast;
import org.mariotaku.harmony.view.ArtistAlbumsGridView;
import android.content.res.Resources;
import org.mariotaku.harmony.app.TrackBrowserActivity;

public class ArtistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Constants, ExpandableListView.OnChildClickListener {

	

	private ArtistsAdapter mArtistsAdapter;
	private ExpandableListView mListView;


	private BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mArtistsAdapter.notifyDataSetChanged();
		}

	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView = (ExpandableListView) getView().findViewById(R.id.artist_expandable_list);
		mArtistsAdapter = new ArtistsAdapter(getActivity(), mListView, this);
		mListView.setAdapter(mArtistsAdapter);
		mListView.setOnCreateContextMenuListener(this);
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
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String[] cols = new String[] { Audio.Artists._ID, Audio.Artists.ARTIST,
				Audio.Artists.NUMBER_OF_ALBUMS, Audio.Artists.NUMBER_OF_TRACKS };
		final Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		return new CursorLoader(getActivity(), uri, cols, null, null,
				Audio.Artists.DEFAULT_SORT_ORDER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.artist_album_browser, container, false);
		return view;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mArtistsAdapter.setGroupCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mArtistsAdapter.changeCursor(data);
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

	private static class ArtistsAdapter extends SimpleCursorTreeAdapter {

		private static final String EXTRAS_KEY_GROUP_POSITION = "group_position";
		
		private final ExpandableListView mListView;
		private final ExpandableListView.OnChildClickListener mChildListener;
		private final ContentResolver mResolver;
		private final int mGridViewSpacing, mColumnWidth;
		
		private int mGroupArtistIdIdx, mGroupArtistIdx, mGroupAlbumIdx, mGroupSongIdx;
		private long mCurrentArtistId, mCurrentAlbumId;
		
		public ArtistsAdapter(Context context, ExpandableListView listView, ExpandableListView.OnChildClickListener childListener) {
			super(context, null, R.layout.artist_list_item_group, new String[0], new int[0],
					R.layout.artist_list_item_child, new String[0], new int[0]);
			mListView = listView;
			mChildListener = childListener;
			mResolver = context.getContentResolver();
			final Resources res = context.getResources();
			mGridViewSpacing = res.getDimensionPixelSize(R.dimen.default_element_spacing);
			mColumnWidth = res.getDimensionPixelSize(R.dimen.gridview_item_width);
		}

		@Override
		public void bindChildView(View view, Context context, final Cursor cursor, boolean isLastChild) {
			final GridView gridview = (GridView) view;
			final Bundle extras = cursor.getExtras();
			final int groupPos = extras.getInt(EXTRAS_KEY_GROUP_POSITION);
			final AlbumsAdapter adapter = new AlbumsAdapter(context, cursor);
			gridview.setAdapter(adapter);
			gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> view, View child, int position, long id) {
						if (mChildListener == null) return;
						mChildListener.onChildClick(mListView, view, groupPos, position, id);
					}
				});
		}

		@Override
		public void bindGroupView(View view, Context context, Cursor cursor, boolean isexpanded) {

			ViewHolderGroup viewholder = (ViewHolderGroup) view.getTag();

			String artist = cursor.getString(mGroupArtistIdx);
			boolean unknown = artist == null || MediaStore.UNKNOWN_STRING.equals(artist);
			if (unknown) {
				viewholder.artist_name.setText(R.string.unknown_artist);
			} else {
				viewholder.artist_name.setText(artist);
			}

			int numalbums = cursor.getInt(mGroupAlbumIdx);
			int numsongs = cursor.getInt(mGroupSongIdx);

			String songs_albums = MusicUtils.makeAlbumsLabel(context, numalbums, numsongs, unknown);

			viewholder.album_track_count.setText(songs_albums);

			if (mCurrentArtistId == cursor.getLong(mGroupArtistIdIdx)) {
				viewholder.artist_name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						R.drawable.ic_indicator_nowplaying_small, 0);
			} else {
				viewholder.artist_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public View newGroupView(Context context, Cursor cursor, boolean isExpanded,
				ViewGroup parent) {
			final View view = super.newGroupView(context, cursor, isExpanded, parent);
			view.setTag(new ViewHolderGroup(view));
			return view;
		}

		@Override
		protected Cursor getChildrenCursor(final Cursor groupCursor) {
			final long id = groupCursor.getLong(groupCursor.getColumnIndex(Audio.Artists._ID));
			final String[] cols = new String[] { Audio.Albums._ID, Audio.Albums.ALBUM,
					Audio.Albums.ARTIST, Audio.Albums.NUMBER_OF_SONGS,
					Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST, Audio.Albums.ALBUM_ART };
			final Uri uri = Audio.Artists.Albums.getContentUri(EXTERNAL_VOLUME, id);
			final Cursor c = mResolver.query(uri, cols, null, null, Audio.Albums.DEFAULT_SORT_ORDER);
			final Bundle extras = c.getExtras();
			extras.putInt(EXTRAS_KEY_GROUP_POSITION, groupCursor.getPosition());
			return c;
		}
		
		@Override
		public void changeCursor(final Cursor cursor) {
			super.changeCursor(cursor);
			if (cursor == null) return;
			mGroupArtistIdIdx = cursor.getColumnIndexOrThrow(Audio.Artists._ID);
			mGroupArtistIdx = cursor.getColumnIndexOrThrow(Audio.Artists.ARTIST);
			mGroupAlbumIdx = cursor.getColumnIndexOrThrow(Audio.Artists.NUMBER_OF_ALBUMS);
			mGroupSongIdx = cursor.getColumnIndexOrThrow(Audio.Artists.NUMBER_OF_TRACKS);
		}

		private class ViewHolderGroup {

			TextView artist_name;
			TextView album_track_count;

			public ViewHolderGroup(View view) {
				artist_name = (TextView) view.findViewById(R.id.name);
				album_track_count = (TextView) view.findViewById(R.id.summary);
			}
		}

	}
	
}
