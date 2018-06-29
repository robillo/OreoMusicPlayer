package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistDao;
import com.robillo.dancingplayer.models.MostPlayed;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import java.util.List;

public class MostPlayedRepository {

    private MostPlayedDao mostPlayedDao;

    public MostPlayedRepository(MostPlayedDao mostPlayedDao) {
        this.mostPlayedDao = mostPlayedDao;
    }

    public void checkIfExistsAndInsert(MainActivity activity, String songId) {
        if(songId == null) return;

        LiveData<Integer> integerLiveData = mostPlayedDao.checkIfSongExists(songId);
        integerLiveData.observe(activity, integer -> {
            if(integer != null) {
                updateMostPlayedCount(songId, integer + 1);
            }
            else {
                insertMostPlayed(new MostPlayed(songId, 1));
            }
        });
    }

    private void updateMostPlayedCount(String songId, int playCount) {
        new updateMpAsyncTask(mostPlayedDao).execute(songId, String.valueOf(playCount));
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
            mAsyncTaskDao.updateMostPlayed(strings[0], Integer.valueOf(strings[1]));
            return null;
        }
    }
}
