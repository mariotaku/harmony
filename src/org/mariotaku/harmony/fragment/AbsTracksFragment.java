package org.mariotaku.harmony.fragment;

import android.app.LoaderManager;
import android.database.Cursor;
import android.content.Loader;
import android.os.Bundle;
import android.content.CursorLoader;
import android.net.Uri;
import android.provider.MediaStore.Audio;

public abstract class AbsTracksFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String[] AUDIO_COLUMNS = new String[] { Audio.AudioColumns._ID, Audio.AudioColumns.TITLE, Audio.AudioColumns.DATA,
		Audio.AudioColumns.ALBUM, Audio.AudioColumns.ARTIST, Audio.AudioColumns.ARTIST_ID, Audio.AudioColumns.DURATION };
	
	public final Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_COLUMNS, getWhereClause(args), getWhereArgs(args), getSortOrder(args));
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// TODO: Implement this method
	}

	public void onLoaderReset(final Loader<Cursor> loader) {
		// TODO: Implement this method
	}
	
	protected abstract String getWhereClause(final Bundle args);
	
	protected abstract String[] getWhereArgs(final Bundle args);
	
	protected String getSortOrder(final Bundle args) {
		return Audio.AudioColumns.TITLE;
	}
}
