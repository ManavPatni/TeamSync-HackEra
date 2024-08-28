package com.mnvpatni.teamsync.admin

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        binding.shimmerLayout.visibility = View.VISIBLE
        binding.rvTeams.visibility = View.INVISIBLE
        binding.shimmerLayout.startShimmer()

        participantAdapter = TeamDetailAdapter(context = requireContext()){ itemCount ->
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
        }
        binding.rvTeams.adapter = participantAdapter
        binding.rvTeams.layoutManager = LinearLayoutManager(context)

        binding.tvSortBy.setOnClickListener {
            showSortByDialog()
        }

        getTeams()

        // Attach TextWatcher to the search EditText
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                participantAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
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

    private fun showSortByDialog() {
        val options = arrayOf("All", "Present", "Received Kit")
        MaterialAlertDialogBuilder(requireContext())
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

}