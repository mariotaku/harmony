package org.mariotaku.harmony.view;

import org.mariotaku.harmony.R;
import org.mariotaku.harmony.model.AlbumInfo;
import org.mariotaku.harmony.model.TrackInfo;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

public class AlbumArtView extends ImageSwitcher implements ViewFactory {

	private static final int IMAGE_VIEWS_COUNT = 2;

	private final ImageView[] mImageViews = new ImageView[IMAGE_VIEWS_COUNT];
	private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);
	
	private int mImageIndex;
	private TrackInfo mTrackInfo;

	public AlbumArtView(final Context context) {
		this(context, null);
	}

	public AlbumArtView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		for (int i = 0; i < IMAGE_VIEWS_COUNT; i++) {
			final ImageView view = new ImageView(context);
			view.setScaleType(ImageView.ScaleType.CENTER_CROP);
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			mImageViews[i] = view;
		}
		setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
		setOutAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_out));
		setFactory(this);
	}

	public TrackInfo getTrackInfo() {
		return mTrackInfo;
	}

	public void loadAlbumArt(final TrackInfo track) {
		final TrackInfo old = mTrackInfo;
		mTrackInfo = track;
		if (track == null) {
			setImageDrawable(null);
			return;
		}
		mExecutor.execute(new LoadAlbumArtRunnable(this, track));
	}	

	@Override
	public View makeView() {
		if (mImageIndex >= mImageViews.length) {
			mImageIndex = 0;
		}
		final ImageView view = mImageViews[mImageIndex];
		mImageIndex++;
		return view;
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

	static class LoadAlbumArtRunnable implements Runnable {

		private final AlbumArtView view;
		private final TrackInfo track;
		private final int mSize;
		private final Resources res;
		private final Context context;
		
		LoadAlbumArtRunnable(AlbumArtView view, TrackInfo track) {
			this.view = view;
			this.track = track;
			this.context = view.getContext();
			this.res = context.getResources();
			view.measure(0, 0);
			final int size = Math.max(view.getMeasuredWidth(), view.getMeasuredHeight());
			if (size > 0) {
				mSize = size;
			} else {
				final DisplayMetrics metrics = res.getDisplayMetrics();
				mSize = Math.max(metrics.widthPixels, metrics.heightPixels);
			}
		}
		
		@Override
		public void run() {
			if (track == null || track.data == null) {
				view.post(new DisplayAlbumArtRunnable(view, null));
				return;
			}
			// first try to load AlbumArt.jpg
			if (track.data.startsWith("/")) {
				final File data = new File(track.data);
				final String path = new File(data.getParentFile(), "AlbumArt.jpg").getAbsolutePath();
				final Bitmap bitmap = decodeBitmap(path);
				if (bitmap != null) {
					view.post(new DisplayAlbumArtRunnable(view, new BitmapDrawable(res, bitmap)));
					return;
				}
				// couldn't load AlbumArt.jpg, so load from MediaStore
			}
			final AlbumInfo album = AlbumInfo.getAlbumInfo(context, track);
			if (album != null) {
			final Bitmap bitmap = decodeBitmap(album.album_art);
				if (bitmap != null) {
					view.post(new DisplayAlbumArtRunnable(view, new BitmapDrawable(res, bitmap)));
					return;
				}
			}
			// no album art available.
			view.post(new DisplayAlbumArtRunnable(view, null));
		}
		
		private Bitmap decodeBitmap(final String path) {
			if (path == null) return null;
			final BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);
			final float bmp_size = Math.max(opts.outWidth, opts.outHeight);
			if (bmp_size == 0) return null;
			opts.inSampleSize = (int) Math.floor(bmp_size / mSize);
			opts.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(path, opts);			
		}
	}

	static class DisplayAlbumArtRunnable implements Runnable {

		private final AlbumArtView view;
		private final BitmapDrawable drawable;
		
		DisplayAlbumArtRunnable(final AlbumArtView view, final BitmapDrawable drawable) {
			this.view = view;
			this.drawable = drawable;
		}

		@Override
		public void run() {
			if (drawable != null) {
				view.setImageDrawable(drawable);
			} else {
				view.setImageResource(R.drawable.ic_mp_albumart_unknown);
			}
		}		

	}
}
