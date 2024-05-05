package com.example.kotlin9.ui.personalarea

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PersonalAreaViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is personal area Fragment"
    }
    val text: LiveData<String> = _text
}