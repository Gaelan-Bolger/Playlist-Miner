package com.unknown.gaelan;

import android.content.Context;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.SnapshotId;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;

/**
 * Created by Gaelan on 1/3/2015.
 */
public class SpotifyHelper {

    public static void createPlaylist(Context context, User user, String playlistName, boolean isPublic, ResponseHandler responseHandler) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("https://api.spotify.com/v1/users/" + user.id + "/playlists");
        try {
            JSONObject requestData = new JSONObject();
            requestData.accumulate("name", playlistName);
            requestData.accumulate("public", isPublic);
            StringEntity stringEntity = new StringEntity(requestData.toString());
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + PrefsHelper.getToken(context));
            httpClient.execute(httpPost, responseHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addTracksToPlaylist(Context context, User user, String playlistName, String playlistId, String trackUris, Callback<SnapshotId> callback) {
        PlaylistMiner.getSpotifyService(context).addTracksToPlaylist(user.id, playlistId, trackUris, callback);
    }

    public static String getPlaylistTrackUris(List<PlaylistTrack> tracks) {
        StringBuilder sb = new StringBuilder();
        int trackCount = tracks.size();
        for (int i = 0; i < trackCount; i++) {
            sb.append(tracks.get(i).track.uri);
            if (trackCount > 1 && i < trackCount - 1)
                sb.append(",");
        }
        return sb.toString();
    }

    public static String getRankedTrackUris(ArrayList<RankedTrack> tracks, int minRank) {
        StringBuilder sb = new StringBuilder();
        int trackCount = tracks.size();
        for (int i = 0; i < trackCount; i++) {
            RankedTrack rankedTrack = tracks.get(i);
            if (rankedTrack.rank >= minRank) {
                sb.append(rankedTrack.track.uri);
                if (trackCount > 1 && i < trackCount - 1)
                    sb.append(",");
            }
        }
        return sb.toString();
    }
}
