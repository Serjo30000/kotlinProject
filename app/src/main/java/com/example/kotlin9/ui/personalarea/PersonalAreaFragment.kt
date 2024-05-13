package com.example.kotlin9.ui.personalarea

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kotlin9.R
import com.example.kotlin9.api.UserLibraryDtoOut
import com.example.kotlin9.api.UserService
import com.example.kotlin9.databinding.FragmentPersonalAreaBinding
import com.example.kotlin9.navigation.NavigationSecurity.Companion.checkNavigation
import com.example.kotlin9.navigation.NavigationSecurity.Companion.decodedToken
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class PersonalAreaFragment : Fragment() {

    private var _binding: FragmentPersonalAreaBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val login = decodedToken(tokenString)

        checkNavigation(findNavController(),login)

        _binding = FragmentPersonalAreaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val loginTextViewPersoanlArea : TextView = binding.loginTextViewPersoanlArea
        val birthdayTextViewPersonalArea : TextView = binding.birthdayTextViewPersonalArea
        val surnameTextViewPersonalArea : TextView = binding.surnameTextViewPersonalArea
        val nameTextViewPersonalArea : TextView = binding.nameTextViewPersonalArea
        val logoImageViewPersonalArea : ImageView = binding.logoImageViewPersonalArea

        val dateFormat = SimpleDateFormat("yyyy/MM/dd")

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
                        loginTextViewPersoanlArea.text = "Логин: " + statusCode.login
                        birthdayTextViewPersonalArea.text = "День рождения: " + dateFormat.format(statusCode.birthday)
                        surnameTextViewPersonalArea.text = "Фамилия: " + statusCode.surname
                        nameTextViewPersonalArea.text = "Имя: " + statusCode.name
                        val imageDayFifthWeatherURL = "https://firebasestorage.googleapis.com/v0/b/kotlin9-a1336.appspot.com/o/avatars%2F" + statusCode.avatar + "?alt=media"
                        Picasso.get().load(imageDayFifthWeatherURL).into(logoImageViewPersonalArea)
                    }
                }
            }

            override fun onFailure(call: Call<UserLibraryDtoOut>, t: Throwable) {
                println("Ошибка сервера")
            }
        })


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        binding.buttonEditPersonalArea.setOnClickListener {
            findNavController().navigate(R.id.nav_edit_personal_area)
        }

        binding.buttonEditPasswordPersonalArea.setOnClickListener {
            findNavController().navigate(R.id.nav_edit_password_personal_area)
        }

        binding.buttonLogOutPersonalArea.setOnClickListener{
            editor.putString("TOKEN_KEY", "")
            editor.apply()
            findNavController().navigate(R.id.nav_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}