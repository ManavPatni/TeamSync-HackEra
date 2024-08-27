package com.mnvpatni.teamsync.admin

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.databinding.ActivityCommitteeMemberDetailsBinding
import com.mnvpatni.teamsync.network.RetrofitInstance
import kotlinx.coroutines.launch

class CommitteeMemberDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommitteeMemberDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommitteeMemberDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get data from intent
        val uid = intent.getStringExtra("uid") ?: "N/A"
        val name = intent.getStringExtra("name") ?: "N/A"
        val type = intent.getStringExtra("type") ?: "N/A"
        val post = intent.getStringExtra("post") ?: "N/A"
        val access_to = intent.getStringExtra("access_to") ?: "N/A"
        val status = intent.getIntExtra("status", 0)

        // Populate the UI
        binding.tvUid.text = uid
        binding.etDisplayName.setText(name)
        binding.etPost.setText(post)
        binding.etAccessTo.setText(access_to)

        // Set Spinner selection based on intent data
        binding.userType.setSelection(
            when (type) {
                "Admin" -> 0
                "Volunteer" -> 1
                else -> -1 // Set to -1 if the type doesn't match
            }
        )

        binding.spinnerStatus.setSelection(
            when (status) {
                0 -> 0 // Assuming "Active" is at index 0
                1 -> 1 // Assuming "Inactive" is at index 1
                else -> -1 // Set to -1 if the status doesn't match
            }
        )
    }

    private fun update() {
        lifecycleScope.launch {
            try {
                // Call the API to get the members
                val response = RetrofitInstance.api.updateCommitteeMembers()

                if (response.statusCode == 200) {
                    val apiResponse = response.message

                    Snackbar.make(binding.root, apiResponse.toString(), Snackbar.LENGTH_SHORT)
                        .show()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Request failed: ${response.error}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data: ${e.localizedMessage}", e)
                Toast.makeText(
                    context,
                    "An unexpected error occurred: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}