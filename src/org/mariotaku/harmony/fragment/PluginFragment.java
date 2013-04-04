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

import java.util.List;

import org.mariotaku.harmony.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PluginFragment extends ListFragment implements LoaderCallbacks<List<ApplicationInfo>> {

	private static PackageManager mPackageManager;

	private PluginAdapter mAdapter;

	private List<ApplicationInfo> mPluginsList;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<List<ApplicationInfo>> onCreateLoader(int id, Bundle args) {
		return new AppListLoader(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.plugins_manager, container, false);
		return view;
	}

	@Override
	public void onLoaderReset(Loader<List<ApplicationInfo>> loader) {

	}

	@Override
	public void onLoadFinished(Loader<List<ApplicationInfo>> loader, List<ApplicationInfo> data) {
		mAdapter = new PluginAdapter(getActivity(), R.layout.playlist_list_item, data);
		setListAdapter(mAdapter);

	}

	public static class AppListLoader extends AsyncTaskLoader<List<ApplicationInfo>> {

		public AppListLoader(Context context) {
			super(context);
			mPackageManager = context.getPackageManager();
		}

		@Override
		public List<ApplicationInfo> loadInBackground() {
			return mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
					| PackageManager.GET_DISABLED_COMPONENTS);
		}

	}

	private class PluginAdapter extends ArrayAdapter<ApplicationInfo> {

		private List<ApplicationInfo> mList;

		private LayoutInflater inflater;

		public PluginAdapter(Context context, int resource, List<ApplicationInfo> objects) {
			super(context, resource, objects);
			this.inflater = LayoutInflater.from(context);
			mList = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder viewholder = view != null ? (ViewHolder) view.getTag() : null;

			if (viewholder == null) {
				view = inflater.inflate(R.layout.playlist_list_item, null);
				viewholder = new ViewHolder(view);
				view.setTag(viewholder);
			}

			viewholder.plugin_icon.setImageDrawable(mList.get(position).loadIcon(mPackageManager));
			viewholder.plugin_name.setText(mList.get(position).loadLabel(mPackageManager));
			viewholder.plugin_description.setText(mList.get(position).loadDescription(
					mPackageManager));

			return view;
		}

		private class ViewHolder {

			ImageView plugin_icon;
			TextView plugin_name;
			TextView plugin_description;

			public ViewHolder(View view) {

				plugin_icon = (ImageView) view.findViewById(R.id.plugin_icon);
				plugin_name = (TextView) view.findViewById(R.id.plugin_name);
				plugin_description = (TextView) view.findViewById(R.id.plugin_description);
			}
		}

	}
}
