package com.robillo.dancingplayer.views.activities.main.song_list_frag;

import android.database.Cursor;
import android.view.View;

import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.models.ThemeColors;

import java.util.List;

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

    void refreshForUserThemeColors(ThemeColors currentUserThemeColors, String themeName);

    void startThemeChangeActivity();

    int getControllerVisibility();

    void refreshSongListFragmentForSongDelete(Song song, int index);

    void renderRecyclerViewForAudioList(List<Song> audioList);

    void notifyDataSetChanged(int position);

    void showErrorLayout();

    void hideErrorLayout();
}
