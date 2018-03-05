package com.robillo.oreomusicplayer.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.robillo.oreomusicplayer.events.SongChangeEvent;
import com.robillo.oreomusicplayer.models.Song;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by robinkamboj on 04/03/18.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicServiceInterface {

    private final int EMPTY_CELLS_COUNT = 2;

    private final IBinder musicBind = new MusicBinder();
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosition;

    @Override
    public void onCreate() {
        //create the service
        super.onCreate();

        //initialize position
        songPosition =0;

        //create player
        player = new MediaPlayer();

        initMusicPlayer();
    }

    @Override
    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public void setList(ArrayList<Song> songsList) {
        songs = songsList;
    }

    @Override
    public void playSong() {
        player.reset();
        //get song
        Song song = songs.get(songPosition);
        //get id
        long currSong = Long.valueOf(song.getId());
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void setSong(int songIndex){
        if(songIndex < songs.size() - EMPTY_CELLS_COUNT && songIndex >= 2) {
            Log.e("message", songs.get(songIndex).getTitle());
            EventBus.getDefault().post(new SongChangeEvent(songs.get(songIndex)));
            songPosition = songIndex;
            playSong();
        }
    }

    @Override
    public int getPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pausePlayer() {
        player.pause();
    }

    @Override
    public void seekPlayer(int position) {
        player.seekTo(position);
    }

    @Override
    public void playPlayer() {
        player.start();
    }

    @Override
    public void playPrevious() {
        setSong(songPosition+1);
    }

    @Override
    public void playNext() {
        setSong(songPosition-1);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(songPosition < songs.size()-EMPTY_CELLS_COUNT){
            songPosition++;
            setSong(songPosition);
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
    }
}
