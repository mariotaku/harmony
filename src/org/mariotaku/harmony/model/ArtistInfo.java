package org.mariotaku.harmony.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Artists;
import android.text.TextUtils;

public class ArtistInfo {

	private static final String[] ARTIST_INFO_COLUMNS = new String[] { Artists._ID, Artists.ARTIST };

	public final String artist;
	public final long artist_id;

	public ArtistInfo(final Cursor cursor) {
		artist = cursor.getString(cursor.getColumnIndex(Artists.ARTIST));
		artist_id = cursor.getLong(cursor.getColumnIndex(Artists._ID));
	}

	public static ArtistInfo getArtistInfo(final Context context, final TrackInfo track) {
		if (context == null || track == null) return null;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Artists._ID + " = " + track.artist_id;
		final Cursor c = resolver.query(Artists.EXTERNAL_CONTENT_URI, ARTIST_INFO_COLUMNS, where, null, null);
		try {
			if (c == null || c.getCount() == 0) return null;
			c.moveToFirst();
			return new ArtistInfo(c);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	@Override
	public String toString() {
		return "ArtistInfo{artist=" + artist + ",artist_id=" + artist_id + "}";
	}

	public static boolean isUnknownArtist(final ArtistInfo artist) {
		if (artist == null) return true;
		return TextUtils.isEmpty(artist.artist) || MediaStore.UNKNOWN_STRING.equals(artist.artist);
	}
	
}
