package com.robillo.dancingplayer.views.activities.main;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.databases.AllSongsDatabase.SongDatabase;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played.MostPlayedRepository;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistRepository;
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_songs.SongRepository;
import com.robillo.dancingplayer.events.PlayerStateNoSongPlayingEvent;
import com.robillo.dancingplayer.events.SongChangeEvent;
import com.robillo.dancingplayer.models.Playlist;
import com.robillo.dancingplayer.models.PlaylistRowItem;
import com.robillo.dancingplayer.models.SetSeekBarEvent;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.events.ThemeChangeEvent;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.preferences.PreferencesHelper;
import com.robillo.dancingplayer.services.MusicService;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.utils.ApplicationUtils;
import com.robillo.dancingplayer.views.activities.main.EditDialogFragment.EditDialogFragment;
import com.robillo.dancingplayer.views.activities.main.adapters.PlaylistAdapter;
import com.robillo.dancingplayer.views.activities.main.bottom_sheet.BottomSheetFragment;
import com.robillo.dancingplayer.views.activities.main.song_list_frag.SongsListFragment;
import com.robillo.dancingplayer.views.activities.main.song_play_frag.SongPlayFragmentSheet;
import com.robillo.dancingplayer.views.activities.main.songs_sort_frag.SongsSortFragment;
import com.robillo.dancingplayer.views.activities.theme_change.ThemeChangeActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.robillo.dancingplayer.utils.AppConstants.ALBUM;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_ID;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_ID;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.COMPOSER;
import static com.robillo.dancingplayer.utils.AppConstants.CREATE_NEW_PLAYLIST;
import static com.robillo.dancingplayer.utils.AppConstants.DATA;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED;
import static com.robillo.dancingplayer.utils.AppConstants.DURATION;
import static com.robillo.dancingplayer.utils.AppConstants.FIRST_LOAD;
import static com.robillo.dancingplayer.utils.AppConstants.FROM;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_ACTIVITY;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_BOTTOM_CONTROLLER;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_FRAGMENT;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_SONGS_LIST;
import static com.robillo.dancingplayer.utils.AppConstants.ID;
import static com.robillo.dancingplayer.utils.AppConstants.INDEX;
import static com.robillo.dancingplayer.utils.AppConstants.MODIFY;
import static com.robillo.dancingplayer.utils.AppConstants.OLD_PLAYLIST_NAME;
import static com.robillo.dancingplayer.utils.AppConstants.POSITION;
import static com.robillo.dancingplayer.utils.AppConstants.REQUEST_CODE;
import static com.robillo.dancingplayer.utils.AppConstants.SIZE;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.YEAR;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    BottomSheetBehavior playlistSheetBehavior;
    BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private ThemeColors currentUserThemeColors = null;
    private PreferencesHelper helper = null;
    private List<PlaylistRowItem> playlistRowItems = new ArrayList<>();
    private SongDatabase songDatabase;
    private SongRepository songRepository;
    private PlaylistRepository playlistRepository;
    private MostPlayedRepository mostPlayedRepository;
    private LiveData<List<Song>> listLiveData;

    @SuppressWarnings("FieldCanBeLocal")
    private Song currentSong = null;

    @BindView(R.id.fragment_container)
    FrameLayout mFragmentContainer;

    @BindView(R.id.bottom_sheet)
    LinearLayout layoutPlaylistBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setUp();
    }

    public void startServiceForAudioList(final ArrayList<Song> songList) {

        //connect to the service
        ServiceConnection musicConnection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
                //get service
                musicService = binder.getService();
                //pass list
                musicService.setSongsList(songList);
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void playSong(int songIndex) {
        if(musicService != null) musicService.setSong(songIndex);
    }

    public void setUp() {
        songDatabase = SongDatabase.getInstance(this);
        if(helper == null) helper = new PreferencesHelper(this);
        selectedPlaylist = new ApplicationUtils().convertStringToPlaylistRowItem(helper.getCurrentPlaylistTitle());

        songRepository = getSongRepository();
        songRepository.deleteAllSongs();

        refreshForUserThemeColors();
        setSongListFragment();
        setPlaylistBottomSheet();
    }

    public void putSongsListIntoDatabase(List<Song> audioList) {
        if (songRepository == null) songRepository = getSongRepository();

        //noinspection ToArrayCallWithZeroLengthArrayArgument
        songRepository.insertSongs(audioList.toArray(new Song[audioList.size()]));

        loadSongsForSelectedPlaylistFromDb();
    }

    public void loadSongsForSelectedPlaylistFromDb() {

        PreferencesHelper helper = new PreferencesHelper(this);

        listLiveData = songRepository
                .getAllSongs(
                        selectedPlaylist.getTitle(),
                        helper.getSortOrderForSongs(),
                        helper.getRecentlyAddedCount(),
                        helper.getRecentlyPlayedCount(),
                        helper.getMostPlayedCount()
                );

        listLiveData.observe(this, songs -> {
            if(songs != null && songs.size() > 0) {
                updateRecyclerViewForLoadedPlaylist(songs, songs.size());
                startMusicServiceForCurrentPlaylist(songs);
            }
            else {
                updateRecyclerViewForLoadedPlaylist(songs, -1);
            }
        });
    }

    public void removeObservers() {
        if(listLiveData != null) listLiveData.removeObservers(this);
    }

    public void removeSongCurrentPlaylist(Song song, int index) {
        if(songRepository == null) songRepository = getSongRepository();
        if(playlistRepository == null) playlistRepository = getPlaylistRepository();

        Song serviceSong = getMusicService().getSong();
        if(serviceSong != null && serviceSong.getId().equals(song.getId())) {
            playNextSong();
            getMusicService().cancelNotification();
        }
        getMusicService().removeSongFromListInMusicServiceById(song.getId());

        playlistRepository.removeSongFromPlaylist(song.getId(), new PreferencesHelper(this).getCurrentPlaylistTitle());

        LiveData<Integer> liveData = songRepository.getNumRows();
        liveData.observe(this, integer -> {
            refreshSongListFragmentForSongDelete(song, index);
            Toast.makeText(this, "Song Removed From Playlist", Toast.LENGTH_SHORT).show();
            liveData.removeObservers(this);
        });
    }

    public void updateRecyclerViewForLoadedPlaylist(List<Song> audioList, int size) {
        SongsListFragment fragment = (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

        if(fragment != null) {
            fragment.renderRecyclerViewForAudioList(audioList);
            if(size < 0) {
                fragment.showErrorLayout();
            }
            else fragment.hideErrorLayout();
        }
    }

    public void addSongToPlaylist(String songId, String playlist) {
        if(songDatabase == null) songDatabase = SongDatabase.getInstance(this);

        if(playlistRepository == null) getPlaylistRepository();

        LiveData<List<Song>> listLiveData = getPlaylistRepository().getSongsByPlaylistName(playlist);

        listLiveData.observe(this, songs -> {
            boolean isAlreadyPresent = false;
            if(songs != null) {
                for(Song s : songs) {
                    if(s.getId().equals(songId)) {
                        isAlreadyPresent = true;
                        Toast.makeText(this, getString(R.string.song_present_in_playlist), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
            if(!isAlreadyPresent) {
                getPlaylistRepository().insertPlaylistItem(new Playlist(songId, playlist));
                Toast.makeText(this, getString(R.string.song_added_to_playlist), Toast.LENGTH_SHORT).show();
            }
            listLiveData.removeObservers(this);
        });
    }

    public void startMusicServiceForCurrentPlaylist(List<Song> audioList) {
        audioList.add(0, new Song());
        audioList.add(new Song());
        audioList.add(new Song());

        if(getMusicService() == null)
            startServiceForAudioList(new ArrayList<>(audioList));
        else {
            updateServiceList(new ArrayList<>(audioList));
        }
    }

    public SongRepository getSongRepository() {
        if(songDatabase == null)
            songDatabase = SongDatabase.getInstance(this);

        return new SongRepository(songDatabase.getSongDao());
    }

    public MostPlayedRepository getMostPlayedRepository() {
        if(songDatabase == null) songDatabase = SongDatabase.getInstance(this);

        return new MostPlayedRepository(songDatabase.getMostPlayedDao());
    }

    public PlaylistRepository getPlaylistRepository() {
        if(songDatabase == null)
            songDatabase = SongDatabase.getInstance(this);

        return new PlaylistRepository(songDatabase.getPlaylistDao());
    }

    public void refreshForUserThemeColors() {

        helper = new PreferencesHelper(this);
        currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, currentUserThemeColors.getColorPrimaryDark()));
    }

    public void startThemeChangeActivity() {
        startActivityForResult(new Intent(this, ThemeChangeActivity.class), REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                rescanDevice();
            }
            else //noinspection StatementWithEmptyBody
                if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void showSnackBar(String text) {
        Snackbar.make(
                    findViewById(R.id.coordinator_layout), text, Snackbar.LENGTH_SHORT
                ).show();
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public void updateServiceList(ArrayList<Song> updatedAudioList) {
        musicService.setSongPosn(0);
        musicService.updateAudioList(updatedAudioList);
    }

    public int getCurrentSongDuration() {
        if(musicService!=null && musicBound) return musicService.getPosition();
        else return 0;
    }

    public void rescanDevice() {
        if(getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list)) != null){
            SongsListFragment fragment = (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));
            if (fragment != null) {
                fragment.fetchSongsAsync(FROM_ACTIVITY);
            }
        }
    }

    public void refreshSongListFragmentForSongDelete(Song song, int index) {
        SongsListFragment fragment = (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));
        if(fragment != null){
            fragment.refreshSongListFragmentForSongDelete(index);
        }
    }

    public void removeSongFromDb(String songId) {
        if(songRepository == null) songRepository = getSongRepository();
        if(playlistRepository == null) playlistRepository = getPlaylistRepository();

        songRepository.deleteSongById(songId);
        playlistRepository.deleteSongById(songId);
    }

    public void showSongOptionsOnBottomSheet(Song song, int index) {
        if (bottomSheetFragment != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(INDEX, index);

            bundle.putString(DATA, song.getData());
            bundle.putString(TITLE, song.getTitle());
            bundle.putString(TITLE_KEY, song.getTitleKey());
            bundle.putString(ID, song.getId());
            bundle.putString(DATE_ADDED, song.getDateAdded());
            bundle.putString(DATE_MODIFIED, song.getDateModified());
            bundle.putString(DURATION, song.getDuration());
            bundle.putString(COMPOSER, song.getComposer());
            bundle.putString(ALBUM, song.getAlbum());
            bundle.putString(ALBUM_ID, song.getAlbumId());
            bundle.putString(ALBUM_KEY, song.getAlbumKey());
            bundle.putString(ARTIST, song.getArtist());
            bundle.putString(ARTIST_ID, song.getArtistId());
            bundle.putString(ARTIST_KEY, song.getArtistKey());
            bundle.putString(SIZE, song.getSize());
            bundle.putString(YEAR, song.getYear());
            bottomSheetFragment.setArguments(bundle);

            if(playlistSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                hidePlaylistBottomSheet();
                new Handler().postDelayed(() ->  {
                    hideOrRemoveBottomSheet();
                    if(!bottomSheetFragment.isAdded())
                        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                }, 350);
            }
            else {
                if(!bottomSheetFragment.isAdded())
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }

        }
        else {
            bottomSheetFragment = new BottomSheetFragment();
            showSongOptionsOnBottomSheet(song, index);
        }
    }

    public void hideOrRemoveBottomSheet() {
        if(bottomSheetFragment != null) {
            if(!bottomSheetFragment.isHidden()) {
                //noinspection ConstantConditions
                if(bottomSheetFragment != null) bottomSheetFragment.dismiss();
            }
        }
    }

    public void removeSongFromListInMusicService(Song song) {
        MusicService service = getMusicService();
        if(service != null) service.removeSongFromList(song);
    }

    public void hideSongPlayFragment(SongPlayFragmentSheet fragmentSheet) {
        if(fragmentSheet != null && fragmentSheet.isAdded() && fragmentSheet.isVisible()) fragmentSheet.dismiss();
    }

    public void setSongListFragment() {
        SongsListFragment fragment = new SongsListFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(mFragmentContainer.getId(), fragment, getString(R.string.songs_list));
        transaction.commitAllowingStateLoss();

        fragment.fetchSongsAsync(FROM_FRAGMENT);
    }

    public void setSongPlayFragment() {

        SongPlayFragmentSheet fragmentSheet = (SongPlayFragmentSheet) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

        if(fragmentSheet == null) new SongPlayFragmentSheet().show(getSupportFragmentManager(), getString(R.string.song_play));
        else
            if(!fragmentSheet.isAdded() && fragmentSheet.isHidden()) fragmentSheet.show(getSupportFragmentManager(), getString(R.string.song_play));
    }

    public void setSongsSortFragment() {
        if(getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_sort)) == null){

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.songs_sort_fade_in, 0, 0, 0);
            transaction.add(mFragmentContainer.getId(), new SongsSortFragment(), getString(R.string.songs_sort));
            transaction.addToBackStack(getString(R.string.songs_sort));
            transaction.commit();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SongsListFragment fragment = (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

        if(fragment != null) {
            if(currentSong != null) {
                fragment.setCurrentSong(currentSong);

                if(new PreferencesHelper(this).isPlayEvent())
                    fragment.playPlayer(FROM_ACTIVITY);
                else
                    fragment.pausePlayer(FROM_ACTIVITY);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        Fragment songPlayFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));
        Fragment songsSortFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_sort));

        if(songPlayFragment != null) {                                  //remove song play fragment from back stack
            super.onBackPressed();
        }
        else if(songsSortFragment != null) {
            super.onBackPressed();
        }
        else if(playlistSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            hidePlaylistBottomSheet();
        }
        else {                                                          //don't remove song list fragment from activity
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }
    }

    @Override
    protected void onDestroy() {
        if(musicService!=null) musicService.onDestroy();
        if(playIntent!=null) {
            stopService(playIntent);
        }
        super.onDestroy();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ThemeChangeEvent event) {

        SongsListFragment songListFragment =
                (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

        if(songListFragment != null) {
            refreshForUserThemeColors();
            songListFragment.applyUserTheme(
                    currentUserThemeColors,
                    new PreferencesHelper(this).getUserThemeName()
            );
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SongChangeEvent event) {

        if(event.getSong() != null) {

            if(mostPlayedRepository == null) mostPlayedRepository = getMostPlayedRepository();
            //TODO: was changed
//            mostPlayedRepository.checkIfExistsAndInsertMostPlayed(this, event.getSong().getId());

            currentSong = event.getSong();

            SongsListFragment songListFragment =
                    (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

            if(songListFragment != null) {
                songListFragment.setCurrentSong(currentSong);

                PreferencesHelper helper = new PreferencesHelper(this);

                if(helper.isPlayEvent())
                    songListFragment.playPlayer(FROM_ACTIVITY);
                else
                    songListFragment.pausePlayer(FROM_ACTIVITY);
            }

            SongPlayFragmentSheet songPlayFragment = (SongPlayFragmentSheet) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

            if(songPlayFragment != null) {
                songPlayFragment.setCurrentSong(currentSong);

                PreferencesHelper helper = new PreferencesHelper(this);

                if(helper.isPlayEvent()) songPlayFragment.playPlayer(FROM_ACTIVITY);
                else songPlayFragment.pausePlayer(FROM_ACTIVITY);
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SetSeekBarEvent event) {

        SongPlayFragmentSheet fragment = (SongPlayFragmentSheet) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

        if(fragment != null) {
            fragment.setDurationValues(event.getCurrentDuration(), event.getTotalDuration()/1000);
        }

        EventBus.getDefault().removeStickyEvent(event);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PlayerStateNoSongPlayingEvent event) {

        if(currentSong != null) {

            FragmentManager manager = getSupportFragmentManager();

            SongPlayFragmentSheet playFragment = (SongPlayFragmentSheet) manager.findFragmentByTag(getString(R.string.song_play));
            SongsListFragment listFragment = (SongsListFragment) manager.findFragmentByTag(getString(R.string.songs_list));
            if(playFragment != null && playFragment.isVisible()) playFragment.dismiss();

            if(listFragment != null) {
                currentSong = null;
                listFragment.setCurrentSong(null);
                if(listFragment.getControllerVisibility() == View.VISIBLE) listFragment.fadeOutController();

                Toast toast = Toast.makeText(this, getString(R.string.end_of_list), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 10);
                toast.show();
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    public void playNextSong() {
        musicService.playNext();
    }

    public void playPreviousSong() {
        musicService.playPrevious();
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void toggleRepeatModeInService(String value) {
        if(musicService != null)
            musicService.toggleRepeatMode(value);
    }

    public void toggleShuffleModeInService() {
        if(musicService != null)
            musicService.toggleShuffleMode();
    }

    public void seekTenSecondsForward() {
        if(musicService != null)
            musicService.seekTenSecondsForward();
    }

    public void seekTenSecondsBackwards() {
        if(musicService != null)
            musicService.seekTenSecondsBackwards();
    }

    public void repopulateListSongsListFragment() {
        SongsListFragment fragment =
                (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

        if(fragment != null) {
            fragment.fetchSongsAsync(FROM_FRAGMENT);
        }
    }

    @Override
    public void start() {
        PreferencesHelper helper = new PreferencesHelper(this);
        helper.setIsPlayEvent(true);

        musicService.playPlayer();
    }

    @Override
    public void pause() {
        PreferencesHelper helper = new PreferencesHelper(this);
        helper.setIsPlayEvent(false);

        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicService!=null && musicBound) return musicService.getDuration();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound) return musicService.getPosition();
        else return 0;
    }

    @Override
    public void seekTo(int position) {
        musicService.seekPlayer(position);
    }

    @Override
    public boolean isPlaying() {
        return musicService != null && musicService.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    //code for playlist bottom sheet

    private PlaylistRowItem selectedPlaylist = null;

    @BindView(R.id.create_new_playlist)
    Button createNewPlaylist;

    @BindView(R.id.selected_playlist)
    TextView selectedPlaylistTv;

    @BindView(R.id.up_down_fragment_playlist)
    ImageView imageUpDownPlaylist;

    @BindView(R.id.layout_lift_burrow_playlists)
    LinearLayout liftBurrowPlaylists;

    @BindView(R.id.playlist_recycler_view)
    RecyclerView playlistRecyclerView;

    @OnClick(R.id.layout_lift_burrow_playlists)
    public void setLiftBurrowPlaylists() {
        togglePlaylistBottomSheet();
    }

    public void setPlaylistBottomSheet() {
        loadPlaylistItems(FIRST_LOAD, null);
        setBehaviorCallbacks();
        setCurrentPlaylistAsHeader();
        loadPlaylistsIntoRecyclerView(FROM_BOTTOM_CONTROLLER, null);
    }

    public void loadPlaylistItems(int from, String songId) {
        if(playlistRepository == null) playlistRepository = getPlaylistRepository();

        ApplicationUtils utils = new ApplicationUtils();

        LiveData<List<String>> liveData = playlistRepository.getDistinctPlaylistNames();

        liveData.observe(this, strings -> {

            playlistRowItems = new ArrayList<>();
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.DEFAULT_PLAYLIST_TITLE));
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.RECENTLY_ADDED));
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.RECENTLY_PLAYED));
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.MOST_PLAYED));

            if(strings != null)
                for(String s : strings)
                    playlistRowItems.add(utils.convertStringToPlaylistRowItem(s));

            loadSongsInRvAfterRowItemsLoaded(from, songId);
        });
    }

    public void loadSongsInRvAfterRowItemsLoaded(int from, String songId) {
        List<PlaylistRowItem> itemsToDisplay = new ArrayList<>();

        for(PlaylistRowItem item : playlistRowItems)  {
            if(!item.getTitle().equals(selectedPlaylist.getTitle())) {
                itemsToDisplay.add(item);
            }
        }

        PlaylistAdapter playlistAdapter = new PlaylistAdapter(itemsToDisplay, this, from, songId);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(from == FIRST_LOAD) hidePlaylistBottomSheet();

        playlistRecyclerView.setAdapter(playlistAdapter);
    }

    public void deleteSong(int recyclerIndex, Song deleteSong) {
        if(songRepository == null) songRepository = getSongRepository();
        if(playlistRepository == null) playlistRepository = getPlaylistRepository();

        deleteSongFromSongRepository(deleteSong.getId());
        deleteSongFromPlaylistRepository(deleteSong.getId());
        changeSongIfSameAsCurrentlyPlaying(deleteSong);
        deleteFromDevice(deleteSong);

        refreshSongListFragmentForSongDelete(deleteSong, recyclerIndex);
        hideOrRemoveBottomSheet();
    }

    private void deleteSongFromSongRepository(String songId) {
        songRepository.deleteSongById(songId);
    }

    private void deleteSongFromPlaylistRepository(String songId) {
        playlistRepository.deleteSongById(songId);
    }

    private void changeSongIfSameAsCurrentlyPlaying(Song deleteSong) {
        Song currentSong = getMusicService().getSong();
        if(currentSong != null && currentSong.getId().equals(deleteSong.getId())) {
            getMusicService().cancelNotification();
            removeSongFromListInMusicService(deleteSong);
            playNextSong();
        }
        else removeSongFromListInMusicService(deleteSong);
    }

    private void deleteFromDevice(Song deleteSong) {

        boolean isInfoDeleted = false, isFileDeleted = false;

        int numRows = getContentResolver().delete(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID + "=?",
                new String[] { deleteSong.getId() });

        if(numRows > 0) isInfoDeleted = true;

        File deleteFile = new File(deleteSong.getData());
        if (deleteFile.exists())
            isFileDeleted = deleteFile.delete();

        if(isInfoDeleted && !isFileDeleted)
            Toast.makeText(this,
                    String.format(Locale.ENGLISH, "%s %s", getString(R.string.song_details), getString(R.string.deleted)),
                    Toast.LENGTH_SHORT).show();

        if(!isInfoDeleted && isFileDeleted)
            Toast.makeText(this,
                    String.format(Locale.ENGLISH, "%s %s", getString(R.string.song), getString(R.string.deleted)),
                    Toast.LENGTH_SHORT).show();

        if(isInfoDeleted && isFileDeleted)
            Toast.makeText(this,
                    String.format(
                            Locale.ENGLISH, "%s and %s %s",
                            getString(R.string.song),
                            getString(R.string.song_details),
                            getString(R.string.deleted)),
                    Toast.LENGTH_SHORT).show();

        if(!isInfoDeleted && !isFileDeleted)
            Toast.makeText(this, getString(R.string.deletion_error), Toast.LENGTH_SHORT).show();
    }

    public void setBehaviorCallbacks() {
        playlistSheetBehavior = BottomSheetBehavior.from(layoutPlaylistBottomSheet);

        playlistSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        imageUpDownPlaylist.animate().rotation(180).start();
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        imageUpDownPlaylist.animate().rotation(0).start();
                        createNewPlaylist.setVisibility(View.VISIBLE);
                        loadPlaylistsIntoRecyclerView(FROM_BOTTOM_CONTROLLER, null);
                    }
                    break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING: break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        });
    }

    public void setCurrentPlaylistAsHeader() {
        if(selectedPlaylist == null)
            selectedPlaylist = new ApplicationUtils().convertStringToPlaylistRowItem(helper.getCurrentPlaylistTitle());

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.item_animation_fall_down);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                selectedPlaylistTv.setText(selectedPlaylist.getTitle());
            }

            @Override
            public void onAnimationEnd(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        selectedPlaylistTv.startAnimation(animation);
    }

    public void loadPlaylistsIntoRecyclerView(int from, String songId) {
        loadPlaylistItems(from, songId);
    }

    public void updatePlaylistListForSelectedItem(PlaylistRowItem playlistRowItem, int position) {

        selectedPlaylist = playlistRowItem;
        helper.setCurrentPlaylistTitle(selectedPlaylist.getTitle());
        setCurrentPlaylistAsHeader();

        loadPlaylistsIntoRecyclerView(FIRST_LOAD, null);

        if(songRepository == null) songRepository = getSongRepository();

        loadSongsForSelectedPlaylistFromDb();
    }

    public void hidePlaylistBottomSheet() {
        if (playlistSheetBehavior != null && playlistSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            playlistSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            imageUpDownPlaylist.animate().rotation(0).start();
        }
    }

    public void showPlaylistBottomSheet(int from, String songId) {
        if(from == FROM_BOTTOM_CONTROLLER) {
            if (playlistSheetBehavior != null && playlistSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                playlistSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                imageUpDownPlaylist.animate().rotation(180).start();
            }
        }
        else if(from == FROM_SONGS_LIST) {
            createNewPlaylist.setVisibility(View.GONE);
            if (playlistSheetBehavior != null && playlistSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                new Handler().postDelayed(() -> {
                    playlistSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    imageUpDownPlaylist.animate().rotation(180).start();
                }, 100);
            }
        }

        loadPlaylistsIntoRecyclerView(from, songId);
    }

    public void showEditCreateDialogFragment(int from, int position, String oldPlaylistName) {
        EditDialogFragment fragment = new EditDialogFragment();
        Bundle args = new Bundle();
        args.putInt(FROM, from);
        args.putInt(POSITION, position);
        args.putString(OLD_PLAYLIST_NAME, oldPlaylistName);
        fragment.setArguments(args);
        fragment.show(getFragmentManager(), getString(R.string.edit_dialog_fragment));
    }

    public void handleCreateNewPlaylist(String playlistName) {
        if(playlistRepository == null) playlistRepository = getPlaylistRepository();
        playlistRepository.insertPlaylistItem(new Playlist(null, playlistName));

        loadPlaylistsIntoRecyclerView(MODIFY, null);

        Toast.makeText(this, getString(R.string.playlist_created), Toast.LENGTH_SHORT).show();
    }

    public void handleEditPlaylistName(String newPlaylistName, int position, String oldPlaylistName) {
        if(playlistRepository == null) playlistRepository = getPlaylistRepository();
        playlistRepository.changePlaylistName(oldPlaylistName, newPlaylistName);

        loadPlaylistsIntoRecyclerView(MODIFY, null);

        Toast.makeText(this, getString(R.string.playlist_modified), Toast.LENGTH_SHORT).show();
    }

    public void handleDeletePlaylist(String playlistName) {
        deletePlaylistInDb(playlistName);

        loadPlaylistsIntoRecyclerView(MODIFY, null);

        Toast.makeText(this, getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show();
    }

    public void deletePlaylistInDb(String playlistName) {
        if(playlistRepository == null) playlistRepository = getPlaylistRepository();

        playlistRepository.deleteAllInstancesOfPlaylist(playlistName);
    }

    public void togglePlaylistBottomSheet() {
        if (playlistSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            playlistSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            imageUpDownPlaylist.animate().rotation(180).start();
        } else {
            playlistSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            imageUpDownPlaylist.animate().rotation(0).start();
        }
    }

    @OnClick(R.id.create_new_playlist)
    public void createNewPlaylist() {
        showEditCreateDialogFragment(CREATE_NEW_PLAYLIST, -1, "");
    }
}