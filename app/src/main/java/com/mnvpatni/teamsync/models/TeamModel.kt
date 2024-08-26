package com.mnvpatni.teamsync.models

import android.os.Parcel
import android.os.Parcelable

// Data class representing the API response
data class TeamModel(
    val statusCode: Int,           // The status code of the API response
    val body: List<Team>           // A list of teams
)

// Data class representing a team
data class Team(
    val team_id: String,            // Team Id
    val team_name: String,         // The name of the team
    val college: String,           // The college associated with the team
    val city: String,              // The city where the team is located
    val state: String,             // The state where the team is located
    val members: List<Member>      // A list of members belonging to the team
)

// Data class representing a member of a team
data class Member(
    val name: String,              // The name of the team member
    val phone_number: String?,     // The phone number of the team member, nullable
    val email_id: String,          // The email ID of the team member
    val gender: String,            // The gender of the team member
    val age: Int,                  // The age of the team member
    val role: String               // The role of the team member within the team
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(phone_number)
        parcel.writeString(email_id)
        parcel.writeString(gender)
        parcel.writeInt(age)
        parcel.writeString(role)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Member> {
        override fun createFromParcel(parcel: Parcel): Member {
            return Member(parcel)
        }

        override fun newArray(size: Int): Array<Member?> {
            return arrayOfNulls(size)
        }
    }
}


