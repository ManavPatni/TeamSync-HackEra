package com.mnvpatni.teamsync.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.databinding.ActivityAdminDashboardBinding
import com.mnvpatni.teamsync.scanner.FoodScannerActivity
import com.mnvpatni.teamsync.scanner.RestRoomScannerActivity
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref
import com.mnvpatni.teamsync.volunteer.VolunteerDashboard

class AdminDashboard : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityAdminDashboardBinding

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

        replaceFragment(AdminHomeFragment())

        //Hamburger menu item onclick listener
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(AdminHomeFragment())
                    drawerLayout.closeDrawer(navView)
                }
                R.id.nav_permittedZone -> {
                    Snackbar.make(binding.main,"This feature is currently not available..",Snackbar.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(navView)
                }
                R.id.nav_manageCommittee -> {
                    replaceFragment(CommitteeMembersFragment())
                    drawerLayout.closeDrawer(navView)
                }
                R.id.nav_mealScanner -> startActivity(
                    Intent(
                        this,
                        FoodScannerActivity::class.java
                    )
                )
                R.id.nav_manageTeam -> {
                    Snackbar.make(binding.main,"This feature is currently not available..",Snackbar.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(navView)
                }
                R.id.nav_restRoomScanner -> startActivity(
                    Intent(
                        this,
                        RestRoomScannerActivity::class.java
                    )
                )
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_adminMain, fragment)
        fragmentTransaction.commit()
    }

}