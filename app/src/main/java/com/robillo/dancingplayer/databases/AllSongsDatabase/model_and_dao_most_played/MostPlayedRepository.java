package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistDao;
import com.robillo.dancingplayer.models.MostPlayed;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import java.util.Date;
import java.util.List;

public class MostPlayedRepository {

    private MostPlayedDao mostPlayedDao;

    public MostPlayedRepository(MostPlayedDao mostPlayedDao) {
        this.mostPlayedDao = mostPlayedDao;
    }

    public void checkIfExistsAndInsertMostPlayed(MainActivity activity, String songId) {
        if(songId == null) return;

        LiveData<Integer> integerLiveData = mostPlayedDao.checkIfSongExists(songId);
        integerLiveData.observe(activity, integer -> {
            if(integer != null) updateMostPlayedCount(songId, integer + 1, new Date().getTime());
            else insertMostPlayed(new MostPlayed(songId, 1, new Date().getTime()));

            integerLiveData.removeObservers(activity);
        });
    }

    public void checkIfExistsAndInsertRecentlyPlayed(MainActivity activity, String songId) {
        if(songId == null) return;

        LiveData<Integer> integerLiveData = mostPlayedDao.checkIfSongExists(songId);
        integerLiveData.observe(activity, integer -> {
            if(integer != null) updateMostPlayedCount(songId, integer + 1, new Date().getTime());
            else insertMostPlayed(new MostPlayed(songId, 1, new Date().getTime()));

            integerLiveData.removeObservers(activity);
        });
    }

    private void updateMostPlayedCount(String songId, int playCount, Long timeSince70) {
        new updateMpAsyncTask(mostPlayedDao).execute(songId, String.valueOf(playCount), String.valueOf(timeSince70));
    }

    private void updateRecentlyPlayedCount(String songId, long timeSince70) {
        new updateRpAsyncTask(mostPlayedDao).execute(songId, String.valueOf(timeSince70));
    }

    private void insertMostPlayed(MostPlayed mostPlayed) {
        new insertMpAsyncTask(mostPlayedDao).execute(mostPlayed);
    }

    public LiveData<List<Song>> getMostPlayedSongs(String sortOrder, int limit) {
        return mostPlayedDao.getMostPlayedSongs(limit);
    }

    static class insertMpAsyncTask extends AsyncTask<MostPlayed, Void, Void> {

        private MostPlayedDao mAsyncTaskDao;

        insertMpAsyncTask(MostPlayedDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(MostPlayed... mostPlayeds) {
            mAsyncTaskDao.insertMostPlayed(mostPlayeds);
            return null;
        }
    }

    static class updateMpAsyncTask extends AsyncTask<String, Void, Void> {

        private MostPlayedDao mAsyncTaskDao;

        updateMpAsyncTask(MostPlayedDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            mAsyncTaskDao.updateMostPlayed(strings[0], Integer.valueOf(strings[1]), Long.valueOf(strings[2]));
            return null;
        }
    }

    static class updateRpAsyncTask extends AsyncTask<String, Void, Void> {

        private MostPlayedDao mAsyncTaskDao;

        updateRpAsyncTask(MostPlayedDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            mAsyncTaskDao.updateRecentlyPlayed(strings[0], Long.valueOf(strings[1]));
            return null;
        }
    }
}
