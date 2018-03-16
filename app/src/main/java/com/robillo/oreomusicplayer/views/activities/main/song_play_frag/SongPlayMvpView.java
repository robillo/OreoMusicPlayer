package com.robillo.oreomusicplayer.views.activities.main.song_play_frag;

import android.view.View;

import com.robillo.oreomusicplayer.models.Song;

public interface SongPlayMvpView {

    void setUp(View v);

    void setCurrentSong(Song song);

    void resetAlbumArtAnimation();

    void playPlayer(int from);

    void pausePlayer(int from);

    void setPreferencesToViews();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();
}
