package com.robillo.dancingplayer.databases.AllSongsDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao.Song;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao.SongDao;
import com.robillo.dancingplayer.utils.AppConstants;

@SuppressWarnings("unused")
@Database(entities = {Song.class}, version = 1)
public abstract class SongsDatabase extends RoomDatabase {

    private static volatile SongsDatabase instance;

    static synchronized SongsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static SongsDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                SongsDatabase.class,
                AppConstants.SONGS_DATABASE_NAME).build();
    }

    public abstract SongDao getSongDao();
}
