/*
 *  YAMMP - Yet Another Multi Media Player for android
 *  Copyright (C) 2011-2012  Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This file is part of YAMMP.
 *
 *  YAMMP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAMMP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAMMP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.harmony.fragment;

import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.MusicUtils;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class QueryFragment extends ListFragment implements Constants, LoaderCallbacks<Cursor> {

	private QueryListAdapter mAdapter;

	private String mFilterString = "";
	private Cursor mQueryCursor;
	private ListView mTrackList;

	public QueryFragment() {

	}

	public QueryFragment(Bundle arguments) {
		setArguments(arguments);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		mAdapter = new QueryListAdapter(getActivity(), R.layout.query_list_item, null,
				new String[] {}, new int[] {}, 0);

		setListAdapter(mAdapter);

		getListView().setOnCreateContextMenuListener(this);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, getArguments(), this);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String filter = "";

		if (args != null) {
			filter = args.getString(INTENT_KEY_FILTER) != null ? args.getString(INTENT_KEY_FILTER)
					: "";
		}

		StringBuilder where = new StringBuilder();

		where.append(Audio.Media.IS_MUSIC + "=1");
		where.append(" AND " + Audio.Media.TITLE + " != ''");

		String[] cols = new String[] { BaseColumns._ID, Audio.Media.MIME_TYPE,
				Audio.Artists.ARTIST, Audio.Albums.ALBUM, Audio.Media.TITLE, "data1", "data2" };

		Uri uri = Uri.parse("content://media/external/audio/search/fancy/" + Uri.encode(filter));

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), uri, cols, where.toString(), null, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.query_browser, container, false);
		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// Dialog doesn't allow us to wait for a result, so we need to store
		// the info we need for when the dialog posts its result
		mQueryCursor.moveToPosition(position);
		if (mQueryCursor.isBeforeFirst() || mQueryCursor.isAfterLast()) return;
		String selectedType = mQueryCursor.getString(mQueryCursor
				.getColumnIndexOrThrow(Audio.Media.MIME_TYPE));

		if ("artist".equals(selectedType)) {
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/album");
			intent.putExtra("artist", Long.valueOf(id).toString());
			startActivity(intent);
		} else if ("album".equals(selectedType)) {
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
			intent.putExtra("album", Long.valueOf(id).toString());
			startActivity(intent);
		} else if (position >= 0 && id >= 0) {
			long[] list = new long[] { id };
			MusicUtils.playAll(getActivity(), list, 0);
		} else {
			Log.e("QueryBrowser", "invalid position/id: " + position + "/" + id);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		if (data == null) {
			getActivity().finish();
			return;
		}

		mQueryCursor = data;
		mAdapter.swapCursor(data);

	}

	public void onServiceConnected(ComponentName name, IBinder service) {

		Bundle bundle = getArguments();

		String action = bundle != null ? bundle.getString(INTENT_KEY_ACTION) : null;
		String data = bundle != null ? bundle.getString(INTENT_KEY_DATA) : null;

		if (Intent.ACTION_VIEW.equals(action)) {
			// this is something we got from the search bar
			Uri uri = Uri.parse(data);
			if (data.startsWith("content://media/external/audio/media/")) {
				// This is a specific file
				String id = uri.getLastPathSegment();
				long[] list = new long[] { Long.valueOf(id) };
				MusicUtils.playAll(getActivity(), list, 0);
				getActivity().finish();
				return;
			} else if (data.startsWith("content://media/external/audio/albums/")) {
				// This is an album, show the songs on it
				Intent i = new Intent(Intent.ACTION_PICK);
				i.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
				i.putExtra("album", uri.getLastPathSegment());
				startActivity(i);
				return;
			} else if (data.startsWith("content://media/external/audio/artists/")) {
				// This is an artist, show the albums for that artist
				Intent i = new Intent(Intent.ACTION_PICK);
				i.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/album");
				i.putExtra("artist", uri.getLastPathSegment());
				startActivity(i);
				return;
			}
		}

		mFilterString = bundle != null ? bundle.getString(SearchManager.QUERY) : null;
		if (MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)) {
			String focus = bundle != null ? bundle.getString(MediaStore.EXTRA_MEDIA_FOCUS) : null;
			String artist = bundle != null ? bundle.getString(MediaStore.EXTRA_MEDIA_ARTIST) : null;
			String album = bundle != null ? bundle.getString(MediaStore.EXTRA_MEDIA_ALBUM) : null;
			String title = bundle != null ? bundle.getString(MediaStore.EXTRA_MEDIA_TITLE) : null;
			if (focus != null) {
				if (focus.startsWith("audio/") && title != null) {
					mFilterString = title;
				} else if (Audio.Albums.ENTRY_CONTENT_TYPE.equals(focus)) {
					if (album != null) {
						mFilterString = album;
						if (artist != null) {
							mFilterString = mFilterString + " " + artist;
						}
					}
				} else if (Audio.Artists.ENTRY_CONTENT_TYPE.equals(focus)) {
					if (artist != null) {
						mFilterString = artist;
					}
				}
			}
		}

		mTrackList = getListView();
		mTrackList.setTextFilterEnabled(true);
	}

	public void onServiceDisconnected(ComponentName name) {

	}

	private class QueryListAdapter extends SimpleCursorAdapter {

		private QueryListAdapter(Context context, int layout, Cursor cursor, String[] from,
				int[] to, int flags) {
			super(context, layout, cursor, from, to, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder viewholder = (ViewHolder) view.getTag();

			String mimetype = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.MIME_TYPE));

			if (mimetype == null) {
				mimetype = "audio/";
			}
			if (mimetype.equals("artist")) {
				viewholder.result_icon.setImageResource(R.drawable.ic_mp_list_artist);
				String name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Artists.ARTIST));
				String displayname = name;
				boolean isunknown = false;
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown_artist);
					isunknown = true;
				}
				viewholder.query_result.setText(displayname);

				int numalbums = cursor.getInt(cursor.getColumnIndexOrThrow("data1"));
				int numsongs = cursor.getInt(cursor.getColumnIndexOrThrow("data2"));

				String songs_albums = MusicUtils.makeAlbumsSongsLabel(context, numalbums, numsongs,
						isunknown);

				viewholder.result_summary.setText(songs_albums);

			} else if (mimetype.equals("album")) {
				viewholder.result_icon.setImageResource(R.drawable.ic_mp_list_album);
				String name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Albums.ALBUM));
				String displayname = name;
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown_album);
				}
				viewholder.query_result.setText(displayname);

				name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Artists.ARTIST));
				displayname = name;
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown_artist);
				}
				viewholder.result_summary.setText(displayname);

			} else if (mimetype.startsWith("audio/") || mimetype.equals("application/ogg")
					|| mimetype.equals("application/x-ogg")) {
				viewholder.result_icon.setImageResource(R.drawable.ic_mp_list_song);
				String name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE));
				viewholder.query_result.setText(name);

				String displayname = cursor.getString(cursor
						.getColumnIndexOrThrow(Audio.Artists.ARTIST));
				if (displayname == null || displayname.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown_artist);
				}
				name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Albums.ALBUM));
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					name = context.getString(R.string.unknown_album);
				}
				viewholder.result_summary.setText(displayname + " - " + name);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = super.newView(context, cursor, parent);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}

		private class ViewHolder {

			ImageView result_icon;
			TextView query_result;
			TextView result_summary;

			public ViewHolder(View view) {
				result_icon = (ImageView) view.findViewById(R.id.icon);
				query_result = (TextView) view.findViewById(R.id.name);
				result_summary = (TextView) view.findViewById(R.id.summary);
			}

		}

	}

}
