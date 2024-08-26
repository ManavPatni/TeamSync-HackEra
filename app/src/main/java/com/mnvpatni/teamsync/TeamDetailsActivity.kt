package com.mnvpatni.teamsync

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnvpatni.teamsync.adapter.TeamMemberAdapter
import com.mnvpatni.teamsync.databinding.ActivityTeamDetailsBinding
import com.mnvpatni.teamsync.models.Member
import com.mnvpatni.teamsync.network.RetrofitInstance
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref
import kotlinx.coroutines.launch

class TeamDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamDetailsBinding
    private lateinit var teamMemberAdapter: TeamMemberAdapter
    private lateinit var leaderEmail: String
    private lateinit var leaderPhone: String

    private lateinit var pd: ProgressDialog
    private lateinit var authSharedPref: AuthSharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authSharedPref = AuthSharedPref(this)

        pd = ProgressDialog(this)
        pd.setMessage("Please wait...")

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Get data from intent
        val teamUid = intent.getStringExtra("uid") ?: "N/A"
        val teamName = intent.getStringExtra("team_name") ?: "N/A"
        val college = intent.getStringExtra("college") ?: "N/A"
        val city = intent.getStringExtra("city") ?: "N/A"
        val state = intent.getStringExtra("state") ?: "N/A"
        val members: ArrayList<Member> = intent.getParcelableArrayListExtra("members") ?: arrayListOf()

        // Extract leader contact info from members
        val leader = members.find { it.role == "leader" }
        leaderEmail = leader?.email_id ?: ""
        leaderPhone = leader?.phone_number ?: ""

        // Populate the UI
        binding.btnBack.text = teamUid
        binding.tvTeamName.text = teamName
        binding.tvCollegeName.text = college
        binding.tvCityState.text = "$city, $state"

        if (authSharedPref.accessTo() == "attendance_marker") {
            binding.btnPresent.visibility = View.VISIBLE
            binding.btnKit.visibility = View.GONE
        } else if (authSharedPref.accessTo() == "kit_distributor") {
            binding.btnPresent.visibility = View.GONE
            binding.btnKit.visibility = View.VISIBLE
        } else {
            binding.btnPresent.visibility = View.GONE
            binding.btnKit.visibility = View.GONE
        }

        // Setup RecyclerView
        teamMemberAdapter = TeamMemberAdapter(members)
        binding.rvTeam.apply {
            layoutManager = LinearLayoutManager(this@TeamDetailsActivity)
            adapter = teamMemberAdapter
        }

        // Setup Call Button
        binding.btnCall.setOnClickListener {
            makeCall(leaderPhone)
        }

        // Setup Mail Button
        binding.btnEmail.setOnClickListener {
            sendMail(leaderEmail)
        }

        binding.btnPresent.setOnClickListener {
            markAttendance(teamUid,"Attendance")
        }
        binding.btnKit.setOnClickListener {
            markAttendance(teamUid,"Kit")
        }

    }

    private fun makeCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
    }

    private fun sendMail(email: String) {
        if (email.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Subject")
                putExtra(Intent.EXTRA_TEXT, "Body")
            }
            startActivity(intent)
        }
    }

    private fun markAttendance(teamId:String, callFrom: String) {
        pd.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.setAttendance(
                    teamId = teamId,
                    callFrom = callFrom
                )

                if (response.statusCode == "200") {
                    Toast.makeText(this@TeamDetailsActivity,response.message,Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TeamDetailsActivity,response.error,Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("Api error", e.message.toString())
                Toast.makeText(this@TeamDetailsActivity, "An unexpected error occurred!!", Toast.LENGTH_SHORT).show()
            } finally {
                pd.dismiss()
            }
        }

    }

}