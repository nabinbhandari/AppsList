package com.nabinbhandari.appslist;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private List<UsageStats> usageStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);
        List<ApplicationInfo> apps = listApps();
        listView.setAdapter(new AppListAdapter(this, apps));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (usageStats == null) {
                    Toast.makeText(MainActivity.this, "Usage access not provided.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ApplicationInfo info = (ApplicationInfo) parent.getAdapter().getItem(position);
                UsageStats stat = findUsageStatByPackageName(info.packageName);
                if (stat == null) {
                    String message = "Usage statistics not found in the last day for " + info.packageName;
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getStatDesc(stat), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!AppUsagePermissions.hasUsageAccess(this)) {
            AppUsagePermissions.requestAppUsageAccessPermission(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppUsagePermissions.hasUsageAccess(this)) {
            collectStats();
        } else {
            Toast.makeText(this, "Usage access not provided.", Toast.LENGTH_SHORT).show();
        }
    }

    private void collectStats() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return;

        long currentTime = System.currentTimeMillis();
        int millisPerDay = 24 * 3600 * 1000;
        usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                currentTime - millisPerDay, currentTime);
        for (UsageStats stat : usageStats) {
            System.err.println(stat.toString() + stat.getPackageName() + " - " +
                    stat.getLastTimeUsed() + " - " + stat.getTotalTimeInForeground());
        }
    }

    private UsageStats findUsageStatByPackageName(String packageName) {
        for (UsageStats stat : usageStats) {
            if (packageName.equalsIgnoreCase(stat.getPackageName())) {
                return stat;
            }
        }
        return null;
    }

    private static String getStatDesc(UsageStats usageStats) {
        return "Daily usage stats:\n" + usageStats.getPackageName() +
                "\nLast time used: " + parseTime(usageStats.getLastTimeUsed()) +
                "\nTime in Foreground: " + millisToStr(usageStats.getTotalTimeInForeground());
    }

    private static String parseTime(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.getTime().toString();
    }

    private static String millisToStr(long millis) {
        return String.format(Locale.US, "%.2f s.", millis / 1000f);
    }

    private List<ApplicationInfo> listApps() {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        List<ApplicationInfo> filtered = new ArrayList<>();
        for (ApplicationInfo appInfo : apps) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                filtered.add(appInfo);
            }
        }

        Collections.sort(filtered, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo app1, ApplicationInfo app2) {
                String app1Name = pm.getApplicationLabel(app1).toString();
                String app2Name = pm.getApplicationLabel(app2).toString();
                return app1Name.compareTo(app2Name);
            }
        });

        return filtered;
    }

    static class AppListAdapter extends ArrayAdapter<ApplicationInfo> {

        private List<ApplicationInfo> apps;

        AppListAdapter(Context context, List<ApplicationInfo> apps) {
            super(context, 0);
            this.apps = apps;
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = parent.getContext();
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView textView = convertView.findViewById(android.R.id.text1);
            ApplicationInfo info = getItem(position);
            if (info == null) return convertView;

            textView.setText(context.getPackageManager().getApplicationLabel(info));
            return convertView;
        }

        @Override
        public ApplicationInfo getItem(int position) {
            return apps.get(position);
        }

        @Override
        public int getCount() {
            return apps.size();
        }
    }

}
