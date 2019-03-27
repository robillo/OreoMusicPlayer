package com.robillo.dancingplayer.di.component.application

import com.bumptech.glide.RequestManager
import com.robillo.dancingplayer.di.module.others.ContextModule
import com.robillo.dancingplayer.di.module.others.GlideManagerModule
import com.robillo.dancingplayer.di.module.others.PreferencesModule
import com.robillo.dancingplayer.di.scope.DancingPlayerScope
import com.robillo.dancingplayer.preferences.PreferencesHelper
import dagger.Component

@DancingPlayerScope
@Component(modules = [ContextModule::class, PreferencesModule::class, GlideManagerModule::class])
interface DancingPlayerComponent {

    fun getPreferencesHelper(): PreferencesHelper

    fun getGlideManager(): RequestManager
}