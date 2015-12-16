package com.maksl5.bl_hunt.custom_ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.custom_ui.AdjustedEditText;
import com.maksl5.bl_hunt.custom_ui.AdjustedEditText.OnBackKeyClickedListener;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.NetworkThread;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maksl5
 */
public class ProfileLayout {

	public static String userName = "";
	private static String backUpName = "";
	private static boolean isEditable = true;

	public static void initializeView(final MainActivity mainActivity) {

		View parentContainer = mainActivity.findViewById(R.id.profileContainer);

		final TextView nameTextView = (TextView) parentContainer.findViewById(R.id.nameTextView);
		final AdjustedEditText nameEditText = (AdjustedEditText) parentContainer.findViewById(R.id.nameEditText);

		final QuickContactBadge contactImage = (QuickContactBadge) parentContainer.findViewById(R.id.contactBadge);

		// Listener
		nameTextView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				if (isEditable) {
					nameEditText.setText(nameTextView.getText());

					nameTextView.animate().setDuration(500).alpha(0f);
					nameTextView.setVisibility(TextView.GONE);

					nameEditText.setAlpha(0f);
					nameEditText.setVisibility(EditText.VISIBLE);
					nameEditText.animate().setDuration(500).alpha(1f);

					InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				}
				return true;
			}
		});

		OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

				if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
						&& nameEditText.isShown()) {

					imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);

					backUpName = userName;
					userName = nameEditText.getText().toString();

					nameTextView.setText("Applying...");
					submit();

					nameEditText.animate().setDuration(500).alpha(1f);
					nameEditText.setVisibility(EditText.GONE);

					nameTextView.setAlpha(0f);
					nameTextView.setVisibility(TextView.VISIBLE);
					nameTextView.animate().setDuration(500).alpha(1f);

					return true;
				}
				return false;
			}

			private void submit() {

				isEditable = false;

				mainActivity.getBlueHunter().authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

					@Override
					public boolean onResult(int requestId, String resultString) {

						if (requestId == Authentification.NETRESULT_ID_APPLY_NAME) {

							Pattern pattern = Pattern.compile("Error=(\\d+)");
							Matcher matcher = pattern.matcher(resultString);

							if (matcher.find()) {
								int error = Integer.parseInt(matcher.group(1));
								String errorMsg = ErrorHandler.getErrorString(mainActivity, requestId, error);

								setName(mainActivity.getBlueHunter(), backUpName);

								Toast.makeText(mainActivity, errorMsg, Toast.LENGTH_LONG).show();
							}
							else if (resultString.equals("<done />")) {
								setName(mainActivity.getBlueHunter(), userName);
							}
							if (mainActivity.getBlueHunter().loginManager.getLoginState()) isEditable = true;
						}

						return true;
					}
				});

				NetworkThread applyName = new NetworkThread(mainActivity.getBlueHunter());
				applyName.execute(AuthentificationSecure.SERVER_APPLY_NAME, String.valueOf(Authentification.NETRESULT_ID_APPLY_NAME),
						"lt=" + mainActivity.getBlueHunter().authentification.getStoredLoginToken(),
						"s=" + Authentification.getSerialNumber(), "p=" + mainActivity.getBlueHunter().authentification.getStoredPass(),
						"n=" + userName);

			}
		};

		nameEditText.setOnEditorActionListener(onEditorActionListener);

		nameEditText.setOnBackKeyClickListener(new OnBackKeyClickedListener() {

			@Override
			public void onBackKeyClicked() {

				InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

				if (nameEditText.isShown() && imm.isActive(nameEditText)) {

					imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);

					nameEditText.animate().setDuration(500).alpha(0f);
					nameEditText.setVisibility(EditText.GONE);

					nameTextView.setAlpha(0f);
					nameTextView.setVisibility(TextView.VISIBLE);
					nameTextView.animate().setDuration(500).alpha(1f);
				}
			}
		});

		mainActivity.getBlueHunter().authentification.setOnLoginChangeListener(new OnLoginChangeListener() {

			@Override
			public void loginStateChange(boolean loggedIn) {

				if (loggedIn) {
					nameTextView.setText(userName);
					nameTextView.setTextColor(ContextCompat.getColor(mainActivity, R.color.text_holo_light_blue));
					isEditable = true;

					contactImage.setEnabled(true);
					contactImage.setColorFilter(null);
				}
				else {
					nameTextView.setText("Not logged in.");
					nameTextView.setTextColor(Color.GRAY);
					isEditable = false;

					ColorMatrix cMatrix = new ColorMatrix();
					cMatrix.setSaturation(0);

					contactImage.setEnabled(false);
					contactImage.setColorFilter(new ColorMatrixColorFilter(cMatrix));
				}

			}
		});

		contactImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				mainActivity.startActivityForResult(Intent.createChooser(intent, "Select User Image"), MainActivity.REQ_PICK_USER_IMAGE);

			}
		});

		setName(mainActivity.getBlueHunter(), userName);

	}

	/**
	 * @param nameString
	 */
	public static void setName(BlueHunter blueHunter, String nameString) {

		userName = nameString;

		View parentContainer = blueHunter.mainActivity.findViewById(R.id.profileContainer);

		TextView nameTextView;

		if (parentContainer == null) {
			nameTextView = (TextView) blueHunter.mainActivity.findViewById(R.id.nameTextView);
		}
		else {
			nameTextView = (TextView) parentContainer.findViewById(R.id.nameTextView);
		}

		if (nameTextView == null) return;

		if (blueHunter.loginManager.getLoginState()) {
			nameTextView.setText(userName);
			nameTextView.setTextColor(ContextCompat.getColor(blueHunter, R.color.text_holo_light_blue));
			isEditable = true;
		}
		else {
			nameTextView.setText("Not logged in.");
			nameTextView.setTextColor(Color.GRAY);
			isEditable = false;
		}

	}

	public static void passPickedImage(MainActivity mainActivity, Intent intent) {

		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(mainActivity.getContentResolver().openInputStream(intent.getData()), null, o);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final int REQUIRED_SIZE = 200;

		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		o2.inDither = true;

		try {
			Bitmap scaledUserImage = BitmapFactory.decodeStream(mainActivity.getContentResolver().openInputStream(intent.getData()), null,
					o2);

			View parentContainer = mainActivity.mViewPager.getChildAt(FragmentLayoutManager.PAGE_PROFILE + 1);
			QuickContactBadge contactBadge = (QuickContactBadge) parentContainer.findViewById(R.id.contactBadge);

			contactBadge.setImageBitmap(scaledUserImage);

		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}