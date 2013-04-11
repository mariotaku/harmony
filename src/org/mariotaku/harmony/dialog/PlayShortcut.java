package org.mariotaku.harmony.dialog;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.activity.BaseActivity;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.util.ServiceWrapper;

public class PlayShortcut extends BaseActivity implements Constants {

	@Override
	protected void onServiceConnected(ServiceWrapper service) {
		final Intent intent = getIntent();
		final String action = intent.getAction();
		final long playlist_id = intent.getLongExtra(MAP_KEY_ID, PLAYLIST_UNKNOWN);
		if (INTENT_PLAY_SHORTCUT.equals(action) && playlist_id != PLAYLIST_UNKNOWN) {
			switch ((int) playlist_id) {
				case (int) PLAYLIST_ALL_SONGS: {
					MusicUtils.playAll(this, service);
					break;
				}
				case (int) PLAYLIST_RECENTLY_ADDED: {
					MusicUtils.playRecentlyAdded(this, service);
					break;
				}
				default: {
					if (playlist_id >= 0) {
						MusicUtils.playPlaylist(this, service, playlist_id);
					}
					break;
				}
			}
		} else {
			Toast.makeText(this, R.string.error_bad_parameters, Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	@Override
	protected void onServiceDisconnected() {
		finish();
	}

}
