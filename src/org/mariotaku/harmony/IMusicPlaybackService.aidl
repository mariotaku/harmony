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

import android.net.Uri;

import org.mariotaku.harmony.model.TrackInfo;

interface IMusicPlaybackService {
	
	void play();
	void pause();
	void prev();
	void next();
	void stop();
	long seek(long pos);
	boolean togglePause();
	boolean isPlaying();
	TrackInfo getTrackInfo();
	long getDuration();
	long getPosition();

	void open(in long [] list, int position);
	long [] getQueue();
	int getQueuePosition();
	void setQueuePosition(int position);
	void setQueueId(long id);
	void moveQueueItem(int from, int to);
	void enqueue(in long [] list, int action);
	int removeTracks(int first, int last);
	int removeTrack(long id);
	
	void setShuffleMode(int mode);
	void setRepeatMode(int mode);
	int getShuffleMode();
	int getRepeatMode();
	
	int getAudioSessionId();
	void startSleepTimer(long millisecond, boolean gentle);
	void stopSleepTimer();
	long getSleepTimerRemained();

}
