package com.robillo.dancingplayer.views.activities.theme_change;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.views.activities.theme_change.adapters.ThemeChoicesAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ThemeChangeActivity extends AppCompatActivity implements ThemeChangeMvpView {

    private List<ThemeColors> themeColorsList = new ArrayList<>();
    private AppPreferencesHelper helper = null;

    @BindView(R.id.ten_mp)
    TextView ten_mp;

    @BindView(R.id.ten_rp)
    TextView ten_rp;

    @BindView(R.id.ten_ra)
    TextView ten_ra;

    @BindView(R.id.fifty_mp)
    TextView fifty_mp;

    @BindView(R.id.fifty_rp)
    TextView fifty_rp;

    @BindView(R.id.fifty_ra)
    TextView fifty_ra;

    @BindView(R.id.hundred_mp)
    TextView hundred_mp;

    @BindView(R.id.hundred_rp)
    TextView hundred_rp;

    @BindView(R.id.hundred_ra)
    TextView hundred_ra;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.go_back_to_main)
    ImageButton goBackToMain;

    @BindView(R.id.rescan_device)
    Button rescanDevice;

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

        initialisePreferenceHelper();
        ThemeColors currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());

        inflateThemeColors();

        setInitialStatePlaylistSongsCount();

        ThemeChoicesAdapter choicesAdapter = new ThemeChoicesAdapter(this, themeColorsList, currentUserThemeColors);
        recyclerView.setAdapter(choicesAdapter);
    }

    @Override
    public void inflateThemeColors() {
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.PITCH_BLACK));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.BLUE_GREY));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.DEEP_BROWN));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.DEEP_BLUE));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.DEEP_GREEN));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.DEEP_ORANGE));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.AMBER));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.CYAN));
        themeColorsList.add(AppConstants.themeMap.get(AppConstants.LIME));
    }

    @Override
    public void showSnackBarThemeSet(String themeName) {
        Snackbar.make(findViewById(R.id.coordinator_layout),
                getString(R.string.theme_set_successful) + " " + themeName,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void setInitialStatePlaylistSongsCount() {
        initialisePreferenceHelper();

        int mpCount = helper.getMostPlayedCount();
        int rpCount = helper.getRecentlyPlayedCount();
        int raCount = helper.getRecentlyAddedCount();

        setColorsToViews(ten_mp, fifty_mp, hundred_mp, mpCount);
        setColorsToViews(ten_ra, fifty_ra, hundred_ra, raCount);
        setColorsToViews(ten_rp, fifty_rp, hundred_rp, rpCount);
    }

    @Override
    public void setColorsToViews(TextView tenView, TextView fiftyView, TextView hundredView, int count) {
        switch (count) {
            case 10: {
                tenView.setTextColor(getResources().getColor(R.color.white));
                tenView.setBackgroundColor(getResources().getColor(R.color.green_primary_dark));

                fiftyView.setTextColor(getResources().getColor(R.color.colorTextOne));
                fiftyView.setBackgroundColor(getResources().getColor(R.color.white));

                hundredView.setTextColor(getResources().getColor(R.color.colorTextOne));
                hundredView.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            }
            case 50: {
                tenView.setTextColor(getResources().getColor(R.color.colorTextOne));
                tenView.setBackgroundColor(getResources().getColor(R.color.white));

                fiftyView.setTextColor(getResources().getColor(R.color.white));
                fiftyView.setBackgroundColor(getResources().getColor(R.color.green_primary_dark));

                hundredView.setTextColor(getResources().getColor(R.color.colorTextOne));
                hundredView.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            }
            case 100: {
                tenView.setTextColor(getResources().getColor(R.color.colorTextOne));
                tenView.setBackgroundColor(getResources().getColor(R.color.white));

                fiftyView.setTextColor(getResources().getColor(R.color.colorTextOne));
                fiftyView.setBackgroundColor(getResources().getColor(R.color.white));

                hundredView.setTextColor(getResources().getColor(R.color.white));
                hundredView.setBackgroundColor(getResources().getColor(R.color.green_primary_dark));
                break;
            }
        }
    }

    @Override
    public void initialisePreferenceHelper() {
        if(helper == null) helper = new AppPreferencesHelper(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @OnClick(R.id.go_back_to_main)
    void setGoBackToMain() {
        setResult(Activity.RESULT_CANCELED, new Intent());
        onBackPressed();
    }

    @OnClick(R.id.rescan_device)
    void setRescanDevice() {
        setResult(Activity.RESULT_OK, new Intent());
        onBackPressed();
    }

    @OnClick(R.id.ten_mp)
    public void setTen_mp() {
        initialisePreferenceHelper();
        helper.setMostPlayedCount(10);
        setColorsToViews(ten_mp, fifty_mp, hundred_mp, 10);
    }

    @OnClick(R.id.ten_rp)
    public void setTen_rp() {
        initialisePreferenceHelper();
        helper.setRecentlyPlayedCount(10);
        setColorsToViews(ten_rp, fifty_rp, hundred_rp, 10);
    }

    @OnClick(R.id.ten_ra)
    public void setTen_ra() {
        initialisePreferenceHelper();
        helper.setRecentlyAddedCount(10);
        setColorsToViews(ten_ra, fifty_ra, hundred_ra, 10);
    }

    @OnClick(R.id.fifty_mp)
    public void setFifty_mp() {
        initialisePreferenceHelper();
        helper.setMostPlayedCount(50);
        setColorsToViews(ten_mp, fifty_mp, hundred_mp, 50);
    }

    @OnClick(R.id.fifty_rp)
    public void setFifty_rp() {
        initialisePreferenceHelper();
        helper.setRecentlyPlayedCount(50);
        setColorsToViews(ten_rp, fifty_rp, hundred_rp, 50);
    }

    @OnClick(R.id.fifty_ra)
    public void setFifty_ra() {
        initialisePreferenceHelper();
        helper.setRecentlyAddedCount(50);
        setColorsToViews(ten_ra, fifty_ra, hundred_ra, 50);
    }

    @OnClick(R.id.hundred_mp)
    public void setHundred_mp() {
        initialisePreferenceHelper();
        helper.setMostPlayedCount(100);
        setColorsToViews(ten_mp, fifty_mp, hundred_mp, 100);
    }

    @OnClick(R.id.hundred_rp)
    public void setHundred_rp() {
        initialisePreferenceHelper();
        helper.setRecentlyPlayedCount(100);
        setColorsToViews(ten_rp, fifty_rp, hundred_rp, 100);
    }

    @OnClick(R.id.hundred_ra)
    public void setHundred_ra() {
        initialisePreferenceHelper();
        helper.setRecentlyAddedCount(100);
        setColorsToViews(ten_ra, fifty_ra, hundred_ra, 100);
    }

}
