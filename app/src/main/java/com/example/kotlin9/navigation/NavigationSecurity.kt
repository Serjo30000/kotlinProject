package com.example.kotlin9.navigation

import androidx.navigation.NavController
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.kotlin9.R
import com.example.kotlin9.api.CheckDto
import com.example.kotlin9.api.TokenDto
import com.example.kotlin9.api.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NavigationSecurity {
    companion object {
        fun decodedToken(token: String) : String {
            try {
                val decodedJWT: DecodedJWT = JWT.decode(token)
                val login = decodedJWT.getClaim("login").asString()
                return login
            } catch (e: Exception) {
                return ""
            }
        }

        fun checkNavigation(navController: NavController, login: String) {
            try {
                if (login == null || login.equals("")){
                    navController.navigate(R.id.nav_login)
                }

                val retrofit = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val userService = retrofit.create(UserService::class.java)

                val user = CheckDto(login, "")

                val call = userService.check(user)

                call.enqueue(object : Callback<TokenDto> {
                    override fun onResponse(call: Call<TokenDto>, response: Response<TokenDto>) {
                        if (response.isSuccessful) {
                            val statusCode: TokenDto? = response.body()
                            if (statusCode == null || statusCode.token == ""){
                                navController.navigate(R.id.nav_login)
                            }
                        }
                        else{
                            navController.navigate(R.id.nav_login)
                        }
                    }

                    override fun onFailure(call: Call<TokenDto>, t: Throwable) {
                        navController.navigate(R.id.nav_login)
                        println("Ошибка сервера")
                    }
                })
            } catch (e: Exception) {
                println("Ошибка обработки токена: ${e.message}")
            }
        }
    }

}