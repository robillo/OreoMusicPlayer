package com.robillo.oreomusicplayer.views.activities.theme_change;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.robillo.oreomusicplayer.R;

public class ThemeChangeActivity extends AppCompatActivity implements ThemeChangeMvpView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_change);

        setup();
    }

    @Override
    public void setup() {

    }
}
