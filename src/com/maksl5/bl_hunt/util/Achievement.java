/**
 *  Achievement.java in com.maksl5.bl_hunt.storage
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.util;

import java.util.HashMap;
import java.util.List;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.storage.PreferenceManager;

import android.content.Context;
import android.widget.Toast;

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

	public Achievement(int id, boolean isHidden, int title, int description) {

		this.id = id;
		this.title = title;
		this.descriptionString = description;
		this.hasProgress = false;
		this.isHidden = isHidden;

	}

	// Properties

	private boolean isHidden = false;

	private int id;

	private int title;

	private int descriptionString;

	private float boost;

	private boolean hasProgress;
	private String progressString = "none";

	//Returns true if Toast should be showed.
	public boolean accomplish(BlueHunter bhApp, boolean alreadyAccomplished) {

		PreferenceManager.setPref(bhApp, "pref_achievement_" + id, bhApp.authentification.getAchieveHash(id));

		if (!alreadyAccomplished)
			return true;
		
		return false;
	}

	public void invalidate(BlueHunter bhApp) {

		PreferenceManager.deletePreference(bhApp, "pref_achievement_" + id);
	}

	/**
	 * @param numDevices
	 *            the numDevices to set
	 */
	public Achievement setNumDevices(int numDevices) {

		return this;
	}

	/**
	 * @param numExp
	 *            the numExp to set
	 */
	public Achievement setNumExp(int numExp) {

		return this;
	}

	/**
	 * @param numLevel
	 *            the numLevel to set
	 */
	public Achievement setNumLevel(int numLevel) {

		return this;
	}

	/**
	 * @param timeInterval
	 *            the timeInterval to set
	 */
	public Achievement setTimeInterval(int timeInterval) {

		return this;
	}

	/**
	 * @param manufacturerList
	 *            the manufacturerList to set
	 */
	public Achievement setManufacturerList(List<String> manufacturerList) {

		return this;
	}

	/**
	 * @param preferencesList
	 *            the preferencesList to set
	 */
	public Achievement setPreferencesList(HashMap<String, Boolean> preferencesList) {

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

	public boolean isHidden() {

		return isHidden;
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
