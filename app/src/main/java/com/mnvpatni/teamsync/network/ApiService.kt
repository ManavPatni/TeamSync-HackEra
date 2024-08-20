package com.mnvpatni.teamsync.network

import com.mnvpatni.teamsync.models.CommitteeMembersModel
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    //Verify committee members
    @POST("/verify/committee/member")
    suspend fun verifyCommitteeMember(
        @Query("uid") uid: String,
        @Query("user_type") userType: String
    ): CommitteeMembersModel

}