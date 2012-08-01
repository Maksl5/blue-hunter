package com.maksl5.bl_hunt;



import android.R.anim;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;



/**
 * The {@link ActionBarHandler} class handles all communication and events that affect the {@link ActionBar}. It has to
 * be constructed in the {@link Activity#onCreate(Bundle)} method and you must supply the {@link Menu}
 * via the {@link #supplyMenu(Menu)} method in the {@link Activity#onCreateOptionsMenu(Menu)} method.
 * If you won't supply a {@link Menu}, all methods in this class will return null or false.
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

		if(menu == null)
			return false;
		
		menu.clear();
		
		switch(newPage)
		{
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
	}

}
