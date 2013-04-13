package org.mariotaku.harmony.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.ImageLoaderWrapper;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.view.holder.AlbumViewHolder;
import org.mariotaku.harmony.app.HarmonyApplication;
import org.mariotaku.harmony.model.AlbumInfo;

public class AlbumsAdapter extends SimpleCursorAdapter {

	private final ImageLoaderWrapper mImageLoader;		
	private int mIdIdx, mAlbumIdx, mArtistIdx, mAlbumArtIdx;
	private long mCurrentAlbumId = -1;

	public AlbumsAdapter(final Context context) {
		this(context, null);
	}
	
	public AlbumsAdapter(final Context context, final Cursor cursor) {
		super(context, R.layout.album_grid_item, cursor, new String[0], new int[0], 0);
		setCursorIndices(cursor);
		mImageLoader = HarmonyApplication.getInstance(context).getImageLoaderWrapper();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final AlbumViewHolder viewholder = (AlbumViewHolder) view.getTag();
		final String album = cursor.getString(mAlbumIdx), artist = cursor.getString(mArtistIdx);
		final boolean unknown_album = TextUtils.isEmpty(album) || MediaStore.UNKNOWN_STRING.equals(album);
		final boolean unknown_artist = TextUtils.isEmpty(artist) || MediaStore.UNKNOWN_STRING.equals(artist);
		viewholder.album.setText(unknown_album ? context.getString(R.string.unknown_album) : album);
		viewholder.artist.setText(unknown_artist ? context.getString(R.string.unknown_artist) : artist);
		if (mCurrentAlbumId >=0 && mCurrentAlbumId == cursor.getLong(mIdIdx)) {
			viewholder.album.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_nowplaying_small, 0);
		} else {
			viewholder.album.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		final String album_art = cursor.getString(mAlbumArtIdx);
		if (!TextUtils.isEmpty(album_art)) {
			mImageLoader.displayImage(viewholder.album_art, album_art);
		} else {
			viewholder.album_art.setImageResource(R.drawable.ic_mp_albumart_unknown);
		}
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		super.changeCursor(cursor);
		setCursorIndices(cursor);
	}
	
	public AlbumInfo getAlbumInfo(final int position) {
		if (position < 0 || position >= getCount()) return null;
		final Cursor c = (Cursor) getItem(position);
		if (c == null) return null;
		return new AlbumInfo(c);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		view.setTag(new AlbumViewHolder(view));
		return view;
	}

	public void setCurrentAlbumId(final long album_id) {
		if (mCurrentAlbumId == album_id) return;
		mCurrentAlbumId = album_id;
		notifyDataSetChanged();
	}

	private void setCursorIndices(Cursor cursor) {
		if (cursor == null) return;
		mIdIdx = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
		mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
		mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
		mAlbumArtIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
	}

}

