package com.robillo.dancingplayer.views.activities.main.songs_sort_frag;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.SortItem;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.views.activities.main.songs_sort_frag.adapters.SortSongsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DURATION_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DURATION_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.SIZE_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.SIZE_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.YEAR_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.YEAR_DESCENDING;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsSortFragment extends Fragment implements SongsSortMvpView {

    List<SortItem> sortItems = new ArrayList<>();
    AppPreferencesHelper helper = null;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.header)
    TextView header;

    @BindView(R.id.line_colored)
    ImageView lineColored;

    public SongsSortFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_songs_sort, container, false);
        setup(v);
        return v;
    }

    @Override
    public void setup(View v) {
        ButterKnife.bind(this, v);

        //noinspection ConstantConditions
        helper = new AppPreferencesHelper(getActivity());
        ThemeColors currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());
        refreshForUserThemeColors(currentUserThemeColors);

        inflateSortItemsList();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        AppPreferencesHelper helper = new AppPreferencesHelper(getActivity());
        SortSongsAdapter sortAdapter = new SortSongsAdapter(getActivity(), sortItems, helper.getSortOrderForSongs());
        recyclerView.setAdapter(sortAdapter);
    }

    @Override
    public void refreshForUserThemeColors(ThemeColors currentUserThemeColors) {
        header.setBackgroundColor(getResources().getColor(currentUserThemeColors.getColorPrimaryDark()));
        lineColored.setBackgroundColor(getResources().getColor(currentUserThemeColors.getColorPrimary()));
    }

    @Override
    public void inflateSortItemsList() {
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(SIZE_ASCENDING), SIZE_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(SIZE_DESCENDING), SIZE_DESCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(YEAR_ASCENDING), YEAR_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(YEAR_DESCENDING), YEAR_DESCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(ALBUM_ASCENDING), ALBUM_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(ALBUM_DESCENDING), ALBUM_DESCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(TITLE_ASCENDING), TITLE_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(TITLE_DESCENDING), TITLE_DESCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(ARTIST_ASCENDING), ARTIST_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(ARTIST_DESCENDING), ARTIST_DESCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(DURATION_ASCENDING), DURATION_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(DURATION_DESCENDING), DURATION_DESCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(DATE_ADDED_ASCENDING), DATE_ADDED_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(DATE_ADDED_DESCENDING), DATE_ADDED_DESCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(DATE_MODIFIED_ASCENDING), DATE_MODIFIED_ASCENDING));
        sortItems.add(new SortItem(AppConstants.sortOrderMap.get(DATE_MODIFIED_DESCENDING), DATE_MODIFIED_DESCENDING));
    }

    @OnClick(R.id.coordinator_layout)
    public void setCoordinatorLayout() {
        if(getActivity() != null) getActivity().onBackPressed();
    }
}
