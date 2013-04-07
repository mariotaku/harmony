package org.mariotaku.harmony.util;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.mariotaku.harmony.IMusicPlaybackService;
import org.mariotaku.harmony.model.TrackInfo;

public class ServiceWrapper implements IMusicPlaybackService {

	public void open(long[] list, int position) {
		try {
			mService.open(list, position);
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public int getQueuePosition() {
		try {
			return mService.getQueuePosition();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return -1;
	}

	public boolean isPlaying() {
		try {
			return mService.isPlaying();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return false;
	}

	public void stop() {
		try {
			mService.stop();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void pause() {
		try {
			mService.pause();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void play() {
		try {
			mService.play();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void prev() {
		try {
			mService.prev();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void next() {
		try {
			mService.next();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void cycleRepeat() {
		// TODO: Implement this method
	}

	public void toggleShuffle() {
		// TODO: Implement this method
	}

	public long getDuration() {
		try {
			return mService.getDuration();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return -1;
	}

	public long getPosition() {
		try {
			return mService.getPosition();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return -1;
	}

	public long seek(long pos)  {
		try {
			return mService.seek(pos);
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return -1;
	}

	public void enqueue(long[] list, int action) {
		try {
			mService.enqueue(list, action);
			} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void moveQueueItem(int from, int to) {
		try {
			mService.moveQueueItem(from, to);
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public long[] getQueue() {
		try {
			return mService.getQueue();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return null;
	}

	public void setQueuePosition(final int index) {
		try {
			mService.setQueuePosition(index);
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void setQueueId(long id) {
		try {
			mService.setQueueId(id);
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
	}

	public void setShuffleMode(int shufflemode) {
		// TODO: Implement this method
	}

	public int getShuffleMode() {
		// TODO: Implement this method
		return 0;
	}

	public int removeTracks(int first, int last) {
		// TODO: Implement this method
		try {
			return mService.removeTracks(first, last);
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return 0;
	}

	public int removeTrack(long id) {
		// TODO: Implement this method
		try {
			return mService.removeTrack(id);
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return 0;
	}

	public void setRepeatMode(int repeatmode) {
		// TODO: Implement this method
	}

	public int getRepeatMode() {
		// TODO: Implement this method
		return 0;
	}

	public void startSleepTimer(long millisecond, boolean gentle) {
		// TODO: Implement this method
	}

	public void stopSleepTimer() {
		// TODO: Implement this method
	}

	public long getSleepTimerRemained() {
		// TODO: Implement this method
		return 0;
	}

	public boolean togglePause() {
		try {
			return mService.togglePause();
		} catch (RemoteException e) {}
		return false;
	}

	public IBinder asBinder() {
		return mService.asBinder();
	}


	private static final String LOGTAG = "ServiceWrapper";
	private final IMusicPlaybackService mService;

	private ServiceWrapper(IBinder obj) {
		mService = IMusicPlaybackService.Stub.asInterface(obj);
	}

	public TrackInfo getTrackInfo() {
		if (mService == null) return null;
		try {
			return mService.getTrackInfo();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return null;
	}

	public int getAudioSessionId() {
		if (mService == null) return -1;
		try {
			return mService.getAudioSessionId();
		} catch (final RemoteException e) {
			Log.w(LOGTAG, e);
		}
		return -1;
	}

	public static ServiceWrapper getInstance(IBinder obj) {
		if (obj == null) return null;
		return new ServiceWrapper(obj);
	}
}
