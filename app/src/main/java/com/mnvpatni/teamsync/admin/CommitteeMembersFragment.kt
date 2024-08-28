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
import com.google.android.material.snackbar.Snackbar
import com.mnvpatni.teamsync.adapter.CommitteeMembersAdapter
import com.mnvpatni.teamsync.databinding.FragmentCommitteeMembersBinding
import com.mnvpatni.teamsync.network.RetrofitInstance
import kotlinx.coroutines.launch

class CommitteeMembersFragment : Fragment() {

    private lateinit var binding: FragmentCommitteeMembersBinding
    private lateinit var committeeMembersAdapter: CommitteeMembersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommitteeMembersBinding.inflate(layoutInflater)

        binding.shimmerLayout.visibility = View.VISIBLE
        binding.rvCommitteeMembers.visibility = View.INVISIBLE
        binding.shimmerLayout.startShimmer()

        committeeMembersAdapter = CommitteeMembersAdapter(requireContext(), emptyList()){ itemCount ->
            // Show or hide the "Nothing Found" message based on item count
            if (itemCount == 0) {
                binding.animNothing.visibility = View.VISIBLE
                binding.tvNothing.visibility = View.VISIBLE
                binding.rvCommitteeMembers.visibility = View.GONE
            } else {
                binding.animNothing.visibility = View.GONE
                binding.tvNothing.visibility = View.GONE
                binding.rvCommitteeMembers.visibility = View.VISIBLE
            }
        }
        binding.rvCommitteeMembers.layoutManager = LinearLayoutManager(context)
        binding.rvCommitteeMembers.adapter = committeeMembersAdapter

        getMembers()

        // Attach TextWatcher to the search EditText
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                committeeMembersAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun getMembers() {
        lifecycleScope.launch {
            try {
                // Call the API to get the members
                val response = RetrofitInstance.api.getCommitteeMembers()

                binding.shimmerLayout.visibility = View.GONE
                binding.rvCommitteeMembers.visibility = View.VISIBLE
                binding.shimmerLayout.stopShimmer()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.statusCode == 200) {
                        val members = apiResponse.body
                        if (members.isNotEmpty()) {
                            committeeMembersAdapter.updateData(members)
                        } else {
                            Snackbar.make(binding.root, "No members found.", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Failed to fetch data: ${response.code()}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        binding.root,
                        "Request failed: ${response.code()}",
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
