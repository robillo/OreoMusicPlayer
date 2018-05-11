package com.robillo.oreomusicplayer.views.activities.main.song_list_frag;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.views.activities.main.song_list_frag.adapters.SongsAdapter;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.robillo.oreomusicplayer.utils.AppConstants.FROM_ACTIVITY;
import static com.robillo.oreomusicplayer.utils.AppConstants.FROM_FRAGMENT;
import static com.robillo.oreomusicplayer.utils.AppConstants.LAUNCHED_FROM_NOTIFICATION;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("FieldCanBeLocal")
public class SongsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SongListMvpView, GestureDetector.OnGestureListener {

    @SuppressWarnings("FieldCanBeLocal")
    private final int EMPTY_CELLS_COUNT = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int LOADER_ID = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private SongsAdapter mAdapter;
    private static ArrayList<Song> audioList = null;
    Animation rotatingAlbumAnim,
            fadeInAnimationUpper,
            fadeOutAnimationUpper,
            fadeInAnimationController,
            fadeOutAnimationController;
    private static Song currentSong = null;
    private static boolean isAnimatingUpper = false;
    private static boolean isAnimatingController = false;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static long songDurationForCountDownTimer = 0;
    AppPreferencesHelper helper = null;
    private static String SORT_ORDER = null;
    private ThemeColors currentUserThemeColors = null;

    @BindView(R.id.play_pause_song)
    ImageButton playPauseSong;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.progress_current_song)
    ProgressBar progressBarCurrentSong;

    @BindView(R.id.current_song_album_art)
    CircleImageView currentSongAlbumArt;

    @BindView(R.id.current_song_title)
    TextView currentSongTitle;

    @BindView(R.id.current_song_artist)
    TextView currentSongArtist;

    @BindView(R.id.current_song_duration)
    TextView currentSongDuration;

    @BindView(R.id.bottom_controller)
    FrameLayout bottomController;

    @BindView(R.id.hide_or_show_upper)
    LinearLayout hideOrShowUpper;

    @BindView(R.id.launch_play_frag_two)
    LinearLayout launchPlayFragmentTwo;

    @BindView(R.id.sort_options)
    ImageButton sortOptions;

    @BindView(R.id.app_menu_options)
    ImageButton appMenuOptions;

    public SongsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_songs_list, container, false);
        ButterKnife.bind(this, v);

        setUp(v);
        return v;
    }

    @Override
    public void setUp(View v) {
        //noinspection ConstantConditions
        helper = new AppPreferencesHelper(getActivity());
        currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());
        refreshForUserThemeColors(currentUserThemeColors);

        fadeOutAnimationUpper = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        fadeInAnimationUpper = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fadeInAnimationController = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_controller);
        fadeOutAnimationController = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_controller);
        rotatingAlbumAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);

        //marqueue
        currentSongTitle.setSelected(true);
        currentSongArtist.setSelected(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 0) {      //scrolled up
                    if(hideOrShowUpper.getVisibility() == View.VISIBLE && !isAnimatingUpper) {
                        fadeOutUpper();
                    }
                    if(bottomController.getVisibility() == View.VISIBLE && !isAnimatingController) {
                        fadeOutController();
                    }
                }
                else {          //scrolled down
                    if(hideOrShowUpper.getVisibility() == View.GONE && !isAnimatingUpper) {
                        fadeInUpper();
                    }
                    if(bottomController.getVisibility() == View.GONE && !isAnimatingController && currentSong != null) {
                        fadeInController();
                    }
                }
            }
        });
    }


    @Override
    public void refreshForUserThemeColors(ThemeColors userThemeColors) {
        hideOrShowUpper.setBackgroundColor(getResources().getColor(userThemeColors.getColorPrimary()));
        launchPlayFragmentTwo.setBackgroundColor(getResources().getColor(userThemeColors.getColorPrimary()));
        currentSongAlbumArt.setBorderColor(getResources().getColor(userThemeColors.getColorMat()));
    }

    @Override
    public void startThemeChangeActivity() {
        if(getActivity() != null)
            ((MainActivity) getActivity()).startThemeChangeActivity();
    }

    @Override
    public void fetchSongs() {
        if(getActivity()!=null) getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(audioList == null || audioList.size() == 0) {
            fetchSongs();
        }
        else if(getActivity() != null){
            if(getActivity().getIntent().getBooleanExtra(LAUNCHED_FROM_NOTIFICATION, false)) {
                fetchSongs();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        audioList = null;
        audioList = new ArrayList<>();

        SORT_ORDER = helper.getSortOrderForSongs();

        //noinspection ConstantConditions
        return new CursorLoader(getActivity(), uri, null, null, null, SORT_ORDER);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            audioList.add(new Song());
            while (cursor.moveToNext()) {
                audioList.add(new Song(
                        returnCursorElement(cursor, MediaStore.Audio.Media.DATA),
                        returnCursorElement(cursor, MediaStore.Audio.Media.TITLE),
                        returnCursorElement(cursor, MediaStore.Audio.Media.TITLE_KEY),
                        returnCursorElement(cursor, MediaStore.Audio.Media._ID),
                        returnCursorElement(cursor, MediaStore.Audio.Media.DATE_ADDED),
                        returnCursorElement(cursor, MediaStore.Audio.Media.DATE_MODIFIED),
                        returnCursorElement(cursor, MediaStore.Audio.Media.DURATION),
                        returnCursorElement(cursor, MediaStore.Audio.Media.COMPOSER),
                        returnCursorElement(cursor, MediaStore.Audio.Media.ALBUM),
                        returnCursorElement(cursor, MediaStore.Audio.Media.ALBUM_ID),
                        returnCursorElement(cursor, MediaStore.Audio.Media.ALBUM_KEY),
                        returnCursorElement(cursor, MediaStore.Audio.Media.ARTIST),
                        returnCursorElement(cursor, MediaStore.Audio.Media.ARTIST_ID),
                        returnCursorElement(cursor, MediaStore.Audio.Media.ARTIST_KEY),
                        returnCursorElement(cursor, MediaStore.Audio.Media.SIZE),
                        returnCursorElement(cursor, MediaStore.Audio.Media.YEAR)
                ));
            }
            for(int i=0; i<EMPTY_CELLS_COUNT; i++){
                audioList.add(new Song());
            }
        }
        mAdapter = new SongsAdapter(audioList, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            if(activity.getMusicService() == null) {
                activity.startServiceForAudioList(audioList);
            }
            else {
                activity.updateServiceList(audioList);
            }
        }

        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public String returnCursorElement(Cursor cursor, String string) {
        return cursor.getString(cursor.getColumnIndex(string));
    }

    @Override
    public void setCurrentSong(Song song) {

        currentSong = song;

        if(bottomController.getVisibility()==View.GONE)
            if(hideOrShowUpper.getVisibility() == View.VISIBLE)     //either both should be visible or both shouldn't
                bottomController.setVisibility(View.VISIBLE);

        currentSongTitle.setText(song.getTitle());

        songDurationForCountDownTimer = Long.valueOf(song.getDuration());

        long duration = Long.valueOf(song.getDuration())/1000;
        long mins = duration/60;
        long seconds = duration%60;
        String temp = song.getArtist();
        currentSongArtist.setText(temp);
        temp = " ( " + String.valueOf(mins) + ":" + String.valueOf(seconds) + " )";
        currentSongDuration.setText(temp);

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
            else Glide.with(getActivity()).load(R.drawable.icon_drawable).into(currentSongAlbumArt);

        }

        //start rotating animation
        if(bottomController.getVisibility() == View.GONE) {
            hideOrShowUpper.setVisibility(View.VISIBLE);
            bottomController.setVisibility(View.VISIBLE);
        }
        playPlayer(FROM_ACTIVITY);
    }

    @OnClick(R.id.play_pause_song)
    public void playOrPausePlayer() {
        if(getActivity()!=null) {
            if(((MainActivity) getActivity()).isPlaying()){     //pause the player
                pausePlayer(FROM_FRAGMENT);
            }
            else {                                              //play the player
                playPlayer(FROM_FRAGMENT);
            }
        }
    }

    @OnClick(R.id.sort_options)
    void setSortOptions() {
        if(getActivity()!=null) ((MainActivity) getActivity()).setSongsSortFragment();
    }

    @OnClick(R.id.play_next_song)
    public void playNextSong() {
        if(getActivity()!=null) ((MainActivity) getActivity()).playNextSong();
    }

    @OnClick(R.id.play_previous_song)
    public void playPreviousSong() {
        if(getActivity()!=null) ((MainActivity) getActivity()).playPreviousSong();
    }

    @OnClick(R.id.launch_play_frag_one)
    public void setSongPlayFragmentOne() {
        if(getActivity() != null) ((MainActivity) getActivity()).setSongPlayFragment();
    }

    @OnClick(R.id.launch_play_frag_two)
    public void setSongPlayFragmentTwo() {
        if(getActivity() != null) ((MainActivity) getActivity()).setSongPlayFragment();
    }

    @OnClick(R.id.app_menu_options)
    void setAppMenuOptions() {
        startThemeChangeActivity();
    }

    @Override
    public void playPlayer(int from) {
        assert getActivity() != null;
            if(from == FROM_FRAGMENT) ((MainActivity) getActivity()).start();

        playPauseSong.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pause_black_24dp));
        resetAlbumArtAnimation();
        currentSongAlbumArt.startAnimation(rotatingAlbumAnim);
    }

    @Override
    public void pausePlayer(int from) {
        assert getActivity() != null;
            if(from == FROM_FRAGMENT) ((MainActivity) getActivity()).pause();

        playPauseSong.setImageDrawable(getActivity().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        resetAlbumArtAnimation();
    }

    @Override
    public void resetAlbumArtAnimation() {
        if(currentSongAlbumArt.getAnimation() != null) {
            currentSongAlbumArt.getAnimation().cancel();
            currentSongAlbumArt.setAnimation(null);
        }
    }

    @Override
    public void fadeOutUpper() {
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimatingUpper = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hideOrShowUpper.setVisibility(View.GONE);
                isAnimatingUpper = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        fadeOutAnimationUpper.setAnimationListener(listener);
        hideOrShowUpper.startAnimation(fadeOutAnimationUpper);
    }

    @Override
    public void fadeInUpper() {
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimatingUpper = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hideOrShowUpper.setVisibility(View.VISIBLE);
                isAnimatingUpper = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        fadeInAnimationUpper.setAnimationListener(listener);
        hideOrShowUpper.startAnimation(fadeInAnimationUpper);
    }

    @Override
    public void fadeOutController() {
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimatingController = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomController.setVisibility(View.GONE);
                isAnimatingController = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        fadeOutAnimationController.setAnimationListener(listener);
        bottomController.startAnimation(fadeOutAnimationController);
    }

    @Override
    public void fadeInController() {
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimatingController = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomController.setVisibility(View.VISIBLE);
                isAnimatingController = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        fadeInAnimationController.setAnimationListener(listener);
        bottomController.startAnimation(fadeInAnimationController);
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
        Log.e("log", "scrolling");
        if(dy < 0) {      //scrolled down
            Log.e("log", "scrolling down");
        }
        else if(dy > 0) {          //scrolled up
            Log.e("log", "scrolling up");
            Toast.makeText(getActivity(), "UP " + bottomController.getVisibility(), Toast.LENGTH_SHORT).show();
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
}
