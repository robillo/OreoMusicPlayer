package com.robillo.dancingplayer.views.activities.main.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.PlaylistRowItem;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("unused")
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder>{

    private List<PlaylistRowItem> list;
    private Context pContext;

    public PlaylistAdapter(List<PlaylistRowItem> list, Context pContext) {
        this.list = list;
        this.pContext = pContext;
    }

    @NonNull
    @Override
    public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        pContext = parent.getContext();
        return new PlaylistHolder(
                LayoutInflater.from(pContext).inflate(R.layout.row_playlist_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistHolder holder, int position) {
        holder.playlist_title.setText(list.get(position).getTitle());
        if(list.get(position).isPersistent()) {
            holder.delete_playlist.setVisibility(View.GONE);
            holder.edit_name.setVisibility(View.GONE);
        }
        else {
            holder.delete_playlist.setVisibility(View.VISIBLE);
            holder.edit_name.setVisibility(View.VISIBLE);
        }

        holder.playlist_title.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) pContext;
            if(activity != null) activity.updatePlaylistListForSelectedItem(list.get(position), position);
        });

        holder.edit_name.setOnClickListener(v -> {
            Toast.makeText(pContext, "edit playlist name", Toast.LENGTH_SHORT).show();
        });

        holder.delete_playlist.setOnClickListener(v -> {
            Toast.makeText(pContext, "delete playlist", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class PlaylistHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.edit_name)
        ImageView edit_name;

        @BindView(R.id.delete_playlist)
        ImageView delete_playlist;

        @BindView(R.id.playlist_title)
        TextView playlist_title;

        PlaylistHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
