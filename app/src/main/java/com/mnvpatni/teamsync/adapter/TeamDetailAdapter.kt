package com.mnvpatni.teamsync.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.TeamDetailsActivity
import com.mnvpatni.teamsync.models.Team
import java.util.*
import kotlin.collections.ArrayList

class TeamDetailAdapter(
    private var teams: MutableList<Team> = mutableListOf(),
    private val context: Context
) : RecyclerView.Adapter<TeamDetailAdapter.ParticipantViewHolder>() {

    private var filteredTeams: MutableList<Team> = ArrayList(teams)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.team_card, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val team = filteredTeams[position]
        with(holder) {
            teamName.text = team.team_name
            teamSize.text = "${team.members.size} members"
            itemView.setOnClickListener {
                val intent = Intent(context, TeamDetailsActivity::class.java).apply {
                    putExtra("uid", team.team_id)
                    putExtra("team_name", team.team_name)
                    putExtra("college", team.college)
                    putExtra("city", team.city)
                    putExtra("state", team.state)
                    putParcelableArrayListExtra("members", ArrayList(team.members))
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = filteredTeams.size

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val teamName: TextView = itemView.findViewById(R.id.tv_teamName)
        val teamSize: TextView = itemView.findViewById(R.id.tv_teamSize)
    }

    fun updateData(newTeams: List<Team>) {
        teams.clear()
        teams.addAll(newTeams)
        filter("") // Reset the filter when data is updated
    }

    fun filter(query: String) {
        filteredTeams.clear()
        if (query.isEmpty()) {
            filteredTeams.addAll(teams)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            for (team in teams) {
                if (team.team_name.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    team.college.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredTeams.add(team)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun filterByCriteria(criteria: FilterCriteria) {
        filteredTeams.clear()
        when (criteria) {
            FilterCriteria.ALL -> filteredTeams.addAll(teams)
            FilterCriteria.PRESENT -> filteredTeams.addAll(teams.filter { it.isPresent == 1 })
            FilterCriteria.RECEIVED_KIT -> filteredTeams.addAll(teams.filter { it.receivedKit == 1 })
        }
        notifyDataSetChanged()
    }

    enum class FilterCriteria {
        ALL, PRESENT, RECEIVED_KIT
    }

}