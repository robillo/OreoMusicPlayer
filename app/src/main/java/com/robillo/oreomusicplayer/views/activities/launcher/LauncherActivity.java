package com.robillo.oreomusicplayer.views.activities.launcher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LauncherActivity extends AppCompatActivity implements LauncherMvpView {

    @SuppressWarnings("FieldCanBeLocal")
    private final int PERMISSION_REQUEST_CODE = 0;

    @BindView(R.id.optional_permissions_set)
    TextView optionalPermissions;

    @BindView(R.id.app_name)
    TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ButterKnife.bind(this);

        setup();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void setup() {
        setAppropriateStatusColor();
    }

    @OnClick(R.id.allow_access)
    public void askForAccess() {
        askForDevicePermissions();
    }

    @Override
    public void askForDevicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE
                        },
                        PERMISSION_REQUEST_CODE
                );
            }
            else {
                startActivity(new Intent(this, MainActivity.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Thank you! Moving on..", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, MainActivity.class));

                } else {
                    if(optionalPermissions.getVisibility() == View.INVISIBLE) optionalPermissions.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Permission denied. Please allow to continue..", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void setAppropriateStatusColor() {
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
    }
}
