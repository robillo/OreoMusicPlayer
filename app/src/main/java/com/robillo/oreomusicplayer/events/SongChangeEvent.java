package com.robillo.oreomusicplayer.events;

import com.robillo.oreomusicplayer.models.Song;

/**
 * Created by robinkamboj on 04/03/18.
 */

public class SongChangeEvent {

    static public final int PLAY_PLAYER = 0;
    static public final int PAUSE_PLAYER = 1;
    static public final int DO_NOTHING = 2;
    private Song song;
    private int event;

    public SongChangeEvent(Song song, int event) {
        this.song = song;
        this.event = event;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }
}
