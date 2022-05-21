package com.i69app.data.models

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.*

open class ChatMessage(private val user: User?, private val timeStampMillis: Long, private val msg: String) : IMessage {

    companion object {
        private var counter = 0
    }

    override fun getId() = (counter++).toString()

    override fun getCreatedAt(): Date {
        return Date().apply {
            time = timeStampMillis
        }
    }

    override fun getText(): String = msg

    override fun getUser(): IUser = object : IUser {

        override fun getAvatar(): String = user?.avatarPhotos!!.get(user!!.avatarIndex!!).url ?: ""

        override fun getName(): String? = user?.fullName

        override fun getId(): String? = user?.id

    }
}