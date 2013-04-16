package org.mariotaku.harmony.view.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseGridViewHolder {
	
	public final TextView text1;
	public final TextView text2;
	public final ImageView icon;

	public BaseGridViewHolder(final View view) {
		text1 = (TextView) view.findViewById(android.R.id.text1);
		text2 = (TextView) view.findViewById(android.R.id.text2);
		icon = (ImageView) view.findViewById(android.R.id.icon);
	}

}
