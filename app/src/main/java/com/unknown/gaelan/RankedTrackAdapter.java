package com.unknown.gaelan;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unknown.gaelan.R;

import java.util.List;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class RankedTrackAdapter extends RecyclerView.Adapter<RankedTrackAdapter.ViewHolder> {

    private List<RankedTrack> mTracks;
    private Context mContext;

    public RankedTrackAdapter(Context context) {
        mContext = context;
    }

    public void setTracks(List<RankedTrack> tracks) {
        mTracks = tracks;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return null == mTracks ? 0 : mTracks.size();
    }

    @Override
    public RankedTrackAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ranked_track_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RankedTrackAdapter.ViewHolder viewHolder, int i) {
        RankedTrack track = mTracks.get(i);
        viewHolder.rank.setText("" + track.rank);
        viewHolder.name.setText(track.track.name);
        viewHolder.artist.setText(track.track.artists.get(0).name);
        viewHolder.duration.setText("" + track.track.duration_ms);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView rank;
        public TextView name;
        public TextView artist;
        public TextView duration;

        public ViewHolder(View itemView) {
            super(itemView);
            rank = (TextView) itemView.findViewById(R.id.rank);
            name = (TextView) itemView.findViewById(R.id.name);
            artist = (TextView) itemView.findViewById(R.id.artist);
            duration = (TextView) itemView.findViewById(R.id.duration);
        }
    }
}
