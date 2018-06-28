package com.robillo.dancingplayer.utils;

import android.arch.lifecycle.LiveData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistRepository;
import com.robillo.dancingplayer.models.PlaylistRowItem;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void deleteFile(Context context, int index, Song song, String... songIds) {
        MainActivity act = (MainActivity) context;
        Song serviceSong = act.getMusicService().getSong();
        if(serviceSong != null && serviceSong.getId().equals(songIds[0])) {
            act.getMusicService().cancelNotification();
            act.playNextSong();
        }
        act.removeSongFromListInMusicService(song);

        int numRows = context.getContentResolver().delete(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID + "=?",
                songIds);

        boolean isDeleted = (numRows > 0);

        if(isDeleted) Toast.makeText(act, "Song Deleted From Device", Toast.LENGTH_SHORT).show();
        else Toast.makeText(act, "Oops, there was some error in deletion!", Toast.LENGTH_SHORT).show();

        act.refreshSongListFragmentForSongDelete(song, index);
        act.hideOrRemoveBottomSheet();
    }
}
