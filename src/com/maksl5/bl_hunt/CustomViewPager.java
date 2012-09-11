/**
 *  CustomViewPager.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class CustomViewPager extends ViewPager {

	/**
	 * @param context
	 * @param attrs
	 */
	public CustomViewPager(Context context,
			AttributeSet attrs) {

		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.ViewPager#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {

		if (this.getVisibility() != ViewPager.GONE) {
			return super.onTouchEvent(arg0);
		}
		else {
			return false;
		}

	}

}
