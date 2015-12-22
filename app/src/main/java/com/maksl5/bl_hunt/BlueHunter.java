/**
 * BlueHunter.java in com.maksl5.bl_hunt
 * © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;

import com.maksl5.bl_hunt.activity.ActionBarHandler;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.LoginManager;
import com.maksl5.bl_hunt.net.NetworkManager;
import com.maksl5.bl_hunt.net.SynchronizeFoundDevices;
import com.maksl5.bl_hunt.storage.PreferenceManager;

import org.acra.annotation.ReportsCrashes;

/**
 * @author Maksl5[Markus Bensing]
 *
 */
@ReportsCrashes(formKey = "dFpyWWtjQ1E3VV9EaklYbFZETmpQLVE6MQ")
public class BlueHunter extends Application {

    public static final boolean isPlayStore = true;
    public static final boolean isSupport = BuildConfig.IS_SUPPORT;
    private static int versionCode = -1;
    public Authentification authentification;
    public ActionBarHandler actionBarHandler;
    public DiscoveryManager disMan;
    public NetworkManager netMananger;
    public LoginManager loginManager;
    public SynchronizeFoundDevices synchronizeFoundDevices;
    public MainActivity mainActivity;
    public Activity currentActivity;
    private boolean isTablet = false;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {

        // TODO Auto-generated method stub
        super.onCreate();

        //ACRA.init(this);

        boolean xlarge = ((getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean large = ((getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        isTablet = (xlarge || large);

        if (isPlayStore) {
            PreferenceManager.setPref(this, "pref_checkUpdate", false);
        }

        if (isSupport) {

        }

    }

    public int getVersionCode() {

        if (versionCode != -1) return versionCode;

        try {
            return versionCode = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            return 0;
        }
    }

    public String getVersionName() {

        try {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    public boolean isTablet() {
        return isTablet;
    }

}
