package com.tsumutaku.shiranapp.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class friendsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "ただいま工事中"
    }
    val text: LiveData<String> = _text
}