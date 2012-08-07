package com.maksl5.bl_hunt;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



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
	ViewPager mViewPager;

	public ActionBarHandler actionBarHandler;
	public DiscoveryManager disMan;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		// Create the adapter that will return a fragment for each of the primary sections
		// of the app.

		actionBarHandler = new ActionBarHandler(this);
		disMan = new DiscoveryManager(this);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(4);

		registerListener();

	}

	/**
	 * 
	 */

	private void registerListener() {

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				int i = 0;

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

	}

	public void viewsGotCreated() {

		int curPage = mViewPager.getCurrentItem();

		switch (curPage + 1) {
		case 1:
			// Setting up Views
			TextView stateTextView = (TextView) findViewById(R.id.txt_discoveryState);

			// Setting up DiscoveryManager

			if (!disMan.startDiscoveryManager()) {
				if (!disMan.supplyTextView(stateTextView)) {

					// ERROR
				}
				else {
					disMan.startDiscoveryManager();
				}
			}
			break;

		default:
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.act_main, menu);
		actionBarHandler.supplyMenu(menu);
		return true;
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

			// TODO Auto-generated method stub
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

		if (requestCode == 64 & disMan != null) disMan.passEnableBTActivityResult(resultCode);
	}

}
