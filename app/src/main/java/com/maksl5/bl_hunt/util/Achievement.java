/**
 *  Achievement.java in com.maksl5.bl_hunt.storage
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.util;

import android.content.Context;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.storage.PreferenceManager;

// TODO: Auto-generated Javadoc
/**
 * The Class Achievement.
 * 
 * @author Maksl5[Markus Bensing]
 */
public abstract class Achievement {

	private final int id;
	private final int title;
	// Properties
	private final int descriptionString;
	private final boolean hasProgress;
	private boolean isHidden = false;
	private float boost;
	private String progressString = "none";

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

	//Returns true if Toast should be showed.
	public boolean accomplish(BlueHunter bhApp, boolean alreadyAccomplished) {

		PreferenceManager.setPref(bhApp, "pref_achievement_" + id, bhApp.authentification.getAchieveHash(id));

		return !alreadyAccomplished;

	}

	public void invalidate(BlueHunter bhApp) {

		PreferenceManager.deletePreference(bhApp, "pref_achievement_" + id);
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

	/**
	 * @param boost the boost to set
	 */
	public Achievement setBoost(float boost) {

		this.boost = boost;
		return this;
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
