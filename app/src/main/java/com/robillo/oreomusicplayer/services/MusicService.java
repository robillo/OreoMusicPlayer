package com.robillo.oreomusicplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
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
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by robinkamboj on 04/03/18.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicServiceInterface {

    private final int EMPTY_CELLS_COUNT = 2;
    private static final String SESSION_NAME = "session_name";
    private static final String ACTION_PREV = "PREV";
    private static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    private static final String ACTION_TOGGLE_PLAYBACK = "TOGGLE_PLAYBACK";

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;

    private MediaSessionCompat mSession;
    MediaControllerCompat.TransportControls controls;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        //create the service
        super.onCreate();

        //initialize position
        songPosition = 0;

        //create player
        player = new MediaPlayer();

        initMusicPlayer();
    }

    @Override
    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);


        mSession = new MediaSessionCompat(this, SESSION_NAME);
        controls = mSession.getController().getTransportControls();

        // Indicate you're ready to receive media commands
        mSession.setActive(true);
        // Attach a new Callback to receive MediaSession updates
        mSession.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public void onPlay() {
                super.onPlay();
                playPlayer();
            }

            @Override
            public void onPause() {
                super.onPause();
                pausePlayer();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.e("controls", "stop");
            }
        });
    }

    //____________________________________BINDER STUFF__________________________________//

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

    //____________________________SERVICE INTERFACE CONTROLS__________________________//

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

    @Override
    public void setSong(int songIndex){
        if(songIndex < songs.size() - EMPTY_CELLS_COUNT && songIndex >= 2) {
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


    //____________________________MEDIA PLAYER INTERFACE CONTROLS__________________________//

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

//        Intent notIntent = new Intent(this, MainActivity.class);
//        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        notIntent.setAction(ACTION_TOGGLE_PLAYBACK);
//        PendingIntent pendInt = PendingIntent.getActivity(this, 4, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action previous = new NotificationCompat
                .Action.Builder(R.drawable.ic_skip_previous_black_24dp, "prev", retreivePlaybackAction(1))
                .build();

        NotificationCompat.Action pause = new NotificationCompat
                .Action.Builder(R.drawable.ic_pause_black_24dp, "pause", retreivePlaybackAction(2))
                .build();

        NotificationCompat.Action next = new NotificationCompat
                .Action.Builder(R.drawable.ic_skip_next_black_24dp, "next", retreivePlaybackAction(3))
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
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setSmallIcon(R.drawable.oval_shape)
                // Set Notification content information
                .setContentText(songs.get(songPosition).getArtist())
                .setContentInfo(songs.get(songPosition).getAlbum())
                .setContentTitle(songs.get(songPosition).getTitle()).build();


        if (getSystemService(NOTIFICATION_SERVICE) != null) {
            //noinspection ConstantConditions
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, notificationController);
        }
    }


    //____________________________SETTING PENDING INTENTS TO NOTIFICATION ACTIONS__________________________//

    private PendingIntent retreivePlaybackAction(int which) {
        Intent action = new Intent(this, MusicService.class);
        switch (which) {
            case 1:
                // Previous tracks
                action.setAction(ACTION_PREV);
                return PendingIntent.getService(this, which, action, 0);
            case 2:
                // Play and pause
                action.setAction(ACTION_TOGGLE_PLAYBACK);
                return PendingIntent.getService(this, which, action, 0);
            case 3:
                // Skip tracks
                action.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, which, action, 0);
            default:
                break;
        }
        return null;
    }


    //____________________________HANDLING PENDING INTENTS TO NOTIFICATION ACTIONS__________________________//
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            controls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            controls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            controls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREV)) {
            controls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            controls.stop();
        }
    }
}
