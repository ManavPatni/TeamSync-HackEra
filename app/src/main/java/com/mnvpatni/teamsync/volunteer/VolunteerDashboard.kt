package com.mnvpatni.teamsync.volunteer

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.databinding.ActivityVolunteerDashboardBinding
import com.mnvpatni.teamsync.scanner.FoodScannerActivity
import com.mnvpatni.teamsync.scanner.RestRoomScannerActivity
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref

class VolunteerDashboard : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityVolunteerDashboardBinding

    //auth
    private lateinit var auth: FirebaseAuth
    private lateinit var authSharedPref: AuthSharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //auth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        authSharedPref = AuthSharedPref(this)

        //Setting up profile
        binding.tvUsername.text = currentUser!!.displayName
        binding.tvPost.text = authSharedPref.userPost()
        Glide.with(this)
            .load(currentUser.photoUrl)
            .transform(CircleCrop())
            .into(binding.ivProfilePic)

        binding.ivProfilePic.setOnClickListener{
            startActivity(Intent(this,FoodScannerActivity::class.java))
        }

        binding.tvUsername.setOnClickListener {
            startActivity(Intent(this,RestRoomScannerActivity::class.java))  }
    }
}