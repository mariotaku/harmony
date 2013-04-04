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
import org.mariotaku.harmony.util.ServiceUtils;
import org.mariotaku.harmony.util.ServiceWrapper;

public class BaseActivity extends Activity implements Constants, ServiceConnection {

	private ServiceWrapper mService;
	private ServiceUtils.ServiceToken mToken;

	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BROADCAST_MEDIA_CHANGED.equals(intent.getAction())) {
				onCurrentMediaChanged();
			} else if (BROADCAST_PLAYSTATE_CHANGED.equals(intent.getAction())) {
				onPlayStateChanged();
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

	protected void onServiceConnected(final ServiceWrapper service) {

	}

	protected void onServiceDisconnected() {

	}
	
	protected void onStart() {
		super.onStart();
		mToken = ServiceUtils.bindToService(this, this);
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_PLAYSTATE_CHANGED);
		filter.addAction(BROADCAST_MEDIA_CHANGED);
		registerReceiver(mMediaStatusReceiver, filter);
	}
	
	protected void onStop() {
		unregisterReceiver(mMediaStatusReceiver);
		ServiceUtils.unbindFromService(mToken);
		super.onStop();
	}

}
