package com.robillo.oreomusicplayer.views.activities.theme_change;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.views.activities.theme_change.adapters.ThemeChoicesAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ThemeChangeActivity extends AppCompatActivity implements ThemeChangeMvpView {

    private List<ThemeColors> themeColorsList = new ArrayList<>();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.go_back_to_main)
    ImageButton goBackToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_change);

        setup();
    }

    @Override
    public void setup() {
        ButterKnife.bind(this);

        Window window = getWindow();
        View view = window.getDecorView();
        int flags = view.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        view.setSystemUiVisibility(flags);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        AppPreferencesHelper helper = new AppPreferencesHelper(this);
        ThemeColors currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());

        inflateThemeColors();

        ThemeChoicesAdapter choicesAdapter = new ThemeChoicesAdapter(this, themeColorsList, currentUserThemeColors);
        recyclerView.setAdapter(choicesAdapter);
    }

    @Override
    public void inflateThemeColors() {
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.ALL_BLACK));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.VIOLET_LIGHT));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.INDIGO_LIGHT));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.BLUE_LIGHT));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.GREEN_LIGHT));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.YELLOW_LIGHT));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.ORANGE_LIGHT));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.RED_LIGHT));
    }

    @Override
    public void showSnackBarThemeSet(String themeName) {
        Snackbar.make(findViewById(R.id.coordinator_layout),
                getString(R.string.theme_set_successful) + " " + themeName,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @OnClick(R.id.go_back_to_main)
    void setGoBackToMain() {
        onBackPressed();
    }
}
