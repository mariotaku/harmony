package org.mariotaku.harmony.util;

import org.mariotaku.harmony.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesEditor implements Constants {

	private Context mContext;

	public PreferencesEditor(Context context) {
		mContext = context;
	}

	public boolean getBooleanPref(String name, boolean def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getBoolean(name, def);
	}

	public boolean getBooleanState(String name, boolean def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getBoolean(name, def);
	}

	public float getFloatPref(String name, float def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getFloat(name, def);
	}

	public int getIntPref(String name, int def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	public int getIntState(String name, int def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	public long getLongState(String name, long def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getLong(name, def);
	}

	public String getStringPref(String name, String def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getString(name, def);
	}

	public String getStringState(String name, String def) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getString(name, def);
	}

	public void setBooleanPref(String name, boolean value) {

		SharedPreferences preferences = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(name, value);
		editor.apply();
	}

	public void setBooleanState(String name, boolean value) {

		SharedPreferences preferences = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(name, value);
		editor.apply();
	}

	public void setFloatPref(String name, float value) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putFloat(name, value);
		ed.apply();
	}

	public void setIntPref(String name, int value) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putInt(name, value);
		ed.apply();
	}

	public void setIntState(String name, int value) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putInt(name, value);
		ed.apply();
	}

	public void setLongState(String name, long value) {

		SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putLong(name, value);
		ed.apply();
	}

	public void setStringPref(String name, String value) {

		SharedPreferences preferences = mContext.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.apply();
	}

	public void setStringState(String name, String value) {

		SharedPreferences preferences = mContext.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.apply();
	}
}
