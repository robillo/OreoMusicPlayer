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
    private Long timeSince70;

    public MostPlayed(@NonNull String songId, int playCount, Long timeSince70) {
        this.songId = songId;
        this.playCount = playCount;
        this.timeSince70 = timeSince70;
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

    public Long getTimeSince70() {
        return timeSince70;
    }

    public void setTimeSince70(Long timeSince70) {
        this.timeSince70 = timeSince70;
    }
}
