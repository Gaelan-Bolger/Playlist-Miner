package com.unknown.gaelan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchResultsActivity extends ActionBarActivity {

    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_MAX_PLAYLISTS = "max_playlists";
    public static final String EXTRA_MIN_ALLOWED_TRACK_COUNT = "min_allowed_track_count";
    public static final String EXTRA_MAX_ALLOWED_TRACK_COUNT = "max_allowed_track_count";
    private static final String TAG = "SearchResultsActivity";

    private Handler mHandler = new Handler();
    private ProgressDialog mProgress;
    private String mSearchQuery;
    private ArrayList<PlaylistSimple> mPlaylists;
    private PlaylistSimpleAdapter mPlaylistSimpleAdapter;
    private Runnable mDisplayPlaylistsRunnable = new Runnable() {
        @Override
        public void run() {
            hideProgressDialog();
            mPlaylistSimpleAdapter = new PlaylistSimpleAdapter(SearchResultsActivity.this, new ItemClickSupport() {
                @Override
                public void onItemClicked(int position) {
                    PlaylistSimple playlist = mPlaylists.get(position);
                    PlaylistDetailsActivity.start(SearchResultsActivity.this, playlist);
                }

                @Override
                public boolean onItemLongClicked(int position) {
                    return false;
                }
            }, mPlaylists);
            mRecyclerView.setAdapter(mPlaylistSimpleAdapter);
        }
    };
    private SuperRecyclerView mRecyclerView;
    private PlaylistsParserService mService;
    private ServiceConnection mServiceConnection;
    private boolean mBound;
    private int mMinAllowedTrackCount;
    private int mMaxAllowedTrackCount;
    private int mMaxPlaylists;
    private Runnable mHideProgressRunnable = new Runnable() {
        @Override
        public void run() {
            hideProgressDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchQuery = getIntent().getExtras().getString(EXTRA_SEARCH_QUERY);
        mMaxPlaylists = getIntent().getExtras().getInt(EXTRA_MAX_PLAYLISTS, -1);
        mMinAllowedTrackCount = getIntent().getExtras().getInt(EXTRA_MIN_ALLOWED_TRACK_COUNT);
        mMaxAllowedTrackCount = getIntent().getExtras().getInt(EXTRA_MAX_ALLOWED_TRACK_COUNT);
        if (TextUtils.isEmpty(mSearchQuery) || mMaxPlaylists == -1) {
            finish();
            return;
        }

        PrefsHelper.putRecentSearch(this, mSearchQuery);

        setContentView(R.layout.activity_search_results);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("'" + mSearchQuery + "'");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showProgressDialog("Searching playlists");
        performPlaylistSearch(mSearchQuery);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        findViewById(R.id.btn_find_top_tracks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                showProgressDialog("Parsing playlists");
                parsePlaylists();
            }
        });
        mRecyclerView = (SuperRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(getLayoutManager());
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.card_item_padding)));
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
                if (null == mPlaylists) {
                    mPlaylists = new ArrayList<>(playlistsPager.playlists.total);
                }
                List<PlaylistSimple> playlists = playlistsPager.playlists.items;
                for (int i = 0; i < playlists.size(); i++) {
                    PlaylistSimple playlist = playlists.get(i);
                    int total = playlist.tracks.total;
                    if (total >= mMinAllowedTrackCount && total <= mMaxAllowedTrackCount)
                        mPlaylists.add(playlist);
                    if (mPlaylists.size() >= mMaxPlaylists) {
                        mHandler.post(mDisplayPlaylistsRunnable);
                        return;
                    }
                }
                int newOffset = offset + playlists.size();
                if (newOffset >= playlistsPager.playlists.total) {
                    mHandler.post(mDisplayPlaylistsRunnable);
                } else {
                    performPlaylistSearch(searchString, newOffset);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Error searching playlists, " + error.toString());
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
                        showAlert("Tracks ranked: " + rankedTracks.size() + "\nAverage rank: " + getAverageRank(rankedTracks));
                        showRankedTracksFragment(rankedTracks);
                    }

                    private float getAverageRank(ArrayList<RankedTrack> rankedTracks) {
                        float total = 0f;
                        int trackCount = rankedTracks.size();
                        for (int i = 0; i < trackCount; i++) {
                            total += rankedTracks.get(i).rank;
                        }
                        return total / trackCount;
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
        RankedTracksFragment fragment = RankedTracksFragment.newInstance(mSearchQuery, rankedTracks);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "ranked_tracks").commit();
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(this);
    }

}
