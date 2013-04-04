package org.mariotaku.harmony.view.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumViewHolder {
	
	public final TextView album;
	public final TextView artist;
	public final ImageView album_art;

	public AlbumViewHolder(final View view) {
		album = (TextView) view.findViewById(android.R.id.text1);
		artist = (TextView) view.findViewById(android.R.id.text2);
		album_art = (ImageView) view.findViewById(android.R.id.icon);
	}

}
