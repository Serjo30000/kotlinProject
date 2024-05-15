package com.example.kotlin9.ui.registration

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kotlin9.R
import com.example.kotlin9.api.UserLibraryDto
import com.example.kotlin9.api.UserService
import com.example.kotlin9.databinding.FragmentRegistrationBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.sql.Date

class RegistrationFragment : Fragment(){
    private var _binding: FragmentRegistrationBinding? = null

    private val binding get() = _binding!!

    private var selectedImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uploadAvatar.setOnClickListener {
            choosePhotoFromGallery()
        }

        binding.createPhoto.setOnClickListener {
            takePhotoFromCamera()
        }

        binding.registrationUserButton.setOnClickListener {
            val name = binding.editTextPersonNameRegistration.text.toString()
            val surname = binding.editTextPersonSurnameRegistration.text.toString()
            val login = binding.editTextPersonLoginRegistration.text.toString()
            val password = binding.editTextPasswordRegistration.text.toString()
            val repeatPassword = binding.editTextRepeatPasswordRegistration.text.toString()
            val birthday = binding.editTextBirthdayRegistration.text.toString()

            if (name.isEmpty() || surname.isEmpty() || login.isEmpty() || password.isEmpty() || repeatPassword.isEmpty() || birthday.isEmpty()){
                showAlert("Пустые поля", "Пожалуйста введите все значения")
                return@setOnClickListener
            }

            if (!password.equals(repeatPassword)){
                showAlert("Не соответствие паролей", "Пожалуйста введите одинаковые пароли")
                return@setOnClickListener
            }

            if (!isValidDateFormat(birthday)){
                showAlert("Неверный формат даты", "Пожалуйста введите верно дату гггг/мм/дд")
                return@setOnClickListener
            }

            if (isFutureDate(birthday)){
                showAlert("Неверная дата", "Пожалуйста введите дату не из будущего")
                return@setOnClickListener
            }

            registerUser(name, surname, login, password, birthday) {avatar->
                println(avatar)
                if (selectedImage != null) {
                    uploadImageToFirebaseStorage(selectedImage!!,avatar)
                }
                clearFields()
                showAlert("Вы успешно зарегистрировались", "Теперь вы можете залогиниться")
            }

        }
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, avatar: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val imagesRef = storageRef.child("avatars/${avatar}.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnFailureListener {
        }.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val imageURL = uri.toString()
            }
        }
    }

    private fun takePhotoFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAMERA -> {
                    selectedImage = data?.extras?.get("data") as Bitmap
                }
                REQUEST_CODE_GALLERY -> {
                    val selectedImageUri = data?.data
                    selectedImage = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera()
                }
            }
            PERMISSION_CODE_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoFromGallery()
                }
            }
        }
    }

    private fun registerUser(name: String, surname: String, login: String, password: String, birthday: String, callback: (String) -> Unit) {

        val avatar: String = if (selectedImage != null) {
            UUID.randomUUID().toString()
        } else {
            "default"
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        val sqlDate = Date(dateFormat.parse(birthday).time)

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create(gson ))
            .build()

        val userService = retrofit.create(UserService::class.java)

        val user = UserLibraryDto(name, surname, login, password, sqlDate, avatar.toString() + ".jpg")

        val call = userService.saveUserLibraries(user)

        call.enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val statusCode: Int? = response.body()
                    if (statusCode == 0){
                        showAlert("Такой пользователь уже есть", "Пожалуйста придумайте другой логин")
                    }
                    else{
                        callback(avatar)
                    }
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                showAlert("Ошибка сервера", "Пожалуйста попробуйте снова")
            }
        })
    }

    private fun isValidDateFormat(dateString: String): Boolean {
        val regex = """\d{4}/\d{2}/\d{2}""".toRegex()
        return regex.matches(dateString)
    }

    private fun isFutureDate(dateString: String): Boolean {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val selectedDate = sdf.parse(dateString)
        return selectedDate?.after(currentDate) ?: false
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
        binding.editTextPersonNameRegistration.text = null
        binding.editTextPersonSurnameRegistration.text = null
        binding.editTextPersonLoginRegistration.text = null
        binding.editTextPasswordRegistration.text = null
        binding.editTextRepeatPasswordRegistration.text = null
        binding.editTextBirthdayRegistration.text = null
        selectedImage = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_CAMERA = 101
        private const val REQUEST_CODE_GALLERY = 102
        private const val PERMISSION_CODE_CAMERA = 201
        private const val PERMISSION_CODE_GALLERY = 202
    }
}