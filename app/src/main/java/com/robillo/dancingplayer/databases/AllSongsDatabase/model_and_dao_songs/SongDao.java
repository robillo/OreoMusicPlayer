package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_songs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.robillo.dancingplayer.models.Song;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface SongDao {

    @Query("SELECT * FROM Song")
    LiveData<List<Song>> getAllSongs();

    @Query("SELECT * FROM Song WHERE id = :id")
    LiveData<Song> getSongById(String id);

    @Insert(onConflict = REPLACE)
    void insertSong(Song... songs);

    @Update
    void updateSong(Song... songs);

    @Delete
    void deleteSong(Song... songs);

    @Query("DELETE FROM Song WHERE id = :id")
    void deleteSongById(String id);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist")
    LiveData<List<Song>> getSongsByPlaylistName(String playlist);

    @Query("SELECT Playlist.playlist FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Song.id = id")
    LiveData<List<String>> getAllPlaylistsForSongById(String id);
}
