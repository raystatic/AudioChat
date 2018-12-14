package com.example.rahul.audiochat.profile.model

/**
 * Created by rahul on 29/11/18.
 */
data class User(val name:String,
                val status:String,
                val phone: String,
                val profilePicture:String?){
    constructor(): this("","","",null)
}