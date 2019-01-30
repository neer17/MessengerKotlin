package com.two.pilots.KChat.data_class

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Users(var userId:String, var username: String, var email: String, var imageURL: String): Parcelable{
    constructor(): this("", "", "", "")
}