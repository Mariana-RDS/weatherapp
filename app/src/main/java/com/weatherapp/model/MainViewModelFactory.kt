package com.weatherapp.model

import androidx.lifecycle.ViewModel
import com.weatherapp.db.fb.FBDatabase

class MainViewModelFactory(private val db: FBDatabase) :
    androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}