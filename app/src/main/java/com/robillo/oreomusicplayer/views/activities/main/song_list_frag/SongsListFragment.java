package com.robillo.oreomusicplayer.views.activities.main.song_list_frag;

import android.database.Cursor;
import android.media.MediaPlayer;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.adapters.SongsAdapter;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SongListMvpView {

    @SuppressWarnings("FieldCanBeLocal")
    private final int EMPTY_CELLS_COUNT = 2;
    private MediaPlayer mediaPlayer;
    @SuppressWarnings("FieldCanBeLocal")
    private final int LOADER_ID = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private SongsAdapter mAdapter;
    private ArrayList<Song> audioList;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.progress_current_song)
    ProgressBar progressBarCurrentSong;

    @BindView(R.id.current_song_album_art)
    CircleImageView currentSongAlbumArt;

    @BindView(R.id.current_song_title)
    TextView currentSongTitle;

    @BindView(R.id.current_song_artist_duration)
    TextView currentSongArtistDuration;

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
        if(getActivity()!=null) getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        //noinspection ConstantConditions
        return new CursorLoader(getActivity(), uri, null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
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
        mAdapter = new SongsAdapter(audioList, getActivity(), mediaPlayer);
        mRecyclerView.setAdapter(mAdapter);

        if (getActivity() != null) ((MainActivity) getActivity()).startServiceForAudioList(audioList);
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
        currentSongTitle.setText(song.getTitle());

        long duration = Integer.valueOf(song.getDuration())/1000;
        long mins = duration/60;
        long seconds = duration%60;
        String temp = song.getArtist() + " ( " + String.valueOf(mins) + ":" + String.valueOf(seconds) + " )";
        currentSongArtistDuration.setText(temp);

        String path = null;
        if(getActivity()!=null) {
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
        }

        if(path!=null){
            if(getActivity()!=null) Glide.with(getActivity()).load(path).into(currentSongAlbumArt);
        }
        else {
            if(getActivity()!=null) Glide.with(getActivity()).load(R.drawable.oval_shape).into(currentSongAlbumArt);
        }
    }
}
