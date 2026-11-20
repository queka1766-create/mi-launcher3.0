package com.aigaming1766;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(4, 7, 17);
    private final int PANEL = Color.rgb(10, 17, 32);
    private final int CYAN = Color.rgb(41, 217, 255);
    private final int WHITE = Color.WHITE;
    private LinearLayout appList;
    private EditText search;
    private final List<AppEntry> apps = new ArrayList<>();

    static class AppEntry {
        String label, packageName;
        android.graphics.drawable.Drawable icon;
        AppEntry(String l, String p, android.graphics.drawable.Drawable i) {
            label = l; packageName = p; icon = i;
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
        loadApps();
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 28, 22, 16);
        root.setBackgroundColor(BG);

        TextView title = text("1766 AI GAMING", 26, CYAN, true);
        title.setLetterSpacing(0.08f);
        root.addView(title);

        TextView subtitle = text("TU DISPOSITIVO  //  MOTO G04S", 12, 0xff8798b5, true);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.topMargin = 8;
        root.addView(subtitle, subParams);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(WHITE);
        search.setHintTextColor(0xff8798b5);
        search.setHint("Buscar juegos y aplicaciones...");
        search.setPadding(16, 12, 16, 12);
        search.setBackground(rounded(PANEL, 18));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, -2);
        searchParams.topMargin = 22;
        root.addView(search, searchParams);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        TextView count = text("APLICACIONES INSTALADAS", 12, CYAN, true);
        bar.addView(count, new LinearLayout.LayoutParams(0, -2, 1));
        TextView refresh = text("ACTUALIZAR", 11, WHITE, true);
        refresh.setPadding(12, 10, 12, 10);
        refresh.setBackground(rounded(0xff14243a, 14));
        refresh.setOnClickListener(v -> loadApps());
        bar.addView(refresh);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(-1, -2);
        barParams.topMargin = 20;
        root.addView(bar, barParams);

        ScrollView scroll = new ScrollView(this);
        appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(-1, -1);
        listParams.topMargin = 12;
        scroll.addView(appList);
        root.addView(scroll, listParams);

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { renderApps(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
        setContentView(root);
    }

    private void loadApps() {
        apps.clear();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> results = pm.queryIntentActivities(intent, 0);
        for (android.content.pm.ResolveInfo info : results) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String label = info.loadLabel(pm).toString();
            apps.add(new AppEntry(label, pkg, info.loadIcon(pm)));
        }
        Collections.sort(apps, Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
        renderApps(search == null ? "" : search.getText().toString());
    }

    private void renderApps(String query) {
        if (appList == null) return;
        appList.removeAllViews();
        String q = query.toLowerCase(Locale.ROOT).trim();
        int shown = 0;
        for (AppEntry app : apps) {
            if (!app.label.toLowerCase(Locale.ROOT).contains(q) &&
                !app.packageName.toLowerCase(Locale.ROOT).contains(q)) continue;

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(14, 12, 14, 12);
            row.setBackground(rounded(PANEL, 18));
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(app.icon);
            row.addView(icon, new LinearLayout.LayoutParams(48, 48));

            LinearLayout labels = new LinearLayout(this);
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.setPadding(14, 0, 0, 0);
            labels.addView(text(app.label, 15, WHITE, true));
            labels.addView(text(app.packageName, 10, 0xff8798b5, false));
            row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

            row.setOnClickListener(v -> {
                Intent launch = getPackageManager().getLaunchIntentForPackage(app.packageName);
                if (launch != null) startActivity(launch);
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.bottomMargin = 9;
            appList.addView(row, p);
            shown++;
        }
        if (shown == 0) {
            TextView empty = text("No se encontraron aplicaciones.", 14, 0xff8798b5, false);
            empty.setPadding(8, 20, 8, 20);
            appList.addView(empty);
        }
    }
}
