package com.mnvpatni.teamsync.models

// Data class for a single participant
data class Participant(
    val name: String,
    val attended: Int
)

// Data class for the meal update request
data class MealUpdateRequest(
    val team_uid: String,
    val date: String,
    val meal_type: String,
    val participants: List<Participant>
)

// Data class for the API response
data class UpdateResponse(
    val statusCode: Int,
    val body: String?
)