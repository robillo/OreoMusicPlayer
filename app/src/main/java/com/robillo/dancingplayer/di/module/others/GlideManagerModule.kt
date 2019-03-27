package com.robillo.dancingplayer.di.module.others

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.robillo.dancingplayer.di.scope.DancingPlayerScope
import dagger.Module
import dagger.Provides

@Module(includes = [ContextModule::class])
class GlideManagerModule {

    @Provides
    @DancingPlayerScope
    fun glideManager(context: Context): RequestManager {
        return Glide.with(context)
    }
}