package com.robillo.oreomusicplayer.events;

import com.robillo.oreomusicplayer.models.Song;

public class SongChangeEvent {

    private Song song;
    private int event;  //AppConstants.java SongChangeEvent

    public SongChangeEvent(Song song, int event) {
        this.song = song;
        this.event = event;
    }

    public int getEvent() {
        return event;
    }

    public Song getSong() {
        return song;
    }
}
