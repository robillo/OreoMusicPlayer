package com.robillo.oreomusicplayer.views.activities.main.bottom_sheet;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.Song;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.robillo.oreomusicplayer.utils.AppConstants.ALBUM;
import static com.robillo.oreomusicplayer.utils.AppConstants.ALBUM_ID;
import static com.robillo.oreomusicplayer.utils.AppConstants.ALBUM_KEY;
import static com.robillo.oreomusicplayer.utils.AppConstants.ARTIST;
import static com.robillo.oreomusicplayer.utils.AppConstants.ARTIST_ID;
import static com.robillo.oreomusicplayer.utils.AppConstants.ARTIST_KEY;
import static com.robillo.oreomusicplayer.utils.AppConstants.COMPOSER;
import static com.robillo.oreomusicplayer.utils.AppConstants.DATA;
import static com.robillo.oreomusicplayer.utils.AppConstants.DATE_ADDED;
import static com.robillo.oreomusicplayer.utils.AppConstants.DATE_MODIFIED;
import static com.robillo.oreomusicplayer.utils.AppConstants.DURATION;
import static com.robillo.oreomusicplayer.utils.AppConstants.ID;
import static com.robillo.oreomusicplayer.utils.AppConstants.SIZE;
import static com.robillo.oreomusicplayer.utils.AppConstants.TITLE;
import static com.robillo.oreomusicplayer.utils.AppConstants.TITLE_KEY;
import static com.robillo.oreomusicplayer.utils.AppConstants.YEAR;

public class BottomSheetFragment extends BottomSheetDialogFragment implements BottomSheetMvpView {

    Song song = new Song();

    AppPreferencesHelper helper = null;
    @SuppressWarnings("FieldCanBeLocal")
    private ThemeColors currentUserThemeColors = null;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.artist_size)
    TextView artistAndSize;

    @BindView(R.id.line)
    ImageView lineImage;

    @BindView(R.id.line_two)
    ImageView lineImageTwo;

    @BindView(R.id.delete_song)
    LinearLayout deleteSong;

    @BindView(R.id.rate_app)
    LinearLayout rateApp;

    public BottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        setup(v);
        return v;
    }

    @Override
    public void setup(View v) {
        ButterKnife.bind(this, v);
        Bundle bundle = getArguments();

        //noinspection ConstantConditions
        helper = new AppPreferencesHelper(getActivity());
        currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());
        title.setBackgroundColor(getResources().getColor(R.color.white));
        artistAndSize.setBackgroundColor(getResources().getColor(R.color.white));
        lineImage.setBackgroundColor(getResources().getColor(currentUserThemeColors.getColorPrimaryDark()));

        title.setSelected(true);
        artistAndSize.setSelected(true);

        if (bundle != null) {

            song.setData(bundle.getString(DATA));
            song.setTitle(bundle.getString(TITLE));
            song.setTitleKey(bundle.getString(TITLE_KEY));
            song.setId(bundle.getString(ID));
            song.setDateAdded(bundle.getString(DATE_ADDED));
            song.setDateModified(bundle.getString(DATE_MODIFIED));
            song.setDuration(bundle.getString(DURATION));
            song.setComposer(bundle.getString(COMPOSER));
            song.setAlbum(bundle.getString(ALBUM));
            song.setAlbumId(bundle.getString(ALBUM_ID));
            song.setAlbumKey(bundle.getString(ALBUM_KEY));
            song.setArtist(bundle.getString(ARTIST));
            song.setArtistId(bundle.getString(ARTIST_ID));
            song.setArtistKey(bundle.getString(ARTIST_KEY));
            song.setYear(bundle.getString(YEAR));
            song.setSize(bundle.getString(SIZE));

            title.setText(song.getTitle());
            int size = Integer.valueOf(song.getSize())/1024;
            String artistSize = song.getArtist() + " (" + size + ")";
            artistAndSize.setText(artistSize);
        }
    }

    @OnClick(R.id.delete_song)
    public void setDeleteSong() {
        Uri uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(song.getId())
        );
        if(getActivity()!=null) {
            getActivity().getContentResolver().delete(uri, null, null);
            Toast.makeText(getActivity(), "song deleted", Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).rescanDevice();
            ((MainActivity) getActivity()).hideOrRemoveBottomSheet();
        }
    }

    @OnClick(R.id.rate_app)
    public void setRateApp() {

    }
}
