package com.maksl5.bl_hunt;



import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.maksl5.bl_hunt.Authentification.OnNetworkResultAvailableListener;



public class MainActivity extends FragmentActivity {

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
	
	public TextView disStateTextView;
	public TextView userInfoTextView;
	
	private boolean destroyed;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
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

		
		registerListener();
		
		
		
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
	 * 
	 */

	private void registerListener() {

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				actionBarHandler.changePage(position + 1);
				viewsGotCreated();

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
			public boolean onResult(	int requestId,
									String resultString) {
			
				switch (requestId) {
				case Authentification.NETRESULT_ID_SERIAL_CHECK:
					Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_LONG).show();
					break;
				case Authentification.NETRESULT_ID_GET_USER_INFO:
					userInfoTextView.setText(Html.fromHtml(resultString));
					userInfoTextView.setVisibility(TextView.VISIBLE);
					userInfoTextView.requestLayout();
					userInfoTextView.invalidate();
					
					
					TableRow userInfoRow =
							(TableRow) findViewById(R.id.userInfoTableRow);
					userInfoRow.setVisibility(TableRow.VISIBLE);
					userInfoRow.invalidate();
					userInfoRow.setVisibility(TableRow.INVISIBLE);
					break;
				}
				
				return false;
			}
		});


	}

	public void viewsGotCreated() {

		int curPage = mViewPager.getCurrentItem();

		switch (curPage + 1) {
		case 1:

			break;

		default:
			break;
		}

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
		
		int verCode = 0;
		try {
			verCode = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionCode;
		}
		catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		authentification.checkUpdate();
		
		//Network Stuff
		
		
		NetworkThread serialSubmit = new NetworkThread(this, netMananger);
		serialSubmit.execute(AuthentificationSecure.SERVER_CHECK_SERIAL, String.valueOf(Authentification.NETRESULT_ID_SERIAL_CHECK), "s=" + Authentification.getSerialNumber(), "v=" + verCode, "h=" + authentification.getSerialNumberHash());
		
		
		new DatabaseHelper(this, verCode).getWritableDatabase().close();
	
		
		TableRow userInfoRow =
				(TableRow) findViewById(R.id.userInfoTableRow);
		userInfoTextView = (TextView) findViewById(R.id.userInfoTxtView);
		
		
		NetworkThread getUserInfo = new NetworkThread(this, netMananger);
		getUserInfo.execute(AuthentificationSecure.SERVER_GET_USER_INFO, String.valueOf(Authentification.NETRESULT_ID_GET_USER_INFO));
		
		
		
		
		userInfoRow.setVisibility(TableRow.INVISIBLE);
		
		
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

	public class CustomSectionFragment extends Fragment {

		public CustomSectionFragment() {

		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(	LayoutInflater inflater,
									ViewGroup container,
									Bundle savedInstanceState) {

			Bundle args = getArguments();
			FragmentLayoutManager fragLayMan = new FragmentLayoutManager(inflater, container, args, this.getActivity());
			return fragLayMan.getSpecificView();

		}

		@Override
		public void onViewCreated(	View view,
									Bundle savedInstanceState) {

			super.onViewCreated(view, savedInstanceState);

			viewsGotCreated();
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

		if ((requestCode == 64 | requestCode == 128) & disMan != null) disMan.passEnableBTActivityResult(resultCode, requestCode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {

		disMan.unregisterReceiver();
		disMan.stopDiscoveryManager();
		destroyed = true;

		super.onDestroy();
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}

}
