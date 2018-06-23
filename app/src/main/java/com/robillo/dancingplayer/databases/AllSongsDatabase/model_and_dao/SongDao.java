package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SongDao {

    @Query("SELECT * FROM Song")
    List<Song> getAllSongs();

    @Query("SELECT * FROM Song WHERE id=:id")
    Song getSong(String id);

    @Insert
    void insertSong(Song... songs);

    @Update
    void updateSong(Song... songs);

    @Delete
    void deleteSong(Song... songs);
}
