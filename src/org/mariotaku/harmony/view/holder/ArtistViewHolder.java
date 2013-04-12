package org.mariotaku.harmony.view.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.mariotaku.harmony.view.ArtistAlbumsStackView;

public class ArtistViewHolder {

	public final TextView albums_count;
	public final TextView artist;
	public final ArtistAlbumsStackView albums;

	public ArtistViewHolder(final View view) {
		artist = (TextView) view.findViewById(android.R.id.text1);
		albums_count = (TextView) view.findViewById(android.R.id.text2);
		albums = (ArtistAlbumsStackView) view.findViewById(android.R.id.icon);
	}

}
