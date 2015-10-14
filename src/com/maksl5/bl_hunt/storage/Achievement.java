/**
 *  Achievement.java in com.maksl5.bl_hunt.storage
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.storage;

import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;

// TODO: Auto-generated Javadoc
/**
 * The Class Achievement.
 * 
 * @author Maksl5[Markus Bensing]
 */
public abstract class Achievement {

	/**
	 * 
	 */
	public Achievement(int id, int title, int description, boolean hasProgress) {

		this.id = id;
		this.title = title;
		this.descriptionString = description;
		this.hasProgress = hasProgress;

	}

	public Achievement(int id, int title, int description) {

		this.id = id;
		this.title = title;
		this.descriptionString = description;
		this.hasProgress = false;

	}

	// Properties

	/** The number of devices to complete this achievement. 0 for not needed. */
	private int numDevices;

	/** The experience needed to complete this achievement. 0 for not needed. */
	private int numExp;

	/** The level needed to complete this achievement. 0 for not needed. */
	private int numLevel;

	/**
	 * The time interval in which this achievement has to be completed. 0 for no
	 * time interval.
	 */
	private int timeInterval;

	/** The list of manufacturers for which the achievement is only calculated. */
	private List<String> manufacturerList;

	/**
	 * The preferences which must have a specific state to accomplish the
	 * achievement.
	 */
	private HashMap<String, Boolean> preferencesList;

	private int id;

	private int title;

	private int descriptionString;

	private float boost;

	private boolean hasProgress;
	private String progressString = "none";

	public void accomplish(BlueHunter bhApp, boolean alreadyAccomplished) {

		PreferenceManager.setPref(bhApp, "pref_achievement_" + id, bhApp.authentification.getAchieveHash(id));

		if (!alreadyAccomplished)
			Toast.makeText(bhApp, bhApp.getString(R.string.str_achievement_accomplish, getName(bhApp)), Toast.LENGTH_LONG).show();
	}

	public void invalidate(BlueHunter bhApp) {

		PreferenceManager.deletePreference(bhApp, "pref_achievement_" + id);
	}

	/**
	 * @param numDevices
	 *            the numDevices to set
	 */
	public Achievement setNumDevices(int numDevices) {

		this.numDevices = numDevices;
		return this;
	}

	/**
	 * @param numExp
	 *            the numExp to set
	 */
	public Achievement setNumExp(int numExp) {

		this.numExp = numExp;
		return this;
	}

	/**
	 * @param numLevel
	 *            the numLevel to set
	 */
	public Achievement setNumLevel(int numLevel) {

		this.numLevel = numLevel;
		return this;
	}

	/**
	 * @param timeInterval
	 *            the timeInterval to set
	 */
	public Achievement setTimeInterval(int timeInterval) {

		this.timeInterval = timeInterval;
		return this;
	}

	/**
	 * @param manufacturerList
	 *            the manufacturerList to set
	 */
	public Achievement setManufacturerList(List<String> manufacturerList) {

		this.manufacturerList = manufacturerList;
		return this;
	}

	/**
	 * @param preferencesList
	 *            the preferencesList to set
	 */
	public Achievement setPreferencesList(HashMap<String, Boolean> preferencesList) {

		this.preferencesList = preferencesList;
		return this;
	}

	/**
	 * @param boost
	 *            the boost to set
	 */
	public Achievement setBoost(float boost) {

		this.boost = boost;
		return this;
	}

	public String getName(Context context) {
		return context.getString(title);
	}

	public String getDescription(Context context) {
		return context.getString(descriptionString);
	}

	public float getBoost() {
		return boost;
	}

	public int getId() {
		return id;
	}

	public boolean hasProgress() {
		return hasProgress;
	}

	public String getProgressString() {
		if (hasProgress)
			return progressString;
		else
			return "none";

	}
	
	public void setNewProgressString(String progress) {
		progressString = progress;
	}

	public abstract boolean check(BlueHunter bhApp, int deviceNum, int exp);

}
