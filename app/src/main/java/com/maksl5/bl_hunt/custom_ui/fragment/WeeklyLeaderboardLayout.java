package com.maksl5.bl_hunt.custom_ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog.Builder;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.maksl5.bl_hunt.BlueHunter;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author Maksl5
 */
public class WeeklyLeaderboardLayout {

    public static final int ARRAY_INDEX_NAME = 0;
    public static final int ARRAY_INDEX_LEVEL = 1;
    public static final int ARRAY_INDEX_PROGRESS_MAX = 2;
    public static final int ARRAY_INDEX_PROGRESS_VALUE = 3;
    public static final int ARRAY_INDEX_DEV_NUMBER = 4;
    public static final int ARRAY_INDEX_EXP = 5;
    public static final int ARRAY_INDEX_ID = 6;
    public final static ArrayList<LBAdapterData> completeLbList = new ArrayList<>();
    public static SparseArray<Integer[]> changeList = new SparseArray<>();
    public static long timeOffset = 0;
    public static TextView timerTextView = null;
    public static int lastWeeklyRank = 0;
    public static int lastWeeklyCount = 0;
    private static int userRank = -1;
    private volatile static ArrayList<LBAdapterData> showedLbList = new ArrayList<>();
    private static ThreadManager threadManager = null;

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
                listView = (ListView) pager.findViewById(R.id.weeklyLdListView);
            } else {
                listView = (ListView) pageView.findViewById(R.id.weeklyLdListView);
            }

            if (listView == null) {
                listView = (ListView) bhApp.mainActivity.findViewById(R.id.weeklyLdListView);
            }

            if (listView == null) {
                return;
            }

            ldAdapter = (LeaderboardAdapter) listView.getAdapter();
            if (ldAdapter == null || ldAdapter.isEmpty()) {
                ldAdapter = new WeeklyLeaderboardLayout().new LeaderboardAdapter(bhApp.mainActivity,
                        showedLbList);
                listView.setAdapter(ldAdapter);
            }

            ldAdapter.notifyDataSetChanged();

            refreshUserRow(bhApp.mainActivity);

            return;

        }

        if (threadManager == null) {
            threadManager = new WeeklyLeaderboardLayout().new ThreadManager();
        }

        RefreshThread refreshThread = new WeeklyLeaderboardLayout().new RefreshThread(bhApp, threadManager);
        if (refreshThread.canRun()) {
            showedLbList.clear();
            completeLbList.clear();
            bhApp.actionBarHandler.getMenuItem(R.id.menu_search).collapseActionView();
            refreshThread.execute(1, 5, 0);
        }

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
            lv = (ListView) pager.findViewById(R.id.weeklyLdListView);
        } else {
            lv = (ListView) pageView.findViewById(R.id.weeklyLdListView);
        }

        if (lv == null) {
            lv = (ListView) bhApp.mainActivity.findViewById(R.id.weeklyLdListView);
        }

        if (lv == null) {
            return;
        }

        LeaderboardAdapter lbAdapter = (LeaderboardAdapter) lv.getAdapter();

        if (lbAdapter == null || lbAdapter.isEmpty()) {
            lbAdapter = new WeeklyLeaderboardLayout().new LeaderboardAdapter(bhApp.mainActivity,
                    showedLbList);
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

            }

            showedLbList = newValues;
            lbAdapter.refreshList(showedLbList);

        }

    }

    private static void scrollToPosition(MainActivity mainActivity, int index) {

        if (completeLbList != null && completeLbList.size() != 0) {

            if (mainActivity.mViewPager == null) {
                mainActivity.mViewPager = (ViewPager) mainActivity.findViewById(R.id.pager);
            }

            mainActivity.mViewPager.setCurrentItem(FragmentLayoutManager.PAGE_LEADERBOARD, true);

            ListView listView = (ListView) mainActivity.mViewPager.findViewById(R.id.weeklyLdListView);

            if (listView != null) {

                LeaderboardLayout.ensureScrolling(listView, index);

            }

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
        int num = userData.getDevNum();

        View container = mainActivity.findViewById(R.id.weeklyLdUserRL);

        container.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                scrollToPosition(mainActivity, userRank - 1);
            }
        });

        TextView rankView = (TextView) container.findViewById(R.id.rankTxtView);
        TextView nameView = (TextView) container.findViewById(R.id.nameTxtView);
        TextView devicesView = (TextView) container.findViewById(R.id.devTxtView);

        ImageView changeRankImgView = (ImageView) container.findViewById(R.id.changeRankImgView);
        TextView changeRankTxtView = (TextView) container.findViewById(R.id.changeRankTxtView);

        ImageView changeDEVImgView = (ImageView) container.findViewById(R.id.changeDEVImgView);
        TextView changeDEVTxtView = (TextView) container.findViewById(R.id.changeDEVTxtView);

        TextView estimatedBoostTxt = (TextView) container.findViewById(R.id.estimatedBoostTxt);

        DecimalFormat decimalFormat = new DecimalFormat(",###");

        rankView.setText("" + userRank + ".");
        nameView.setText(name);
        nameView.setTag(id);

        devicesView.setText(decimalFormat.format(num) + " Devices");

        NumberFormat percentage = NumberFormat.getPercentInstance();
        String boostString = mainActivity.getString(R.string.str_leaderboard_estimatedExp, "+" + percentage.format(weeklyBoostAllocation(completeLbList.size(), userRank)));
        estimatedBoostTxt.setText(boostString);
        estimatedBoostTxt.setVisibility(View.VISIBLE);

        Integer[] changes = changeList.get(id);

        Integer rankBefore, devBefore;

        if (changes == null) {

            rankBefore = userRank;
            devBefore = num;

        } else {

            rankBefore = changes[0];
            devBefore = changes[1];

        }

        if (rankBefore == null) rankBefore = userRank;
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

    public static float weeklyBoostAllocation() {
        return weeklyBoostAllocation(lastWeeklyCount, lastWeeklyRank);
    }

    public static float weeklyBoostAllocation(int count, int forRank) {

        switch (forRank) {
            case 0:
                return 0f;
            case 1:
                return 1f;
            case 2:
                return 0.75f;
            case 3:
                return 0.5f;
        }

        float boost = 0f;

        if (count > 3) {

            int weeklyPlaceSub3 = forRank - 3;
            int weeklyCountSub3 = count - 3;

            int tempBonus = (int) (-(25 / (float) weeklyCountSub3) * (weeklyPlaceSub3 - 1) + 25);

            boost = tempBonus / 100f;


        }

        return boost;
    }

    static class ViewHolder {

        TextView rank;
        TextView name;

        TextView devices;

        ImageView changeRankImg;
        TextView changeRankTxt;

        ImageView changeDEVImg;
        TextView changeDEVTxt;

        RelativeLayout prgTableRow;
        TableRow expTableRow;
        LinearLayout devLL;

        TextView estimatedBoostTxt;

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
                listView = (ListView) pager.findViewById(R.id.weeklyLdListView);
            } else {
                listView = (ListView) pageView.findViewById(R.id.weeklyLdListView);
            }

            if (listView == null) {
                listView = (ListView) bhApp.mainActivity.findViewById(R.id.weeklyLdListView);
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

                int i = 1;

                URL httpUri = new URL(AuthentificationSecure.SERVER_GET_WEEKLY_LEADERBOARD + "?s=" + startIndex + "&l=" + length + "&uid=" + bhApp.loginManager.getUid());

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

                lastWeeklyRank = 0;

                Pattern pattern = Pattern.compile("Error=(\\d+)");
                Matcher matcher = pattern.matcher(result);
                if (matcher.find()) {
                    int errorCode = Integer.parseInt(matcher.group(1));

                    LBAdapterData data = new LBAdapterData("Error " + errorCode, 0, 0);
                    showedLbList.add(data);

                    if (ldAdapter != null) ldAdapter.refreshList(showedLbList);

                    listView.setSelectionFromTop(scrollIndex, scrollTop);

                    threadManager.finished(this, true);

                    return;
                }

                DocumentBuilder docBuilder;
                Document document;

                try {
                    docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    document = docBuilder.parse(new InputSource(new StringReader(result)));
                } catch (Exception e) {
                    return;
                }

                document.getDocumentElement().normalize();

                Element rootElement = document.getDocumentElement();

                long nextCycleTStamp = Long.parseLong(rootElement.getAttribute("until"));
                timeOffset = System.currentTimeMillis() - Long.parseLong(rootElement.getAttribute("now"));

                String weeklyPlaceStr = rootElement.getAttribute("lastRank");
                String weeklyCountStr = rootElement.getAttribute("lastCount");

                try {
                    lastWeeklyRank = Integer.parseInt(weeklyPlaceStr);
                    lastWeeklyCount = Integer.parseInt(weeklyCountStr);
                } catch (NumberFormatException exception) {
                    lastWeeklyRank = 0;
                    lastWeeklyCount = 0;
                }

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
                        int num = Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent());

                        last = element.getAttribute("last").equals("1");

                        LBAdapterData data = new LBAdapterData(name, num, id);

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

                if (last || (startIndex == 1 && nodes.getLength() == 0)) {

                    if (isUserInLD) {
                        View container = bhApp.mainActivity.findViewById(R.id.weeklyLdUserRL);
                        container.setVisibility(View.VISIBLE);

                        refreshUserRow(bhApp.mainActivity);
                    }

                    long cachedNextCycleTS = PreferenceManager.getPref(bhApp, "cachedNextCycle", 0L);

                    // store new cycleTS as cache
                    PreferenceManager.setPref(bhApp, "cachedNextCycle", nextCycleTStamp);

                    if (lastWeeklyRank != 0) {


                        if (cachedNextCycleTS != nextCycleTStamp) {

                            Builder builder = new Builder(bhApp.mainActivity);
                            builder.setTitle("Congratulations!");

                            float boost = weeklyBoostAllocation();


                            NumberFormat percentage = NumberFormat.getPercentInstance();
                            String boostString = percentage.format(boost);

                            if (lastWeeklyRank > 3) {

                                Snackbar.make(bhApp.currentActivity.getWindow().getDecorView(), "You got place " + lastWeeklyRank + " in the weekly leaderboard. Reward for 7 days: " + boostString + " boost!", Snackbar.LENGTH_LONG).show();

                            } else {
                                builder.setMessage(
                                        "You managed to get place " + lastWeeklyRank + " in the last weekly leaderboard!!! You will gain an extra "
                                                + boostString + " boost as reward until the current cycle ends. Congratulations!!!");

                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                    }
                                });

                                builder.show();

                            }

                        }

                    }

                    AchievementsLayout.updateBoostIndicator(bhApp);

                    showedLbList = new ArrayList<>(completeLbList);
                    ldAdapter.refreshList(showedLbList);

                    listView.setSelectionFromTop(scrollIndex, scrollTop);

                    final SwipeRefreshLayout weeklySwipeRefresh = (SwipeRefreshLayout) bhApp.mainActivity.findViewById(R.id.weeklySwipeRefresh);
                    if (weeklySwipeRefresh != null)
                        weeklySwipeRefresh.setRefreshing(false);

                } else {

                    ldAdapter.notifyDataSetChanged();
                    new RefreshThread(bhApp, threadManager).execute(startIndex + length, length, (isUserInLD) ? 1 : 0);

                }

            } catch (NullPointerException e) {
                if (bhApp != null && threadManager != null) {
                    e.printStackTrace();
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

            View container = bhApp.mainActivity.findViewById(R.id.weeklyLdUserRL);
            if (container != null)
                container.setVisibility(View.GONE);


            ldAdapter.notifyDataSetChanged();

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
                    if (!progressBar.isVisible())
                        progressBar.setVisible(true);
                }

            }

        }
    }

    public class LBAdapterData {

        private String name;
        private int devNum;
        private int id;

        public LBAdapterData(String name, int devNum, int id) {

            this.name = name;
            this.devNum = devNum;
            this.id = id;
        }

        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }

        public int getDevNum() {

            return devNum;
        }

        public void setDevNum(int devNum) {

            this.devNum = devNum;
        }

        public int getId() {

            return id;
        }

        public void setId(int id) {

            this.id = id;
        }

        @Override
        public boolean equals(Object o) {

            return o instanceof LBAdapterData && id == ((LBAdapterData) (o)).id;
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

                WeeklyLeaderboardLayout.ViewHolder viewHolder = new ViewHolder();

                viewHolder.rank = (TextView) rowView.findViewById(R.id.rankTxtView);
                viewHolder.name = (TextView) rowView.findViewById(R.id.nameTxtView);
                viewHolder.devices = (TextView) rowView.findViewById(R.id.devTxtView);

                viewHolder.changeRankImg = (ImageView) rowView.findViewById(R.id.changeRankImgView);
                viewHolder.changeRankTxt = (TextView) rowView.findViewById(R.id.changeRankTxtView);

                viewHolder.changeDEVImg = (ImageView) rowView.findViewById(R.id.changeDEVImgView);
                viewHolder.changeDEVTxt = (TextView) rowView.findViewById(R.id.changeDEVTxtView);

                viewHolder.prgTableRow = (RelativeLayout) rowView.findViewById(R.id.LdrPrgParent);
                viewHolder.expTableRow = (TableRow) rowView.findViewById(R.id.LDRTableRow01);
                viewHolder.devLL = (LinearLayout) rowView.findViewById(R.id.LDRtableRow4).getParent();

                viewHolder.estimatedBoostTxt = (TextView) rowView.findViewById(R.id.estimatedBoostTxt);

                rowView.setTag(viewHolder);
            }

            WeeklyLeaderboardLayout.ViewHolder holder = (WeeklyLeaderboardLayout.ViewHolder) rowView.getTag();

            if (holder != null && dataList != null && dataList.size() > position) {

                LBAdapterData user = dataList.get(position);

                String nameString = user.getName();

                DecimalFormat decimalFormat = new DecimalFormat(",###");

                int rankNow = (completeLbList.indexOf(user) + 1);

                if (Build.VERSION.SDK_INT >= 16) {
                    if (rankNow == 1) {
                        rowView.setBackground(new ColorDrawable(Color.argb(0xcc, 0xd4, 0xaf, 0x37)));
                    } else if (rankNow == 2) {
                        rowView.setBackground(new ColorDrawable(Color.argb(0xcc, 0xdd, 0xdd, 0xdd)));
                    } else if (rankNow == 3) {
                        rowView.setBackground(new ColorDrawable(Color.argb(0xcc, 0xcd, 0x7f, 0x32)));
                    } else {
                        rowView.setBackground(null);
                    }
                }

                holder.rank.setText("" + rankNow + ".");
                holder.name.setText(nameString);
                holder.name.setTag(user.getId());
                holder.devices.setText(decimalFormat.format(user.getDevNum()) + " Devices");

                holder.prgTableRow.setVisibility(View.GONE);
                holder.expTableRow.setVisibility(View.GONE);
                holder.devLL.setGravity(Gravity.CENTER);

                NumberFormat percentage = NumberFormat.getPercentInstance();
                String boostString = getContext().getString(R.string.str_leaderboard_estimatedExp, "+" + percentage.format(weeklyBoostAllocation(dataList.size(), rankNow)));
                holder.estimatedBoostTxt.setText(boostString);
                holder.estimatedBoostTxt.setVisibility(View.VISIBLE);

                int devNow = user.getDevNum();

                Integer[] changes = changeList.get(user.getId());

                Integer rankBefore, devBefore;

                if (changes == null) {

                    rankBefore = rankNow;
                    devBefore = devNow;

                } else {

                    rankBefore = changes[0];
                    devBefore = changes[1];

                }

                if (rankBefore == null) rankBefore = rankNow;
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