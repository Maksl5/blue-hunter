package com.maksl5.bl_hunt.activity;



import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TableRow;

import com.maksl5.bl_hunt.FragmentLayoutManager;
import com.maksl5.bl_hunt.R;



/**
 * The {@link ActionBarHandler} class handles all communication and events that affect the {@link ActionBar}. It has to
 * be constructed in the {@link Activity#onCreate(Bundle)} method and you must supply the {@link Menu} via the
 * {@link #supplyMenu(Menu)} method in the {@link Activity#onCreateOptionsMenu(Menu)} method. If you won't supply a
 * {@link Menu} or the supplied {@link Menu} is null, all methods in this class will throw a NullMenuException.
 */

public class ActionBarHandler implements OnNavigationListener, OnQueryTextListener, OnActionExpandListener {

	private MainActivity mainActivity;
	public ActionBar actBar;
	private MenuInflater menuInflater;
	private Menu menu;

	private CompoundButton disSwitch;
	private ProgressBar progressBar;
	private int currentPage;

	private int moveStartY;
	private int beforeLastY;
	private int lastY;

	@TargetApi(11)
	public ActionBarHandler(MainActivity activity) {

		mainActivity = activity;
		actBar = mainActivity.getActionBar();

		actBar.setDisplayShowTitleEnabled(false);
		actBar.setDisplayUseLogoEnabled(true);
		actBar.setDisplayShowHomeEnabled(true);

		menuInflater = mainActivity.getMenuInflater();
	}

	/**
	 * 
	 */
	public void initialize() {

		checkMenuNull();

		final ViewPager viewPager = (ViewPager) mainActivity.findViewById(R.id.pager);
		final TableRow userInfoRow = (TableRow) mainActivity.findViewById(R.id.userInfoTableRow);

		userInfoRow.setVisibility(View.VISIBLE);

		viewPager.getChildAt(0).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(	View v,
									MotionEvent event) {

				final int Y = (int) event.getRawY();

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					moveStartY = (int) event.getY();

					userInfoRow.setVisibility(View.VISIBLE);
					return true;
				case MotionEvent.ACTION_MOVE:
					int top = Y - moveStartY;

					beforeLastY = lastY;
					lastY = (int) event.getRawY();

					viewPager.setTop(top);
					userInfoRow.setBottom(top);
					viewPager.getLayoutParams().height = viewPager.getBottom() - top;
					return true;
				case MotionEvent.ACTION_UP:

					if ((Y - beforeLastY) < 0) {
						onNavigationItemSelected(0, 0);
					}
					else {
						onNavigationItemSelected(1, 0);
					}
					return true;

				}

				return false;
			}
		});

		disSwitch = (CompoundButton) getActionView(R.id.menu_switch);
		disSwitch.setPadding(5, 0, 5, 0);

		progressBar = new ProgressBar(mainActivity, null, android.R.attr.progressBarStyleSmall);
		getMenuItem(R.id.menu_progress).setVisible(false).setActionView(progressBar);

		progressBar.setPadding(5, 0, 5, 0);

		SearchView srchView = (SearchView) getActionView(R.id.menu_search);
		srchView.setOnQueryTextListener(this);

		getMenuItem(R.id.menu_search).setOnActionExpandListener(this);

		changePage(FragmentLayoutManager.PAGE_DEVICE_DISCOVERY);

	}

	public boolean changePage(int newPage) {

		checkMenuNull();

		currentPage = newPage;

		switch (newPage) {
		case FragmentLayoutManager.PAGE_DEVICE_DISCOVERY:
			menu.findItem(R.id.menu_search).setVisible(false);
			menu.findItem(R.id.menu_info).setVisible(false);
			break;
		case FragmentLayoutManager.PAGE_LEADERBOARD:
			menu.findItem(R.id.menu_search).setVisible(true);
			menu.findItem(R.id.menu_info).setVisible(false);
			
			onQueryTextChange("");
			menu.findItem(R.id.menu_search).collapseActionView();
			break;
		case FragmentLayoutManager.PAGE_FOUND_DEVICES:
			menu.findItem(R.id.menu_search).setVisible(true);
			menu.findItem(R.id.menu_info).setVisible(true);

			onQueryTextChange("");
			menu.findItem(R.id.menu_search).collapseActionView();
			break;
		case FragmentLayoutManager.PAGE_ACHIEVEMENTS:
			menu.findItem(R.id.menu_search).setVisible(false);
			menu.findItem(R.id.menu_info).setVisible(false);
			break;
		case FragmentLayoutManager.PAGE_PROFILE:

			break;
		}

		return true;
	}

	public void supplyMenu(Menu menu) {

		this.menu = menu;
		checkMenuNull();
	}

	/**
	 * @throws NullMenuException
	 * 
	 */
	private void checkMenuNull() {

		if (menu == null) {

			mainActivity.invalidateOptionsMenu();

		}

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
	 * @see android.app.ActionBar.OnNavigationListener#onNavigationItemSelected(int, long)
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition,
											long itemId) {

		final ViewPager parentView = (ViewPager) mainActivity.findViewById(R.id.pager);
		final TableRow userInfoRow = (TableRow) mainActivity.findViewById(R.id.userInfoTableRow);

		Animation animation;

		switch (itemPosition) {
		case 0:

			// parentView.setLayerType(ViewPager.LAYER_TYPE_HARDWARE, null);
			// userInfoRow.setLayerType(TableRow.LAYER_TYPE_HARDWARE, null);

			animation = new SizeAnimation(parentView, userInfoRow, 0);
			animation.setDuration(500);
			animation.setFillAfter(true);

			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

					Log.d("layout", "before: view.getBottom = " + parentView.getBottom());

					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {

					userInfoRow.setVisibility(View.INVISIBLE);
					Log.d("layout", "after: view.getBottom = " + parentView.getBottom());
					parentView.requestLayout();
					// parentView.setLayerType(ViewPager.LAYER_TYPE_NONE, null);
					// userInfoRow.setLayerType(TableRow.LAYER_TYPE_NONE, null);
					// TODO Auto-generated method stub

				}
			});

			parentView.startAnimation(animation);
			break;
		case 1:

			// parentView.setLayerType(ViewPager.LAYER_TYPE_HARDWARE, null);
			// userInfoRow.setLayerType(TableRow.LAYER_TYPE_HARDWARE, null);

			animation = new SizeAnimation(parentView, userInfoRow, userInfoRow.getMeasuredHeight());
			animation.setDuration(500);
			animation.setFillAfter(true);

			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

					userInfoRow.setVisibility(View.VISIBLE);
					Log.d("layout", "before: view.getBottom = " + parentView.getBottom());
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {

					Log.d("layout", "after: view.getBottom = " + parentView.getBottom());
					parentView.requestLayout();
					// parentView.setLayerType(ViewPager.LAYER_TYPE_NONE, null);
					// userInfoRow.setLayerType(TableRow.LAYER_TYPE_NONE, null);
					// TODO Auto-generated method stub

				}
			});

			parentView.startAnimation(animation);

			break;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.SearchView.OnQueryTextListener#onQueryTextChange(java.lang.String)
	 */
	@Override
	public boolean onQueryTextChange(String newText) {

		if (currentPage == FragmentLayoutManager.PAGE_FOUND_DEVICES) {

			FragmentLayoutManager.FoundDevicesLayout.filterFoundDevices(newText, mainActivity);

		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.SearchView.OnQueryTextListener#onQueryTextSubmit(java.lang.String)
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

	public class SizeAnimation extends Animation {

		int targetTop;
		ViewPager view;
		TableRow userRow;
		int initialBot;
		int initialTop;

		public SizeAnimation(ViewPager view,
				TableRow userRow,
				int targetTop) {

			this.view = view;
			this.userRow = userRow;
			this.targetTop = targetTop;
			this.initialBot = view.getBottom();
			this.initialTop = view.getTop();

			Log.d("hardware_accelerated?", String.valueOf(view.isHardwareAccelerated()));

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.animation.Animation#applyTransformation(float, android.view.animation.Transformation)
		 */
		@Override
		protected void applyTransformation(	float interpolatedTime,
											Transformation t) {

			if (targetTop == initialTop) {
				return;
			}
			else if (targetTop > initialTop) {

				int top = (int) ((targetTop - initialTop) * interpolatedTime + initialTop);

				view.setTop(top);
				userRow.setBottom(top);
				view.getLayoutParams().height = view.getBottom() - top;

			}
			else if (targetTop < initialTop) {

				int top = (int) ((initialTop - targetTop) * (1 - interpolatedTime) + targetTop);

				view.setTop(top);
				userRow.setBottom(top);
				view.getLayoutParams().height = view.getBottom() - top;

			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.animation.Animation#initialize(int, int, int, int)
		 */
		@Override
		public void initialize(	int width,
								int height,
								int parentWidth,
								int parentHeight) {

			// TODO Auto-generated method stub
			super.initialize(width, height, parentWidth, parentHeight);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.animation.Animation#willChangeBounds()
		 */
		@Override
		public boolean willChangeBounds() {

			// TODO Auto-generated method stub
			return true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.MenuItem.OnActionExpandListener#onMenuItemActionCollapse(android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {

		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.MenuItem.OnActionExpandListener#onMenuItemActionExpand(android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		
		return true;
	}

}
