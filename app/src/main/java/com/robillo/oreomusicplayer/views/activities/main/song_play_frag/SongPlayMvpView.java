package com.robillo.oreomusicplayer.views.activities.main.song_play_frag;

import android.database.Cursor;
import android.view.View;

import com.robillo.oreomusicplayer.models.Song;

/**
 * Created by robinkamboj on 16/03/18.
 */

public interface SongPlayMvpView {

    void setUp(View v);

    void setCurrentSong(Song song);

    void resetAlbumArtAnimation();

    void playPlayer(int from);

    void pausePlayer(int from);
}
