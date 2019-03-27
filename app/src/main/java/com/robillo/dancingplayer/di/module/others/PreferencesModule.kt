package com.robillo.dancingplayer.di.module.others

import android.content.Context
import com.robillo.dancingplayer.di.scope.DancingPlayerScope
import com.robillo.dancingplayer.preferences.PreferencesHelper
import dagger.Module
import dagger.Provides

@Module(includes = [ContextModule::class])
class PreferencesModule {

    @Provides
    @DancingPlayerScope
    fun preferencesHelper(context: Context): PreferencesHelper {
        return PreferencesHelper(context)
    }
}