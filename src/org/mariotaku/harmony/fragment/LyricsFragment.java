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
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.adapter.LyricsAdapter;
import org.mariotaku.harmony.loader.LyricsLoader;
import org.mariotaku.harmony.model.Lyrics;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.LyricsTimer;
import org.mariotaku.harmony.util.PreferencesEditor;
import org.mariotaku.harmony.util.ServiceWrapper;
import org.mariotaku.harmony.util.Utils;
import org.mariotaku.harmony.view.ExtendedFrameLayout;
import org.mariotaku.harmony.view.ExtendedViewPager;
import org.mariotaku.harmony.view.iface.IExtendedView.OnSizeChangedListener;
import org.mariotaku.harmony.view.iface.IExtendedViewGroup.TouchInterceptor;
import android.widget.AbsListView;

public class LyricsFragment extends BaseListFragment implements Constants, OnLongClickListener, LoaderManager.LoaderCallbacks<Lyrics>,
		LyricsTimer.Callbacks, OnSizeChangedListener, TouchInterceptor, OnItemLongClickListener,
				OnScaleGestureListener, OnScrollListener {

	private static final String EXTRA_LYRICS_PATH = "lyrics_path";
	
	private boolean mBusy;

	private PreferencesEditor mPreferences;
 
	private ScaleGestureDetector mScaleGestureDetector;

	@Override
	public void dispatchTouchEvent(ViewGroup view, MotionEvent event) {
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		final float size = mAdapter.getTextSize() * detector.getScaleFactor();
		mAdapter.setTextSize(Utils.limit(size, TEXTSIZE_LYRICS_MIN, TEXTSIZE_LYRICS_MAX));
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		mAdapter.setAutoWrapEnabled(false);
		final View view = getActivity().findViewById(R.id.pager);
		if (view instanceof ExtendedViewPager) {
			((ExtendedViewPager) view).setPagingEnabled(false);
		}
		mBusy = true;
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		mAdapter.setAutoWrapEnabled(true);
		final View view = getActivity().findViewById(R.id.pager);
		if (view instanceof ExtendedViewPager) {
			((ExtendedViewPager) view).setPagingEnabled(true);
		}
		mBusy = false;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mBusy = scrollState != SCROLL_STATE_IDLE;
	}

	@Override
	public boolean onInterceptTouchEvent(ViewGroup view, MotionEvent event) {
		return event.getPointerCount() > 1;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> view, View child, int position, long id) {
		return true;
	}

	@Override
	public boolean onTouchEvent(ViewGroup view, MotionEvent event) {
		if (event.getPointerCount() > 1) {
			mScaleGestureDetector.onTouchEvent(event);
		}
		return true;
	}	

	@Override
	public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
		mHeaderView.setMinimumWidth(w);
		mHeaderView.setMinimumHeight(h / 2);
		mViewHeight = h;
		mAdapter.setMaxWidth(w / 3 * 2);
	}
	
	private View mHeaderView;
	private int mViewHeight;

	private LyricsTimer mLyricsTimer;
	private LyricsAdapter mAdapter;
	private ServiceWrapper mService = null;
	
	private Lyrics mLyrics;

	private ListView mListView;
	private ExtendedFrameLayout mContainerView;	
	
	private boolean mLoaderInitialized;

	@Override
	public void onLyricsChanged(Lyrics.Line current) {
		if (current == null) return;
		final int position = current.getIndex();
		if (!mBusy) {
			final View item_view = mAdapter.getView(position, null, null);
			item_view.measure(0, 0);
			final int item_height = item_view.getMeasuredHeight();
			final int offset = mViewHeight / 2 - item_height / 2;
			mListView.smoothScrollToPositionFromTop(position + mListView.getHeaderViewsCount(), offset);
		}
		mAdapter.setCurrentPosition(position);
	}

	@Override
	public boolean isPlaying() {
		return mService != null && mService.isPlaying();
	}

	@Override
	public long getPosition() {
		if (mService == null) return -1;
		return mService.getPosition();
	}
	
	@Override
	public Loader<Lyrics> onCreateLoader(int id, Bundle args) {
		setListShown(false);
		return new LyricsLoader(getActivity(), args.getString(EXTRA_LYRICS_PATH));
	}

	@Override
	public void onLoadFinished(Loader<Lyrics> loader, Lyrics data) {
		mLyrics = data;
		mAdapter.loadLyrics(data);
		mLyricsTimer.loadLyrics(data);
		setListShown(true);		
	}

	@Override
	public void onLoaderReset(final Loader<Lyrics> loader) {
	}

	@Override
	protected void onSeekChanged() {
		mLyricsTimer.resume();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = new PreferencesEditor(getActivity());
		mHeaderView = new View(getActivity());
		mScaleGestureDetector = new ScaleGestureDetector(getActivity(), this);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.addFooterView(mHeaderView, null, false);
		mListView.setDivider(null);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnScrollListener(this);
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
		if (mLyrics == null || mService == null) return;
		mService.seek(mLyrics.get(position - l.getHeaderViewsCount()).getActualTime());
	}

	@Override
	public boolean onLongClick(final View v) {
		searchLyrics();
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mAdapter.setTextSize(mPreferences.getFloatPref(PREFERENCE_KEY_LYRICS_TEXTSIZE, PREFERENCE_DEFAULT_TEXTSIZE_LYRICS));
	}
	

	@Override
	public void onStop() {
		mPreferences.setFloatPref(PREFERENCE_KEY_LYRICS_TEXTSIZE, mAdapter.getTextSize());
		super.onStop();
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

	@Override
	protected void onCurrentMediaChanged() {
		loadLyrics();
	}

	@Override
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
