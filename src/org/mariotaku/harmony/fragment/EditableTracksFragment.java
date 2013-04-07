package org.mariotaku.harmony.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mobeta.android.dslv.DragSortListView;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.adapter.TracksAdapter;

public abstract class EditableTracksFragment extends AbsTracksFragment implements DragSortListView.DragSortListener {

	@Override
	public DragSortListView getListView() {
		return (DragSortListView) super.getListView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final DragSortListView list = getListView();
		list.setDragSortListener(this);
		//list.setOnItemClickListener(this);
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tracks_browser, null);
	}

}
