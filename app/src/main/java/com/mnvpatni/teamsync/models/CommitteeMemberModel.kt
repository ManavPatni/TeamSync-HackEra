package com.mnvpatni.teamsync.models

import android.os.Parcel
import android.os.Parcelable

// Data class representing the API response
data class CommitteeModel(
    val statusCode: Int,
    val body: List<CommitteeMemberModel>
)

// Data class representing a member of a committee
data class CommitteeMemberModel(
    val id: Int,
    val uid: String?,
    val full_name: String,
    val user_type: String,
    val post: String?,
    val access_to: String?,
    val status: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString() ?: "",  // Provide a default value in case it's null
        parcel.readString() ?: "",  // Provide a default value in case it's null
        parcel.readString(),        // No need for default value, as post is nullable
        parcel.readString(),        // No need for default value, as access_to is nullable
        parcel.readInt()            // Read the status as Int instead of String
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(uid)
        parcel.writeString(full_name)
        parcel.writeString(user_type)
        parcel.writeString(post)
        parcel.writeString(access_to)
        parcel.writeInt(status)  // Write the status as Int
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CommitteeMemberModel> {
        override fun createFromParcel(parcel: Parcel): CommitteeMemberModel {
            return CommitteeMemberModel(parcel)
        }

        override fun newArray(size: Int): Array<CommitteeMemberModel?> {
            return arrayOfNulls(size)
        }
    }
}
