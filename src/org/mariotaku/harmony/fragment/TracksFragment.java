package org.mariotaku.harmony.fragment;

import android.os.Bundle;
import android.provider.MediaStore;

public class TracksFragment extends AbsTracksFragment {

	protected String getWhereClause(final Bundle args) {
		return MediaStore.Audio.AudioColumns.IS_MUSIC + " = 1";
	}

	protected String[] getWhereArgs(final Bundle args) {
		return null;
	}

	protected String getSortOrder(final Bundle args) {
		return MediaStore.Audio.AudioColumns.TITLE;
	}
	
}
