package com.maksl5.bl_hunt.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.custom_ui.RandomToast;
import com.maksl5.bl_hunt.custom_ui.fragment.DeviceDiscoveryLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.FoundDevicesLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.LeaderboardLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.ProfileLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.WeeklyLeaderboardLayout;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;
import com.maksl5.bl_hunt.util.MacAddress;

/**
 * The {@link ActionBarHandler} class handles all communication and events that
 * affect the {@link ActionBar}. It has to be constructed in the
 * {@link Activity#onCreate(Bundle)} method and you must supply the {@link Menu}
 * via the {@link #supplyMenu(Menu)} method in the
 * {@link Activity#onCreateOptionsMenu(Menu)} method. If you won't supply a
 * {@link Menu} or the supplied {@link Menu} is null, all methods in this class
 * will throw a NullMenuException.
 */

public class ActionBarHandler implements OnQueryTextListener, OnActionExpandListener {

	private BlueHunter bhApp;
	public ActionBar actBar;
	private MenuInflater menuInflater;
	private Menu menu;

	private CompoundButton disSwitch;
	private ProgressBar progressBar;
	private int currentPage;

	private int moveStartY;
	private int beforeLastY;
	private int lastY;

	public ActionMode.Callback actionModeCallback;

	@TargetApi(11)
	public ActionBarHandler(BlueHunter app) {

		bhApp = app;
		actBar = bhApp.mainActivity.getActionBar();

		actBar.setDisplayShowTitleEnabled(false);
		actBar.setDisplayUseLogoEnabled(true);
		actBar.setDisplayShowHomeEnabled(true);
		actBar.setIcon(R.drawable.ic_logo);

		menuInflater = bhApp.mainActivity.getMenuInflater();
	}

	/**
	 * 
	 */
	public void initialize() {

		checkMenuNull();

		final ViewPager viewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);

		disSwitch = (CompoundButton) getActionView(R.id.menu_switch);
		disSwitch.setPadding(5, 0, 5, 0);

		progressBar = new ProgressBar(bhApp, null, android.R.attr.progressBarStyleSmall);
		getMenuItem(R.id.menu_progress).setVisible(false).setActionView(progressBar);

		progressBar.setPadding(5, 0, 5, 0);

		SearchView srchView = (SearchView) getActionView(R.id.menu_search);
		srchView.setOnQueryTextListener(this);

		getMenuItem(R.id.menu_search).setOnActionExpandListener(this);

		getMenuItem(R.id.menu_boostIndicator).setTitleCondensed(bhApp.getString(R.string.str_discovery_loading));

		changePage(FragmentLayoutManager.PAGE_DEVICE_DISCOVERY);

		actionModeCallback = new ActionMode.Callback() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {

				MenuInflater inflater = mode.getMenuInflater();
				switch (currentPage) {
				case FragmentLayoutManager.PAGE_FOUND_DEVICES:
					inflater.inflate(R.menu.act_context_fd, menu);
					return true;
				default:
					return false;
				}

			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				switch (currentPage) {
				case FragmentLayoutManager.PAGE_FOUND_DEVICES:

					switch (item.getItemId()) {
					case R.id.menu_context_remove:

						MacAddress macAddress = FoundDevicesLayout.getSelectedMac();
						if (macAddress == null) {
							Toast.makeText(bhApp, "Error removing device.", Toast.LENGTH_LONG).show();
							mode.finish();
						}

						if (!new DatabaseManager(bhApp).deleteDevice(macAddress)) {
							Toast.makeText(bhApp, "Error removing device.", Toast.LENGTH_LONG).show();
						}
						else {
							FoundDevicesLayout.refreshFoundDevicesList(bhApp, false);
							DeviceDiscoveryLayout.updateIndicatorViews(bhApp.mainActivity);

							bhApp.mainActivity.updateNotification();
							Toast.makeText(bhApp, "Successfully removed device.", Toast.LENGTH_LONG).show();
						}
						mode.finish();
						return true;
					default:
						break;
					}

					break;

				default:
					break;
				}

				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {

				switch (currentPage) {
				case FragmentLayoutManager.PAGE_FOUND_DEVICES:

					FoundDevicesLayout.selectedItem = -1;

					break;

				default:
					break;
				}

			}
		};

	}

	public boolean changePage(int newPage) {

		checkMenuNull();

		try {

			currentPage = newPage;

			switch (newPage) {
			case FragmentLayoutManager.PAGE_DEVICE_DISCOVERY:
				menu.findItem(R.id.menu_search).setVisible(false);
				menu.findItem(R.id.menu_info).setVisible(false);
				menu.findItem(R.id.menu_refresh).setVisible(false);

				menu.findItem(R.id.menu_boostIndicator).setVisible(true);
				menu.findItem(R.id.menu_switch).setVisible(true);
				menu.findItem(R.id.menu_submit_mac).setVisible(false);

				if (bhApp.isTablet()) {
					menu.findItem(R.id.menu_boostIndicator).setVisible(true);
					menu.findItem(R.id.menu_switch).setVisible(true);
				}

				onQueryTextChange("");
				menu.findItem(R.id.menu_search).collapseActionView();
				
				break;
			case FragmentLayoutManager.PAGE_LEADERBOARD:
				menu.findItem(R.id.menu_search).setVisible(true);
				menu.findItem(R.id.menu_info).setVisible(false);
				menu.findItem(R.id.menu_refresh).setVisible(true);

				menu.findItem(R.id.menu_boostIndicator).setVisible(false);
				menu.findItem(R.id.menu_switch).setVisible(false);
				menu.findItem(R.id.menu_submit_mac).setVisible(false);

				if (bhApp.isTablet()) {
					menu.findItem(R.id.menu_boostIndicator).setVisible(true);
					menu.findItem(R.id.menu_switch).setVisible(true);
				}

				onQueryTextChange("");
				menu.findItem(R.id.menu_search).collapseActionView();

				if (!PreferenceManager.getPref(bhApp, "pref_syncingActivated", false))
					RandomToast.create(bhApp, bhApp.getString(R.string.str_tip_leaderboard), 0.25).show();

				break;
			case FragmentLayoutManager.PAGE_FOUND_DEVICES:
				menu.findItem(R.id.menu_search).setVisible(true);
				menu.findItem(R.id.menu_info).setVisible(true);
				menu.findItem(R.id.menu_refresh).setVisible(false);

				menu.findItem(R.id.menu_boostIndicator).setVisible(true);
				menu.findItem(R.id.menu_switch).setVisible(false);
				menu.findItem(R.id.menu_submit_mac).setVisible(true);

				if (bhApp.isTablet()) {
					menu.findItem(R.id.menu_boostIndicator).setVisible(true);
					menu.findItem(R.id.menu_switch).setVisible(true);
				}

				onQueryTextChange("");
				menu.findItem(R.id.menu_search).collapseActionView();
				break;
			case FragmentLayoutManager.PAGE_ACHIEVEMENTS:
				menu.findItem(R.id.menu_search).setVisible(false);
				menu.findItem(R.id.menu_info).setVisible(false);
				menu.findItem(R.id.menu_refresh).setVisible(false);

				menu.findItem(R.id.menu_boostIndicator).setVisible(true);
				menu.findItem(R.id.menu_switch).setVisible(false);
				menu.findItem(R.id.menu_submit_mac).setVisible(false);

				if (bhApp.isTablet()) {
					menu.findItem(R.id.menu_boostIndicator).setVisible(true);
					menu.findItem(R.id.menu_switch).setVisible(true);
				}

				break;
			case FragmentLayoutManager.PAGE_PROFILE:
				if (ProfileLayout.userName.startsWith("Player")) {
					RandomToast.create(bhApp, bhApp.getString(R.string.str_tip_changeName), 0.25).show();
				}
				break;
			}
		}
		catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	public int getCurrentPage() {

		return currentPage;
	}

	public void supplyMenu(Menu menu) {

		this.menu = menu;
		checkMenuNull();
	}

	/**
	 * @throws NullMenuException
	 * 
	 */
	private boolean checkMenuNull() {

		if (menu == null) {

			bhApp.mainActivity.invalidateOptionsMenu();
			return true;
		}

		return false;

	}

	public View getActionView(int resourceId) {

		checkMenuNull();
		return menu.findItem(resourceId).getActionView();
	}

	public MenuItem getMenuItem(int resourceId) {

		checkMenuNull();
		return menu.findItem(resourceId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.SearchView.OnQueryTextListener#onQueryTextChange(java.
	 * lang.String)
	 */
	@Override
	public boolean onQueryTextChange(String newText) {

		if (currentPage == FragmentLayoutManager.PAGE_FOUND_DEVICES) FoundDevicesLayout.filterFoundDevices(newText, bhApp);

		if (currentPage == FragmentLayoutManager.PAGE_LEADERBOARD) {

			if (LeaderboardLayout.currentSelectedTab == 0) {
				LeaderboardLayout.filterLeaderboard(newText, bhApp);
			}
			else {
				WeeklyLeaderboardLayout.filterLeaderboard(newText, bhApp);
			}

		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.SearchView.OnQueryTextListener#onQueryTextSubmit(java.
	 * lang.String)
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {

		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	public class NullMenuException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NullMenuException(String msg) {

			super(msg);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.MenuItem.OnActionExpandListener#onMenuItemActionCollapse
	 * (android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {

		if (currentPage == FragmentLayoutManager.PAGE_FOUND_DEVICES) {
			menu.findItem(R.id.menu_info).setVisible(true);
			menu.findItem(R.id.menu_boostIndicator).setVisible(true);
		}
		if (currentPage == FragmentLayoutManager.PAGE_LEADERBOARD) {
			menu.findItem(R.id.menu_refresh).setVisible(true);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.MenuItem.OnActionExpandListener#onMenuItemActionExpand(
	 * android .view.MenuItem)
	 */
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {

		if (currentPage == FragmentLayoutManager.PAGE_FOUND_DEVICES) {
			menu.findItem(R.id.menu_info).setVisible(false);
			menu.findItem(R.id.menu_boostIndicator).setVisible(false);
		}
		if (currentPage == FragmentLayoutManager.PAGE_LEADERBOARD) {
			menu.findItem(R.id.menu_refresh).setVisible(false);
		}

		return true;
	}

	public void setDiscoverySwitchEnabled(boolean enabled) {
		if (disSwitch != null) {
			disSwitch.setEnabled(enabled);
		}
		else {
			disSwitch = (CompoundButton) getActionView(R.id.menu_switch);
			disSwitch.setPadding(5, 0, 5, 0);
			disSwitch.setEnabled(enabled);
		}

	}

}
