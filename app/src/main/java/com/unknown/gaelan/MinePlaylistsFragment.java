package com.unknown.gaelan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;

public class MinePlaylistsFragment extends Fragment {

    private EditText etSearch;
    private TextView tvMaxPlaylists;
    private RangeBar rbMaxPlaylists;
    private TextView tvMinTrackCount;
    private TextView tvMaxTrackCount;
    private RangeBar rbTrackCount;
    private int mMaxPlaylists = 50;
    private int mMinTrackCount = 5;
    private int mMaxTrackCount = 100;


    public static MinePlaylistsFragment newInstance() {
        return new MinePlaylistsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine_playlists, container, false);

        view.findViewById(R.id.iv_learn_keywords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle("Keywords").setMessage("Terms to match in Spotify playlists.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        etSearch = (EditText) view.findViewById(R.id.et_search);

        view.findViewById(R.id.iv_learn_playlists).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle("Playlists").setMessage("Set the maximum number of playlists to mine.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        tvMaxPlaylists = (TextView) view.findViewById(R.id.tv_max_playlists);
        rbMaxPlaylists = (RangeBar) view.findViewById(R.id.rb_max_playlists);
        rbMaxPlaylists.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                tvMaxPlaylists.setText("" + (mMaxPlaylists = Integer.parseInt(rightPinValue)));
            }
        });
        rbMaxPlaylists.setRangePinsByValue(5.0f, mMaxPlaylists);

        view.findViewById(R.id.iv_learn_playlist_tracks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle("Playlist tracks").setMessage("Set the minimum and maximum number of tracks required per mined playlists.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        tvMinTrackCount = (TextView) view.findViewById(R.id.tv_min_track_count);
        tvMaxTrackCount = (TextView) view.findViewById(R.id.tv_max_track_count);
        rbTrackCount = (RangeBar) view.findViewById(R.id.rb_playlist_tracks);
        rbTrackCount.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                tvMinTrackCount.setText("" + (mMinTrackCount = Integer.parseInt(leftPinValue)));
                tvMaxTrackCount.setText("" + (mMaxTrackCount = Integer.parseInt(rightPinValue)));
            }
        });
        rbTrackCount.setRangePinsByValue(mMinTrackCount, mMaxTrackCount);

        view.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = etSearch.getText().toString();
                if (searchQuery.length() == 0) {
                    etSearch.setError("Required");
                    return;
                }
                etSearch.setError(null);
                Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
                intent.putExtra(SearchResultsActivity.EXTRA_SEARCH_QUERY, searchQuery);
                intent.putExtra(SearchResultsActivity.EXTRA_MAX_PLAYLISTS, mMaxPlaylists);
                intent.putExtra(SearchResultsActivity.EXTRA_MIN_ALLOWED_TRACK_COUNT, mMinTrackCount);
                intent.putExtra(SearchResultsActivity.EXTRA_MAX_ALLOWED_TRACK_COUNT, mMaxTrackCount);
                startActivity(intent);
            }
        });
        return view;
    }

}