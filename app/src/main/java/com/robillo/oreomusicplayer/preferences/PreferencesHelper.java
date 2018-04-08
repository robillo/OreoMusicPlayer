package com.robillo.oreomusicplayer.preferences;

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

}
