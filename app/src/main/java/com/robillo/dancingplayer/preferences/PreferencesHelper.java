package com.robillo.dancingplayer.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.robillo.dancingplayer.utils.AppConstants;

import java.util.Set;

import static com.robillo.dancingplayer.utils.AppConstants.DEFAULT_PLAYLIST_SONGS_COUNT;
import static com.robillo.dancingplayer.utils.AppConstants.DEFAULT_PLAYLIST_TITLE;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_CURRENT_PLAYLIST_TITLE;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_PLAY_EVENT;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_REPEAT_MODE_ON;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_SHUFFLE_MODE_ON;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_IS_SONG_PLAYING;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_MOST_PLAYED_COUNT;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_PLAYLIST_STRING_SET;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_RECENTLY_ADDED_COUNT;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_RECENTLY_PLAYED_COUNT;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_SORT_ORDER_FOR_SONGS;
import static com.robillo.dancingplayer.utils.AppConstants.KEY_USER_THEME_NAME;
import static com.robillo.dancingplayer.utils.AppConstants.PREF_FILE_NAME;

public class PreferencesHelper {

    private SharedPreferences prefs;

    public PreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public String isRepeatModeOn() {
        return prefs.getString(KEY_IS_REPEAT_MODE_ON, AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE);
    }


    public void setIsRepeatModeOn(String value) {
        prefs.edit().putString(KEY_IS_REPEAT_MODE_ON, value).apply();
    }


    public boolean isShuffleModeOn() {
        return prefs.getBoolean(KEY_IS_SHUFFLE_MODE_ON, false);
    }


    public void setIsShuffleModeOn(boolean value) {
        prefs.edit().putBoolean(KEY_IS_SHUFFLE_MODE_ON, value).apply();
    }


    public boolean isPlayEvent() {
        return prefs.getBoolean(KEY_IS_PLAY_EVENT, false);
    }


    public void setIsPlayEvent(boolean value) {
        prefs.edit().putBoolean(KEY_IS_PLAY_EVENT, value).apply();
    }


    public String getSortOrderForSongs() {
        return prefs.getString(KEY_SORT_ORDER_FOR_SONGS, AppConstants.TITLE_ASCENDING);
    }


    public void setSortOrderForSongs(String sortOrderForSongs) {
        prefs.edit().putString(KEY_SORT_ORDER_FOR_SONGS, sortOrderForSongs).apply();
    }


    public String getUserThemeName() {
        return prefs.getString(KEY_USER_THEME_NAME, AppConstants.PITCH_BLACK);
    }


    public void setUserThemeName(String themeName) {
        prefs.edit().putString(KEY_USER_THEME_NAME, themeName).apply();
    }


    public boolean isSongPlaying() {
        return prefs.getBoolean(KEY_IS_SONG_PLAYING, false);
    }


    public void setIsSongPlaying(boolean isSongPlaying) {
        prefs.edit().putBoolean(KEY_IS_SONG_PLAYING, isSongPlaying).apply();
    }


    public String getCurrentPlaylistTitle() {
        return prefs.getString(KEY_CURRENT_PLAYLIST_TITLE, DEFAULT_PLAYLIST_TITLE);
    }


    public void setCurrentPlaylistTitle(String title) {
        prefs.edit().putString(KEY_CURRENT_PLAYLIST_TITLE, title).apply();
    }


    public Set<String> getPlaylistSet() {
        return prefs.getStringSet(KEY_PLAYLIST_STRING_SET, null);
    }


    public void setPlaylistSet(Set<String> playlistSet) {
        prefs.edit().putStringSet(KEY_PLAYLIST_STRING_SET, playlistSet).apply();
    }


    public int getMostPlayedCount() {
        return prefs.getInt(KEY_MOST_PLAYED_COUNT, DEFAULT_PLAYLIST_SONGS_COUNT);
    }


    public void setMostPlayedCount(int count) {
        prefs.edit().putInt(KEY_MOST_PLAYED_COUNT, count).apply();
    }


    public int getRecentlyPlayedCount() {
        return prefs.getInt(KEY_RECENTLY_PLAYED_COUNT, DEFAULT_PLAYLIST_SONGS_COUNT);
    }


    public void setRecentlyPlayedCount(int count) {
        prefs.edit().putInt(KEY_RECENTLY_PLAYED_COUNT, count).apply();
    }


    public int getRecentlyAddedCount() {
        return prefs.getInt(KEY_RECENTLY_ADDED_COUNT, DEFAULT_PLAYLIST_SONGS_COUNT);
    }


    public void setRecentlyAddedCount(int count) {
        prefs.edit().putInt(KEY_RECENTLY_ADDED_COUNT, count).apply();
    }


}
