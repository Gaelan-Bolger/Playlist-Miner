package com.unknown.gaelan;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.malinskiy.superrecyclerview.SwipeDismissRecyclerViewTouchListener;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Gaelan on 1/4/2015.
 */
public class UserPlaylistsFragment extends Fragment {

    private static final String TAG = "com.unknown.gaelan.UserPlaylistsFragment";
    private User mUser;
    private SuperRecyclerView mRecyclerView;
    private List<Playlist> mPlaylists;
    private PlaylistAdapter mPlaylistsAdapter;

    public static UserPlaylistsFragment newInstance(User user) {
        UserPlaylistsFragment fragment = new UserPlaylistsFragment();
        fragment.setUser(user);
        return fragment;
    }

    private void setUser(User user) {
        mUser = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserPlaylists();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_user_playlists, container, false);
        mRecyclerView = (SuperRecyclerView) view.findViewById(R.id.two_way_view);
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
                    SnackbarManager.show(Snackbar.with(getActivity()).text("Playlist deleted").actionLabel("UNDO").actionListener(new ActionClickListener() {
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
        mRecyclerView.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);

//        mRecyclerView.setHasFixedSize(true);
//        final ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecyclerView);
//        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//            @Override
//            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
//                Intent intent = new Intent(getActivity(), PlaylistDetailsActivity.class);
//                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_OWNER_ID, mUser.id);
//                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_ID, mPlaylists.get(i).id);
//                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_NAME, mPlaylists.get(i).name);
//                startActivity(intent);
//            }
//        });
//        itemClickSupport.setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(RecyclerView recyclerView, View view, int i, long l) {
//                return false;
//            }
//        });
//        final Drawable divider = getResources().getDrawable(R.drawable.divider_thin_opaque);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        return view;
    }

    private void getUserPlaylists() {
        PlaylistMiner.getSpotifyService(getActivity()).getPlaylists(mUser.id, new Callback<Pager<Playlist>>() {
            @Override
            public void success(Pager<Playlist> playlistPager, Response response) {
                mPlaylists = playlistPager.items;
                mPlaylistsAdapter = new PlaylistAdapter(getActivity(), mPlaylists);
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
