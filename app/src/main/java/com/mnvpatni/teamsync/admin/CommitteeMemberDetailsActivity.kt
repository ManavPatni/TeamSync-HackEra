package com.mnvpatni.teamsync.admin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.databinding.ActivityCommitteeMemberDetailsBinding
import com.mnvpatni.teamsync.network.RetrofitInstance
import kotlinx.coroutines.launch

class CommitteeMemberDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommitteeMemberDetailsBinding

    companion object {
        private const val TAG = "CommitteeMemberDetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommitteeMemberDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val uid = intent.getStringExtra("uid")
        val name = intent.getStringExtra("name")
        val type = intent.getStringExtra("type")
        val post = intent.getStringExtra("post")
        val accessTo = intent.getStringExtra("access_to")
        val status = intent.getIntExtra("status", 0)

        // Populate the UI
        binding.tvUid.text = uid ?: "N/A"
        binding.etDisplayName.setText(name ?: "")
        binding.etPost.setText(post ?: "")
        binding.etAccessTo.setText(accessTo ?: "")

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

        // Handle Update Button Click
        binding.btnUpdate.setOnClickListener {
            update()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    private fun update() {
        lifecycleScope.launch {
            try {
                val uid = binding.tvUid.text.toString()
                val userType = binding.userType.selectedItem.toString() // Get the selected item text
                val post = binding.etPost.text.toString()
                val accessTo = binding.etAccessTo.text.toString()
                val status = when (binding.spinnerStatus.selectedItem.toString()) {
                    "Approved" -> 1
                    "Rejected" -> 0
                    else -> 0
                }.toString()

                // Call the API to update committee members
                val response = RetrofitInstance.api.updateCommitteeMembers(
                    uid = uid,
                    user_type = userType,
                    post = post,
                    access_to = accessTo,
                    status = status
                )

                if (response.statusCode == 200) {
                    Snackbar.make(binding.root, response.message.toString(), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Request failed: ${response.error}", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data: ${e.localizedMessage}", e)
                Toast.makeText(
                    this@CommitteeMemberDetailsActivity,
                    "An unexpected error occurred: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
