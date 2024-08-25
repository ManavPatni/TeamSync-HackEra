package com.mnvpatni.teamsync.models

// Data class for a single participant
data class RestRoomParticipant(
    val name: String,
    val isInRestRoom: Int
)

// Data class for the meal update request
data class UpdateRestRoomRequest(
    val team_uid: String,
    val participants: List<RestRoomParticipant>
)

// Data class for the API response
data class UpdateRestRoomResponse(
    val statusCode: Int,
    val message: String?,
    val error: String?
)