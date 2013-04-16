package org.mariotaku.harmony.loader;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import java.util.ArrayList;
import java.util.Arrays;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.model.GenreInfo;

public class GenresLoader extends AsyncTaskLoader<GenreInfo[]> implements Constants {

	private static final String[] CURSOR_COLUMNS = new String[] { Audio.Genres._ID, Audio.Genres.NAME };
	private final ContentResolver mResolver;

	private final String mWhere, mSortOrder;
	private final String[] mWhereArgs;
	
	public GenresLoader(final Context context, final String where, final String[] whereArgs, final String sortOrder) {
		super(context);
		mResolver = context.getContentResolver();
		mWhere = where;
		mWhereArgs = whereArgs;
		mSortOrder = sortOrder;
	}

	@Override
	public GenreInfo[] loadInBackground() {
		final ArrayList<GenreInfo> list = new ArrayList<GenreInfo>();
		final Cursor genres_cur = mResolver.query(Audio.Genres.EXTERNAL_CONTENT_URI, CURSOR_COLUMNS, mWhere, mWhereArgs, mSortOrder);
		if (genres_cur != null) {
			final int id_idx = genres_cur.getColumnIndex(Audio.Genres._ID), name_idx = genres_cur.getColumnIndex(Audio.Genres.NAME);
			genres_cur.moveToFirst();
			while (!genres_cur.isAfterLast()) {
				final long id = genres_cur.getLong(id_idx);
				final Uri uri = Audio.Genres.Members.getContentUri(EXTERNAL_VOLUME, id);
				final Cursor c = mResolver.query(uri, new String[0], Audio.Genres.Members.IS_MUSIC + " = 1", null, null);
				final int count = c.getCount();
				c.close();
				if (count != 0) {
					final GenreInfo genre = GenreInfo.valueOf(genres_cur.getString(name_idx));
					if (!list.contains(genre)) {
						list.add(genre);
					}
				}
				genres_cur.moveToNext();
			}
			genres_cur.close();
		}
		final GenreInfo[] array = list.toArray(new GenreInfo[list.size()]);
		Arrays.sort(array);
		return array;
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}
	
}
