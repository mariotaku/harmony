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

import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.util.PreferencesEditor;
import org.mariotaku.harmony.widget.TouchInterceptor;
import org.mariotaku.harmony.widget.TouchInterceptor.OnDropListener;
import org.mariotaku.harmony.widget.TouchInterceptor.OnRemoveListener;

import android.app.ListFragment;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.Audio.Playlists;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import org.mariotaku.harmony.fragment.BaseListFragment;

public class TrackFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
		Constants, OnDropListener, OnRemoveListener {

	private TracksAdapter mAdapter;

	private Cursor mCursor;
	private int mSelectedPosition;
	private long mSelectedId;
	private String mCurrentTrackName, mCurrentAlbumName, mCurrentArtistNameForAlbum;
	private int mIdIdx, mTrackIdx, mAlbumIdx, mArtistIdx, mDurationIdx;
	private boolean mEditMode = false;
	private ListView mListView;
	long mPlaylistId = -1;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			String mimetype = getArguments().getString(INTENT_KEY_TYPE);
			if (Audio.Playlists.CONTENT_TYPE.equals(mimetype)) {
				mPlaylistId = getArguments().getLong(Audio.Playlists._ID);
				switch ((int) mPlaylistId) {
					case (int) PLAYLIST_QUEUE:
						mEditMode = true;
						break;
					case (int) PLAYLIST_FAVORITES:
						mEditMode = true;
						break;
					default:
						if (mPlaylistId > 0) {
							mEditMode = true;
						}
						break;
				}

			}
		}

		mAdapter = new TracksAdapter(getActivity(), mEditMode ? R.layout.track_list_item_edit_mode
				: R.layout.track_list_item, null, new String[] {}, new int[] {}, 0);

		setListAdapter(mAdapter);

		mListView = getListView();

		mListView.setOnCreateContextMenuListener(this);

		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (mCursor == null) return false;

		Intent intent;

		switch (item.getItemId()) {
			case PLAY_SELECTION:
				int position = mSelectedPosition;

				if (mPlaylistId == PLAYLIST_QUEUE) {
					MusicUtils.setQueuePosition(position);
					return true;
				}
				MusicUtils.playAll(getActivity(), mCursor, position);
				return true;
			case DELETE_ITEMS:
				intent = new Intent(INTENT_DELETE_ITEMS);
				Bundle bundle = new Bundle();
				bundle.putString(
						INTENT_KEY_PATH,
						Uri.withAppendedPath(Audio.Media.EXTERNAL_CONTENT_URI,
								Uri.encode(String.valueOf(mSelectedId))).toString());
				intent.putExtras(bundle);
				startActivity(intent);
				return true;
			case SEARCH:
				doSearch();
				return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {

		if (mCursor == null) return;

		getActivity().getMenuInflater().inflate(R.menu.music_browser_item, menu);

		AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) info;
		mSelectedPosition = adapterinfo.position;
		mCursor.moveToPosition(mSelectedPosition);
		try {
			mSelectedId = mCursor.getLong(mIdIdx);
		} catch (IllegalArgumentException ex) {
			mSelectedId = adapterinfo.id;
		}

		mCurrentAlbumName = mCursor.getString(mAlbumIdx);
		mCurrentArtistNameForAlbum = mCursor.getString(mArtistIdx);
		mCurrentTrackName = mCursor.getString(mTrackIdx);
		menu.setHeaderTitle(mCurrentTrackName);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String[] cols = new String[] { Audio.Media._ID, Audio.Media.TITLE, Audio.Media.DATA,
				Audio.Media.ALBUM, Audio.Media.ARTIST, Audio.Media.ARTIST_ID, Audio.Media.DURATION };

		StringBuilder where = new StringBuilder();
		String sort_order = Audio.Media.TITLE;

		where.append(Audio.Media.IS_MUSIC + "=1");
		where.append(" AND " + Audio.Media.TITLE + " != ''");

		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;

		if (getArguments() != null) {

			String mimetype = getArguments().getString(INTENT_KEY_TYPE);

			if (Audio.Playlists.CONTENT_TYPE.equals(mimetype)) {

				where = new StringBuilder();
				where.append(Playlists.Members.IS_MUSIC + "=1");
				where.append(" AND " + Playlists.Members.TITLE + " != ''");

				switch ((int) mPlaylistId) {
					case (int) PLAYLIST_QUEUE:
						uri = Audio.Media.EXTERNAL_CONTENT_URI;
						long[] mNowPlaying = MusicUtils.getQueue();
						if (mNowPlaying.length == 0) return null;
						where = new StringBuilder();
						where.append(MediaStore.Audio.Media._ID + " IN (");
						if (mNowPlaying == null || mNowPlaying.length <= 0) return null;
						for (long queue_id : mNowPlaying) {
							where.append(queue_id + ",");
						}
						where.deleteCharAt(where.length() - 1);
						where.append(")");
						sort_order = null;
						break;
					case (int) PLAYLIST_FAVORITES:
						long favorites_id = MusicUtils.getFavoritesId(getActivity());

						cols = new String[] { Playlists.Members._ID, Playlists.Members.AUDIO_ID,
								Playlists.Members.TITLE, Playlists.Members.ALBUM,
								Playlists.Members.ARTIST, Playlists.Members.DURATION };
						uri = Playlists.Members.getContentUri(EXTERNAL_VOLUME, favorites_id);
						sort_order = Playlists.Members.DEFAULT_SORT_ORDER;
						break;
					case (int) PLAYLIST_RECENTLY_ADDED:
						int X = new PreferencesEditor(getActivity()).getIntPref(PREF_KEY_NUMWEEKS,
								2) * 3600 * 24 * 7;
						where = new StringBuilder();
						where.append(Audio.Media.TITLE + " != ''");
						where.append(" AND " + Audio.Media.IS_MUSIC + "=1");
						where.append(" AND " + MediaStore.MediaColumns.DATE_ADDED + ">"
								+ (System.currentTimeMillis() / 1000 - X));
						sort_order = Audio.Media.DATE_ADDED;
						break;
					case (int) PLAYLIST_PODCASTS:
						where = new StringBuilder();
						where.append(Audio.Media.TITLE + " != ''");
						where.append(" AND " + Audio.Media.IS_PODCAST + "=1");
						sort_order = Audio.Media.DATE_ADDED;
						break;
					default:
						if (id < 0) return null;
						cols = new String[] { Playlists.Members._ID, Playlists.Members.AUDIO_ID,
								Playlists.Members.TITLE, Playlists.Members.ALBUM,
								Playlists.Members.ARTIST, Playlists.Members.DURATION };

						uri = Playlists.Members.getContentUri(EXTERNAL_VOLUME, mPlaylistId);
						sort_order = Playlists.Members.DEFAULT_SORT_ORDER;
						break;
				}

			} else if (Audio.Genres.CONTENT_TYPE.equals(mimetype)) {
				long genre_id = getArguments().getLong(Audio.Genres._ID);
				uri = Genres.Members.getContentUri(EXTERNAL_VOLUME, genre_id);
				cols = new String[] { Genres.Members._ID, Genres.Members.TITLE,
						Genres.Members.ALBUM, Genres.Members.ARTIST, Genres.Members.DURATION };
				where = new StringBuilder();
				where.append(Genres.Members.IS_MUSIC + "=1");
				where.append(" AND " + Genres.Members.TITLE + " != ''");
				sort_order = Genres.Members.DEFAULT_SORT_ORDER;

			} else {

				if (Audio.Albums.CONTENT_TYPE.equals(mimetype)) {
					sort_order = Audio.Media.TRACK;
					long album_id = getArguments().getLong(Audio.Albums._ID);
					where.append(" AND " + Audio.Media.ALBUM_ID + "=" + album_id);
				} else if (Audio.Artists.CONTENT_TYPE.equals(mimetype)) {
					sort_order = Audio.Media.TITLE;
					long artist_id = getArguments().getLong(Audio.Artists._ID);
					where.append(" AND " + Audio.Media.ARTIST_ID + "=" + artist_id);
				}

			}

		}

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), uri, cols, where.toString(), null, sort_order);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tracks_browser, container, false);
		return view;
	}

	@Override
	public void onDrop(int from, int to) {

		if (mPlaylistId >= 0) {
			Playlists.Members.moveItem(getActivity().getContentResolver(), mPlaylistId, from, to);
		} else if (mPlaylistId == PLAYLIST_QUEUE) {
			MusicUtils.moveQueueItem(from, to);
			reloadQueueCursor();
		} else if (mPlaylistId == PLAYLIST_FAVORITES) {
			long favorites_id = MusicUtils.getFavoritesId(getActivity());
			Playlists.Members.moveItem(getActivity().getContentResolver(), favorites_id, from, to);
		}

		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mCursor == null || mCursor.getCount() == 0) return;
		// When selecting a track from the queue, just jump there instead of
		// reloading the queue. This is both faster, and prevents accidentally
		// dropping out of party shuffle.

		if (mPlaylistId == PLAYLIST_QUEUE) {
			MusicUtils.setQueueId(id);
			return;
		}
		MusicUtils.playAll(getActivity(), mCursor, position);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursor = null;
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		if (data == null) {
			getActivity().finish();
			return;
		}

		mCursor = data;

		if (getArguments() != null
				&& Playlists.CONTENT_TYPE.equals(getArguments().getString(INTENT_KEY_TYPE))
				&& (getArguments().getLong(Playlists._ID) >= 0 || getArguments().getLong(
						Playlists._ID) == PLAYLIST_FAVORITES)) {

			mIdIdx = data.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID);
			mTrackIdx = data.getColumnIndexOrThrow(Playlists.Members.TITLE);
			mAlbumIdx = data.getColumnIndexOrThrow(Playlists.Members.ALBUM);
			mArtistIdx = data.getColumnIndexOrThrow(Playlists.Members.ARTIST);
			mDurationIdx = data.getColumnIndexOrThrow(Playlists.Members.DURATION);
		} else if (getArguments() != null
				&& Genres.CONTENT_TYPE.equals(getArguments().getString(INTENT_KEY_TYPE))) {
			mIdIdx = data.getColumnIndexOrThrow(Genres.Members._ID);
			mTrackIdx = data.getColumnIndexOrThrow(Genres.Members.TITLE);
			mAlbumIdx = data.getColumnIndexOrThrow(Genres.Members.ALBUM);
			mArtistIdx = data.getColumnIndexOrThrow(Genres.Members.ARTIST);
			mDurationIdx = data.getColumnIndexOrThrow(Genres.Members.DURATION);
		} else {
			mIdIdx = data.getColumnIndexOrThrow(Audio.Media._ID);
			mTrackIdx = data.getColumnIndexOrThrow(Audio.Media.TITLE);
			mAlbumIdx = data.getColumnIndexOrThrow(Audio.Media.ALBUM);
			mArtistIdx = data.getColumnIndexOrThrow(Audio.Media.ARTIST);
			mDurationIdx = data.getColumnIndexOrThrow(Audio.Media.DURATION);
		}

		mAdapter.changeCursor(data);

		if (mEditMode) {
			((TouchInterceptor) mListView).setDropListener(this);
			((TouchInterceptor) mListView).setRemoveListener(this);
			mListView.setDivider(null);
			mListView.setSelector(R.drawable.list_selector_background);
		}

	}

	@Override
	public void onRemove(int which) {

		removePlaylistItem(which);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(getArguments() != null ? getArguments() : new Bundle());
		super.onSaveInstanceState(outState);
	}

	private void doSearch() {

		CharSequence title = null;
		String query = null;

		Intent i = new Intent();
		i.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		title = mCurrentTrackName;
		if (MediaStore.UNKNOWN_STRING.equals(mCurrentArtistNameForAlbum)) {
			query = mCurrentTrackName;
		} else {
			query = mCurrentArtistNameForAlbum + " " + mCurrentTrackName;
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

	private void reloadQueueCursor() {
		if (mPlaylistId == PLAYLIST_QUEUE) {

			String[] cols = new String[] { Audio.Media._ID, Audio.Media.TITLE, Audio.Media.DATA,
					Audio.Media.ALBUM, Audio.Media.ARTIST, Audio.Media.ARTIST_ID,
					Audio.Media.DURATION };
			StringBuilder where = new StringBuilder();
			where.append(Playlists.Members.IS_MUSIC + "=1");
			where.append(" AND " + Playlists.Members.TITLE + " != ''");

			Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;

			long[] mNowPlaying = MusicUtils.getQueue();
			if (mNowPlaying.length == 0) {
				// return;
			}
			where = new StringBuilder();
			where.append(MediaStore.Audio.Media._ID + " IN (");
			for (int i = 0; i < mNowPlaying.length; i++) {
				where.append(mNowPlaying[i]);
				if (i < mNowPlaying.length - 1) {
					where.append(",");
				}
			}
			where.append(")");

			mCursor = MusicUtils.query(getActivity(), uri, cols, where.toString(), null, null);
			mAdapter.changeCursor(mCursor);
		}
	}

	private void removePlaylistItem(int which) {

		mCursor.moveToPosition(which);
		long id = mCursor.getLong(mIdIdx);

		if (mPlaylistId >= 0) {
			Uri uri = Playlists.Members.getContentUri("external", mPlaylistId);
			getActivity().getContentResolver().delete(uri, Playlists.Members.AUDIO_ID + "=" + id,
					null);
		} else if (mPlaylistId == PLAYLIST_QUEUE) {
			MusicUtils.removeTrack(id);
			reloadQueueCursor();
		} else if (mPlaylistId == PLAYLIST_FAVORITES) {
			MusicUtils.removeFromFavorites(getActivity(), id);
		}

		mListView.invalidateViews();

	}

	private class TracksAdapter extends SimpleCursorAdapter {

		private TracksAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to,
				int flags) {
			super(context, layout, cursor, from, to, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder viewholder = (ViewHolder) view.getTag();

			String track_name = cursor.getString(mTrackIdx);
			viewholder.track_name.setText(track_name);

			String artist_name = cursor.getString(mArtistIdx);
			if (artist_name == null || MediaStore.UNKNOWN_STRING.equals(artist_name)) {
				viewholder.artist_name.setText(R.string.unknown_artist);
			} else {
				viewholder.artist_name.setText(artist_name);
			}

			long secs = cursor.getLong(mDurationIdx) / 1000;

			if (secs <= 0) {
				viewholder.track_duration.setText("");
			} else {
				viewholder.track_duration.setText(MusicUtils.makeTimeString(context, secs));
			}

			long audio_id = cursor.getLong(mIdIdx);

			long currentaudioid = -1;
			if (currentaudioid == audio_id) {
				viewholder.track_name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						R.drawable.ic_indicator_nowplaying_small, 0);
			} else {
				viewholder.track_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = super.newView(context, cursor, parent);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}

		private class ViewHolder {

			TextView track_name;
			TextView artist_name;
			TextView track_duration;

			public ViewHolder(View view) {
				track_name = (TextView) view.findViewById(R.id.name);
				artist_name = (TextView) view.findViewById(R.id.summary);
				track_duration = (TextView) view.findViewById(R.id.duration);
			}

		}

	}

}
