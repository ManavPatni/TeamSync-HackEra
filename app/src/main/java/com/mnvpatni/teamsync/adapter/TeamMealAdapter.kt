package com.mnvpatni.teamsync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.models.MealTeamMember

class TeamMealAdapter(private var participants: MutableList<MealTeamMember> = mutableListOf()) :
    RecyclerView.Adapter<TeamMealAdapter.ParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = participants[position]
        with(holder) {
            memberNumTextView.text = (position + 1).toString()
            nameTextView.text = participant.name
            genderAgeTextView.text = "${participant.gender} - ${participant.age}"
            attendedCheckBox.isChecked = participant.attended == 1

            // Set listener to update participant's attended status
            attendedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                participants[position] = participant.copy(attended = if (isChecked) 1 else 0)
            }
        }
    }

    override fun getItemCount(): Int = participants.size

    fun getParticipants(): List<MealTeamMember> = participants

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberNumTextView: TextView = itemView.findViewById(R.id.tv_memberNumber)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_memberName)
        val genderAgeTextView: TextView = itemView.findViewById(R.id.tv_memberGenderAge)
        val attendedCheckBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    fun updateData(newParticipants: List<MealTeamMember>) {
        // Update data and notify changes in a more efficient way
        participants.clear()
        participants.addAll(newParticipants)
        notifyDataSetChanged()
    }
}