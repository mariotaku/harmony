package org.mariotaku.harmony.fragment;

import com.mobeta.android.dslv.DragSortListView;
import android.os.Bundle;

public class EditableTracksFragment extends AbsTracksFragment implements DragSortListView.DragSortListener {

	public void onDrag(int from, int to) {
		// TODO: Implement this method
	}

	public void onDrop(int from, int to) {
		// TODO: Implement this method
	}

	public void onRemove(int which) {
		// TODO: Implement this method
	}

	protected String getWhereClause(Bundle args) {
		// TODO: Implement this method
		return null;
	}

	protected String[] getWhereArgs(Bundle args) {
		// TODO: Implement this method
		return null;
	}
	
}
