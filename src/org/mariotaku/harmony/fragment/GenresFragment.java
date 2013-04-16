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
import org.mariotaku.harmony.activity.TracksBrowserActivity;
import org.mariotaku.harmony.loader.GenresLoader;
import org.mariotaku.harmony.adapter.ArrayAdapter;
import org.mariotaku.harmony.model.GenreInfo;
import org.mariotaku.harmony.util.ListUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import org.mariotaku.harmony.view.holder.BaseGridViewHolder;

public class GenresFragment extends BaseFragment implements LoaderCallbacks<GenreInfo[]>, AdapterView.OnItemClickListener {

	private GenresAdapter mAdapter;
	private GridView mGridView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new GenresAdapter(getActivity());
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<GenreInfo[]> onCreateLoader(int id, Bundle args) {
		return new GenresLoader(getActivity(), null, null, Audio.Genres.NAME);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.base_grid_view, container, false);
		mGridView = (GridView) view.findViewById(android.R.id.list);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> view, View child, int position, long id) {
		final GenreInfo genre = mAdapter.getItem(position);
		final Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME_HARMONY_TRACKS);
		builder.authority(AUTHORITY_GENRES);
		builder.appendPath(genre.getName());
		final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
		intent.setClass(getActivity(), TracksBrowserActivity.class);
		startActivity(intent);
	}

	@Override
	public void onLoaderReset(Loader<GenreInfo[]> loader) {
		mAdapter.clear();
	}

	@Override
	public void onLoadFinished(Loader<GenreInfo[]> loader, GenreInfo[] data) {
		mAdapter.setData(data);
	}

	private static class GenresAdapter extends ArrayAdapter<GenreInfo> {

		private GenresAdapter(Context context) {
			super(context, R.layout.base_grid_item);
		}

		public void setData(GenreInfo[] data) {
			clear();
			addAll(ListUtils.fromArray(data));
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final BaseGridViewHolder tag = (BaseGridViewHolder) view.getTag();
			final BaseGridViewHolder holder = tag != null ? tag : new BaseGridViewHolder(view);
			if (tag == null) {
				view.setTag(holder);
			}			
			final GenreInfo line = getItem(position);
			holder.text1.setText(line.getName());
			holder.icon.setImageResource(R.drawable.ic_mp_albumart_unknown);
			return view;
		}

	}

}
