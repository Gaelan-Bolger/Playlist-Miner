package com.unknown.gaelan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;

/**
 * Created by Gaelan on 1/3/2015.
 */
public class RankedTracksFragment extends Fragment {

    private ArrayList<RankedTrack> mRankedTracks;
    private RankedTrackAdapter mRankedTrackAdapter;
    private TwoWayView mRecyclerView;

    public static RankedTracksFragment newInstance(ArrayList<RankedTrack> rankedTracks) {
        RankedTracksFragment fragment = new RankedTracksFragment();
        fragment.setRankedTracks(rankedTracks);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRankedTrackAdapter = new RankedTrackAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_ranked_tracks, container, false);
        mRecyclerView = (TwoWayView) view.findViewById(R.id.two_way_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mRankedTrackAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRankedTrackAdapter.setTracks(mRankedTracks);
    }

    private void setRankedTracks(ArrayList<RankedTrack> rankedTracks) {
        mRankedTracks = rankedTracks;
    }
}
