package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_songs;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.utils.AppConstants;

import java.util.List;

public class SongRepository {

    private SongDao songDao;

    public SongRepository(SongDao songDao) {
        this.songDao = songDao;
    }

    public LiveData<List<Song>> getSongsByPlaylistName(String playlist, String sortOrder) {
        return songDao.getSongsPlaylist(playlist);
    }

    private boolean isDefault(String title) {
        return title.equals(AppConstants.DEFAULT_PLAYLIST_TITLE);
    }

    private boolean isRecentlyAdded(String title) {
        return title.equals(AppConstants.RECENTLY_ADDED);
    }

    private boolean isRecentlyPlayed(String title) {
        return title.equals(AppConstants.RECENTLY_PLAYED);
    }

    private boolean isMostPlayed(String title) {
        return title.equals(AppConstants.MOST_PLAYED);
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
        return songDao.getSongId(id);
    }

    public void deleteSongById(String id) {
        new deleteByIdAsyncTask(songDao).execute(id);
    }

    public LiveData<Integer> getNumRows() {
        return songDao.getNumberOfRows();
    }

    public void deleteAllSongs() {
        new deleteAsyncTask(songDao).execute();
    }

    //AsyncTasks

    static class deleteAsyncTask extends AsyncTask<Void, Void, LiveData<Integer>> {

        private SongDao mAsyncTaskDao;

        deleteAsyncTask(SongDao dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected LiveData<Integer> doInBackground(Void... voids) {
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

    static class deleteByIdAsyncTask extends AsyncTask<String, Void, Void> {

        private SongDao mAsyncTaskDao;

        deleteByIdAsyncTask(SongDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteSongId(params[0]);
            return null;
        }
    }

    public LiveData<List<Song>> getAllSongs(String playlist, String sortOrder, int ra, int rp, int mp) {
        switch (sortOrder) {
            case AppConstants.SIZE_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsSizeA();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistSizeA(playlist);
            }
            case AppConstants.SIZE_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsSizeD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistSizeD(playlist);
            }
            case AppConstants.YEAR_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsYearA();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistYearA(playlist);
            }
            case AppConstants.YEAR_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsYearD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistYearD(playlist);
            }
            case AppConstants.ALBUM_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsAlbumA();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistAlbumA(playlist);
            }
            case AppConstants.ALBUM_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsAlbumD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistAlbumD(playlist);
            }
            case AppConstants.TITLE_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsTitleA();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistTitleA(playlist);
            }
            case AppConstants.TITLE_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsTitleD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistTitleD(playlist);
            }
            case AppConstants.ARTIST_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsArtistA();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistArtistA(playlist);
            }
            case AppConstants.ARTIST_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsArtistD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistArtistD(playlist);
            }
            case AppConstants.DURATION_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsDurationA();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistDurationA(playlist);
            }
            case AppConstants.DURATION_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsDurationD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistDurationD(playlist);
            }
            case AppConstants.DATE_ADDED_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsDateAddedD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistDateAddedD(playlist);
            }
            case AppConstants.DATE_ADDED_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsDateAddedD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistDateAddedD(playlist);
            }
            case AppConstants.DATE_MODIFIED_ASCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsDateModifiedA();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistDateModifiedA(playlist);
            }
            case AppConstants.DATE_MODIFIED_DESCENDING: {
                if(isDefault(playlist)) return songDao.getAllSongsDateModifiedD();
                else if(isRecentlyAdded(playlist)) return getRecentlyAddedSongs(sortOrder, ra);
                else if(isMostPlayed(playlist)) return getMostPlayedSongs(sortOrder, ra);
                else return songDao.getSongsPlaylistDateModifiedD(playlist);
            }
        }
        return songDao.getAllSongs();
    }

    public LiveData<List<Song>> getRecentlyAddedSongs(String sortOrder, int limit) {
        return songDao.getRecentlyAddedSongs(limit);
    }

    public LiveData<List<Song>> getMostPlayedSongs(String sortOrder, int limit) {
        return songDao.getMostPlayedSongs(limit);
    }

    public LiveData<List<Song>> getRecentlyPlayedSongs(String sortOrder, int limit) {
        return songDao.getRecentlyPlayedSongs(limit);
    }
}
