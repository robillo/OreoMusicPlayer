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

    @Query("DELETE FROM Song")
    void deleteAllSongs();

    @Query("SELECT * FROM Song")
    LiveData<List<Song>> getAllSongs();

    @Query("SELECT * FROM Song WHERE id = :id")
    LiveData<Song> getSongId(String id);

    @Insert(onConflict = REPLACE)
    void insertSong(Song... songs);

    @Update
    void updateSong(Song... songs);

    @Delete
    void deleteSong(Song... songs);

    @Query("SELECT COUNT(id) FROM Song")
    LiveData<Integer> getNumberOfRows();

    @Query("DELETE FROM Song WHERE id = :id")
    void deleteSongId(String id);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist")
    LiveData<List<Song>> getSongsPlaylist(String playlist);

    @Query("SELECT Playlist.playlist FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Song.id = :id")
    LiveData<List<String>> getAllPlaylistsForSongId(String id);

    //sorted songs get by playlist name and sort order

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY size ASC")
    LiveData<List<Song>> getSongsPlaylistSizeA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY size DESC")
    LiveData<List<Song>> getSongsPlaylistSizeD(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY year ASC")
    LiveData<List<Song>> getSongsPlaylistYearA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY year DESC")
    LiveData<List<Song>> getSongsPlaylistYearD(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY album ASC")
    LiveData<List<Song>> getSongsPlaylistAlbumA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY album DESC")
    LiveData<List<Song>> getSongsPlaylistAlbumD(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY title ASC")
    LiveData<List<Song>> getSongsPlaylistTitleA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY title DESC")
    LiveData<List<Song>> getSongsPlaylistTitleD(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY artist ASC")
    LiveData<List<Song>> getSongsPlaylistArtistA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY artist DESC")
    LiveData<List<Song>> getSongsPlaylistArtistD(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY duration ASC")
    LiveData<List<Song>> getSongsPlaylistDurationA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY duration DESC")
    LiveData<List<Song>> getSongsPlaylistDurationD(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY dateAdded ASC")
    LiveData<List<Song>> getSongsPlaylistDateAddedA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY dateAdded DESC")
    LiveData<List<Song>> getSongsPlaylistDateAddedD(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY dateModified ASC")
    LiveData<List<Song>> getSongsPlaylistDateModifiedA(String playlist);

    @Query("SELECT Song.* FROM Song INNER JOIN Playlist ON Song.id = Playlist.song_id WHERE Playlist.playlist = :playlist ORDER BY dateModified DESC")
    LiveData<List<Song>> getSongsPlaylistDateModifiedD(String playlist);

    //all songs get by playlist name and sort order

    @Query("SELECT Song.* FROM Song ORDER BY size ASC")
    LiveData<List<Song>> getAllSongsSizeA();

    @Query("SELECT Song.* FROM Song ORDER BY size DESC")
    LiveData<List<Song>> getAllSongsSizeD();

    @Query("SELECT Song.* FROM Song ORDER BY year ASC")
    LiveData<List<Song>> getAllSongsYearA();

    @Query("SELECT Song.* FROM Song ORDER BY year DESC")
    LiveData<List<Song>> getAllSongsYearD();

    @Query("SELECT Song.* FROM Song ORDER BY album ASC")
    LiveData<List<Song>> getAllSongsAlbumA();

    @Query("SELECT Song.* FROM Song ORDER BY album DESC")
    LiveData<List<Song>> getAllSongsAlbumD();

    @Query("SELECT Song.* FROM Song ORDER BY title ASC")
    LiveData<List<Song>> getAllSongsTitleA();

    @Query("SELECT Song.* FROM Song ORDER BY title DESC")
    LiveData<List<Song>> getAllSongsTitleD();

    @Query("SELECT Song.* FROM Song ORDER BY artist ASC")
    LiveData<List<Song>> getAllSongsArtistA();

    @Query("SELECT Song.* FROM Song ORDER BY artist DESC")
    LiveData<List<Song>> getAllSongsArtistD();

    @Query("SELECT Song.* FROM Song ORDER BY duration ASC")
    LiveData<List<Song>> getAllSongsDurationA();

    @Query("SELECT Song.* FROM Song ORDER BY duration DESC")
    LiveData<List<Song>> getAllSongsDurationD();

    @Query("SELECT Song.* FROM Song ORDER BY dateAdded ASC")
    LiveData<List<Song>> getAllSongsDateAddedA();

    @Query("SELECT Song.* FROM Song ORDER BY dateAdded DESC")
    LiveData<List<Song>> getAllSongsDateAddedD();

    @Query("SELECT Song.* FROM Song ORDER BY dateModified ASC")
    LiveData<List<Song>> getAllSongsDateModifiedA();

    @Query("SELECT Song.* FROM Song ORDER BY dateModified DESC")
    LiveData<List<Song>> getAllSongsDateModifiedD();

    @Query("SELECT * FROM Song ORDER BY dateAdded DESC LIMIT :limit")
    public LiveData<List<Song>> getRecentlyAddedSongs(int limit);

    @Query("SELECT Song.* FROM Song INNER JOIN MostPlayed WHERE Song.id = MostPlayed.songId ORDER BY playCount DESC LIMIT :limit")
    LiveData<List<Song>> getMostPlayedSongs(int limit);

    @Query("SELECT Song.* FROM Song INNER JOIN MostPlayed WHERE Song.id = MostPlayed.songId ORDER BY timeSince70 DESC LIMIT :limit")
    LiveData<List<Song>> getRecentlyPlayedSongs(int limit);
}