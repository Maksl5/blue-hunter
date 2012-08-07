package com.maksl5.bl_hunt;



import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;



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

	public ActionBarHandler(Activity activity) {

		parentActivity = activity;
		actBar = parentActivity.getActionBar();
		menuInflater = parentActivity.getMenuInflater();
	}

	public boolean changePage(int newPage) {

		checkMenuNull();

		menu.clear();

		switch (newPage) {
		case 1:
			menuInflater.inflate(R.menu.act_main, menu);
			break;
		case 2:
			menuInflater.inflate(R.menu.act_main, menu);
			menu.add("Search").setActionView(new SearchView(parentActivity)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			break;
		case 3:
			menuInflater.inflate(R.menu.act_main, menu);
			menu.add("Search").setActionView(new SearchView(parentActivity)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			break;
		case 4:
			menuInflater.inflate(R.menu.act_main, menu);
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
	
		if(menu == null)
			throw new NullMenuException("The Menu object is null. This is caused, because you either haven't supplied a Menu, or the supplied Menu was null. Read the class description to avoid this Exception.");
		
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
