package com.example.kotlin9.ui.editpasswordpersonalarea

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kotlin9.R
import com.example.kotlin9.api.UserService
import com.example.kotlin9.databinding.FragmentEditPasswordPersonalAreaBinding
import com.example.kotlin9.navigation.NavigationSecurity
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Request

class EditPasswordPersonalAreaFragment : Fragment(){
    private var _binding: FragmentEditPasswordPersonalAreaBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val login = NavigationSecurity.decodedToken(tokenString)

        NavigationSecurity.checkNavigation(findNavController(), login)

        _binding = FragmentEditPasswordPersonalAreaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        binding.editUserButtonEditPPA.setOnClickListener {
            val password = binding.editTextPasswordEditPPA.text.toString()
            val repeatPassword = binding.editTextRepeatPasswordEditPPA.text.toString()

            if (password.isEmpty() || repeatPassword.isEmpty()){
                showAlert("Пустые поля", "Пожалуйста введите все значения")
                return@setOnClickListener
            }

            if (!password.equals(repeatPassword)){
                showAlert("Не соответствие паролей", "Пожалуйста введите одинаковые пароли")
                return@setOnClickListener
            }

            editPasswordUser(password) {
                clearFields()
                editor.putString("TOKEN_KEY", "")
                editor.apply()
                findNavController().navigate(R.id.nav_login)
            }

        }
    }

    private fun editPasswordUser(password: String, callback: () -> Unit) {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val interceptor = Interceptor { chain ->
            val originalRequest: Request = chain.request()
            val modifiedRequest: Request = originalRequest.newBuilder()
                .header("Authorization", "Bearer $tokenString")
                .build()
            chain.proceed(modifiedRequest)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient) // Устанавливаем наш OkHttpClient с интерцептором
            .build()

        val userService = retrofit.create(UserService::class.java)

        val call = userService.updateUserPasswordByToken(tokenString, password)

        call.enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val statusCode: Int? = response.body()
                    if (statusCode == 1){
                        callback()
                    }
                    else{
                        clearFields()
                        showAlert("Вы неправильно заполнили поля", "Пожалуйста заполните заново")
                    }
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
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
        binding.editTextPasswordEditPPA.text = null
        binding.editTextRepeatPasswordEditPPA.text = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}