package com.unknown.gaelan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.SnapshotId;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Gaelan on 1/3/2015.
 */
public class RankedTracksFragment extends Fragment {

    private static final String TAG = "RankedTracksFragment";
    private static final String ARG_SEARCH_QUERY = "ARG_SEARCH_QUERY";

    private ArrayList<RankedTrack> mRankedTracks;
    private RankedTrackAdapter mRankedTrackAdapter;
    private TwoWayView mRecyclerView;
    private String mSearchQuery;

    public static RankedTracksFragment newInstance(String searchQuery, ArrayList<RankedTrack> rankedTracks) {
        RankedTracksFragment fragment = new RankedTracksFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SEARCH_QUERY, searchQuery);
        fragment.setArguments(bundle);
        fragment.setRankedTracks(rankedTracks);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSearchQuery = getArguments().getString(ARG_SEARCH_QUERY);
        mRankedTrackAdapter = new RankedTrackAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_ranked_tracks, container, false);
        mRecyclerView = (TwoWayView) view.findViewById(R.id.two_way_view);
        mRecyclerView.setHasFixedSize(true);
        final ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecyclerView);
        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
                PlaylistMiner.getPlayer(getActivity()).play(mRankedTracks.get(i).track.uri);
            }
        });
        final Drawable divider = getResources().getDrawable(R.drawable.divider_thin_opaque);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        mRecyclerView.setAdapter(mRankedTrackAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRankedTrackAdapter.setTracks(mRankedTracks);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                onSaveAsPlaylist();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        PlaylistMiner.getPlayer(getActivity()).pause();
        super.onStop();
    }

    private void setRankedTracks(ArrayList<RankedTrack> rankedTracks) {
        mRankedTracks = rankedTracks;
    }

    private void onSaveAsPlaylist() {
        SaveRankedTracksOptionsDialogFragment.newInstance(mSearchQuery, getHighestRank(), new SaveRankedTracksOptionsDialogFragment.SaveRankedTracksOptionsListener() {
            @Override
            public void onConfirmed(String playlistName, int minimumRank) {
                saveAsPlaylist(playlistName, minimumRank);
            }

            @Override
            public void onCancelled() {

            }
        }).show(getFragmentManager(), "save_options");
    }

    private int getHighestRank() {
        return mRankedTracks.get(0).rank;
    }

    private void saveAsPlaylist(final String playlistName, final int minimumRank) {
        PlaylistMiner.getSpotifyService(getActivity()).getMe(new Callback<User>() {
            @Override
            public void success(final User user, Response response) {
                SpotifyHelper.createPlaylist(getActivity(), user, playlistName, false, new BasicResponseHandler() {

                    @Override
                    public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
                        String json = EntityUtils.toString(response.getEntity());
                        JsonObject responseData = new Gson().fromJson(json, JsonObject.class);
                        final String playlistId = responseData.get("id").getAsString();
                        String trackUris = SpotifyHelper.getRankedTrackUris(mRankedTracks, minimumRank);
                        SpotifyHelper.addTracksToPlaylist(getActivity(), user, playlistName, playlistId, trackUris, new Callback<SnapshotId>() {
                            @Override
                            public void success(SnapshotId snapshotId, Response response) {
                                Log.d(TAG, "Playlist created, SnapshotId = " + snapshotId);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(getActivity()).setTitle("Playlist '" + playlistName + "' created")
                                                .setMessage("Your new playlist can now be found in your Spotify library")
                                                .setNegativeButton("Show playlist", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent(getActivity(), PlaylistDetailsActivity.class);
                                                        intent
                                                                .putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_OWNER_ID, user.id);
                                                        intent
                                                                .putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_ID, playlistId);
                                                        intent
                                                                .putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_NAME, playlistName);
                                                        getActivity().startActivity(intent);
                                                        getActivity().finish();
                                                    }
                                                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                getActivity().finish();
                                            }
                                        }).create().show();
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

            }
        });
    }

}
