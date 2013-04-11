package org.mariotaku.harmony.view;
import android.widget.GridView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.view.View;
import android.database.DataSetObserver;
import android.view.ViewGroup;
import android.util.Log;

public class HorizontalGridView extends GridView {

	public HorizontalGridView(Context context) {
		this(context, null);
	}
	
	public HorizontalGridView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.gridViewStyle);
	}
	
	public HorizontalGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPivotX(0);
		setPivotY(0);
		setRotation(90);
		setRotationY(180);
	}
	
	public void setAdapter(final ListAdapter adapter) {
		super.setAdapter(new AdapterWrapper(adapter));
	}
	
	public ListAdapter getAdapter() {
		final ListAdapter adapter = super.getAdapter();
		return adapter instanceof AdapterWrapper ? ((AdapterWrapper) adapter).getWrapped() : adapter;
	}
	
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int w = MeasureSpec.getSize(widthMeasureSpec), h = MeasureSpec.getSize(heightMeasureSpec);
		final int wSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY), hSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
		setMeasuredDimension(h, w);
		super.onMeasure(wSpec, hSpec);
	}
	
	static class AdapterWrapper implements ListAdapter {

		private ListAdapter mAdapter;

		AdapterWrapper(ListAdapter adapter) {
			mAdapter = adapter;
		}
		
		public ListAdapter getWrapped() {
			return mAdapter;
		}
		
		public void registerDataSetObserver(DataSetObserver observer) {
			mAdapter.registerDataSetObserver(observer);
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {
			mAdapter.unregisterDataSetObserver(observer);
		}

		public int getCount() {
			return mAdapter.getCount();
		}

		public Object getItem(int position) {
			return mAdapter.getItem(position);
		}

		public long getItemId(int position) {
			return mAdapter.getItemId(position);
		}

		public boolean hasStableIds() {
			return mAdapter.hasStableIds();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = mAdapter.getView(position, convertView, parent);
			if (convertView == null) {
				view.setPivotX(0);
				view.setPivotX(0);
				view.setRotation(view.getRotation() + 90);
				view.setRotationY(view.getRotationY() + 180);
			}
			return view;
		}

		public int getItemViewType(int position) {
			return mAdapter.getItemViewType(position);
		}

		public int getViewTypeCount() {
			return mAdapter.getViewTypeCount();
		}

		public boolean isEmpty() {
			return mAdapter.isEmpty();
		}

		public boolean areAllItemsEnabled() {
			return mAdapter.areAllItemsEnabled();
		}

		public boolean isEnabled(int position) {
			return mAdapter.isEnabled(position);
		}
		
		
	}
}
