package com.robillo.oreomusicplayer.views.activities.main;

import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.models.ThemeColors;

import java.util.ArrayList;

public interface MainActivityMvpView {

    void setUp();

    void setSongListFragment();

    void setSongPlayFragment();

    void setSongsSortFragment();

    void askForDevicePermissions();

    void startServiceForAudioList(ArrayList<Song> songList);

    void playSong(int songIndex);

    void playNextSong();

    void playPreviousSong();

    Song getCurrentSong();

    void toggleRepeatModeInService();

    void toggleShuffleModeInService();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();

    void repopulateListSongsListFragment();

    void refreshForUserThemeColors(ThemeColors currentUserThemeColors);

    void startThemeChangeActivityForResult();

    void showSnackBar(String text);

}
