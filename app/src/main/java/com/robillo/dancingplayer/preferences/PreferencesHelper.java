package com.robillo.dancingplayer.preferences;

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

}
