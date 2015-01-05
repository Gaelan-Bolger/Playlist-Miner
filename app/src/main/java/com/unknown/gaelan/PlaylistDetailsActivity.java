package com.unknown.gaelan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
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
    private TwoWayView mRecyclerView;
    private PlaylistAdapter mPlaylistAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String playlistOwnerId = extras.getString(EXTRA_PLAYLIST_OWNER_ID);
        String playlistId = extras.getString(EXTRA_PLAYLIST_ID);
        mPlaylistName = extras.getString(EXTRA_PLAYLIST_NAME);

        mPlaylistAdapter = new PlaylistAdapter(this);
        setContentView(R.layout.activity_playlist_details);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mPlaylistName);

        performPlaylistTracksSearch(playlistOwnerId, playlistId);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mEmptyText = (TextView) findViewById(android.R.id.empty);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        mRecyclerView = (TwoWayView) findViewById(R.id.two_way_view);
        final ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecyclerView);
        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
                PlaylistMiner.getPlayer(PlaylistDetailsActivity.this).play(mTracks.get(i).track.uri);
            }
        });
        final Drawable divider = getResources().getDrawable(R.drawable.divider_thin_opaque);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        mRecyclerView.setAdapter(mPlaylistAdapter);
    }

    @Override
    protected void onStop() {
        PlaylistMiner.getPlayer(this).pause();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
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
                if (null != mRecyclerView)
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.GONE);
                            if (mTracks.size() == 0) {
                                mEmptyText.setVisibility(View.VISIBLE);
                            }
                            mPlaylistAdapter.setTracks(mTracks);
                        }
                    });
            }

            @Override
            public void failure(final RetrofitError error) {
                if (null != mRecyclerView)
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.GONE);
                            mEmptyText.setText("An error occurred, " + error.getMessage());
                            mEmptyText.setVisibility(View.VISIBLE);
                        }
                    });
            }
        });

    }

    private void onSavePlaylist() {
        new AlertDialog.Builder(this).setCancelable(true).setTitle("Save playlist").setMessage("Would you like to add this playlist to your library?")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                savePlaylist();
            }
        }).create().show();
    }

    private void savePlaylist() {
        PlaylistMiner.getSpotifyService(this).getMe(new Callback<User>() {
            @Override
            public void success(final User user, Response response) {
                SpotifyHelper.createPlaylist(PlaylistDetailsActivity.this, user, mPlaylistName, false, new BasicResponseHandler() {
                    @Override
                    public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
                        String json = EntityUtils.toString(response.getEntity());
                        JsonObject responseData = new Gson().fromJson(json, JsonObject.class);
                        String playlistId = responseData.get("id").getAsString();
                        String trackUris = SpotifyHelper.getPlaylistTrackUris(mTracks);
                        SpotifyHelper.addTracksToPlaylist(PlaylistDetailsActivity.this, user, mPlaylistName, playlistId, trackUris, new Callback<SnapshotId>() {
                            @Override
                            public void success(SnapshotId snapshotId, Response response) {
                                Log.d(TAG, "Playlist created, SnapshotId = " + snapshotId);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PlaylistDetailsActivity.this, "Playlist '" + mPlaylistName + "' added to library", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d(TAG, "Error adding songs to playlist, " + error.getMessage());
                            }
                        });
                        return json;
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Error retrieving user, " + error.getMessage());
            }
        });
    }

    class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

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
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(sContext).inflate(R.layout.playlist_track_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PlaylistTrack playlistTrack = sTracks.get(position);
            holder.name.setText(playlistTrack.track.name);
            holder.artist.setText(playlistTrack.track.artists.get(0).name);
        }

        @Override
        public int getItemCount() {
            return null == sTracks ? 0 : sTracks.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView name;
            public TextView artist;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                artist = (TextView) itemView.findViewById(R.id.artist);
            }
        }
    }

}
