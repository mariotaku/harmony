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
import org.mariotaku.harmony.widget.SeparatedListAdapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.mariotaku.harmony.app.TrackBrowserActivity;

public class PlaylistFragment extends ListFragment implements LoaderCallbacks<Cursor>, Constants {

	private PlaylistsAdapter mPlaylistsAdapter;

	private SmartPlaylistsAdapter mSmartPlaylistsAdapter;

	private SeparatedListAdapter mAdapter;
	private Long[] mSmartPlaylists = new Long[] { PLAYLIST_FAVORITES, PLAYLIST_RECENTLY_ADDED,
			PLAYLIST_PODCASTS };

	private int mIdIdx, mNameIdx;

	public PlaylistFragment() {

	}

	public PlaylistFragment(Bundle args) {
		setArguments(args);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);

		mPlaylistsAdapter = new PlaylistsAdapter(getActivity(), null, false);
		mSmartPlaylistsAdapter = new SmartPlaylistsAdapter(getActivity(),
				R.layout.playlist_list_item, mSmartPlaylists);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String[] cols = new String[] { MediaStore.Audio.Playlists._ID,
				MediaStore.Audio.Playlists.NAME };

		Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

		StringBuilder where = new StringBuilder();

		where.append(MediaStore.Audio.Playlists.NAME + " != '" + PLAYLIST_NAME_FAVORITES + "'");
		for (String hide_playlist : HIDE_PLAYLISTS) {
			where.append(" AND " + MediaStore.Audio.Playlists.NAME + " != '" + hide_playlist + "'");
		}

		return new CursorLoader(getActivity(), uri, cols, where.toString(), null,
				MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.playlists_browser, container, false);
		return view;
	}

	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) {

		long playlist_id = (Long) ((Object[]) view.getTag())[1];

		showDetails(position, playlist_id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mPlaylistsAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		if (data == null) {
			getActivity().finish();
			return;
		}

		mIdIdx = data.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);
		mNameIdx = data.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);

		mPlaylistsAdapter.changeCursor(data);

		mAdapter = new SeparatedListAdapter(getActivity());
		mAdapter.addSection(getString(R.string.my_playlists), mPlaylistsAdapter);
		mAdapter.addSection(getString(R.string.smart_playlists), mSmartPlaylistsAdapter);

		setListAdapter(mAdapter);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(getArguments() != null ? getArguments() : new Bundle());
		super.onSaveInstanceState(outState);
	}

	private void showDetails(int index, long id) {


		long playlist_id = id;

		Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_TYPE, MediaStore.Audio.Playlists.CONTENT_TYPE);
		bundle.putLong(MediaStore.Audio.Playlists._ID, playlist_id);


			Intent intent = new Intent(getActivity(), TrackBrowserActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
	}

	private class PlaylistsAdapter extends CursorAdapter {

		private PlaylistsAdapter(Context context, Cursor cursor, boolean autoRequery) {
			super(context, cursor, autoRequery);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder viewholder = (ViewHolder) ((Object[]) view.getTag())[0];

			String playlist_name = cursor.getString(mNameIdx);
			viewholder.playlist_name.setText(playlist_name);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = LayoutInflater.from(context).inflate(R.layout.playlist_list_item, null);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(new Object[] { viewholder, cursor.getLong(mIdIdx) });
			return view;
		}

		private class ViewHolder {

			TextView playlist_name;

			public ViewHolder(View view) {
				playlist_name = (TextView) view.findViewById(R.id.playlist_name);
			}
		}

	}

	private class SmartPlaylistsAdapter extends ArrayAdapter<Long> {

		Long[] playlists = new Long[] {};
		LayoutInflater inflater;

		private SmartPlaylistsAdapter(Context context, int resid, Long[] playlists) {
			super(context, resid, playlists);
			this.playlists = playlists;
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			ViewHolder viewholder = view != null ? (ViewHolder) ((Object[]) view.getTag())[0]
					: null;

			if (viewholder == null) {
				view = inflater.inflate(R.layout.playlist_list_item, null);
				viewholder = new ViewHolder(view);
				view.setTag(new Object[] { viewholder, playlists[position] });
			}

			switch (playlists[position].intValue()) {
				case (int) PLAYLIST_FAVORITES:
					viewholder.playlist_name.setText(R.string.favorites);
					viewholder.playlist_name.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.ic_mp_list_playlist_favorite, 0, 0, 0);
					break;
				case (int) PLAYLIST_RECENTLY_ADDED:
					viewholder.playlist_name.setText(R.string.recently_added);
					viewholder.playlist_name.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.ic_mp_list_playlist_recent, 0, 0, 0);
					break;
				case (int) PLAYLIST_PODCASTS:
					viewholder.playlist_name.setText(R.string.podcasts);
					viewholder.playlist_name.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.ic_mp_list_playlist_podcast, 0, 0, 0);
					break;
			}

			return view;

		}

		private class ViewHolder {

			TextView playlist_name;

			public ViewHolder(View view) {
				playlist_name = (TextView) view.findViewById(R.id.playlist_name);
			}
		}
	}

}
