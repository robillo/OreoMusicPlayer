package com.robillo.oreomusicplayer.views.activities.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.views.activities.main.song_list_frag.SongsListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainActivityMvpView {

    @SuppressWarnings("FieldCanBeLocal")
    private final int PERMISSION_REQUEST_CODE = 0;

    @BindView(R.id.fragment_container)
    FrameLayout mFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setUp();
    }

    @Override
    public void setUp() {
        askForDevicePermissions();
    }

    @Override
    public void setSongListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(mFragmentContainer.getId(), new SongsListFragment());
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void setSongPlayFragment() {

    }

    @Override
    public void askForDevicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
            else {
                setSongListFragment();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE :
                if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setSongListFragment();
                }
                else {
                    askForDevicePermissions();
                }
                break;
        }
    }
}
