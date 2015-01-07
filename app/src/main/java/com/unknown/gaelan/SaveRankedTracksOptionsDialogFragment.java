package com.unknown.gaelan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Gaelan on 1/3/2015.
 */
public class SaveRankedTracksOptionsDialogFragment extends DialogFragment {

    private static final java.lang.String ARG_HIGHEST_RANK = "highest_rank";
    private static final java.lang.String ARG_SEARCH_QUERY = "search_query";

    private SaveRankedTracksOptionsListener mListener;
    private int mHighestRank;
    private String mSearchQuery;

    public static SaveRankedTracksOptionsDialogFragment newInstance(String searchQuery, int highestRank, SaveRankedTracksOptionsListener listener) {
        SaveRankedTracksOptionsDialogFragment fragment = new SaveRankedTracksOptionsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_HIGHEST_RANK, highestRank);
        bundle.putString(ARG_SEARCH_QUERY, searchQuery);
        fragment.setArguments(bundle);
        fragment.setListener(listener);
        return fragment;
    }

    private void setListener(SaveRankedTracksOptionsListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHighestRank = getArguments().getInt(ARG_HIGHEST_RANK, 0);
        mSearchQuery = getArguments().getString(ARG_SEARCH_QUERY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_save_ranked_tracks_options, null);
        final EditText etPlaylistName = (EditText) view.findViewById(R.id.et_playlist_name);
        etPlaylistName.setHint(mSearchQuery);
        final TextView tvMinimumRankValue = (TextView) view.findViewById(R.id.tv_minimum_rank_value);
        tvMinimumRankValue.setText(String.valueOf(1));
        final SeekBar sbMinimumRank = (SeekBar) view.findViewById(R.id.sb_minimum_rank);
        sbMinimumRank.setMax(mHighestRank - 1);
        sbMinimumRank.setProgress(0);
        sbMinimumRank.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMinimumRankValue.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setCancelable(true).setTitle("Save as playlist").setView(view).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mListener.onCancelled();
            }
        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mListener.onConfirmed(etPlaylistName.getText().toString(), sbMinimumRank.getProgress() + 1);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                mListener.onCancelled();
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public interface SaveRankedTracksOptionsListener {
        public void onConfirmed(String playlistName, int minimumRank);

        public void onCancelled();
    }
}
