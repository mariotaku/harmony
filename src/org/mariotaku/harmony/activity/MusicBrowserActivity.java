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
 
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.activity.BaseActivity;
import org.mariotaku.harmony.app.HarmonyApplication;
import org.mariotaku.harmony.dialog.ScanningProgress;
import org.mariotaku.harmony.fragment.AlbumsFragment;
import org.mariotaku.harmony.fragment.ArtistAlbumsFragment;
import org.mariotaku.harmony.fragment.GenresFragment;
import org.mariotaku.harmony.fragment.TracksFragment;
import org.mariotaku.harmony.model.AlbumInfo;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.ImageLoaderWrapper;
import org.mariotaku.harmony.util.PreferencesEditor;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.fragment.ArtistsFragment;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.view.AlbumArtView;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.Menu;

public class MusicBrowserActivity extends BaseActivity implements Constants, View.OnClickListener, OnNavigationListener, 
		TabListener, OnPageChangeListener  {

	private TextView mTrackName;
	private TextView mTrackDetail;

	private AlbumArtView mAlbumArt;

	private ArrayAdapter<String> mSpinnerAdapter;
	private TabsAdapter mTabsAdapter;

	private ActionBar mActionBar;
	private ViewPager mViewPager;

	private ServiceWrapper mService;
	private PreferencesEditor mPrefs;

	private ImageLoaderWrapper mImageLoader;

	private View mControlContainer;
	private ImageButton mPlayPauseButton, mNextButton;
	
	public void onClick(final View view) {
		if (mService == null) return;
		switch (view.getId()) {
			case R.id.music_browser_control: {
				if (mService.getTrackInfo() == null) {
					MusicUtils.shuffleAll(this, mService);
				} else {
					final Intent intent = new Intent(this, MusicPlaybackActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
					startActivity(intent);
				}
				break;
			}
			case R.id.play_pause: {
				if (mService.isPlaying()) {
					mService.pause();
				} else {
					mService.play();
				}
				break;
			}
			case R.id.next: {
				mService.next();
				break;
			}
		}
	}
	

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mImageLoader = HarmonyApplication.getInstance(this).getImageLoaderWrapper();
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		//mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mPrefs = new PreferencesEditor(this);

		final String mount_state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(mount_state)
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(mount_state)) {
			startActivity(new Intent(this, ScanningProgress.class));
			finish();
		}

		setContentView(R.layout.music_browser);
		mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
		mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mActionBar.setDisplayShowCustomEnabled(true);
		//mActionBar.setDisplayShowHomeEnabled(true);
		//mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//mActionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
		mActionBar.setCustomView(R.layout.music_browser_control_bar);
		mControlContainer = mActionBar.getCustomView();
		//mViewPager.setEnabled(false);
		mAlbumArt = (AlbumArtView) mControlContainer.findViewById(R.id.album_art);
		mTrackName = (TextView) mControlContainer.findViewById(R.id.track_name);
		mTrackDetail = (TextView) mControlContainer.findViewById(R.id.track_detail);
		mPlayPauseButton = (ImageButton) mControlContainer.findViewById(R.id.play_pause);
		mNextButton = (ImageButton) mControlContainer.findViewById(R.id.next);
		mTabsAdapter = new TabsAdapter(this);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(mTabsAdapter);
		mPlayPauseButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		configureTabs(!mViewPager.isEnabled());
		final int currenttab = mPrefs.getIntState(STATE_KEY_PAGE_POSITION_BROWSER, 0);
		mActionBar.setSelectedNavigationItem(currenttab);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		mViewPager.setCurrentItem(itemPosition);
		return true;
	}

	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		mService = service;
		updateTrackInfo();
		updatePlayPauseButton();
	}

	@Override
	protected void onServiceDisconnected() {
		mService = null;
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ViewPager) findViewById(R.id.pager);
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageSelected(int position) {
		mActionBar.setSelectedNavigationItem(position);
		mPrefs.setIntState(STATE_KEY_PAGE_POSITION_BROWSER, position);
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
	
	private void configureTabs(final boolean horizontal_mode) {
		//if (horizontal_mode) {	
			addTab(ArtistsFragment.class, getString(R.string.artists));
		//} else {
		//	addTab(ArtistAlbumsFragment.class, getString(R.string.artists));
		//}
		addTab(AlbumsFragment.class, getString(R.string.albums));
		addTab(TracksFragment.class, getString(R.string.tracks));
		//mTabsAdapter.addFragment(new PlaylistFragment(), getString(R.string.playlists));
		addTab(GenresFragment.class, getString(R.string.genres));
	}
	
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_music_browser, menu);
		return true;
	}
	
	private void addTab(final Class<? extends Fragment> clz, final String name) {
		mTabsAdapter.add(clz);
		mSpinnerAdapter.add(name);
		mActionBar.addTab(mActionBar.newTab().setText(name).setTabListener(this));
	}
	
	protected void onCurrentMediaChanged() {
		updateTrackInfo();
		updatePlayPauseButton();
	}

	protected void onPlayStateChanged() {
		updatePlayPauseButton();
	}
	
	private void updateTrackInfo() {
		if (mService == null) return;
		final TrackInfo track = mService.getTrackInfo();
		mPlayPauseButton.setVisibility(track != null ? View.VISIBLE : View.GONE);
		mNextButton.setVisibility(track != null ? View.VISIBLE : View.GONE);
		if (track != null) {
			mTrackName.setText(track.title);
			if (!TrackInfo.isUnknownArtist(track)) {
				mTrackDetail.setText(track.artist);
			} else if (!TrackInfo.isUnknownAlbum(track)) {
				mTrackDetail.setText(track.album);
			} else {
				mTrackDetail.setText(R.string.unknown_artist);
			}
			mAlbumArt.loadAlbumArt(track);
		} else {
			mTrackName.setText(R.string.empty_queue);
			mTrackDetail.setText(R.string.touch_to_shuffle_all);
			mAlbumArt.loadAlbumArt(null);
		}
	}

	private void updatePlayPauseButton() {
		if (mService == null) return;
		if (mService.isPlaying()) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_ic_pause);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_ic_play);
		}
	}
	
	private static class TabsAdapter extends FragmentStatePagerAdapter {
		
		private final ArrayList<Class<? extends Fragment>> mFragments = new ArrayList<Class<? extends Fragment>>();
		private final Context mContext;

		public TabsAdapter(final Activity activity) {
			super(activity.getFragmentManager());
			mContext = activity;
		}

		public void add(final Class<? extends Fragment> clz) {
			mFragments.add(clz);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return Fragment.instantiate(mContext, mFragments.get(position).getName());
		}

	}
}
