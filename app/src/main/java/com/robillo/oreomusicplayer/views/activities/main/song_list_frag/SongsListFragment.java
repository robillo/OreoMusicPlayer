package com.robillo.oreomusicplayer.views.activities.main.song_list_frag;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
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

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.adapters.SongsAdapter;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.services.MusicService;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        //noinspection ConstantConditions
        return new CursorLoader(getActivity(), uri, null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public String returnCursorElement(Cursor cursor, String string) {
        return cursor.getString(cursor.getColumnIndex(string));
    }
}
