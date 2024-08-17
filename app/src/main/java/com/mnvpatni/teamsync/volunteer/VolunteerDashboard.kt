package com.mnvpatni.teamsync.volunteer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.databinding.ActivityVolunteerDashboardBinding

class VolunteerDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}