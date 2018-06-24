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

    public LiveData<List<Playlist>> getAllPlaylistItems() {
        return playlistItems;
    }

    //AsyncTasks

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
}
