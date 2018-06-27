package com.robillo.dancingplayer.utils;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.util.Log;

import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistRepository;
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
}
