package com.mnvpatni.teamsync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.models.Member

class TeamMemberAdapter(private val members: List<Member>) : RecyclerView.Adapter<TeamMemberAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.memberNumber.text = (position + 1).toString()
        holder.name.text = member.name
        holder.ageGender.text = "${member.gender} - ${member.age}"
        holder.checkbox.visibility = View.GONE
    }

    override fun getItemCount(): Int = members.size

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberNumber: TextView = itemView.findViewById(R.id.tv_memberNumber)
        val name: TextView = itemView.findViewById(R.id.tv_memberName)
        val ageGender: TextView = itemView.findViewById(R.id.tv_memberGenderAge)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
    }
}
