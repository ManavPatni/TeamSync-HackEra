package com.mnvpatni.teamsync.network

import com.mnvpatni.teamsync.models.CommitteeMembersModel
import com.mnvpatni.teamsync.models.MealResponse
import com.mnvpatni.teamsync.models.MealUpdateRequest
import com.mnvpatni.teamsync.models.RestRoomResponse
import com.mnvpatni.teamsync.models.TeamModel
import com.mnvpatni.teamsync.models.UpdateAttendanceModel
import com.mnvpatni.teamsync.models.UpdateResponse
import com.mnvpatni.teamsync.models.UpdateRestRoomRequest
import com.mnvpatni.teamsync.models.UpdateRestRoomResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
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

    //Update meal info
    @POST("meals/update")
    fun updateMealRecords(@Body requestBody: MealUpdateRequest): Call<UpdateResponse>

    //rest room info
    @GET("rest-room/get")
    suspend fun getTeamRestRoomStatus(
        @Query("team_uid") teamUID: String
    ): RestRoomResponse

    //update rest room info
    @POST("rest-room/update")
    fun updateRestRoomDetails(@Body requestBody: UpdateRestRoomRequest): Call<UpdateRestRoomResponse>

    //get team info
    @GET("team/get")
    suspend fun getTeams(): Response<TeamModel>

    //team attendance
    @POST("team/attendance")
    suspend fun setAttendance(
        @Query("team_id") teamId: String,
        @Query("call_from") callFrom: String
    ): UpdateAttendanceModel

}