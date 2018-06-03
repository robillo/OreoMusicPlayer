package com.robillo.oreomusicplayer.views.activities.main.bottom_sheet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.utils.AppConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.robillo.oreomusicplayer.utils.AppConstants.ARTIST;
import static com.robillo.oreomusicplayer.utils.AppConstants.SIZE;
import static com.robillo.oreomusicplayer.utils.AppConstants.TITLE;

public class BottomSheetFragment extends BottomSheetDialogFragment implements BottomSheetMvpView {

    AppPreferencesHelper helper = null;
    @SuppressWarnings("FieldCanBeLocal")
    private ThemeColors currentUserThemeColors = null;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.artist_size)
    TextView artistAndSize;

    @BindView(R.id.line)
    ImageView lineImage;

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
        title.setBackgroundColor(getResources().getColor(currentUserThemeColors.getColorPrimary()));
        artistAndSize.setTextColor(getResources().getColor(currentUserThemeColors.getColorPrimaryDark()));
        lineImage.setBackgroundColor(getResources().getColor(currentUserThemeColors.getColorPrimaryDark()));

        if (bundle != null) {
            title.setText(bundle.getString(TITLE));
            String artistSize = bundle.getString(ARTIST) + " (" + bundle.getString(SIZE) + ")";
            artistAndSize.setText(artistSize);
        }
    }
}
