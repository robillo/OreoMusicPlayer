package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.robillo.dancingplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    private SongDao songDao;
    private LiveData<List<Song>> allSongs;

    public SongRepository(SongDao songDao) {
        this.songDao = songDao;
        allSongs = this.songDao.getAllSongs();
    }

    LiveData<List<Song>> getAllSongs() {
        return allSongs;
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

    //AsyncTasks

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
