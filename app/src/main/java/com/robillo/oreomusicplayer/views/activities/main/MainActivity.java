package com.robillo.oreomusicplayer.views.activities.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.events.SongChangeEvent;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.services.MusicService;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.views.activities.main.song_list_frag.SongListMvpView;
import com.robillo.oreomusicplayer.views.activities.main.song_list_frag.SongsListFragment;
import com.robillo.oreomusicplayer.views.activities.main.song_play_frag.SongPlayFragment;

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
        askForDevicePermissions();
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
            transaction.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right);
            transaction.add(mFragmentContainer.getId(), new SongPlayFragment(), getString(R.string.song_play));
            transaction.addToBackStack(getString(R.string.song_play));
            transaction.commit();
        }
    }

    @Override
    public void askForDevicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
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
                if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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

            SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
            if(preferences.getBoolean("play_event", false))
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
        super.onBackPressed();
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
    public void onMessageEvent(SongChangeEvent event) {

        currentSong = event.getSong();

        SongsListFragment songListFragment =
                (SongsListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.songs_list));

        if(songListFragment != null) {
            songListFragment.setCurrentSong(currentSong);

            SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
            Boolean play_or_pause = preferences.getBoolean("play_event", false);

            if(play_or_pause)
                songListFragment.playPlayer(FROM_ACTIVITY);
            else
                songListFragment.pausePlayer(FROM_ACTIVITY);
        }

        SongPlayFragment songPlayFragment =
                (SongPlayFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.song_play));

        if(songPlayFragment != null) {
            songPlayFragment.setCurrentSong(currentSong);

            SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
            Boolean play_or_pause = preferences.getBoolean("play_event", false);

            if(play_or_pause)
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
    public void toggleRepeatModeInService() {
        if(musicService != null)
            musicService.toggleRepeatMode();
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
    public void start() {
        SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("play_event", true);
        editor.apply();

        musicService.playPlayer();
    }

    @Override
    public void pause() {
        SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("play_event", false);
        editor.apply();

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
