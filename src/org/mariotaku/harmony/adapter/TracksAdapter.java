package org.mariotaku.harmony.adapter;

import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.MusicUtils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TracksAdapter extends SimpleCursorAdapter {

	private int mIdIdx, mTrackIdx, mAlbumIdx, mArtistIdx, mDurationIdx;

	private long mCurrentTrackId;

	public TracksAdapter(final Context context) {
		super(context, R.layout.track_list_item, null, new String[0], new int[0], 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder viewholder = (ViewHolder) view.getTag();

		final String track = cursor.getString(mTrackIdx);
		viewholder.track.setText(track);

		final String artist = cursor.getString(mArtistIdx);
		if (artist == null || MediaStore.UNKNOWN_STRING.equals(artist)) {
			viewholder.artist.setText(R.string.unknown_artist);
		} else {
			viewholder.artist.setText(artist);
		}

		long secs = cursor.getLong(mDurationIdx) / 1000;

		if (secs <= 0) {
			viewholder.duration.setText(null);
		} else {
			viewholder.duration.setText(MusicUtils.makeTimeString(context, secs));
		}

		final long audio_id = cursor.getLong(mIdIdx);

		if (mCurrentTrackId == audio_id) {
			viewholder.track.setCompoundDrawablesWithIntrinsicBounds(0, 0,
																	 R.drawable.ic_indicator_nowplaying_small, 0);
		} else {
			viewholder.track.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}

	}

	@Override
	public void changeCursor(final Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor == null) return;
		mIdIdx = cursor.getColumnIndexOrThrow(Audio.AudioColumns._ID);
		mTrackIdx = cursor.getColumnIndexOrThrow(Audio.AudioColumns.TITLE);
		mAlbumIdx = cursor.getColumnIndexOrThrow(Audio.AudioColumns.ALBUM);
		mArtistIdx = cursor.getColumnIndexOrThrow(Audio.AudioColumns.ARTIST);
		mDurationIdx = cursor.getColumnIndexOrThrow(Audio.AudioColumns.DURATION);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		view.setTag(new ViewHolder(view));
		return view;
	}

	public void setCurrentTrackId(final long track_id) {
		if (mCurrentTrackId == track_id) return;
		mCurrentTrackId = track_id;
		notifyDataSetChanged();
	}

	private static class ViewHolder {

		final TextView track, artist, duration;

		ViewHolder(View view) {
			track = (TextView) view.findViewById(R.id.name);
			artist = (TextView) view.findViewById(R.id.summary);
			duration = (TextView) view.findViewById(R.id.duration);
		}

	}

}

