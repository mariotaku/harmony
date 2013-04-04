package org.mariotaku.harmony.fragment;

import android.os.Bundle;
import org.mariotaku.harmony.app.HarmonyApplication;
import org.mariotaku.harmony.util.ImageLoaderWrapper;

public class AlbumArtFragment extends BaseFragment {

	private ImageLoaderWrapper mImageLoader;
	
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mImageLoader = HarmonyApplication.getInstance(getActivity()).getImageLoaderWrapper();
	}
}
