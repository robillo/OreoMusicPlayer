package com.robillo.oreomusicplayer.events;

import com.robillo.oreomusicplayer.models.Song;

/**
 * Created by robinkamboj on 04/03/18.
 */

public class SongChangeEvent {

    private Song song;

    public SongChangeEvent(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }
}
