/**
 * 
 */
package com.maksl5.bl_hunt;



import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



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

		// TODO Auto-generated method stub

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		super.onCreate(savedInstanceState);

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

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void registerListeners() {

		yesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				EnableBluetoothActivity.this.setResult(BT_ENABLE_RESULT_ENABLE);
				finish();

			}
		});

		// ///////////////////////////////
		// ///////////////////////////////

		noButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

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
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) return true;
		return false;
	}

}
