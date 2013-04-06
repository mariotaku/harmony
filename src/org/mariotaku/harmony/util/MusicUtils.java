/*
 * Copyright (C) 2012 The MusicMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.harmony.util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.IMusicPlaybackService;
import org.mariotaku.harmony.MusicPlaybackService;
import org.mariotaku.harmony.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.Audio.Playlists;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import org.mariotaku.harmony.model.TrackInfo;

public class MusicUtils implements Constants {

	public static IMusicPlaybackService sService = null;

	private final static long[] mEmptyList = new long[0];

	private static ContentValues[] sContentValuesCache = null;

	/*
	 * Try to use String.format() as little as possible, because it creates a
	 * new Formatter every time you call it, which is very inefficient. Reusing
	 * an existing Formatter more than tripled the speed of makeTimeString().
	 * This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
	 */
	private static StringBuilder sFormatBuilder = new StringBuilder();

	private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());

	private static final Object[] sTimeArgs = new Object[5];

	public static final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");

	private static LogEntry[] mMusicLog = new LogEntry[100];

	private static int mLogPtr = 0;

	private static Time mTime = new Time();
	
	public static void addToCurrentPlaylist(Context context, long[] list) {

		if (sService == null) return;
		try {
			sService.enqueue(list, MusicPlaybackService.ACTION_LAST);
			String message = context.getResources().getQuantityString(
					R.plurals.NNNtrackstoplaylist, list.length, Integer.valueOf(list.length));
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		} catch (RemoteException ex) {
		}
	}

	public static void addToFavorites(Context context, long id) {

		long favorites_id;

		if (id < 0) {
			// this shouldn't happen (the menuitems shouldn't be visible
			// unless the selected item represents something playable
			Log.e(LOGTAG_MUSICUTILS, "playlist id " + id + " is invalid.");
		} else {
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.

			String favorites_where = Audio.Playlists.NAME + "='" + PLAYLIST_NAME_FAVORITES + "'";
			String[] favorites_cols = new String[] { Audio.Playlists._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = resolver.query(favorites_uri, favorites_cols, favorites_where, null,
					null);
			if (cursor.getCount() <= 0) {
				favorites_id = createPlaylist(context, PLAYLIST_NAME_FAVORITES);
			} else {
				cursor.moveToFirst();
				favorites_id = cursor.getLong(0);
				cursor.close();
			}

			String[] cols = new String[] { Playlists.Members.AUDIO_ID };
			Uri uri = Playlists.Members.getContentUri("external", favorites_id);
			Cursor cur = resolver.query(uri, cols, null, null, null);

			int base = cur.getCount();
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (cur.getLong(0) == id) return;
				cur.moveToNext();
			}
			cur.close();

			ContentValues values = new ContentValues();
			values.put(Playlists.Members.AUDIO_ID, id);
			values.put(Playlists.Members.PLAY_ORDER, base + 1);
			resolver.insert(uri, values);

		}
	}

	public static void addToPlaylist(Context context, long[] ids, long playlistid) {

		if (ids == null) {
			// this shouldn't happen (the menuitems shouldn't be visible
			// unless the selected item represents something playable
			Log.e("MusicBase", "ListSelection null");
		} else {
			int size = ids.length;
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.
			String[] cols = new String[] { "count(*)" };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
			Cursor cur = resolver.query(uri, cols, null, null, null);
			cur.moveToFirst();
			int base = cur.getInt(0);
			cur.close();
			int numinserted = 0;
			for (int i = 0; i < size; i += 1000) {
				makeInsertItems(ids, i, 1000, base);
				numinserted += resolver.bulkInsert(uri, sContentValuesCache);
			}
			String message = context.getResources().getQuantityString(
					R.plurals.NNNtrackstoplaylist, numinserted, numinserted);
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			// mLastPlaylistSelected = playlistid;
		}
	}

	public static void clearPlaylist(Context context, int plid) {

		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", plid);
		context.getContentResolver().delete(uri, null, null);
		return;
	}

	public static void clearQueue() {

		if (sService == null) return;

		try {
			sService.removeTracks(0, Integer.MAX_VALUE);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static long createPlaylist(Context context, String name) {

		if (name != null && name.length() > 0) {
			ContentResolver resolver = context.getContentResolver();
			String[] cols = new String[] { MediaStore.Audio.Playlists.NAME };
			String whereclause = MediaStore.Audio.Playlists.NAME + " = '" + name + "'";
			Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cols,
					whereclause, null, null);
			if (cur.getCount() <= 0) {
				ContentValues values = new ContentValues(1);
				values.put(MediaStore.Audio.Playlists.NAME, name);
				Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
				return Long.parseLong(uri.getLastPathSegment());
			}
			return -1;
		}
		return -1;
	}

	public static void debugDump(PrintWriter out) {

		for (int i = 0; i < mMusicLog.length; i++) {
			int idx = mLogPtr + i;
			if (idx >= mMusicLog.length) {
				idx -= mMusicLog.length;
			}
			LogEntry entry = mMusicLog[idx];
			if (entry != null) {
				entry.dump(out);
			}
		}
	}

	public static void debugLog(Object o) {

		mMusicLog[mLogPtr] = new LogEntry(o);
		mLogPtr++;
		if (mLogPtr >= mMusicLog.length) {
			mLogPtr = 0;
		}
	}

	public static void deleteLyrics(Context context, long[] list) {

		String[] cols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID };
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media._ID + " IN (");
		for (int i = 0; i < list.length; i++) {
			where.append(list[i]);
			if (i < list.length - 1) {
				where.append(",");
			}
		}
		where.append(")");
		Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols,
				where.toString(), null, null);

		int mDeletedLyricsCount = 0;

		if (c != null) {
			// remove files from card
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String name = c.getString(1);
				String lyrics = name.substring(0, name.lastIndexOf(".")) + ".lrc";
				File f = new File(lyrics);
				try { // File.delete can throw a security exception
					if (!f.delete()) {
						// I'm not sure if we'd ever get here (deletion would
						// have to fail, but no exception thrown)
						Log.e(LOGTAG_MUSICUTILS, "Failed to delete file " + lyrics);
					} else {
						mDeletedLyricsCount += 1;
					}
					c.moveToNext();
				} catch (SecurityException ex) {
					c.moveToNext();
				}
			}
			c.close();
		}

		String message = context.getResources().getQuantityString(R.plurals.NNNlyricsdeleted,
				mDeletedLyricsCount, Integer.valueOf(mDeletedLyricsCount));

		//reloadLyrics();
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	// XXX
	public static void deleteTracks(Context context, long[] list) {

		String[] cols = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID };
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media._ID + " IN (");
		for (int i = 0; i < list.length; i++) {
			where.append(list[i]);
			if (i < list.length - 1) {
				where.append(",");
			}
		}
		where.append(")");
		Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols,
				where.toString(), null, null);

		if (c != null) {

			// step 1: remove selected tracks from the current playlist, as well
			// as from the album art cache
			try {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					// remove from current playlist
					long id = c.getLong(0);
					sService.removeTrack(id);
					// remove from album art cache
					c.moveToNext();
				}
			} catch (RemoteException ex) {
			}

			// step 2: remove selected tracks from the database
			context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					where.toString(), null);

			// step 3: remove files from card
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String name = c.getString(1);
				File f = new File(name);
				try { // File.delete can throw a security exception
					if (!f.delete()) {
						// I'm not sure if we'd ever get here (deletion would
						// have to fail, but no exception thrown)
						Log.e(LOGTAG_MUSICUTILS, "Failed to delete file " + name);
					}
					c.moveToNext();
				} catch (SecurityException ex) {
					c.moveToNext();
				}
			}
			c.close();
		}

		String message = context.getResources().getQuantityString(R.plurals.NNNtracksdeleted,
				list.length, Integer.valueOf(list.length));

		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		// We deleted a number of tracks, which could affect any number of
		// things
		// in the media content domain, so update everything.
		context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
	}

	public static String getAlbumName(Context context, long album_id, boolean default_name) {
		String where = Audio.Albums._ID + "=" + album_id;
		String[] cols = new String[] { Audio.Albums.ALBUM };
		Uri uri = Audio.Albums.EXTERNAL_CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, cols, where, null, null);
		if (cursor.getCount() <= 0) {
			if (default_name)
				return context.getString(R.string.unknown_album);
			else
				return MediaStore.UNKNOWN_STRING;
		} else {
			cursor.moveToFirst();
			String name = cursor.getString(0);
			cursor.close();
			if (name == null || MediaStore.UNKNOWN_STRING.equals(name)) {
				if (default_name)
					return context.getString(R.string.unknown_album);
				else
					return MediaStore.UNKNOWN_STRING;
			}
			return name;
		}

	}

	public static long[] getAllSongs(Context context) {

		Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media._ID },
				MediaStore.Audio.Media.IS_MUSIC + "=1", null, null);
		try {
			if (c == null || c.getCount() == 0) return null;
			int len = c.getCount();
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				c.moveToNext();
				list[i] = c.getLong(0);
			}

			return list;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public static String getArtistName(Context context, long artist_id, boolean default_name) {
		String where = Audio.Artists._ID + "=" + artist_id;
		String[] cols = new String[] { Audio.Artists.ARTIST };
		Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, cols, where, null, null);
		if (cursor.getCount() <= 0) {
			if (default_name)
				return context.getString(R.string.unknown_artist);
			else
				return MediaStore.UNKNOWN_STRING;
		} else {
			cursor.moveToFirst();
			String name = cursor.getString(0);
			cursor.close();
			if (name == null || MediaStore.UNKNOWN_STRING.equals(name)) {
				if (default_name)
					return context.getString(R.string.unknown_artist);
				else
					return MediaStore.UNKNOWN_STRING;
			}
			return name;
		}

	}

	public static Uri getArtworkUri(Context context, long album_id) {
		return getArtworkUri(context, -1, album_id);
	}

	public static Uri getArtworkUri(Context context, long song_id, long album_id) {

		if (album_id < 0) {
			// This is something that is not in the database, so get the album
			// art directly
			// from the file.
			if (song_id >= 0) return getArtworkUriFromFile(context, song_id, -1);
			return null;
		}

		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(ALBUMART_URI, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				return uri;
			} catch (FileNotFoundException ex) {
				// The album art thumbnail does not actually exist. Maybe the
				// user deleted it, or
				// maybe it never existed to begin with.
				return getArtworkUriFromFile(context, song_id, album_id);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}

	public static String getBetterGenresWhereClause(Context context) {

		StringBuilder builder = new StringBuilder();

		ContentResolver resolver = context.getContentResolver();
		String[] genres_cols = new String[] { Audio.Genres._ID };
		Uri genres_uri = Audio.Genres.EXTERNAL_CONTENT_URI;

		Cursor genres_cursor = resolver.query(genres_uri, genres_cols, null, null, null);

		if (genres_cursor != null) {
			if (genres_cursor.getCount() <= 0) {
				genres_cursor.close();
				return null;
			}
		} else
			return null;

		builder.append(Audio.Genres._ID + " IN (");
		genres_cursor.moveToFirst();
		while (!genres_cursor.isAfterLast()) {
			long genre_id = genres_cursor.getLong(0);
			StringBuilder where = new StringBuilder();
			where.append(Genres.Members.IS_MUSIC + "=1");
			where.append(" AND " + Genres.Members.TITLE + "!=''");
			String[] cols = new String[] { Genres.Members._ID };
			Uri uri = Genres.Members.getContentUri(EXTERNAL_VOLUME, genre_id);
			Cursor member_cursor = context.getContentResolver().query(uri, cols, where.toString(),
					null, null);
			if (member_cursor != null) {
				if (member_cursor.getCount() > 0) {
					builder.append(genre_id + ",");
				}
				member_cursor.close();
			}
			genres_cursor.moveToNext();
		}
		genres_cursor.close();
		builder.deleteCharAt(builder.length() - 1);
		builder.append(")");

		return builder.toString();
	}

	public static int getCardId(Context context) {

		ContentResolver res = context.getContentResolver();
		Cursor c = res.query(Uri.parse("content://media/external/fs_id"), null, null, null, null);
		int id = -1;
		if (c != null) {
			c.moveToFirst();
			id = c.getInt(0);
			c.close();
		}
		return id;
	}
	
	public static int getCurrentShuffleMode() {

		int mode = SHUFFLE_NONE;
		if (sService != null) {
			try {
				mode = sService.getShuffleMode();
			} catch (RemoteException ex) {
			}
		}
		return mode;
	}

	public static long getFavoritesId(Context context) {
		long favorites_id = -1;
		String favorites_where = Audio.Playlists.NAME + "='" + PLAYLIST_NAME_FAVORITES + "'";
		String[] favorites_cols = new String[] { Audio.Playlists._ID };
		Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		Cursor cursor = query(context, favorites_uri, favorites_cols, favorites_where, null, null);
		if (cursor.getCount() <= 0) {
			favorites_id = createPlaylist(context, PLAYLIST_NAME_FAVORITES);
		} else {
			cursor.moveToFirst();
			favorites_id = cursor.getLong(0);
			cursor.close();
		}
		return favorites_id;
	}

	public static String getGenreName(Context context, long genre_id, boolean default_name) {
		String where = Audio.Genres._ID + "=" + genre_id;
		String[] cols = new String[] { Audio.Genres.NAME };
		Uri uri = Audio.Genres.EXTERNAL_CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, cols, where, null, null);
		if (cursor.getCount() <= 0) {
			if (default_name)
				return context.getString(R.string.unknown_genre);
			else
				return MediaStore.UNKNOWN_STRING;
		} else {
			cursor.moveToFirst();
			String name = cursor.getString(0);
			cursor.close();
			if (name == null || MediaStore.UNKNOWN_STRING.equals(name)) {
				if (default_name)
					return context.getString(R.string.unknown_genre);
				else
					return MediaStore.UNKNOWN_STRING;
			}
			return name;
		}
	}

	public static String getPlaylistName(Context context, long playlist_id) {
		String where = Audio.Playlists._ID + "=" + playlist_id;
		String[] cols = new String[] { Audio.Playlists.NAME };
		Uri uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, cols, where, null, null);
		if (cursor.getCount() <= 0) return "";

		cursor.moveToFirst();
		String name = cursor.getString(0);
		cursor.close();
		return name;
	}

	public static int getQueuePosition() {
		if (sService == null) return 0;

		try {
			return sService.getQueuePosition();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static long getSleepTimerRemained() {

		long remained = 0;
		if (sService == null) return remained;
		try {
			remained = sService.getSleepTimerRemained();
		} catch (Exception e) {
			// do nothing
		}
		return remained;
	}

	public static long[] getSongListForAlbum(Context context, long id) {

		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = MediaStore.Audio.Media.ALBUM_ID + "=" + id + " AND "
				+ MediaStore.Audio.Media.IS_MUSIC + "=1";
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where,
				null, MediaStore.Audio.Media.TRACK);

		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return mEmptyList;
	}

	public static long[] getSongListForArtist(Context context, long id) {

		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = MediaStore.Audio.Media.ARTIST_ID + "=" + id + " AND "
				+ MediaStore.Audio.Media.IS_MUSIC + "=1";
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where,
				null, MediaStore.Audio.Media.ALBUM_KEY + "," + MediaStore.Audio.Media.TRACK);

		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return mEmptyList;
	}

	public static long[] getSongListForCursor(Cursor cursor) {

		if (cursor == null) return mEmptyList;
		int len = cursor.getCount();
		long[] list = new long[len];
		cursor.moveToFirst();
		int colidx = -1;
		try {
			colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
		} catch (IllegalArgumentException ex) {
			colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
		}
		for (int i = 0; i < len; i++) {
			list[i] = cursor.getLong(colidx);
			cursor.moveToNext();
		}
		return list;
	}

	public static long[] getSongListForPlaylist(Context context, long plid) {

		final String[] ccols = new String[] { MediaStore.Audio.Playlists.Members.AUDIO_ID };
		Cursor cursor = query(context, Playlists.Members.getContentUri("external", plid), ccols,
				null, null, Playlists.Members.DEFAULT_SORT_ORDER);

		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return mEmptyList;
	}

	public static String getTrackName(Context context, long audio_id) {
		String where = Audio.Media._ID + "=" + audio_id;
		String[] cols = new String[] { Audio.Media.TITLE };
		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, cols, where, null, null);
		if (cursor.getCount() <= 0) return "";

		cursor.moveToFirst();
		String name = cursor.getString(0);
		cursor.close();
		return name;
	}

	public static boolean isFavorite(Context context, long id) {

		long favorites_id;

		if (id < 0) {
			// this shouldn't happen (the menuitems shouldn't be visible
			// unless the selected item represents something playable
			Log.e(LOGTAG_MUSICUTILS, "playlist id " + id + " is invalid.");
		} else {
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.

			String favorites_where = Audio.Playlists.NAME + "='" + PLAYLIST_NAME_FAVORITES + "'";
			String[] favorites_cols = new String[] { Audio.Playlists._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = resolver.query(favorites_uri, favorites_cols, favorites_where, null,
					null);
			if (cursor.getCount() <= 0) {
				favorites_id = createPlaylist(context, PLAYLIST_NAME_FAVORITES);
			} else {
				cursor.moveToFirst();
				favorites_id = cursor.getLong(0);
				cursor.close();
			}

			String[] cols = new String[] { Playlists.Members.AUDIO_ID };
			Uri uri = Playlists.Members.getContentUri("external", favorites_id);
			Cursor cur = resolver.query(uri, cols, null, null, null);

			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (cur.getLong(0) == id) {
					cur.close();
					return true;
				}
				cur.moveToNext();
			}
			cur.close();
			return false;
		}
		return false;
	}

	public static boolean isMediaScannerScanning(Context context) {

		boolean result = false;
		Cursor cursor = query(context, MediaStore.getMediaScannerUri(),
				new String[] { MediaStore.MEDIA_SCANNER_VOLUME }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() == 1) {
				cursor.moveToFirst();
				result = "external".equals(cursor.getString(0));
			}
			cursor.close();
		}

		return result;
	}

	/*
	 * Returns true if a file is currently opened for playback (regardless of
	 * whether it's playing or paused).
	 */
	public static boolean isMusicLoaded() {

		if (MusicUtils.sService != null) {
			try {
				return sService.getTrackInfo() != null;
			} catch (RemoteException ex) {
			}
		}
		return false;
	}

	public static String makeAlbumsLabel(Context context, int numalbums, int numsongs,
			boolean isUnknown) {

		// There are two formats for the albums/songs information:
		// "N Song(s)" - used for unknown artist/album
		// "N Album(s)" - used for known albums

		StringBuilder songs_albums = new StringBuilder();

		Resources r = context.getResources();
		if (isUnknown) {
			String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
			sFormatBuilder.setLength(0);
			sFormatter.format(f, Integer.valueOf(numsongs));
			songs_albums.append(sFormatBuilder);
		} else {
			String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
			sFormatBuilder.setLength(0);
			sFormatter.format(f, Integer.valueOf(numalbums));
			songs_albums.append(sFormatBuilder);
			songs_albums.append("\n");
		}
		return songs_albums.toString();
	}

	/**
	 * This is now only used for the query screen
	 */
	public static String makeAlbumsSongsLabel(Context context, int numalbums, int numsongs,
			boolean isUnknown) {

		// There are several formats for the albums/songs information:
		// "1 Song" - used if there is only 1 song
		// "N Songs" - used for the "unknown artist" item
		// "1 Album"/"N Songs"
		// "N Album"/"M Songs"
		// Depending on locale, these may need to be further subdivided

		StringBuilder songs_albums = new StringBuilder();

		Resources r = context.getResources();
		if (!isUnknown) {
			String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
			sFormatBuilder.setLength(0);
			sFormatter.format(f, Integer.valueOf(numalbums));
			songs_albums.append(sFormatBuilder);
			songs_albums.append("\n");
		}
		String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
		sFormatBuilder.setLength(0);
		sFormatter.format(f, Integer.valueOf(numsongs));
		songs_albums.append(sFormatBuilder);
		return songs_albums.toString();
	}

	public static void makePlaylistList(Context context, boolean create_shortcut,
			List<Map<String, String>> list) {

		Map<String, String> map;

		String[] cols = new String[] { Audio.Playlists._ID, Audio.Playlists.NAME };
		StringBuilder where = new StringBuilder();

		ContentResolver resolver = context.getContentResolver();
		if (resolver == null) {
			System.out.println("resolver = null");
		} else {
			where.append(Audio.Playlists.NAME + " != ''");
			where.append(" AND " + Audio.Playlists.NAME + " != '" + PLAYLIST_NAME_FAVORITES + "'");
			Cursor cur = resolver.query(Audio.Playlists.EXTERNAL_CONTENT_URI, cols,
					where.toString(), null, Audio.Playlists.NAME);
			list.clear();

			map = new HashMap<String, String>();
			map.put(MAP_KEY_ID, String.valueOf(PLAYLIST_FAVORITES));
			map.put(MAP_KEY_NAME, context.getString(R.string.favorites));
			list.add(map);

			if (create_shortcut) {
				map = new HashMap<String, String>();
				map.put(MAP_KEY_ID, String.valueOf(PLAYLIST_ALL_SONGS));
				map.put(MAP_KEY_NAME, context.getString(R.string.play_all));
				list.add(map);

				map = new HashMap<String, String>();
				map.put(MAP_KEY_ID, String.valueOf(PLAYLIST_RECENTLY_ADDED));
				map.put(MAP_KEY_NAME, context.getString(R.string.recently_added));
				list.add(map);
			} else {
				map = new HashMap<String, String>();
				map.put(MAP_KEY_ID, String.valueOf(PLAYLIST_QUEUE));
				map.put(MAP_KEY_NAME, context.getString(R.string.queue));
				list.add(map);

				map = new HashMap<String, String>();
				map.put(MAP_KEY_ID, String.valueOf(PLAYLIST_NEW));
				map.put(MAP_KEY_NAME, context.getString(R.string.new_playlist));
				list.add(map);
			}

			if (cur != null && cur.getCount() > 0) {
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					map = new HashMap<String, String>();
					map.put(MAP_KEY_ID, String.valueOf(cur.getLong(0)));
					map.put(MAP_KEY_NAME, cur.getString(1));
					list.add(map);
					cur.moveToNext();
				}

			}
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static String makeTimeString(Context context, long secs) {

		String durationformat = context.getString(secs < 3600 ? R.string.durationformatshort
				: R.string.durationformatlong);

		/*
		 * Provide multiple arguments so the format can be changed easily by
		 * modifying the xml.
		 */
		sFormatBuilder.setLength(0);

		final Object[] timeArgs = sTimeArgs;
		timeArgs[0] = secs / 3600;
		timeArgs[1] = secs / 60;
		timeArgs[2] = secs / 60 % 60;
		timeArgs[3] = secs;
		timeArgs[4] = secs % 60;

		return sFormatter.format(durationformat, timeArgs).toString();
	}

	public static void movePlaylistItem(Context context, Cursor cursor, long playlist_id, int from,
			int to) {

		if (from < 0) {
			from = 0;
		}
		if (to < 0) {
			to = 0;
		}

		ContentResolver resolver = context.getContentResolver();
		cursor.moveToPosition(from);
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID));
		Uri uri = Playlists.Members.getContentUri("external", playlist_id);
		resolver.delete(uri, Playlists.Members.AUDIO_ID + "=" + id, null);

		ContentValues values = new ContentValues();
		values.put(Playlists.Members.AUDIO_ID, id);
		values.put(Playlists.Members.PLAY_ORDER, to);
		resolver.insert(uri, values);
	}

	public static void moveQueueItem(int from, int to) {
		if (sService == null) return;

		try {
			sService.moveQueueItem(from, to);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static String parseGenreName(String orig) {
		int genre_id = -1;

		if (orig == null || orig.trim().length() <= 0) return "Unknown";

		try {
			genre_id = Integer.parseInt(orig);
		} catch (NumberFormatException e) {
			// string is not a valid number, so return original value.
			return orig;
		}
		if (genre_id >= 0 && genre_id < GENRES_DB.length)
			return GENRES_DB[genre_id];
		else
			return "Unknown";

	}

	public static void playAll(Context context) {

		playAll(context, getAllSongs(context), 0);
	}

	public static void playAll(Context context, Cursor cursor) {

		playAll(context, cursor, 0, false);
	}

	public static void playAll(Context context, Cursor cursor, int position) {

		playAll(context, cursor, position, false);
	}

	public static void playAll(Context context, long[] list, int position) {

		playAll(context, list, position, false);
	}

	public static void playPlaylist(Context context, long plid) {

		long[] list = getSongListForPlaylist(context, plid);
		if (list != null) {
			playAll(context, list, -1, false);
		}
	}

	public static void playRecentlyAdded(Context context) {

		// do a query for all songs added in the last X weeks
		int weekX = new PreferencesEditor(context).getIntPref(PREF_KEY_NUMWEEKS, 2) * 3600 * 24 * 7;
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = MediaStore.MediaColumns.DATE_ADDED + ">"
				+ (System.currentTimeMillis() / 1000 - weekX);
		Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where,
				null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		if (cursor == null) // Todo: show a message
			return;
		try {
			int len = cursor.getCount();
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				cursor.moveToNext();
				list[i] = cursor.getLong(0);
			}
			MusicUtils.playAll(context, list, 0);
		} catch (SQLiteException ex) {
		} finally {
			cursor.close();
		}
	}

	public static Cursor query(Context context, Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
	}

	public static Cursor query(Context context, Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder, int limit) {

		try {
			ContentResolver resolver = context.getContentResolver();
			if (resolver == null) return null;
			if (limit > 0) {
				uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
			}
			return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (UnsupportedOperationException ex) {
			return null;
		}

	}

	public static void removeFromFavorites(Context context, long id) {

		long favorites_id;

		if (id < 0) {
			// this shouldn't happen (the menuitems shouldn't be visible
			// unless the selected item represents something playable
			Log.e(LOGTAG_MUSICUTILS, "playlist id " + id + " is invalid.");
		} else {
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.

			String favorites_where = Audio.Playlists.NAME + "='" + PLAYLIST_NAME_FAVORITES + "'";
			String[] favorites_cols = new String[] { Audio.Playlists._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = resolver.query(favorites_uri, favorites_cols, favorites_where, null,
					null);
			if (cursor.getCount() <= 0) {
				favorites_id = createPlaylist(context, PLAYLIST_NAME_FAVORITES);
			} else {
				cursor.moveToFirst();
				favorites_id = cursor.getLong(0);
				cursor.close();
			}

			Uri uri = Playlists.Members.getContentUri("external", favorites_id);
			resolver.delete(uri, Playlists.Members.AUDIO_ID + "=" + id, null);

		}
	}

	public static int removeTrack(long id) {
		if (sService == null) return 0;

		try {
			return sService.removeTrack(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int removeTracks(int first, int last) {
		if (sService == null) return 0;

		try {
			return sService.removeTracks(first, last);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void renamePlaylist(Context context, long id, String name) {

		if (name != null && name.length() > 0) {
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues(1);
			values.put(MediaStore.Audio.Playlists.NAME, name);
			resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values,
					MediaStore.Audio.Playlists._ID + "=?", new String[] { String.valueOf(id) });

			Toast.makeText(context, R.string.playlist_renamed, Toast.LENGTH_SHORT).show();
		}
	}

	public static void setBackground(View v, Bitmap bm) {

		if (bm == null) {
			v.setBackgroundResource(0);
			return;
		}

		int vwidth = v.getWidth();
		int vheight = v.getHeight();
		int bwidth = bm.getWidth();
		int bheight = bm.getHeight();
		float scalex = (float) vwidth / bwidth;
		float scaley = (float) vheight / bheight;
		float scale = Math.max(scalex, scaley) * 1.3f;

		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		Bitmap bg = Bitmap.createBitmap(vwidth, vheight, config);
		Canvas c = new Canvas(bg);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		ColorMatrix greymatrix = new ColorMatrix();
		greymatrix.setSaturation(0);
		ColorMatrix darkmatrix = new ColorMatrix();
		darkmatrix.setScale(.3f, .3f, .3f, 1.0f);
		greymatrix.postConcat(darkmatrix);
		ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
		paint.setColorFilter(filter);
		Matrix matrix = new Matrix();
		matrix.setTranslate(-bwidth / 2, -bheight / 2); // move bitmap center to
														// origin
		matrix.postRotate(10);
		matrix.postScale(scale, scale);
		matrix.postTranslate(vwidth / 2, vheight / 2); // Move bitmap center to
														// view center
		c.drawBitmap(bm, matrix, paint);
		v.setBackgroundDrawable(new BitmapDrawable(bg));
	}

	public static void setQueueId(long id) {
		if (sService == null) return;
		try {
			sService.setQueueId(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void setQueuePosition(int index) {
		if (sService == null) return;
		try {
			sService.setQueuePosition(index);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void setSpinnerState(Activity a) {

		if (isMediaScannerScanning(a)) {
			// start the progress spinner
			a.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
					Window.PROGRESS_INDETERMINATE_ON);

			a.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
					Window.PROGRESS_VISIBILITY_ON);
		} else {
			// stop the progress spinner
			a.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
					Window.PROGRESS_VISIBILITY_OFF);
		}
	}

	public static void shuffleAll(Context context) {

		playAll(context, getAllSongs(context), 0, true);
	}

	public static void shuffleAll(Context context, Cursor cursor) {

		playAll(context, cursor, 0, true);
	}

	public static void startSleepTimer(long milliseconds, boolean gentle) {

		if (sService == null) return;
		try {
			sService.startSleepTimer(milliseconds, gentle);
		} catch (Exception e) {
			// do nothing
		}
	}

	public static void stopSleepTimer() {

		if (sService == null) return;
		try {
			sService.stopSleepTimer();
		} catch (Exception e) {
			// do nothing
		}
	}


	/** get album art for specified file */
	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {

		Bitmap bm = null;

		if (albumid < 0 && songid < 0)
			throw new IllegalArgumentException("Must specify an album or a song id");

		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(ALBUMART_URI, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			}
		} catch (IllegalStateException ex) {
		} catch (FileNotFoundException ex) {
		}
		return bm;
	}

	private static Uri getArtworkUriFromFile(Context context, long songid, long albumid) {

		if (albumid < 0 && songid < 0) return null;

		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) return uri;
			} else {
				Uri uri = ContentUris.withAppendedId(ALBUMART_URI, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) return uri;
			}
		} catch (FileNotFoundException ex) {
			//
		}
		return null;
	}

	private static Bitmap getDefaultArtwork(Context context) {

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeStream(
				context.getResources().openRawResource(R.drawable.ic_mp_albumart_unknown), null,
				opts);
	}

	/**
	 * @param ids
	 *            The source array containing all the ids to be added to the
	 *            playlist
	 * @param offset
	 *            Where in the 'ids' array we start reading
	 * @param len
	 *            How many items to copy during this pass
	 * @param base
	 *            The play order offset to use for this pass
	 */
	private static void makeInsertItems(long[] ids, int offset, int len, int base) {

		// adjust 'len' if would extend beyond the end of the source array
		if (offset + len > ids.length) {
			len = ids.length - offset;
		}
		// allocate the ContentValues array, or reallocate if it is the wrong
		// size
		if (sContentValuesCache == null || sContentValuesCache.length != len) {
			sContentValuesCache = new ContentValues[len];
		}
		// fill in the ContentValues array with the right values for this pass
		for (int i = 0; i < len; i++) {
			if (sContentValuesCache[i] == null) {
				sContentValuesCache[i] = new ContentValues();
			}

			sContentValuesCache[i].put(Playlists.Members.PLAY_ORDER, base + offset + i);
			sContentValuesCache[i].put(Playlists.Members.AUDIO_ID, ids[offset + i]);
		}
	}

	private static void playAll(Context context, Cursor cursor, int position, boolean force_shuffle) {

		long[] list = getSongListForCursor(cursor);
		playAll(context, list, position, force_shuffle);
	}

	private static void playAll(Context context, long[] list, int position, boolean force_shuffle) {
		if (list == null || list.length == 0 || sService == null) {
			Log.d(LOGTAG_MUSICUTILS, "attempt to play empty song list");
			// Don't try to play empty playlists. Nothing good will come of it.
			Toast.makeText(context, R.string.emptyplaylist, Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			if (force_shuffle) {
				sService.setShuffleMode(SHUFFLE_NORMAL);
			}
			long curid = sService.getTrackInfo().id;
			int curpos = sService.getQueuePosition();
			if (position != -1 && curpos == position && curid == list[position]) {
				// The selected file is the file that's currently playing;
				// figure out if we need to restart with a new playlist,
				// or just launch the playback activity.
				long[] playlist = sService.getQueue();
				if (Arrays.equals(list, playlist)) {
					// we don't need to set a new list, but we should resume
					// playback if needed
					sService.play();
					return; // the 'finally' block will still run
				}
			}
			if (position < 0) {
				position = 0;
			}
			sService.open(list, force_shuffle ? -1 : position);
			sService.play();
		} catch (RemoteException ex) {
		} finally {
			Intent intent = new Intent(INTENT_PLAYBACK_VIEWER)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	static protected Uri getContentURIForPath(String path) {

		return Uri.fromFile(new File(path));
	}

	// A really simple BitmapDrawable-like class, that doesn't do
	// scaling, dithering or filtering.
	private static class FastBitmapDrawable extends Drawable {

		private Bitmap mBitmap;

		public FastBitmapDrawable(Bitmap b) {
			mBitmap = b;
		}

		@Override
		public void draw(Canvas canvas) {
			if (mBitmap == null) return;
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.OPAQUE;
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}
	}

	static class LogEntry {

		Object item;
		long time;

		LogEntry(Object o) {

			item = o;
			time = System.currentTimeMillis();
		}

		void dump(PrintWriter out) {

			mTime.set(time);
			out.print(mTime.toString() + " : ");
			if (item instanceof Exception) {
				((Exception) item).printStackTrace(out);
			} else {
				out.println(item);
			}
		}
	}
}
