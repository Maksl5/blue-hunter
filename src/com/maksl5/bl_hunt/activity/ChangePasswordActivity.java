/**
 * 
 */
package com.maksl5.bl_hunt.activity;



import java.security.PublicKey;

import com.maksl5.bl_hunt.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * @author Maksl5
 * 
 */
public class ChangePasswordActivity extends Activity {

	public static final int MODE_CHANGE_ONLINE_PASS = 100;
	public static final int MODE_CHANGE_LOGIN_PASS = 101;

	Button cancelButton;
	Button applyButton;

	EditText oldPass;
	EditText newPass;
	EditText confirmPass;

	ImageView equalIndicator;

	boolean isPasswordSet = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		super.onCreate(savedInstanceState);

		Bundle parameters = getIntent().getExtras();
		int mode = parameters.getInt("mode");
		isPasswordSet = parameters.getBoolean("passSet");

		if (mode == MODE_CHANGE_ONLINE_PASS) {
			setContentView(R.layout.act_change_online_pass);
			
			TextView title = (TextView) findViewById(R.id.textView1);
			if(!isPasswordSet) {
				title.setText(R.string.str_changePass_new_title);
			}

			cancelButton = (Button) findViewById(R.id.cancelButton);
			applyButton = (Button) findViewById(R.id.confirmButton);

			oldPass = (EditText) findViewById(R.id.oldPassEdit);
			newPass = (EditText) findViewById(R.id.newPassEdit);
			confirmPass = (EditText) findViewById(R.id.confirmPassEdit);

			equalIndicator = (ImageView) findViewById(R.id.equalIndicator);

			applyButton.setEnabled(false);

			if (!isPasswordSet) {
				oldPass.setVisibility(EditText.GONE);
			}

			cancelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					setResult(0);
					finish();

				}
			});

			applyButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					String caption = (String) applyButton.getText();
					if (caption.equalsIgnoreCase(getString(R.string.str_changePass_applyButton)) && newPass.getText().toString() != null && !newPass.getText().toString().equals("")) {
						applyButton.setText(R.string.str_changePass_sureButton);
					}
					else if (caption.equalsIgnoreCase(getString(R.string.str_changePass_sureButton))) {

						if (newPass.getText().toString().equals(confirmPass.getText().toString())) {
							if (isPasswordSet && oldPass.getText() != null && !oldPass.getText().equals("")) {
								Intent intent = new Intent();
								intent.putExtra("oldPass", MainActivity.thisActivity.authentification.getPassHash(oldPass.getText().toString()));
								intent.putExtra("newPass", MainActivity.thisActivity.authentification.getPassHash(newPass.getText().toString()));
								setResult(1, intent);
								finish();
							}
							else {
								Intent intent = new Intent();
								intent.putExtra("newPass", MainActivity.thisActivity.authentification.getPassHash(newPass.getText().toString()));
								setResult(2, intent);
								finish();
							}
						}

					}

				}
			});

			oldPass.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {

					applyButton.setText(R.string.str_changePass_applyButton);

					if (isPasswordSet) {
						if (s.toString() == null || s.toString().equals("")) {
							applyButton.setEnabled(false);
						}
						else {
							applyButton.setEnabled(true);
						}
					}

				}
			});

			TextWatcher textWatcher = new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {

					applyButton.setText(R.string.str_changePass_applyButton);
					checkEqualPass();
				}
			};

			newPass.addTextChangedListener(textWatcher);
			confirmPass.addTextChangedListener(textWatcher);

		}else if (mode == MODE_CHANGE_LOGIN_PASS) {
			setContentView(R.layout.act_change_login_pass);
			
			newPass = (EditText) findViewById(R.id.newPassEdit);
			
			cancelButton = (Button) findViewById(R.id.cancelButton);
			applyButton = (Button) findViewById(R.id.confirmButton);
			
			applyButton.setEnabled(false);
			
			cancelButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				
					setResult(0);
					finish();
					
				}
			});
			
			applyButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				
					String newPassText = newPass.getText().toString();
					
					if(newPassText != null && !newPassText.equals("")) {
						
						Intent intent = new Intent();
						intent.putExtra("newLoginPass", MainActivity.thisActivity.authentification.getPassHash(newPassText));
						setResult(1, intent);
						finish();
						
					}
					
				}
			});
			
			newPass.addTextChangedListener( new TextWatcher() {
				
				@Override
				public void onTextChanged(	CharSequence s,
											int start,
											int before,
											int count) {
				
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void beforeTextChanged(	CharSequence s,
												int start,
												int count,
												int after) {
				
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
				
					if(s.toString() != null && !s.toString().equals("")) {
						applyButton.setEnabled(true);
					}else {
						applyButton.setEnabled(false);
					}
					
				}
			});
			
			
		}

	}

	public void checkEqualPass() {

		String newPassText = newPass.getText().toString();
		String confirmPassText = confirmPass.getText().toString();
		String oldPassText = oldPass.getText().toString();

		if (!isPasswordSet) {
			if (newPassText != null && !newPassText.equals("") && newPassText.equals(confirmPassText)) {

				equalIndicator.setImageResource(R.drawable.ic_correct);
				applyButton.setEnabled(true);
			}
			else {
				equalIndicator.setImageResource(R.drawable.ic_error);
				applyButton.setEnabled(false);
			}
		}else {
			if (newPassText != null && !newPassText.equals("") && newPassText.equals(confirmPassText) && oldPassText != null && !oldPassText.equals("")) {

				equalIndicator.setImageResource(R.drawable.ic_correct);
				applyButton.setEnabled(true);
			}
			else {
				equalIndicator.setImageResource(R.drawable.ic_error);
				applyButton.setEnabled(false);
			}
			
		}

	}

}
