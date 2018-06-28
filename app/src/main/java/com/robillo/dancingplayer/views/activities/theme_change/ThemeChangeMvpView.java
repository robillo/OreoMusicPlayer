package com.robillo.dancingplayer.views.activities.theme_change;

import android.widget.TextView;

public interface ThemeChangeMvpView {

    void setup();

    void inflateThemeColors();

    void showSnackBarThemeSet(String themeName);

    void setInitialStatePlaylistSongsCount();

    void setColorsToViews(TextView tenView, TextView fiftyView, TextView hundredView, int count);

    void initialisePreferenceHelper();
}
