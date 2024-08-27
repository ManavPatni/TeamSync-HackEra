package com.mnvpatni.teamsync.admin

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mnvpatni.teamsync.adapter.TeamDetailAdapter
import com.mnvpatni.teamsync.databinding.FragmentAdminHomeBinding
import com.mnvpatni.teamsync.network.RetrofitInstance
import kotlinx.coroutines.launch


class AdminHomeFragment : Fragment() {

    private lateinit var binding: FragmentAdminHomeBinding
    private lateinit var participantAdapter: TeamDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminHomeBinding.inflate(layoutInflater)

        participantAdapter = TeamDetailAdapter(context = requireContext())
        binding.rvTeams.adapter = participantAdapter
        binding.rvTeams.layoutManager = LinearLayoutManager(context)

        getTeams()

        return binding.root
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
                Toast.makeText(context, "An unexpected error occurred: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}