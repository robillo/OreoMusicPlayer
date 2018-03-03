package com.robillo.oreomusicplayer.views;

import android.app.Application;

import com.robillo.oreomusicplayer.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by robinkamboj on 04/03/18.
 */

public class OreoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Raleway-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
