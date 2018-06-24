package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_songs;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.robillo.dancingplayer.models.Song;

import java.util.List;

public class SongRepository {

    private SongDao songDao;

    public SongRepository(SongDao songDao) {
        this.songDao = songDao;
    }

    public LiveData<List<Song>> getSongsByPlaylistName(String playlist) {
        return songDao.getSongsByPlaylistName(playlist);
    }

    public LiveData<List<Song>> getAllSongs() {
        return songDao.getAllSongs();
    }

    public void insertSongs(Song... songs) {
        new insertAsyncTask(songDao).execute(songs);
    }

    public void updateSongs(Song... songs) {
        new updateAsyncTask(songDao).execute(songs);
    }

    public void deleteSongs(Song... songs) {
        songDao.deleteSong(songs);
    }

    public LiveData<Song> getSongById(String id) {
        return songDao.getSongById(id);
    }

    public void deleteSongById(String id) {
        songDao.deleteSongById(id);
    }

    public void deleteAllSongs() {
        new deleteAsyncTask(songDao).execute();
    }

    //AsyncTasks

    static class deleteAsyncTask extends AsyncTask<Void, Void, Void> {

        private SongDao mAsyncTaskDao;

        deleteAsyncTask(SongDao dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAllSongs();
            return null;
        }
    }

    static class insertAsyncTask extends AsyncTask<Song, Void, Void> {

        private SongDao mAsyncTaskDao;

        insertAsyncTask(SongDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Song... params) {
            mAsyncTaskDao.insertSong(params);
            return null;
        }
    }

    static class updateAsyncTask extends AsyncTask<Song, Void, Void> {

        private SongDao mAsyncTaskDao;

        updateAsyncTask(SongDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Song... params) {
            mAsyncTaskDao.updateSong(params);
            return null;
        }
    }
}
