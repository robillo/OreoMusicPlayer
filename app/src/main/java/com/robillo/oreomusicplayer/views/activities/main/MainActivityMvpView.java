package com.robillo.oreomusicplayer.views.activities.main;

import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.services.MusicService;

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

    void toggleRepeatModeInService(String value);

    void toggleShuffleModeInService();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();

    void repopulateListSongsListFragment();

    void refreshForUserThemeColors();

    void startThemeChangeActivity();

    void showSnackBar(String text);

    MusicService getMusicService();

    void updateServiceList(ArrayList<Song> updatedAudioList);

    int getCurrentSongDuration();

    void rescanDevice();

    void showSongOptionsOnBottomSheet(Song song);

}
