/**
 *  BlueHunter.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;


/**
 * @author Maksl5[Markus Bensing]
 *
 */
@ReportsCrashes(formKey = "dFpyWWtjQ1E3VV9EaklYbFZETmpQLVE6MQ")
public class BlueHunter extends Application {

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {

		// TODO Auto-generated method stub
		super.onCreate();
		
		
		ACRA.init(this);
	}

}
