package org.mariotaku.harmony.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import java.util.HashMap;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.IMusicPlaybackService;
import org.mariotaku.harmony.MusicPlaybackService;

public class ServiceUtils implements Constants {

	private static final String LOGTAG = "ServiceUtils";

	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

	public static ServiceToken bindToService(Activity context) {
		return bindToService(context, null);
	}

	public static ServiceToken bindToService(Context context, ServiceConnection callback) {

		ContextWrapper cw = new ContextWrapper(context);
		cw.startService(new Intent(cw, MusicPlaybackService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (cw.bindService(new Intent(cw, MusicPlaybackService.class), sb, 0)) {
			sConnectionMap.put(cw, sb);
			return new ServiceToken(cw);
		}
		Log.e("Music", "Failed to bind to service");
		return null;
	}

	public static void unbindFromService(ServiceToken token) {

		if (token == null) {
			Log.e(LOGTAG, "Trying to unbind with null token");
			return;
		}
		ContextWrapper wrapper = token.mWrappedContext;
		ServiceBinder binder = sConnectionMap.remove(wrapper);
		if (binder == null) {
			Log.e(LOGTAG, "Trying to unbind for unknown Context");
			return;
		}
		wrapper.unbindService(binder);
		if (sConnectionMap.isEmpty()) {
			// presumably there is nobody interested in the service at this
			// point,
			// so don't hang on to the ServiceConnection
			MusicUtils.sService = null;
		}
	}

	public static class ServiceToken {

		ContextWrapper mWrappedContext;

		ServiceToken(ContextWrapper context) {

			mWrappedContext = context;
		}
	}

	private static class ServiceBinder implements ServiceConnection {

		ServiceConnection mCallback;

		ServiceBinder(ServiceConnection callback) {

			mCallback = callback;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			if (MusicUtils.sService == null) {
				MusicUtils.sService = IMusicPlaybackService.Stub.asInterface(service);
			}
			if (mCallback != null) {
				mCallback.onServiceConnected(className, service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			if (mCallback != null) {
				mCallback.onServiceDisconnected(className);
			}
		}
	}

}
