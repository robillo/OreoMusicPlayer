package com.robillo.dancingplayer.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Relation;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity
public class Playlist {

    //one song can be in multiple playlists (one song -> multiple playlists), therefore : one to many relationship

    @PrimaryKey(autoGenerate = true)
    private int pk;

    @ColumnInfo(name = "song_id")
    private String id;
    private String playlist;

    public Playlist(@NonNull String id, @NonNull String playlist) {
        this.id = id;
        this.playlist = playlist;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(@NonNull String playlist) {
        this.playlist = playlist;
    }
}
