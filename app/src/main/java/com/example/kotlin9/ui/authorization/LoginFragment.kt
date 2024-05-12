package com.example.kotlin9.ui.authorization

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kotlin9.R
import com.example.kotlin9.api.AuthenticationDto
import com.example.kotlin9.api.TokenDto
import com.example.kotlin9.api.UserLibraryDto
import com.example.kotlin9.api.UserService
import com.example.kotlin9.databinding.FragmentLoginBinding
import com.example.kotlin9.databinding.FragmentMusicBinding
import com.example.kotlin9.ui.music.MusicViewModel
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class LoginFragment : Fragment(){
    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val login = binding.editTextTextPersonName.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            if (login.isEmpty() && password.isEmpty()){
                showAlert("Пустые поля", "Пожалуйста введите все значения")
                return@setOnClickListener
            }

            loginUser(login, password);
        }

        binding.registrationButton.setOnClickListener{
            findNavController().navigate(R.id.nav_registration)
        }
    }

    private fun loginUser(login: String, password: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val userService = retrofit.create(UserService::class.java)

        val user = AuthenticationDto(login, password)

        val call = userService.logIn(user)

        call.enqueue(object : Callback<TokenDto> {
            override fun onResponse(call: Call<TokenDto>, response: Response<TokenDto>) {
                if (response.isSuccessful) {
                    val statusCode: TokenDto? = response.body()
                    if (statusCode != null){
                        if (statusCode.token != ""){
                            val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("TOKEN_KEY", statusCode.token)
                            editor.apply()
                            println(sharedPreferences.getString("TOKEN_KEY", null))
                            findNavController().navigate(R.id.nav_personal_area)
                        }
                        else{
                            clearFields()
                            showAlert("Вы не вошли в аккаунт", "Пожалуйста введите заново поля")
                        }
                    }
                    else{
                        clearFields()
                        showAlert("Вы не вошли в аккаунт", "Пожалуйста введите заново поля")
                    }
                }
                else{
                    clearFields()
                    showAlert("Вы не вошли в аккаунт", "Пожалуйста введите заново поля")
                }
            }

            override fun onFailure(call: Call<TokenDto>, t: Throwable) {
                showAlert("Ошибка сервера", "Пожалуйста попробуйте снова")
            }
        })
    }

    private fun showAlert(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.create().show()
    }

    private fun clearFields() {
        binding.editTextTextPersonName.text = null
        binding.editTextTextPassword.text = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}