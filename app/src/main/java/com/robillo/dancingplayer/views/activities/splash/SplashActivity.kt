package com.robillo.dancingplayer.views.activities.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.di.component.activity.DaggerSplashActivityComponent
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.views.activities.launcher.LauncherActivity

import com.robillo.dancingplayer.models.ThemeColors
import com.robillo.dancingplayer.views.DancingPlayerApplication
import com.robillo.dancingplayer.views.activities.base.BaseActivity
import com.robillo.dancingplayer.views.activities.home.HomeActivity
import kotlinx.android.synthetic.main.activity_splash.*
import javax.inject.Inject

class SplashActivity : BaseActivity() {

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    lateinit var colors: ThemeColors

    private val delayUntilLaunch = 1000L

    override fun getLayoutResId(): Int {
        return R.layout.activity_splash
    }

    override fun init() {

        setComponent()
        getUserTheme()
        setViewsForTheme()
        setWindowFlagsForColor(colors.colorPrimary)
        initPlaylistForNewUser()

        Handler().postDelayed({
            if (arePermissionsGiven()) startActivity(Intent(this, HomeActivity::class.java))
            else startActivity(Intent(this, LauncherActivity::class.java))
        }, delayUntilLaunch)
    }

    private fun setComponent() {
        DaggerSplashActivityComponent.builder()
                .dancingPlayerComponent(DancingPlayerApplication.get(this).dancingPlayerComponent()).build()
                .injectSplashActivity(this)
    }

    private fun getUserTheme() {
        var themeName = preferencesHelper.userThemeName
        if (preferencesHelper.userThemeName == AppConstants.PITCH_BLACK) themeName = AppConstants.BLUE_GREY

        colors = AppConstants.themeMap[themeName]!!
    }

    private fun setViewsForTheme() {
        gradient_image_view.background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                        getIntColor(colors.colorPrimary),
                        getIntColor(colors.colorPrimaryDark),
                        getIntColor(colors.colorPrimaryDark)
                )
        )
    }

    private fun arePermissionsGiven(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun initPlaylistForNewUser() {
        var playlists: MutableSet<String>? = preferencesHelper.playlistSet
        if(playlists == null) {
            playlists = HashSet()
            playlists.add(AppConstants.DEFAULT_PLAYLIST_TITLE)
            playlists.add(AppConstants.MOST_PLAYED)
            playlists.add(AppConstants.RECENTLY_ADDED)
            playlists.add(AppConstants.RECENTLY_PLAYED)
            preferencesHelper.playlistSet = playlists
        }
    }
}