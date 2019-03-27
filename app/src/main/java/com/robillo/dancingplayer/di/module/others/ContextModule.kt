package com.robillo.dancingplayer.di.module.others

import android.content.Context
import com.robillo.dancingplayer.di.scope.DancingPlayerScope
import dagger.Module
import dagger.Provides

@Module
class ContextModule(val context: Context) {

    @Provides
    @DancingPlayerScope
    fun context(): Context {
        return context
    }
}