package com.unknown.gaelan;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RetrofitError;

public class PlaylistsParserService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private ArrayList<PlaylistSimple> mPlaylists;
    private PlaylistsParserCallback mCallback;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void parsePlaylists(ArrayList<PlaylistSimple> playlists, PlaylistsParserCallback playlistsParsedCallback) {
        mPlaylists = playlists;
        mCallback = playlistsParsedCallback;
        parsePlaylists();
    }

    private void parsePlaylists() {
        new AsyncTask<Void, String, RetrofitError>() {

            public ArrayList<RankedTrack> mRankedTracks;

            @Override
            protected RetrofitError doInBackground(Void... params) {
                HashMap<String, RankedTrack> trackMap = new HashMap<>();
                for (int i = 0; i < mPlaylists.size(); i++) {
                    PlaylistSimple playlist = mPlaylists.get(i);
                    String playlistId = playlist.id;
                    String playlistName = playlist.name;
                    String playlistOwnerId = playlist.owner.id;
                    publishProgress(playlistName, String.valueOf(i + 1));
                    try {
                        Pager<PlaylistTrack> playlistTrackPager = PlaylistMiner.getSpotifyService(PlaylistsParserService.this).getPlaylistTracks(playlistOwnerId, playlistId);
                        List<PlaylistTrack> playlistTracks = playlistTrackPager.items;
                        for (int j = 0; j < playlistTracks.size(); j++) {
                            Track track = playlistTracks.get(j).track;
                            if (null != track && !TextUtils.isEmpty(track.id)) {
                                RankedTrack rankedTrack = trackMap.get(track.id);
                                if (null == rankedTrack) {
                                    trackMap.put(track.id, new RankedTrack(track));
                                } else {
                                    rankedTrack.increment();
                                    trackMap.put(track.id, rankedTrack);
                                }
                            }
                        }
                    } catch (RetrofitError error) {
                        return error;
                    }
                }

                mRankedTracks = new ArrayList<RankedTrack>(trackMap.values());
                Collections.sort(mRankedTracks, RankedTrack.RANK_COMPARATOR);
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mCallback.onParsingPlaylist(values[0], Integer.parseInt(values[1]));
            }

            @Override
            protected void onPostExecute(RetrofitError error) {
                if (null != error)
                    mCallback.onErrorParsingPlaylists(error);
                else if (null != mRankedTracks)
                    mCallback.onPlaylistsParsed(mRankedTracks);
            }
        }.execute();
    }

    public interface PlaylistsParserCallback {

        public void onParsingPlaylist(String playlistName, int playlistPosition);

        public void onPlaylistsParsed(ArrayList<RankedTrack> rankedTracks);

        public void onErrorParsingPlaylists(RetrofitError error);

    }

    public class LocalBinder extends Binder {
        PlaylistsParserService getService() {
            return PlaylistsParserService.this;
        }
    }

}
