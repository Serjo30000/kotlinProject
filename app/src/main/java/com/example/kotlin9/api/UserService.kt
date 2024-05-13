package com.example.kotlin9.api
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Param
import retrofit2.Call
import retrofit2.http.*
import java.sql.Date

data class UserLibraryDto(val name: String, val surname: String, val login: String, val password: String, val birthday: Date, val avatar: String)

data class AuthenticationDto(val login: String, val password: String)

data class CheckDto(val login: String, val role: String)

data class TokenDto(val token: String)

data class UserLibraryDtoOut(val name: String, val surname: String, val login: String, val role: String, val birthday: Date, val avatar: String)

interface UserService {
    @PUT("/api/userLibraries/save")
    fun saveUserLibraries(@Body aDto: UserLibraryDto): Call<Int>

    @POST("/api/auth/login")
    fun logIn(@Body aDto: AuthenticationDto): Call<TokenDto>

    @POST("/api/auth/check")
    fun check(@Body tokenUser: CheckDto): Call<TokenDto>

    @GET("/api/userLibraries/user/{login}")
    fun getByLoginUserLibrary(@Path("login") login: String): Call<UserLibraryDtoOut>

    @PUT("/api/userLibraries/updatePasswordByToken/{token}")
    fun updateUserPasswordByToken(@Path("token") token: String, @Query("password") password: String): Call<Int>

    @PUT("/api/userLibraries/updateNotPasswordByToken/{token}")
    fun updateUSerNotPasswordByToken(@Path("token") token: String, @Body aDto: UserLibraryDto): Call<Int>
}