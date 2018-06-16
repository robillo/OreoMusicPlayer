package com.robillo.oreomusicplayer.views.activities.splash;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.views.activities.launcher.LauncherActivity;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SplashActivity extends AppCompatActivity implements SplashMvpView {

    boolean permissionsAllowed;

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(permissionsAllowed) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                else {
                    startActivity(new Intent(SplashActivity.this, LauncherActivity.class));
                }
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
