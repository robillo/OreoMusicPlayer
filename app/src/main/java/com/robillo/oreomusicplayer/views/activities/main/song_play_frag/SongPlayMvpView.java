package com.robillo.oreomusicplayer.views.activities.main.song_play_frag;

import android.view.View;

import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.models.ThemeColors;

public interface SongPlayMvpView {

    void setUp(View v);

    void setCurrentSong(Song song);

    void resetAlbumArtAnimation();

    void playPlayer(int from);

    void pausePlayer(int from);

    void setPreferencesToViews();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();

    void refreshForUserThemeColors(ThemeColors currentUserThemeColors);

    void startTimerForProgress();

    void stopTimerForProgress();

    void updateTimer(int millisInFuture, int countDownInterval);

    void setProgressToSeekBar(int currentDuration, int totalDuration);

    void setDurationValues(int currentDuration, int totalDuration);

    void pauseTimer();

    void resumeTimer(int millisInFuture, int countDownInterval);
}
