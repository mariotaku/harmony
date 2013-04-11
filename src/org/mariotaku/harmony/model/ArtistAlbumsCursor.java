package org.mariotaku.harmony.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.Constants;
import android.content.ContentResolver;

public class ArtistAlbumsCursor extends MergeCursor implements Constants {

	private static final String[] CHILD_COLUMNS = new String[] { Audio.Albums._ID, Audio.Albums.ALBUM, Audio.Albums.ARTIST,
		Audio.Albums.ALBUM_ART, Audio.Albums.NUMBER_OF_SONGS, Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST };
	
	private final Bundle mExtras = new Bundle();

	private ArtistAlbumsCursor(final Cursor[] cursors) {
		super(cursors);
	}
	
	public Bundle getExtras() {
		return mExtras;
	}
	
	public static Cursor getInstance(final Context context, final Cursor groupCursor) {
		if (context == null || groupCursor == null) return null;
		final ContentResolver resolver = context.getContentResolver();
		final long id = groupCursor.getLong(groupCursor.getColumnIndex(Audio.Artists._ID));
		final Uri uri = Audio.Artists.Albums.getContentUri(EXTERNAL_VOLUME, id);
		final MatrixCursor extra_cursor = new MatrixCursor(CHILD_COLUMNS);
		final Object[] cols = new Object[] {-1, context.getString(R.string.all_tracks), groupCursor.getString(groupCursor.getColumnIndex(Audio.Albums.ARTIST)), null, 0, 0};
		extra_cursor.addRow(cols);
		final Cursor album_cursor = resolver.query(uri, CHILD_COLUMNS, null, null, Audio.Albums.DEFAULT_SORT_ORDER);
		final Cursor c = new ArtistAlbumsCursor(new Cursor[] { extra_cursor, album_cursor });
		c.getExtras().putInt(EXTRA_GROUP_POSITION, groupCursor.getPosition());
		return c;
	}
}
