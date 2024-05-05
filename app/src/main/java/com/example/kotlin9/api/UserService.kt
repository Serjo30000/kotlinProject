package com.example.kotlin9.api
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT
import java.sql.Date

data class UserLibraryDto(val name: String, val surname: String, val login: String, val password: String, val birthday: Date, val avatar: String)

interface UserService {
    @PUT("/api/userLibraries/save")
    fun saveUserLibraries(@Body aDto: UserLibraryDto): Call<Int>
}