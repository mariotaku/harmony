package org.mariotaku.harmony.util;

import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import java.util.List;
import org.mariotaku.harmony.model.Lyrics;

public final class LyricsTimer {

	private static final int LYRICS_REFRESHED = 1;
	private static final int LYRICS_RESUMED = 2;
	private static final int LYRICS_POSITION_CHANGED = 3;
	private static final int LYRICS_PAUSED = 4;

	private final Callbacks mListener;
	private final Handler mHandler;

	private Lyrics mLyrics;
	private int mCurrentIndex;

	public LyricsTimer(final Callbacks listener) {
		mHandler = new Handler();
		mListener = listener;
	}

	public void loadLyrics(final Lyrics lyrics) {
		mLyrics = lyrics;
		mCurrentIndex = 0;
		if (lyrics == null) return;
		mLyricsHandler.sendEmptyMessage(LYRICS_RESUMED);
	}
	
	public int getCurrentIndex() {
		return mCurrentIndex;
	}
	
	public void resume() {
		mLyricsHandler.sendEmptyMessage(LYRICS_RESUMED);
	}

	public void pause() {
		mLyricsHandler.sendEmptyMessage(LYRICS_PAUSED);
	}

	private Handler mLyricsHandler = new Handler() {


		@Override
		public void handleMessage(Message msg) {
			mLyricsHandler.removeMessages(LYRICS_REFRESHED);
			if (mLyrics == null) return;
			switch (msg.what) {
				case LYRICS_REFRESHED: {
					if (mCurrentIndex >= mLyrics.size()) return;
					mListener.onLyricsChanged(mLyrics.get(mCurrentIndex));
					mCurrentIndex++;
					if (mListener.isPlaying()) {
						final long delay = mLyrics.get(mCurrentIndex).getActualTime() - mListener.getPosition();
						mLyricsHandler.sendEmptyMessageDelayed(LYRICS_REFRESHED, delay);
					}
					break;
				}
				case LYRICS_RESUMED:
				case LYRICS_POSITION_CHANGED: {
					final long position = mListener.getPosition();
					final Lyrics.Line current = mLyrics.find(position);
					mListener.onLyricsChanged(current);
					mCurrentIndex = current.getIndex() + 1;
					if (mListener.isPlaying() && mCurrentIndex < mLyrics.size()) {
						final long delay = mLyrics.get(mCurrentIndex).getActualTime() - position;
						mLyricsHandler.sendEmptyMessageDelayed(LYRICS_REFRESHED, delay);
					}
					break;
				}
				case LYRICS_PAUSED: {
					break;
				}
			}
		}
	};


	public static interface Callbacks {

		public void onLyricsChanged(Lyrics.Line current);

		public boolean isPlaying();
		
		public long getPosition();
	}
}
