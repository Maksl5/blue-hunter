/**
 *  PreferenceActivity.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.MenuItem;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActionBar actionBar = this.getActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onBuildHeaders(java.util.List)
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {

		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		super.onResume();

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:

				finish();

				break;

			default:
				break;
		}

		return true;
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

			if (!Authentification.newUpdateAvailable) {
				Preference newUpdatePref = findPreference("pref_newUpdateAvailable");
				PreferenceScreen infoScreen = getPreferenceScreen();
				infoScreen.removePreference(newUpdatePref);
			}

			initializeStaticPrefs();
			registerListeners();
		}

		/**
		 * 
		 */
		private void registerListeners() {

			// New Update Available - Click
			Preference newUpdatePref = findPreference("pref_newUpdateAvailable");
			if (newUpdatePref != null) {
				newUpdatePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						Intent browserIntent =
								new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Maksl5/blue-hunter/raw/master/bin/Blue%20Hunter.apk"));
						startActivity(browserIntent);

						return true;
					}
				});
			}

			// Show Changelog Click
			Preference showChangelogPref = findPreference("pref_changelog");
			if (showChangelogPref != null) {
				showChangelogPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						MainActivity main = MainActivity.thisActivity;
						main.authentification.showChangelog(InfoFragment.this.getActivity());

						return true;
					}
				});
			}

		}

		private void initializeStaticPrefs() {

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

	public static class BehaviourFragment extends PreferenceFragment {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.behaviour_preference);

			registerListeners();
		}

		private void registerListeners() {

			Preference showNotificationPref = findPreference("pref_showNotification");
			if (showNotificationPref != null) {
				showNotificationPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {

						MainActivity.thisActivity.alterNotification(newValue.equals(true));

						return true;
					}
				});
			}

		}

	}
}
