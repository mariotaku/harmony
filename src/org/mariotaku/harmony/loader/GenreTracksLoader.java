package org.mariotaku.harmony.loader;

import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.model.GenreInfo;
import org.mariotaku.harmony.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import org.mariotaku.harmony.util.SortCursor;

public class GenreTracksLoader extends CursorLoader implements Constants {

	private static final Uri GENRES_CONTENT_URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
	private static final String[] GENRES_COLUMNS = new String[]{ MediaStore.Audio.Genres._ID };

	private final ContentResolver mResolver;
	
	private final String[] mColumns;
	private final String[] mGenres;
	private final String mSortOrder;

	public GenreTracksLoader(final Context context, final String[] cols, final String[] genres, final String sortOrder) {
		super(context);
		mResolver = context.getContentResolver();
		mColumns = cols;
		mGenres = genres;
		mSortOrder = sortOrder;
	}

	@Override
	public Cursor loadInBackground() {
		if (mGenres == null) return null;
		final ArrayList<String> where_args = new ArrayList<String>();
		for (final String genre_str : mGenres) {
			final GenreInfo genre = GenreInfo.valueOf(genre_str);
			if (genre.isUnknown()) {
				where_args.add("");
				where_args.add(" ");
			}
			where_args.add(String.valueOf(genre.getId()));
			where_args.add(genre.getName());
		}
		final char[] question_marks = new char[where_args.size()];
		Arrays.fill(question_marks, '?');
		final String where = MediaStore.Audio.Genres.NAME + " IN(" + ArrayUtils.toString(question_marks, ',', false) + ") COLLATE NOCASE";
		final Cursor ids_cursor = mResolver.query(GENRES_CONTENT_URI, GENRES_COLUMNS, where, where_args.toArray(new String[where_args.size()]), null);
		if (ids_cursor == null) return null;
		final int ids_count = ids_cursor.getCount();
		final Cursor[] cursors = new Cursor[ids_count];
		final int idx = ids_cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
		final String members_where = MediaStore.Audio.Genres.Members.IS_MUSIC + " = 1";
		for (int i = 0; i < ids_count; i++) {
			ids_cursor.moveToPosition(i);
			final Uri uri = MediaStore.Audio.Genres.Members.getContentUri(EXTERNAL_VOLUME, ids_cursor.getLong(idx));
			cursors[i] = mResolver.query(uri, mColumns, members_where, null, mSortOrder);
		}
		return new SortCursor(cursors, mSortOrder);
	}
}
