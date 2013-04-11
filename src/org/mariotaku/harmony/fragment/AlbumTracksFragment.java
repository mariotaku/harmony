package org.mariotaku.harmony.fragment;

import android.os.Bundle;
import android.provider.MediaStore.Audio;
import org.mariotaku.harmony.util.ArrayUtils;

public class AlbumTracksFragment extends AbsTracksFragment {

	protected String getWhereClause(final Bundle args) {
		if (args == null) return null;
		return Audio.Media.ALBUM_ID + " IN (" + ArrayUtils.toString(args.getLongArray(INTENT_KEY_ALBUM_IDS), ',', false) + ")";
	}

	protected String getSortOrder(final Bundle args) {
		return Audio.Media.TRACK;
	}

}
