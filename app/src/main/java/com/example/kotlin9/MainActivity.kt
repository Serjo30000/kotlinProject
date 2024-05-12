package com.example.kotlin9

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlin9.api.UserLibraryDtoOut
import com.example.kotlin9.api.UserService
import com.example.kotlin9.databinding.ActivityMainBinding
import com.example.kotlin9.navigation.NavigationSecurity.Companion.decodedToken
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_personal_area, R.id.nav_weather, R.id.nav_music
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val loginHeader = findViewById<TextView>(R.id.LoginHeader)
        val imageView = findViewById<ImageView>(R.id.logoImageView)
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""
        val login = decodedToken(tokenString)
        loginHeader.text = login

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val userService = retrofit.create(UserService::class.java)

        val call = userService.getByLoginUserLibrary(login)

        call.enqueue(object : Callback<UserLibraryDtoOut> {
            override fun onResponse(call: Call<UserLibraryDtoOut>, response: Response<UserLibraryDtoOut>) {
                if (response.isSuccessful) {
                    val statusCode: UserLibraryDtoOut? = response.body()
                    if (statusCode != null){
                        val imageDayFifthWeatherURL = "https://firebasestorage.googleapis.com/v0/b/kotlin9-a1336.appspot.com/o/avatars%2F" + statusCode.avatar + "?alt=media"
                        Picasso.get().load(imageDayFifthWeatherURL).into(imageView)
                    }
                }
            }

            override fun onFailure(call: Call<UserLibraryDtoOut>, t: Throwable) {
                println("Ошибка сервера")
            }
        })

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}