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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import org.mariotaku.harmony.app.TrackBrowserActivity;

public class GenresFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, Constants {

	private GenresAdapter mAdapter;
	private int mNameIdx;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);

		mAdapter = new GenresAdapter(getActivity(), null, false);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String[] cols = new String[] { Audio.Genres._ID, Audio.Genres.NAME };

		String where = MusicUtils.getBetterGenresWhereClause(getActivity());

		Uri uri = Audio.Genres.EXTERNAL_CONTENT_URI;

		return new CursorLoader(getActivity(), uri, cols, where, null,
				Audio.Genres.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) {
		showDetails(position, id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		mNameIdx = data.getColumnIndexOrThrow(Audio.Genres.NAME);
		mAdapter.changeCursor(data);
		setListAdapter(mAdapter);
	}

	private void showDetails(int index, long id) {


		Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_TYPE, Audio.Genres.CONTENT_TYPE);
		bundle.putLong(Audio.Genres._ID, id);


			Intent intent = new Intent(getActivity(), TrackBrowserActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
	}

	private class GenresAdapter extends CursorAdapter {

		private GenresAdapter(Context context, Cursor cursor, boolean autoRequery) {
			super(context, cursor, autoRequery);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder viewholder = (ViewHolder) view.getTag();

			String genre_name = cursor.getString(mNameIdx);
			viewholder.genre_name.setText(MusicUtils.parseGenreName(genre_name));

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = LayoutInflater.from(context).inflate(R.layout.playlist_list_item, null);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}

		private class ViewHolder {

			TextView genre_name;

			public ViewHolder(View view) {
				genre_name = (TextView) view.findViewById(R.id.playlist_name);
			}
		}

	}

}
