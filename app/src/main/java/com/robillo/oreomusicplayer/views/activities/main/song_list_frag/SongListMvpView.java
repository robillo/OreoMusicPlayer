package com.robillo.oreomusicplayer.views.activities.main.song_list_frag;

import android.database.Cursor;
import android.view.View;

import com.robillo.oreomusicplayer.models.Song;

/**
 * Created by robinkamboj on 05/03/18.
 */

public interface SongListMvpView {

    final int FROM_ACTIVITY = 0;
    final int FROM_FRAGMENT = 1;

    void setUp(View v);

    void fetchSongs();

    String returnCursorElement(Cursor cursor, String string);

    void setCurrentSong(Song song);

    Song getCurrentSong();

    void playNextSong();

    void playPlayer(int from);

    void pausePlayer(int from);

    void resetAlbumArtAnimation();

    void fadeOutUpper();

    void fadeInUpper();

    void fadeOutController();

    void fadeInController();
}
