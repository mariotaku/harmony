package org.mariotaku.harmony.util;

public class Utils {

	public static float limit(final float value, final float value1, final float value2) {
		final float min = Math.min(value1, value2), max = Math.max(value1, value2);
		return Math.max(Math.min(value, max), min);
	}

	public static int limit(final int value, final int value1, final int value2) {
		final int min = Math.min(value1, value2), max = Math.max(value1, value2);
		return Math.max(Math.min(value, max), min);
	}
}
