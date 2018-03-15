package com.robillo.oreomusicplayer.events;

import com.robillo.oreomusicplayer.models.Song;

public class SongChangeEvent {
    private Song song;
    public SongChangeEvent(Song song) {this.song = song;}
    public Song getSong() {return song;}
}
