package com.robillo.dancingplayer.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.robillo.dancingplayer.utils.AppConstants;

import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_PLAY_EVENT;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_REPEAT_MODE_ON;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_SHUFFLE_MODE_ON;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_SONG_PLAYING;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_SORT_ORDER_FOR_SONGS;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_USER_THEME_NAME;
import static com.robillo.dancingplayer.utils.AppConstants.PREF_FILE_NAME;

public class AppPreferencesHelper implements PreferencesHelper {

    private SharedPreferences prefs;

    public AppPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }


    @Override
    public String isRepeatModeOn() {
        return prefs.getString(KEY_IS_REPEAT_MODE_ON, AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE);
    }

    @Override
    public void setIsRepeatModeOn(String value) {
        prefs.edit().putString(KEY_IS_REPEAT_MODE_ON, value).apply();
    }

    @Override
    public boolean isShuffleModeOn() {
        return prefs.getBoolean(KEY_IS_SHUFFLE_MODE_ON, false);
    }

    @Override
    public void setIsShuffleModeOn(boolean value) {
        prefs.edit().putBoolean(KEY_IS_SHUFFLE_MODE_ON, value).apply();
    }

    @Override
    public boolean isPlayEvent() {
        return prefs.getBoolean(KEY_IS_PLAY_EVENT, false);
    }

    @Override
    public void setIsPlayEvent(boolean value) {
        prefs.edit().putBoolean(KEY_IS_PLAY_EVENT, value).apply();
    }

    @Override
    public String getSortOrderForSongs() {
        return prefs.getString(KEY_SORT_ORDER_FOR_SONGS, AppConstants.DATE_ADDED_ASCENDING);
    }

    @Override
    public void setSortOrderForSongs(String sortOrderForSongs) {
        prefs.edit().putString(KEY_SORT_ORDER_FOR_SONGS, sortOrderForSongs).apply();
    }

    @Override
    public String getUserThemeName() {
        return prefs.getString(KEY_USER_THEME_NAME, AppConstants.BLUE_GREY);
    }

    @Override
    public void setUserThemeName(String themeName) {
        prefs.edit().putString(KEY_USER_THEME_NAME, themeName).apply();
    }

    @Override
    public boolean isSongPlaying() {
        return prefs.getBoolean(KEY_IS_SONG_PLAYING, false);
    }

    @Override
    public void setIsSongPlaying(boolean isSongPlaying) {
        prefs.edit().putBoolean(KEY_IS_SONG_PLAYING, isSongPlaying).apply();
    }
}
