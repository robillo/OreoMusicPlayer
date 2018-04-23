package com.robillo.oreomusicplayer.views.activities.main.song_play_frag;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.utils.ApplicationUtils;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.robillo.oreomusicplayer.utils.AppConstants.FROM_ACTIVITY;
import static com.robillo.oreomusicplayer.utils.AppConstants.FROM_FRAGMENT;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongPlayFragment extends Fragment implements SongPlayMvpView, GestureDetector.OnGestureListener {

    private CountDownTimer timer;
    @SuppressWarnings("FieldCanBeLocal")
    private static Song currentSong = null;
    @SuppressWarnings("FieldCanBeLocal")
    private boolean isHidingAlready = false;
    private GestureDetector mGestureDetector;
    private AppPreferencesHelper helper = null;
    @SuppressWarnings("FieldCanBeLocal")
    private ThemeColors currentUserThemeColors = null;
    private Animation rotatingAlbumAnim;
    private int currentDurationProgress, totalDurationProgress;

    @BindView(R.id.bottom_controller)
    LinearLayout bottomController;

    @BindView(R.id.back_to_song_list)
    ImageButton backToSongList;

    @BindView(R.id.album_name)
    TextView currentSongAlbumName;

    @BindView(R.id.song_menu_options)
    ImageButton songMenuOptions;

    @BindView(R.id.repeat_song)
    ImageButton repeatSong;

    @BindView(R.id.shuffle_songs)
    ImageButton shuffleSongs;

    @BindView(R.id.current_song_current_progress)
    TextView currentSongCurrentProgress;

    @BindView(R.id.current_song_max_progress)
    TextView currentSongMaxProgress;

    @BindView(R.id.current_song_progress_seekbar)
    SeekBar currentSongProgressSeekBar;

    @BindView(R.id.back_ten_seconds)
    ImageButton tenSecondsBack;

    @BindView(R.id.forward_ten_seconds)
    ImageButton tenSecondsForward;

    @BindView(R.id.play_pause_song)
    ImageButton playPauseSong;

    @BindView(R.id.current_song_album_art)
    CircleImageView currentSongAlbumArt;

    @BindView(R.id.current_song_title)
    TextView currentSongTitle;

    @BindView(R.id.current_song_artist)
    TextView currentSongArtist;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    public SongPlayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_song_play, container, false);
        setUp(v);
        return v;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setUp(View v) {
        ButterKnife.bind(this, v);

        //noinspection ConstantConditions
        helper = new AppPreferencesHelper(getActivity());
        currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());
        refreshForUserThemeColors(currentUserThemeColors);
        rotatingAlbumAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        setPreferencesToViews();

        mGestureDetector = new GestureDetector(getActivity(), this);
        coordinatorLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

//      //set duration
        currentDurationProgress = ((MainActivity) getActivity()).getCurrentSongDuration()/1000;
        totalDurationProgress = ((MainActivity) getActivity()).getDuration()/1000;

        setDurationValues(currentDurationProgress, totalDurationProgress);

        //marqueue
        currentSongTitle.setSelected(true);
        currentSongArtist.setSelected(true);
        currentSongAlbumName.setSelected(true);

        if(getActivity() != null){
            currentSong = ((MainActivity) getActivity()).getCurrentSong();
            setCurrentSong(currentSong);
        }
    }


    @Override
    public void refreshForUserThemeColors(ThemeColors currentUserThemeColors) {
        bottomController.setBackgroundColor(getResources().getColor(currentUserThemeColors.getColorPrimary()));
        currentSongAlbumArt.setBorderColor(getResources().getColor(currentUserThemeColors.getColorMat()));
    }

    @Override
    public void setCurrentSong(Song song) {

        currentSong = song;

        currentSongTitle.setText(song.getTitle());
        currentSongAlbumName.setText(song.getAlbum());

        currentSongArtist.setText(song.getArtist());

        currentSongMaxProgress
                .setText(
                        new ApplicationUtils()
                                .formatStringOutOfSeconds(
                                        Integer.valueOf(song.getDuration())/1000
                                )
                );

        currentSongCurrentProgress
                .setText(
                        new ApplicationUtils()
                                .formatStringOutOfSeconds(
                                        currentDurationProgress
                                )
                );

        String path = null;
        if(getActivity()!=null) {

            //get path for the album art for this song
            Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {String.valueOf(song.getAlbumId())},
                    null);
            if(cursor!=null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                // do whatever you need to do
                cursor.close();
            }

            //set album art
            if(path!=null) Glide.with(getActivity()).load(path).into(currentSongAlbumArt);
            else Glide.with(getActivity()).load(R.drawable.oval_shape).into(currentSongAlbumArt);

        }

        if(getActivity()!=null) {                                                   //play or pause
            if(((MainActivity) getActivity()).isPlaying()) playPlayer(FROM_ACTIVITY);
            else pausePlayer(FROM_ACTIVITY);
        }
    }

    @Override
    public void resetAlbumArtAnimation() {
        if(currentSongAlbumArt.getAnimation() != null) {
            currentSongAlbumArt.getAnimation().cancel();
            currentSongAlbumArt.setAnimation(null);
        }
    }

    @Override
    public void playPlayer(int from) {
        assert getActivity() != null;
        if(from == FROM_FRAGMENT) ((MainActivity) getActivity()).start();

        playPauseSong.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pause_circle_outline_black_24dp));
        resetAlbumArtAnimation();
        currentSongAlbumArt.startAnimation(rotatingAlbumAnim);
    }

    @Override
    public void pausePlayer(int from) {
        assert getActivity() != null;
        if(from == FROM_FRAGMENT) ((MainActivity) getActivity()).pause();

        playPauseSong.setImageDrawable(getActivity().getDrawable(R.drawable.ic_play_circle_outline_black_24dp));
        resetAlbumArtAnimation();
    }

    @Override
    public void setPreferencesToViews() {
        if(getActivity() != null) {

            switch (helper.isRepeatModeOn()) {
                case AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE:
                    //change tint to linear traverse => "unticked grey" and drawable to loop
                    repeatSong
                            .setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_repeat_black_24dp)
                            );
                    repeatSong
                            .setColorFilter(
                                    ContextCompat.getColor(getActivity(), R.color.colorTextOne)
                            );
                    break;
                case AppConstants.REPEAT_MODE_VALUE_LOOP:
                    //change tint to loop list => "ticked primary" and drawable to loop
                    repeatSong
                            .setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_repeat_black_24dp)
                            );
                    repeatSong
                            .setColorFilter(
                                    ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorPrimary())
                            );
                    break;
                case AppConstants.REPEAT_MODE_VALUE_REPEAT:
                    //change tint to repeat => "ticked primary" and drawable to repeat one
                    repeatSong
                            .setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_repeat_one_black_24dp)
                            );
                    repeatSong
                            .setColorFilter(
                                    ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorPrimary())
                            );
                    break;
            }


            if(helper.isShuffleModeOn())
                //change tint to repeat => "repeat"
                shuffleSongs
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorPrimary())
                        );
            else
                //change tint to repeat => "non repeat"
                shuffleSongs
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), R.color.colorTextOne)
                        );
        }
    }

    @Override
    public void seekTenSecondsForward() {
        if(getActivity()!=null) ((MainActivity) getActivity()).seekTenSecondsForward();
    }

    @Override
    public void seekTenSecondsBackwards() {
        if(getActivity()!=null) ((MainActivity) getActivity()).seekTenSecondsBackwards();
    }

    @OnClick(R.id.play_pause_song)
    public void playOrPausePlayer() {
        if(getActivity()!=null) {
            if(((MainActivity) getActivity()).isPlaying()) pausePlayer(FROM_FRAGMENT);
            else playPlayer(FROM_FRAGMENT);
        }
    }

    @OnClick(R.id.play_next_song)
    public void playNextSong() {
        if(getActivity()!=null) ((MainActivity) getActivity()).playNextSong();
    }

    @OnClick(R.id.play_previous_song)
    public void playPreviousSong() {
        if(getActivity()!=null) ((MainActivity) getActivity()).playPreviousSong();
    }

    @OnClick(R.id.shuffle_songs)
    void toggleShuffleSongs() {
        if(getActivity() != null) {

            AppPreferencesHelper helper = new AppPreferencesHelper(getActivity());

            if (helper.isShuffleModeOn()) {
                helper.setIsShuffleModeOn(false);

                //change tint to normal => "non repeat"
                shuffleSongs
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), R.color.colorTextOne)
                        );
            } else {
                helper.setIsShuffleModeOn(true);

                //change tint to repeat => "repeat"
                shuffleSongs
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorPrimary())
                        );
            }

            ((MainActivity) getActivity()).toggleShuffleModeInService();
        }
    }

    @OnClick(R.id.repeat_song)
    void toggleRepeatSong() {
        if(getActivity() != null) {

            AppPreferencesHelper helper = new AppPreferencesHelper(getActivity());

            switch (helper.isRepeatModeOn()) {
                case AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE:
                    helper.setIsRepeatModeOn(AppConstants.REPEAT_MODE_VALUE_LOOP);
                    repeatSong
                            .setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_repeat_black_24dp)
                            );
                    repeatSong
                            .setColorFilter(
                                    ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorPrimary())
                            );

                    Log.e("tag", helper.isRepeatModeOn());
                    break;
                case AppConstants.REPEAT_MODE_VALUE_LOOP:
                    helper.setIsRepeatModeOn(AppConstants.REPEAT_MODE_VALUE_REPEAT);
                    repeatSong
                            .setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_repeat_one_black_24dp)
                            );
                    repeatSong
                            .setColorFilter(
                                    ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorPrimary())
                            );
                    Log.e("tag", helper.isRepeatModeOn());
                    break;
                case AppConstants.REPEAT_MODE_VALUE_REPEAT:
                    helper.setIsRepeatModeOn(AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE);
                    repeatSong
                            .setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_repeat_black_24dp)
                            );
                    repeatSong
                            .setColorFilter(
                                    ContextCompat.getColor(getActivity(), R.color.colorTextOne)
                            );
                    Log.e("tag", helper.isRepeatModeOn());
                    break;
            }

            ((MainActivity) getActivity()).toggleRepeatModeInService(helper.isRepeatModeOn());
        }
    }

    @OnClick(R.id.forward_ten_seconds)
    void setTenSecondsForward() {
        currentDurationProgress += 10;
        //even if it is greater than song duration, music service
        //will change to next song, setcurrent song will be called
        //and current duration will be reset to zero
        setProgressToSeekBar(currentDurationProgress, totalDurationProgress);
        seekTenSecondsForward();
    }

    @OnClick(R.id.back_ten_seconds)
    void setTenSecondsBack() {
        currentDurationProgress -= 10;
        setProgressToSeekBar(currentDurationProgress, totalDurationProgress);
        seekTenSecondsBackwards();
    }

    @OnClick(R.id.current_song_progress_seekbar)
    void setCurrentSongProgressSeekBar() {

    }

    @OnClick(R.id.back_to_song_list)
    void setBackToSongList() {
        if(getActivity() != null) getActivity().onBackPressed();
    }

    @OnClick(R.id.song_menu_options)
    void setSongMenuOptions() {

    }

    @OnClick(R.id.current_song_current_progress)
    void setCurrentSongCurrentProgress() {

    }

    @OnClick(R.id.coordinator_layout)
    void  setCoordinatorLayout() {

    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent eventOne, MotionEvent eventTwo, float dx, float dy) {
        //noinspection StatementWithEmptyBody
        if(dy < 0) {      //scrolled down
            if(!isHidingAlready)
                if(getActivity() != null)
                    getActivity().onBackPressed();
        }
        else {          //scrolled up

        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void setProgressToSeekBar(int currentDuration, int totalDuration) {
        if(totalDuration != 0) {
            int percentProgress = (currentDuration * 100)/totalDuration;
            currentSongProgressSeekBar.setProgress(percentProgress);
            currentSongCurrentProgress
                    .setText(
                            new ApplicationUtils()
                                    .formatStringOutOfSeconds(
                                            currentDuration
                                    )
                    );
            Log.e(
                    "tag",
                    currentDuration + " " +
                            totalDuration + " " +
                            currentSongProgressSeekBar.getProgress()
            );
        }
        else {
            currentSongProgressSeekBar.setProgress(0);
        }
    }

    @Override
    public void setDurationValues(int currentDuration, int totalDuration) {
        currentDurationProgress = currentDuration;
        totalDurationProgress = totalDuration;
        setProgressToSeekBar(currentDuration, totalDuration);

        startTimerForProgress();

    }

    @Override
    public void startTimerForProgress() {
        updateTimer((totalDurationProgress - currentDurationProgress) * 1000, 1000);
        timer.start();
    }

    @Override
    public void stopTimerForProgress() {
        if(timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void updateTimer(int millisInFuture, int countDownInterval) {
        if(timer != null) timer = null;

        timer = new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentDurationProgress += 1;
                setProgressToSeekBar(currentDurationProgress, totalDurationProgress);
            }

            @Override
            public void onFinish() {
                timer.cancel();
            }
        };
    }
}
