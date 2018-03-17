package com.robillo.oreomusicplayer.utils;

import android.provider.MediaStore;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class AppConstants {

    //SharedPreferencesHelper
    public static final String PREF_FILE_NAME = "MY_PREFERENCES";
    public static final String KEY_IS_REPEAT_MODE_ON = "is_repeat_mode_on";
    public static final String KEY_IS_SHUFFLE_MODE_ON = "is_shuffle_mode_on";
    public static final String KEY_IS_PLAY_EVENT = "is_play_event";
    public static final String KEY_SORT_ORDER_FOR_SONGS = "sort_order_for_songs";

    //MusicService
    public static final int EMPTY_CELLS_COUNT = 2;
    public static final String CHANNEL_ID = "channelId";
    public static final String SESSION_NAME = "session_name";
    public static final String ACTION_PREV = "PREV";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_TOGGLE_PLAYBACK = "TOGGLE_PLAYBACK";
    public static final int CONTROLLER_NOTIFICATION_ID = 0;

    //SongListFragment
    public static final int FROM_ACTIVITY = 0;
    public static final int FROM_FRAGMENT = 1;

    //SongListFragment SortOrder
    private static final String ASCENDING = "ASC";
    private static final String DESCENDING = "DESC";

    public static final String SIZE_ASCENDING = MediaStore.Audio.Media.SIZE + " " + ASCENDING;
    public static final String SIZE_DESCENDING = MediaStore.Audio.Media.SIZE + " " + DESCENDING;

    public static final String YEAR_ASCENDING = MediaStore.Audio.Media.YEAR + " " + ASCENDING;
    public static final String YEAR_DESCENDING = MediaStore.Audio.Media.YEAR + " " + DESCENDING;

    public static final String ALBUM_ASCENDING = MediaStore.Audio.Media.ALBUM + " " + ASCENDING;
    public static final String ALBUM_DESCENDING = MediaStore.Audio.Media.ALBUM + " " + DESCENDING;

    public static final String TITLE_ASCENDING = MediaStore.Audio.Media.TITLE + " " + ASCENDING;
    public static final String TITLE_DESCENDING = MediaStore.Audio.Media.TITLE + " " + DESCENDING;

    public static final String ARTIST_ASCENDING = MediaStore.Audio.Media.ARTIST + " " + ASCENDING;
    public static final String ARTIST_DESCENDING = MediaStore.Audio.Media.ARTIST + " " + DESCENDING;

    public static final String DURATION_ASCENDING = MediaStore.Audio.Media.DURATION + " " + ASCENDING;
    public static final String DURATION_DESCENDING = MediaStore.Audio.Media.DURATION + " " + DESCENDING;

    public static final String DATE_ADDED_ASCENDING = MediaStore.Audio.Media.DATE_ADDED + " " + ASCENDING;      //DEFAULT
    public static final String DATE_ADDED_DESCENDING = MediaStore.Audio.Media.DATE_ADDED + " " + DESCENDING;

    public static final String DATE_MODIFIED_ASCENDING = MediaStore.Audio.Media.DATE_MODIFIED + " " + ASCENDING;
    public static final String DATE_MODIFIED_DESCENDING = MediaStore.Audio.Media.DATE_MODIFIED + " " + DESCENDING;

    public static HashMap<String, String> sortOrderMap = new HashMap<>();

    static {
        sortOrderMap.put(SIZE_ASCENDING, "Size Ascending");
        sortOrderMap.put(SIZE_DESCENDING, "Size Descending");
        sortOrderMap.put(YEAR_ASCENDING, "Year Ascending");
        sortOrderMap.put(YEAR_DESCENDING, "Year Descending");
        sortOrderMap.put(ALBUM_ASCENDING, "Album Ascending");
        sortOrderMap.put(ALBUM_DESCENDING, "Album Descending");
        sortOrderMap.put(TITLE_ASCENDING, "Title Ascending");
        sortOrderMap.put(TITLE_DESCENDING, "Title Descending");
        sortOrderMap.put(ARTIST_ASCENDING, "Artist Ascending");
        sortOrderMap.put(ARTIST_DESCENDING, "Artist Descending");
        sortOrderMap.put(DURATION_ASCENDING, "Duration Ascending");
        sortOrderMap.put(DURATION_DESCENDING, "Duration Descending");
        sortOrderMap.put(DATE_ADDED_ASCENDING, "Date Added Ascending");
        sortOrderMap.put(DATE_ADDED_DESCENDING, "Date Added Descending");
        sortOrderMap.put(DATE_MODIFIED_ASCENDING, "Date Modified Ascending");
        sortOrderMap.put(DATE_MODIFIED_DESCENDING, "Date Modified Descending");
    }
}
