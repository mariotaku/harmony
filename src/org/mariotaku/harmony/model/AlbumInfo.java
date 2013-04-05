package org.mariotaku.harmony.model;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Albums;
import android.text.TextUtils;

public class AlbumInfo {

	private static final String[] ALBUM_INFO_COLUMNS = new String[] { Albums._ID, Albums.ALBUM, Albums.ARTIST, Albums.ALBUM_ART };

	public final String album, artist, album_art;
	public final long album_id;

	public AlbumInfo(final Cursor cursor) {
		album = cursor.getString(cursor.getColumnIndex(Albums.ALBUM));
		artist = cursor.getString(cursor.getColumnIndex(Albums.ARTIST));
		album_art = cursor.getString(cursor.getColumnIndex(Albums.ALBUM_ART));
		album_id = cursor.getLong(cursor.getColumnIndex(Albums._ID));
	}

	public static AlbumInfo getAlbumInfo(final Context context, final TrackInfo track) {
		if (context == null || track == null) return null;
		final ContentResolver resolver = context.getContentResolver();
		Cursor c = null;
		try {
			final String where = Albums._ID + " = " + track.album_id;
			c = resolver.query(Albums.EXTERNAL_CONTENT_URI, ALBUM_INFO_COLUMNS, where, null, null);
			if (c.getCount() == 0) return null;
			c.moveToFirst();
			return new AlbumInfo(c);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	@Override
	public String toString() {
		return "AlbumInfo{album=" + album + ",album_id=" + album_id + ",artist=" + artist + ",album_art,"
			+ album_art + "}";
	}
	
	public static boolean isUnknownAlbum(final AlbumInfo info) {
		if (info == null) return true;
		return TextUtils.isEmpty(info.album) || MediaStore.UNKNOWN_STRING.equals(info.album);
	}

	public static boolean isUnknownArtist(final TrackInfo info) {
		if (info == null) return true;
		return TextUtils.isEmpty(info.artist) || MediaStore.UNKNOWN_STRING.equals(info.artist);
	}
	
}
