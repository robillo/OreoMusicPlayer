package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.robillo.dancingplayer.models.Playlist;
import com.robillo.dancingplayer.models.Song;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface PlaylistDao {

    @Query("DELETE FROM Playlist WHERE playlist = :playlistName")
    void deletePlaylistByPlaylistName(String playlistName);

    @Query("DELETE FROM Playlist WHERE song_id = :id")
    void deleteSongById(String id);

    @Query("SELECT * FROM Playlist")
    LiveData<List<Playlist>> getAllPlaylistItems();

    @Insert(onConflict = REPLACE)
    void insertPlaylistItem(Playlist... playlistItem);

    @Delete
    void deletePlaylistItem(Playlist... playlistItem);

    @Query("SELECT Playlist.playlist FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Song.id = :id")
    LiveData<List<String>> getAllPlaylistsForSongById(String id);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist")
    LiveData<List<Song>> getSongsByPlaylistName(String playlist);

}
