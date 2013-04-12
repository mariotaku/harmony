package org.mariotaku.harmony.view;

import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.app.HarmonyApplication;
import org.mariotaku.harmony.util.ImageLoaderWrapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public final class ArtistAlbumsStackView extends FrameLayout implements Constants {

	private static final int STACK_ITEM_COUNT = 3;
	private static final Map<Long, String[]> ALBUMS_CACHE = new LinkedHashMap<Long, String[]>(48);

	private ExecutorService mExecutor;
	private final ContentResolver mResolver;
	private final ImageLoaderWrapper mImageLoader;

	private long mArtistId;
	private String[] mArts;

	
	public ArtistAlbumsStackView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ArtistAlbumsStackView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mExecutor = Executors.newFixedThreadPool(8);
		mResolver = context.getContentResolver();
		mImageLoader = HarmonyApplication.getInstance(context).getImageLoaderWrapper();
		for (int i = 0; i < STACK_ITEM_COUNT; i++) {
			final ItemView v = new ItemView(context);
			final int gravity;
			if (i == 0) {
				gravity = Gravity.TOP|Gravity.RIGHT;
			} else if (i == STACK_ITEM_COUNT - 1) {
				gravity = Gravity.BOTTOM|Gravity.LEFT;
			} else {
				gravity = Gravity.CENTER;
			}
			final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, gravity);
			addView(v, lp);
			v.setBackgroundResource(R.drawable.album_item_shadow);
		}
	}
	
	public final long getArtistId() {
		return mArtistId;
	}

	@Override
	public final boolean onInterceptTouchEvent(final MotionEvent event) {
		return false;
	}

	@Override
	public final boolean onTouchEvent(final MotionEvent event) {
		return false;
	}
	
	public synchronized void showAlbums(long id) {
		if (mArts != null && id == mArtistId) return;
		mArtistId = id;
		mArts = null;
		final int child_count = getChildCount();
		for (int i = 0; i < child_count; i++) {
			final ImageView v = (ImageView) getChildAt(i);
			if (v != null) {
				v.setImageDrawable(null);
			}
		}
		if (ALBUMS_CACHE.containsKey(id)) {
			mArts = ALBUMS_CACHE.get(id);
			final int size = Math.min(mArts.length, getChildCount());
			for (int i = 0; i < size; i ++) {
				final ImageView v = (ImageView) getChildAt(size - 1 - i);
				if (v != null) {
					final String art = mArts[i];
					if (!TextUtils.isEmpty(art)) {
						mImageLoader.displayImage(v, art);
					} else {
						v.setImageResource(R.drawable.ic_mp_albumart_unknown);
					}
				}
			}
			return;
		}
		mExecutor.execute(new LoadAlbumsRunnable(this, id));
	}
	
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec), height = MeasureSpec.getSize(heightMeasureSpec);
		final ViewGroup.LayoutParams lp = getLayoutParams();
		if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
			super.onMeasure(heightMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(height, height);
		} else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
			super.onMeasure(widthMeasureSpec, widthMeasureSpec);
			setMeasuredDimension(width, width);
		} else {
			if (width > height) {
				super.onMeasure(heightMeasureSpec, heightMeasureSpec);
				setMeasuredDimension(height, height);
			} else {
				super.onMeasure(widthMeasureSpec, widthMeasureSpec);
				setMeasuredDimension(width, width);
			}
		}
	}

	private static class ItemView extends ImageView {

		private static final float SCALE_FACTOR = 0.9f;

		private ItemView(final Context context) {
			super(context);
		}

		@Override
		protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
			final int width = (int) (MeasureSpec.getSize(widthMeasureSpec) * SCALE_FACTOR), height = (int) (MeasureSpec.getSize(heightMeasureSpec) * SCALE_FACTOR);
			final int wMode = MeasureSpec.getMode(widthMeasureSpec), hMode = MeasureSpec.getMode(heightMeasureSpec);
			final int wSpec = MeasureSpec.makeMeasureSpec(width, wMode), hSpec = MeasureSpec.makeMeasureSpec(height, hMode);
			final ViewGroup.LayoutParams lp = getLayoutParams();
			if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
				super.onMeasure(hSpec, hSpec);
				setMeasuredDimension(height, height);
			} else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
				super.onMeasure(wSpec, wSpec);
				setMeasuredDimension(width, width);
			} else {
				if (width > height) {
					super.onMeasure(hSpec, hSpec);
					setMeasuredDimension(height, height);
				} else {
					super.onMeasure(wSpec, wSpec);
					setMeasuredDimension(width, width);
				}
			}
		}

	}
	
	private static class LoadAlbumsRunnable implements Runnable {

		private final ArtistAlbumsStackView view;		
		private final long id;
		
		private LoadAlbumsRunnable(final ArtistAlbumsStackView view, final long id) {
			this.view = view;
			this.id = id;
		}
		
		@Override
		public void run() {
			final Uri uri = Audio.Artists.Albums.getContentUri(EXTERNAL_VOLUME, id);
			final String[] cols = new String[] { Audio.Albums.ALBUM_ART };
			final String where = Audio.Artists.Albums.ALBUM_ART + " NOT NULL";
			final String order = Audio.Artists.Albums.ALBUM;
			final Cursor c = view.mResolver.query(uri, cols, where, null, order);
			final int count = Math.min(STACK_ITEM_COUNT, c != null ? c.getCount() : -1);
			final String[] arts = new String[STACK_ITEM_COUNT];
			if (count > 0) {
				int idx = c.getColumnIndex(Audio.Artists.Albums.ALBUM_ART);
				c.moveToFirst();
				for (int i = 0; i < STACK_ITEM_COUNT; i++) {
					final String art = c.getString(idx);
					arts[i] = art;
					if (!c.moveToNext()) {
						c.moveToFirst();
					}
				}
			}
			if (c != null) {
				c.close();
			}
			ALBUMS_CACHE.put(id, arts);
			if (id != view.getArtistId()) return;
			view.post(new ShowAlbumsRunnable(view, id));
		}
		
	}
	
	private static class ShowAlbumsRunnable implements Runnable {

		private final ArtistAlbumsStackView view;
		private final long id;
		
		ShowAlbumsRunnable(final ArtistAlbumsStackView view, final long id) {
			this.view = view;
			this.id = id;
		}
		
		@Override
		public void run() {
			// If view was recycled, do nothing.
			if (view.getArtistId() != id) return;
			view.showAlbums(id);
		}
		
	}

}
