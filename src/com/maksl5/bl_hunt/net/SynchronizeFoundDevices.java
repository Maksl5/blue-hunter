/**
 *  SynchronizeFoundDevices.java in com.maksl5.bl_hunt.net
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.net;

import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;


/**
 * @author Maksl5[Markus Bensing]
 *
 */
public class SynchronizeFoundDevices implements OnNetworkResultAvailableListener{
	
	private static int exp = 0;
	
	public void sart() {

		

	}

	/* (non-Javadoc)
	 * @see com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener#onResult(int, java.lang.String)
	 */
	@Override
	public boolean onResult(int requestId,
							String resultString) {

		
		return false;
	}
}
