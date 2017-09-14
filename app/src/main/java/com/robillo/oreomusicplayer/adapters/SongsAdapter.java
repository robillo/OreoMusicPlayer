package com.robillo.oreomusicplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.adapters.holders.SongHolder;
import com.robillo.oreomusicplayer.models.Song;

import java.io.IOException;
import java.util.List;

/**
 * Created by robinkamboj on 11/09/17.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongHolder> implements View.OnClickListener{

    private List<Song> list;
    @SuppressWarnings("FieldCanBeLocal")
    private Context context;
    private Uri uri;

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
        long duration = Integer.valueOf(list.get(position).getDuration())/1000;
        long mins = duration/60;
        long seconds = duration%60;
        holder.artistDuration.setText(list.get(position).getArtist() + " (" + String.valueOf(mins) + ":" + String.valueOf(seconds) + ")");
        uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(list.get(position).getId()));
        holder.songCard.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return list!=null?list.size():0;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.song_card:{
                Log.e("CLICKED", uri.toString());
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(context, uri);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                break;
            }
        }
    }
}
