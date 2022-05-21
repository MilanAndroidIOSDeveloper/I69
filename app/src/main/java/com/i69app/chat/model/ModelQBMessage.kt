package com.i69app.chat.model


import com.google.gson.annotations.SerializedName

data class ModelQBMessage(
    @SerializedName("attachments")
    var attachments: List<Attachment>,
    @SerializedName("dateSent")
    var dateSent: Int,
    @SerializedName("delayed")
    var delayed: Boolean,
    @SerializedName("deliveredIds")
    var deliveredIds: List<Int>,
    @SerializedName("dialogId")
    var dialogId: String,
    @SerializedName("_id")
    var id: String,
    @SerializedName("markable")
    var markable: Boolean,
    @SerializedName("properties")
    var properties: Properties,
    @SerializedName("readIds")
    var readIds: List<Int>,
    @SerializedName("recipientId")
    var recipientId: Int,
    @SerializedName("saveToHistory")
    var saveToHistory: Boolean,
    @SerializedName("senderId")
    var senderId: Int
) {
    data class Attachment(
        @SerializedName("content-type")
        var contentType: String,
        @SerializedName("duration")
        var duration: Int,
        @SerializedName("height")
        var height: Int,
        @SerializedName("id")
        var id: String,
        @SerializedName("name")
        var name: String,
        @SerializedName("size")
        var size: Double,
        @SerializedName("type")
        var type: String,
        @SerializedName("width")
        var width: Int
    )

    data class Properties(
        @SerializedName("all_read")
        var allRead: String,
        @SerializedName("message")
        var message: String,
        @SerializedName("read")
        var read: String,
        @SerializedName("updated_at")
        var updatedAt: String
    )
}