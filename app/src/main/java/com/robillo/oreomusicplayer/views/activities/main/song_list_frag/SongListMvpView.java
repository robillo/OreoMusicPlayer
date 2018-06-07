package com.robillo.oreomusicplayer.views.activities.main.song_list_frag;

import android.database.Cursor;
import android.view.View;

import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.models.ThemeColors;

public interface SongListMvpView {

    void setUp(View v);

    void fetchSongs(int from);

    String returnCursorElement(Cursor cursor, String string);

    void setCurrentSong(Song song);

    void playNextSong();

    void playPlayer(int from);

    void pausePlayer(int from);

    void resetAlbumArtAnimation();

    void fadeOutUpper();

    void fadeInUpper();

    void fadeOutController();

    void fadeInController();

    void refreshForUserThemeColors(ThemeColors currentUserThemeColors);

    void startThemeChangeActivity();

    void makeControllerInvisible();

}
