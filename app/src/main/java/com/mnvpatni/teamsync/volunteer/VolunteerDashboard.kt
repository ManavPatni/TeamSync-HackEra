package com.mnvpatni.teamsync.volunteer

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.adapter.TeamDetailAdapter
import com.mnvpatni.teamsync.adapter.TeamRestRoomAdapter
import com.mnvpatni.teamsync.databinding.ActivityVolunteerDashboardBinding
import com.mnvpatni.teamsync.models.Team
import com.mnvpatni.teamsync.network.RetrofitInstance
import com.mnvpatni.teamsync.scanner.FoodScannerActivity
import com.mnvpatni.teamsync.scanner.RestRoomScannerActivity
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref
import kotlinx.coroutines.launch

class VolunteerDashboard : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityVolunteerDashboardBinding
    private lateinit var participantAdapter: TeamDetailAdapter

    // Hamburger Menu
    private lateinit var toggle: ActionBarDrawerToggle

    //auth
    private lateinit var auth: FirebaseAuth
    private lateinit var authSharedPref: AuthSharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shimmerLayout.visibility = View.VISIBLE
        binding.rvTeams.visibility = View.INVISIBLE
        binding.shimmerLayout.startShimmer()

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            askNotificationPermission()
        }

        //Setting up profile
        binding.tvUsername.text = currentUser!!.displayName
        binding.tvPost.text = authSharedPref.userPost()
        tvUsername.text = auth.currentUser!!.displayName
        tvPost.text = authSharedPref.userPost()
        Glide.with(this)
            .load(currentUser.photoUrl)
            .transform(CircleCrop())
            .into(binding.ivProfilePic)

        binding.tvSortBy.setOnClickListener {
            showSortByDialog()
        }

        participantAdapter = TeamDetailAdapter(context = this){ itemCount ->
            // Show or hide the "Nothing Found" message based on item count
            if (itemCount == 0) {
                binding.animNothing.visibility = View.VISIBLE
                binding.tvNothing.visibility = View.VISIBLE
                binding.rvTeams.visibility = View.GONE
            } else {
                binding.animNothing.visibility = View.GONE
                binding.tvNothing.visibility = View.GONE
                binding.rvTeams.visibility = View.VISIBLE
            }
            binding.tvTeams.text = "Teams - ${participantAdapter.itemCount}"
        }
        binding.rvTeams.adapter = participantAdapter
        binding.rvTeams.layoutManager = LinearLayoutManager(this)

        getTeams()

        //Hamburger menu item onclick listener
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home ->  startActivity(Intent(this,VolunteerDashboard::class.java))
                R.id.nav_mealScanner ->  startActivity(Intent(this,FoodScannerActivity::class.java))
                R.id.nav_restRoomScanner ->  startActivity(Intent(this,RestRoomScannerActivity::class.java))
                R.id.nav_permittedZone -> {
                    Snackbar.make(binding.main,"This feature is currently not available..",Snackbar.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(navView)
                }
            }
            true
        }

        // Attach TextWatcher to the search EditText
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                participantAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun getTeams() {
        lifecycleScope.launch {
            try {
                // Call the API to get the teams
                val response = RetrofitInstance.api.getTeams()

                if (response.isSuccessful) {

                    binding.shimmerLayout.visibility = View.GONE
                    binding.rvTeams.visibility = View.VISIBLE
                    binding.shimmerLayout.stopShimmer()

                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.statusCode == 200) {
                        val teams = apiResponse.body
                        if (teams.isNotEmpty()) {
                            participantAdapter.updateData(teams)
                            binding.rvTeams.adapter = participantAdapter
                            binding.tvTeams.text = "Teams - ${participantAdapter.itemCount}"
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
                Toast.makeText(this@VolunteerDashboard, "An unexpected error occurred: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSortByDialog() {
        val options = arrayOf("All", "Present", "Received Kit")
        MaterialAlertDialogBuilder(this)
            .setTitle("Sort by")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> participantAdapter.filterByCriteria(TeamDetailAdapter.FilterCriteria.ALL)
                    1 -> participantAdapter.filterByCriteria(TeamDetailAdapter.FilterCriteria.PRESENT)
                    2 -> participantAdapter.filterByCriteria(TeamDetailAdapter.FilterCriteria.RECEIVED_KIT)
                }
            }
            .show()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    return
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show a dialog or some rationale to explain why the permission is needed
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Notification Permission Required")
                        .setMessage("We need permission to send you notifications.")
                        .setPositiveButton("OK") { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "You are now eligible to receive direct updates.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

}