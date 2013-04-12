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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.activity.BaseActivity;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.util.PreferencesEditor;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.widget.RepeatingImageButton;
import org.mariotaku.harmony.widget.RepeatingImageButton.RepeatListener;
import org.mariotaku.harmony.model.AlbumInfo;
import org.mariotaku.harmony.util.ImageLoaderWrapper;
import android.widget.ImageView;
import android.text.TextUtils;
import org.mariotaku.harmony.app.HarmonyApplication;
import org.mariotaku.harmony.fragment.LyricsFragment;
import android.content.Context;
import android.app.Activity;
import org.mariotaku.harmony.fragment.QueueFragment;
import org.mariotaku.harmony.view.ExtendedRelativeLayout;
import android.view.ViewGroup;
import android.view.MotionEvent;
import org.mariotaku.harmony.util.Utils;
import android.support.v4.app.NavUtils;

public class MusicPlaybackActivity extends BaseActivity implements Constants, View.OnClickListener, SeekBar.OnSeekBarChangeListener,
ViewPager.OnPageChangeListener, RepeatingImageButton.RepeatListener, ExtendedRelativeLayout.TouchInterceptor, ActionBar.OnMenuVisibilityListener {

 	private static final float ALBUM_ART_ALPHA_INACTIVE = 0.3f;
 
	private static final int REQUEST_EQUALIZER = 1;

	private ActionBar mActionBar;
	private boolean mIsShowingMenu;

	private boolean mSeeking = false;

	private boolean mDeviceHasDpad;

	private long mStartSeekPos = 0;

	private long mLastSeekEventTime;
	private boolean mIntentDeRegistered = false;

	private long mPosOverride = -1;
	private boolean mFromTouch = false;
	private long mDuration;
	private boolean mPaused;

	private TextView mTrackName, mTrackDetail;
	private TextView mCurrentTime, mTotalTime;
	private SeekBar mSeekBar;
	private ImageView mAlbumArt;
	private ViewPager mViewPager;
	private RepeatingImageButton mPrevButton, mNextButton;
	private ImageButton mPlayPauseButton;
	private ExtendedRelativeLayout mPlaybackContainer;

	private PagerAdapter mAdapter;

	private ServiceWrapper mService;
	private PreferencesEditor mPreferences;
	private ImageLoaderWrapper mImageLoader;

	private final Handler mHandler = new Handler();
	private Runnable mRefreshRunnable, mHideActionBarRunnable;
	
	@Override
	public void dispatchTouchEvent(final ViewGroup view, final MotionEvent event) {
	}
	

	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.play_pause: {
				doPauseResume();
				break;
			}
			case R.id.next: {
				doNext();
				break;
			}
			case R.id.prev: {
				doPrev();
				break;
			}
		}
	}	

	@Override
	public boolean onInterceptTouchEvent(ViewGroup view, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mActionBar.show();
			mHandler.removeCallbacks(mHideActionBarRunnable);
			if (!mIsShowingMenu) {
				mHandler.postDelayed(mHideActionBarRunnable, 3000);
			}
		}
		return false;
	}

	@Override
	public void onMenuVisibilityChanged(boolean visible) {
		mIsShowingMenu = visible;
		mHandler.removeCallbacks(mHideActionBarRunnable);
		if (!visible) {
			mHandler.postDelayed(mHideActionBarRunnable, 3000);
		}
	}

	@Override
	public boolean onTouchEvent(ViewGroup view, MotionEvent event) {
		return false;
	}
	
	@Override
	public void onPageSelected(final int position) {
		mAlbumArt.setAlpha(position == 1 ? 1 : ALBUM_ART_ALPHA_INACTIVE);
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
		if (position == 0) {
			mAlbumArt.setAlpha(ALBUM_ART_ALPHA_INACTIVE + (1.0f - ALBUM_ART_ALPHA_INACTIVE) * positionOffset);
		} else if (position == 1) {
			mAlbumArt.setAlpha(1.0f - (1.0f - ALBUM_ART_ALPHA_INACTIVE) * positionOffset);
		} else {
			mAlbumArt.setAlpha(ALBUM_ART_ALPHA_INACTIVE);
		}
	}
	
	@Override
	public void onPageScrollStateChanged(final int state) {
	}

	@Override
	public void onProgressChanged(final SeekBar bar, final int progress, final boolean fromuser) {
		if (!fromuser || mService == null) return;
		mPosOverride = mDuration * progress / 1000;
		mService.seek(mPosOverride);

		refreshNow();
		// trackball event, allow progress updates
		if (!mFromTouch) {
			refreshNow();
			mPosOverride = -1;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar bar) {
		mLastSeekEventTime = 0;
		mFromTouch = true;		
		mHandler.removeCallbacks(mRefreshRunnable);
	}

	@Override
	public void onStopTrackingTouch(SeekBar bar) {
		mPosOverride = -1;
		mFromTouch = false;
		// Ensure that progress is properly updated in the future,
		mHandler.post(mRefreshRunnable);
	}
		
	@Override
	public void onRepeat(View v, long howlong, int repcnt) {
		switch (v.getId()) {
			case R.id.prev: {
				scanBackward(repcnt, howlong);
				break;
			}
			case R.id.next: {
				scanForward(repcnt, howlong);
				break;
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageLoader = HarmonyApplication.getInstance(this).getImageLoaderWrapper();
		mPreferences = new PreferencesEditor(this);
		mRefreshRunnable = new RefreshRunnable(this);
		mHideActionBarRunnable = new HideActionBarRunnable(this);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.music_playback);
		mActionBar = getActionBar();
		mActionBar.addOnMenuVisibilityListener(this);
		mActionBar.hide();

		mSeekBar.setMax(1000);
		mSeekBar.setOnSeekBarChangeListener(this);

		mPlayPauseButton.setOnClickListener(this);		
		mPrevButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		
		mPrevButton.setRepeatListener(this, 260);
		mNextButton.setRepeatListener(this, 260);

		mDeviceHasDpad = getResources().getConfiguration().navigation == Configuration.NAVIGATION_DPAD;
		mPlaybackContainer.setTouchInterceptor(this);

		mAdapter = new PagerAdapter(this);
		mViewPager.setAdapter(mAdapter);
		mAdapter.addFragment(QueueFragment.class);
		mAdapter.addFragment(Fragment.class);
		mAdapter.addFragment(LyricsFragment.class);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setCurrentItem(1, false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_music_playback, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mService == null) return super.onKeyDown(keyCode, event);
		final int repcnt = event.getRepeatCount();

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (!useDpadMusicControl()) {
					break;
				}
				if (!mPrevButton.hasFocus()) {
					mPrevButton.requestFocus();
				}
				scanBackward(repcnt, event.getEventTime() - event.getDownTime());
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!useDpadMusicControl()) {
					break;
				}
				if (!mNextButton.hasFocus()) {
					mNextButton.requestFocus();
				}
				scanForward(repcnt, event.getEventTime() - event.getDownTime());
				return true;

				// case KeyEvent.KEYCODE_R:
				// toggleRepeat();
				// return true;
				//
				// case KeyEvent.KEYCODE_S:
				// toggleShuffle();
				// return true;

			case KeyEvent.KEYCODE_N: {
				doNext();
				return true;
			}
			case KeyEvent.KEYCODE_P: {
				doPrev();
				return true;
			}
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_SPACE: {
				doPauseResume();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if (!useDpadMusicControl()) {
						break;
					}
					if (mService != null) {
						if (!mSeeking && mStartSeekPos >= 0) {
							mPlayPauseButton.requestFocus();
							if (mStartSeekPos < 1000) {
								mService.prev();
							} else {
								mService.seek(0);
							}
						} else {
							scanBackward(-1, event.getEventTime() - event.getDownTime());
							mPlayPauseButton.requestFocus();
							mStartSeekPos = -1;
						}
					}
					mSeeking = false;
					mPosOverride = -1;
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if (!useDpadMusicControl()) {
						break;
					}
					if (mService != null) {
						if (!mSeeking && mStartSeekPos >= 0) {
							mPlayPauseButton.requestFocus();
							mService.next();
						} else {
							scanForward(-1, event.getEventTime() - event.getDownTime());
							mPlayPauseButton.requestFocus();
							mStartSeekPos = -1;
						}
					}
					mSeeking = false;
					mPosOverride = -1;
					return true;
			}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onLongClick(View v) {

		// TODO search media info

		String track = getTitle().toString();
		String artist = "";// mArtistNameView.getText().toString();
		String album = "";// mAlbumNameView.getText().toString();

		CharSequence title = getString(R.string.mediasearch, track);
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
		i.putExtra(MediaStore.EXTRA_MEDIA_TITLE, track);

		String query = track;
		if (!getString(R.string.unknown_artist).equals(artist)
				&& !getString(R.string.unknown_album).equals(album)) {
			query = artist + " " + track;
			i.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album);
			i.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
		} else if (getString(R.string.unknown_artist).equals(artist)
				&& !getString(R.string.unknown_album).equals(album)) {
			query = album + " " + track;
			i.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album);
		} else if (!getString(R.string.unknown_artist).equals(artist)
				&& getString(R.string.unknown_album).equals(album)) {
			query = artist + " " + track;
			i.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
		}
		i.putExtra(SearchManager.QUERY, query);
		i.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "audio/*");
		startActivity(Intent.createChooser(i, title));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mService == null) return true;
		switch (item.getItemId()) {
			case MENU_ADD_TO_PLAYLIST: {
				final Intent intent = new Intent(INTENT_ADD_TO_PLAYLIST);
				long[] list_to_be_added = new long[1];
				//list_to_be_added[0] = MusicUtils.getCurrentAudioId();
				intent.putExtra(INTENT_KEY_LIST, list_to_be_added);
				startActivity(intent);
				break;
			}
			case EQUALIZER: {
				final Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
				intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
				intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mService.getAudioSessionId());
				intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
				startActivityForResult(intent, REQUEST_EQUALIZER);
				break;
			}
			case MENU_SLEEP_TIMER: {
				final Intent intent = new Intent(INTENT_SLEEP_TIMER);
				startActivity(intent);
				break;
			}
			case DELETE_ITEMS: {
				final Intent intent = new Intent(INTENT_DELETE_ITEMS);
				Bundle bundle = new Bundle();
//				bundle.putString(
//						INTENT_KEY_PATH,
//						Uri.withAppendedPath(Audio.Media.EXTERNAL_CONTENT_URI,
//								Uri.encode(String.valueOf(MusicUtils.getCurrentAudioId())))
//								.toString());
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case SETTINGS: {
				final Intent intent = new Intent(INTENT_APPEARANCE_SETTINGS);
				startActivity(intent);
				break;
			}
			case MENU_HOME: {
				NavUtils.navigateUpFromSameTask(this);
				break;
			}
			case ADD_TO_FAVORITES: {
				toggleFavorite();
				break;
			}
			case MENU_SHUFFLE_MODE_NONE: {
				mService.setShuffleMode(SHUFFLE_MODE_NONE);
				break;
			}
			case MENU_SHUFFLE_MODE_ALL: {
				mService.setShuffleMode(SHUFFLE_MODE_ALL);
				break;
			}
			case MENU_REPEAT_MODE_NONE: {
				mService.setRepeatMode(REPEAT_MODE_NONE);
				break;
			}
			case MENU_REPEAT_MODE_ALL: {
				mService.setRepeatMode(REPEAT_MODE_ALL);
				break;
			}
			case MENU_REPEAT_MODE_CURRENT: {
				mService.setRepeatMode(REPEAT_MODE_CURRENT);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem equalizer = menu.findItem(EQUALIZER);
		if (equalizer != null) {
			final PackageManager pm = getPackageManager();
			final Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
			equalizer.setVisible(!pm.queryIntentActivities(intent, 0).isEmpty());
		}
//		final MenuItem item = menu.findItem(ADD_TO_FAVORITES);
//		try {
//			if (item != null && mService != null)
//				item.setIcon(mService.isFavorite(mService.getAudioId()) ? R.drawable.ic_menu_star
//						: R.drawable.ic_menu_star_off);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
		if (mService != null) {
			switch (mService.getShuffleMode()) {
				case SHUFFLE_MODE_ALL: {
					menu.findItem(MENU_SHUFFLE_MODE_ALL).setChecked(true);
					break;
				}
				default: {
					menu.findItem(MENU_SHUFFLE_MODE_NONE).setChecked(true);
					break;
				}
			}
			switch (mService.getRepeatMode()) {
				case REPEAT_MODE_ALL: {
					menu.findItem(MENU_REPEAT_MODE_ALL).setChecked(true);
					break;
				}
				case REPEAT_MODE_CURRENT: {
					menu.findItem(MENU_REPEAT_MODE_CURRENT).setChecked(true);
					break;
				}
				default: {
					menu.findItem(MENU_REPEAT_MODE_NONE).setChecked(true);
					break;
				}
			}
		}
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mIntentDeRegistered) {
			mPaused = false;
		}
	}

	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		mService = service;
		// something went wrong
		if (service == null) {
			Toast.makeText(this, R.string.service_start_error_msg, Toast.LENGTH_SHORT);
			finish();
			return;
		}
		if (service.getTrackInfo() == null) {
			// Navigate to music browser
			finish();
			return;
		}
		updateTrackInfo();
		updatePlayPauseButton();
		queueNextRefresh(refreshNow());
	}



	@Override
	protected void onStart() {
		super.onStart();
		mPaused = false;
		final boolean lyrics_wakelock = mPreferences.getBooleanPref(KEY_LYRICS_WAKELOCK, DEFAULT_LYRICS_WAKELOCK);
		if (lyrics_wakelock) {
			getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		final int page_pos = mPreferences.getIntState(STATE_KEY_PAGE_POSITION_PLAYBACK, 1);
		mViewPager.setCurrentItem(Utils.limit(page_pos, 0, mAdapter.getCount()));
	}

	@Override
	protected void onStop() {
		mPaused = true;
		mPreferences.setIntState(STATE_KEY_PAGE_POSITION_PLAYBACK, mViewPager.getCurrentItem());
		getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		mActionBar.removeOnMenuVisibilityListener(this);
		super.onDestroy();
	}


	protected void onCurrentMediaChanged() {
		updateTrackInfo();
		updatePlayPauseButton();
	}
	
	protected void onPlayStateChanged() {
		updatePlayPauseButton();
	}
	
	protected void onRepeatModeChanged() {
		invalidateOptionsMenu();
	}

	protected void onShuffleModeChanged() {
		invalidateOptionsMenu();
	}
	
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mPlaybackContainer = (ExtendedRelativeLayout) findViewById(R.id.music_playback);
		mAlbumArt = (ImageView) findViewById(R.id.album_art);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTrackName = (TextView) findViewById(R.id.track_name);
		mTrackDetail = (TextView) findViewById(R.id.track_detail);
		mCurrentTime = (TextView) findViewById(R.id.current_time);
		mTotalTime = (TextView) findViewById(R.id.total_time);
		mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
		mPrevButton = (RepeatingImageButton) findViewById(R.id.prev);
		mPlayPauseButton = (ImageButton) findViewById(R.id.play_pause);
		mNextButton = (RepeatingImageButton) findViewById(R.id.next);
	}

	private void doNext() {
		if (mService == null) return;
		mService.next();
	}

	private void doPauseResume() {
		if (mService == null) return;
		if (mService.isPlaying()) {
			mService.pause();
		} else {
			mService.play();
		}
		refreshNow();
		updatePlayPauseButton();
	}

	private void doPrev() {
		if (mService == null) return;
		if (mService.getPosition() < 2000) {
			mService.prev();
		} else {
			mService.seek(0);
			mService.play();
		}
	}


	private void queueNextRefresh(long delay) {
		if (!mPaused && !mFromTouch) {
			mHandler.postDelayed(mRefreshRunnable, delay);
		}
	}

	private long refreshNow() {
		if (mService == null) return 500;
		final long pos = mPosOverride < 0 ? mService.getPosition() : mPosOverride;
		long remaining = 1000 - pos % 1000;
		if (pos >= 0 && mDuration > 0) {
			mCurrentTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
			if (mService.isPlaying()) {
				mCurrentTime.setVisibility(View.VISIBLE);
			} else {
				// blink the counter
				// If the progress bar is still been dragged, then we do not
				// want to blink the
				// currentTime. It would cause flickering due to change in
				// the visibility.
				if (mFromTouch) {
					mCurrentTime.setVisibility(View.VISIBLE);
				} else {
					int vis = mCurrentTime.getVisibility();
					mCurrentTime.setVisibility(vis == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
				}
				remaining = 500;
			}
			// Normalize our progress along the progress bar's scale
			mSeekBar.setProgress((int) (1000 * pos / mDuration));
		} else {
			mCurrentTime.setText("--:--");
			mSeekBar.setProgress(0);
		}
		// return the number of milliseconds until the next full second, so
		// the counter can be updated at just the right time
		return remaining;
	}

	private void scanBackward(int repcnt, long delta) {
		if (mService == null) return;
		if (repcnt == 0) {
			mStartSeekPos = mService.getPosition();
			mLastSeekEventTime = 0;
			mSeeking = false;
		} else {
			mSeeking = true;
			if (delta < 5000) {
				// seek at 10x speed for the first 5 seconds
				delta = delta * 10;
			} else {
				// seek at 40x after that
				delta = 50000 + (delta - 5000) * 40;
			}
			long newpos = mStartSeekPos - delta;
			if (newpos < 0) {
				// move to previous track
				mService.prev();
				long duration = mService.getDuration();
				mStartSeekPos += duration;
				newpos += duration;
			}
			if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
				mService.seek(newpos);
				mLastSeekEventTime = delta;
			}
			if (repcnt >= 0) {
				mPosOverride = newpos;
			} else {
				mPosOverride = -1;
			}
			refreshNow();
		}
	}

	private void scanForward(int repcnt, long delta) {
		if (mService == null) return;
		if (repcnt == 0) {
			mStartSeekPos = mService.getPosition();
			mLastSeekEventTime = 0;
			mSeeking = false;
		} else {
			mSeeking = true;
			if (delta < 5000) {
				// seek at 10x speed for the first 5 seconds
				delta = delta * 10;
			} else {
				// seek at 40x after that
				delta = 50000 + (delta - 5000) * 40;
			}
			long newpos = mStartSeekPos + delta;
			long duration = mService.getDuration();
			if (newpos >= duration) {
				// move to next track
				mService.next();
				mStartSeekPos -= duration; // is OK to go negative
				newpos -= duration;
			}
			if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
				mService.seek(newpos);
				mLastSeekEventTime = delta;
			}
			if (repcnt >= 0) {
				mPosOverride = newpos;
			} else {
				mPosOverride = -1;
			}
			refreshNow();
		}
	}

	private void updatePlayPauseButton() {
		if (mService != null && mService.isPlaying()) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_ic_pause);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_ic_play);
		}
	}

	private void toggleFavorite() {
		if (mService == null) return;
	}

	
	private void updateTrackInfo() {
		if (mService == null) return;
		final TrackInfo track = mService.getTrackInfo();
		if (track == null) return;
		final AlbumInfo album = AlbumInfo.getAlbumInfo(this, track);
		mTrackName.setText(track.title);
		if (!TrackInfo.isUnknownArtist(track)) {
			mTrackDetail.setText(track.artist);
		} else if (!TrackInfo.isUnknownAlbum(track)) {
			mTrackDetail.setText(track.album);
		} else {
			mTrackDetail.setText(R.string.unknown_artist);
		}
		mSeekBar.setProgress(0);
		mDuration = mService.getDuration();
		mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / 1000));
		mImageLoader.displayImage(mAlbumArt, album != null ? album.album_art : null);
	}
	
	private boolean useDpadMusicControl() {
		return mDeviceHasDpad && (mPrevButton.isFocused() || mNextButton.isFocused() || mPlayPauseButton.isFocused());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (resultCode != RESULT_OK) return;
		switch (requestCode) {
			case DELETE_ITEMS:
				if (resultCode == RESULT_DELETE_MUSIC) {
					finish();
				}
				break;
		}
	}

	private static class PagerAdapter extends FragmentStatePagerAdapter {

		private final Context mContext;
		private final ArrayList<Class<? extends Fragment>> mFragments = new ArrayList<Class<? extends Fragment>>();

		public PagerAdapter(final Activity activity) {
			super(activity.getFragmentManager());
			mContext = activity;
		}

		public void addFragment(Class<? extends Fragment> fragment) {
			mFragments.add(fragment);
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

	private static final class RefreshRunnable implements Runnable {

		private final MusicPlaybackActivity mActivity;

		private RefreshRunnable(final MusicPlaybackActivity activity) {
			mActivity = activity;
		}

		@Override
		public void run() {
			mActivity.queueNextRefresh(mActivity.refreshNow());
		}

	}


	private static final class HideActionBarRunnable implements Runnable {

		private final ActionBar mActionBar;

		private HideActionBarRunnable(final Activity activity) {
			mActionBar = activity.getActionBar();
		}

		@Override
		public void run() {
			mActionBar.hide();
		}

	}

}
