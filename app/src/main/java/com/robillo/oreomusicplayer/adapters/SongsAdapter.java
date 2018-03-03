package com.robillo.oreomusicplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by robinkamboj on 11/09/17.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongHolder> {

    private List<Song> list;
    private MediaPlayer mediaPlayer;
    private Context context;
    private Context parentContext;
    private Uri uri;

    public SongsAdapter(List<Song> list, Context context, MediaPlayer mediaPlayer) {
        this.list = list;
        this.context = context;
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentContext = parent.getContext();
        return new SongHolder(LayoutInflater.from(parentContext).inflate(R.layout.row_song, parent, false));
    }

    @Override
    public void onBindViewHolder(SongHolder holder, int position) {

        if(position == 0) {
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

        holder.songCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(list.get(_pos).getId()));

                if(mediaPlayer!=null){

                    if(context!=null) ((MainActivity) context).setSongPlayFragment();

                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(parentContext, uri);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list!=null?list.size():0;
    }

    class SongHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.artist_duration)
        TextView artistDuration;
        @BindView(R.id.song_card)
        CardView songCard;

        SongHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
