package com.robillo.dancingplayer.views.activities.main.song_list_frag.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.utils.ApplicationUtils;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

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
    public void onBindViewHolder(@NonNull SongHolder holder, @SuppressLint("RecyclerView") final int position) {

        //noinspection UnnecessaryLocalVariable
        final int _pos = position;

        if(list.get(_pos) == null || (list.get(_pos).getTitle() == null && list.get(_pos).getArtist() == null)) {
            holder.moreButton.setVisibility(View.GONE);

            String empty = "";
            holder.title.setText(empty);
            holder.artistDuration.setText(empty);
            holder.itemView.setClickable(false);
        }
        else {
            holder.moreButton.setVisibility(View.VISIBLE);

            holder.title.setText(list.get(_pos).getTitle());
            long duration = 0;
            try {
                duration = Integer.valueOf(list.get(_pos).getDuration())/1000;
            }
            catch (NumberFormatException e) {
                Log.e("exception", "number format exception");
            }
            String temp = list.get(_pos).getArtist() + " (" + new ApplicationUtils().formatStringOutOfSeconds((int) duration) + ")";
            holder.artistDuration.setText(temp);

            holder.linearLayout.setOnClickListener(v -> {
                if(context!=null){
                    ((MainActivity) context).playSong(_pos);
                }
            });

            holder.linearLayout.setOnLongClickListener(view -> {
                ((MainActivity) context).showSongOptionsOnBottomSheet(list.get(_pos), _pos);
                return true;
            });

            holder.moreButton.setOnClickListener(view -> ((MainActivity) context).showSongOptionsOnBottomSheet(list.get(_pos), _pos));
        }
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
        @BindView(R.id.more)
        ImageButton moreButton;

        SongHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
