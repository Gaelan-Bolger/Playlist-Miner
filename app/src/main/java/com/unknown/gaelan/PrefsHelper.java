package com.unknown.gaelan;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class PrefsHelper {

    private static final String TAG = "PrefsHelper";
    private static final String PREF_ACCESS_TOKEN = "pref_access_token";
    private static final String PREF_ACCESS_TOKEN_EXPIRATION = "pref_access_token_expiration";
    private static final String PREF_RECENT_SEARCHES = "pref_recent_searches";

    public static void putAccessToken(Context context, String accessToken, int expiresIn) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_ACCESS_TOKEN, accessToken);
        editor.putLong(PREF_ACCESS_TOKEN_EXPIRATION, System.currentTimeMillis() + (expiresIn * 1000));
        editor.commit();
    }

    public static String getAccessToken(Context context) {
        return getDefaultSharedPreferences(context).getString(PREF_ACCESS_TOKEN, null);
    }

    public static Boolean isAccessTokenExpired(Context context) {
        return System.currentTimeMillis() > getDefaultSharedPreferences(context).getLong(PREF_ACCESS_TOKEN_EXPIRATION, 0L);
    }

    public static void putRecentSearch(Context context, String searchQuery) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);
        String recentSearches = preferences.getString(PREF_RECENT_SEARCHES, "");
        ArrayList<String> items;
        if (TextUtils.isEmpty(recentSearches))
            items = new ArrayList<>();
        else {
            String[] recentSearchesArray = recentSearches.split(";");
            items = new ArrayList<>(Arrays.asList(recentSearchesArray));
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
            preferences.edit().putString(PREF_RECENT_SEARCHES, sb.toString()).commit();
        }
    }

    public static String[] getRecentSearches(Context context) {
        String recentSearches = getDefaultSharedPreferences(context).getString(PREF_RECENT_SEARCHES, "");
        return recentSearches.split(";");
    }

    public static void clearRecentSearches(Context context) {
        getDefaultSharedPreferences(context).edit().remove(PREF_RECENT_SEARCHES).commit();
    }

    private static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
