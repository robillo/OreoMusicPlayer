package com.robillo.oreomusicplayer.views.activities.main.song_play_frag;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
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
public class SongPlayFragment extends Fragment implements SongPlayMvpView {

    @SuppressWarnings("FieldCanBeLocal")
    private static Song currentSong = null;
    private AppPreferencesHelper helper = null;
    @SuppressWarnings("FieldCanBeLocal")
    private ThemeColors currentUserThemeColors = null;
    Animation rotatingAlbumAnim;

    @BindView(R.id.bottom_controller)
    LinearLayout bottomController;

    @BindView(R.id.back_to_song_list)
    ImageButton backToSongList;

    @BindView(R.id.album_name)
    TextView albumName;

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

    @Override
    public void setUp(View v) {
        ButterKnife.bind(this, v);

        helper = new AppPreferencesHelper(getActivity());
        currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());
        refreshForUserThemeColors(currentUserThemeColors);

        rotatingAlbumAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);

        setPreferencesToViews();

        if(getActivity() != null){
            currentSong = ((MainActivity) getActivity()).getCurrentSong();
            setCurrentSong(currentSong);
        }
    }


    @Override
    public void refreshForUserThemeColors(ThemeColors currentUserThemeColors) {
        bottomController.setBackgroundColor(getResources().getColor(currentUserThemeColors.getColorPrimaryDark()));
    }

    @Override
    public void setCurrentSong(Song song) {

        currentSong = song;

        currentSongTitle.setText(song.getTitle());
        albumName.setText(song.getAlbum());

        currentSongArtist.setText(song.getArtist());

        long duration = Long.valueOf(song.getDuration())/1000;
        long mins = duration/60;
        long seconds = duration%60;

        String lhs, rhs;

        if(mins < 10) lhs = "0" + String.valueOf(mins);
        else lhs = String.valueOf(mins);

        if(seconds < 10) rhs = "0" + String.valueOf(seconds);
        else rhs = String.valueOf(seconds);

        String temp = lhs + ":" + rhs;
        currentSongMaxProgress.setText(temp);

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

        if(getActivity()!=null) {
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

            if(helper.isRepeatModeOn())
                //change tint to repeat => "repeat"
                repeatSong
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorAccent())
                        );
            else
                //change tint to repeat => "non repeat"
                repeatSong
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), R.color.colorTextOne)
                        );

            if(helper.isShuffleModeOn())
                //change tint to repeat => "repeat"
                shuffleSongs
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorAccent())
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
                                ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorAccent())
                        );
            }

            ((MainActivity) getActivity()).toggleShuffleModeInService();
        }
    }

    @OnClick(R.id.repeat_song)
    void toggleRepeatSong() {
        if(getActivity() != null) {

            AppPreferencesHelper helper = new AppPreferencesHelper(getActivity());

            if(helper.isRepeatModeOn()) {
                helper.setIsRepeatModeOn(false);

                //change tint to normal => "non repeat"
                repeatSong
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), R.color.colorTextOne)
                        );
            }
            else {
                helper.setIsRepeatModeOn(true);

                //change tint to repeat => "repeat"
                repeatSong
                        .setColorFilter(
                                ContextCompat.getColor(getActivity(), currentUserThemeColors.getColorAccent())
                        );
            }

            ((MainActivity) getActivity()).toggleRepeatModeInService();
        }
    }

    @OnClick(R.id.forward_ten_seconds)
    void setTenSecondsForward() {
        seekTenSecondsForward();
    }

    @OnClick(R.id.back_ten_seconds)
    void setTenSecondsBack() {
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
}
