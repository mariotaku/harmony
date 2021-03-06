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

package org.mariotaku.harmony.activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.fragment.AlbumTracksFragment;
import org.mariotaku.harmony.fragment.ArtistTracksFragment;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.util.ArrayUtils;
import org.mariotaku.harmony.fragment.GenreTracksFragment;

public class TracksBrowserActivity extends BaseActivity implements Constants {

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int URI_CODE_ALBUM_TRACKS = 1;
	private static final int URI_CODE_ARTIST_TRACKS = 2;
	private static final int URI_CODE_GENRE_TRACKS = 3;

	private ActionBar mActionBar;

	static {
		URI_MATCHER.addURI(AUTHORITY_ALBUMS, "*", URI_CODE_ALBUM_TRACKS);
		URI_MATCHER.addURI(AUTHORITY_ARTISTS, "*", URI_CODE_ARTIST_TRACKS);
		URI_MATCHER.addURI(AUTHORITY_GENRES, "*", URI_CODE_GENRE_TRACKS);
	}
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		
		final Intent intent = getIntent();
		final Uri uri = intent.getData();
		final Fragment fragment;
		final Bundle args = new Bundle();
		switch (URI_MATCHER.match(uri)) {
			case URI_CODE_ALBUM_TRACKS: {
				fragment = new AlbumTracksFragment();
				final long[] ids = ArrayUtils.fromString(uri.getLastPathSegment(), ',');
				args.putLongArray(INTENT_KEY_ALBUM_IDS, ids);
				fragment.setArguments(args);
				break;
			}
			case URI_CODE_ARTIST_TRACKS: {
				fragment = new ArtistTracksFragment();
				final long[] ids = ArrayUtils.fromString(uri.getLastPathSegment(), ',');
				args.putLongArray(INTENT_KEY_ARTIST_IDS, ids);
				fragment.setArguments(args);
				break;
			}
			case URI_CODE_GENRE_TRACKS: {
				fragment = new GenreTracksFragment();
				final String[] genres = uri.getLastPathSegment().split(",");
				args.putStringArray(INTENT_KEY_GENRES, genres);
				fragment.setArguments(args);
				break;
			}
			default: {
				finish();
				return;
			}
		}

		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, fragment);
		ft.commit();
	}

	static long parseLong(final String string, final long def) {
		if (string == null) return def;
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException e) {
			return def;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return true;
	}

	@Override
	public void onServiceConnected(ServiceWrapper service) {

	}

	@Override
	public void onServiceDisconnected() {
	}

	private void setTitle() {
//		String mimeType = bundle.getString(INTENT_KEY_TYPE);
//		String name;
//		long id;
//		if (Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
//			id = bundle.getLong(Audio.Playlists._ID);
//			switch ((int) id) {
//				case (int) PLAYLIST_QUEUE:
//					setTitle(R.string.now_playing);
//					return;
//				case (int) PLAYLIST_FAVORITES:
//					setTitle(R.string.favorites);
//					return;
//				case (int) PLAYLIST_RECENTLY_ADDED:
//					setTitle(R.string.recently_added);
//					return;
//				case (int) PLAYLIST_PODCASTS:
//					setTitle(R.string.podcasts);
//					return;
//				default:
//					if (id < 0) {
//						setTitle(R.string.music_library);
//						return;
//					}
//			}
//
//			name = MusicUtils.getPlaylistName(this, id);
//		} else if (Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
//			id = bundle.getLong(Audio.Artists._ID);
//			name = MusicUtils.getArtistName(this, id, true);
//		} else if (Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
//			id = bundle.getLong(Audio.Albums._ID);
//			name = MusicUtils.getAlbumName(this, id, true);
//		} else if (Audio.Genres.CONTENT_TYPE.equals(mimeType)) {
//			id = bundle.getLong(Audio.Genres._ID);
//			name = MusicUtils.parseGenreName(MusicUtils.getGenreName(this, id,
//					true));
//		} else {
//			setTitle(R.string.music_library);
//			return;
//		}
//
//		setTitle(name);

	}

}
