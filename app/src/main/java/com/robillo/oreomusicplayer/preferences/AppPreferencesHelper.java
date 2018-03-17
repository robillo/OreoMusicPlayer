package com.robillo.oreomusicplayer.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.robillo.oreomusicplayer.utils.AppConstants;

public class AppPreferencesHelper implements PreferencesHelper {

    private SharedPreferences prefs;

    public AppPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(AppConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isRepeatModeOn() {
        return prefs.getBoolean(AppConstants.KEY_IS_REPEAT_MODE_ON, false);
    }

    @Override
    public void setIsRepeatModeOn(boolean value) {
        prefs.edit().putBoolean(AppConstants.KEY_IS_REPEAT_MODE_ON, value).apply();
    }

    @Override
    public boolean isShuffleModeOn() {
        return prefs.getBoolean(AppConstants.KEY_IS_SHUFFLE_MODE_ON, false);
    }

    @Override
    public void setIsShuffleModeOn(boolean value) {
        prefs.edit().putBoolean(AppConstants.KEY_IS_SHUFFLE_MODE_ON, value).apply();
    }

    @Override
    public boolean isPlayEvent() {
        return prefs.getBoolean(AppConstants.KEY_IS_PLAY_EVENT, false);
    }

    @Override
    public void setIsPlayEvent(boolean value) {
        prefs.edit().putBoolean(AppConstants.KEY_IS_PLAY_EVENT, value).apply();
    }

    @Override
    public String getSortOrderForSongs() {
        return prefs.getString(AppConstants.KEY_SORT_ORDER_FOR_SONGS, AppConstants.DATE_ADDED_ASCENDING);
    }

    @Override
    public void setSortOrderForSongs(String sortOrderForSongs) {
        prefs.edit().putString(AppConstants.KEY_IS_PLAY_EVENT, sortOrderForSongs).apply();
    }
}
