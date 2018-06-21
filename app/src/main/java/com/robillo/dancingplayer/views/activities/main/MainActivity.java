package com.robillo.dancingplayer.views.activities.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.events.PlayerStateNoSongPlayingEvent;
import com.robillo.dancingplayer.events.SongChangeEvent;
import com.robillo.dancingplayer.models.SetSeekBarEvent;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.models.ThemeChangeEvent;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.services.MusicService;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.views.activities.main.bottom_sheet.BottomSheetFragment;
import com.robillo.dancingplayer.views.activities.main.song_list_frag.SongsListFragment;
import com.robillo.dancingplayer.views.activities.main.song_play_frag.SongPlayFragmentSheet;
import com.robillo.dancingplayer.views.activities.main.songs_sort_frag.SongsSortFragment;
import com.robillo.dancingplayer.views.activities.theme_change.ThemeChangeActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.robillo.dancingplayer.utils.AppConstants.ALBUM;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_ID;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_ID;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.COMPOSER;
import static com.robillo.dancingplayer.utils.AppConstants.DATA;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED;
import static com.robillo.dancingplayer.utils.AppConstants.DURATION;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_ACTIVITY;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_FRAGMENT;
import static com.robillo.dancingplayer.utils.AppConstants.ID;
import static com.robillo.dancingplayer.utils.AppConstants.INDEX;
import static com.robillo.dancingplayer.utils.AppConstants.REQUEST_CODE;
import static com.robillo.dancingplayer.utils.AppConstants.SIZE;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.YEAR;

public class MainActivity extends AppCompatActivity implements MainActivityMvpView, MediaController.MediaPlayerControl {

    BottomSheetBehavior playlistSheetBehavior;

    BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    @SuppressWarnings("FieldCanBeLocal")
    private ThemeColors currentUserThemeColors = null;
    @SuppressWarnings("FieldCanBeLocal")
    private AppPreferencesHelper helper = null;

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

    @Override
    public void startServiceForAudioList(final ArrayList<Song> songList) {
        //connect to the service
        ServiceConnection musicConnection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
                //get service
                musicService = binder.getService();
                //pass list
                musicService.setList(songList);
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

    @Override
    public void playSong(int songIndex) {
        musicService.setSong(songIndex);
    }

    @Override
    public void setUp() {
        refreshForUserThemeColors();
        setSongListFragment();

        playlistSheetBehavior = BottomSheetBehavior.from(layoutPlaylistBottomSheet);

        playlistSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
//                        btnBottomSheet.setText("Close Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
//                        btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    @Override
    public void togglePlaylistBottomSheet() {
        if (playlistSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            playlistSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            btnBottomSheet.setText("Close sheet");
        } else {
            playlistSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            playlistSheetBehavior.setText("Expand sheet");
        }        if (playlistSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            playlistSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            btnBottomSheet.setText("Close sheet");
        } else {
            playlistSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            playlistSheetBehavior.setText("Expand sheet");
        }
    }

    @Override
    public void refreshForUserThemeColors() {

        helper = new AppPreferencesHelper(this);
        currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());

        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, currentUserThemeColors.getColorPrimaryDark()));
    }

    @Override
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

    @Override
    public void showSnackBar(String text) {
        Snackbar.make(
                    findViewById(R.id.coordinator_layout), text, Snackbar.LENGTH_SHORT
                ).show();
    }

    @Override
    public MusicService getMusicService() {
        return musicService;
    }

    @Override
    public void updateServiceList(ArrayList<Song> updatedAudioList) {
        musicService.updateAudioList(updatedAudioList);
    }

    @Override
    public int getCurrentSongDuration() {
//        if(musicService!=null && musicBound && musicService.isPlaying()) return musicService.getPosition();
        if(musicService!=null && musicBound) return musicService.getPosition();
        else return 0;
    }

    @Override
    public void rescanDevice() {
        if(getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list)) != null){
            SongsListFragment fragment = (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));
            fragment.fetchSongs(FROM_ACTIVITY);
        }
    }

    @Override
    public void refreshSongListFragmentForSongDelete(Song song, int index) {
        SongsListFragment fragment = (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));
        if(fragment != null){
            fragment.refreshSongListFragmentForSongDelete(song, index);
        }
    }

    @Override
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
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        }
        else {
            bottomSheetFragment = new BottomSheetFragment();
            showSongOptionsOnBottomSheet(song, index);
        }
    }

    @Override
    public void hideOrRemoveBottomSheet() {
        if(bottomSheetFragment != null) {
            if(!bottomSheetFragment.isHidden()) {
                bottomSheetFragment.dismiss();
            }
        }
    }

    @Override
    public void removeSongFromListInMusicService(Song song) {
        MusicService service = getMusicService();
        if(service != null) {
            service.removeSongFromList(song);
        }
    }

    @Override
    public void hideSongPlayFragment(SongPlayFragmentSheet fragmentSheet) {
        fragmentSheet.dismiss();
    }

    @Override
    public void setSongListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(mFragmentContainer.getId(), new SongsListFragment(), getString(R.string.songs_list));
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void setSongPlayFragment() {

        SongPlayFragmentSheet fragmentSheet =
                (SongPlayFragmentSheet) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

        if(fragmentSheet == null)
            new SongPlayFragmentSheet().show(getSupportFragmentManager(), getString(R.string.song_play));
        else {
            fragmentSheet.show(getSupportFragmentManager(), getString(R.string.song_play));
        }
    }

    @Override
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

                AppPreferencesHelper helper = new AppPreferencesHelper(this);

                if(helper.isPlayEvent())
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
            songListFragment.refreshForUserThemeColors(currentUserThemeColors);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SongChangeEvent event) {

        if(event.getSong() != null) {
            currentSong = event.getSong();

            SongsListFragment songListFragment =
                    (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

            if(songListFragment != null) {
                songListFragment.setCurrentSong(currentSong);

                AppPreferencesHelper helper = new AppPreferencesHelper(this);

                if(helper.isPlayEvent())
                    songListFragment.playPlayer(FROM_ACTIVITY);
                else
                    songListFragment.pausePlayer(FROM_ACTIVITY);
            }

            SongPlayFragmentSheet songPlayFragment =
                    (SongPlayFragmentSheet) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

            if(songPlayFragment != null) {
                songPlayFragment.setCurrentSong(currentSong);

                AppPreferencesHelper helper = new AppPreferencesHelper(this);

                if(helper.isPlayEvent())
                    songPlayFragment.playPlayer(FROM_ACTIVITY);
                else
                    songPlayFragment.pausePlayer(FROM_ACTIVITY);
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SetSeekBarEvent event) {

        SongPlayFragmentSheet fragment =
                (SongPlayFragmentSheet) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

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
            if(playFragment != null) playFragment.dismiss();

            if(listFragment != null) {
                currentSong = null;
                listFragment.setCurrentSong(null);
                if(listFragment.getControllerVisibility() == View.VISIBLE) listFragment.fadeOutController();

                Toast toast = Toast.makeText(this, "End Of List", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 10);
                toast.show();
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Override
    public void playNextSong() {
        musicService.playNext();
    }

    @Override
    public void playPreviousSong() {
        musicService.playPrevious();
    }

    @Override
    public Song getCurrentSong() {
        return currentSong;
    }

    @Override
    public void toggleRepeatModeInService(String value) {
        if(musicService != null)
            musicService.toggleRepeatMode(value);
    }

    @Override
    public void toggleShuffleModeInService() {
        if(musicService != null)
            musicService.toggleShuffleMode();
    }

    @Override
    public void seekTenSecondsForward() {
        if(musicService != null)
            musicService.seekTenSecondsForward();
    }

    @Override
    public void seekTenSecondsBackwards() {
        if(musicService != null)
            musicService.seekTenSecondsBackwards();
    }

    @Override
    public void repopulateListSongsListFragment() {
        SongsListFragment fragment =
                (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

        if(fragment != null) {
            fragment.fetchSongs(FROM_FRAGMENT);
        }
    }

    @Override
    public void start() {
        AppPreferencesHelper helper = new AppPreferencesHelper(this);
        helper.setIsPlayEvent(true);

        musicService.playPlayer();
    }

    @Override
    public void pause() {
        AppPreferencesHelper helper = new AppPreferencesHelper(this);
        helper.setIsPlayEvent(false);

        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
//        if(musicService!=null && musicBound && musicService.isPlaying()) return musicService.getDuration();
        if(musicService!=null && musicBound) return musicService.getDuration();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
//        if(musicService!=null && musicBound && musicService.isPlaying()) return musicService.getPosition();
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
}
