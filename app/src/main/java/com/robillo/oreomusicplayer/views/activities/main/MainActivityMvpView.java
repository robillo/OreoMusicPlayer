package com.robillo.oreomusicplayer.views.activities.main;

import com.robillo.oreomusicplayer.models.Song;

import java.util.ArrayList;

/**
 * Created by robinkamboj on 04/03/18.
 */

public interface MainActivityMvpView {

    void setUp();

    void setSongListFragment();

    void setSongPlayFragment();

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
}
