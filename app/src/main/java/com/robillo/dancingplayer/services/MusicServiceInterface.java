package com.robillo.dancingplayer.services;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;

import com.robillo.dancingplayer.models.Song;

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

    Song getSong();

    void seekPlayer(int position);

    void playPlayer();

    void playPrevious();

    void playNext();

    void buildNotification(boolean play_or_pause);

    void cancelNotification();

    Bitmap getBitmapAlbumArt();

    void toggleRepeatMode(String value);

    void toggleShuffleMode();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();

    void updateAudioList(ArrayList<Song> songs);

    void callStateListener();

    void setPlayerStateToNoSongPlaying();

    void removeSongFromList(Song song);

    void audioFocusChangeListenerPrelims();

    boolean audioManagerRequestAudioFocus();

    PendingIntent setupNotificationPendingIntent();

    NotificationCompat.Action returnAction(int id, String title, int which);
}
