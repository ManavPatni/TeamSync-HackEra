package com.mnvpatni.teamsync.models

data class RestRoomResponse(
    val statusCode: Int,
    val body: RestRoomTeamDetails
)

data class RestRoomTeamDetails(
    val team_name: String,
    val college: String,
    val city: String,
    val state: String,
    val details: List<RestRoomTeamMember>
)

data class RestRoomTeamMember(
    val name: String,
    val gender: String,
    val age: Int,
    val role: String,
    val isInRestRoom: Int
)
