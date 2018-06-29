package com.robillo.dancingplayer.events;

import com.robillo.dancingplayer.models.Song;

public class IncrementMostPlayedCountForSongEvent {

    Song song;

    IncrementMostPlayedCountForSongEvent(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }
}
