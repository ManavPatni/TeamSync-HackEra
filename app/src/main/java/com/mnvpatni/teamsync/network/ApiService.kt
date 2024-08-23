package com.mnvpatni.teamsync.network

import com.mnvpatni.teamsync.models.CommitteeMembersModel
import com.mnvpatni.teamsync.models.MealResponse
import com.mnvpatni.teamsync.models.RestRoomResponse
import com.mnvpatni.teamsync.models.TeamModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    //Verify committee members
    @POST("verify/committee/member")
    suspend fun verifyCommitteeMember(
        @Query("uid") uid: String,
        @Query("name") name: String,
        @Query("user_type") userType: String
    ): CommitteeMembersModel

    //get meal info
    @GET("meals/get")
    suspend fun getTeamMealStatus(
        @Query("team_uid") teamUID: String,
        @Query("date") date: String,
        @Query("meal_type") mealType: String
    ): MealResponse

    //rest room info
    @GET("rest-room/get")
    suspend fun getTeamRestRoomStatus(
        @Query("team_uid") teamUID: String
    ): RestRoomResponse

    @GET("team/get")
    suspend fun getTeams(): Response<TeamModel>

}