package com.mnvpatni.teamsync.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.TeamDetailsActivity
import com.mnvpatni.teamsync.admin.CommitteeMemberDetailsActivity
import com.mnvpatni.teamsync.models.CommitteeMemberModel

class CommitteeMembersAdapter(private val context: Context, private var members: List<CommitteeMemberModel>) : RecyclerView.Adapter<CommitteeMembersAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.memberNumber.text = (position + 1).toString()
        holder.name.text = member.full_name
        holder.ageGender.text = member.post ?: "N/A"
        holder.checkbox.visibility = View.GONE
        holder.itemView.setOnClickListener {
            val intent = Intent(context, CommitteeMemberDetailsActivity::class.java).apply {
                putExtra("uid", member.uid)
                putExtra("name", member.full_name)
                putExtra("type", member.user_type)
                putExtra("post", member.post)
                putExtra("access_to", member.access_to)
                putExtra("status", member.status)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = members.size

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberNumber: TextView = itemView.findViewById(R.id.tv_memberNumber)
        val name: TextView = itemView.findViewById(R.id.tv_memberName)
        val ageGender: TextView = itemView.findViewById(R.id.tv_memberGenderAge)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    fun updateData(newMembers: List<CommitteeMemberModel>) {
        members = newMembers
        notifyDataSetChanged() // Ensure the RecyclerView updates properly
    }
}
