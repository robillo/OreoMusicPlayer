package com.robillo.oreomusicplayer.views.activities.main.song_list_frag.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

    private List<Song> list;
    private Context context;
    @SuppressWarnings("FieldCanBeLocal")
    private Context parentContext;

    public SongsAdapter(List<Song> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        parentContext = parent.getContext();
        return new SongHolder(LayoutInflater.from(parentContext).inflate(R.layout.row_song, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SongHolder holder, int position) {

        if(list.get(position).getTitle() == null) {
            String empty = " ";
            holder.title.setText(empty);
            holder.artistDuration.setText(empty);
            holder.itemView.setClickable(false);
            return;
        }

        holder.title.setText(list.get(position).getTitle());
        long duration = Integer.valueOf(list.get(position).getDuration())/1000;
        long mins = duration/60;
        long seconds = duration%60;
        String temp = list.get(position).getArtist() + " ( " + String.valueOf(mins) + ":" + String.valueOf(seconds) + " )";
        holder.artistDuration.setText(temp);

        final int _pos = position;

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context!=null){
                    ((MainActivity) context).playSong(_pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list!=null ? list.size() : 0;
    }

    class SongHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.artist_duration)
        TextView artistDuration;
        @BindView(R.id.song_card)
        CardView songCard;
        @BindView(R.id.linear_layout)
        LinearLayout linearLayout;

        SongHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            title.setSelected(true);
        }
    }
}
