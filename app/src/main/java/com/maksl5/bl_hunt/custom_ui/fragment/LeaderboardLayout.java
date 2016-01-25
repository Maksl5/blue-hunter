package com.maksl5.bl_hunt.custom_ui.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.storage.PreferenceManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author Maksl5
 */
public class LeaderboardLayout {

    public static final int ARRAY_INDEX_NAME = 0;
    public static final int ARRAY_INDEX_LEVEL = 1;
    public static final int ARRAY_INDEX_PROGRESS_MAX = 2;
    public static final int ARRAY_INDEX_PROGRESS_VALUE = 3;
    public static final int ARRAY_INDEX_DEV_NUMBER = 4;
    public static final int ARRAY_INDEX_EXP = 5;
    public static final int ARRAY_INDEX_ID = 6;
    public final static ArrayList<LBAdapterData> completeLbList = new ArrayList<>();
    public static SparseArray<Integer[]> changeList = new SparseArray<>();
    public static int userRank = -1;
    public static int currentSelectedTab = 0;
    private volatile static ArrayList<LBAdapterData> showedLbList = new ArrayList<>();
    private static ThreadManager threadManager = null;

    public static void initTabs(final BlueHunter bhApp) {

        if (bhApp.mainActivity.mViewPager == null) {
            bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
        }

        ViewPager pager = bhApp.mainActivity.mViewPager;

        TabHost tabHost = (TabHost) pager.findViewById(android.R.id.tabhost);

        tabHost.setup();

        TabSpec globalTab = tabHost.newTabSpec("global").setIndicator(bhApp.getString(R.string.str_leaderboard_global))
                .setContent(R.id.globalTab);
        TabSpec weeklyTab = tabHost.newTabSpec("weekly").setIndicator(bhApp.getString(R.string.str_leaderboard_weekly))
                .setContent(R.id.weeklyTab);

        tabHost.addTab(globalTab);
        tabHost.addTab(weeklyTab);

        tabHost.setCurrentTab(currentSelectedTab);

        WeeklyLeaderboardLayout.timerTextView = (TextView) pager.findViewById(R.id.weeklyTimerTxt);

        tabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                bhApp.actionBarHandler.onQueryTextChange("");
                bhApp.actionBarHandler.getMenuItem(R.id.menu_search).collapseActionView();

                if (tabId.equals("global")) {
                    currentSelectedTab = 0;

                } else {
                    currentSelectedTab = 1;

                    Thread updateTimerThread = new Thread() {

                        @Override
                        public void run() {

                            final long nextCycleTimestamp = PreferenceManager.getPref(bhApp, "cachedNextCycle", 0L);

                            while (WeeklyLeaderboardLayout.timerTextView != null && currentSelectedTab == 1 && nextCycleTimestamp != 0) {

                                bhApp.mainActivity.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        if (WeeklyLeaderboardLayout.timeOffset != 0) {
                                            long restTime = nextCycleTimestamp
                                                    - (System.currentTimeMillis() - WeeklyLeaderboardLayout.timeOffset);

                                            if (restTime < 0) {
                                                refreshLeaderboard(bhApp);
                                                return;
                                            }

                                            int days = (int) (restTime / (float) 86400000);
                                            int hours = (int) (restTime / (float) 3600000) - days * 24;
                                            int minutes = (int) (restTime / (float) 60000) - days * 24 * 60 - hours * 60;
                                            int seconds = (int) (restTime / (float) 1000) - days * 24 * 60 * 60 - hours * 60 * 60
                                                    - minutes * 60;

                                            String timerString;

                                            if (nextCycleTimestamp == 1450051200000L) {
                                                timerString = String.format("Initial Start in %dd %dh %dmin and %dsec", days, hours,
                                                        minutes, seconds);

                                            } else {
                                                timerString = bhApp.getString(R.string.str_leaderboard_weeklyTimer, days, hours, minutes,
                                                        seconds);
                                            }

                                            WeeklyLeaderboardLayout.timerTextView.setText(timerString);
                                        }

                                    }
                                });

                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }
                        }

                    };

                    updateTimerThread.start();

                }

            }
        });

    }

    public static void refreshLeaderboard(final BlueHunter bhApp) {

        refreshLeaderboard(bhApp, false);
    }

    public static void refreshLeaderboard(final BlueHunter bhApp, boolean orientationChanged) {

        if (orientationChanged) {

            ListView listView;
            LeaderboardAdapter ldAdapter;

            if (bhApp.mainActivity.mViewPager == null) {
                bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
            }

            ViewPager pager = bhApp.mainActivity.mViewPager;
            View pageView = pager.getChildAt(FragmentLayoutManager.PAGE_LEADERBOARD + 1);

            if (pageView == null) {
                listView = (ListView) pager.findViewById(R.id.globalLdListView);
            } else {
                listView = (ListView) pageView.findViewById(R.id.globalLdListView);
            }

            if (listView == null) {
                listView = (ListView) bhApp.mainActivity.findViewById(R.id.globalLdListView);
            }

            if (listView == null) {
                return;
            }

            ldAdapter = (LeaderboardAdapter) listView.getAdapter();
            if (ldAdapter == null || ldAdapter.isEmpty()) {
                ldAdapter = new LeaderboardLayout().new LeaderboardAdapter(bhApp.mainActivity,
                        showedLbList);
                listView.setAdapter(ldAdapter);
            }

            ldAdapter.notifyDataSetChanged();

            refreshUserRow(bhApp.mainActivity);

            WeeklyLeaderboardLayout.refreshLeaderboard(bhApp, true);

            return;

        }

        if (threadManager == null) {
            threadManager = new LeaderboardLayout().new ThreadManager();
        }

        RefreshThread refreshThread = new LeaderboardLayout().new RefreshThread(bhApp, threadManager);
        if (refreshThread.canRun()) {
            showedLbList.clear();
            completeLbList.clear();
            bhApp.actionBarHandler.getMenuItem(R.id.menu_search).collapseActionView();
            refreshThread.execute(1, 5, 0);
        }

        WeeklyLeaderboardLayout.refreshLeaderboard(bhApp, false);

    }

    public static void filterLeaderboard(String text, BlueHunter bhApp) {

        if (threadManager.running) return;

        if (bhApp.mainActivity.mViewPager == null) {
            bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
        }

        ViewPager pager = bhApp.mainActivity.mViewPager;
        View pageView = pager.getChildAt(FragmentLayoutManager.PAGE_LEADERBOARD + 1);
        ListView lv;

        if (pageView == null) {
            lv = (ListView) pager.findViewById(R.id.globalLdListView);
        } else {
            lv = (ListView) pageView.findViewById(R.id.globalLdListView);
        }

        if (lv == null) {
            lv = (ListView) bhApp.mainActivity.findViewById(R.id.globalLdListView);
        }

        if (lv == null) {
            return;
        }

        LeaderboardAdapter lbAdapter = (LeaderboardAdapter) lv.getAdapter();

        if (lbAdapter == null || lbAdapter.isEmpty()) {
            lbAdapter = new LeaderboardLayout().new LeaderboardAdapter(bhApp.mainActivity, showedLbList);
            lv.setAdapter(lbAdapter);
        }

        text = text.toLowerCase();

        if (text.length() == 0) {
            if (!showedLbList.equals(completeLbList)) {
                showedLbList = new ArrayList<>(completeLbList);
                lbAdapter.refreshList(showedLbList);
            }
        } else {

            ArrayList<LBAdapterData> filterList = new ArrayList<>(completeLbList);

            final int count = filterList.size();
            final ArrayList<LBAdapterData> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final LBAdapterData data = filterList.get(i);

                if (data.getName().toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

                if (("" + data.getDevNum()).toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

                if (("" + data.getExp()).toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

                if (("" + data.getLevel()).toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

            }

            showedLbList = newValues;
            lbAdapter.refreshList(showedLbList);

        }

    }

    public static void scrollToPosition(MainActivity mainActivity, int index) {

        if (completeLbList != null && completeLbList.size() != 0) {

            if (mainActivity.mViewPager == null) {
                mainActivity.mViewPager = (ViewPager) mainActivity.findViewById(R.id.pager);
            }

            mainActivity.mViewPager.setCurrentItem(FragmentLayoutManager.PAGE_LEADERBOARD, true);

            View pageView = mainActivity.findViewById(R.id.ldRootContainer);

            TabHost tabHost = (TabHost) pageView.findViewById(android.R.id.tabhost);
            tabHost.setCurrentTab(0);

            ListView listView = (ListView) mainActivity.mViewPager.findViewById(R.id.globalLdListView);

            if (listView != null) {

                ensureScrolling(listView, index);

            }

        }

    }

    public static void ensureScrolling(final ListView lv, int index) {
        ensureScrolling(lv, index, 0);
    }

    public static void ensureScrolling(final ListView lv, final int index, int attempts) {

        if (lv.getFirstVisiblePosition() != index && attempts < 1) {
            attempts++;
            final int attemptsForAnonymousClass = attempts;
            lv.smoothScrollToPositionFromTop(index, 0, 500);
            lv.post(new Runnable() {
                @Override
                public void run() {
                    ensureScrolling(lv, index, attemptsForAnonymousClass);
                }
            });
        }
    }

    public static void cancelAllTasks() {
        if (threadManager != null && threadManager.refreshThread != null)
            threadManager.refreshThread.cancel(true);

    }

    private static void refreshUserRow(final MainActivity mainActivity) {

        LBAdapterData userData = completeLbList.get(userRank - 1);

        String name = userData.getName();
        int id = userData.getId();
        int level = userData.getLevel();
        int progressMax = userData.getProgressMax();
        int progressValue = userData.getProgressValue();
        int exp = userData.getExp();
        int num = userData.getDevNum();

        View container = mainActivity.findViewById(R.id.globalLdUserRL);

        container.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                scrollToPosition(mainActivity, userRank - 1);
            }
        });

        TextView rankView = (TextView) container.findViewById(R.id.rankTxtView);
        TextView nameView = (TextView) container.findViewById(R.id.nameTxtView);
        TextView levelView = (TextView) container.findViewById(R.id.levelTxtView);
        ProgressBar levelPrgView = (ProgressBar) container.findViewById(R.id.levelPrgBar);
        TextView devicesView = (TextView) container.findViewById(R.id.devTxtView);
        TextView expView = (TextView) container.findViewById(R.id.expTxtView);

        ImageView changeRankImgView = (ImageView) container.findViewById(R.id.changeRankImgView);
        TextView changeRankTxtView = (TextView) container.findViewById(R.id.changeRankTxtView);

        ImageView changeEXPImgView = (ImageView) container.findViewById(R.id.changeEXPImgView);
        TextView changeEXPTxtView = (TextView) container.findViewById(R.id.changeEXPTxtView);

        ImageView changeDEVImgView = (ImageView) container.findViewById(R.id.changeDEVImgView);
        TextView changeDEVTxtView = (TextView) container.findViewById(R.id.changeDEVTxtView);

        DecimalFormat decimalFormat = new DecimalFormat(",###");

        rankView.setText("" + userRank + ".");
        nameView.setText(name);
        nameView.setTag(id);
        levelView.setText("" + level);
        levelPrgView.setMax(progressMax);
        levelPrgView.setProgress(progressValue);
        devicesView.setText(decimalFormat.format(num) + " Devices");
        expView.setText(decimalFormat.format(exp) + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation));

        Integer[] changes = changeList.get(id);

        Integer rankBefore, expBefore, devBefore;

        if (changes == null) {

            rankBefore = userRank;
            expBefore = exp;
            devBefore = num;

        } else {

            rankBefore = changes[0];
            expBefore = changes[1];
            devBefore = changes[2];

        }

        if (rankBefore == null) rankBefore = userRank;
        if (expBefore == null) expBefore = exp;
        if (devBefore == null) devBefore = num;

        changeRankTxtView.setText("" + Math.abs(rankBefore - userRank));

        if ((rankBefore - userRank) > 0) {

            changeRankImgView.setImageResource(R.drawable.ic_change_up);

        } else if ((rankBefore - userRank) < 0) {

            changeRankImgView.setImageResource(R.drawable.ic_change_down);

        } else if ((rankBefore - userRank) == 0) {

            changeRankImgView.setImageResource(android.R.color.transparent);
            changeRankTxtView.setText("");
        }

        // change in exp
        changeEXPTxtView.setText("" + decimalFormat.format(Math.abs(expBefore - exp)));

        if ((expBefore - exp) > 0) {

            changeEXPImgView.setImageResource(R.drawable.ic_change_down_s);

        } else if ((expBefore - exp) < 0) {

            changeEXPImgView.setImageResource(R.drawable.ic_change_up_s);

        } else if ((expBefore - exp) == 0) {

            changeEXPImgView.setImageResource(android.R.color.transparent);
            changeEXPTxtView.setText("");
        }

        // change in dev
        changeDEVTxtView.setText("" + decimalFormat.format(Math.abs(devBefore - num)));

        if ((devBefore - num) > 0) {

            changeDEVImgView.setImageResource(R.drawable.ic_change_down_s);

        } else if ((devBefore - num) < 0) {

            changeDEVImgView.setImageResource(R.drawable.ic_change_up_s);

        } else if ((devBefore - num) == 0) {

            changeDEVImgView.setImageResource(android.R.color.transparent);
            changeDEVTxtView.setText("");
        }

    }

    static class ViewHolder {

        TextView rank;
        TextView name;
        TextView level;
        ProgressBar levelPrg;
        TextView devices;
        TextView exp;

        ImageView changeRankImg;
        TextView changeRankTxt;

        ImageView changeEXPImg;
        TextView changeEXPTxt;

        ImageView changeDEVImg;
        TextView changeDEVTxt;
    }

    private class RefreshThread extends AsyncTask<Integer, Void, String> {

        private final BlueHunter bhApp;
        private ListView listView;

        private LeaderboardAdapter ldAdapter;

        private ThreadManager threadManager;

        private boolean canRun = true;

        private int scrollIndex;
        private int scrollTop;

        private int startIndex;
        private int length;

        private boolean isUserInLD = false;

        private RefreshThread(BlueHunter app, ThreadManager threadManager) {

            super();
            this.bhApp = app;

            if (bhApp.mainActivity.mViewPager == null) {
                bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
            }

            ViewPager pager = bhApp.mainActivity.mViewPager;
            View pageView = pager.getChildAt(FragmentLayoutManager.PAGE_LEADERBOARD + 1);

            if (pageView == null) {
                listView = (ListView) pager.findViewById(R.id.globalLdListView);
            } else {
                listView = (ListView) pageView.findViewById(R.id.globalLdListView);
            }

            if (listView == null) {
                listView = (ListView) bhApp.mainActivity.findViewById(R.id.globalLdListView);
            }

            if (listView == null) {
                canRun = false;
                return;
            }

            scrollIndex = listView.getFirstVisiblePosition();
            View v = listView.getChildAt(0);
            scrollTop = (v == null) ? 0 : v.getTop();

            this.ldAdapter = (LeaderboardAdapter) listView.getAdapter();
            if (this.ldAdapter == null || this.ldAdapter.isEmpty()) {
                this.ldAdapter = new LeaderboardAdapter(bhApp.mainActivity, showedLbList);
                this.listView.setAdapter(ldAdapter);
            }

            this.threadManager = threadManager;

            if (!this.threadManager.setThread(this)) {
                canRun = false;
            }

        }

        public boolean canRun() {

            return canRun;
        }

        @Override
        protected String doInBackground(Integer... params) {

            startIndex = params[0];
            length = params[1];

            isUserInLD = (params[2] == 1);

            try {

                URL httpUri = new URL(AuthentificationSecure.SERVER_GET_LEADERBOARD + "?s=" + startIndex + "&l=" + length);

                HttpURLConnection conn = (HttpURLConnection) httpUri.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");

                conn.connect();

                int responseCode = conn.getResponseCode();

                String result;

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = br.readLine()) != null) {
                        stringBuilder.append(line).append(System.lineSeparator());
                    }

                    stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(System.lineSeparator()));

                    result = stringBuilder.toString();

                } else {

                    return "Error=" + responseCode + "\n" + conn.getResponseMessage();

                }

                return result;
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "Error=5\n" + e.getMessage();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "Error=1\n" + e.getMessage();
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {

            try {

                Pattern pattern = Pattern.compile("Error=(\\d+)");
                Matcher matcher = pattern.matcher(result);
                if (matcher.find()) {
                    int errorCode = Integer.parseInt(matcher.group(1));

                    LBAdapterData data = new LBAdapterData("Error " + errorCode, 0, 100, 0, 0, 0, 0);
                    showedLbList.add(data);

                    if (ldAdapter != null) ldAdapter.refreshList(showedLbList);

                    listView.setSelectionFromTop(scrollIndex, scrollTop);

                    threadManager.finished(this, true);

                    return;
                }

                DocumentBuilder docBuilder;
                Document document = null;

                try {
                    docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    document = docBuilder.parse(new InputSource(new StringReader(result)));
                } catch (Exception e) {

                }

                document.getDocumentElement().normalize();

                NodeList nodes = document.getElementsByTagName("user");

                boolean last = false;

                int uid = bhApp.loginManager.getUid();

                for (int i = 0; i < nodes.getLength(); i++) {

                    Node node = nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {

                        Element element = (Element) node;

                        int id = Integer.parseInt(element.getAttribute("id"));
                        int rank = Integer.parseInt(element.getAttribute("rank"));

                        String name = element.getElementsByTagName("name").item(0).getTextContent();
                        int exp = Integer.parseInt(element.getElementsByTagName("exp").item(0).getTextContent());
                        int num = Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent());

                        int level = LevelSystem.getLevel(exp);
                        int progressMax = LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level);
                        int progressValue = exp - LevelSystem.getLevelStartExp(level);

                        last = element.getAttribute("last").equals("1");

                        LBAdapterData data = new LBAdapterData(name, level, progressMax, progressValue, num, exp, id);

                        if (uid == id) {
                            isUserInLD = true;
                            userRank = rank;
                        }

                        completeLbList.add(data);
                        completeLbList.set(rank - 1, data);
                        showedLbList = new ArrayList<>(completeLbList);

                    }

                }

                threadManager.finished(this, last);

                if (last) {

                    int currentExp = LevelSystem.getCachedUserExp(bhApp);

                    if (uid != -1 && isUserInLD) {

                        int exp = -1;

                        for (int i = 0; i < completeLbList.size(); i++) {

                            LBAdapterData data = completeLbList.get(i);

                            if (i == 0 && currentExp >= data.getExp()) {
                                break;
                            }

                            if (currentExp >= data.getExp() && (i - 1) >= 0) {

                                if (completeLbList.get(i - 1).getId() == uid && (i - 2) >= 0) {
                                    if (completeLbList.get(i - 2).getId() != uid) {
                                        exp = completeLbList.get(i - 2).getExp();
                                        break;
                                    }
                                } else {
                                    exp = completeLbList.get(i - 1).getExp();
                                    break;
                                }
                            }

                        }

                        if (currentExp < completeLbList.get(completeLbList.size() - 1).getExp()) {

                            exp = completeLbList.get(completeLbList.size() - 1).getExp();
                        }

                        DeviceDiscoveryLayout.expToUpdate = exp;
                        DeviceDiscoveryLayout.updateNextRankIndicator(bhApp.mainActivity, exp);

                    } else {

                        DeviceDiscoveryLayout.updateNextRankIndicator(bhApp.mainActivity, -1);

                    }

                    if (isUserInLD) {
                        View container = bhApp.mainActivity.findViewById(R.id.globalLdUserRL);
                        container.setVisibility(View.VISIBLE);

                        refreshUserRow(bhApp.mainActivity);
                        bhApp.mainActivity.updateNotification();

                    }

                    showedLbList = new ArrayList<>(completeLbList);
                    ldAdapter.refreshList(showedLbList);

                    listView.setSelectionFromTop(scrollIndex, scrollTop);
                } else {

                    ldAdapter.notifyDataSetChanged();
                    new RefreshThread(bhApp, threadManager).execute(startIndex + length, length, (isUserInLD) ? 1 : 0);

                }

            } catch (NullPointerException e) {
                if (bhApp != null && threadManager != null) {
                    new RefreshThread(bhApp, threadManager).execute(startIndex, length, (isUserInLD) ? 1 : 0);
                }

            }

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(Void... values) {

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            isUserInLD = false;

            View container = bhApp.mainActivity.findViewById(R.id.globalLdUserRL);
            container.setVisibility(View.GONE);
        }
    }

    private class ThreadManager {

        RefreshThread refreshThread;
        boolean running;

        public boolean setThread(RefreshThread refreshThread) {

            if (running) {
                return false;
            }

            this.refreshThread = refreshThread;
            setRunning(true, false);
            return true;
        }

        public void finished(RefreshThread refreshThread, boolean last) {

            if (this.refreshThread.equals(refreshThread)) {
                setRunning(false, last);
                return;
            }
        }

        private void setRunning(boolean running, boolean last) {

            this.running = running;

            if (!running && last) {
                if (!refreshThread.bhApp.netMananger.areThreadsRunning()) {
                    MenuItem progressBar = refreshThread.bhApp.actionBarHandler.getMenuItem(R.id.menu_progress);
                    if (progressBar != null) {
                        progressBar.setVisible(false);
                    }
                }
            } else {
                MenuItem progressBar = refreshThread.bhApp.actionBarHandler.getMenuItem(R.id.menu_progress);
                if (progressBar != null) {
                    ;
                    if (!progressBar.isVisible())
                        progressBar.setVisible(true);
                }

            }

        }
    }

    public class LBAdapterData {

        private String name;
        private int level;
        private int progressMax;
        private int progressValue;
        private int devNum;
        private int exp;
        private int id;

        public LBAdapterData(String name, int level, int progressMax, int progressValue, int devNum, int exp, int id) {

            this.name = name;
            this.level = level;
            this.progressMax = progressMax;
            this.progressValue = progressValue;
            this.devNum = devNum;
            this.exp = exp;
            this.id = id;
        }

        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }

        public int getLevel() {

            return level;
        }

        public void setLevel(int level) {

            this.level = level;
        }

        public int getProgressMax() {

            return progressMax;
        }

        public void setProgressMax(int progressMax) {

            this.progressMax = progressMax;
        }

        public int getProgressValue() {

            return progressValue;
        }

        public void setProgressValue(int progressValue) {

            this.progressValue = progressValue;
        }

        public int getDevNum() {

            return devNum;
        }

        public void setDevNum(int devNum) {

            this.devNum = devNum;
        }

        public int getExp() {

            return exp;
        }

        public void setExp(int exp) {

            this.exp = exp;
        }

        public int getId() {

            return id;
        }

        public void setId(int id) {

            this.id = id;
        }

        @Override
        public boolean equals(Object o) {

            if (o instanceof LBAdapterData)
                return id == ((LBAdapterData) (o)).id;
            else
                return false;
        }

    }

    public class LeaderboardAdapter extends ArrayAdapter<LBAdapterData> {

        private final ArrayList<LBAdapterData> dataList;
        private final ArrayList<LBAdapterData> originalDataList;

        public LeaderboardAdapter(Context context, ArrayList<LBAdapterData> newLbData) {

            super(context, R.layout.act_page_leaderboard_row, showedLbList);

            dataList = newLbData;
            originalDataList = newLbData;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.act_page_leaderboard_row, parent, false);

                LeaderboardLayout.ViewHolder viewHolder = new ViewHolder();

                viewHolder.rank = (TextView) rowView.findViewById(R.id.rankTxtView);
                viewHolder.name = (TextView) rowView.findViewById(R.id.nameTxtView);
                viewHolder.level = (TextView) rowView.findViewById(R.id.levelTxtView);
                viewHolder.levelPrg = (ProgressBar) rowView.findViewById(R.id.levelPrgBar);
                viewHolder.devices = (TextView) rowView.findViewById(R.id.devTxtView);
                viewHolder.exp = (TextView) rowView.findViewById(R.id.expTxtView);

                viewHolder.changeRankImg = (ImageView) rowView.findViewById(R.id.changeRankImgView);
                viewHolder.changeRankTxt = (TextView) rowView.findViewById(R.id.changeRankTxtView);

                viewHolder.changeEXPImg = (ImageView) rowView.findViewById(R.id.changeEXPImgView);
                viewHolder.changeEXPTxt = (TextView) rowView.findViewById(R.id.changeEXPTxtView);

                viewHolder.changeDEVImg = (ImageView) rowView.findViewById(R.id.changeDEVImgView);
                viewHolder.changeDEVTxt = (TextView) rowView.findViewById(R.id.changeDEVTxtView);

                rowView.setTag(viewHolder);
            }

            LeaderboardLayout.ViewHolder holder = (LeaderboardLayout.ViewHolder) rowView.getTag();

            if (holder != null && dataList != null && dataList.size() > position) {

                LBAdapterData user = dataList.get(position);

                String nameString = user.getName();

                DecimalFormat decimalFormat = new DecimalFormat(",###");

                int rankNow = (completeLbList.indexOf(user) + 1);

                if (rankNow == 0)
                    holder.rank.setText("");
                else
                    holder.rank.setText("" + rankNow + ".");

                holder.name.setText(nameString);
                holder.name.setTag(user.getId());
                holder.level.setText("" + user.getLevel());
                holder.levelPrg.setMax(user.getProgressMax());
                holder.levelPrg.setProgress(user.getProgressValue());
                holder.devices.setText(decimalFormat.format(user.getDevNum()) + " Devices");
                holder.exp.setText(
                        decimalFormat.format(user.getExp()) + " " + getContext().getString(R.string.str_foundDevices_exp_abbreviation));

                int expNow = user.getExp();
                int devNow = user.getDevNum();

                Integer[] changes = changeList.get(user.getId());

                Integer rankBefore, expBefore, devBefore;

                if (changes == null) {

                    rankBefore = rankNow;
                    expBefore = expNow;
                    devBefore = devNow;

                } else {

                    rankBefore = changes[0];
                    expBefore = changes[1];
                    devBefore = changes[2];

                }

                if (rankBefore == null) rankBefore = rankNow;
                if (expBefore == null) expBefore = expNow;
                if (devBefore == null) devBefore = devNow;

                // change in rank
                holder.changeRankTxt.setText("" + Math.abs(rankBefore - rankNow));

                if ((rankBefore - rankNow) > 0) {

                    holder.changeRankImg.setImageResource(R.drawable.ic_change_up);

                } else if ((rankBefore - rankNow) < 0) {

                    holder.changeRankImg.setImageResource(R.drawable.ic_change_down);

                } else if ((rankBefore - rankNow) == 0) {

                    holder.changeRankImg.setImageResource(android.R.color.transparent);
                    holder.changeRankTxt.setText("");
                }

                // change in exp
                holder.changeEXPTxt.setText("" + decimalFormat.format(Math.abs(expBefore - expNow)));

                if ((expBefore - expNow) > 0) {

                    holder.changeEXPImg.setImageResource(R.drawable.ic_change_down_s);

                } else if ((expBefore - expNow) < 0) {

                    holder.changeEXPImg.setImageResource(R.drawable.ic_change_up_s);

                } else if ((expBefore - expNow) == 0) {

                    holder.changeEXPImg.setImageResource(android.R.color.transparent);
                    holder.changeEXPTxt.setText("");
                }

                // change in dev
                holder.changeDEVTxt.setText("" + decimalFormat.format(Math.abs(devBefore - devNow)));

                if ((devBefore - devNow) > 0) {

                    holder.changeDEVImg.setImageResource(R.drawable.ic_change_down_s);

                } else if ((devBefore - devNow) < 0) {

                    holder.changeDEVImg.setImageResource(R.drawable.ic_change_up_s);

                } else if ((devBefore - devNow) == 0) {

                    holder.changeDEVImg.setImageResource(android.R.color.transparent);
                    holder.changeDEVTxt.setText("");
                }

            }
            return rowView;
        }

        public void refreshList(ArrayList<LBAdapterData> data) {

            clear();
            addAll(data);
            notifyDataSetChanged();

        }

    }

}