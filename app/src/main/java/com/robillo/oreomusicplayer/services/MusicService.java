package com.robillo.oreomusicplayer.services;

import android.app.Notification;
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
import android.view.Gravity;
import android.widget.Toast;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.events.SongChangeEvent;
import com.robillo.oreomusicplayer.models.SetSeekBarEvent;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

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
import static com.robillo.oreomusicplayer.utils.AppConstants.EMPTY_CELLS_COUNT;
import static com.robillo.oreomusicplayer.utils.AppConstants.LAUNCHED_FROM_NOTIFICATION;
import static com.robillo.oreomusicplayer.utils.AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE;

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

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);

        initMusicPlayer();
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



        callStateListener();
        audioFocusChangeListenerPrelims();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.e("changed to ", focusChange + " focus change");
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
            Toast toast = Toast.makeText(this, "End Of List", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 10);
            toast.show();
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

        if(!audioManagerRequestAudioFocus()) {
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
        if(currentSong != null) {
            if (!mPlayOnAudioFocus) {
                Log.e("abandon", "from pause");
                abandonAudioFocus();
            }

            player.pause();
            buildNotification(false);

            new AppPreferencesHelper(this).setIsPlayEvent(false);

            EventBus.getDefault().postSticky(new SongChangeEvent(currentSong));
        }
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
        else if(songPosition < songs.size() - AppConstants.EMPTY_CELLS_COUNT){
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

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(LAUNCHED_FROM_NOTIFICATION, true);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        notificationController = new NotificationCompat.Builder(this, CHANNEL_ID)
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
                .setColor(getResources().getColor(R.color.black))
                // Set the large and small icons
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.oval_shape)
                // Set Notification content information
                .setContentText(songs.get(songPosition).getArtist())
                .setContentInfo(songs.get(songPosition).getAlbum())
                .setContentTitle(songs.get(songPosition).getTitle())
                .setContentIntent(contentIntent)
                .build();

        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(mNotificationManager != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, AppConstants.CHANNEL_NAME, importance);
                mNotificationManager.createNotificationChannel(mChannel);
            }

            mNotificationManager.notify(CONTROLLER_NOTIFICATION_ID, notificationController);
        }
    }

    @Override
    public void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

        Bitmap bitmap = null;
        if(imgFile != null && imgFile.exists()) {
            bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
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
    public void audioManagerServiceListener() {

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

        if(audioManagerRequestAudioFocus()) {
            //do something
        }
    }

    @Override
    public void configMediaPlayerState(MediaPlayer mp) {

    }

    @Override
    public boolean audioManagerRequestAudioFocus() {
        int audioFocus = audioManager.requestAudioFocus(this, //audio focus change listener
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);


        if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // other app had stopped playing song now , so u can do u stuff now .
            Log.e("audio focus", "granted");
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

        return audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
//        return (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED || focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

//    @Override
//    public void refreshNotificationForThemeChange() {
//        if(currentSong != null) {
//            AppPreferencesHelper helper = new AppPreferencesHelper(this);
//            buildNotification(helper.isPlayEvent());
//        }
//    }


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
            }                                                   //else don't update song position so that same song plays again

            setSong(songPosition);
        }

        if(isRepeatModeOn().equals(REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE) && songPosition == songs.size() - EMPTY_CELLS_COUNT) {
            Log.e("completion", "end");
            pausePlayer();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        if(!audioManagerRequestAudioFocus())
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        player = null;
        unregisterReceiver(mNoisyReceiver);
    }
}
