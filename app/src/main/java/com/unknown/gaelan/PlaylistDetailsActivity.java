package com.unknown.gaelan;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.SnapshotId;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class PlaylistDetailsActivity extends ActionBarActivity {

    public static final String EXTRA_PLAYLIST_OWNER_ID = "playlist_owner_id";
    public static final String EXTRA_PLAYLIST_ID = "playlist_id";
    public static final String EXTRA_PLAYLIST_NAME = "playlist_name";
    private static final String TAG = "PlaylistDetailsActivity";
    private String mPlaylistName;
    private List<PlaylistTrack> mTracks;
    private TextView mEmptyText;
    private ProgressBar mProgress;
    private ListView mListView;
    private PlaylistAdapter mPlaylistAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String playlistOwnerId = extras.getString(EXTRA_PLAYLIST_OWNER_ID);
        String playlistId = extras.getString(EXTRA_PLAYLIST_ID);
        mPlaylistName = extras.getString(EXTRA_PLAYLIST_NAME);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mPlaylistName);
        setContentView(R.layout.activity_playlist_details);

        performPlaylistTracksSearch(playlistOwnerId, playlistId);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mEmptyText = (TextView) findViewById(android.R.id.empty);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(mProgress);
        mPlaylistAdapter = new PlaylistAdapter(this);
        mListView.setAdapter(mPlaylistAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save_playlist:
                onSavePlaylist();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void performPlaylistTracksSearch(String playlistOwnerId, String playlistId) {
        PlaylistMiner.getSpotifyService(this).getPlaylistTracks(playlistOwnerId, playlistId, new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                mTracks = playlistTrackPager.items;
                if (null != mListView)
                    mListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mPlaylistAdapter.setTracks(mTracks);
                        }
                    });
            }

            @Override
            public void failure(final RetrofitError error) {
                if (null != mListView)
                    mListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.INVISIBLE);
                            mEmptyText.setText("An error occurred, " + error.getMessage());
                            mListView.setEmptyView(mEmptyText);
                        }
                    });
            }
        });

    }

    private void onSavePlaylist() {
        PlaylistMiner.getSpotifyService(this).getMe(new Callback<User>() {
            @Override
            public void success(final User user, Response response) {
                createPlaylist(user, mPlaylistName);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Error retrieving user, " + error.getMessage());
            }
        });


    }

    private void createPlaylist(final User user, final String playlistName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("https://api.spotify.com/v1/users/" + user.id + "/playlists");
        try {
            JSONObject requestData = new JSONObject();
            requestData.accumulate("name", playlistName);
            requestData.accumulate("public", false);
            StringEntity stringEntity = new StringEntity(requestData.toString());
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + PrefsHelper.getToken(this));
            ResponseHandler responseHandler = new BasicResponseHandler() {
                @Override
                public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
                    String json = EntityUtils.toString(response.getEntity());
                    JsonObject responseData = new Gson().fromJson(json, JsonObject.class);
                    String playlistId = responseData.get("id").getAsString();
                    addTracksToPlaylist(user, playlistName, playlistId);
                    return json;
                }
            };
            httpClient.execute(httpPost, responseHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTracksToPlaylist(User user, final String playlistName, String playlistId) {
        String trackUris = getPlaylistUris();
        PlaylistMiner.getSpotifyService(this).addTracksToPlaylist(user.id, playlistId, trackUris, new Callback<SnapshotId>() {
            @Override
            public void success(SnapshotId snapshotId, Response response) {
                Log.d(TAG, "Playlist created, SnapshotId = " + snapshotId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlaylistDetailsActivity.this, "Playlist '" + playlistName + "' saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Error adding songs to playlist, " + error.getMessage());
            }
        });
    }

    private String getPlaylistUris() {
        StringBuilder sb = new StringBuilder();
        int trackCount = mTracks.size();
        for (int i = 0; i < trackCount; i++) {
            sb.append(mTracks.get(i).track.uri);
            if (trackCount > 1 && i < trackCount - 1)
                sb.append(",");
        }
        return sb.toString();
    }

    class PlaylistAdapter extends BaseAdapter {

        private Context sContext;
        private List<PlaylistTrack> sTracks;

        public PlaylistAdapter(Context context) {
            sContext = context;
        }

        public void setTracks(List<PlaylistTrack> tracks) {
            this.sTracks = tracks;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return null == sTracks ? 0 : sTracks.size();
        }

        @Override
        public Object getItem(int position) {
            return sTracks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(sContext).inflate(android.R.layout.simple_list_item_2, parent, false);
            ((TextView) view.findViewById(android.R.id.text1)).setText(sTracks.get(position).track.name);
            ((TextView) view.findViewById(android.R.id.text2)).setText(sTracks.get(position).track.artists.get(0).name);
            return view;
        }
    }

}
