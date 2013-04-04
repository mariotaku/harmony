package org.mariotaku.harmony.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Albums;
import android.text.TextUtils;
import android.content.ContentResolver;

public class TrackInfo implements Parcelable {

	public static final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable.Creator<TrackInfo>() {

		public TrackInfo createFromParcel(final Parcel in) {
			return new TrackInfo(in);
		}

		public TrackInfo[] newArray(final int size) {
			return new TrackInfo[size];
		}
		
	};
	
	public final String title, album, artist, data;
	public final long track_id, album_id, artist_id;

	public TrackInfo(final Cursor cursor) {
		title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
		album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
		artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
		track_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
		album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
		artist_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
		data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
	}

	public TrackInfo(final Parcel in) {
		title = in.readString();
		album = in.readString();
		artist = in.readString();
		track_id = in.readLong();
		album_id = in.readLong();
		artist_id = in.readLong();
		data = in.readString();
	}

	public int describeContents() {
		return 0;
	}
	
	public String toString() {
		return "TrackInfo{album=" + album + ",album_id=" + album_id + ",artist=" + artist + ",artist_id,"
				+ artist_id + "data=" + data + ",title=" + title + ",track_id=" + track_id + "}";
	}

	public void writeToParcel(final Parcel out, final int flags) {
		out.writeString(title);
		out.writeString(album);
		out.writeString(artist);
		out.writeLong(track_id);
		out.writeLong(album_id);
		out.writeLong(artist_id);
		out.writeString(data);
	}
	
	public static boolean isUnknownAlbum(final TrackInfo info) {
		if (info == null) return true;
		return TextUtils.isEmpty(info.album) || MediaStore.UNKNOWN_STRING.equals(info.album);
	}
	
	public static boolean isUnknownArtist(final TrackInfo info) {
		if (info == null) return true;
		return TextUtils.isEmpty(info.artist) || MediaStore.UNKNOWN_STRING.equals(info.artist);
	}

}
