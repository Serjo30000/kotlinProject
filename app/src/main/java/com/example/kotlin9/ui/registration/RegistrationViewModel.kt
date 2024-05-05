package com.example.kotlin9.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrationViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = "This is registration Fragment"
    }
    val text: LiveData<String> = _text
}