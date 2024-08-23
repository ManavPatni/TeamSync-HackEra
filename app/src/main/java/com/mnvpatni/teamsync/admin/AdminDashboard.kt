package com.mnvpatni.teamsync.admin

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.adapter.TeamDetailAdapter
import com.mnvpatni.teamsync.databinding.ActivityAdminDashboardBinding
import com.mnvpatni.teamsync.databinding.ActivityVolunteerDashboardBinding
import com.mnvpatni.teamsync.network.RetrofitInstance
import com.mnvpatni.teamsync.scanner.FoodScannerActivity
import com.mnvpatni.teamsync.scanner.RestRoomScannerActivity
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref
import com.mnvpatni.teamsync.volunteer.VolunteerDashboard
import kotlinx.coroutines.launch

class AdminDashboard : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var participantAdapter: TeamDetailAdapter

    // Hamburger Menu
    private lateinit var toggle: ActionBarDrawerToggle

    //auth
    private lateinit var auth: FirebaseAuth
    private lateinit var authSharedPref: AuthSharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //auth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        authSharedPref = AuthSharedPref(this)

        // Hamburger Side menu
        val drawerLayout = binding.main
        val navView: NavigationView = binding.navView
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.cvMenu.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }
        val tvUsername = navView.getHeaderView(0).findViewById<TextView>(R.id.tv_username)
        val tvPost = navView.getHeaderView(0).findViewById<TextView>(R.id.tv_post)

        //Setting up profile
        binding.tvUsername.text = currentUser!!.displayName
        binding.tvPost.text = authSharedPref.userPost()
        tvUsername.text = auth.currentUser!!.displayName
        tvPost.text = authSharedPref.userPost()
        Glide.with(this)
            .load(currentUser.photoUrl)
            .transform(CircleCrop())
            .into(binding.ivProfilePic)


        participantAdapter = TeamDetailAdapter(context = this)
        binding.rvTeams.adapter = participantAdapter
        binding.rvTeams.layoutManager = LinearLayoutManager(this)

        getTeams()

        //Hamburger menu item onclick listener
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home ->  startActivity(Intent(this, VolunteerDashboard::class.java))
                R.id.nav_mealScanner ->  startActivity(
                    Intent(this,
                        FoodScannerActivity::class.java)
                )
                R.id.nav_restRoomScanner ->  startActivity(
                    Intent(this,
                        RestRoomScannerActivity::class.java)
                )
            }
            true
        }
    }

    private fun getTeams() {
        lifecycleScope.launch {
            try {
                // Call the API to get the teams
                val response = RetrofitInstance.api.getTeams()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.statusCode == 200) {
                        val teams = apiResponse.body
                        if (teams.isNotEmpty()) {
                            participantAdapter.updateData(teams)
                            binding.rvTeams.adapter = participantAdapter
                        } else {
                            Snackbar.make(binding.root, "No teams found.", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(binding.root, "Failed to fetch data: ${response.code()}", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(binding.root, "Request failed: ${response.code()}", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data: ${e.localizedMessage}", e)
                Toast.makeText(this@AdminDashboard, "An unexpected error occurred: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}