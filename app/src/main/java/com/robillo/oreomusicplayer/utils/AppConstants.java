package com.robillo.oreomusicplayer.utils;

import android.provider.MediaStore;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.ThemeColors;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AppConstants {

    //IntentExtra
    public static final String LAUNCHED_FROM_NOTIFICATION = "launched_from_notification";

    //SharedPreferencesHelper
    public static final String PREF_FILE_NAME = "MY_PREFERENCES";
    public static final String KEY_IS_REPEAT_MODE_ON = "is_repeat_mode_on";
    public static final String KEY_IS_SHUFFLE_MODE_ON = "is_shuffle_mode_on";
    public static final String KEY_IS_PLAY_EVENT = "is_play_event";
    public static final String KEY_SORT_ORDER_FOR_SONGS = "sort_order_for_songs";
    public static final String KEY_USER_THEME_NAME = "theme_name";
    public static final String KEY_IS_SONG_PLAYING = "is_song_playing";

    public static final String REPEAT_MODE_VALUE_REPEAT = "repeating";
    public static final String REPEAT_MODE_VALUE_LOOP = "looping";
    public static final String REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE = "linearly_traversing_once";

    //MusicService
    public static final int EMPTY_CELLS_COUNT = 2;
    public static final String CHANNEL_ID = "channelId";
    public static final String CHANNEL_NAME = "Player Notifications";
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
    public static HashMap<String, ThemeColors> themeMap = new HashMap<>();

    //ThemeChange
    public static final String ALL_BLACK = "ALL BLACK";
    public static final String RED_LIGHT = "RED - LIGHT";
    public static final String ORANGE_LIGHT = "ORANGE - LIGHT";
    public static final String YELLOW_LIGHT = "YELLOW - LIGHT";
    public static final String GREEN_LIGHT = "GREEN - LIGHT";
    public static final String BLUE_LIGHT = "BLUE - LIGHT";
    public static final String INDIGO_LIGHT = "INDIGO - LIGHT";
    public static final String VIOLET_LIGHT = "VOILET - LIGHT";

    public static final String BLUE_GREY = "BLUE GREY";
    public static final String DEEP_BROWN = "DEEP BROWN";
    public static final String DEEP_ORANGE = "DEEP ORANGE";
    public static final String DEEP_GREEN = "DEEP GREEN";
    public static final String DEEP_BLUE = "DEEP BLUE";
    public static final String AMBER = "AMBER";
    public static final String LIME = "LIME";
    public static final String CYAN = "CYAN";


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

        themeMap.put(
                BLUE_GREY,
                new ThemeColors(
                        R.color.blue_grey_primary,
                        R.color.blue_grey_primary_dark,
                        R.color.blue_grey_accent,
                        R.color.blue_grey_mat,
                        BLUE_GREY)
        );

        themeMap.put(
                DEEP_BROWN,
                new ThemeColors(
                        R.color.deep_brown_primary,
                        R.color.deep_brown_primary_dark,
                        R.color.deep_brown_accent,
                        R.color.deep_brown_mat,
                        DEEP_BROWN)
        );

        themeMap.put(
                DEEP_ORANGE,
                new ThemeColors(
                        R.color.deep_orange_primary,
                        R.color.deep_orange_primary_dark,
                        R.color.deep_orange_accent,
                        R.color.deep_orange_mat,
                        DEEP_ORANGE)
        );

        themeMap.put(
                DEEP_GREEN,
                new ThemeColors(
                        R.color.deep_green_primary,
                        R.color.deep_green_primary_dark,
                        R.color.deep_green_accent,
                        R.color.deep_green_mat,
                        DEEP_GREEN)
        );

        themeMap.put(
                DEEP_BLUE,
                new ThemeColors(
                        R.color.deep_blue_primary,
                        R.color.deep_blue_primary_dark,
                        R.color.deep_blue_accent,
                        R.color.deep_blue_mat,
                        DEEP_BLUE)
        );

        themeMap.put(
                AMBER,
                new ThemeColors(
                        R.color.amber_primary,
                        R.color.amber_primary_dark,
                        R.color.amber_accent,
                        R.color.amber_mat,
                        AMBER)
        );

        themeMap.put(
                LIME,
                new ThemeColors(
                        R.color.lime_primary,
                        R.color.lime_primary_dark,
                        R.color.lime_accent,
                        R.color.lime_mat,
                        LIME)
        );

        themeMap.put(
                CYAN,
                new ThemeColors(
                        R.color.cyan_primary,
                        R.color.cyan_primary_dark,
                        R.color.cyan_accent,
                        R.color.cyan_mat,
                        CYAN)
        );
//
//        themeMap.put(
//                ALL_BLACK,
//                new ThemeColors(R.color.all_black_primary, R.color.all_black_primary_dark, R.color.all_black_accent, ALL_BLACK)
//        );
//        themeMap.put(
//                VIOLET_LIGHT,
//                new ThemeColors(R.color.violet_primary, R.color.violet_primary_dark, R.color.violet_accent, VIOLET_LIGHT)
//        );
//        themeMap.put(
//                INDIGO_LIGHT,
//                new ThemeColors(R.color.indigo_primary, R.color.indigo_primary_dark, R.color.indigo_accent, INDIGO_LIGHT)
//        );
//        themeMap.put(
//                BLUE_LIGHT,
//                new ThemeColors(R.color.blue_primary, R.color.blue_primary_dark, R.color.blue_accent, BLUE_LIGHT)
//        );
//        themeMap.put(
//                GREEN_LIGHT,
//                new ThemeColors(R.color.green_primary, R.color.green_primary_dark, R.color.green_accent, GREEN_LIGHT)
//        );
//        themeMap.put(
//                YELLOW_LIGHT,
//                new ThemeColors(R.color.yellow_primary, R.color.yellow_primary_dark, R.color.yellow_accent, YELLOW_LIGHT)
//        );
//        themeMap.put(
//                ORANGE_LIGHT,
//                new ThemeColors(R.color.orange_primary, R.color.orange_primary_dark, R.color.orange_accent, ORANGE_LIGHT)
//        );
//        themeMap.put(
//                RED_LIGHT,
//                new ThemeColors(R.color.red_primary, R.color.red_primary_dark, R.color.red_accent, RED_LIGHT)
//        );
    }
}
