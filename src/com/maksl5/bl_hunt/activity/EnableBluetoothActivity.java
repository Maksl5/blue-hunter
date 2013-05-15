/**
 * 
 */
package com.maksl5.bl_hunt.activity;



import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.storage.PreferenceManager;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class EnableBluetoothActivity extends Activity {

	public static final int BT_ENABLE_RESULT_ENABLE = 1;
	public static final int BT_ENABLE_RESULT_NOT_ENABLE = -1;

	private Button yesButton;
	private Button noButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		super.onCreate(savedInstanceState);

		((BlueHunter) getApplication()).currentActivity = this;

		setContentView(R.layout.act_enable_bluetooth);

		yesButton = (Button) findViewById(R.id.yesButton);
		noButton = (Button) findViewById(R.id.noButton);

		registerListeners();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		super.onResume();

		((BlueHunter) getApplication()).currentActivity = this;

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void registerListeners() {

		final CheckBox rememberCheckBox = (CheckBox) findViewById(R.id.chkRemember);

		yesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (rememberCheckBox.isChecked())
					PreferenceManager.setPref(getApplicationContext(), "pref_enableBT_remember", 1);

				EnableBluetoothActivity.this.setResult(BT_ENABLE_RESULT_ENABLE);
				finish();

			}
		});

		// ///////////////////////////////
		// ///////////////////////////////

		noButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (rememberCheckBox.isChecked())
					PreferenceManager.setPref(getApplicationContext(), "pref_enableBT_remember", 2);

				EnableBluetoothActivity.this.setResult(BT_ENABLE_RESULT_NOT_ENABLE);
				finish();

			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(	int keyCode,
							KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) return true;
		return false;
	}

}
