/**
 *  CustomPagerTransformer.java in com.maksl5.bl_hunt.custom_ui
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.custom_ui;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

/**
 * @author Maksl5[Markus Bensing]
 *
 */
public class CustomPagerTransformer implements PageTransformer {

	private static float MIN_SCALE = 0.85f;
	private static float MIN_ALPHA = 0.5f;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.view.ViewPager.PageTransformer#transformPage(android
	 * .view.View, float)
	 */
	@Override
	public void transformPage(View view, float position) {

		int pageWidth = view.getWidth();
		int pageHeight = view.getHeight();

		if (position < -1 || position > 1) {
			view.setAlpha(0);
		}
		else if (Math.abs(position) <= 1) {
			float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
			float vertMargin = pageHeight * (1 - scaleFactor) / 2;
			float horzMargin = pageWidth * (1 - scaleFactor) / 2;
			if (position < 0) {
				view.setTranslationX(horzMargin - vertMargin / 2);
			}
			else {
				view.setTranslationX(-horzMargin + vertMargin / 2);

			}

			// Scale the page down (between MIN_SCALE and 1)
			view.setScaleX(scaleFactor);
			view.setScaleY(scaleFactor);

			// Fade the page relative to its size.
			view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
		}

	}

}
