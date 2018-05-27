package com.robillo.oreomusicplayer.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;

import com.robillo.oreomusicplayer.models.Song;

import java.util.ArrayList;

@SuppressWarnings("unused")
public interface MusicServiceInterface {

    void initMusicPlayer();

    void setList(ArrayList<Song> songsList);

    void playSong();

    void setSong(int songIndex);

    int getPosition();

    int getDuration();

    boolean isPlaying();

    void pausePlayer();

    void seekPlayer(int position);

    void playPlayer();

    void playPrevious();

    void playNext();

    void buildNotification(boolean play_or_pause);

    void cancelNotification(Context context, int notificationId);

    Bitmap getBitmapAlbumArt();

    void toggleRepeatMode(String value);

    void toggleShuffleMode();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();

    void updateAudioList(ArrayList<Song> songs);

    void callStateListener();

    void audioManagerServiceListener();

    void audioFocusChangeListenerPrelims();

    void configMediaPlayerState(MediaPlayer mp);

//    void refreshNotificationForThemeChange();
}
