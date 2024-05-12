package com.example.kotlin9.ui.music

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin9.R
import com.example.kotlin9.databinding.FragmentMusicBinding
import com.example.kotlin9.novigation.NavigationSecurity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MusicFragment : Fragment() {

    private var _binding: FragmentMusicBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: AudioFilesAdapter

    val storage = FirebaseStorage.getInstance()

    val storageRef = storage.reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val login = NavigationSecurity.decodedToken(tokenString)

        NavigationSecurity.checkNavigation(findNavController(), login)

        _binding = FragmentMusicBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AudioFilesAdapter()
        recyclerView.adapter = adapter

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAllAudioFiles()
    }

    private fun getAllAudioFiles() {
        val rootRef = storageRef.child("musics")

        rootRef.listAll()
            .addOnSuccessListener { listResult ->
                val audioFilesList = mutableListOf<StorageReference>()

                listResult.items.forEach { item ->
                    if (item.name.endsWith(".mp3") || item.name.endsWith(".wav") || item.name.endsWith(".ogg")) {
                        audioFilesList.add(item)
                    }
                }
                adapter.setItems(audioFilesList)
            }

            .addOnFailureListener { exception ->
                showAlert("Ошибка сервера", "Пожалуйста попробуйте снова")
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter.stopMusic()
    }
}