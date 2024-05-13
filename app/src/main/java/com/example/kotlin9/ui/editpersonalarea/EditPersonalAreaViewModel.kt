package com.example.kotlin9.ui.editpersonalarea

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditPersonalAreaViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = "This is edit personal area Fragment"
    }
    val text: LiveData<String> = _text
}