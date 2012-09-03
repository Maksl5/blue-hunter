/**
 *  PreferenceActivity.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.util.List;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onBuildHeaders(java.util.List)
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {

		loadHeadersFromResource(R.xml.preference_headers, target);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
	
		// TODO Auto-generated method stub
		super.onResume();
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	/**
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	public static class InfoFragment extends PreferenceFragment {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.info_preference);

			initializeStaticPrefs();
		}

		private void initializeStaticPrefs() {

			SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();

			Preference infoPref = findPreference("pref_version");

			try {
				infoPref.setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
			}
			catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
