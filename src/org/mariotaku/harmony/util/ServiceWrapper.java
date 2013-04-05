package org.mariotaku.harmony.util;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.mariotaku.harmony.IMusicPlaybackService;
import org.mariotaku.harmony.model.TrackInfo;

public class ServiceWrapper implements IMusicPlaybackService {
	
	public void openFile(String path) {
		// TODO: Implement this method
		try {
			mService.openFile(path);
		} catch (RemoteException e) {}
	}

	public void open(long[] list, int position) {
		// TODO: Implement this method
	}

	public int getQueuePosition() {
		// TODO: Implement this method
		return 0;
	}

	public boolean isPlaying() {
		try {
			return mService.isPlaying();
		} catch (RemoteException e) {}
		return false;
	}

	public void stop() {
		try {
			mService.stop();
		} catch (RemoteException e) {}
	}

	public void pause() {
		try {
			mService.pause();
		} catch (RemoteException e) {}
	}

	public void play() {
		try {
			mService.play();
		} catch (RemoteException e) {}
	}

	public void prev() {
		try {
			mService.prev();
		} catch (RemoteException e) {}
	}

	public void next() {
		try {
			mService.next();
		} catch (RemoteException e) {}
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
		// TODO: Implement this method
		try {
			return mService.getPosition();
		} catch (RemoteException e) {}
		return -1;
	}

	public long seek(long pos)  {
		// TODO: Implement this method
		try {
			return mService.seek(pos);
		} catch (RemoteException e) {}
		return -1;
	}

	public void enqueue(long[] list, int action) {
		// TODO: Implement this method
	}

	public void moveQueueItem(int from, int to) {
		// TODO: Implement this method
	}

	public long[] getQueue() {
		// TODO: Implement this method
		return null;
	}

	public void setQueuePosition(int index) {
		// TODO: Implement this method
	}

	public void setQueueId(long id) {
		// TODO: Implement this method
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
		return 0;
	}

	public int removeTrack(long id) {
		// TODO: Implement this method
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

	public void toggleFavorite() {
		// TODO: Implement this method
	}

	public void addToFavorites(long id) {
		// TODO: Implement this method
	}

	public void removeFromFavorites(long id) {
		// TODO: Implement this method
	}

	public boolean isFavorite(long id) {
		// TODO: Implement this method
		return false;
	}

	public boolean togglePause() {
		// TODO: Implement this method
		return false;
	}

	public IBinder asBinder() {
		// TODO: Implement this method
		return null;
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
