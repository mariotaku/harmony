package org.mariotaku.harmony.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.model.AlbumInfo;
import org.mariotaku.harmony.model.ArtistAlbumsCursor;
import org.mariotaku.harmony.model.ArtistInfo;
import org.mariotaku.harmony.util.MusicUtils;

public final class ArtistsAdapter extends SimpleCursorTreeAdapter implements Constants {

	private final ExpandableListView mListView;
	private final ExpandableListView.OnChildClickListener mChildClickListener;
	private final OnChildLongClickListener mChildLongClickListener;
	private final ContentResolver mResolver;
	private final int mGridViewSpacing, mColumnWidth;

	private int mArtistIdIdx, mArtistIdx, mAlbumNumberIdx, mTrackNumberIdx;
	private long mCurrentArtistId, mCurrentAlbumId;

	private final Context mContext;

	public ArtistsAdapter(final Context context, final ExpandableListView listView, ExpandableListView.OnChildClickListener childClickListener, 
			final OnChildLongClickListener childLongClickListener) {
		super(context, null, R.layout.artist_list_item_group, new String[0], new int[0],
			  R.layout.artist_list_item_child, new String[0], new int[0]);
		mContext = context;
		mListView = listView;
		mChildClickListener = childClickListener;
		mChildLongClickListener = childLongClickListener;
		mResolver = context.getContentResolver();
		final Resources res = context.getResources();
		mGridViewSpacing = res.getDimensionPixelSize(R.dimen.default_element_spacing);
		mColumnWidth = res.getDimensionPixelSize(R.dimen.gridview_item_width);
	}

	public void setCurrentAlbumId(final long album_id) {
		if (mCurrentAlbumId == album_id) return;
		mCurrentAlbumId = album_id;
		notifyDataSetChanged();
	}

	public void setCurrentArtistId(final long artist_id) {
		if (mCurrentArtistId == artist_id) return;
		mCurrentArtistId = artist_id;
		notifyDataSetChanged();
	}

	@Override
	public void bindChildView(View view, Context context, final Cursor cursor, boolean isLastChild) {
		final GridView gridview = (GridView) view;
		final Bundle extras = cursor.getExtras();
		final int groupPos = extras.getInt(EXTRA_GROUP_POSITION);
		final AlbumsAdapter adapter = new AlbumsAdapter(context, cursor);
		adapter.setCurrentAlbumId(mCurrentAlbumId);
		gridview.setAdapter(adapter);
		final OnItemClickListenerImpl listener = new OnItemClickListenerImpl(mListView, mChildClickListener, mChildLongClickListener, groupPos);
		gridview.setOnItemClickListener(listener);
		gridview.setOnItemLongClickListener(listener);
	}

	@Override
	public void bindGroupView(View view, Context context, Cursor cursor, boolean isexpanded) {
		final ViewHolderGroup viewholder = (ViewHolderGroup) view.getTag();
		final String artist = cursor.getString(mArtistIdx);
		boolean unknown = TextUtils.isEmpty(artist) || MediaStore.UNKNOWN_STRING.equals(artist);
		if (unknown) {
			viewholder.artist_name.setText(R.string.unknown_artist);
		} else {
			viewholder.artist_name.setText(artist);
		}

		final int numalbums = cursor.getInt(mAlbumNumberIdx);
		final int numsongs = cursor.getInt(mTrackNumberIdx);

		final String songs_albums = MusicUtils.makeAlbumsLabel(context, numalbums, numsongs, unknown);

		viewholder.album_track_count.setText(songs_albums);

		if (mCurrentArtistId == cursor.getLong(mArtistIdIdx)) {
			viewholder.artist_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_nowplaying_small, 0);
		} else {
			viewholder.artist_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor == null) return;
		mArtistIdIdx = cursor.getColumnIndex(Audio.Artists._ID);
		mArtistIdx = cursor.getColumnIndex(Audio.Artists.ARTIST);
		mAlbumNumberIdx = cursor.getColumnIndex(Audio.Artists.NUMBER_OF_ALBUMS);
		mTrackNumberIdx = cursor.getColumnIndex(Audio.Artists.NUMBER_OF_TRACKS);
	}

	public AlbumInfo getAlbumInfo(final int groupPos, final int childPos) {
		if (groupPos < 0 || groupPos >= getGroupCount() || childPos < 0) return null;
		final Cursor c = getChildrenCursor(getGroup(groupPos));
		try { 
			if (c == null || childPos >= c.getCount()) return null;
			c.moveToPosition(childPos);
			return new AlbumInfo(c);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public ArtistInfo getArtistInfo(int groupPos) {
		if (groupPos < 0 || groupPos >= getGroupCount()) return null;
		final Cursor c = getGroup(groupPos);
		if (c == null) return null;
		return new ArtistInfo(c);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public View newGroupView(final Context context, final Cursor cursor, final boolean isExpanded, final ViewGroup parent) {
		final View view = super.newGroupView(context, cursor, isExpanded, parent);
		view.setTag(new ViewHolderGroup(view));
		return view;
	}

	@Override
	protected Cursor getChildrenCursor(final Cursor groupCursor) {
		return ArtistAlbumsCursor.getInstance(mContext, groupCursor);
	}

	public static interface OnChildLongClickListener {
		boolean onChildLongClick(ExpandableListView listView, View view, int groupPos, int childPos, long id);
	}
	
	private static class ViewHolderGroup {

		final TextView artist_name;
		final TextView album_track_count;

		public ViewHolderGroup(View view) {
			artist_name = (TextView) view.findViewById(R.id.name);
			album_track_count = (TextView) view.findViewById(R.id.summary);
		}
	}
	
	private static class OnItemClickListenerImpl implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
		
		private final ExpandableListView mView;
		private final ExpandableListView.OnChildClickListener mChildClickListener;
		private final OnChildLongClickListener mChildLongClickListener;
		private final int mGroupPos;
		
		OnItemClickListenerImpl(ExpandableListView view, ExpandableListView.OnChildClickListener childClickListener, OnChildLongClickListener childLongClickListener, final int groupPos) {
			mView = view;
			mChildClickListener = childClickListener;
			mChildLongClickListener = childLongClickListener;
			mGroupPos = groupPos;
		}
		
		@Override
		public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
			if (mChildClickListener == null) return;
			mChildClickListener.onChildClick(mView, child, mGroupPos, position, id);
		}

		@Override
		public boolean onItemLongClick(final AdapterView<?> view, final View child, final int position, final long id) {
			if (mChildClickListener == null) return false;
			return mChildLongClickListener.onChildLongClick(mView, child, mGroupPos, position, id);
		}
	}

}
