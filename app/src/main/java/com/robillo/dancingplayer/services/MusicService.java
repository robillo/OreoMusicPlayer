package com.robillo.dancingplayer.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.events.PlayerStateNoSongPlayingEvent;
import com.robillo.dancingplayer.events.SongChangeEvent;
import com.robillo.dancingplayer.models.SetSeekBarEvent;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.robillo.dancingplayer.utils.AppConstants.ACTION_NEXT;
import static com.robillo.dancingplayer.utils.AppConstants.ACTION_PAUSE;
import static com.robillo.dancingplayer.utils.AppConstants.ACTION_PLAY;
import static com.robillo.dancingplayer.utils.AppConstants.ACTION_PREV;
import static com.robillo.dancingplayer.utils.AppConstants.ACTION_STOP;
import static com.robillo.dancingplayer.utils.AppConstants.ACTION_TOGGLE_PLAYBACK;
import static com.robillo.dancingplayer.utils.AppConstants.CHANNEL_ID;
import static com.robillo.dancingplayer.utils.AppConstants.CONTROLLER_NOTIFICATION_ID;
import static com.robillo.dancingplayer.utils.AppConstants.EMPTY_CELLS_COUNT;
import static com.robillo.dancingplayer.utils.AppConstants.LAUNCHED_FROM_NOTIFICATION;
import static com.robillo.dancingplayer.utils.AppConstants.NEXT_NOT;
import static com.robillo.dancingplayer.utils.AppConstants.PLAY_OR_PAUSE_NOT;
import static com.robillo.dancingplayer.utils.AppConstants.PREVIOUS_NOT;
import static com.robillo.dancingplayer.utils.AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicServiceInterface, AudioManager.OnAudioFocusChangeListener {

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private boolean wasPausedByInterrupt = false;
    private boolean mPlayOnAudioFocus = false;
    @SuppressWarnings("FieldCanBeLocal")
    private PhoneStateListener phoneStateListener;
    @SuppressWarnings("FieldCanBeLocal")
    private AudioManager audioManager = null;
    @SuppressWarnings("FieldCanBeLocal")
    private TelephonyManager telephonyManager;
    @SuppressWarnings("FieldCanBeLocal")
    private AudioAttributes audioAttributes;
    AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(player != null && isPlaying()) {
                pausePlayer();
            }
        }
    };

    private static String IS_REPEAT_MODE_ON = AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE;
    private static boolean IS_SHUFFLE_MODE_ON = false;

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private Song currentSong;
    private int songPosition;
    private MediaSessionCompat mSession;
    NotificationCompat.Builder builder = null;
    NotificationManager notificationManager = null;
    NotificationChannel mChannel = null;
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

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);

        initMusicPlayer();
    }

    @Override
    public void onDestroy() {

        player = null;
        cancelNotification();
        if(mNoisyReceiver != null)
            try { unregisterReceiver(mNoisyReceiver); }
            catch (IllegalArgumentException e) { Log.e("music serv 137", "already unregistered"); }
            catch (Exception e) { Log.e("musix serv 138", "other exception for receiver"); }
        super.onDestroy();
    }

    //____________________________________INITIAL SETUP CALL______________________________________//
    @Override
    public void initMusicPlayer() {

        AppPreferencesHelper helper = new AppPreferencesHelper(this);
        IS_REPEAT_MODE_ON = helper.isRepeatModeOn();
        IS_SHUFFLE_MODE_ON = helper.isShuffleModeOn();

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

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, AppConstants.CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(mChannel);
        }


        callStateListener();
        audioFocusChangeListenerPrelims();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPlayOnAudioFocus && !isPlaying()) {
                    playPlayer();
                } else if (isPlaying()) {
//                    setVolume(MEDIA_VOLUME_DEFAULT);
                      player.setVolume(1f, 1f);
                }
                mPlayOnAudioFocus = false;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                setVolume(MediaPlayer.MEDIA_VOLUME_DUCK);
                player.setVolume(0.2f, 0.2f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()) {
                    mPlayOnAudioFocus = true;
                    pausePlayer();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                abandonAudioFocus();
                mPlayOnAudioFocus = false;
                pausePlayer();
                break;
        }
    }

    private void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this);
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
        if(player != null) player.stop();
        if(player != null) player.release();
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

            AppPreferencesHelper helper = new AppPreferencesHelper(this);
            helper.setIsPlayEvent(true);

            EventBus.getDefault().postSticky(new SongChangeEvent(songs.get(songIndex)));
            songPosition = songIndex;
            playSong();
        }
        else if(isRepeatModeOn().equals(AppConstants.REPEAT_MODE_VALUE_LOOP)) {
            setSong(1);
        }
        else {
            if(isPlaying()) pausePlayer();
            currentSong = null;
            cancelNotification();
            EventBus.getDefault().postSticky(new PlayerStateNoSongPlayingEvent());
            EventBus.getDefault().postSticky(new SongChangeEvent(null));
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
        if (currentSong == null)
            return;

        if(audioManagerRequestAudioFocus()) {
            return;
        }

        player.start();
        buildNotification(true);

        AppPreferencesHelper helper = new AppPreferencesHelper(this);
        helper.setIsPlayEvent(true);

        EventBus.getDefault().postSticky(new SongChangeEvent(currentSong));
    }

    @Override
    public void pausePlayer() {

        if(songs == null) return;

        if(currentSong != null) {
            if (!mPlayOnAudioFocus) {
                abandonAudioFocus();
            }

            player.pause();
            buildNotification(false);

            new AppPreferencesHelper(this).setIsPlayEvent(false);

            EventBus.getDefault().postSticky(new SongChangeEvent(currentSong));
        }
    }

    @Override
    public Song getSong() {
        return currentSong;
    }

    @Override
    public void playPrevious() {

        if(songs == null) return;

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

        if(songs == null) return;

        if(isShuffleModeOn()) {
            // nextInt is normally exclusive of the top value
            // so add 1 to make it inclusive
            int min = 1;
            int max = songs.size() - AppConstants.EMPTY_CELLS_COUNT + 1;
            songPosition = ThreadLocalRandom.current().nextInt(min, max);
        }
        else if(songPosition < songs.size() - AppConstants.EMPTY_CELLS_COUNT){
            songPosition++;
        }

        setSong(songPosition);
    }

    @Override
    public void buildNotification(boolean play_or_pause) {

        int playOrPauseDrawable;
        if(play_or_pause) playOrPauseDrawable = R.drawable.ic_pause_black_24dp;
        else playOrPauseDrawable = R.drawable.ic_play_arrow_black_24dp;

        NotificationCompat.Action previous = returnAction(R.drawable.ic_skip_previous_black_24dp, PREVIOUS_NOT, 1);
        NotificationCompat.Action pause = returnAction(playOrPauseDrawable, PLAY_OR_PAUSE_NOT, 2);
        NotificationCompat.Action next = returnAction(R.drawable.ic_skip_next_black_24dp, NEXT_NOT, 3);

        if(songs == null) {
            cancelNotification();
            return;
        }

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                .addAction(previous)
                .addAction(pause)
                .addAction(next)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mSession.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.black))
                .setLargeIcon(getBitmapAlbumArt())
                .setSmallIcon(R.drawable.icon_drawable)
                .setContentText(songs.get(songPosition).getArtist())
                .setContentInfo(songs.get(songPosition).getAlbum())
                .setContentTitle(songs.get(songPosition).getTitle())
                .setContentIntent(setupNotificationPendingIntent())
                .setOngoing(play_or_pause);

        notificationManager.notify(CONTROLLER_NOTIFICATION_ID, builder.build());
    }

    @Override
    public NotificationCompat.Action returnAction(int id, String title, int which) {
        return new NotificationCompat
                .Action.Builder(id, title, retreivePlaybackAction(which)).build();
    }

    @Override
    public void cancelNotification() {
        if(notificationManager != null) {
            notificationManager.cancel(CONTROLLER_NOTIFICATION_ID);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(AppConstants.CHANNEL_ID);
            }
        }
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

        Bitmap bitmap;
        if(imgFile != null && imgFile.exists()) {
            bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        else {
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_drawable)).getBitmap();
        }
        return bitmap;
    }

    @Override
    public void toggleRepeatMode(String value) {
        setIsRepeatModeOn(value);
    }

    @Override
    public void toggleShuffleMode() {
        AppPreferencesHelper helper = new AppPreferencesHelper(this);
        if(helper.isShuffleModeOn())
            setIsShuffleModeOn(true);
        else
            setIsShuffleModeOn(false);
    }

    @Override
    public void seekTenSecondsForward() {
        if(player.getCurrentPosition() < (player.getDuration() - 11000)) {        //1 second for performance lag
            player.seekTo(player.getCurrentPosition() + 10000);
        }
        else {
            if(isRepeatModeOn().equals(AppConstants.REPEAT_MODE_VALUE_REPEAT)) {
                setSong(songPosition);
            }
            else {
                playNext();
            }
        }
    }

    @Override
    public void seekTenSecondsBackwards() {
        if(player.getCurrentPosition() > 11000) {                                 //current position is more than 10 seconds
            player.seekTo(player.getCurrentPosition() - 10000);
        }
        else {
            if(isRepeatModeOn().equals(AppConstants.REPEAT_MODE_VALUE_REPEAT)) {
                setSong(songPosition);
            }
            else {
                playPrevious();
            }
        }
    }

    @Override
    public void updateAudioList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    //Handle incoming phone calls
    @Override
    public void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (player != null) {
                            if(new AppPreferencesHelper(MusicService.this).isPlayEvent()) {
                                pausePlayer();
                                wasPausedByInterrupt = true;
                            }
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (player != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                if(wasPausedByInterrupt && currentSong != null) {
                                    playPlayer();
                                    wasPausedByInterrupt = false;
                                }
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void setPlayerStateToNoSongPlaying() {

    }

    @Override
    public void removeSongFromList(Song song) {
        if(songs != null && songs.contains(song))
            songs.remove(song);
    }

    @Override
    public void audioFocusChangeListenerPrelims() {
        audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);

        audioAttributes =
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(audioAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(this)
                            .build();
        }
    }

    @Override
    public boolean audioManagerRequestAudioFocus() {
        int audioFocus = audioManager.requestAudioFocus(this, //audio focus change listener
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);


        //noinspection StatementWithEmptyBody
        if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // other app had stopped playing song now , so u can do u stuff now .
        }

        int focusRequest = -10;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            focusRequest = audioManager.requestAudioFocus(audioFocusRequest);
        }
        switch (focusRequest) {
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                // don’t start playback
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                // actually start playback
        }

        return (audioFocus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED && focusRequest != AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    @Override
    public PendingIntent setupNotificationPendingIntent() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(LAUNCHED_FROM_NOTIFICATION, true);

        return PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public static String isRepeatModeOn() {
        return IS_REPEAT_MODE_ON;
    }

    public static void setIsRepeatModeOn(String isRepeatModeOn) {
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

            if(!isRepeatModeOn().equals(AppConstants.REPEAT_MODE_VALUE_REPEAT)) {
                //if repeat mode is off, update songPosition for setPlayerStateToNoSongPlaying song to be played

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
            }                                                   //else don't update song position so that same song plays again

            setSong(songPosition);
        }

        if(isRepeatModeOn().equals(REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE) && songPosition == songs.size() - EMPTY_CELLS_COUNT) {
            pausePlayer();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        if(audioManagerRequestAudioFocus())
            return;

        //start playback
        mp.start();

        //notification
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_ARTIST, songs.get(songPosition).getArtist())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, songs.get(songPosition).getAlbum())
                .putString(MediaMetadata.METADATA_KEY_TITLE, songs.get(songPosition).getTitle())
                .build());

        buildNotification(true);

        EventBus.getDefault().postSticky(new SetSeekBarEvent(0, mp.getDuration()));
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
