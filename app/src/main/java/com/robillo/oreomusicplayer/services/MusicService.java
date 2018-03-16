package com.robillo.oreomusicplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.events.SongChangeEvent;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.utils.AppConstants;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.robillo.oreomusicplayer.utils.AppConstants.ACTION_NEXT;
import static com.robillo.oreomusicplayer.utils.AppConstants.ACTION_PAUSE;
import static com.robillo.oreomusicplayer.utils.AppConstants.ACTION_PLAY;
import static com.robillo.oreomusicplayer.utils.AppConstants.ACTION_PREV;
import static com.robillo.oreomusicplayer.utils.AppConstants.ACTION_STOP;
import static com.robillo.oreomusicplayer.utils.AppConstants.ACTION_TOGGLE_PLAYBACK;
import static com.robillo.oreomusicplayer.utils.AppConstants.CHANNEL_ID;
import static com.robillo.oreomusicplayer.utils.AppConstants.CONTROLLER_NOTIFICATION_ID;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicServiceInterface {

    private static boolean IS_REPEAT_MODE_ON = false;
    private static boolean IS_SHUFFLE_MODE_ON = false;

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private Song currentSong;
    private int songPosition;

    private MediaSessionCompat mSession;
    Notification notificationController = null;
    MediaControllerCompat.TransportControls controls;
    private final IBinder musicBind = new MusicBinder();


    //____________________________________SERVICE LIFECYCLE CALLBACkS______________________________//

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

    //____________________________________INITIAL SETUP CALL______________________________________//
    @Override
    public void initMusicPlayer() {

        SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        IS_REPEAT_MODE_ON = preferences.getBoolean("is_repeat_mode_on", false);
        IS_SHUFFLE_MODE_ON = preferences.getBoolean("is_shuffle_mode_on", false);

        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);


        mSession = new MediaSessionCompat(this, AppConstants.SESSION_NAME);
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
        if(songIndex < songs.size() - AppConstants.EMPTY_CELLS_COUNT && songIndex >= 1) {
            currentSong = songs.get(songIndex);

            SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("play_event", true);
            editor.apply();

            EventBus.getDefault().postSticky(new SongChangeEvent(songs.get(songIndex)));
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
    public void seekPlayer(int position) {
        player.seekTo(position);
    }

    @Override
    public void playPlayer() {
        player.start();
        buildNotification(true);

        SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("play_event", true);
        editor.apply();

        EventBus.getDefault().postSticky(new SongChangeEvent(currentSong));
    }

    @Override
    public void pausePlayer() {
        player.pause();
        buildNotification(false);

        SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("play_event", false);
        editor.apply();

        EventBus.getDefault().postSticky(new SongChangeEvent(currentSong));
    }

    @Override
    public void playPrevious() {

        if(isShuffleModeOn()) {
            // nextInt is normally exclusive of the top value
            // so add 1 to make it inclusive
            int min = 1;
            int max = songs.size() - AppConstants.EMPTY_CELLS_COUNT + 1;
            songPosition = ThreadLocalRandom.current().nextInt(min, max);
        }
        else {
            songPosition--;
        }

        setSong(songPosition);
    }

    @Override
    public void playNext() {

        if(isShuffleModeOn()) {
            // nextInt is normally exclusive of the top value
            // so add 1 to make it inclusive
            int min = 1;
            int max = songs.size() - AppConstants.EMPTY_CELLS_COUNT + 1;
            songPosition = ThreadLocalRandom.current().nextInt(min, max);
        }
        else {
            songPosition++;
        }

        setSong(songPosition);
    }

    @Override
    public void buildNotification(boolean play_or_pause) {

        int playOrPauseDrawable;
        if(play_or_pause)
            playOrPauseDrawable = R.drawable.ic_pause_black_24dp;
        else
            playOrPauseDrawable = R.drawable.ic_play_arrow_black_24dp;

        NotificationCompat.Action previous = new NotificationCompat
                .Action.Builder(R.drawable.ic_skip_previous_black_24dp, "prev", retreivePlaybackAction(1)).build();

        NotificationCompat.Action pause = new NotificationCompat
                .Action.Builder(playOrPauseDrawable, "play_or_pause", retreivePlaybackAction(2)).build();

        NotificationCompat.Action next = new NotificationCompat
                .Action.Builder(R.drawable.ic_skip_next_black_24dp, "next", retreivePlaybackAction(3)).build();

        Bitmap bitmap = getBitmapAlbumArt();

        notificationController = new NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
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
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.oval_shape)
                // Set Notification content information
                .setContentText(songs.get(songPosition).getArtist())
                .setContentInfo(songs.get(songPosition).getAlbum())
                .setContentTitle(songs.get(songPosition).getTitle()).build();


        if (getSystemService(NOTIFICATION_SERVICE) != null) {
            //noinspection ConstantConditions
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(CONTROLLER_NOTIFICATION_ID, notificationController);
        }
    }

    @Override
    public void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
                notificationManager.cancel(CONTROLLER_NOTIFICATION_ID);

    }

    @Override
    public Bitmap getBitmapAlbumArt() {
        String path = null;
        if(currentSong != null) {
            //get path for the album art for this song
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {String.valueOf(currentSong.getAlbumId())},
                    null);
            if(cursor!=null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                // do whatever you need to do
                cursor.close();
            }
        }

        File imgFile = null;
        if(path != null) {
            imgFile = new File(path);
        }

        Bitmap bitmap = null;
        if(imgFile != null && imgFile.exists()) {
            bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return bitmap;
    }

    @Override
    public void toggleRepeatMode() {
        SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        if(preferences.getBoolean("is_repeat_mode_on", false))
            setIsRepeatModeOn(true);
        else
            setIsRepeatModeOn(false);
    }

    @Override
    public void toggleShuffleMode() {
        SharedPreferences preferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        if(preferences.getBoolean("is_shuffle_mode_on", false))
            setIsShuffleModeOn(true);
        else
            setIsShuffleModeOn(false);
    }

    public static boolean isRepeatModeOn() {
        return IS_REPEAT_MODE_ON;
    }

    public static void setIsRepeatModeOn(boolean isRepeatModeOn) {
        IS_REPEAT_MODE_ON = isRepeatModeOn;
    }

    public static boolean isShuffleModeOn() {
        return IS_SHUFFLE_MODE_ON;
    }

    public static void setIsShuffleModeOn(boolean isShuffleModeOn) {
        IS_SHUFFLE_MODE_ON = isShuffleModeOn;
    }

    //____________________________MEDIA PLAYER INTERFACE CONTROLS__________________________//

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(songPosition < songs.size() - AppConstants.EMPTY_CELLS_COUNT){

            if(!isRepeatModeOn()) {
                //if repeat mode is off, update songPosition for next song to be played

                if(isShuffleModeOn()) {
                    // nextInt is normally exclusive of the top value
                    // so add 1 to make it inclusive
                    int min = 1;
                    int max = songs.size() - AppConstants.EMPTY_CELLS_COUNT + 1;
                    songPosition = ThreadLocalRandom.current().nextInt(min, max);
                }
                else {
                    songPosition++;
                }
            }

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

        buildNotification(true);
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
        } else if (actionString.equalsIgnoreCase(ACTION_TOGGLE_PLAYBACK)) {
            if (isPlaying()) {
                buildNotification(false);
                controls.pause();
            }
            else {
                buildNotification(true);
                controls.play();
            }
        }
    }
}
