package com.robillo.oreomusicplayer.views.activities.main.songs_sort_frag;

import android.view.View;

import com.robillo.oreomusicplayer.models.ThemeColors;

public interface SongsSortMvpView {

    void setup(View v);

    void inflateSortItemsList();

    void refreshForUserThemeColors(ThemeColors currentUserThemeColors);

}
