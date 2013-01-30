/**
 *  SynchronizeFoundDevices.java in com.maksl5.bl_hunt.net
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.net;



import com.maksl5.bl_hunt.BlueHunter;
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

		exp = LevelSystem.getUserExp(blHunt);
		deviceNum = new DatabaseManager(blHunt, blHunt.getVersionCode()).getDeviceNum();

	}

	public void start() {

		exp = LevelSystem.getUserExp(blueHunter);
		deviceNum = new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getDeviceNum();

		boolean loggedIn = blueHunter.loginManager.getLoginState();

		if (loggedIn) {

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

			break;

		default:
			break;
		}

		return false;
	}
}
