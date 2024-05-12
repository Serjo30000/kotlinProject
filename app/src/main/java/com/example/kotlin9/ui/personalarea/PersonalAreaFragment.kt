package com.example.kotlin9.ui.personalarea

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.kotlin9.databinding.FragmentPersonalAreaBinding
import java.util.*

class PersonalAreaFragment : Fragment() {

    private var _binding: FragmentPersonalAreaBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(PersonalAreaViewModel::class.java)

        _binding = FragmentPersonalAreaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPersonalArea
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        println(sharedPreferences.getString("TOKEN_KEY", null))

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val secretKey = "OLUV5b2mFXpasEyjUnijlG7B2wWt+cvkM/LnP7EZuX/OqUYDm9ZVlpSHWco8qk5oJ4FQQa5w0ghtzno5RpyB3A=="

        processToken(tokenString, secretKey)

        return root
    }

    fun processToken(token: String, secret: String) {
        try {
            val decodedJWT: DecodedJWT = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)

            val nbf: Date? = decodedJWT.notBefore

            val currentTime = Date()

            if (nbf != null && currentTime.before(nbf)) {
                println("Токен еще не действителен")
            } else {
                val login = decodedJWT.getClaim("login").asString()
                println(login)
            }
        } catch (e: TokenExpiredException) {
            println("Токен истек")
        } catch (e: Exception) {
            println("Ошибка обработки токена: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}