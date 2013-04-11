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

package org.mariotaku.harmony.app;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;

public class AppearanceSettingsActivity extends PreferenceActivity implements Constants {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		getPreferenceManager().setSharedPreferencesName(SHAREDPREFS_PREFERENCES);
		addPreferencesFromResource(R.xml.appearance_settings);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent;
		switch (item.getItemId()) {
			case MENU_HOME:
				intent = new Intent(INTENT_PLAYBACK_VIEWER);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
