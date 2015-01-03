package com.unknown.gaelan;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class PlaylistMiner extends Application {

    protected static final String CLIENT_ID = "8f3ae002e2c24b7db50a4258b4311226";
    protected static final String REDIRECT_URI = "gb-unknown://callback";

    private static SpotifyService mSpotifyService;


    public static SpotifyService getSpotifyService(Context context) {
        if (null == mSpotifyService) {
            String accessToken = PrefsHelper.getToken(context);
            if (TextUtils.isEmpty(accessToken))
                return null;
            SpotifyApi spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(accessToken);
            mSpotifyService = spotifyApi.getService();
        }
        return mSpotifyService;
    }

}
