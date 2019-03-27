package com.robillo.dancingplayer.views.activities.theme_change

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.ThemeColors
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.views.activities.theme_change.adapters.ThemeChoicesAdapter

import java.util.ArrayList

import butterknife.ButterKnife
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_theme_change.*

class ThemeChangeActivity : AppCompatActivity() {

    private val themeColorsList = ArrayList<ThemeColors>()
    private lateinit var helper: PreferencesHelper

    enum class PlayListType {
        MOST_PLAYED,
        RECENTLY_PLAYED,
        RECENTLY_ADDED
    }

    companion object {
        private const val TEN = 10
        private const val FIFTY = 50
        private const val HUNDRED = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_change)

        setup()
    }

    private fun setup() {
        helper = PreferencesHelper(this)

        val window = window
        val view = window.decorView
        var flags = view.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        view.systemUiVisibility = flags
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        val currentUserThemeColors = AppConstants.themeMap[helper.userThemeName]

        inflateThemeColors()

        setInitialStatePlaylistSongsCount()

        val choicesAdapter = ThemeChoicesAdapter(this, themeColorsList, currentUserThemeColors!!)
        recycler_view!!.adapter = choicesAdapter

        setClickListeners()
    }

    private fun setClickListeners() {
        go_back_to_main!!.setOnClickListener { setGoBackToMain() }
        rescan_device!!.setOnClickListener { setRescanDevice() }
        ten_mp!!.setOnClickListener { updateForCount(TEN, PlayListType.MOST_PLAYED.name) }
        ten_rp!!.setOnClickListener { updateForCount(TEN, PlayListType.RECENTLY_PLAYED.name) }
        ten_ra!!.setOnClickListener { updateForCount(TEN, PlayListType.RECENTLY_ADDED.name) }
        fifty_mp!!.setOnClickListener { updateForCount(FIFTY, PlayListType.MOST_PLAYED.name) }
        fifty_rp!!.setOnClickListener { updateForCount(FIFTY, PlayListType.RECENTLY_PLAYED.name) }
        fifty_ra!!.setOnClickListener { updateForCount(FIFTY, PlayListType.RECENTLY_ADDED.name) }
        hundred_mp!!.setOnClickListener { updateForCount(HUNDRED, PlayListType.MOST_PLAYED.name) }
        hundred_rp!!.setOnClickListener { updateForCount(HUNDRED, PlayListType.RECENTLY_PLAYED.name) }
        hundred_ra!!.setOnClickListener { updateForCount(HUNDRED, PlayListType.RECENTLY_ADDED.name) }
    }

    private fun inflateThemeColors() {
        themeColorsList.add(AppConstants.themeMap[AppConstants.PITCH_BLACK]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.BLUE_GREY]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.DEEP_BROWN]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.DEEP_BLUE]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.DEEP_GREEN]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.DEEP_ORANGE]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.AMBER]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.CYAN]!!)
        themeColorsList.add(AppConstants.themeMap[AppConstants.LIME]!!)
    }

    fun showSnackBarThemeSet(themeName: String) {
        Snackbar.make(findViewById(R.id.coordinator_layout),
                getString(R.string.theme_set_successful) + " " + themeName,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    private fun setInitialStatePlaylistSongsCount() {

        val mpCount = helper.mostPlayedCount
        val rpCount = helper.recentlyPlayedCount
        val raCount = helper.recentlyAddedCount

        setColorsToViews(ten_mp, fifty_mp, hundred_mp, mpCount)
        setColorsToViews(ten_ra, fifty_ra, hundred_ra, raCount)
        setColorsToViews(ten_rp, fifty_rp, hundred_rp, rpCount)
    }

    private fun setColorsToViews(tenView: TextView, fiftyView: TextView, hundredView: TextView, count: Int) {
        when (count) {
            TEN -> {
                enableTextView(tenView)
                disableTextView(fiftyView)
                disableTextView(hundredView)
            }
            FIFTY -> {
                disableTextView(tenView)
                enableTextView(fiftyView)
                disableTextView(hundredView)
            }
            HUNDRED -> {
                disableTextView(tenView)
                disableTextView(fiftyView)
                enableTextView(hundredView)
            }
        }
    }

    private fun disableTextView(textView: TextView) {
        textView.setTextColor(fetchColor(R.color.colorTextOne))
        textView.setBackgroundColor(fetchColor(R.color.white))
    }

    private fun enableTextView(textView: TextView) {
        textView.setTextColor(fetchColor(R.color.white))
        textView.setBackgroundColor(fetchColor(R.color.green_primary_dark))
    }

    private fun fetchColor(color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun setGoBackToMain() {
        setResult(Activity.RESULT_CANCELED, Intent())
        onBackPressed()
    }

    private fun setRescanDevice() {
        setResult(Activity.RESULT_OK, Intent())
        onBackPressed()
    }

    private fun updateForCount(count: Int, playListType: String) {
        when(playListType) {
            PlayListType.MOST_PLAYED.name -> {
                helper.mostPlayedCount = count
                setColorsToViews(ten_mp, fifty_mp, hundred_mp, count)
            }
            PlayListType.RECENTLY_PLAYED.name -> {
                helper.recentlyPlayedCount = count
                setColorsToViews(ten_rp, fifty_rp, hundred_rp, count)
            }
            PlayListType.RECENTLY_ADDED.name -> {
                helper.recentlyAddedCount = count
                setColorsToViews(ten_ra, fifty_ra, hundred_ra, count)
            }
        }
    }
}
