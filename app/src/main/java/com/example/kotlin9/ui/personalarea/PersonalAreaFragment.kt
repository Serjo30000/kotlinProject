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
import androidx.navigation.fragment.findNavController
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.kotlin9.databinding.FragmentPersonalAreaBinding
import com.example.kotlin9.novigation.NavigationSecurity.Companion.checkNavigation
import com.example.kotlin9.novigation.NavigationSecurity.Companion.decodedToken
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

        val homeViewModel =
            ViewModelProvider(this).get(PersonalAreaViewModel::class.java)

        _binding = FragmentPersonalAreaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPersonalArea
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}