package com.unknown.gaelan;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.Config;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class PlaylistMiner extends Application implements
        PlayerNotificationCallback, ConnectionStateCallback {

    protected static final String CLIENT_ID = "8f3ae002e2c24b7db50a4258b4311226";
    protected static final String REDIRECT_URI = "gb-unknown://callback";
    private static final String TAG = "PlaylistMiner";

    private static PlaylistMiner mApp;
    private static SpotifyService mSpotifyService;
    private static Player mPlayer;

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

    public static Player getPlayer(final Context context) {
        if (null == mPlayer) {
            Config playerConfig = new Config(context, PrefsHelper.getToken(context), CLIENT_ID);
            Spotify spotify = new Spotify();
            mPlayer = spotify.getPlayer(playerConfig, context, new Player.InitializationObserver() {
                @Override
                public void onInitialized() {
                    mPlayer.addConnectionStateCallback(mApp);
                    mPlayer.addPlayerNotificationCallback(mApp);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                }
            });
        }
        return mPlayer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    @Override
    public void onTerminate() {
        Spotify.destroyPlayer(mPlayer);
        super.onTerminate();
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d(TAG, "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "Temporary error occurred");
    }

    @Override
    public void onNewCredentials(String s) {
        Log.d(TAG, "User credentials blob received");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, "Playback event received: " + eventType.name());
        switch (eventType) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d(TAG, "Playback error received: " + errorType.name() + ", " + errorDetails);
        switch (errorType) {
            case TRACK_UNAVAILABLE:
                Toast.makeText(mApp, "Track currently unavailable", Toast.LENGTH_SHORT).show();
                break;
            case ERROR_PLAYBACK:
            case ERROR_UNKNOWN:
            default:
                break;
        }
    }

}
