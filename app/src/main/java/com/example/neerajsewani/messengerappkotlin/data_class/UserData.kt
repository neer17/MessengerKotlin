package com.example.neerajsewani.messengerappkotlin.data_class

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserData(var username: String, var email: String, var imageURL: String): Parcelable