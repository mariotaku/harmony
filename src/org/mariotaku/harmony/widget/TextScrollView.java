package org.mariotaku.harmony.widget;

import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.LyricsSplitter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class TextScrollView extends ScrollView implements OnLongClickListener {

	// Namespaces to read attributes
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

	private static final String ATTR_TEXTSIZE = "textSize";
	private static final String ATTR_SHADOWCOLOR = "shadowColor";
	private static final String ATTR_SHADOWDX = "shadowDx";
	private static final String ATTR_SHADOWDY = "shadowDy";
	private static final String ATTR_SHADOWRADIUS = "shadowRadius";

	private LinearLayout mScrollContainer;
	private LinearLayout mContentContainer, mContentEmptyView;
	private boolean mSmoothScrolling = false;
	private boolean mEnableAutoScrolling = true;
	private int mTextColor = Color.WHITE, mShadowColor = Color.BLACK;
	private float mTextSize = 15.0f, mShadowDx = 0.0f, mShadowDy = 0.0f, mShadowRadius = 0.0f;
	private int mLastLineId = -1;
	private Object[] mContent;
	private final int TIMEOUT = 1;
	public OnLineSelectedListener mListener;
	private Context mContext;

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case TIMEOUT:
					mHandler.removeMessages(TIMEOUT);
					mEnableAutoScrolling = true;
					break;
			}
		}
	};

	public TextScrollView(Context context) {

		super(context);
		init(context);
	}

	public TextScrollView(Context context, AttributeSet attrs) {

		super(context, attrs);

		mShadowRadius = attrs.getAttributeFloatValue(ANDROID_NS, ATTR_SHADOWRADIUS, 0.0f);
		mShadowDx = attrs.getAttributeFloatValue(ANDROID_NS, ATTR_SHADOWDX, 0.0f);
		mShadowDy = attrs.getAttributeFloatValue(ANDROID_NS, ATTR_SHADOWDY, 0.0f);

		String shadow_color_value = attrs.getAttributeValue(ANDROID_NS, ATTR_SHADOWCOLOR);
		if (shadow_color_value != null) {
			if (shadow_color_value.startsWith("#")) {
				try {
					mShadowColor = Color.parseColor(shadow_color_value);
				} catch (IllegalArgumentException e) {
					Log.e("TextScrollView", "Wrong color: " + shadow_color_value);
				}
			} else if (shadow_color_value.startsWith("@")) {
				int colorResourceId = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_SHADOWCOLOR,
						0);
				if (colorResourceId != 0) {
					mShadowColor = context.getResources().getColor(colorResourceId);
				}
			}
		}

		String text_size_value = attrs.getAttributeValue(ANDROID_NS, ATTR_TEXTSIZE);
		if (text_size_value != null) {
			mTextSize = parseTextSize(text_size_value);
		}

		init(context);
		
	}

	@Override
	public boolean onLongClick(View view) {
		if (mListener != null) {
			Object tag = view.getTag();
			int id = tag != null ? Integer.valueOf(tag.toString()) : 0;
			mListener.onLineSelected(id);
		}
		return true;
	}

	@Override
	public void onSizeChanged(int width, int height, int old_width, int old_height) {

		mContentEmptyView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
		mContentContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		mContentContainer.setPadding(0, height / 2, 0, height / 2);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				mHandler.sendEmptyMessageDelayed(TIMEOUT, 2000L);
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				mHandler.removeMessages(TIMEOUT);
				mEnableAutoScrolling = false;
				break;
		}
		return super.onTouchEvent(event);

	}

	public void setContentGravity(int gravity) {
		if (mContentContainer != null) {
			mContentContainer.setGravity(gravity);
		}
	}

	public void setCurrentLine(int lineid, boolean force) {

		if (findViewWithTag(mLastLineId) != null) {
			((TextView) findViewWithTag(mLastLineId)).setTextColor(Color.argb(0xD0,
					Color.red(mTextColor), Color.green(mTextColor), Color.blue(mTextColor)));
			((TextView) findViewWithTag(mLastLineId)).getPaint().setFakeBoldText(false);
		}

		if (findViewWithTag(lineid) != null) {
			((TextView) findViewWithTag(lineid)).setTextColor(Color.argb(0xFF,
					Color.red(mTextColor), Color.green(mTextColor), Color.blue(mTextColor)));
			((TextView) findViewWithTag(lineid)).getPaint().setFakeBoldText(true);
			if (mEnableAutoScrolling || force) {
				if (mSmoothScrolling) {
					smoothScrollTo(0, findViewWithTag(lineid).getTop()
							+ findViewWithTag(lineid).getHeight() / 2 - getHeight() / 2);
				} else {
					scrollTo(0, findViewWithTag(lineid).getTop()
							+ findViewWithTag(lineid).getHeight() / 2 - getHeight() / 2);
				}
			}
			mLastLineId = lineid;
		}

	}

	public void setLineSelectedListener(OnLineSelectedListener listener) {

		mListener = listener;
	}

	@Override
	public void setSmoothScrollingEnabled(boolean smooth) {

		mSmoothScrolling = smooth;
	}

	public void setTextColor(int color) {

		mTextColor = color;
		setTextContent(mContent);
		setCurrentLine(mLastLineId, true);
	}

	public void setTextContent(Object[] content) {

		mContent = content;
		mLastLineId = -1;
		mContentContainer.removeAllViews();
		System.gc();

		if (content == null || content.length == 0) {
			mContentContainer.setVisibility(View.GONE);
			mContentEmptyView.setVisibility(View.VISIBLE);
			return;
		}

		mContentContainer.setVisibility(View.VISIBLE);
		mContentEmptyView.setVisibility(View.GONE);

		int content_id = 0;

		for (Object line : content) {
			TextView mTextView = new TextView(mContext);
			mTextView.setText(LyricsSplitter.split(String.valueOf(line), mTextView.getTextSize()));
			mTextView.setTextColor(Color.argb(0xD0, Color.red(mTextColor), Color.green(mTextColor),
					Color.blue(mTextColor)));
			float density = getResources().getDisplayMetrics().density;
			mTextView.setShadowLayer(mShadowRadius * density, mShadowDx, mShadowDy, mShadowColor);
			mTextView.setGravity(Gravity.CENTER);
			mTextView.setTextSize(mTextSize);
			mTextView.setOnLongClickListener(this);
			if (content_id < content.length) {
				mTextView.setTag(content_id);
				content_id++;
			}
			mContentContainer.addView(mTextView, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		}
		if (mSmoothScrolling) {
			smoothScrollTo(0, 0);
		} else {
			scrollTo(0, 0);
		}

	}

	public void setTextSize(float size) {

		mTextSize = size;
		setTextContent(mContent);
		setCurrentLine(mLastLineId, true);
	}

	private void init(Context context) {

		mContext = context;

		setVerticalScrollBarEnabled(false);
		mScrollContainer = new LinearLayout(context);
		mScrollContainer.setOrientation(LinearLayout.VERTICAL);
		addView(mScrollContainer, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		mContentContainer = new LinearLayout(context);
		mContentContainer.setOrientation(LinearLayout.VERTICAL);
		mContentContainer.setPadding(0, getHeight() / 2, 0, getHeight() / 2);
		mScrollContainer.addView(mContentContainer, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		mContentEmptyView = (LinearLayout) inflate(context, R.layout.content_empty_view, null);
		mContentEmptyView.setLayoutParams(new LayoutParams(getWidth(), getHeight()));
		mScrollContainer.addView(mContentEmptyView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER));
	}

	private float parseTextSize(String value) {
		float density = getResources().getDisplayMetrics().density;
		if (value == null) throw new IllegalArgumentException("Value cannot be null!");
		if (value.endsWith("px"))
			return Integer.parseInt(value.replaceAll("px", "")) / density;
		else if (value.endsWith("dip"))
			return Float.parseFloat(value.replaceAll("dip", ""));
		else if (value.endsWith("sp"))
			return Float.parseFloat(value.replaceAll("sp", ""));
		else
			throw new IllegalArgumentException("Value " + value + " is not valid!");
	}

	public interface OnLineSelectedListener {

		void onLineSelected(int id);
	}

}
