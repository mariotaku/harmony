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
import org.mariotaku.harmony.IMusicPlaybackService;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.widget.TextScrollView;
import org.mariotaku.harmony.widget.TextScrollView.OnLineSelectedListener;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import android.app.Fragment;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.fragment.BaseFragment;
import android.app.LoaderManager;
import android.content.Loader;
import org.mariotaku.harmony.loader.LyricsLoader;
import android.widget.Toast;
import org.mariotaku.harmony.model.Lyrics;
import org.mariotaku.harmony.util.LyricsTimer;

public class LyricsFragment extends BaseFragment implements Constants, OnLineSelectedListener, OnLongClickListener,
	LoaderManager.LoaderCallbacks<Lyrics>, LyricsTimer.Callbacks {

	private LyricsTimer mLyricsTimer;

	public void onLyricsChanged(Lyrics.Line current) {
		if (current == null) return;
		mLyricsScrollView.setCurrentLine(current.getIndex(), false);
	}

	public boolean isPlaying() {
		return mService != null && mService.isPlaying();
	}

	public long getPosition() {
		if (mService == null) return -1;
		return mService.getPosition();
	}
	

	private static final String EXTRA_LYRICS_PATH = "lyrics_path";
			
	public Loader<Lyrics> onCreateLoader(int id, Bundle args) {
		return new LyricsLoader(getActivity(), args.getString(EXTRA_LYRICS_PATH));
	}

	public void onLoadFinished(Loader<Lyrics> loader, Lyrics data) {
		mLyrics = data;
		mLyricsScrollView.setTextContent(data != null ? data.getAll() : null);
		mLyricsTimer.loadLyrics(data);
	}

	public void onLoaderReset(Loader<Lyrics> id) {
		// TODO: Implement this method
	}
	
	private boolean mLoaderInitialized;

	private ServiceWrapper mService = null;
	private Lyrics mLyrics;

	// for lyrics displaying
	private TextScrollView mLyricsScrollView;
	private TextView mLyricsInfoMessage;

	protected void onSeekChanged() {
		mLyricsTimer.resume();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mLyricsScrollView.setContentGravity(Gravity.CENTER_HORIZONTAL);
		mLyricsInfoMessage.setOnLongClickListener(this);
		mLyricsTimer = new LyricsTimer(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.lyrics_view, container, false);
		mLyricsScrollView = (TextScrollView) view.findViewById(R.id.lyrics_scroll);
		mLyricsInfoMessage = (TextView) view.findViewById(R.id.message);
		return view;
	}

	@Override
	public void onLineSelected(int id) {
		if (mLyrics == null) return;
		mService.seek(mLyrics.get(id).getActualTime());
	}

	@Override
	public boolean onLongClick(View v) {
		searchLyrics();
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		mLyricsScrollView.setLineSelectedListener(this);
		mLyricsScrollView.setSmoothScrollingEnabled(true);
	}
	
	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		mService = service;
	}

	@Override
	protected void onServiceDisconnected() {
		mService = null;
	}
	
	protected void onCurrentMediaChanged() {
		final TrackInfo track = mService.getTrackInfo();
		if (track == null) return;
		final String path = track.data.substring(0, track.data.lastIndexOf(".")) + ".lrc";
		loadLyrics(path);
	}
	
	protected void onPlayStateChanged() {
		if (isPlaying()) {
			mLyricsTimer.resume();
		} else {
			mLyricsTimer.pause();
		}
	}

	// TODO lyrics load animation
	private void loadLyrics(final String path) {
		final LoaderManager lm = getLoaderManager();
		final Bundle args = new Bundle();
		args.putString(EXTRA_LYRICS_PATH, path);
		if (mLoaderInitialized) {
			lm.restartLoader(0, args, this);
		} else {
			lm.initLoader(0, args, this);
			mLoaderInitialized = true;
		}
	}

	private void searchLyrics() {

		final TrackInfo info = mService.getTrackInfo();
		if (info == null) return;
		final String lyricsPath = info.data.substring(0, info.data.lastIndexOf(".")) + ".lrc";
		try {
			Intent intent = new Intent(INTENT_SEARCH_LYRICS);
			intent.putExtra(INTENT_KEY_ARTIST, info.artist);
			intent.putExtra(INTENT_KEY_TRACK, info.title);
			intent.putExtra(INTENT_KEY_PATH, lyricsPath);
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// e.printStackTrace();
		}
	}

}
