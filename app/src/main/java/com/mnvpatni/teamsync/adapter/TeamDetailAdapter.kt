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

class TeamDetailAdapter(
    private var teams: MutableList<Team> = mutableListOf(),
    private val context: Context
) : RecyclerView.Adapter<TeamDetailAdapter.ParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.team_card, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val team = teams[position]
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

    override fun getItemCount(): Int = teams.size

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val teamName: TextView = itemView.findViewById(R.id.tv_teamName)
        val teamSize: TextView = itemView.findViewById(R.id.tv_teamSize)
    }

    fun updateData(newTeams: List<Team>) {
        teams.clear()
        teams.addAll(newTeams)
        notifyDataSetChanged() // Ensure the RecyclerView updates properly
    }
}
