/**
 *  NetworkMananger.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.MenuItem;


/**
 * @author Maksl5[Markus Bensing]
 *
 */
public class NetworkMananger {

	private MainActivity mainActivity;
	private List<NetworkThread> curRunningThreads;
	
	public NetworkMananger(MainActivity mActivity) {
		mainActivity = mActivity;
		curRunningThreads = new ArrayList<NetworkThread>();
	}
	
	public void addRunningThread(NetworkThread netThread) {
		curRunningThreads.add(netThread);
	}
	
	public synchronized void threadFinished(NetworkThread netThread) {
		curRunningThreads.remove(netThread);
		checkList();
	}
	
	/**
	 * 
	 */
	private void checkList() {

		if(curRunningThreads.size()== 0) {
			if (!mainActivity.isDestroyed()) {
				MenuItem progressBar = mainActivity.actionBarHandler.getMenuItem(R.id.menu_progress);
				progressBar.setVisible(false);
			}
		}
		
	}
	
}
