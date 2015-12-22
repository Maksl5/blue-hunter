package com.maksl5.bl_hunt.custom_ui.fragment;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.maksl5.bl_hunt.DiscoveryManager.DiscoveryState;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.ManufacturerList;
import com.maksl5.bl_hunt.util.FoundDevice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksl5
 */
public class DeviceDiscoveryLayout {

    public static DiscoveryAdapter dAdapter;
    public static ListView lv;

    public static int expToUpdate = 0;

    public static void updateIndicatorViews(MainActivity mainActivity) {

        // Debug.startMethodTracing("updateIndicatorViews");

        List<FoundDevice> allDevices = DatabaseManager.getCachedList();

        if (allDevices == null) {

            new DatabaseManager(mainActivity.getBlueHunter()).loadAllDevices();
            return;

        }

        TextView expTextView = (TextView) mainActivity.findViewById(R.id.expIndicator);
        TextView lvlTextView = (TextView) mainActivity.findViewById(R.id.lvlIndicator);
        TextView devicesTextView = (TextView) mainActivity.findViewById(R.id.txt_devices);
        TextView devExpPerDayTxt = (TextView) mainActivity.findViewById(R.id.txt_devExpPerDay);
        TextView devExpTodayTxt = (TextView) mainActivity.findViewById(R.id.txt_devExpToday);

        ProgressBar progressBar = (ProgressBar) mainActivity.findViewById(R.id.progressBar1);

        long expTimeA = System.currentTimeMillis();

        // Debug.startMethodTracing("getUserExp");

        int exp = LevelSystem.getUserExp(mainActivity.getBlueHunter());

        // Debug.stopMethodTracing();

        long expTimeB = System.currentTimeMillis();

        Log.d("getUserExp", "" + (expTimeB - expTimeA) + "ms");

        int level = LevelSystem.getLevel(exp);

        DecimalFormat df = new DecimalFormat(",###");

        String format1 = df.format(exp);
        String format2 = mainActivity.getString(R.string.str_foundDevices_exp_abbreviation);
        String format3 = df.format(LevelSystem.getLevelEndExp(level));
        String format4 = mainActivity.getString(R.string.str_foundDevices_exp_abbreviation);

        expTextView.setText(String.format("%s %s / %s %s", format1, format2, format3, format4));
        lvlTextView.setText(String.format("%d", level));

        progressBar.setMax(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level));
        progressBar.setProgress(exp - LevelSystem.getLevelStartExp(level));

        int deviceNum = allDevices.size();

        int lastIndex = allDevices.size() - 1;

        long firstTime = 0;

        do {

            if (lastIndex < 0) break;

            firstTime = allDevices.get(lastIndex).getTime();

            lastIndex--;
        }
        while (firstTime == 0);

        long now = System.currentTimeMillis();
        float devPerDay = (deviceNum / (float) (now - firstTime)) * 86400000;
        float expPerDay = (exp / (float) (now - firstTime)) * 86400000;

        devicesTextView.setText(df.format(deviceNum));
        devExpPerDayTxt.setText(String.format("%.2f / %.2f", devPerDay, expPerDay));

        int expTodayNum = 0;
        int devicesTodayNum = 0;

        for (FoundDevice foundDevice : allDevices) {

            if (foundDevice.getTime() > (now - 86400000)) {
                devicesTodayNum++;
                int manufacturer = foundDevice.getManufacturer();
                float bonus = foundDevice.getBoost();

                expTodayNum += ManufacturerList.getExp(manufacturer) * (1 + bonus);
            }

        }

        devExpTodayTxt.setText(String.format("%d / %d", devicesTodayNum, expTodayNum));

        onlyCurListUpdate(mainActivity);

        if (exp > expToUpdate && expToUpdate != 0) {
            expToUpdate = 0;
            LeaderboardLayout.refreshLeaderboard(mainActivity.getBlueHunter());
        }

        updateNextRankIndicator(mainActivity, (expToUpdate == 0) ? -1 : expToUpdate);

        // Debug.stopMethodTracing();

    }

    public static void onlyCurListUpdate(MainActivity mainActivity) {

        if (mainActivity == null || mainActivity.destroyed) return;

        ArrayList<FoundDevice> curList = new ArrayList<>(mainActivity.getBlueHunter().disMan.getFDInCurDiscoverySession());
        int curNewDevices = mainActivity.getBlueHunter().disMan.newDevicesInCurDiscoverySession;

        TextView devInCurDisTxt = (TextView) mainActivity.findViewById(R.id.txt_discovery_devInSession_value);

        if (devInCurDisTxt != null) {

            devInCurDisTxt.setText(String.format("%d / %d", curNewDevices, curList.size() - curNewDevices));

        }

        if (dAdapter == null) {
            dAdapter = new DeviceDiscoveryLayout().new DiscoveryAdapter(mainActivity, curList);

            if (lv == null) lv = (ListView) mainActivity.findViewById(R.id.discoveryListView);

            lv.setAdapter(dAdapter);
        }

        if (lv != null && mainActivity.getBlueHunter() != null && mainActivity.getBlueHunter().disMan != null && mainActivity.getBlueHunter().disMan.getCurDiscoveryState() != -2) {

            if (mainActivity.getBlueHunter().disMan.getCurDiscoveryState() == DiscoveryState.DISCOVERY_STATE_RUNNING
                    && lv.getVisibility() != View.VISIBLE) {
                startShowLV(mainActivity);
            } else if (mainActivity.getBlueHunter().disMan.getCurDiscoveryState() != DiscoveryState.DISCOVERY_STATE_RUNNING
                    && mainActivity.getBlueHunter().disMan.getCurDiscoveryState() != DiscoveryState.DISCOVERY_STATE_FINISHED
                    && lv.getVisibility() == View.VISIBLE) {
                stopShowLV(mainActivity);
            }
        }

        if (dAdapter.values.equals(curList)) {
            dAdapter.notifyDataSetChanged();
        } else {
            dAdapter.clear();
            dAdapter.addAll(curList);
        }

    }

    public static void updateDuringDBLoading(MainActivity mainActivity) {

        TextView expTextView = (TextView) mainActivity.findViewById(R.id.expIndicator);
        TextView lvlTextView = (TextView) mainActivity.findViewById(R.id.lvlIndicator);
        TextView devicesTextView = (TextView) mainActivity.findViewById(R.id.txt_devices);

        ProgressBar progressBar = (ProgressBar) mainActivity.findViewById(R.id.progressBar1);

        List<FoundDevice> foundDevices = DatabaseManager.getProgressList();

        if (foundDevices != null) {

            int exp = LevelSystem.getUserExp(foundDevices);
            int level = LevelSystem.getLevel(exp);

            DecimalFormat df = new DecimalFormat(",###");

            String format1 = df.format(exp);
            String format2 = mainActivity.getString(R.string.str_foundDevices_exp_abbreviation);
            String format3 = df.format(LevelSystem.getLevelEndExp(level));
            String format4 = mainActivity.getString(R.string.str_foundDevices_exp_abbreviation);

            expTextView.setText(String.format("%s %s / %s %s", format1, format2, format3, format4));
            lvlTextView.setText(String.format("%d", level));

            progressBar.setMax(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level));
            progressBar.setProgress(exp - LevelSystem.getLevelStartExp(level));

            int deviceNum = foundDevices.size();

            devicesTextView.setText(df.format(deviceNum));

        } else {

            expTextView.setText("Loading...");
            lvlTextView.setText("Loading...");
            devicesTextView.setText("Loading...");

        }

        if (true) {

            TextView devInCurDisTxt = (TextView) mainActivity.findViewById(R.id.txt_discovery_devInSession_value);
            TextView devExpPerDayTxt = (TextView) mainActivity.findViewById(R.id.txt_devExpPerDay);
            TextView devExpTodayTxt = (TextView) mainActivity.findViewById(R.id.txt_devExpToday);

            String loadingString = mainActivity.getString(R.string.str_discovery_loading);

            devInCurDisTxt.setText(loadingString);
            devExpPerDayTxt.setText(loadingString);
            devExpTodayTxt.setText(loadingString);
        }

    }

    public static void updateNextRankIndicator(final MainActivity mainActivity, int exp) {

        TextView nextRankIndicator = (TextView) mainActivity.findViewById(R.id.nextRankIndicator);

        int oldVisible = nextRankIndicator.getVisibility();

        if (exp == -1) {

            nextRankIndicator.setVisibility(TextView.GONE);

        } else {

            ProgressBar progressBar = (ProgressBar) mainActivity.findViewById(R.id.progressBar1);
            RelativeLayout parent = (RelativeLayout) progressBar.getParent();

            int userExp = LevelSystem.getUserExp(mainActivity.getBlueHunter());
            int level = LevelSystem.getLevel(userExp);

            int levelStart = LevelSystem.getLevelStartExp(level);
            int levelEnd = LevelSystem.getLevelEndExp(level);

            progressBar.setMax(levelEnd - levelStart);
            progressBar.setProgress(userExp - levelStart);

            // define window range
            int left = parent.getLeft();

            int width = progressBar.getWidth();

            float percOfProgressbar = (float) (exp - levelStart) / (float) (levelEnd - levelStart);

            if (percOfProgressbar < 0) {
                percOfProgressbar = 0f;
            }
            if (percOfProgressbar > 1) {
                percOfProgressbar = 1f;
            }

            int xPlacedOverProgressbar = (int) (left + (width / (float) 2) * (percOfProgressbar - 0.5f));

            int indicatorCenter = nextRankIndicator.getWidth() / 2;

            // new method

            int calculatedCenterOffset = (int) ((width / (float) 2) * ((percOfProgressbar - 0.5) * 2));

            TableRow tableRow = (TableRow) nextRankIndicator.getParent();

            nextRankIndicator.setVisibility(View.VISIBLE);

            DecimalFormat df = new DecimalFormat(",###");

            int needExpForNextRank = exp - userExp;

            String format1 = df.format(needExpForNextRank);
            String format2 = mainActivity.getString(R.string.str_foundDevices_exp_abbreviation);
            String format3 = mainActivity.getString(R.string.str_discovery_left_remaining);

            nextRankIndicator
                    .setText(String.format("%s%n%s %s %s", mainActivity.getString(R.string.str_discovery_nextRank), format1, format2, format3));

            TableLayout.LayoutParams params = (android.widget.TableLayout.LayoutParams) tableRow.getLayoutParams();
            params.setMargins(calculatedCenterOffset, params.topMargin, params.rightMargin, params.bottomMargin);
            tableRow.setLayoutParams(params);
            tableRow.requestLayout();

            nextRankIndicator.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    LeaderboardLayout.scrollToPosition(mainActivity, LeaderboardLayout.userRank - 2);

                }
            });

        }

        int newVisible = nextRankIndicator.getVisibility();

        if (oldVisible != newVisible) updateIndicatorViews(mainActivity);

    }

    public static void startShowLV(final MainActivity main) {

        // AlphaAnimation animation = new AlphaAnimation(1f, 0f);
        // animation.setDuration(1000);
        //
        // TableLayout discoveryInfo = (TableLayout) main
        // .findViewById(R.id.discoveryInfoTable);
        // discoveryInfo.startAnimation(animation);
        //
        // lv.setVisibility(View.VISIBLE);
        //
        // animation = new AlphaAnimation(0f, 1f);
        //
        // animation.setDuration(1000);
        // discoveryInfo.startAnimation(animation);
        // lv.startAnimation(animation);

        if (lv == null) lv = (ListView) main.findViewById(R.id.discoveryListView);

        if (lv == null) return;

        int orientation = main.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            TableLayout discoveryInfo = (TableLayout) main.findViewById(R.id.discoveryInfoTable);
            TranslateAnimation animation2 = new TranslateAnimation(0, 0, discoveryInfo.getTop(), 0);
            animation2.setDuration(1000);

            discoveryInfo.startAnimation(animation2);

            RelativeLayout relativeLayout = (RelativeLayout) lv.getParent();

            lv.setVisibility(View.VISIBLE);
            animation2 = new TranslateAnimation(0, 0, relativeLayout.getHeight() - discoveryInfo.getHeight(), 0);
            animation2.setDuration(1000);

            lv.startAnimation(animation2);

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            TableLayout discoveryInfo = (TableLayout) main.findViewById(R.id.discoveryInfoTable);
            RelativeLayout relativeLayout = (RelativeLayout) lv.getParent();

            TranslateAnimation animation2 = new TranslateAnimation(discoveryInfo.getLeft(), 0, 0, 0);
            animation2.setDuration(1000);

            discoveryInfo.startAnimation(animation2);

            lv.setVisibility(View.VISIBLE);
            animation2 = new TranslateAnimation(relativeLayout.getWidth() - discoveryInfo.getWidth(), 0, 0, 0);
            animation2.setDuration(1000);

            lv.startAnimation(animation2);

        }

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FoundDevice device = main.getBlueHunter().disMan.getFDInCurDiscoverySession().get(position);

                List<FoundDevice> fDevices = DatabaseManager.getCachedList();

                if (fDevices != null) {

                    int newPosition = fDevices.indexOf(device);

                    main.mViewPager.setCurrentItem(FragmentLayoutManager.PAGE_FOUND_DEVICES, true);
                    ListView listView = (ListView) main.findViewById(R.id.listView2);

                    listView.smoothScrollToPositionFromTop(newPosition, 0, 1000);

                }

            }
        });

    }

    public static void stopShowLV(MainActivity main) {

        if (lv == null) lv = (ListView) main.findViewById(R.id.discoveryListView);

        if (lv == null) return;

        int orientation = main.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            RelativeLayout relativeLayout = (RelativeLayout) lv.getParent();
            TableLayout discoveryInfo = (TableLayout) main.findViewById(R.id.discoveryInfoTable);

            TranslateAnimation animation2 = new TranslateAnimation(0, 0, 0, relativeLayout.getHeight() - discoveryInfo.getHeight());
            animation2.setDuration(750);

            lv.startAnimation(animation2);

            lv.setVisibility(View.GONE);

            float topOfDiscoveryInfo = relativeLayout.getHeight() / (float) 2 - discoveryInfo.getHeight() / (float) 2;

            TranslateAnimation animation = new TranslateAnimation(0, 0, -topOfDiscoveryInfo, 0);
            animation.setDuration(750);

            discoveryInfo.startAnimation(animation);

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            RelativeLayout relativeLayout = (RelativeLayout) lv.getParent();
            TableLayout discoveryInfo = (TableLayout) main.findViewById(R.id.discoveryInfoTable);

            TranslateAnimation animation2 = new TranslateAnimation(0, relativeLayout.getWidth() - discoveryInfo.getWidth(), 0, 0);
            animation2.setDuration(750);

            lv.startAnimation(animation2);

            lv.setVisibility(View.GONE);

            float leftOfDiscoveryInfo = relativeLayout.getWidth() / (float) 2 - discoveryInfo.getWidth() / (float) 2;

            TranslateAnimation animation = new TranslateAnimation(-leftOfDiscoveryInfo, 0, 0, 0);
            animation.setDuration(750);

            discoveryInfo.startAnimation(animation);

        }

    }

    static class Holder {

        TextView macAddress;
        TextView manufacturer;
        TextView exp;
        ImageView rssi;
        TextView state;

    }

    public class DiscoveryAdapter extends ArrayAdapter<FoundDevice> {

        private final Activity context;
        private final ArrayList<FoundDevice> values;

        public DiscoveryAdapter(Activity context, ArrayList<FoundDevice> curList) {

            super(context, R.layout.act_page_discovery_row, curList);
            this.values = curList;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.act_page_discovery_row, null);

                DeviceDiscoveryLayout.Holder holder = new Holder();
                holder.macAddress = (TextView) rowView.findViewById(R.id.macTxtView);
                holder.manufacturer = (TextView) rowView.findViewById(R.id.manufacturerTxtView);
                holder.exp = (TextView) rowView.findViewById(R.id.expTxtView);
                holder.rssi = (ImageView) rowView.findViewById(R.id.rssiView);
                holder.state = (TextView) rowView.findViewById(R.id.DDCStateTxtView);

                rowView.setTag(holder);

            }

            DeviceDiscoveryLayout.Holder vHolder = (DeviceDiscoveryLayout.Holder) rowView.getTag();

            FoundDevice device = values.get(position);

            String mac = device.getMacAddressString();
            int manufacturer = device.getManufacturer();

            int exp = ManufacturerList.getExp(manufacturer);

            int bonusExp = (int) (ManufacturerList.getExp(manufacturer) * device.getBoost());

            exp = bonusExp + exp;

            int rssi = device.getRssi();
            int rssiRes = 0;

            // 0 bars
            if (rssi <= -102) rssiRes = 0;

            // 1 bar
            if (rssi >= -101 && rssi <= -93) rssiRes = R.drawable.rssi_1;

            // 2 bars
            if (rssi >= -92 && rssi <= -87) rssiRes = R.drawable.rssi_2;

            // 3 bars
            if (rssi >= -86 && rssi <= -78) rssiRes = R.drawable.rssi_3;

            // 4 bars
            if (rssi >= -77 && rssi <= -40) rssiRes = R.drawable.rssi_4;

            // 5 bars
            if (rssi >= -41) rssiRes = R.drawable.rssi_5;

            String expString = context.getString(R.string.str_foundDevices_exp_abbreviation);

            if (device.isOld()) {
                vHolder.state.setText(R.string.str_discovery_state_old);
                vHolder.state.setTypeface(null, Typeface.NORMAL);
                vHolder.state.setTextColor(ContextCompat.getColor(context, R.color.text_discovery_state_old));
                ((View) vHolder.state.getParent()).setAlpha(0.75f);

                vHolder.exp.setText("=" + exp + " " + expString);
            } else {
                vHolder.state.setText(R.string.str_discovery_state_new);
                vHolder.state.setTypeface(null, Typeface.BOLD);
                vHolder.state.setTextColor(ContextCompat.getColor(context, R.color.text_holo_light_blue));
                ((View) vHolder.state.getParent()).setAlpha(1f);

                vHolder.exp.setText("+" + exp + " " + expString);
            }

            vHolder.macAddress.setText(mac);
            vHolder.manufacturer.setText(ManufacturerList.getName(manufacturer));
            vHolder.rssi.setImageResource(rssiRes);

            return rowView;
        }

    }

}