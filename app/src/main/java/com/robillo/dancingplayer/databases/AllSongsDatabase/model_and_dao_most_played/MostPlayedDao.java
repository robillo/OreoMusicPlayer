package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.robillo.dancingplayer.models.MostPlayed;
import com.robillo.dancingplayer.models.Song;

import java.util.List;

@Dao
public interface MostPlayedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMostPlayed(MostPlayed... mostPlayed);

    @Query("SELECT playCount FROM MostPlayed WHERE songId = :songId LIMIT 1")
    LiveData<Integer> checkIfSongExists(String songId);

    @Query("UPDATE MostPlayed SET playCount = :playCount, timeSince70 = :timeSince70 WHERE songId = :songId")
    void updateMostPlayed(String songId, int playCount, Long timeSince70);

    @Query("UPDATE MostPlayed SET timeSince70 = :timeSince70 WHERE songId = :songId")
    void updateRecentlyPlayed(String songId, long timeSince70);

    @Query("SELECT Song.* FROM Song INNER JOIN MostPlayed WHERE Song.id = MostPlayed.songId ORDER BY playCount DESC LIMIT :limit")
    LiveData<List<Song>> getMostPlayedSongs(int limit);

    @Query("SELECT Song.* FROM Song INNER JOIN MostPlayed WHERE Song.id = MostPlayed.songId ORDER BY timeSince70 DESC LIMIT :limit")
    LiveData<List<Song>> getRecentlyPlayedSongs(int limit);
}
