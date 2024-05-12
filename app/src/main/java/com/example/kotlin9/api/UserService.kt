package com.example.kotlin9.api
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import java.sql.Date

data class UserLibraryDto(val name: String, val surname: String, val login: String, val password: String, val birthday: Date, val avatar: String)

data class AuthenticationDto(val login: String, val password: String)

data class TokenDto(val token: String)

interface UserService {
    @PUT("/api/userLibraries/save")
    fun saveUserLibraries(@Body aDto: UserLibraryDto): Call<Int>

    @POST("/api/auth/login")
    fun logIn(@Body aDto: AuthenticationDto): Call<TokenDto>
}