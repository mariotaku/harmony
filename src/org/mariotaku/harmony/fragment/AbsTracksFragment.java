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
import org.mariotaku.harmony.adapter.TracksAdapter;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.util.PreferencesEditor;

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
import android.widget.CursorAdapter;
import android.content.ContentResolver;

public abstract class AbsTracksFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {

	private static final String[] AUDIO_COLUMNS = new String[] { Audio.AudioColumns._ID, Audio.AudioColumns.TITLE, Audio.AudioColumns.DATA,
			Audio.AudioColumns.ALBUM, Audio.AudioColumns.ARTIST, Audio.AudioColumns.ARTIST_ID, Audio.AudioColumns.DURATION };

	private TracksAdapter mAdapter;
	private boolean mEditMode = false;
	private ListView mListView;
	long mPlaylistId = -1;

	private ContentResolver mResolver;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mResolver = getActivity().getContentResolver();
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

		mAdapter = new TracksAdapter(getActivity());
		setListAdapter(mAdapter);
		mListView = getListView();
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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

						uri = Playlists.Members.getContentUri(EXTERNAL_VOLUME, mPlaylistId);
						sort_order = Playlists.Members.DEFAULT_SORT_ORDER;
						break;
				}

			} else if (Audio.Genres.CONTENT_TYPE.equals(mimetype)) {
				long genre_id = getArguments().getLong(Audio.Genres._ID);
				uri = Genres.Members.getContentUri(EXTERNAL_VOLUME, genre_id);
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
		return new CursorLoader(getActivity(), uri, AUDIO_COLUMNS, where.toString(), null, sort_order);
	}

	public void onDrop(int from, int to) {
		if (mPlaylistId >= 0) {
			Playlists.Members.moveItem(mResolver, mPlaylistId, from, to);
		} else if (mPlaylistId == PLAYLIST_QUEUE) {
			MusicUtils.moveQueueItem(from, to);
		} else if (mPlaylistId == PLAYLIST_FAVORITES) {
			long favorites_id = MusicUtils.getFavoritesId(getActivity());
			Playlists.Members.moveItem(mResolver, favorites_id, from, to);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
	}

}
