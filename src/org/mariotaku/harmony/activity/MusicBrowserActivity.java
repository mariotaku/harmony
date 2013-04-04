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

package org.mariotaku.harmony.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.View;
import java.util.ArrayList;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.activity.BaseActivity;
import org.mariotaku.harmony.dialog.ScanningProgress;
import org.mariotaku.harmony.fragment.AlbumFragment;
import org.mariotaku.harmony.fragment.ArtistFragment;
import org.mariotaku.harmony.fragment.GenreFragment;
import org.mariotaku.harmony.fragment.TrackFragment;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.PreferencesEditor;
import org.mariotaku.harmony.util.ServiceWrapper;
import android.widget.TextView;

public class MusicBrowserActivity extends BaseActivity implements Constants, OnPageChangeListener, View.OnClickListener {

	private TextView mTrackName;
	private TextView mTrackDetail;

	public void onClick(final View view) {
		// TODO: Implement this method
		switch (view.getId()) {
			case R.id.music_browser_control: {
				startActivity(new Intent(this, MusicPlaybackActivity.class));
				break;
			}
		}
	}
	

	private ActionBar mActionBar;
	private ViewPager mViewPager;

	private TabsAdapter mTabsAdapter;

	private ServiceWrapper mService;
	private PreferencesEditor mPrefs;


	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mPrefs = new PreferencesEditor(this);

		String mount_state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(mount_state)
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(mount_state)) {
			startActivity(new Intent(this, ScanningProgress.class));
			finish();
		}

		setContentView(R.layout.music_browser);
		mViewPager.setOnPageChangeListener(this);
		mTabsAdapter = new TabsAdapter(getFragmentManager());
		configureTabs(icicle);

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageSelected(int position) {
		mActionBar.setSelectedNavigationItem(position);
		mPrefs.setIntState(STATE_KEY_CURRENTTAB, position);

	}

	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		mService = service;
		updateTrackInfo();
	}

	@Override
	protected void onServiceDisconnected() {
		mService = null;
	}

	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTrackName = (TextView) findViewById(R.id.track_name);
		mTrackDetail = (TextView) findViewById(R.id.track_detail);
	}

	private void configureTabs(Bundle args) {

		mTabsAdapter.addFragment(new ArtistFragment(), getString(R.string.artists).toUpperCase());
		mTabsAdapter.addFragment(new AlbumFragment(), getString(R.string.albums).toUpperCase());
		mTabsAdapter.addFragment(new TrackFragment(), getString(R.string.tracks).toUpperCase());
		//mTabsAdapter.addFragment(new PlaylistFragment(), getString(R.string.playlists)
		//		.toUpperCase());
		mTabsAdapter.addFragment(new GenreFragment(), getString(R.string.genres).toUpperCase());
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(mTabsAdapter);
		int currenttab = mPrefs.getIntState(STATE_KEY_CURRENTTAB, 0);
		mActionBar.setSelectedNavigationItem(currenttab);
	}
	
	protected void onCurrentMediaChanged() {
		updateTrackInfo();
	}

	private void updateTrackInfo() {
		if (mService == null) return;
		final TrackInfo track = mService.getTrackInfo();
		if (track == null) {
			// Empty playlist
		} else {
			mTrackName.setText(track.title);
			if (!TrackInfo.isUnknownArtist(track)) {
				mTrackDetail.setText(track.artist);
			} else if (!TrackInfo.isUnknownAlbum(track)) {
				mTrackDetail.setText(track.album);
			} else {
				mTrackDetail.setText(R.string.unknown_artist);
			}
		}
	}

	private class TabsAdapter extends FragmentStatePagerAdapter implements TabListener {

		private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

		public TabsAdapter(FragmentManager manager) {
			super(manager);
		}

		public void addFragment(Fragment fragment, String name) {
			mFragments.add(fragment);
			Tab tab = mActionBar.newTab();
			tab.setText(name);
			tab.setTabListener(this);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		}

	}
}
