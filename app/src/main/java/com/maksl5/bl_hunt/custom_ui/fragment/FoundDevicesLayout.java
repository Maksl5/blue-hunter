package com.maksl5.bl_hunt.custom_ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.ManufacturerList;
import com.maksl5.bl_hunt.storage.PreferenceManager;
import com.maksl5.bl_hunt.util.FoundDevice;
import com.maksl5.bl_hunt.util.MacAddress;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksl5
 */
public class FoundDevicesLayout {

    public static final int ARRAY_INDEX_MAC_ADDRESS = 0;
    public static final int ARRAY_INDEX_NAME = 1;
    public static final int ARRAY_INDEX_RSSI = 2;
    public static final int ARRAY_INDEX_MANUFACTURER = 3;
    public static final int ARRAY_INDEX_EXP = 4;
    public static final int ARRAY_INDEX_TIME = 5;
    public static int selectedItem = -1;
    private static final OnItemLongClickListener onLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            if (view.getContext() instanceof MainActivity) {

                MainActivity mainActivity = (MainActivity) view.getContext();

                selectedItem = position;

                mainActivity.startActionMode(mainActivity.getBlueHunter().actionBarHandler.actionModeCallback);

                ListView foundDevListView = (ListView) parent;

                // for (int i = 0; i < foundDevListView.getChildCount(); i++) {
                // View child = foundDevListView.getChildAt(i);
                // ((CheckBox)
                // child.findViewById(R.id.selectCheckbox)).setVisibility(View.VISIBLE);
                // }

                foundDevListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                foundDevListView.setItemChecked(position, true);

                return true;
            } else {
                return false;
            }
        }

    };
    private static ArrayList<FDAdapterData> showedFdList = new ArrayList<>();
    private static ArrayList<FDAdapterData> completeFdList = new ArrayList<>();
    private static ThreadManager threadManager = null;

    public static void refreshFoundDevicesList(final BlueHunter bhApp) {

        if (threadManager == null) {
            threadManager = new FoundDevicesLayout().new ThreadManager();
        }

        if (false) {

            ListView listView;
            FoundDevicesAdapter fdAdapter;

            if (bhApp.mainActivity.mViewPager == null) {
                bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
            }

            ViewPager pager = bhApp.mainActivity.mViewPager;

            if (pager == null) {
                return;
            }

            View pageView = pager.getChildAt(FragmentLayoutManager.PAGE_FOUND_DEVICES + 1);

            if (pageView == null) {
                listView = (ListView) pager.findViewById(R.id.listView2);
            } else {
                listView = (ListView) pageView.findViewById(R.id.listView2);
            }

            if (listView == null) {
                listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView2);
            }

            if (listView == null) return;

            fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
            if (fdAdapter == null || fdAdapter.isEmpty()) {
                fdAdapter = new FoundDevicesLayout().new FoundDevicesAdapter(bhApp.mainActivity,
                        showedFdList);
                listView.setAdapter(fdAdapter);
            }

            fdAdapter.notifyDataSetChanged();
            return;

        }

        RefreshThread refreshThread = new FoundDevicesLayout().new RefreshThread(bhApp, threadManager);
        if (refreshThread.canRun()) {
            refreshThread.execute();
        } else {

            ListView listView;
            FoundDevicesAdapter fdAdapter;

            if (bhApp.mainActivity.mViewPager == null) {
                bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
            }

            ViewPager pager = bhApp.mainActivity.mViewPager;

            if (pager == null) {
                return;
            }

            View pageView = pager.getChildAt(FragmentLayoutManager.PAGE_FOUND_DEVICES + 1);

            if (pageView == null) {
                listView = (ListView) pager.findViewById(R.id.listView2);
            } else {
                listView = (ListView) pageView.findViewById(R.id.listView2);
            }

            if (listView == null) {
                listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView2);
            }

            if (listView == null) return;

            fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
            if (fdAdapter == null || fdAdapter.isEmpty()) {
                fdAdapter = new FoundDevicesLayout().new FoundDevicesAdapter(bhApp.mainActivity,
                        showedFdList);
                listView.setAdapter(fdAdapter);
            }

            fdAdapter.refreshList(showedFdList);

        }

    }

    public static void filterFoundDevices(String text, BlueHunter bhApp) {

        if (threadManager.running) return;

        ArrayList<FDAdapterData> searchedList = new ArrayList<>();

        ListView listView;
        FoundDevicesAdapter fdAdapter;

        if (bhApp.mainActivity.mViewPager == null) {
            bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
        }

        ViewPager pager = bhApp.mainActivity.mViewPager;

        if (pager == null) {
            return;
        }

        View pageView = pager.getChildAt(FragmentLayoutManager.PAGE_FOUND_DEVICES + 1);

        if (pageView == null) {
            listView = (ListView) pager.findViewById(R.id.listView2);
        } else {
            listView = (ListView) pageView.findViewById(R.id.listView2);
        }

        if (listView == null) {
            listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView2);
        }

        if (listView == null) return;

        fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
        if (fdAdapter == null || fdAdapter.isEmpty()) {
            fdAdapter = new FoundDevicesLayout().new FoundDevicesAdapter(bhApp.mainActivity,
                    showedFdList);
            listView.setAdapter(fdAdapter);
        }

        text = text.toLowerCase();

        if (text.equals("[unknown]")) {

            for (FDAdapterData data : completeFdList) {

                if (data.getManufacturer() == 0) {
                    searchedList.add(data);
                }
            }
            showedFdList = searchedList;
            fdAdapter.refreshList(showedFdList);

        } else if (text.length() == 0) {
            if (!showedFdList.equals(completeFdList)) {
                showedFdList = new ArrayList<>(completeFdList);
                fdAdapter.refreshList(showedFdList);

            }
        } else {

            ArrayList<FDAdapterData> filterList = new ArrayList<>(completeFdList);

            final int count = filterList.size();
            final ArrayList<FDAdapterData> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final FDAdapterData data = filterList.get(i);

                if (data.getMacAddress().toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

                if (data.getName() != null && data.getName().toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

                if (data.getTimeFormatted().toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

                if (ManufacturerList.getName(data.getManufacturer()).toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

                if (data.getExpString().toLowerCase().contains(text))
                    if (!newValues.contains(data)) newValues.add(data);

            }

            showedFdList = newValues;
            fdAdapter.refreshList(showedFdList);

        }

    }

    /**
     * @return
     */
    public static MacAddress getSelectedMac() {

        if (selectedItem == -1) return null;

        return showedFdList.get(selectedItem).macAddress;

    }

    public static void cancelAllTasks() {
        if (threadManager != null && threadManager.refreshThread != null)
            threadManager.refreshThread.cancel(true);
    }

    static class ViewHolder {

        TextView macAddress;
        TextView name;
        TextView manufacturer;
        ImageView rssi;
        TextView time;
        TextView exp;
        TableRow nameTableRow;
        CheckBox selectCheckBox;
    }

    private class RefreshThread extends AsyncTask<Void, ArrayList<FDAdapterData>, ArrayList<FDAdapterData>> {

        private final BlueHunter bhApp;
        boolean needManuCheck = false;
        private ListView listView;
        private FoundDevicesAdapter fdAdapter;
        private ThreadManager threadManager;
        private boolean canRun = true;
        private int scrollIndex;
        private int scrollTop;

        private RefreshThread(BlueHunter app, ThreadManager threadManager) {

            super();
            this.bhApp = app;

            this.needManuCheck = PreferenceManager.getPref(bhApp, "requireManuCheck", false);

            if (bhApp.mainActivity.mViewPager == null) {
                bhApp.mainActivity.mViewPager = (ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
            }

            ViewPager pager = bhApp.mainActivity.mViewPager;

            if (pager == null) {
                canRun = false;
                return;
            }

            View pageView = pager.getChildAt(FragmentLayoutManager.PAGE_FOUND_DEVICES + 1);

            if (pageView == null) {
                listView = (ListView) pager.findViewById(R.id.listView2);
            } else {
                listView = (ListView) pageView.findViewById(R.id.listView2);
            }

            if (listView == null) {
                listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView2);
            }

            if (listView == null) {
                canRun = false;
                return;
            }

            listView.setOnItemLongClickListener(onLongClickListener);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String macString = showedFdList.get(position).getMacAddress();
                    if (macString != null) {

                        ClipboardManager clipboardManager = (ClipboardManager) bhApp.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("Mac Address", macString));

                        Toast.makeText(bhApp, "Copied Mac Address " + macString + " into clipboard", Toast.LENGTH_SHORT).show();

                    }

                }
            });

            scrollIndex = listView.getFirstVisiblePosition();
            View v = listView.getChildAt(0);
            scrollTop = (v == null) ? 0 : v.getTop();

            this.fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
            if (this.fdAdapter == null || this.fdAdapter.isEmpty()) {
                this.fdAdapter = new FoundDevicesAdapter(bhApp.mainActivity, showedFdList);
                this.listView.setAdapter(fdAdapter);
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
        protected ArrayList<FDAdapterData> doInBackground(Void... params) {

            //Debug.startMethodTracing("FDThread");
            long backTimeA = System.currentTimeMillis();

            if (DatabaseManager.getCachedList() == null) {

                new DatabaseManager(bhApp).loadAllDevices();
                return completeFdList;

            }

            List<FoundDevice> allDevices = new ArrayList<>(DatabaseManager.getCachedList());

            ArrayList<FDAdapterData> listViewList = new ArrayList<>();

            String expString = bhApp.getString(R.string.str_foundDevices_exp_abbreviation);

            DateTimeFormatter dateTimeFormat = DateTimeFormat.mediumDateTime();

            FDAdapterData adapterData;

            int i = 1;

            for (FoundDevice device : allDevices) {

                MacAddress deviceMac = device.getMacAddress();
                int manufacturer = device.getManufacturer();
                long time = device.getTime();
                float bonusExpMultiplier = device.getBoost();
                short rssi = device.getRssi();

                if (needManuCheck && manufacturer == 0) {
                    manufacturer = ManufacturerList.getManufacturer(deviceMac.getA(), deviceMac.getB(), deviceMac.getC()).getId();
                    if (manufacturer != 0) {
                        new DatabaseManager(bhApp).addManufacturerToDevice(deviceMac, manufacturer);
                    }

                }

                if (time == -1L) time = (long) 0;

                if (bonusExpMultiplier == -1f) {
                    FDAdapterData addBonus = new FDAdapterData();
                    addBonus.setMacAddress(deviceMac);
                    addBonus.setManufacturer(-100);

                    ArrayList<FDAdapterData> publishList = new ArrayList<>();
                    publishList.add(addBonus);

                    publishProgress(publishList);
                    bonusExpMultiplier = 0f;
                }

                // exp calc
                int bonusExp = (int) (ManufacturerList.getExp(manufacturer) * bonusExpMultiplier);

                String completeExpString = (bonusExp == 0) ? "+" + ManufacturerList.getExp(manufacturer) + " " + expString
                        : "+" + ManufacturerList.getExp(manufacturer) + " + " + bonusExp + " " + expString;

                // RSSI calculation
                int rssiRes = 0;

                // 0 bars
                if (rssi <= -102) rssiRes = 0;

                    // 1 bar
                else if (rssi >= -101 && rssi <= -93) rssiRes = R.drawable.rssi_1;

                    // 2 bars
                else if (rssi >= -92 && rssi <= -87) rssiRes = R.drawable.rssi_2;

                    // 3 bars
                else if (rssi >= -86 && rssi <= -78) rssiRes = R.drawable.rssi_3;

                    // 4 bars
                else if (rssi >= -77 && rssi <= -40) rssiRes = R.drawable.rssi_4;

                    // 5 bars
                else rssiRes = R.drawable.rssi_5;

                adapterData = new FDAdapterData(deviceMac, device.getName(), rssiRes, manufacturer, completeExpString,
                        new DateTime(time).toString(dateTimeFormat));

                listViewList.add(adapterData);


                Log.d("FD doInBackground()", "@device #" + i);

                i++;

                // publishProgress(listViewList);

            }

            //Debug.stopMethodTracing();
            long backTimeB = System.currentTimeMillis();

            Log.d("FD Background Thread", "" + (backTimeB - backTimeA) + "ms");

            return listViewList;

            // ListenerClass listenerClass = new FragmentLayoutManager().new
            // ListenerClass();

            // lv.setOnHierarchyChangeListener(listenerClass);

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(ArrayList<FDAdapterData> result) {

            long postA = System.currentTimeMillis();

            if (!completeFdList.equals(result)) {
                completeFdList = new ArrayList<>(result);
                showedFdList = new ArrayList<>(result);

                fdAdapter.refreshList(showedFdList);
            } else {
                fdAdapter.notifyDataSetChanged();
            }

            listView.setSelectionFromTop(scrollIndex, scrollTop);

            bhApp.synchronizeFoundDevices.checkAndStart();

            if (needManuCheck) {
                DeviceDiscoveryLayout.updateIndicatorViews(bhApp.mainActivity);
                PreferenceManager.setPref(bhApp, "requireManuCheck", false);
            }

            threadManager.finished(this);

            long postB = System.currentTimeMillis();

            Log.d("FD Thread PostExecute", "" + (postB - postA) + "ms");

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @SafeVarargs
        @Override
        protected final void onProgressUpdate(ArrayList<FDAdapterData>... values) {

            // showedFdList = values[0];

            // fdAdapter.refill(showedFdList);

            // if (values[0].get(0).getManufacturer() == -100)
            // new
            // DatabaseManager(bhApp).addBoostToDevices(values[0].get(0).getMacAddress(),
            // 0f);
            //
            // listView.setSelectionFromTop(scrollIndex, scrollTop);

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            if (needManuCheck) {
                Toast.makeText(bhApp, "Require Manufacturer Check...", Toast.LENGTH_SHORT).show();
                Log.d("FD Thread", "Require Manufacturer Check...");
            }
        }
    }

    private class ThreadManager {

        RefreshThread refreshThread;
        boolean running;

        /**
         * @param refreshThread
         * @return
         */
        public boolean setThread(RefreshThread refreshThread) {

            if (running) {
                return false;
            }

            this.refreshThread = refreshThread;
            running = true;
            return true;
        }

        public void finished(RefreshThread refreshThread) {

            if (this.refreshThread.equals(refreshThread)) {
                running = false;
                return;
            }
        }

    }

    public class FDAdapterData {

        private MacAddress macAddress;
        private String name;
        private int rssiRes;
        private int manufacturer;
        private String expString;
        private String timeFormatted;

        public FDAdapterData(MacAddress macAddress, String name, int rssiRes, int manufacturer, String expString, String timeFormatted) {

            this.macAddress = macAddress;

            this.name = name;
            this.rssiRes = rssiRes;
            this.manufacturer = manufacturer;
            this.expString = expString;
            this.timeFormatted = timeFormatted;
        }

        /**
         *
         */
        public FDAdapterData() {

            // TODO Auto-generated constructor stub
        }

        public String getMacAddress() {

            return macAddress.getMacString();
        }

        public void setMacAddress(MacAddress macAddress) {

            this.macAddress = macAddress;

        }

        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }

        public int getRssiRes() {

            return rssiRes;
        }

        public void setRssiRes(int rssiRes) {

            this.rssiRes = rssiRes;
        }

        public int getManufacturer() {

            return manufacturer;
        }

        public void setManufacturer(int manufacturer) {

            this.manufacturer = manufacturer;
        }

        public String getExpString() {

            return expString;
        }

        public void setExpString(String expString) {

            this.expString = expString;
        }

        public String getTimeFormatted() {

            return timeFormatted;
        }

        public void setTimeFormatted(String timeFormatted) {

            this.timeFormatted = timeFormatted;
        }

        @Override
        public boolean equals(Object o) {

            if (!(o instanceof FDAdapterData)) return false;

            FDAdapterData fData = (FDAdapterData) o;

            boolean equalMac = macAddress.equals(fData.macAddress);

            if (!equalMac) return false;

            if (name == null && fData.name == null) {
                return true;
            } else
                return name != null && name.equals(fData.name);

        }

    }

    public class FoundDevicesAdapter extends ArrayAdapter<FDAdapterData> {

        private final ArrayList<FDAdapterData> dataList;

        public FoundDevicesAdapter(Context context, ArrayList<FDAdapterData> objects) {

            super(context, R.layout.act_page_founddevices_row, objects);
            dataList = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.act_page_founddevices_row, null);

                FoundDevicesLayout.ViewHolder viewHolder = new ViewHolder();

                viewHolder.macAddress = (TextView) rowView.findViewById(R.id.macTxtView);
                viewHolder.name = (TextView) rowView.findViewById(R.id.nameTxtView);
                viewHolder.manufacturer = (TextView) rowView.findViewById(R.id.manufacturerTxtView);
                viewHolder.rssi = (ImageView) rowView.findViewById(R.id.rssiView);
                viewHolder.time = (TextView) rowView.findViewById(R.id.timeTxtView);
                viewHolder.exp = (TextView) rowView.findViewById(R.id.expTxtView);
                viewHolder.nameTableRow = (TableRow) rowView.findViewById(R.id.FDRtableRow1);

                rowView.setTag(viewHolder);
            }

            FoundDevicesLayout.ViewHolder holder = (FoundDevicesLayout.ViewHolder) rowView.getTag();

            FDAdapterData data = dataList.get(position);

            if (holder != null && data != null) {

                String nameString = data.getName();
                if (nameString == null || nameString.equals("null")) {
                    nameString = "";
                    holder.nameTableRow.setVisibility(View.GONE);
                } else {
                    holder.nameTableRow.setVisibility(View.VISIBLE);
                }

                holder.macAddress.setText(data.getMacAddress());
                holder.name.setText(nameString);
                holder.manufacturer.setText(ManufacturerList.getName(data.getManufacturer()));

                int rssiId = data.getRssiRes();
                if (rssiId == 0)
                    holder.rssi.setImageResource(android.R.color.transparent);
                else
                    holder.rssi.setImageResource(rssiId);

                holder.time.setText(data.getTimeFormatted());
                holder.exp.setText(data.getExpString());

            }
            return rowView;
        }

        public void refreshList(ArrayList<FDAdapterData> data) {

            clear();
            addAll(new ArrayList<>(data));
            notifyDataSetChanged();

        }

    }

}