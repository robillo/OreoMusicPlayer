package com.robillo.dancingplayer.views.activities.main;

import android.support.v4.app.FragmentActivity;

import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played.MostPlayedRepository;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistRepository;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_songs.SongRepository;
import com.robillo.dancingplayer.models.PlaylistRowItem;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.services.MusicService;
import com.robillo.dancingplayer.views.activities.main.song_play_frag.SongPlayFragmentSheet;

import java.util.ArrayList;
import java.util.List;

public interface MainActivityMvpView {

    void setUp();

    void setSongListFragment();

    void setSongPlayFragment();

    void setSongsSortFragment();

    void startServiceForAudioList(ArrayList<Song> songList);

    void playSong(int songIndex);

    void playNextSong();

    void playPreviousSong();

    Song getCurrentSong();

    void toggleRepeatModeInService(String value);

    void toggleShuffleModeInService();

    void seekTenSecondsForward();

    void seekTenSecondsBackwards();

    void repopulateListSongsListFragment();

    void refreshForUserThemeColors();

    void startThemeChangeActivity();

    void showSnackBar(String text);

    MusicService getMusicService();

    void updateServiceList(ArrayList<Song> updatedAudioList);

    int getCurrentSongDuration();

    void rescanDevice();

    void showSongOptionsOnBottomSheet(Song song, int index);

    void hideOrRemoveBottomSheet();

    void removeSongFromListInMusicService(Song song);

    void hideSongPlayFragment(SongPlayFragmentSheet fragmentSheet);

    void refreshSongListFragmentForSongDelete(Song song, int index);

    //playlist bottom sheet calls

    void loadPlaylistItems(int from, String songId);

    void togglePlaylistBottomSheet();

    void setPlaylistBottomSheet();

    void  setCurrentPlaylistAsHeader();

    void loadPlaylistsIntoRecyclerView(int from, String songId);

    void setBehaviorCallbacks();

    void updatePlaylistListForSelectedItem(PlaylistRowItem playlistRowItem, int position);

    void hidePlaylistBottomSheet();

    void showPlaylistBottomSheet(int from, String songId);

    void showEditCreateDialogFragment(int from, int position, String oldPlaylistName);

    void handleCreateNewPlaylist(String playlistName);

    void handleEditPlaylistName(String newPlaylistName, int position, String oldPlaylistName);

    void handleDeletePlaylist(String playlistName);

    void deletePlaylistInDb(String playlistName);

    //database handling

    void putSongsListIntoDatabase(List<Song> audioList);

    void startMusicServiceForCurrentPlaylist(List<Song> audioList);

    SongRepository getSongRepository();

    PlaylistRepository getPlaylistRepository();

    MostPlayedRepository getMostPlayedRepository();

    void updateRecyclerViewForLoadedPlaylist(List<Song> audioList);

    void addSongToPlaylist(String songId, String playlist);

    void loadSongsForSelectedPlaylistFromDb();

    void removeObservers();

    void removeSongCurrentPlaylist(Song song, int index);

    void removeSongFromDb(String songId);

    void loadSongsInRvAfterRowItemsLoaded(int from, String songId);

    void deleteSong(FragmentActivity activity, int index, Song song, String id);
}
