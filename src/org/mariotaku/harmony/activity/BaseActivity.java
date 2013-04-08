package org.mariotaku.harmony.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.model.TrackInfo;
import org.mariotaku.harmony.util.ServiceUtils;
import org.mariotaku.harmony.util.ServiceWrapper;

public class BaseActivity extends Activity implements Constants, ServiceConnection {

	private ServiceWrapper mService;
	private ServiceUtils.ServiceToken mToken;

	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_MEDIA_CHANGED.equals(action)) {
				onCurrentMediaChanged();
			} else if (BROADCAST_PLAY_STATE_CHANGED.equals(action)) {
				onPlayStateChanged();
			} else if (BROADCAST_SEEK_CHANGED.equals(action)) {
				onSeekChanged();
			} else if (BROADCAST_SHUFFLE_MODE_CHANGED.equals(action)) {
				onShuffleModeChanged();
			} else if (BROADCAST_REPEAT_MODE_CHANGED.equals(action)) {
				onRepeatModeChanged();
			}
		}

	};

	public final void onServiceConnected(final ComponentName service, final IBinder obj) {
		onServiceConnected(mService = ServiceWrapper.getInstance(obj));
	}

	public final void onServiceDisconnected(final ComponentName service) {
		onServiceDisconnected();
		mService = null;
	}
	
	protected final ServiceWrapper getServiceWrapper() {
		return mService;
	}

	protected void onCurrentMediaChanged() {

	}
	
	protected void onPlayStateChanged() {
		
	}

	protected void onRepeatModeChanged() {

	}
	
	protected void onSeekChanged() {
		
	}

	protected void onServiceConnected(final ServiceWrapper service) {

	}

	protected void onServiceDisconnected() {

	}

	protected void onShuffleModeChanged() {

	}
	
	protected void onStart() {
		super.onStart();
		mToken = ServiceUtils.bindToService(this, this);
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_PLAY_STATE_CHANGED);
		filter.addAction(BROADCAST_MEDIA_CHANGED);
		filter.addAction(BROADCAST_SEEK_CHANGED);
		filter.addAction(BROADCAST_SHUFFLE_MODE_CHANGED);
		filter.addAction(BROADCAST_REPEAT_MODE_CHANGED);
		registerReceiver(mMediaStatusReceiver, filter);
	}
	
	protected void onStop() {
		unregisterReceiver(mMediaStatusReceiver);
		ServiceUtils.unbindFromService(mToken);
		super.onStop();
	}

}
