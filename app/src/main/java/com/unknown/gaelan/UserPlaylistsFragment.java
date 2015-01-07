package com.unknown.gaelan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.malinskiy.superrecyclerview.SwipeDismissRecyclerViewTouchListener;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Gaelan on 1/4/2015.
 */
public class UserPlaylistsFragment extends Fragment {

    private static final String TAG = "com.unknown.gaelan.UserPlaylistsFragment";
    private SuperRecyclerView mRecyclerView;
    private List<Playlist> mPlaylists;
    private UserPlaylistsAdapter mPlaylistsAdapter;

    public static UserPlaylistsFragment newInstance() {
        UserPlaylistsFragment fragment = new UserPlaylistsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserPlaylists();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_user_playlists, container, false);
        mRecyclerView = (SuperRecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(getLayoutManager());
        mRecyclerView.setupSwipeToDismiss(new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {

            public Playlist mRemovedPlaylist;

            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                for (final int position : reverseSortedPositions) {
                    mRemovedPlaylist = mPlaylistsAdapter.removeViaSwipe(position);
                    SnackbarManager.show(Snackbar.with(getActivity()).text("Playlist deleted").actionLabel("UNDO")
                            .actionColor(getResources().getColor(android.R.color.holo_orange_light))
                            .actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked(Snackbar snackbar) {
                                    mPlaylistsAdapter.insertViaUndo(position, mRemovedPlaylist);
                                }
                            }).duration(Snackbar.SnackbarDuration.LENGTH_LONG));
                }
            }
        });
        mRecyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserPlaylists();
            }
        });
        mRecyclerView.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light,
                android.R.color.holo_green_light, android.R.color.holo_red_light);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.card_item_padding)));
        return view;
    }

    private void getUserPlaylists() {
        PlaylistMiner.getSpotifyService(getActivity()).getPlaylists(PlaylistMiner.getUser().id, new Callback<Pager<Playlist>>() {
            @Override
            public void success(Pager<Playlist> playlistPager, Response response) {
                mPlaylists = playlistPager.items;
                mPlaylistsAdapter = new UserPlaylistsAdapter(getActivity(), new ItemClickSupport() {
                    @Override
                    public void onItemClicked(int position) {
                        Playlist playlist = mPlaylists.get(position);
                        PlaylistDetailsActivity.start(getActivity(), playlist);
                    }

                    @Override
                    public boolean onItemLongClicked(int position) {
                        return false;
                    }
                }, mPlaylists);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.getSwipeToRefresh().setRefreshing(false);
                        mRecyclerView.setAdapter(mPlaylistsAdapter);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

}
