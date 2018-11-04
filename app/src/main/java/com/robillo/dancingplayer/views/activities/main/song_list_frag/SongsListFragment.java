package com.robillo.dancingplayer.views.activities.main.song_list_frag;

import android.animation.Animator;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.utils.ApplicationUtils;
import com.robillo.dancingplayer.views.activities.main.song_list_frag.adapters.SongsAdapter;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.views.activities.main.MainActivity;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.willowtreeapps.spruce.Spruce;
import com.willowtreeapps.spruce.animation.DefaultAnimations;
import com.willowtreeapps.spruce.sort.DefaultSort;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.robillo.dancingplayer.utils.AppConstants.BLUE_GREY;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_ACTIVITY;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_FRAGMENT;
import static com.robillo.dancingplayer.utils.AppConstants.LAUNCHED_FROM_NOTIFICATION;
import static com.robillo.dancingplayer.utils.AppConstants.PITCH_BLACK;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("FieldCanBeLocal")
public class SongsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SongListMvpView, GestureDetector.OnGestureListener {

    private boolean hideOnFastScroll = false;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EMPTY_CELLS_COUNT = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int LOADER_ID = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private SongsAdapter mAdapter;
    private ArrayList<Song> audioList = null;
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
    private ThemeColors currentUserThemeColors = null;
    private int from = FROM_FRAGMENT;

    @BindView(R.id.rotate_view_album_art)
    CardView rotateViewAlbumArtCard;

    @BindView(R.id.rescan_device)
    Button rescanDevice;

    @BindView(R.id.bottom_line)
    ImageView bottomLine;

    @BindView(R.id.top_line)
    ImageView topLine;

    @BindView(R.id.play_pause_song)
    ImageButton playPauseSong;

    @BindView(R.id.recycler_view)
    FastScrollRecyclerView mRecyclerView;

    @BindView(R.id.current_song_album_art)
    ImageView currentSongAlbumArt;

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

    @BindView(R.id.launch_play_frag_one)
    LinearLayout launchPlayFragmentOne;

    @BindView(R.id.sort_options)
    ImageButton sortOptions;

    @BindView(R.id.app_menu_options)
    ImageButton appMenuOptions;

    @BindView(R.id.app_name)
    TextView appName;

    @BindView(R.id.error_layout)
    LinearLayout errorLayout;

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
        String themeName = new AppPreferencesHelper(getActivity()).getUserThemeName();

        currentUserThemeColors = AppConstants.themeMap.get(themeName);
        refreshForUserThemeColors(currentUserThemeColors, themeName);

        fadeOutAnimationUpper = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        fadeInAnimationUpper = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fadeInAnimationController = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_controller);
        fadeOutAnimationController = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_controller);

        rotatingAlbumAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);

        //marque
        currentSongTitle.setSelected(true);
        currentSongArtist.setSelected(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setOnFastScrollStateChangeListener(new OnFastScrollStateChangeListener() {
            @Override
            public void onFastScrollStart() {
                hideOnFastScroll = true;
                if(hideOrShowUpper.getVisibility() == View.VISIBLE && !isAnimatingUpper) {
                    fadeOutUpper();
                }
                if(bottomController.getVisibility() == View.VISIBLE && !isAnimatingController) {
                    fadeOutController();
                }
            }

            @Override
            public void onFastScrollStop() {
                hideOnFastScroll = false;
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!hideOnFastScroll) {
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
            }
        });
    }

    @Override
    public void refreshForUserThemeColors(ThemeColors userThemeColors, String themeName) {
        hideOrShowUpper.setBackgroundColor(getResources().getColor(userThemeColors.getColorPrimaryDark()));
        mRecyclerView.setPopupBgColor(getResources().getColor(userThemeColors.getColorPrimaryDark()));
        launchPlayFragmentTwo.setBackgroundColor(getResources().getColor(userThemeColors.getColorPrimaryDark()));

        if(themeName.equals(PITCH_BLACK) || themeName.equals(BLUE_GREY)) {
            bottomLine.setBackgroundColor(getResources().getColor(R.color.readBlack));
            topLine.setBackgroundColor(getResources().getColor(R.color.readBlack));
        }
        else {
            bottomLine.setBackgroundColor(getResources().getColor(userThemeColors.getColorPrimary()));
            topLine.setBackgroundColor(getResources().getColor(userThemeColors.getColorPrimary()));
        }
    }

    @Override
    public void startThemeChangeActivity() {
        if(getActivity() != null)
            ((MainActivity) getActivity()).startThemeChangeActivity();
    }

    @Override
    public int getControllerVisibility() {
        return bottomController.getVisibility();
    }

    @Override
    public void refreshSongListFragmentForSongDelete(Song song, int index) {
        mAdapter.notifyItemRemoved(index);
        mAdapter.notifyItemRangeChanged(index, mAdapter.getItemCount());
        mAdapter.removeListItem(index);

        //one empty set at top, two at bottom = three
        if(mAdapter.getItemCount() <= 1 + AppConstants.EMPTY_CELLS_COUNT) showErrorLayout();
        else hideErrorLayout();
    }

    @Override
    public void fetchSongs(int from) {
        this.from = from;
        if(getActivity()!=null) {
            getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(audioList == null && from != FROM_ACTIVITY) {
            Log.e("tag", "from fragment onresume");
            fetchSongs(FROM_FRAGMENT);
        }
        else if(getActivity() != null){
            if(getActivity().getIntent().getBooleanExtra(LAUNCHED_FROM_NOTIFICATION, false)) {
                Log.e("tag", "from fragment notification");
                fetchSongs(FROM_FRAGMENT);
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //noinspection ConstantConditions
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        List<Song> list = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                list.add(new Song(
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
        }

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) activity.putSongsListIntoDatabase(list);

        if(from == FROM_ACTIVITY) {
            Toast.makeText(getActivity(), R.string.rescanned, Toast.LENGTH_SHORT).show();
            from = FROM_FRAGMENT;
        }
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void renderRecyclerViewForAudioList(List<Song> list) {

        MainActivity activity = (MainActivity) getActivity();
        if(activity != null) activity.removeObservers();

        audioList = null;
        audioList = new ArrayList<>();

        audioList.add(0, new Song());

        audioList.addAll(list);

        for(int i=0; i<EMPTY_CELLS_COUNT; i++) audioList.add(new Song());

        mAdapter = new SongsAdapter(audioList, getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public String returnCursorElement(Cursor cursor, String string) {
        return cursor.getString(cursor.getColumnIndex(string));
    }

    @Override
    public void notifyDataSetChanged(int position) {
        if(mAdapter != null) {
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            mAdapter.removeListItem(position);
        }
    }

    @Override
    public void setCurrentSong(Song song) {

        currentSong = song;

        if(currentSong == null)
            return;

        if(bottomController.getVisibility()==View.GONE)
            if(hideOrShowUpper.getVisibility() == View.VISIBLE)     //either both should be visible or both shouldn't
                bottomController.setVisibility(View.VISIBLE);

        currentSongTitle.setText(song.getTitle());

        songDurationForCountDownTimer = Long.valueOf(song.getDuration());

        long duration = Long.valueOf(song.getDuration())/1000;
        currentSongArtist.setText(song.getArtist());
        currentSongDuration.setText(new ApplicationUtils().formatStringOutOfSeconds((int) duration));

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
            Glide.with(getActivity())
                    .load(path)
                    .apply(RequestOptions
                            .centerCropTransform()
                            .placeholder(R.drawable.icon_drawable)
                    )
                    .into(currentSongAlbumArt);

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

    @OnClick(R.id.app_name)
    public void setAppName() {

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
        rotateViewAlbumArtCard.startAnimation(rotatingAlbumAnim);
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
        if(rotateViewAlbumArtCard.getAnimation() != null) {
            rotateViewAlbumArtCard.getAnimation().cancel();
            rotateViewAlbumArtCard.setAnimation(null);
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
        //noinspection StatementWithEmptyBody
        if(dy < 0) {      //scrolled down
        }
        else //noinspection StatementWithEmptyBody
            if(dy > 0) {          //scrolled up
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
    public void showErrorLayout() {
        mRecyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        new Spruce
                .SpruceBuilder(errorLayout)
                .sortWith(new DefaultSort(/*interObjectDelay=*/30L))
                .animateWith(new Animator[] {DefaultAnimations.shrinkAnimator(errorLayout, /*duration=*/200)})
                .start();
    }

    @Override
    public void hideErrorLayout() {
        mRecyclerView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.rescan_device)
    public void setRescanDevice() {
        errorLayout.setVisibility(View.GONE);
        fetchSongs(FROM_FRAGMENT);
    }
}
