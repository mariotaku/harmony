package org.mariotaku.harmony.view;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.StackView;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.adapter.ArrayAdapter;
import org.mariotaku.harmony.app.HarmonyApplication;
import org.mariotaku.harmony.util.ImageLoaderWrapper;
import org.mariotaku.harmony.util.ListUtils;

public class ArtistAlbumsStackView extends StackView implements Constants {

	private static final int STACK_ITEM_COUNT = 3;
	private static final Map<Long, String[]> ALBUMS_CACHE = new LinkedHashMap<Long, String[]>(48);

	private ExecutorService mExecutor = Executors.newFixedThreadPool(8);

	private long mArtistId;

	private final AlbumArtsAdapter mAdapter;	
	private final ContentResolver mResolver;
	
	public ArtistAlbumsStackView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ArtistAlbumsStackView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mResolver = context.getContentResolver();
		setAdapter(mAdapter = new AlbumArtsAdapter(context));
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
		if (!mAdapter.isEmpty() && id == mArtistId) return;
		mArtistId = id;
		mAdapter.clear();
		final int child_count = getChildCount();
		for (int i = 0; i < child_count; i++) {
			final ImageView v = (ImageView) getChildAt(i).findViewById(android.R.id.icon);
			if (v != null) {
				v.setImageDrawable(null);
			}
		}
		if (ALBUMS_CACHE.containsKey(id)) {
			mAdapter.addAll(ListUtils.fromArray(ALBUMS_CACHE.get(id)));
			return;
		}
		mExecutor.execute(new LoadAlbumsRunnable(id));
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
	
	class LoadAlbumsRunnable implements Runnable {

		final long id;
		
		LoadAlbumsRunnable(long id) {
			this.id = id;
		}
		
		public void run() {
			final Uri uri = Audio.Artists.Albums.getContentUri(EXTERNAL_VOLUME, id);
			final String[] cols = new String[] { Audio.Albums.ALBUM_ART };
			final String where = Audio.Artists.Albums.ALBUM_ART + " NOT NULL";
			final String order = Audio.Artists.Albums.ALBUM;
			final Cursor c = mResolver.query(uri, cols, where, null, order);
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
			if (id == mArtistId) {
				post(new AddAlbumArtRunnable(id));
			}
		}
		
		
	}
	
	class AddAlbumArtRunnable implements Runnable {

		final long id;
		
		AddAlbumArtRunnable(long id) {
			this.id = id;
		}
		
		@Override
		public void run() {
			if (mArtistId == id) {
				showAlbums(id);
			}
		}
		
	}
	
	private static class AlbumArtsAdapter extends ArrayAdapter<String> {

		private ImageLoaderWrapper mImageLoader;
		AlbumArtsAdapter(Context context) {
			super(context, R.layout.artist_albums_stack_item);
			mImageLoader = HarmonyApplication.getInstance(context).getImageLoaderWrapper();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final ImageView image = (ImageView) view.findViewById(android.R.id.icon);
			final String art = position >= 0 && position < getCount() ? getItem(position) : null;
			if (!TextUtils.isEmpty(art)) {
				mImageLoader.displayImage(image, art);
			} else {
				image.setImageResource(R.drawable.ic_mp_albumart_unknown);
			}
			return view;
		}
		
	}
}
