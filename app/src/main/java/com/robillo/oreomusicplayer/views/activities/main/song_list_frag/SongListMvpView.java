package com.robillo.oreomusicplayer.views.activities.main.song_list_frag;

import android.database.Cursor;
import android.view.View;

import com.robillo.oreomusicplayer.models.Song;

/**
 * Created by robinkamboj on 05/03/18.
 */

public interface SongListMvpView {

    void setUp(View v);

    void fetchSongs();

    String returnCursorElement(Cursor cursor, String string);

    void setCurrentSong(Song song);

    void playNextSong();

    void playPlayer();

    void pausePlayer();

    void resetAlbumArtAnimation();
}
