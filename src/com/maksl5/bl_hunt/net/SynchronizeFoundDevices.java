/**
 *  SynchronizeFoundDevices.java in com.maksl5.bl_hunt.net
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.net;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.renderscript.ProgramStore.BlendDstFunc;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.net.Authentification.LoginManager;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.storage.DatabaseManager;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class SynchronizeFoundDevices implements OnNetworkResultAvailableListener {

	private static int exp = 0;
	private static int deviceNum = 0;
	private BlueHunter blueHunter;

	public SynchronizeFoundDevices(BlueHunter blHunt) {

		blueHunter = blHunt;

	}

	public void start() {

		exp = LevelSystem.getUserExp(blueHunter);
		deviceNum = new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getDeviceNum();

		boolean loggedIn = blueHunter.loginManager.getLoginState();

		if (loggedIn) {

			blueHunter.authentification.setOnNetworkResultAvailableListener(this);
			
			NetworkThread checkSync = new NetworkThread(blueHunter);
			checkSync.execute(AuthentificationSecure.SERVER_SYNC_FD_CHECK, String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_CHECK), "lt=" + blueHunter.authentification.getStoredLoginToken(), "s=" + Authentification.getSerialNumber(), "p=" + blueHunter.authentification.getStoredPass(), "e=" + exp, "n=" + deviceNum);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener#onResult(int, java.lang.String)
	 */
	@Override
	public boolean onResult(int requestId,
							String resultString) {

		switch (requestId) {
		case Authentification.NETRESULT_ID_SYNC_FD_CHECK:

			
			Pattern pattern = Pattern.compile("Error=(\\d+)");
			Matcher matcher = pattern.matcher(resultString);

			if (matcher.find()) {
				int error = Integer.parseInt(matcher.group(1));

				String errorMsg = ErrorHandler.getErrorString(blueHunter, requestId, error);
				
				Toast.makeText(blueHunter, errorMsg, Toast.LENGTH_LONG).show();
				return true;
			}
			
			Pattern checkSyncPattern = Pattern.compile("<needsSync>([0-1])</needsSync>");
			Matcher checkSyncMatcher = checkSyncPattern.matcher(resultString);
			
			if(checkSyncMatcher.find()) {
				boolean needsSync = "1".equals(checkSyncMatcher.group(1));
				
				String tempMsg = "";
				
				if(needsSync) {
					tempMsg = "Needs sync.";
				}else {
					tempMsg = "Doesn't need sync.";
				}
				
				Toast.makeText(blueHunter, tempMsg, Toast.LENGTH_LONG).show();
				
			}
			return true;
		}

		return false;
	}
}
