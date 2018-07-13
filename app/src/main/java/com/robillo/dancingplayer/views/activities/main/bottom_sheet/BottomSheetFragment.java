package com.robillo.dancingplayer.views.activities.main.bottom_sheet;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.utils.ApplicationUtils;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.robillo.dancingplayer.utils.AppConstants.ALBUM;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_ID;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_ID;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.COMPOSER;
import static com.robillo.dancingplayer.utils.AppConstants.DATA;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED;
import static com.robillo.dancingplayer.utils.AppConstants.DEFAULT_PLAYLIST_TITLE;
import static com.robillo.dancingplayer.utils.AppConstants.DURATION;
import static com.robillo.dancingplayer.utils.AppConstants.FROM_SONGS_LIST;
import static com.robillo.dancingplayer.utils.AppConstants.ID;
import static com.robillo.dancingplayer.utils.AppConstants.INDEX;
import static com.robillo.dancingplayer.utils.AppConstants.MOST_PLAYED;
import static com.robillo.dancingplayer.utils.AppConstants.RECENTLY_ADDED;
import static com.robillo.dancingplayer.utils.AppConstants.RECENTLY_PLAYED;
import static com.robillo.dancingplayer.utils.AppConstants.SIZE;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE_KEY;
import static com.robillo.dancingplayer.utils.AppConstants.YEAR;

public class BottomSheetFragment extends BottomSheetDialogFragment implements BottomSheetMvpView {

    Song song = new Song();
    int index = -1;

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

    @BindView(R.id.add_to_playlist)
    LinearLayout addToPlaylist;

    @BindView(R.id.remove_from_playlist)
    LinearLayout removeFromPlaylist;

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

        showRemoveFromPlaylistIfUserPlaylist(v);

        if (bundle != null) {
            index = bundle.getInt(INDEX);

            song.setData(bundle.getString(DATA));
            song.setTitle(bundle.getString(TITLE));
            song.setTitleKey(bundle.getString(TITLE_KEY));
            //noinspection ConstantConditions
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
            String artistSize = song.getArtist() + " (" + new ApplicationUtils().formatSizeKBtoMB(size) + ")";
            artistAndSize.setText(artistSize);
        }
    }

    @Override
    public void showRemoveFromPlaylistIfUserPlaylist(View v) {
        if(getActivity() != null)  {
            String current = new AppPreferencesHelper(getActivity()).getCurrentPlaylistTitle();
            if(current.equals(DEFAULT_PLAYLIST_TITLE) || current.equals(MOST_PLAYED) || current.equals(RECENTLY_ADDED) || current.equals(RECENTLY_PLAYED)) {
                removeFromPlaylist.setVisibility(View.GONE);
                v.findViewById(R.id.line_remove_from_playlist).setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.delete_song)
    public void setDeleteSong() {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null) activity.deleteSong(getActivity(), index, song, song.getId());
    }

    @OnClick(R.id.rate_app)
    public void setRateApp() {
        if(getActivity() != null) {
            Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
            }
        }
    }

    @OnClick(R.id.add_to_playlist)
    public void addToPlaylist() {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null) {
            activity.hideOrRemoveBottomSheet();
            activity.showPlaylistBottomSheet(FROM_SONGS_LIST, song.getId());
        }
    }

    @OnClick(R.id.remove_from_playlist)
    public void removeFromPlaylist() {
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null) {
            activity.hideOrRemoveBottomSheet();
            activity.removeSongCurrentPlaylist(song, index);
        }
    }
}
