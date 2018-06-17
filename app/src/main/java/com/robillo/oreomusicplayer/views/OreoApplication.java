package com.robillo.oreomusicplayer.views;

import android.app.Application;

import com.robillo.oreomusicplayer.R;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class OreoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/spr.otf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }
}
