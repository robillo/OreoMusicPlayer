package com.robillo.dancingplayer.databases.AllSongsDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played.MostPlayedDao;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistDao;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_songs.SongDao;
import com.robillo.dancingplayer.models.MostPlayed;
import com.robillo.dancingplayer.models.Playlist;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.utils.AppConstants;

@Database(entities = {Song.class, Playlist.class, MostPlayed.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class SongDatabase extends RoomDatabase {

    private static volatile SongDatabase instance;

    public static synchronized SongDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static SongDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                SongDatabase.class,
                AppConstants.SONG_DATABASE_NAME).build();
    }

    public abstract SongDao getSongDao();

    public abstract PlaylistDao getPlaylistDao();

    public abstract MostPlayedDao getMostPlayedDao();
}
