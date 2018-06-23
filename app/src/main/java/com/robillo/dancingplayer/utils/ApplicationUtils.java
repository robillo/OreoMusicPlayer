package com.robillo.dancingplayer.utils;

import android.content.Context;

import com.robillo.dancingplayer.models.PlaylistRowItem;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ApplicationUtils {

    public ApplicationUtils() {
    }

    public String formatStringOutOfSeconds(int duration) {
        int mins = duration/60;
        int seconds = duration%60;

        String lhs, rhs;

        if(mins < 10) lhs = "0" + String.valueOf(mins);
        else lhs = String.valueOf(mins);

        if(seconds < 10) rhs = "0" + String.valueOf(seconds);
        else rhs = String.valueOf(seconds);

        return lhs + " : " + rhs;
    }

    public String formatSizeKBtoMB(float size) {
        return String.format("%s MB", String.format(Locale.ENGLISH, "%.2f", size/1024));
    }

    public PlaylistRowItem convertStringToPlaylistRowItem(String title) {
        boolean isPersistent = false;

        if(title.equals(AppConstants.DEFAULT_PLAYLIST_TITLE) || title.equals(AppConstants.MOST_PLAYED) ||
                        title.equals(AppConstants.RECENTLY_ADDED) || title.equals(AppConstants.RECENTLY_PLAYED)) {
            isPersistent = true;
        }

        return new PlaylistRowItem(title, isPersistent);
    }

    public List<PlaylistRowItem> getPlaylistItemsFromContext(Context context) {

        Set<String> playlistSet = houseKeepingItemsNotNull(new AppPreferencesHelper(context));

        List<PlaylistRowItem> items = new ArrayList<>();
        List<String> list = new ArrayList<>(playlistSet);
        for(String s : list) {
            items.add(convertStringToPlaylistRowItem(s));
        }

        return items;
    }

    public void createNewPlaylistInPreferences(String playlistName, Context context) {
        AppPreferencesHelper helper = new AppPreferencesHelper(context);

        Set<String> playlistSet = houseKeepingItemsNotNull(helper);

        playlistSet.add(playlistName);

        helper.setPlaylistSet(playlistSet);
    }

    public void changePlaylistInPreferences(String newPlaylistName, Context context, String oldPlaylistName) {
        AppPreferencesHelper helper = new AppPreferencesHelper(context);

        Set<String> playlistSet = houseKeepingItemsNotNull(helper);

        for(String s : playlistSet) {
            if(s.equals(oldPlaylistName)) {
                playlistSet.remove(oldPlaylistName);
                playlistSet.add(newPlaylistName);
                break;
            }
        }

        helper.setPlaylistSet(playlistSet);
    }

    public void removePlaylistInPreferences(Context context, String playlistName) {
        AppPreferencesHelper helper = new AppPreferencesHelper(context);

        Set<String> playlistSet = houseKeepingItemsNotNull(helper);

        for(String s : playlistSet) {
            if(s.equals(playlistName)) {
                playlistSet.remove(playlistName);
                break;
            }
        }

        helper.setPlaylistSet(playlistSet);
    }

    private Set<String> houseKeepingItemsNotNull(AppPreferencesHelper helper) {
        Set<String> playlistSet = helper.getPlaylistSet();
        if(playlistSet == null) {
            playlistSet = new HashSet<>();
            playlistSet.add(AppConstants.DEFAULT_PLAYLIST_TITLE);
            playlistSet.add(AppConstants.MOST_PLAYED);
            playlistSet.add(AppConstants.RECENTLY_ADDED);
            playlistSet.add(AppConstants.RECENTLY_PLAYED);
            helper.setPlaylistSet(playlistSet);
        }
        return playlistSet;
    }
}
