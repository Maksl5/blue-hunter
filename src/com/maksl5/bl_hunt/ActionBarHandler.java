package com.maksl5.bl_hunt;



import android.R.anim;
import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;



/**
 * The {@link ActionBarHandler} class handles all communication and events that affect the {@link ActionBar}. It has to
 * be constructed in the {@link Activity#onCreate(Bundle)} method and you must supply the {@link Menu} via the
 * {@link #supplyMenu(Menu)} method in the {@link Activity#onCreateOptionsMenu(Menu)} method. If you won't supply a
 * {@link Menu} or the supplied {@link Menu} is null, all methods in this class will throw a NullMenuException.
 */

public class ActionBarHandler {

	private Activity parentActivity;
	private ActionBar actBar;
	private MenuInflater menuInflater;
	private Menu menu;

	private CompoundButton disSwitch;
	private ProgressBar progressBar;

	public ActionBarHandler(Activity activity) {

		parentActivity = activity;
		actBar = parentActivity.getActionBar();
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

}
