package com.mnvpatni.teamsync

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mnvpatni.teamsync.admin.AdminDashboard
import com.mnvpatni.teamsync.auth.SignInActivity
import com.mnvpatni.teamsync.databinding.ActivitySplashScreenBinding
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref
import com.mnvpatni.teamsync.volunteer.VolunteerDashboard
import java.util.Timer
import kotlin.concurrent.timerTask

class SplashScreen : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivitySplashScreenBinding

    // Auth Shared Preferences
    private lateinit var authSharedPref: AuthSharedPref

    // Splash timeout
    private val splashTimeOut = 1800L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authSharedPref = AuthSharedPref(this)

        if (!authSharedPref.isSignedIn()) {
            Timer().schedule(timerTask {
                runOnUiThread { exitWithAnim() }
            }, splashTimeOut)
        } else {
            Timer().schedule(timerTask {
                val activityClass = if (authSharedPref.userType() == "Admin") {
                    AdminDashboard::class.java
                } else {
                    VolunteerDashboard::class.java
                }
                exit(activityClass)
            }, splashTimeOut)
        }
    }

    // Navigate to the next activity without animation
    private fun exit(activityClass: Class<out Activity>) {
        startActivity(Intent(this@SplashScreen, activityClass))
        finishAffinity()
    }

    // Navigate to the SignInActivity with animation
    private fun exitWithAnim() {
        val intent = Intent(this, SignInActivity::class.java)
        val bundle = ActivityOptions.makeSceneTransitionAnimation(
            this,
            android.util.Pair(binding.ivLogo as View, "app_logo"),
            android.util.Pair(binding.tvAppName as View, "app_name")
        ).toBundle()
        startActivity(intent, bundle)
        finishAffinity()
    }
}
