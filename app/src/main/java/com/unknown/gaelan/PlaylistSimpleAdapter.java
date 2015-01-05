package com.unknown.gaelan;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * Created by Gaelan on 1/4/2015.
 */
public class PlaylistSimpleAdapter extends RecyclerView.Adapter<PlaylistSimpleAdapter.ViewHolder> {

    private final Context sContext;
    private List<PlaylistSimple> sPlaylists;

    public PlaylistSimpleAdapter(Context context) {
        this(context, null);
    }

    public PlaylistSimpleAdapter(Context context, List<PlaylistSimple> playlists) {
        this.sContext = context;
        this.sPlaylists = playlists;
    }

    public void setPlaylists(List<PlaylistSimple> playlists) {
        this.sPlaylists = playlists;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(sContext).inflate(R.layout.playlist_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlaylistSimple playlist = sPlaylists.get(position);
        List<Image> images = playlist.images;
        if (null != images && images.size() > 0)
            Picasso.with(sContext).load(images.get(0).url).resize(192, 192).into(holder.image);
        else
            holder.image.setImageResource(R.drawable.ic_launcher);
        holder.name.setText(playlist.name);
        holder.trackCount.setText(playlist.tracks.total + " tracks");
    }

    @Override
    public int getItemCount() {
        return null == sPlaylists ? 0 : sPlaylists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView trackCount;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            trackCount = (TextView) itemView.findViewById(R.id.track_count);
        }
    }
}
