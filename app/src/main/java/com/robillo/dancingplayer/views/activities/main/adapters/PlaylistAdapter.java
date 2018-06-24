package com.robillo.dancingplayer.views.activities.main.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.PlaylistRowItem;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("unused")
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder>{

    private List<PlaylistRowItem> list;
    private Context pContext;
    private int from;
    private String songId;

    public PlaylistAdapter(List<PlaylistRowItem> list, Context pContext, int from, String songId) {
        this.list = list;
        this.pContext = pContext;
        this.from = from;
        this.songId = songId;
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

        if(from != AppConstants.FROM_SONGS_LIST) {
            if(list.get(position).isPersistent()) {
                holder.delete_playlist.setVisibility(View.GONE);
                holder.edit_name.setVisibility(View.GONE);
            }
            else {
                holder.delete_playlist.setVisibility(View.VISIBLE);
                holder.edit_name.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.delete_playlist.setVisibility(View.GONE);
            holder.edit_name.setVisibility(View.GONE);

            if(list.get(position).isPersistent()) {
                holder.blank_view.setVisibility(View.GONE);
                holder.add_to_this_playlist.setVisibility(View.GONE);
                holder.playlist_title.setTextColor(pContext.getResources().getColor(R.color.colorTextThree));
            }
            else {
                holder.blank_view.setVisibility(View.VISIBLE);
                holder.add_to_this_playlist.setVisibility(View.VISIBLE);
                holder.playlist_title.setTextColor(pContext.getResources().getColor(R.color.colorTextOne));
            }
        }

        holder.playlist_title.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) pContext;
            if(activity != null) {
                handlePlaylistClick(activity, from, position, songId);
            }
        });

        holder.blank_view.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) pContext;
            if(activity != null) {
                handlePlaylistClick(activity, from, position, songId);
            }
        });

        holder.edit_name.setOnClickListener(v -> {
            ((MainActivity) pContext)
                    .showEditCreateDialogFragment(AppConstants.EDIT_PLAYLIST_NAME, position, list.get(position).getTitle());
        });

        holder.delete_playlist.setOnClickListener(v -> {
            ((MainActivity) pContext)
                    .handleDeletePlaylist(list.get(position).getTitle());
        });

        holder.add_to_this_playlist.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) pContext;
            if(activity != null) {
                handlePlaylistClick(activity, from, position, songId);
            }
        });
    }

    private void handlePlaylistClick(MainActivity activity, int from, int position, String songId) {
        if(from != AppConstants.FROM_SONGS_LIST) {
            activity.updatePlaylistListForSelectedItem(list.get(position), position);
            Log.e("tag", "update playlist");
        }
        else {
            if(list.get(position).isPersistent()) {
                Toast.makeText(pContext, "You can only modify playlists that you created", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.e("tag", "add song");
                activity.addSongToPlaylist(songId, list.get(position).getTitle());
            }
        }
    }

    public void addItem(PlaylistRowItem item) {
        list.add(item);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class PlaylistHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.background_view)
        LinearLayout background_view;

        @BindView(R.id.add_to_this_playlist)
        ImageView add_to_this_playlist;

        @BindView(R.id.blank_view)
        View blank_view;

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
