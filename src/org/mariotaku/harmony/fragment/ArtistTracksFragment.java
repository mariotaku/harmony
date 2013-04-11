package org.mariotaku.harmony.fragment;

import android.os.Bundle;
import android.provider.MediaStore.Audio;
import org.mariotaku.harmony.util.ArrayUtils;

public class ArtistTracksFragment extends AbsTracksFragment {

	protected String getWhereClause(final Bundle args) {
		if (args == null) return null;
		return Audio.Media.ARTIST_ID + " IN (" + ArrayUtils.toString(args.getLongArray(INTENT_KEY_ARTIST_IDS), ',', false) + ")";
	}

	protected String getSortOrder(final Bundle args) {
		return Audio.Media.TITLE;
	}
	
}
