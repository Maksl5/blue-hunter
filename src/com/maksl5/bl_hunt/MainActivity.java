package com.maksl5.bl_hunt;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import android.widget.Chronometer;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.maksl5.bl_hunt.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.DiscoveryManager.DiscoveryState;
import com.maksl5.bl_hunt.CustomUI.PatternProgressBar;


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
	public NetworkMananger netMananger;
	public NotificationManager notificationManager;
	public Notification.Builder stateNotificationBuilder;

	public TextView disStateTextView;
	public TextView userInfoTextView;

	public int versionCode = 0;

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
		netMananger = new NetworkMananger(this);

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

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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

				actionBarHandler.changePage(position + 1);

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

		authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

			@Override
			public boolean onResult(int requestId,
									String resultString) {

				switch (requestId) {
				case Authentification.NETRESULT_ID_SERIAL_CHECK:
					Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_LONG).show();
					break;
				case Authentification.NETRESULT_ID_GET_USER_INFO:
					userInfoTextView.setText(Html.fromHtml(resultString));
					userInfoTextView.setVisibility(TextView.VISIBLE);
					userInfoTextView.setClickable(true);
					userInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
					userInfoTextView.setLinkTextColor(Color.DKGRAY);
					userInfoTextView.requestLayout();
					userInfoTextView.invalidate();

					TableRow userInfoRow = (TableRow) findViewById(R.id.userInfoTableRow);
					userInfoRow.setVisibility(TableRow.VISIBLE);
					userInfoRow.invalidate();
					userInfoRow.setVisibility(TableRow.INVISIBLE);
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

		userInfoRow.setVisibility(TableRow.INVISIBLE);
		FragmentLayoutManager.refreshFoundDevicesList(this);
		FragmentLayoutManager.updateIndicatorViews(this);

		PatternProgressBar progressBar = (PatternProgressBar) findViewById(R.id.progressBar1);

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
			args.putInt(CustomSectionFragment.ARG_SECTION_NUMBER, i + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {

			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			switch (position) {
			case 0:
				return getString(R.string.str_pageTitle_main).toUpperCase();
			case 1:
				return getString(R.string.str_pageTitle_leaderboard).toUpperCase();
			case 2:
				return getString(R.string.str_pageTitle_foundDevices).toUpperCase();
			case 3:
				return getString(R.string.str_pageTitle_achievements).toUpperCase();
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

		disMan.unregisterReceiver();
		disMan.stopDiscoveryManager();
		destroyed = true;

		super.onDestroy();
	}

	public boolean isDestroyed() {

		return destroyed;
	}

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

		FragmentLayoutManager.refreshFoundDevicesList(this);

	}

}
