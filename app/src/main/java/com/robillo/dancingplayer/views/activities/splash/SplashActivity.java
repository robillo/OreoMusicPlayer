package com.robillo.dancingplayer.views.activities.splash;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.views.activities.launcher.LauncherActivity;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SplashActivity extends AppCompatActivity implements SplashMvpView {

    boolean permissionsAllowed;

    @BindView(R.id.app_name)
    TextView appName;

    @BindView(R.id.app_icon)
    CircleImageView appIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        setup();
    }

    @Override
    public void setup() {
        checkForPermissions();

        ThemeColors colors = AppConstants
                .themeMap
                .get(new AppPreferencesHelper(this).getUserThemeName());

        appName.setBackgroundColor(getResources().getColor(colors.getColorPrimary()));
        appIcon.setBorderColor(getResources().getColor(colors.getColorPrimary()));

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, colors.getColorPrimaryDark()));

        new Handler().postDelayed(() -> {
            if(permissionsAllowed) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            else {
                startActivity(new Intent(SplashActivity.this, LauncherActivity.class));
            }
        }, 1000);
    }

    @Override
    public void checkForPermissions() {
        permissionsAllowed =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
}
