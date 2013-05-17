package org.mariotaku.harmony.util;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class Utils {

	public static float limit(final float value, final float value1, final float value2) {
		final float min = Math.min(value1, value2), max = Math.max(value1, value2);
		return Math.max(Math.min(value, max), min);
	}

	public static int limit(final int value, final int value1, final int value2) {
		final int min = Math.min(value1, value2), max = Math.max(value1, value2);
		return Math.max(Math.min(value, max), min);
	}
	
	public static long[] getCursorIds(final Cursor cursor) {
		if (cursor == null || cursor.isClosed()) return null;
		final int pos_backup = cursor.getPosition(), size = cursor.getCount(), idx = cursor.getColumnIndexOrThrow(BaseColumns._ID);
		cursor.moveToFirst();
		final long[] ids = new long[size];
		for (int i = 0; i < size; i++) {
			cursor.moveToPosition(i);
			ids[i] = cursor.getLong(idx);
		}
		cursor.moveToPosition(pos_backup);
		return ids;
	}
	
	public static final int parseInt(final String string, final int def) {
		if (TextUtils.isEmpty(string)) return def;
		try {
			return Integer.parseInt(string);
		} catch (final NumberFormatException e) {
			return def;
		}
	}
}
