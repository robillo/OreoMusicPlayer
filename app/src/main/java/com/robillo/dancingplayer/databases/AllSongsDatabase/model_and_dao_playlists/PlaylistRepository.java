package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.robillo.dancingplayer.models.Playlist;
import com.robillo.dancingplayer.models.Song;

import java.util.List;

public class PlaylistRepository {

    private PlaylistDao playlistDao;
    private LiveData<List<Playlist>> playlistItems;

    public PlaylistRepository(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
        playlistItems = this.playlistDao.getAllPlaylistItems();
    }

    public void insertPlaylistItem(Playlist... playlists) {
        new insertAsyncTask(playlistDao).execute(playlists);
    }

    public LiveData<List<Song>> getSongsByPlaylistName(String playlist) {
        return playlistDao.getSongsByPlaylistName(playlist);
    }

    public LiveData<List<String>> getDistinctPlaylistNames() {
        return playlistDao.getDistinctPlaylistNames();
    }

    public void deleteSongById(String id) {
        new deleteByIdAsyncTask(playlistDao).execute(id);
    }

    public void deleteAllInstancesOfPlaylist(String playlistName) {
        new removePlaylistAsyncTask(playlistDao).execute(playlistName);
    }

    public LiveData<List<Playlist>> getAllPlaylistItems() {
        return playlistItems;
    }

    public void removeSongFromPlaylist(String songId, String currentPlaylistTitle) {
        new removeFromPlaylistAsyncTask(playlistDao).execute(songId, currentPlaylistTitle);
    }

    public void changePlaylistName(String oldPlaylistName, String newPlaylistName) {
        new updateAsyncTask(playlistDao).execute(oldPlaylistName, newPlaylistName);
    }

    //AsyncTasks

    static class updateAsyncTask extends AsyncTask<String, Void, Void> {

        private PlaylistDao mAsyncTaskDao;

        updateAsyncTask(PlaylistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.changePlaylistName(params[0], params[1]);
            return null;
        }
    }

    static class deleteByIdAsyncTask extends AsyncTask<String, Void, Void> {

        private PlaylistDao mAsyncTaskDao;

        deleteByIdAsyncTask(PlaylistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteSongById(params[0]);
            return null;
        }
    }

    static class insertAsyncTask extends AsyncTask<Playlist, Void, Void> {

        private PlaylistDao mAsyncTaskDao;

        insertAsyncTask(PlaylistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Playlist... params) {
            mAsyncTaskDao.insertPlaylistItem(params);
            return null;
        }
    }

    static class removePlaylistAsyncTask extends AsyncTask<String, Void, Void> {

        private PlaylistDao mAsyncTaskDao;

        removePlaylistAsyncTask(PlaylistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            mAsyncTaskDao.deletePlaylistByPlaylistName(strings[0]);
            return null;
        }
    }

    static class removeFromPlaylistAsyncTask extends AsyncTask<String, Void, Void> {

        private PlaylistDao mAsyncTaskDao;

        removeFromPlaylistAsyncTask(PlaylistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            mAsyncTaskDao.removeSongFromPlaylist(strings[0], strings[1]);
            return null;
        }
    }
}
