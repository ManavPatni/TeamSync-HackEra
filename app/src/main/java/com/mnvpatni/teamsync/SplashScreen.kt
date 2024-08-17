package com.mnvpatni.teamsync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mnvpatni.teamsync.admin.AdminDashboard
import com.mnvpatni.teamsync.auth.SignInActivity
import com.mnvpatni.teamsync.databinding.ActivitySplashScreenBinding
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref
import com.mnvpatni.teamsync.volunteer.VolunteerDashboard
import java.util.Timer
import kotlin.concurrent.timerTask

class SplashScreen : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivitySplashScreenBinding

    //auth pref
    private lateinit var authSharedPref: AuthSharedPref

    //time out
    private val splashTimeOut = 1800L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authSharedPref = AuthSharedPref(this)

        when {
            !authSharedPref.isSignedIn() -> {
                exit(SignInActivity::class.java)
            }
            else -> {
                if (authSharedPref.userType() == "Admin") {
                    exit(AdminDashboard::class.java)
                } else {
                    exit(VolunteerDashboard::class.java)
                }
            }
        }
    }

    private fun exit(activityClass: Class<out Activity>) {
        Timer().schedule(timerTask {
            startActivity(Intent(this@SplashScreen, activityClass))
            finishAffinity()
        }, splashTimeOut)
    }
}