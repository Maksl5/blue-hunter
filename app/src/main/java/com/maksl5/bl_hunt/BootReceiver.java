/**
 *  BootReceiver.java in com.maksl5.bl_hunt
 *  © Markus 2013
 */
package com.maksl5.bl_hunt;



import com.maksl5.bl_hunt.net.CheckUpdateService;
import com.maksl5.bl_hunt.storage.PreferenceManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;



public class BootReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(	Context context,
							Intent intent) {

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent serviceIntent = new Intent(context, CheckUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
		alarmManager.cancel(pendingIntent);
		
		if (PreferenceManager.getPref(context, "pref_checkUpdate", true)) {
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1 * 60 * 1000, AlarmManager.INTERVAL_HOUR, pendingIntent);
		}


	}

}
