package com.robillo.oreomusicplayer.views.activities.theme_change;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.utils.AppConstants;

public class ThemeChangeActivity extends AppCompatActivity implements ThemeChangeMvpView {

    @SuppressWarnings("FieldCanBeLocal")
    private ThemeColors currentUserThemeColors = null;
    @SuppressWarnings("FieldCanBeLocal")
    private AppPreferencesHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_change);

        setup();
    }

    @Override
    public void setup() {
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

        helper = new AppPreferencesHelper(this);
        currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());
    }
}
