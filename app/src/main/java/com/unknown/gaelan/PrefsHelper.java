package com.unknown.gaelan;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class PrefsHelper {

    private static final String PREF_ACCESS_TOKEN = "pref_access_token";
    private static final String PREF_RECENT_SEARCHES = "pref_recent_searches";
    private static final String PREF_MAX_ALLOWED_TRACK_COUNT = "pref_max_allowed_track_count";

    public static void putToken(Context context, String accessToken) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_ACCESS_TOKEN, accessToken).commit();
    }

    public static String getToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCESS_TOKEN, null);
    }

    public static void putRecent(Context context, String searchQuery) {
        String recentSearches = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_RECENT_SEARCHES, "");
        ArrayList<String> items = new ArrayList<>(Arrays.asList(recentSearches.split(";")));
        if (!items.contains(searchQuery)) {
            items.add(searchQuery);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                sb.append(items.get(i));
                if (i > 0 && i < items.size() - 1) {
                    sb.append(";");
                }
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

    public static int getMaxAllowedTrackCount(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_MAX_ALLOWED_TRACK_COUNT, 100);
    }
}
