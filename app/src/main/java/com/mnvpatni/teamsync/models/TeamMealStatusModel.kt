package com.mnvpatni.teamsync.models

data class MealResponse(
    val statusCode: Int,
    val body: MealTeamDetails
)

data class MealTeamDetails(
    val team_name: String,
    val college: String,
    val city: String,
    val state: String,
    val details: List<MealTeamMember>
)

data class MealTeamMember(
    val name: String,
    val gender: String,
    val age: Int,
    val role: String,
    val attended: Int
)
