package com.robillo.oreomusicplayer.views.fragments;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.adapters.SongsAdapter;
import com.robillo.oreomusicplayer.models.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    @SuppressWarnings("FieldCanBeLocal")
    private final int PERMISSION_REQUEST_CODE = 0;
    private final int LOADER_ID = 0;
    private List<Song> audioList;
    private SongsAdapter mAdapter;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    public SongsListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_songs_list, container, false);
        ButterKnife.bind(this, v);
        askForPermissions();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if(checkForPermission()){
            getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
        return v;
    }

    void askForPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                getActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    boolean checkForPermission(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        return new CursorLoader(getActivity(), uri, null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String titleKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE_KEY));
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                String dateModified = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String composer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String albumKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String artistId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                String artistKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_KEY));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                String year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));

                // Save to audioList
                audioList.add(new Song(data, title, titleKey, id, dateAdded, dateModified, duration, composer, album, albumId,
                        albumKey, artist, artistId, artistKey, size, year));
            }
        }
        mAdapter = new SongsAdapter(audioList, getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("song_parcelable", (ArrayList<? extends Parcelable>) audioList);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            audioList = savedInstanceState.getParcelableArrayList("song_parcelable");
            Log.e("AUDIO LIST SIZE", " " + (audioList != null ? audioList.size() : 0));
        }
    }
}
