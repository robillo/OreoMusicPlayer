package com.robillo.dancingplayer.views.activities.base

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import butterknife.ButterKnife
import io.github.inflationx.viewpump.ViewPumpContextWrapper

abstract class BaseActivity: AppCompatActivity() {

    abstract fun init()

    abstract fun getLayoutResId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        ButterKnife.bind(this)

        init()
    }

    protected fun setWindowFlagsForColor(color: Int) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getIntColor(color)
    }

    protected fun getIntColor(color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}