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

package org.mariotaku.harmony;

import org.mariotaku.harmony.activity.MusicBrowserActivity;
import org.mariotaku.harmony.activity.MusicPlaybackActivity;
import org.mariotaku.harmony.util.MusicUtils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Simple widget to show currently playing album art along with play/pause and
 * next track buttons.
 */
public class MediaAppWidgetProvider4x1 extends AppWidgetProvider implements Constants {

	private static String mTrackName, mTrackDetail;
	private static long mAlbumId, mAudioId;
	private static boolean mIsPlaying;
	private static String[] mLyrics = new String[] {};
	private static int mLyricsStat;

	@Override
	public void onReceive(Context context, Intent intent) {
		performUpdate(context, intent);
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		init(context, appWidgetIds);

	}

	/**
	 * Initialize given widgets to default state, where we launch Music on
	 * default click and hide actions if service not running.
	 */
	private void init(Context context, int[] appWidgetIds) {

		final Resources res = context.getResources();
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.album_appwidget4x1);

		views.setViewVisibility(R.id.track_name, View.GONE);
		views.setTextViewText(R.id.track_detail, res.getText(R.string.widget_initial_text));
		views.setViewVisibility(R.id.album_art, View.GONE);
		views.setViewVisibility(R.id.lyrics_line, View.GONE);

		linkButtons(context, views, false);
		pushUpdate(context, appWidgetIds, views);
	}

	/**
	 * Link up various button actions using {@link PendingIntents}.
	 * 
	 * @param isPlaying
	 *            True if player is active in background, which means widget
	 *            click will launch {@link MusicPlaybackActivity}, otherwise we
	 *            launch {@link MusicBrowserActivity}.
	 */
	private void linkButtons(Context context, RemoteViews views, boolean isPlaying) {

		// Connect up various buttons and touch events
		PendingIntent pendingIntent;

		if (isPlaying) {
			pendingIntent = PendingIntent.getActivity(context, 0,
					new Intent(INTENT_PLAYBACK_VIEWER), 0);
			views.setOnClickPendingIntent(R.id.album_appwidget, pendingIntent);
		} else {
			pendingIntent = PendingIntent.getActivity(context, 0, new Intent(INTENT_MUSIC_BROWSER),
					0);
			views.setOnClickPendingIntent(R.id.album_appwidget, pendingIntent);
		}

		final ComponentName serviceName = new ComponentName(context, MusicPlaybackService.class);

		pendingIntent = PendingIntent.getService(context, 0,
				new Intent(TOGGLEPAUSE_ACTION).setComponent(serviceName), 0);
		views.setOnClickPendingIntent(R.id.control_play, pendingIntent);

		pendingIntent = PendingIntent.getService(context, 0,
				new Intent(NEXT_ACTION).setComponent(serviceName), 0);
		views.setOnClickPendingIntent(R.id.control_next, pendingIntent);
	}

	/**
	 * Update all active widget instances by pushing changes
	 */
	private void performUpdate(Context context, Intent intent) {

		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this
				.getClass()));

		final Resources res = context.getResources();
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.album_appwidget4x1);

		if (BROADCAST_MEDIA_CHANGED.equals(intent.getAction())
				|| BROADCAST_PLAY_STATE_CHANGED.equals(intent.getAction())) {
			mTrackName = intent.getStringExtra(BROADCAST_KEY_TRACK);
			String artist = intent.getStringExtra(BROADCAST_KEY_ARTIST);
			String album = intent.getStringExtra(BROADCAST_KEY_ALBUM);

			if (artist != null && !MediaStore.UNKNOWN_STRING.equals(artist)) {
				mTrackDetail = artist;
			} else if (album != null && !MediaStore.UNKNOWN_STRING.equals(album)) {
				mTrackDetail = album;
			} else {
				mTrackDetail = null;
			}

			mAlbumId = intent.getLongExtra(BROADCAST_KEY_ALBUMID, -1);
			mAudioId = intent.getLongExtra(BROADCAST_KEY_SONGID, -1);
			mIsPlaying = intent.getBooleanExtra(BROADCAST_KEY_PLAYING, false);
			views.setTextViewText(R.id.lyrics_line, "");
		} else if (BROADCAST_NEW_LYRICS_LOADED.equals(intent.getAction())) {
			mLyrics = intent.getStringArrayExtra(BROADCAST_KEY_LYRICS);
			mLyricsStat = intent.getIntExtra(BROADCAST_KEY_LYRICS_STATUS, LYRICS_STATUS_INVALID);
		}

		CharSequence errorState = null;

		// Format title string with track number, or show SD card message
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_SHARED) || status.equals(Environment.MEDIA_UNMOUNTED)) {
			errorState = res.getText(R.string.sdcard_busy_title);
		} else if (status.equals(Environment.MEDIA_REMOVED)) {
			errorState = res.getText(R.string.sdcard_missing_title);
		} else if (mTrackName == null) {
			errorState = res.getText(R.string.emptyplaylist);
		}

		if (errorState != null) {
			// Show error state to user
			views.setViewVisibility(R.id.track_name, View.GONE);
			views.setTextViewText(R.id.track_detail, errorState);
			views.setViewVisibility(R.id.album_art, View.GONE);
			views.setViewVisibility(R.id.lyrics_line, View.GONE);
		} else {
			// No error, so show normal titles and artwork
			views.setViewVisibility(R.id.track_name, View.VISIBLE);
			views.setViewVisibility(R.id.lyrics_line, View.VISIBLE);
			views.setTextViewText(R.id.track_name, mTrackName);
			views.setViewVisibility(R.id.album_art, mTrackDetail != null ? View.VISIBLE : View.GONE);
			if (mTrackDetail != null) {
				views.setTextViewText(R.id.track_detail, mTrackDetail);
			}
			// Set album art
			Uri uri = null;
			if (mAudioId >= 0 && mAlbumId >= 0) {
				uri = MusicUtils.getArtworkUri(context, mAudioId, mAlbumId);
			}
			if (uri != null) {
				views.setImageViewUri(R.id.album_art, uri);
			} else {
				views.setImageViewResource(R.id.album_art, R.drawable.ic_mp_albumart_unknown);
			}
			if (BROADCAST_LYRICS_REFRESHED.equals(intent.getAction())) {
				int lyrics_id = intent.getIntExtra(BROADCAST_KEY_LYRICS_ID, -1);
				if (mLyrics != null && mLyrics.length > lyrics_id && lyrics_id >= 0) {
					if (mLyricsStat == LYRICS_STATUS_OK) {
						views.setViewVisibility(R.id.track_name, View.VISIBLE);
						views.setTextViewText(R.id.lyrics_line, mLyrics[lyrics_id]);
					} else {
						views.setViewVisibility(R.id.lyrics_line, View.GONE);
						views.setTextViewText(R.id.lyrics_line, "");
					}
				} else {
					views.setViewVisibility(R.id.lyrics_line, View.GONE);
				}
			}
		}

		if (mIsPlaying) {
			views.setImageViewResource(R.id.control_play, R.drawable.btn_playback_ic_pause);
		} else {
			views.setImageViewResource(R.id.control_play, R.drawable.btn_playback_ic_play);
		}

		linkButtons(context, views, mIsPlaying);
		pushUpdate(context, appWidgetIds, views);
	}

	private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {

		// Update specific list of appWidgetIds if given, otherwise default to
		// all
		final AppWidgetManager gm = AppWidgetManager.getInstance(context);
		if (appWidgetIds != null) {
			gm.updateAppWidget(appWidgetIds, views);
		} else {
			gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
		}
	}
}
