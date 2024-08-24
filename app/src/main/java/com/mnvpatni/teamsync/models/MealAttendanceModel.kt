package com.mnvpatni.teamsync.models

data class MealAttendanceApiResponse(
    val message: String?,
    val error: String?
)

data class Participant(
    val name: String,
    val attended: Int = 0
)

data class MealAttendanceRequest(
    val team_uid: String,
    val date: String,
    val meal_type: String,
    val participants: List<Participant>
)