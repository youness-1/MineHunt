package com.example.minehunt

import android.content.SharedPreferences

enum class Skin(val image: Int) {
    PLAYER1(R.drawable.player1),
    PLAYER2(R.drawable.player2),
    PLAYER3(R.drawable.player3),
    PLAYER4(R.drawable.player4),
    PLAYER5(R.drawable.player5),
    PLAYER6(R.drawable.player6),
    UNDEAD(R.drawable.undead)
}

class Player(sharedPref: SharedPreferences){
    var name = "NewPlayer"
        set(value) {
            if (value=="addscore"){
                score+=100
                return
            }
            if (value.isNotEmpty() && value!="NewPlayer") {
                field = value
                with (sharedPref.edit()) {
                    putString("playerName", name)
                    commit()
                }
            }
        }
    var sprite = Skin.PLAYER1
        set(value) {
            field=value
            with (sharedPref.edit()) {
                putString("playerSprite", value.name)
                commit()
            }
        }
        get() {
            return if (undead)
                Skin.UNDEAD
            else
                field
        }
    var undead=false
    var score = 0
        set(value) {
            field=value
            with (sharedPref.edit()) {
                putInt("playerScore", value)
                commit()
            }
        }
    var wins = 0
        set(value) {
            field=value
            with (sharedPref.edit()) {
                putInt("playerWins", value)
                commit()
            }
        }
    var looses = 0
        set(value) {
            field=value
            with (sharedPref.edit()) {
                putInt("playerLooses", value)
                commit()
            }
        }
    private val sharedPref: SharedPreferences
    init {
        this.sharedPref= sharedPref
        name = sharedPref.getString("playerName", "NewPlayer").toString()
        sprite = Skin.valueOf(sharedPref.getString("playerSprite", "PLAYER1").toString())
        score = sharedPref.getInt("playerScore", 0)
        wins = sharedPref.getInt("playerWins", 0)
        looses = sharedPref.getInt("playerLooses", 0)
    }

    override fun toString(): String {
        return "$name | score: $score wins: $wins looses $looses"
    }
}
