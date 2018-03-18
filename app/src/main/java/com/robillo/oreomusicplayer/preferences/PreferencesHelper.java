package com.robillo.oreomusicplayer.preferences;

interface PreferencesHelper {

    boolean isRepeatModeOn();
    void setIsRepeatModeOn(boolean value);

    boolean isShuffleModeOn();
    void setIsShuffleModeOn(boolean value);

    boolean isPlayEvent();
    void setIsPlayEvent(boolean value);

    String getSortOrderForSongs();
    void setSortOrderForSongs(String sortOrderForSongs);

    String getUserThemeName();
    void setUserThemeName(String themeName);

}
