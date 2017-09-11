package com.robillo.oreomusicplayer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.adapters.holders.SongHolder;
import com.robillo.oreomusicplayer.models.Song;

import java.util.List;

/**
 * Created by robinkamboj on 11/09/17.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongHolder>{

    private List<Song> list;
    @SuppressWarnings("FieldCanBeLocal")
    private Context context;

    public SongsAdapter(List<Song> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_song, parent, false);
        return new SongHolder(v);
    }

    @Override
    public void onBindViewHolder(SongHolder holder, int position) {
        holder.title.setText(list.get(position).getTitle());
        holder.artistDuration.setText(list.get(position).getArtist() + " (" + list.get(position).getDuration() + ")");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
