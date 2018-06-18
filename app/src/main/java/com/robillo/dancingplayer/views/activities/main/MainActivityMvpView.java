package com.robillo.dancingplayer.views.activities.main;

import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.services.MusicService;
import com.robillo.dancingplayer.views.activities.main.song_play_frag.SongPlayFragmentSheet;

import java.util.ArrayList;

public interface MainActivityMvpView {

    void setUp();

    void setSongListFragment();

    void setSongPlayFragment();

    void setSongsSortFragment();

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

    void showSongOptionsOnBottomSheet(Song song, int index);

    void hideOrRemoveBottomSheet();

    void removeSongFromListInMusicService(Song song);

    void hideSongPlayFragment(SongPlayFragmentSheet fragmentSheet);

    void refreshSongListFragmentForSongDelete(Song song, int index);

}
