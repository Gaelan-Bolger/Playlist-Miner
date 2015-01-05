package com.unknown.gaelan;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class PrefsHelper {

    private static final String PREF_ACCESS_TOKEN = "pref_access_token";
    private static final String PREF_RECENT_SEARCHES = "pref_recent_searches";
    private static final int DEFAULT_MAX_ALLOWED_TRACK_COUNT = 100;
    private static final int DEFAULT_MAX_SEARCH_RESULTS = 100;

    public static void putToken(Context context, String accessToken) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_ACCESS_TOKEN, accessToken).commit();
    }

    public static String getToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCESS_TOKEN, null);
    }

    public static void putRecent(Context context, String searchQuery) {
        String recentSearches = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_RECENT_SEARCHES, "");
        ArrayList<String> items;
        if (TextUtils.isEmpty(recentSearches))
            items = new ArrayList<>();
        else {
            String[] recents = recentSearches.split(";");
            items = new ArrayList<>(Arrays.asList(recents));
        }
        if (!items.contains(searchQuery)) {
            items.add(searchQuery);
            int count = items.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(items.get(i));
                if (count > 1 && i < count - 1)
                    sb.append(";");
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_RECENT_SEARCHES, sb.toString()).commit();
        }
    }

    public static String[] getRecents(Context context) {
        String recentSearches = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_RECENT_SEARCHES, "");
        return recentSearches.split(";");
    }

    public static void clearRecents(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(PREF_RECENT_SEARCHES).commit();
    }

}
