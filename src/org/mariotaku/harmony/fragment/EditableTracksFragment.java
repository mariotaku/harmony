package org.mariotaku.harmony.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mobeta.android.dslv.DragSortListView;
import org.mariotaku.harmony.R;

public abstract class EditableTracksFragment extends AbsTracksFragment implements DragSortListView.DragSortListener {

	@Override
	public DragSortListView getListView() {
		return (DragSortListView) super.getListView();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tracks_browser, null);
	}
	
	@Override
	public void onDrag(int from, int to) {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDrop(int from, int to) {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onRemove(int which) {
		mAdapter.notifyDataSetChanged();
	}
}
