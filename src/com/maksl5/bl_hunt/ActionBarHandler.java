package com.maksl5.bl_hunt;



import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;



/**
 * The {@link ActionBarHandler} class handles all communication and events that affect the {@link ActionBar}. It has to
 * be constructed in the {@link Activity#onCreate(Bundle)} method and you must supply the {@link Menu} via the
 * {@link #supplyMenu(Menu)} method in the {@link Activity#onCreateOptionsMenu(Menu)} method. If you won't supply a
 * {@link Menu} or the supplied {@link Menu} is null, all methods in this class will throw a NullMenuException.
 */

public class ActionBarHandler implements OnNavigationListener {

	private Activity parentActivity;
	private ActionBar actBar;
	private MenuInflater menuInflater;
	private Menu menu;

	private CompoundButton disSwitch;
	private ProgressBar progressBar;

	@TargetApi(11)
	public ActionBarHandler(Activity activity) {

		parentActivity = activity;
		actBar = parentActivity.getActionBar();
		actBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actBar.setDisplayShowTitleEnabled(false);

		List<String> spinnerItems = new ArrayList<String>();
		spinnerItems.add((String) activity.getTitle());
		spinnerItems.add("User Information");

		if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			actBar.setListNavigationCallbacks(new ArrayAdapter<String>(actBar.getThemedContext(), R.layout.dropdown_list_item, spinnerItems), this);
		}
		else {
			actBar.setListNavigationCallbacks(new ArrayAdapter<String>(activity, R.layout.dropdown_list_item, spinnerItems), this);
		}

		menuInflater = parentActivity.getMenuInflater();

	}

	/**
	 * 
	 */
	public void initialize() {

		disSwitch = (CompoundButton) menu.findItem(R.id.menu_switch).getActionView();
		disSwitch.setPadding(5, 0, 5, 0);

		progressBar = new ProgressBar(parentActivity, null, android.R.attr.progressBarStyleSmall);
		menu.findItem(R.id.menu_progress).setVisible(false).setActionView(progressBar);

		progressBar.setPadding(5, 0, 5, 0);

		changePage(1);

	}

	public boolean changePage(int newPage) {

		checkMenuNull();

		switch (newPage) {
		case 1:
			menu.findItem(R.id.menu_search).setVisible(false);
			break;
		case 2:
			menu.findItem(R.id.menu_search).setVisible(true);
			break;
		case 3:
			menu.findItem(R.id.menu_search).setVisible(true);
			break;
		case 4:
			menu.findItem(R.id.menu_search).setVisible(false);
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

		if (menu == null)
			throw new NullMenuException("The Menu object is null. This is caused, because you either haven't supplied a Menu, or the supplied Menu was null. Read the class description to avoid this Exception.");

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

		final ViewPager parentView = (ViewPager) parentActivity.findViewById(R.id.pager);
		final TableRow userInfoRow = (TableRow) parentActivity.findViewById(R.id.userInfoTableRow);
		TextView userInfoTextView = (TextView) parentActivity.findViewById(R.id.userInfoTxtView);
		Animation animation;

		switch (itemPosition) {
		case 0:

			animation = new SizeAnimation(parentView, 0);
			animation.setDuration(1000);
			animation.setFillAfter(true);

			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

					userInfoRow.setVisibility(TableRow.INVISIBLE);
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
					// TODO Auto-generated method stub

				}
			});

			parentView.startAnimation(animation);
			break;
		case 1:

			animation = new SizeAnimation(parentView, userInfoRow.getMeasuredHeight());
			animation.setDuration(1000);
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
					Log.d("layout", "after: view.getBottom = " + parentView.getBottom());
					userInfoRow.setVisibility(TableRow.VISIBLE);
					// TODO Auto-generated method stub

				}
			});

			parentView.startAnimation(animation);

			break;
		}
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
		int initialBot;
		int initialTop;

		public SizeAnimation(ViewPager view,
				int targetTop) {

			this.view = view;
			this.targetTop = targetTop;
			this.initialBot = view.getBottom();
			this.initialTop = view.getTop();
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

				view.setTop((int) ((targetTop - initialTop) * interpolatedTime + initialTop));
				// view.setBottom(initialBot);
				view.getLayoutParams().height =
						view.getBottom() - (int) ((targetTop - initialTop) * interpolatedTime + initialTop);

				view.invalidate();
				view.requestLayout();
				
				

			}
			else if (targetTop < initialTop) {

				view.setTop((int) ((initialTop - targetTop) * (1 - interpolatedTime) + targetTop));
				// view.setBottom(initialBot);
				view.getLayoutParams().height =
						view.getBottom() - (int) ((initialTop - targetTop) * (1 - interpolatedTime) + targetTop);
				
				view.invalidate();
				view.requestLayout();
				view.forceLayout();

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

}
