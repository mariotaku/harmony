package org.mariotaku.harmony.util;

import org.mariotaku.harmony.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesEditor implements Constants {

	private Context context;

	public PreferencesEditor(Context context) {

		this.context = context;
	}

	public boolean getBooleanPref(String name, boolean def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getBoolean(name, def);
	}

	public boolean getBooleanState(String name, boolean def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getBoolean(name, def);
	}

	public float getFloatPref(String name, float def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getFloat(name, def);
	}

	public int getIntPref(String name, int def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	public int getIntState(String name, int def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	public long getLongState(String name, long def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getLong(name, def);
	}

	public String getStringPref(String name, String def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		return prefs.getString(name, def);
	}

	public String getStringState(String name, String def) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		return prefs.getString(name, def);
	}

	public void setBooleanPref(String name, boolean value) {

		SharedPreferences preferences = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public void setBooleanState(String name, boolean value) {

		SharedPreferences preferences = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public void setFloatPref(String name, float value) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putFloat(name, value);
		ed.commit();
	}

	public void setIntPref(String name, int value) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putInt(name, value);
		ed.commit();
	}

	public void setIntState(String name, int value) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putInt(name, value);
		ed.commit();
	}

	public void setLongState(String name, long value) {

		SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putLong(name, value);
		ed.commit();
	}

	public void setStringPref(String name, String value) {

		SharedPreferences preferences = context.getSharedPreferences(SHAREDPREFS_PREFERENCES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}

	public void setStringState(String name, String value) {

		SharedPreferences preferences = context.getSharedPreferences(SHAREDPREFS_STATES,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}
}
