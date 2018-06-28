package com.robillo.dancingplayer.preferences;

import java.util.Set;

interface PreferencesHelper {

    String isRepeatModeOn();
    void setIsRepeatModeOn(String value);

    boolean isShuffleModeOn();
    void setIsShuffleModeOn(boolean value);

    boolean isPlayEvent();
    void setIsPlayEvent(boolean value);

    String getSortOrderForSongs();
    void setSortOrderForSongs(String sortOrderForSongs);

    String getUserThemeName();
    void setUserThemeName(String themeName);

    boolean isSongPlaying();
    void setIsSongPlaying(boolean isSongPlaying);

    String getCurrentPlaylistTitle();
    void setCurrentPlaylistTitle(String title);

    Set<String> getPlaylistSet();
    void setPlaylistSet(Set<String> playlistSet);

    int getMostPlayedCount();
    void setMostPlayedCount(int count);

    int getRecentlyPlayedCount();
    void setRecentlyPlayedCount(int count);

    int getRecentlyAddedCount();
    void setRecentlyAddedCount(int count);

}
