package com.maksl5.bl_hunt.activity;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build.VERSION;
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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.maksl5.bl_hunt.DiscoveryManager;
import com.maksl5.bl_hunt.FragmentLayoutManager;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.DiscoveryManager.DiscoveryState;
import com.maksl5.bl_hunt.FragmentLayoutManager.DeviceDiscoveryLayout;
import com.maksl5.bl_hunt.FragmentLayoutManager.FoundDevicesLayout;
import com.maksl5.bl_hunt.FragmentLayoutManager.StatisticLayout;
import com.maksl5.bl_hunt.R.drawable;
import com.maksl5.bl_hunt.R.id;
import com.maksl5.bl_hunt.R.layout;
import com.maksl5.bl_hunt.R.menu;
import com.maksl5.bl_hunt.R.string;
import com.maksl5.bl_hunt.custom_ui.PatternProgressBar;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.NetworkManager;
import com.maksl5.bl_hunt.net.NetworkThread;
import com.maksl5.bl_hunt.net.Authentification.LoginManager;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;



public class MainActivity extends FragmentActivity {

	public static MainActivity thisActivity;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory.
	 * If this becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;

	public ActionBarHandler actionBarHandler;
	public DiscoveryManager disMan;
	public Authentification authentification;
	public NetworkManager netMananger;
	public NotificationManager notificationManager;
	public Notification.Builder stateNotificationBuilder;
	public LoginManager loginManager;

	public TextView disStateTextView;
	public TextView userInfoTextView;

	public int versionCode = 0;
	public boolean passSet = false;

	private boolean destroyed;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		thisActivity = this;
		destroyed = false;
		setContentView(R.layout.act_main);
		// Create the adapter that will return a fragment for each of the primary sections
		// of the app.

		actionBarHandler = new ActionBarHandler(this);
		disMan = new DiscoveryManager(this);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		authentification = new Authentification(this, this);
		netMananger = new NetworkManager(this);
		
		loginManager = authentification.new LoginManager(Authentification.getSerialNumber(), authentification.getStoredPass(), authentification.getStoredLoginToken());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(4);

		try {
			versionCode =
					getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionCode;
		}
		catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		registerListener();

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		stateNotificationBuilder = new Notification.Builder(this);

		stateNotificationBuilder.setOngoing(true);
		stateNotificationBuilder.setSmallIcon(R.drawable.ic_launcher);
		stateNotificationBuilder.setAutoCancel(false);
		stateNotificationBuilder.setContentTitle(DiscoveryState.getUnformatedDiscoveryState(DiscoveryState.DISCOVERY_STATE_STOPPED, this));
		stateNotificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0));

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

	/**
	 * 
	 */

	private void registerListener() {

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				actionBarHandler.changePage(position);

			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

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

		authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

			@Override
			public boolean onResult(int requestId, String resultString) {

				switch (requestId) {
					case Authentification.NETRESULT_ID_SERIAL_CHECK:

						Pattern pattern = Pattern.compile("Error=(\\d+)");
						Matcher matcher = pattern.matcher(resultString);

						if (matcher.find()) {
							int error = Integer.parseInt(matcher.group(1));

							String errorMsg = "Error " + error + " while checking serial."; 
							
							switch (error) {
								case 1:
								case 4:
								case 5:
									errorMsg+= " (Could not retrieve data. Possibly no internet connection.)";
									break;
								case 404:
									errorMsg += " (Could not retrieve data. Possibly server is down or script not available.)";
									break;
								case 500:
									errorMsg += " (Could not retrieve data. Server error. Please contact the developer.)";
									break;
								case 1001:
								case 1002:
								case 1004:
									errorMsg += " (Uncomplete parameters)";
									break;
								case 1003:
									errorMsg += " (Hashes don't match)";
									break;
								case 1005:
									errorMsg += " (Failed inserting serial into DB)";
									break;
								case 1006:
									errorMsg += " (Failed updating serial in DB)";
									break;
								case 1007:
									errorMsg += " (More than 1 row in DB query result)";
									break;
							}
							
							Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
							break;
						}

						loginManager.login();
						break;
					case Authentification.NETRESULT_ID_GET_USER_INFO:
						
						Pattern pattern2 = Pattern.compile("Error=(\\d+)");
						Matcher matcher2 = pattern2.matcher(resultString);

						if (matcher2.find()) {
							int error = Integer.parseInt(matcher2.group(1));

							String errorMsg = "Error " + error + " while getting User Info."; 
							
							switch (error) {
								case 1:
								case 4:
								case 5:
									errorMsg+= " (Could not retrieve data. Possibly no internet connection.)";
									break;
								case 404:
									errorMsg += " (Could not retrieve data. Possibly server is down or script not available.)";
									break;
								case 500:
									errorMsg += " (Could not retrieve data. Server error. Please contact the developer.)";
									break;
							}
							
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

		getMenuInflater().inflate(R.menu.act_main, menu);
		actionBarHandler.supplyMenu(menu);
		actionBarHandler.initialize();

		// Setting up Views
		if (disStateTextView == null) disStateTextView = (TextView) findViewById(R.id.txt_discoveryState);
		// Setting up DiscoveryManager

		if (!disMan.startDiscoveryManager()) {
			if (!disMan.supplyTextView(disStateTextView)) {

				// ERROR
			}
			else {
				disMan.startDiscoveryManager();
			}
		}

		// Network Stuff

		NetworkThread serialSubmit = new NetworkThread(this, netMananger);
		serialSubmit.execute(AuthentificationSecure.SERVER_CHECK_SERIAL, String.valueOf(Authentification.NETRESULT_ID_SERIAL_CHECK), "s=" + Authentification.getSerialNumber(), "v=" + versionCode, "h=" + authentification.getSerialNumberHash());

		new DatabaseManager(this, versionCode).close();

		TableRow userInfoRow = (TableRow) findViewById(R.id.userInfoTableRow);
		userInfoTextView = (TextView) findViewById(R.id.userInfoTxtView);

		NetworkThread getUserInfo = new NetworkThread(this, netMananger);
		getUserInfo.execute(AuthentificationSecure.SERVER_GET_USER_INFO, String.valueOf(Authentification.NETRESULT_ID_GET_USER_INFO));

		userInfoRow.setVisibility(View.INVISIBLE);
		FragmentLayoutManager.FoundDevicesLayout.refreshFoundDevicesList(this);
		FragmentLayoutManager.DeviceDiscoveryLayout.updateIndicatorViews(this);
		FragmentLayoutManager.StatisticLayout.initializeView(this);

		updateNotification();

		return true;
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
				Intent intent = new Intent(this, PreferenceActivity.class);
				startActivity(intent);
				break;
			case R.id.menu_info:

				break;
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
				case FragmentLayoutManager.PAGE_STATISTICS:
					return getString(R.string.str_pageTitle_statistics).toUpperCase();
			}
			return null;
		}
	}

	public static class CustomSectionFragment extends Fragment {

		public CustomSectionFragment() {

		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			Bundle args = getArguments();

			return FragmentLayoutManager.getSpecificView(args, inflater, container, container.getContext());

		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {

			super.onViewCreated(view, savedInstanceState);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);

		if ((requestCode == 64 | requestCode == 128) & disMan != null)
			disMan.passEnableBTActivityResult(resultCode, requestCode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {

		notificationManager.cancelAll();
		loginManager.unregisterInternetReceiver();
		
		disMan.unregisterReceiver();
		disMan.stopDiscoveryManager();
		destroyed = true;

		super.onDestroy();
	}

	public boolean isDestroyed() {

		return destroyed;
	}

	@SuppressWarnings("deprecation")
	public void updateNotification() {

		if (PreferenceManager.getPref(this, "pref_showNotification", true)) {

			int exp = LevelSystem.getUserExp(this);
			int level = LevelSystem.getLevel(exp);

			if (VERSION.SDK_INT >= 14)
				stateNotificationBuilder.setProgress(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level), exp - LevelSystem.getLevelStartExp(level), false);

			stateNotificationBuilder.setContentText("Level " + level + "\t" + exp + " / " + LevelSystem.getLevelEndExp(level) + " " + getString(R.string.str_foundDevices_exp_abbreviation));

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

			int exp = LevelSystem.getUserExp(this);
			int level = LevelSystem.getLevel(exp);

			if (VERSION.SDK_INT >= 14)
				stateNotificationBuilder.setProgress(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level), exp - LevelSystem.getLevelStartExp(level), false);

			stateNotificationBuilder.setContentText("Level " + level + "\t" + exp + " / " + LevelSystem.getLevelEndExp(level) + " " + getString(R.string.str_foundDevices_exp_abbreviation));

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

		FragmentLayoutManager.FoundDevicesLayout.refreshFoundDevicesList(this);

	}

}
