/*
 *			Copyright (C) 2012 The MusicMod Open Source Project
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *				http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */

package org.mariotaku.harmony.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class LyricsDownloader {

	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F' };

	private static final String UTF_16LE = "utf-16le";
	private static final String UTF_8 = "utf-8";

	public OnProgressChangeListener mListener;

	public LyricsDownloader() {

	}

	/**
	 * Download lyrics from server
	 * 
	 * @param id
	 *            Id of selected item.
	 * @param file
	 *            Destination file.
	 */
	public void download(SearchResult result, File file) throws IOException {

		final String url_string = buildDownloadUrl(result.getId(), result.getVerifyCode());

		URL url = new URL(url_string);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.connect();
		FileOutputStream output = new FileOutputStream(file);
		InputStream input = connection.getInputStream();
		int total_size = connection.getContentLength();
		int downloaded_size = 0;
		byte[] buffer = new byte[1024];
		int buffer_length = 0;
		while ((buffer_length = input.read(buffer)) > 0) {
			output.write(buffer, 0, buffer_length);
			downloaded_size += buffer_length;
			if (mListener != null) {
				mListener.onProgressChange(downloaded_size, total_size);
			}
		}
		output.close();
	}

	/**
	 * Download lyrics from server
	 * 
	 * @param id
	 *            Id of selected item.
	 * @param file
	 *            Destination file path.
	 */
	public void download(SearchResult result, String path) throws IOException {
		download(result, new File(path));
	}

	public void removeOnProgressChangeListener(OnProgressChangeListener listener) {
		mListener = null;
	}

	/**
	 * Search lyrics from server
	 * 
	 * @return result list in string array.
	 * 
	 * @param artist
	 *            The artist of sound track.
	 * @param track
	 *            The name of sound track.
	 */
	public SearchResult[] search(String artist, String track) throws XmlPullParserException, IOException {

		URL url = new URL(buildSearchUrl(encode(artist), encode(track)));
		return parseResult(url.openStream());
	}

	public void setOnProgressChangeListener(OnProgressChangeListener listener) {

		mListener = listener;
	}

	private String encode(String source) {


		if (source == null) {
			source = "";
		}

		source = source.replaceAll("[\\p{P} ]", "").toLowerCase();
		byte[] bytes = null;

		try {
			bytes = source.getBytes(UTF_16LE);
		} catch (Exception e) {
			e.printStackTrace();
			bytes = source.getBytes();
		}

		char[] charactor = new char[2];
		StringBuilder builder = new StringBuilder();
		for (byte byteValue : bytes) {
			charactor[0] = HEX_DIGITS[byteValue >>> 4 & 0X0F];
			charactor[1] = HEX_DIGITS[byteValue & 0X0F];
			builder.append(charactor);
		}
		return builder.toString();
	}

	private static final String TAG_RESULT = "result";
	private static final String TAG_LRC = "lrc";
	private static final String ATTR_ID = "id";
	private static final String ATTR_ARTIST = "artist";
	private static final String ATTR_TITLE = "title";
	
	private SearchResult[] parseResult(final InputStream is) throws XmlPullParserException, IOException {

		final ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);

		XmlPullParser parser = factory.newPullParser();
		parser.setInput(new InputStreamReader(is));

		int eventType = parser.getEventType();
		String tagName;
		boolean lookingForEndOfUnknownTag = false;
		String unknownTagName = null;

		// This loop will skip to the result start tag
		do {
			if (eventType == XmlPullParser.START_TAG) {
				tagName = parser.getName();
				if (TAG_RESULT.equals(tagName)) {
					// Go to next tag
					eventType = parser.next();
					break;
				}
				throw new IOException("Expecting result, got " + tagName);
			}
			eventType = parser.next();
		} while (eventType != XmlPullParser.END_DOCUMENT);

		boolean reachedEndOfResult = false;
		while (!reachedEndOfResult) {
			switch (eventType) {
				case XmlPullParser.START_TAG:
					if (lookingForEndOfUnknownTag) {
						break;
					}
					tagName = parser.getName();
					if (TAG_LRC.equals(tagName)) {
						final String artist = parser.getAttributeValue(parser.getNamespace(), ATTR_ARTIST);
						final String title = parser.getAttributeValue(parser.getNamespace(), ATTR_TITLE);
						final int id = Integer.valueOf(parser.getAttributeValue(parser.getNamespace(), ATTR_ID));
						final String verifyCode = generateVerifyCode(artist, title, id);
						if (title != null && id > 0 && verifyCode != null) {
							resultList.add(new SearchResultImpl(id, verifyCode, title, artist));
						}
					} else {
						lookingForEndOfUnknownTag = true;
						unknownTagName = tagName;
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
						lookingForEndOfUnknownTag = false;
						unknownTagName = null;
					} else if (TAG_RESULT.equals(tagName)) {
						reachedEndOfResult = true;
					}
					break;
			}
			eventType = parser.next();
		}
		return resultList.toArray(new SearchResult[resultList.size()]);
	}

	private static String generateVerifyCode(final String artist, final String title, final int id) {

			final byte[] bytes;
			try {
				bytes = (artist + title).getBytes(UTF_8);
			} catch (UnsupportedEncodingException e) {
				return null;
			}
			int[] song = new int[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				song[i] = bytes[i] & 0xff;
			}
			int intVal1 = 0, intVal2 = 0, intVal3 = 0;
			intVal1 = (id & 0xFF00) >> 8;
			if ((id & 0xFF0000) == 0) {
				intVal3 = 0xFF & ~intVal1;
			} else {
				intVal3 = 0xFF & (id & 0x00FF0000) >> 16;
			}

			intVal3 = intVal3 | (0xFF & id) << 8;
			intVal3 = intVal3 << 8;
			intVal3 = intVal3 | 0xFF & intVal1;
			intVal3 = intVal3 << 8;

			if ((id & 0xFF000000) == 0) {
				intVal3 = intVal3 | 0xFF & ~id;
			} else {
				intVal3 = intVal3 | 0xFF & id >> 24;
			}

			int uBound = bytes.length - 1;
			while (uBound >= 0) {
				int c = song[uBound];
				if (c >= 0x80) {
					c = c - 0x100;
				}
				intVal1 = c + intVal2;
				intVal2 = intVal2 << uBound % 2 + 4;
				intVal2 = intVal1 + intVal2;
				uBound -= 1;
			}

			uBound = 0;
			intVal1 = 0;

			while (uBound <= bytes.length - 1) {
				int c = song[uBound];
				if (c >= 128) {
					c -= 256;
				}
				int intVal4 = c + intVal1;
				intVal1 = intVal1 << uBound % 2 + 3;
				intVal1 = intVal1 + intVal4;
				uBound += 1;
			}

			int intVal5 = intVal2 ^ intVal3;
			intVal5 = intVal5 + (intVal1 | id);
			intVal5 = intVal5 * (intVal1 | intVal3);
			intVal5 = intVal5 * (intVal2 ^ id);

			return String.valueOf(intVal5);
	}

	private static String buildDownloadUrl(int id, String code) {
		return "http://ttlrcct.qianqian.com/dll/lyricsvr.dll?dl?Id=" + id + "&Code=" + code;
	}

	private static String buildSearchUrl(String artist, String track) {
		return "http://ttlrcct.qianqian.com/dll/lyricsvr.dll?sh?Artist=" + artist + "&Title="
				+ track + "&Flags=0";
	}

	public interface OnProgressChangeListener {

		void onProgressChange(int progress, int total);
	}
	
	public static interface SearchResult {
		
		public int getId();
		
		public String getVerifyCode();
		
		public String getArtist();
		
		public String getTitle();
		
	}
	
	private static final class SearchResultImpl implements SearchResult {

		private final String artist;
		private final String title;
		private final String verifyCode;
		private final int id;
		
		public SearchResultImpl(final int id, final String verifyCode, final String title, final String artist) {
			this.id = id;
			this.verifyCode = verifyCode;
			this.title = title;
			this.artist = artist;
		}
		
		public String getArtist() {
			return artist;
		}

		public String getTitle() {
			return title;
		}

		public int getId() {
			return id;
		}

		public String getVerifyCode() {
			return verifyCode;
		}
		
	}
}
