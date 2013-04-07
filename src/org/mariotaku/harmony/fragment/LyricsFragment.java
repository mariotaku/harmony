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

import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.adapter.LyricsAdapter;
import org.mariotaku.harmony.loader.LyricsLoader;
import org.mariotaku.harmony.model.Lyrics;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.LyricsTimer;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.view.ExtendedFrameLayout;
import org.mariotaku.harmony.view.iface.IExtendedView.OnSizeChangedListener;
import org.mariotaku.harmony.view.iface.IExtendedViewGroup.TouchInterceptor;
import android.widget.Toast;

public class LyricsFragment extends BaseListFragment implements Constants, OnLongClickListener, LoaderManager.LoaderCallbacks<Lyrics>,
LyricsTimer.Callbacks, OnSizeChangedListener, TouchInterceptor, OnItemLongClickListener,
OnScaleGestureListener {

	private static float limit(final float value, final float value1, final float value2) {
		final float min = Math.min(value1, value2), max = Math.max(value1, value2);
		return Math.max(Math.min(value, max), min);
	}
 
	private ScaleGestureDetector mScaleGestureDetector;

	public boolean onScale(ScaleGestureDetector detctor) {
		final float size = mAdapter.getTextSize() * detctor.getScaleFactor();
		mAdapter.setTextSize(limit(size, 10, 24));
		return true;
	}

	public boolean onScaleBegin(ScaleGestureDetector detector) {
		mAdapter.setAutoWrapEnabled(false);
		return true;
	}

	public void onScaleEnd(ScaleGestureDetector detector) {
		mAdapter.setAutoWrapEnabled(true);
	}
	

	public void dispatchTouchEvent(ViewGroup view, MotionEvent event) {
	}

	public boolean onInterceptTouchEvent(ViewGroup view, MotionEvent event) {
		return event.getPointerCount() > 1;
	}

	public boolean onTouchEvent(ViewGroup view, MotionEvent event) {
		return mScaleGestureDetector.onTouchEvent(event);
	}	

	public boolean onItemLongClick(AdapterView<?> view, View child, int position, long id) {
		return true;
	}
	

	private View mHeaderView;
	private int mViewHeight;

	public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
		// TODO: Implement this method
		mHeaderView.setMinimumWidth(w);
		mHeaderView.setMinimumHeight(h / 2);
		mViewHeight = h;
		mAdapter.setMaxWidth(w / 3 * 2);
	}
	

	private LyricsTimer mLyricsTimer;
	private LyricsAdapter mAdapter;
	private ServiceWrapper mService = null;
	
	private Lyrics mLyrics;

	private ListView mListView;
	private ExtendedFrameLayout mContainerView;	
	
	private boolean mLoaderInitialized;

	public void onLyricsChanged(Lyrics.Line current) {
		if (current == null) return;
		final int position = current.getIndex();
		final View item_view = mAdapter.getView(position, null, null);
		item_view.measure(0, 0);
		final int h = item_view.getMeasuredHeight();
		mListView.smoothScrollToPositionFromTop(position + mListView.getHeaderViewsCount(), mViewHeight / 2 - h / 2);
		mListView.setOnItemLongClickListener(this);
		mAdapter.setCurrentPosition(position);
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
		setListShown(false);
		return new LyricsLoader(getActivity(), args.getString(EXTRA_LYRICS_PATH));
	}

	public void onLoadFinished(Loader<Lyrics> loader, Lyrics data) {
		mLyrics = data;
		//mLyricsScrollView.setTextContent(data != null ? data.getAll() : null);
		mAdapter.loadLyrics(data);
		mLyricsTimer.loadLyrics(data);
		setListShown(true);		
	}

	public void onLoaderReset(Loader<Lyrics> id) {
		// TODO: Implement this method
	}

	// for lyrics displaying
	//private TextScrollView mLyricsScrollView;
	//private TextView mLyricsInfoMessage;

	protected void onSeekChanged() {
		mLyricsTimer.resume();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//mLyricsScrollView.setContentGravity(Gravity.CENTER_HORIZONTAL);
		//mLyricsInfoMessage.setOnLongClickListener(this);
		mHeaderView = new View(getActivity());
		mScaleGestureDetector = new ScaleGestureDetector(getActivity(), this);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.addFooterView(mHeaderView, null, false);
		mListView.setDivider(null);
		mContainerView.setOnSizeChangedListener(this);
		mContainerView.setTouchInterceptor(this);
		mAdapter = new LyricsAdapter(getActivity());
		setListAdapter(mAdapter);
		setListShown(false);
		mLyricsTimer = new LyricsTimer(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		mContainerView = new ExtendedFrameLayout(getActivity());
		mContainerView.addView(view);
		return mContainerView;
	}
	
	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mLyrics == null) return;
		mService.seek(mLyrics.get(position - l.getHeaderViewsCount()).getActualTime());
	}

	@Override
	public boolean onLongClick(final View v) {
		searchLyrics();
		return true;
	}

	@Override
	protected void onServiceConnected(final ServiceWrapper service) {
		mService = service;
		loadLyrics();		
	}

	@Override
	protected void onServiceDisconnected() {
		mService = null;
	}
	
	protected void onCurrentMediaChanged() {
		loadLyrics();
	}
	
	protected void onPlayStateChanged() {
		if (isPlaying()) {
			mLyricsTimer.resume();
		} else {
			mLyricsTimer.pause();
		}
	}

	private void loadLyrics() {
		if (mService == null) return;
		final TrackInfo track = mService.getTrackInfo();
		if (track == null) return;
		final String path = track.data.substring(0, track.data.lastIndexOf(".")) + ".lrc";
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
