/**
 * 
 */
package com.maksl5.bl_hunt;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Contacts.Intents.UI;
import android.view.View;
import android.view.WindowManager;


/**
 * @author Maksl5[Markus Bensing]
 *
 */
public class EnableBluetoothActivity extends Activity {
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		// TODO Auto-generated method stub
		
		
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.act_enable_bluetooth);
	}

}
