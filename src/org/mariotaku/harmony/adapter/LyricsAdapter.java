package org.mariotaku.harmony.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.model.Lyrics;
import org.mariotaku.harmony.util.ListUtils;
import org.mariotaku.harmony.util.LyricsSplitter;

public class LyricsAdapter extends ArrayAdapter<Lyrics.Line> {

	private float mDensity, mMaxWidth, mTextSize;
	private int mCurrentPosition = -1;
	private boolean mAutoWrapEnabled = true;

	public LyricsAdapter(final Context context) {
		super(context, R.layout.lyrics_line_list_item);
		final Resources res = context.getResources();
		final DisplayMetrics metrics = res.getDisplayMetrics();
		mDensity = metrics.density;
		mMaxWidth = metrics.widthPixels / 3 * 2;
	}

	public void setAutoWrapEnabled(final boolean wrap) {
		if (mAutoWrapEnabled == wrap) return;
		mAutoWrapEnabled = wrap;
		if (wrap) {
			notifyDataSetChanged();
		}
		
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Lyrics.Line line = getItem(position);
		final TextView text_view = (TextView) view.findViewById(android.R.id.text1);
		if (mTextSize == 0) {
			mTextSize = text_view.getTextSize();
		}
		text_view.setTextSize(mTextSize);
		final String text = mAutoWrapEnabled ? LyricsSplitter.split(line.getText(), mTextSize * mDensity, mMaxWidth) : line.getText();
		text_view.setText(text);
		text_view.getPaint().setFakeBoldText(mCurrentPosition == position);
		text_view.setAlpha(mCurrentPosition == position ? 1.0f : 0.5f);
		return view;
	}
	
	public float getTextSize() {
		return mTextSize;
	}

	public void loadLyrics(final Lyrics lyrics) {
		clear();
		if (lyrics == null) return;
		addAll(ListUtils.fromArray(lyrics.getAll()));
	}

	public void setCurrentPosition(final int position) {
		if (mCurrentPosition == position) return;
		mCurrentPosition = position;
		notifyDataSetChanged();
	}

	public void setMaxWidth(final float width) {
		if (mMaxWidth == width) return;
		mMaxWidth = width;
		notifyDataSetChanged();
	}
	
	public void setTextSize(final float size) {
		if (mTextSize == size) return;
		mTextSize = size;
		notifyDataSetChanged();
	}
}
