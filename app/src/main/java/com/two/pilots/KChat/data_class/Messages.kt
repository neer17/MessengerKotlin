package com.two.pilots.KChat.data_class

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
    data class Messages(val fromId: String = "", val receiverId: String ="", val message: String = "", val timestamp: Long = 0)