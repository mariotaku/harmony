package org.mariotaku.harmony.fragment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import org.mariotaku.harmony.util.ArrayUtils;
import org.mariotaku.harmony.util.ServiceWrapper;

public class QueueFragment extends EditableTracksFragment {

	private ServiceWrapper mService;

	protected String getSortOrder(final Bundle args) {
		return null;
	}
	
	protected String getWhereClause(Bundle args) {
		final long[] queue = mService != null ? mService.getQueue() : null;
		return MediaStore.Audio.Media._ID + " IN(" + ArrayUtils.toString(queue, ',', false) + ")";
	}

	protected String[] getWhereArgs(Bundle args) {
		return null;
	}
	
	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		mService = service;
		loadData();
	}

	@Override
	protected void onServiceDisconnected() {
		mService = null;
	}
	
	@Override
	protected void loadData() {
		if (mService == null) return;
		super.loadData();
	}
}
