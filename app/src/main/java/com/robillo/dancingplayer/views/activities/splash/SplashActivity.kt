package com.robillo.dancingplayer.views.activities.splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.preferences.AppPreferencesHelper
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.views.activities.launcher.LauncherActivity
import com.robillo.dancingplayer.views.activities.main.MainActivity

import butterknife.ButterKnife
import com.robillo.dancingplayer.models.ThemeColors
import com.robillo.dancingplayer.views.activities.base.BaseActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    lateinit var colors: ThemeColors
    lateinit var preferencesHelper: AppPreferencesHelper

    override fun getLayoutResId(): Int {
        return R.layout.activity_splash
    }

    override fun init() {

        preferencesHelper = AppPreferencesHelper(this)

        getUserTheme()
        setViewsForTheme()
        setWindowFlagsForTheme()
        initPlaylistForNewUser()

        Handler().postDelayed({
            if (arePermissionsGiven()) startActivity(Intent(this, MainActivity::class.java))
            else startActivity(Intent(this, LauncherActivity::class.java))
        }, 1000)
    }

    private fun getUserTheme() {
        var themeName = AppPreferencesHelper(this).userThemeName
        if (themeName == AppConstants.PITCH_BLACK) themeName = AppConstants.BLUE_GREY

        colors = AppConstants.themeMap[themeName]!!
    }

    private fun setViewsForTheme() {
        gradient_image_view.background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                        ContextCompat.getColor(this, colors.colorPrimary),
                        ContextCompat.getColor(this, colors.colorPrimaryDark),
                        ContextCompat.getColor(this, colors.colorPrimaryDark)
                )
        )
    }

    private fun setWindowFlagsForTheme() {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, colors.colorPrimary)
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