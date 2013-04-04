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

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import org.mariotaku.harmony.Constants;
import org.mariotaku.harmony.R;
import org.mariotaku.harmony.activity.BaseActivity;
import org.mariotaku.harmony.util.MusicUtils;
import org.mariotaku.harmony.fragment.QueryFragment;

public class QueryBrowserActivity extends BaseActivity implements Constants, TextWatcher {

	private Intent intent;
	private Bundle bundle;
	private QueryFragment fragment;

	@Override
	public void afterTextChanged(Editable s) {

		// don't care about this one
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		// don't care about this one
	}

	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		configureActivity();

		intent = getIntent();
		bundle = icicle != null ? icicle : intent.getExtras();

		if (bundle == null) {
			bundle = new Bundle();
		}

		if (bundle.getString(INTENT_KEY_ACTION) == null) {
			bundle.putString(INTENT_KEY_ACTION, intent.getAction());
		}
		if (bundle.getString(INTENT_KEY_DATA) == null) {
			bundle.putString(INTENT_KEY_DATA, intent.getDataString());
		}
		if (bundle.getString(SearchManager.QUERY) == null) {
			bundle.putString(SearchManager.QUERY, intent.getStringExtra(SearchManager.QUERY));
		}
		if (bundle.getString(MediaStore.EXTRA_MEDIA_FOCUS) == null) {
			bundle.putString(MediaStore.EXTRA_MEDIA_FOCUS,
					intent.getStringExtra(MediaStore.EXTRA_MEDIA_FOCUS));
		}
		if (bundle.getString(MediaStore.EXTRA_MEDIA_ARTIST) == null) {
			bundle.putString(MediaStore.EXTRA_MEDIA_ARTIST,
					intent.getStringExtra(MediaStore.EXTRA_MEDIA_ARTIST));
		}
		if (bundle.getString(MediaStore.EXTRA_MEDIA_ALBUM) == null) {
			bundle.putString(MediaStore.EXTRA_MEDIA_ALBUM,
					intent.getStringExtra(MediaStore.EXTRA_MEDIA_ALBUM));
		}
		if (bundle.getString(MediaStore.EXTRA_MEDIA_TITLE) == null) {
			bundle.putString(MediaStore.EXTRA_MEDIA_TITLE,
					intent.getStringExtra(MediaStore.EXTRA_MEDIA_TITLE));
		}

		fragment = new QueryFragment(bundle);

		getFragmentManager().beginTransaction().replace(android.R.id.content, fragment)
				.commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent;
		switch (item.getItemId()) {
			case GOTO_HOME:
				intent = new Intent(INTENT_MUSIC_BROWSER);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outcicle) {
		outcicle.putAll(bundle);
		super.onSaveInstanceState(outcicle);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		Bundle args = fragment.getArguments();
		if (args == null) {
			args = new Bundle();
		}
		args.putString(INTENT_KEY_FILTER, s.toString());

		fragment.getLoaderManager().restartLoader(0, args, fragment);
	}

	private void configureActivity() {

		View mCustomView;

		setContentView(new FrameLayout(this));

		getActionBar().setCustomView(R.layout.actionbar_query_browser);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		mCustomView = getActionBar().getCustomView();

		if (mCustomView != null) {
			((EditText) mCustomView.findViewById(R.id.query_editor)).addTextChangedListener(this);

		}
	}

}
