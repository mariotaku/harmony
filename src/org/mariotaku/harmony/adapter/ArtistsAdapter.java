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
import org.mariotaku.harmony.view.holder.ArtistViewHolder;
import org.mariotaku.harmony.app.HarmonyApplication;
import org.mariotaku.harmony.model.AlbumInfo;
import android.content.res.Resources;

public class ArtistsAdapter extends SimpleCursorAdapter {

	private int mIdIdx, mArtistIdx, mAlbumsCountIdx;
	private long mCurrentArtistId = -1;

	private final Resources mResources;

	public ArtistsAdapter(final Context context) {
		super(context, R.layout.artist_grid_item, null, new String[0], new int[0], 0);
		mResources = context.getResources();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ArtistViewHolder holder = (ArtistViewHolder) view.getTag();
		final String artist = cursor.getString(mArtistIdx);
		final boolean unknown_artist = TextUtils.isEmpty(artist) || MediaStore.UNKNOWN_STRING.equals(artist);
		holder.artist.setText(unknown_artist ? context.getString(R.string.unknown_artist) : artist);
		final long id = cursor.getLong(mIdIdx);
		if (mCurrentArtistId == id) {
			holder.artist.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_nowplaying_small, 0);
		} else {
			holder.artist.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		final int albums_count = cursor.getInt(mAlbumsCountIdx);
		if (albums_count > 0) {
			holder.albums_count.setText(mResources.getQuantityString(R.plurals.Nalbums, albums_count, albums_count));
		} else {
			holder.albums_count.setText(null);
		}
		holder.albums.showAlbums(id);
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		super.changeCursor(cursor);
		setCursorIndices(cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		view.setTag(new ArtistViewHolder(view));
		return view;
	}

	public void setCurrentArtistId(final long album_id) {
		if (mCurrentArtistId == album_id) return;
		mCurrentArtistId = album_id;
		notifyDataSetChanged();
	}

	private void setCursorIndices(Cursor cursor) {
		if (cursor == null) return;
		mIdIdx = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
		mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		mAlbumsCountIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
	}

}

