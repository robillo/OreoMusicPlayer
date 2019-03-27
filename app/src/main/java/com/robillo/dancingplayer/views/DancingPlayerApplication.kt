package com.robillo.dancingplayer.views

import android.app.Activity
import android.app.Application

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.di.component.application.DaggerDancingPlayerComponent
import com.robillo.dancingplayer.di.component.application.DancingPlayerComponent
import com.robillo.dancingplayer.di.module.others.ContextModule

import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class DancingPlayerApplication : Application() {

    private lateinit var component: DancingPlayerComponent

    companion object {
        fun get(activity: Activity): DancingPlayerApplication {
            return activity.application as DancingPlayerApplication
        }
    }

    override fun onCreate() {
        super.onCreate()

        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Comfortaa-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())

        component = DaggerDancingPlayerComponent.builder()
                .contextModule(ContextModule(this))
                .build()
    }

    fun dancingPlayerComponent(): DancingPlayerComponent {
        return component
    }
}
