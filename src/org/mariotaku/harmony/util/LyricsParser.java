/*
 *              Copyright (C) 2011 The MusicMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.harmony.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mariotaku.harmony.model.Lyrics;
import org.mozilla.universalchardet.UniversalDetector;

public class LyricsParser {

	public static Lyrics parse(File file) throws IOException {
		final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		final byte[] buffer = new byte[1024];
		int size = 0;
		while ((size = in.read(buffer)) != -1) {
			out.write(buffer, 0, size);
		}
		in.close();
		final byte[] content = out.toByteArray();
		final UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(content, 0, content.length);
		detector.dataEnd();
		final String detected = detector.getDetectedCharset(), encoding = detected != null ? detected : null;
		return parseLRCString(new String(content, 0, content.length, encoding));
	}

	public static Lyrics parse(String path) throws IOException {
		if (path == null) throw new FileNotFoundException();
		return parse(new File(path));
	}

	private static final Pattern PATTERN_LRC_OFFSET = Pattern.compile("\\[offset:(\\d+)\\]", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_LRC_LINE = Pattern.compile("(\\[[0-9:\\.\\[\\]]+\\])+(.*)");
	private static final Pattern PATTERN_LRC_TIMESTAMP = Pattern.compile("\\[(\\d+):([0-9\\.]+)\\]");
	
	private static long parseLong(final String string, final long def) {
		if (string == null) return def;
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static float parseFloat(final String string, final float def) {
		if (string == null) return def;
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static Lyrics parseLRCString(final String string) throws LyricsParseException {

		final LyricsImpl lyrics = new LyricsImpl();
		// lyrics offset tag
		final Matcher matcher_offset = PATTERN_LRC_OFFSET.matcher(string);
		if (matcher_offset.find()) {
			lyrics.setOffset(parseLong(matcher_offset.group(1), 0));
		}

		// lyrics timestamp tag
		final Matcher matcher_lrc = PATTERN_LRC_LINE.matcher(string);
		while (matcher_lrc.find()) {
			Matcher matcher_timestamp = PATTERN_LRC_TIMESTAMP.matcher(matcher_lrc.group(1));
			while (matcher_timestamp.find()) {
				final String content = matcher_lrc.group(2);
				final long timestamp = parseLong(matcher_timestamp.group(1), 0) * 60000 + (long) (parseFloat(matcher_timestamp.group(2), 0) * 1000);
				lyrics.addLine(timestamp, content);
			}
		}
		lyrics.endOfLyrics();
		return lyrics;
	}
	
	private static final class LyricsImpl implements Lyrics {

		private final List<Long> timestamps = new ArrayList<Long>();
		private final HashMap<Long, String> map = new HashMap<Long, String>();

		private long offset;

		public Line get(final int index) {
			final int index_limited = limit(index, 0, timestamps.size() - 1);
			final long timestamp = timestamps.get(index_limited);
			return new LineImpl(timestamp, offset, index_limited, map.get(timestamp));
		}
		
		public Line find(final long current) {
			final int size = timestamps.size();
			for (int i = size - 1; i >= 0; i--) {
				final long timestamp = timestamps.get(i);
				if (timestamp <= current + offset) return new LineImpl(timestamp, offset, i, map.get(timestamp));
			}
			return get(0);
		}

		public Line findNoOffset(final long current) {
			return find(current - offset);
		}

		public Line[] getAll() {
			final int size = timestamps.size();
			final Line[] array = new Line[size];
			
			for (int i = 0; i < size; i++) {
				final long timestamp = timestamps.get(i);
				array[i] = new LineImpl(timestamp, offset, i, map.get(timestamp));
			}
			return array;
		}
		
		public long getOffset() {
			return offset;
		}
		
		public int size() {
			return map.size();
		}

		private void addLine(final long timestamp, final String content) {
			map.put(timestamp, content);
		}
		
		private void endOfLyrics() throws LyricsParseException {
			if (map.isEmpty()) throw new LyricsParseException();
			timestamps.addAll(map.keySet());
			Collections.sort(timestamps);
		}

		private void setOffset(long offset) {
			this.offset = offset;
		}

		private static int limit(final int value, final int min, final int max) {
			if (min > max) throw new IllegalArgumentException();
			if (value < min) return min;
			if (value > max) return max;
			return value;
		}

		private static final class LineImpl implements Line {

			private static final String LRC_TEMPLATE = "[%d:%.2f]%s";
			private final long time, actualTime;
			private final String text;
			private final int index;

			LineImpl(final long time, final long offset, final int index, final String text) {
				this.time = time;
				this.actualTime = time + offset;
				this.index = index;
				this.text = text;
			}
			
			public long getActualTime() {
				return actualTime;
			}

			public String getText() {
				return text;
			}
			
			public int getIndex() {
				return index;
			}

			public long getTime() {
				return time;
			}
			
			public String toString() {
				return text;
				//return String.format(LRC_TEMPLATE, time / 60000, (float) (time % 60000) / 1000, text);
			}
		}
		
	}
	
	public static final class LyricsParseException extends IOException {
		
	}
}
