package com.unknown.gaelan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchResultsActivity extends ActionBarActivity {

    public static final String EXTRA_SEARCH_QUERY = "search_query";
    private static final String TAG = "SearchResultsActivity";
    private static final int MAX_SEARCH_RESULTS = 100;
    private static int MAX_ALLOWED_TRACK_COUNT = 100;

    private Handler mHandler = new Handler();
    private ProgressDialog mProgress;
    private String mSearchQuery;
    private ArrayList<PlaylistSimple> mPlaylists;
    private Runnable mDisplayPlaylistsRunnable = new Runnable() {
        @Override
        public void run() {
            hideProgressDialog();
            mPlaylistsAdapter.setPlaylists(mPlaylists);
        }
    };
    private PlaylistsAdapter mPlaylistsAdapter;
    private TextView btnFindTopTracks;
    private TwoWayView mRecyclerView;
    private Runnable mHideProgressRunnable = new Runnable() {
        @Override
        public void run() {
            hideProgressDialog();
        }
    };
    private PlaylistsParserService mService;
    private ServiceConnection mServiceConnection;
    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchQuery = getIntent().getExtras().getString(EXTRA_SEARCH_QUERY);
        if (TextUtils.isEmpty(mSearchQuery)) {
            finish();
            return;
        }

        PrefsHelper.putRecent(this, mSearchQuery);
        MAX_ALLOWED_TRACK_COUNT = PrefsHelper.getMaxAllowedTrackCount(this);

        getSupportActionBar().setTitle("'" + mSearchQuery + "'");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_search_results);

        showProgressDialog("Searching playlists");
        performPlaylistSearch(mSearchQuery);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        btnFindTopTracks = (TextView) findViewById(R.id.btn_find_top_tracks);
        btnFindTopTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFindTopTracks.setEnabled(false);
                showProgressDialog("Parsing playlists");
                parsePlaylists();
            }
        });
        mRecyclerView = (TwoWayView) findViewById(R.id.two_way_view);
        mRecyclerView.setHasFixedSize(true);
        final ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecyclerView);
        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
                PlaylistSimple playlist = mPlaylists.get(i);
                Intent intent = new Intent(SearchResultsActivity.this, PlaylistDetailsActivity.class);
                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_OWNER_ID, playlist.owner.id);
                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_ID, playlist.id);
                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_NAME, playlist.name);
                startActivity(intent);
            }
        });
        final Drawable divider = getResources().getDrawable(R.drawable.divider_thin_opaque);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        mPlaylistsAdapter = new PlaylistsAdapter(this);
        mRecyclerView.setAdapter(mPlaylistsAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressDialog(String msg) {
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(msg);
        mProgress.show();
    }

    private void updateProgress(String msg) {
        if (null != mProgress)
            mProgress.setMessage(msg);
    }

    private void hideProgressDialog() {
        if (null != mProgress)
            mProgress.dismiss();
    }

    private void showAlert(String msg) {
        hideProgressDialog();
        new AlertDialog.Builder(this).setMessage(msg).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private void performPlaylistSearch(String searchString) {
        performPlaylistSearch(searchString, 0);
    }

    private void performPlaylistSearch(final String searchString, final int offset) {
        PlaylistMiner.getSpotifyService(this).searchPlaylists(searchString, offset, 20, new Callback<PlaylistsPager>() {

            @Override
            public void success(final PlaylistsPager playlistsPager, Response response) {
                Log.d("Success", response.toString());
                if (null == mPlaylists) {
                    mPlaylists = new ArrayList<>(playlistsPager.playlists.total);
                }
                List<PlaylistSimple> items = playlistsPager.playlists.items;
                for (int i = 0; i < items.size(); i++) {
                    PlaylistSimple playlist = items.get(i);
                    if (playlist.tracks.total <= MAX_ALLOWED_TRACK_COUNT) {
                        mPlaylists.add(playlist);
                    }
                }
                int newOffset = offset + items.size();
                if (newOffset >= MAX_SEARCH_RESULTS || newOffset >= playlistsPager.playlists.total) {
                    mHandler.post(mDisplayPlaylistsRunnable);
                } else {
                    performPlaylistSearch(searchString, newOffset);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Failure", error.toString());
                mHandler.post(mHideProgressRunnable);
            }
        });
    }

    private void parsePlaylists() {
        if (mBound)
            unbindService(mServiceConnection);
        mBound = false;
        Intent intent = new Intent(this, PlaylistsParserService.class);
        bindService(intent, mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PlaylistsParserService.LocalBinder binder = (PlaylistsParserService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                mService.parsePlaylists(mPlaylists, new PlaylistsParserService.PlaylistsParserCallback() {

                    @Override
                    public void onParsingPlaylist(String playlistName, int playlistPosition) {
                        Log.d(TAG, "Parsing playlist " + playlistPosition + " of " + mPlaylists.size());
                        updateProgress("Parsing playlist " + playlistPosition + " of " + mPlaylists.size());
                    }

                    @Override
                    public void onPlaylistsParsed(ArrayList<RankedTrack> rankedTracks) {
                        unbindService();
                        Log.d(TAG, "Tracks ranked = " + rankedTracks.size());
                        showAlert("Tracks ranked = " + rankedTracks.size());
                        showRankedTracksFragment(rankedTracks);
                    }

                    @Override
                    public void onErrorParsingPlaylists(RetrofitError error) {
                        unbindService();
                        Log.d(TAG, "Error parsing playlists, " + error.getMessage());
                        showAlert("Error parsing playlists, " + error.getMessage());
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
            }

        }, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        unbindService(mServiceConnection);
        mBound = false;
    }

    private void showRankedTracksFragment(ArrayList<RankedTrack> rankedTracks) {
        RankedTracksFragment fragment = RankedTracksFragment.newInstance(rankedTracks);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "ranked_tracks").commit();
    }

    class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

        private final Context sContext;
        private List<PlaylistSimple> sPlaylists;

        public PlaylistsAdapter(Context context) {
            this.sContext = context;
        }

        public void setPlaylists(List<PlaylistSimple> playlists) {
            this.sPlaylists = playlists;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(sContext).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PlaylistSimple playlist = mPlaylists.get(position);
            holder.name.setText(playlist.name);
            holder.trackCount.setText(playlist.tracks.total + " tracks");
        }

        @Override
        public int getItemCount() {
            return null == sPlaylists ? 0 : sPlaylists.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView name;
            public TextView trackCount;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(android.R.id.text1);
                trackCount = (TextView) itemView.findViewById(android.R.id.text2);
            }
        }
    }

}
