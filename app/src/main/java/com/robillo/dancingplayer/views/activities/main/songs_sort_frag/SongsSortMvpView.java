package com.robillo.dancingplayer.views.activities.main.songs_sort_frag;

import android.view.View;

import com.robillo.dancingplayer.models.ThemeColors;

public interface SongsSortMvpView {

    void setup(View v);

    void inflateSortItemsList();

    void refreshForUserThemeColors(ThemeColors currentUserThemeColors);

}
