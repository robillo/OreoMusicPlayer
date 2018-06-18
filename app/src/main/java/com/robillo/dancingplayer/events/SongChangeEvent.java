package com.robillo.dancingplayer.events;

import com.robillo.dancingplayer.models.Song;

public class SongChangeEvent {
    private Song song;
    public SongChangeEvent(Song song) {this.song = song;}
    public Song getSong() {return song;}
}
