package com.i69app.chat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.i69app.data.models.Photo
import com.i69app.profile.db.converters.UserConverters

@Entity(tableName = "chat_dialogs")
data class ModelQBChatDialogs(
    @SerializedName("created_at")
    var createdAt: String,
    @PrimaryKey
    @SerializedName("_id")
    var id: String,
    @SerializedName("last_message")
    var lastMessage: String?,
    @SerializedName("last_message_date_sent")
    var lastMessageDateSent: Int,
    @SerializedName("last_message_user_id")
    var lastMessageUserId: Int,
    @SerializedName("name")
    var name: String,
    @SerializedName("occupants_ids")
    var occupantsIds: MutableList<Int>,
    @SerializedName("type")
    var type: Int,
    @SerializedName("unread_messages_count")
    var unreadMessagesCount: Int,
    @SerializedName("updated_at")
    var updatedAt: String,
    @SerializedName("user_id")
    var userId: Int,
    @SerializedName("local_user_id")
    var localUserId: String?,
    @SerializedName("local_user_image")
    @TypeConverters(UserConverters::class)
    var localUserImages: MutableList<Photo>?,
    @SerializedName("local_user_name")
    var userFullName: String?
)