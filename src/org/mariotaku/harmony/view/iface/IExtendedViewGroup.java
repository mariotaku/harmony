package org.mariotaku.harmony.view.iface;

import android.view.MotionEvent;
import android.view.ViewGroup;

public interface IExtendedViewGroup extends IExtendedView {

	public void setTouchInterceptor(final TouchInterceptor listener);

	public static interface TouchInterceptor {

		boolean dispatchTouchEvent(ViewGroup view, MotionEvent event);

		boolean onInterceptTouchEvent(ViewGroup view, MotionEvent event);

		boolean onTouchEvent(ViewGroup view, MotionEvent event);

	}

}
