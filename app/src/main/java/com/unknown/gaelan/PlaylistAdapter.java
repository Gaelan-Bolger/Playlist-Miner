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
import kaaes.spotify.webapi.android.models.Playlist;

/**
 * Created by Gaelan on 1/4/2015.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final Context sContext;
    private List<Playlist> sPlaylists;

    public PlaylistAdapter(Context context) {
        this(context, null);
    }

    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        this.sContext = context;
        this.sPlaylists = playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
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
        Playlist playlist = sPlaylists.get(position);
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

    public Playlist removeViaSwipe(int position) {
        Playlist playlist = sPlaylists.remove(position);
        notifyDataSetChanged();
        return playlist;
    }

    public void insertViaUndo(int position, Playlist playlist) {
        sPlaylists.add(position, playlist);
        notifyDataSetChanged();
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
