package com.maksl5.bl_hunt.activity;



import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.DiscoveryManager;
import com.maksl5.bl_hunt.DiscoveryManager.DiscoveryState;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.custom_ui.ColorSystem;
import com.maksl5.bl_hunt.custom_ui.CustomPagerTransformer;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.NetworkManager;
import com.maksl5.bl_hunt.net.NetworkThread;
import com.maksl5.bl_hunt.net.SynchronizeFoundDevices;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;



public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory.
	 * If this becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	public static final int REQ_PICK_USER_IMAGE = 32;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;

	private BlueHunter bhApp;

	protected NotificationManager notificationManager;
	public Notification.Builder stateNotificationBuilder;

	protected TextView disStateTextView;
	public TextView userInfoTextView;

	public boolean passSet = false;

	public int exp = 0;

	private boolean destroyed;

	public int oldVersion = 0;
	public int newVersion = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Debug.startMethodTracing("blHunt_12");
		
		bhApp = (BlueHunter) getApplication();
		bhApp.mainActivity = this;
		bhApp.currentActivity = this;

		destroyed = false;
		setContentView(R.layout.act_main);

		bhApp.actionBarHandler = new ActionBarHandler(bhApp);
		bhApp.disMan = new DiscoveryManager(bhApp);

		// Create the adapter that will return a fragment for each of the primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		bhApp.authentification = new Authentification(bhApp);
		bhApp.netMananger = new NetworkManager(bhApp);

		bhApp.loginManager =
				bhApp.authentification.new LoginManager(Authentification.getSerialNumber(), bhApp.authentification.getStoredPass(), bhApp.authentification.getStoredLoginToken());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(5);
		mViewPager.setPageTransformer(true, new CustomPagerTransformer());

		registerListener();

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		stateNotificationBuilder = new Notification.Builder(this);

		stateNotificationBuilder.setOngoing(true);
		stateNotificationBuilder.setSmallIcon(R.drawable.ic_launcher);
		stateNotificationBuilder.setAutoCancel(false);
		stateNotificationBuilder.setContentTitle(DiscoveryState.getUnformatedDiscoveryState(DiscoveryState.DISCOVERY_STATE_STOPPED, this));
		stateNotificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0));

		if (PreferenceManager.getPref(bhApp, "pref_enableBackground", false)) {
			try {
				getWindow().setBackgroundDrawableResource(R.drawable.bg_main);
			}
			catch (Exception e) {
				PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
			}
			catch (OutOfMemoryError e) {
				PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
			}

		}

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

		bhApp.currentActivity = this;

	}

	/**
	 * 
	 */

	private void registerListener() {

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				bhApp.actionBarHandler.changePage(position);

			}

			@Override
			public void onPageScrolled(	int position,
										float positionOffset,
										int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

				switch (state) {

				case ViewPager.SCROLL_STATE_DRAGGING:

				case ViewPager.SCROLL_STATE_IDLE:

				case ViewPager.SCROLL_STATE_SETTLING:

				}

			}
		});

		bhApp.authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

			@Override
			public boolean onResult(int requestId,
									String resultString) {

				switch (requestId) {
				case Authentification.NETRESULT_ID_SERIAL_CHECK:

					Pattern pattern = Pattern.compile("Error=(\\d+)");
					Matcher matcher = pattern.matcher(resultString);

					if (matcher.find()) {
						int error = Integer.parseInt(matcher.group(1));

						String errorMsg = ErrorHandler.getErrorString(bhApp, requestId, error);
						FragmentLayoutManager.ProfileLayout.setName(bhApp, errorMsg, true);

						Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
						break;
					}

					Pattern xmlPattern = Pattern.compile("<done name=\"(.+)\" />");
					Matcher xmlMatcher = xmlPattern.matcher(resultString);

					if (xmlMatcher.find()) {

						String nameString = "";

						try {
							nameString = URLDecoder.decode(xmlMatcher.group(1), "UTF-8");
						}
						catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						FragmentLayoutManager.ProfileLayout.setName(bhApp, nameString, false);

					}

					bhApp.loginManager.login();
					break;
				case Authentification.NETRESULT_ID_GET_USER_INFO:

					Pattern pattern2 = Pattern.compile("Error=(\\d+)");
					Matcher matcher2 = pattern2.matcher(resultString);

					if (matcher2.find()) {
						int error = Integer.parseInt(matcher2.group(1));

						String errorMsg = ErrorHandler.getErrorString(bhApp, requestId, error);

						userInfoTextView.setText(errorMsg);
						userInfoTextView.setVisibility(View.VISIBLE);
						userInfoTextView.setClickable(true);
						userInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
						userInfoTextView.setLinkTextColor(Color.DKGRAY);
						userInfoTextView.requestLayout();
						userInfoTextView.invalidate();

						TableRow userInfoRow = (TableRow) findViewById(R.id.userInfoTableRow);
						userInfoRow.setVisibility(View.VISIBLE);
						userInfoRow.invalidate();
						userInfoRow.setVisibility(View.INVISIBLE);
						return true;
					}

					userInfoTextView.setText(Html.fromHtml(resultString));
					userInfoTextView.setVisibility(View.VISIBLE);
					userInfoTextView.setClickable(true);
					userInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
					userInfoTextView.setLinkTextColor(Color.DKGRAY);
					userInfoTextView.requestLayout();
					userInfoTextView.invalidate();

					TableRow userInfoRow = (TableRow) findViewById(R.id.userInfoTableRow);
					userInfoRow.setVisibility(View.VISIBLE);
					userInfoRow.invalidate();
					userInfoRow.setVisibility(View.INVISIBLE);
					break;
				}

				return false;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		bhApp.currentActivity = this;

		getMenuInflater().inflate(R.menu.act_main, menu);
		bhApp.actionBarHandler.supplyMenu(menu);
		bhApp.actionBarHandler.initialize();

		// Setting up Views
		if (disStateTextView == null)
			disStateTextView = (TextView) findViewById(R.id.txt_discoveryState);
		// Setting up DiscoveryManager

		if (!bhApp.disMan.startDiscoveryManager()) {
			if (!bhApp.disMan.supplyTextView(disStateTextView)) {

				// ERROR
			}
			else {
				bhApp.disMan.startDiscoveryManager();
			}
		}

		// Network Stuff

		bhApp.authentification.checkUpdate();

		NetworkThread serialSubmit = new NetworkThread(bhApp);
		serialSubmit.execute(AuthentificationSecure.SERVER_CHECK_SERIAL, String.valueOf(Authentification.NETRESULT_ID_SERIAL_CHECK), "s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(), "h=" + bhApp.authentification.getSerialNumberHash());

		new DatabaseManager(bhApp, bhApp.getVersionCode()).close();

		bhApp.synchronizeFoundDevices = new SynchronizeFoundDevices(bhApp);
		bhApp.authentification.setOnLoginChangeListener(bhApp.synchronizeFoundDevices);

		userInfoTextView = (TextView) findViewById(R.id.userInfoTxtView);

		TableRow userInfoRow = (TableRow) findViewById(R.id.userInfoTableRow);
		userInfoRow.setVisibility(View.VISIBLE);
		userInfoRow.invalidate();
		userInfoRow.setVisibility(View.INVISIBLE);

		NetworkThread getUserInfo = new NetworkThread(bhApp);
		getUserInfo.execute(AuthentificationSecure.SERVER_GET_USER_INFO, String.valueOf(Authentification.NETRESULT_ID_GET_USER_INFO));

		FragmentLayoutManager.FoundDevicesLayout.refreshFoundDevicesList(bhApp);
		FragmentLayoutManager.DeviceDiscoveryLayout.updateIndicatorViews(this);
		FragmentLayoutManager.ProfileLayout.initializeView(this);
		FragmentLayoutManager.LeaderboardLayout.refreshLeaderboard(bhApp);
		FragmentLayoutManager.AchievementsLayout.initializeAchievements(bhApp);

		updateNotification();

		// Debug.stopMethodTracing();

		upgrade();

		return true;
	}

	/**
	 * 
	 */
	private void upgrade() {

		if (oldVersion == 0 || newVersion == 0) return;

		if (oldVersion > newVersion) return;

		if (oldVersion < 1065) {
			bhApp.synchronizeFoundDevices.needForceOverrideUp = true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_info:

			break;
		case R.id.menu_refresh:
			if (bhApp.actionBarHandler.getCurrentPage() == FragmentLayoutManager.PAGE_LEADERBOARD) {
				FragmentLayoutManager.LeaderboardLayout.refreshLeaderboard(bhApp);
			}
		default:
			break;
		}
		return false;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {

			super(fm);
		}

		@Override
		public Fragment getItem(int i) {

			Fragment fragment = new CustomSectionFragment();
			Bundle args = new Bundle();
			args.putInt(CustomSectionFragment.ARG_SECTION_NUMBER, i);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {

			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			switch (position) {
			case FragmentLayoutManager.PAGE_DEVICE_DISCOVERY:
				return getString(R.string.str_pageTitle_main).toUpperCase();
			case FragmentLayoutManager.PAGE_LEADERBOARD:
				return getString(R.string.str_pageTitle_leaderboard).toUpperCase();
			case FragmentLayoutManager.PAGE_FOUND_DEVICES:
				return getString(R.string.str_pageTitle_foundDevices).toUpperCase();
			case FragmentLayoutManager.PAGE_ACHIEVEMENTS:
				return getString(R.string.str_pageTitle_achievements).toUpperCase();
			case FragmentLayoutManager.PAGE_PROFILE:
				return getString(R.string.str_pageTitle_profile).toUpperCase();
			}
			return null;
		}
	}

	public static class CustomSectionFragment extends Fragment {

		public CustomSectionFragment() {

		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(	LayoutInflater inflater,
									ViewGroup container,
									Bundle savedInstanceState) {

			Bundle args = getArguments();

			return FragmentLayoutManager.getSpecificView(args, inflater, container, container.getContext());

		}

		@Override
		public void onViewCreated(	View view,
									Bundle savedInstanceState) {

			super.onViewCreated(view, savedInstanceState);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode,
									int resultCode,
									Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);

		if ((requestCode == 64 | requestCode == 128) & bhApp.disMan != null)
			bhApp.disMan.passEnableBTActivityResult(resultCode, requestCode);

		if (requestCode == REQ_PICK_USER_IMAGE && resultCode == RESULT_OK)
			FragmentLayoutManager.ProfileLayout.passPickedImage(this, intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {

		notificationManager.cancelAll();
		bhApp.loginManager.unregisterInternetReceiver();

		bhApp.disMan.unregisterReceiver();
		bhApp.disMan.stopDiscoveryManager();

		bhApp.synchronizeFoundDevices.saveChanges();

		destroyed = true;

		super.onDestroy();
	}

	public boolean isDestroyed() {

		return destroyed;
	}

	public BlueHunter getBlueHunter() {

		return bhApp;
	}

	@SuppressWarnings("deprecation")
	public void updateNotification() {

		if (PreferenceManager.getPref(this, "pref_showNotification", true)) {

			int level = LevelSystem.getLevel(exp);

			if (VERSION.SDK_INT >= 14)
				stateNotificationBuilder.setProgress(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level), exp - LevelSystem.getLevelStartExp(level), false);

			DecimalFormat df = new DecimalFormat(",###");

			stateNotificationBuilder.setContentText(String.format("%s %d"+ (char) 9 + "%s / %s %s", getString(R.string.str_foundDevices_level), level, df.format(exp), df.format(LevelSystem.getLevelEndExp(level)), getString(R.string.str_foundDevices_exp_abbreviation)));

			if (VERSION.SDK_INT >= 16) {
				notificationManager.notify(1, stateNotificationBuilder.build());
			}
			else {
				notificationManager.notify(1, stateNotificationBuilder.getNotification());
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void alterNotification(boolean show) {

		if (show) {

			int level = LevelSystem.getLevel(exp);

			if (VERSION.SDK_INT >= 14)
				stateNotificationBuilder.setProgress(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level), exp - LevelSystem.getLevelStartExp(level), false);

			DecimalFormat df = new DecimalFormat(",###");
			
			stateNotificationBuilder.setContentText(String.format("%s %d"+ (char) 9 + "%s / %s %s", getString(R.string.str_foundDevices_level), level, df.format(exp), df.format(LevelSystem.getLevelEndExp(level)), getString(R.string.str_foundDevices_exp_abbreviation)));

			if (VERSION.SDK_INT >= 16) {
				notificationManager.notify(1, stateNotificationBuilder.build());
			}
			else {
				notificationManager.notify(1, stateNotificationBuilder.getNotification());
			}

		}
		else {
			notificationManager.cancelAll();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		if (PreferenceManager.getPref(bhApp, "pref_enableBackground", false)) {
			try {
				getWindow().setBackgroundDrawableResource(R.drawable.bg_main);
			}
			catch (Exception e) {
				PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
			}
			catch (OutOfMemoryError e) {
				PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
			}

		}

		FragmentLayoutManager.FoundDevicesLayout.refreshFoundDevicesList(bhApp);

	}

}
