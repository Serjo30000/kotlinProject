package com.example.kotlin9.ui.editpasswordpersonalarea

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditPasswordPersonalAreaViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = "This is edit password personal area Fragment"
    }
    val text: LiveData<String> = _text
}