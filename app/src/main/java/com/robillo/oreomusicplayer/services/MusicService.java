package com.robillo.oreomusicplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.events.SongChangeEvent;
import com.robillo.oreomusicplayer.models.Song;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by robinkamboj on 04/03/18.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicServiceInterface {

    private static final String ACTION_TOGGLE_PLAYBACK = "com.robillo.oreomusicplayer.services.TOGGLE_PLAYBACK";
    private static final String ACTION_PREV = "com.robillo.oreomusicplayer.services.PREV";
    private static final String ACTION_NEXT = "com.robillo.oreomusicplayer.services.NEXT";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_STOP = "action_stop";

    private final int EMPTY_CELLS_COUNT = 2;

    NotifyServiceReceiver notifyServiceReceiver;
    private final IBinder musicBind = new MusicBinder();
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosition;

    //for notification
    private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    private MediaController mController;
    private IntentFilter notificationControlsFilter;

    @Override
    public void onCreate() {
        //create the service
        super.onCreate();

        //initialize position
        songPosition = 0;

        //create player
        player = new MediaPlayer();
        notifyServiceReceiver = new NotifyServiceReceiver();

        initMusicPlayer();

        mManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mSession = new MediaSessionCompat(this, "MY_SESSION");
    }

    @Override
    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        notificationControlsFilter = new IntentFilter();
        notificationControlsFilter.addAction(ACTION_PLAY);
        notificationControlsFilter.addAction(ACTION_PAUSE);
        notificationControlsFilter.addAction(ACTION_NEXT);
        notificationControlsFilter.addAction(ACTION_PREV);
        notificationControlsFilter.addAction(ACTION_STOP);
        registerReceiver(notifyServiceReceiver, notificationControlsFilter);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public void setList(ArrayList<Song> songsList) {
        songs = songsList;
    }

    @Override
    public void playSong() {
        player.reset();
        //get song
        Song song = songs.get(songPosition);
        //get id
        long currSong = Long.valueOf(song.getId());
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void setSong(int songIndex){
        if(songIndex < songs.size() - EMPTY_CELLS_COUNT && songIndex >= 2) {
            Log.e("message", songs.get(songIndex).getTitle());
            EventBus.getDefault().post(new SongChangeEvent(songs.get(songIndex)));
            songPosition = songIndex;
            playSong();
        }
    }

    @Override
    public int getPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pausePlayer() {
        player.pause();
    }

    @Override
    public void seekPlayer(int position) {
        player.seekTo(position);
    }

    @Override
    public void playPlayer() {
        player.start();
    }

    @Override
    public void playPrevious() {
        setSong(songPosition-1);
    }

    @Override
    public void playNext() {
        setSong(songPosition+1);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(songPosition < songs.size()-EMPTY_CELLS_COUNT){
            songPosition++;
            setSong(songPosition);
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        //notification
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_ARTIST, songs.get(songPosition).getArtist())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, songs.get(songPosition).getAlbum())
                .putString(MediaMetadata.METADATA_KEY_TITLE, songs.get(songPosition).getTitle())
                .build());
        // Indicate you're ready to receive media commands
        mSession.setActive(true);
        // Attach a new Callback to receive MediaSession updates
        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
            }

            @Override
            public void onPause() {
                super.onPause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
            }
        });

        NotificationCompat.Action previous = new NotificationCompat
                .Action.Builder(R.drawable.ic_skip_previous_black_24dp, "prev", retreivePlaybackAction(3))
                .build();

        NotificationCompat.Action pause = new NotificationCompat
                .Action.Builder(R.drawable.ic_pause_black_24dp, "prev", retreivePlaybackAction(1))
                .build();

        NotificationCompat.Action next = new NotificationCompat
                .Action.Builder(R.drawable.ic_skip_next_black_24dp, "next", retreivePlaybackAction(2))
                .build();

        // Create a new Notification

        final Notification notificationController = new NotificationCompat.Builder(this, "channel_id")
                // Hide the timestamp
                .setShowWhen(false)
                .addAction(previous)
                .addAction(pause)
                .addAction(next)
                // Set the Notification style
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mSession.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(0xFFDB4437)
                // Set the large and small icons
                .setSmallIcon(R.drawable.oval_shape)
                // Set Notification content information
                .setContentText(songs.get(songPosition).getArtist())
                .setContentInfo(songs.get(songPosition).getAlbum())
                .setContentTitle(songs.get(songPosition).getTitle()).build();
                // Add some playback controls

        if (getSystemService(NOTIFICATION_SERVICE) != null) {
            //noinspection ConstantConditions
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, notificationController);
        }

        // Do something with your TransportControls
        final MediaControllerCompat.TransportControls controls = mSession.getController().getTransportControls();
    }

    private PendingIntent retreivePlaybackAction(int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(this, MusicService.class);
        switch (which) {
            case 1:
                // Play and pause
                action = new Intent(ACTION_TOGGLE_PLAYBACK);
                action.putExtra("extra", ACTION_TOGGLE_PLAYBACK);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 1, action, 0);
                return pendingIntent;
            case 2:
                // Skip tracks
                action = new Intent(ACTION_NEXT);
                action.putExtra("extra", ACTION_NEXT);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 2, action, 0);
                return pendingIntent;
            case 3:
                // Previous tracks
                action = new Intent(ACTION_PREV);
                action.putExtra("extra", ACTION_PREV);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 3, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

    public class NotifyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("pending intent", "extras " + intent.getExtras());
            if(action != null) {
                Log.e("pending intent", "action " + action);
                switch (action) {
                    case ACTION_TOGGLE_PLAYBACK: {
                        Log.e("pending intent", "toggle");
                        break;
                    }
                    case ACTION_NEXT: {
                        Log.e("pending intent", "next");
                        break;
                    }
                    case ACTION_PREV: {
                        Log.e("pending intent", "prev");
                        break;
                    }
                    case ACTION_STOP: {
                        Log.e("pending intent", "stop");
                        break;
                    }
                }
            }
        }
    }
}
