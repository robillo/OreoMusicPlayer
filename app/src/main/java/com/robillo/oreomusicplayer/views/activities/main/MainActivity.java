package com.robillo.oreomusicplayer.views.activities.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.events.SongChangeEvent;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.models.ThemeChangeEvent;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.services.MusicService;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.views.activities.main.song_list_frag.SongsListFragment;
import com.robillo.oreomusicplayer.views.activities.main.song_play_frag.SongPlayFragment;
import com.robillo.oreomusicplayer.views.activities.main.songs_sort_frag.SongsSortFragment;
import com.robillo.oreomusicplayer.views.activities.theme_change.ThemeChangeActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.robillo.oreomusicplayer.utils.AppConstants.CONTROLLER_NOTIFICATION_ID;
import static com.robillo.oreomusicplayer.utils.AppConstants.FROM_ACTIVITY;

public class MainActivity extends AppCompatActivity implements MainActivityMvpView, MediaController.MediaPlayerControl {

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    @SuppressWarnings("FieldCanBeLocal")
    private ThemeColors currentUserThemeColors = null;
    @SuppressWarnings("FieldCanBeLocal")
    private AppPreferencesHelper helper = null;

    @SuppressWarnings("FieldCanBeLocal")
    private Song currentSong = null;

    @SuppressWarnings("FieldCanBeLocal")
    private final int PERMISSION_REQUEST_CODE = 0;

    @BindView(R.id.fragment_container)
    FrameLayout mFragmentContainer;

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
        askForDevicePermissions();
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
        startActivity(new Intent(this, ThemeChangeActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        if(musicService!=null && musicBound && musicService.isPlaying()) return musicService.getPosition();
        else return 0;
    }

    @Override
    public void setSongListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(mFragmentContainer.getId(), new SongsListFragment(), getString(R.string.songs_list));
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void setSongPlayFragment() {
        if(getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play)) == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.bottom_up_song_play, 0, 0, R.anim.top_down_song_play);
            transaction.add(mFragmentContainer.getId(), new SongPlayFragment(), getString(R.string.song_play));
            transaction.addToBackStack(getString(R.string.song_play));
            transaction.commit();
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
    public void askForDevicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(
                        new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE
                        },
                        PERMISSION_REQUEST_CODE
                );
            }
            else {
                setSongListFragment();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE :
                if(grantResults.length == 3
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    setSongListFragment();
                }
                else {
                    askForDevicePermissions();
                }
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
            if(currentSong != null)
                fragment.setCurrentSong(currentSong);

            AppPreferencesHelper helper = new AppPreferencesHelper(this);

            if(helper.isPlayEvent())
                fragment.playPlayer(FROM_ACTIVITY);
            else
                fragment.pausePlayer(FROM_ACTIVITY);
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
        if(playIntent!=null) {
            musicService.cancelNotification(this, CONTROLLER_NOTIFICATION_ID);
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

//        if(musicService != null) {
//            musicService.refreshNotificationForThemeChange();
//        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SongChangeEvent event) {

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

        SongPlayFragment songPlayFragment =
                (SongPlayFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

        if(songPlayFragment != null) {
            songPlayFragment.setCurrentSong(currentSong);

            AppPreferencesHelper helper = new AppPreferencesHelper(this);

            if(helper.isPlayEvent())
                songPlayFragment.playPlayer(FROM_ACTIVITY);
            else
                songPlayFragment.pausePlayer(FROM_ACTIVITY);
        }
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
            fragment.fetchSongs();
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
        if(musicService!=null && musicBound && musicService.isPlaying()) return musicService.getDuration();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && musicService.isPlaying()) return musicService.getPosition();
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
