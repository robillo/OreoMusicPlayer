package com.robillo.oreomusicplayer.services;

import com.robillo.oreomusicplayer.models.Song;

import java.util.ArrayList;

/**
 * Created by robinkamboj on 04/03/18.
 */

public interface MusicServiceInterface {

    void initMusicPlayer();

    void setList(ArrayList<Song> songsList);

    void playSong();

    void setSong(int songIndex);
}