package org.mariotaku.harmony.fragment;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import org.mariotaku.harmony.loader.GenreTracksLoader;
import org.mariotaku.harmony.model.GenreInfo;

public class GenreTracksFragment extends AbsTracksFragment {

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final String[] genres = args != null ? args.getStringArray(INTENT_KEY_GENRES) : null;
		return new GenreTracksLoader(getActivity(), AUDIO_COLUMNS, genres, getSortOrder(args));
	}
	
	protected String getSortOrder(final Bundle args) {
		return Audio.Media.TITLE;
	}

}
