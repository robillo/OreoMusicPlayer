package com.robillo.dancingplayer.views.activities.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.views.activities.home.HomeActivity

import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_launcher.*

class LauncherActivity : AppCompatActivity(), LauncherMvpView {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        setup()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun setup() {
        setAppropriateStatusColor()
        setClickListeners()
    }

    private fun setClickListeners() {
        allow_access.setOnClickListener { v -> askForDevicePermissions() }
    }

    override fun askForDevicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (arePermissionsGiven())
                requestPermissions()
            else
                startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    @SuppressLint("NewApi")
    private fun arePermissionsGiven(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("NewApi")
    private fun requestPermissions() {
        requestPermissions(
                arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE
                ),
                PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                takeActionForPermissions(checkResultsForPermissions(grantResults))
            }
        }
    }

    private fun checkResultsForPermissions(grantResults: IntArray): Boolean {
        return grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
    }

    private fun takeActionForPermissions(arePermissionsGranted: Boolean) {
        if(arePermissionsGranted) {
            startActivity(Intent(this, HomeActivity::class.java))
            Toast.makeText(this, getString(R.string.moving_on_string), Toast.LENGTH_SHORT).show()
        }
        else {
            if (optional_permissions_set.visibility == View.INVISIBLE) optional_permissions_set.visibility = View.VISIBLE
            Toast.makeText(this, getString(R.string.permission_denied_string), Toast.LENGTH_SHORT).show()
        }
    }

    override fun setAppropriateStatusColor() {
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
    }
}
