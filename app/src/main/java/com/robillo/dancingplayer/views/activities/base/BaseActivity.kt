package com.robillo.dancingplayer.views.activities.base

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.ButterKnife
import com.robillo.dancingplayer.R
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

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}