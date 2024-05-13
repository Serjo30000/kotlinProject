package com.example.kotlin9.ui.editpersonalarea

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kotlin9.R
import com.example.kotlin9.api.UserLibraryDto
import com.example.kotlin9.api.UserLibraryDtoOut
import com.example.kotlin9.api.UserService
import com.example.kotlin9.databinding.FragmentEditPersonalAreaBinding
import com.example.kotlin9.navigation.NavigationSecurity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class EditPersonalAreaFragment : Fragment(){
    private var _binding: FragmentEditPersonalAreaBinding? = null

    private val binding get() = _binding!!

    private var selectedImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val login = NavigationSecurity.decodedToken(tokenString)

        NavigationSecurity.checkNavigation(findNavController(), login)

        _binding = FragmentEditPersonalAreaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val editTextPersonLoginEditPA : TextView = binding.editTextPersonLoginEditPA
        val editTextPersonNameEditPA : TextView = binding.editTextPersonNameEditPA
        val editTextPersonSurnameEditPA : TextView = binding.editTextPersonSurnameEditPA
        val editTextBirthdayEditPA : TextView = binding.editTextBirthdayEditPA
        val textViewNameImageEditPA : TextView = binding.textViewNameImageEditPA

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
                        editTextPersonLoginEditPA.text = statusCode.login
                        editTextBirthdayEditPA.text = dateFormat.format(statusCode.birthday)
                        editTextPersonSurnameEditPA.text = statusCode.surname
                        editTextPersonNameEditPA.text = statusCode.name
                        textViewNameImageEditPA.text = statusCode.avatar
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

        binding.uploadAvatarEditPA.setOnClickListener {
            choosePhotoFromGallery()
        }

        binding.createPhotoEditPA.setOnClickListener {
            takePhotoFromCamera()
        }

        binding.editUserButtonEditPA.setOnClickListener {
            val name = binding.editTextPersonNameEditPA.text.toString()
            val surname = binding.editTextPersonSurnameEditPA.text.toString()
            val login = binding.editTextPersonLoginEditPA.text.toString()
            val birthday = binding.editTextBirthdayEditPA.text.toString()

            if (name.isEmpty() || surname.isEmpty() || login.isEmpty() || birthday.isEmpty()){
                showAlert("Пустые поля", "Пожалуйста введите все значения")
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

            editNotPasswordUser(name, surname, login, birthday) {avatar->
                if (selectedImage != null) {
                    if (!binding.textViewNameImageEditPA.text.toString().equals("default.jpg")){
                        deleteImageToFirebaseStorage(binding.textViewNameImageEditPA.text.toString())
                    }
                    uploadImageToFirebaseStorage(selectedImage!!,avatar)
                }
                clearFields()
                editor.putString("TOKEN_KEY", "")
                editor.apply()
                findNavController().navigate(R.id.nav_login)
            }

        }
    }

    private  fun deleteImageToFirebaseStorage(avatar: String){
        val storage = Firebase.storage
        val storageRef = storage.reference

        val imagesRef = storageRef.child("avatars/${avatar}")

        imagesRef.delete()
            .addOnSuccessListener {
                println("Изображение успешно удалено.")
            }
            .addOnFailureListener { exception ->
                println("Ошибка при удалении изображения: $exception")
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

    private fun editNotPasswordUser(name: String, surname: String, login: String, birthday: String, callback: (String) -> Unit) {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val avatar: String = if (selectedImage != null) {
            UUID.randomUUID().toString()
        } else {
            binding.textViewNameImageEditPA.text.toString()
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        val sqlDate = Date(dateFormat.parse(birthday).time)

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
            .client(okHttpClient)
            .build()

        val userService = retrofit.create(UserService::class.java)

        var avatarImage = avatar

        if (avatarImage.equals(binding.textViewNameImageEditPA.text.toString())){
            avatarImage = ""
        }
        else{
            avatarImage = avatar + ".jpg"
        }

        val user = UserLibraryDto(name, surname, login, "", sqlDate, avatarImage)

        val call = userService.updateUSerNotPasswordByToken(tokenString, user)

        call.enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val statusCode: Int? = response.body()
                    if (statusCode == 1){
                        callback(avatar)
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
        binding.editTextPersonLoginEditPA.text = null
        binding.editTextPersonNameEditPA.text = null
        binding.editTextPersonSurnameEditPA.text = null
        binding.editTextBirthdayEditPA.text = null
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