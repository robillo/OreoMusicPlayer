package com.robillo.oreomusicplayer.services;

import android.content.Context;
import android.graphics.Bitmap;

import com.robillo.oreomusicplayer.models.Song;

import java.util.ArrayList;

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

    void toggleRepeatMode();

    void toggleShuffleMode();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();

    void updateAudioList(ArrayList<Song> songs);

//    void refreshNotificationForThemeChange();
}
