package com.robillo.dancingplayer.di.component.activity

import com.robillo.dancingplayer.di.component.application.DancingPlayerComponent
import com.robillo.dancingplayer.di.scope.PerFragmentScope
import com.robillo.dancingplayer.views.activities.splash.SplashActivity
import dagger.Component

@PerFragmentScope
@Component(dependencies = [DancingPlayerComponent::class])
interface SplashActivityComponent {

    fun injectSplashActivity(splashActivity: SplashActivity)
}