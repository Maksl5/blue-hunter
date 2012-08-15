/**
 *  Authentification.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import android.R.integer;
import android.content.Context;
import android.widget.Toast;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class Authentification {

	private AuthentificationSecure secure;
	private Context context;
	private ArrayList<OnNetworkResultAvailableListener> onNetworkResultAvailableListener = new ArrayList<Authentification.OnNetworkResultAvailableListener>();

	
	public Authentification(Context con) {
		context = con;
		
		secure = new AuthentificationSecure(this);

	}

	public static String getSerialNumber() {

		String serial = "NULL";

		try {
			Class<?> sysPropClass = Class.forName("android.os.SystemProperties");
			Method get = sysPropClass.getMethod("get", String.class);
			serial = (String) get.invoke(sysPropClass, "ro.serialno");
		}
		catch (Exception ignored) {
			serial = "NULL";
		}

		if(serial == null | serial.equals(""))
			serial = "NULL";
		
		return serial;
	}
	
	public String getSerialNumberHash() {
		return secure.getSerialHash(getSerialNumber());
	}

	
	/**
	 * @return the context
	 */
	public Context getContext() {
	
		return context;
	}
	
public void start() {

	
}

public interface OnNetworkResultAvailableListener {
	public abstract void onResult(int requestId, String resultString);
}

public void setOnNetworkResultAvailableListener(OnNetworkResultAvailableListener listener) {
	onNetworkResultAvailableListener.add(listener);
}

/**
 * @return the onNetworkResultAvailableListener
 */
public void fireOnNetworkResultAvailable(int requestId, String resultString) {

for (OnNetworkResultAvailableListener listener : onNetworkResultAvailableListener) {
	listener.onResult(requestId, resultString);
}
}
}
