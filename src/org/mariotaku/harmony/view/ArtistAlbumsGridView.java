package org.mariotaku.harmony.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.util.Log;

public class ArtistAlbumsGridView extends GridView {

	public ArtistAlbumsGridView(final Context context) {
		this(context, null);
	}

	public ArtistAlbumsGridView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.gridViewStyle);
	}

	public ArtistAlbumsGridView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final ListAdapter adapter = getAdapter();
		if (adapter == null) return;
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int vertical_spacing = getVerticalSpacing();
		final int col_width = getColumnWidth() > 0 ? getColumnWidth() : getRequestedColumnWidth();
		final int num_columns = getNumColumns() > 0 ? getNumColumns() : (width / col_width);
		final int gridview_rows = (int) Math.ceil((float) adapter.getCount() / num_columns);
		final int spacing_sum = vertical_spacing * (gridview_rows - 1);
		final int height = col_width * gridview_rows + spacing_sum;
		setMeasuredDimension(width, height);
	}
}
