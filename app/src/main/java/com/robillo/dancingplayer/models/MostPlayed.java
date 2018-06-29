package com.robillo.dancingplayer.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class MostPlayed {

    @SuppressWarnings("NullableProblems")
    @NonNull
    @PrimaryKey
    private String songId;
    private int playCount;

    public MostPlayed(@NonNull String songId, int playCount) {
        this.songId = songId;
        this.playCount = playCount;
    }

    @NonNull
    public String getSongId() {
        return songId;
    }

    public void setSongId(@NonNull String songId) {
        this.songId = songId;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
}
