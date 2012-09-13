/**
 *  DatabaseHelper.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maksl5.bl_hunt.Authentification.OnNetworkResultAvailableListener;

import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	public final static String DATABASE_NAME = "foundDevicesDataStorage.db";
	public final static String FOUND_DEVICES_TABLE = "foundDevices";

	private MainActivity mainActivity;
	private int version;

	// CREATE DECLARATION
	public final static String FOUND_DEVICES_CREATE =
			"Create Table " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, macAddress Text Not Null, name Text, RSSI Integer Not Null, manufacturerField Text);";

	public DatabaseHelper(MainActivity mainActivity,
			int version) {

		super(mainActivity, DATABASE_NAME, null, version);

		this.mainActivity = mainActivity;
		this.version = version;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(FOUND_DEVICES_CREATE);
		showChangelog(10);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(	SQLiteDatabase db,
							int oldVersion,
							int newVersion) {

		showChangelog(oldVersion, newVersion, 0);

	}

	private void showChangelog() {

		showChangelog(0);
	}

	private void showChangelog(int limit) {

		showChangelog(0, 0, limit);
	}

	/**
	 * 
	 */
	private void showChangelog(	int oldVersion,
								int newVersion,
								int limit) {

		NetworkThread getChangelog = new NetworkThread(mainActivity, mainActivity.netMananger);

		mainActivity.authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

			@Override
			public boolean onResult(int requestId,
									String resultString) {

				if (requestId == Authentification.NETRESULT_ID_UPDATED) {

					Pattern pattern = Pattern.compile("Error=(\\d+)");
					Matcher matcher = pattern.matcher(resultString);

					if (matcher.find()) {
						int error = Integer.parseInt(matcher.group(1));

						switch (error) {
						case 1:
						case 4:
						case 5:
							Toast.makeText(mainActivity, "Error retrieving changelog for new version", Toast.LENGTH_LONG).show();
							break;
						case 90:
							Toast.makeText(mainActivity, "Updated to new version. No Changelog available.", Toast.LENGTH_LONG).show();
							break;
						case 404:
							Toast.makeText(mainActivity, "Updated to new version. Changelog could not be found on the server.", Toast.LENGTH_LONG).show();
							break;
						}
					}
					else {

						Dialog changelogDialog = new Dialog(mainActivity);
						ScrollView changeLogScrollView = new ScrollView(mainActivity);
						TextView changelogTextView = new TextView(mainActivity);
						changelogDialog.setTitle("Changelog");
						int padding =
								mainActivity.getResources().getDimensionPixelSize(R.dimen.padding_small);
						changelogTextView.setPadding(padding, padding, padding, padding);
						changelogDialog.addContentView(changeLogScrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						changeLogScrollView.addView(changelogTextView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						changelogTextView.setText(Html.fromHtml(resultString));
						changelogDialog.show();

					}

					return true;
				}

				return false;
			}
		});
		if (limit == 0) {
			if (oldVersion == 0 | newVersion == 0) {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED));
			}
			else {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "old=" + oldVersion, "new=" + newVersion);
			}
		}
		else {
			if (oldVersion == 0 | newVersion == 0) {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "l=" + limit);
			}
			else {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "old=" + oldVersion, "new=" + newVersion, "l=" + limit);
			}
			
		}

	}
}
