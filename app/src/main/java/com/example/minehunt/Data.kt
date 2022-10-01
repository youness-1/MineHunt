package com.example.minehunt

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.example.minehunt.fragments.minehunt.GameData

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPref:SharedPreferences
    val player : Player
    var game: GameData? = null
    init {
        sharedPref = application.getSharedPreferences("com.example.minehunt.ui.SETTINGS", Context.MODE_PRIVATE)
        player = Player(sharedPref)
    }
}