package com.nabinbhandari.appslist;

import android.app.Activity;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

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
                ApplicationInfo info = (ApplicationInfo) parent.getAdapter().getItem(position);
                Toast.makeText(MainActivity.this, info.packageName, Toast.LENGTH_SHORT).show();
            }
        });
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
