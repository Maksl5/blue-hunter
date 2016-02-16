package com.maksl5.bl_hunt.activity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.DiscoveryManager;
import com.maksl5.bl_hunt.DiscoveryManager.DiscoveryState;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.custom_ui.CustomPagerTransformer;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.custom_ui.ParallaxPageTransformer;
import com.maksl5.bl_hunt.custom_ui.ParallaxPageTransformer.ParallaxTransformInformation;
import com.maksl5.bl_hunt.custom_ui.fragment.AchievementsLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.DeviceDiscoveryLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.FoundDevicesLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.LeaderboardLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.LeaderboardLayout.LBAdapterData;
import com.maksl5.bl_hunt.custom_ui.fragment.ProfileLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.StatisticsFragment;
import com.maksl5.bl_hunt.custom_ui.fragment.WeeklyLeaderboardLayout;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.CheckUpdateService;
import com.maksl5.bl_hunt.net.NetworkManager;
import com.maksl5.bl_hunt.net.NetworkThread;
import com.maksl5.bl_hunt.net.SynchronizeFoundDevices;
import com.maksl5.bl_hunt.storage.AchievementSystem;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.ManufacturerList;
import com.maksl5.bl_hunt.storage.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity {

    public static final int REQ_PICK_USER_IMAGE = 32;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;
    public NotificationCompat.Builder stateNotificationBuilder;
    public TextView userInfoTextView;
    public boolean passSet = false;
    public int oldVersion = 0;
    public int newVersion = 0;
    public boolean justStarted = true;
    public boolean destroyed = false;
    public View parentView;
    private NotificationManager notificationManager;
    private TextView disStateTextView;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private BlueHunter bhApp;


    // private InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Debug.startMethodTracing("startTrace");

        bhApp = (BlueHunter) getApplication();
        bhApp.mainActivity = this;
        bhApp.currentActivity = this;

        setContentView(R.layout.act_main);

        parentView = findViewById(R.id.reLayout);

        ManufacturerList.setContext(this);

        bhApp.actionBarHandler = new ActionBarHandler(bhApp);
        bhApp.disMan = new DiscoveryManager(bhApp);

        // Create the adapter that will return a fragment for each of the
        // primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        bhApp.authentification = new Authentification(bhApp);
        bhApp.netMananger = new NetworkManager(bhApp);

        bhApp.loginManager = bhApp.authentification.new LoginManager(Authentification.getSerialNumber(),
                bhApp.authentification.getStoredPass(), bhApp.authentification.getStoredLoginToken());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(7);

        //PageTransformer parallaxPageTransformer = setupPageTransformer();

        mViewPager.setPageTransformer(true, new CustomPagerTransformer(this));


        registerListener();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (PreferenceManager.getPref(bhApp, "pref_enableBackground", true)) {
            try {
                getWindow().setBackgroundDrawableResource(R.drawable.activity_bg);
            } catch (Exception | OutOfMemoryError e) {
                PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }

        }

        if (PreferenceManager.getPref(this, "pref_showAttentionDialog", true)) {

            Builder builder = new Builder(this);
            builder.setTitle("Information");

            builder.setMessage(Html.fromHtml("Attention:<br><br>" + "This version (" + bhApp.getVersionName()
                    + ") of BlueHunter is not stable, as it is still in Alpha phase. If you find bugs, crashes, etc. I would appreciate if you go to this <a href=\"http://bluehunter.maks-dev.com/issues\">Issues Tracker</a> and make a quick entry/task. That's it.<br>Thank you very much!"));

            builder.setPositiveButton("Ok", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });

            ((TextView) builder.show().findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        }

        PreferenceManager.setPref(this, "pref_showAd", false);

//        if (BlueHunter.isSupport)
//            PreferenceManager.setPref(this, "pref_showAd", false);
//
//
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getString(R.string.ad_id));
//
//        AdRequest adRequest = new AdRequest.Builder().addTestDevice("50EB319D6AF558CA9DE12A51A68E7A2E").build();
//        mInterstitialAd.loadAd(adRequest);
//
//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                if (!destroyed && PreferenceManager.getPref(MainActivity.this, "pref_showAd", true) && !BlueHunter.isSupport) {
//                    mInterstitialAd.show();
//                }
//            }
//
//            @Override
//            public void onAdLeftApplication() {
//                Log.d("debug", "onAdLeftApplication()");
//
//                long lastClicked = PreferenceManager.getPref(MainActivity.this, "pref_lastAdClicked", 0L);
//                boolean isNewBoost = lastClicked + 86400000 < System.currentTimeMillis();
//
//                PreferenceManager.setPref(MainActivity.this, "pref_lastAdClicked", System.currentTimeMillis());
//
//                AchievementsLayout.updateBoostIndicator(bhApp);
//
//                if (isNewBoost) {
//                    Toast.makeText(MainActivity.this, getString(R.string.str_Toast_adClick), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//
//            @Override
//            public void onAdClosed() {
//                long lastClicked = PreferenceManager.getPref(MainActivity.this, "pref_lastAdClicked", 0L);
//
//                if (lastClicked + 86400000 < System.currentTimeMillis()) {
//                    RandomSnackbar.create(MainActivity.this, getString(R.string.str_Toast_adTip), 0.5D).show();
//                }
//
//            }
//        });


    }

    private PageTransformer setupPageTransformer() {
        ParallaxPageTransformer pageTransformer = new ParallaxPageTransformer();

        pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.DDtableRow5, 1, -0.5f));
        //pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.DDtableRow1, 1, -0.8f));
        pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.lvlIndicator, 1, 1.5f));
        pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.DDtableRow2, 1, -0.75f));

        pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.txt_devices, 1, -1.56f));
        pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.txt_devExpPerDay, 1, -1.57f));
        pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.txt_devExpToday, 1, -1.55f));
        pageTransformer.addViewToParallax(new ParallaxTransformInformation(R.id.txt_discovery_devInSession_value, 1, -1.58f));

        return pageTransformer;

        // return new RotationPageTransformer(160);

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

        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        bhApp.currentActivity = this;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent serviceIntent = new Intent(this, CheckUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        alarmManager.cancel(pendingIntent);

        if (PreferenceManager.getPref(this, "pref_checkUpdate", true)) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                    AlarmManager.INTERVAL_HOUR, pendingIntent);
        }

    }

    /**
     *
     */

    private void registerListener() {

        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                //bhApp.actionBarHandler.changePage(position);


            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                bhApp.actionBarHandler.transformPage(position, positionOffset);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

                switch (state) {

                    case ViewPager.SCROLL_STATE_DRAGGING:

                    case ViewPager.SCROLL_STATE_IDLE:

                    case ViewPager.SCROLL_STATE_SETTLING:

                }

            }
        });

        bhApp.authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

            @Override
            public boolean onResult(int requestId, String resultString) {

                switch (requestId) {
                    case Authentification.NETRESULT_ID_SERIAL_CHECK:

                        Pattern pattern = Pattern.compile("Error=(\\d+)");
                        Matcher matcher = pattern.matcher(resultString);

                        if (matcher.find()) {
                            int error = Integer.parseInt(matcher.group(1));

                            String errorMsg = ErrorHandler.getErrorString(bhApp, requestId, error);
                            ProfileLayout.setName(bhApp, errorMsg);

                            Snackbar.make(bhApp.mainActivity.parentView, errorMsg, Snackbar.LENGTH_INDEFINITE).show();
                            break;
                        }

                        Pattern xmlPattern = Pattern.compile("<done name=\"(.+)\" uid=\"(\\d+)\" />");
                        Matcher xmlMatcher = xmlPattern.matcher(resultString);

                        if (xmlMatcher.find()) {

                            String nameString = "";

                            try {
                                nameString = URLDecoder.decode(xmlMatcher.group(1), "UTF-8");
                                int uid = Integer.parseInt(xmlMatcher.group(2));

                                bhApp.loginManager.setUid(uid);
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            ProfileLayout.setName(bhApp, nameString);

                        }

                        bhApp.loginManager.login();
                        break;
                    case Authentification.NETRESULT_ID_GET_USER_INFO:

                        //
                        // Pattern pattern2 = Pattern.compile("Error=(\\d+)");
                        // Matcher matcher2 = pattern2.matcher(resultString);
                        //
                        // if (matcher2.find()) {
                        // int error = Integer.parseInt(matcher2.group(1));
                        //
                        // String errorMsg = ErrorHandler.getErrorString(
                        // bhApp, requestId, error);
                        //
                        // userInfoTextView.setText(errorMsg);
                        // userInfoTextView.setVisibility(View.VISIBLE);
                        // userInfoTextView.setClickable(true);
                        // userInfoTextView
                        // .setMovementMethod(LinkMovementMethod
                        // .getInstance());
                        // userInfoTextView.setLinkTextColor(Color.DKGRAY);
                        // userInfoTextView.requestLayout();
                        // userInfoTextView.invalidate();
                        //
                        // TableRow userInfoRow = (TableRow)
                        // findViewById(R.id.userInfoTableRow);
                        // userInfoRow.setVisibility(View.VISIBLE);
                        // userInfoRow.invalidate();
                        // userInfoRow.setVisibility(View.INVISIBLE);
                        // return true;
                        // }
                        //
                        // userInfoTextView.setText(Html
                        // .fromHtml(resultString));
                        // userInfoTextView.setVisibility(View.VISIBLE);
                        // userInfoTextView.setClickable(true);
                        // userInfoTextView
                        // .setMovementMethod(LinkMovementMethod
                        // .getInstance());
                        // userInfoTextView.setLinkTextColor(Color.DKGRAY);
                        // userInfoTextView.requestLayout();
                        // userInfoTextView.invalidate();
                        //
                        // TableRow userInfoRow = (TableRow)
                        // findViewById(R.id.userInfoTableRow);
                        // userInfoRow.setVisibility(View.VISIBLE);
                        // userInfoRow.invalidate();
                        // userInfoRow.setVisibility(View.INVISIBLE);
                        // break;
                        //
                        //
                }

                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        bhApp.currentActivity = this;


        getMenuInflater().inflate(R.menu.act_main, menu);
        bhApp.actionBarHandler.supplyMenu(menu);
        bhApp.actionBarHandler.initialize();

        // Setting up Views
        if (disStateTextView == null)
            disStateTextView = (TextView) findViewById(R.id.txt_discoveryState);
        // Setting up DiscoveryManager

        long disTimeA = System.currentTimeMillis();

        if (!bhApp.disMan.startDiscoveryManager()) {
            if (!bhApp.disMan.supplyTextView(disStateTextView)) {

                // ERROR
            } else {
                bhApp.disMan.startDiscoveryManager();
            }
        }

        long disTimeB = System.currentTimeMillis();

        Log.d("Init Time", "Discovery Manager: " + (disTimeB - disTimeA) + "ms");

        // Network Stuff

        bhApp.authentification.checkUpdate();

        NetworkThread serialSubmit = new NetworkThread(bhApp);
        serialSubmit.execute(AuthentificationSecure.SERVER_CHECK_SERIAL, String.valueOf(Authentification.NETRESULT_ID_SERIAL_CHECK),
                "s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(),
                "h=" + bhApp.authentification.getSerialNumberHash());

        new DatabaseManager(bhApp).close();

        bhApp.synchronizeFoundDevices = new SynchronizeFoundDevices(bhApp);
        bhApp.authentification.setOnLoginChangeListener(bhApp.synchronizeFoundDevices);

        NetworkThread getUserInfo = new NetworkThread(bhApp);
        getUserInfo.execute(AuthentificationSecure.SERVER_GET_USER_INFO, String.valueOf(Authentification.NETRESULT_ID_GET_USER_INFO));

        // Debug.startMethodTracing("startTrace");

        DeviceDiscoveryLayout.updateDuringDBLoading(this);

        LeaderboardLayout.initTabs(bhApp);
        LeaderboardLayout.changeList = new DatabaseManager(bhApp).getLeaderboardChanges();
        WeeklyLeaderboardLayout.changeList = new DatabaseManager(bhApp).getWeeklyLeaderboardChanges();
        LeaderboardLayout.refreshLeaderboard(bhApp);

        ProfileLayout.initializeView(this);
        StatisticsFragment.initializeStatisticsView(this);

        if (DatabaseManager.getCachedList() == null) {
            new DatabaseManager(bhApp).loadAllDevices();
        } else {
            DeviceDiscoveryLayout.updateIndicatorViews(bhApp.mainActivity);
            FoundDevicesLayout.refreshFoundDevicesList(bhApp);
            AchievementsLayout.initializeAchievements(bhApp);
            AchievementsLayout.updateBoostIndicator(bhApp);

            if (PreferenceManager.getPref(this, "pref_runDiscoveryAfterStart", false)) {
                ((CompoundButton) bhApp.actionBarHandler.getActionView(R.id.menu_switch)).setChecked(true);
            }

            justStarted = false;
            updateNotification();

        }


        // Debug.stopMethodTracing();

        upgrade();


//        if (!PreferenceManager.getPref(MainActivity.this, "pref_showAd", true) && !BlueHunter.isSupport) {
//            RandomSnackbar.create(MainActivity.this, getString(R.string.str_Toast_adTip), 0.2D).show();
//        }


        return true;
    }

    /**
     *
     */
    private void upgrade() {

        if (oldVersion == 0 || newVersion == 0) return;

        if (oldVersion > newVersion) return;

        if (oldVersion < 1065) {
            bhApp.synchronizeFoundDevices.needForceOverrideUp = true;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_info:

                break;
            case R.id.menu_refresh:
                if (bhApp.actionBarHandler.getCurrentPage() == FragmentLayoutManager.PAGE_LEADERBOARD) {
                    LeaderboardLayout.refreshLeaderboard(bhApp);
                }
                break;
            case R.id.menu_submit_mac:
                Intent intentSubmit = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bluehunter.mac.maks-dev.com"));
                startActivity(intentSubmit);
                break;
            case R.id.menu_boostIndicator:
                LevelSystem.getBoostCompositionDialog(bhApp).show();
                break;
            default:
                break;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if ((requestCode == 64 | requestCode == 128) & bhApp.disMan != null)
            bhApp.disMan.passEnableBTActivityResult(resultCode, requestCode);

        if (requestCode == REQ_PICK_USER_IMAGE && resultCode == RESULT_OK)
            ProfileLayout.passPickedImage(this, intent);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {

        if (PreferenceManager.getPref(bhApp, "pref_disBtExit", false)) {

            bhApp.disMan.disableBluetooth();

        }

        destroyed = true;

        notificationManager.cancel(1);
        bhApp.loginManager.unregisterInternetReceiver();

        bhApp.disMan.unregisterReceiver();
        bhApp.disMan.stopDiscoveryManager();

        // GLOBAL

        SparseArray<Integer[]> leaderboardChanges = new SparseArray<>();

        for (int i = 0; i < LeaderboardLayout.completeLbList.size(); i++) {
            LBAdapterData leaderboardEntry = LeaderboardLayout.completeLbList.get(i);

            leaderboardChanges.put(leaderboardEntry.getId(), new Integer[]{
                    i + 1, leaderboardEntry.getExp(), leaderboardEntry.getDevNum()});

        }

        // WEEKLY

        SparseArray<Integer[]> weeklyLeaderboardChanges = new SparseArray<>();

        for (int i = 0; i < WeeklyLeaderboardLayout.completeLbList.size(); i++) {
            com.maksl5.bl_hunt.custom_ui.fragment.WeeklyLeaderboardLayout.LBAdapterData weeklyLeaderboardEntry = WeeklyLeaderboardLayout.completeLbList
                    .get(i);

            weeklyLeaderboardChanges.put(weeklyLeaderboardEntry.getId(), new Integer[]{
                    i + 1, weeklyLeaderboardEntry.getDevNum()});

        }

        try {
            new DatabaseManager(bhApp).resetLeaderboardChanges();
            new DatabaseManager(bhApp).setLeaderboardChanges(leaderboardChanges);

            new DatabaseManager(bhApp).resetWeeklyLeaderboardChanges();
            new DatabaseManager(bhApp).setWeeklyLeaderboardChanges(weeklyLeaderboardChanges);
        } catch (SQLiteDatabaseLockedException e) {
            Snackbar.make(bhApp.mainActivity.parentView, "Could not save Leaderboard changes.", Snackbar.LENGTH_LONG).show();
        }

        bhApp.synchronizeFoundDevices.saveChanges();

        bhApp.netMananger.cancelAllTasks();
        DatabaseManager.cancelAllTasks();
        FoundDevicesLayout.cancelAllTasks();
        LeaderboardLayout.cancelAllTasks();
        AchievementSystem.cancelAllTasks();

        super.onDestroy();
    }

    public BlueHunter getBlueHunter() {

        return bhApp;
    }


    public void updateNotification() {

        if (PreferenceManager.getPref(this, "pref_showNotification", true)) {

            stateNotificationBuilder = new NotificationCompat.Builder(this);

            int discoveryStateInt = bhApp.disMan.getCurDiscoveryState();

            boolean stopped = discoveryStateInt != DiscoveryState.DISCOVERY_STATE_RUNNING && discoveryStateInt != DiscoveryState.DISCOVERY_STATE_FINISHED;

            stateNotificationBuilder.setOngoing(true);
            stateNotificationBuilder.setSmallIcon((stopped) ? R.drawable.ic_bluetooth : R.drawable.ic_bluetooth_searching);
            stateNotificationBuilder.setAutoCancel(false);
            stateNotificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0));


            int level = LevelSystem.getLevel(LevelSystem.getCachedUserExp(bhApp));

            stateNotificationBuilder.setProgress(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level),
                    LevelSystem.getCachedUserExp(bhApp) - LevelSystem.getLevelStartExp(level), false);

            DecimalFormat df = new DecimalFormat(",###");

            String discoveryState = DiscoveryState.getUnformatedDiscoveryState(bhApp.disMan.getCurDiscoveryState(), this);


            String userExpForm = df.format(LevelSystem.getCachedUserExp(bhApp));
            String levelEndExpForm = df.format(LevelSystem.getLevelEndExp(level));

            String expString = String.format("%s / %s %s", userExpForm, levelEndExpForm, getString(R.string.str_foundDevices_exp_abbreviation));
            String levelString = String.format("%s: %d", getString(R.string.str_foundDevices_level), level);
            String devicesString = String.format("%s %d", getString(R.string.str_discovery_devices), new DatabaseManager(bhApp).getDeviceNum());
            String rankString = String.format("%s: %d", getString(R.string.str_leaderboard_rank), LeaderboardLayout.userRank);

            String notificationText = String.format("%s%n%s%n%s%n%s", expString, levelString, devicesString, rankString);

            stateNotificationBuilder.setContentTitle(discoveryState);
            stateNotificationBuilder.setContentText(expString);


            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            inboxStyle.setBigContentTitle(discoveryState);
            inboxStyle.addLine(expString);
            inboxStyle.addLine(levelString);
            inboxStyle.addLine(devicesString);

            if (LeaderboardLayout.userRank > 0)
                inboxStyle.addLine(rankString);


            stateNotificationBuilder.setStyle(inboxStyle);


            int iconId = (stopped) ? R.drawable.ic_play_circle : R.drawable.ic_pause_circle;
            String actionString = (stopped) ? getString(R.string.str_notification_start) : getString(R.string.str_notification_pause);

            int requestCode = (stopped) ? 1 : 2;

            Intent actionIntent = new Intent("notification.userinput");
            actionIntent.putExtra("MODE", requestCode);
            PendingIntent pendingActionIntent = PendingIntent.getBroadcast(this, 1, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            stateNotificationBuilder.addAction(iconId, actionString, pendingActionIntent);


            notificationManager.notify(1, stateNotificationBuilder.build());

        }
    }

    @SuppressWarnings("deprecation")
    public void alterNotification(boolean show) {

        if (show) {

            updateNotification();

        } else {
            notificationManager.cancel(1);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.support.v4.app.FragmentActivity#onConfigurationChanged(android
     * .content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if (PreferenceManager.getPref(bhApp, "pref_enableBackground", true)) {
            try {
                getWindow().setBackgroundDrawableResource(R.drawable.bg_main);
            } catch (Exception | OutOfMemoryError e) {
                PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }

        }

        // Bundle params = new Bundle();
        // params.putInt(CustomSectionFragment.ARG_SECTION_NUMBER,
        // FragmentLayoutManager.PAGE_DEVICE_DISCOVERY);
        //
        // View view = FragmentLayoutManager.getSpecificView(params,
        // getLayoutInflater(), viewGroup, this);

        mSectionsPagerAdapter.notifyDataSetChanged();

        disStateTextView = (TextView) findViewById(R.id.txt_discoveryState);
        bhApp.disMan.supplyNewTextView(disStateTextView);

        DeviceDiscoveryLayout.updateIndicatorViews(this);
        ProfileLayout.initializeView(this);
        StatisticsFragment.initializeStatisticsView(this);
        AchievementsLayout.initializeAchievements(bhApp);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        final int oldHeight = progressBar.getHeight();
        final int oldWidth = progressBar.getWidth();

        Log.d("onConfigurationChanged", "oldHeight=" + oldHeight);
        Log.d("onConfigurationChanged", "oldWidth=" + oldWidth);

        ViewTreeObserver observer = progressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                Log.d("onGlobalLayout", "height=" + progressBar.getHeight());
                Log.d("onGlobalLayout", "width=" + progressBar.getWidth());

                if (progressBar.getHeight() != oldHeight && progressBar.getWidth() != oldWidth) {
                    progressBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    DeviceDiscoveryLayout.updateNextRankIndicator(MainActivity.this, DeviceDiscoveryLayout.expToUpdate);

                }
            }
        });

        LeaderboardLayout.initTabs(bhApp);
        LeaderboardLayout.refreshLeaderboard(bhApp, true);
        FoundDevicesLayout.refreshFoundDevicesList(bhApp);

    }

    public static class CustomSectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        public CustomSectionFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            Bundle args = getArguments();

            return FragmentLayoutManager.getSpecificView(args, inflater, container, container.getContext());

        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {

            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int i) {

            Fragment fragment = new CustomSectionFragment();
            Bundle args = new Bundle();
            args.putInt(CustomSectionFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {

            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case FragmentLayoutManager.PAGE_DEVICE_DISCOVERY:
                    return getString(R.string.str_pageTitle_main).toUpperCase();
                case FragmentLayoutManager.PAGE_LEADERBOARD:
                    return getString(R.string.str_pageTitle_leaderboard).toUpperCase();
                case FragmentLayoutManager.PAGE_FOUND_DEVICES:
                    return getString(R.string.str_pageTitle_foundDevices).toUpperCase();
                case FragmentLayoutManager.PAGE_ACHIEVEMENTS:
                    return getString(R.string.str_pageTitle_achievements).toUpperCase();
                case FragmentLayoutManager.PAGE_PROFILE:
                    return getString(R.string.str_pageTitle_profile).toUpperCase();
                case FragmentLayoutManager.PAGE_STATISTICS:
                    return getString(R.string.str_pageTitle_statistics).toUpperCase();
            }
            return null;
        }
    }

}
