package org.mariotaku.harmony.fragment;

import android.os.Bundle;
import android.provider.MediaStore.Audio;

public class TracksFragment extends AbsTracksFragment {

	protected String[] getWhereArgs(final Bundle args) {
		return null;
	}
	
	protected String getWhereClause(final Bundle args) {
		return Audio.Media.IS_MUSIC + " = 1";
	}
	
	protected String getSortOrder(final Bundle args) {
		return Audio.Media.TITLE;
	}
	
}
