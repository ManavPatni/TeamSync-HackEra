package com.mnvpatni.teamsync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnvpatni.teamsync.adapter.TeamMemberAdapter
import com.mnvpatni.teamsync.databinding.ActivityTeamDetailsBinding
import com.mnvpatni.teamsync.models.Member

class TeamDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeamDetailsBinding
    private lateinit var teamMemberAdapter: TeamMemberAdapter
    private lateinit var leaderEmail: String
    private lateinit var leaderPhone: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Get data from intent
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
        binding.tvTeamName.text = teamName
        binding.tvCollegeName.text = college
        binding.tvCityState.text = "$city, $state"

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

}