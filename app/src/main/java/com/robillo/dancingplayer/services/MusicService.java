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
        MediaPlayer.OnCompletionListener, MusicServiceInterface,
        AudioManager.OnAudioFocusChangeListener {

    private int playOrPauseDrawable;
    private boolean isOngoingProcess;
    private boolean ongoingCall = false;
    private boolean wasPausedByInterrupt = false;
    private boolean mPlayOnAudioFocus = false;
    private AudioManager audioManager = null;
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
        super.onCreate();

        initializeDataAtLaunch();
    }

    private void initializeDataAtLaunch() {
        songPosition = 0;

        player = new MediaPlayer();

        registerCallInterruptionReceiver();

        initMusicPlayer();
    }

    private void registerCallInterruptionReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
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
        setDataInPreferences();
        setPlayerDefaults();
        setPlayerListeners();
        setupMediaSessionForNotification();
        setupNotificationChannel();
        setupIncomingCallsListener();
        audioFocusChangeListenerPrelims();
    }

    private void setDataInPreferences() {
        AppPreferencesHelper helper = new AppPreferencesHelper(this);
        IS_REPEAT_MODE_ON = helper.isRepeatModeOn();
        IS_SHUFFLE_MODE_ON = helper.isShuffleModeOn();
    }

    private void setPlayerDefaults() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void setPlayerListeners() {
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    private void setupMediaSessionForNotification() {
        mSession = new MediaSessionCompat(this, AppConstants.SESSION_NAME);
        controls = mSession.getController().getTransportControls();
        mSession.setActive(true);
        setMediaSessionInstanceCallback();
    }

    private void setMediaSessionInstanceCallback() {
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

    private void setupNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            mChannel = new NotificationChannel(CHANNEL_ID, AppConstants.CHANNEL_NAME, importance);
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPlayOnAudioFocus && !isPlaying()) {
                    playPlayer();
                } else if (isPlaying()) {
                      player.setVolume(1f, 1f);
                }
                mPlayOnAudioFocus = false;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
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
    public void setSongsList(ArrayList<Song> songsList) {
        songs = songsList;
    }

    @Override
    public void playSong() {

        try {
            player.reset();
        }
        catch (NullPointerException e) {
            player = new MediaPlayer();
            initMusicPlayer();
        }
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
            player.prepareAsync();
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
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
        buildNotificationForPlaying();

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
            buildNotificationForPaused();

            new AppPreferencesHelper(this).setIsPlayEvent(false);

            EventBus.getDefault().postSticky(new SongChangeEvent(currentSong));
        }
    }

    @Override
    public Song getSong() {
        return currentSong;
    }

    @Override
    public int songsListSize() {
        return songs == null ? 0 : songs.size();
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

    private void buildNotificationForPlaying() {
        playOrPauseDrawable = R.drawable.ic_pause_black_24dp;
        isOngoingProcess = true;
        buildNotification();
    }

    private void buildNotificationForPaused() {
        playOrPauseDrawable = R.drawable.ic_play_arrow_black_24dp;
        isOngoingProcess = false;
        buildNotification();
    }

    private void buildNotification() {

        NotificationCompat.Action previousAction =
                returnAction(R.drawable.ic_skip_previous_black_24dp, PREVIOUS_NOT, 1);
        NotificationCompat.Action pauseAction =
                returnAction(playOrPauseDrawable, PLAY_OR_PAUSE_NOT, 2);
        NotificationCompat.Action nextAction =
                returnAction(R.drawable.ic_skip_next_black_24dp, NEXT_NOT, 3);

        if(ifSongPositionNotInList()) {
            cancelNotification();
            return;
        }

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                .addAction(previousAction)
                .addAction(pauseAction)
                .addAction(nextAction)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.black))
                .setLargeIcon(getBitmapAlbumArt())
                .setSmallIcon(R.drawable.icon_drawable)
                .setContentText(songs.get(songPosition).getArtist())
                .setContentInfo(songs.get(songPosition).getAlbum())
                .setContentTitle(songs.get(songPosition).getTitle())
                .setContentIntent(setupNotificationPendingIntent())
                .setOngoing(isOngoingProcess);

        notificationManager.notify(CONTROLLER_NOTIFICATION_ID, builder.build());
    }

    private boolean ifSongPositionNotInList() {
        return songs == null || songPosition >= songs.size() - 1 || songPosition < 0;
    }

    @Override
    public NotificationCompat.Action returnAction(int id, String title, int requestCode) {
        return new NotificationCompat
                .Action.Builder(id, title, retrievePlaybackAction(requestCode)).build();
    }

    @Override
    public void removeSongFromListInMusicServiceById(String songId) {
        if(songs != null && songId != null) {
            for(Song s : songs) {
                //noinspection ConstantConditions
                if(s != null && s.getId() != null &&  s.getId().equals(songId)) {
                    songs.remove(s);
                    break;
                }
            }
        }
    }

    @Override
    public void setSongPosn(int songPosn) {
        songPosition = songPosn;
    }

    @Override
    public void cancelNotification() {
        if(notificationManager != null) notificationManager.cancel(CONTROLLER_NOTIFICATION_ID);
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

    @Override
    public void setupIncomingCallsListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        pausePlayerWhenPhoneRinging();
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        playPlayerWhenPhoneIdle();
                        break;
                }
            }
        };

        if (telephonyManager != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void pausePlayerWhenPhoneRinging() {
        if (player != null) {
            if (new AppPreferencesHelper(MusicService.this).isPlayEvent()) {
                pausePlayer();
                wasPausedByInterrupt = true;
            }
            ongoingCall = true;
        }
    }

    private void playPlayerWhenPhoneIdle() {
        if (player != null) {
            if (ongoingCall) {
                ongoingCall = false;
                if (wasPausedByInterrupt && currentSong != null) {
                    playPlayer();
                    wasPausedByInterrupt = false;
                }
            }
        }
    }

    @Override
    public void removeSongFromList(Song song) {
        Song songToDelete = null;
        if(songs != null) {
            for (Song s : songs) {
                //noinspection ConstantConditions
                if(s != null && s.getId() != null && s.getId().equals(song.getId())) {
                    songToDelete = s;
                }
            }
            songs.remove(songToDelete);
        }
    }

    @Override
    public void audioFocusChangeListenerPrelims() {
        audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
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
    public void onPrepared(MediaPlayer mediaPlayer) {

        if(audioManagerRequestAudioFocus()) return;

        startPlaybackOnPrepared(mediaPlayer);
        setMetadataForSessionInstance();
        buildNotificationForPlaying();

        EventBus.getDefault().postSticky(new SetSeekBarEvent(0, mediaPlayer.getDuration()));
    }

    private void startPlaybackOnPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    private void setMetadataForSessionInstance() {
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songs.get(songPosition).getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songs.get(songPosition).getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songs.get(songPosition).getTitle())
                .build());
    }

    //____________________________SETTING PENDING INTENTS TO NOTIFICATION ACTIONS__________________________//
    private PendingIntent retrievePlaybackAction(int requestCode) {
        PendingIntent returnIntent = null;
        int NO_FLAGS = 0;
        Intent intentAction = new Intent(this, MusicService.class);
        switch (requestCode) {
            case 1:
                intentAction.setAction(ACTION_PREV);
                returnIntent = PendingIntent.getService(this, requestCode, intentAction, NO_FLAGS);
                break;
            case 2:
                intentAction.setAction(ACTION_TOGGLE_PLAYBACK);
                returnIntent = PendingIntent.getService(this, requestCode, intentAction, NO_FLAGS);
                break;
            case 3:
                intentAction.setAction(ACTION_NEXT);
                returnIntent = PendingIntent.getService(this, requestCode, intentAction, NO_FLAGS);
                break;
        }
        return returnIntent;
    }

    //____________________________HANDLING PENDING INTENTS TO NOTIFICATION ACTIONS__________________________//

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;
        String action = playbackAction.getAction();
        switch (action) {
            case ACTION_PLAY: controls.play(); break;
            case ACTION_PAUSE: controls.pause(); break;
            case ACTION_NEXT: controls.skipToNext(); break;
            case ACTION_PREV: controls.skipToPrevious(); break;
            case ACTION_STOP: controls.stop(); break;
            case ACTION_TOGGLE_PLAYBACK: togglePlayerForAction(); break;
        }
    }

    private void togglePlayerForAction() {
        boolean isMusicPlaying = isPlaying();
        if(isMusicPlaying) buildNotificationForPaused(); else buildNotificationForPlaying();
        if(isMusicPlaying) controls.pause(); else controls.play();
    }
}
